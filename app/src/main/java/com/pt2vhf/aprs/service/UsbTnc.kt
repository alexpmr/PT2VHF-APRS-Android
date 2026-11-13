package com.pt2vhf.aprs.service

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.pt2vhf.aprs.core.Ax25
import com.pt2vhf.aprs.core.Kiss
import com.pt2vhf.aprs.core.UsbChip
import com.pt2vhf.aprs.core.ftdiBaudDivisor
import com.pt2vhf.aprs.core.usbControlLines
import java.util.concurrent.atomic.AtomicLong

/**
 * TNC KISS por USB-OTG (herdado da 0.1.0). Driver serial próprio, sem bibliotecas externas:
 * CDC-ACM (Mobilinkd, NinoTNC, rádios com USB nativo), FTDI (FT232R/FT231X) e Silicon Labs CP210x.
 * CH340/CH341 e PL2303 ainda não são suportados (o app avisa).
 *
 * Com [kissMode] = false a mesma porta serve de PTT do modem AFSK (#93): abre com DTR e RTS desligados, não lê
 * nada e só aciona as linhas ([setLines]) ou escreve comandos CAT ([writeRaw]).
 */
class UsbSerialTnc(
    context: Context,
    private val onFrame: (String) -> Unit,
    private val onState: (connected: Boolean, name: String, error: String) -> Unit,
    private val kissMode: Boolean = true,
) {
    private val app = context.applicationContext
    private val usb = app.getSystemService(UsbManager::class.java)
    @Volatile private var connection: UsbDeviceConnection? = null
    @Volatile private var outEp: UsbEndpoint? = null
    @Volatile private var claimed: List<UsbInterface> = emptyList()
    @Volatile private var running = false
    private val epoch = AtomicLong(0L)
    @Volatile private var baud = 9600
    @Volatile private var receiverRegistered = false
    @Volatile private var chip: UsbChip? = null
    @Volatile private var controlIfaceId = 0
    @Volatile private var dataIfaceId = 0
    private val permissionAction = if (kissMode) ACTION_PERMISSION else "$ACTION_PERMISSION.PTT"

    val connected: Boolean get() = running && connection != null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            when (intent.action) {
                permissionAction -> {
                    val device = deviceExtra(intent)
                    if (!wanted) return
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && device != null) open(device)
                    else onState(false, "USB", ERR_PERMISSION)
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device = deviceExtra(intent)
                    if (device != null && running) { close(); onState(false, device.productName ?: "USB", ERR_DETACHED) }
                }
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> if (!running && wanted) connect(baud)
            }
        }
    }
    @Volatile private var wanted = false

    @Suppress("DEPRECATION")
    private fun deviceExtra(intent: Intent): UsbDevice? =
        if (Build.VERSION.SDK_INT >= 33) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
        else intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)

    private fun register() {
        if (receiverRegistered) return
        val filter = IntentFilter(permissionAction).apply {
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        }
        ContextCompat.registerReceiver(app, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        receiverRegistered = true
    }

    /** Primeiro dispositivo serial reconhecido; null se nenhum estiver conectado. */
    fun findDevice(): UsbDevice? = usb?.deviceList?.values?.firstOrNull { chipOf(it) != null }

    /** Nome do primeiro dispositivo USB conectado (suportado ou não), para a tela Rádio. */
    fun describeAttached(): String? = usb?.deviceList?.values?.firstOrNull()?.let {
        (it.productName ?: "USB %04X:%04X".format(it.vendorId, it.productId)) + if (chipOf(it) == null) " ($ERR_UNSUPPORTED)" else ""
    }

    private fun chipOf(d: UsbDevice): UsbChip? = when {
        d.vendorId == 0x0403 -> UsbChip.FTDI
        d.vendorId == 0x10C4 -> UsbChip.CP210X
        (0 until d.interfaceCount).any { d.getInterface(it).interfaceClass == UsbConstants.USB_CLASS_CDC_DATA } -> UsbChip.CDC
        else -> null
    }

    fun connect(baudRate: Int) {
        disconnect()
        baud = baudRate
        wanted = true
        register()
        val device = findDevice()
        if (device == null) {
            onState(false, "USB", if (usb?.deviceList?.isNotEmpty() == true) ERR_UNSUPPORTED else ERR_NO_DEVICE)
            return
        }
        if (usb?.hasPermission(device) == true) open(device)
        else {
            val flags = if (Build.VERSION.SDK_INT >= 31) PendingIntent.FLAG_MUTABLE else 0
            val pi = PendingIntent.getBroadcast(app, if (kissMode) 0 else 1, Intent(permissionAction).setPackage(app.packageName), flags)
            runCatching { usb?.requestPermission(device, pi) }
        }
    }

    private fun open(device: UsbDevice) {
        val chip = chipOf(device) ?: run { onState(false, "USB", ERR_UNSUPPORTED); return }
        val name = device.productName ?: "USB %04X:%04X".format(device.vendorId, device.productId)
        val conn = usb?.openDevice(device) ?: run { onState(false, name, ERR_OPEN); return }
        try {
            val (dataIface, inEp, out) = endpoints(device, chip) ?: throw IllegalStateException(ERR_OPEN)
            val ifaces = mutableListOf(dataIface)
            if (chip == UsbChip.CDC) {
                (0 until device.interfaceCount).map { device.getInterface(it) }
                    .firstOrNull { it.interfaceClass == UsbConstants.USB_CLASS_COMM }?.let { ifaces.add(0, it) }
            }
            for (i in ifaces) check(conn.claimInterface(i, true)) { ERR_OPEN }
            configure(conn, chip, ifaces.first().id, dataIface.id, device.interfaceCount > 1)
            this.chip = chip
            controlIfaceId = ifaces.first().id
            dataIfaceId = dataIface.id
            connection = conn
            outEp = out
            claimed = ifaces
            running = true
            val session = epoch.incrementAndGet()
            onState(true, name, "")
            if (kissMode) Thread({ readLoop(session, conn, inEp, chip, name) }, "usb-tnc").apply { isDaemon = true; start() }
        } catch (e: Exception) {
            runCatching { conn.close() }
            onState(false, name, e.message ?: ERR_OPEN)
        }
    }

    private fun endpoints(device: UsbDevice, chip: UsbChip): Triple<UsbInterface, UsbEndpoint, UsbEndpoint>? {
        val candidates = (0 until device.interfaceCount).map { device.getInterface(it) }
            .filter { chip != UsbChip.CDC || it.interfaceClass == UsbConstants.USB_CLASS_CDC_DATA }
        for (iface in candidates) {
            var input: UsbEndpoint? = null
            var output: UsbEndpoint? = null
            for (e in 0 until iface.endpointCount) {
                val ep = iface.getEndpoint(e)
                if (ep.type != UsbConstants.USB_ENDPOINT_XFER_BULK) continue
                if (ep.direction == UsbConstants.USB_DIR_IN) input = ep else output = ep
            }
            if (input != null && output != null) return Triple(iface, input, output)
        }
        return null
    }

    private fun configure(conn: UsbDeviceConnection, chip: UsbChip, controlIface: Int, dataIface: Int, multiPort: Boolean) {
        // TNC KISS: DTR+RTS ligados como sempre. PTT (#93): tudo desligado para o rádio não entrar em TX ao abrir.
        val lines = usbControlLines(chip, kissMode, kissMode, controlIface, dataIface)
        when (chip) {
            UsbChip.CDC -> {
                val coding = byteArrayOf((baud and 0xFF).toByte(), (baud shr 8 and 0xFF).toByte(),
                    (baud shr 16 and 0xFF).toByte(), (baud shr 24 and 0xFF).toByte(), 0, 0, 8)
                conn.controlTransfer(0x21, 0x20, 0, controlIface, coding, coding.size, 1000) // SET_LINE_CODING
            }
            UsbChip.FTDI -> {
                val index = dataIface + 1
                conn.controlTransfer(0x40, 0, 0, index, null, 0, 1000) // reset
                val (value, high) = ftdiBaudDivisor(baud)
                conn.controlTransfer(0x40, 3, value, if (multiPort) (high shl 8) or index else high, null, 0, 1000) // baud
                conn.controlTransfer(0x40, 4, 0x0008, index, null, 0, 1000) // 8N1
                conn.controlTransfer(0x40, 2, 0, index, null, 0, 1000) // sem controle de fluxo
            }
            UsbChip.CP210X -> {
                conn.controlTransfer(0x41, 0x00, 0x0001, dataIface, null, 0, 1000) // IFC_ENABLE
                val b = byteArrayOf((baud and 0xFF).toByte(), (baud shr 8 and 0xFF).toByte(),
                    (baud shr 16 and 0xFF).toByte(), (baud shr 24 and 0xFF).toByte())
                conn.controlTransfer(0x41, 0x1E, 0, dataIface, b, 4, 1000) // SET_BAUDRATE
                conn.controlTransfer(0x41, 0x03, 0x0800, dataIface, null, 0, 1000) // 8N1
            }
        }
        conn.controlTransfer(lines.requestType, lines.request, lines.value, lines.index, null, 0, 1000) // DTR/RTS
    }

    /** #93: liga/desliga DTR e RTS (PTT). false se a porta não estiver aberta ou o pedido falhar. */
    fun setLines(dtr: Boolean, rts: Boolean): Boolean {
        val conn = connection ?: return false
        val c = chip ?: return false
        val req = usbControlLines(c, dtr, rts, controlIfaceId, dataIfaceId)
        return synchronized(this) { conn.controlTransfer(req.requestType, req.request, req.value, req.index, null, 0, 1000) >= 0 }
    }

    /** #93: escreve bytes crus (comando CAT). */
    fun writeRaw(data: ByteArray): Boolean {
        val conn = connection ?: return false
        val ep = outEp ?: return false
        return synchronized(this) { writeAll(conn, ep, data) }
    }

    private fun writeAll(conn: UsbDeviceConnection, ep: UsbEndpoint, data: ByteArray): Boolean {
        var off = 0
        while (off < data.size) {
            val chunk = data.copyOfRange(off, minOf(data.size, off + ep.maxPacketSize.coerceAtLeast(16)))
            val n = conn.bulkTransfer(ep, chunk, chunk.size, 2000)
            if (n <= 0) return false
            off += n
        }
        return true
    }

    private fun readLoop(session: Long, conn: UsbDeviceConnection, inEp: UsbEndpoint, chip: UsbChip, name: String) {
        val decoder = Kiss.Decoder()
        val buf = ByteArray(maxOf(inEp.maxPacketSize, 64) * 4)
        val packet = maxOf(inEp.maxPacketSize, 64)
        try {
            while (running && epoch.get() == session) {
                val n = conn.bulkTransfer(inEp, buf, buf.size, 250)
                if (n <= 0) continue
                val data = if (chip == UsbChip.FTDI) {
                    // Cada pacote FTDI começa com 2 bytes de estado do modem.
                    val out = java.io.ByteArrayOutputStream(n)
                    var off = 0
                    while (off < n) {
                        val len = minOf(packet, n - off)
                        if (len > 2) out.write(buf, off + 2, len - 2)
                        off += len
                    }
                    out.toByteArray()
                } else buf.copyOf(n)
                if (data.isEmpty()) continue
                for (frame in decoder.feed(data)) runCatching { onFrame(Ax25.toTnc2(frame)) }
            }
        } catch (e: Exception) {
            if (epoch.get() == session) onState(false, name, e.message ?: ERR_OPEN)
        }
    }

    fun send(tnc2: String): Boolean {
        val conn = connection ?: return false
        val ep = outEp ?: return false
        val data = runCatching { Kiss.encode(Ax25.fromTnc2(tnc2)) }.getOrNull() ?: return false
        return synchronized(this) { writeAll(conn, ep, data) }
    }

    private fun close() {
        if (!kissMode) runCatching { setLines(false, false) } // nunca deixar o rádio preso em TX
        running = false
        epoch.incrementAndGet()
        val conn = connection
        connection = null
        outEp = null
        claimed.forEach { runCatching { conn?.releaseInterface(it) } }
        claimed = emptyList()
        chip = null
        runCatching { conn?.close() }
    }

    fun disconnect() {
        wanted = false
        close()
    }

    fun release() {
        disconnect()
        if (receiverRegistered) runCatching { app.unregisterReceiver(receiver) }
        receiverRegistered = false
    }

    companion object {
        const val ACTION_PERMISSION = "com.pt2vhf.aprs.USB_PERMISSION"
        const val ERR_PERMISSION = "Permissão USB negada"
        const val ERR_NO_DEVICE = "Nenhum TNC USB conectado"
        const val ERR_UNSUPPORTED = "Adaptador USB não suportado"
        const val ERR_OPEN = "Falha ao abrir o USB"
        const val ERR_DETACHED = "USB desconectado"
    }
}
