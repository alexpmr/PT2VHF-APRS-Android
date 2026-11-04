package com.pt2vhf.aprs.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import com.pt2vhf.aprs.BuildConfig
import com.pt2vhf.aprs.core.Ax25
import com.pt2vhf.aprs.core.Kiss
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

/** Conexão APRS-IS com reconexão automática; uma linha TNC2 por pacote. */
class AprsIsClient(
    private val onLine: (String) -> Unit,
    private val onState: (connected: Boolean, verified: Boolean, error: String) -> Unit,
) {
    @Volatile private var socket: Socket? = null
    @Volatile private var output: OutputStream? = null
    @Volatile private var running = false
    private val epoch = AtomicLong(0L)
    private var thread: Thread? = null

    fun start(host: String, port: Int, call: String, passcode: Int, filter: String) {
        stop()
        val sessionEpoch = epoch.incrementAndGet()
        running = true
        thread = Thread({
            var backoff = 5_000L
            while (running && epoch.get() == sessionEpoch) {
                var sessionSocket: Socket? = null
                try {
                    val s = Socket()
                    sessionSocket = s
                    s.connect(InetSocketAddress(host, port), 15_000)
                    if (!running || epoch.get() != sessionEpoch) { s.close(); break }
                    s.soTimeout = 120_000
                    socket = s
                    output = s.getOutputStream()
                    val login = "user $call pass $passcode vers PT2VHF-APRS-Android ${BuildConfig.VERSION_NAME}" +
                        (if (filter.isNotBlank()) " filter $filter" else "") + "\r\n"
                    output?.write(login.toByteArray(Charsets.ISO_8859_1))
                    output?.flush()
                    if (epoch.get() == sessionEpoch) onState(true, false, "")
                    backoff = 5_000L
                    val reader = BufferedReader(InputStreamReader(s.getInputStream(), Charsets.ISO_8859_1))
                    while (running && epoch.get() == sessionEpoch) {
                        val line = reader.readLine() ?: break
                        if (line.startsWith("#")) {
                            if (line.contains("logresp") && line.contains(" verified") && epoch.get() == sessionEpoch) onState(true, true, "")
                            if (line.contains("unverified") && epoch.get() == sessionEpoch) onState(true, false, "Passcode não verificado pelo servidor")
                            continue
                        }
                        onLine(line)
                    }
                    if (epoch.get() == sessionEpoch) onState(false, false, if (running) "Servidor encerrou a conexão" else "")
                } catch (e: Exception) {
                    if (epoch.get() == sessionEpoch) onState(false, false, e.message ?: e.javaClass.simpleName)
                } finally {
                    runCatching { sessionSocket?.close() }
                    if (socket === sessionSocket) { socket = null; output = null }
                }
                if (running && epoch.get() == sessionEpoch) Thread.sleep(backoff)
                backoff = (backoff * 2).coerceAtMost(120_000L)
            }
        }, "aprs-is").also { it.isDaemon = true; it.start() }
    }

    fun send(line: String): Boolean {
        // Call from an IO thread only; AprsService queues TX on aprs-tx.
        // A missing/unverified session is NOT a socket error: never disconnect a healthy
        // APRS-IS login just because a TX was attempted before the writer was ready.
        var writeError: Exception? = null
        val sent = synchronized(this) {
            val activeSocket = socket
            val writer = output
            if (!running || activeSocket == null || writer == null ||
                activeSocket.isClosed || !activeSocket.isConnected || activeSocket.isOutputShutdown) {
                false
            } else {
                try {
                    writer.write((line + "\r\n").toByteArray(Charsets.ISO_8859_1))
                    writer.flush()
                    true
                } catch (e: Exception) {
                    writeError = e
                    false
                }
            }
        }
        if (writeError != null && running) {
            onState(false, false, "Erro na escrita APRS-IS: ${writeError!!.javaClass.simpleName}")
            runCatching { socket?.close() } // reconnect via existing reader loop
        }
        return sent
    }

    /** APRS-IS supports updating the filter in-place; do not log in again after a GPS fix. */
    fun updateFilter(filter: String): Boolean {
        val safe = filter.trim().replace("\r", "").replace("\n", " ").take(250)
        return safe.isNotEmpty() && send("#filter $safe")
    }

    /** Invalidate green TX indication immediately before closing an obsolete socket. */
    fun reconnectNow() {
        onState(false, false, "")
        runCatching { socket?.close() }
    }

    fun stop() {
        running = false
        epoch.incrementAndGet()
        onState(false, false, "")
        runCatching { socket?.close() }
        output = null
        thread = null
    }
}

/** TNC KISS por Bluetooth clássico (SPP): Mobilinkd, BTECH UV-Pro, Radtel RT-950 Pro… */
class BluetoothTnc(
    private val onFrame: (String) -> Unit,
    private val onState: (connected: Boolean, name: String, error: String) -> Unit,
) {
    @Volatile private var socket: BluetoothSocket? = null
    @Volatile private var running = false

    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        disconnect()
        running = true
        Thread({
            val name = runCatching { device.name }.getOrNull() ?: device.address
            try {
                val s = device.createRfcommSocketToServiceRecord(SPP_UUID)
                s.connect()
                socket = s
                onState(true, name, "")
                val decoder = Kiss.Decoder()
                val buf = ByteArray(1024)
                val input = s.inputStream
                while (running) {
                    val n = input.read(buf)
                    if (n < 0) break
                    for (frame in decoder.feed(buf.copyOf(n))) {
                        runCatching { onFrame(Ax25.toTnc2(frame)) }
                    }
                }
                onState(false, name, if (running) "Conexão Bluetooth encerrada" else "")
            } catch (e: Exception) {
                onState(false, name, e.message ?: "Falha ao conectar")
            } finally {
                runCatching { socket?.close() }
                socket = null
            }
        }, "bt-tnc").also { it.isDaemon = true; it.start() }
    }

    fun send(tnc2: String): Boolean {
        val s = socket ?: return false
        return try {
            synchronized(this) { s.outputStream.write(Kiss.encode(Ax25.fromTnc2(tnc2))); s.outputStream.flush() }
            true
        } catch (e: Exception) { false }
    }

    val connected: Boolean get() = socket?.isConnected == true

    fun disconnect() {
        running = false
        runCatching { socket?.close() }
        socket = null
    }

    companion object {
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}

/**
 * TNC KISS sobre TCP (herdado da 0.1.0): Direwolf, soundmodem, UZ7HO ou TNC de rede.
 * Mesmo enquadramento KISS do Bluetooth; reconecta com espera crescente até ser desconectado.
 */
class KissTcpTnc(
    private val onFrame: (String) -> Unit,
    private val onState: (connected: Boolean, name: String, error: String) -> Unit,
) {
    @Volatile private var socket: Socket? = null
    @Volatile private var running = false
    private val epoch = AtomicLong(0L)

    fun connect(host: String, port: Int) {
        disconnect()
        val session = epoch.incrementAndGet()
        running = true
        val name = "$host:$port"
        Thread({
            var backoff = 5_000L
            while (running && epoch.get() == session) {
                var s: Socket? = null
                try {
                    s = Socket()
                    s.connect(InetSocketAddress(host, port), 10_000)
                    s.tcpNoDelay = true
                    socket = s
                    if (epoch.get() == session) onState(true, name, "")
                    backoff = 5_000L
                    val decoder = Kiss.Decoder()
                    val buf = ByteArray(1024)
                    val input = s.getInputStream()
                    while (running && epoch.get() == session) {
                        val n = input.read(buf)
                        if (n < 0) break
                        for (frame in decoder.feed(buf.copyOf(n))) runCatching { onFrame(Ax25.toTnc2(frame)) }
                    }
                    if (epoch.get() == session) onState(false, name, if (running) "Conexão KISS TCP encerrada" else "")
                } catch (e: Exception) {
                    if (epoch.get() == session) onState(false, name, e.message ?: "Falha ao conectar")
                } finally {
                    runCatching { s?.close() }
                    if (socket === s) socket = null
                }
                if (running && epoch.get() == session) runCatching { Thread.sleep(backoff) }
                backoff = (backoff * 2).coerceAtMost(60_000L)
            }
        }, "kiss-tcp").also { it.isDaemon = true; it.start() }
    }

    fun send(tnc2: String): Boolean {
        val s = socket ?: return false
        return try {
            synchronized(this) {
                s.getOutputStream().write(Kiss.encode(Ax25.fromTnc2(tnc2)))
                s.getOutputStream().flush()
            }
            true
        } catch (e: Exception) { false }
    }

    val connected: Boolean get() = socket?.let { it.isConnected && !it.isClosed } == true

    fun disconnect() {
        running = false
        epoch.incrementAndGet()
        runCatching { socket?.close() }
        socket = null
    }
}
