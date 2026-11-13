package com.pt2vhf.aprs.data

/**
 * #82: recomendações de SSID de WB4APR ("APRS SSID Recommendations", 2012). São só orientação:
 * o campo continua aceitando qualquer valor de 0 a 15. Os textos de cada uso ficam nos recursos
 * `ssid_use_0` … `ssid_use_15` (5 idiomas).
 */
object SsidAdvice {
    /** Os 16 valores possíveis do SSID AX.25 (4 bits), na ordem da tabela. */
    val VALUES: List<Int> = (0..15).toList()

    /** Sugestão para o perfil, igual aos padrões de [Settings.presetFor]; null quando não há uma só. */
    fun suggestedFor(profile: String): Int? = when (profile) {
        Settings.PROFILE_BASE -> 0
        Settings.PROFILE_MOBILE -> 9
        Settings.PROFILE_PORTABLE -> 7
        else -> null
    }

    /** Indicativo completo como aparece na rede: sem sufixo para 0. */
    fun fullCall(callsign: String, ssid: Int): String {
        val base = callsign.trim().uppercase().substringBefore('-')
        return if (ssid in 1..15) "$base-$ssid" else base
    }
}
