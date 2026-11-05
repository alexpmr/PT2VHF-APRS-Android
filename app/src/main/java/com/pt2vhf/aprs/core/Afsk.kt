package com.pt2vhf.aprs.core

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * Modem AFSK 1200 bd (Bell 202) + HDLC/AX.25 em código puro (item #44).
 *
 * Implementação própria, sem código do APRSdroid (GPL) nem de outros projetos:
 *  - TX: quadro AX.25 → FCS CRC-16/X.25 → bit-stuffing → NRZI → tons 1200/2200 Hz em fase contínua.
 *  - RX: correlação I/Q com os dois tons em janela de 1 bit, discriminador normalizado (independe do
 *    nível do áudio), recuperação de relógio por PLL digital, NRZI, remoção de bit-stuffing e checagem
 *    do FCS. No modo "alta qualidade" rodam três demoduladores com compensações de ênfase diferentes
 *    (rádios entregam o áudio com de-ênfase), e quadros repetidos são descartados.
 * Testável na JVM; o serviço Android só alimenta amostras PCM de 16 bits.
 */

object Hdlc {
    const val FLAG = 0x7E

    /** CRC-16/X.25 (polinômio 0x1021 refletido, inicial 0xFFFF, XOR final 0xFFFF), como no AX.25. */
    fun crc16(data: ByteArray, length: Int = data.size): Int {
        var crc = 0xFFFF
        for (i in 0 until length) {
            crc = crc xor (data[i].toInt() and 0xFF)
            repeat(8) { crc = if (crc and 1 != 0) (crc ushr 1) xor 0x8408 else crc ushr 1 }
        }
        return crc.inv() and 0xFFFF
    }

    /** Quadro com FCS anexado (byte menos significativo primeiro). */
    fun withFcs(frame: ByteArray): ByteArray {
        val fcs = crc16(frame)
        return frame + byteArrayOf((fcs and 0xFF).toByte(), (fcs ushr 8).toByte())
    }

    fun fcsOk(frameWithFcs: ByteArray): Boolean {
        if (frameWithFcs.size < 3) return false
        val n = frameWithFcs.size - 2
        val fcs = (frameWithFcs[n].toInt() and 0xFF) or ((frameWithFcs[n + 1].toInt() and 0xFF) shl 8)
        return crc16(frameWithFcs, n) == fcs
    }

    /**
     * Bits a transmitir (antes do NRZI): [preambleFlags] flags, dados+FCS com bit-stuffing
     * (LSB primeiro) e [tailFlags] flags de fechamento.
     */
    fun encodeBits(frame: ByteArray, preambleFlags: Int, tailFlags: Int = 2): BooleanArray {
        val out = ArrayList<Boolean>((frame.size + 2) * 10 + (preambleFlags + tailFlags) * 8)
        fun flag() { for (i in 0 until 8) out += (FLAG shr i) and 1 == 1 }
        repeat(preambleFlags.coerceAtLeast(1)) { flag() }
        var ones = 0
        for (b in withFcs(frame)) {
            val v = b.toInt() and 0xFF
            for (i in 0 until 8) {
                val bit = (v shr i) and 1 == 1
                out += bit
                if (bit) {
                    ones++
                    if (ones == 5) { out += false; ones = 0 }
                } else ones = 0
            }
        }
        repeat(tailFlags.coerceAtLeast(1)) { flag() }
        return out.toBooleanArray()
    }

    /** NRZI do AX.25: bit 0 troca o tom, bit 1 mantém. true = tom de marca (1200 Hz). */
    fun nrzi(bits: BooleanArray, startMark: Boolean = true): BooleanArray {
        var tone = startMark
        return BooleanArray(bits.size) { i -> if (!bits[i]) tone = !tone; tone }
    }

    /** Recebe bits já decodificados do NRZI e devolve quadros AX.25 (sem FCS) com FCS válido. */
    class Decoder(private val maxBytes: Int = 400) {
        private var ones = 0
        private val bits = BooleanArray(maxBytes * 8 + 16)
        private var count = 0
        private var inFrame = false
        /** Último instante (em bits) em que um flag foi visto; usado para detectar canal ocupado. */
        var bitClock = 0L
            private set
        var lastFlagBit = -1_000_000L
            private set

        fun push(bit: Boolean): ByteArray? {
            bitClock++
            if (bit) {
                ones++
                if (ones >= 7) { inFrame = false; count = 0; return null } // abort / ruído
                append(true)
                return null
            }
            val previousOnes = ones
            ones = 0
            if (previousOnes == 5) return null // zero inserido pelo bit-stuffing
            if (previousOnes == 6) {
                // Flag 01111110: os 7 bits anteriores (0 + seis 1) já foram acrescentados.
                lastFlagBit = bitClock
                val dataBits = count - 7
                var frame: ByteArray? = null
                if (inFrame && dataBits >= 18 * 8 && dataBits % 8 == 0) {
                    val bytes = ByteArray(dataBits / 8)
                    for (i in 0 until dataBits) if (bits[i]) bytes[i / 8] = (bytes[i / 8].toInt() or (1 shl (i % 8))).toByte()
                    if (fcsOk(bytes)) frame = bytes.copyOf(bytes.size - 2)
                }
                inFrame = true
                count = 0
                return frame
            }
            append(false)
            return null
        }

        private fun append(bit: Boolean) {
            if (!inFrame) return
            if (count >= bits.size) { inFrame = false; count = 0; return }
            bits[count++] = bit
        }

        val receiving: Boolean get() = inFrame && count > 16
    }
}

/** Gera áudio AFSK (PCM 16 bits mono) para um quadro AX.25. */
class AfskModulator(
    private val sampleRate: Int = 48_000,
    private val markHz: Double = 1200.0,
    private val spaceHz: Double = 2200.0,
    private val baud: Double = 1200.0,
) {
    /**
     * @param txDelayMs preâmbulo de flags (TXDELAY) para o rádio abrir e estabilizar.
     * @param voxLeadMs tom contínuo antes do preâmbulo para disparar o VOX (0 = nenhum).
     * @param amplitude 0..1 do fundo de escala.
     */
    fun modulate(frame: ByteArray, txDelayMs: Int = 300, voxLeadMs: Int = 0, tailMs: Int = 30,
                 amplitude: Double = 0.7): ShortArray {
        val flags = ceil(txDelayMs.coerceIn(0, 2000) * baud / 8.0 / 1000.0).toInt().coerceAtLeast(2)
        val tailFlags = ceil(tailMs.coerceIn(0, 500) * baud / 8.0 / 1000.0).toInt().coerceAtLeast(2)
        val tones = Hdlc.nrzi(Hdlc.encodeBits(frame, flags, tailFlags))
        val amp = amplitude.coerceIn(0.0, 1.0) * Short.MAX_VALUE
        val leadSamples = (voxLeadMs.coerceIn(0, 2000) * sampleRate / 1000.0).roundToInt()
        val dataSamples = ceil(tones.size * sampleRate / baud).toInt()
        val out = ShortArray(leadSamples + dataSamples + sampleRate / 100)
        var phase = 0.0
        var n = 0
        val markStep = 2 * PI * markHz / sampleRate
        val spaceStep = 2 * PI * spaceHz / sampleRate
        repeat(leadSamples) {
            out[n++] = (amp * sin(phase)).roundToInt().toShort()
            phase += markStep
        }
        val samplesPerBit = sampleRate / baud
        var bitEnd = samplesPerBit
        var bitIndex = 0
        var local = 0.0
        while (bitIndex < tones.size && n < out.size) {
            out[n++] = (amp * sin(phase)).roundToInt().toShort()
            phase += if (tones[bitIndex]) markStep else spaceStep
            if (phase > 2 * PI) phase -= 2 * PI
            local += 1.0
            if (local >= bitEnd) { bitIndex++; bitEnd += samplesPerBit }
        }
        return out.copyOf(n)
    }

    /** Tom de teste para ajustar o nível no rádio: alterna 1200/2200 Hz a cada 0,5 s. */
    fun testTone(seconds: Double = 3.0, amplitude: Double = 0.7): ShortArray {
        val total = (seconds * sampleRate).roundToInt()
        val amp = amplitude.coerceIn(0.0, 1.0) * Short.MAX_VALUE
        var phase = 0.0
        return ShortArray(total) { i ->
            val f = if ((i / (sampleRate / 2)) % 2 == 0) markHz else spaceHz
            phase += 2 * PI * f / sampleRate
            if (phase > 2 * PI) phase -= 2 * PI
            (amp * sin(phase)).roundToInt().toShort()
        }
    }
}

/** Um demodulador: correlação I/Q + PLL + NRZI + HDLC. */
private class AfskChannel(sampleRate: Int, private val spaceGain: Double) {
    private val window = (sampleRate / 1200.0).roundToInt().coerceAtLeast(4)
    private val table = 1024
    private val sinT = DoubleArray(table) { sin(2 * PI * it / table) }
    private val markStep = 1200.0 * table / sampleRate
    private val spaceStep = 2200.0 * table / sampleRate
    private var markPh = 0.0
    private var spacePh = 0.0
    private val mi = DoubleArray(window); private val mq = DoubleArray(window)
    private val si = DoubleArray(window); private val sq = DoubleArray(window)
    private var sMi = 0.0; private var sMq = 0.0; private var sSi = 0.0; private var sSq = 0.0
    private var idx = 0
    private val bitStep = 1200.0 / sampleRate
    private var pll = 0.0
    private var lastSign = false
    private var lastTone = false
    val hdlc = Hdlc.Decoder()

    fun feed(x: Double): ByteArray? {
        val mIdx = markPh.toInt() and (table - 1)
        val sIdx = spacePh.toInt() and (table - 1)
        val mc = x * sinT[(mIdx + table / 4) and (table - 1)]
        val ms = x * sinT[mIdx]
        val sc = x * sinT[(sIdx + table / 4) and (table - 1)]
        val ss = x * sinT[sIdx]
        markPh += markStep; if (markPh >= table) markPh -= table
        spacePh += spaceStep; if (spacePh >= table) spacePh -= table
        sMi += mc - mi[idx]; mi[idx] = mc
        sMq += ms - mq[idx]; mq[idx] = ms
        sSi += sc - si[idx]; si[idx] = sc
        sSq += ss - sq[idx]; sq[idx] = ss
        idx = (idx + 1) % window
        val mark = sqrt(sMi * sMi + sMq * sMq)
        val space = sqrt(sSi * sSi + sSq * sSq) * spaceGain
        val d = (mark - space) / (mark + space + 1e-9)
        val sign = d > 0
        // PLL: a transição deveria cair no meio entre duas amostragens (fase 0,5).
        if (sign != lastSign) {
            pll += 0.35 * (0.5 - pll)
            lastSign = sign
        }
        pll += bitStep
        if (pll >= 1.0) {
            pll -= 1.0
            val bit = sign == lastTone
            lastTone = sign
            return hdlc.push(bit)
        }
        return null
    }
}

/**
 * Demodulador AFSK 1200 para PCM 16 bits. [highQuality] roda três canais com compensação de ênfase
 * (0 dB, +6 dB e -6 dB no tom de 2200 Hz) e devolve cada quadro uma única vez.
 */
class AfskDemodulator(sampleRate: Int, highQuality: Boolean = true) {
    private val channels = (if (highQuality) listOf(1.0, 2.0, 0.5) else listOf(1.0)).map { AfskChannel(sampleRate, it) }
    private val recent = ArrayDeque<Pair<Int, Long>>()
    private var sampleClock = 0L
    private val dedupeSamples = sampleRate.toLong() // 1 s
    private var levelAcc = 0.0
    private var levelCount = 0
    private val levelWindow = sampleRate / 10
    /** Nível RMS da entrada (0..1), atualizado a cada 100 ms. */
    @Volatile var level: Double = 0.0
        private set

    /** Canal ocupado: algum demodulador está no meio de um quadro. */
    val busy: Boolean get() = channels.any { it.hdlc.receiving }

    fun feed(samples: ShortArray, length: Int = samples.size): List<ByteArray> {
        var out: MutableList<ByteArray>? = null
        for (i in 0 until length) {
            val x = samples[i] / 32768.0
            levelAcc += x * x
            if (++levelCount >= levelWindow) {
                level = sqrt(levelAcc / levelCount)
                levelAcc = 0.0; levelCount = 0
            }
            sampleClock++
            for (ch in channels) {
                val frame = ch.feed(x) ?: continue
                val key = frame.contentHashCode()
                while (recent.isNotEmpty() && sampleClock - recent.first().second > dedupeSamples) recent.removeFirst()
                if (recent.any { it.first == key }) continue
                recent.addLast(key to sampleClock)
                (out ?: mutableListOf<ByteArray>().also { out = it }) += frame
            }
        }
        return out ?: emptyList()
    }
}
