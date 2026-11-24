package com.pt2vhf.aprs.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** #109: posição comprimida (base-91), usada por trackers/iGates LoRa APRS. */
class CompressedPositionTest {
    @Test fun specExampleWithCourseAndSpeed() {
        // Exemplo do APRS 1.0.1, cap. 9: 49°30'N 72°45'W, curso 88°, 36,2 nós.
        val pos = parsePosition("!/5L!!<*e7>7P[")!!
        assertEquals(49.5, pos.lat, 1e-4)
        assertEquals(-72.75, pos.lon, 1e-4)
        assertEquals('/', pos.symbolTable)
        assertEquals('>', pos.symbol)
        assertEquals(88, pos.course)
        assertEquals(36.2 * 1.852, pos.speedKmh!!, 0.2)
    }

    @Test fun loraPacketFromTncAppearsWithPosition() {
        val packet = AprsPacket.parse("PY4XV-7>APLRT1,WIDE1-1:!/Vu:MB%7e>  ALoRa APRS")!!
        val pos = parsePacketPosition(packet)
        assertNotNull(pos)
        assertEquals(-16.68, pos!!.lat, 1e-4)
        assertEquals(-49.25, pos.lon, 1e-4)
        assertNull(pos.course)
        assertNull(pos.speedKmh)
        assertEquals("LoRa APRS", pos.comment)
    }

    @Test fun timestampedMessagingAndOverlay() {
        val pos = parsePosition("@092345z/5L!!<*e7>7P[")!!
        assertEquals(49.5, pos.lat, 1e-4)
        val overlay = parsePosition("=b5L!!<*e7#  A")!!
        assertEquals('1', overlay.symbolTable)
        assertEquals('#', overlay.symbol)
    }

    @Test fun altitudeFormIgnoresCourseSpeed() {
        val pos = parsePosition("!/5L!!<*e7>S]1")!!
        assertNull(pos.course)
        assertNull(pos.speedKmh)
    }

    @Test fun compressedObject() {
        val obj = parseObject(";LORA-GW  *092345z/5L!!<*e7r  A")!!
        assertEquals("LORA-GW", obj.name)
        assertEquals(49.5, obj.position.lat, 1e-4)
    }

    @Test fun invalidCompressedIsRejected() {
        assertNull(parsePosition("!/5L!"))
        assertNull(parsePosition("!.5L!!<*e7>7P["))
        // Não comprimida continua igual.
        assertEquals(-15.5, parsePosition("!1530.00S/04730.00W>")!!.lat, 1e-6)
    }
}
