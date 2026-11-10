package com.pt2vhf.aprs.ui

import android.annotation.SuppressLint
import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt2vhf.aprs.BuildConfig
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.LinkStatus
import com.pt2vhf.aprs.data.Settings
import com.pt2vhf.aprs.data.ownLinkSummaries
import com.pt2vhf.aprs.data.rangeRecords
import com.pt2vhf.aprs.service.AprsService
import java.text.DateFormat
import java.util.Date

// ---------- Rádio / TNC ----------

private fun periodStart(period: String, now: Long = System.currentTimeMillis()): Long = when (period) {
    "24h" -> now - 86_400_000L
    "7d" -> now - 7 * 86_400_000L
    "30d" -> now - 30 * 86_400_000L
    else -> 0L
}

@SuppressLint("MissingPermission")
@Composable
fun RadioScreen(settings: Settings, onSettingsChanged: (Settings) -> Unit) {
    val context = LocalContext.current
    val status by AprsRepository.status.collectAsState()
    val monitor by AprsRepository.monitor.collectAsState()
    val stations by AprsRepository.stations.collectAsState()
    val links by AprsRepository.links.collectAsState()
    val rfEvents by AprsRepository.rfEvents.collectAsState()
    val connectionLog by AprsRepository.connectionLog.collectAsState()
    val bonded = remember {
        runCatching { context.getSystemService(BluetoothManager::class.java)?.adapter?.bondedDevices?.toList() }
            .getOrNull().orEmpty()
    }
    var tcpHost by rememberSaveable { mutableStateOf(settings.tncHost) }
    var tcpPort by rememberSaveable { mutableStateOf(settings.tncPort.toString()) }
    var period by rememberSaveable { mutableStateOf("all") }
    var monitorFilter by rememberSaveable { mutableStateOf("") }
    var monitorMedium by rememberSaveable { mutableStateOf("all") }
    var anonymize by rememberSaveable { mutableStateOf(true) }
    var exportResult by remember { mutableStateOf<String?>(null) }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) {
            exportResult = runCatching {
                context.contentResolver.openOutputStream(uri)?.use {
                    it.write(buildDiagnostics(settings, AprsRepository.status.value, anonymize).toByteArray())
                }
                UiText.text(R.string.diag_export_done)
            }.getOrElse { UiText.text(R.string.diag_export_failed) }
        }
    }
    var micGranted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) }
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        micGranted = granted
        if (granted && settings.tncType == "audio") AprsService.send(context, AprsService.ACTION_RELOAD)
    }
    fun change(updated: Settings) {
        Settings.save(context, updated)
        onSettingsChanged(updated)
        AprsService.send(context, AprsService.ACTION_RELOAD)
    }
    fun saveTnc(type: String, host: String = settings.tncHost, port: Int = settings.tncPort) {
        val updated = settings.copy(tncType = type, tncHost = host.trim(), tncPort = port)
        Settings.save(context, updated)
        onSettingsChanged(updated)
        AprsService.send(context, AprsService.ACTION_RELOAD)
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionCard(if (status.tncConnected) UiText.text(R.string.tnc_connected) else "TNC") {
            val kind = when (settings.tncType) {
                "tcp" -> "KISS TCP"
                "audio" -> UiText.text(R.string.tnc_audio)
                "usb" -> UiText.text(R.string.tnc_usb)
                else -> "KISS Bluetooth"
            }
            Text(if (status.tncConnected) "${status.tncName} · $kind · ${status.tncRx} RX · ${status.tncTx} TX"
                else UiText.connectionError(status.tncError).ifBlank { UiText.text(R.string.pair_tnc) },
                color = if (status.tncConnected) Pal.green else Pal.muted, fontSize = 14.sp)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedChoice("Bluetooth", settings.tncType == "bt") { if (settings.tncType != "bt") saveTnc("bt") }
                OutlinedChoice("KISS TCP", settings.tncType == "tcp") { if (settings.tncType != "tcp") saveTnc("tcp") }
                OutlinedChoice(UiText.text(R.string.tnc_audio), settings.tncType == "audio") {
                    if (settings.tncType != "audio") { saveTnc("audio"); if (!micGranted) micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                }
                OutlinedChoice(UiText.text(R.string.tnc_usb), settings.tncType == "usb") { if (settings.tncType != "usb") saveTnc("usb") }
            }
            if (status.tncConnected) OutlinedButton(onClick = { AprsService.send(context, AprsService.ACTION_BT_DISCONNECT) }) {
                Text(UiText.text(R.string.disconnect))
            }
        }
        if (settings.tncType == "audio") AudioTncCard(settings, ::change, onRequestMic = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }, micGranted)
        else if (settings.tncType == "usb") UsbTncCard(settings, ::change)
        else if (settings.tncType == "tcp") SectionCard("KISS TCP") {
            Text(UiText.text(R.string.kiss_tcp_hint), color = Pal.muted, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(tcpHost, { tcpHost = it.trim() }, singleLine = true, modifier = Modifier.weight(2f),
                    label = { Text(UiText.text(R.string.server)) })
                OutlinedTextField(tcpPort, { tcpPort = it.filter(Char::isDigit).take(5) }, singleLine = true,
                    modifier = Modifier.weight(1f), label = { Text(UiText.text(R.string.port)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            val portN = tcpPort.toIntOrNull()
            TextButton(enabled = tcpHost.isNotBlank() && portN != null && portN in 1..65535, onClick = {
                saveTnc("tcp", tcpHost, portN ?: 8001)
                AprsService.send(context, AprsService.ACTION_TCP_CONNECT)
            }) { Text(UiText.text(R.string.connect)) }
        } else SectionCard(UiText.text(R.string.paired_devices)) {
            if (bonded.isEmpty()) Text(UiText.text(R.string.no_paired), color = Pal.muted, fontSize = 14.sp)
            for (d in bonded) {
                val name = runCatching { d.name }.getOrNull() ?: d.address
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(name, modifier = Modifier.weight(1f))
                    if (status.tncConnected && settings.btAddress == d.address) Text(UiText.text(R.string.in_use), color = Pal.green)
                    else TextButton(onClick = {
                        AprsService.send(context, AprsService.ACTION_BT_CONNECT, mapOf(AprsService.EXTRA_ADDRESS to d.address))
                    }) { Text(UiText.text(R.string.connect)) }
                }
            }
            Text(UiText.text(R.string.future_tnc), color = Pal.muted, fontSize = 12.sp)
        }

        // #4: meus enlaces RF (somente evidência do TNC deste aparelho)
        SectionCard(UiText.text(R.string.my_rf_links)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("24h" to R.string.period_24h, "7d" to R.string.period_7d, "30d" to R.string.period_30d,
                    "all" to R.string.filter_all).forEach { (key, label) ->
                    OutlinedChoice(UiText.text(label), period == key) { period = key }
                }
            }
            val summaries = remember(rfEvents, period) { ownLinkSummaries(rfEvents, periodStart(period)) }
            if (summaries.isEmpty()) Text(UiText.text(R.string.my_rf_links_empty), color = Pal.muted, fontSize = 13.sp)
            summaries.take(30).forEach { link ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(link.call, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(when {
                            link.kind == "digi" -> UiText.text(R.string.my_packet_digipeated)
                            link.direct -> UiText.text(R.string.rf_direct)
                            else -> UiText.text(R.string.via, link.via)
                        } + " · ${link.count}× · ${ago(link.lastSeen)}", color = Pal.muted, fontSize = 12.sp)
                    }
                    Text(link.maxDistanceKm?.let { "%.1f km".format(it) } ?: "—", fontSize = 13.sp,
                        textAlign = TextAlign.End, modifier = Modifier.width(72.dp))
                }
            }
        }

        // #14: alcance e recordes RF comprovados pelo TNC
        SectionCard(UiText.text(R.string.rf_records)) {
            Text(UiText.text(R.string.rf_records_notice), color = Pal.muted, fontSize = 12.sp)
            listOf(true to R.string.rf_records_direct, false to R.string.rf_records_digi).forEach { (direct, title) ->
                val records = remember(rfEvents, period, direct) { rangeRecords(rfEvents, periodStart(period), direct) }
                Text(UiText.text(title), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                if (records.isEmpty()) Text("—", color = Pal.muted)
                records.forEachIndexed { i, r ->
                    Row(Modifier.fillMaxWidth()) {
                        Text("${i + 1}. ${r.call}" + if (!r.direct) " · ${r.via}" else "",
                            modifier = Modifier.weight(1f), fontSize = 13.sp)
                        Text("%.1f km · %s".format(r.distanceKm, ago(r.time)), fontSize = 12.sp, color = Pal.muted)
                    }
                }
            }
            TextButton(onClick = { AprsRepository.clearRfEvents() }) { Text(UiText.text(R.string.rf_records_clear), color = Pal.orange) }
        }

        // #13: centro de diagnóstico
        SectionCard(UiText.text(R.string.diagnostics)) {
            KeyValue("APRS-IS", if (status.aprsIsVerified) UiText.text(R.string.aprs_ok)
                else if (status.aprsIsConnected) UiText.text(R.string.aprs_unverified) else UiText.text(R.string.aprs_disconnected))
            if (status.aprsIsServer.isNotBlank()) KeyValue(UiText.text(R.string.server), status.aprsIsServer)
            if (status.aprsIsSince > 0) KeyValue(UiText.text(R.string.diag_connected_since), hhmm(status.aprsIsSince))
            KeyValue(UiText.text(R.string.diag_reconnects), status.aprsIsReconnects.toString())
            KeyValue(UiText.text(R.string.aprs_received), status.aprsRx.toString())
            KeyValue(UiText.text(R.string.aprs_sent), status.aprsTx.toString())
            KeyValue(UiText.text(R.string.tnc_received), status.tncRx.toString())
            KeyValue(UiText.text(R.string.tnc_sent), status.tncTx.toString())
            KeyValue(UiText.text(R.string.diag_beacons), "${status.beaconsSent} / ${status.beaconsDigipeated}")
            KeyValue(UiText.text(R.string.diag_pending_ack), AprsRepository.messages.value
                .count { it.outgoing && it.status.startsWith(com.pt2vhf.aprs.data.InternalText.MSG_AWAITING_STEM) }.toString())
            KeyValue(UiText.text(R.string.station_count), stations.size.toString())
            KeyValue(UiText.text(R.string.rf_link_count), links.size.toString())
            KeyValue("GPS", status.gpsAccuracyM?.let { "±${it.toInt()} m" } ?: UiText.text(R.string.no_gps))
            if (status.aprsIsError.isNotBlank())
                Text(UiText.text(R.string.aprs_error) + ": " + UiText.connectionError(status.aprsIsError),
                    color = Pal.orange, fontSize = 12.sp)
            if (status.tncError.isNotBlank())
                Text(UiText.text(R.string.tnc_error) + ": " + UiText.connectionError(status.tncError),
                    color = Pal.orange, fontSize = 12.sp)
            Text(UiText.text(R.string.diag_transitions), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            if (connectionLog.isEmpty()) Text("—", color = Pal.muted)
            connectionLog.takeLast(15).reversed().forEach {
                Text("${hhmm(it.time)} ${it.source}: ${it.text}", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { anonymize = !anonymize }) {
                Checkbox(anonymize, { anonymize = it })
                Text(UiText.text(R.string.diag_anonymize), fontSize = 13.sp)
            }
            OutlinedButton(onClick = {
                exportResult = null
                exporter.launch("pt2vhf-aprs-diagnostico.txt")
            }) { Text(UiText.text(R.string.diag_export)) }
            exportResult?.let { Text(it, color = Pal.muted, fontSize = 12.sp) }
        }
        SectionCard(UiText.text(R.string.live_monitor)) {
            Text(UiText.text(R.string.live_monitor_logs_hint), color = Pal.muted, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf("all" to R.string.filter_all, "rx" to R.string.monitor_rx, "tx" to R.string.monitor_tx,
                    "rf" to R.string.msg_filter_rf).forEach { (key, label) ->
                    OutlinedChoice(UiText.text(label), monitorMedium == key) { monitorMedium = key }
                }
            }
            OutlinedTextField(monitorFilter, { monitorFilter = it }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(UiText.text(R.string.monitor_filter), fontSize = 12.sp) })
            if (!settings.monitorEnabled) Text(UiText.text(R.string.monitor_disabled), color = Pal.orange, fontSize = 12.sp)
            val shown = monitor.filter { line ->
                (monitorFilter.isBlank() || line.contains(monitorFilter, true)) && when (monitorMedium) {
                    "rx" -> line.startsWith("RX")
                    "tx" -> line.startsWith("TX")
                    "rf" -> line.startsWith("RX RF") || line.startsWith("TX RF")
                    else -> true
                }
            }
            if (shown.isEmpty()) Text(UiText.text(R.string.await_tnc), color = Pal.muted, fontSize = 13.sp)
            for (line in shown.takeLast(40).reversed()) Text(line, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Pal.text)
        }
    }
}

private val COORD_LAT = Regex("""\d{4}\.\d{2}[NS]""")
private val COORD_LON = Regex("""\d{5}\.\d{2}[EW]""")
private val DECIMAL = Regex("""-?\d{1,3}\.\d{3,}""")

/** Texto de diagnóstico (#13). Nunca inclui passcode; anonimiza indicativo próprio e coordenadas se pedido. */
internal fun buildDiagnostics(settings: Settings, status: LinkStatus, anonymize: Boolean): String {
    val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
    fun clean(text: String): String {
        if (!anonymize) return text
        var out = text
        if (settings.callsign.isNotBlank()) out = out.replace(Regex(Regex.escape(settings.callsign) + "(-\\d{1,2})?", RegexOption.IGNORE_CASE), "MYCALL")
        out = COORD_LAT.replace(out, "****.**N")
        out = COORD_LON.replace(out, "*****.**E")
        return DECIMAL.replace(out, "*.***")
    }
    val sb = StringBuilder()
    sb.appendLine("PT2VHF APRS Client for Android ${BuildConfig.VERSION_NAME} — ${df.format(Date())}")
    sb.appendLine("Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT}), ${android.os.Build.MODEL}")
    sb.appendLine("Call: ${clean(settings.fullCall)} · profile ${settings.profile} · server ${settings.server}:${settings.port}")
    sb.appendLine("TX RF=${settings.txViaRf} TX APRS-IS=${settings.txViaInternet} rxOnly=${settings.rxOnly} beacon=${settings.beaconMode}")
    sb.appendLine("TNC type=${settings.tncType} connected=${status.tncConnected} ${status.tncName}")
    sb.appendLine("APRS-IS connected=${status.aprsIsConnected} verified=${status.aprsIsVerified} reconnects=${status.aprsIsReconnects}")
    sb.appendLine("Counters: aprsRx=${status.aprsRx} aprsTx=${status.aprsTx} tncRx=${status.tncRx} tncTx=${status.tncTx} beacons=${status.beaconsSent} digipeated=${status.beaconsDigipeated}")
    if (status.aprsIsError.isNotBlank()) sb.appendLine("APRS-IS error: ${status.aprsIsError}")
    if (status.tncError.isNotBlank()) sb.appendLine("TNC error: ${status.tncError}")
    sb.appendLine()
    sb.appendLine("== Connection log ==")
    AprsRepository.connectionLog.value.forEach { sb.appendLine("${df.format(Date(it.time))} ${it.source}: ${clean(it.text)}") }
    sb.appendLine()
    sb.appendLine("== Monitor (last 200) ==")
    AprsRepository.monitor.value.forEach { sb.appendLine(clean(it)) }
    return sb.toString()
}
