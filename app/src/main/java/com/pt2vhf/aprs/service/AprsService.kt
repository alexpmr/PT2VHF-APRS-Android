package com.pt2vhf.aprs.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
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
import com.pt2vhf.aprs.core.effectiveAprsFilter
import com.pt2vhf.aprs.core.messageInfo
import com.pt2vhf.aprs.core.composeBeaconComment
import com.pt2vhf.aprs.core.decodeAprsText
import com.pt2vhf.aprs.core.formatAltitude
import com.pt2vhf.aprs.core.formatVoiceFrequency
import com.pt2vhf.aprs.data.Station
import com.pt2vhf.aprs.core.positionInfo
import com.pt2vhf.aprs.core.splitMessage
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.ChatMessage
import com.pt2vhf.aprs.data.Medium
import com.pt2vhf.aprs.data.OutboundSendResult
import com.pt2vhf.aprs.data.OutboundBeaconResult
import com.pt2vhf.aprs.data.Settings
import com.pt2vhf.aprs.data.UpdateManager
import com.pt2vhf.aprs.data.MyObject
import com.pt2vhf.aprs.core.objectInfo
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

/** Serviço em primeiro plano: mantém APRS-IS, TNC Bluetooth, GPS e o tracker ativos com a tela desligada. */
class AprsService : Service(), LocationListener {
    private lateinit var settings: Settings
    private val handler = Handler(Looper.getMainLooper())
    private var smart = SmartBeaconing()
    private fun configuredSmartBeaconing(s: Settings): SmartBeaconing {
        // #49: com a economia ativa, intervalos mínimos maiores (≥ 2 min rápido, ≥ 30 s entre curvas).
        val saver = ::settings.isInitialized && batterySaverActive()
        return SmartBeaconing(
            slowSpeed = s.smartSlowSpeedKmh.toDouble(),
            slowRateSec = s.smartSlowIntervalSec,
            fastSpeed = maxOf(s.smartFastSpeedKmh, s.smartSlowSpeedKmh + 1).toDouble(),
            fastRateSec = if (saver) maxOf(120, s.smartFastIntervalSec) else s.smartFastIntervalSec,
            minTurnAngle = s.smartMinTurnAngle.toDouble(),
            turnSlope = s.smartTurnSlope.toDouble(),
            minTurnTimeSec = if (saver) maxOf(30, s.smartMinTurnTimeSec) else s.smartMinTurnTimeSec,
            stoppedRateSec = s.stoppedBeaconMinutes * 60,
        )
    }
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
            val before = AprsRepository.status.value
            AprsRepository.updateStatus {
                it.copy(aprsIsConnected = connected, aprsIsVerified = verified,
                    aprsIsServer = "${settings.server}:${settings.port}", aprsIsError = error,
                    aprsIsSince = if (verified && !it.aprsIsVerified) System.currentTimeMillis()
                        else if (!connected) 0L else it.aprsIsSince,
                    aprsIsReconnects = if (connected && !it.aprsIsConnected && it.aprsIsEverConnected)
                        it.aprsIsReconnects + 1 else it.aprsIsReconnects,
                    aprsIsEverConnected = it.aprsIsEverConnected || connected)
            }
            if (before.aprsIsConnected != connected || before.aprsIsVerified != verified || (error.isNotBlank() && error != before.aprsIsError)) {
                AprsRepository.logConnection("APRS-IS", when {
                    verified -> "verified ${settings.server}:${settings.port}"
                    connected -> "connected ${settings.server}:${settings.port} (login pending/unverified)"
                    else -> "disconnected" + if (error.isNotBlank()) ": $error" else ""
                })
            }
            if (verified) {
                alertsQuietUntil = System.currentTimeMillis() + 60_000L // no burst after (re)connecting
                updateAprsFilterForKnownPosition()
                handler.postDelayed({ announceVersionIfNeeded() }, 5_000L)
            }
            refreshNotification()
        },
    )
    private val tncFrame: (String) -> Unit = { line ->
        AprsRepository.updateStatus { it.copy(tncRx = it.tncRx + 1) }
        handleLine(line, Medium.RF)
    }
    private val tncState: (Boolean, String, String) -> Unit = { connected, name, error ->
        val before = AprsRepository.status.value
        AprsRepository.updateStatus { it.copy(tncConnected = connected, tncName = name, tncError = error) }
        if (before.tncConnected != connected || (error.isNotBlank() && error != before.tncError))
            AprsRepository.logConnection("TNC", (if (connected) "connected " else "disconnected ") + name +
                if (error.isNotBlank()) ": $error" else "")
        refreshNotification()
    }
    private val tnc = BluetoothTnc(onFrame = tncFrame, onState = tncState)
    private val tcpTnc = KissTcpTnc(onFrame = tncFrame, onState = tncState)
    // #44 modem AFSK pelo áudio e TNC KISS por USB-OTG (herdado da 0.1.0).
    private val audioTnc by lazy {
        AudioTnc(this, onFrame = tncFrame, onState = tncState, onLevel = { level, tx ->
            AprsRepository.updateStatus { it.copy(audioLevel = level, audioTransmitting = tx) }
        })
    }
    private val usbTnc by lazy { UsbSerialTnc(this, onFrame = tncFrame, onState = tncState) }
    private var audioStarted = false
    private var usbStarted = false
    private fun tncConnected(): Boolean = tnc.connected || tcpTnc.connected ||
        (audioStarted && audioTnc.connected) || (usbStarted && usbTnc.connected)
    private fun tncSend(line: String): Boolean = when {
        audioStarted && audioTnc.connected -> audioTnc.send(line)
        usbStarted && usbTnc.connected -> usbTnc.send(line)
        tcpTnc.connected -> tcpTnc.send(line)
        else -> tnc.send(line)
    }
    private fun audioConfig(s: Settings) = AudioConfig(s.audioInput, s.audioOutput, s.audioVolume, s.audioTxDelayMs,
        s.audioVoxLeadMs, s.audioHighQuality)
    private fun disconnectAllTnc() {
        tnc.disconnect()
        tcpTnc.disconnect()
        if (audioStarted) { audioTnc.stop(); audioStarted = false }
        if (usbStarted) { usbTnc.disconnect(); usbStarted = false }
        AprsRepository.updateStatus { it.copy(tncConnected = false, tncName = "", audioLevel = 0.0, audioTransmitting = false) }
    }
    @Volatile private var alertsQuietUntil = System.currentTimeMillis() + 60_000L
    private val alertCooldown = HashMap<String, Long>()

    private fun restoreTrackerPreference() {
        val enabled = settings.configured && settings.trackerAutoStart && !settings.rxOnly &&
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
        running = true
        applyManualPosition()
        connectAprsIs()
        startLocation()
        watchNetwork()
        watchScreen()
        if (!Settings.flag(this, "tnc_manual_pause")) connectTnc()
        handler.post(ticker)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_RELOAD -> {
                val previousTnc = listOf(settings.tncType, settings.tncHost, settings.tncPort, audioConfig(settings), settings.usbBaud)
                val previousLocation = listOf(settings.positionSource, settings.manualLat, settings.manualLon,
                    settings.gpsPrecision, settings.useNetworkLocation, settings.batterySaver, settings.batteryAutoPercent)
                settings = Settings.load(this)
                AprsRepository.retentionPolicy = settings.retention
                // Audio needs the microphone foreground-service type; refresh types while the UI is visible.
                startForegroundCompat()
                if (previousTnc != listOf(settings.tncType, settings.tncHost, settings.tncPort, audioConfig(settings), settings.usbBaud) &&
                    !Settings.flag(this, "tnc_manual_pause")) connectTnc()
                else if (!tncConnected() && !Settings.flag(this, "tnc_manual_pause") &&
                    (settings.tncType == "audio" || settings.tncType == "usb")) connectTnc()
                if (previousLocation != listOf(settings.positionSource, settings.manualLat, settings.manualLon,
                        settings.gpsPrecision, settings.useNetworkLocation, settings.batterySaver, settings.batteryAutoPercent)) {
                    applyManualPosition()
                    restartLocation()
                }
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
            ACTION_WIDGET_TRACKER -> {
                val on = AprsRepository.status.value.trackerOn
                if (on) {
                    Settings.setFlag(this, "tracker_explicit_off", true)
                    AprsRepository.updateStatus { it.copy(trackerOn = false, nextBeaconSec = null) }
                } else if (settings.configured && !settings.rxOnly && (settings.txViaInternet || settings.txViaRf)) {
                    Settings.setFlag(this, "tracker_explicit_off", false)
                    AprsRepository.updateStatus { it.copy(trackerOn = true) }
                    checkTracker()
                }
            }
            ACTION_AUDIO_TEST -> if (audioStarted && !settings.rxOnly && settings.txViaRf)
                txWorker.execute { runCatching { audioTnc.testTone() } }
            ACTION_MSG_CANCEL -> intent.getStringExtra(EXTRA_MESSAGE_ID)?.toLongOrNull()?.let { id ->
                AprsRepository.setRetry(id, 0, null)
                AprsRepository.setStatus(id, "Cancelada")
            }
            ACTION_MSG_RESEND -> intent.getStringExtra(EXTRA_MESSAGE_ID)?.toLongOrNull()?.let { id ->
                val local = AprsRepository.message(id)
                if (local != null && local.outgoing && !settings.rxOnly) {
                    AprsRepository.setStatus(id, "Enviando")
                    txWorker.execute { runCatching { sendWithRetry(local, local.to, local.text, local.msgId, 0) } }
                }
            }
            ACTION_OBJECT_SEND -> intent.getStringExtra(EXTRA_OBJECT_ID)?.toLongOrNull()?.let { id ->
                AprsRepository.myObjects.value.firstOrNull { it.id == id }?.let { obj -> txWorker.execute { sendObject(obj, true) } }
            }
            ACTION_OBJECT_KILL -> intent.getStringExtra(EXTRA_OBJECT_ID)?.toLongOrNull()?.let { id ->
                AprsRepository.myObjects.value.firstOrNull { it.id == id }?.let { obj ->
                    txWorker.execute {
                        sendObject(obj, false)
                        AprsRepository.removeObject(obj.id)
                    }
                }
            }
            ACTION_API_BEACON -> if (settings.apiEnabled && settings.apiAllowBeacon) queueBeacon()
            ACTION_API_STATUS -> if (settings.apiEnabled) broadcastStatus()
            ACTION_TCP_CONNECT -> {
                Settings.setFlag(this, "tnc_manual_pause", false)
                connectTnc()
            }
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
                val replyTo = intent.getStringExtra(EXTRA_REPLY_TO)?.toLongOrNull()
                // A stopped worker must not leave the composer waiting without a result.
                runCatching {
                    txWorker.execute {
                        val submitted = runCatching { sendMessage(to, message, replyTo) }.getOrDefault(false)
                        if (!requestId.isNullOrBlank()) {
                            AprsRepository.reportSendResult(OutboundSendResult(requestId, submitted,
                                if (submitted) "" else sendFailureReason()))
                        }
                    }
                }.onFailure {
                    if (!requestId.isNullOrBlank())
                        AprsRepository.reportSendResult(OutboundSendResult(requestId, false, sendFailureReason()))
                }
            }
            ACTION_BT_CONNECT -> intent.getStringExtra(EXTRA_ADDRESS)?.let {
                Settings.setFlag(this, "tnc_manual_pause", false)
                settings = settings.copy(btAddress = it); Settings.save(this, settings); connectBluetooth(it)
            }
            ACTION_BT_PAUSE -> {
                Settings.setFlag(this, "tnc_manual_pause", true)
                disconnectAllTnc()
            }
            ACTION_BT_RESUME -> {
                Settings.setFlag(this, "tnc_manual_pause", false)
                connectTnc()
            }
            ACTION_BT_DISCONNECT -> {
                if (settings.tncType == "bt") { settings = settings.copy(btAddress = ""); Settings.save(this, settings) }
                else Settings.setFlag(this, "tnc_manual_pause", true)
                disconnectAllTnc()
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
        tcpTnc.disconnect()
        if (audioStarted) audioTnc.stop()
        if (usbStarted) usbTnc.release()
        screenReceiver?.let { runCatching { unregisterReceiver(it) } }
        running = false
        runCatching { (getSystemService(LOCATION_SERVICE) as LocationManager).removeUpdates(this) }
        networkCallback?.let { runCatching { getSystemService(ConnectivityManager::class.java).unregisterNetworkCallback(it) } }
        AprsRepository.updateStatus { it.copy(aprsIsConnected = false, aprsIsVerified = false, tncConnected = false, trackerOn = false) }
        runCatching { StatusWidget.refresh(this) }
        super.onDestroy()
    }

    /**
     * Called after login verification and GPS first fix. Keep filter updates out of
     * the AprsIsClient property initializer to avoid circular type inference.
     */
    private fun updateAprsFilterForKnownPosition() {
        if (!::settings.isInitialized || !settings.filter.contains("r/")) return
        val gps = AprsRepository.status.value
        val lat = gps.lat ?: return
        val lon = gps.lon ?: return
        val filter = effectiveAprsFilter(settings.filter, lat, lon, settings.fullCall)
        if (!txWorker.isShutdown) txWorker.execute { aprsIs.updateFilter(filter) }
    }

    /**
     * #43: um boletim por versão, só pelo APRS-IS verificado (nunca RF), no idioma atual, com até 67 caracteres.
     * Só marca como anunciado depois da escrita bem-sucedida no socket.
     */
    private fun announceVersionIfNeeded() {
        if (!::settings.isInitialized || !settings.announceVersion || !settings.configured || settings.rxOnly ||
            !settings.txViaInternet) return
        val version = com.pt2vhf.aprs.BuildConfig.VERSION_NAME
        if (Settings.readOption(this, "announced_version", "") == version) return
        if (txWorker.isShutdown) return
        txWorker.execute {
            if (!AprsRepository.status.value.aprsIsVerified) return@execute
            if (Settings.readOption(this, "announced_version", "") == version) return@execute
            val text = UiText.text(R.string.announce_bulletin, version).take(67)
            val info = messageInfo(ANNOUNCE_DEST, text, null)
            val line = "${settings.fullCall}>$TOCALL,TCPIP*:$info"
            if (aprsIs.send(line)) {
                Settings.saveOption(this, "announced_version", version)
                AprsRepository.updateStatus { it.copy(aprsTx = it.aprsTx + 1) }
                AprsRepository.logPacket("TX", Medium.INTERNET, line)
                AprsRepository.monitorLine("TX APRS-IS $line")
                AprsRepository.add(ChatMessage(AprsRepository.newId(), settings.fullCall, ANNOUNCE_DEST, text,
                    System.currentTimeMillis(), true, Medium.INTERNET, "APRS-IS", null, "Enviada · APRS-IS",
                    AprsMessage.Kind.BULLETIN))
            }
        }
    }

    private fun connectAprsIs(force: Boolean = false) {
        if (!settings.configured || (!force && (!settings.connectAprsIs || Settings.flag(this, "aprs_manual_pause")))) return
        val s = AprsRepository.status.value
        aprsIs.start(settings.server, settings.port, settings.fullCall, settings.passcode,
            effectiveAprsFilter(settings.filter, s.lat, s.lon, settings.fullCall))
    }

    /** Connects the configured TNC (Bluetooth KISS or KISS TCP); does nothing if none is set. */
    private fun connectTnc() {
        disconnectAllTnc()
        when (settings.tncType) {
            "tcp" -> if (settings.tncHost.isNotBlank()) tcpTnc.connect(settings.tncHost.trim(), settings.tncPort)
            "audio" -> { audioStarted = true; audioTnc.start(audioConfig(settings)) }
            "usb" -> { usbStarted = true; usbTnc.connect(settings.usbBaud) }
            else -> if (settings.btAddress.isNotEmpty()) connectBluetooth(settings.btAddress)
        }
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

    /**
     * #6 precisão do GPS e #49 economia de bateria. Posição fixa (manual) não liga o GPS.
     * Alta: GPS a cada 2 s; equilibrada: GPS 10 s/20 m; baixa: só rede (ou GPS a cada 60 s sem rede).
     * Com a economia ativa (tela desligada), o GPS passa a 30 s / 50 m.
     */
    @SuppressLint("MissingPermission")
    private fun startLocation() {
        if (settings.manualPosition) return
        if (!granted(Manifest.permission.ACCESS_FINE_LOCATION) && !granted(Manifest.permission.ACCESS_COARSE_LOCATION)) return
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val saver = batterySaverActive()
        val (gpsMs, gpsMeters) = when {
            saver -> 30_000L to 50f
            settings.gpsPrecision == "balanced" -> 10_000L to 20f
            settings.gpsPrecision == "low" -> 60_000L to 100f
            else -> 2_000L to 0f
        }
        val useGps = granted(Manifest.permission.ACCESS_FINE_LOCATION) &&
            (settings.gpsPrecision != "low" || !settings.useNetworkLocation)
        if (useGps) runCatching { lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, gpsMs, gpsMeters, this, Looper.getMainLooper()) }
        if (settings.useNetworkLocation)
            runCatching { lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, maxOf(10_000L, gpsMs), 0f, this, Looper.getMainLooper()) }
        AprsRepository.updateStatus { it.copy(batterySaverActive = saver) }
    }

    private fun restartLocation() {
        runCatching { (getSystemService(LOCATION_SERVICE) as LocationManager).removeUpdates(this) }
        lastLocation = null
        startLocation()
    }

    /** #6: posição fixa digitada ou escolhida no mapa entra no lugar do GPS. */
    private fun applyManualPosition() {
        if (settings.manualPosition) {
            AprsRepository.updateStatus {
                it.copy(lat = settings.manualLat, lon = settings.manualLon, speedKmh = 0.0, course = null,
                    gpsAccuracyM = null, manualPosition = true)
            }
        } else AprsRepository.updateStatus { it.copy(manualPosition = false) }
    }

    // ---------- #49 economia de bateria ----------

    private var screenReceiver: BroadcastReceiver? = null
    @Volatile private var screenOn = true
    private var lastBatterySample = 0L

    private fun batteryPercent(): Int? = runCatching {
        getSystemService(BatteryManager::class.java)?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }.getOrNull()?.takeIf { it in 0..100 }

    private fun batterySaverActive(): Boolean {
        val wanted = when (settings.batterySaver) {
            "on" -> true
            "auto" -> (batteryPercent() ?: 100) <= settings.batteryAutoPercent
            else -> false
        }
        return wanted && !screenOn
    }

    private fun watchScreen() {
        screenOn = getSystemService(PowerManager::class.java)?.isInteractive != false
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val now = intent.action == Intent.ACTION_SCREEN_ON
                if (now == screenOn) return
                screenOn = now
                if (settings.batterySaver != "off") {
                    restartLocation()
                    smart = configuredSmartBeaconing(settings)
                }
            }
        }
        runCatching {
            ContextCompat.registerReceiver(this, receiver,
                IntentFilter(Intent.ACTION_SCREEN_ON).apply { addAction(Intent.ACTION_SCREEN_OFF) }, ContextCompat.RECEIVER_NOT_EXPORTED)
            screenReceiver = receiver
        }
    }

    override fun onLocationChanged(location: Location) {
        if (settings.manualPosition) return
        val previous = lastLocation
        if (previous != null && previous.provider == LocationManager.GPS_PROVIDER &&
            location.provider != LocationManager.GPS_PROVIDER && location.time - previous.time < 30_000) return
        val firstFix = previous == null
        lastLocation = location
        val speed = if (location.hasSpeed()) location.speed * 3.6 else 0.0
        val hadFix = AprsRepository.status.value.gpsAccuracyM != null
        AprsRepository.updateStatus {
            it.copy(lat = location.latitude, lon = location.longitude, gpsAccuracyM = location.accuracy,
                speedKmh = speed, gpsFixTimeMs = System.currentTimeMillis(),
                course = if (location.hasBearing()) location.bearing.toInt() else it.course,
                altitudeM = if (location.hasAltitude()) location.altitude else it.altitudeM)
        }
        if (!hadFix) AprsRepository.logConnection("GPS", "fix ${location.provider} ±${location.accuracy.toInt()} m")
        if (AprsRepository.status.value.trackerOn &&
            Settings.readOption(this, "tracklog_enabled", "true") == "true") {
            AprsRepository.recordTrack(location.latitude, location.longitude)
        }
        if (firstFix && settings.filter.contains("r/")) {
            val expanded = effectiveAprsFilter(settings.filter, location.latitude, location.longitude, settings.fullCall)
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
        val interval = (if (batterySaverActive()) maxOf(300, settings.periodicIntervalSec) else settings.periodicIntervalSec).toLong()
        // A fixed position has no speed/course: SmartBeaconing falls back to the periodic interval.
        val periodic = settings.beaconMode == "periodic" || settings.manualPosition
        val due = if (periodic) since >= interval
            else smart.shouldBeacon(since, s.speedKmh, abs(change))
        if (due) queueBeacon()
        AprsRepository.updateStatus {
            it.copy(nextBeaconSec = if (periodic)
                (interval - (System.currentTimeMillis() - lastBeaconAt) / 1000).coerceAtLeast(0)
                else smart.nextBeaconSeconds((System.currentTimeMillis() - lastBeaconAt) / 1000, it.speedKmh))
        }
    }

    private var lastHourly = System.currentTimeMillis()
    private var lastWidgetMinute = 0L

    /** Tarefas periódicas da 0.1.9: retenção de hora em hora, aviso de versão, bateria, objetos e widget. */
    private fun periodicJobs() {
        val now = System.currentTimeMillis()
        if (now - lastHourly >= 3_600_000L) {
            lastHourly = now
            runCatching { AprsRepository.applyRetention(settings.retention) } // #42: "24 h" precisa rodar ao menos de hora em hora
            runCatching { AprsRepository.applyLogRetention() }
        }
        runCatching { UpdateManager.maybeCheck(this, UpdateManager.PERIODIC_MS, fromBackground = true) } // #39
        if (now - lastBatterySample >= 5 * 60_000L) {
            lastBatterySample = now
            batteryPercent()?.let { pct ->
                AprsRepository.addBatterySample(pct, now)
                val saver = batterySaverActive()
                val before = AprsRepository.status.value.batterySaverActive
                AprsRepository.updateStatus { it.copy(batteryPercent = pct, batterySaverActive = saver) }
                if (saver != before && settings.batterySaver == "auto") { restartLocation(); smart = configuredSmartBeaconing(settings) }
            }
        }
        // #48: repetição periódica dos meus objetos ativos.
        if (!settings.rxOnly) for (obj in AprsRepository.myObjects.value) {
            if (!obj.active) continue
            val due = if (obj.intervalMin <= 0) obj.lastSent == 0L else now - obj.lastSent >= obj.intervalMin * 60_000L
            if (due) {
                AprsRepository.markObjectSent(obj.id, now) // avoid queueing twice before the TX finishes
                txWorker.execute { sendObject(obj, true) }
            }
        }
        if (now / 60_000L != lastWidgetMinute) { lastWidgetMinute = now / 60_000L; runCatching { StatusWidget.refresh(this) } }
    }

    /** #48: objeto/item pelo(s) meio(s) autorizado(s); [alive] false envia o encerramento ("killed"). */
    private fun sendObject(obj: MyObject, alive: Boolean): Boolean {
        if (!settings.configured || settings.rxOnly) return false
        val info = objectInfo(obj.name, alive, obj.lat, obj.lon, obj.symbolTable, obj.symbol, obj.comment, obj.item)
        val ok = transmit(info) != null
        if (ok && alive) AprsRepository.markObjectSent(obj.id, System.currentTimeMillis())
        return ok
    }

    private val ticker = object : Runnable {
        override fun run() {
            runCatching { periodicJobs() }
            AprsRepository.updateStatus { state ->
                if (!state.manualPosition && state.gpsAccuracyM != null && state.gpsFixTimeMs > 0 &&
                    System.currentTimeMillis() - state.gpsFixTimeMs > 120_000L) {
                    AprsRepository.logConnection("GPS", "fix lost (> 2 min without location)")
                    state.copy(gpsAccuracyM = null)
                } else state
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
        if (!settings.configured || settings.rxOnly) return false
        val s = AprsRepository.status.value
        val lat = s.lat ?: return false
        val lon = s.lon ?: return false
        val moving = settings.sendCourseSpeed && !s.manualPosition && s.speedKmh >= 5.0
        val comment = composeBeaconComment(settings.comment, formatVoiceFrequency(settings.voiceFrequency),
            if (settings.sendAltitude) formatAltitude(s.altitudeM) else null)
        val info = positionInfo(lat, lon, settings.symbolTable, settings.symbol, comment,
            if (moving) s.course else null, if (moving) s.speedKmh else null, ambiguity = settings.ambiguity)
        if (transmit(info) == null) return false
        lastBeaconAt = System.currentTimeMillis()
        lastBeaconCourse = s.course
        AprsRepository.updateStatus { it.copy(beaconsSent = it.beaconsSent + 1, lastBeaconAt = lastBeaconAt) }
        runCatching { StatusWidget.refresh(this) }
        return true
    }

    /** Envia pelo(s) meio(s) ativo(s) e autorizado(s): RF (TNC) e/ou APRS-IS. Retorna o meio usado. */
    private fun transmit(info: String): Medium? {
        val call = settings.fullCall
        if (settings.rxOnly) {
            AprsRepository.monitorLine("TX ${UiText.text(R.string.not_sent)} (RX only) $call>$TOCALL:$info")
            return null
        }
        var used: Medium? = null
        if (settings.txViaRf && tncConnected()) {
            val path = settings.rfPath.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            val line = "$call>$TOCALL" + (if (path.isNotEmpty()) "," + path.joinToString(",") else "") + ":" + info
            if (tncSend(line)) {
                used = Medium.RF
                AprsRepository.updateStatus { it.copy(tncTx = it.tncTx + 1) }
                AprsRepository.logPacket("TX", Medium.RF, line)
            }
        }
        val status = AprsRepository.status.value
        if (settings.txViaInternet && status.aprsIsVerified) {
            val line = "$call>$TOCALL,TCPIP*:$info"
            if (aprsIs.send(line)) {
                used = used ?: Medium.INTERNET
                AprsRepository.updateStatus { it.copy(aprsTx = it.aprsTx + 1) }
                AprsRepository.logPacket("TX", Medium.INTERNET, line)
            }
        }
        AprsRepository.monitorLine("TX ${used?.label ?: UiText.text(R.string.not_sent)} $call>$TOCALL:$info")
        return used
    }

    private fun sendMessage(to: String, text: String, replyTo: Long? = null): Boolean {
        val dest = to.trim().uppercase()
        if (dest.isEmpty() || text.isBlank() || !settings.configured || settings.rxOnly) return false
        // Never message our own callsign by mistake (#20).
        if (dest == settings.fullCall) return false
        val general = dest == "CQ" || dest.startsWith("BLN")
        var allSubmitted = true
        for (part in splitMessage(text)) {
            val msgId = if (general) null else nextMsgId()
            val local = ChatMessage(AprsRepository.newId(), settings.fullCall, dest, part, System.currentTimeMillis(),
                true, null, msgId = msgId, status = "Enviando",
                kind = if (dest.startsWith("BLN")) AprsMessage.Kind.BULLETIN else AprsMessage.Kind.MESSAGE,
                replyToId = replyTo)
            AprsRepository.add(local)
            if (!sendWithRetry(local, dest, part, msgId, 0)) allSubmitted = false
        }
        return allSubmitted
    }

    /** true means written to at least one authorized transport, not remotely delivered. */
    /** #47: tentativas e intervalo base configuráveis (padrão 5 / 30 s × n); cancelar interrompe os reenvios. */
    private fun sendWithRetry(local: ChatMessage, dest: String, text: String, msgId: String?, attempt: Int): Boolean {
        if (!AprsRepository.containsMessage(local.id) || AprsRepository.isAcked(local.id)) {
            AprsRepository.setRetry(local.id, 0, null)
            return true
        }
        val retries = settings.msgRetries.coerceIn(1, 10)
        val medium = transmit(messageInfo(dest, text, msgId))
        when {
            medium == null -> { AprsRepository.setRetry(local.id, 0, null); AprsRepository.setStatus(local.id, "Sem conexão para enviar") }
            msgId == null -> AprsRepository.setStatus(local.id, "Enviada · ${medium.label}")
            attempt >= retries - 1 -> {
                AprsRepository.setRetry(local.id, 0, null)
                // Wait one more interval for a late ACK before declaring failure.
                val delay = settings.msgRetryBaseSec * 1000L * (attempt + 1)
                AprsRepository.setStatus(local.id, "Aguardando ACK (${attempt + 1}ª tentativa)")
                handler.postDelayed({
                    if (AprsRepository.containsMessage(local.id) && !AprsRepository.isAcked(local.id))
                        AprsRepository.setStatus(local.id, "Sem ACK após $retries tentativas")
                }, delay)
            }
            else -> {
                AprsRepository.setStatus(local.id, if (attempt == 0) "Aguardando ACK · ${medium.label}"
                    else "Aguardando ACK (${attempt + 1}ª tentativa)")
                val delay = settings.msgRetryBaseSec * 1000L * (attempt + 1)
                AprsRepository.setRetry(local.id, attempt + 1, System.currentTimeMillis() + delay)
                handler.postDelayed({
                    if (!txWorker.isShutdown) txWorker.execute {
                        sendWithRetry(local, dest, text, msgId, attempt + 1)
                    }
                }, delay)
            }
        }
        return medium != null
    }

    private fun sendFailureReason(): String {
        val s = AprsRepository.status.value
        return when {
            !settings.configured -> UiText.text(R.string.configure_callsign)
            settings.rxOnly -> UiText.text(R.string.rx_only_blocked)
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
        AprsRepository.logPacket("RX", medium, line)
        val packet = AprsPacket.parse(line) ?: return
        if (medium == Medium.INTERNET)
            AprsRepository.updateStatus { it.copy(aprsRx = it.aprsRx + 1) }
        if (settings.monitorEnabled) AprsRepository.monitorLine("RX ${medium.label} ${decodeAprsText(line.take(300))}")
        val previous = AprsRepository.stations.value[packet.source]
        val eventsBefore = AprsRepository.rfEvents.value.size
        val forUs = AprsRepository.ingest(packet, medium, settings.fullCall)
        runCatching { checkAlerts(packet, medium, previous, eventsBefore) }
        if (settings.apiEnabled) runCatching { broadcastPacket(packet, medium) }
        if (forUs == null) return
        // Send ACK through an authorized transport on the IO worker, not the APRS-IS reader.
        forUs.msgId?.takeIf { !settings.rxOnly }?.let { id ->
            val info = ackInfo(forUs.from, id)
            if (!txWorker.isShutdown) txWorker.execute {
                if (medium == Medium.RF && settings.txViaRf && tncConnected()) {
                    val path = settings.rfPath.split(',').map { it.trim() }.filter { it.isNotEmpty() }
                    val ack = "${settings.fullCall}>$TOCALL" +
                        (if (path.isEmpty()) "" else "," + path.joinToString(",")) + ":$info"
                    if (tncSend(ack)) AprsRepository.logPacket("TX", Medium.RF, ack)
                } else if (settings.txViaInternet && AprsRepository.status.value.aprsIsVerified) {
                    val ack = "${settings.fullCall}>$TOCALL,TCPIP*:$info"
                    if (aprsIs.send(ack)) AprsRepository.logPacket("TX", Medium.INTERNET, ack)
                }
            }
        }
        if (settings.notifyMessages) notifyMessage(forUs)
    }

    // ---------- Alertas de favoritas (#12) ----------

    private fun inQuietHours(): Boolean {
        if (!settings.quietHours) return false
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val start = settings.quietStart
        val end = settings.quietEnd
        return if (start == end) false else if (start < end) hour in start until end else hour >= start || hour < end
    }

    private fun checkAlerts(packet: AprsPacket, medium: Medium, previous: Station?, eventsBefore: Int) {
        val now = System.currentTimeMillis()
        if (now < alertsQuietUntil || inQuietHours()) return
        val call = packet.source
        val events = AprsRepository.rfEvents.value
        val newEvent = if (events.size > eventsBefore) events.lastOrNull() else null
        if (settings.alertMyDigi && newEvent?.kind == "digi") {
            alert("digi:${newEvent.call}", UiText.text(R.string.alert_my_digi_title),
                UiText.text(R.string.alert_my_digi_body, newEvent.call))
        }
        if (call !in AprsRepository.favorites.value) return
        val current = AprsRepository.stations.value[call]
        when {
            previous == null && current != null && settings.alertFavFirst ->
                alert("first:$call", UiText.text(R.string.alert_fav_title, call), UiText.text(R.string.alert_fav_first))
            previous != null && settings.alertFavReturn &&
                now - previous.lastHeard >= settings.alertReturnMinutes * 60_000L ->
                alert("return:$call", UiText.text(R.string.alert_fav_title, call),
                    UiText.text(R.string.alert_fav_return, (now - previous.lastHeard) / 60_000L))
            previous != null && current != null && settings.alertFavPosition &&
                current.lastHeard > previous.lastHeard &&
                com.pt2vhf.aprs.core.haversineKm(previous.lat, previous.lon, current.lat, current.lon) >= 0.1 ->
                alert("pos:$call", UiText.text(R.string.alert_fav_title, call), UiText.text(R.string.alert_fav_position))
        }
        if (settings.alertFavRf && medium == Medium.RF && newEvent?.kind == "heard" && newEvent.direct && newEvent.call == call) {
            alert("rf:$call", UiText.text(R.string.alert_fav_title, call), UiText.text(R.string.alert_fav_rf))
        }
    }

    private fun alert(key: String, title: String, body: String) {
        val now = System.currentTimeMillis()
        synchronized(alertCooldown) {
            if (now - (alertCooldown[key] ?: 0L) < 10 * 60_000L) return
            alertCooldown[key] = now
        }
        if (!canNotify()) return
        val n = NotificationCompat.Builder(this, CH_ALERTS)
            .setSmallIcon(R.drawable.ic_stat_aprs)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .build()
        runCatching { getSystemService(NotificationManager::class.java).notify(key.hashCode(), n) }
    }

    // ---------- API para outros apps (#15) ----------

    private fun broadcastPacket(packet: AprsPacket, medium: Medium) {
        val station = AprsRepository.stations.value[packet.source] ?: return
        if (station.lastHeard < System.currentTimeMillis() - 5_000L) return // only positions from this packet
        val intent = Intent(ApiContract.EVENT_POSITION).apply {
            putExtra(ApiContract.EXTRA_VERSION, ApiContract.VERSION)
            putExtra(ApiContract.EXTRA_CALL, station.call)
            putExtra(ApiContract.EXTRA_LAT, station.lat)
            putExtra(ApiContract.EXTRA_LON, station.lon)
            putExtra(ApiContract.EXTRA_SYMBOL, "${station.symbolTable}${station.symbol}")
            putExtra(ApiContract.EXTRA_COMMENT, station.comment)
            putExtra(ApiContract.EXTRA_MEDIUM, medium.name)
            putExtra(ApiContract.EXTRA_TIME, station.lastHeard)
        }
        sendBroadcast(intent, ApiContract.PERMISSION)
    }

    private fun broadcastStatus() {
        val s = AprsRepository.status.value
        val intent = Intent(ApiContract.EVENT_STATUS).apply {
            putExtra(ApiContract.EXTRA_VERSION, ApiContract.VERSION)
            putExtra(ApiContract.EXTRA_CALL, settings.fullCall)
            putExtra(ApiContract.EXTRA_APRS_IS, s.aprsIsVerified)
            putExtra(ApiContract.EXTRA_TNC, s.tncConnected)
            putExtra(ApiContract.EXTRA_TRACKER, s.trackerOn)
            putExtra(ApiContract.EXTRA_STATIONS, AprsRepository.stations.value.size)
            putExtra(ApiContract.EXTRA_RX_ONLY, settings.rxOnly)
        }
        sendBroadcast(intent, ApiContract.PERMISSION)
    }

    private fun createChannels() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CH_SERVICE, UiText.text(R.string.connection_channel), NotificationManager.IMPORTANCE_LOW))
        nm.createNotificationChannel(NotificationChannel(CH_MESSAGES, UiText.text(R.string.messages_channel), NotificationManager.IMPORTANCE_HIGH))
        nm.createNotificationChannel(NotificationChannel(CH_ALERTS, UiText.text(R.string.alerts_channel), NotificationManager.IMPORTANCE_DEFAULT))
    }

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(this, 0,
        Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

    private fun buildNotification(): Notification {
        val s = AprsRepository.status.value
        val parts = mutableListOf<String>()
        parts += if (s.aprsIsVerified) UiText.text(R.string.aprs_ok) else if (s.aprsIsConnected) UiText.text(R.string.aprs_unverified) else UiText.text(R.string.aprs_disconnected)
        if (s.tncConnected) parts += "TNC ${s.tncName}"
        if (settings.rxOnly) parts += UiText.text(R.string.profile_rx)
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
        runCatching { StatusWidget.refresh(this) }
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
            if (Build.VERSION.SDK_INT >= 30 && ::settings.isInitialized && settings.tncType == "audio" &&
                granted(Manifest.permission.RECORD_AUDIO))
                types = types or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        }
        // Android 14 only allows the microphone type while the app is visible; fall back without it.
        val ok = runCatching { ServiceCompat.startForeground(this, NOTIF_SERVICE, buildNotification(), types) }.isSuccess
        if (!ok) runCatching {
            ServiceCompat.startForeground(this, NOTIF_SERVICE, buildNotification(),
                types and ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE.inv())
        }
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
        private const val CH_ALERTS = "alerts"
        /** true while the foreground service is alive; the API receiver only forwards then. */
        @Volatile var running = false
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
        const val ACTION_TCP_CONNECT = "tcp_connect"
        const val ACTION_API_BEACON = "api_beacon"
        const val ACTION_API_STATUS = "api_status"
        const val ACTION_WIDGET_TRACKER = "widget_tracker"
        const val ACTION_AUDIO_TEST = "audio_test"
        const val ACTION_MSG_RESEND = "msg_resend"
        const val ACTION_MSG_CANCEL = "msg_cancel"
        const val ACTION_OBJECT_SEND = "object_send"
        const val ACTION_OBJECT_KILL = "object_kill"
        const val EXTRA_MESSAGE_ID = "message_id"
        const val EXTRA_OBJECT_ID = "object_id"
        /** #43: destino do boletim de versão (boletim geral 0). */
        const val ANNOUNCE_DEST = "BLN0"
        const val EXTRA_REPLY_TO = "reply_to"
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
