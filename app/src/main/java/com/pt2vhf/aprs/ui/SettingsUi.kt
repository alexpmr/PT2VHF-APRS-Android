package com.pt2vhf.aprs.ui

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt2vhf.aprs.BuildConfig
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.core.APRS_SERVERS
import com.pt2vhf.aprs.core.plausibleAmateurCallsign
import com.pt2vhf.aprs.core.DEFAULT_FILTER
import com.pt2vhf.aprs.core.formatVoiceFrequency
import com.pt2vhf.aprs.core.validHostPort
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.Settings
import com.pt2vhf.aprs.data.MapIconSize
import com.pt2vhf.aprs.data.UpdateCheck
import kotlinx.coroutines.launch

internal fun profileLabel(profile: String): String = UiText.text(when (profile) {
    Settings.PROFILE_BASE -> R.string.profile_base
    Settings.PROFILE_MOBILE -> R.string.profile_mobile
    Settings.PROFILE_PORTABLE -> R.string.profile_portable
    Settings.PROFILE_RX -> R.string.profile_rx
    else -> R.string.profile_custom
})

@Composable
private fun SectionTitle(text: String) {
    Text(text.uppercase(), color = Pal.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp, modifier = Modifier.padding(top = 10.dp))
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { onChange(!checked) }, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked, onChange)
        Text(label, fontSize = 14.sp)
    }
}

@Composable
private fun NumberField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(value, { onChange(it.filter(Char::isDigit).take(5)) }, label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = modifier)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> Choices(options: List<Pair<T, String>>, selected: T, onSelect: (T) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { (value, label) -> OutlinedChoice(label, value == selected) { onSelect(value) } }
    }
}

@Composable
private fun RetentionRow(
    title: String, days: Int, max: Int,
    dayOptions: List<Pair<Int, String>>, maxOptions: List<Pair<Int, String>>,
    onDays: (Int) -> Unit, onMax: (Int) -> Unit,
) {
    Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    Text(UiText.text(R.string.retention_age), fontSize = 12.sp, color = Pal.muted)
    Choices(dayOptions, days, onDays)
    Text(UiText.text(R.string.retention_count), fontSize = 12.sp, color = Pal.muted)
    Choices(maxOptions, max, onMax)
}

/** Seletor visual de símbolo (#6): tabelas primária e alternativa com os ícones embutidos no APK. */
@Composable
internal fun SymbolPicker(current: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var table by remember { mutableStateOf(if (current.firstOrNull() == '/') '/' else '\\') }
    val codes = remember { ('!'..'~').toList() }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pal.bar,
        title = { Text(UiText.text(R.string.symbol_picker)) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedChoice(UiText.text(R.string.symbol_primary), table == '/') { table = '/' }
                    OutlinedChoice(UiText.text(R.string.symbol_alternate), table == '\\') { table = '\\' }
                }
                LazyVerticalGrid(GridCells.Adaptive(40.dp), Modifier.heightIn(max = 380.dp)) {
                    items(codes) { code ->
                        val drawable = remember(table, code) { AprsSymbolIcons.icon(context, table, code) as? BitmapDrawable }
                        val selected = current == "$table$code"
                        Box(Modifier.padding(2.dp).size(36.dp)
                            .background(if (selected) Pal.mine else Pal.ground, RoundedCornerShape(6.dp))
                            .clickable { onPick("$table$code") }, contentAlignment = Alignment.Center) {
                            if (drawable != null) Image(BitmapPainter(drawable.bitmap.asImageBitmap()), "$table$code",
                                Modifier.size(28.dp))
                            else Text("$code", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(UiText.text(R.string.close)) } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(current: Settings, onDismiss: () -> Unit, onSave: (Settings) -> Unit) {
    val context = LocalContext.current
    var profile by remember { mutableStateOf(current.profile) }
    var call by remember { mutableStateOf(current.callsign) }
    var ssid by remember { mutableStateOf(current.ssid.toString()) }
    val presetHosts = APRS_SERVERS.map { it.host }
    var custom by remember { mutableStateOf(current.server !in presetHosts) }
    var server by remember { mutableStateOf(current.server) }
    var port by remember { mutableStateOf(current.port.toString()) }
    var filter by remember { mutableStateOf(current.filter) }
    var comment by remember { mutableStateOf(current.comment) }
    var voiceFreq by remember { mutableStateOf(current.voiceFrequency) }
    var symbol by remember { mutableStateOf("${current.symbolTable}${current.symbol}") }
    var path by remember { mutableStateOf(current.rfPath) }
    var txRf by remember { mutableStateOf(current.txViaRf) }
    var txIs by remember { mutableStateOf(current.txViaInternet) }
    var autoAprsIs by remember { mutableStateOf(current.connectAprsIs) }
    var beaconMode by remember { mutableStateOf(current.beaconMode) }
    var autoTracker by remember { mutableStateOf(current.trackerAutoStart) }
    var periodicSec by remember { mutableStateOf(current.periodicIntervalSec.toString()) }
    var slowSpeed by remember { mutableStateOf(current.smartSlowSpeedKmh.toString()) }
    var fastSpeed by remember { mutableStateOf(current.smartFastSpeedKmh.toString()) }
    var slowSec by remember { mutableStateOf(current.smartSlowIntervalSec.toString()) }
    var fastSec by remember { mutableStateOf(current.smartFastIntervalSec.toString()) }
    var turnAngle by remember { mutableStateOf(current.smartMinTurnAngle.toString()) }
    var turnSlope by remember { mutableStateOf(current.smartTurnSlope.toString()) }
    var turnTime by remember { mutableStateOf(current.smartMinTurnTimeSec.toString()) }
    var stoppedMin by remember { mutableStateOf(current.stoppedBeaconMinutes.toString()) }
    var ambiguity by remember { mutableStateOf(current.ambiguity) }
    var courseSpeed by remember { mutableStateOf(current.sendCourseSpeed) }
    var altitude by remember { mutableStateOf(current.sendAltitude) }
    var keepScreen by remember { mutableStateOf(current.keepScreenOn) }
    var mapIconSize by remember { mutableStateOf(MapIconSize.fromKey(Settings.readOption(context, MapIconSize.OPTION_KEY, "medium"))) }
    var maxAge by remember { mutableStateOf(current.stationMaxAgeHours) }
    var notifyMsg by remember { mutableStateOf(current.notifyMessages) }
    var monitorOn by remember { mutableStateOf(current.monitorEnabled) }
    var favFirst by remember { mutableStateOf(current.alertFavFirst) }
    var favReturn by remember { mutableStateOf(current.alertFavReturn) }
    var favPos by remember { mutableStateOf(current.alertFavPosition) }
    var favRf by remember { mutableStateOf(current.alertFavRf) }
    var myDigi by remember { mutableStateOf(current.alertMyDigi) }
    var returnMin by remember { mutableStateOf(current.alertReturnMinutes.toString()) }
    var quiet by remember { mutableStateOf(current.quietHours) }
    var quietStart by remember { mutableStateOf(current.quietStart.toString()) }
    var quietEnd by remember { mutableStateOf(current.quietEnd.toString()) }
    var apiOn by remember { mutableStateOf(current.apiEnabled) }
    var apiControl by remember { mutableStateOf(current.apiAllowControl) }
    var apiBeacon by remember { mutableStateOf(current.apiAllowBeacon) }
    var retSame by remember { mutableStateOf(current.retPrivateDays == current.retGeneralDays &&
        current.retGeneralDays == current.retBulletinDays && current.retPrivateMax == current.retGeneralMax &&
        current.retGeneralMax == current.retBulletinMax) }
    var retPrivDays by remember { mutableStateOf(current.retPrivateDays) }
    var retGenDays by remember { mutableStateOf(current.retGeneralDays) }
    var retBlnDays by remember { mutableStateOf(current.retBulletinDays) }
    var retPrivMax by remember { mutableStateOf(current.retPrivateMax) }
    var retGenMax by remember { mutableStateOf(current.retGeneralMax) }
    var retBlnMax by remember { mutableStateOf(current.retBulletinMax) }
    var retentionConfirmedFor by remember { mutableStateOf<Int?>(null) }
    // 0.1.9: posição (#6), bateria (#49), boletim (#43), reenvio (#47), aviso de versão (#39)
    var positionSource by remember { mutableStateOf(current.positionSource) }
    var manualLat by remember { mutableStateOf(current.manualLat?.let { "%.5f".format(java.util.Locale.ROOT, it) } ?: "") }
    var manualLon by remember { mutableStateOf(current.manualLon?.let { "%.5f".format(java.util.Locale.ROOT, it) } ?: "") }
    var gpsPrecision by remember { mutableStateOf(current.gpsPrecision) }
    var useNetwork by remember { mutableStateOf(current.useNetworkLocation) }
    var batterySaver by remember { mutableStateOf(current.batterySaver) }
    var batteryAuto by remember { mutableStateOf(current.batteryAutoPercent.toString()) }
    var announce by remember { mutableStateOf(current.announceVersion) }
    var retries by remember { mutableStateOf(current.msgRetries.toString()) }
    var retryBase by remember { mutableStateOf(current.msgRetryBaseSec.toString()) }
    var updateNotify by remember { mutableStateOf(current.updateNotify) }
    var shareCallsign by remember { mutableStateOf(current.shareCallsign) }
    var stripAccents by remember { mutableStateOf(current.stripAccents) }
    // #80: SSID digitado nesta edição não é trocado pelo do perfil; #82: janela "Qual SSID usar?".
    var ssidTouched by remember { mutableStateOf(false) }
    var ssidHelp by remember { mutableStateOf(false) }
    var confirmClose by remember { mutableStateOf(false) }
    var deleteMessagesOpen by remember { mutableStateOf(false) }
    var backupIncludeMessages by remember { mutableStateOf(false) }
    var backupMessage by remember { mutableStateOf<String?>(null) }
    var importPreview by remember { mutableStateOf<com.pt2vhf.aprs.data.SettingsBackup.Preview?>(null) }
    var importMessages by remember { mutableStateOf(true) }
    val backupExporter = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) backupMessage = runCatching {
            val text = com.pt2vhf.aprs.data.SettingsBackup.export(context, backupIncludeMessages)
            context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
            UiText.text(R.string.backup_done)
        }.getOrElse { UiText.text(R.string.backup_failed) }
    }
    val backupImporter = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            val text = context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }.orEmpty()
            importPreview = com.pt2vhf.aprs.data.SettingsBackup.preview(text)
        }.onFailure { backupMessage = UiText.text(R.string.backup_invalid) }
    }
    var expanded by remember { mutableStateOf(false) }
    var symbolPicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    // #74: abas por assunto; primeira configuração abre em Estação, depois lembra a última aba da sessão.
    var tab by remember { mutableStateOf(if (current.configured) SettingsTabMemory.last else SettingsTab.STATION) }
    var confirmEmptyFilter by remember { mutableStateOf(false) }
    var clearTarget by remember { mutableStateOf<Int?>(null) }
    var clearBusy by remember { mutableStateOf(false) }
    var clearResult by remember { mutableStateOf<String?>(null) }
    val clearScope = rememberCoroutineScope()

    val base = call.trim().uppercase().substringBefore('-')
    val initialLat = remember { manualLat }
    val initialLon = remember { manualLon }
    val passcode = if (base.isNotEmpty()) com.pt2vhf.aprs.core.aprsPasscode(base).toString() else "—"

    fun build(force: Boolean = false): Settings? {
        val ssidN = ssid.toIntOrNull() ?: -1
        val portN = port.toIntOrNull() ?: -1
        val full = if (ssidN in 1..15) "$base-$ssidN" else base
        if (!force) when {
            !plausibleAmateurCallsign(full) || ssidN !in 0..15 -> {
                error = UiText.text(R.string.invalid_call) + "\n" + callsignHint(com.pt2vhf.aprs.core.checkAmateurCallsign(full))
                tab = SettingsTab.STATION
                return null
            }
            !validHostPort(server, portN) -> { error = UiText.text(R.string.invalid_server); tab = SettingsTab.APRS_IS; return null }
            symbol.length != 2 -> { error = UiText.text(R.string.invalid_symbol); tab = SettingsTab.STATION; return null }
            voiceFreq.isNotBlank() && formatVoiceFrequency(voiceFreq) == null -> {
                error = UiText.text(R.string.invalid_frequency); tab = SettingsTab.STATION; return null
            }
            positionSource == "manual" && (manualLat.replace(',', '.').toDoubleOrNull()?.takeIf { it in -90.0..90.0 } == null ||
                manualLon.replace(',', '.').toDoubleOrNull()?.takeIf { it in -180.0..180.0 } == null) -> {
                error = UiText.text(R.string.manual_invalid); tab = SettingsTab.POSITION; return null
            }
        }
        val pd = retPrivDays
        return current.copy(callsign = base, ssid = ssidN.coerceIn(0, 15), server = server.trim(),
            port = if (portN in 1..65535) portN else current.port, filter = filter.trim(),
            comment = comment, symbolTable = symbol.getOrElse(0) { '/' }, symbol = symbol.getOrElse(1) { '>' }, rfPath = path,
            txViaRf = txRf, txViaInternet = txIs, connectAprsIs = autoAprsIs,
            beaconMode = beaconMode, trackerAutoStart = autoTracker,
            periodicIntervalSec = (periodicSec.toIntOrNull() ?: current.periodicIntervalSec).coerceIn(30, 86400),
            smartSlowSpeedKmh = (slowSpeed.toIntOrNull() ?: current.smartSlowSpeedKmh).coerceIn(1, 200),
            smartFastSpeedKmh = (fastSpeed.toIntOrNull() ?: current.smartFastSpeedKmh).coerceIn(5, 300),
            smartSlowIntervalSec = (slowSec.toIntOrNull() ?: current.smartSlowIntervalSec).coerceIn(30, 86400),
            smartFastIntervalSec = (fastSec.toIntOrNull() ?: current.smartFastIntervalSec).coerceIn(30, 86400),
            smartMinTurnAngle = (turnAngle.toIntOrNull() ?: current.smartMinTurnAngle).coerceIn(5, 90),
            smartTurnSlope = (turnSlope.toIntOrNull() ?: current.smartTurnSlope).coerceIn(0, 1000),
            smartMinTurnTimeSec = (turnTime.toIntOrNull() ?: current.smartMinTurnTimeSec).coerceIn(5, 600),
            stoppedBeaconMinutes = (stoppedMin.toIntOrNull() ?: current.stoppedBeaconMinutes).coerceIn(0, 1440),
            profile = profile, ambiguity = ambiguity, sendCourseSpeed = courseSpeed, sendAltitude = altitude,
            voiceFrequency = voiceFreq.trim(), keepScreenOn = keepScreen, stationMaxAgeHours = maxAge,
            notifyMessages = notifyMsg, monitorEnabled = monitorOn,
            alertFavFirst = favFirst, alertFavReturn = favReturn, alertFavPosition = favPos, alertFavRf = favRf,
            alertMyDigi = myDigi, alertReturnMinutes = (returnMin.toIntOrNull() ?: 60).coerceIn(5, 1440),
            quietHours = quiet, quietStart = (quietStart.toIntOrNull() ?: 22).coerceIn(0, 23),
            quietEnd = (quietEnd.toIntOrNull() ?: 7).coerceIn(0, 23),
            apiEnabled = apiOn, apiAllowControl = apiControl, apiAllowBeacon = apiBeacon,
            retPrivateDays = pd, retGeneralDays = if (retSame) pd else retGenDays,
            retBulletinDays = if (retSame) pd else retBlnDays,
            retPrivateMax = retPrivMax, retGeneralMax = if (retSame) retPrivMax else retGenMax,
            retBulletinMax = if (retSame) retPrivMax else retBlnMax,
            positionSource = positionSource,
            manualLat = if (manualLat == initialLat) current.manualLat
                else manualLat.replace(',', '.').toDoubleOrNull()?.takeIf { it in -90.0..90.0 },
            manualLon = if (manualLon == initialLon) current.manualLon
                else manualLon.replace(',', '.').toDoubleOrNull()?.takeIf { it in -180.0..180.0 },
            gpsPrecision = gpsPrecision, useNetworkLocation = useNetwork,
            batterySaver = batterySaver, batteryAutoPercent = (batteryAuto.toIntOrNull() ?: 20).coerceIn(5, 80),
            announceVersion = announce,
            msgRetries = (retries.toIntOrNull() ?: 5).coerceIn(1, 10),
            msgRetryBaseSec = (retryBase.toIntOrNull() ?: 30).coerceIn(10, 300),
            updateNotify = updateNotify, shareCallsign = shareCallsign, stripAccents = stripAccents)
    }

    /** Loads the chosen profile into the form; nothing is saved or transmitted until Save. */
    fun loadProfile(name: String) {
        // #80: tocar no perfil já marcado não recarrega nada (antes apagava o que foi digitado).
        if (name == profile) return
        val p = Settings.forProfile(context, build(force = true) ?: current, name)
        profile = name
        if (!ssidTouched) ssid = p.ssid.toString()
        symbol = "${p.symbolTable}${p.symbol}"; comment = p.comment
        voiceFreq = p.voiceFrequency; path = p.rfPath; beaconMode = p.beaconMode
        periodicSec = p.periodicIntervalSec.toString(); slowSpeed = p.smartSlowSpeedKmh.toString()
        fastSpeed = p.smartFastSpeedKmh.toString(); slowSec = p.smartSlowIntervalSec.toString()
        fastSec = p.smartFastIntervalSec.toString(); turnAngle = p.smartMinTurnAngle.toString()
        turnSlope = p.smartTurnSlope.toString(); turnTime = p.smartMinTurnTimeSec.toString()
        stoppedMin = p.stoppedBeaconMinutes.toString(); txRf = p.txViaRf; txIs = p.txViaInternet
        autoAprsIs = p.connectAprsIs; filter = p.filter; ambiguity = p.ambiguity
        courseSpeed = p.sendCourseSpeed; altitude = p.sendAltitude; autoTracker = p.trackerAutoStart
    }

    /** Valida e grava; mesma regra no ✓ do cabeçalho, no "Salvar" do rodapé e na pergunta ao sair. */
    fun trySave() {
        val s = build() ?: return
        if (s.filter.isBlank() && !confirmEmptyFilter) {
            confirmEmptyFilter = true; error = UiText.text(R.string.filter_empty); tab = SettingsTab.APRS_IS
            return
        }
        // A retention change that deletes messages right away needs a second, explicit tap.
        val victims = AprsRepository.retentionPreview(s.retention)
        if (victims > 0 && retentionConfirmedFor != victims) {
            retentionConfirmedFor = victims
            error = UiText.text(R.string.retention_confirm, victims)
            tab = SettingsTab.MESSAGES
            return
        }
        onSave(s)
        AprsRepository.applyRetention(s.retention)
    }

    /** #80: Voltar/Cancelar nunca descarta alterações em silêncio. */
    fun requestClose() {
        if (build(force = true) != current) confirmClose = true else onDismiss()
    }

    // #42: "24 h" = 1 dia corrido, contado do horário de cada mensagem.
    val dayOptions = listOf(1, 7, 30, 90, 180, 365, 0).map { it to when (it) {
        0 -> UiText.text(R.string.retention_no_limit)
        1 -> UiText.text(R.string.retention_24h)
        else -> UiText.text(R.string.days_n, it)
    } }
    val maxOptions = listOf(500, 1000, 5000, 0).map { it to (if (it == 0) UiText.text(R.string.retention_no_limit) else "%,d".format(it)) }

    // #74: tela cheia com abas por assunto; cada aba rola sozinha e o "Salvar" grava todas de uma vez.
    // #85: janela borda a borda em todas as versões, com recuo das barras do sistema e do teclado
    // (Android 15/16 com navegação por 3 botões ou barra de tarefas cobria o rodapé com o "Salvar").
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { requestClose() },
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false,
            dismissOnClickOutside = false, decorFitsSystemWindows = false),
    ) {
        androidx.compose.material3.Surface(Modifier.fillMaxSize(), color = Pal.bar, contentColor = Pal.text) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).background(Pal.ground)) {
            Row(Modifier.fillMaxWidth().background(Pal.bar).padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(UiText.text(R.string.settings), fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                // #85: "Salvar" também no cabeçalho, visível mesmo se o rodapé ficar escondido.
                TextButton(onClick = { trySave() }) { Text("✓ " + UiText.text(R.string.save), fontWeight = FontWeight.Bold) }
            }
            androidx.compose.material3.ScrollableTabRow(selectedTabIndex = tab.ordinal, containerColor = Pal.bar,
                contentColor = Pal.accent, edgePadding = 8.dp) {
                SettingsTab.entries.forEach { t ->
                    androidx.compose.material3.Tab(selected = tab == t, onClick = { tab = t; SettingsTabMemory.last = t },
                        selectedContentColor = Pal.accent, unselectedContentColor = Pal.muted,
                        text = { Text(t.label(), fontSize = 13.sp, maxLines = 1) })
                }
            }
            if (!current.configured && tab == SettingsTab.STATION)
                Text(UiText.text(R.string.settings_first_run), color = Pal.yellow, fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth().background(Pal.card).padding(horizontal = 16.dp, vertical = 6.dp))
            androidx.compose.runtime.key(tab) {
            Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                when (tab) {
                SettingsTab.STATION -> {
                // ----- Perfil (#10) -----
                SectionTitle(UiText.text(R.string.profile_section))
                Choices(Settings.PROFILES.map { it to profileLabel(it) }, profile) { loadProfile(it) }
                Text(UiText.text(if (profile == Settings.PROFILE_RX) R.string.profile_rx_notice else R.string.profile_notice),
                    color = if (profile == Settings.PROFILE_RX) Pal.orange else Pal.muted, fontSize = 12.sp)

                // ----- Estação -----
                SectionTitle(UiText.text(R.string.station_section))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(call, { call = it.uppercase().take(6) }, label = { Text(UiText.text(R.string.callsign) + " *") }, singleLine = true,
                        modifier = Modifier.weight(2f), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters))
                    OutlinedTextField(ssid, { ssid = it.filter(Char::isDigit).take(2); ssidTouched = true },
                        label = { Text("SSID") }, singleLine = true,
                        trailingIcon = {
                            Text("ⓘ", fontSize = 20.sp, color = Pal.accent,
                                modifier = Modifier.clickable { ssidHelp = true }.padding(8.dp))
                        },
                        modifier = Modifier.weight(1.3f).onFocusChanged {
                            // #82: abre sozinha na primeira edição, até marcar "Não mostrar de novo".
                            if (it.isFocused && !Settings.flag(context, SSID_HELP_SEEN)) ssidHelp = true
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                // #80: o SSID fica guardado no perfil ativo.
                Text(UiText.text(R.string.ssid_saved_in_profile, profileLabel(profile)), color = Pal.muted, fontSize = 12.sp)
                // #59: país pelo prefixo ITU ou motivo da recusa, enquanto digita; aviso se o salvo é inválido.
                val callCheck = remember(call) { com.pt2vhf.aprs.core.checkAmateurCallsign(call) }
                if (call.isNotBlank()) Text(callsignHint(callCheck), fontSize = 12.sp,
                    color = if (callCheck.ok) Pal.green else Pal.orange)
                if (current.callsign.isNotBlank() && !current.configured)
                    Text(UiText.text(R.string.saved_call_invalid, current.callsign), color = Pal.orange, fontSize = 12.sp)
                Text(UiText.text(R.string.passcode, passcode), color = Pal.muted, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val drawable = remember(symbol) {
                        if (symbol.length == 2) AprsSymbolIcons.icon(context, symbol[0], symbol[1]) as? BitmapDrawable else null
                    }
                    Box(Modifier.size(44.dp).border(1.dp, Pal.line, RoundedCornerShape(8.dp)).clickable { symbolPicker = true },
                        contentAlignment = Alignment.Center) {
                        if (drawable != null) Image(BitmapPainter(drawable.bitmap.asImageBitmap()), symbol, Modifier.size(32.dp))
                        else Text(symbol)
                    }
                    OutlinedTextField(symbol, { symbol = it.take(2) }, label = { Text(UiText.text(R.string.symbol_table)) }, singleLine = true,
                        supportingText = { Text(UiText.text(R.string.symbols_hint)) }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(comment, { comment = it.take(43) }, label = { Text(UiText.text(R.string.beacon_comment)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(voiceFreq, { voiceFreq = it.take(12) }, label = { Text(UiText.text(R.string.voice_frequency)) }, singleLine = true,
                    supportingText = { Text(UiText.text(R.string.voice_frequency_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth())
                }

                SettingsTab.APRS_IS -> {
                SectionTitle("APRS-IS")
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
                NumberField(UiText.text(R.string.port), port, { port = it })
                CheckRow(UiText.text(R.string.aprs_auto_connect), autoAprsIs) { autoAprsIs = it }
                OutlinedTextField(filter, { filter = it }, label = { Text(UiText.text(R.string.aprs_filter)) }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text(UiText.text(R.string.filter_hint, DEFAULT_FILTER)) })
                }

                SettingsTab.BEACON -> {
                SectionTitle(UiText.text(R.string.beacon_section))
                OutlinedTextField(path, { path = it.uppercase() }, label = { Text(UiText.text(R.string.rf_path)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                CheckRow(UiText.text(R.string.tx_rf), txRf) { txRf = it }
                CheckRow(UiText.text(R.string.tx_aprs), txIs) { txIs = it }
                CheckRow(UiText.text(R.string.tracker_auto_start), autoTracker) { autoTracker = it }
                Text(UiText.text(R.string.tracker_auto_start_notice), color = Pal.muted, fontSize = 12.sp)
                Text(UiText.text(R.string.beacon_mode), color = Pal.muted)
                Choices(listOf("smart" to UiText.text(R.string.mode_smart), "periodic" to UiText.text(R.string.mode_periodic),
                    "manual" to UiText.text(R.string.mode_manual)), beaconMode) { beaconMode = it }
                if (beaconMode == "periodic") NumberField(UiText.text(R.string.beacon_interval), periodicSec, { periodicSec = it })
                if (beaconMode == "smart") {
                    Text(UiText.text(R.string.smart_help), color = Pal.muted, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NumberField(UiText.text(R.string.smart_slow_speed), slowSpeed, { slowSpeed = it }, Modifier.weight(1f))
                        NumberField(UiText.text(R.string.smart_fast_speed), fastSpeed, { fastSpeed = it }, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NumberField(UiText.text(R.string.smart_slow_rate), slowSec, { slowSec = it }, Modifier.weight(1f))
                        NumberField(UiText.text(R.string.smart_fast_rate), fastSec, { fastSec = it }, Modifier.weight(1f))
                    }
                    Text(UiText.text(R.string.smart_corner_help), color = Pal.muted, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NumberField(UiText.text(R.string.smart_turn_angle), turnAngle, { turnAngle = it }, Modifier.weight(1f))
                        NumberField(UiText.text(R.string.smart_turn_slope), turnSlope, { turnSlope = it }, Modifier.weight(1f))
                        NumberField(UiText.text(R.string.smart_turn_time), turnTime, { turnTime = it }, Modifier.weight(1f))
                    }
                    NumberField(UiText.text(R.string.smart_stopped_minutes), stoppedMin, { stoppedMin = it })
                }
                }

                SettingsTab.POSITION -> {
                SectionTitle(UiText.text(R.string.position_section))
                Choices(listOf("gps" to UiText.text(R.string.position_source_gps), "manual" to UiText.text(R.string.position_source_manual)),
                    positionSource) { positionSource = it }
                if (positionSource == "manual") {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(manualLat, { manualLat = it.take(12) }, singleLine = true, modifier = Modifier.weight(1f),
                            label = { Text(UiText.text(R.string.manual_lat)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                        OutlinedTextField(manualLon, { manualLon = it.take(12) }, singleLine = true, modifier = Modifier.weight(1f),
                            label = { Text(UiText.text(R.string.manual_lon)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    }
                    val known = AprsRepository.status.value
                    TextButton(enabled = known.lat != null && known.lon != null, onClick = {
                        manualLat = "%.5f".format(java.util.Locale.ROOT, known.lat ?: 0.0)
                        manualLon = "%.5f".format(java.util.Locale.ROOT, known.lon ?: 0.0)
                    }) { Text(UiText.text(R.string.manual_use_last)) }
                    Text(UiText.text(R.string.manual_map_hint), color = Pal.muted, fontSize = 12.sp)
                } else {
                    Text(UiText.text(R.string.gps_precision), fontSize = 13.sp)
                    Choices(listOf("high" to UiText.text(R.string.gps_high), "balanced" to UiText.text(R.string.gps_balanced),
                        "low" to UiText.text(R.string.gps_low)), gpsPrecision) { gpsPrecision = it }
                    CheckRow(UiText.text(R.string.use_network_location), useNetwork) { useNetwork = it }
                }

                // ----- Privacidade -----
                SectionTitle(UiText.text(R.string.privacy_section))
                Text(UiText.text(R.string.ambiguity_label), fontSize = 13.sp)
                Choices((0..4).map { it to UiText.text(R.string.ambiguity_level, it, listOf("18 m", "185 m", "1,8 km", "18 km", "110 km")[it]) },
                    ambiguity) { ambiguity = it }
                CheckRow(UiText.text(R.string.send_course_speed), courseSpeed) { courseSpeed = it }
                CheckRow(UiText.text(R.string.send_altitude), altitude) { altitude = it }

                // ----- Economia de bateria (#49) -----
                SectionTitle(UiText.text(R.string.battery_section))
                Choices(listOf("off" to UiText.text(R.string.battery_off), "on" to UiText.text(R.string.battery_on),
                    "auto" to UiText.text(R.string.battery_auto)), batterySaver) { batterySaver = it }
                if (batterySaver == "auto") NumberField(UiText.text(R.string.battery_auto_percent), batteryAuto, { batteryAuto = it.take(2) })
                Text(UiText.text(R.string.battery_notice), color = Pal.muted, fontSize = 12.sp)
                }

                SettingsTab.MAP -> {
                SectionTitle(UiText.text(R.string.display_section))
                CheckRow(UiText.text(R.string.keep_screen_on), keepScreen) { keepScreen = it }
                Text(UiText.text(R.string.station_max_age), fontSize = 13.sp)
                Choices(listOf(0, 1, 6, 24, 72, 168).map { it to (if (it == 0) UiText.text(R.string.filter_all) else UiText.text(R.string.hours_n, it)) },
                    maxAge) { maxAge = it }
                // #63: tamanho dos ícones no mapa, salvo na hora (vale ao voltar para o mapa).
                Text(UiText.text(R.string.map_icon_size), fontSize = 13.sp)
                Choices(listOf(MapIconSize.SMALL to UiText.text(R.string.map_icon_small),
                    MapIconSize.MEDIUM to UiText.text(R.string.map_icon_medium),
                    MapIconSize.LARGE to UiText.text(R.string.map_icon_large)), mapIconSize) {
                    mapIconSize = it
                    Settings.saveOption(context, MapIconSize.OPTION_KEY, it.key)
                    MapIconPrefs.changed.value = it
                }
                }

                SettingsTab.MESSAGES -> {
                // ----- Notificações e alertas (#6, #12) -----
                SectionTitle(UiText.text(R.string.notifications_section))
                CheckRow(UiText.text(R.string.notify_messages), notifyMsg) { notifyMsg = it }
                Text(UiText.text(R.string.alerts_favorites), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                CheckRow(UiText.text(R.string.alert_opt_first), favFirst) { favFirst = it }
                CheckRow(UiText.text(R.string.alert_opt_return), favReturn) { favReturn = it }
                if (favReturn) NumberField(UiText.text(R.string.alert_return_minutes), returnMin, { returnMin = it })
                CheckRow(UiText.text(R.string.alert_opt_position), favPos) { favPos = it }
                CheckRow(UiText.text(R.string.alert_opt_rf), favRf) { favRf = it }
                CheckRow(UiText.text(R.string.alert_opt_my_digi), myDigi) { myDigi = it }
                CheckRow(UiText.text(R.string.quiet_hours), quiet) { quiet = it }
                if (quiet) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumberField(UiText.text(R.string.quiet_start), quietStart, { quietStart = it.take(2) }, Modifier.weight(1f))
                    NumberField(UiText.text(R.string.quiet_end), quietEnd, { quietEnd = it.take(2) }, Modifier.weight(1f))
                }
                Text(UiText.text(R.string.alerts_notice), color = Pal.muted, fontSize = 12.sp)

                // #47 reenvio e #43 boletim de versão
                SectionTitle(UiText.text(R.string.nav_messages))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumberField(UiText.text(R.string.msg_retries), retries, { retries = it.take(2) }, Modifier.weight(1f))
                    NumberField(UiText.text(R.string.msg_retry_base), retryBase, { retryBase = it.take(3) }, Modifier.weight(1f))
                }
                CheckRow(UiText.text(R.string.announce_version), announce) { announce = it }
                // #77: tirar acentos antes de enviar (padrão ligado).
                CheckRow(UiText.text(R.string.strip_accents), stripAccents) { stripAccents = it }
                Text(UiText.text(R.string.strip_accents_hint), color = Pal.muted, fontSize = 12.sp)

                // ----- Mensagens: retenção (#26) -----
                SectionTitle(UiText.text(R.string.retention_section))
                val stored = AprsRepository.messages.value.size
                Text(UiText.text(R.string.retention_stored, stored, AprsRepository.messageStorageBytes() / 1024),
                    color = Pal.muted, fontSize = 12.sp)
                CheckRow(UiText.text(R.string.retention_same_for_all), retSame) { retSame = it }
                if (retSame) RetentionRow(UiText.text(R.string.retention_all), retPrivDays, retPrivMax, dayOptions, maxOptions, { retPrivDays = it }, { retPrivMax = it })
                else {
                    RetentionRow(UiText.text(R.string.chats), retPrivDays, retPrivMax, dayOptions, maxOptions, { retPrivDays = it }, { retPrivMax = it })
                    RetentionRow(UiText.text(R.string.general), retGenDays, retGenMax, dayOptions, maxOptions, { retGenDays = it }, { retGenMax = it })
                    RetentionRow(UiText.text(R.string.bulletins), retBlnDays, retBlnMax, dayOptions, maxOptions, { retBlnDays = it }, { retBlnMax = it })
                }
                Text(UiText.text(R.string.retention_notice), color = Pal.muted, fontSize = 12.sp)
                val preview = build(force = true)?.let { AprsRepository.retentionPreview(it.retention) } ?: 0
                if (preview > 0) Text(UiText.text(R.string.retention_preview, preview), color = Pal.orange, fontSize = 12.sp)
                OutlinedButton(onClick = { deleteMessagesOpen = true; clearResult = null },
                    enabled = !clearBusy, modifier = Modifier.fillMaxWidth()) {
                    Text(UiText.text(R.string.msg_delete_title), color = Pal.orange)
                }
                clearResult?.let { Text(it, color = Pal.muted, fontSize = 13.sp) }
                }

                SettingsTab.SYSTEM -> {
                SectionTitle(UiText.text(R.string.notifications_section))
                CheckRow(UiText.text(R.string.update_notify), updateNotify) { updateNotify = it }
                CheckRow(UiText.text(R.string.share_callsign), shareCallsign) { shareCallsign = it }
                Text(UiText.text(R.string.share_callsign_hint), color = Pal.muted, fontSize = 12.sp)

                // ----- API (#15) -----
                SectionTitle(UiText.text(R.string.api_section))
                CheckRow(UiText.text(R.string.api_enable), apiOn) { apiOn = it }
                if (apiOn) {
                    CheckRow(UiText.text(R.string.api_allow_control), apiControl) { apiControl = it }
                    CheckRow(UiText.text(R.string.api_allow_beacon), apiBeacon) { apiBeacon = it }
                }
                Text(UiText.text(R.string.api_notice), color = Pal.muted, fontSize = 12.sp)

                // ----- Diagnóstico -----
                SectionTitle(UiText.text(R.string.diagnostics))
                CheckRow(UiText.text(R.string.monitor_enable), monitorOn) { monitorOn = it }

                // ----- Dados locais -----
                SectionTitle(UiText.text(R.string.local_data_section))
                OutlinedButton(onClick = { clearTarget = 1; clearResult = null },
                    enabled = !clearBusy, modifier = Modifier.fillMaxWidth()) {
                    Text(UiText.text(R.string.clear_all_stations), color = Pal.orange)
                }
                clearResult?.let { Text(it, color = Pal.muted, fontSize = 13.sp) }

                // ----- Backup (#50) -----
                SectionTitle(UiText.text(R.string.backup_section))
                CheckRow(UiText.text(R.string.backup_include_messages), backupIncludeMessages) { backupIncludeMessages = it }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(onClick = { backupMessage = null; backupExporter.launch("pt2vhf-aprs-config.json") }) {
                        Text(UiText.text(R.string.backup_export))
                    }
                    OutlinedButton(onClick = { backupMessage = null; backupImporter.launch(arrayOf("application/json", "text/plain", "*/*")) }) {
                        Text(UiText.text(R.string.backup_import))
                    }
                }
                Text(UiText.text(R.string.backup_passcode_note), color = Pal.muted, fontSize = 12.sp)
                backupMessage?.let { Text(it, color = Pal.green, fontSize = 13.sp) }
                }

                SettingsTab.ABOUT -> {
                // ----- Sobre (#79; antes no fim da aba Sistema, #38) -----
                SectionTitle(UiText.text(R.string.about_section))
                Text(UiText.text(R.string.app_name), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(UiText.text(R.string.about_version, BuildConfig.VERSION_NAME), fontSize = 14.sp)
                UpdateStatusSection()
                OutlinedButton(onClick = { sendLog(context, build(force = true) ?: current) }) { Text(UiText.text(R.string.send_log)) }
                Text(UiText.text(R.string.about_site), color = Pal.muted, fontSize = 12.sp)
                Text(UpdateCheck.SITE.removePrefix("https://").removePrefix("www."), color = Pal.accent, fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(UpdateCheck.SITE))) })
                SectionTitle(UiText.text(R.string.about_contributors))
                Text(UiText.text(R.string.about_contributors_thanks), color = Pal.muted, fontSize = 12.sp)
                CONTRIBUTORS.forEach { Text("• $it", fontSize = 15.sp, fontWeight = FontWeight.Bold) }
                }
                }
            }
            }
            // Rodapé fixo: erro de validação (a aba do campo já foi aberta) e Salvar.
            Column(Modifier.fillMaxWidth().background(Pal.bar).padding(horizontal = 16.dp, vertical = 4.dp)) {
                if (error.isNotEmpty()) Text(error, color = Pal.orange, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    if (current.configured) TextButton(onClick = { requestClose() }) { Text(UiText.text(R.string.cancel)) }
                    TextButton(onClick = { trySave() }) { Text(UiText.text(R.string.save), fontWeight = FontWeight.Bold) }
                }
            }
        }
        }
    }
    if (ssidHelp) SsidHelpDialog(callsign = base, profile = profile, current = ssid.toIntOrNull(),
        onPick = { ssid = it.toString(); ssidTouched = true; ssidHelp = false },
        onDismiss = { ssidHelp = false })
    if (confirmClose) AlertDialog(
        onDismissRequest = { confirmClose = false },
        containerColor = Pal.bar,
        title = { Text(UiText.text(R.string.settings_unsaved_title)) },
        text = { Text(UiText.text(R.string.settings_unsaved_body)) },
        confirmButton = { TextButton(onClick = { confirmClose = false; trySave() }) {
            Text(UiText.text(R.string.save), fontWeight = FontWeight.Bold) } },
        dismissButton = { TextButton(onClick = { confirmClose = false; onDismiss() }) {
            Text(UiText.text(R.string.settings_discard), color = Pal.orange) } },
    )
    if (symbolPicker) SymbolPicker(symbol, onPick = { symbol = it; symbolPicker = false }, onDismiss = { symbolPicker = false })
    if (deleteMessagesOpen) DeleteMessagesDialog(onDismiss = { deleteMessagesOpen = false }, onResult = { clearResult = it })
    importPreview?.let { preview ->
        AlertDialog(
            onDismissRequest = { importPreview = null },
            containerColor = Pal.bar,
            title = { Text(UiText.text(R.string.backup_confirm_title)) },
            text = {
                Column {
                    if (preview.fromAprsDroid)
                        Text(UiText.text(R.string.aprsdroid_import_body, preview.callsign.ifBlank { "—" }, preview.keys, preview.ignored))
                    else Text(UiText.text(R.string.backup_confirm_body, preview.callsign.ifBlank { "—" }, preview.keys,
                        preview.appVersion.ifBlank { "?" }, preview.messages))
                    // #59/#50: indicativo do arquivo também passa pela validação; inválido não conecta.
                    if (preview.callsign.isNotBlank() && !plausibleAmateurCallsign(preview.callsign))
                        Text(UiText.text(R.string.saved_call_invalid, preview.callsign), color = Pal.orange, fontSize = 12.sp)
                    if (preview.messages > 0) CheckRow(UiText.text(R.string.backup_import_messages), importMessages) { importMessages = it }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val count = runCatching { com.pt2vhf.aprs.data.SettingsBackup.apply(context, preview, importMessages) }
                    importPreview = null
                    if (count.isSuccess) {
                        backupMessage = UiText.text(R.string.backup_import_done, count.getOrDefault(0))
                        val loaded = Settings.load(context)
                        // Reload the form from what was imported; Save still has to be tapped to apply to the service.
                        onSave(loaded)
                    } else backupMessage = UiText.text(R.string.backup_invalid)
                }) { Text(UiText.text(R.string.backup_import)) }
            },
            dismissButton = { TextButton(onClick = { importPreview = null }) { Text(UiText.text(R.string.cancel)) } },
        )
    }
    clearTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { if (!clearBusy) clearTarget = null },
            containerColor = Pal.bar,
            title = { Text(UiText.text(if (target == 1) R.string.clear_all_stations else R.string.clear_all_messages)) },
            text = { Text(UiText.text(when {
                clearBusy -> R.string.clear_local_data_busy
                target == 1 -> R.string.clear_stations_confirmation
                else -> R.string.clear_messages_confirmation
            })) },
            confirmButton = {
                TextButton(onClick = {
                    if (!clearBusy) {
                        clearBusy = true
                        clearScope.launch {
                            val succeeded = try {
                                if (target == 1) AprsRepository.clearStations() else AprsRepository.clearMessages()
                            } catch (_: Exception) { false }
                            clearBusy = false
                            clearTarget = null
                            clearResult = UiText.text(if (succeeded) R.string.clear_local_data_success
                                else R.string.clear_local_data_error)
                        }
                    }
                }, enabled = !clearBusy) {
                    Text(UiText.text(R.string.clear_data_confirm), color = Pal.orange)
                }
            },
            dismissButton = {
                TextButton(onClick = { clearTarget = null }, enabled = !clearBusy) {
                    Text(UiText.text(R.string.cancel))
                }
            },
        )
    }
}

/** #74: abas da tela de configurações, na ordem exibida. */
internal enum class SettingsTab {
    STATION, APRS_IS, BEACON, POSITION, MAP, MESSAGES, SYSTEM, ABOUT;

    fun label(): String = when (this) {
        STATION -> UiText.text(R.string.station_section)
        APRS_IS -> "APRS-IS"
        BEACON -> UiText.text(R.string.settings_tab_beacon)
        POSITION -> UiText.text(R.string.position_section)
        MAP -> UiText.text(R.string.nav_map)
        MESSAGES -> UiText.text(R.string.nav_messages)
        SYSTEM -> UiText.text(R.string.settings_tab_system)
        ABOUT -> UiText.text(R.string.about_section)
    }
}

/** #79: radioamadores que ajudaram a melhorar o app (indicativos não se traduzem). Ampliar só aqui. */
internal val CONTRIBUTORS = listOf("PU2PYC", "PP5AU", "PU2MUS")

private const val SSID_HELP_SEEN = "ssid_help_seen"

/** #82: "Qual SSID usar?" com as recomendações de WB4APR; tocar numa linha preenche o SSID. Nada é transmitido. */
@Composable
private fun SsidHelpDialog(callsign: String, profile: String, current: Int?, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var dontShow by remember { mutableStateOf(true) }
    val suggested = com.pt2vhf.aprs.data.SsidAdvice.suggestedFor(profile)
    fun close(action: () -> Unit) {
        if (dontShow) Settings.setFlag(context, SSID_HELP_SEEN, true)
        action()
    }
    AlertDialog(
        onDismissRequest = { close(onDismiss) },
        containerColor = Pal.bar,
        title = { Text(UiText.text(R.string.ssid_help_title)) },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(UiText.text(R.string.ssid_help_intro), color = Pal.muted, fontSize = 12.sp)
                com.pt2vhf.aprs.data.SsidAdvice.VALUES.forEach { n ->
                    val mark = n == suggested
                    Row(Modifier.fillMaxWidth()
                        .background(if (n == current) Pal.mine else if (mark) Pal.card else Pal.bar, RoundedCornerShape(6.dp))
                        .clickable { close { onPick(n) } }.padding(horizontal = 6.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(if (n == 0) "—" else "-$n", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            modifier = Modifier.padding(end = 8.dp).size(width = 32.dp, height = 20.dp))
                        Column(Modifier.weight(1f)) {
                            Text(UiText.text(SSID_USES[n]), fontSize = 13.sp)
                            Text(com.pt2vhf.aprs.data.SsidAdvice.fullCall(callsign.ifBlank { "PY2XYZ" }, n) +
                                (if (mark) "  ·  " + UiText.text(R.string.ssid_help_profile, profileLabel(profile)) else ""),
                                color = if (mark) Pal.green else Pal.muted, fontSize = 11.sp)
                        }
                    }
                }
                Text(UiText.text(R.string.ssid_help_notes), color = Pal.muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                CheckRow(UiText.text(R.string.ssid_help_dont_show), dontShow) { dontShow = it }
                Text(UiText.text(R.string.ssid_help_source), color = Pal.muted, fontSize = 11.sp)
            }
        },
        confirmButton = { TextButton(onClick = { close(onDismiss) }) { Text(UiText.text(R.string.close)) } },
    )
}

private val SSID_USES = listOf(R.string.ssid_use_0, R.string.ssid_use_1, R.string.ssid_use_2, R.string.ssid_use_3,
    R.string.ssid_use_4, R.string.ssid_use_5, R.string.ssid_use_6, R.string.ssid_use_7, R.string.ssid_use_8,
    R.string.ssid_use_9, R.string.ssid_use_10, R.string.ssid_use_11, R.string.ssid_use_12, R.string.ssid_use_13,
    R.string.ssid_use_14, R.string.ssid_use_15)

/** #74: última aba aberta, lembrada enquanto o app está em memória. */
internal object SettingsTabMemory { var last: SettingsTab = SettingsTab.STATION }

/** #59: texto curto do resultado da validação do indicativo. */
private fun callsignHint(c: com.pt2vhf.aprs.core.CallsignCheck): String = when {
    c.ok -> UiText.text(R.string.call_country, (com.pt2vhf.aprs.core.flagEmoji(c.country) + " " +
        (com.pt2vhf.aprs.core.countryName(c.country, UiText.locale()) ?: "?")).trim())
    c.problem == com.pt2vhf.aprs.core.CallsignProblem.PREFIX -> UiText.text(R.string.call_bad_prefix, c.prefix ?: "?")
    c.problem == com.pt2vhf.aprs.core.CallsignProblem.SSID -> UiText.text(R.string.call_bad_ssid)
    else -> UiText.text(R.string.call_bad_format)
}
