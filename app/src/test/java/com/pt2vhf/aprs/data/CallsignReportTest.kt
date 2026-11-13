package com.pt2vhf.aprs.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** #84: indicativo enviado na consulta de versão (lista de estações no site). */
class CallsignReportTest {
    private val base = "https://www.pt2vhf.com/update.php?product=aprs-android"

    @Test fun sendsOnlyTheCallsignByDefault() {
        val s = Settings(callsign = "py2xyz", ssid = 9)
        assertEquals("PY2XYZ", UpdateCheck.reportedCallsign(s))
        assertEquals("$base&call=PY2XYZ", UpdateCheck.endpoint(UpdateCheck.reportedCallsign(s)))
    }

    @Test fun neverSendsSsidLocationOrLocator() {
        val s = Settings(callsign = "PY2XYZ-7", positionSource = "manual", manualLat = -23.5, manualLon = -46.6)
        val url = UpdateCheck.endpoint(UpdateCheck.reportedCallsign(s))
        assertEquals("$base&call=PY2XYZ", url)
    }

    @Test fun portableCallsignIsEncoded() {
        assertEquals("$base&call=CT%2FPY2XYZ%2FP", UpdateCheck.endpoint(UpdateCheck.reportedCallsign(Settings(callsign = "CT/PY2XYZ/P"))))
    }

    @Test fun optionOffSendsNothing() {
        val s = Settings(callsign = "PY2XYZ", shareCallsign = false)
        assertNull(UpdateCheck.reportedCallsign(s))
        assertEquals(base, UpdateCheck.endpoint(UpdateCheck.reportedCallsign(s)))
    }

    @Test fun unconfiguredStationSendsNothing() {
        assertNull("vazio", UpdateCheck.reportedCallsign(Settings(callsign = "")))
        assertNull("espaços", UpdateCheck.reportedCallsign(Settings(callsign = "   ")))
        assertNull("exemplo", UpdateCheck.reportedCallsign(Settings(callsign = "PT2VHF")))
        assertNull("exemplo com SSID", UpdateCheck.reportedCallsign(Settings(callsign = "pt2vhf-9")))
        assertNull("manual sem lat/lon", UpdateCheck.reportedCallsign(Settings(callsign = "PY2XYZ", positionSource = "manual")))
        assertNull("manual sem lon", UpdateCheck.reportedCallsign(Settings(callsign = "PY2XYZ", positionSource = "manual", manualLat = -23.5)))
        assertEquals(base, UpdateCheck.endpoint(null))
        assertEquals(base, UpdateCheck.endpoint(""))
    }

    @Test fun invalidCallsignSendsNothing() {
        for (bad in listOf("ABCDEF", "123456", "PY2<XYZ>", "PY2XYZ&x=1", "A1/B2/C3/D4", "PY2XYZ PY2ABC", "ABCDEFGHI1"))
            assertNull(bad, UpdateCheck.reportedCallsign(Settings(callsign = bad)))
    }

    @Test fun enabledByDefault() {
        assertEquals(true, Settings().shareCallsign)
    }
}
