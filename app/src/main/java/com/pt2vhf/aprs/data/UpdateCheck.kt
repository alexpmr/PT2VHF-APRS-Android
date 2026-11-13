package com.pt2vhf.aprs.data

import com.pt2vhf.aprs.BuildConfig
import com.pt2vhf.aprs.core.isNewerVersion
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(val version: String, val notes: String, val downloadUrl: String, val page: String)

/** Consulta somente o site oficial (mesma API do desktop), produto "aprs-android". */
object UpdateCheck {
    const val ENDPOINT = "https://www.pt2vhf.com/update.php?product=aprs-android"
    const val SITE = "https://www.pt2vhf.com/aprs-android"

    /** Mesmo formato aceito pelo site (stations_lib.php): letras e dígitos, até dois "/" (ex.: CT/PY2XYZ/P). */
    private val CALLSIGN = Regex("^(?=.*[0-9])(?=.*[A-Z])[A-Z0-9]{1,8}(/[A-Z0-9]{1,8}){0,2}$")
    /** Indicativo de exemplo: quem nunca configurou a estação não entra na lista. */
    private const val EXAMPLE_CALL = "PT2VHF"

    /**
     * #84: indicativo informado na consulta de versão (lista de estações em www.pt2vhf.com), ou null.
     * Só o indicativo, sem SSID; nunca localização nem locator. Null se a opção estiver desligada ou a
     * estação não estiver configurada (indicativo vazio, de exemplo ou inválido; posição manual sem lat/lon).
     */
    fun reportedCallsign(settings: Settings): String? {
        if (!settings.shareCallsign) return null
        if (settings.positionSource == "manual" && (settings.manualLat == null || settings.manualLon == null)) return null
        val call = settings.callsign.trim().uppercase().substringBefore('-')
        if (call.isEmpty() || call == EXAMPLE_CALL || call.length > 16 || !CALLSIGN.matches(call)) return null
        return call
    }

    /** URL da consulta: igual à de antes quando não há indicativo a informar. */
    fun endpoint(call: String?): String =
        if (call.isNullOrEmpty()) ENDPOINT else ENDPOINT + "&call=" + call.replace("/", "%2F")

    sealed class Result {
        data class Available(val info: UpdateInfo) : Result()
        object UpToDate : Result()
        object Failure : Result()
    }

    fun fetch(call: String? = null): UpdateInfo? = (fetchResult(call) as? Result.Available)?.info

    /** Distingue "atualizado" de "falha de rede" (#39), para a nova tentativa e o resultado em Sobre. */
    fun fetchResult(call: String? = null): Result {
        val conn = URL(endpoint(call)).openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = false
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        conn.setRequestProperty("User-Agent", "PT2VHF-APRS-Android/${BuildConfig.VERSION_NAME}")
        return try {
            if (conn.responseCode != 200) return Result.Failure
            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            if (json.optString("status") != "available") return Result.UpToDate
            val version = json.optString("version")
            if (!isNewerVersion(version, BuildConfig.VERSION_NAME)) return Result.UpToDate
            val files = json.optJSONArray("files")
            val url = (0 until (files?.length() ?: 0)).map { files!!.getJSONObject(it) }
                .firstOrNull { it.optString("name").endsWith(".apk") }?.optString("url")
                ?.takeIf { it.startsWith("https://www.pt2vhf.com/") } ?: SITE
            Result.Available(UpdateInfo(version, json.optString("notes"), url, json.optString("page", SITE)))
        } catch (e: Exception) {
            Result.Failure
        } finally {
            conn.disconnect()
        }
    }
}
