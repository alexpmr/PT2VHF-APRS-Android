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
) : Marker(map) {
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = labelTextPx
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val stroke = Paint(text).apply {
        color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 3f * density
        strokeJoin = Paint.Join.ROUND
    }
    private val gap = 3f * density
    private val labelWidth = if (label.isEmpty()) 0f else text.measureText(label)
    private val screen = Point()

    init {
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
    }

    override fun draw(canvas: Canvas, pj: Projection) {
        super.draw(canvas, pj)
        if (label.isEmpty() || !isEnabled) return
        pj.toPixels(position, screen)
        val x = screen.x + iconPx / 2f + gap
        val y = screen.y - (text.descent() + text.ascent()) / 2
        val savedText = text.alpha; val savedStroke = stroke.alpha
        val a = (alpha * 255).toInt().coerceIn(0, 255)
        text.alpha = a; stroke.alpha = a
        canvas.drawText(label, x, y, stroke)
        canvas.drawText(label, x, y, text)
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
