package com.pt2vhf.aprs.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

class AfskTest {
    private val tnc2 = "PT2VHF-9>APZVHF,WIDE1-1,WIDE2-1:=1547.50S/04752.30W>PT2VHF APRS www.PT2VHF.com"
    private val frame = Ax25.fromTnc2(tnc2)

    @Test
    fun crcMatchesAx25CheckValue() {
        // CRC-16/X.25 de "123456789" = 0x906E (valor de referência do algoritmo).
        assertEquals(0x906E, Hdlc.crc16("123456789".toByteArray()))
        assertTrue(Hdlc.fcsOk(Hdlc.withFcs(frame)))
    }

    @Test
    fun bitStuffingNeverSendsSixOnesInsideTheFrame() {
        val ones = ByteArray(40) { 0xFF.toByte() }
        val bits = Hdlc.encodeBits(ones, preambleFlags = 1, tailFlags = 1)
        val body = bits.copyOfRange(8, bits.size - 8)
        var run = 0
        for (b in body) { run = if (b) run + 1 else 0; assertTrue(run <= 5) }
    }

    @Test
    fun hdlcDecoderRoundTripWithoutAudio() {
        val bits = Hdlc.encodeBits(frame, 4, 2)
        val decoder = Hdlc.Decoder()
        val frames = bits.toList().mapNotNull { decoder.push(it) }
        assertEquals(1, frames.size)
        assertEquals(tnc2, Ax25.toTnc2(frames[0]))
    }

    private fun decode(audio: ShortArray, rate: Int, hq: Boolean = true): List<String> {
        val demod = AfskDemodulator(rate, hq)
        val out = mutableListOf<String>()
        // Blocos como os do AudioRecord.
        var i = 0
        while (i < audio.size) {
            val n = minOf(1024, audio.size - i)
            demod.feed(audio.copyOfRange(i, i + n)).forEach { out += Ax25.toTnc2(it) }
            i += n
        }
        return out
    }

    private fun withSilence(audio: ShortArray, rate: Int): ShortArray =
        ShortArray(rate / 3) + audio + ShortArray(rate / 3)

    @Test
    fun modulateThenDemodulateAt48k() {
        val audio = AfskModulator(48_000).modulate(frame, txDelayMs = 200)
        assertEquals(listOf(tnc2), decode(withSilence(audio, 48_000), 48_000))
    }

    @Test
    fun demodulatesAt44k1AndLowPowerMode() {
        val audio = AfskModulator(44_100).modulate(frame, txDelayMs = 200)
        assertEquals(listOf(tnc2), decode(withSilence(audio, 44_100), 44_100, hq = false))
    }

    @Test
    fun survivesNoiseLowLevelAndDeEmphasis() {
        val rate = 48_000
        val clean = AfskModulator(rate).modulate(frame, txDelayMs = 250, amplitude = 0.05)
        val rnd = Random(7)
        // De-ênfase simples (passa-baixas de 1ª ordem) + ruído branco + zumbido de 60 Hz.
        var y = 0.0
        val noisy = ShortArray(clean.size) { i ->
            y += 0.35 * (clean[i] - y)
            val v = y + rnd.nextGaussian() * 250 + 300 * sin(2 * PI * 60 * i / rate)
            v.roundToInt().coerceIn(-32768, 32767).toShort()
        }
        assertEquals(listOf(tnc2), decode(withSilence(noisy, rate), rate))
    }

    @Test
    fun twoFramesBackToBackAndDuplicatesSuppressed() {
        val rate = 48_000
        val other = "PY2ABC>APRS,WIDE2-1::PT2VHF-9 :teste 123{42"
        val m = AfskModulator(rate)
        val audio = m.modulate(frame, 200) + m.modulate(Ax25.fromTnc2(other), 200)
        assertEquals(listOf(tnc2, other), decode(withSilence(audio, rate), rate))
    }

    @Test
    fun randomNoiseProducesNoFrames() {
        val rnd = Random(3)
        val noise = ShortArray(48_000 * 5) { (rnd.nextGaussian() * 3000).roundToInt().toShort() }
        assertTrue(decode(noise, 48_000).isEmpty())
    }

    @Test
    fun voxLeadAndTestToneHaveExpectedLength() {
        val m = AfskModulator(48_000)
        val plain = m.modulate(frame, txDelayMs = 300, voxLeadMs = 0)
        val withLead = m.modulate(frame, txDelayMs = 300, voxLeadMs = 500)
        assertEquals(24_000, withLead.size - plain.size)
        assertEquals(48_000 * 3, m.testTone(3.0).size)
        assertFalse(plain.all { it.toInt() == 0 })
    }
}
