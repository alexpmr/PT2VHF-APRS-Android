package com.pt2vhf.aprs.core

import java.util.Locale

/**
 * Telemetria APRS (APRS 1.0.1, cap. 13): quadros "T#" e as definições PARM./UNIT./EQNS./BITS.
 * enviadas como mensagem endereçada à própria estação. Valores ausentes ficam nulos.
 */
data class TelemetryFrame(
    val time: Long,
    val sequence: String,
    /** Até 5 canais analógicos brutos; null quando o canal não veio no quadro. */
    val analog: List<Double?>,
    /** 8 bits digitais (true = 1); vazio quando não vieram. */
    val bits: List<Boolean>,
    val comment: String = "",
)

data class TelemetryDefinitions(
    /** Nomes dos 5 canais analógicos seguidos dos 8 bits (até 13 entradas). */
    val names: List<String> = emptyList(),
    val units: List<String> = emptyList(),
    /** a, b, c por canal analógico: valor = a·x² + b·x + c. */
    val equations: List<Triple<Double, Double, Double>> = emptyList(),
    /** Bits que indicam "ativo" (BITS.), 8 posições. */
    val activeBits: List<Boolean> = emptyList(),
    val projectTitle: String = "",
) {
    fun analogName(i: Int): String? = names.getOrNull(i)?.takeIf { it.isNotBlank() }
    fun analogUnit(i: Int): String? = units.getOrNull(i)?.takeIf { it.isNotBlank() }
    fun bitName(i: Int): String? = names.getOrNull(5 + i)?.takeIf { it.isNotBlank() }
    fun bitUnit(i: Int): String? = units.getOrNull(5 + i)?.takeIf { it.isNotBlank() }

    /** Valor escalonado; sem equação definida, o padrão do protocolo é 0,1,0 (valor bruto). */
    fun scaled(i: Int, raw: Double): Double {
        val (a, b, c) = equations.getOrNull(i) ?: return raw
        return a * raw * raw + b * raw + c
    }

    fun hasEquation(i: Int): Boolean = equations.getOrNull(i) != null

    /** Bit "ligado" segundo BITS.; sem BITS., 1 = ativo. */
    fun bitActive(i: Int, value: Boolean): Boolean = if (activeBits.size > i) value == activeBits[i] else value

    val isEmpty: Boolean get() = names.isEmpty() && units.isEmpty() && equations.isEmpty() &&
        activeBits.isEmpty() && projectTitle.isBlank()
}

private val SEQUENCE = Regex("^(\\d{1,3}|MIC)$", RegexOption.IGNORE_CASE)

/** "T#005,199,000,255,073,123,01101001" ou "T#MIC199,000,..." (e variantes com decimais). */
fun parseTelemetryFrame(info: String, time: Long): TelemetryFrame? {
    if (!info.startsWith("T#")) return null
    val body = info.substring(2)
    val parts = body.split(',')
    if (parts.size < 2) return null
    var seq = parts[0].trim()
    val values = ArrayList<String>(parts.drop(1))
    // "T#MIC199,..." junta a sequência MIC ao primeiro valor.
    if (seq.length > 3 && seq.uppercase(Locale.ROOT).startsWith("MIC")) {
        values.add(0, seq.substring(3)); seq = "MIC"
    }
    if (!SEQUENCE.matches(seq)) return null
    val analog = (0 until 5).map { i -> values.getOrNull(i)?.trim()?.toDoubleOrNull() }
    if (analog.all { it == null }) return null
    val bitsField = values.getOrNull(5)?.trim().orEmpty()
    val bitDigits = bitsField.take(8)
    val bits = if (bitDigits.length == 8 && bitDigits.all { it == '0' || it == '1' })
        bitDigits.map { it == '1' } else emptyList()
    val comment = if (bits.isNotEmpty()) bitsField.drop(8).trim() else ""
    return TelemetryFrame(time, seq.uppercase(Locale.ROOT), analog, bits, comment)
}

/**
 * Aplica uma mensagem de definição (texto depois de ":ADDRESSEE:") às definições atuais.
 * Devolve null quando o texto não é PARM./UNIT./EQNS./BITS.
 */
fun applyTelemetryDefinition(current: TelemetryDefinitions, text: String): TelemetryDefinitions? {
    val clean = text.substringBefore('{').trimEnd()
    fun fields(prefix: String) = clean.removePrefix(prefix).split(',').map { it.trim() }
    return when {
        clean.startsWith("PARM.") -> current.copy(names = fields("PARM.").take(13))
        clean.startsWith("UNIT.") -> current.copy(units = fields("UNIT.").take(13))
        clean.startsWith("EQNS.") -> {
            val numbers = fields("EQNS.").map { it.toDoubleOrNull() }
            val eqs = (0 until 5).map { i ->
                val a = numbers.getOrNull(i * 3); val b = numbers.getOrNull(i * 3 + 1); val c = numbers.getOrNull(i * 3 + 2)
                if (a == null || b == null || c == null) null else Triple(a, b, c)
            }
            // Canal sem coeficientes válidos fica com o padrão do protocolo (0,1,0 = bruto).
            if (eqs.all { it == null }) current
            else current.copy(equations = eqs.map { it ?: Triple(0.0, 1.0, 0.0) })
        }
        clean.startsWith("BITS.") -> {
            val body = clean.removePrefix("BITS.")
            val digits = body.take(8)
            if (digits.length < 8 || digits.any { it != '0' && it != '1' }) current
            else current.copy(activeBits = digits.map { it == '1' },
                projectTitle = body.drop(8).removePrefix(",").trim())
        }
        else -> null
    }
}

/**
 * Lê o endereçado e o texto de uma mensagem APRS (":ADDRESSEE:texto") sem interpretar ACK/ID.
 * Usado para localizar definições de telemetria da estação.
 */
fun telemetryDefinitionTarget(info: String): Pair<String, String>? {
    if (!info.startsWith(":") || info.length < 11 || info[10] != ':') return null
    val text = info.substring(11)
    if (!(text.startsWith("PARM.") || text.startsWith("UNIT.") || text.startsWith("EQNS.") || text.startsWith("BITS.")))
        return null
    return info.substring(1, 10).trim().uppercase(Locale.ROOT) to text
}
