package com.pt2vhf.aprs.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.pt2vhf.aprs.BuildConfig
import com.pt2vhf.aprs.MainActivity
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.core.isNewerVersion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Aviso de nova versão (#39): verificação na abertura, ao voltar ao primeiro plano e a cada ~12 h pelo
 * serviço, nova tentativa 30 min após falha de rede, indicador persistente, notificação única por versão
 * e "Ignorar esta versão". Fonte única: o site oficial (UpdateCheck). Nunca instala sozinho.
 */
object UpdateManager {
    data class State(
        val available: UpdateInfo? = null,
        val lastCheck: Long = 0L,
        /** "" (nunca), "ok" (atualizado), "new", "fail". */
        val lastResult: String = "",
        val checking: Boolean = false,
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()
    private val running = AtomicBoolean(false)
    private var loaded = false
    const val PERIODIC_MS = 12 * 3_600_000L
    const val RESUME_MS = 3_600_000L
    private const val RETRY_MS = 30 * 60_000L
    private const val CHANNEL = "updates"

    @Synchronized private fun load(context: Context) {
        if (loaded) return
        loaded = true
        val version = Settings.readOption(context, "update_version", "")
        val available = if (version.isNotBlank() && isNewerVersion(version, BuildConfig.VERSION_NAME))
            UpdateInfo(version, Settings.readOption(context, "update_notes", ""),
                Settings.readOption(context, "update_url", UpdateCheck.SITE), UpdateCheck.SITE) else null
        _state.value = State(available, Settings.readOption(context, "update_last_check", "0").toLongOrNull() ?: 0L,
            Settings.readOption(context, "update_last_result", "").let { if (it == "new" && available == null) "ok" else it })
    }

    fun current(context: Context): State { load(context); return _state.value }

    fun ignoredVersion(context: Context): String = Settings.readOption(context, "update_ignored", "")
    fun ignore(context: Context, version: String) = Settings.saveOption(context, "update_ignored", version)

    /**
     * Verifica se a última tentativa tem mais de [minIntervalMs] (ou se [force]). Falha de rede é silenciosa
     * e agenda nova tentativa em 30 min. [fromBackground] permite a notificação do Android.
     */
    fun maybeCheck(context: Context, minIntervalMs: Long, force: Boolean = false, fromBackground: Boolean = false) {
        val app = context.applicationContext
        load(app)
        val now = System.currentTimeMillis()
        val st = _state.value
        val interval = if (st.lastResult == "fail") minOf(minIntervalMs, RETRY_MS) else minIntervalMs
        // #86: indicativo ainda não informado nesta versão (app novo/atualizado ou indicativo trocado) → consulta já.
        val key = UpdateCheck.reportKey(UpdateCheck.reportedCallsign(Settings.load(app)), BuildConfig.VERSION_NAME)
        val pendingReport = key.isNotEmpty() && key != Settings.readOption(app, "update_reported_key", "")
        if (!force && !pendingReport && now - st.lastCheck < interval) return
        if (!running.compareAndSet(false, true)) return
        _state.value = st.copy(checking = true)
        Thread({
            try {
                val call = UpdateCheck.reportedCallsign(Settings.load(app))
                val outcome = runCatching { UpdateCheck.fetchResult(call) }.getOrElse { UpdateCheck.Result.Failure }
                if (outcome != UpdateCheck.Result.Failure)
                    Settings.saveOption(app, "update_reported_key", UpdateCheck.reportKey(call, BuildConfig.VERSION_NAME))
                val time = System.currentTimeMillis()
                val next = when (outcome) {
                    is UpdateCheck.Result.Available -> State(outcome.info, time, "new")
                    UpdateCheck.Result.UpToDate -> State(null, time, "ok")
                    UpdateCheck.Result.Failure -> _state.value.copy(lastCheck = time, lastResult = "fail", checking = false)
                }
                _state.value = next.copy(checking = false)
                Settings.saveOption(app, "update_last_check", time.toString())
                Settings.saveOption(app, "update_last_result", next.lastResult)
                when (outcome) {
                    is UpdateCheck.Result.Available -> {
                        Settings.saveOption(app, "update_version", outcome.info.version)
                        Settings.saveOption(app, "update_notes", outcome.info.notes)
                        Settings.saveOption(app, "update_url", outcome.info.downloadUrl)
                        if (fromBackground) notifyOnce(app, outcome.info)
                    }
                    UpdateCheck.Result.UpToDate -> Settings.saveOption(app, "update_version", "")
                    UpdateCheck.Result.Failure -> {}
                }
            } finally {
                running.set(false)
                if (_state.value.checking) _state.value = _state.value.copy(checking = false)
            }
        }, "update-check").apply { isDaemon = true; start() }
    }

    /** Uma notificação por versão, respeitando a opção e a permissão de notificações. */
    private fun notifyOnce(context: Context, info: UpdateInfo) {
        if (!Settings.load(context).updateNotify) return
        if (Settings.readOption(context, "update_notified", "") == info.version) return
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        nm.createNotificationChannel(NotificationChannel(CHANNEL, UiText.text(R.string.updates_channel),
            NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(context, 7, Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra(MainActivity.EXTRA_SHOW_UPDATE, true),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_aprs)
            .setContentTitle(UiText.text(R.string.new_version, info.version))
            .setContentText(UiText.text(R.string.update_notification_body))
            .setAutoCancel(true)
            .setContentIntent(open)
            .build()
        runCatching { nm.notify(9100, n) }
        Settings.saveOption(context, "update_notified", info.version)
    }
}
