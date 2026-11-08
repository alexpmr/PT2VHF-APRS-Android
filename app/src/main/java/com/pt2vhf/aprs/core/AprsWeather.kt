package com.pt2vhf.aprs.core

/**
 * Conventional uncompressed APRS weather report. Values are normalized to SI display
 * units; unavailable fields remain null. This is NOT compressed WX/Mic-E decoding.
 */
data class AprsWeather(
    val timestamp: Long,
    val temperatureC: Double? = null,
    val humidityPercent: Int? = null,
    val pressureHpa: Double? = null,
    val windDirectionDeg: Int? = null,
    val windKmh: Double? = null,
    val gustKmh: Double? = null,
    val rain1hMm: Double? = null,
    val rain24hMm: Double? = null,
    val rainSinceMidnightMm: Double? = null,
)

/** Position-bearing /_ WX and positionless _MMDDHHMM WX reports. */
fun parseAprsWeather(info: String, receivedAt: Long = System.currentTimeMillis()): AprsWeather? {
    val position = parsePosition(info)
    val text = when {
        position?.symbol == '_' -> position.comment
        info.startsWith("_") && info.length >= 9 -> info.substring(9)
        else -> return null
    }
    fun field(code: String, digits: Int): Int? =
        Regex(Regex.escape(code) + "(\\d{$digits})").find(text)?.groupValues?.get(1)?.toIntOrNull()
    val compactWind = Regex("^(\\d{3})/(\\d{3})").find(text)
    val windDir = compactWind?.groupValues?.get(1)?.toIntOrNull()
        ?: field("c", 3)
    val windMph = compactWind?.groupValues?.get(2)?.toIntOrNull()
        ?: field("s", 3)
    val tempF = Regex("t(-?\\d{2,3})").find(text)?.groupValues?.get(1)?.toIntOrNull()
    val humidity = field("h", 2)?.let { if (it == 0) 100 else it.takeIf { h -> h in 1..100 } }
    val pressure = field("b", 5)?.div(10.0)?.takeIf { it in 700.0..1100.0 }
    fun mph(value: Int?) = value?.takeIf { it in 0..250 }?.times(1.609344)
    fun rain(code: String) = field(code, 3)?.times(0.254)
    val sample = AprsWeather(
        timestamp = receivedAt,
        temperatureC = tempF?.takeIf { it in -150..160 }?.let { (it - 32) * 5.0 / 9.0 },
        humidityPercent = humidity,
        pressureHpa = pressure,
        windDirectionDeg = windDir?.takeIf { it in 0..360 },
        windKmh = mph(windMph),
        gustKmh = mph(field("g", 3)),
        rain1hMm = rain("r"),
        rain24hMm = rain("p"),
        rainSinceMidnightMm = rain("P"),
    )
    return sample.takeIf {
        it.temperatureC != null || it.humidityPercent != null || it.pressureHpa != null ||
        it.windKmh != null || it.gustKmh != null || it.rain1hMm != null ||
        it.rain24hMm != null || it.rainSinceMidnightMm != null
    }
}
