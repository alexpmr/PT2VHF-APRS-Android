package com.pt2vhf.aprs.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.List
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.Settings
import com.pt2vhf.aprs.data.UpdateCheck
import com.pt2vhf.aprs.data.UpdateInfo
import com.pt2vhf.aprs.service.AprsService
import kotlinx.coroutines.Dispatchers
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

enum class Tab(val labelId: Int) { MAP(R.string.nav_map), MESSAGES(R.string.nav_messages), STATIONS(R.string.nav_stations), RADIO(R.string.nav_radio), TRACKER(R.string.nav_tracker) }

@Composable
fun App() {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(Settings.load(context)) }
    var tab by rememberSaveable { mutableStateOf(Tab.MAP) }
    var showSettings by rememberSaveable { mutableStateOf(!settings.configured) }
    var selectedStation by rememberSaveable { mutableStateOf<String?>(null) }
    var chatWith by rememberSaveable { mutableStateOf<String?>(null) }
    var update by remember { mutableStateOf<UpdateInfo?>(null) }
    var siteNotice by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) { update = withContext(Dispatchers.IO) { runCatching { UpdateCheck.fetch() }.getOrNull() } }

    MaterialTheme(colorScheme = scheme) {
        Scaffold(
            containerColor = Pal.ground,
            topBar = { TopBar(settings, onSettings = { showSettings = true }) },
            bottomBar = {
                NavigationBar(containerColor = Pal.bar) {
                    Tab.entries.forEach { t ->
                        NavigationBarItem(
                            selected = tab == t,
                            onClick = { tab = t; if (t != Tab.MESSAGES) chatWith = null },
                            icon = {
                                Icon(when (t) {
                                    Tab.MAP -> Icons.Filled.Place
                                    Tab.MESSAGES -> Icons.Filled.Email
                                    Tab.STATIONS -> Icons.Filled.List
                                    Tab.RADIO -> Icons.Filled.Share
                                    Tab.TRACKER -> Icons.Filled.LocationOn
                                }, contentDescription = null)
                            },
                            label = { Text(UiText.text(t.labelId), fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Pal.accent, selectedTextColor = Pal.accent,
                                unselectedIconColor = Pal.muted, unselectedTextColor = Pal.muted, indicatorColor = Pal.card,
                            ),
                        )
                    }
                }
            },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (tab) {
                    Tab.MAP -> MapScreen(onStation = { selectedStation = it })
                    Tab.MESSAGES -> MessagesScreen(settings, chatWith, onOpenChat = { chatWith = it }, onStation = { selectedStation = it })
                    Tab.STATIONS -> StationsScreen(onStation = { selectedStation = it })
                    Tab.RADIO -> RadioScreen(settings)
                    Tab.TRACKER -> TrackerScreen(settings)
                }
            }
        }

        selectedStation?.let { call ->
            StationDialog(call, onDismiss = { selectedStation = null }, onMessage = {
                selectedStation = null; chatWith = call; tab = Tab.MESSAGES
            })
        }
        if (showSettings) {
            SettingsDialog(settings, onDismiss = { if (settings.configured) showSettings = false }, onSave = {
                Settings.save(context, it); settings = Settings.load(context); showSettings = false
                AprsService.send(context, AprsService.ACTION_RELOAD)
            })
        }
        val pending = update
        if (!showSettings && siteNotice) {
            OfficialSiteNotice(onClose = { siteNotice = false })
        } else if (!showSettings && pending != null) {
            AlertDialog(
                onDismissRequest = { update = null },
                containerColor = Pal.bar,
                title = { Text(UiText.text(R.string.new_version, pending.version)) },
                text = { Text(pending.notes.ifBlank { UiText.text(R.string.new_version_notice) }) },
                confirmButton = {
                    TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(pending.downloadUrl)))
                        update = null
                    }) { Text(UiText.text(R.string.download_install)) }
                },
                dismissButton = { TextButton(onClick = { update = null }) { Text(UiText.text(R.string.later)) } },
            )
        }
    }
}

@Composable
private fun TopBar(settings: Settings, onSettings: () -> Unit) {
    val context = LocalContext.current
    val status by AprsRepository.status.collectAsState()
    Column(
        Modifier.fillMaxWidth().background(Pal.bar).statusBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (settings.configured) settings.fullCall else UiText.text(R.string.configure_callsign),
                    fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Pal.text)
                Text("PT2VHF APRS Client for Android", fontSize = 12.sp, color = Pal.muted)
            }
            OutlinedButton(
                onClick = { AprsService.send(context, AprsService.ACTION_BEACON) },
                enabled = settings.configured && status.lat != null,
                border = androidx.compose.foundation.BorderStroke(2.dp, Pal.yellow),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Pal.yellow),
            ) { Text(UiText.text(R.string.send_beacon), fontWeight = FontWeight.SemiBold) }
            IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = UiText.text(R.string.settings), tint = Pal.text) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("APRS-IS", status.aprsIsVerified)
            Chip("TNC", status.tncConnected)
            Chip("GPS", status.gpsAccuracyM != null &&
                status.gpsFixTimeMs > 0 && System.currentTimeMillis() - status.gpsFixTimeMs <= 120_000L)
        }
    }
}

@Composable
fun Chip(text: String, ok: Boolean) {
    Row(
        Modifier.background(if (ok) Pal.greenBg else Color(0xFF3A1A23), RoundedCornerShape(14.dp))
            .semantics { contentDescription = "$text: ${if (ok) "connected" else "disconnected"}" }
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).background(if (ok) Color(0xFF3CCF7C) else Color(0xFFF26F70), CircleShape))
        Spacer(Modifier.size(6.dp))
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (ok) Pal.green else Color(0xFFF26F70))
    }
}

/** Mesmo aviso dos aplicativos de desktop, uma vez por abertura. */
@Composable
private fun OfficialSiteNotice(onClose: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = Color(0xFF2A2C33),
        title = { Text(UiText.text(R.string.official_downloads), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(UiText.text(R.string.official_notice))
                Text("www.PT2VHF.com", color = Color(0xFF72D2FF), fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.padding(vertical = 4.dp)
                        .clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(UpdateCheck.SITE))) })
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onClose, border = androidx.compose.foundation.BorderStroke(2.dp, Pal.yellow)) {
                Text(UiText.text(R.string.understood), color = Pal.text)
            }
        },
    )
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
