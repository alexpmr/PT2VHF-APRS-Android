package com.pt2vhf.aprs

import androidx.annotation.StringRes
import com.pt2vhf.aprs.data.INTERRUPTED_STATUS
import com.pt2vhf.aprs.data.InternalText as T
import com.pt2vhf.aprs.service.AudioTnc
import com.pt2vhf.aprs.service.UsbTnc

/** Recurso de texto + argumentos, resolvido por [UiText]. */
data class TextRes(@StringRes val id: Int, val args: List<Any> = emptyList())

/**
 * #76: mapeamento puro (sem Context) dos valores internos para os recursos traduzidos. Fica separado de [UiText]
 * para o teste de unidade conferir que todo valor interno tem tradução; null = mostrar o texto como chegou.
 */
object UiTextKeys {
    @StringRes
    fun role(raw: String): Int? = when (raw) {
        T.ROLE_DIGI -> R.string.role_digi
        T.ROLE_IGATE -> R.string.role_igate
        T.ROLE_WEATHER -> R.string.role_weather
        T.ROLE_MOBILE -> R.string.role_mobile
        T.ROLE_PERSON -> R.string.role_person
        T.ROLE_AIRCRAFT -> R.string.role_aircraft
        T.ROLE_VESSEL -> R.string.role_vessel
        T.ROLE_STATION -> R.string.role_station
        else -> null
    }

    @StringRes
    fun serverLabel(raw: String): Int? = when (raw) {
        "América do Sul (padrão)" -> R.string.server_sa
        "Mundial (automático)" -> R.string.server_global
        "América do Norte" -> R.string.server_na
        "Europa e África" -> R.string.server_eu
        "Ásia" -> R.string.server_asia
        "Oceania" -> R.string.server_oceania
        else -> null
    }

    fun messageStatus(raw: String): TextRes? = when {
        raw == T.MSG_RECEIVED -> TextRes(R.string.msg_received)
        raw == T.MSG_ACKED -> TextRes(R.string.ack_received)
        raw == T.MSG_REJECTED -> TextRes(R.string.rejected)
        raw == T.MSG_SENDING -> TextRes(R.string.msg_sending)
        raw == T.MSG_NO_CONNECTION -> TextRes(R.string.no_connection)
        raw == INTERRUPTED_STATUS -> TextRes(R.string.msg_interrupted)
        raw == T.noAck(5) -> TextRes(R.string.no_ack)
        raw.startsWith(T.MSG_NO_ACK_PREFIX) && raw.endsWith(T.MSG_NO_ACK_SUFFIX) ->
            raw.removePrefix(T.MSG_NO_ACK_PREFIX).removeSuffix(T.MSG_NO_ACK_SUFFIX).toIntOrNull()
                ?.let { TextRes(R.string.no_ack_n, listOf(it)) }
        raw == T.MSG_CANCELLED -> TextRes(R.string.msg_cancelled)
        raw.startsWith(T.MSG_SENT_PREFIX) -> TextRes(R.string.msg_sent_via, listOf(raw.removePrefix(T.MSG_SENT_PREFIX)))
        raw.startsWith(T.MSG_AWAITING_PREFIX) -> TextRes(R.string.awaiting_ack, listOf(raw.removePrefix(T.MSG_AWAITING_PREFIX)))
        raw.startsWith(T.MSG_RETRY_PREFIX) && raw.endsWith(T.MSG_RETRY_SUFFIX) ->
            raw.removePrefix(T.MSG_RETRY_PREFIX).removeSuffix(T.MSG_RETRY_SUFFIX).toIntOrNull()
                ?.let { TextRes(R.string.retry_ack, listOf(it)) }
        else -> null
    }

    fun receptionVia(raw: String): TextRes? = when {
        raw == T.VIA_RF_DIRECT -> TextRes(R.string.rf_direct)
        raw.startsWith(T.VIA_RF_PREFIX) -> TextRes(R.string.via, listOf(raw.removePrefix(T.VIA_RF_PREFIX)))
        else -> null
    }

    fun connectionError(raw: String): TextRes? = when {
        raw == T.ERR_UNVERIFIED -> TextRes(R.string.unverified_passcode)
        raw == T.ERR_SERVER_CLOSED -> TextRes(R.string.server_closed)
        raw == T.ERR_BT_CLOSED -> TextRes(R.string.bt_closed)
        raw == T.ERR_KISS_CLOSED -> TextRes(R.string.kiss_tcp_closed)
        raw == T.ERR_CONNECT_FAILED -> TextRes(R.string.connection_failed)
        raw.startsWith(T.ERR_WRITE_PREFIX) -> TextRes(R.string.err_aprs_write, listOf(raw.removePrefix(T.ERR_WRITE_PREFIX)))
        raw == AudioTnc.ERR_PERMISSION -> TextRes(R.string.err_mic_permission)
        raw == AudioTnc.ERR_AUDIO -> TextRes(R.string.err_audio_open)
        raw == UsbTnc.ERR_PERMISSION -> TextRes(R.string.err_usb_permission)
        raw == UsbTnc.ERR_NO_DEVICE -> TextRes(R.string.err_usb_none)
        raw == UsbTnc.ERR_UNSUPPORTED -> TextRes(R.string.err_usb_unsupported)
        raw == UsbTnc.ERR_OPEN -> TextRes(R.string.err_usb_open)
        raw == UsbTnc.ERR_DETACHED -> TextRes(R.string.err_usb_detached)
        else -> null
    }
}
