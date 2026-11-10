package com.pt2vhf.aprs.core

import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/** Passcode APRS-IS (algoritmo público), sobre o indicativo sem SSID. */
fun aprsPasscode(callsign: String): Int {
    val base = callsign.trim().uppercase(Locale.ROOT).substringBefore('-')
    var hash = 0x73e2
    var i = 0
    while (i < base.length) {
        hash = hash xor (base[i].code shl 8)
        if (i + 1 < base.length) hash = hash xor base[i + 1].code
        i += 2
    }
    return hash and 0x7fff
}

fun fullCallsign(callsign: String, ssid: Int): String {
    val base = callsign.trim().uppercase(Locale.ROOT).substringBefore('-')
    return if (ssid in 1..15) "$base-$ssid" else base
}

val CALLSIGN_RE = Regex("^[A-Z0-9]{1,6}(-([0-9]|1[0-5]))?$")

/**
 * Indicativo de radioamador plausível no mundo (#59): formato + prefixo alocado pela ITU
 * ([checkAmateurCallsign]). Não prova titularidade; destinos táticos (CQ, BLN1) não passam por aqui.
 */
fun plausibleAmateurCallsign(value: String): Boolean = checkAmateurCallsign(value).ok

/** Servidores oferecidos na lista; "Outro…" permite digitar. Padrão igual ao desktop. */
data class AprsServer(val host: String, val label: String)

val APRS_SERVERS = listOf(
    AprsServer("soam.aprs2.net", "América do Sul (padrão)"),
    AprsServer("rotate.aprs2.net", "Mundial (automático)"),
    AprsServer("noam.aprs2.net", "América do Norte"),
    AprsServer("euro.aprs2.net", "Europa e África"),
    AprsServer("asia.aprs2.net", "Ásia"),
    AprsServer("aunz.aprs2.net", "Oceania"),
)
const val DEFAULT_SERVER = "soam.aprs2.net"
const val DEFAULT_PORT = 14580
const val DEFAULT_FILTER = "r/4000"

/** `r/4000` (atalho do desktop) vira `r/<lat>/<lon>/4000` com a posição atual. */
fun expandFilter(filter: String, lat: Double?, lon: Double?): String {
    val parts = filter.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return parts.joinToString(" ") { part ->
        val m = Regex("^r/(\\d+(?:\\.\\d+)?)$").find(part)
        if (m != null && lat != null && lon != null)
            String.format(Locale.ROOT, "r/%.3f/%.3f/%s", lat, lon, m.groupValues[1])
        else part
    }
}

/**
 * APRS-IS group-message filter accepts wildcards. Subscribe to both bare callsign and
 * all SSIDs of this operator; a geographical filter alone misses message-only packets.
 * Remove the r/4000 shortcut until a GPS fix makes it a valid range filter.
 */
fun effectiveAprsFilter(filter: String, lat: Double?, lon: Double?, ownCall: String): String {
    val expanded = expandFilter(filter, lat, lon)
    val parts = expanded.split(Regex("\\s+")).filter {
        it.isNotBlank() && !Regex("^r/\\d+(?:\\.\\d+)?$").matches(it)
    }.toMutableList()
    val base = ownCall.trim().uppercase(Locale.ROOT).substringBefore('-')
    if (base.isNotBlank()) {
        val group = "g/$base/$base-*"
        if (group !in parts) parts += group
    }
    return parts.joinToString(" ")
}

fun validHostPort(host: String, port: Int): Boolean =
    port in 1..65535 && Regex("^[A-Za-z0-9.-]{1,253}$").matches(host.trim()) && !host.startsWith(".")

/** Pacote no formato TNC2: ORIGEM>DESTINO,PATH:INFO */
data class AprsPacket(
    val source: String,
    val destination: String,
    val path: List<String>,
    val info: String,
    val raw: String,
    /** Source gateway for a third-party RF packet; null for direct TNC2 packets. */
    val thirdPartyGateway: String? = null,
) {
    /** Elementos do path que foram repetidos por RF (marcados com '*'), sem pseudo-saltos de internet/rede digital. */
    val rfDigipeaters: List<String>
        get() = path.takeWhile { !it.startsWith("q") }
            .filter { it.endsWith("*") }
            .map { it.removeSuffix("*") }
            .filter { !isNonRfHop(it) }

    val viaInternet: Boolean
        get() = path.any { it.startsWith("TCPIP") || it == "qAC" || it == "qAS" || it == "qAI" }

    companion object {
        fun parse(line: String): AprsPacket? = parseDepth(line, 0)

        private fun parseDepth(line: String, depth: Int): AprsPacket? {
            val clean = line.trimEnd('\r', '\n')
            if (clean.isEmpty() || clean.startsWith("#")) return null
            val colon = clean.indexOf(':')
            val gt = clean.indexOf('>')
            if (colon < 0 || gt < 1 || gt > colon) return null
            val header = clean.substring(0, colon)
            val source = header.substring(0, gt).uppercase(Locale.ROOT)
            val rest = header.substring(gt + 1).split(',')
            if (rest.isEmpty() || rest[0].isEmpty()) return null
            val info = clean.substring(colon + 1)
            if (info.startsWith("}")) {
                // Third-party frame from an iGate: the original sender is in the inner header.
                // Permit only one encapsulation layer to prevent recursive malformed packets.
                if (depth != 0) return null
                return parseDepth(info.substring(1), 1)?.copy(raw = clean, thirdPartyGateway = source)
            }
            return AprsPacket(source, rest[0], rest.drop(1), info, clean)
        }
    }
}

/** Evidência *local* de um caminho RF exige que o pacote tenha chegado pelo TNC.
 * Um caminho APRS-IS pode relatar digipeaters, mas não confirma recepção no rádio deste aparelho.
 */
fun localRfEvidenceLinks(packet: AprsPacket, receivedViaTnc: Boolean): List<Pair<String, String>> {
    if (!receivedViaTnc || packet.thirdPartyGateway != null) return emptyList()
    val chain = listOf(packet.source) + packet.rfDigipeaters
    return chain.zipWithNext()
}

/** Marcadores de internet/redes digitais/gateways que não comprovam RF (ex.: DMR*). */
private val NON_RF_HOPS = setOf(
    "TCPIP", "TCPXX", "NOGATE", "RFONLY", "DMR", "DSTAR", "D-STAR", "YSF", "C4FM",
    "NXDN", "P25", "M17", "TETRA", "ECHOLINK", "WIRES", "WIRESX",
)

fun isNonRfHop(hop: String): Boolean {
    val name = hop.uppercase(Locale.ROOT).removeSuffix("*")
    return name in NON_RF_HOPS || name.startsWith("WIDE") || name.startsWith("TRACE") || name.startsWith("RELAY")
}

data class Position(val lat: Double, val lon: Double, val symbolTable: Char, val symbol: Char,
                    val comment: String, val course: Int? = null, val speedKmh: Double? = null)

data class AprsMessage(val addressee: String, val text: String, val id: String?, val kind: Kind,
                       /** Piggybacked ACK in {MM}AA; distinct from the new message's MM. */
                       val replyAckId: String? = null) {
    enum class Kind { MESSAGE, ACK, REJ, BULLETIN }
}

// Minutes may contain spaces when the sender uses position ambiguity (APRS 1.0.1, ch. 6).
private val UNCOMPRESSED = Regex("^(\\d{2})([\\d ]{2}\\.[\\d ]{2})([NS])(.)(\\d{3})([\\d ]{2}\\.[\\d ]{2})([EW])(.)(.*)$")

/** Ambiguous minutes ("03.5 ", "0 .  ") become the centre of the ambiguity box. */
private fun ambiguousMinutes(text: String): Double? {
    val blanks = text.count { it == ' ' }
    if (blanks == 0) return text.toDoubleOrNull()
    // Blanks must be the trailing digits: "MM.m ", "MM.  ", "M .  ", "  .  ".
    val digits = text.replace(".", "")
    if (digits.trimEnd().contains(' ')) return null
    val base = text.replace(' ', '0').toDoubleOrNull() ?: return null
    val half = when (blanks) { 1 -> 0.05; 2 -> 0.5; 3 -> 5.0; else -> 30.0 }
    return (base + half).coerceAtMost(59.99)
}

/** Posição não comprimida (!, =, /, @). Minutos >= 60 são rejeitados, como no desktop. */
fun parsePosition(info: String): Position? {
    val body = when {
        info.startsWith("!") || info.startsWith("=") -> info.substring(1)
        (info.startsWith("/") || info.startsWith("@")) && info.length > 8 -> info.substring(8)
        else -> return null
    }
    val m = UNCOMPRESSED.find(body) ?: return null
    val g = m.groupValues
    val latMin = ambiguousMinutes(g[2]) ?: return null
    val lonMin = ambiguousMinutes(g[6]) ?: return null
    if (latMin >= 60.0 || lonMin >= 60.0) return null
    var lat = g[1].toInt() + latMin / 60.0
    var lon = g[5].toInt() + lonMin / 60.0
    if (lat > 90 || lon > 180) return null
    if (g[3] == "S") lat = -lat
    if (g[7] == "W") lon = -lon
    var comment = g[9]
    var course: Int? = null
    var speed: Double? = null
    val ext = Regex("^(\\d{3})/(\\d{3})").find(comment)
    if (ext != null && g[8][0] != '_') {
        course = ext.groupValues[1].toInt()
        speed = ext.groupValues[2].toInt() * 1.852
        comment = comment.substring(7)
    }
    return Position(lat, lon, g[4][0], g[8][0], comment.trim(), course, speed)
}

/** Precompiled: literal closing brace is part of APRS reply-ack syntax. */
private val ACK_BODY_PATTERN = Regex("^(ack|rej)([A-Za-z0-9]{1,5})(?:[}]([A-Za-z0-9]{1,5}))?$")
private val MESSAGE_ID_SUFFIX_PATTERN = Regex("^([A-Za-z0-9]{1,5})(?:[}]([A-Za-z0-9]{1,5}))?$")

fun parseMessage(info: String): AprsMessage? {
    if (!info.startsWith(":") || info.length < 11 || info[10] != ':') return null
    val addressee = info.substring(1, 10).trim().uppercase(Locale.ROOT)
    val body = info.substring(11)
    val ackMatch = ACK_BODY_PATTERN.matchEntire(body)
    if (ackMatch != null) {
        val kind = if (ackMatch.groupValues[1] == "ack") AprsMessage.Kind.ACK else AprsMessage.Kind.REJ
        return AprsMessage(addressee, "", ackMatch.groupValues[2], kind,
            ackMatch.groupValues[3].ifEmpty { null })
    }
    val brace = body.lastIndexOf('{')
    val suffix = if (brace >= 0) body.substring(brace + 1) else ""
    val idMatch = MESSAGE_ID_SUFFIX_PATTERN.matchEntire(suffix)
    val text = if (idMatch != null) body.substring(0, brace) else body
    val id = idMatch?.groupValues?.get(1)
    val replyAck = idMatch?.groupValues?.get(2)?.ifEmpty { null }
    val kind = if (addressee.startsWith("BLN")) AprsMessage.Kind.BULLETIN else AprsMessage.Kind.MESSAGE
    return AprsMessage(addressee, text, id, kind, replyAck)
}

fun formatLatitude(lat: Double): String {
    val a = abs(lat)
    var deg = floor(a).toInt()
    var min = (a - deg) * 60.0
    if ((min * 100).roundToInt() >= 6000) { deg += 1; min = 0.0 }
    return String.format(Locale.ROOT, "%02d%05.2f%s", deg, min, if (lat < 0) "S" else "N")
}

fun formatLongitude(lon: Double): String {
    val a = abs(lon)
    var deg = floor(a).toInt()
    var min = (a - deg) * 60.0
    if ((min * 100).roundToInt() >= 6000) { deg += 1; min = 0.0 }
    return String.format(Locale.ROOT, "%03d%05.2f%s", deg, min, if (lon < 0) "W" else "E")
}

/** Info de posição APRS: !DDMM.mmS/DDDMM.mmW>CCC/SSScomentário */
const val PT2VHF_BEACON_SITE = "www.PT2VHF.com"
private val PT2VHF_BEACON_URL = Regex(
    """(?i)(?<![a-z0-9])(?:https?://)?(?:www\.)?pt2vhf\.com(?:/[a-z0-9_./-]*)?"""
)

/** Normaliza o texto apenas para transmissão, preservando as configurações salvas. */
fun beaconCommentWithSite(comment: String, maxChars: Int = 43): String {
    require(maxChars >= PT2VHF_BEACON_SITE.length)
    val original = comment.replace(Regex("""\s+"""), " ").trim()
    val description = PT2VHF_BEACON_URL.replace(original, " ")
        .replace(Regex("""\s+"""), " ").trim()
    val maxPrefix = (maxChars - PT2VHF_BEACON_SITE.length - 1).coerceAtLeast(0)
    val prefix = description.take(maxPrefix).trimEnd()
    return if (prefix.isEmpty()) PT2VHF_BEACON_SITE else "$prefix $PT2VHF_BEACON_SITE"
}

fun positionInfo(lat: Double, lon: Double, table: Char, symbol: Char, comment: String,
                 course: Int? = null, speedKmh: Double? = null, messaging: Boolean = true,
                 ambiguity: Int = 0): String {
    val ext = if (course != null && speedKmh != null)
        String.format(Locale.ROOT, "%03d/%03d", ((course % 360) + 360) % 360, (speedKmh / 1.852).roundToInt().coerceIn(0, 999))
    else ""
    val (la, lo) = applyAmbiguity(formatLatitude(lat), formatLongitude(lon), ambiguity)
    return (if (messaging) "=" else "!") + la + table + lo + symbol + ext + comment.take(43)
}

fun messageInfo(to: String, text: String, id: String?): String {
    val addressee = to.uppercase(Locale.ROOT).padEnd(9).take(9)
    val body = text.replace(Regex("[|~{]"), " ").take(67)
    return ":$addressee:$body" + (if (id != null) "{$id" else "")
}

fun ackInfo(to: String, id: String): String = ":" + to.uppercase(Locale.ROOT).padEnd(9).take(9) + ":ack" + id

/** Divide texto longo em partes de até 67 caracteres, preferindo espaços. */
fun splitMessage(text: String, limit: Int = 67): List<String> {
    val clean = text.replace(Regex("\\s+"), " ").trim()
    if (clean.length <= limit) return listOf(clean)
    val parts = mutableListOf<String>()
    var rest = clean
    while (rest.length > limit) {
        var cut = rest.lastIndexOf(' ', limit)
        if (cut < limit / 2) cut = limit
        parts += rest.substring(0, cut).trim()
        rest = rest.substring(cut).trim()
    }
    if (rest.isNotEmpty()) parts += rest
    return parts
}

fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
    return 2 * r * atan2(sqrt(a), sqrt(1 - a))
}

fun bearingDeg(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
    val y = sin(Math.toRadians(lon2 - lon1)) * cos(Math.toRadians(lat2))
    val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
        sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(Math.toRadians(lon2 - lon1))
    return ((Math.toDegrees(atan2(y, x)) + 360) % 360).roundToInt() % 360
}

fun compassName(deg: Int): String =
    listOf("N", "NE", "L", "SE", "S", "SO", "O", "NO")[((deg + 22) % 360) / 45]

fun maidenhead(lat: Double, lon: Double): String {
    val x = lon + 180.0
    val y = lat + 90.0
    val a = ('A' + (x / 20).toInt()).toString() + ('A' + (y / 10).toInt())
    val b = ((x % 20) / 2).toInt().toString() + (y % 10).toInt()
    val c = ('a' + ((x % 2) * 12).toInt()).toString() + ('a' + ((y % 1) * 24).toInt())
    return a + b + c
}

/** SmartBeaconing (parâmetros clássicos do HamHUD, em km/h). */
class SmartBeaconing(
    private val slowSpeed: Double = 5.0,
    private val slowRateSec: Int = 1800,
    private val fastSpeed: Double = 90.0,
    private val fastRateSec: Int = 60,
    private val minTurnAngle: Double = 28.0,
    private val turnSlope: Double = 255.0,
    private val minTurnTimeSec: Int = 15,
    /** Interval when practically stopped (< 1 km/h); 0 = use slowRateSec. */
    private val stoppedRateSec: Int = 0,
) {
    private fun rateFor(speedKmh: Double): Int = when {
        stoppedRateSec > 0 && speedKmh < 1.0 -> stoppedRateSec
        speedKmh < slowSpeed -> slowRateSec
        speedKmh > fastSpeed -> fastRateSec
        else -> (fastRateSec * fastSpeed / speedKmh).toInt()
    }

    fun shouldBeacon(secondsSinceLast: Long, speedKmh: Double, headingChange: Double): Boolean {
        val rate = rateFor(speedKmh)
        if (secondsSinceLast >= rate) return true
        if (speedKmh >= slowSpeed && secondsSinceLast >= minTurnTimeSec) {
            val threshold = minTurnAngle + turnSlope / speedKmh
            if (abs(headingChange) > threshold) return true
        }
        return false
    }

    fun nextBeaconSeconds(secondsSinceLast: Long, speedKmh: Double): Long =
        (rateFor(speedKmh) - secondsSinceLast).coerceAtLeast(0)
}

/** Comparação de versões x.y.z. */
fun isNewerVersion(candidate: String, current: String): Boolean {
    fun parts(v: String) = v.trim().removePrefix("v").split('.').map { it.toIntOrNull() ?: 0 }
    val a = parts(candidate)
    val b = parts(current)
    for (i in 0 until maxOf(a.size, b.size)) {
        val x = a.getOrElse(i) { 0 }
        val y = b.getOrElse(i) { 0 }
        if (x != y) return x > y
    }
    return false
}
