package com.pt2vhf.aprs.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import com.pt2vhf.aprs.data.MapIconSize
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.flow.MutableStateFlow

/** #63: troca de tamanho feita em Configurações chega ao mapa já aberto por baixo do diálogo. */
internal object MapIconPrefs {
    val changed = MutableStateFlow<MapIconSize?>(null)
}

/**
 * APRS symbol sprites from hessu/aprs-symbols, source and credit:
 * https://github.com/hessu/aprs-symbols
 * Atlas has 16 columns x 6 rows (ASCII symbol codes ! through ~).
 * Primary / and secondary backslash have distinct images; alphanumerics on
 * the table selector are overlays on the secondary table.
 *
 * #63: o atlas é o de alta resolução (células de 128 px). Para o mapa, cada símbolo é
 * reduzido com filtragem para o tamanho real em pixels (tamanho escolhido × densidade),
 * com sombra leve para destacar sobre qualquer mapa. Cache por símbolo+overlay+tamanho.
 */
internal object AprsSymbolIcons {
    private const val CELL = 128
    private val icons = ConcurrentHashMap<String, Bitmap>()
    private val sheets = ConcurrentHashMap<Int, Bitmap>()
    private val guard = Any()
    /** Bitmaps já dimensionados para o mapa; ~200 símbolos distintos × 3 tamanhos cabem com folga. */
    private val sized = object : LruCache<String, Bitmap>(16 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    private fun sheet(context: Context, table: Int): Bitmap? {
        sheets[table]?.let { return it }
        synchronized(guard) {
            sheets[table]?.let { return it }
            return runCatching {
                context.assets.open("aprs/aprs-symbols-$CELL-$table.png").use { stream ->
                    BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply { inScaled = false })
                }
            }.getOrNull()?.also { if (it.width >= CELL * 16 && it.height >= CELL * 6) sheets[table] = it }
        }
    }

    /** Símbolo na resolução da fonte (128 px), para listas e seletores que redimensionam por conta própria. */
    fun icon(context: Context, tableChar: Char, symbol: Char): Drawable? {
        val bitmap = source(context, tableChar, symbol) ?: return null
        return BitmapDrawable(context.resources, bitmap).apply { setBounds(0, 0, CELL, CELL) }
    }

    private fun source(context: Context, tableChar: Char, symbol: Char): Bitmap? {
        val index = symbol.code - '!'.code
        if (index !in 0 until 94) return null
        val secondary = tableChar != '/'
        val sheet = sheet(context, if (secondary) 1 else 0) ?: return null
        val overlay = if (secondary && tableChar != '\\' && tableChar.isLetterOrDigit()) tableChar else null
        val key = (if (secondary) "1" else "0") + symbol + (overlay?.toString() ?: "")
        return icons.getOrPut(key) {
            val image = Bitmap.createBitmap(sheet, (index % 16) * CELL, (index / 16) * CELL, CELL, CELL)
                .copy(Bitmap.Config.ARGB_8888, true)
            if (overlay != null) {
                // Overlay desenhado na resolução da fonte para continuar nítido após a redução.
                val canvas = Canvas(image)
                val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.BLACK
                    textSize = CELL * 0.48f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    style = Paint.Style.STROKE
                    strokeWidth = CELL * 0.075f
                    strokeJoin = Paint.Join.ROUND
                    textAlign = Paint.Align.CENTER
                }
                val face = Paint(border).apply { style = Paint.Style.FILL; color = Color.WHITE }
                val y = CELL / 2f - (face.descent() + face.ascent()) / 2
                canvas.drawText(overlay.toString(), CELL / 2f, y, border)
                canvas.drawText(overlay.toString(), CELL / 2f, y, face)
            }
            image
        }
    }

    /** Visible fallback for unknown symbols (#1): a neutral "?" disc, never another real symbol. */
    private fun fallback(): Bitmap = icons.getOrPut("fallback") {
        val image = Bitmap.createBitmap(CELL, CELL, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)
        val disc = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(120, 130, 145) }
        canvas.drawCircle(CELL / 2f, CELL / 2f, CELL / 2.6f, disc)
        val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE; textSize = CELL * 0.54f; textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("?", CELL / 2f, CELL / 2f - (text.descent() + text.ascent()) / 2, text)
        image
    }

    /**
     * Ícone do mapa com [sizePx] pixels reais (#63). A sombra cabe numa margem de ~8 % para não
     * cortar; o bitmap tem densidade do aparelho para que osmdroid o desenhe 1:1, sem nova escala.
     */
    fun mapIcon(context: Context, tableChar: Char, symbol: Char, sizePx: Int): Drawable {
        val key = "$tableChar$symbol|$sizePx"
        val bitmap = synchronized(sized) { sized.get(key) } ?: run {
            val base = source(context, tableChar, symbol) ?: fallback()
            val margin = maxOf(2, sizePx / 12)
            val out = Bitmap.createBitmap(sizePx + margin * 2, sizePx + margin * 2, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            val scaled = Bitmap.createScaledBitmap(base, sizePx, sizePx, true)
            // Sombra: alfa do próprio símbolo, desfocado, levemente deslocado para baixo.
            val blur = Paint().apply { maskFilter = BlurMaskFilter(margin * 0.9f, BlurMaskFilter.Blur.NORMAL) }
            val offset = IntArray(2)
            val alpha = scaled.extractAlpha(blur, offset)
            val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(150, 0, 0, 0) }
            canvas.drawBitmap(alpha, (margin + offset[0]).toFloat(), (margin + offset[1] + margin / 3).toFloat(), shadow)
            alpha.recycle()
            canvas.drawBitmap(scaled, null, Rect(margin, margin, margin + sizePx, margin + sizePx),
                Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
            if (scaled !== base) scaled.recycle()
            out.density = context.resources.displayMetrics.densityDpi
            synchronized(sized) { sized.put(key, out) }
            out
        }
        return BitmapDrawable(context.resources, bitmap).apply { setBounds(0, 0, bitmap.width, bitmap.height) }
    }
}
