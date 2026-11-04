package com.pt2vhf.aprs.data

import com.pt2vhf.aprs.core.AprsPacket
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RfStatsTest {
    private val noDigi: (String) -> Pair<Double, Double>? = { null }

    @Test fun directViaDigiAndOwnPacket() {
        val direct = rfEventFor(AprsPacket.parse("PT2AAA>APRS:!1548.00S/04806.60W>")!!, "ME", -15.8, -48.11, -15.8, -48.0, noDigi, 1)!!
        assertTrue(direct.direct)
        assertEquals("heard", direct.kind)
        assertTrue(direct.distanceKm!! > 10)
        val viaDigi = rfEventFor(AprsPacket.parse("PT2AAA>APRS,DIGI1*,WIDE2-1:!x")!!, "ME", null, null, null, null, noDigi, 1)!!
        assertEquals("DIGI1", viaDigi.via)
        assertTrue(!viaDigi.direct)
        val mine = rfEventFor(AprsPacket.parse("ME>APZVHF,DIGI2*:=x")!!, "ME", null, null, null, null, noDigi, 1)!!
        assertEquals("digi", mine.kind)
        assertEquals("DIGI2", mine.call)
        assertNull(rfEventFor(AprsPacket.parse("ME>APZVHF,WIDE1-1:=x")!!, "ME", null, null, null, null, noDigi, 1))
    }

    @Test fun recordsKeepBestPerStationAndSeparateDirect() {
        fun e(call: String, d: Double, direct: Boolean, t: Long = 10) =
            RfEvent(call, "heard", if (direct) "" else "DIGI", direct, t, d, null, null, null, null)
        val events = listOf(e("A", 10.0, true), e("A", 30.0, true), e("B", 20.0, true), e("C", 99.0, false), e("D", 50.0, true, t = 1))
        assertEquals(listOf("A" to 30.0, "B" to 20.0), rangeRecords(events, since = 5, direct = true).map { it.call to it.distanceKm })
        assertEquals(listOf("C"), rangeRecords(events, since = 0, direct = false).map { it.call })
        val links = ownLinkSummaries(events)
        assertEquals(2, links.first { it.call == "A" }.count)
    }

    @Test fun rolesCoverNewCategories() {
        assertEquals("Aeronave", stationRole('/', '\''))
        assertEquals("Embarcação", stationRole('/', 's'))
        assertEquals("Pessoa", stationRole('/', '['))
        assertEquals("Móvel", stationRole('/', '>'))
        assertEquals("Digipeater", stationRole('/', '#'))
    }
}
