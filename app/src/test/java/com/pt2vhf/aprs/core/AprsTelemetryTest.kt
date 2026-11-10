package com.pt2vhf.aprs.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AprsTelemetryTest {
    @Test fun parsesFrameWithBitsAndComment() {
        val f = parseTelemetryFrame("T#005,199,000,255,073,123,01101001 bateria", 10L)!!
        assertEquals("005", f.sequence)
        assertEquals(listOf(199.0, 0.0, 255.0, 73.0, 123.0), f.analog)
        assertEquals(listOf(false, true, true, false, true, false, false, true), f.bits)
        assertEquals("bateria", f.comment)
        assertEquals(10L, f.time)
    }

    @Test fun acceptsMicSequenceDecimalsAndMissingChannels() {
        val f = parseTelemetryFrame("T#MIC199,12.5", 1L)!!
        assertEquals("MIC", f.sequence)
        assertEquals(listOf(199.0, 12.5, null, null, null), f.analog)
        assertTrue(f.bits.isEmpty())
    }

    @Test fun rejectsNonTelemetry() {
        assertNull(parseTelemetryFrame("!1530.00S/04700.00W>", 1L))
        assertNull(parseTelemetryFrame("T#abc,1,2", 1L))
        assertNull(parseTelemetryFrame("T#001,x,y", 1L))
    }

    @Test fun definitionsScaleValuesAndLabelBits() {
        var d = TelemetryDefinitions()
        d = applyTelemetryDefinition(d, "PARM.Bateria,Temp,,,,Porta,Alarme")!!
        d = applyTelemetryDefinition(d, "UNIT.V,°C")!!
        d = applyTelemetryDefinition(d, "EQNS.0,0.075,0,0,0.5,-40,0,1,0")!!
        d = applyTelemetryDefinition(d, "BITS.10000000,Estação solar")!!
        assertEquals("Bateria", d.analogName(0))
        assertNull(d.analogName(2))
        assertEquals("V", d.analogUnit(0))
        assertEquals(14.925, d.scaled(0, 199.0), 1e-9)
        assertEquals(-3.5, d.scaled(1, 73.0), 1e-9)
        // Canal sem coeficientes: padrão do protocolo, valor bruto.
        assertEquals(5.0, d.scaled(4, 5.0), 1e-9)
        assertEquals("Porta", d.bitName(0))
        assertTrue(d.bitActive(0, true))
        assertTrue(d.bitActive(1, false))
        assertEquals("Estação solar", d.projectTitle)
        assertNull(applyTelemetryDefinition(d, "mensagem comum"))
    }

    @Test fun findsDefinitionAddressee() {
        assertEquals("PT2WX-13" to "PARM.Volt", telemetryDefinitionTarget(":PT2WX-13 :PARM.Volt"))
        assertNull(telemetryDefinitionTarget(":PT2WX-13 :ola"))
    }
}
