package com.pt2vhf.aprs.data

import android.content.Context
import com.pt2vhf.aprs.BuildConfig
import com.pt2vhf.aprs.core.AprsMessage
import org.json.JSONArray
import org.json.JSONObject

/**
 * Backup e restauração das configurações em arquivo JSON (#50). Formato versionado; importar valida tudo
 * antes de gravar e nunca apaga chaves que não estão no arquivo. O passcode APRS-IS não é guardado em
 * lugar nenhum (é calculado do indicativo), então nunca entra no arquivo.
 */
object SettingsBackup {
    const val FORMAT = 1
    private const val APP = "pt2vhf-aprs-android"
    private const val PREFS = "pt2vhf_aprs"

    /** Estado de sessão, pausas manuais, avisos e caches que não devem viajar para outro aparelho. */
    private val EXCLUDED_PREFIXES = listOf("update_", "tracker_explicit_off", "aprs_manual_pause", "tnc_manual_pause",
        "announced_version", "crash_", "bt_address")

    private fun excluded(key: String) = EXCLUDED_PREFIXES.any { key.startsWith(it) }

    fun export(context: Context, includeMessages: Boolean): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).all
        val values = JSONObject()
        for ((key, value) in prefs.toSortedMap()) {
            if (excluded(key)) continue
            val entry = when (value) {
                is Boolean -> JSONObject().put("t", "b").put("v", value)
                is Int -> JSONObject().put("t", "i").put("v", value)
                is Long -> JSONObject().put("t", "l").put("v", value)
                is Float -> JSONObject().put("t", "f").put("v", value.toDouble())
                is String -> JSONObject().put("t", "s").put("v", value)
                is Set<*> -> JSONObject().put("t", "ss").put("v", JSONArray(value.map { it.toString() }))
                else -> null
            } ?: continue
            values.put(key, entry)
        }
        val root = JSONObject()
            .put("format", FORMAT).put("app", APP).put("appVersion", BuildConfig.VERSION_NAME)
            .put("exportedAt", System.currentTimeMillis()).put("prefs", values)
        if (includeMessages) {
            val arr = JSONArray()
            AprsRepository.messages.value.forEach { m ->
                arr.put(JSONObject().put("id", m.id).put("from", m.from).put("to", m.to).put("text", m.text)
                    .put("time", m.time).put("out", m.outgoing).put("medium", m.medium?.name ?: "").put("via", m.via)
                    .put("msgId", m.msgId ?: "").put("status", m.status).put("kind", m.kind.name))
            }
            root.put("messages", arr)
        }
        return root.toString(2)
    }

    data class Preview(val callsign: String, val keys: Int, val messages: Int, val appVersion: String, val json: JSONObject)

    /** Valida o arquivo sem alterar nada. Lança IllegalArgumentException com o motivo. */
    fun preview(text: String): Preview {
        val root = runCatching { JSONObject(text) }.getOrElse { throw IllegalArgumentException("json") }
        require(root.optString("app") == APP) { "app" }
        val format = root.optInt("format", -1)
        require(format in 1..FORMAT) { "format" }
        val prefs = root.optJSONObject("prefs") ?: throw IllegalArgumentException("prefs")
        val call = prefs.optJSONObject("callsign")?.optString("v").orEmpty()
        return Preview(call, prefs.length(), root.optJSONArray("messages")?.length() ?: 0,
            root.optString("appVersion"), root)
    }

    /** Aplica um arquivo já validado. Devolve quantas mensagens foram importadas. */
    fun apply(context: Context, preview: Preview, importMessages: Boolean): Int {
        val prefs = preview.json.getJSONObject("prefs")
        val editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        for (key in prefs.keys()) {
            if (excluded(key)) continue
            val e = prefs.optJSONObject(key) ?: continue
            when (e.optString("t")) {
                "b" -> editor.putBoolean(key, e.optBoolean("v"))
                "i" -> editor.putInt(key, e.optInt("v"))
                "l" -> editor.putLong(key, e.optLong("v"))
                "f" -> editor.putFloat(key, e.optDouble("v").toFloat())
                "s" -> editor.putString(key, e.optString("v"))
                "ss" -> e.optJSONArray("v")?.let { arr -> editor.putStringSet(key, (0 until arr.length()).map { arr.getString(it) }.toSet()) }
            }
        }
        editor.commit()
        if (!importMessages) return 0
        val arr = preview.json.optJSONArray("messages") ?: return 0
        val list = (0 until arr.length()).mapNotNull { i ->
            runCatching {
                val o = arr.getJSONObject(i)
                ChatMessage(o.getLong("id"), o.getString("from"), o.getString("to"), o.getString("text"), o.getLong("time"),
                    o.getBoolean("out"), o.optString("medium").takeIf { it.isNotEmpty() }?.let { runCatching { Medium.valueOf(it) }.getOrNull() },
                    o.optString("via"), o.optString("msgId").ifEmpty { null }, o.optString("status"),
                    runCatching { AprsMessage.Kind.valueOf(o.optString("kind")) }.getOrDefault(AprsMessage.Kind.MESSAGE))
            }.getOrNull()
        }
        return AprsRepository.importMessages(list)
    }
}
