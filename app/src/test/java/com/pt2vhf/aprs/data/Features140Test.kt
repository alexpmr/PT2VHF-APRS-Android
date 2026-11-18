package com.pt2vhf.aprs.data

import com.pt2vhf.aprs.core.AprsMessage
import com.pt2vhf.aprs.core.parseMessage
import com.pt2vhf.aprs.service.channelIdFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** v1.4: #101 ACK de mensagens e #106 canais de notificação. */
class Features140Test {
    @Test fun ackFromSameCallsignMatches() {
        assertTrue(ackMatchesDestination("PT2ATC-7", "PT2ATC-7"))
        assertTrue(ackMatchesDestination("pt2atc", "PT2ATC"))
    }

    @Test fun ackFromSsidMatchesDestinationTypedWithoutSsid() {
        // Mensagem para PT2ATC aceita por PT2ATC-7: o ACK vem de PT2ATC-7 (#101 causa 2).
        assertTrue(ackMatchesDestination("PT2ATC", "PT2ATC-7"))
        assertTrue(ackMatchesDestination("PT2ATC-7", "PT2ATC"))
    }

    @Test fun ackFromOtherSsidOrCallDoesNotMatch() {
        assertFalse(ackMatchesDestination("PT2ATC-7", "PT2ATC-9"))
        assertFalse(ackMatchesDestination("PT2ATC", "PT2ABC"))
        assertFalse(ackMatchesDestination("PT2AT", "PT2ATC-7"))
    }

    @Test fun ackLeavesFromTheAddressUsedBySender() {
        assertEquals("PT2VHF", ackSourceFor("PT2VHF", "PT2VHF-9"))
        assertEquals("PT2VHF-9", ackSourceFor("PT2VHF-9", "PT2VHF-9"))
        assertEquals("PT2VHF", ackSourceFor("PT2VHF", "PT2VHF"))
    }

    @Test fun ackBodyToleratesTrailingSpaceAndEmptyReplyAck() {
        val a = parseMessage(":PT2VHF-9 :ack12 ")!!
        assertEquals(AprsMessage.Kind.ACK, a.kind)
        assertEquals("12", a.id)
        val b = parseMessage(":PT2VHF-9 :ack7}")!!
        assertEquals(AprsMessage.Kind.ACK, b.kind)
        assertEquals("7", b.id)
        assertNull(b.replyAckId)
        assertEquals(AprsMessage.Kind.REJ, parseMessage(":PT2VHF   :rej3")!!.kind)
    }

    @Test fun messageIdSurvivesTrailingSpace() {
        val m = parseMessage(":PT2VHF   :Ola tudo bem{42 ")!!
        assertEquals("42", m.id)
        assertEquals("Ola tudo bem", m.text)
    }

    @Test fun channelVersionZeroKeepsLegacyId() {
        assertEquals("messages", channelIdFor("messages", 0))
        assertEquals("messages_v3", channelIdFor("messages", 3))
    }
}
