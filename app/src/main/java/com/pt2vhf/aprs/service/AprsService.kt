package com.pt2vhf.aprs.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.pt2vhf.aprs.MainActivity
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.core.AprsMessage
import com.pt2vhf.aprs.core.AprsPacket
import com.pt2vhf.aprs.core.SmartBeaconing
import com.pt2vhf.aprs.core.ackInfo
import com.pt2vhf.aprs.core.expandFilter
import com.pt2vhf.aprs.core.messageInfo
import com.pt2vhf.aprs.core.beaconCommentWithSite
import com.pt2vhf.aprs.core.positionInfo
import com.pt2vhf.aprs.core.splitMessage
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.ChatMessage
import com.pt2vhf.aprs.data.Medium
import com.pt2vhf.aprs.data.OutboundSendResult
import com.pt2vhf.aprs.data.OutboundBeaconResult
import com.pt2vhf.aprs.data.Settings
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

/** Serviço em primeiro plano: mantém APRS-IS, TNC Bluetooth, GPS e o tracker ativos com a tela desligada. */
class AprsService : Service(), LocationListener {
    private lateinit var settings: Settings
    private val handler = Handler(Looper.getMainLooper())
    private var smart = SmartBeaconing()
    private fun configuredSmartBeaconing(s: Settings): SmartBeaconing = SmartBeaconing(
        slowSpeed = s.smartSlowSpeedKmh.toDouble(),
        slowRateSec = s.smartSlowIntervalSec,
        fastSpeed = maxOf(s.smartFastSpeedKmh, s.smartSlowSpeedKmh + 1).toDouble(),
        fastRateSec = s.smartFastIntervalSec,
    )
    @Volatile private var lastBeaconAt = 0L
    private val beaconPending = AtomicBoolean(false)
    // Android invokes onStartCommand and LocationListener on the main thread. Never write TCP there.
    private val txWorker = Executors.newSingleThreadExecutor { task ->
        Thread(task, "aprs-tx").apply { isDaemon = true }
    }
    private var lastBeaconCourse: Int? = null
    private var lastLocation: Location? = null
    private var msgCounter = (System.currentTimeMillis() / 1000 % 900).toInt() + 1
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    @Volatile private var previousDefaultNetwork: Network? = null

    private val aprsIs = AprsIsClient(
        onLine = { line -> handleLine(line, Medium.INTERNET) },
        onState = { connected, verified, error ->
            AprsRepository.updateStatus {
                it.copy(aprsIsConnected = connected, aprsIsVerified = verified,
                    aprsIsServer = "${settings.server}:${settings.port}", aprsIsError = error)
            }
            refreshNotification()
        },
    )
    private val tnc = BluetoothTnc(
        onFrame = { line ->
            AprsRepository.updateStatus { it.copy(tncRx = it.tncRx + 1) }
            handleLine(line, Medium.RF)
        },
        onState = { connected, name, error ->
            AprsRepository.updateStatus { it.copy(tncConnected = connected, tncName = name, tncError = error) }
            refreshNotification()
        },
    )

    private fun restoreTrackerPreference() {
        val enabled = settings.configured && settings.trackerAutoStart &&
            (settings.txViaInternet || settings.txViaRf) &&
            !Settings.flag(this, "tracker_explicit_off")
        AprsRepository.updateStatus {
            it.copy(trackerOn = enabled, nextBeaconSec = if (enabled) it.nextBeaconSec else null)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        AprsRepository.init(this)
        settings = Settings.load(this)
        smart = configuredSmartBeaconing(settings)
        restoreTrackerPreference()
        createChannels()
        startForegroundCompat()
        connectAprsIs()
        startLocation()
        watchNetwork()
        if (settings.btAddress.isNotEmpty() && !Settings.flag(this, "tnc_manual_pause")) connectBluetooth(settings.btAddress)
        handler.post(ticker)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_RELOAD -> {
                settings = Settings.load(this)
                smart = configuredSmartBeaconing(settings)
                restoreTrackerPreference()
                if (settings.connectAprsIs && !Settings.flag(this, "aprs_manual_pause")) connectAprsIs() else {
                    aprsIs.stop()
                    AprsRepository.updateStatus { it.copy(aprsIsConnected = false, aprsIsVerified = false, aprsIsError = "") }
                }
                refreshNotification()
            }
            ACTION_APRS_CONNECT -> {
                Settings.setFlag(this, "aprs_manual_pause", false)
                connectAprsIs(force = true)
            }
            ACTION_APRS_DISCONNECT -> {
                Settings.setFlag(this, "aprs_manual_pause", true)
                aprsIs.stop()
                AprsRepository.updateStatus {
                    it.copy(aprsIsConnected = false, aprsIsVerified = false, aprsIsError = "")
                }
            }
            ACTION_BEACON -> queueBeacon(intent.getStringExtra(EXTRA_REQUEST_ID))
            ACTION_TRACKER_ON -> {
                Settings.setFlag(this, "tracker_explicit_off", false)
                AprsRepository.updateStatus { it.copy(trackerOn = true) }
                // Let the regular SmartBeaconing clock handle the next TX after a fresh GPS fix.
                checkTracker()
            }
            ACTION_TRACKER_OFF -> {
                Settings.setFlag(this, "tracker_explicit_off", true)
                AprsRepository.updateStatus { it.copy(trackerOn = false, nextBeaconSec = null) }
            }
            ACTION_MESSAGE -> intent.getStringExtra(EXTRA_TO)?.let { to ->
                // Write on dedicated IO worker. Report true only after transport accepts the bytes.
                val message = intent.getStringExtra(EXTRA_TEXT).orEmpty()
                val requestId = intent.getStringExtra(EXTRA_REQUEST_ID)
                txWorker.execute {
                    val submitted = runCatching { sendMessage(to, message) }.getOrDefault(false)
                    if (!requestId.isNullOrBlank()) {
                        AprsRepository.reportSendResult(OutboundSendResult(requestId, submitted,
                            if (submitted) "" else sendFailureReason()))
                    }
                }
            }
            ACTION_BT_CONNECT -> intent.getStringExtra(EXTRA_ADDRESS)?.let {
                Settings.setFlag(this, "tnc_manual_pause", false)
                settings = settings.copy(btAddress = it); Settings.save(this, settings); connectBluetooth(it)
            }
            ACTION_BT_PAUSE -> {
                Settings.setFlag(this, "tnc_manual_pause", true)
                tnc.disconnect()
                AprsRepository.updateStatus { it.copy(tncConnected = false, tncName = "") }
            }
            ACTION_BT_RESUME -> {
                Settings.setFlag(this, "tnc_manual_pause", false)
                settings.btAddress.takeIf { it.isNotBlank() }?.let { connectBluetooth(it) }
            }
            ACTION_BT_DISCONNECT -> {
                settings = settings.copy(btAddress = ""); Settings.save(this, settings); tnc.disconnect()
                AprsRepository.updateStatus { it.copy(tncConnected = false, tncName = "") }
            }
            ACTION_STOP -> {
                Settings.setFlag(this, "tracker_explicit_off", true)
                AprsRepository.updateStatus { it.copy(trackerOn = false, nextBeaconSec = null) }
                stopSelf(); return START_NOT_STICKY
            }
        }
        refreshNotification()
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        txWorker.shutdownNow()
        aprsIs.stop()
        tnc.disconnect()
        runCatching { (getSystemService(LOCATION_SERVICE) as LocationManager).removeUpdates(this) }
        networkCallback?.let { runCatching { getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(it) } }
        AprsRepository.updateStatus { it.copy(aprsIsConnected = false, aprsIsVerified = false, tncConnected = false, trackerOn = false) }
        super.onDestroy()
    }

    private fun connectAprsIs(force: Boolean = false) {
        if (!settings.configured || (!force && (!settings.connectAprsIs || Settings.flag(this, "aprs_manual_pause")))) return
        val s = AprsRepository.status.value
        aprsIs.start(settings.server, settings.port, settings.fullCall, settings.passcode,
            expandFilter(settings.filter, s.lat, s.lon))
    }

    @SuppressLint("MissingPermission")
    private fun connectBluetooth(address: String) {
        if (Build.VERSION.SDK_INT >= 31 && !granted(Manifest.permission.BLUETOOTH_CONNECT)) return
        val adapter = getSystemService(BluetoothManager::class.java)?.adapter ?: return
        val device = runCatching { adapter.getRemoteDevice(address) }.getOrNull() ?: return
        tnc.connect(device)
    }

    private fun watchNetwork() {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return
        val cb = object : ConnectivityManager.NetworkCallback() {
            // Wi-Fi ↔ dados móveis: reconecta em vez de esperar o timeout do socket antigo.
            override fun onAvailable(network: Network) {
                val previous = previousDefaultNetwork
                previousDefaultNetwork = network
                // The initial callback reports the existing default network, not a handover.
                if (previous != null && previous != network) {
                    handler.postDelayed({
                        if (settings.connectAprsIs && !Settings.flag(this@AprsService, "aprs_manual_pause"))
                            aprsIs.reconnectNow()
                    }, 1500)
                }
            }
        }
        runCatching { cm.registerDefaultNetworkCallback(cb) }
        networkCallback = cb
    }

    @SuppressLint("MissingPermission")
    private fun startLocation() {
        if (!granted(Manifest.permission.ACCESS_FINE_LOCATION)) return
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        runCatching { lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 0f, this, Looper.getMainLooper()) }
        runCatching { lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000L, 0f, this, Looper.getMainLooper()) }
    }

    override fun onLocationChanged(location: Location) {
        val previous = lastLocation
        if (previous != null && previous.provider == LocationManager.GPS_PROVIDER &&
            location.provider != LocationManager.GPS_PROVIDER && location.time - previous.time < 30_000) return
        val firstFix = previous == null
        lastLocation = location
        val speed = if (location.hasSpeed()) location.speed * 3.6 else 0.0
        AprsRepository.updateStatus {
            it.copy(lat = location.latitude, lon = location.longitude, gpsAccuracyM = location.accuracy,
                speedKmh = speed, gpsFixTimeMs = System.currentTimeMillis(),
                course = if (location.hasBearing()) location.bearing.toInt() else it.course)
        }
        if (AprsRepository.status.value.trackerOn &&
            Settings.readOption(this, "tracklog_enabled", "true") == "true") {
            AprsRepository.recordTrack(location.latitude, location.longitude)
        }
        if (firstFix && settings.filter.contains("r/")) {
            val expanded = expandFilter(settings.filter, location.latitude, location.longitude)
            // Reconfigure the filter within the authenticated session, without closing the socket.
            txWorker.execute { aprsIs.updateFilter(expanded) }
        }
        checkTracker()
    }

    private fun checkTracker() {
        val s = AprsRepository.status.value
        if (!s.trackerOn || s.lat == null || settings.beaconMode == "manual") return
        val since = (System.currentTimeMillis() - lastBeaconAt) / 1000
        val course = s.course
        val change = if (course != null && lastBeaconCourse != null)
            ((course - lastBeaconCourse!! + 540) % 360 - 180).toDouble() else 0.0
        val interval = settings.periodicIntervalSec.toLong()
        val due = if (settings.beaconMode == "periodic") since >= interval
            else smart.shouldBeacon(since, s.speedKmh, abs(change))
        if (due) queueBeacon()
        AprsRepository.updateStatus {
            it.copy(nextBeaconSec = if (settings.beaconMode == "periodic")
                (interval - (System.currentTimeMillis() - lastBeaconAt) / 1000).coerceAtLeast(0)
                else smart.nextBeaconSeconds((System.currentTimeMillis() - lastBeaconAt) / 1000, it.speedKmh))
        }
    }

    private val ticker = object : Runnable {
        override fun run() {
            AprsRepository.updateStatus { state ->
                if (state.gpsFixTimeMs > 0 && System.currentTimeMillis() - state.gpsFixTimeMs > 120_000L)
                    state.copy(gpsAccuracyM = null) else state
            }
            checkTracker()
            handler.postDelayed(this, 5000)
        }
    }

    /** Never block Android main thread with APRS-IS/TNC writes or duplicate auto beacons. */
    private fun queueBeacon(requestId: String? = null) {
        if (!beaconPending.compareAndSet(false, true)) {
            if (!requestId.isNullOrBlank())
                AprsRepository.reportBeaconResult(OutboundBeaconResult(requestId, false))
            return
        }
        txWorker.execute {
            try {
                val submitted = runCatching { sendBeacon() }.getOrDefault(false)
                if (!requestId.isNullOrBlank())
                    AprsRepository.reportBeaconResult(OutboundBeaconResult(requestId, submitted))
            } finally {
                beaconPending.set(false)
            }
        }
    }

    private fun sendBeacon(): Boolean {
        if (!settings.configured) return false
        val s = AprsRepository.status.value
        val lat = s.lat ?: return false
        val lon = s.lon ?: return false
        val moving = s.speedKmh >= 5.0
        val info = positionInfo(lat, lon, settings.symbolTable, settings.symbol, beaconCommentWithSite(settings.comment),
            if (moving) s.course else null, if (moving) s.speedKmh else null)
        if (transmit(info) == null) return false
        lastBeaconAt = System.currentTimeMillis()
        lastBeaconCourse = s.course
        AprsRepository.updateStatus { it.copy(beaconsSent = it.beaconsSent + 1) }
        return true
    }

    /** Envia pelo(s) meio(s) ativo(s) e autorizado(s): RF (TNC) e/ou APRS-IS. Retorna o meio usado. */
    private fun transmit(info: String): Medium? {
        val call = settings.fullCall
        var used: Medium? = null
        if (settings.txViaRf && tnc.connected) {
            val path = settings.rfPath.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            val line = "$call>$TOCALL" + (if (path.isNotEmpty()) "," + path.joinToString(",") else "") + ":" + info
            if (tnc.send(line)) { used = Medium.RF; AprsRepository.updateStatus { it.copy(tncTx = it.tncTx + 1) } }
        }
        val status = AprsRepository.status.value
        if (settings.txViaInternet && status.aprsIsVerified) {
            if (aprsIs.send("$call>$TOCALL,TCPIP*:$info")) {
                used = used ?: Medium.INTERNET
                AprsRepository.updateStatus { it.copy(aprsTx = it.aprsTx + 1) }
            }
        }
        AprsRepository.monitorLine("TX ${used?.label ?: UiText.text(R.string.not_sent)} $call>$TOCALL:$info")
        return used
    }

    private fun sendMessage(to: String, text: String): Boolean {
        val dest = to.trim().uppercase()
        if (dest.isEmpty() || text.isBlank() || !settings.configured) return false
        val general = dest == "CQ" || dest.startsWith("BLN")
        var allSubmitted = true
        for (part in splitMessage(text)) {
            val msgId = if (general) null else nextMsgId()
            val local = ChatMessage(AprsRepository.newId(), settings.fullCall, dest, part, System.currentTimeMillis(),
                true, null, msgId = msgId, status = "Enviando",
                kind = if (dest.startsWith("BLN")) AprsMessage.Kind.BULLETIN else AprsMessage.Kind.MESSAGE)
            AprsRepository.add(local)
            if (!sendWithRetry(local, dest, part, msgId, 0)) allSubmitted = false
        }
        return allSubmitted
    }

    /** true means written to at least one authorized transport, not remotely delivered. */
    private fun sendWithRetry(local: ChatMessage, dest: String, text: String, msgId: String?, attempt: Int): Boolean {
        if (!AprsRepository.containsMessage(local.id) || AprsRepository.isAcked(local.id)) return true
        val medium = transmit(messageInfo(dest, text, msgId))
        when {
            medium == null -> AprsRepository.setStatus(local.id, "Sem conexão para enviar")
            msgId == null -> AprsRepository.setStatus(local.id, "Enviada · ${medium.label}")
            attempt >= 4 -> AprsRepository.setStatus(local.id, "Sem ACK após 5 tentativas")
            else -> {
                AprsRepository.setStatus(local.id, if (attempt == 0) "Aguardando ACK · ${medium.label}"
                    else "Aguardando ACK (${attempt + 1}ª tentativa)")
                handler.postDelayed({ sendWithRetry(local, dest, text, msgId, attempt + 1) }, 30_000L * (attempt + 1))
            }
        }
        return medium != null
    }

    private fun sendFailureReason(): String {
        val s = AprsRepository.status.value
        return when {
            !settings.configured -> UiText.text(R.string.configure_callsign)
            !settings.txViaInternet && !settings.txViaRf -> UiText.text(R.string.tx_disabled)
            !settings.txViaInternet && !s.tncConnected -> UiText.text(R.string.tx_tnc_unavailable)
            settings.txViaInternet && !s.aprsIsVerified && (!settings.txViaRf || !s.tncConnected) ->
                UiText.text(if (s.aprsIsConnected) R.string.tx_aprs_unverified else R.string.tx_aprs_unavailable)
            else -> UiText.text(R.string.tx_send_failed)
        }
    }

    private fun nextMsgId(): String {
        msgCounter = msgCounter % 999 + 1
        return msgCounter.toString()
    }

    private fun handleLine(line: String, medium: Medium) {
        val packet = AprsPacket.parse(line) ?: return
        if (medium == Medium.INTERNET)
            AprsRepository.updateStatus { it.copy(aprsRx = it.aprsRx + 1) }
        AprsRepository.monitorLine("RX ${medium.label} ${line.take(300)}")
        val forUs = AprsRepository.ingest(packet, medium, settings.fullCall) ?: return
        // ACK pelo mesmo meio por onde a mensagem chegou.
        forUs.msgId?.let { id ->
            val info = ackInfo(forUs.from, id)
            if (medium == Medium.RF && tnc.connected) {
                tnc.send("${settings.fullCall}>$TOCALL:${info}")
            } else if (AprsRepository.status.value.aprsIsVerified) {
                aprsIs.send("${settings.fullCall}>$TOCALL,TCPIP*:$info")
            }
        }
        notifyMessage(forUs)
    }

    private fun createChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CH_SERVICE, UiText.text(R.string.connection_channel), NotificationManager.IMPORTANCE_LOW))
        nm.createNotificationChannel(NotificationChannel(CH_MESSAGES, UiText.text(R.string.messages_channel), NotificationManager.IMPORTANCE_HIGH))
    }

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(this, 0,
        Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    private fun buildNotification(): Notification {
        val s = AprsRepository.status.value
        val parts = mutableListOf<String>()
        parts += if (s.aprsIsVerified) UiText.text(R.string.aprs_ok) else if (s.aprsIsConnected) UiText.text(R.string.aprs_unverified) else UiText.text(R.string.aprs_disconnected)
        if (s.tncConnected) parts += "TNC ${s.tncName}"
        if (s.trackerOn) parts += UiText.text(R.string.active_tracker_notification, s.beaconsSent)
        return NotificationCompat.Builder(this, CH_SERVICE)
            .setSmallIcon(R.drawable.ic_stat_aprs)
            .setContentTitle(if (settings.configured) settings.fullCall else "PT2VHF APRS Client")
            .setContentText(parts.joinToString(" · "))
            .setOngoing(true)
            .setContentIntent(openAppIntent())
            .build()
    }

    private fun refreshNotification() {
        if (!canNotify()) return
        runCatching { getSystemService(NotificationManager::class.java).notify(NOTIF_SERVICE, buildNotification()) }
    }

    private fun notifyMessage(m: ChatMessage) {
        if (!canNotify()) return
        val n = NotificationCompat.Builder(this, CH_MESSAGES)
            .setSmallIcon(R.drawable.ic_stat_aprs)
            .setContentTitle(UiText.text(R.string.message_from, m.from))
            .setContentText(m.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(m.text))
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .build()
        getSystemService(NotificationManager::class.java).notify(m.id.toInt(), n)
    }

    private fun startForegroundCompat() {
        var types = 0
        if (Build.VERSION.SDK_INT >= 29) {
            if (granted(Manifest.permission.ACCESS_FINE_LOCATION)) types = types or ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            // specialUse: cliente APRS conectado o tempo todo (dataSync tem limite de 6 h no Android 15).
            if (Build.VERSION.SDK_INT >= 34) types = types or ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            if (Build.VERSION.SDK_INT >= 31 && granted(Manifest.permission.BLUETOOTH_CONNECT))
                types = types or ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
        }
        ServiceCompat.startForeground(this, NOTIF_SERVICE, buildNotification(), types)
    }

    private fun canNotify(): Boolean = Build.VERSION.SDK_INT < 33 || granted(Manifest.permission.POST_NOTIFICATIONS)

    private fun granted(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}

    companion object {
        const val TOCALL = "APZVHF"
        private const val CH_SERVICE = "service"
        private const val CH_MESSAGES = "messages"
        private const val NOTIF_SERVICE = 1
        const val ACTION_RELOAD = "reload"
        const val ACTION_BEACON = "beacon"
        const val ACTION_APRS_CONNECT = "aprs_connect"
        const val ACTION_APRS_DISCONNECT = "aprs_disconnect"
        const val ACTION_TRACKER_ON = "tracker_on"
        const val ACTION_TRACKER_OFF = "tracker_off"
        const val ACTION_MESSAGE = "message"
        const val ACTION_BT_CONNECT = "bt_connect"
        const val ACTION_BT_DISCONNECT = "bt_disconnect"
        const val ACTION_BT_PAUSE = "bt_pause"
        const val ACTION_BT_RESUME = "bt_resume"
        const val ACTION_STOP = "stop"
        const val EXTRA_TO = "to"
        const val EXTRA_TEXT = "text"
        const val EXTRA_REQUEST_ID = "request_id"
        const val EXTRA_ADDRESS = "address"

        fun send(context: Context, action: String, extras: Map<String, String> = emptyMap()) {
            val intent = Intent(context, AprsService::class.java).setAction(action)
            extras.forEach { (k, v) -> intent.putExtra(k, v) }
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
