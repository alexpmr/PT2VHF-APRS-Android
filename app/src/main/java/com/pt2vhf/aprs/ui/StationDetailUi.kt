package com.pt2vhf.aprs.ui

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.core.AprsWeather
import com.pt2vhf.aprs.core.PathHop
import com.pt2vhf.aprs.core.QMeaning
import com.pt2vhf.aprs.core.bearingDeg
import com.pt2vhf.aprs.core.compassName
import com.pt2vhf.aprs.core.decodeAprsText
import com.pt2vhf.aprs.core.haversineKm
import com.pt2vhf.aprs.core.maidenhead
import com.pt2vhf.aprs.core.qMeaning
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.Station
import com.pt2vhf.aprs.data.StationInsights
import com.pt2vhf.aprs.data.buildStationInsights
import com.pt2vhf.aprs.data.otherSsids
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

private const val LIVE_MS = 30 * 60_000L

/** #58.6: atalhos de ordenação da lista de estações; seta mostra o sentido do critério ativo. */
@Composable
fun StationSortChips(sort: String, descending: Boolean, onTap: (String) -> Unit) {
    val options = listOf(
        "type" to R.string.station_sort_type,
        "callsign" to R.string.station_sort_name,
        "distance" to R.string.station_sort_distance,
        "heard" to R.string.station_sort_recent,
    )
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 10.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { (key, label) ->
            val active = key == sort
            val name = UiText.text(label)
            val direction = UiText.text(if (descending) R.string.station_descending else R.string.station_ascending)
            Box(Modifier.heightIn(min = 36.dp)
                .background(if (active) Pal.mine else Pal.card, RoundedCornerShape(18.dp))
                .border(1.dp, if (active) Pal.accent else Pal.line, RoundedCornerShape(18.dp))
                .clickable { onTap(key) }
                .semantics { contentDescription = if (active) "$name, $direction" else name }
                .padding(horizontal = 12.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text(name + if (active) (if (descending) " ↓" else " ↑") else "",
                    color = if (active) Pal.accent else Pal.text, fontSize = 13.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}

/**
 * Ficha da estação em tela cheia (#58.4/#58.6): visão geral com recepção e outros SSIDs,
 * caminho do último pacote, telemetria/clima com gráficos e pacotes brutos.
 * Tudo vem do histórico local; o que não foi recebido aparece como "não informado".
 */
@Composable
fun StationSheet(initialCall: String, onDismiss: () -> Unit, onMessage: (String) -> Unit) {
    var call by rememberSaveable(initialCall) { mutableStateOf(initialCall) }
    val context = LocalContext.current
    val favorites by AprsRepository.favorites.collectAsState()
    val followed by AprsRepository.followCall.collectAsState()
    val stations by AprsRepository.stations.collectAsState()
    val status by AprsRepository.status.collectAsState()
    val weatherLive by AprsRepository.weatherHistory.collectAsState()
    val packetLog by AprsRepository.packetLog.collectAsState()
    val s: Station? = stations[call]
    var insights by remember(call) { mutableStateOf<StationInsights?>(null) }
    // Recarrega quando chega pacote novo desta estação (atualização automática da ficha).
    val newestId = packetLog.firstOrNull { it.source == call }?.id
    LaunchedEffect(call, newestId) {
        val packets = AprsRepository.stationPackets(call)
        val snapshot = AprsRepository.stations.value
        insights = withContext(Dispatchers.Default) {
            buildStationInsights(call, packets, snapshot, System.currentTimeMillis())
        }
    }
    var tab by rememberSaveable(call) { mutableIntStateOf(0) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = Pal.ground) {
            Column(Modifier.fillMaxSize()) {
                SheetHeader(call, s, onDismiss)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 6.dp)) {
                    TextButton(onClick = { onMessage(call) }) { Text("✉ " + UiText.text(R.string.message)) }
                    TextButton(onClick = { AprsRepository.followStation(if (followed == call) null else call) }) {
                        Text(UiText.text(if (followed == call) R.string.stop_following else R.string.follow_station))
                    }
                    if (s != null) TextButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("geo:${s.lat},${s.lon}?q=${s.lat},${s.lon}($call)")))
                    }) { Text(UiText.text(R.string.navigate)) }
                    TextButton(onClick = { AprsRepository.toggleFavorite(call) }) {
                        Text((if (call in favorites) "★ " else "☆ ") +
                            UiText.text(if (call in favorites) R.string.favorite_remove else R.string.favorite_add))
                    }
                }
                val tabs = listOf(R.string.sheet_tab_overview, R.string.sheet_tab_path,
                    R.string.sheet_tab_telemetry, R.string.sheet_tab_raw)
                ScrollableTabRow(selectedTabIndex = tab, containerColor = Pal.bar, edgePadding = 8.dp) {
                    tabs.forEachIndexed { i, id ->
                        Tab(selected = tab == i, onClick = { tab = i }, text = { Text(UiText.text(id), fontSize = 13.sp) })
                    }
                }
                val data = insights
                Box(Modifier.fillMaxSize()) {
                    if (data == null) Text(UiText.text(R.string.sheet_loading), color = Pal.muted,
                        modifier = Modifier.padding(16.dp))
                    else when (tab) {
                        0 -> OverviewTab(s, data, status.lat, status.lon, stations.values, weatherLive[call].orEmpty()) { call = it }
                        1 -> PathTab(data)
                        2 -> TelemetryTab(data, weatherLive[call].orEmpty())
                        else -> RawTab(data)
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetHeader(call: String, s: Station?, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()
    Row(Modifier.fillMaxWidth().background(Pal.bar).padding(12.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        val drawable = remember(s?.symbolTable, s?.symbol) {
            s?.let { AprsSymbolIcons.icon(context, it.symbolTable, it.symbol) as? BitmapDrawable }
        }
        if (drawable != null) Image(BitmapPainter(drawable.bitmap.asImageBitmap()),
            s?.role?.let { UiText.role(it) }, Modifier.size(44.dp))
        Column(Modifier.weight(1f)) {
            val base = call.substringBefore('-')
            val ssid = call.substringAfter('-', "")
            Text(buildAnnotatedString {
                append(base)
                if (ssid.isNotEmpty()) withStyle(SpanStyle(color = Pal.accent)) { append("-$ssid") }
            }, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Pal.text)
            if (s != null) {
                val live = now - s.lastHeard <= LIVE_MS
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Tag(UiText.role(s.role))
                    Box(Modifier.size(8.dp).background(if (live) Pal.green else Pal.muted, CircleShape))
                    Text(UiText.text(if (live) R.string.sheet_live else R.string.sheet_inactive) + " · " + ago(s.lastHeard),
                        color = Pal.muted, fontSize = 12.sp)
                }
                Text(String.format(Locale.ROOT, "%.5f, %.5f", s.lat, s.lon), color = Pal.muted, fontSize = 12.sp)
            } else Text(UiText.text(R.string.no_position), color = Pal.muted, fontSize = 12.sp)
        }
        TextButton(onClick = onDismiss, modifier = Modifier.semantics {
            contentDescription = UiText.text(R.string.close)
        }) { Text("✕", fontSize = 20.sp) }
    }
}

@Composable
private fun Tag(text: String) {
    Text(text, color = Pal.ground, fontSize = 11.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.background(Pal.accent, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 1.dp))
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, color = Pal.accent, fontWeight = FontWeight.Bold, fontSize = 14.sp,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
}

private fun notInformed() = UiText.text(R.string.not_informed)

@Composable
private fun InfoCard(label: String, value: String?, modifier: Modifier) {
    Column(modifier.background(Pal.card, RoundedCornerShape(8.dp)).padding(10.dp)) {
        Text(label, color = Pal.muted, fontSize = 11.sp)
        Text(value ?: notInformed(), color = if (value == null) Pal.muted else Pal.text,
            fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CardGrid(cards: List<Pair<String, String?>>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        cards.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { (label, value) -> InfoCard(label, value, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private fun km(v: Double?) = v?.let { String.format(Locale.getDefault(), "%.1f km", it) }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OverviewTab(
    s: Station?, data: StationInsights, myLat: Double?, myLon: Double?,
    all: Collection<Station>, weatherLive: List<AprsWeather>, onOpen: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
        // Estação meteorológica: condições no topo.
        val wx = (data.weather + weatherLive).maxByOrNull { it.timestamp }
        if (wx != null) {
            SectionHeader(UiText.text(R.string.wx_section) + " · " + ago(wx.timestamp))
            CardGrid(listOfNotNull(
                wx.temperatureC?.let { UiText.text(R.string.wx_temperature) to String.format(Locale.getDefault(), "%.1f °C", it) },
                wx.humidityPercent?.let { UiText.text(R.string.wx_humidity) to "$it %" },
                wx.pressureHpa?.let { UiText.text(R.string.wx_pressure) to String.format(Locale.getDefault(), "%.1f hPa", it) },
                wx.windKmh?.let { UiText.text(R.string.wx_wind) to String.format(Locale.getDefault(), "%.0f km/h", it) +
                    (wx.windDirectionDeg?.let { d -> " · ${compassName(d)}" } ?: "") },
            ))
        }
        SectionHeader(UiText.text(R.string.sheet_section_station))
        val distance = if (s != null && myLat != null && myLon != null) haversineKm(myLat, myLon, s.lat, s.lon) else null
        val bearing = if (s != null && myLat != null && myLon != null) bearingDeg(myLat, myLon, s.lat, s.lon) else null
        CardGrid(listOf(
            UiText.text(R.string.position) to s?.let { String.format(Locale.ROOT, "%.4f, %.4f", it.lat, it.lon) },
            "Grid" to s?.let { maidenhead(it.lat, it.lon) },
            UiText.text(R.string.distance) to km(distance),
            UiText.text(R.string.direction) to bearing?.let { "${compassName(it)} $it°" },
            UiText.text(R.string.speed) to s?.speedKmh?.let { String.format(Locale.getDefault(), "%.0f km/h", it) },
            UiText.text(R.string.sheet_course) to s?.course?.takeIf { it in 1..360 }?.let { "$it° ${compassName(it % 360)}" },
            UiText.text(R.string.sheet_altitude) to data.altitudeM?.let { String.format(Locale.getDefault(), "%.0f m", it) },
            UiText.text(R.string.sheet_type) to s?.let { UiText.role(it.role) },
            UiText.text(R.string.sheet_last_packet) to (data.lastPacketAt ?: s?.lastHeard)?.let {
                ago(it) + " · " + UiText.text(R.string.sheet_packets_total, data.totalPackets) },
            UiText.text(R.string.sheet_first_seen) to s?.firstSeen?.let { fullTime(it) },
            UiText.text(R.string.sheet_transport) to s?.medium?.label,
            UiText.text(R.string.symbol) to s?.let { "${it.symbolTable}${it.symbol}" },
            "PHG" to data.phg,
        ))
        if (s != null && s.comment.isNotBlank()) {
            SectionHeader(UiText.text(R.string.sheet_comment))
            var expanded by remember { mutableStateOf(false) }
            Text(s.comment, color = Pal.text, fontSize = 14.sp, maxLines = if (expanded) Int.MAX_VALUE else 3,
                modifier = Modifier.clickable { expanded = !expanded })
        }

        val r = data.reception
        SectionHeader(UiText.text(R.string.sheet_reception_24h))
        CardGrid(listOf(
            UiText.text(R.string.sheet_packets_window) to r.packetsInWindow.toString(),
            UiText.text(R.string.sheet_heard_by_igates) to r.igates.size.toString(),
            UiText.text(R.string.sheet_farthest_igate) to r.farthestIgate?.let { g -> g + (km(r.farthestIgateKm)?.let { " · $it" } ?: "") },
            UiText.text(R.string.sheet_last_hop) to r.lastHop?.let { h -> h + (km(r.lastHopKm)?.let { " · $it" } ?: "") },
            UiText.text(R.string.sheet_local_rf) to UiText.text(R.string.sheet_local_rf_value, r.localRfPackets, r.localRfDirect),
            UiText.text(R.string.sheet_internet_packets) to r.internetPackets.toString(),
        ))
        if (r.igates.isNotEmpty()) Text(r.igates.joinToString(", "), color = Pal.muted, fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp))
        if (s != null && s.heardBy.isNotEmpty()) Text(UiText.text(R.string.heard_by_rf, s.heardBy.joinToString(", ")),
            color = Pal.green, fontSize = 12.sp)
        Text(UiText.text(R.string.sheet_reception_note), color = Pal.muted, fontSize = 11.sp,
            modifier = Modifier.padding(top = 4.dp))

        val siblings = otherSsids(data.call, all)
        SectionHeader(UiText.text(R.string.sheet_other_ssids))
        if (siblings.isEmpty()) Text(UiText.text(R.string.sheet_no_other_ssids), color = Pal.muted, fontSize = 12.sp)
        else FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            siblings.forEach { other ->
                Text("${other.call} · ${ago(other.lastHeard)}", color = Pal.text, fontSize = 13.sp,
                    modifier = Modifier.background(Pal.card, RoundedCornerShape(16.dp))
                        .border(1.dp, Pal.line, RoundedCornerShape(16.dp))
                        .clickable { onOpen(other.call) }.padding(horizontal = 10.dp, vertical = 8.dp))
            }
        }
        Spacer(Modifier.heightIn(min = 24.dp))
    }
}

private fun qText(code: String?): String = when (qMeaning(code)) {
    QMeaning.RF_IGATE -> UiText.text(R.string.q_rf_igate)
    QMeaning.RF_IGATE_UNVERIFIED -> UiText.text(R.string.q_rf_igate_unverified)
    QMeaning.CLIENT_VERIFIED -> UiText.text(R.string.q_client)
    QMeaning.CLIENT_UNVERIFIED -> UiText.text(R.string.q_client_unverified)
    QMeaning.SERVER -> UiText.text(R.string.q_server)
    QMeaning.INTERNET -> UiText.text(R.string.q_internet)
    QMeaning.OTHER, null -> UiText.text(R.string.q_other)
}

private fun hopText(hop: PathHop): String = when (hop.kind) {
    PathHop.Kind.ORIGIN -> UiText.text(R.string.hop_origin)
    PathHop.Kind.DIGIPEATER -> UiText.text(R.string.hop_digi)
    PathHop.Kind.ALIAS_USED -> UiText.text(R.string.hop_alias_used)
    PathHop.Kind.ALIAS_UNUSED -> UiText.text(R.string.hop_alias_unused)
    PathHop.Kind.NETWORK -> UiText.text(R.string.hop_network)
    PathHop.Kind.Q_CONSTRUCT -> qText(hop.qCode)
    PathHop.Kind.IGATE -> UiText.text(R.string.hop_igate)
    PathHop.Kind.THIRD_PARTY -> UiText.text(R.string.hop_third_party)
}

@Composable
private fun PathTab(data: StationInsights) {
    val path = data.lastPath
    val last = data.lastPacket
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
        if (path == null || last == null) {
            Text(UiText.text(R.string.sheet_no_packets), color = Pal.muted)
            return@Column
        }
        SectionHeader(UiText.text(R.string.sheet_last_path) + " · " + ago(last.time) + " · " + last.medium.label)
        Text(if (path.direct) UiText.text(R.string.sheet_path_direct) else UiText.text(R.string.sheet_path_hops, path.rfHops),
            color = Pal.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.heightIn(min = 8.dp))
        path.hops.forEachIndexed { i, hop ->
            val color = when (hop.kind) {
                PathHop.Kind.ORIGIN -> Pal.accent
                PathHop.Kind.DIGIPEATER, PathHop.Kind.ALIAS_USED -> Pal.green
                PathHop.Kind.IGATE, PathHop.Kind.THIRD_PARTY -> Pal.yellow
                PathHop.Kind.ALIAS_UNUSED -> Pal.muted
                else -> Pal.orange
            }
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(18.dp)) {
                    Box(Modifier.size(14.dp).background(color, CircleShape))
                    if (i < path.hops.lastIndex) Box(Modifier.width(2.dp).heightIn(min = 30.dp).background(Pal.line))
                }
                Column {
                    Text(hop.call, color = Pal.text, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace)
                    Text(hopText(hop), color = Pal.muted, fontSize = 12.sp)
                }
            }
        }
        SectionHeader(UiText.text(R.string.logs_raw))
        SelectionContainer {
            Text(decodeAprsText(last.raw), fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Pal.text)
        }
    }
}

@Composable
private fun TelemetryTab(data: StationInsights, weatherLive: List<AprsWeather>) {
    var hours by rememberSaveable { mutableIntStateOf(24) }
    val now = System.currentTimeMillis()
    val cutoff = now - hours * 3_600_000L
    // Séries do histórico local + amostras desta sessão, sem repetir o mesmo instante.
    val weather = (data.weather + weatherLive).distinctBy { it.timestamp }.sortedBy { it.timestamp }
    val telemetry = data.telemetry
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
        if (weather.isEmpty() && telemetry.isEmpty()) {
            Text(UiText.text(R.string.sheet_no_telemetry), color = Pal.muted)
            return@Column
        }
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            listOf(1, 6, 24, 168, 720).forEach { h ->
                TextButton(onClick = { hours = h }) {
                    Text(when (h) { 168 -> "7d"; 720 -> "30d"; else -> "${h}h" },
                        color = if (hours == h) Pal.accent else Pal.muted, fontSize = 13.sp,
                        fontWeight = if (hours == h) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
        Text(UiText.text(R.string.sheet_retention_note), color = Pal.muted, fontSize = 11.sp)
        if (weather.isNotEmpty()) {
            val w = weather.filter { it.timestamp >= cutoff }
            SectionHeader(UiText.text(R.string.wx_section))
            fun series(read: (AprsWeather) -> Double?) = w.mapNotNull { s -> read(s)?.let { ChartPoint(s.timestamp, it) } }
            val charts = listOf(
                Triple(R.string.wx_temperature, "°C", series { it.temperatureC }),
                Triple(R.string.wx_humidity, "%", series { it.humidityPercent?.toDouble() }),
                Triple(R.string.wx_pressure, "hPa", series { it.pressureHpa }),
                Triple(R.string.wx_wind, "km/h", series { it.windKmh }),
                Triple(R.string.wx_gust, "km/h", series { it.gustKmh }),
            )
            charts.filter { it.third.isNotEmpty() }.forEach { (title, unit, points) ->
                TimeSeriesChart(UiText.text(title), unit, points)
            }
            // Chuva é acumulada: barras, uma por informe, sem interpolar.
            listOf(R.string.wx_rain_hour to series { it.rain1hMm }, R.string.wx_rain_day to series { it.rain24hMm },
                R.string.wx_rain_midnight to series { it.rainSinceMidnightMm }).filter { it.second.isNotEmpty() }
                .forEach { (title, points) -> TimeSeriesChart(UiText.text(title), "mm", points, bars = true) }
            if (w.isEmpty()) Text(UiText.text(R.string.chart_no_samples_period), color = Pal.muted, fontSize = 12.sp)
        }
        if (telemetry.isNotEmpty()) {
            val defs = data.definitions
            val t = telemetry.filter { it.time >= cutoff }
            SectionHeader(UiText.text(R.string.sheet_telemetry) +
                (defs.projectTitle.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""))
            val latest = telemetry.last()
            Text(UiText.text(R.string.sheet_telemetry_last, latest.sequence, ago(latest.time)), color = Pal.muted, fontSize = 12.sp)
            if (defs.isEmpty) Text(UiText.text(R.string.sheet_telemetry_no_defs), color = Pal.muted, fontSize = 11.sp)
            (0 until 5).forEach { ch ->
                val raw = latest.analog[ch] ?: return@forEach
                val name = defs.analogName(ch) ?: UiText.text(R.string.sheet_channel, ch + 1)
                val unit = defs.analogUnit(ch).orEmpty()
                val scaled = defs.scaled(ch, raw)
                KeyValue(name, String.format(Locale.getDefault(), "%.2f %s", scaled, unit).trim() +
                    if (defs.hasEquation(ch)) " (" + UiText.text(R.string.sheet_raw_value, raw.toString()) + ")" else "")
                TimeSeriesChart(name, unit, t.mapNotNull { f -> f.analog[ch]?.let { ChartPoint(f.time, defs.scaled(ch, it)) } },
                    decimals = 2)
            }
            if (latest.bits.isNotEmpty()) {
                SectionHeader(UiText.text(R.string.sheet_bits))
                latest.bits.forEachIndexed { i, bit ->
                    val name = defs.bitName(i) ?: "B${i + 1}"
                    val active = defs.bitActive(i, bit)
                    // Última mudança deste bit no histórico local.
                    val changedAt = telemetry.zipWithNext().lastOrNull { (a, b) ->
                        a.bits.getOrNull(i) != null && b.bits.getOrNull(i) != null && a.bits[i] != b.bits[i]
                    }?.second?.time
                    KeyValue(name, (if (bit) "1" else "0") + " · " +
                        UiText.text(if (active) R.string.sheet_bit_on else R.string.sheet_bit_off) +
                        (changedAt?.let { " · " + UiText.text(R.string.sheet_bit_changed, ago(it)) } ?: ""))
                }
            }
            if (latest.comment.isNotBlank()) Text(latest.comment, color = Pal.muted, fontSize = 12.sp)
        }
        Spacer(Modifier.heightIn(min = 24.dp))
    }
}

@Composable
private fun RawTab(data: StationInsights) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(UiText.text(R.string.sheet_packets_total, data.packets.size), color = Pal.muted, fontSize = 12.sp,
                modifier = Modifier.weight(1f).padding(start = 4.dp))
            if (data.packets.isNotEmpty()) TextButton(onClick = {
                shareText(context, "APRS ${data.call}", data.packets.joinToString("\n") { fullTime(it.time) + " " + it.raw })
            }) { Text(UiText.text(R.string.logs_share)) }
        }
        if (data.packets.isEmpty()) Text(UiText.text(R.string.sheet_no_packets), color = Pal.muted,
            modifier = Modifier.padding(16.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(data.packets, key = { it.id }) { e ->
                Column(Modifier.fillMaxWidth().clickable { clipboard.setText(AnnotatedString(e.raw)) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("${fullTime(e.time)} · ${ago(e.time)} · ${e.medium.label}", color = Pal.muted, fontSize = 11.sp)
                    Text(decodeAprsText(e.raw), fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Pal.text)
                }
            }
        }
    }
}
