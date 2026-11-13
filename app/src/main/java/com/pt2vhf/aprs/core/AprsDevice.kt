package com.pt2vhf.aprs.core

import java.util.Locale

/*
 * #89: rádio/aplicativo da estação (tocall e Mic-E, tabela aprs-deviceid em DeviceIdTable.kt),
 * decodificação Mic-E completa e campos extras do pacote (PHG, frequência anunciada).
 * Puro Kotlin, sem Android, para testes na JVM.
 */

/** Status Mic-E (bits A/B/C do destino). A UI traduz pela chave. */
enum class MicEStatus { OFF_DUTY, EN_ROUTE, IN_SERVICE, RETURNING, COMMITTED, SPECIAL, PRIORITY, EMERGENCY, CUSTOM, UNKNOWN }

data class MicE(
    val position: Position,
    val status: MicEStatus,
    /** 0..6 quando [status] é CUSTOM. */
    val customIndex: Int? = null,
    val altitudeM: Double? = null,
    val device: String? = null,
)

private fun isMicEType(c: Char) = c == '`' || c == '\'' || c == 0x1C.toChar() || c == 0x1D.toChar()

/** Decodifica um pacote Mic-E (APRS 1.0.1, cap. 10). Retorna null se o destino/info não forem Mic-E válidos. */
fun parseMicE(destination: String, info: String): MicE? {
    if (info.length < 9 || !isMicEType(info[0])) return null
    val dest = destination.uppercase(Locale.ROOT).substringBefore('-')
    if (dest.length != 6) return null
    val digits = CharArray(6)
    // 0 = padrão, 1 = custom, -1 = sem bit (dígito)
    val bits = IntArray(3)
    val kinds = IntArray(3)
    for (i in 0 until 6) {
        val c = dest[i]
        val (digit, bit, kind) = when (c) {
            in '0'..'9' -> Triple(c, 0, 0)
            in 'A'..'J' -> Triple('0' + (c - 'A'), 1, 1)
            'K' -> Triple(' ', 1, 1)
            'L' -> Triple(' ', 0, 0)
            in 'P'..'Y' -> Triple('0' + (c - 'P'), 1, 0)
            'Z' -> Triple(' ', 1, 0)
            else -> return null
        }
        if (i > 3 && digit == ' ') return null
        digits[i] = digit
        if (i < 3) { bits[i] = bit; kinds[i] = kind }
        else if (kind == 1) return null // A..K só valem nos 3 primeiros
    }
    fun flag(i: Int) = dest[i] in 'P'..'Z'
    val north = flag(3)
    val lonOffset = if (flag(4)) 100 else 0
    val west = flag(5)

    val latText = String(digits).replace(' ', '0')
    val latDeg = latText.substring(0, 2).toInt()
    val latMin = (latText.substring(2, 4) + "." + latText.substring(4, 6)).toDouble()
    if (latDeg > 89 || latMin >= 60.0) return null
    var lat = latDeg + latMin / 60.0
    if (!north) lat = -lat

    fun b(i: Int) = info[i].code - 28
    var lonDeg = b(1) + lonOffset
    if (lonDeg in 180..189) lonDeg -= 80 else if (lonDeg in 190..199) lonDeg -= 190
    var lonMin = b(2)
    if (lonMin >= 60) lonMin -= 60
    val lonHun = b(3)
    if (lonDeg !in 0..179 || lonMin !in 0..59 || lonHun !in 0..99) return null
    var lon = lonDeg + (lonMin + lonHun / 100.0) / 60.0
    if (west) lon = -lon

    var speedKnots = b(4) * 10 + b(5) / 10
    var course = (b(5) % 10) * 100 + b(6)
    if (speedKnots >= 800) speedKnots -= 800
    if (course >= 400) course -= 400
    val symbol = info[7]
    val table = info[8]

    var comment = info.substring(9)
    val device = micEDevice(comment)
    comment = stripMicEDevice(comment)
    var altitude: Double? = null
    if (comment.length >= 4 && comment[3] == '}') {
        val alt = base91(comment.substring(0, 3))
        if (alt != null) { altitude = (alt - 10000).toDouble(); comment = comment.substring(4) }
    }

    val v = bits[0] * 4 + bits[1] * 2 + bits[2]
    val custom = (0..2).any { bits[it] == 1 && kinds[it] == 1 }
    val standard = (0..2).any { bits[it] == 1 && kinds[it] == 0 }
    val status = when {
        custom && standard -> MicEStatus.UNKNOWN
        custom -> MicEStatus.CUSTOM
        else -> listOf(MicEStatus.EMERGENCY, MicEStatus.PRIORITY, MicEStatus.SPECIAL, MicEStatus.COMMITTED,
            MicEStatus.RETURNING, MicEStatus.IN_SERVICE, MicEStatus.EN_ROUTE, MicEStatus.OFF_DUTY)[v]
    }
    val customIndex = if (status == MicEStatus.CUSTOM) 7 - v else null

    val pos = Position(lat, lon, table, symbol, comment.trim(),
        course.takeIf { it in 1..360 }, if (course in 1..360 || speedKnots > 0) speedKnots * 1.852 else null)
    return MicE(pos, status, customIndex, altitude, device)
}

private fun base91(s: String): Int? {
    var v = 0
    for (c in s) { if (c.code !in 33..123) return null; v = v * 91 + (c.code - 33) }
    return v
}

/** Posição de qualquer formato suportado: não comprimida ou Mic-E. */
fun parsePacketPosition(packet: AprsPacket): Position? =
    parsePosition(packet.info) ?: parseMicE(packet.destination, packet.info)?.position

private fun micELegacy(comment: String): Triple<String, String, String>? {
    if (comment.isEmpty() || (comment[0] != '>' && comment[0] != ']')) return null
    val prefix = comment[0].toString()
    val last = comment.last().toString()
    return DEVICE_MICE_LEGACY.firstOrNull { it.first == prefix && it.second.isNotEmpty() && comment.length > 1 && it.second == last }
        ?: DEVICE_MICE_LEGACY.firstOrNull { it.first == prefix && it.second.isEmpty() }
}

private fun micENew(comment: String): Pair<String, String>? {
    if (comment.length < 3 || (comment[0] != '`' && comment[0] != '\'')) return null
    val tail = comment.takeLast(2)
    return DEVICE_MICE.firstOrNull { it.first == tail }
}

/** Aparelho pelo comentário Mic-E (prefixo/sufixo de fabricante). */
fun micEDevice(comment: String): String? = micENew(comment)?.second ?: micELegacy(comment)?.third

private fun stripMicEDevice(comment: String): String {
    micENew(comment)?.let { return comment.substring(1, comment.length - 2) }
    micELegacy(comment)?.let { e -> return comment.substring(1, comment.length - e.second.length) }
    if (comment.isNotEmpty() && (comment[0] == '`' || comment[0] == '\'')) return comment.substring(1)
    return comment
}

/** Tocalls deste app (não estão na tabela pública). */
private val OWN_TOCALLS = mapOf("APZVHF" to "PT2VHF APRS Client")

private fun tocallMatches(pattern: String, call: String): Boolean {
    var i = 0
    while (i < pattern.length) {
        val p = pattern[i]
        if (p == '*') return true
        if (i >= call.length) return false
        val c = call[i]
        val ok = when (p) { '?' -> true; 'n' -> c.isDigit(); else -> p == c }
        if (!ok) return false
        i++
    }
    return i == call.length
}

/** Aparelho/aplicativo pelo destino (tocall); o padrão mais específico vence. */
fun deviceByTocall(destination: String): String? {
    val call = destination.uppercase(Locale.ROOT).substringBefore('-')
    OWN_TOCALLS[call]?.let { return it }
    return DEVICE_TOCALLS.filter { tocallMatches(it.first, call) }
        .maxByOrNull { (p, _) -> p.count { it != '?' && it != '*' && it != 'n' } * 10 + p.length }?.second
}

/** Rádio/aplicativo do pacote: Mic-E pelo comentário, demais pelo tocall. */
fun identifyDevice(packet: AprsPacket): String? {
    val info = packet.info
    if (info.isNotEmpty() && isMicEType(info[0])) return if (info.length > 9) micEDevice(info.substring(9)) else null
    return deviceByTocall(packet.destination)
}

/** PHGphgd decodificado: potência (W), altura (m), ganho (dB) e direção (0 = omni). */
data class Phg(val powerW: Int, val heightM: Int, val gainDb: Int, val directionDeg: Int)

fun decodePhg(code: String): Phg? {
    if (code.length != 4 || !code.all { it.isDigit() }) return null
    val p = code[0] - '0'
    val h = code[1] - '0'
    val g = code[2] - '0'
    val d = code[3] - '0'
    if (d > 8) return null
    val feet = 10.0 * (1 shl h)
    return Phg(p * p, Math.round(feet * 0.3048).toInt(), g, d * 45)
}

/** Frequência anunciada no comentário: "146.520MHz T088 +060" → "146.520 MHz · T088 · +0.600 MHz". */
private val FREQ = Regex("(\\d{3}\\.\\d{2,3})\\s?MHz(?:\\s+([TCD])(\\d{3}))?(?:\\s+([+-])(\\d{3}))?", RegexOption.IGNORE_CASE)

fun announcedFrequency(comment: String): String? {
    val m = FREQ.find(comment) ?: return null
    val g = m.groupValues
    val parts = mutableListOf("${g[1]} MHz")
    if (g[2].isNotEmpty()) parts += g[2].uppercase(Locale.ROOT) + g[3]
    if (g[4].isNotEmpty()) parts += g[4] + String.format(Locale.ROOT, "%.3f MHz", g[5].toInt() * 0.01)
    return parts.joinToString(" · ")
}
