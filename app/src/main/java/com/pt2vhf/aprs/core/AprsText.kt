package com.pt2vhf.aprs.core

import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.util.Locale
import kotlin.math.roundToInt

/*
 * Texto APRS para apresentação (item #27).
 *
 * As linhas APRS-IS e os frames AX.25 são lidos como ISO-8859-1, o que preserva
 * cada byte original em um caractere (0..255). Aqui, somente para exibição:
 *  - texto puramente ASCII fica como está;
 *  - se os bytes formarem UTF-8 válido, decodifica como UTF-8 (acentos, emojis);
 *  - senão, interpreta como CP1252/Latin-1 (acentos de clientes antigos);
 *  - caracteres de controle viram U+FFFD, para não quebrar balões nem buscas.
 * O pacote bruto, IDs de mensagem/ACK e campos de posição não passam por aqui.
 */

private val CP1252_HIGH = charArrayOf(
    '€', '�', '‚', 'ƒ', '„', '…', '†', '‡',
    'ˆ', '‰', 'Š', '‹', 'Œ', '�', 'Ž', '�',
    '�', '‘', '’', '“', '”', '•', '–', '—',
    '˜', '™', 'š', '›', 'œ', '�', 'ž', 'Ÿ',
)

/** true quando todos os caracteres representam um byte (0..255), como a leitura ISO-8859-1 produz. */
private fun isByteString(raw: String): Boolean = raw.all { it.code <= 0xFF }

fun decodeAprsText(raw: String): String {
    if (raw.isEmpty()) return raw
    val decoded = when {
        raw.all { it.code < 0x80 } -> raw
        !isByteString(raw) -> raw // já é Unicode (ex.: digitado localmente)
        else -> {
            val bytes = ByteArray(raw.length) { raw[it].code.toByte() }
            val utf8 = runCatching {
                Charsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString()
            }.getOrNull()
            utf8 ?: buildString(raw.length) {
                for (c in raw) {
                    val code = c.code
                    append(if (code in 0x80..0x9F) CP1252_HIGH[code - 0x80] else c)
                }
            }
        }
    }
    return sanitizeControls(decoded)
}

/** Remove quebras/controles que desalinham a interface, sem apagar a informação: marca com U+FFFD. */
fun sanitizeControls(text: String): String = buildString(text.length) {
    for (c in text) {
        append(
            when {
                c == '\t' -> ' '
                c.code < 0x20 || c.code == 0x7F -> '�'
                else -> c
            }
        )
    }
}

// ---------- Beacon: privacidade, frequência de voz e altitude (item #6) ----------

/** Ambiguidade APRS (0..4): troca os últimos dígitos dos minutos por espaços, na latitude e na longitude. */
fun applyAmbiguity(lat: String, lon: String, level: Int): Pair<String, String> {
    val n = level.coerceIn(0, 4)
    if (n == 0) return lat to lon
    // lat "DDMM.mmN" -> posições 6,5,3,2 ; lon "DDDMM.mmW" -> 7,6,4,3
    val latPos = intArrayOf(6, 5, 3, 2)
    val lonPos = intArrayOf(7, 6, 4, 3)
    val la = lat.toCharArray()
    val lo = lon.toCharArray()
    for (i in 0 until n) {
        if (latPos[i] < la.size) la[latPos[i]] = ' '
        if (lonPos[i] < lo.size) lo[lonPos[i]] = ' '
    }
    return String(la) to String(lo)
}

/** Frequência de voz no formato APRS 1.2 "FFF.FFFMHz"; aceita "146.52", "146,520", "146.520 MHz". */
fun formatVoiceFrequency(input: String): String? {
    val clean = input.trim().uppercase(Locale.ROOT).removeSuffix("MHZ").trim().replace(',', '.')
    if (clean.isEmpty()) return null
    val mhz = clean.toDoubleOrNull() ?: return null
    if (mhz < 1.0 || mhz >= 1000.0) return null
    return String.format(Locale.ROOT, "%07.3fMHz", mhz)
}

/** Altitude APRS "/A=001234" (pés). */
fun formatAltitude(meters: Double?): String? {
    if (meters == null || !meters.isFinite()) return null
    val feet = (meters * 3.28084).roundToInt().coerceIn(-99999, 999999)
    return if (feet < 0) String.format(Locale.ROOT, "/A=-%05d", -feet)
    else String.format(Locale.ROOT, "/A=%06d", feet)
}

/**
 * Compõe o comentário transmitido: [frequência] [altitude] [descrição] www.PT2VHF.com,
 * sempre dentro do teto de 43 caracteres do comentário de posição.
 */
fun composeBeaconComment(description: String, voiceFrequency: String?, altitude: String?): String {
    val prefix = buildString {
        if (!voiceFrequency.isNullOrBlank()) append(voiceFrequency).append(' ')
        if (!altitude.isNullOrBlank()) append(altitude).append(' ')
    }
    val room = (43 - prefix.length).coerceAtLeast(PT2VHF_BEACON_SITE.length)
    return (prefix + beaconCommentWithSite(description, room)).take(43)
}
