package com.pt2vhf.aprs.core

import java.util.Locale

/*
 * #69: decodificador de pacote APRS colado ou escolhido no log. Reaproveita os analisadores do app;
 * o que eles não entendem (ex.: Mic-E, posição comprimida) aparece como tipo identificado, sem inventar valores.
 * As chaves são estáveis e a UI traduz os rótulos.
 */
enum class DecodedKey {
    SOURCE, DESTINATION, THIRD_PARTY, TYPE, LATITUDE, LONGITUDE, GRID, SYMBOL, COURSE, SPEED, ALTITUDE, PHG,
    COMMENT, STATUS, OBJECT_NAME, OBJECT_STATE, ADDRESSEE, MESSAGE_TEXT, MESSAGE_ID, ACK_ID, REPLY_ACK,
    TEMPERATURE, HUMIDITY, PRESSURE, WIND, GUST, RAIN_1H, RAIN_24H, RAIN_MIDNIGHT,
    TELEMETRY_SEQUENCE, TELEMETRY_ANALOG, TELEMETRY_BITS, NOT_DECODED,
}

data class DecodedField(val key: DecodedKey, val value: String)

data class DecodedPacket(val packet: AprsPacket, val type: String, val path: PathSummary, val fields: List<DecodedField>)

private val ALT = Regex("/A=(-?\\d{6})")
private val PHG_RE = Regex("PHG(\\d{4})")

private fun f1(v: Double) = "%.1f".format(Locale.ROOT, v)
private fun f5(v: Double) = "%.5f".format(Locale.ROOT, v)

fun decodePacket(raw: String): DecodedPacket? {
    val packet = AprsPacket.parse(raw.trim()) ?: return null
    val info = packet.info
    val type = packetType(info)
    val out = ArrayList<DecodedField>()
    fun add(key: DecodedKey, value: String?) { if (!value.isNullOrBlank()) out += DecodedField(key, value) }
    add(DecodedKey.SOURCE, packet.source)
    add(DecodedKey.DESTINATION, packet.destination)
    add(DecodedKey.THIRD_PARTY, packet.thirdPartyGateway)
    add(DecodedKey.TYPE, type)

    fun position(pos: Position) {
        add(DecodedKey.LATITUDE, f5(pos.lat))
        add(DecodedKey.LONGITUDE, f5(pos.lon))
        add(DecodedKey.GRID, maidenhead(pos.lat, pos.lon))
        add(DecodedKey.SYMBOL, "${pos.symbolTable}${pos.symbol}")
        pos.course?.let { add(DecodedKey.COURSE, "$it° ${compassName(it % 360)}") }
        pos.speedKmh?.let { add(DecodedKey.SPEED, "${f1(it)} km/h") }
    }
    fun extras(text: String) {
        ALT.find(text)?.groupValues?.get(1)?.toIntOrNull()?.let { add(DecodedKey.ALTITUDE, "%.0f m".format(Locale.ROOT, it * 0.3048)) }
        PHG_RE.find(text)?.groupValues?.get(1)?.let { add(DecodedKey.PHG, it) }
    }
    fun weather(w: AprsWeather) {
        w.temperatureC?.let { add(DecodedKey.TEMPERATURE, "${f1(it)} °C") }
        w.humidityPercent?.let { add(DecodedKey.HUMIDITY, "$it %") }
        w.pressureHpa?.let { add(DecodedKey.PRESSURE, "${f1(it)} hPa") }
        w.windKmh?.let { kmh -> add(DecodedKey.WIND, "${f1(kmh)} km/h" + (w.windDirectionDeg?.let { " $it°" } ?: "")) }
        w.gustKmh?.let { add(DecodedKey.GUST, "${f1(it)} km/h") }
        w.rain1hMm?.let { add(DecodedKey.RAIN_1H, "${f1(it)} mm") }
        w.rain24hMm?.let { add(DecodedKey.RAIN_24H, "${f1(it)} mm") }
        w.rainSinceMidnightMm?.let { add(DecodedKey.RAIN_MIDNIGHT, "${f1(it)} mm") }
    }

    when (type) {
        "position", "weather" -> {
            val pos = parsePosition(info)
            if (pos != null) {
                position(pos)
                parseAprsWeather(info, 0L)?.let { weather(it) }
                if (pos.symbol != '_') add(DecodedKey.COMMENT, decodeAprsText(pos.comment))
                extras(pos.comment)
            } else if (info.startsWith("_")) parseAprsWeather(info, 0L)?.let { weather(it) }
            else add(DecodedKey.NOT_DECODED, info)
        }
        "object", "item" -> {
            val obj = parseObject(info)
            if (obj != null) {
                add(DecodedKey.OBJECT_NAME, obj.name)
                add(DecodedKey.OBJECT_STATE, if (obj.alive) "alive" else "killed")
                position(obj.position)
                add(DecodedKey.COMMENT, decodeAprsText(obj.position.comment))
                extras(obj.position.comment)
            } else add(DecodedKey.NOT_DECODED, info)
        }
        "message", "bulletin", "ack", "telemetry" -> {
            val msg = parseMessage(info)
            if (msg != null) {
                add(DecodedKey.ADDRESSEE, msg.addressee)
                when (msg.kind) {
                    AprsMessage.Kind.ACK, AprsMessage.Kind.REJ -> add(DecodedKey.ACK_ID,
                        (if (msg.kind == AprsMessage.Kind.ACK) "ack " else "rej ") + msg.id)
                    else -> { add(DecodedKey.MESSAGE_TEXT, decodeAprsText(msg.text)); add(DecodedKey.MESSAGE_ID, msg.id) }
                }
                add(DecodedKey.REPLY_ACK, msg.replyAckId)
            } else parseTelemetryFrame(info, 0L)?.let { t ->
                add(DecodedKey.TELEMETRY_SEQUENCE, t.sequence)
                add(DecodedKey.TELEMETRY_ANALOG, t.analog.joinToString(" · ") { v -> v?.let { f1(it) } ?: "—" })
                if (t.bits.isNotEmpty()) add(DecodedKey.TELEMETRY_BITS, t.bits.joinToString("") { if (it) "1" else "0" })
                add(DecodedKey.COMMENT, t.comment)
            } ?: add(DecodedKey.NOT_DECODED, info)
        }
        "status" -> add(DecodedKey.STATUS, decodeAprsText(info.substring(1)))
        else -> add(DecodedKey.NOT_DECODED, info)
    }
    return DecodedPacket(packet, type, describePath(packet), out)
}

/**
 * #69: frequências APRS mais usadas por região (referência; confira o plano de banda local).
 * Regiões são códigos ISO de país ou grupos especiais ("*EU" Europa/África, "*SAT" ISS e satélites).
 */
data class AprsFrequency(val mhz: String, val regions: List<String>)

val APRS_FREQUENCIES = listOf(
    AprsFrequency("145.570", listOf("BR")),
    AprsFrequency("144.390", listOf("US", "CA", "MX", "CL", "CO", "ID", "MY")),
    AprsFrequency("144.800", listOf("*EU", "RU", "ZA")),
    AprsFrequency("144.930", listOf("AR", "UY")),
    AprsFrequency("145.175", listOf("AU")),
    AprsFrequency("144.575", listOf("NZ")),
    AprsFrequency("144.640", listOf("CN", "TW", "JP")),
    AprsFrequency("145.525", listOf("TH")),
    AprsFrequency("145.825", listOf("*SAT")),
)
