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
}
