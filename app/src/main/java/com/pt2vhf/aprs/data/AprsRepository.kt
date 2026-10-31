package com.pt2vhf.aprs.data

import android.content.Context
import com.pt2vhf.aprs.core.AprsMessage
import com.pt2vhf.aprs.core.AprsPacket
import com.pt2vhf.aprs.core.parseMessage
import com.pt2vhf.aprs.core.parsePosition
import com.pt2vhf.aprs.core.localRfEvidenceLinks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class Medium(val label: String) { RF("RF"), INTERNET("APRS-IS") }

data class Station(
    val call: String,
    val lat: Double,
    val lon: Double,
    val symbolTable: Char,
    val symbol: Char,
    val comment: String,
    val lastHeard: Long,
    val medium: Medium,
    val via: String,
    val speedKmh: Double?,
    val course: Int?,
    val heardBy: Set<String> = emptySet(),
    val firstSeen: Long = lastHeard,
) {
    val role: String
        get() = when {
            symbol == '#' -> "Digipeater"
            symbol == '&' || (symbolTable == 'I' || symbolTable == 'R') -> "iGate"
            symbol == '_' -> "Estação meteorológica"
            symbol in setOf('>', 'k', 'u', 'v', 'j', '<', 'b', '[') -> "Móvel"
            else -> "Estação"
        }
}

data class RfLink(val from: String, val to: String, val lastSeen: Long)

data class TrackPoint(val lat: Double, val lon: Double, val recordedAt: Long)

data class ChatMessage(
    val id: Long,
    val from: String,
    val to: String,
    val text: String,
    val time: Long,
    val outgoing: Boolean,
    val medium: Medium?,
    val via: String = "",
    val msgId: String? = null,
    val status: String = "",
    val kind: AprsMessage.Kind = AprsMessage.Kind.MESSAGE,
)

/** Result of an explicit send request; a socket write is not an APRS message ACK. */
data class OutboundSendResult(val requestId: String, val submitted: Boolean, val reason: String = "")

data class LinkStatus(
    val aprsIsConnected: Boolean = false,
    val aprsIsVerified: Boolean = false,
    val aprsIsServer: String = "",
    val aprsIsError: String = "",
    val tncConnected: Boolean = false,
    val tncName: String = "",
    val tncError: String = "",
    val gpsAccuracyM: Float? = null,
    val gpsFixTimeMs: Long = 0L,
    val lat: Double? = null,
    val lon: Double? = null,
    val speedKmh: Double = 0.0,
    val course: Int? = null,
    val trackerOn: Boolean = false,
    val beaconsSent: Int = 0,
    val beaconsDigipeated: Int = 0,
    val nextBeaconSec: Long? = null,
    val rxPackets: Int = 0,
    val aprsRx: Int = 0,
    val aprsTx: Int = 0,
    val tncRx: Int = 0,
    val tncTx: Int = 0,
)

/** Estado do app compartilhado entre o serviço e a interface. */
object AprsRepository {
    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()
    private val _followCall = MutableStateFlow<String?>(null)
    val followCall: StateFlow<String?> = _followCall.asStateFlow()
    private var preferences: android.content.SharedPreferences? = null

    private val _stations = MutableStateFlow<Map<String, Station>>(emptyMap())
    val stations: StateFlow<Map<String, Station>> = _stations.asStateFlow()

    private val _links = MutableStateFlow<List<RfLink>>(emptyList())
    val links: StateFlow<List<RfLink>> = _links.asStateFlow()

    private val _track = MutableStateFlow<List<TrackPoint>>(emptyList())
    val track: StateFlow<List<TrackPoint>> = _track.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _status = MutableStateFlow(LinkStatus())
    val status: StateFlow<LinkStatus> = _status.asStateFlow()

    private val _monitor = MutableStateFlow<List<String>>(emptyList())
    val monitor: StateFlow<List<String>> = _monitor.asStateFlow()

    // Keep results per request so rapid navigation or two compositors cannot lose an acknowledgement.
    private val _sendResults = MutableStateFlow<Map<String, OutboundSendResult>>(emptyMap())
    val sendResults: StateFlow<Map<String, OutboundSendResult>> = _sendResults.asStateFlow()

    fun reportSendResult(result: OutboundSendResult) {
        _sendResults.update { (it + (result.requestId to result)).entries.toList().takeLast(64).associate { e -> e.key to e.value } }
    }

    private var nextId = System.currentTimeMillis()
    private var file: File? = null
    private var stationStore: StationStore? = null
    private val diskWorker = Executors.newSingleThreadExecutor { r -> Thread(r, "aprs-station-db").apply { isDaemon = true } }
    private val stationCacheEpoch = AtomicLong(0L)
    private const val RETENTION_MS = 30L * 24 * 60 * 60 * 1000

    @Synchronized fun init(context: Context) {
        if (file != null) return
        preferences = context.applicationContext.getSharedPreferences("pt2vhf_aprs", Context.MODE_PRIVATE)
        _favorites.value = preferences?.getStringSet("favorite_stations", emptySet())?.toSet().orEmpty()
        file = File(context.filesDir, "messages.json")
        stationStore = StationStore(context.applicationContext)
        runCatching { load() }
        val restoreEpoch = stationCacheEpoch.get()
        diskWorker.execute {
            runCatching {
                val store = stationStore ?: return@execute
                val since = System.currentTimeMillis() - RETENTION_MS
                store.prune(since)
                val previousStations = store.readStations(since)
                val previousLinks = store.readLinks(since)
                val previousTrack = store.readTrack(System.currentTimeMillis() - 7L * 86400000)
                // A packet received during restore must never be replaced by older cache data.
                _stations.update { live ->
                    if (restoreEpoch != stationCacheEpoch.get()) return@update live
                    val combined = previousStations.toMutableMap()
                    for ((call, fresh) in live) {
                        if ((combined[call]?.lastHeard ?: 0L) <= fresh.lastHeard) combined[call] = fresh
                    }
                    combined
                }
                _links.update { current ->
                    if (restoreEpoch != stationCacheEpoch.get()) return@update current
                    val combined = (previousLinks + current).groupBy { it.from to it.to }
                    combined.values.map { list -> list.maxBy { it.lastSeen } }.takeLast(2000)
                }
                _track.update { current ->
                    (previousTrack + current).distinctBy { it.recordedAt }.sortedBy { it.recordedAt }.takeLast(10000)
                }
            }
        }
    }

    fun toggleFavorite(call: String) {
        _favorites.update { current -> if (call in current) current - call else current + call }
        preferences?.edit()?.putStringSet("favorite_stations", _favorites.value.toSet())?.apply()
    }

    fun followStation(call: String?) { _followCall.value = call }

    fun recordTrack(lat: Double, lon: Double, whenMs: Long = System.currentTimeMillis()) {
        val p = TrackPoint(lat, lon, whenMs)
        val last = _track.value.lastOrNull()
        if (last != null && whenMs - last.recordedAt < 15_000L &&
            com.pt2vhf.aprs.core.haversineKm(last.lat, last.lon, lat, lon) < 0.01) return
        _track.update { (it + p).takeLast(10000) }
        diskWorker.execute { runCatching { stationStore?.saveTrack(p) } }
    }

    fun clearTrack() {
        _track.value = emptyList()
        diskWorker.execute { runCatching { stationStore?.clearTrack() } }
    }

    suspend fun clearStations(): Boolean {
        val store = stationStore ?: return false
        stationCacheEpoch.incrementAndGet()
        _stations.value = emptyMap()
        _links.value = emptyList()
        _followCall.value = null
        return withContext(Dispatchers.IO) {
            runCatching { diskWorker.submit<Boolean> { store.clearStationsAndLinks(); true }.get() }
                .getOrDefault(false)
        }
    }

    @Synchronized fun clearMessages(): Boolean {
        val f = file ?: return false
        return runCatching {
            f.writeText("[]")
            _messages.value = emptyList()
            true
        }.getOrDefault(false)
    }

    fun containsMessage(id: Long): Boolean = _messages.value.any { it.id == id }

    fun updateStatus(change: (LinkStatus) -> LinkStatus) = _status.update(change)

    fun monitorLine(line: String) = _monitor.update { (it + line).takeLast(200) }

    /** Processa um pacote recebido; devolve a mensagem endereçada a nós, se houver. */
    fun ingest(packet: AprsPacket, medium: Medium, ownCall: String, now: Long = System.currentTimeMillis()): ChatMessage? {
        _status.update { it.copy(rxPackets = it.rxPackets + 1) }
        val digis = packet.rfDigipeaters
        // A trilha de um pacote APRS-IS descreve um caminho anunciado, não uma recepção RF
        // feita pelo telefone. Somente o TNC físico pode registrar evidência RF local.
        val observedRfPath = localRfEvidenceLinks(packet, medium == Medium.RF)
        if (observedRfPath.isNotEmpty()) {
            val newLinks = observedRfPath.map { (a, b) -> RfLink(a, b, now) }
            _links.update { old ->
                (old.filter { l -> newLinks.none { it.from == l.from && it.to == l.to } } + newLinks).takeLast(2000)
            }
            diskWorker.execute { runCatching { stationStore?.saveLinks(newLinks) } }
            if (packet.source == ownCall) _status.update { it.copy(beaconsDigipeated = it.beaconsDigipeated + 1) }
        }
        parsePosition(packet.info)?.let { pos ->
            var persisted: Station? = null
            _stations.update { map ->
                val existing = map[packet.source]
                val heardBy = (existing?.heardBy ?: emptySet()) +
                    (if (medium == Medium.RF) digis.take(1) else emptyList())
                val station = Station(
                    packet.source, pos.lat, pos.lon, pos.symbolTable, pos.symbol, pos.comment, now, medium,
                    packet.path.joinToString(","), pos.speedKmh, pos.course, heardBy,
                    firstSeen = existing?.firstSeen ?: now,
                )
                persisted = station
                map + (packet.source to station)
            }
            persisted?.let { station -> diskWorker.execute { runCatching { stationStore?.saveStation(station) } } }
        } ?: run {
            // A station can send a message/status without new coordinates.
            // Keep its last known position but refresh reception metadata.
            // Unknown stations without position are not plotted at (0,0).
            var refreshed: Station? = null
            _stations.update { existing ->
                val previous = existing[packet.source]
                if (previous == null || now <= previous.lastHeard) existing
                else {
                    previous.copy(lastHeard = now, medium = medium,
                        via = packet.path.joinToString(",")).let {
                        refreshed = it
                        existing + (packet.source to it)
                    }
                }
            }
            refreshed?.let { station ->
                diskWorker.execute { runCatching { stationStore?.saveStation(station) } }
            }
        }
        val msg = parseMessage(packet.info) ?: return null
        return when (msg.kind) {
            AprsMessage.Kind.ACK, AprsMessage.Kind.REJ -> {
                if (msg.addressee == ownCall) markAck(packet.source, msg.id, msg.kind == AprsMessage.Kind.ACK)
                null
            }
            else -> {
                val forUs = msg.addressee == ownCall
                val general = msg.kind == AprsMessage.Kind.BULLETIN || msg.addressee == "CQ" ||
                    msg.addressee.startsWith("CQ")
                if (!forUs && !general) return null
                val via = if (medium == Medium.RF)
                    packet.rfDigipeaters.firstOrNull()?.let { "RF via $it" } ?: "RF direto"
                else "APRS-IS"
                val duplicate = _messages.value.any {
                    !it.outgoing && it.from == packet.source && it.msgId != null && it.msgId == msg.id && it.text == msg.text
                }
                if (duplicate) return null
                val chat = ChatMessage(nextId++, packet.source, msg.addressee, msg.text, now, false, medium, via, msg.id,
                    "Recebida", msg.kind)
                add(chat)
                if (forUs) chat else null
            }
        }
    }

    fun add(message: ChatMessage) {
        _messages.update { (it + message).takeLast(1000) }
        runCatching { save() }
    }

    fun newId(): Long = nextId++

    fun setStatus(id: Long, status: String) {
        _messages.update { list -> list.map { if (it.id == id) it.copy(status = status) else it } }
        runCatching { save() }
    }

    private fun markAck(from: String, msgId: String?, ack: Boolean) {
        if (msgId == null) return
        _messages.update { list ->
            list.map {
                if (it.outgoing && it.to == from && it.msgId == msgId) it.copy(status = if (ack) "ACK recebido" else "Rejeitada")
                else it
            }
        }
        runCatching { save() }
    }

    fun isAcked(localId: Long): Boolean =
        _messages.value.firstOrNull { it.id == localId }?.status?.let { it == "ACK recebido" || it == "Rejeitada" } == true

    @Synchronized private fun save() {
        val f = file ?: return
        val arr = JSONArray()
        for (m in _messages.value.takeLast(500)) {
            arr.put(JSONObject().put("id", m.id).put("from", m.from).put("to", m.to).put("text", m.text)
                .put("time", m.time).put("out", m.outgoing).put("medium", m.medium?.name ?: "")
                .put("via", m.via).put("msgId", m.msgId ?: "").put("status", m.status).put("kind", m.kind.name))
        }
        f.writeText(arr.toString())
    }

    private fun load() {
        val f = file ?: return
        if (!f.exists()) return
        val arr = JSONArray(f.readText())
        val list = (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            ChatMessage(o.getLong("id"), o.getString("from"), o.getString("to"), o.getString("text"),
                o.getLong("time"), o.getBoolean("out"),
                o.optString("medium").takeIf { it.isNotEmpty() }?.let { Medium.valueOf(it) },
                o.optString("via"), o.optString("msgId").ifEmpty { null }, o.optString("status"),
                runCatching { AprsMessage.Kind.valueOf(o.optString("kind")) }.getOrDefault(AprsMessage.Kind.MESSAGE))
        }
        _messages.value = list
        nextId = maxOf(nextId, (list.maxOfOrNull { it.id } ?: 0) + 1)
    }
}
