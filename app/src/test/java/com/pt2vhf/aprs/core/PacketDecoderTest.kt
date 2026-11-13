package com.pt2vhf.aprs.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PacketDecoderTest {
    private fun DecodedPacket.v(key: DecodedKey) = fields.firstOrNull { it.key == key }?.value

    @Test fun decodesPositionWithCourseAltitude() {
        val d = decodePacket("PT2CAR-9>APRS,PT2DIG-1*,WIDE2-1,qAR,PT2IGT:!1548.00S/04806.60W>090/025Carro /A=001234")!!
        assertEquals("position", d.type)
        assertEquals("PT2CAR-9", d.v(DecodedKey.SOURCE))
        assertEquals("-15.80000", d.v(DecodedKey.LATITUDE))
        assertEquals("/>", d.v(DecodedKey.SYMBOL))
        assertEquals("46.3 km/h", d.v(DecodedKey.SPEED))
        assertEquals("376 m", d.v(DecodedKey.ALTITUDE))
        assertEquals("PT2IGT", d.path.igate)
    }

    @Test fun decodesWeather() {
        val d = decodePacket("PT2WX>APRS:!1548.00S/04806.60W_090/010g015t077h50b10132")!!
        assertEquals("weather", d.type)
        assertEquals("25.0 °C", d.v(DecodedKey.TEMPERATURE))
        assertEquals("50 %", d.v(DecodedKey.HUMIDITY))
        assertEquals("1013.2 hPa", d.v(DecodedKey.PRESSURE))
    }

    @Test fun decodesMessageAndAck() {
        val m = decodePacket("PT2VHF-9>APRS::PY2ABC   :Ola{42")!!
        assertEquals("PY2ABC", m.v(DecodedKey.ADDRESSEE))
        assertEquals("Ola", m.v(DecodedKey.MESSAGE_TEXT))
        assertEquals("42", m.v(DecodedKey.MESSAGE_ID))
        val a = decodePacket("PY2ABC>APRS::PT2VHF-9 :ack42")!!
        assertEquals("ack", a.type)
        assertEquals("ack 42", a.v(DecodedKey.ACK_ID))
    }

    @Test fun decodesTelemetryStatusAndUnknown() {
        val t = decodePacket("PT2TLM>APRS:T#005,199,000,255,073,123,01101001")!!
        assertEquals("005", t.v(DecodedKey.TELEMETRY_SEQUENCE))
        assertEquals("01101001", t.v(DecodedKey.TELEMETRY_BITS))
        assertEquals("QRV", decodePacket("PT2VHF>APRS:>QRV")!!.v(DecodedKey.STATUS))
        val mic = decodePacket("PT2VHF-9>T4QR5U:`c5Il!Ok/`\"4V}_%")!!
        assertEquals("mice", mic.type)
        // #89: Mic-E agora é decodificado (posição, altitude e aparelho).
        assertEquals("44.20917", mic.v(DecodedKey.LATITUDE))
        assertEquals("-71.42417", mic.v(DecodedKey.LONGITUDE))
        assertTrue(mic.fields.none { it.key == DecodedKey.NOT_DECODED })
        assertEquals("Yaesu FTM-400DR", mic.v(DecodedKey.DEVICE))
        assertNull(decodePacket("isto não é pacote"))
    }

    @Test fun frequencyTableHasBrazil() {
        assertEquals("145.570", APRS_FREQUENCIES.first { "BR" in it.regions }.mhz)
    }

    @Test fun weatherLabelForMap() {
        val w = parseWeatherComment("090/010g015t077h50", 0L)!!
        assertEquals("25°C · 16 km/h L", weatherMapLabel(w))
        assertNull(weatherMapLabel(AprsWeather(0L, humidityPercent = 40)))
    }
}
