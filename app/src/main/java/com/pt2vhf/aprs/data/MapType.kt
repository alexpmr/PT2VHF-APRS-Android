package com.pt2vhf.aprs.data

/**
 * #94: tipo de mapa. Todos usam servidores de tiles sem chave e com uso em aplicativos permitido, com atribuição:
 * OpenStreetMap (padrão, escuro e cinza, que são o mesmo tile com filtro de cores, mesmo cache) e OpenTopoMap.
 * Satélite ficou de fora: os servidores gratuitos conhecidos proíbem uso em app ou exigem chave/conta.
 */
enum class MapType(val key: String) {
    STANDARD("standard"), DARK("dark"), GRAY("gray"), TOPO("topo"), AUTO("auto");

    /** Tipo efetivo: Automático vira Escuro quando o sistema está no modo escuro, senão Padrão. */
    fun resolve(systemDark: Boolean): MapType = if (this == AUTO) (if (systemDark) DARK else STANDARD) else this

    /** Atribuição obrigatória mostrada no canto do mapa. */
    val attribution: String get() = when (this) {
        TOPO -> "© OpenStreetMap · SRTM | © OpenTopoMap (CC-BY-SA)"
        else -> "© OpenStreetMap"
    }

    companion object {
        const val OPTION_KEY = "map_type"
        fun fromKey(key: String?): MapType = entries.firstOrNull { it.key == key } ?: STANDARD
    }
}

/** Matriz de cor 4×5 (formato do android.graphics.ColorMatrix) — puro Kotlin para poder testar fora do Android. */
object MapColors {
    val IDENTITY = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    )

    /** a ∘ b: aplica b primeiro e depois a. */
    fun concat(a: FloatArray, b: FloatArray): FloatArray {
        val out = FloatArray(20)
        for (r in 0 until 4) {
            for (c in 0 until 5) {
                var v = 0f
                for (k in 0 until 4) v += a[r * 5 + k] * b[k * 5 + c]
                if (c == 4) v += a[r * 5 + 4]
                out[r * 5 + c] = v
            }
        }
        return out
    }

    /** Aplica a matriz a uma cor RGB (0..255) — usado nos testes. */
    fun apply(m: FloatArray, r: Int, g: Int, b: Int): IntArray = IntArray(3) { row ->
        (m[row * 5] * r + m[row * 5 + 1] * g + m[row * 5 + 2] * b + m[row * 5 + 4]).toInt().coerceIn(0, 255)
    }

    /** #22: brilho só dos tiles, 50–150 %. Abaixo de 100 escurece; acima clareia em direção ao branco. */
    fun brightness(percent: Int): FloatArray {
        val f = percent.coerceIn(50, 150) / 100f
        if (f <= 1f) return floatArrayOf(
            f, 0f, 0f, 0f, 0f,
            0f, f, 0f, 0f, 0f,
            0f, 0f, f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
        val k = 2f - f
        val add = 255f * (f - 1f)
        return floatArrayOf(
            k, 0f, 0f, 0f, add,
            0f, k, 0f, 0f, add,
            0f, 0f, k, 0f, add,
            0f, 0f, 0f, 1f, 0f,
        )
    }

    /** Inverte as cores e gira o matiz 180°: fundo quase preto, nomes claros, água azul-acinzentada escura. */
    val DARK: FloatArray = run {
        val invert = floatArrayOf(
            -1f, 0f, 0f, 0f, 255f,
            0f, -1f, 0f, 0f, 255f,
            0f, 0f, -1f, 0f, 255f,
            0f, 0f, 0f, 1f, 0f,
        )
        // hue-rotate(180°) do padrão de filtros (coeficientes de luminância 0.213/0.715/0.072).
        val hue = floatArrayOf(
            -0.574f, 1.430f, 0.144f, 0f, 0f,
            0.426f, 0.430f, 0.144f, 0f, 0f,
            0.426f, 1.430f, -0.856f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f,
        )
        // Reduz contraste e escurece um pouco, para os ícones e rótulos das estações se destacarem.
        val soften = floatArrayOf(
            0.80f, 0f, 0f, 0f, 8f,
            0f, 0.80f, 0f, 0f, 10f,
            0f, 0f, 0.80f, 0f, 14f,
            0f, 0f, 0f, 1f, 0f,
        )
        concat(soften, concat(hue, invert))
    }

    /** Cinza claro/minimalista: tira a saturação e clareia levemente. */
    val GRAY: FloatArray = floatArrayOf(
        0.2126f * 0.85f, 0.7152f * 0.85f, 0.0722f * 0.85f, 0f, 30f,
        0.2126f * 0.85f, 0.7152f * 0.85f, 0.0722f * 0.85f, 0f, 30f,
        0.2126f * 0.85f, 0.7152f * 0.85f, 0.0722f * 0.85f, 0f, 30f,
        0f, 0f, 0f, 1f, 0f,
    )

    /** Matriz final para o tipo (já resolvido) e o brilho; null quando não há nada a aplicar. */
    fun forMap(type: MapType, brightnessPercent: Int): FloatArray? {
        val base = when (type) {
            MapType.DARK -> DARK
            MapType.GRAY -> GRAY
            else -> null
        }
        val b = brightnessPercent.coerceIn(50, 150)
        if (base == null) return if (b == 100) null else brightness(b)
        return if (b == 100) base else concat(brightness(b), base)
    }
}
