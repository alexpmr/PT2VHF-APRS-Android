package com.pt2vhf.aprs.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Typeface
import android.view.MotionEvent
import org.osmdroid.views.MapView
import org.osmdroid.views.Projection
import org.osmdroid.views.overlay.Marker
import kotlin.math.abs

/**
 * Marcador de estação (#63): ícone já dimensionado + rótulo do indicativo desenhado direto no
 * canvas (sem um bitmap por estação, o que pesaria com ícones maiores) e área de toque de
 * pelo menos 48 dp em volta do símbolo, mesmo no tamanho Pequeno.
 */
internal class StationMarker(
    map: MapView,
    private val label: String,
    private val iconPx: Int,
    private val touchPx: Int,
    labelTextPx: Float,
    density: Float,
    /** #67: segunda linha (clima), em azul; esmaecida quando o dado é antigo. */
    private val subLabel: String = "",
    private val subDim: Boolean = false,
) : Marker(map) {
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = labelTextPx
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val stroke = Paint(text).apply {
        color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 3f * density
        strokeJoin = Paint.Join.ROUND
    }
    private val sub = Paint(text).apply { color = if (subDim) 0xFF9FB1C4.toInt() else 0xFF7CC4FF.toInt(); textSize = labelTextPx * 0.9f }
    private val subStroke = Paint(stroke).apply { textSize = labelTextPx * 0.9f }
    private val gap = 3f * density
    private val labelWidth = if (label.isEmpty()) 0f else text.measureText(label)
    private val screen = Point()

    init {
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
    }

    override fun draw(canvas: Canvas, pj: Projection) {
        super.draw(canvas, pj)
        if ((label.isEmpty() && subLabel.isEmpty()) || !isEnabled) return
        pj.toPixels(position, screen)
        val x = screen.x + iconPx / 2f + gap
        val y = screen.y - (text.descent() + text.ascent()) / 2
        val savedText = text.alpha; val savedStroke = stroke.alpha
        val a = (alpha * 255).toInt().coerceIn(0, 255)
        text.alpha = a; stroke.alpha = a
        canvas.drawText(label, x, y, stroke)
        canvas.drawText(label, x, y, text)
        if (subLabel.isNotEmpty()) {
            val y2 = y + text.textSize * 1.05f
            canvas.drawText(subLabel, x, y2, subStroke)
            canvas.drawText(subLabel, x, y2, sub)
        }
        text.alpha = savedText; stroke.alpha = savedStroke
    }

    override fun hitTest(event: MotionEvent, mapView: MapView): Boolean {
        if (!isEnabled) return false
        mapView.projection.toPixels(position, screen)
        val dx = event.x - screen.x
        val dy = event.y - screen.y
        val half = maxOf(iconPx, touchPx) / 2f
        if (abs(dx) <= half && abs(dy) <= half) return true
        // O rótulo também abre a ficha, como antes.
        return labelWidth > 0f && dx > 0 && dx <= iconPx / 2f + gap + labelWidth && abs(dy) <= iconPx / 2f
    }
}

/** #66: grupo de estações — círculo com a contagem, desenhado direto no canvas (sem bitmap). */
internal class ClusterMarker(map: MapView, count: Int, private val radiusPx: Float, density: Float) : Marker(map) {
    private val label = if (count > 999) "999+" else count.toString()
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xE02F86D6.toInt() }
    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; style = Paint.Style.STROKE; strokeWidth = 2f * density
    }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textAlign = Paint.Align.CENTER; textSize = radiusPx * 0.8f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val screen = Point()

    override fun draw(canvas: Canvas, pj: Projection) {
        if (!isEnabled) return
        pj.toPixels(position, screen)
        canvas.drawCircle(screen.x.toFloat(), screen.y.toFloat(), radiusPx, fill)
        canvas.drawCircle(screen.x.toFloat(), screen.y.toFloat(), radiusPx, ring)
        canvas.drawText(label, screen.x.toFloat(), screen.y - (text.descent() + text.ascent()) / 2, text)
    }

    override fun hitTest(event: MotionEvent, mapView: MapView): Boolean {
        if (!isEnabled) return false
        mapView.projection.toPixels(position, screen)
        val dx = event.x - screen.x
        val dy = event.y - screen.y
        return dx * dx + dy * dy <= radiusPx * radiusPx * 1.5f
    }
}
