package com.pt2vhf.aprs.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmartDigipeaterTest {
    private fun digi(cfg: DigiConfig = DigiConfig("PY6TL-1")) = SmartDigipeater(cfg) { 0.0 }
    private fun rep(d: DigiDecision) = d as DigiDecision.Repeat
    private fun why(d: DigiDecision) = (d as DigiDecision.Drop).reason
    private val pos = "!1258.00S/03830.00W>"

    @Test fun fillInTracesOwnCall() {
        val r = rep(digi().onHeard("PY6ABC-9>APRS,WIDE1-1,WIDE2-1:${pos}a", 0))
        assertEquals("PY6ABC-9>APRS,PY6TL-1*,WIDE1*,WIDE2-1:${pos}a", r.line)
        assertEquals(DigiReason.FILL_IN, r.reason)
        assertEquals(0L, r.delayMs)
    }

    @Test fun wideNDecrementsAndFinishes() {
        val d = digi()
        assertEquals("PY6A>APRS,PY6TL-1*,WIDE2-1:${pos}a", rep(d.onHeard("PY6A>APRS,WIDE2-2:${pos}a", 0)).line)
        assertEquals("PY6B>APRS,PY6X*,PY6TL-1*,WIDE2*:${pos}b",
            rep(d.onHeard("PY6B>APRS,PY6X*,WIDE2-1:${pos}b", 0)).line)
    }

    @Test fun ownCallAndAliasAreRepeated() {
        val d = digi(DigiConfig("PY6TL-1", aliases = setOf("SSA")))
        assertEquals("PY6A>APRS,PY6TL-1*:>x", rep(d.onHeard("PY6A>APRS,PY6TL-1:>x", 0)).line)
        assertEquals(DigiReason.ALIAS, rep(d.onHeard("PY6B>APRS,SSA,WIDE2-1:>y", 0)).reason)
    }

    @Test fun neverOwnPacketNorUsedPathNorNetwork() {
        val d = digi()
        assertEquals(DigiReason.OWN_PACKET, why(d.onHeard("PY6TL-1>APRS,WIDE1-1:>x", 0)))
        assertEquals(DigiReason.NOT_FOR_ME, why(d.onHeard("PY6A>APRS,PY6X*,WIDE2*:>x", 0)))
        assertEquals(DigiReason.NOT_FOR_ME, why(d.onHeard("PY6A>APRS:>x", 0)))
        assertEquals(DigiReason.NON_RF, why(d.onHeard("PY6A>APRS,TCPIP*,WIDE1-1:>x", 0)))
    }

    @Test fun duplicateWithinWindow() {
        val d = digi()
        rep(d.onHeard("PY6A>APRS,WIDE1-1:${pos}a", 0))
        assertEquals(DigiReason.DUPLICATE, why(d.onHeard("PY6A>APRS,PY6X*,WIDE2-1:${pos}a", 2_000)))
    }

    @Test fun fillInOnlyFromDirectStations() {
        assertEquals(DigiReason.NOT_DIRECT, why(digi().onHeard("PY6A>APRS,PY6X*,WIDE1-1:>x", 0)))
    }

    @Test fun trapReducesOrDropsExcessivePath() {
        val r = rep(digi().onHeard("PY6A>APRS,WIDE7-7:>x", 0))
        assertEquals(DigiReason.WIDE_TRAPPED, r.reason)
        assertEquals("PY6A>APRS,PY6TL-1*,WIDE2-1:>x", r.line)
        assertEquals(DigiReason.EXCESS_PATH,
            why(digi(DigiConfig("PY6TL-1", trapDrop = true)).onHeard("PY6A>APRS,WIDE7-7:>x", 0)))
    }

    @Test fun viscousWhenNeighborCoversAndCancelsWhenNeighborRepeats() {
        val d = digi()
        // Vizinho PY6DIG repetiu PY6A antes: a cobertura foi aprendida.
        d.onHeard("PY6A>APRS,PY6DIG*,WIDE1*:${pos}1", 0)
        val r = rep(d.onHeard("PY6A>APRS,WIDE1-1:${pos}2", 120_000))
        assertEquals("PY6DIG", r.coveredBy)
        assertEquals(5_000L, r.delayMs)
        // O vizinho repete de novo dentro do atraso: cancelado.
        d.onHeard("PY6A>APRS,PY6DIG*,WIDE1*:${pos}2", 121_000)
        assertNull(d.release(r.token))
        assertEquals(1, d.stats()[DigiReason.COVERED])
        // Origem não coberta: repete na hora (preenche o buraco).
        assertEquals(0L, rep(d.onHeard("PY6Z>APRS,WIDE1-1:${pos}z", 130_000)).delayMs)
        assertEquals(1, d.neighbors(130_000).size)
    }

    @Test fun viscousReleasesWhenNobodyRepeats() {
        val d = digi()
        d.onHeard("PY6A>APRS,PY6DIG*,WIDE1*:${pos}1", 0)
        val r = rep(d.onHeard("PY6A>APRS,WIDE1-1:${pos}2", 120_000))
        assertEquals(r.line, d.release(r.token))
    }

    @Test fun perSourceRateButMessagesHavePriority() {
        val d = digi()
        rep(d.onHeard("PY6A>APRS,WIDE1-1:${pos}1", 0))
        assertEquals(DigiReason.SOURCE_RATE, why(d.onHeard("PY6A>APRS,WIDE1-1:${pos}2", 10_000)))
        val m = rep(d.onHeard("PY6A>APRS,WIDE1-1::PY6B     :oi{1", 11_000))
        assertTrue(m.priority)
    }

    @Test fun busyChannelKeepsOnlyDirectFillInAndSaturatedKeepsOnlyPriority() {
        val d = digi(DigiConfig("PY6TL-1", busyPerMin = 5, saturatedPerMin = 10, perSourceSec = 0))
        for (k in 0 until 6) d.onHeard("PY6N$k>APRS:>ruido", k * 100L)
        assertEquals(DigiReason.BUSY, why(d.onHeard("PY6A>APRS,WIDE2-2:${pos}a", 1_000)))
        assertEquals(DigiReason.FILL_IN, rep(d.onHeard("PY6B>APRS,WIDE1-1:${pos}b", 1_100)).reason)
        for (k in 0 until 5) d.onHeard("PY6M$k>APRS:>ruido", 1_200L + k)
        assertEquals(DigiReason.SATURATED, why(d.onHeard("PY6C>APRS,WIDE1-1:${pos}c", 1_300)))
        assertTrue(rep(d.onHeard("PY6C>APRS,WIDE1-1::PY6D     :socorro{2", 1_400)).priority)
    }

    @Test fun radiusUsesPacketPosition() {
        val d = digi(DigiConfig("PY6TL-1", radiusKm = 20))
        // Salvador (-12.97, -38.50); pacote de Feira de Santana (~100 km).
        assertEquals(DigiReason.OUT_OF_RADIUS,
            why(d.onHeard("PY6F>APRS,WIDE1-1:!1216.00S/03858.00W>", 0, -12.97, -38.50)))
        assertEquals(DigiReason.FILL_IN,
            rep(d.onHeard("PY6S>APRS,WIDE1-1:!1258.00S/03830.00W>", 0, -12.97, -38.50)).reason)
    }

    @Test fun globalLimitPerMinute() {
        val d = digi(DigiConfig("PY6TL-1", maxPerMinute = 2, adaptive = false))
        rep(d.onHeard("PY6A>APRS,WIDE1-1:>1", 0)); rep(d.onHeard("PY6B>APRS,WIDE1-1:>2", 1))
        assertEquals(DigiReason.GLOBAL_RATE, why(d.onHeard("PY6C>APRS,WIDE1-1:>3", 2)))
    }
}
