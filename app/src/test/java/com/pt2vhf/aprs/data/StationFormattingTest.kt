package com.pt2vhf.aprs.data

import org.junit.Assert.assertEquals
import org.junit.Test

class StationFormattingTest {
    @Test fun missingSpeedIsNotStationary() {
        assertEquals("—", stationSpeedLabel(null))
        assertEquals("—", stationSpeedLabel(Double.NaN))
        assertEquals("—", stationSpeedLabel(Double.POSITIVE_INFINITY))
        assertEquals("—", stationSpeedLabel(-3.0))
    }

    @Test fun stationaryAndMobileAreDifferent() {
        assertEquals("0 km/h", stationSpeedLabel(0.0))
        assertEquals("65 km/h", stationSpeedLabel(65.1))
        assertEquals("66 km/h", stationSpeedLabel(65.7))
    }
}
