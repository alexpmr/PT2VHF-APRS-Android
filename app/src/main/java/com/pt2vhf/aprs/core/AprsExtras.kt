package com.pt2vhf.aprs.core

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

/*
 * Lógica pura da 0.1.9: tipos de pacote (#51), objetos/itens APRS (#48), idade das mensagens (#40),
 * rotas GPX (rotas), divisor de baud FTDI (USB-OTG) e estimativa de consumo de bateria (#49).
 */

// ---------- Tipo de pacote (#51) ----------

/** Chave estável (não traduzida) do tipo do pacote APRS, pelo identificador de tipo de dado. */
fun packetType(info: String): String {
    if (info.isEmpty()) return "other"
    return when (info[0]) {
        '!', '=', '/', '@' -> if (parsePosition(info)?.symbol == '_') "weather" else "position"
        ':' -> {
            val m = parseMessage(info)
            when {
                m == null -> "other"
                info.length > 11 && info.substring(11).startsWith("PARM.") ||
                    info.length > 11 && info.substring(11).startsWith("UNIT.") ||
                    info.length > 11 && info.substring(11).startsWith("EQNS.") ||
                    info.length > 11 && info.substring(11).startsWith("BITS.") -> "telemetry"
                m.kind == AprsMessage.Kind.ACK || m.kind == AprsMessage.Kind.REJ -> "ack"
                m.kind == AprsMessage.Kind.BULLETIN -> "bulletin"
                else -> "message"
            }
        }
        ';' -> "object"
        ')' -> "item"
        '>' -> "status"
        'T' -> if (info.startsWith("T#")) "telemetry" else "other"
        '_' -> "weather"
        '`', '\'', 0x1C.toChar(), 0x1D.toChar() -> "mice"
        '?' -> "query"
        '<' -> "capabilities"
        '$' -> "nmea"
        '{' -> "user"
        else -> "other"
    }
}

val PACKET_TYPES = listOf("position", "message", "bulletin", "ack", "object", "item", "status", "telemetry",
    "weather", "mice", "query", "capabilities", "nmea", "user", "other")

// ---------- Objetos e itens (#48) ----------

/** Nome de objeto: 1 a 9 caracteres ASCII imprimíveis (objeto) ou 3 a 9 (item), sem '!' '_' para itens. */
fun validObjectName(name: String, item: Boolean): Boolean {
    val n = name.trim()
    if (n.length !in (if (item) 3 else 1)..9) return false
    return n.all { it.code in 0x20..0x7E && it != '|' && it != '~' } && (!item || n.none { it == '!' || it == '_' })
}

private fun utcStamp(timeMs: Long): String {
    val c = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    c.timeInMillis = timeMs
    return String.format(Locale.ROOT, "%02d%02d%02dz", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
}

/**
 * Objeto APRS: `;NOME_____*DDHHMMzDDMM.mmN/DDDMM.mmW>comentário` (vivo `*`, encerrado `_`).
 * Item APRS: `)NOME!DDMM.mmN/DDDMM.mmW>comentário` (vivo `!`, encerrado `_`).
 */
fun objectInfo(name: String, alive: Boolean, lat: Double, lon: Double, table: Char, symbol: Char,
               comment: String, item: Boolean = false, timeMs: Long = System.currentTimeMillis()): String {
    val clean = name.trim().take(9)
    val pos = formatLatitude(lat) + table + formatLongitude(lon) + symbol
    val text = comment.replace(Regex("[|~]"), " ").take(43)
    return if (item) ")" + clean + (if (alive) "!" else "_") + pos + text
    else ";" + clean.padEnd(9) + (if (alive) "*" else "_") + utcStamp(timeMs) + pos + text
}

data class ObjectReport(val name: String, val alive: Boolean, val position: Position, val item: Boolean)

/** Lê objeto/item recebido (formato não comprimido). */
fun parseObject(info: String): ObjectReport? {
    if (info.startsWith(";") && info.length >= 18) {
        val name = info.substring(1, 10).trim()
        val flag = info[10]
        if (flag != '*' && flag != '_' || name.isEmpty()) return null
        val pos = parsePosition("!" + info.substring(18)) ?: return null
        return ObjectReport(name, flag == '*', pos, false)
    }
    if (info.startsWith(")")) {
        val end = info.indexOfFirst { it == '!' || it == '_' }.takeIf { it in 4..10 } ?: return null
        val name = info.substring(1, end).trim()
        val pos = parsePosition("!" + info.substring(end + 1)) ?: return null
        return ObjectReport(name, info[end] == '!', pos, true)
    }
    return null
}

// ---------- Idade das mensagens (#40) ----------

/** sameDay: mesma data local do aparelho; hours/minutes: tempo decorrido (só faz sentido se sameDay). */
data class MessageAge(val sameDay: Boolean, val hours: Int, val minutes: Int, val justNow: Boolean)

fun messageAge(time: Long, now: Long, zone: TimeZone = TimeZone.getDefault()): MessageAge {
    val a = Calendar.getInstance(zone).apply { timeInMillis = time }
    val b = Calendar.getInstance(zone).apply { timeInMillis = now }
    val same = a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    val elapsedMin = ((now - time).coerceAtLeast(0L) / 60_000L).toInt()
    return MessageAge(same, elapsedMin / 60, elapsedMin % 60, now - time < 60_000L)
}

// ---------- Rotas GPX ----------

data class RoutePoint(val lat: Double, val lon: Double)
data class ParsedRoute(val name: String, val points: List<RoutePoint>)

private val GPX_POINT = Regex("""<(?:\w+:)?(rtept|trkpt|wpt)\b([^>]*)>""", RegexOption.IGNORE_CASE)
private val GPX_ATTR = Regex("""\b(lat|lon)\s*=\s*["']([-+0-9.eE]+)["']""")
private val GPX_NAME = Regex("""<(?:\w+:)?name>\s*(?:<!\[CDATA\[)?(.*?)(?:]]>)?\s*</(?:\w+:)?name>""",
    setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

/**
 * Lê um GPX (rota `rtept`, trilha `trkpt`; `wpt` só se não houver os outros). Sem DTD/entidades externas:
 * leitura por expressão regular, só de atributos lat/lon.
 */
fun parseGpxRoute(xml: String, fallbackName: String = "Rota"): ParsedRoute? {
    val routeOrTrack = ArrayList<RoutePoint>()
    val waypoints = ArrayList<RoutePoint>()
    for (m in GPX_POINT.findAll(xml)) {
        val attrs = GPX_ATTR.findAll(m.groupValues[2]).associate { it.groupValues[1].lowercase() to it.groupValues[2] }
        val lat = attrs["lat"]?.toDoubleOrNull() ?: continue
        val lon = attrs["lon"]?.toDoubleOrNull() ?: continue
        if (lat !in -90.0..90.0 || lon !in -180.0..180.0) continue
        if (m.groupValues[1].equals("wpt", true)) waypoints += RoutePoint(lat, lon) else routeOrTrack += RoutePoint(lat, lon)
    }
    val points = routeOrTrack.ifEmpty { waypoints }
    if (points.size < 2) return null
    val name = GPX_NAME.find(xml)?.groupValues?.get(1)?.trim()?.take(40)?.ifBlank { null } ?: fallbackName
    return ParsedRoute(name, points.take(20_000))
}

fun routeLengthKm(points: List<RoutePoint>): Double =
    points.zipWithNext().sumOf { (a, b) -> haversineKm(a.lat, a.lon, b.lat, b.lon) }

/** Distância restante pela rota a partir do ponto da rota mais próximo da posição atual. */
fun routeRemainingKm(points: List<RoutePoint>, lat: Double, lon: Double): Pair<Double, Double>? {
    if (points.size < 2) return null
    val nearest = points.indices.minBy { haversineKm(lat, lon, points[it].lat, points[it].lon) }
    val off = haversineKm(lat, lon, points[nearest].lat, points[nearest].lon)
    return routeLengthKm(points.subList(nearest, points.size)) to off
}

// ---------- USB-OTG: FTDI ----------

/**
 * Divisor de baud dos chips FTDI FT232R/FT231X (relógio 3 MHz, frações de 1/8).
 * Devolve (value, indexHigh) do pedido SET_BAUDRATE.
 */
fun ftdiBaudDivisor(baud: Int): Pair<Int, Int> {
    require(baud in 183..3_000_000)
    val fracCode = intArrayOf(0, 3, 2, 4, 1, 5, 6, 7)
    var div8 = ((3_000_000L * 8 + baud / 2) / baud).toInt() // divisor em oitavos
    if (div8 < 8) div8 = 0 // 3 Mbaud
    val integer = div8 shr 3
    val frac = fracCode[div8 and 7]
    val encoded = integer or (frac shl 14)
    return (encoded and 0xFFFF) to ((encoded shr 16) and 0xFFFF)
}

// ---------- Bateria (#49) ----------

/** Queda média em %/h entre amostras (tempo ms, nível %); null se faltam dados ou o aparelho carregou. */
fun batteryDrainPerHour(samples: List<Pair<Long, Int>>): Double? {
    if (samples.size < 2) return null
    val first = samples.first()
    val last = samples.last()
    val hours = (last.first - first.first) / 3_600_000.0
    if (hours < 0.25) return null
    if (samples.zipWithNext().any { (a, b) -> b.second > a.second }) return null
    val drop = first.second - last.second
    return (drop / hours * 10).roundToInt() / 10.0
}
