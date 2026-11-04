package com.pt2vhf.aprs.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import java.util.concurrent.ConcurrentHashMap

/**
 * APRS symbol sprites from hessu/aprs-symbols, source and credit:
 * https://github.com/hessu/aprs-symbols
 * Atlas has 16 columns x 6 rows (ASCII symbol codes ! through ~).
 * Primary / and secondary backslash have distinct images; alphanumerics on
 * the table selector are overlays on the secondary table.
 */
internal object AprsSymbolIcons {
    private const val CELL = 48
    private val icons = ConcurrentHashMap<String, Bitmap>()
    private val sheets = ConcurrentHashMap<Int, Bitmap>()
    private val guard = Any()

    private fun sheet(context: Context, table: Int): Bitmap? {
        sheets[table]?.let { return it }
        synchronized(guard) {
            sheets[table]?.let { return it }
            return runCatching {
                context.assets.open("aprs/aprs-symbols-48-$table.png").use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()?.also { if (it.width >= CELL * 16 && it.height >= CELL * 6) sheets[table] = it }
        }
    }

    fun icon(context: Context, tableChar: Char, symbol: Char): Drawable? {
        val index = symbol.code - '!'.code
        if (index !in 0 until 94) return null
        val secondary = tableChar != '/'
        val sheet = sheet(context, if (secondary) 1 else 0) ?: return null
        val overlay = if (secondary && tableChar != '\\' && tableChar.isLetterOrDigit()) tableChar else null
        val key = (if (secondary) "1" else "0") + symbol + (overlay?.toString() ?: "")
        val bitmap = icons.getOrPut(key) {
            val image = Bitmap.createBitmap(sheet, (index % 16) * CELL, (index / 16) * CELL, CELL, CELL)
                .copy(Bitmap.Config.ARGB_8888, true)
            if (overlay != null) {
                val canvas = Canvas(image)
                val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    textSize = 23f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    style = Paint.Style.STROKE
                    strokeWidth = 3.5f
                    textAlign = Paint.Align.CENTER
                }
                val face = Paint(border).apply { style = Paint.Style.FILL; color = Color.WHITE }
                canvas.drawText(overlay.toString(), CELL / 2f, CELL / 2f + 8, border)
                canvas.drawText(overlay.toString(), CELL / 2f, CELL / 2f + 8, face)
            }
            image
        }
        return BitmapDrawable(context.resources, bitmap).apply {
            // Atlas dimensions are physical pixels. osmdroid uses drawable intrinsic bounds.
            setBounds(0, 0, CELL, CELL)
        }
    }

    /** Visible fallback for unknown symbols (#1): a neutral "?" disc, never another real symbol. */
    private fun fallback(): Bitmap = icons.getOrPut("fallback") {
        val image = Bitmap.createBitmap(CELL, CELL, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        val disc = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(120, 130, 145) }
        canvas.drawCircle(CELL / 2f, CELL / 2f, CELL / 2.6f, disc)
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; textSize = 26f; textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("?", CELL / 2f, CELL / 2f + 9, text)
        image
    }

    private val labeledCache = ConcurrentHashMap<String, Bitmap>()

    /**
     * Symbol plus callsign label to its right, like other APRS maps (#1). Returns the drawable and the
     * horizontal anchor (0..1) that keeps the symbol centre on the station position.
     */
    fun labeled(context: Context, tableChar: Char, symbol: Char, label: String, density: Float): Pair<Drawable, Float> {
        val key = "$tableChar$symbol|$label|$density"
        val bitmap = labeledCache.getOrPut(key) {
            val base = (icon(context, tableChar, symbol) as? BitmapDrawable)?.bitmap ?: fallback()
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE; textSize = 12f * density
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val stroke = Paint(textPaint).apply { color = Color.BLACK; style = Paint.Style.STROKE; strokeWidth = 3f * density / 2 }
            val textWidth = if (label.isEmpty()) 0 else (textPaint.measureText(label) + 6 * density).toInt()
            val image = Bitmap.createBitmap(CELL + textWidth, CELL, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(image)
            canvas.drawBitmap(base, 0f, 0f, null)
            if (label.isNotEmpty()) {
                val y = CELL / 2f + textPaint.textSize / 3
                canvas.drawText(label, CELL + 2 * density, y, stroke)
                canvas.drawText(label, CELL + 2 * density, y, textPaint)
            }
            image
        }
        if (labeledCache.size > 3000) labeledCache.clear()
        val drawable = BitmapDrawable(context.resources, bitmap).apply { setBounds(0, 0, bitmap.width, bitmap.height) }
        return drawable to (CELL / 2f) / bitmap.width
    }
}
