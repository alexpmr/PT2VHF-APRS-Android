package com.pt2vhf.aprs.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.core.AprsWeather
import java.util.Locale

/** #58: WX snapshot and lightweight per-session charts; avoid fabricating missing measurements. */
@Composable
fun StationWeatherPanel(history: List<AprsWeather>) {
    if (history.isEmpty()) return
    var hours by remember { mutableIntStateOf(24) }
    val latest = history.maxByOrNull { it.timestamp } ?: return
    val cutoff = System.currentTimeMillis() - hours * 3_600_000L
    val visible = history.filter { it.timestamp >= cutoff }.sortedBy { it.timestamp }
    fun n(value: Double?, unit: String): String? =
        value?.let { String.format(Locale.getDefault(), "%.1f %s", it, unit) }

    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(UiText.text(R.string.wx_section) + " · " + ago(latest.timestamp),
            fontWeight = FontWeight.Bold, color = Pal.accent)
        latest.temperatureC?.let { WxMetric(UiText.text(R.string.wx_temperature), n(it, "°C") ?: "") }
        latest.humidityPercent?.let { WxMetric(UiText.text(R.string.wx_humidity), "$it %") }
        latest.pressureHpa?.let { WxMetric(UiText.text(R.string.wx_pressure), n(it, "hPa") ?: "") }
        latest.windKmh?.let { WxMetric(UiText.text(R.string.wx_wind),
            (n(it, "km/h") ?: "") + (latest.windDirectionDeg?.let { d -> " · $d°" } ?: "")) }
        latest.gustKmh?.let { WxMetric(UiText.text(R.string.wx_gust), n(it, "km/h") ?: "") }
        latest.rain1hMm?.let { WxMetric(UiText.text(R.string.wx_rain_hour), n(it, "mm") ?: "") }
        latest.rain24hMm?.let { WxMetric(UiText.text(R.string.wx_rain_day), n(it, "mm") ?: "") }
        latest.rainSinceMidnightMm?.let { WxMetric(UiText.text(R.string.wx_rain_midnight), n(it, "mm") ?: "") }
        Row {
            listOf(1, 6, 24, 168).forEach { h ->
                TextButton(onClick = { hours = h }) {
                    Text(if (h == 168) "7d" else "${h}h",
                        color = if (hours == h) Pal.accent else Pal.muted, fontSize = 12.sp)
                }
            }
        }
        if (visible.size < 2) Text(UiText.text(R.string.wx_history_insufficient),
            color = Pal.muted, fontSize = 12.sp)
        WeatherPlot(UiText.text(R.string.wx_temperature), visible, "°C") { it.temperatureC }
        WeatherPlot(UiText.text(R.string.wx_humidity), visible, "%") { it.humidityPercent?.toDouble() }
        WeatherPlot(UiText.text(R.string.wx_pressure), visible, "hPa") { it.pressureHpa }
        WeatherPlot(UiText.text(R.string.wx_wind), visible, "km/h") { it.windKmh }
        WeatherPlot(UiText.text(R.string.wx_gust), visible, "km/h") { it.gustKmh }
        WeatherPlot(UiText.text(R.string.wx_rain_hour), visible, "mm") { it.rain1hMm }
    }
}

@Composable
private fun WxMetric(name: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(name, color = Pal.muted, fontSize = 12.sp)
        Text(value, color = Pal.text, fontWeight = FontWeight.Medium, fontSize = 12.sp)
    }
}

@Composable
private fun WeatherPlot(title: String, samples: List<AprsWeather>, unit: String,
                        read: (AprsWeather) -> Double?) {
    val values = samples.mapNotNull { sample ->
        read(sample)?.let { sample.timestamp to it }
    }
    if (values.size < 2) return
    val lo = values.minOf { it.second }
    val hi = values.maxOf { it.second }
    val start = values.minOf { it.first }
    val end = values.maxOf { it.first }
    val range = (hi - lo).takeIf { it > 0.00001 } ?: 1.0
    val timeSpan = (end - start).takeIf { it > 0L } ?: 1L
    Text("$title · ${String.format(Locale.getDefault(), "%.1f", lo)}–${String.format(Locale.getDefault(), "%.1f", hi)} $unit",
        color = Pal.muted, fontSize = 12.sp)
    Canvas(Modifier.fillMaxWidth().height(76.dp).padding(bottom = 8.dp)) {
        val points = values.map { (at, value) ->
            Offset(
                ((at - start).toFloat() / timeSpan * size.width).toFloat(),
                (size.height - (value - lo).toFloat() / range.toFloat() * size.height)
            )
        }
        for (i in 1 until points.size) {
            drawLine(Pal.accent, points[i - 1], points[i], strokeWidth = 2.dp.toPx())
        }
    }
}
