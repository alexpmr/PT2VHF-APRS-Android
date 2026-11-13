package com.pt2vhf.aprs.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.pt2vhf.aprs.core.Ax25
import com.pt2vhf.aprs.core.BleKiss
import com.pt2vhf.aprs.core.Kiss
import com.pt2vhf.aprs.data.InternalText
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * #92: TNC KISS por Bluetooth Low Energy (GATT). Aceita o serviço KISS BLE "padrão" (Mobilinkd TNC4, PicoAPRS,
 * trackers LoRa APRS…) e o Nordic UART. Mesmo enquadramento KISS dos outros TNCs; quadros maiores que o MTU
 * vão em pedaços, uma escrita por vez. Reconecta sozinho enquanto estiver selecionado.
 */
@SuppressLint("MissingPermission") // AprsService confere BLUETOOTH_CONNECT antes de conectar
class BleTnc(
    context: Context,
    private val onFrame: (String) -> Unit,
    private val onState: (connected: Boolean, name: String, error: String) -> Unit,
) {
    private val app = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val epoch = AtomicLong(0L)
    @Volatile private var gatt: BluetoothGatt? = null
    @Volatile private var writeChar: BluetoothGattCharacteristic? = null
    @Volatile private var ready = false
    @Volatile private var running = false
    @Volatile private var mtu = BleKiss.DEFAULT_MTU
    @Volatile private var writeDone: CountDownLatch? = null
    @Volatile private var writeOk = false
    @Volatile private var address = ""
    @Volatile private var name = "BLE"
    private var backoff = 5_000L

    val connected: Boolean get() = running && ready

    fun connect(address: String) {
        disconnect()
        this.address = address
        running = true
        backoff = 5_000L
        open(epoch.incrementAndGet())
    }

    private fun open(session: Long) {
        if (!running || epoch.get() != session) return
        val adapter = app.getSystemService(BluetoothManager::class.java)?.adapter
        val device = runCatching { adapter?.getRemoteDevice(address) }.getOrNull()
        if (adapter == null || device == null || !adapter.isEnabled) {
            onState(false, name, InternalText.ERR_CONNECT_FAILED)
            retry(session)
            return
        }
        name = runCatching { device.name }.getOrNull()?.takeIf { it.isNotBlank() } ?: address
        mtu = BleKiss.DEFAULT_MTU
        ready = false
        gatt = runCatching { device.connectGatt(app, false, callback(session), BluetoothDevice.TRANSPORT_LE) }.getOrNull()
        if (gatt == null) { onState(false, name, InternalText.ERR_CONNECT_FAILED); retry(session) }
    }

    private fun retry(session: Long) {
        if (!running || epoch.get() != session) return
        val wait = backoff
        backoff = (backoff * 2).coerceAtMost(60_000L)
        handler.postDelayed({ open(session) }, wait)
    }

    private fun lost(session: Long, g: BluetoothGatt, error: String) {
        if (epoch.get() != session) { runCatching { g.close() }; return }
        val wasReady = ready
        ready = false
        writeChar = null
        writeOk = false
        writeDone?.countDown()
        runCatching { g.close() }
        if (gatt === g) gatt = null
        if (running) {
            onState(false, name, if (wasReady && error.isEmpty()) InternalText.ERR_BLE_CLOSED else error)
            retry(session)
        }
    }

    private fun callback(session: Long) = object : BluetoothGattCallback() {
        private val decoder = Kiss.Decoder()

        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            if (epoch.get() != session) { runCatching { g.close() }; return }
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                // MTU maior primeiro (menos pedaços por quadro); se o pedido não sair, segue no padrão.
                if (!g.requestMtu(BleKiss.WANTED_MTU)) g.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED || status != BluetoothGatt.GATT_SUCCESS) {
                lost(session, g, if (ready) "" else InternalText.ERR_CONNECT_FAILED)
            }
        }

        override fun onMtuChanged(g: BluetoothGatt, newMtu: Int, status: Int) {
            if (epoch.get() != session) return
            if (status == BluetoothGatt.GATT_SUCCESS) mtu = newMtu
            g.discoverServices()
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (epoch.get() != session) return
            val profile = BleKiss.pick(g.services.map { it.uuid })
            val service = profile?.let { g.getService(it.service) }
            val tx = profile?.let { service?.getCharacteristic(it.write) }
            val rx = profile?.let { service?.getCharacteristic(it.notify) }
            if (status != BluetoothGatt.GATT_SUCCESS || tx == null || rx == null) {
                // Aparelho sem serviço KISS: não adianta insistir.
                running = false
                onState(false, name, InternalText.ERR_BLE_NO_KISS)
                runCatching { g.disconnect(); g.close() }
                if (gatt === g) gatt = null
                return
            }
            writeChar = tx
            g.setCharacteristicNotification(rx, true)
            val cccd = rx.getDescriptor(BleKiss.CCCD)
            if (cccd == null) { becomeReady(); return }
            val value = if (rx.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0)
                BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
            val started = if (Build.VERSION.SDK_INT >= 33) g.writeDescriptor(cccd, value) == BluetoothGatt.GATT_SUCCESS
            else @Suppress("DEPRECATION") run { cccd.setValue(value); g.writeDescriptor(cccd) }
            if (!started) becomeReady() // alguns aparelhos notificam mesmo sem o CCCD
        }

        override fun onDescriptorWrite(g: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            if (epoch.get() == session && descriptor.uuid == BleKiss.CCCD) becomeReady()
        }

        private fun becomeReady() {
            if (ready) return
            ready = true
            backoff = 5_000L
            onState(true, name, "")
        }

        override fun onCharacteristicWrite(g: BluetoothGatt, c: BluetoothGattCharacteristic, status: Int) {
            writeOk = status == BluetoothGatt.GATT_SUCCESS
            writeDone?.countDown()
        }

        override fun onCharacteristicChanged(g: BluetoothGatt, c: BluetoothGattCharacteristic, value: ByteArray) {
            received(value)
        }

        @Deprecated("Android < 13")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(g: BluetoothGatt, c: BluetoothGattCharacteristic) {
            if (Build.VERSION.SDK_INT < 33) c.value?.let { received(it.copyOf()) }
        }

        private fun received(data: ByteArray) {
            if (epoch.get() != session) return
            for (frame in decoder.feed(data)) runCatching { onFrame(Ax25.toTnc2(frame)) }
        }
    }

    /** Chamar só da thread de TX: espera a confirmação de cada pedaço antes do próximo. */
    fun send(tnc2: String): Boolean {
        val g = gatt ?: return false
        val c = writeChar ?: return false
        if (!connected) return false
        val data = runCatching { Kiss.encode(Ax25.fromTnc2(tnc2)) }.getOrNull() ?: return false
        val type = if (c.properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0)
            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT else BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
        return synchronized(this) {
            for (chunk in BleKiss.chunks(data, mtu)) {
                val latch = CountDownLatch(1)
                writeDone = latch
                writeOk = false
                val started = if (Build.VERSION.SDK_INT >= 33) g.writeCharacteristic(c, chunk, type) == BluetoothGatt.GATT_SUCCESS
                else @Suppress("DEPRECATION") run { c.writeType = type; c.setValue(chunk); g.writeCharacteristic(c) }
                if (!started || !latch.await(3, TimeUnit.SECONDS) || !writeOk) return@synchronized false
            }
            true
        }
    }

    fun disconnect() {
        running = false
        epoch.incrementAndGet()
        handler.removeCallbacksAndMessages(null)
        ready = false
        writeChar = null
        writeDone?.countDown()
        val g = gatt
        gatt = null
        runCatching { g?.disconnect() }
        runCatching { g?.close() }
    }
}
