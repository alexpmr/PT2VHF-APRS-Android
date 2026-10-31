package com.pt2vhf.aprs.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AprsTest {
    @Test fun passcodeIgnoresSsidAndCase() {
        // Valores de referência do algoritmo público (N0CALL = 13023).
        assertEquals(13023, aprsPasscode("N0CALL"))
        assertEquals(aprsPasscode("PT2VHF"), aprsPasscode("pt2vhf-9"))
    }

    @Test fun filterShortcutExpandsWithPosition() {
        assertEquals("r/-15.780/-47.930/4000", expandFilter("r/4000", -15.78, -47.93))
        assertEquals("r/4000", expandFilter("r/4000", null, null))
        assertEquals("m/200 b/PT2*", expandFilter("m/200 b/PT2*", -15.0, -47.0))
    }

    @Test fun serverValidation() {
        assertTrue(validHostPort("soam.aprs2.net", 14580))
        assertFalse(validHostPort("soam aprs", 14580))
        assertFalse(validHostPort("soam.aprs2.net", 70000))
        assertEquals("soam.aprs2.net", APRS_SERVERS.first().host)
    }

    @Test fun parsesPositionAndDigis() {
        val p = AprsPacket.parse("PT2CAR-9>APRS,PT2DIG-1*,WIDE2-1,qAR,PT2IGT:!1548.00S/04806.60W>090/025Carro")!!
        assertEquals("PT2CAR-9", p.source)
        assertEquals(listOf("PT2DIG-1"), p.rfDigipeaters)
        val pos = parsePosition(p.info)!!
        assertEquals(-15.8, pos.lat, 1e-6)
        assertEquals(-48.11, pos.lon, 1e-6)
        assertEquals(90, pos.course)
        assertEquals(25 * 1.852, pos.speedKmh!!, 1e-6)
        assertEquals("Carro", pos.comment)
    }

    @Test fun dmrAndInternetHopsAreNotRf() {
        val p = AprsPacket.parse("PP5PM-9>APDR16,PP5PM,DMR*,qAR,PP5PM:=1130.35S/03747.21W>Roberto")!!
        assertTrue(p.rfDigipeaters.isEmpty())
        val i = AprsPacket.parse("PT2ABC>APRS,TCPIP*,qAC,T2BRAZIL:!1545.00S/04752.80W-casa")!!
        assertTrue(i.viaInternet)
        assertTrue(i.rfDigipeaters.isEmpty())
    }

    @Test fun rejectsInvalidMinutes() {
        assertNull(parsePosition("!2677.84S/04812.00W-x"))
    }

    @Test fun weatherWindIsNotSpeed() {
        val pos = parsePosition("!2330.00S/04630.00W_090/012g020t077")!!
        assertNull(pos.speedKmh)
    }

    @Test fun messagesAndAcks() {
        val m = parseMessage(":PT2VHF-9 :Bom dia{12")!!
        assertEquals("PT2VHF-9", m.addressee)
        assertEquals("Bom dia", m.text)
        assertEquals("12", m.id)
        assertEquals(AprsMessage.Kind.ACK, parseMessage(":PT2VHF-9 :ack12")!!.kind)
        assertEquals(AprsMessage.Kind.BULLETIN, parseMessage(":BLN1     :Roda sabado")!!.kind)
        assertEquals(":PT2ABC-7 :Oi{3", messageInfo("pt2abc-7", "Oi", "3"))
        assertEquals(":PT2ABC-7 :ack3", ackInfo("PT2ABC-7", "3"))
    }

    @Test fun positionRoundTrip() {
        val info = positionInfo(-15.78, -47.93, '/', '>', "Teste", 90, 60.0)
        assertTrue(info.startsWith("=1546.80S/04755.80W>090/032"))
        val pos = parsePosition(info)!!
        assertEquals(-15.78, pos.lat, 1e-3)
        assertEquals(-47.93, pos.lon, 1e-3)
    }

    @Test fun beaconCommentAlwaysIncludesCanonicalSite() {
        val examples = mapOf(
            "" to "www.PT2VHF.com",
            "Teste" to "Teste www.PT2VHF.com",
            "PT2VHF APRS www.PT2VHF.com" to "PT2VHF APRS www.PT2VHF.com",
            "https://www.pt2vhf.com/aprs-android" to "www.PT2VHF.com",
            "Site http://pt2vhf.com/aprs" to "Site www.PT2VHF.com",
            "teste WWW.pt2VHF.com" to "teste www.PT2VHF.com"
        )
        for ((input, expected) in examples) {
            assertEquals(expected, beaconCommentWithSite(input))
        }
        for (input in listOf("X".repeat(200), "Texto com descricao longa e URL http://pt2vhf.com/aprs", "  ")) {
            val formatted = beaconCommentWithSite(input)
            assertTrue(formatted.endsWith(PT2VHF_BEACON_SITE))
            assertEquals(1, Regex("(?i)pt2vhf\\.com").findAll(formatted).count())
            assertTrue(formatted.length <= 43)
            val onAir = positionInfo(-15.78, -47.93, '/', '>', formatted, 90, 60.0)
            assertEquals(formatted, parsePosition(onAir)!!.comment)
        }
    }

    @Test fun splitsLongMessages() {
        val parts = splitMessage("a".repeat(30) + " " + "b".repeat(50) + " " + "c".repeat(20))
        assertTrue(parts.all { it.length <= 67 })
        assertEquals(2, parts.size)
    }

    @Test fun kissAndAx25RoundTrip() {
        val raw = "PT2VHF-9>APRS,WIDE1-1:=1546.80S/04755.80W>teste"
        val frame = Ax25.fromTnc2(raw)
        val kiss = Kiss.encode(frame)
        val frames = Kiss.Decoder().feed(kiss)
        assertEquals(1, frames.size)
        assertArrayEquals(frame, frames[0])
        assertEquals(raw, Ax25.toTnc2(frames[0]))
    }

    @Test fun kissEscapesSpecialBytes() {
        val payload = byteArrayOf(0x01, 0xC0.toByte(), 0xDB.toByte(), 0x02)
        val decoded = Kiss.Decoder().feed(Kiss.encode(payload))
        assertArrayEquals(payload, decoded.single())
    }

    @Test fun smartBeaconingRates() {
        val sb = SmartBeaconing()
        assertFalse(sb.shouldBeacon(60, 0.0, 0.0))
        assertTrue(sb.shouldBeacon(1800, 0.0, 0.0))
        assertTrue(sb.shouldBeacon(61, 100.0, 0.0))
        assertTrue(sb.shouldBeacon(20, 50.0, 90.0))
        assertFalse(sb.shouldBeacon(10, 50.0, 90.0))
    }

    @Test fun geoHelpers() {
        assertEquals("GH64", maidenhead(-15.78, -47.93).take(4))
        assertEquals("L", compassName(90))
        assertNotNull(haversineKm(0.0, 0.0, 0.0, 1.0))
        assertEquals(111.19, haversineKm(0.0, 0.0, 0.0, 1.0), 0.1)
    }

    @Test fun versionComparison() {
        assertTrue(isNewerVersion("0.1.1", "0.1.0"))
        assertTrue(isNewerVersion("v1.0", "0.9.9"))
        assertFalse(isNewerVersion("0.1.0", "0.1.0"))
        assertFalse(isNewerVersion("0.0.9", "0.1.0"))
    }
 
    @Test fun onlyLocalTncFramesProduceRfLinkEvidence() {
        val rf = AprsPacket.parse("PT2MOB-9>APRS,PT2DIG-1*,qAR,PT2IG:!1548.00S/04806.60W>Teste")!!
        assertEquals(emptyList<Pair<String, String>>(), localRfEvidenceLinks(rf, false))
        assertEquals(listOf("PT2MOB-9" to "PT2DIG-1"), localRfEvidenceLinks(rf, true))

        // Internet and digital-network hops do not become local RF proof.
        val internet = AprsPacket.parse("PT2MOB-9>APRS,TCPIP*,qAC:!1548.00S/04806.60W>Teste")!!
        assertTrue(localRfEvidenceLinks(internet, true).isEmpty())
        val direct = AprsPacket.parse("PT2MOB-9>APRS:!1548.00S/04806.60W>Teste")!!
        assertTrue(localRfEvidenceLinks(direct, true).isEmpty())
    }

}
