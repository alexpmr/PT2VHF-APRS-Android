package com.pt2vhf.aprs.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings as AndroidSettings
import androidx.core.app.NotificationCompat
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.data.Settings
import com.pt2vhf.aprs.UiText

/**
 * #106: um canal de notificação do Android por tipo de aviso, com som escolhido no app.
 *
 * O Android não deixa o app trocar o som de um canal já criado. Por isso cada troca de som apaga o canal e cria outro
 * com id novo (`base_vN`). A versão 0 usa o id antigo (`messages`, `alerts`, `sound_ack`, `sound_digi`) para não
 * perder o que o usuário já ajustou no sistema antes da 1.4.
 */
enum class NotificationType(val base: String, private val titleRes: Int, val importance: Int) {
    PERSONAL("messages", R.string.notif_type_personal, NotificationManager.IMPORTANCE_HIGH),
    BULLETIN("msg_bulletin", R.string.notif_type_bulletin, NotificationManager.IMPORTANCE_DEFAULT),
    GROUP("msg_group", R.string.notif_type_group, NotificationManager.IMPORTANCE_DEFAULT),
    ACK("sound_ack", R.string.sound_ack_channel, NotificationManager.IMPORTANCE_DEFAULT),
    DIGI("sound_digi", R.string.sound_digi_channel, NotificationManager.IMPORTANCE_DEFAULT),
    ALERTS("alerts", R.string.alerts_channel, NotificationManager.IMPORTANCE_DEFAULT);

    fun title(): String = UiText.text(titleRes)
}

object NotificationTypes {
    /** Valor salvo de som: "" = padrão do sistema, [SILENT] = sem som, ou a URI de um som do aparelho. */
    const val SILENT = "silent"

    private fun soundKey(t: NotificationType) = "notif_sound_${t.name.lowercase()}"
    private fun versionKey(t: NotificationType) = "notif_ver_${t.name.lowercase()}"

    fun sound(context: Context, t: NotificationType): String = Settings.readOption(context, soundKey(t), "")

    fun channelId(context: Context, t: NotificationType): String =
        channelIdFor(t.base, Settings.readOption(context, versionKey(t), "0").toIntOrNull() ?: 0)

    /** Cria (ou mantém) os canais de todos os tipos com o som escolhido. */
    fun ensureChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        NotificationType.entries.forEach { t -> runCatching { nm.createNotificationChannel(buildChannel(context, t)) } }
    }

    /** Troca o som do tipo: apaga o canal atual e cria um novo com o som escolhido. */
    fun setSound(context: Context, t: NotificationType, value: String) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val oldId = channelId(context, t)
        val next = (Settings.readOption(context, versionKey(t), "0").toIntOrNull() ?: 0) + 1
        Settings.saveOption(context, soundKey(t), value)
        Settings.saveOption(context, versionKey(t), next.toString())
        runCatching { nm?.deleteNotificationChannel(oldId) }
        runCatching { nm?.createNotificationChannel(buildChannel(context, t)) }
    }

    private fun buildChannel(context: Context, t: NotificationType): NotificationChannel =
        NotificationChannel(channelId(context, t), t.title(), t.importance).apply {
            when (val v = sound(context, t)) {
                "" -> Unit // som padrão do sistema
                SILENT -> setSound(null, null)
                else -> setSound(Uri.parse(v), AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            }
        }

    /** Nome do som atual para mostrar na tela. */
    fun soundLabel(context: Context, t: NotificationType): String = when (val v = sound(context, t)) {
        "" -> UiText.text(R.string.notif_sound_default)
        SILENT -> UiText.text(R.string.notif_sound_silent)
        else -> runCatching { RingtoneManager.getRingtone(context, Uri.parse(v))?.getTitle(context) }.getOrNull()
            ?: UiText.text(R.string.notif_sound_custom)
    }

    /** Intent do seletor de sons do Android, já com padrão e silencioso como opções. */
    fun pickerIntent(context: Context, t: NotificationType): Intent =
        Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, t.title())
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            val existing: Uri? = when (val v = sound(context, t)) {
                "" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                SILENT -> null
                else -> Uri.parse(v)
            }
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, existing)
        }

    /** Converte a resposta do seletor no valor salvo. */
    fun valueFromPicker(uri: Uri?): String = when {
        uri == null -> SILENT
        uri == RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) -> ""
        else -> uri.toString()
    }

    /** Abre as configurações do canal no Android (vibração, aparecer na tela, Não perturbe). */
    fun channelSettingsIntent(context: Context, t: NotificationType): Intent =
        Intent(AndroidSettings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            .putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName)
            .putExtra(AndroidSettings.EXTRA_CHANNEL_ID, channelId(context, t))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /** "Testar": aviso de exemplo no canal do tipo, com o mesmo som e as mesmas regras do aviso real. */
    fun test(context: Context, t: NotificationType) {
        ensureChannels(context)
        val n = NotificationCompat.Builder(context, channelId(context, t))
            .setSmallIcon(R.drawable.ic_stat_aprs)
            .setContentTitle(t.title())
            .setContentText(UiText.text(R.string.notif_test_body))
            .setAutoCancel(true)
            .setTimeoutAfter(8_000L)
            .build()
        runCatching { context.getSystemService(NotificationManager::class.java)?.notify(TEST_ID + t.ordinal, n) }
    }

    private const val TEST_ID = 0x7E57_000
}

/** #106: id do canal para a versão [version]; a versão 0 mantém o id antigo. */
fun channelIdFor(base: String, version: Int): String = if (version <= 0) base else "${base}_v$version"
