package com.pt2vhf.aprs.core

import java.util.UUID
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** #92 (KISS sobre BLE) e #93 (PTT por RTS/DTR ou CAT). */
class RadioControlTest {
    private fun hex(vararg v: Int) = ByteArray(v.size) { v[it].toByte() }

    @Test fun pickPrefersKissBleOverNordicUart() {
        val other = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
        assertSame(BleKiss.KISS_BLE, BleKiss.pick(listOf(other, BleKiss.NORDIC_UART.service, BleKiss.KISS_BLE.service)))
        assertSame(BleKiss.NORDIC_UART, BleKiss.pick(listOf(other, BleKiss.NORDIC_UART.service)))
        assertNull(BleKiss.pick(listOf(other)))
        assertTrue(BleKiss.advertisesKiss(listOf(UUID.fromString("00000001-BA2A-46C9-AE49-01B0961F68BB"))))
    }

    @Test fun chunksFollowMtuAndKeepOrder() {
        val data = ByteArray(100) { it.toByte() }
        val small = BleKiss.chunks(data, 23)
        assertEquals(listOf(20, 20, 20, 20, 20), small.map { it.size })
        assertArrayEquals(data, small.reduce { a, b -> a + b })
        assertEquals(listOf(100), BleKiss.chunks(data, 247).map { it.size })
        assertEquals(5, BleKiss.chunks(data, 5).size) // MTU inválido cai no mínimo de 20 bytes
        assertTrue(BleKiss.chunks(ByteArray(0), 247).isEmpty())
    }

    @Test fun chunksOfRealKissFrame() {
        val frame = Kiss.encode(Ax25.fromTnc2("PT2VHF-9>APZVHF,WIDE1-1,WIDE2-1:!2330.00S/04630.00W>Teste de quadro longo via BLE"))
        val parts = BleKiss.chunks(frame, 23)
        assertTrue(parts.all { it.size <= 20 })
        assertArrayEquals(frame, parts.reduce { a, b -> a + b })
    }

    @Test fun catCommands() {
        assertArrayEquals("TX;".toByteArray(), CatPtt.command(CatPtt.KENWOOD, true))
        assertArrayEquals("RX;".toByteArray(), CatPtt.command(CatPtt.KENWOOD, false))
        assertArrayEquals("TX1;".toByteArray(), CatPtt.command(CatPtt.YAESU, true))
        assertArrayEquals("TX0;".toByteArray(), CatPtt.command(CatPtt.YAESU, false))
        assertArrayEquals(hex(0, 0, 0, 0, 0x08), CatPtt.command(CatPtt.YAESU_OLD, true))
        assertArrayEquals(hex(0, 0, 0, 0, 0x88), CatPtt.command(CatPtt.YAESU_OLD, false))
        assertArrayEquals(hex(0xFE, 0xFE, 0x94, 0xE0, 0x1C, 0x00, 0x01, 0xFD), CatPtt.command(CatPtt.ICOM, true))
        assertArrayEquals(hex(0xFE, 0xFE, 0xA4, 0xE0, 0x1C, 0x00, 0x00, 0xFD), CatPtt.command(CatPtt.ICOM, false, 0xA4))
    }

    @Test fun civAddressParsing() {
        assertEquals(0x94, CatPtt.parseCivAddress("94"))
        assertEquals(0xA4, CatPtt.parseCivAddress("0xA4"))
        assertEquals(0x8C, CatPtt.parseCivAddress("8ch"))
        assertNull(CatPtt.parseCivAddress(""))
        assertNull(CatPtt.parseCivAddress("E0")) // endereço do controlador
        assertNull(CatPtt.parseCivAddress("123"))
        assertNull(CatPtt.parseCivAddress("ZZ"))
    }

    @Test fun pttLinesOnlyRaiseChosenLineDuringTx() {
        assertEquals(false to true, pttLines(PttMode.RTS, true))
        assertEquals(false to false, pttLines(PttMode.RTS, false))
        assertEquals(true to false, pttLines(PttMode.DTR, true))
        assertEquals(false to false, pttLines(PttMode.DTR, false))
        assertEquals(false to false, pttLines(PttMode.CAT, true))
    }

    @Test fun controlLineRequestsPerChip() {
        // Mesmos valores que o TNC USB sempre usou para DTR+RTS ligados.
        assertEquals(UsbControlRequest(0x21, 0x22, 0x03, 0), usbControlLines(UsbChip.CDC, true, true, 0, 1))
        assertEquals(UsbControlRequest(0x40, 1, 0x0303, 1), usbControlLines(UsbChip.FTDI, true, true, 0, 0))
        assertEquals(UsbControlRequest(0x41, 0x07, 0x0303, 0), usbControlLines(UsbChip.CP210X, true, true, 0, 0))
        // PTT por RTS: só o bit 1; a máscara do FTDI/CP210x mantém as duas linhas sob controle.
        assertEquals(UsbControlRequest(0x21, 0x22, 0x02, 0), usbControlLines(UsbChip.CDC, false, true, 0, 1))
        assertEquals(UsbControlRequest(0x40, 1, 0x0302, 1), usbControlLines(UsbChip.FTDI, false, true, 0, 0))
        assertEquals(UsbControlRequest(0x41, 0x07, 0x0300, 0), usbControlLines(UsbChip.CP210X, false, false, 0, 0))
    }
}

class RadioControlTextTest {
    @Test fun newConnectionErrorsAreTranslated() {
        listOf(com.pt2vhf.aprs.data.InternalText.ERR_BLE_CLOSED, com.pt2vhf.aprs.data.InternalText.ERR_BLE_NO_KISS,
            com.pt2vhf.aprs.data.InternalText.ERR_PTT_FAILED).forEach {
            org.junit.Assert.assertNotNull(it, com.pt2vhf.aprs.UiTextKeys.connectionError(it))
        }
    }
}
