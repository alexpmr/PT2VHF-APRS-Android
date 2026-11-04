package com.pt2vhf.aprs.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AprsTextTest {
    /** Bytes as the APRS-IS reader produces them: one ISO-8859-1 char per byte. */
    private fun asRead(bytes: ByteArray) = String(bytes, Charsets.ISO_8859_1)

    @Test fun asciiIsUnchanged() {
        assertEquals("HAPPY HALLOWEEN 73", decodeAprsText("HAPPY HALLOWEEN 73"))
    }

    @Test fun validUtf8IsDecoded() {
        assertEquals("Olá João 🎃", decodeAprsText(asRead("Olá João 🎃".toByteArray(Charsets.UTF_8))))
    }

    @Test fun latin1AndCp1252AreKept() {
        assertEquals("Olá", decodeAprsText(asRead(byteArrayOf(0x4F, 0x6C, 0xE1.toByte()))))
        // 0x80 is the euro sign in CP1252; 0x93/0x94 are curly quotes.
        assertEquals("€ “x”", decodeAprsText(asRead(byteArrayOf(0x80.toByte(), 0x20, 0x93.toByte(), 0x78, 0x94.toByte()))))
    }

    @Test fun controlsAreMarkedNotRemoved() {
        assertEquals("A�B C", decodeAprsText("A\u0007B\tC"))
    }

    @Test fun ambiguityBlanksTrailingDigits() {
        val (lat, lon) = applyAmbiguity("1548.12S", "04806.34W", 2)
        assertEquals("1548.  S", lat)
        assertEquals("04806.  W", lon)
        assertEquals("15  .  S" to "048  .  W", applyAmbiguity("1548.12S", "04806.34W", 4))
        assertEquals("1548.12S" to "04806.34W", applyAmbiguity("1548.12S", "04806.34W", 0))
    }

    @Test fun ambiguousPositionsAreParsedAtBoxCentre() {
        val info = positionInfo(-15.8, -48.11, '/', '>', "x", ambiguity = 2)
        assertTrue(info.contains("1548.  S"))
        val pos = parsePosition(info)!!
        assertEquals(-(15 + 48.5 / 60.0), pos.lat, 1e-6)
    }

    @Test fun voiceFrequencyAndAltitudeFormats() {
        assertEquals("146.520MHz", formatVoiceFrequency("146,52"))
        assertEquals("438.500MHz", formatVoiceFrequency("438.5 MHz"))
        assertNull(formatVoiceFrequency("abc"))
        assertEquals("/A=003281", formatAltitude(1000.0))
        assertNull(formatAltitude(null))
    }

    @Test fun composedCommentKeepsSiteAndLimit() {
        val c = composeBeaconComment("PT2VHF APRS Client for Android muito longo", "146.520MHz", "/A=003281")
        assertTrue(c.length <= 43)
        assertTrue(c.startsWith("146.520MHz /A=003281 "))
        assertTrue(c.endsWith(PT2VHF_BEACON_SITE))
    }

    @Test fun stoppedIntervalOverridesSlowRate() {
        val sb = SmartBeaconing(stoppedRateSec = 600)
        assertTrue(sb.shouldBeacon(600, 0.0, 0.0))
        assertEquals(1200L, SmartBeaconing().nextBeaconSeconds(600, 0.0))
    }
}
