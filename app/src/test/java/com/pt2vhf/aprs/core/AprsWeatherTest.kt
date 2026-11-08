package com.pt2vhf.aprs.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class AprsWeatherTest {
    @Test fun parsesConventionalWxAndConvertsUnits() {
        val wx = parseAprsWeather("!2330.00S/04630.00W_090/012g020t077r005p012P003h50b10150", 1234L)!!
        assertEquals(1234L, wx.timestamp)
        assertEquals(25.0, wx.temperatureC!!, 0.01)
        assertEquals(50, wx.humidityPercent)
        assertEquals(1015.0, wx.pressureHpa!!, 0.01)
        assertEquals(90, wx.windDirectionDeg)
        assertEquals(12 * 1.609344, wx.windKmh!!, 0.001)
        assertEquals(20 * 1.609344, wx.gustKmh!!, 0.001)
        assertEquals(1.27, wx.rain1hMm!!, 0.001)
        assertEquals(3.048, wx.rain24hMm!!, 0.001)
    }

    @Test fun handlesMissingFieldsAndHumidityHundred() {
        val wx = parseAprsWeather("_10090556c...s...g...t-05h00b10000", 500L)!!
        assertEquals(100, wx.humidityPercent)
        assertEquals((-5 - 32) * 5.0 / 9.0, wx.temperatureC!!, 0.001)
        assertNull(wx.windKmh)
        assertNotNull(wx.pressureHpa)
    }

    @Test fun rejectsNonWxAndUnknownReadings() {
        assertNull(parseAprsWeather("!2330.00S/04630.00W>090/025Carro"))
        assertNull(parseAprsWeather("_10090556c...s...g...t...P..."))
    }
}
