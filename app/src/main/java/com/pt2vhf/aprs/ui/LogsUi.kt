package com.pt2vhf.aprs.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.core.AprsPacket
import com.pt2vhf.aprs.core.PACKET_TYPES
import com.pt2vhf.aprs.core.decodeAprsText
import com.pt2vhf.aprs.core.parseMessage
import com.pt2vhf.aprs.core.parseObject
import com.pt2vhf.aprs.core.parsePacketPosition
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.Medium
import com.pt2vhf.aprs.data.PacketLogEntry
import com.pt2vhf.aprs.data.Settings
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

internal fun packetTypeLabel(type: String): String = UiText.text(when (type) {
    "position" -> R.string.type_position
    "message" -> R.string.type_message
    "bulletin" -> R.string.type_bulletin
    "ack" -> R.string.type_ack
    "object" -> R.string.type_object
    "item" -> R.string.type_item
    "status" -> R.string.type_status
    "telemetry" -> R.string.type_telemetry
    "weather" -> R.string.type_weather
    "mice" -> R.string.type_mice
    "query" -> R.string.type_query
    "capabilities" -> R.string.type_capabilities
    "nmea" -> R.string.type_nmea
    "user" -> R.string.type_user
    else -> R.string.type_other
})

private fun fullStamp(t: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, UiText.locale()).format(Date(t))

internal fun logMatches(e: PacketLogEntry, dir: String, medium: String, type: String, text: String): Boolean =
    (dir == "all" || e.direction.equals(dir, true)) &&
        (medium == "all" || (medium == "rf") == (e.medium == Medium.RF)) &&
        (type == "all" || e.type == type) &&
        (text.isBlank() || e.raw.contains(text.trim(), true))

private fun logsAsText(list: List<PacketLogEntry>, csv: Boolean): String {
    val iso = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.ROOT)
    val sb = StringBuilder()
    if (csv) sb.appendLine("time,direction,medium,type,source,destination,raw")
    for (e in list.asReversed()) {
        if (csv) {
            fun q(v: String) = "\"" + v.replace("\"", "\"\"") + "\""
            sb.appendLine(listOf(iso.format(Date(e.time)), e.direction, e.medium.label, e.type, e.source, e.destination)
                .joinToString(",") { q(it) } + "," + q(e.raw))
        } else sb.appendLine("${iso.format(Date(e.time))} ${e.direction} ${e.medium.label} [${e.type}] ${e.raw}")
    }
    return sb.toString()
}

/** Aba Logs (#51): todos os pacotes recebidos e enviados, persistidos com retenção configurável. */
@Composable
fun LogsScreen(onStation: (String) -> Unit) {
    val context = LocalContext.current
    val all by AprsRepository.packetLog.collectAsState()
    val scope = rememberCoroutineScope()
    var dir by rememberSaveable { mutableStateOf(Settings.readOption(context, "logs_dir", "all")) }
    var medium by rememberSaveable { mutableStateOf(Settings.readOption(context, "logs_medium", "all")) }
    var type by rememberSaveable { mutableStateOf(Settings.readOption(context, "logs_type", "all")) }
    var text by rememberSaveable { mutableStateOf("") }
    var paused by rememberSaveable { mutableStateOf(false) }
    var frozen by remember { mutableStateOf<List<PacketLogEntry>>(emptyList()) }
    var typeMenu by remember { mutableStateOf(false) }
    var optionsOpen by remember { mutableStateOf(false) }
    var details by remember { mutableStateOf<PacketLogEntry?>(null) }
    var toolsRaw by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var exportCsv by remember { mutableStateOf(false) }
    val source = if (paused) frozen else all
    val shown = remember(source, dir, medium, type, text) { source.filter { logMatches(it, dir, medium, type, text) } }
    val listState = rememberLazyListState()
    LaunchedEffect(all.firstOrNull()?.id) { if (!paused && listState.firstVisibleItemIndex <= 1) listState.scrollToItem(0) }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) scope.launch {
            val everything = AprsRepository.allPacketLog().filter { logMatches(it, dir, medium, type, text) }
            message = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { it.write(logsAsText(everything, exportCsv).toByteArray()) }
                UiText.text(R.string.logs_export_done, everything.size)
            }.getOrElse { UiText.text(R.string.diag_export_failed) }
        }
    }
    fun save(key: String, v: String) = Settings.saveOption(context, key, v)
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            CompactSearchField(text, { text = it }, UiText.text(R.string.logs_filter_hint), Modifier.weight(1f))
            TextButton(onClick = {
                if (!paused) frozen = all
                paused = !paused
            }) { Text(if (paused) "▶" else "⏸", fontSize = 18.sp) }
            // #69: ferramentas (decodificador de pacote e frequências APRS).
            TextButton(onClick = { toolsRaw = "" }, modifier = Modifier.semantics {
                contentDescription = UiText.text(R.string.tools_title)
            }) { Text("🛠", fontSize = 18.sp) }
            TextButton(onClick = { optionsOpen = true }) { Text("⋮", fontSize = 20.sp) }
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf("all" to R.string.filter_all, "RX" to R.string.monitor_rx, "TX" to R.string.monitor_tx).forEach { (k, l) ->
                OutlinedChoice(UiText.text(l), dir == k) { dir = k; save("logs_dir", k) }
            }
            listOf("all" to R.string.filter_all, "rf" to R.string.msg_filter_rf, "is" to R.string.station_source_internet).forEach { (k, l) ->
                OutlinedChoice(UiText.text(l), medium == k) { medium = k; save("logs_medium", k) }
            }
            androidx.compose.foundation.layout.Box {
                OutlinedChoice(UiText.text(R.string.logs_type) + ": " + if (type == "all") UiText.text(R.string.filter_all) else packetTypeLabel(type),
                    type != "all") { typeMenu = true }
                DropdownMenu(typeMenu, { typeMenu = false }) {
                    DropdownMenuItem(text = { Text(UiText.text(R.string.filter_all)) }, onClick = { type = "all"; save("logs_type", "all"); typeMenu = false })
                    PACKET_TYPES.forEach { t ->
                        DropdownMenuItem(text = { Text(packetTypeLabel(t)) }, onClick = { type = t; save("logs_type", t); typeMenu = false })
                    }
                }
            }
        }
        Text(UiText.text(R.string.logs_count, shown.size, all.size) + if (paused) " · ⏸" else "",
            color = Pal.muted, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 12.dp))
        message?.let { Text(it, color = Pal.green, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp)) }
        if (shown.isEmpty()) Empty(UiText.text(R.string.logs_empty))
        LazyColumn(Modifier.fillMaxSize(), state = listState) {
            items(shown, key = { it.id }) { e ->
                Column(Modifier.fillMaxWidth().clickable { details = e }.padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(e.direction, color = if (e.direction == "TX") Pal.yellow else Pal.green, fontWeight = FontWeight.Bold,
                            fontSize = 11.sp, modifier = Modifier.padding(end = 6.dp))
                        Text(e.medium.label, color = Pal.accent, fontSize = 11.sp, modifier = Modifier.padding(end = 6.dp))
                        Text("${e.source}→${e.destination}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Text(packetTypeLabel(e.type), color = Pal.muted, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp))
                        Text(fullStamp(e.time), color = Pal.muted, fontSize = 10.sp)
                    }
                    Text(decodeAprsText(e.raw), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Pal.text,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
    details?.let { e -> PacketDetailsDialog(e, onDismiss = { details = null }, onStation = { details = null; onStation(it) },
        onDecode = { raw -> details = null; toolsRaw = raw }) }
    toolsRaw?.let { raw -> ToolsDialog(raw, onDismiss = { toolsRaw = null }) }
    if (optionsOpen) LogOptionsDialog(
        onDismiss = { optionsOpen = false },
        onExport = { csv -> optionsOpen = false; exportCsv = csv; message = null
            exporter.launch(if (csv) "pt2vhf-aprs-logs.csv" else "pt2vhf-aprs-logs.txt") },
        onCleared = { ok -> optionsOpen = false; message = UiText.text(if (ok) R.string.clear_local_data_success else R.string.clear_local_data_error) },
    )
}

@Composable
private fun LogOptionsDialog(onDismiss: () -> Unit, onExport: (Boolean) -> Unit, onCleared: (Boolean) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var days by remember { mutableStateOf(AprsRepository.logRetentionDays()) }
    var max by remember { mutableStateOf(AprsRepository.logMaxRows()) }
    var confirmClear by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pal.bar,
        title = { Text(UiText.text(R.string.nav_logs)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(UiText.text(R.string.logs_retention), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 7, 30).forEach { d ->
                        OutlinedChoice(if (d == 1) UiText.text(R.string.retention_24h) else UiText.text(R.string.days_n, d), days == d) {
                            days = d; Settings.saveOption(context, "log_retention_days", d.toString()); AprsRepository.applyLogRetention()
                        }
                    }
                }
                Text(UiText.text(R.string.retention_count), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(5000, 20000, 100000).forEach { m ->
                        OutlinedChoice("%,d".format(m), max == m) {
                            max = m; Settings.saveOption(context, "log_max", m.toString()); AprsRepository.applyLogRetention()
                        }
                    }
                }
                Text(UiText.text(R.string.logs_stored, AprsRepository.packetLogCount()), color = Pal.muted, fontSize = 12.sp)
                TextButton(onClick = { onExport(false) }) { Text(UiText.text(R.string.logs_export_txt)) }
                TextButton(onClick = { onExport(true) }) { Text(UiText.text(R.string.logs_export_csv)) }
                TextButton(onClick = { confirmClear = true }) { Text(UiText.text(R.string.logs_clear), color = Pal.orange) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(UiText.text(R.string.close)) } },
    )
    if (confirmClear) AlertDialog(
        onDismissRequest = { confirmClear = false },
        containerColor = Pal.bar,
        title = { Text(UiText.text(R.string.logs_clear)) },
        text = { Text(UiText.text(R.string.logs_clear_confirm)) },
        confirmButton = {
            TextButton(onClick = {
                confirmClear = false
                scope.launch { onCleared(AprsRepository.clearPacketLog()) }
            }) { Text(UiText.text(R.string.clear_data_confirm), color = Pal.orange) }
        },
        dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(UiText.text(R.string.cancel)) } },
    )
}

@Composable
private fun PacketDetailsDialog(e: PacketLogEntry, onDismiss: () -> Unit, onStation: (String) -> Unit, onDecode: (String) -> Unit = {}) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val packet = remember(e.raw) { AprsPacket.parse(e.raw) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pal.bar,
        title = { Text(UiText.text(R.string.logs_details)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                KeyValue(UiText.text(R.string.logs_time), fullStamp(e.time))
                KeyValue(UiText.text(R.string.logs_direction), e.direction)
                KeyValue(UiText.text(R.string.logs_medium), e.medium.label)
                KeyValue(UiText.text(R.string.logs_type), packetTypeLabel(e.type))
                if (packet != null) {
                    KeyValue(UiText.text(R.string.logs_source), packet.source)
                    KeyValue(UiText.text(R.string.logs_destination), packet.destination)
                    if (packet.path.isNotEmpty()) Text(UiText.text(R.string.logs_path) + ": " + packet.path.joinToString(","),
                        fontSize = 13.sp, color = Pal.muted)
                    val obj = parseObject(packet.info)
                    val pos = obj?.position ?: parsePacketPosition(packet)
                    if (obj != null) KeyValue(UiText.text(R.string.type_object), obj.name + if (obj.alive) "" else " ✗")
                    if (pos != null) {
                        KeyValue(UiText.text(R.string.position), "%.5f, %.5f".format(pos.lat, pos.lon))
                        KeyValue(UiText.text(R.string.symbol), "${pos.symbolTable}${pos.symbol}")
                        pos.speedKmh?.let { KeyValue(UiText.text(R.string.speed), "%.0f km/h".format(it)) }
                        if (pos.comment.isNotBlank()) Text(decodeAprsText(pos.comment), fontSize = 13.sp)
                    }
                    parseMessage(packet.info)?.let { m ->
                        KeyValue(UiText.text(R.string.logs_destination), m.addressee)
                        if (m.text.isNotBlank()) Text(decodeAprsText(m.text), fontSize = 14.sp)
                        m.id?.let { KeyValue("ID", it) }
                    }
                }
                Text(UiText.text(R.string.logs_raw), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(decodeAprsText(e.raw), fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                Row {
                    TextButton(onClick = { clipboard.setText(AnnotatedString(e.raw)) }) { Text(UiText.text(R.string.logs_copy)) }
                    TextButton(onClick = { shareText(context, "APRS", e.raw) }) { Text(UiText.text(R.string.logs_share)) }
                    TextButton(onClick = { onDecode(e.raw) }) { Text(UiText.text(R.string.tools_decode)) }
                }
                if (packet != null && AprsRepository.stations.value.containsKey(packet.source))
                    TextButton(onClick = { onStation(packet.source) }) { Text(UiText.text(R.string.logs_open_station)) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(UiText.text(R.string.close)) } },
    )
}
