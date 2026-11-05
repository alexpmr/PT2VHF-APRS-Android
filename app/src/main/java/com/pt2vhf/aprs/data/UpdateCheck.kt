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

    sealed class Result {
        data class Available(val info: UpdateInfo) : Result()
        object UpToDate : Result()
        object Failure : Result()
    }

    fun fetch(): UpdateInfo? = (fetchResult() as? Result.Available)?.info

    /** Distingue "atualizado" de "falha de rede" (#39), para a nova tentativa e o resultado em Sobre. */
    fun fetchResult(): Result {
        val conn = URL(ENDPOINT).openConnection() as HttpURLConnection
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
