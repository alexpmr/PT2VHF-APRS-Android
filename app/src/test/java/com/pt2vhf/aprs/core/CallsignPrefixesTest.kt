package com.pt2vhf.aprs.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CallsignPrefixesTest {
    @Test fun acceptsRealCallsignsFromManyCountries() {
        val cases = mapOf(
            "PT2VHF-9" to "BR", "PY2ABC" to "BR", "ZZ2ABC" to "BR", "K1ABC" to "US", "W1AW" to "US",
            "N0CALL" to "US", "KH6ABC" to "US", "AL7AB" to "US", "DL1ABC" to "DE", "2E0ABC" to "GB",
            "M0ABC" to "GB", "VK2ABC" to "AU", "JA1ABC" to "JP", "7J1ABC" to "JP", "4X1AB" to "IL",
            "9A1A" to "HR", "3DA0RU" to "SZ", "3D2AB" to "FJ", "E51ABC" to "CK", "BV2AB" to "TW",
            "BH4ABC" to "CN", "LU1ABC" to "AR", "CX1AB" to "UY", "CE3ABC" to "CL", "XE1ABC" to "MX",
            "VE3ABC" to "CA", "F4ABC" to "FR", "I2ABC" to "IT", "EA4ABC" to "ES", "CT1ABC" to "PT",
            "R1ABC" to "RU", "UA9ABC" to "RU", "HB9ABC" to "CH", "ZS6ABC" to "ZA", "SU1AB" to "EG",
            "A61AB" to "AE", "HL1ABC" to "KR", "OH2ABC" to "FI", "OK1ABC" to "CZ", "YO3ABC" to "RO",
        )
        for ((call, iso) in cases) {
            val check = checkAmateurCallsign(call)
            assertTrue("Expected valid: $call (${check.problem})", check.ok)
            assertEquals("Country of $call", iso, check.country)
        }
    }

    @Test fun acceptsSpecialEntities() {
        assertEquals("*UN", checkAmateurCallsign("4U1ITU").country)
        assertEquals("*SMOM", checkAmateurCallsign("1A0KM").country)
    }

    @Test fun rejectsWordsAndUnallocatedPrefixes() {
        mapOf(
            "CASA" to CallsignProblem.FORMAT, "TESTE" to CallsignProblem.FORMAT, "ABC" to CallsignProblem.FORMAT,
            "ABC123" to CallsignProblem.FORMAT, "12345" to CallsignProblem.FORMAT, "PT2VH5" to CallsignProblem.FORMAT,
            "" to CallsignProblem.EMPTY, "PT2VHF-16" to CallsignProblem.SSID, "PT2VHF-X" to CallsignProblem.SSID,
            "QA1ABC" to CallsignProblem.PREFIX, "1B1ABC" to CallsignProblem.PREFIX, "0A1ABC" to CallsignProblem.PREFIX,
            "PT2VHFXX" to CallsignProblem.FORMAT,
        ).forEach { (call, problem) ->
            val check = checkAmateurCallsign(call)
            assertFalse("Expected invalid: $call", check.ok)
            assertEquals("Problem for $call", problem, check.problem)
        }
    }

    @Test fun reportsUnknownPrefix() {
        val check = checkAmateurCallsign("QA1ABC")
        assertEquals("QA", check.prefix)
        assertNull(check.country)
    }

    @Test fun splitsSsidSeparately() {
        val check = checkAmateurCallsign(" pt2vhf-15 ")
        assertTrue(check.ok)
        assertEquals("PT2VHF", check.base)
        assertEquals(15, check.ssid)
    }

    @Test fun countryOfAnyCallsignIncludingReceivedOnes() {
        assertEquals("BR", callsignCountry("PT2VHF-9"))
        assertEquals("US", callsignCountry("W1AW-10"))
        assertNull(callsignCountry("CQ"))
        assertNull(callsignCountry("BLN1"))
    }

    @Test fun flagFromIso() {
        assertEquals("🇧🇷", flagEmoji("BR"))
        assertEquals("", flagEmoji("*UN"))
        assertEquals("", flagEmoji(null))
    }

    @Test fun legacyHelperKeepsBehaviour() {
        assertTrue(plausibleAmateurCallsign("PT2VHF-9"))
        assertFalse(plausibleAmateurCallsign("CASA"))
        assertFalse(plausibleAmateurCallsign("QA1ABC"))
    }
}
