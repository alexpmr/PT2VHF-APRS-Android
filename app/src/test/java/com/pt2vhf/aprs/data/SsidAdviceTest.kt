package com.pt2vhf.aprs.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SsidAdviceTest {
    @Test fun coversAllSixteenValues() {
        assertEquals((0..15).toList(), SsidAdvice.VALUES)
    }

    @Test fun suggestionMatchesProfilePresets() {
        val base = Settings(callsign = "PY2ARS")
        for (p in listOf(Settings.PROFILE_BASE, Settings.PROFILE_MOBILE, Settings.PROFILE_PORTABLE))
            assertEquals(Settings.presetFor(base, p).ssid, SsidAdvice.suggestedFor(p))
        assertNull(SsidAdvice.suggestedFor(Settings.PROFILE_RX))
    }

    @Test fun fullCallOmitsZero() {
        assertEquals("PY2ARS", SsidAdvice.fullCall("py2ars", 0))
        assertEquals("PY2ARS-13", SsidAdvice.fullCall("PY2ARS-9", 13))
    }
}
