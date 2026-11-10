package com.pt2vhf.aprs.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AprsPathTest {
    private fun path(line: String) = describePath(AprsPacket.parse(line)!!)

    @Test fun namedDigipeatersAndIgateFromAprsIs() {
        val p = path("PT2AAA-9>APRS,PY2XYZ*,WIDE2-1,qAR,PU2IGT:!1530.00S/04700.00W>")
        assertEquals(1, p.rfHops)
        assertEquals("PY2XYZ", p.lastDigipeater)
        assertEquals("PU2IGT", p.igate)
        assertEquals("qAR", p.qCode)
        assertFalse(p.direct)
        assertEquals(listOf(PathHop.Kind.ORIGIN, PathHop.Kind.DIGIPEATER, PathHop.Kind.ALIAS_UNUSED,
            PathHop.Kind.Q_CONSTRUCT, PathHop.Kind.IGATE), p.hops.map { it.kind })
    }

    @Test fun starOnLastUsedHopMarksPreviousOnesAsUsed() {
        val p = path("PT2AAA>APRS,PY2AAA,PY2BBB,WIDE2*,qAO,PU2IGT:>status")
        assertEquals(2, p.rfHops)
        assertEquals("PY2BBB", p.lastDigipeater)
        assertEquals(PathHop.Kind.ALIAS_USED, p.hops[3].kind)
    }

    @Test fun directToIgateHasNoHops() {
        val p = path("PT2AAA>APRS,WIDE1-1,WIDE2-1,qAR,PU2IGT:>status")
        assertTrue(p.direct)
        assertEquals(0, p.rfHops)
        assertNull(p.lastDigipeater)
        assertTrue(qIsRfGate(p.qCode))
    }

    @Test fun consumedAliasWithoutCallsignCountsOneUnknownHop() {
        val p = path("PT2AAA>APRS,WIDE1*,WIDE2-1:>status")
        assertEquals(1, p.rfHops)
        assertNull(p.lastDigipeater)
        assertNull(p.igate)
    }

    @Test fun internetClientIsNotAnRfGate() {
        val p = path("PT2AAA>APRS,TCPIP*,qAC,T2BRAZIL:>status")
        assertEquals(PathHop.Kind.NETWORK, p.hops[1].kind)
        assertEquals(QMeaning.CLIENT_VERIFIED, qMeaning(p.qCode))
        assertFalse(qIsRfGate(p.qCode))
        assertEquals(0, p.rfHops)
    }
}
