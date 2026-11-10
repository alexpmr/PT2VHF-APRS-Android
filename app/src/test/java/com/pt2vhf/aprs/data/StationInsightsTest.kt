package com.pt2vhf.aprs.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StationInsightsTest {
    private val now = 1_800_000_000_000L
    private var id = 0L
    private fun rx(minAgo: Long, raw: String, medium: Medium = Medium.INTERNET): PacketLogEntry {
        val src = raw.substringBefore('>')
        return PacketLogEntry(++id, now - minAgo * 60_000, "RX", medium, raw, src, "APRS", "other")
    }
    private fun station(call: String, lat: Double, lon: Double, heard: Long = now) =
        Station(call, lat, lon, '/', '&', "", heard, Medium.INTERNET, "", null, null)

    private val stations = mapOf(
        "PT2WX" to station("PT2WX", -15.0, -47.0),
        "PU2NEAR" to station("PU2NEAR", -15.1, -47.0),
        "PU2FAR" to station("PU2FAR", -16.0, -47.0),
        "PY2DIG" to station("PY2DIG", -15.5, -47.0),
    )

    @Test fun receptionSeparatesIgatesHopsAndLocalRf() {
        val entries = listOf(
            rx(120, "PT2WX>APRS,WIDE2-1,qAR,PU2NEAR:!1500.00S/04700.00W_090/005t077/A=003281"),
            rx(60, "PT2WX>APRS,PY2DIG*,WIDE2-1,qAR,PU2FAR:!1500.00S/04700.00W_090/005t078"),
            rx(30, "PT2WX>APRS,PY2DIG*,WIDE2-1:!1500.00S/04700.00W_090/005t079", Medium.RF),
            rx(25 * 60, "PT2WX>APRS,qAR,PU2OLD:!1500.00S/04700.00W_090/005t070"),
            rx(10, "OTHER>APRS,qAR,PU2NEAR:!1500.00S/04700.00W>"),
        )
        val i = buildStationInsights("PT2WX", entries, stations, now)
        assertEquals(4, i.totalPackets)
        assertEquals(3, i.reception.packetsInWindow)
        // PU2OLD está fora das 24 h.
        assertEquals(listOf("PU2FAR", "PU2NEAR"), i.reception.igates)
        assertEquals("PU2FAR", i.reception.farthestIgate)
        assertEquals(111.2, i.reception.farthestIgateKm!!, 0.5)
        // Último pacote veio por RF local via PY2DIG, sem iGate.
        assertEquals("PY2DIG", i.reception.lastHop)
        assertNull(i.reception.lastIgate)
        assertEquals(55.6, i.reception.lastHopKm!!, 0.5)
        assertEquals(1, i.reception.localRfPackets)
        assertEquals(0, i.reception.localRfDirect)
        assertEquals(2, i.reception.internetPackets)
        assertEquals(1000.0, i.altitudeM!!, 0.2)
        assertEquals(4, i.weather.size)
        assertEquals("PT2WX", i.packets.first().source)
    }

    @Test fun duplicatesCountOnceInSeriesAndTelemetryUsesDefinitions() {
        val entries = listOf(
            rx(50, "PT2WX>APRS,qAR,PU2NEAR::PT2WX    :PARM.Bateria,Temp"),
            rx(49, "PT2WX>APRS,qAR,PU2NEAR::PT2WX    :EQNS.0,0.1,0,0,1,-40"),
            rx(40, "PT2WX>APRS,qAR,PU2NEAR:T#001,120,65,0,0,0,10000000"),
            rx(40, "PT2WX>APRS,PY2DIG*:T#001,120,65,0,0,0,10000000", Medium.RF),
            rx(20, "PT2WX>APRS,qAR,PU2NEAR:T#002,125,66,0,0,0,00000000"),
            rx(5, "OTHER>APRS,qAR,PU2NEAR::PT2WX    :UNIT.V,C"),
        )
        val i = buildStationInsights("PT2WX", entries, stations, now)
        assertEquals(2, i.telemetry.size)
        assertEquals("Bateria", i.definitions.analogName(0))
        assertEquals(listOf("V", "C"), i.definitions.units)
        assertEquals(12.5, i.definitions.scaled(0, i.telemetry.last().analog[0]!!), 1e-9)
        assertTrue(i.weather.isEmpty())
    }

    @Test fun stationWithoutDataHasNoInventedFields() {
        val i = buildStationInsights("PT2NONE", emptyList(), stations, now)
        assertEquals(0, i.totalPackets)
        assertNull(i.lastPath)
        assertNull(i.altitudeM)
        assertNull(i.reception.farthestIgate)
        assertTrue(i.reception.igates.isEmpty())
    }

    @Test fun otherSsidsShareTheBaseCallsign() {
        val all = listOf(station("PT2VHF-9", 0.0, 0.0, 10), station("PT2VHF", 0.0, 0.0, 30),
            station("PT2VHF-7", 0.0, 0.0, 20), station("PT2VHFX", 0.0, 0.0, 40))
        assertEquals(listOf("PT2VHF", "PT2VHF-7"), otherSsids("PT2VHF-9", all).map { it.call })
    }
}
