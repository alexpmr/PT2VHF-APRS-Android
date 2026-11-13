package com.pt2vhf.aprs.ui

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt2vhf.aprs.BuildConfig
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.core.batteryDrainPerHour
import com.pt2vhf.aprs.core.formatVoiceFrequency
import com.pt2vhf.aprs.core.messageAge
import com.pt2vhf.aprs.core.parseGpxRoute
import com.pt2vhf.aprs.core.routeLengthKm
import com.pt2vhf.aprs.core.validObjectName
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.CrashLog
import com.pt2vhf.aprs.data.MessageCategory
import com.pt2vhf.aprs.data.MyObject
import com.pt2vhf.aprs.data.Settings
import com.pt2vhf.aprs.data.UpdateInfo
import com.pt2vhf.aprs.data.UpdateManager
import com.pt2vhf.aprs.service.AprsService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

// ---------- Utilitários ----------

internal fun shareText(context: Context, subject: String, text: String) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain")
        .putExtra(Intent.EXTRA_SUBJECT, subject).putExtra(Intent.EXTRA_TEXT, text)
    runCatching { context.startActivity(Intent.createChooser(send, subject).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

/** #40: "04/11/2026 14:32 · há 3h e 45min" no dia corrente; só data e hora nos dias anteriores. */
internal fun messageTimeLabel(time: Long, now: Long = System.currentTimeMillis()): String {
    val stamp = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, UiText.locale()).format(Date(time))
    val age = messageAge(time, now)
    if (!age.sameDay) return stamp
    val elapsed = when {
        age.justNow -> UiText.text(R.string.elapsed_now)
        age.hours == 0 -> UiText.text(R.string.elapsed_min, age.minutes)
        else -> UiText.text(R.string.elapsed_h_min, age.hours, age.minutes)
    }
    return "$stamp · $elapsed"
}

/** Relógio de composição que avança a cada minuto (tempo decorrido e virada de meia-noite). */
@Composable
internal fun minuteClock(): Long {
    var clock by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L - System.currentTimeMillis() % 60_000L + 50)
            clock = System.currentTimeMillis()
        }
    }
    return clock
}

/** #45: relatório para "Enviar log": diagnóstico + configurações (sem passcode) + último travamento. */
internal fun buildReport(context: Context, settings: Settings, anonymize: Boolean): String {
    val sb = StringBuilder(buildDiagnostics(settings, AprsRepository.status.value, anonymize))
    sb.appendLine()
    sb.appendLine("== Settings (passcode is never stored) ==")
    sb.appendLine("profile=${settings.profile} beacon=${settings.beaconMode} position=${settings.positionSource} gps=${settings.gpsPrecision}")
    sb.appendLine("tnc=${settings.tncType} audioIn=${settings.audioInput} audioOut=${settings.audioOutput} vol=${settings.audioVolume} txdelay=${settings.audioTxDelayMs} vox=${settings.audioVoxLeadMs} usbBaud=${settings.usbBaud} ptt=${settings.pttMode}/${settings.pttBaud}/${settings.catProtocol}")
    sb.appendLine("txRf=${settings.txViaRf} txIs=${settings.txViaInternet} autoIs=${settings.connectAprsIs} retries=${settings.msgRetries}/${settings.msgRetryBaseSec}s saver=${settings.batterySaver}")
    sb.appendLine("filter=${if (anonymize) "(hidden)" else settings.filter} path=${settings.rfPath}")
    sb.appendLine()
    sb.appendLine("== Last crash ==")
    sb.appendLine(CrashLog.last(context) ?: "none")
    sb.appendLine()
    sb.appendLine("== Last recoverable operation error ==")
    sb.appendLine(CrashLog.lastRecoverable(context) ?: "none")
    return sb.toString()
}

internal fun sendLog(context: Context, settings: Settings, anonymize: Boolean = true) {
    shareText(context, UiText.text(R.string.send_log_subject, BuildConfig.VERSION_NAME), buildReport(context, settings, anonymize))
    CrashLog.markSeen(context)
}

// ---------- #39 aviso de nova versão ----------

@Composable
internal fun UpdateDialog(info: UpdateInfo, onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pal.bar,
        title = { Text(UiText.text(R.string.new_version, info.version)) },
        text = { Text(info.notes.ifBlank { UiText.text(R.string.new_version_notice) }) },
        confirmButton = {
            TextButton(onClick = {
                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(info.downloadUrl))) }
                onDismiss()
            }) { Text(UiText.text(R.string.download_install)) }
        },
        dismissButton = {
            Row {
                TextButton(onClick = { UpdateManager.ignore(context, info.version); onDismiss() }) {
                    Text(UiText.text(R.string.ignore_version), color = Pal.muted)
                }
                TextButton(onClick = onDismiss) { Text(UiText.text(R.string.later)) }
            }
        },
    )
}

/** Sobre: versão instalada, última verificação, resultado e "Verificar agora". */
@Composable
internal fun UpdateStatusSection() {
    val context = LocalContext.current
    val state by UpdateManager.state.collectAsState()
    LaunchedEffect(Unit) { UpdateManager.current(context) }
    Text(UiText.text(R.string.current_installed_version, BuildConfig.VERSION_NAME), color = Pal.text, fontSize = 13.sp)
    Text(if (state.lastCheck == 0L) UiText.text(R.string.update_never) else UiText.text(R.string.update_last,
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, UiText.locale()).format(Date(state.lastCheck))),
        color = Pal.muted, fontSize = 12.sp)
    val result = when {
        state.checking -> UiText.text(R.string.update_checking)
        state.available != null -> UiText.text(R.string.update_result_new, state.available!!.version)
        state.lastResult == "ok" -> UiText.text(R.string.update_result_ok)
        state.lastResult == "fail" -> UiText.text(R.string.update_result_fail)
        else -> ""
    }
    if (result.isNotEmpty()) Text(result, color = if (state.available != null) Pal.yellow else Pal.muted, fontSize = 12.sp)
    OutlinedButton(onClick = { UpdateManager.maybeCheck(context, 0L, force = true) }, enabled = !state.checking) {
        Text(UiText.text(R.string.update_check_now))
    }
}

// ---------- #41 apagar mensagens por tipo ----------

@Composable
internal fun DeleteMessagesDialog(onDismiss: () -> Unit, onResult: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var target by remember { mutableStateOf<Int?>(null) } // 0 todas, 1 boletins/gerais, 2 pessoais
    var busy by remember { mutableStateOf(false) }
    val t = target
    if (t == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Pal.bar,
            title = { Text(UiText.text(R.string.msg_delete_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = { target = 0 }) {
                        Text(UiText.text(R.string.msg_delete_all, AprsRepository.countMessages(null)), color = Pal.orange)
                    }
                    TextButton(onClick = { target = 1 }) {
                        Text(UiText.text(R.string.msg_delete_bulletins, AprsRepository.countMessages(MessageCategory.BULLETIN)), color = Pal.orange)
                    }
                    TextButton(onClick = { target = 2 }) {
                        Text(UiText.text(R.string.msg_delete_private, AprsRepository.countMessages(MessageCategory.PRIVATE)), color = Pal.orange)
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text(UiText.text(R.string.cancel)) } },
        )
    } else {
        AlertDialog(
            onDismissRequest = { if (!busy) target = null },
            containerColor = Pal.bar,
            title = { Text(UiText.text(R.string.msg_delete_title)) },
            text = { Text(UiText.text(when (t) {
                0 -> R.string.msg_delete_confirm_all
                1 -> R.string.msg_delete_confirm_bulletins
                else -> R.string.msg_delete_confirm_private
            })) },
            confirmButton = {
                TextButton(enabled = !busy, onClick = {
                    busy = true
                    scope.launch {
                        val ok = runCatching {
                            AprsRepository.clearMessages(when (t) { 0 -> null; 1 -> MessageCategory.BULLETIN; else -> MessageCategory.PRIVATE })
                        }.getOrDefault(false)
                        busy = false
                        onResult(UiText.text(if (ok) R.string.clear_local_data_success else R.string.clear_local_data_error))
                        onDismiss()
                    }
                }) { Text(UiText.text(R.string.clear_data_confirm), color = Pal.orange) }
            },
            dismissButton = { TextButton(enabled = !busy, onClick = { target = null }) { Text(UiText.text(R.string.cancel)) } },
        )
    }
}

// ---------- #48 objetos APRS ----------

@Composable
internal fun ObjectDialog(initial: MyObject, isNew: Boolean, settings: Settings, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initial.name) }
    var item by remember { mutableStateOf(initial.item) }
    var symbol by remember { mutableStateOf("${initial.symbolTable}${initial.symbol}") }
    var comment by remember { mutableStateOf(initial.comment.substringAfter("MHz ", initial.comment)) }
    var freq by remember { mutableStateOf(if (initial.comment.contains("MHz")) initial.comment.substringBefore("MHz").trim() else "") }
    var lat by remember { mutableStateOf("%.5f".format(java.util.Locale.ROOT, initial.lat)) }
    var lon by remember { mutableStateOf("%.5f".format(java.util.Locale.ROOT, initial.lon)) }
    var interval by remember { mutableStateOf(initial.intervalMin) }
    var picker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    fun build(): MyObject? {
        val la = lat.replace(',', '.').toDoubleOrNull()
        val lo = lon.replace(',', '.').toDoubleOrNull()
        if (!validObjectName(name, item)) { error = UiText.text(R.string.object_invalid_name); return null }
        if (la == null || lo == null || la !in -90.0..90.0 || lo !in -180.0..180.0) { error = UiText.text(R.string.manual_invalid); return null }
        if (symbol.length != 2) { error = UiText.text(R.string.invalid_symbol); return null }
        val f = if (freq.isBlank()) null else formatVoiceFrequency(freq.substringBefore(' '))
        if (freq.isNotBlank() && f == null) { error = UiText.text(R.string.invalid_frequency); return null }
        // Tom/offset escritos depois da frequência ("146.910- T123") ficam no comentário.
        val extra = freq.trim().substringAfter(' ', "").trim()
        val text = listOfNotNull(f, extra.ifBlank { null }, comment.trim().ifBlank { null }).joinToString(" ")
        return initial.copy(name = name.trim(), item = item, lat = la, lon = lo, symbolTable = symbol[0], symbol = symbol[1],
            comment = text.take(43), intervalMin = interval, active = true)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pal.bar,
        title = { Text(UiText.text(if (isNew) R.string.object_new else R.string.object_edit)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedChoice(UiText.text(R.string.object_type_object), !item) { item = false }
                    OutlinedChoice(UiText.text(R.string.object_type_item), item) { item = true }
                }
                OutlinedTextField(name, { name = it.take(9) }, singleLine = true, label = { Text(UiText.text(R.string.object_name)) },
                    modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val drawable = remember(symbol) {
                        if (symbol.length == 2) AprsSymbolIcons.icon(context, symbol[0], symbol[1]) as? BitmapDrawable else null
                    }
                    Box(Modifier.size(44.dp).border(1.dp, Pal.line, RoundedCornerShape(8.dp)).clickable { picker = true },
                        contentAlignment = Alignment.Center) {
                        if (drawable != null) Image(BitmapPainter(drawable.bitmap.asImageBitmap()), symbol, Modifier.size(32.dp))
                        else Text(symbol)
                    }
                    Text(UiText.text(R.string.object_symbol) + " $symbol", color = Pal.muted, fontSize = 13.sp)
                }
                OutlinedTextField(freq, { freq = it.take(20) }, singleLine = true, label = { Text(UiText.text(R.string.object_frequency)) },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(comment, { comment = it.take(43) }, singleLine = true, label = { Text(UiText.text(R.string.object_comment)) },
                    modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(lat, { lat = it.take(12) }, singleLine = true, label = { Text(UiText.text(R.string.manual_lat)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                    OutlinedTextField(lon, { lon = it.take(12) }, singleLine = true, label = { Text(UiText.text(R.string.manual_lon)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.weight(1f))
                }
                Text(UiText.text(R.string.object_interval), fontSize = 13.sp)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(0, 10, 15, 20, 30, 60).forEach { m ->
                        OutlinedChoice(if (m == 0) UiText.text(R.string.object_once) else UiText.text(R.string.object_minutes, m), interval == m) { interval = m }
                    }
                }
                Text(UiText.text(R.string.object_tx_note), color = Pal.muted, fontSize = 12.sp)
                if (settings.rxOnly) Text(UiText.text(R.string.rx_only_blocked), color = Pal.orange, fontSize = 12.sp)
                if (error.isNotEmpty()) Text(error, color = Pal.orange, fontSize = 13.sp)
            }
        },
        confirmButton = {
            TextButton(enabled = !settings.rxOnly && settings.configured, onClick = {
                val obj = build() ?: return@TextButton
                AprsRepository.upsertObject(obj.copy(lastSent = 0L))
                AprsService.send(context, AprsService.ACTION_OBJECT_SEND, mapOf(AprsService.EXTRA_OBJECT_ID to obj.id.toString()))
                onDismiss()
            }) { Text(UiText.text(R.string.object_save_send)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiText.text(R.string.cancel)) } },
    )
    if (picker) SymbolPicker(symbol, onPick = { symbol = it; picker = false }, onDismiss = { picker = false })
}

internal fun newObjectAt(lat: Double, lon: Double) = MyObject(System.currentTimeMillis(), "", false, lat, lon, '/', 'r', "", 10, true)

@Composable
internal fun MyObjectsCard(settings: Settings) {
    val context = LocalContext.current
    val objects by AprsRepository.myObjects.collectAsState()
    var editing by remember { mutableStateOf<MyObject?>(null) }
    var killing by remember { mutableStateOf<MyObject?>(null) }
    SectionCard(UiText.text(R.string.my_objects)) {
        if (objects.isEmpty()) Text(UiText.text(R.string.my_objects_empty), color = Pal.muted, fontSize = 13.sp)
        objects.forEach { o ->
            Column(Modifier.fillMaxWidth()) {
                Text(o.name + if (o.item) " (${UiText.text(R.string.object_type_item)})" else "", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(UiText.text(R.string.object_status,
                    if (o.active) o.comment.ifBlank { "${o.symbolTable}${o.symbol}" } else "⏸",
                    if (o.intervalMin == 0) UiText.text(R.string.object_once) else UiText.text(R.string.object_minutes, o.intervalMin),
                    if (o.lastSent > 0) ago(o.lastSent) else "—"), color = Pal.muted, fontSize = 12.sp)
                Row(Modifier.horizontalScroll(rememberScrollState())) {
                    TextButton(onClick = { editing = o }) { Text(UiText.text(R.string.object_edit)) }
                    TextButton(onClick = { AprsRepository.upsertObject(o.copy(active = !o.active)) }) {
                        Text(UiText.text(if (o.active) R.string.object_pause else R.string.object_resume))
                    }
                    TextButton(enabled = !settings.rxOnly, onClick = {
                        AprsService.send(context, AprsService.ACTION_OBJECT_SEND, mapOf(AprsService.EXTRA_OBJECT_ID to o.id.toString()))
                    }) { Text(UiText.text(R.string.object_send_now)) }
                    TextButton(onClick = { killing = o }) { Text(UiText.text(R.string.object_kill), color = Pal.orange) }
                }
            }
        }
    }
    editing?.let { ObjectDialog(it, false, settings, onDismiss = { editing = null }) }
    killing?.let { o ->
        AlertDialog(
            onDismissRequest = { killing = null },
            containerColor = Pal.bar,
            title = { Text(o.name) },
            text = { Text(UiText.text(R.string.object_kill_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    if (settings.rxOnly || !settings.configured) AprsRepository.removeObject(o.id)
                    else AprsService.send(context, AprsService.ACTION_OBJECT_KILL, mapOf(AprsService.EXTRA_OBJECT_ID to o.id.toString()))
                    killing = null
                }) { Text(UiText.text(R.string.object_kill), color = Pal.orange) }
            },
            dismissButton = { TextButton(onClick = { killing = null }) { Text(UiText.text(R.string.cancel)) } },
        )
    }
}

// ---------- Rotas (GPX) ----------

@Composable
internal fun RoutesCard() {
    val context = LocalContext.current
    val routes by AprsRepository.routes.collectAsState()
    var result by remember { mutableStateOf<String?>(null) }
    val importer = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) result = runCatching {
            val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }.orEmpty()
            val parsed = parseGpxRoute(text, UiText.text(R.string.routes_card)) ?: return@runCatching UiText.text(R.string.route_invalid)
            AprsRepository.addRoute(parsed.name, parsed.points)
            UiText.text(R.string.route_imported, parsed.name, parsed.points.size)
        }.getOrElse { UiText.text(R.string.route_invalid) }
    }
    SectionCard(UiText.text(R.string.routes_card)) {
        if (routes.isEmpty()) Text(UiText.text(R.string.routes_empty), color = Pal.muted, fontSize = 13.sp)
        routes.forEach { r ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(r.visible, { AprsRepository.setRouteVisible(r.id, it) })
                Column(Modifier.weight(1f)) {
                    Text(r.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(UiText.text(R.string.route_info, r.points.size, routeLengthKm(r.points)), color = Pal.muted, fontSize = 12.sp)
                }
                TextButton(onClick = { AprsRepository.removeRoute(r.id) }) { Text("✕", color = Pal.orange) }
            }
        }
        TextButton(onClick = { result = null; importer.launch(arrayOf("application/gpx+xml", "application/xml", "text/xml", "application/octet-stream", "*/*")) }) {
            Text(UiText.text(R.string.routes_import))
        }
        result?.let { Text(it, color = Pal.muted, fontSize = 12.sp) }
    }
}

// ---------- #49 bateria ----------

@Composable
internal fun BatteryCard(settings: Settings) {
    val status by AprsRepository.status.collectAsState()
    val samples by AprsRepository.batterySamples.collectAsState()
    val drain = remember(samples) { batteryDrainPerHour(samples) }
    SectionCard(UiText.text(R.string.battery_card)) {
        status.batteryPercent?.let { Text(UiText.text(R.string.battery_level, it), fontSize = 14.sp) }
        Text(drain?.let { UiText.text(R.string.battery_drain, it) } ?: UiText.text(R.string.battery_drain_unknown),
            color = Pal.muted, fontSize = 12.sp)
        Text(UiText.text(R.string.battery_section) + ": " + UiText.text(when (settings.batterySaver) {
            "on" -> R.string.battery_on
            "auto" -> R.string.battery_auto
            else -> R.string.battery_off
        }) + " · " + UiText.text(if (status.batterySaverActive) R.string.battery_saver_active else R.string.battery_saver_inactive),
            color = if (status.batterySaverActive) Pal.green else Pal.muted, fontSize = 12.sp)
        if (settings.batterySaver != "off") Text(UiText.text(R.string.battery_notice), color = Pal.muted, fontSize = 11.sp)
    }
}

// ---------- Modo carro ----------

/**
 * Tela simplificada para uso no carro: velocidade grande, conexões, próximo beacon, última mensagem,
 * botões grandes e leitura em voz alta opcional das mensagens recebidas (TTS do Android).
 */
@Composable
internal fun CarModeScreen(settings: Settings, onExit: () -> Unit) {
    val context = LocalContext.current
    val status by AprsRepository.status.collectAsState()
    val messages by AprsRepository.messages.collectAsState()
    var speak by remember { mutableStateOf(Settings.readOption(context, "car_speak", "false") == "true") }
    val tts = remember { arrayOfNulls<TextToSpeech>(1) }
    DisposableEffect(Unit) {
        tts[0] = TextToSpeech(context) { code -> if (code == TextToSpeech.SUCCESS) tts[0]?.setLanguage(UiText.locale()) }
        onDispose { tts[0]?.shutdown(); tts[0] = null }
    }
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = settings.keepScreenOn }
    }
    val incoming = messages.lastOrNull { !it.outgoing && (it.to == settings.fullCall || it.kind != com.pt2vhf.aprs.core.AprsMessage.Kind.MESSAGE || it.to.startsWith("CQ")) }
    val openedAt = remember { System.currentTimeMillis() }
    LaunchedEffect(incoming?.id) {
        val m = incoming ?: return@LaunchedEffect
        if (speak && m.time >= openedAt && m.to == settings.fullCall)
            tts[0]?.speak(UiText.text(R.string.message_from, m.from) + ". " + m.text, TextToSpeech.QUEUE_ADD, null, "msg${m.id}")
    }
    Column(Modifier.fillMaxSize().background(Color.Black).statusBarsPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(settings.fullCall, color = Pal.text, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            TextButton(onClick = onExit) { Text(UiText.text(R.string.car_exit), fontSize = 18.sp) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("APRS-IS", status.aprsIsVerified)
            Chip("TNC", status.tncConnected)
            Chip("GPS", status.manualPosition || (status.gpsAccuracyM != null && System.currentTimeMillis() - status.gpsFixTimeMs <= 120_000L))
        }
        Text("%.0f km/h".format(status.speedKmh), color = Pal.text, fontSize = 72.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Text(listOfNotNull(status.course?.let { "${com.pt2vhf.aprs.core.compassName(it)} $it°" },
            status.nextBeaconSec?.takeIf { status.trackerOn }?.let { UiText.text(R.string.car_next_beacon, it.toInt()) })
            .joinToString(" · "), color = Pal.muted, fontSize = 18.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Column(Modifier.fillMaxWidth().background(Pal.card, RoundedCornerShape(12.dp)).padding(12.dp)) {
            Text(UiText.text(R.string.car_last_message), color = Pal.muted, fontSize = 14.sp)
            if (incoming == null) Text(UiText.text(R.string.car_no_message), color = Pal.muted, fontSize = 18.sp)
            else {
                Text(incoming.from + " · " + ago(incoming.time), color = Pal.accent, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(incoming.text, color = Pal.text, fontSize = 22.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth().clickable {
            speak = !speak; Settings.saveOption(context, "car_speak", speak.toString())
        }, verticalAlignment = Alignment.CenterVertically) {
            Checkbox(speak, { speak = it; Settings.saveOption(context, "car_speak", it.toString()) })
            Text(UiText.text(R.string.car_speak), color = Pal.text, fontSize = 16.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { AprsService.send(context, AprsService.ACTION_BEACON) },
                enabled = settings.configured && !settings.rxOnly && status.lat != null,
                colors = ButtonDefaults.buttonColors(containerColor = Pal.yellow, contentColor = Color.Black),
                modifier = Modifier.weight(1f).height(84.dp)) {
                Text(UiText.text(R.string.send_beacon), fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Button(onClick = {
                AprsService.send(context, if (status.trackerOn) AprsService.ACTION_TRACKER_OFF else AprsService.ACTION_TRACKER_ON)
            }, enabled = settings.configured && !settings.rxOnly && status.lat != null,
                colors = ButtonDefaults.buttonColors(containerColor = if (status.trackerOn) Pal.orange else Pal.blue),
                modifier = Modifier.weight(1f).height(84.dp)) {
                Text(UiText.text(if (status.trackerOn) R.string.stop_tracker else R.string.start_tracker), fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
        }
    }
}

// ---------- #44 modem de áudio e USB-OTG (tela Rádio) ----------

@Composable
internal fun AudioTncCard(settings: Settings, onChange: (Settings) -> Unit, onRequestMic: () -> Unit, micGranted: Boolean) {
    val context = LocalContext.current
    val status by AprsRepository.status.collectAsState()
    var volume by remember(settings.audioVolume) { mutableStateOf(settings.audioVolume) }
    var txDelay by remember(settings.audioTxDelayMs) { mutableStateOf(settings.audioTxDelayMs.toString()) }
    var vox by remember(settings.audioVoxLeadMs) { mutableStateOf(settings.audioVoxLeadMs.toString()) }
    fun inputLabel(k: String) = UiText.text(when (k) {
        "unprocessed" -> R.string.audio_in_unprocessed
        "wired" -> R.string.audio_in_wired
        "usb" -> R.string.audio_in_usb
        "bt" -> R.string.audio_in_bt
        else -> R.string.audio_in_default
    })
    SectionCard(UiText.text(R.string.audio_card)) {
        Text(UiText.text(R.string.audio_hint), color = Pal.muted, fontSize = 12.sp)
        if (!micGranted) Button(onClick = onRequestMic) { Text(UiText.text(R.string.audio_permission)) }
        Text(when {
            status.audioTransmitting -> UiText.text(R.string.audio_transmitting)
            status.tncConnected && settings.tncType == "audio" -> UiText.text(R.string.audio_listening)
            else -> UiText.connectionError(status.tncError).ifBlank { "—" }
        }, color = if (status.audioTransmitting) Pal.yellow else if (status.tncConnected) Pal.green else Pal.muted, fontSize = 13.sp)
        Text(UiText.text(R.string.audio_level), fontSize = 12.sp, color = Pal.muted)
        LinearProgressIndicator(progress = { (status.audioLevel * 4).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = if (status.audioLevel > 0.22) Pal.orange else Pal.green)
        Text(UiText.text(R.string.audio_input), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Settings.AUDIO_INPUTS.forEach { k -> OutlinedChoice(inputLabel(k), settings.audioInput == k) { onChange(settings.copy(audioInput = k)) } }
        }
        Text(UiText.text(R.string.audio_output), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Settings.AUDIO_OUTPUTS.forEach { k ->
                OutlinedChoice(if (k == "default") UiText.text(R.string.audio_out_default) else inputLabel(k), settings.audioOutput == k) {
                    onChange(settings.copy(audioOutput = k))
                }
            }
        }
        Text(UiText.text(R.string.audio_volume, volume), fontSize = 13.sp)
        Slider(volume.toFloat(), { volume = it.toInt() }, valueRange = 5f..100f,
            onValueChangeFinished = { onChange(settings.copy(audioVolume = volume)) })
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedTextField(txDelay, { txDelay = it.filter(Char::isDigit).take(4) }, singleLine = true, modifier = Modifier.weight(1f),
                label = { Text(UiText.text(R.string.audio_txdelay)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(vox, { vox = it.filter(Char::isDigit).take(4) }, singleLine = true, modifier = Modifier.weight(1f),
                label = { Text(UiText.text(R.string.audio_vox_lead)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        }
        Text(UiText.text(R.string.audio_quality), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedChoice(UiText.text(R.string.audio_quality_high), settings.audioHighQuality) { onChange(settings.copy(audioHighQuality = true)) }
            OutlinedChoice(UiText.text(R.string.audio_quality_low), !settings.audioHighQuality) { onChange(settings.copy(audioHighQuality = false)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            OutlinedButton(onClick = {
                onChange(settings.copy(audioTxDelayMs = (txDelay.toIntOrNull() ?: settings.audioTxDelayMs).coerceIn(50, 1500),
                    audioVoxLeadMs = (vox.toIntOrNull() ?: settings.audioVoxLeadMs).coerceIn(0, 1500)))
                AprsService.send(context, AprsService.ACTION_BT_RESUME)
            }, enabled = micGranted) { Text(UiText.text(R.string.audio_start)) }
            OutlinedButton(onClick = { AprsService.send(context, AprsService.ACTION_AUDIO_TEST) },
                enabled = status.tncConnected && settings.txViaRf && !settings.rxOnly && !status.audioTransmitting) {
                Text(UiText.text(R.string.audio_test_tone))
            }
        }
        Text(UiText.text(R.string.audio_tx_note), color = Pal.muted, fontSize = 11.sp)
    }
}

@Composable
internal fun UsbTncCard(settings: Settings, onChange: (Settings) -> Unit) {
    val context = LocalContext.current
    val status by AprsRepository.status.collectAsState()
    val attached = remember(status.tncConnected, status.tncError) {
        runCatching {
            context.getSystemService(android.hardware.usb.UsbManager::class.java)?.deviceList?.values?.firstOrNull()
                ?.let { it.productName ?: "USB %04X:%04X".format(it.vendorId, it.productId) }
        }.getOrNull()
    }
    SectionCard(UiText.text(R.string.usb_card)) {
        Text(UiText.text(R.string.usb_hint), color = Pal.muted, fontSize = 12.sp)
        Text(attached?.let { UiText.text(R.string.usb_detected, it) } ?: UiText.text(R.string.usb_none), fontSize = 13.sp)
        Text(UiText.text(R.string.usb_baud), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Settings.USB_BAUDS.forEach { b -> OutlinedChoice(b.toString(), settings.usbBaud == b) { onChange(settings.copy(usbBaud = b)) } }
        }
        OutlinedButton(onClick = { AprsService.send(context, AprsService.ACTION_BT_RESUME) }) { Text(UiText.text(R.string.connect)) }
    }
}
