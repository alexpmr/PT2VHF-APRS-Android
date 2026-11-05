package com.pt2vhf.aprs.data

import android.content.Context
import com.pt2vhf.aprs.BuildConfig
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Último travamento do app (#45): exceção não tratada gravada em arquivo antes do Android fechar o processo.
 * Na abertura seguinte o app oferece "Enviar log"; o texto entra no relatório de Sobre → Enviar log.
 */
object CrashLog {
    private const val FILE = "last_crash.txt"
    private const val SEEN = "last_crash_seen.txt"

    fun install(context: Context) {
        val app = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                val sw = StringWriter()
                error.printStackTrace(PrintWriter(sw))
                val stamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ROOT).format(Date())
                File(app.filesDir, FILE).writeText(
                    "PT2VHF APRS Client for Android ${BuildConfig.VERSION_NAME}\n$stamp\nThread: ${thread.name}\n\n" +
                        sw.toString().take(60_000))
            }
            previous?.uncaughtException(thread, error)
        }
    }

    /** Travamento ainda não apresentado ao usuário. */
    fun pending(context: Context): String? = File(context.filesDir, FILE).takeIf { it.exists() }?.readText()

    /** Último travamento conhecido (novo ou já visto), para o relatório. */
    fun last(context: Context): String? = pending(context) ?: File(context.filesDir, SEEN).takeIf { it.exists() }?.readText()

    fun markSeen(context: Context) {
        val f = File(context.filesDir, FILE)
        if (f.exists()) f.renameTo(File(context.filesDir, SEEN))
    }
}
