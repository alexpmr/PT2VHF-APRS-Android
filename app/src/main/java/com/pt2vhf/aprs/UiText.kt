package com.pt2vhf.aprs

import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
import java.util.Locale

/** Translations use Android resource qualifiers. The default values are English. */
class Pt2vhfApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UiText.initialize(this)
        com.pt2vhf.aprs.data.CrashLog.install(this) // #45
    }
}

object UiText {
    private lateinit var context: Context

    fun initialize(application: Context) {
        context = application.applicationContext
    }

    fun locale(): Locale = context.resources.configuration.locales[0]

    fun text(@StringRes id: Int, vararg arguments: Any): String =
        if (arguments.isEmpty()) context.getString(id) else context.getString(id, *arguments)

    fun role(raw: String): String = text(
        when (raw) {
            "Digipeater" -> R.string.role_digi
            "iGate" -> R.string.role_igate
            "Estação meteorológica" -> R.string.role_weather
            "Móvel" -> R.string.role_mobile
            "Pessoa" -> R.string.role_person
            "Aeronave" -> R.string.role_aircraft
            "Embarcação" -> R.string.role_vessel
            else -> R.string.role_station
        }
    )

    fun serverLabel(raw: String): String {
        val resource = when (raw) {
            "América do Sul (padrão)" -> R.string.server_sa
            "Mundial (automático)" -> R.string.server_global
            "América do Norte" -> R.string.server_na
            "Europa e África" -> R.string.server_eu
            "Ásia" -> R.string.server_asia
            "Oceania" -> R.string.server_oceania
            else -> return raw
        }
        return text(resource)
    }

    /** Wire-format values, callsigns and raw APRS packets are never translated. */
    fun messageStatus(raw: String): String = when {
        raw == "Recebida" -> text(R.string.msg_received)
        raw == "ACK recebido" -> text(R.string.ack_received)
        raw == "Rejeitada" -> text(R.string.rejected)
        raw == "Enviando" -> text(R.string.msg_sending)
        raw == "Sem conexão para enviar" -> text(R.string.no_connection)
        raw == "Sem ACK após 5 tentativas" -> text(R.string.no_ack)
        raw.startsWith("Sem ACK após ") && raw.endsWith(" tentativas") ->
            raw.removePrefix("Sem ACK após ").removeSuffix(" tentativas").toIntOrNull()
                ?.let { text(R.string.no_ack_n, it) } ?: raw
        raw == "Cancelada" -> text(R.string.msg_cancelled)
        raw.startsWith("Enviada · ") -> text(R.string.msg_sent_via, raw.removePrefix("Enviada · "))
        raw.startsWith("Aguardando ACK · ") -> text(R.string.awaiting_ack, raw.removePrefix("Aguardando ACK · "))
        raw.startsWith("Aguardando ACK (") && raw.endsWith("ª tentativa)") -> {
            val attempt = raw.substringAfter("Aguardando ACK (").substringBefore("ª tentativa)").toIntOrNull()
            if (attempt != null) text(R.string.retry_ack, attempt) else raw
        }
        else -> raw
    }

    fun receptionVia(raw: String): String = when {
        raw == "RF direto" -> text(R.string.rf_direct)
        raw.startsWith("RF via ") -> text(R.string.via, raw.removePrefix("RF via "))
        else -> raw
    }

    fun connectionError(raw: String): String = when (raw) {
        "Passcode não verificado pelo servidor" -> text(R.string.unverified_passcode)
        "Servidor encerrou a conexão" -> text(R.string.server_closed)
        "Conexão Bluetooth encerrada" -> text(R.string.bt_closed)
        "Conexão KISS TCP encerrada" -> text(R.string.kiss_tcp_closed)
        "Falha ao conectar" -> text(R.string.connection_failed)
        "Permissão de microfone negada" -> text(R.string.err_mic_permission)
        "Falha ao abrir o áudio" -> text(R.string.err_audio_open)
        "Permissão USB negada" -> text(R.string.err_usb_permission)
        "Nenhum TNC USB conectado" -> text(R.string.err_usb_none)
        "Adaptador USB não suportado" -> text(R.string.err_usb_unsupported)
        "Falha ao abrir o USB" -> text(R.string.err_usb_open)
        "USB desconectado" -> text(R.string.err_usb_detached)
        else -> raw
    }
}
