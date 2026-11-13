package com.pt2vhf.aprs.core

import java.text.Normalizer

/*
 * #77: remove acentos antes de enviar uma mensagem, para rádios e clientes que só exibem ASCII.
 * "ação" → "acao", "Ç" → "C", "º"/"ª" → "o"/"a". Só afeta o envio; a exibição do que chega (#27) não muda.
 */

private val SPECIAL = mapOf(
    'ß' to "ss", 'æ' to "ae", 'Æ' to "AE", 'œ' to "oe", 'Œ' to "OE", 'ø' to "o", 'Ø' to "O",
    'đ' to "d", 'Đ' to "D", 'ł' to "l", 'Ł' to "L", 'þ' to "th", 'Þ' to "TH", 'ð' to "d", 'Ð' to "D",
    'ı' to "i", 'º' to "o", 'ª' to "a", '°' to "o",
    '‘' to "'", '’' to "'", '‚' to ",", '“' to "\"", '”' to "\"", '„' to "\"", '«' to "\"", '»' to "\"",
    '–' to "-", '—' to "-", '−' to "-", '…' to "...", '•' to "*", '·' to ".", '€' to "EUR", '£' to "GBP",
    '¡' to "!", '¿' to "?", '×' to "x", '÷' to "/", '\u00A0' to " ",
)

/** Texto só com ASCII imprimível: letras sem acento, símbolos comuns trocados, o resto (emojis etc.) vira "?". */
fun stripAccents(text: String): String {
    val sb = StringBuilder(text.length)
    var i = 0
    while (i < text.length) {
        val cp = text.codePointAt(i)
        i += Character.charCount(cp)
        val ch = cp.toChar()
        when {
            cp in 0x20..0x7E -> sb.append(ch)
            cp > 0xFFFF -> sb.append('?')
            ch == '\n' || ch == '\r' || ch == '\t' -> sb.append(' ')
            ch in SPECIAL -> sb.append(SPECIAL.getValue(ch))
            else -> sb.append(Normalizer.normalize(ch.toString(), Normalizer.Form.NFD)
                .filter { it.code in 0x20..0x7E }.ifEmpty { "?" })
        }
    }
    return sb.toString()
}
