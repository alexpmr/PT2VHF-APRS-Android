package com.pt2vhf.aprs.data

/**
 * #63: tamanho dos símbolos no mapa (Configurações → Tela e mapa). Padrão Médio.
 * Tamanhos em dp; o rótulo do indicativo acompanha a proporção.
 */
enum class MapIconSize(val key: String, val dp: Int) {
    SMALL("small", 26), MEDIUM("medium", 34), LARGE("large", 44);

    /** Texto do rótulo proporcional ao ícone (12 sp no Médio, nunca menor que 10 sp). */
    val labelSp: Float get() = maxOf(10f, 12f * dp / MEDIUM.dp)

    companion object {
        const val OPTION_KEY = "map_icon_size"
        /** Área mínima de toque recomendada pelo Android. */
        const val TOUCH_DP = 48
        fun fromKey(key: String?): MapIconSize = entries.firstOrNull { it.key == key } ?: MEDIUM
    }
}

/** Pixels reais do ícone para a densidade do aparelho, arredondados e nunca abaixo de 1. */
fun mapIconPx(size: MapIconSize, density: Float): Int = maxOf(1, Math.round(size.dp * density))
