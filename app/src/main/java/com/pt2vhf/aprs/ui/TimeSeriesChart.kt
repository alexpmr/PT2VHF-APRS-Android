package com.pt2vhf.aprs.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import java.util.Locale
import kotlin.math.abs

/** Uma amostra real; o gráfico nunca cria pontos intermediários. */
data class ChartPoint(val time: Long, val value: Double)

private fun fmt(v: Double, decimals: Int) = String.format(Locale.getDefault(), "%.${decimals}f", v)

/**
 * Série temporal simples (#58.6): eixo com mínimo/máximo reais e unidade, horário inicial e final,
 * lacunas visíveis quando faltam amostras (a linha é interrompida) e valor ao tocar no ponto.
 * [bars] = medidas acumuladas (chuva), desenhadas como barras em vez de linha contínua.
 */
@Composable
fun TimeSeriesChart(title: String, unit: String, points: List<ChartPoint>, bars: Boolean = false, decimals: Int = 1) {
    val data = remember(points) { points.sortedBy { it.time } }
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(title + if (unit.isNotBlank()) " ($unit)" else "", color = Pal.text, fontSize = 13.sp,
            fontWeight = FontWeight.Medium)
        if (data.size < 2) {
            Text(UiText.text(R.string.chart_not_enough), color = Pal.muted, fontSize = 12.sp)
            return@Column
        }
        val lo = data.minOf { it.value }
        val hi = data.maxOf { it.value }
        val start = data.first().time
        val end = data.last().time
        var selected by remember(data) { mutableStateOf<ChartPoint?>(null) }
        // Lacuna: intervalo bem maior que o típico entre amostras (mínimo 30 min).
        val gaps = data.zipWithNext { a, b -> b.time - a.time }
        val typical = gaps.sorted()[gaps.size / 2]
        val gapLimit = maxOf(typical * 3, 30 * 60_000L)
        val baseLo = if (bars) minOf(0.0, lo) else lo
        val range = (hi - baseLo).takeIf { it > 1e-9 } ?: 1.0
        val span = (end - start).takeIf { it > 0 } ?: 1L
        Text(UiText.text(R.string.chart_range, fmt(lo, decimals), fmt(hi, decimals), unit),
            color = Pal.muted, fontSize = 11.sp)
        Canvas(Modifier.fillMaxWidth().height(96.dp)
            .semantics { contentDescription = "$title: ${fmt(lo, decimals)}–${fmt(hi, decimals)} $unit" }
            .pointerInput(data) {
                detectTapGestures { tap ->
                    val x = tap.x / size.width
                    val t = start + (x * span).toLong()
                    selected = data.minByOrNull { abs(it.time - t) }
                }
            }) {
            fun px(p: ChartPoint) = Offset(
                (p.time - start).toFloat() / span * size.width,
                size.height - ((p.value - baseLo) / range).toFloat() * size.height,
            )
            // Linhas de referência de mínimo e máximo.
            val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            drawLine(Pal.line, Offset(0f, 0f), Offset(size.width, 0f), pathEffect = dash)
            drawLine(Pal.line, Offset(0f, size.height), Offset(size.width, size.height), pathEffect = dash)
            if (bars) {
                val w = maxOf(2.dp.toPx(), size.width / (data.size * 2f))
                data.forEach { p ->
                    val o = px(p)
                    val zero = size.height - ((0.0 - baseLo) / range).toFloat() * size.height
                    drawRect(Pal.accent, Offset(o.x - w / 2, minOf(o.y, zero)), Size(w, abs(zero - o.y).coerceAtLeast(1f)))
                }
            } else {
                for (i in 1 until data.size) {
                    if (data[i].time - data[i - 1].time > gapLimit) continue
                    drawLine(Pal.accent, px(data[i - 1]), px(data[i]), strokeWidth = 2.dp.toPx())
                }
                data.forEach { drawCircle(Pal.accent, 2.dp.toPx(), px(it)) }
            }
            selected?.let { drawCircle(Pal.yellow, 5.dp.toPx(), px(it)) }
        }
        Row(Modifier.fillMaxWidth()) {
            val label: (Long) -> String = if (span > 20 * 3_600_000L) ::fullTime else ::hhmm
            Text(label(start), color = Pal.muted, fontSize = 10.sp, modifier = Modifier.weight(1f))
            Text(label(end), color = Pal.muted, fontSize = 10.sp)
        }
        selected?.let {
            Text(UiText.text(R.string.chart_selected, fmt(it.value, decimals), unit, fullTime(it.time)),
                color = Pal.yellow, fontSize = 12.sp)
        }
    }
}

internal fun fullTime(t: Long): String =
    java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT, UiText.locale())
        .format(java.util.Date(t))
