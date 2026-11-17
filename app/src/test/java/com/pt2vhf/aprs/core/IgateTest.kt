package com.pt2vhf.aprs.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IgateTest {
    private fun send(r: GateResult) = (r as GateResult.Send).line
    private fun skip(r: GateResult) = (r as GateResult.Skip).reason

    @Test fun rfToIsAddsQarAndKeepsOnlyLastUsedMark() {
        val line = send(rfToIs("PY6ABC-9>APRS,PY6DIG*,WIDE1*,WIDE2-1:!1258.00S/03830.00W>teste", "PY6TL-10"))
        assertEquals("PY6ABC-9>APRS,PY6DIG,WIDE1*,WIDE2-1,qAR,PY6TL-10:!1258.00S/03830.00W>teste", line)
    }

    @Test fun rfToIsBlocksNoGateMarks() {
        for (hop in listOf("TCPIP*", "TCPXX", "NOGATE", "RFONLY"))
            assertEquals(GateSkip.NO_GATE_PATH, skip(rfToIs("PY6ABC>APRS,$hop:>status", "PY6TL")))
    }

    @Test fun rfToIsSkipsQueriesAndQConstructs() {
        assertEquals(GateSkip.QUERY, skip(rfToIs("PY6ABC>APRS,WIDE1-1:?IGATE?", "PY6TL")))
        assertEquals(GateSkip.MALFORMED, skip(rfToIs("PY6ABC>APRS,qAR,PY1X:>x", "PY6TL")))
        assertEquals(GateSkip.MALFORMED, skip(rfToIs("lixo sem cabecalho", "PY6TL")))
    }

    @Test fun rfToIsUnwrapsThirdPartyOnlyWhenInnerIsAllowed() {
        // Mensagem que um iGate passou da internet para o RF: interno tem TCPIP, não volta para a internet.
        assertEquals(GateSkip.NO_GATE_PATH,
            skip(rfToIs("PY6IGT>APRS,WIDE1-1:}PT2VHF>APRS,TCPIP,PY6IGT*::PY6ABC   :oi{1", "PY6TL")))
        val line = send(rfToIs("PY6GW>APRS:}PY6XYZ>APRS,PY6DIG*:>radio", "PY6TL"))
        assertEquals("PY6XYZ>APRS,PY6DIG*,qAR,PY6TL:>radio", line)
    }

    @Test fun rfToIsTruncatesAtLineBreak() {
        assertEquals("PY6ABC>APRS,qAR,PY6TL:>a", send(rfToIs("PY6ABC>APRS:>a\r\nPY6Z>APRS:>b", "PY6TL")))
    }

    private val local = setOf("PY6ABC-7")

    @Test fun isToRfOnlyMessagesToLocalStations() {
        val line = send(isToRf("PT2VHF-9>APPT2V,TCPIP*,qAC,T2BRAZIL::PY6ABC-7 :ola{12", "PY6TL-10", "APPT2V",
            listOf("WIDE1-1")) { it in local })
        assertEquals("PY6TL-10>APPT2V,WIDE1-1:}PT2VHF-9>APPT2V,TCPIP,PY6TL-10*::PY6ABC-7 :ola{12", line)
        assertEquals(GateSkip.NOT_LOCAL, skip(isToRf("PT2VHF>APRS,TCPIP*::PY1ZZZ   :ola{1", "PY6TL", "APPT2V",
            emptyList()) { it in local }))
        assertEquals(GateSkip.NOT_MESSAGE, skip(isToRf("PT2VHF>APRS,TCPIP*:!1258.00S/03830.00W>", "PY6TL",
            "APPT2V", emptyList()) { true }))
        assertEquals(GateSkip.BULLETIN, skip(isToRf("PT2VHF>APRS,TCPIP*::BLN1     :aviso", "PY6TL", "APPT2V",
            emptyList()) { true }))
    }

    @Test fun isToRfAcksAlsoGoAndLocalSenderIsSkipped() {
        assertTrue(isToRf("PT2VHF>APRS,TCPIP*::PY6ABC-7 :ack12", "PY6TL", "APPT2V", emptyList()) { it in local }
            is GateResult.Send)
        assertEquals(GateSkip.SENDER_IS_LOCAL, skip(isToRf("PY6ABC-7>APRS,TCPIP*::PY6ABC-7 :x{1", "PY6TL",
            "APPT2V", emptyList()) { it in local }))
        assertEquals(GateSkip.OWN, skip(isToRf("PY6TL>APRS,TCPIP*::PY6ABC-7 :x{1", "PY6TL", "APPT2V",
            emptyList()) { it in local }))
    }

    @Test fun dupeFilterAndRateLimiter() {
        val d = GateDupeFilter(30_000)
        assertFalse(d.isDuplicate("a", 0))
        assertTrue(d.isDuplicate("a", 29_000))
        assertFalse(d.isDuplicate("a", 61_000))
        val r = RateLimiter(2, 60_000)
        assertTrue(r.tryAcquire(0)); assertTrue(r.tryAcquire(1)); assertFalse(r.tryAcquire(2))
        assertTrue(r.tryAcquire(60_001))
    }

    @Test fun localHeardExpires() {
        val h = LocalHeard(30 * 60_000L)
        h.heard("py6abc-7", 0)
        assertTrue(h.isLocal("PY6ABC-7", 29 * 60_000L))
        assertFalse(h.isLocal("PY6ABC-7", 31 * 60_000L))
    }
}
