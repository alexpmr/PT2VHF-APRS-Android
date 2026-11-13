package com.pt2vhf.aprs.core

import java.util.UUID

// ---------- #92 TNC KISS por Bluetooth LE ----------

/** Serviços KISS-sobre-BLE reconhecidos e a fragmentação dos quadros no MTU. Sem Android: testável na JVM. */
object BleKiss {
    /** Serviço GATT com a característica em que o app escreve e a que notifica o que o TNC recebeu. */
    data class Profile(val name: String, val service: UUID, val write: UUID, val notify: UUID)

    /** KISS BLE "padrão" (Mobilinkd TNC4, PicoAPRS, trackers LoRa APRS e outros). */
    val KISS_BLE = Profile("KISS BLE",
        UUID.fromString("00000001-ba2a-46c9-ae49-01b0961f68bb"),
        UUID.fromString("00000002-ba2a-46c9-ae49-01b0961f68bb"),
        UUID.fromString("00000003-ba2a-46c9-ae49-01b0961f68bb"))

    /** Nordic UART Service: RX do periférico (escrita) …0002, TX do periférico (notificação) …0003. */
    val NORDIC_UART = Profile("Nordic UART",
        UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"),
        UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"),
        UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e"))

    /** Em ordem de preferência: se o aparelho tiver os dois, usa o KISS BLE. */
    val PROFILES = listOf(KISS_BLE, NORDIC_UART)

    /** Descritor CCCD para ligar notificações. */
    val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    /** MTU padrão do BLE quando a negociação não acontece (20 bytes úteis). */
    const val DEFAULT_MTU = 23
    const val WANTED_MTU = 247

    fun pick(services: Collection<UUID>): Profile? = PROFILES.firstOrNull { it.service in services }

    /** O anúncio do aparelho cita algum serviço KISS conhecido? (só para destacar na lista de busca) */
    fun advertisesKiss(services: Collection<UUID>): Boolean = pick(services) != null

    /** Divide [data] em pedaços de no máximo MTU−3 bytes (cabeçalho ATT), na ordem. */
    fun chunks(data: ByteArray, mtu: Int): List<ByteArray> {
        val size = (mtu.coerceAtLeast(DEFAULT_MTU) - 3).coerceAtMost(512)
        if (data.isEmpty()) return emptyList()
        return (data.indices step size).map { data.copyOfRange(it, minOf(data.size, it + size)) }
    }
}

// ---------- #93 PTT por RTS/DTR ou CAT ----------

/** Modos de PTT do modem AFSK. VOX é o comportamento de sempre (o áudio aciona o rádio). */
object PttMode {
    const val VOX = "vox"
    const val RTS = "rts"
    const val DTR = "dtr"
    const val CAT = "cat"
    val ALL = listOf(VOX, RTS, DTR, CAT)
}

/** Comandos CAT para entrar e sair de transmissão. */
object CatPtt {
    const val KENWOOD = "kenwood" // Kenwood, Elecraft: TX; / RX;
    const val YAESU = "yaesu" // Yaesu FT-991/FT-891/FTDX/FT-710: TX1; / TX0;
    const val YAESU_OLD = "yaesu_old" // Yaesu FT-817/818/857/897: 5 bytes binários
    const val ICOM = "icom" // Icom CI-V: comando 1C 00
    val PROTOCOLS = listOf(KENWOOD, YAESU, YAESU_OLD, ICOM)
    const val DEFAULT_CIV = 0x94 // IC-7300
    private const val CIV_CONTROLLER = 0xE0

    fun command(protocol: String, transmit: Boolean, civAddress: Int = DEFAULT_CIV): ByteArray = when (protocol) {
        KENWOOD -> (if (transmit) "TX;" else "RX;").toByteArray(Charsets.US_ASCII)
        YAESU -> (if (transmit) "TX1;" else "TX0;").toByteArray(Charsets.US_ASCII)
        YAESU_OLD -> bytes(0, 0, 0, 0, if (transmit) 0x08 else 0x88)
        ICOM -> bytes(0xFE, 0xFE, civAddress and 0xFF, CIV_CONTROLLER, 0x1C, 0x00, if (transmit) 1 else 0, 0xFD)
        else -> throw IllegalArgumentException(protocol)
    }

    /** Endereço CI-V digitado em hexadecimal ("94", "0x94", "A4h"); null se inválido. */
    fun parseCivAddress(text: String): Int? = text.trim().lowercase().removePrefix("0x").removeSuffix("h")
        .takeIf { it.length in 1..2 }?.toIntOrNull(16)?.takeIf { it in 0x01..0xDF }

    private fun bytes(vararg v: Int) = ByteArray(v.size) { v[it].toByte() }
}

/** Chips USB-serial do driver próprio (#47). */
enum class UsbChip { CDC, FTDI, CP210X }

/** Pedido de controle USB (controlTransfer) para mudar DTR/RTS. */
data class UsbControlRequest(val requestType: Int, val request: Int, val value: Int, val index: Int)

/**
 * Pedido que liga/desliga DTR e RTS em cada chip: CDC-ACM SET_CONTROL_LINE_STATE, FTDI SET_MODEM_CTRL (máscara
 * nos bits 8–9) e CP210x SET_MHS (idem).
 */
fun usbControlLines(chip: UsbChip, dtr: Boolean, rts: Boolean, controlIface: Int, dataIface: Int): UsbControlRequest {
    val bits = (if (dtr) 1 else 0) or (if (rts) 2 else 0)
    return when (chip) {
        UsbChip.CDC -> UsbControlRequest(0x21, 0x22, bits, controlIface)
        UsbChip.FTDI -> UsbControlRequest(0x40, 1, 0x0300 or bits, dataIface + 1)
        UsbChip.CP210X -> UsbControlRequest(0x41, 0x07, 0x0300 or bits, dataIface)
    }
}

/** Linhas (DTR, RTS) para o modo de PTT: só a linha escolhida sobe, e só durante a transmissão. */
fun pttLines(mode: String, transmit: Boolean): Pair<Boolean, Boolean> = when (mode) {
    PttMode.RTS -> false to transmit
    PttMode.DTR -> transmit to false
    else -> false to false
}
