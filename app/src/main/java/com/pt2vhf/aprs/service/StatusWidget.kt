package com.pt2vhf.aprs.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.pt2vhf.aprs.MainActivity
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.Settings
import java.text.DateFormat
import java.util.Date

/**
 * Widget da tela inicial (#46): APRS-IS/TNC/GPS, rastreador, último beacon, botões Beacon agora e
 * Rastreador. Sem atualização periódica: o serviço chama [refresh] quando o estado muda.
 * Os botões passam pelo serviço, que aplica as mesmas autorizações de TX do app.
 */
class StatusWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) = render(context, manager, ids)

    companion object {
        private const val GREEN = 0xFF3CCF7C.toInt()
        private const val RED = 0xFFF26F70.toInt()

        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = runCatching { manager.getAppWidgetIds(ComponentName(context, StatusWidget::class.java)) }
                .getOrNull() ?: return
            if (ids.isEmpty()) return
            render(context, manager, ids)
        }

        private fun service(context: Context, code: Int, action: String): PendingIntent =
            PendingIntent.getForegroundService(context, code,
                Intent(context, AprsService::class.java).setAction(action),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        private fun render(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val settings = Settings.load(context)
            val s = AprsRepository.status.value
            val alive = AprsService.running
            val gpsOk = s.manualPosition || (s.gpsAccuracyM != null && System.currentTimeMillis() - s.gpsFixTimeMs <= 120_000L)
            val last = if (s.lastBeaconAt > 0) DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(s.lastBeaconAt)) else "—"
            val ago = if (s.lastBeaconAt > 0) " (" + UiText.text(R.string.minutes_ago, (System.currentTimeMillis() - s.lastBeaconAt) / 60_000L) + ")" else ""
            val views = RemoteViews(context.packageName, R.layout.widget_status)
            views.setTextViewText(R.id.widget_call, if (settings.configured) settings.fullCall else "PT2VHF APRS")
            views.setTextColor(R.id.widget_aprs, if (alive && s.aprsIsVerified) GREEN else RED)
            views.setTextColor(R.id.widget_tnc, if (alive && s.tncConnected) GREEN else RED)
            views.setTextColor(R.id.widget_gps, if (alive && gpsOk) GREEN else RED)
            val trackerText = if (alive && s.trackerOn) UiText.text(R.string.widget_tracker_on) else UiText.text(R.string.widget_tracker_off)
            views.setTextViewText(R.id.widget_status, when {
                !alive -> UiText.text(R.string.widget_service_stopped)
                settings.rxOnly -> trackerText + " · " + UiText.text(R.string.profile_rx)
                else -> trackerText + " · " + UiText.text(R.string.widget_last_beacon, last + ago)
            })
            views.setTextViewText(R.id.widget_tracker, UiText.text(if (alive && s.trackerOn) R.string.stop_tracker else R.string.start_tracker))
            views.setOnClickPendingIntent(R.id.widget_beacon, service(context, 41, AprsService.ACTION_BEACON))
            views.setOnClickPendingIntent(R.id.widget_tracker, service(context, 42, AprsService.ACTION_WIDGET_TRACKER))
            views.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(context, 43,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            runCatching { manager.updateAppWidget(ids, views) }
        }
    }
}
