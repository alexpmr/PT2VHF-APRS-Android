package com.pt2vhf.aprs.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** #89: tocall, Mic-E, PHG e frequência anunciada. */
class AprsDeviceTest {
    // 23°32.56'S 46°38.50'W, 36 nós, rumo 251, "Returning", FT5D (_3), altitude 61 m.
    private val miceInfo = "`JBNoZO>/`\"4T}QRV 145.570_3"

    @Test fun decodesMicEPosition() {
        val m = parseMicE("R3325V", miceInfo)
        assertNotNull(m)
        m!!
        assertEquals(-(23 + 32.56 / 60), m.position.lat, 1e-6)
        assertEquals(-(46 + 38.50 / 60), m.position.lon, 1e-6)
        assertEquals(251, m.position.course)
        assertEquals(36 * 1.852, m.position.speedKmh!!, 1e-6)
        assertEquals('>', m.position.symbol)
        assertEquals('/', m.position.symbolTable)
        assertEquals(MicEStatus.RETURNING, m.status)
        assertEquals(61.0, m.altitudeM!!, 0.1)
        assertEquals("Yaesu FT5D", m.device)
        assertEquals("QRV 145.570", m.position.comment)
    }

    @Test fun micEStatusAndInvalid() {
        assertEquals(MicEStatus.EMERGENCY, parseMicE("233256", miceInfo)?.status)
        assertEquals(MicEStatus.OFF_DUTY, parseMicE("RSS25V", miceInfo)?.status)
        assertEquals(MicEStatus.CUSTOM, parseMicE("BCD25V", miceInfo)?.status)
        assertEquals(0, parseMicE("BCD25V", miceInfo)?.customIndex)
        assertNull(parseMicE("APRS", miceInfo))
        assertNull(parseMicE("R3325V", "!2332.56S/04638.50W>"))
    }

    @Test fun packetPositionFallsBackToMicE() {
        val p = AprsPacket.parse("PY2XX-9>R3325V,WIDE1-1:$miceInfo")!!
        assertEquals(-23.5427, parsePacketPosition(p)!!.lat, 1e-3)
        assertEquals("Yaesu FT5D", identifyDevice(p))
    }

    @Test fun kenwoodLegacy() {
        assertEquals("Kenwood TH-D74", micEDevice(">texto^"))
        assertEquals("Kenwood TM-D710", micEDevice("]comentário="))
        assertEquals("Kenwood TM-D700", micEDevice("]comentário"))
    }

    @Test fun tocalls() {
        assertEquals("PT2VHF APRS Client", deviceByTocall("APZVHF"))
        assertEquals(true, deviceByTocall("APDR16")!!.contains("APRSdroid"))
        assertNull(deviceByTocall("BEACON"))
    }

    @Test fun phgAndFrequency() {
        assertEquals(Phg(25, 6, 3, 0), decodePhg("5130"))
        assertEquals(90, decodePhg("2242")!!.directionDeg)
        assertNull(decodePhg("12a4"))
        assertEquals("146.520 MHz · T088 · +0.600 MHz", announcedFrequency("Rep 146.520MHz T088 +060 net"))
        assertEquals("145.570 MHz", announcedFrequency("QRV 145.570MHz"))
        assertNull(announcedFrequency("QRV 145.570"))
    }
}

/** #88: e-mail e site no comentário viram link. */
class CommentLinksTest {
    @Test fun emailAndSite() {
        val text = "QRV 145.570 fulano@exemplo.com.br, veja www.PT2VHF.com."
        val links = findCommentLinks(text)
        assertEquals(2, links.size)
        assertEquals("mailto:fulano@exemplo.com.br", links[0].target)
        assertEquals("fulano@exemplo.com.br", text.substring(links[0].start, links[0].end))
        assertEquals("https://www.PT2VHF.com", links[1].target)
    }

    @Test fun plainTextUnchanged() {
        assertEquals(0, findCommentLinks("PY2XYZ-9 @ 145.570 QRV").size)
        assertEquals(0, findCommentLinks("").size)
    }
}
