package com.pt2vhf.aprs.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone

class AprsExtrasTest {
    @Test
    fun classifiesPacketTypes() {
        assertEquals("position", packetType("=1547.50S/04752.30W>teste"))
        assertEquals("weather", packetType("!1547.50S/04752.30W_090/005g010t077"))
        assertEquals("message", packetType(":PT2VHF-9 :ola{12"))
        assertEquals("ack", packetType(":PT2VHF-9 :ack12"))
        assertEquals("bulletin", packetType(":BLN0     :boletim"))
        assertEquals("telemetry", packetType(":PT2VHF-9 :PARM.Volt"))
        assertEquals("telemetry", packetType("T#001,1,2,3,4,5,00000000"))
        assertEquals("object", packetType(";REP146910*041530z1547.50S/04752.30Wr"))
        assertEquals("item", packetType(")AID#2!4903.50N/07201.75WA"))
        assertEquals("status", packetType(">em operação"))
        assertEquals("mice", packetType("`c51!f?>/]\"4W}"))
        assertEquals("query", packetType("?APRS?"))
        assertEquals("other", packetType(""))
    }

    @Test
    fun objectAndItemRoundTrip() {
        val t = 1_762_300_000_000L // 2025-11-04 ~23:46 UTC
        val obj = objectInfo("REP146910", true, -15.7916, -47.8716, '/', 'r', "146.910- T123", timeMs = t)
        assertTrue(obj.startsWith(";REP146910*"))
        assertTrue(obj.substring(11, 18).endsWith("z"))
        val parsed = parseObject(obj)
        assertNotNull(parsed)
        assertEquals("REP146910", parsed!!.name)
        assertTrue(parsed.alive)
        assertEquals(-15.7916, parsed.position.lat, 0.001)
        assertEquals('r', parsed.position.symbol)
        val killed = parseObject(objectInfo("RPT", false, 1.0, 2.0, '/', 'r', "", timeMs = t))!!
        assertFalse(killed.alive)
        assertEquals("RPT", killed.name)
        val item = objectInfo("AID2", true, 49.0583, -72.0292, '/', 'A', "ok", item = true)
        assertTrue(item.startsWith(")AID2!"))
        val pi = parseObject(item)!!
        assertTrue(pi.item && pi.alive)
        assertEquals("AID2", pi.name)
        assertEquals("object", packetType(obj))
    }

    @Test
    fun objectNamesAreValidated() {
        assertTrue(validObjectName("REP146910", false))
        assertFalse(validObjectName("REP1469100", false))
        assertFalse(validObjectName("AB", true))
        assertFalse(validObjectName("AB!C", true))
        assertTrue(validObjectName("A", false))
    }

    @Test
    fun messageAgeSameDayAndYesterday() {
        val utc = TimeZone.getTimeZone("UTC")
        val now = 1_762_300_000_000L // 23:46 UTC
        val a = messageAge(now - (3 * 60 + 45) * 60_000L, now, utc)
        assertTrue(a.sameDay)
        assertEquals(3, a.hours)
        assertEquals(45, a.minutes)
        assertTrue(messageAge(now - 20_000L, now, utc).justNow)
        assertFalse(messageAge(now - 24 * 3_600_000L, now, utc).sameDay)
    }

    @Test
    fun gpxRouteAndTrackParsing() {
        val gpx = """<?xml version="1.0"?><gpx><rte><name>Trilha Pico</name>
            <rtept lat="-15.70" lon="-47.80"/><rtept lat='-15.71' lon='-47.81'></rtept><rtept lon="-47.82" lat="-15.72"/>
            </rte><wpt lat="1" lon="1"/></gpx>"""
        val r = parseGpxRoute(gpx)!!
        assertEquals("Trilha Pico", r.name)
        assertEquals(3, r.points.size)
        assertEquals(-47.82, r.points[2].lon, 1e-9)
        assertTrue(routeLengthKm(r.points) > 2.0)
        val rem = routeRemainingKm(r.points, -15.71, -47.81)!!
        assertEquals(routeLengthKm(r.points.drop(1)), rem.first, 1e-9)
        assertNull(parseGpxRoute("<gpx><trk><trkseg><trkpt lat=\"1\" lon=\"2\"/></trkseg></trk></gpx>"))
    }

    @Test
    fun ftdiDivisorsMatchDatasheetValues() {
        assertEquals(0x4138 to 0, ftdiBaudDivisor(9600))
        assertEquals((52 or (3 shl 14)) to 0, ftdiBaudDivisor(57_600))
        assertEquals(26 to 0, ftdiBaudDivisor(115_200).let { (it.first and 0x3FFF) to it.second })
        assertEquals(2500 to 0, ftdiBaudDivisor(1200))
    }

    @Test
    fun batteryDrainNeedsEnoughTimeAndNoCharging() {
        val h = 3_600_000L
        assertEquals(4.0, batteryDrainPerHour(listOf(0L to 80, h to 78, 2 * h to 72))!!, 0.01)
        assertNull(batteryDrainPerHour(listOf(0L to 80, 60_000L to 79)))
        assertNull(batteryDrainPerHour(listOf(0L to 70, h to 75)))
    }
}
