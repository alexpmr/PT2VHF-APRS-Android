package com.pt2vhf.aprs.data

import com.pt2vhf.aprs.core.AprsPacket
import com.pt2vhf.aprs.core.callsignCountry
import com.pt2vhf.aprs.core.checkAmateurCallsign
import com.pt2vhf.aprs.core.parseMessage
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Locale

/*
 * #58.5 Comunidade: rodadas (nets) de check-in por mensagem APRS. A participação é calculada só com o
 * que este app ouviu (mensagens recebidas e pacotes do log); a fonte é sempre informada na tela.
 * Lógica pura para testes na JVM.
 */

/**
 * Rodada: mensagens para [destination] (ex.: ANSRVR) com a [keyword] no texto (ex.: HOTG) no dia da semana
 * [weekday] (1 = segunda … 7 = domingo), das [startHour] por [hours] horas, no fuso [utc] ou local.
 */
data class NetDefinition(
    val name: String,
    val destination: String,
    val keyword: String,
    val weekday: Int,
    val startHour: Int = 0,
    val hours: Int = 24,
    val utc: Boolean = true,
    /** Texto pré-preenchido do check-in. */
    val checkInText: String = "",
) {
    fun zone(): ZoneId = if (utc) ZoneOffset.UTC else ZoneId.systemDefault()
    fun window(date: LocalDate): LongRange {
        val start = date.atStartOfDay(zone()).plusHours(startHour.toLong()).toInstant().toEpochMilli()
        return start until start + hours * 3_600_000L
    }
}

/** Rodada semanal conhecida: APRS Thursday (mensagem a ANSRVR com "CQ HOTG", toda quinta-feira em UTC). */
val DEFAULT_NETS = listOf(
    NetDefinition("APRS Thursday", "ANSRVR", "HOTG", 4, 0, 24, true, "CQ HOTG 73 PT2VHF APRS Android"),
)

/** Uma linha por rodada, campos separados por "|". */
fun serializeNets(nets: List<NetDefinition>): String = nets.joinToString("\n") { n ->
    listOf(n.name, n.destination, n.keyword, n.weekday, n.startHour, n.hours, n.utc, n.checkInText)
        .joinToString("|") { it.toString().replace("|", "/").replace("\n", " ") }
}

fun parseNets(text: String): List<NetDefinition> = text.lines().mapNotNull { line ->
    val f = line.split('|')
    if (f.size < 8) return@mapNotNull null
    val weekday = f[3].toIntOrNull()?.takeIf { it in 1..7 } ?: return@mapNotNull null
    val dest = f[1].trim().uppercase(Locale.ROOT).takeIf { it.isNotEmpty() && it.length <= 9 } ?: return@mapNotNull null
    NetDefinition(f[0].trim().ifEmpty { dest }, dest, f[2].trim().uppercase(Locale.ROOT), weekday,
        f[4].toIntOrNull()?.coerceIn(0, 23) ?: 0, f[5].toIntOrNull()?.coerceIn(1, 168) ?: 24,
        f[6].trim() != "false", f[7])
}

/** Data mais recente (hoje inclusive) em que a rodada acontece. */
fun latestNetDate(net: NetDefinition, today: LocalDate): LocalDate {
    var d = today
    while (d.dayOfWeek != DayOfWeek.of(net.weekday)) d = d.minusDays(1)
    return d
}

data class NetCheckIn(val call: String, val time: Long, val text: String, val country: String?)

data class NetSummary(
    val checkIns: List<NetCheckIn>,
    /** País (ISO ou null = não identificado) → quantidade, do maior para o menor. */
    val byCountry: List<Pair<String?, Int>>,
)

private fun participantOf(from: String, text: String, destination: String): String? {
    if (!from.equals(destination, ignoreCase = true)) return from.uppercase(Locale.ROOT)
    // Retransmissões do próprio servidor (ex.: ANSRVR) trazem o indicativo de quem fez check-in no texto.
    return text.split(Regex("[\\s:<>,]+")).map { it.uppercase(Locale.ROOT) }
        .firstOrNull { checkAmateurCallsign(it).ok }
}

/**
 * Check-ins da rodada na data: mensagens recebidas/enviadas para o destino da rodada (ou retransmitidas por ele)
 * com a palavra-chave, dentro da janela. Um registro por indicativo (o primeiro), em ordem de chegada.
 */
fun netSummary(
    net: NetDefinition,
    date: LocalDate,
    messages: List<ChatMessage>,
    packets: List<PacketLogEntry>,
    query: String = "",
): NetSummary {
    val window = net.window(date)
    val keyword = net.keyword.uppercase(Locale.ROOT)
    val dest = net.destination.uppercase(Locale.ROOT)
    val seen = LinkedHashMap<String, NetCheckIn>()
    fun consider(from: String, to: String, text: String, time: Long) {
        if (time !in window) return
        val upper = text.uppercase(Locale.ROOT)
        if (keyword.isNotEmpty() && !upper.contains(keyword)) return
        if (!to.equals(dest, ignoreCase = true) && !from.equals(dest, ignoreCase = true)) return
        val call = participantOf(from, text, dest) ?: return
        if (call !in seen) seen[call] = NetCheckIn(call, time, text, callsignCountry(call))
    }
    val events = ArrayList<Pair<Long, () -> Unit>>()
    messages.forEach { m -> events += m.time to { consider(m.from, m.to, m.text, m.time) } }
    packets.forEach { e ->
        if (e.direction != "RX") return@forEach
        val p = AprsPacket.parse(e.raw) ?: return@forEach
        val msg = parseMessage(p.info) ?: return@forEach
        if (msg.kind != com.pt2vhf.aprs.core.AprsMessage.Kind.MESSAGE) return@forEach
        events += e.time to { consider(p.source, msg.addressee, msg.text, e.time) }
    }
    events.sortedBy { it.first }.forEach { it.second() }
    val q = query.trim().uppercase(Locale.ROOT)
    val list = seen.values.filter { q.isEmpty() || it.call.contains(q) }
    val byCountry = list.groupingBy { it.country }.eachCount().entries
        .sortedWith(compareByDescending<Map.Entry<String?, Int>> { it.value }.thenBy { it.key ?: "~" })
        .map { it.key to it.value }
    return NetSummary(list, byCountry)
}
