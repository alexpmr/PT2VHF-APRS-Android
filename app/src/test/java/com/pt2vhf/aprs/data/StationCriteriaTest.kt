package com.pt2vhf.aprs.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StationCriteriaTest {
    private val first = Station("PT2AAA-9", -15.0, -47.0, '/', '>',
        "mobile", 1_700_200_000_000, Medium.RF, "", 20.0, 90, firstSeen = 1_700_000_000_000)
    private val second = Station("PT2BBB", -16.0, -47.5, '/', '_',
        "weather", 1_700_100_000_000, Medium.INTERNET, "", null, null,
        firstSeen = 1_700_050_000_000)

    @Test fun sortsBothDirections() {
        val all = listOf(first, second)
        assertEquals(listOf("PT2AAA-9", "PT2BBB"), selectStations(all,
            StationCriteria(sort = "callsign"), null, null).map { it.call })
        assertEquals(listOf("PT2BBB", "PT2AAA-9"), selectStations(all,
            StationCriteria(sort = "callsign", descending = true), null, null).map { it.call })
        assertEquals("PT2AAA-9", selectStations(all,
            StationCriteria(sort = "heard", descending = true), null, null).first().call)
        assertEquals("PT2BBB", selectStations(all,
            StationCriteria(sort = "first", descending = true), null, null).first().call)
    }

    @Test fun combinesFiltersRatherThanOnlySorting() {
        val criteria = StationCriteria(
            callsign = "AAA", source = "rf", roleNames = setOf("Móvel"),
            favoritesOnly = true, maxDistanceKm = 10.0,
        )
        val found = selectStations(listOf(first, second), criteria, -15.0, -47.0, setOf("PT2AAA-9"))
        assertEquals(listOf(first), found)
        assertTrue(selectStations(listOf(first, second), criteria,
            null, null, setOf("PT2AAA-9")).isEmpty())
        assertTrue(selectStations(listOf(first, second), criteria,
            -15.0, -47.0, emptySet()).isEmpty())
    }

    @Test fun firstAndLastReceptionAreDifferent() {
        val min = java.time.Instant.ofEpochMilli(1_700_000_000_000)
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString()
        assertEquals(listOf(first, second).size,
            selectStations(listOf(first, second), StationCriteria(firstFrom = min),
                null, null).size)
        val future = "2100-01-01"
        assertTrue(selectStations(listOf(first, second),
            StationCriteria(receivedFrom = future), null, null).isEmpty())
    }

    // ---- #58.6: ordenação em um toque ----
    private fun st(call: String, lat: Double, lon: Double, symbol: Char, heard: Long = 1L) =
        Station(call, lat, lon, '/', symbol, "", heard, Medium.INTERNET, "", null, null)
    private val noPos = st("PT2ZZZ", 999.0, 999.0, '>') // coordenada inválida = sem distância
    private val near = st("PT2NEAR", -15.01, -47.0, '#')
    private val far = st("PT2FAR", -16.0, -47.0, '_')
    private val mid = st("PT2MID", -15.5, -47.0, '>')

    @Test fun distanceKeepsUnknownAtTheEndInBothDirections() {
        val all = listOf(noPos, far, near, mid)
        assertEquals(listOf("PT2NEAR", "PT2MID", "PT2FAR", "PT2ZZZ"),
            selectStations(all, StationCriteria(sort = "distance"), -15.0, -47.0).map { it.call })
        assertEquals(listOf("PT2FAR", "PT2MID", "PT2NEAR", "PT2ZZZ"),
            selectStations(all, StationCriteria(sort = "distance", descending = true), -15.0, -47.0).map { it.call })
        // Sem posição própria, nenhuma distância é conhecida: ordem estável por indicativo.
        assertEquals(listOf("PT2FAR", "PT2MID", "PT2NEAR", "PT2ZZZ"),
            selectStations(all, StationCriteria(sort = "distance", descending = true), null, null).map { it.call })
    }

    @Test fun typeGroupsInDocumentedOrderAndKeepsCallsignAscendingInsideGroup() {
        val mobile2 = st("PT2AAB", 0.0, 0.0, '>')
        val all = listOf(far, near, mid, mobile2)
        assertEquals(listOf("PT2AAB", "PT2MID", "PT2FAR", "PT2NEAR"),
            selectStations(all, StationCriteria(sort = "type"), null, null).map { it.call })
        assertEquals(listOf("PT2NEAR", "PT2FAR", "PT2AAB", "PT2MID"),
            selectStations(all, StationCriteria(sort = "type", descending = true), null, null).map { it.call })
        assertTrue(stationTypeRank("Móvel") < stationTypeRank("Estação"))
        assertTrue(stationTypeRank("iGate") < stationTypeRank("desconhecido"))
    }

    @Test fun secondTapOnActiveSortInvertsDirection() {
        assertEquals("callsign" to false, toggleStationSort("distance", false, "callsign"))
        assertEquals("callsign" to true, toggleStationSort("callsign", false, "callsign"))
        assertEquals("callsign" to false, toggleStationSort("callsign", true, "callsign"))
        assertEquals("type" to false, toggleStationSort("callsign", true, "type"))
        assertEquals("heard" to true, toggleStationSort("distance", false, "heard"))
    }
}
