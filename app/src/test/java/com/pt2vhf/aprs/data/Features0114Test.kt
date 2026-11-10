package com.pt2vhf.aprs.data

import com.pt2vhf.aprs.core.AprsMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset

class Features0114Test {
    // ---------- #64 acompanhar mensagem nova ----------
    @Test fun followsNewestOnlyWhenReaderIsAtTheEnd() {
        assertTrue(followNewestAtEnd(lastVisibleIndex = 9, previousCount = 10, newestIsMine = false))
        assertTrue(followNewestAtEnd(lastVisibleIndex = 8, previousCount = 10, newestIsMine = false))
        assertFalse(followNewestAtEnd(lastVisibleIndex = 3, previousCount = 10, newestIsMine = false))
        assertTrue("own message always scrolls", followNewestAtEnd(3, 10, true))
        assertTrue("first load scrolls", followNewestAtEnd(-1, 0, false))
        assertTrue(followNewestAtTop(0))
        assertFalse(followNewestAtTop(5))
    }

    // ---------- #70 APRSdroid ----------
    @Test fun importsAprsDroidSharedPrefsXml() {
        val xml = """<?xml version='1.0' encoding='utf-8' standalone='yes' ?>
            <map>
                <string name="callsign">py2abc</string>
                <string name="ssid">9</string>
                <string name="passcode">12345</string>
                <string name="symbol">/&gt;</string>
                <string name="status">Rodando &amp; testando</string>
                <string name="digi_path">wide1-1,wide2-1</string>
                <string name="tcp.server">soam.aprs2.net:14580</string>
                <string name="interval">10</string>
                <boolean name="smartbeaconing" value="false" />
                <string name="unknown.key">x</string>
            </map>"""
        val raw = parseFlatPrefs(xml)!!
        val conv = convertAprsDroid(raw)!!
        val prefs = conv.prefs.associate { it.key to it.value }
        assertEquals("PY2ABC", prefs["callsign"])
        assertEquals("9", prefs["ssid"])
        assertEquals("/", prefs["symbol_table"])
        assertEquals(">", prefs["symbol"])
        assertEquals("Rodando & testando", prefs["comment"])
        assertEquals("WIDE1-1,WIDE2-1", prefs["rf_path"])
        assertEquals("soam.aprs2.net", prefs["server"])
        assertEquals("14580", prefs["port"])
        assertEquals("600", prefs["periodic_interval"])
        assertEquals("periodic", prefs["beacon_mode"])
        assertFalse("passcode never imported", prefs.containsKey("passcode"))
        assertEquals(1, conv.ignored)
        assertEquals("PY2ABC-9", conv.callsign)
    }

    @Test fun importsAprsDroidJsonProfile() {
        val json = """{"callsign":"PT2VHF","ssid":"7","symbol":"\/[","smartbeaconing":true,"nested":{"a":1},"tcp.filter":"r\/-15\/-47\/100"}"""
        val conv = convertAprsDroid(parseFlatPrefs(json)!!)!!
        val prefs = conv.prefs.associate { it.key to it.value }
        assertEquals("PT2VHF", prefs["callsign"])
        assertEquals("[", prefs["symbol"])
        assertEquals("smart", prefs["beacon_mode"])
        assertEquals("r/-15/-47/100", prefs["filter"])
    }

    @Test fun rejectsFilesWithoutCallsign() {
        assertNull(convertAprsDroid(mapOf("foo" to "bar")))
        assertNull(parseFlatPrefs("not a prefs file"))
    }

    // ---------- #66 agrupamento ----------
    @Test fun clustersCloseStationsAndKeepsIsolatedOnes() {
        val near = (0 until 5).map { ClusterPoint("A$it", -15.80 + it * 0.0001, -47.90) }
        val far = ClusterPoint("FAR", -23.5, -46.6)
        val clusters = clusterStations(near + far, zoom = 8.0, cellPx = 60.0)
        assertEquals(2, clusters.size)
        assertEquals(5, clusters.first { it.size > 1 }.size)
        assertEquals(listOf("FAR"), clusters.first { it.size == 1 }.keys)
        // Zoom alto separa as estações.
        assertEquals(6, clusterStations(near + far, zoom = 19.0, cellPx = 60.0).size)
    }

    @Test fun mercatorIsMonotonic() {
        val (x1, y1) = mercatorPx(0.0, 0.0, 0.0)
        assertEquals(128.0, x1, 1e-9)
        assertEquals(128.0, y1, 1e-9)
        assertTrue(mercatorPx(10.0, 10.0, 1.0).second < mercatorPx(-10.0, 10.0, 1.0).second)
    }

    // ---------- #58.3 filtros ----------
    private fun st(call: String, table: Char = '/', sym: Char = '>', comment: String = "", heard: Long = 1_000_000L,
                   lat: Double = -15.8, lon: Double = -47.9) =
        Station(call, lat, lon, table, sym, comment, heard, Medium.INTERNET, "", null, null)

    @Test fun filterPanelCriteria() {
        val now = 10_000_000L
        val list = listOf(
            st("PT2VHF-9", comment = "Carro em Brasília", heard = now - 60_000),
            st("W1AW", table = '/', sym = '_', heard = now - 2 * 3_600_000),
            st("DL1ABC-10", heard = now - 30 * 60_000),
            st("PY2XYZ", lat = 0.0, lon = 0.0, heard = now),
        )
        fun sel(c: StationCriteria) = selectStations(list, c, null, null, now = now).map { it.call }.toSet()
        assertEquals(setOf("PT2VHF-9"), sel(StationCriteria(callsign = "brasília")))
        assertEquals(setOf("PT2VHF-9", "PY2XYZ"), sel(StationCriteria(countries = setOf("BR"))))
        assertEquals(setOf("W1AW", "PY2XYZ"), sel(StationCriteria(ssids = setOf(0))))
        assertEquals(setOf("W1AW"), sel(StationCriteria(symbol = "/_")))
        assertEquals(setOf("W1AW"), sel(StationCriteria(symbol = "_")))
        assertEquals(setOf("PT2VHF-9", "PY2XYZ"), sel(StationCriteria(lastSeenMinutes = 15)))
        assertEquals(3, sel(StationCriteria(withPositionOnly = true)).size)
        assertEquals(3, activeStationFilterCount(StationCriteria(countries = setOf("BR"), ssids = setOf(9), lastSeenMinutes = 60)))
        assertEquals(0, activeStationFilterCount(StationCriteria(sort = "callsign", descending = true)))
    }

    // ---------- #58.4 trilha ----------
    @Test fun trailByPeriodAndDistance() {
        val now = 100 * 3_600_000L
        val points = listOf(
            TrackPoint(-15.80, -47.90, now - 5 * 3_600_000L),
            TrackPoint(-15.81, -47.90, now - 30 * 60_000L),
            TrackPoint(-15.82, -47.90, now - 10 * 60_000L),
        )
        assertEquals(2, trailWithin(points, now, 3_600_000L).size)
        assertEquals(3, trailWithin(points, now, 6 * 3_600_000L).size)
        assertEquals(2.22, trailDistanceKm(points), 0.02)
    }

    @Test fun insightsCollectPositions() {
        val entries = listOf(
            PacketLogEntry(1, 1000, "RX", Medium.INTERNET, "PT2CAR-9>APRS:!1548.00S/04806.60W>Carro", "PT2CAR-9", "APRS", "position"),
            PacketLogEntry(2, 120_000, "RX", Medium.INTERNET, "PT2CAR-9>APRS:!1549.00S/04806.60W>Carro", "PT2CAR-9", "APRS", "position"),
        )
        val insights = buildStationInsights("PT2CAR-9", entries, emptyMap(), 200_000)
        assertEquals(2, insights.positions.size)
        assertTrue(insights.positions[0].recordedAt < insights.positions[1].recordedAt)
    }

    // ---------- #58.5 comunidade ----------
    @Test fun netCheckInsByCountry() {
        val thursday = LocalDate.of(2026, 11, 5)
        val t = thursday.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() + 3_600_000L
        fun msg(id: Long, from: String, to: String, text: String, time: Long = t) =
            ChatMessage(id, from, to, text, time, false, Medium.INTERNET, kind = AprsMessage.Kind.MESSAGE)
        val messages = listOf(
            msg(1, "ANSRVR", "PT2VHF", "N:HOTG PY2ABC> Hello from Sao Paulo"),
            msg(2, "ANSRVR", "PT2VHF", "N:HOTG W1AW> 73"),
            msg(3, "ANSRVR", "PT2VHF", "N:HOTG PY2ABC> again"),
            msg(4, "ANSRVR", "PT2VHF", "N:HOTG DL1ABC> hi", time = t + 3 * 86_400_000L),
            msg(5, "PT2XYZ", "PT2VHF", "nada a ver"),
        )
        val packets = listOf(PacketLogEntry(9, t + 60_000, "RX", Medium.INTERNET,
            "PT2VHF-9>APRS::ANSRVR   :CQ HOTG teste{12", "PT2VHF-9", "APRS", "message"))
        val net = DEFAULT_NETS.first()
        val summary = netSummary(net, thursday, messages, packets)
        assertEquals(listOf("PY2ABC", "W1AW", "PT2VHF-9"), summary.checkIns.map { it.call })
        assertEquals("BR" to 2, summary.byCountry.first())
        assertEquals(1, netSummary(net, thursday, messages, packets, query = "w1").checkIns.size)
        assertEquals(thursday, latestNetDate(net, LocalDate.of(2026, 11, 10)))
    }

    @Test fun netsRoundTrip() {
        val nets = DEFAULT_NETS + NetDefinition("Rodada BR", "CQ", "RODADA", 7, 21, 2, false, "QRV")
        assertEquals(nets, parseNets(serializeNets(nets)))
        assertTrue(parseNets("lixo").isEmpty())
        assertNotNull(parseNets("x|ANSRVR|K|1|0|24|true|").firstOrNull())
    }
}
