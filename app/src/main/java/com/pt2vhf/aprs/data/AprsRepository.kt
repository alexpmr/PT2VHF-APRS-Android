package com.pt2vhf.aprs.data

import android.content.Context
import com.pt2vhf.aprs.core.AprsMessage
import com.pt2vhf.aprs.core.AprsPacket
import com.pt2vhf.aprs.core.parseMessage
import com.pt2vhf.aprs.core.parsePosition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

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
    val tncRx: Int = 0,
    val tncTx: Int = 0,
)

/** Estado do app compartilhado entre o serviço e a interface. */
object AprsRepository {
    private val _stations = MutableStateFlow<Map<String, Station>>(emptyMap())
    val stations: StateFlow<Map<String, Station>> = _stations.asStateFlow()

    private val _links = MutableStateFlow<List<RfLink>>(emptyList())
    val links: StateFlow<List<RfLink>> = _links.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _status = MutableStateFlow(LinkStatus())
    val status: StateFlow<LinkStatus> = _status.asStateFlow()

    private val _monitor = MutableStateFlow<List<String>>(emptyList())
    val monitor: StateFlow<List<String>> = _monitor.asStateFlow()

    private var nextId = System.currentTimeMillis()
    private var file: File? = null

    fun init(context: Context) {
        if (file != null) return
        file = File(context.filesDir, "messages.json")
        runCatching { load() }
    }

    fun updateStatus(change: (LinkStatus) -> LinkStatus) = _status.update(change)

    fun monitorLine(line: String) = _monitor.update { (it + line).takeLast(200) }

    /** Processa um pacote recebido; devolve a mensagem endereçada a nós, se houver. */
    fun ingest(packet: AprsPacket, medium: Medium, ownCall: String, now: Long = System.currentTimeMillis()): ChatMessage? {
        _status.update { it.copy(rxPackets = it.rxPackets + 1) }
        val digis = packet.rfDigipeaters
        // Sem RF comprovado não há enlace (internet, DMR e outras redes digitais ficam de fora).
        if (digis.isNotEmpty()) {
            val chain = listOf(packet.source) + digis
            val newLinks = chain.zipWithNext().map { (a, b) -> RfLink(a, b, now) }
            _links.update { old ->
                (old.filter { l -> newLinks.none { it.from == l.from && it.to == l.to } } + newLinks).takeLast(2000)
            }
            if (packet.source == ownCall) _status.update { it.copy(beaconsDigipeated = it.beaconsDigipeated + 1) }
        }
        parsePosition(packet.info)?.let { pos ->
            val m = if (medium == Medium.RF || digis.isNotEmpty()) Medium.RF else Medium.INTERNET
            _stations.update { map ->
                val heardBy = (map[packet.source]?.heardBy ?: emptySet()) + digis.take(1)
                map + (packet.source to Station(
                    packet.source, pos.lat, pos.lon, pos.symbolTable, pos.symbol, pos.comment, now, m,
                    packet.path.joinToString(","), pos.speedKmh, pos.course, heardBy,
                ))
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
                val via = packet.rfDigipeaters.firstOrNull()?.let { "RF via $it" }
                    ?: if (medium == Medium.RF) "RF direto" else "APRS-IS"
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

    private fun save() {
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
