package com.pt2vhf.aprs.data

import java.util.Locale

/*
 * #70: importar configurações do APRSdroid. Aceita o arquivo de preferências do Android
 * (shared_prefs XML) e o perfil exportado em JSON plano. Só as chaves com equivalente no PT2VHF
 * são convertidas; o passcode nunca é importado (é calculado do indicativo).
 * Lógica pura (sem org.json) para testar na JVM.
 */

/** Preferência convertida para o formato do backup do PT2VHF: t = "s" (texto), "i" (inteiro), "b" (sim/não). */
data class ImportedPref(val key: String, val type: String, val value: String)

data class AprsDroidConversion(
    val prefs: List<ImportedPref>,
    /** Chaves do APRSdroid que foram aproveitadas. */
    val recognized: List<String>,
    /** Quantas chaves do arquivo não têm equivalente e foram ignoradas. */
    val ignored: Int,
    val callsign: String,
)

private val XML_ENTRY = Regex(
    "<(string|boolean|int|long|float)\\s+name=\"([^\"]+)\"(?:\\s+value=\"([^\"]*)\")?\\s*(?:/>|>([\\s\\S]*?)</\\1>)")

private fun unescapeXml(text: String) = text.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
    .replace("&apos;", "'").replace("&#39;", "'").replace("&amp;", "&")

/** Lê pares chave/valor de um XML de preferências ou de um objeto JSON plano. null se não for nenhum dos dois. */
fun parseFlatPrefs(text: String): Map<String, String>? {
    val trimmed = text.trim().removePrefix("﻿")
    if (trimmed.startsWith("<")) {
        val map = LinkedHashMap<String, String>()
        XML_ENTRY.findAll(trimmed).forEach { m ->
            val value = m.groupValues[3].ifEmpty { m.groupValues[4] }
            map[unescapeXml(m.groupValues[2])] = unescapeXml(value)
        }
        return map.takeIf { it.isNotEmpty() }
    }
    if (trimmed.startsWith("{")) return runCatching { FlatJson(trimmed).parseObject() }.getOrNull()
    return null
}

/** Analisador JSON mínimo: devolve só os valores simples do primeiro nível, como texto. */
private class FlatJson(private val s: String) {
    private var i = 0
    fun parseObject(): Map<String, String> {
        val map = LinkedHashMap<String, String>()
        expect('{'); ws()
        if (peek() == '}') { i++; return map }
        while (true) {
            ws(); val key = string(); ws(); expect(':'); ws()
            val value = value()
            if (value != null) map[key] = value
            ws()
            when (s[i++]) { ',' -> continue; '}' -> return map; else -> error("json") }
        }
    }
    private fun value(): String? = when (peek()) {
        '"' -> string()
        '{', '[' -> { skipNested(); null }
        else -> {
            val start = i
            while (i < s.length && s[i] !in ",}] \n\r\t") i++
            s.substring(start, i).takeIf { it != "null" }
        }
    }
    private fun skipNested() {
        var depth = 0
        do {
            when (s[i]) { '{', '[' -> depth++; '}', ']' -> depth--; '"' -> { string(); continue } }
            i++
        } while (depth > 0)
    }
    private fun string(): String {
        expect('"')
        val out = StringBuilder()
        while (true) {
            val c = s[i++]
            when (c) {
                '"' -> return out.toString()
                '\\' -> when (val e = s[i++]) {
                    'n' -> out.append('\n'); 't' -> out.append('\t'); 'r' -> out.append('\r')
                    'u' -> { out.append(s.substring(i, i + 4).toInt(16).toChar()); i += 4 }
                    else -> out.append(e)
                }
                else -> out.append(c)
            }
        }
    }
    private fun ws() { while (i < s.length && s[i].isWhitespace()) i++ }
    private fun peek() = s[i]
    private fun expect(c: Char) { if (s[i++] != c) error("json") }
}

/**
 * Converte preferências do APRSdroid. Exige a chave "callsign" (sem ela o arquivo não é do APRSdroid).
 * Valores fora de faixa são descartados, nunca ajustados em silêncio.
 */
fun convertAprsDroid(raw: Map<String, String>): AprsDroidConversion? {
    val callRaw = raw["callsign"]?.trim()?.uppercase(Locale.ROOT)?.takeIf { it.isNotEmpty() } ?: return null
    val out = ArrayList<ImportedPref>()
    val used = ArrayList<String>()
    val base = callRaw.substringBefore('-')
    out += ImportedPref("callsign", "s", base); used += "callsign"
    val ssid = raw["ssid"]?.trim()?.toIntOrNull() ?: callRaw.substringAfter('-', "").toIntOrNull()
    if (ssid != null && ssid in 0..15) { out += ImportedPref("ssid", "i", ssid.toString()); used += "ssid" }
    raw["symbol"]?.takeIf { it.length == 2 }?.let {
        out += ImportedPref("symbol_table", "s", it[0].toString())
        out += ImportedPref("symbol", "s", it[1].toString()); used += "symbol"
    }
    raw["status"]?.takeIf { it.isNotBlank() }?.let { out += ImportedPref("comment", "s", it.trim()); used += "status" }
    raw["digi_path"]?.trim()?.takeIf { it.isNotEmpty() }?.let {
        out += ImportedPref("rf_path", "s", it.uppercase(Locale.ROOT)); used += "digi_path"
    }
    raw["tcp.server"]?.trim()?.takeIf { it.isNotEmpty() }?.let { server ->
        val host = server.substringBefore(':')
        val port = server.substringAfter(':', "").toIntOrNull()
        if (host.isNotEmpty()) out += ImportedPref("server", "s", host)
        if (port != null && port in 1..65535) out += ImportedPref("port", "i", port.toString())
        used += "tcp.server"
    }
    raw["tcp.filter"]?.trim()?.takeIf { it.isNotEmpty() }?.let { out += ImportedPref("filter", "s", it); used += "tcp.filter" }
    raw["interval"]?.trim()?.toIntOrNull()?.takeIf { it in 1..1440 }?.let {
        out += ImportedPref("periodic_interval", "i", (it * 60).toString()); used += "interval"
    }
    raw["smartbeaconing"]?.trim()?.lowercase(Locale.ROOT)?.takeIf { it == "true" || it == "false" }?.let {
        out += ImportedPref("beacon_mode", "s", if (it == "true") "smart" else "periodic"); used += "smartbeaconing"
    }
    val ignored = raw.keys.count { it !in used && it != "passcode" }
    return AprsDroidConversion(out, used, ignored, base + if (ssid != null && ssid in 1..15) "-$ssid" else "")
}
