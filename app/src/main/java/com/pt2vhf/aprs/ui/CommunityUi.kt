package com.pt2vhf.aprs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.core.countryName
import com.pt2vhf.aprs.core.flagEmoji
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.DEFAULT_NETS
import com.pt2vhf.aprs.data.NetDefinition
import com.pt2vhf.aprs.data.OutboundSendResult
import com.pt2vhf.aprs.data.Settings
import com.pt2vhf.aprs.data.latestNetDate
import com.pt2vhf.aprs.data.netSummary
import com.pt2vhf.aprs.data.parseNets
import com.pt2vhf.aprs.data.serializeNets
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.UUID

private const val NETS_KEY = "community_nets"

private fun loadNets(context: android.content.Context): List<NetDefinition> =
    parseNets(Settings.readOption(context, NETS_KEY, "")).ifEmpty { DEFAULT_NETS }

/**
 * #58.5 Comunidade: rodadas de check-in por mensagem APRS. Participantes e países vêm só do que este app
 * ouviu (mensagens recebidas e log de pacotes). O check-in só transmite depois da confirmação.
 */
@Composable
fun CommunityScreen(settings: Settings, onStation: (String) -> Unit) {
    val context = LocalContext.current
    var nets by remember { mutableStateOf(loadNets(context)) }
    var selected by rememberSaveable { mutableStateOf(0) }
    val net = nets.getOrElse(selected) { nets.first() }
    var date by remember(net) { mutableStateOf(latestNetDate(net, LocalDate.now(net.zone()))) }
    var query by rememberSaveable { mutableStateOf("") }
    var netMenu by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<NetDefinition?>(null) }
    var checkIn by remember { mutableStateOf<String?>(null) }
    var expandedCountry by remember { mutableStateOf<String?>("") }
    val messages by AprsRepository.messages.collectAsState()
    val packets by AprsRepository.packetLog.collectAsState()
    val summary = remember(net, date, messages, packets, query) { netSummary(net, date, messages, packets, query) }
    val locale = UiText.locale()
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    TextButton(onClick = { netMenu = true }) {
                        Text("▾ " + net.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    DropdownMenu(expanded = netMenu, onDismissRequest = { netMenu = false }) {
                        nets.forEachIndexed { i, n ->
                            DropdownMenuItem(text = { Text(n.name) }, onClick = { selected = i; netMenu = false })
                        }
                        DropdownMenuItem(text = { Text("+ " + UiText.text(R.string.net_add)) }, onClick = {
                            netMenu = false
                            editing = NetDefinition("", "CQ", "", LocalDate.now().dayOfWeek.value, 20, 2, false, "")
                        })
                    }
                }
                TextButton(onClick = { editing = net }) { Text(UiText.text(R.string.net_edit), fontSize = 13.sp) }
            }
            val day = DayOfWeek.of(net.weekday).getDisplayName(TextStyle.FULL, locale)
            Text(UiText.text(R.string.net_schedule, day, "%02d:00".format(net.startHour), net.hours,
                if (net.utc) "UTC" else UiText.text(R.string.net_local_time), net.destination,
                net.keyword.ifBlank { "—" }), color = Pal.muted, fontSize = 12.sp)
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { date = date.minusWeeks(1) }) { Text("‹") }
                Text(date.format(java.time.format.DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)),
                    modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Pal.text,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                TextButton(onClick = { date = date.plusWeeks(1) },
                    enabled = date.plusWeeks(1) <= LocalDate.now(net.zone())) { Text("›") }
            }
            OutlinedTextField(query, { query = it.uppercase() }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(UiText.text(R.string.search_callsign_short)) })
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard(UiText.text(R.string.net_participants), summary.checkIns.size.toString(), Modifier.weight(1f))
                StatCard(UiText.text(R.string.net_countries), summary.byCountry.count { it.first != null }.toString(), Modifier.weight(1f))
            }
            Text(UiText.text(R.string.net_source_notice), color = Pal.muted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
            TextButton(enabled = settings.configured && !settings.rxOnly, onClick = {
                checkIn = net.checkInText.ifBlank { listOf(net.keyword, settings.fullCall).filter { it.isNotBlank() }.joinToString(" ") }
            }) { Text("✓ " + UiText.text(R.string.net_check_in)) }
        }
        if (summary.checkIns.isEmpty()) item { Text(UiText.text(R.string.net_no_check_ins), color = Pal.muted) }
        items(summary.byCountry, key = { it.first ?: "?" }) { (iso, count) ->
            val open = expandedCountry == (iso ?: "?")
            Column(Modifier.fillMaxWidth().background(Pal.bar, RoundedCornerShape(10.dp))
                .clickable { expandedCountry = if (open) "" else (iso ?: "?") }.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(flagEmoji(iso) + " " + (countryName(iso, locale) ?: UiText.text(R.string.net_country_unknown)),
                        modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    Text(count.toString(), color = Pal.accent, fontWeight = FontWeight.Bold)
                }
                if (open) summary.checkIns.filter { it.country == iso }.forEach { c ->
                    Row(Modifier.fillMaxWidth().clickable { onStation(c.call) }.padding(vertical = 4.dp)) {
                        Text(c.call, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                        Text(hhmm(c.time) + " · " + c.text, color = Pal.muted, fontSize = 12.sp, maxLines = 1,
                            overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
    editing?.let { n ->
        NetEditDialog(n, canDelete = nets.contains(n) && nets.size > 1, onDismiss = { editing = null }, onDelete = {
            nets = nets - n; selected = 0
            Settings.saveOption(context, NETS_KEY, serializeNets(nets)); editing = null
        }) { updated ->
            val index = nets.indexOf(n)
            nets = if (index >= 0) nets.toMutableList().also { it[index] = updated } else nets + updated
            selected = nets.indexOf(updated).coerceAtLeast(0)
            Settings.saveOption(context, NETS_KEY, serializeNets(nets)); editing = null
        }
    }
    checkIn?.let { initial ->
        CheckInDialog(net, initial, onDismiss = { checkIn = null }) { text ->
            val id = UUID.randomUUID().toString()
            dispatchMessage(context, net.destination, text, id, null)
            checkIn = null
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Column(modifier.background(Pal.card, RoundedCornerShape(10.dp)).padding(10.dp)) {
        Text(label.uppercase(), color = Pal.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Pal.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

/** Confirmação antes de transmitir o check-in (texto editável; o destino é o da rodada). */
@Composable
private fun CheckInDialog(net: NetDefinition, initial: String, onDismiss: () -> Unit, onSend: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    val results by AprsRepository.sendResults.collectAsState()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pal.bar,
        title = { Text(UiText.text(R.string.net_check_in)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(UiText.text(R.string.message_destination, net.destination), color = Pal.muted, fontSize = 12.sp)
                OutlinedTextField(text, { text = it.take(67) }, modifier = Modifier.fillMaxWidth())
                Text(UiText.text(R.string.net_check_in_confirm), color = Pal.muted, fontSize = 12.sp)
            }
        },
        confirmButton = { TextButton(enabled = text.isNotBlank(), onClick = { onSend(text.trim()) }) { Text(UiText.text(R.string.send)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiText.text(R.string.cancel)) } },
    )
}

@Composable
private fun NetEditDialog(
    initial: NetDefinition,
    canDelete: Boolean,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSave: (NetDefinition) -> Unit,
) {
    var name by remember { mutableStateOf(initial.name) }
    var dest by remember { mutableStateOf(initial.destination) }
    var keyword by remember { mutableStateOf(initial.keyword) }
    var weekday by remember { mutableStateOf(initial.weekday) }
    var start by remember { mutableStateOf(initial.startHour.toString()) }
    var hours by remember { mutableStateOf(initial.hours.toString()) }
    var utc by remember { mutableStateOf(initial.utc) }
    var text by remember { mutableStateOf(initial.checkInText) }
    val locale = UiText.locale()
    val valid = name.isNotBlank() && dest.trim().length in 1..9 && start.toIntOrNull() in 0..23 && hours.toIntOrNull() in 1..168
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pal.bar,
        title = { Text(UiText.text(if (initial.name.isBlank()) R.string.net_add else R.string.net_edit)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(name, { name = it }, singleLine = true, label = { Text(UiText.text(R.string.net_name)) })
                OutlinedTextField(dest, { dest = it.uppercase().take(9) }, singleLine = true,
                    label = { Text(UiText.text(R.string.net_destination)) })
                OutlinedTextField(keyword, { keyword = it.uppercase() }, singleLine = true,
                    label = { Text(UiText.text(R.string.net_keyword)) })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    DayOfWeek.values().forEach { d ->
                        OutlinedChoice(d.getDisplayName(TextStyle.NARROW, locale), weekday == d.value) { weekday = d.value }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(start, { start = it.filter(Char::isDigit).take(2) }, singleLine = true,
                        label = { Text(UiText.text(R.string.net_start_hour)) }, modifier = Modifier.weight(1f))
                    OutlinedTextField(hours, { hours = it.filter(Char::isDigit).take(3) }, singleLine = true,
                        label = { Text(UiText.text(R.string.net_hours)) }, modifier = Modifier.weight(1f))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(utc, { utc = it })
                    Text("UTC", modifier = Modifier.clickable { utc = !utc })
                }
                OutlinedTextField(text, { text = it.take(67) }, label = { Text(UiText.text(R.string.net_check_in_text)) })
            }
        },
        confirmButton = {
            TextButton(enabled = valid, onClick = {
                onSave(NetDefinition(name.trim(), dest.trim(), keyword.trim(), weekday, start.toInt(), hours.toInt(), utc, text.trim()))
            }) { Text(UiText.text(R.string.save)) }
        },
        dismissButton = {
            Row {
                if (canDelete) TextButton(onClick = onDelete) { Text(UiText.text(R.string.net_delete)) }
                TextButton(onClick = onDismiss) { Text(UiText.text(R.string.cancel)) }
            }
        },
    )
}
