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
}
