package com.pt2vhf.aprs.data

import android.content.Context
import com.pt2vhf.aprs.core.AprsMessage
import com.pt2vhf.aprs.core.AprsPacket
import com.pt2vhf.aprs.core.decodeAprsText
import com.pt2vhf.aprs.core.haversineKm
import com.pt2vhf.aprs.core.localRfEvidenceLinks
import com.pt2vhf.aprs.core.parseMessage
import com.pt2vhf.aprs.core.parsePosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

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

    /** Live trail of each station received in this session (#11); not restored from cache. */
    private val _trails = MutableStateFlow<Map<String, List<TrackPoint>>>(emptyMap())
    val trails: StateFlow<Map<String, List<TrackPoint>>> = _trails.asStateFlow()

    /** Local RF evidence (TNC only) for my links and range records (#4, #14). */
    private val _rfEvents = MutableStateFlow<List<RfEvent>>(emptyList())
    val rfEvents: StateFlow<List<RfEvent>> = _rfEvents.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _status = MutableStateFlow(LinkStatus())
    val status: StateFlow<LinkStatus> = _status.asStateFlow()

    private val _monitor = MutableStateFlow<List<String>>(emptyList())
    val monitor: StateFlow<List<String>> = _monitor.asStateFlow()

    private val _connectionLog = MutableStateFlow<List<ConnectionEvent>>(emptyList())
    val connectionLog: StateFlow<List<ConnectionEvent>> = _connectionLog.asStateFlow()

    // Keep results per request so rapid navigation or two compositors cannot lose an acknowledgement.
    private val _sendResults = MutableStateFlow<Map<String, OutboundSendResult>>(emptyMap())
    val sendResults: StateFlow<Map<String, OutboundSendResult>> = _sendResults.asStateFlow()

    fun reportSendResult(result: OutboundSendResult) {
        _sendResults.update { (it + (result.requestId to result)).entries.toList().takeLast(64).associate { e -> e.key to e.value } }
    }

    private val _beaconResults = MutableStateFlow<Map<String, OutboundBeaconResult>>(emptyMap())
    val beaconResults: StateFlow<Map<String, OutboundBeaconResult>> = _beaconResults.asStateFlow()
    fun reportBeaconResult(result: OutboundBeaconResult) {
        _beaconResults.update {
            (it + (result.requestId to result)).entries.toList().takeLast(32).associate { e -> e.key to e.value }
        }
    }

    private var nextId = System.currentTimeMillis()
    private var stationStore: StationStore? = null
    private var messageStore: MessageStore? = null
    private var appContext: Context? = null
    private val diskWorker = Executors.newSingleThreadExecutor { r -> Thread(r, "aprs-station-db").apply { isDaemon = true } }
    private val stationCacheEpoch = AtomicLong(0L)
    private const val RETENTION_MS = 30L * 24 * 60 * 60 * 1000
    private const val RF_EVENTS_RETENTION_MS = 365L * 24 * 60 * 60 * 1000
    @Volatile var retentionPolicy: RetentionPolicy = RetentionPolicy()
    private var addsSinceRetention = 0

    fun trackRetentionDays(context: Context): Int =
        Settings.readOption(context, "track_retention_days", "7").toIntOrNull()?.coerceIn(1, 365) ?: 7

    @Synchronized fun init(context: Context) {
        if (stationStore != null) return
        appContext = context.applicationContext
        preferences = context.applicationContext.getSharedPreferences("pt2vhf_aprs", Context.MODE_PRIVATE)
        _favorites.value = preferences?.getStringSet("favorite_stations", emptySet())?.toSet().orEmpty()
        retentionPolicy = Settings.load(context).retention
        stationStore = StationStore(context.applicationContext)
        val msgStore = MessageStore(context.applicationContext)
        messageStore = msgStore
        // Messages are read synchronously so a conversation never opens empty after an update.
        runCatching { msgStore.migrateLegacyJson(File(context.filesDir, "messages.json")) }
        runCatching {
            val all = msgStore.readAll()
            val victims = retentionVictims(all, retentionPolicy)
            if (victims.isNotEmpty()) msgStore.delete(victims)
            val kept = all.filter { it.id !in victims }
            _messages.value = kept
            nextId = maxOf(nextId, (kept.maxOfOrNull { it.id } ?: 0) + 1)
        }
        val trackDays = trackRetentionDays(context)
        val restoreEpoch = stationCacheEpoch.get()
        diskWorker.execute {
            runCatching {
                val store = stationStore ?: return@execute
                val now = System.currentTimeMillis()
                val since = now - RETENTION_MS
                val trackSince = now - trackDays * 86_400_000L
                store.prune(since, trackSince, now - RF_EVENTS_RETENTION_MS)
                val previousStations = store.readStations(since)
                val previousLinks = store.readLinks(since)
                val previousTrack = store.readTrack(trackSince)
                val previousEvents = store.readRfEvents(now - RF_EVENTS_RETENTION_MS)
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
                _rfEvents.update { current -> (previousEvents + current).sortedBy { it.time }.takeLast(20000) }
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
            haversineKm(last.lat, last.lon, lat, lon) < 0.01) return
        _track.update { (it + p).takeLast(10000) }
        diskWorker.execute { runCatching { stationStore?.saveTrack(p) } }
    }

    fun clearTrack() {
        _track.value = emptyList()
        diskWorker.execute { runCatching { stationStore?.clearTrack() } }
    }

    /** Applies a new track retention immediately (#5). */
    fun pruneTrack(days: Int) {
        val since = System.currentTimeMillis() - days.coerceIn(1, 365) * 86_400_000L
        _track.update { list -> list.filter { it.recordedAt >= since } }
        diskWorker.execute {
            runCatching {
                val now = System.currentTimeMillis()
                stationStore?.prune(now - RETENTION_MS, since, now - RF_EVENTS_RETENTION_MS)
            }
        }
    }

    fun clearRfEvents() {
        _rfEvents.value = emptyList()
        diskWorker.execute { runCatching { stationStore?.clearRfEvents() } }
    }

    suspend fun clearStations(): Boolean {
        val store = stationStore ?: return false
        stationCacheEpoch.incrementAndGet()
        _stations.value = emptyMap()
        _links.value = emptyList()
        _trails.value = emptyMap()
        _followCall.value = null
        return withContext(Dispatchers.IO) {
            runCatching { diskWorker.submit<Boolean> { store.clearStationsAndLinks(); true }.get() }
                .getOrDefault(false)
        }
    }

    suspend fun clearMessages(): Boolean {
        val store = messageStore ?: return false
        _messages.value = emptyList()
        return withContext(Dispatchers.IO) {
            runCatching { diskWorker.submit<Boolean> { store.clear(); true }.get() }.getOrDefault(false)
        }
    }

    // ---------- Retenção (#26) ----------

    fun retentionPreview(policy: RetentionPolicy): Int = retentionVictims(_messages.value, policy).size

    /** Applies [policy] now; returns how many messages were removed. Never touches pending sends. */
    fun applyRetention(policy: RetentionPolicy = retentionPolicy): Int {
        retentionPolicy = policy
        val victims = retentionVictims(_messages.value, policy)
        if (victims.isEmpty()) return 0
        _messages.update { list -> list.filter { it.id !in victims } }
        diskWorker.execute { runCatching { messageStore?.delete(victims) } }
        return victims.size
    }

    fun messageStorageBytes(): Long = messageStore?.sizeBytes() ?: 0L

    fun containsMessage(id: Long): Boolean = _messages.value.any { it.id == id }

    fun message(id: Long?): ChatMessage? = id?.let { wanted -> _messages.value.firstOrNull { it.id == wanted } }

    fun updateStatus(change: (LinkStatus) -> LinkStatus) = _status.update(change)

    fun monitorLine(line: String) = _monitor.update { (it + line).takeLast(200) }

    fun logConnection(source: String, text: String) {
        _connectionLog.update { (it + ConnectionEvent(System.currentTimeMillis(), source, text)).takeLast(300) }
    }

    private fun recordTrail(call: String, lat: Double, lon: Double, now: Long) {
        _trails.update { map ->
            val list = map[call].orEmpty()
            val last = list.lastOrNull()
            if (last != null && haversineKm(last.lat, last.lon, lat, lon) < 0.02) map
            else map + (call to (list + TrackPoint(lat, lon, now)).takeLast(500))
        }
    }

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
        val position = parsePosition(packet.info)
        if (medium == Medium.RF) {
            val own = _status.value
            val known = _stations.value
            rfEventFor(packet, ownCall, position?.lat ?: known[packet.source]?.lat,
                position?.lon ?: known[packet.source]?.lon, own.lat, own.lon,
                { call -> known[call]?.let { it.lat to it.lon } }, now)?.let { event ->
                _rfEvents.update { (it + event).takeLast(20000) }
                diskWorker.execute { runCatching { stationStore?.saveRfEvent(event) } }
            }
        }
        position?.let { pos ->
            var persisted: Station? = null
            _stations.update { map ->
                val existing = map[packet.source]
                val heardBy = (existing?.heardBy ?: emptySet()) +
                    (if (medium == Medium.RF) digis.take(1) else emptyList())
                val station = Station(
                    packet.source, pos.lat, pos.lon, pos.symbolTable, pos.symbol, decodeAprsText(pos.comment), now, medium,
                    packet.path.joinToString(","), pos.speedKmh, pos.course, heardBy,
                    firstSeen = existing?.firstSeen ?: now,
                )
                persisted = station
                map + (packet.source to station)
            }
            recordTrail(packet.source, pos.lat, pos.lon, now)
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
                // Display text only (#27): IDs/ACK and the raw packet keep the original bytes.
                val text = decodeAprsText(msg.text)
                val duplicate = _messages.value.any {
                    !it.outgoing && it.from == packet.source && it.msgId != null && it.msgId == msg.id && it.text == text
                }
                if (duplicate) return null
                val chat = ChatMessage(nextId++, packet.source, msg.addressee, text, now, false, medium, via, msg.id,
                    "Recebida", msg.kind)
                add(chat)
                if (forUs) chat else null
            }
        }
    }

    fun add(message: ChatMessage) {
        _messages.update { it + message }
        diskWorker.execute { runCatching { messageStore?.upsert(listOf(message)) } }
        // Retention is evaluated on load and every 100 new records, not by a periodic job.
        if (++addsSinceRetention >= 100) {
            addsSinceRetention = 0
            applyRetention()
        }
    }

    fun newId(): Long = nextId++

    fun setStatus(id: Long, status: String) {
        var changed: ChatMessage? = null
        _messages.update { list ->
            list.map { if (it.id == id) it.copy(status = status).also { c -> changed = c } else it }
        }
        changed?.let { m -> diskWorker.execute { runCatching { messageStore?.upsert(listOf(m)) } } }
    }

    private fun markAck(from: String, msgId: String?, ack: Boolean) {
        if (msgId == null) return
        val changed = ArrayList<ChatMessage>()
        _messages.update { list ->
            changed.clear()
            list.map {
                if (it.outgoing && it.to == from && it.msgId == msgId)
                    it.copy(status = if (ack) "ACK recebido" else "Rejeitada").also { c -> changed += c }
                else it
            }
        }
        val snapshot = changed.toList()
        diskWorker.execute { runCatching { messageStore?.upsert(snapshot) } }
    }

    fun isAcked(localId: Long): Boolean =
        _messages.value.firstOrNull { it.id == localId }?.status?.let { it == "ACK recebido" || it == "Rejeitada" } == true
}
