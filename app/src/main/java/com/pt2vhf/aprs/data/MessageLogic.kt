package com.pt2vhf.aprs.data

import com.pt2vhf.aprs.core.AprsMessage
import com.pt2vhf.aprs.core.CALLSIGN_RE
import java.util.Locale

// ---------- Conversas: filtros combináveis (#2) ----------

data class MessageCriteria(
    val text: String = "",
    val direction: String = "all", // all | in | out
    val medium: String = "all", // all | rf | internet
    val status: String = "all", // all | pending | failed
    val period: String = "all", // all | 24h | 7d | 30d
) {
    val active: Boolean get() = text.isNotBlank() || direction != "all" || medium != "all" ||
        status != "all" || period != "all"
}

fun isPendingStatus(status: String): Boolean = status == "Enviando" || status.startsWith("Aguardando ACK")

/** #60: envio cujo serviço morreu no meio; sem isso o balão ficaria "tentando" para sempre. */
const val INTERRUPTED_STATUS = "Interrompida"

fun interruptPending(list: List<ChatMessage>): List<ChatMessage> =
    list.map { if (it.outgoing && isPendingStatus(it.status)) it.copy(status = INTERRUPTED_STATUS) else it }

fun isFailedStatus(status: String): Boolean =
    status == "Sem conexão para enviar" || status == INTERRUPTED_STATUS || status.startsWith("Sem ACK após ") || status == "Rejeitada"

fun matchesMessage(m: ChatMessage, c: MessageCriteria, now: Long = System.currentTimeMillis()): Boolean {
    val text = c.text.trim()
    if (text.isNotEmpty() && !(m.from.contains(text, true) || m.to.contains(text, true) || m.text.contains(text, true)))
        return false
    when (c.direction) {
        "in" -> if (m.outgoing) return false
        "out" -> if (!m.outgoing) return false
    }
    when (c.medium) {
        // Outgoing messages have no reception medium; their status tells the transport used.
        "rf" -> if (!(m.medium == Medium.RF || (m.outgoing && m.status.contains("RF")))) return false
        "internet" -> if (!(m.medium == Medium.INTERNET || (m.outgoing && m.status.contains("APRS-IS")))) return false
    }
    when (c.status) {
        "pending" -> if (!(m.outgoing && isPendingStatus(m.status))) return false
        "failed" -> if (!(m.outgoing && isFailedStatus(m.status))) return false
    }
    val window = when (c.period) {
        "24h" -> 86_400_000L
        "7d" -> 7 * 86_400_000L
        "30d" -> 30 * 86_400_000L
        else -> null
    }
    if (window != null && m.time < now - window) return false
    return true
}

/** Private conversations grouped by contact, ordered by last RX or TX activity. */
fun conversations(
    messages: List<ChatMessage>,
    ownCall: String,
    criteria: MessageCriteria,
    newestFirst: Boolean,
    now: Long = System.currentTimeMillis(),
): List<Pair<String, List<ChatMessage>>> {
    val direct = messages.filter {
        it.kind == AprsMessage.Kind.MESSAGE && it.to != "CQ" && !it.to.startsWith("CQ") &&
            (it.to == ownCall || it.to == ownCall.substringBefore('-') || it.outgoing) &&
            matchesMessage(it, criteria, now)
    }
    val grouped = direct.groupBy { if (it.outgoing) it.to else it.from }.toList()
    val order = compareBy<Pair<String, List<ChatMessage>>> { (_, list) -> list.maxOf { it.time } }
        .thenBy { it.first }
    return grouped.sortedWith(if (newestFirst) order.reversed() else order)
}

// ---------- Busca de indicativo para nova conversa (#21) ----------

data class CallsignHit(val call: String, val lastContact: Long?, val medium: Medium?, val inHistory: Boolean)

/**
 * Busca por substring, sem distinção de maiúsculas, nas estações recebidas (inclui cache local)
 * e nos participantes do histórico de mensagens. Nunca inventa estações nem troca o SSID.
 */
fun searchCallsigns(
    query: String,
    stations: Collection<Station>,
    messages: List<ChatMessage>,
    ownCall: String,
    limit: Int = 50,
): List<CallsignHit> {
    val q = query.trim().uppercase(Locale.ROOT)
    if (q.isEmpty()) return emptyList()
    val own = ownCall.uppercase(Locale.ROOT)
    val hits = LinkedHashMap<String, CallsignHit>()
    for (s in stations) {
        val call = s.call.uppercase(Locale.ROOT)
        if (call == own || !call.contains(q)) continue
        hits[call] = CallsignHit(call, s.lastHeard, s.medium, false)
    }
    for (m in messages) {
        if (m.kind != AprsMessage.Kind.MESSAGE) continue
        val other = (if (m.outgoing) m.to else m.from).uppercase(Locale.ROOT)
        if (other == own || other == "CQ" || other.startsWith("CQ") || other.startsWith("BLN")) continue
        if (!other.contains(q) || !CALLSIGN_RE.matches(other)) continue
        val previous = hits[other]
        hits[other] = if (previous == null) CallsignHit(other, m.time, if (m.outgoing) null else m.medium, true)
        else previous.copy(
            inHistory = true,
            lastContact = maxOf(previous.lastContact ?: 0L, m.time),
        )
    }
    return hits.values.sortedWith(
        compareByDescending<CallsignHit> { it.call == q }
            .thenByDescending { it.call.startsWith(q) }
            .thenByDescending { it.lastContact ?: 0L }
            .thenBy { it.call }
    ).take(limit)
}

// ---------- Retenção configurável (#26) ----------

enum class MessageCategory { PRIVATE, GENERAL, BULLETIN }

fun messageCategory(m: ChatMessage): MessageCategory = when {
    m.kind == AprsMessage.Kind.BULLETIN || m.to.startsWith("BLN") -> MessageCategory.BULLETIN
    m.to == "CQ" || m.to.startsWith("CQ") -> MessageCategory.GENERAL
    else -> MessageCategory.PRIVATE
}

/**
 * Stable deletion snapshot. It is applied to SQLite before the in-memory list changes:
 * packets arriving after confirmation must not be removed from the database or the UI.
 */
fun deletionVictimIds(messages: List<ChatMessage>, category: MessageCategory?): Set<Long> =
    messages.asSequence().filter { message ->
        when (category) {
            null -> true
            MessageCategory.PRIVATE -> messageCategory(message) == MessageCategory.PRIVATE
            else -> messageCategory(message) != MessageCategory.PRIVATE
        }
    }.map { it.id }.toSet()

/** days = 0: sem prazo; max = 0: sem limite de quantidade. */
data class RetentionRule(val days: Int = 0, val max: Int = 5000)

data class RetentionPolicy(
    val private: RetentionRule = RetentionRule(),
    val general: RetentionRule = RetentionRule(),
    val bulletin: RetentionRule = RetentionRule(),
) {
    fun rule(category: MessageCategory): RetentionRule = when (category) {
        MessageCategory.PRIVATE -> private
        MessageCategory.GENERAL -> general
        MessageCategory.BULLETIN -> bulletin
    }
}

/**
 * Devolve os ids que a política removeria. Mensagens em envio ou aguardando ACK nunca são removidas.
 * Prazo e quantidade valem juntos: sai o que vencer primeiro. Horário inválido no futuro não é apagado.
 */
fun retentionVictims(messages: List<ChatMessage>, policy: RetentionPolicy, now: Long = System.currentTimeMillis()): Set<Long> {
    val victims = HashSet<Long>()
    for ((category, list) in messages.groupBy { messageCategory(it) }) {
        val rule = policy.rule(category)
        val removable = list.filter { !(it.outgoing && isPendingStatus(it.status)) }
        if (rule.days > 0) {
            val limit = now - rule.days * 86_400_000L
            removable.filter { it.time in 1 until limit }.forEach { victims += it.id }
        }
        if (rule.max > 0) {
            val remaining = list.filter { it.id !in victims }
            val excess = remaining.size - rule.max
            if (excess > 0) {
                removable.filter { it.id !in victims }
                    .sortedWith(compareBy<ChatMessage> { it.time }.thenBy { it.id })
                    .take(excess).forEach { victims += it.id }
            }
        }
    }
    return victims
}
