package com.pt2vhf.aprs.core

import java.io.ByteArrayOutputStream
import java.util.Locale

/** KISS (FEND/FESC) — mesmo protocolo do TNC do desktop. */
object Kiss {
    const val FEND = 0xC0
    const val FESC = 0xDB
    const val TFEND = 0xDC
    const val TFESC = 0xDD

    fun encode(payload: ByteArray, port: Int = 0): ByteArray {
        val out = ByteArrayOutputStream()
        out.write(FEND)
        out.write((port and 0x0F) shl 4)
        for (b in payload) {
            when (b.toInt() and 0xFF) {
                FEND -> { out.write(FESC); out.write(TFEND) }
                FESC -> { out.write(FESC); out.write(TFESC) }
                else -> out.write(b.toInt())
            }
        }
        out.write(FEND)
        return out.toByteArray()
    }

    /** Decodificador de fluxo: alimente com os bytes recebidos, recebe os frames de dados completos. */
    class Decoder {
        private val buffer = ByteArrayOutputStream()
        private var inFrame = false
        private var escaped = false

        fun feed(chunk: ByteArray): List<ByteArray> {
            val frames = mutableListOf<ByteArray>()
            for (raw in chunk) {
                val b = raw.toInt() and 0xFF
                if (b == FEND) {
                    if (inFrame && buffer.size() > 1) {
                        val data = buffer.toByteArray()
                        if ((data[0].toInt() and 0x0F) == 0) frames += data.copyOfRange(1, data.size)
                    }
                    buffer.reset(); inFrame = true; escaped = false
                    continue
                }
                if (!inFrame) continue
                if (escaped) {
                    buffer.write(if (b == TFEND) FEND else if (b == TFESC) FESC else b)
                    escaped = false
                } else if (b == FESC) {
                    escaped = true
                } else {
                    buffer.write(b)
                }
            }
            return frames
        }
    }
}

/** AX.25 UI frame <-> TNC2 */
object Ax25 {
    private fun encodeAddress(value: String, last: Boolean): ByteArray {
        val repeated = value.endsWith("*")
        val clean = value.removeSuffix("*").uppercase(Locale.ROOT)
        val call = clean.substringBefore('-').take(6).padEnd(6)
        val ssid = clean.substringAfter('-', "0").toIntOrNull()?.coerceIn(0, 15) ?: 0
        val out = ByteArray(7)
        for (i in 0 until 6) out[i] = (call[i].code shl 1).toByte()
        var s = 0x60 or (ssid shl 1)
        if (repeated) s = s or 0x80
        if (last) s = s or 0x01
        out[6] = s.toByte()
        return out
    }

    /** Converte "ORIG>DEST,PATH:info" (sem elementos q/TCPIP) para um frame AX.25 UI. */
    fun fromTnc2(raw: String): ByteArray {
        val packet = AprsPacket.parse(raw) ?: throw IllegalArgumentException("Pacote TNC2 inválido")
        val path = packet.path.filter { !it.startsWith("q") && !it.startsWith("TCPIP") }.take(8)
        val addresses = listOf(packet.destination, packet.source) + path
        val out = ByteArrayOutputStream()
        addresses.forEachIndexed { i, a -> out.write(encodeAddress(a, i == addresses.lastIndex)) }
        out.write(0x03)
        out.write(0xF0)
        out.write(packet.info.toByteArray(Charsets.ISO_8859_1))
        return out.toByteArray()
    }

    fun toTnc2(frame: ByteArray): String {
        require(frame.size >= 16) { "Frame AX.25 curto demais" }
        val addresses = mutableListOf<String>()
        var offset = 0
        while (offset + 7 <= frame.size) {
            val call = (0 until 6).map { ((frame[offset + it].toInt() and 0xFF) shr 1).toChar() }
                .joinToString("").trim()
            val s = frame[offset + 6].toInt() and 0xFF
            val ssid = (s shr 1) and 0x0F
            val name = if (ssid > 0) "$call-$ssid" else call
            addresses += if (addresses.size >= 2 && (s and 0x80) != 0) "$name*" else name
            offset += 7
            if ((s and 0x01) != 0) break
            require(addresses.size < 10) { "Endereçamento AX.25 excessivo" }
        }
        require(addresses.size >= 2 && offset + 2 <= frame.size) { "Frame AX.25 sem origem/destino" }
        val info = String(frame, offset + 2, frame.size - offset - 2, Charsets.ISO_8859_1)
        val path = addresses.drop(2)
        return addresses[1] + ">" + addresses[0] + (if (path.isNotEmpty()) "," + path.joinToString(",") else "") + ":" + info
    }
}
