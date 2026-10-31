package com.pt2vhf.aprs.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.core.APRS_SERVERS
import com.pt2vhf.aprs.core.AprsMessage
import com.pt2vhf.aprs.core.CALLSIGN_RE
import com.pt2vhf.aprs.core.DEFAULT_FILTER
import com.pt2vhf.aprs.core.bearingDeg
import com.pt2vhf.aprs.core.compassName
import com.pt2vhf.aprs.core.haversineKm
import com.pt2vhf.aprs.core.maidenhead
import com.pt2vhf.aprs.core.validHostPort
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.ChatMessage
import com.pt2vhf.aprs.data.Medium
import com.pt2vhf.aprs.data.Settings
import com.pt2vhf.aprs.data.Station
import com.pt2vhf.aprs.service.AprsService
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.Date

private fun ago(t: Long): String {
    val s = (System.currentTimeMillis() - t) / 1000
    return when {
        s < 60 -> UiText.text(R.string.now)
        s < 3600 -> UiText.text(R.string.minutes_ago, s / 60)
        s < 86400 -> UiText.text(R.string.hours_ago, s / 3600)
        else -> java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT, UiText.locale()).format(Date(t))
    }
}

private fun hhmm(t: Long) = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, UiText.locale()).format(Date(t))

// ---------- Mapa ----------

@Composable
fun MapScreen(onStation: (String) -> Unit) {
    val stations by AprsRepository.stations.collectAsState()
    val links by AprsRepository.links.collectAsState()
    val status by AprsRepository.status.collectAsState()
    var centered by remember { mutableStateOf(false) }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(9.0)
                controller.setCenter(GeoPoint(-15.78, -47.93))
            }
        },
        update = { map ->
            map.overlays.clear()
            val dayAgo = System.currentTimeMillis() - 24 * 3600_000L
            for (l in links.filter { it.lastSeen > dayAgo }) {
                val a = stations[l.from] ?: continue
                val b = stations[l.to] ?: continue
                map.overlays.add(Polyline(map).apply {
                    setPoints(listOf(GeoPoint(a.lat, a.lon), GeoPoint(b.lat, b.lon)))
                    outlinePaint.color = 0xFFF5E400.toInt()
                    outlinePaint.strokeWidth = 5f
                })
            }
            for (s in stations.values) {
                map.overlays.add(Marker(map).apply {
                    position = GeoPoint(s.lat, s.lon)
                    title = s.call
                    snippet = "${UiText.role(s.role)} · ${ago(s.lastHeard)}"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ -> onStation(s.call); true }
                })
            }
            val lat = status.lat
            val lon = status.lon
            if (lat != null && lon != null) {
                map.overlays.add(Marker(map).apply {
                    position = GeoPoint(lat, lon)
                    title = UiText.text(R.string.me)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                })
                if (!centered) { map.controller.setCenter(GeoPoint(lat, lon)); centered = true }
            }
            map.invalidate()
        },
    )
}

// ---------- Mensagens ----------

@Composable
fun MessagesScreen(settings: Settings, chatWith: String?, onOpenChat: (String?) -> Unit, onStation: (String) -> Unit) {
    if (chatWith != null) {
        Conversation(settings, chatWith, onBack = { onOpenChat(null) }, onStation = onStation)
        return
    }
    val messages by AprsRepository.messages.collectAsState()
    var tab by rememberSaveable { mutableStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab, containerColor = Pal.bar, contentColor = Pal.accent) {
            listOf(UiText.text(R.string.chats), UiText.text(R.string.general), UiText.text(R.string.bulletins)).forEachIndexed { i, label ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
            }
        }
        when (tab) {
            0 -> {
                val own = settings.fullCall
                val direct = messages.filter { it.kind == AprsMessage.Kind.MESSAGE && it.to != "CQ" && (it.to == own || it.outgoing) }
                val byContact = direct.groupBy { if (it.outgoing) it.to else it.from }
                    .toList().sortedByDescending { (_, list) -> list.maxOf { it.time } }
                var newCall by rememberSaveable { mutableStateOf("") }
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(newCall, { newCall = it.uppercase().take(9) }, label = { Text(UiText.text(R.string.new_chat)) },
                        singleLine = true, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters))
                    TextButton(onClick = { if (CALLSIGN_RE.matches(newCall)) onOpenChat(newCall) }) { Text(UiText.text(R.string.open)) }
                }
                if (byContact.isEmpty()) Empty(UiText.text(R.string.no_chats))
                LazyColumn {
                    items(byContact, key = { it.first }) { (contact, list) ->
                        val last = list.maxBy { it.time }
                        Column(Modifier.fillMaxWidth().clickable { onOpenChat(contact) }.padding(16.dp, 12.dp)) {
                            Row { Text(contact, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text(ago(last.time), color = Pal.muted, fontSize = 12.sp) }
                            Text(last.text, color = Pal.muted, maxLines = 1, fontSize = 14.sp)
                        }
                    }
                }
            }
            else -> {
                val list = messages.filter {
                    if (tab == 1) it.kind == AprsMessage.Kind.MESSAGE && (it.to == "CQ" || it.to.startsWith("CQ"))
                    else it.kind == AprsMessage.Kind.BULLETIN
                }
                GroupChat(list, settings, if (tab == 1) "CQ" else "BLN0", onStation)
            }
        }
    }
}

@Composable
private fun Empty(text: String) {
    Text(text, color = Pal.muted, modifier = Modifier.padding(24.dp), fontSize = 14.sp)
}

@Composable
private fun Bubble(m: ChatMessage, showSender: Boolean, onStation: (String) -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (m.outgoing) Alignment.End else Alignment.Start) {
        Column(
            Modifier.widthIn(max = 300.dp)
                .background(if (m.outgoing) Pal.mine else Pal.card, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            if (showSender && !m.outgoing) Text(m.from, color = Pal.accent, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                modifier = Modifier.clickable { onStation(m.from) })
            Text(m.text, fontSize = 15.sp)
            val meta = if (m.outgoing) "${hhmm(m.time)} · ${UiText.messageStatus(m.status)}" else "${hhmm(m.time)} · ${UiText.receptionVia(m.via)}"
            Text(meta, color = if (m.outgoing && m.status.startsWith("Aguardando")) Pal.yellow else Pal.muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun Composer(placeholder: String, onSend: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    Row(Modifier.fillMaxWidth().background(Pal.bar).padding(10.dp).imePadding(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(text, { text = it }, placeholder = { Text(placeholder) }, modifier = Modifier.weight(1f), maxLines = 3)
        Button(onClick = { if (text.isNotBlank()) { onSend(text); text = "" } }, modifier = Modifier.padding(start = 8.dp)) { Text(UiText.text(R.string.send)) }
    }
}

@Composable
private fun Conversation(settings: Settings, contact: String, onBack: () -> Unit, onStation: (String) -> Unit) {
    val context = LocalContext.current
    val messages by AprsRepository.messages.collectAsState()
    val stations by AprsRepository.stations.collectAsState()
    val list = messages.filter { it.kind == AprsMessage.Kind.MESSAGE &&
        ((it.outgoing && it.to == contact) || (!it.outgoing && it.from == contact && it.to == settings.fullCall)) }
    val state = rememberLazyListState()
    LaunchedEffect(list.size) { if (list.isNotEmpty()) state.animateScrollToItem(list.size - 1) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().background(Pal.bar).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(UiText.text(R.string.back)) }
            Column(Modifier.weight(1f).clickable { onStation(contact) }) {
                Text(contact, fontWeight = FontWeight.Bold)
                stations[contact]?.let { Text("${it.medium.label} · ${ago(it.lastHeard)}", color = Pal.green, fontSize = 12.sp) }
            }
        }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 12.dp), state = state, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.id }) { Bubble(it, false, onStation) }
        }
        Composer(UiText.text(R.string.message_to, contact)) {
            AprsService.send(context, AprsService.ACTION_MESSAGE, mapOf(AprsService.EXTRA_TO to contact, AprsService.EXTRA_TEXT to it))
        }
    }
}

@Composable
private fun GroupChat(list: List<ChatMessage>, settings: Settings, defaultTo: String, onStation: (String) -> Unit) {
    val context = LocalContext.current
    val state = rememberLazyListState()
    LaunchedEffect(list.size) { if (list.isNotEmpty()) state.animateScrollToItem(list.size - 1) }
    Column(Modifier.fillMaxSize()) {
        if (list.isEmpty()) Empty(if (defaultTo == "CQ") UiText.text(R.string.no_general) else UiText.text(R.string.no_bulletins))
        LazyColumn(Modifier.weight(1f).padding(horizontal = 12.dp), state = state, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.id }) { Bubble(it, true, onStation) }
        }
        Composer(if (defaultTo == "CQ") UiText.text(R.string.message_all) else UiText.text(R.string.bulletin_default)) {
            AprsService.send(context, AprsService.ACTION_MESSAGE, mapOf(AprsService.EXTRA_TO to defaultTo, AprsService.EXTRA_TEXT to it))
        }
    }
}

// ---------- Estações ----------

@Composable
fun StationsScreen(onStation: (String) -> Unit) {
    val stations by AprsRepository.stations.collectAsState()
    val status by AprsRepository.status.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    val list = stations.values.filter { query.isBlank() || it.call.contains(query.trim(), ignoreCase = true) }
        .sortedByDescending { it.lastHeard }
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(query, { query = it }, label = { Text(UiText.text(R.string.search_callsign)) }, singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(12.dp))
        if (list.isEmpty()) Empty(UiText.text(R.string.no_stations))
        LazyColumn {
            items(list, key = { it.call }) { s ->
                Row(Modifier.fillMaxWidth().clickable { onStation(s.call) }.padding(16.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(s.call, fontWeight = FontWeight.Bold)
                        Text("${UiText.role(s.role)} · ${s.medium.label} · ${ago(s.lastHeard)}", color = Pal.muted, fontSize = 12.sp)
                    }
                    val lat = status.lat; val lon = status.lon
                    if (lat != null && lon != null) Text("%.1f km".format(haversineKm(lat, lon, s.lat, s.lon)), color = Pal.muted)
                }
            }
        }
    }
}

@Composable
fun StationDialog(call: String, onDismiss: () -> Unit, onMessage: () -> Unit) {
    val context = LocalContext.current
    val stations by AprsRepository.stations.collectAsState()
    val status by AprsRepository.status.collectAsState()
    val s: Station? = stations[call]
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pal.bar,
        title = { Text(call, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (s == null) Text(UiText.text(R.string.no_position), color = Pal.muted)
                else {
                    Text("${UiText.role(s.role)} · ${s.medium.label} · ${ago(s.lastHeard)}", color = Pal.muted, fontSize = 13.sp)
                    val lat = status.lat; val lon = status.lon
                    if (lat != null && lon != null) {
                        val d = bearingDeg(lat, lon, s.lat, s.lon)
                        KeyValue(UiText.text(R.string.distance), "%.1f km".format(haversineKm(lat, lon, s.lat, s.lon)))
                        KeyValue(UiText.text(R.string.direction), "${compassName(d)} $d°")
                    }
                    KeyValue(UiText.text(R.string.position), "%.4f, %.4f".format(s.lat, s.lon))
                    KeyValue("Grid", maidenhead(s.lat, s.lon))
                    s.speedKmh?.let { KeyValue(UiText.text(R.string.speed), "%.0f km/h".format(it)) }
                    if (s.comment.isNotBlank()) Text(s.comment, fontSize = 14.sp)
                    Text(UiText.text(R.string.via, s.via.ifBlank { UiText.text(R.string.direct) }), color = Pal.muted, fontSize = 12.sp)
                    if (s.heardBy.isNotEmpty()) Text(UiText.text(R.string.heard_by_rf, s.heardBy.joinToString(", ")), color = Pal.green, fontSize = 12.sp)
                }
            }
        },
        confirmButton = { TextButton(onClick = onMessage) { Text(UiText.text(R.string.message)) } },
        dismissButton = {
            Row {
                if (s != null) TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${s.lat},${s.lon}?q=${s.lat},${s.lon}($call)")))
                }) { Text(UiText.text(R.string.navigate)) }
                TextButton(onClick = onDismiss) { Text(UiText.text(R.string.close)) }
            }
        },
    )
}

// ---------- Rádio / TNC ----------

@SuppressLint("MissingPermission")
@Composable
fun RadioScreen(settings: Settings) {
    val context = LocalContext.current
    val status by AprsRepository.status.collectAsState()
    val monitor by AprsRepository.monitor.collectAsState()
    val bonded = remember {
        runCatching { context.getSystemService(BluetoothManager::class.java)?.adapter?.bondedDevices?.toList() }
            .getOrNull().orEmpty()
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionCard(if (status.tncConnected) UiText.text(R.string.tnc_connected) else "TNC") {
            Text(if (status.tncConnected) "${status.tncName} · KISS Bluetooth · ${status.tncRx} RX · ${status.tncTx} TX"
                else UiText.connectionError(status.tncError).ifBlank { UiText.text(R.string.pair_tnc) },
                color = if (status.tncConnected) Pal.green else Pal.muted, fontSize = 14.sp)
            if (status.tncConnected) OutlinedButton(onClick = { AprsService.send(context, AprsService.ACTION_BT_DISCONNECT) }) { Text(UiText.text(R.string.disconnect)) }
        }
        SectionCard(UiText.text(R.string.paired_devices)) {
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
        SectionCard(UiText.text(R.string.live_monitor)) {
            if (monitor.isEmpty()) Text(UiText.text(R.string.await_tnc), color = Pal.muted, fontSize = 13.sp)
            for (line in monitor.takeLast(40).reversed()) Text(line, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Pal.text)
        }
    }
}

// ---------- Tracker ----------

@Composable
fun TrackerScreen(settings: Settings) {
    val context = LocalContext.current
    val status by AprsRepository.status.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionCard {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (status.trackerOn) UiText.text(R.string.tracker_active) else UiText.text(R.string.tracker_off),
                    color = if (status.trackerOn) Pal.green else Pal.muted, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("%.0f km/h".format(status.speedKmh), fontSize = 44.sp, fontWeight = FontWeight.Bold)
                status.nextBeaconSec?.let { Text(UiText.text(R.string.next_beacon, it), color = Pal.muted, fontSize = 13.sp) }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Stat(UiText.text(R.string.sent), status.beaconsSent.toString(), Modifier.weight(1f))
                    Stat(UiText.text(R.string.digipeated), status.beaconsDigipeated.toString(), Modifier.weight(1f))
                }
            }
        }
        SectionCard(UiText.text(R.string.configuration)) {
            KeyValue(UiText.text(R.string.callsign), settings.fullCall)
            KeyValue(UiText.text(R.string.symbol), "${settings.symbolTable}${settings.symbol}")
            KeyValue(UiText.text(R.string.send_via), listOfNotNull("RF".takeIf { settings.txViaRf }, "APRS-IS".takeIf { settings.txViaInternet }).joinToString(" + "))
            KeyValue(UiText.text(R.string.rf_path), settings.rfPath)
            Text(settings.comment, color = Pal.muted, fontSize = 13.sp)
        }
        if (status.trackerOn) {
            OutlinedButton(onClick = { AprsService.send(context, AprsService.ACTION_TRACKER_OFF) }, modifier = Modifier.fillMaxWidth().height(52.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Pal.orange)) { Text(UiText.text(R.string.stop_tracker), color = Pal.orange) }
        } else {
            Button(onClick = { AprsService.send(context, AprsService.ACTION_TRACKER_ON) }, modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = settings.configured && status.lat != null) { Text(UiText.text(R.string.start_tracker)) }
        }
        Text(UiText.text(R.string.tracker_notice), color = Pal.muted, fontSize = 12.sp)
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    Column(modifier.background(Pal.ground, RoundedCornerShape(10.dp)).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Pal.muted, fontSize = 11.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

// ---------- Configurações ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(current: Settings, onDismiss: () -> Unit, onSave: (Settings) -> Unit) {
    var call by remember { mutableStateOf(current.callsign) }
    var ssid by remember { mutableStateOf(current.ssid.toString()) }
    val presetHosts = APRS_SERVERS.map { it.host }
    var custom by remember { mutableStateOf(current.server !in presetHosts) }
    var server by remember { mutableStateOf(current.server) }
    var port by remember { mutableStateOf(current.port.toString()) }
    var filter by remember { mutableStateOf(current.filter) }
    var comment by remember { mutableStateOf(current.comment) }
    var symbol by remember { mutableStateOf("${current.symbolTable}${current.symbol}") }
    var path by remember { mutableStateOf(current.rfPath) }
    var txRf by remember { mutableStateOf(current.txViaRf) }
    var txIs by remember { mutableStateOf(current.txViaInternet) }
    var autoAprsIs by remember { mutableStateOf(current.connectAprsIs) }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var confirmEmptyFilter by remember { mutableStateOf(false) }

    val base = call.trim().uppercase().substringBefore('-')
    val passcode = if (base.isNotEmpty()) com.pt2vhf.aprs.core.aprsPasscode(base).toString() else "—"

    fun build(): Settings? {
        val ssidN = ssid.toIntOrNull() ?: -1
        val portN = port.toIntOrNull() ?: -1
        val full = if (ssidN in 1..15) "$base-$ssidN" else base
        return when {
            !CALLSIGN_RE.matches(full) || ssidN !in 0..15 -> { error = UiText.text(R.string.invalid_call); null }
            !validHostPort(server, portN) -> { error = UiText.text(R.string.invalid_server); null }
            symbol.length != 2 -> { error = UiText.text(R.string.invalid_symbol); null }
            else -> current.copy(callsign = base, ssid = ssidN, server = server.trim(), port = portN, filter = filter.trim(),
                comment = comment, symbolTable = symbol[0], symbol = symbol[1], rfPath = path, txViaRf = txRf, txViaInternet = txIs, connectAprsIs = autoAprsIs)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pal.bar,
        title = { Text(UiText.text(R.string.settings)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(call, { call = it.uppercase().take(6) }, label = { Text(UiText.text(R.string.callsign)) }, singleLine = true,
                        modifier = Modifier.weight(2f), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters))
                    OutlinedTextField(ssid, { ssid = it.filter(Char::isDigit).take(2) }, label = { Text("SSID") }, singleLine = true,
                        modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                Text(UiText.text(R.string.passcode, passcode), color = Pal.muted, fontSize = 12.sp)
                ExposedDropdownMenuBox(expanded, { expanded = it }) {
                    OutlinedTextField(
                        value = if (custom) UiText.text(R.string.other) else (APRS_SERVERS.firstOrNull { it.host == server }?.let { "${it.host} — ${UiText.serverLabel(it.label)}" } ?: server),
                        onValueChange = {}, readOnly = true, label = { Text(UiText.text(R.string.aprs_server)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                    )
                    androidx.compose.material3.DropdownMenu(expanded, { expanded = false }) {
                        APRS_SERVERS.forEach { s ->
                            DropdownMenuItem(text = { Text("${s.host} — ${UiText.serverLabel(s.label)}") }, onClick = { server = s.host; custom = false; expanded = false })
                        }
                        DropdownMenuItem(text = { Text(UiText.text(R.string.other_type)) }, onClick = { custom = true; server = ""; expanded = false })
                    }
                }
                if (custom) OutlinedTextField(server, { server = it.trim() }, label = { Text(UiText.text(R.string.server)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(port, { port = it.filter(Char::isDigit).take(5) }, label = { Text(UiText.text(R.string.port)) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(autoAprsIs, { autoAprsIs = it })
                    Text(UiText.text(R.string.aprs_auto_connect))
                }
                OutlinedTextField(filter, { filter = it }, label = { Text(UiText.text(R.string.aprs_filter)) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text(UiText.text(R.string.filter_hint, DEFAULT_FILTER)) })
                OutlinedTextField(comment, { comment = it.take(43) }, label = { Text(UiText.text(R.string.beacon_comment)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(symbol, { symbol = it.take(2) }, label = { Text(UiText.text(R.string.symbol_table)) }, singleLine = true,
                    supportingText = { Text(UiText.text(R.string.symbols_hint)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(path, { path = it.uppercase() }, label = { Text(UiText.text(R.string.rf_path)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(txRf, { txRf = it }); Text(UiText.text(R.string.tx_rf)) }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(txIs, { txIs = it }); Text(UiText.text(R.string.tx_aprs)) }
                if (error.isNotEmpty()) Text(error, color = Pal.orange, fontSize = 13.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val s = build() ?: return@TextButton
                if (s.filter.isBlank() && !confirmEmptyFilter) { confirmEmptyFilter = true; error = UiText.text(R.string.filter_empty); return@TextButton }
                onSave(s)
            }) { Text(UiText.text(R.string.save)) }
        },
        dismissButton = { if (current.configured) TextButton(onClick = onDismiss) { Text(UiText.text(R.string.cancel)) } },
    )
}
