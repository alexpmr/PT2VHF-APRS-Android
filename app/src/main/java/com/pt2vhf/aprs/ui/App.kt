package com.pt2vhf.aprs.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.pt2vhf.aprs.MainActivity
import com.pt2vhf.aprs.data.CrashLog
import com.pt2vhf.aprs.data.UpdateManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt2vhf.aprs.BuildConfig
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.Settings
import com.pt2vhf.aprs.data.UpdateCheck
import com.pt2vhf.aprs.data.UpdateInfo
import com.pt2vhf.aprs.service.AprsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import java.util.UUID
import kotlinx.coroutines.withContext

object Pal {
    val ground = Color(0xFF0B131C)
    val bar = Color(0xFF111C28)
    val line = Color(0xFF22303F)
    val card = Color(0xFF1B2633)
    val text = Color(0xFFE6EDF5)
    val muted = Color(0xFF9FB1C4)
    val accent = Color(0xFF7CC4FF)
    val blue = Color(0xFF2F86D6)
    val yellow = Color(0xFFF5C400)
    val green = Color(0xFF7EE2A8)
    val greenBg = Color(0xFF10301F)
    val orange = Color(0xFFFFB877)
    val mine = Color(0xFF1E3A55)
}

private val scheme = darkColorScheme(
    primary = Pal.blue, onPrimary = Color.White, background = Pal.ground, surface = Pal.bar,
    onBackground = Pal.text, onSurface = Pal.text, surfaceVariant = Pal.card, onSurfaceVariant = Pal.muted,
    secondary = Pal.yellow,
)

enum class Tab(val labelId: Int) { MAP(R.string.nav_map), MESSAGES(R.string.nav_messages), STATIONS(R.string.nav_stations), RADIO(R.string.nav_radio), TRACKER(R.string.nav_tracker), LOGS(R.string.nav_logs) }

@Composable
fun App(onExit: () -> Unit) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(Settings.load(context)) }
    var tab by rememberSaveable { mutableStateOf(Tab.MAP) }
    var showSettings by rememberSaveable { mutableStateOf(!settings.configured) }
    var selectedStation by rememberSaveable { mutableStateOf<String?>(null) }
    var chatWith by rememberSaveable { mutableStateOf<String?>(null) }
    var updateDismissed by rememberSaveable { mutableStateOf<String?>(null) }
    var forceUpdateDialog by remember { mutableStateOf(false) }
    val updateState by UpdateManager.state.collectAsState()
    val showUpdateRequests by MainActivity.showUpdateRequests.collectAsState()
    var crashText by remember { mutableStateOf<String?>(null) }
    var carMode by rememberSaveable { mutableStateOf(false) }
    var replyToId by rememberSaveable { mutableStateOf<Long?>(null) }
    var exitDialog by rememberSaveable { mutableStateOf(false) }
    val serviceStatus by AprsRepository.status.collectAsState()
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    BackHandler {
        when {
            selectedStation != null -> selectedStation = null
            chatWith != null -> chatWith = null
            showSettings && settings.configured -> showSettings = false
            carMode -> carMode = false
            else -> exitDialog = true
        }
    }

    // #39: verificação na abertura/retorno ao app (no máximo 1 vez por hora); o serviço verifica a cada 12 h.
    LifecycleResumeEffect(Unit) {
        UpdateManager.current(context)
        UpdateManager.maybeCheck(context, UpdateManager.RESUME_MS)
        onPauseOrDispose { }
    }
    LaunchedEffect(showUpdateRequests) { if (showUpdateRequests > 0) forceUpdateDialog = true }
    // #45: falha anterior registrada → oferecer o envio do log.
    LaunchedEffect(Unit) { crashText = withContext(Dispatchers.IO) { CrashLog.pending(context) } }
    // #6: manter a tela ligada (opcional), só enquanto o app está visível.
    val view = LocalView.current
    DisposableEffect(settings.keepScreenOn) {
        view.keepScreenOn = settings.keepScreenOn
        onDispose { view.keepScreenOn = false }
    }

    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            containerColor = Pal.ground,
            topBar = { if (!keyboardVisible) TopBar(settings, onSettings = { showSettings = true }, onRadio = { tab = Tab.RADIO },
                updateAvailable = updateState.available != null, onUpdate = { forceUpdateDialog = true }) },
            bottomBar = {
                if (!keyboardVisible) NavigationBar(containerColor = Pal.bar) {
                    Tab.entries.forEach { t ->
                        NavigationBarItem(
                            selected = tab == t,
                            onClick = { tab = t; if (t != Tab.MESSAGES) { chatWith = null; replyToId = null } },
                            icon = {
                                Icon(when (t) {
                                    Tab.MAP -> Icons.Filled.Place
                                    Tab.MESSAGES -> Icons.Filled.Email
                                    Tab.STATIONS -> Icons.Filled.List
                                    Tab.RADIO -> Icons.Filled.Share
                                    Tab.TRACKER -> Icons.Filled.LocationOn
                                    Tab.LOGS -> Icons.Filled.Info
                                }, contentDescription = null)
                            },
                            label = { Text(UiText.text(t.labelId), fontSize = 10.sp, maxLines = 1, softWrap = false) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Pal.accent, selectedTextColor = Pal.accent,
                                unselectedIconColor = Pal.muted, unselectedTextColor = Pal.muted, indicatorColor = Pal.card,
                            ),
                        )
                    }
                }
            },
        ) { padding ->
            // #56: o teclado empurra só o conteúdo (imePadding), uma vez, depois das barras do sistema.
            Box(Modifier.padding(padding).consumeWindowInsets(padding).imePadding().fillMaxSize()) {
                when (tab) {
                    Tab.MAP -> MapScreen(settings, onStation = { selectedStation = it }, onSettingsChanged = { settings = it })
                    Tab.MESSAGES -> MessagesScreen(settings, chatWith, replyToId,
                        onOpenChat = { call, reply -> chatWith = call; replyToId = reply },
                        onStation = { selectedStation = it })
                    Tab.STATIONS -> StationsScreen(settings.stationMaxAgeHours, onStation = { selectedStation = it })
                    Tab.RADIO -> RadioScreen(settings, onSettingsChanged = { settings = it })
                    Tab.TRACKER -> TrackerScreen(settings, onSettingsChanged = { settings = it }, onCarMode = { carMode = true })
                    Tab.LOGS -> LogsScreen(onStation = { selectedStation = it })
                }
            }
        }

        selectedStation?.let { call ->
            // #58.6: ficha completa em tela cheia (substitui o diálogo simples).
            StationSheet(call, onDismiss = { selectedStation = null }, onMessage = { target ->
                selectedStation = null; chatWith = target; replyToId = null; tab = Tab.MESSAGES
            })
        }
        if (showSettings) {
            SettingsDialog(settings, onDismiss = { if (settings.configured) showSettings = false }, onSave = {
                Settings.save(context, it); settings = Settings.load(context); showSettings = false
                AprsService.send(context, AprsService.ACTION_RELOAD)
            })
        }
        if (exitDialog) {
            AlertDialog(
                onDismissRequest = { exitDialog = false },
                containerColor = Pal.bar,
                title = { Text(UiText.text(R.string.exit_confirm_title)) },
                text = { Text(UiText.text(if (serviceStatus.trackerOn)
                    R.string.exit_tracker_active else R.string.exit_confirm_body)) },
                confirmButton = {
                    TextButton(onClick = {
                        AprsService.send(context, AprsService.ACTION_STOP)
                        exitDialog = false
                        onExit()
                    }) {
                        Text(UiText.text(if (serviceStatus.trackerOn)
                            R.string.exit_stop_tracker else R.string.exit_app))
                    }
                },
                dismissButton = {
                    Row {
                        if (serviceStatus.trackerOn) TextButton(onClick = {
                            exitDialog = false
                            onExit()
                        }) { Text(UiText.text(R.string.exit_keep_tracker)) }
                        TextButton(onClick = { exitDialog = false }) {
                            Text(UiText.text(R.string.cancel))
                        }
                    }
                },
            )
        }
        val available = updateState.available
        if (!showSettings && available != null && (forceUpdateDialog ||
                (updateDismissed != available.version && UpdateManager.ignoredVersion(context) != available.version))) {
            UpdateDialog(available, onDismiss = { updateDismissed = available.version; forceUpdateDialog = false })
        }
        crashText?.let {
            AlertDialog(
                onDismissRequest = { CrashLog.markSeen(context); crashText = null },
                containerColor = Pal.bar,
                title = { Text(UiText.text(R.string.crash_title)) },
                text = { Text(UiText.text(R.string.crash_body)) },
                confirmButton = {
                    TextButton(onClick = { sendLog(context, settings); crashText = null }) { Text(UiText.text(R.string.send_log)) }
                },
                dismissButton = {
                    TextButton(onClick = { CrashLog.markSeen(context); crashText = null }) { Text(UiText.text(R.string.later)) }
                },
            )
        }
        if (carMode) {
            Box(Modifier.fillMaxSize().background(Pal.ground).systemBarsPadding()) { CarModeScreen(settings, onExit = { carMode = false }) }
        }
    }
}

@Composable
private fun TopBar(settings: Settings, onSettings: () -> Unit, onRadio: () -> Unit, updateAvailable: Boolean, onUpdate: () -> Unit) {
    val context = LocalContext.current
    val status by AprsRepository.status.collectAsState()
    val beaconResults by AprsRepository.beaconResults.collectAsState()
    var pendingBeaconId by rememberSaveable { mutableStateOf<String?>(null) }
    var beaconFailed by remember { mutableStateOf(false) }
    LaunchedEffect(beaconResults, pendingBeaconId) {
        val id = pendingBeaconId
        if (id != null) {
            beaconResults[id]?.let {
                pendingBeaconId = null
                beaconFailed = !it.submitted
            }
        }
    }
    LaunchedEffect(pendingBeaconId) {
        val id = pendingBeaconId
        if (id != null) {
            delay(15_000)
            if (pendingBeaconId == id) {
                pendingBeaconId = null
                beaconFailed = true
            }
        }
    }
    // #65: cabeçalho compacto (só o indicativo, sem o nome do app) em duas linhas baixas.
    Column(
        Modifier.fillMaxWidth().background(Pal.bar).statusBarsPadding().padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text(if (settings.configured) settings.fullCall else UiText.text(R.string.configure_callsign),
                    fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Pal.text, maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                if (settings.rxOnly) Text(UiText.text(R.string.profile_rx), color = Pal.orange, fontSize = 11.sp, maxLines = 1,
                    modifier = Modifier.padding(start = 6.dp).border(1.dp, Pal.orange, RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp))
            }
            val beaconColor = when {
                pendingBeaconId != null -> Pal.blue
                beaconFailed -> Color(0xFFF26F70)
                else -> Pal.yellow
            }
            OutlinedButton(
                onClick = {
                    val id = UUID.randomUUID().toString()
                    beaconFailed = false
                    pendingBeaconId = id
                    runCatching {
                        AprsService.send(context, AprsService.ACTION_BEACON,
                            mapOf(AprsService.EXTRA_REQUEST_ID to id))
                    }.onFailure {
                        pendingBeaconId = null
                        beaconFailed = true
                    }
                },
                enabled = settings.configured && !settings.rxOnly && status.lat != null && pendingBeaconId == null,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, beaconColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = beaconColor),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.heightIn(min = 36.dp).height(36.dp),
            ) {
                Text(UiText.text(when {
                    pendingBeaconId != null -> R.string.beacon_sending
                    beaconFailed -> R.string.beacon_failed
                    else -> R.string.send_beacon
                }), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1)
            }
            if (updateAvailable) TextButton(onClick = onUpdate) {
                Text("⬆", color = Pal.yellow, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { contentDescription = UiText.text(R.string.update_badge) })
            }
            IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = UiText.text(R.string.settings), tint = Pal.text) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Chip("APRS-IS", status.aprsIsVerified, onClick = {
                AprsService.send(context, if (status.aprsIsConnected)
                    AprsService.ACTION_APRS_DISCONNECT else AprsService.ACTION_APRS_CONNECT)
            })
            Chip("TNC", status.tncConnected, onClick = {
                when {
                    status.tncConnected -> AprsService.send(context, AprsService.ACTION_BT_PAUSE)
                    settings.tncType != "bt" || settings.btAddress.isNotBlank() -> AprsService.send(context, AprsService.ACTION_BT_RESUME)
                    else -> onRadio()
                }
            })
            Chip("GPS", status.manualPosition || status.gpsAccuracyM != null &&
                status.gpsFixTimeMs > 0 && System.currentTimeMillis() - status.gpsFixTimeMs <= 120_000L)
        }
    }
}

@Composable
fun Chip(text: String, ok: Boolean, onClick: (() -> Unit)? = null) {
    Row(
        Modifier.background(if (ok) Pal.greenBg else Color(0xFF3A1A23), RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics { contentDescription = "$text: ${if (ok) "connected" else "disconnected"}" }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(if (ok) Color(0xFF3CCF7C) else Color(0xFFF26F70), CircleShape))
        Spacer(Modifier.size(6.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (ok) Pal.green else Color(0xFFF26F70))
    }
}

@Composable
fun SectionCard(title: String? = null, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(Pal.bar, RoundedCornerShape(12.dp)).border(1.dp, Pal.line, RoundedCornerShape(12.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (title != null) Text(title.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Pal.muted, letterSpacing = 0.5.sp)
        content()
    }
}

@Composable
fun KeyValue(key: String, value: String) {
    Row(Modifier.fillMaxWidth().height(24.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(key, color = Pal.muted, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, color = Pal.text, fontSize = 14.sp)
    }
}
