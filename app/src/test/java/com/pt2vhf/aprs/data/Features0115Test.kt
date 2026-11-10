package com.pt2vhf.aprs.data

import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiTextKeys
import com.pt2vhf.aprs.core.AprsMessage
import com.pt2vhf.aprs.service.AudioTnc
import com.pt2vhf.aprs.service.UsbTnc
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Features0115Test {
    private val own = "PT2VHF-9"
    private var nextId = 1L
    private fun msg(from: String, to: String, time: Long, outgoing: Boolean = false,
                    kind: AprsMessage.Kind = AprsMessage.Kind.MESSAGE) =
        ChatMessage(nextId++, from, to, "t$time", time, outgoing, if (outgoing) null else Medium.INTERNET, kind = kind)

    // ---------- #73 Minhas / Todas e Agrupadas / Lista ----------
    @Test fun classifiesThirdPartyMessages() {
        assertTrue(isThirdPartyMessage(msg("PY1AA", "PY2BB", 1), own))
        assertFalse("to me", isThirdPartyMessage(msg("PY1AA", "PT2VHF-9", 1), own))
        assertFalse("to my bare call", isThirdPartyMessage(msg("PY1AA", "PT2VHF", 1), own))
        assertFalse("mine", isThirdPartyMessage(msg("PT2VHF-9", "PY2BB", 1, outgoing = true), own))
        assertFalse("CQ is general", isThirdPartyMessage(msg("PY1AA", "CQ", 1), own))
        assertFalse("bulletin", isThirdPartyMessage(msg("PY1AA", "BLN1", 1, kind = AprsMessage.Kind.BULLETIN), own))
    }

    @Test fun mineModeIsUnchangedAndAllModeAddsThirdPartyPairs() {
        val list = listOf(
            msg("PY1AA", own, 10), msg(own, "PY1AA", 20, outgoing = true),
            msg("PY3CC", "PY4DD", 30), msg("PY4DD", "PY3CC", 40), msg("PY5EE", "CQ", 50),
        )
        val mine = privateGroups(list, own, MessageCriteria(), newestFirst = true, all = false, now = 100)
        assertEquals(conversations(list, own, MessageCriteria(), true, 100), mine)
        assertEquals(listOf("PY1AA"), mine.map { it.first })
        val all = privateGroups(list, own, MessageCriteria(), newestFirst = true, all = true, now = 100)
        assertEquals(listOf("PY3CC ↔ PY4DD", "PY1AA"), all.map { it.first })
        assertEquals(2, all.first().second.size)
    }

    @Test fun listModeIsChronologicalOnePerLine() {
        val list = listOf(msg("PY1AA", own, 10), msg("PY3CC", "PY4DD", 30), msg(own, "PY1AA", 20, outgoing = true))
        assertEquals(listOf(10L, 20L), privateTimeline(list, own, MessageCriteria(), false, all = false, now = 100).map { it.time })
        assertEquals(listOf(30L, 20L, 10L), privateTimeline(list, own, MessageCriteria(), true, all = true, now = 100).map { it.time })
    }

    @Test fun thirdPartyDoesNotPolluteCallsignSearchHistory() {
        val hits = searchCallsigns("PY", emptyList(), listOf(msg("PY3CC", "PY4DD", 1)), own)
        assertTrue(hits.isEmpty())
    }

    // ---------- #76 valores internos sempre traduzidos ----------
    @Test fun everyInternalRoleHasTranslation() {
        InternalText.ROLES.forEach { assertNotNull(it, UiTextKeys.role(it)) }
        STATION_TYPE_ORDER.forEach { assertNotNull(it, UiTextKeys.role(it)) }
        listOf('/' to '#', '/' to '&', '/' to '_', '/' to '^', '/' to 's', '/' to '[', '/' to '>', '/' to '-')
            .forEach { (t, s) -> assertNotNull(UiTextKeys.role(stationRole(t, s))) }
    }

    @Test fun everyMessageStatusHasTranslation() {
        listOf(InternalText.MSG_RECEIVED, InternalText.MSG_ACKED, InternalText.MSG_REJECTED, InternalText.MSG_SENDING,
            InternalText.MSG_NO_CONNECTION, InternalText.MSG_CANCELLED, INTERRUPTED_STATUS,
            InternalText.sent("RF"), InternalText.awaiting("APRS-IS"), InternalText.retry(3),
            InternalText.noAck(5), InternalText.noAck(7)).forEach { assertNotNull(it, UiTextKeys.messageStatus(it)) }
        assertEquals(R.string.no_ack_n, UiTextKeys.messageStatus(InternalText.noAck(7))!!.id)
        assertEquals(listOf<Any>(3), UiTextKeys.messageStatus(InternalText.retry(3))!!.args)
        // Valores gravados por versões anteriores continuam reconhecidos.
        assertNotNull(UiTextKeys.messageStatus("Aguardando ACK (2ª tentativa)"))
        assertNotNull(UiTextKeys.messageStatus("Enviada · APRS-IS"))
        assertNull(UiTextKeys.messageStatus("qualquer outra"))
        assertTrue(isPendingStatus(InternalText.retry(2)))
        assertTrue(isFailedStatus(InternalText.noAck(3)))
    }

    @Test fun everyConnectionErrorHasTranslation() {
        listOf(InternalText.ERR_UNVERIFIED, InternalText.ERR_SERVER_CLOSED, InternalText.ERR_BT_CLOSED,
            InternalText.ERR_KISS_CLOSED, InternalText.ERR_CONNECT_FAILED, InternalText.writeError("SocketException"),
            AudioTnc.ERR_PERMISSION, AudioTnc.ERR_AUDIO, UsbTnc.ERR_PERMISSION, UsbTnc.ERR_NO_DEVICE,
            UsbTnc.ERR_UNSUPPORTED, UsbTnc.ERR_OPEN, UsbTnc.ERR_DETACHED,
        ).forEach { assertNotNull(it, UiTextKeys.connectionError(it)) }
        assertEquals(listOf<Any>("SocketException"),
            UiTextKeys.connectionError(InternalText.writeError("SocketException"))!!.args)
    }

    @Test fun receptionPathsHaveTranslation() {
        assertNotNull(UiTextKeys.receptionVia(InternalText.VIA_RF_DIRECT))
        assertNotNull(UiTextKeys.receptionVia(InternalText.VIA_RF_PREFIX + "PY2XX-1"))
        assertNull(UiTextKeys.receptionVia("APRS-IS"))
    }
}
