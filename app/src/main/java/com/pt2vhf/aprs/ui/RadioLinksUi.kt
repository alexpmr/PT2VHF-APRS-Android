package com.pt2vhf.aprs.ui

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.core.BleKiss
import com.pt2vhf.aprs.core.CatPtt
import com.pt2vhf.aprs.core.PttMode
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.Settings
import com.pt2vhf.aprs.service.AprsService
import kotlinx.coroutines.delay

// ---------- #92 TNC KISS por Bluetooth LE ----------

private data class BleFound(val address: String, val name: String, val rssi: Int, val kiss: Boolean)

private val blePermissions: Array<String> = if (Build.VERSION.SDK_INT >= 31)
    arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

@SuppressLint("MissingPermission") // a busca só começa depois de conferir as permissões
@Composable
internal fun BleTncCard(settings: Settings) {
    val context = LocalContext.current
    val status by AprsRepository.status.collectAsState()
    val found = remember { mutableStateListOf<BleFound>() }
    var scanning by remember { mutableStateOf(false) }
    val adapter = remember { runCatching { context.getSystemService(BluetoothManager::class.java)?.adapter }.getOrNull() }
    val callback = remember {
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val address = result.device.address
                val name = result.scanRecord?.deviceName?.takeIf { it.isNotBlank() }
                    ?: runCatching { result.device.name }.getOrNull()?.takeIf { it.isNotBlank() } ?: return // sem nome: não é TNC
                val kiss = BleKiss.advertisesKiss(result.scanRecord?.serviceUuids?.map { it.uuid }.orEmpty())
                val item = BleFound(address, name, result.rssi, kiss)
                val i = found.indexOfFirst { it.address == address }
                if (i >= 0) found[i] = item else found += item
            }
        }
    }
    fun stopScan() {
        if (scanning) runCatching { adapter?.bluetoothLeScanner?.stopScan(callback) }
        scanning = false
    }
    fun hasPermissions() = blePermissions.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }
    fun startScan() {
        val scanner = adapter?.takeIf { it.isEnabled }?.bluetoothLeScanner ?: return
        found.clear()
        scanning = runCatching {
            scanner.startScan(null, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), callback)
        }.isSuccess
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.all { it }) startScan()
    }
    LaunchedEffect(scanning) { if (scanning) { delay(12_000); stopScan() } }
    DisposableEffect(Unit) { onDispose { stopScan() } }

    SectionCard(UiText.text(R.string.ble_card)) {
        Text(UiText.text(R.string.ble_hint), color = Pal.muted, fontSize = 12.sp)
        if (Build.VERSION.SDK_INT < 31) Text(UiText.text(R.string.ble_hint_location), color = Pal.muted, fontSize = 12.sp)
        OutlinedButton(enabled = !scanning, onClick = {
            if (hasPermissions()) startScan() else permissionLauncher.launch(blePermissions)
        }) { Text(UiText.text(if (scanning) R.string.ble_scanning else R.string.ble_scan)) }
        val list = found.sortedWith(compareByDescending<BleFound> { it.kiss }.thenByDescending { it.rssi })
        if (!scanning && list.isEmpty() && settings.bleAddress.isBlank())
            Text(UiText.text(R.string.ble_none), color = Pal.muted, fontSize = 13.sp)
        // Aparelho já escolhido continua na lista mesmo sem nova busca.
        if (settings.bleAddress.isNotBlank() && list.none { it.address == settings.bleAddress })
            BleRow(BleFound(settings.bleAddress, status.tncName.takeIf { status.tncConnected } ?: settings.bleAddress, 0, true),
                settings, status.tncConnected) { stopScan() }
        list.forEach { d -> BleRow(d, settings, status.tncConnected) { stopScan() } }
    }
}

@Composable
private fun BleRow(d: BleFound, settings: Settings, tncConnected: Boolean, beforeConnect: () -> Unit) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(d.name, fontSize = 14.sp)
            Text(listOfNotNull(d.address, d.rssi.takeIf { it != 0 }?.let { "$it dBm" },
                UiText.text(R.string.ble_kiss).takeIf { d.kiss }).joinToString(" · "), color = Pal.muted, fontSize = 11.sp)
        }
        if (tncConnected && settings.bleAddress == d.address) Text(UiText.text(R.string.in_use), color = Pal.green)
        else TextButton(onClick = {
            beforeConnect()
            AprsService.send(context, AprsService.ACTION_BLE_CONNECT, mapOf(AprsService.EXTRA_ADDRESS to d.address))
        }) { Text(UiText.text(R.string.connect)) }
    }
}

// ---------- #93 PTT do modem AFSK por RTS/DTR ou CAT ----------

private fun catLabel(protocol: String) = when (protocol) {
    CatPtt.KENWOOD -> "Kenwood/Elecraft"
    CatPtt.YAESU -> "Yaesu FT-991/891/DX"
    CatPtt.YAESU_OLD -> "Yaesu FT-817/857/897"
    else -> "Icom CI-V"
}

@Composable
internal fun PttCard(settings: Settings, onChange: (Settings) -> Unit) {
    SectionCard(UiText.text(R.string.ptt_card)) {
        Text(UiText.text(R.string.ptt_hint), color = Pal.muted, fontSize = 12.sp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PttMode.ALL.forEach { m ->
                OutlinedChoice(m.uppercase(), settings.pttMode == m) { if (settings.pttMode != m) onChange(settings.copy(pttMode = m)) }
            }
        }
        if (settings.pttMode != PttMode.VOX) PttDetails(settings, onChange)
    }
}

@Composable
private fun PttDetails(settings: Settings, onChange: (Settings) -> Unit) {
    val context = LocalContext.current
    val status by AprsRepository.status.collectAsState()
    var civ by remember(settings.civAddress) { mutableStateOf("%02X".format(settings.civAddress)) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(when {
            status.pttReady -> UiText.text(R.string.ptt_ready, status.pttName)
            else -> UiText.connectionError(status.pttError).ifBlank { "—" }
        }, color = if (status.pttReady) Pal.green else Pal.muted, fontSize = 13.sp)
        Text(UiText.text(R.string.usb_baud), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Settings.USB_BAUDS.forEach { b -> OutlinedChoice(b.toString(), settings.pttBaud == b) { onChange(settings.copy(pttBaud = b)) } }
        }
        if (settings.pttMode == PttMode.CAT) {
            Text(UiText.text(R.string.ptt_cat_protocol), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                CatPtt.PROTOCOLS.forEach { p ->
                    OutlinedChoice(catLabel(p), settings.catProtocol == p) { onChange(settings.copy(catProtocol = p)) }
                }
            }
            if (settings.catProtocol == CatPtt.ICOM) Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(civ, { civ = it.take(4) }, singleLine = true, modifier = Modifier.weight(1f),
                    isError = CatPtt.parseCivAddress(civ) == null,
                    label = { Text(UiText.text(R.string.ptt_civ_address)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters))
                TextButton(enabled = CatPtt.parseCivAddress(civ).let { it != null && it != settings.civAddress }, onClick = {
                    CatPtt.parseCivAddress(civ)?.let { onChange(settings.copy(civAddress = it)) }
                }) { Text(UiText.text(R.string.save)) }
            }
        }
        OutlinedButton(onClick = { AprsService.send(context, AprsService.ACTION_BT_RESUME) }) { Text(UiText.text(R.string.connect)) }
    }
}
