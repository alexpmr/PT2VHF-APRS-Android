package com.pt2vhf.aprs.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Slider
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import com.pt2vhf.aprs.data.StationCriteria
import com.pt2vhf.aprs.data.stationSpeedLabel
import com.pt2vhf.aprs.data.selectStations
import com.pt2vhf.aprs.service.AprsService
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.pt2vhf.aprs.data.OutboundSendResult

internal fun ago(t: Long): String {
    val s = (System.currentTimeMillis() - t) / 1000
    return when {
        s < 60 -> UiText.text(R.string.now)
        s < 3600 -> UiText.text(R.string.minutes_ago, s / 60)
        s < 86400 -> UiText.text(R.string.hours_ago, s / 3600)
        else -> java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.SHORT, java.text.DateFormat.SHORT, UiText.locale()).format(Date(t))
    }
}

internal fun hhmm(t: Long) = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT, UiText.locale()).format(Date(t))

private fun stationAge(t: Long, nowMs: Long = System.currentTimeMillis()): String {
    val age = ((nowMs - t).coerceAtLeast(0L)) / 1000
    return if (age >= 86400) UiText.text(R.string.days_ago, age / 86400) else ago(t)
}

@Composable
internal fun Empty(text: String) {
    Text(text, color = Pal.muted, modifier = Modifier.padding(24.dp), fontSize = 14.sp)
}

// ---------- Mapa ----------

/** #22: brilho só da camada cartográfica (tiles), sem tocar nos marcadores nem no brilho do Android. */
internal fun brightnessFilter(percent: Int): android.graphics.ColorMatrixColorFilter? {
    val f = percent.coerceIn(50, 150) / 100f
    if (f == 1f) return null
    val matrix = if (f < 1f) floatArrayOf(
        f, 0f, 0f, 0f, 0f,
        0f, f, 0f, 0f, 0f,
        0f, 0f, f, 0f, 0f,
        0f, 0f, 0f, 1f, 0f,
    ) else {
        // Lighten toward white: out = in * (2 - f) + 255 * (f - 1)
        val k = 2f - f
        val add = 255f * (f - 1f)
        floatArrayOf(
            k, 0f, 0f, 0f, add,
            0f, k, 0f, 0f, add,
            0f, 0f, k, 0f, add,
            0f, 0f, 0f, 1f, 0f,
        )
    }
    return android.graphics.ColorMatrixColorFilter(android.graphics.ColorMatrix(matrix))
}

internal fun trackArgb(name: String, opacityPercent: Int): Int {
    val rgb = when (name) {
        "red" -> 0xF45B69
        "green" -> 0x20D397
        "yellow" -> 0xF5C400
        "purple" -> 0xB57CFF
        "white" -> 0xFFFFFF
        else -> 0x5CAEFF
    }
    val alpha = (opacityPercent.coerceIn(20, 100) * 255 / 100) shl 24
    return alpha or rgb
}

@Composable
private fun LayerToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    DropdownMenuItem(text = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked, onChange)
            Text(label)
        }
    }, onClick = { onChange(!checked) })
}

@Composable
fun MapScreen(settings: Settings, onStation: (String) -> Unit, onSettingsChanged: (Settings) -> Unit = {}) {
    val allStations by AprsRepository.stations.collectAsState()
    val myObjects by AprsRepository.myObjects.collectAsState()
    val routes by AprsRepository.routes.collectAsState()
    var longPress by remember { mutableStateOf<GeoPoint?>(null) }
    var editObject by remember { mutableStateOf<com.pt2vhf.aprs.data.MyObject?>(null) }
    val links by AprsRepository.links.collectAsState()
    val ownTrack by AprsRepository.track.collectAsState()
    val trails by AprsRepository.trails.collectAsState()
    val rfEvents by AprsRepository.rfEvents.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val status by AprsRepository.status.collectAsState()
    val followedCall by AprsRepository.followCall.collectAsState()
    val activeFollow by androidx.compose.runtime.rememberUpdatedState(followedCall)
    var followPaused by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(followedCall) { followPaused = false }
    val savedLat = remember { Settings.readOption(context, "map_view_lat", "").toDoubleOrNull() }
    val savedLon = remember { Settings.readOption(context, "map_view_lon", "").toDoubleOrNull() }
    val savedZoom = remember { Settings.readOption(context, "map_view_zoom", "").toDoubleOrNull() }
    val hasValidViewport = savedLat != null && savedLon != null &&
        savedLat in -90.0..90.0 && savedLon in -180.0..180.0
    var centered by remember { mutableStateOf(hasValidViewport) }
    val mapRef = remember { arrayOfNulls<MapView>(1) }
    var brightness by rememberSaveable {
        mutableStateOf(Settings.readOption(context, "map_brightness", "100").toIntOrNull()?.coerceIn(50, 150) ?: 100)
    }
    var brightnessOpen by remember { mutableStateOf(false) }
    var layersOpen by remember { mutableStateOf(false) }
    fun layer(key: String, default: Boolean) = Settings.readOption(context, key, default.toString()) == "true"
    var showLinks by remember { mutableStateOf(layer("map_layer_links", true)) }
    var showMyLinks by remember { mutableStateOf(layer("map_layer_mylinks", true)) }
    var onlyMyLinks by remember { mutableStateOf(layer("map_only_my_links", false)) }
    var showCoverage by remember { mutableStateOf(layer("map_layer_coverage", false)) }
    var showLabels by remember { mutableStateOf(layer("map_layer_labels", true)) }
    var clock by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(30_000); clock = System.currentTimeMillis() } }
    val maxAgeMs = settings.stationMaxAgeHours * 3_600_000L
    val stations = if (maxAgeMs > 0) allStations.filterValues { clock - it.lastHeard <= maxAgeMs } else allStations
    val trackStyle = remember(ownTrack.size) {
        Triple(Settings.readOption(context, "track_color", "blue"),
            Settings.readOption(context, "track_width", "6").toFloatOrNull() ?: 6f,
            Settings.readOption(context, "track_opacity", "100").toIntOrNull() ?: 100)
    }
    Box(Modifier.fillMaxSize()) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                mapRef[0] = this
                setOnTouchListener { view, event ->
                    if (activeFollow != null && event.actionMasked == android.view.MotionEvent.ACTION_MOVE)
                        followPaused = true
                    if (event.actionMasked == android.view.MotionEvent.ACTION_UP) {
                        // Persist after osmdroid has finished the gesture / kinetic movement.
                        (view as? MapView)?.postDelayed({
                            val map = view as? MapView
                            val point = map?.mapCenter
                            if (map != null && point != null &&
                                point.latitude in -90.0..90.0 && point.longitude in -180.0..180.0) {
                                centered = true // manual viewport wins over a late GPS fix.
                                Settings.saveOption(context, "map_view_lat", point.latitude.toString())
                                Settings.saveOption(context, "map_view_lon", point.longitude.toString())
                                Settings.saveOption(context, "map_view_zoom", map.zoomLevelDouble.toString())
                            }
                        }, 750L)
                    }
                    false
                }
                controller.setZoom((savedZoom ?: if (hasValidViewport) 9.0 else 3.0).coerceIn(2.0, 19.0))
                val initial = if (hasValidViewport) GeoPoint(savedLat!!, savedLon!!)
                    else if (status.lat != null && status.lon != null) GeoPoint(status.lat!!, status.lon!!)
                    else GeoPoint(0.0, 0.0)
                controller.setCenter(initial)
                if (!hasValidViewport && status.lat != null && status.lon != null)
                    centered = true
            }
        },
        update = { map ->
            map.overlayManager.tilesOverlay.setColorFilter(brightnessFilter(brightness))
            map.overlays.clear()
            // Toque longo no mapa: definir posição fixa ou criar objeto neste ponto (#6, #48).
            map.overlays.add(org.osmdroid.views.overlay.MapEventsOverlay(object : org.osmdroid.events.MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
                override fun longPressHelper(p: GeoPoint?): Boolean { longPress = p; return p != null }
            }))
            val now = System.currentTimeMillis()
            // Rotas GPX importadas e visíveis.
            routes.filter { it.visible && it.points.size > 1 }.forEach { r ->
                map.overlays.add(Polyline(map).apply {
                    setPoints(r.points.map { GeoPoint(it.lat, it.lon) })
                    outlinePaint.color = 0xCCB57CFF.toInt()
                    outlinePaint.strokeWidth = 4f * density
                })
            }
            // #14 heat map: coverage observed only through this device's TNC.
            if (showCoverage) {
                rfEvents.asSequence().filter { it.kind == "heard" && it.lat != null && it.lon != null }
                    .distinctBy { "%.3f,%.3f".format(it.lat, it.lon) }.toList().takeLast(600).forEach { e ->
                        map.overlays.add(org.osmdroid.views.overlay.Polygon(map).apply {
                            points = org.osmdroid.views.overlay.Polygon.pointsAsCircle(GeoPoint(e.lat!!, e.lon!!), 900.0)
                            val c = if (e.direct) 0x3CCF7C else 0xFFB877
                            fillPaint.color = (0x55 shl 24) or c
                            outlinePaint.color = (0x99 shl 24) or c
                            outlinePaint.strokeWidth = 1f
                        })
                    }
            }
            if (ownTrack.size > 1) {
                val trace = Polyline(map).apply {
                    setPoints(ownTrack.map { GeoPoint(it.lat, it.lon) })
                    outlinePaint.color = trackArgb(trackStyle.first, trackStyle.third)
                    outlinePaint.strokeWidth = trackStyle.second * density / 2f
                    if (Settings.readOption(context, "track_style", "solid") == "dashed")
                        outlinePaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(15f, 10f), 0f)
                }
                map.overlays.add(trace)
            }
            // #11: trail of the followed station, received live in this session.
            followedCall?.let { trails[it] }?.takeIf { it.size > 1 }?.let { trail ->
                map.overlays.add(Polyline(map).apply {
                    setPoints(trail.map { GeoPoint(it.lat, it.lon) })
                    outlinePaint.color = 0xFFFFB877.toInt()
                    outlinePaint.strokeWidth = 5f
                    outlinePaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(12f, 8f), 0f)
                })
            }
            val dayAgo = now - 24 * 3600_000L
            if (showLinks && !onlyMyLinks) for (l in links.filter { it.lastSeen > dayAgo }) {
                val a = stations[l.from] ?: continue
                val b = stations[l.to] ?: continue
                map.overlays.add(Polyline(map).apply {
                    setPoints(listOf(GeoPoint(a.lat, a.lon), GeoPoint(b.lat, b.lon)))
                    outlinePaint.color = 0xFFF5E400.toInt()
                    outlinePaint.strokeWidth = 5f
                })
            }
            // #4: my own RF links (TNC evidence), last 24 h, in their own colour.
            val myLat = status.lat
            val myLon = status.lon
            if (showMyLinks || onlyMyLinks) {
                rfEvents.filter { it.time > dayAgo }.distinctBy { it.call to it.kind }.forEach { e ->
                    val fromLat = e.ownLat ?: myLat ?: return@forEach
                    val fromLon = e.ownLon ?: myLon ?: return@forEach
                    val target = when {
                        e.kind == "digi" -> e.lat?.let { la -> e.lon?.let { lo -> la to lo } }
                        e.direct -> e.lat?.let { la -> e.lon?.let { lo -> la to lo } }
                        else -> stations[e.via]?.let { it.lat to it.lon }
                    } ?: return@forEach
                    map.overlays.add(Polyline(map).apply {
                        setPoints(listOf(GeoPoint(fromLat, fromLon), GeoPoint(target.first, target.second)))
                        outlinePaint.color = if (e.kind == "digi") 0xFFB57CFF.toInt() else 0xFF3CCF7C.toInt()
                        outlinePaint.strokeWidth = 7f
                    })
                }
            }
            for (s in stations.values) {
                map.overlays.add(Marker(map).apply {
                    position = GeoPoint(s.lat, s.lon)
                    title = s.call
                    val (drawable, anchorU) = AprsSymbolIcons.labeled(context, s.symbolTable, s.symbol,
                        if (showLabels) s.call else "", density)
                    icon = drawable
                    snippet = "${UiText.role(s.role)} · ${ago(s.lastHeard)}"
                    setAnchor(anchorU, Marker.ANCHOR_CENTER)
                    setOnMarkerClickListener { _, _ -> onStation(s.call); true }
                })
            }
            if (myLat != null && myLon != null) {
                map.overlays.add(Marker(map).apply {
                    position = GeoPoint(myLat, myLon)
                    title = UiText.text(R.string.me)
                    val (drawable, anchorU) = AprsSymbolIcons.labeled(context, settings.symbolTable, settings.symbol,
                        if (showLabels) settings.fullCall else "", density)
                    icon = drawable
                    setAnchor(anchorU, Marker.ANCHOR_CENTER)
                })
                if (!centered && followedCall == null) {
                    map.controller.setCenter(GeoPoint(myLat, myLon)); centered = true
                }
            }
            // Meus objetos/itens (#48).
            myObjects.forEach { o ->
                map.overlays.add(Marker(map).apply {
                    position = GeoPoint(o.lat, o.lon)
                    title = o.name
                    val (drawable, anchorU) = AprsSymbolIcons.labeled(context, o.symbolTable, o.symbol,
                        if (showLabels) o.name + if (o.active) "" else " ✕" else "", density)
                    icon = drawable
                    alpha = if (o.active) 1f else 0.5f
                    setAnchor(anchorU, Marker.ANCHOR_CENTER)
                    setOnMarkerClickListener { _, _ -> editObject = o; true }
                })
            }
            val followed = followedCall?.let { stations[it] }
            // Linha direta até a estação seguida.
            if (followed != null && myLat != null && myLon != null) map.overlays.add(Polyline(map).apply {
                setPoints(listOf(GeoPoint(myLat, myLon), GeoPoint(followed.lat, followed.lon)))
                outlinePaint.color = 0xAAF5C400.toInt()
                outlinePaint.strokeWidth = 3f * density
                outlinePaint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(6f * density, 6f * density), 0f)
            })
            if (followed != null && !followPaused)
                map.controller.setCenter(GeoPoint(followed.lat, followed.lon))
            map.invalidate()
        },
    )
    Row(Modifier.align(Alignment.TopEnd).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box {
            TextButton(modifier = Modifier.background(Pal.bar, RoundedCornerShape(10.dp)),
                onClick = { layersOpen = true }) { Text("▤ " + UiText.text(R.string.map_layers)) }
            DropdownMenu(expanded = layersOpen, onDismissRequest = { layersOpen = false }) {
                LayerToggle(UiText.text(R.string.layer_labels), showLabels) {
                    showLabels = it; Settings.saveOption(context, "map_layer_labels", it.toString())
                }
                LayerToggle(UiText.text(R.string.layer_all_links), showLinks) {
                    showLinks = it; Settings.saveOption(context, "map_layer_links", it.toString())
                }
                LayerToggle(UiText.text(R.string.layer_my_links), showMyLinks) {
                    showMyLinks = it; Settings.saveOption(context, "map_layer_mylinks", it.toString())
                }
                LayerToggle(UiText.text(R.string.layer_only_my_links), onlyMyLinks) {
                    onlyMyLinks = it; Settings.saveOption(context, "map_only_my_links", it.toString())
                }
                LayerToggle(UiText.text(R.string.layer_coverage), showCoverage) {
                    showCoverage = it; Settings.saveOption(context, "map_layer_coverage", it.toString())
                }
            }
        }
        TextButton(modifier = Modifier.background(Pal.bar, RoundedCornerShape(10.dp))
            .semantics { contentDescription = UiText.text(R.string.map_brightness) },
            onClick = { brightnessOpen = !brightnessOpen }) { Text("☀") }
        TextButton(
            modifier = Modifier.background(Pal.bar, RoundedCornerShape(10.dp)),
            onClick = {
                val lat = status.lat
                val lon = status.lon
                val map = mapRef[0]
                if (map != null && lat != null && lon != null) {
                    AprsRepository.followStation(null)
                    centered = true
                    map.controller.setCenter(GeoPoint(lat, lon))
                    Settings.saveOption(context, "map_view_lat", lat.toString())
                    Settings.saveOption(context, "map_view_lon", lon.toString())
                    Settings.saveOption(context, "map_view_zoom", map.zoomLevelDouble.toString())
                }
            },
        ) { Text("◎ " + UiText.text(R.string.me)) }
    }
    if (brightnessOpen) {
        Column(Modifier.align(Alignment.TopCenter).padding(top = 56.dp, start = 12.dp, end = 12.dp)
            .fillMaxWidth().background(Pal.bar, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("☀ " + UiText.text(R.string.map_brightness) + " $brightness%", modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    brightness = 100; Settings.saveOption(context, "map_brightness", "100")
                }) { Text(UiText.text(R.string.restore_default)) }
            }
            Slider(value = brightness.toFloat(), onValueChange = { brightness = it.toInt() },
                onValueChangeFinished = { Settings.saveOption(context, "map_brightness", brightness.toString()) },
                valueRange = 50f..150f, steps = 19)
        }
    }
    val visibleRoutes = routes.filter { it.visible && it.points.size > 1 }
    if (visibleRoutes.isNotEmpty() && status.lat != null && status.lon != null) {
        Column(Modifier.align(Alignment.TopStart).padding(top = 56.dp, start = 8.dp)
            .background(Pal.bar, RoundedCornerShape(10.dp)).padding(horizontal = 10.dp, vertical = 6.dp)) {
            visibleRoutes.take(3).forEach { r ->
                com.pt2vhf.aprs.core.routeRemainingKm(r.points, status.lat!!, status.lon!!)?.let { (km, off) ->
                    Text(UiText.text(R.string.route_remaining, r.name, km, off), fontSize = 12.sp, color = Pal.text)
                }
            }
        }
    }
    longPress?.let { p ->
        AlertDialog(
            onDismissRequest = { longPress = null },
            containerColor = Pal.bar,
            title = { Text("%.5f, %.5f".format(java.util.Locale.ROOT, p.latitude, p.longitude)) },
            text = {
                Column {
                    TextButton(onClick = {
                        val updated = settings.copy(positionSource = "manual", manualLat = p.latitude, manualLon = p.longitude)
                        Settings.save(context, updated)
                        onSettingsChanged(updated)
                        AprsService.send(context, AprsService.ACTION_RELOAD)
                        android.widget.Toast.makeText(context, UiText.text(R.string.map_position_set), android.widget.Toast.LENGTH_SHORT).show()
                        longPress = null
                    }) { Text(UiText.text(R.string.map_set_position)) }
                    TextButton(enabled = settings.configured, onClick = {
                        editObject = newObjectAt(p.latitude, p.longitude)
                        longPress = null
                    }) { Text(UiText.text(R.string.map_create_object)) }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { longPress = null }) { Text(UiText.text(R.string.cancel)) } },
        )
    }
    editObject?.let { o ->
        ObjectDialog(o, isNew = myObjects.none { it.id == o.id }, settings = settings, onDismiss = { editObject = null })
    }
    if (followedCall != null) {
        val target = allStations[followedCall]
        Column(Modifier.align(Alignment.BottomCenter).padding(8.dp).background(Pal.bar, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (target != null) {
                val parts = mutableListOf(stationSpeedLabel(target.speedKmh))
                target.course?.let { parts += "${compassName(it)} $it°" }
                if (status.lat != null && status.lon != null)
                    parts += "%.1f km".format(haversineKm(status.lat!!, status.lon!!, target.lat, target.lon))
                parts += stationAge(target.lastHeard, clock)
                Text(parts.joinToString(" · "), fontSize = 12.sp, color = Pal.muted)
                if (clock - target.lastHeard > 10 * 60_000L)
                    Text(UiText.text(R.string.follow_stale, (clock - target.lastHeard) / 60_000L), color = Pal.orange, fontSize = 12.sp)
            }
            Row {
                TextButton(onClick = { followPaused = !followPaused }) {
                    Text(if (followPaused) UiText.text(R.string.follow_paused)
                        else UiText.text(R.string.following, followedCall!!))
                }
                TextButton(onClick = { AprsRepository.followStation(null) }) {
                    Text(UiText.text(R.string.stop_following))
                }
            }
        }
    }
    }
}

// ---------- Estações ----------

@Composable
fun StationsScreen(maxAgeHours: Int, onStation: (String) -> Unit) {
    val allStations by AprsRepository.stations.collectAsState()
    val status by AprsRepository.status.collectAsState()
    val favorites by AprsRepository.favorites.collectAsState()
    val context = LocalContext.current
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val compact = screenWidth <= 360
    val callWidth = if (compact) 72.dp else 92.dp
    val ageWidth = if (compact) 38.dp else 52.dp
    val speedWidth = if (compact) 55.dp else 68.dp
    val distanceWidth = if (compact) 58.dp else 71.dp
    var refreshClock by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            refreshClock = System.currentTimeMillis()
        }
    }
    var filterOpen by remember { mutableStateOf(false) }
    var clearStationsOpen by remember { mutableStateOf(false) }
    var clearingStations by remember { mutableStateOf(false) }
    var clearStationsResult by remember { mutableStateOf<String?>(null) }
    val stationClearScope = rememberCoroutineScope()
    var query by rememberSaveable { mutableStateOf(Settings.readOption(context, "station_search", "")) }
    var sort by rememberSaveable { mutableStateOf(Settings.readOption(context, "station_sort_v2", "distance")) }
    var descending by rememberSaveable { mutableStateOf(Settings.readOption(context, "station_desc_v2", "false") == "true") }
    var roles by remember {
        mutableStateOf(Settings.readOption(context, "station_roles_v2", "").split("|").filter { it.isNotBlank() }.toSet())
    }
    var source by rememberSaveable { mutableStateOf(Settings.readOption(context, "station_source_v2", "all")) }
    var favoritesOnly by rememberSaveable { mutableStateOf(Settings.readOption(context, "station_favorites_v2", "false") == "true") }
    var receivedFrom by rememberSaveable { mutableStateOf(Settings.readOption(context, "station_received_from", "")) }
    var receivedUntil by rememberSaveable { mutableStateOf(Settings.readOption(context, "station_received_until", "")) }
    var firstFrom by rememberSaveable { mutableStateOf(Settings.readOption(context, "station_first_from", "")) }
    var firstUntil by rememberSaveable { mutableStateOf(Settings.readOption(context, "station_first_until", "")) }
    var minDistance by rememberSaveable { mutableStateOf(Settings.readOption(context, "station_min_distance", "")) }
    var maxDistance by rememberSaveable { mutableStateOf(Settings.readOption(context, "station_max_distance", "")) }

    val activeFilters = query.isNotBlank() || roles.isNotEmpty() || source != "all" || favoritesOnly ||
        receivedFrom.isNotBlank() || receivedUntil.isNotBlank() || firstFrom.isNotBlank() ||
        firstUntil.isNotBlank() || minDistance.isNotBlank() || maxDistance.isNotBlank()
    val stations = if (maxAgeHours > 0)
        allStations.filterValues { refreshClock - it.lastHeard <= maxAgeHours * 3_600_000L } else allStations
    val list = selectStations(
        stations.values, StationCriteria(
            callsign = query, sort = sort, descending = descending, roleNames = roles,
            source = source, favoritesOnly = favoritesOnly,
            receivedFrom = receivedFrom, receivedUntil = receivedUntil,
            firstFrom = firstFrom, firstUntil = firstUntil,
            minDistanceKm = minDistance.replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 },
            maxDistanceKm = maxDistance.replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0 },
        ), status.lat, status.lon, favorites,
    )
    fun saveFilters() {
        mapOf(
            "station_search" to query,
            "station_sort_v2" to sort, "station_desc_v2" to descending.toString(),
            "station_roles_v2" to roles.sorted().joinToString("|"),
            "station_source_v2" to source, "station_favorites_v2" to favoritesOnly.toString(),
            "station_received_from" to receivedFrom, "station_received_until" to receivedUntil,
            "station_first_from" to firstFrom, "station_first_until" to firstUntil,
            "station_min_distance" to minDistance, "station_max_distance" to maxDistance,
        ).forEach { (key, value) -> Settings.saveOption(context, key, value) }
    }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedTextField(query, { query = it; Settings.saveOption(context, "station_search", it) },
                placeholder = { Text(UiText.text(R.string.search_callsign_short), fontSize = 12.sp, maxLines = 1) },
                singleLine = true, modifier = Modifier.weight(1f).height(48.dp))
            TextButton(onClick = { filterOpen = true }) {
                Text("☷" + if (activeFilters) " ●" else "", fontSize = 20.sp)
            }
            TextButton(onClick = { clearStationsOpen = true }, enabled = !clearingStations && allStations.isNotEmpty(),
                modifier = Modifier.semantics {
                    contentDescription = UiText.text(R.string.clear_all_stations)
                }) {
                Text("🗑", fontSize = 20.sp)
            }
        }
        clearStationsResult?.let { Text(it, color = Pal.muted, fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp)) }
        Text(UiText.text(R.string.station_showing, list.size, stations.size),
            modifier = Modifier.padding(horizontal = 16.dp), color = Pal.muted, fontSize = 12.sp)
        if (list.isEmpty()) Empty(UiText.text(R.string.no_stations))
        LazyColumn {
            items(list, key = { it.call }) { station ->
                Row(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable { onStation(station.call) }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    val drawable = remember(station.symbolTable, station.symbol) {
                        AprsSymbolIcons.icon(context, station.symbolTable, station.symbol) as? BitmapDrawable
                    }
                    if (drawable != null) {
                        Image(painter = BitmapPainter(drawable.bitmap.asImageBitmap()),
                            contentDescription = UiText.role(station.role), modifier = Modifier.size(22.dp))
                    } else {
                        Text("•", color = Pal.accent, fontSize = 18.sp, modifier = Modifier.width(22.dp))
                    }
                    Row(Modifier.width(callWidth), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(station.call, fontWeight = FontWeight.Bold, fontSize = if (compact) 11.sp else 12.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        if (station.call in favorites) Text("★", color = Pal.yellow, fontSize = 13.sp)
                    }
                    Text(stationAge(station.lastHeard, refreshClock), modifier = Modifier.width(ageWidth),
                        fontSize = if (compact) 10.sp else 11.sp, color = Pal.muted,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    // Description is the flex column: truncate it first on narrow displays.
                    Text(station.comment.ifBlank { " " }, modifier = Modifier.weight(1f),
                        color = Pal.muted, fontSize = 11.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(stationSpeedLabel(station.speedKmh), modifier = Modifier.width(speedWidth),
                        color = Pal.muted, fontSize = if (compact) 10.sp else 11.sp,
                        textAlign = TextAlign.End, maxLines = 1)
                    Text(if (status.lat != null && status.lon != null)
                        "%.1f km".format(haversineKm(status.lat!!, status.lon!!, station.lat, station.lon))
                        else "—", modifier = Modifier.width(distanceWidth),
                        color = Pal.text, fontSize = if (compact) 10.sp else 11.sp,
                        textAlign = TextAlign.End, maxLines = 1)
                }
            }
        }
    }
    if (clearStationsOpen) {
        AlertDialog(
            onDismissRequest = { if (!clearingStations) clearStationsOpen = false },
            containerColor = Pal.bar,
            title = { Text(UiText.text(R.string.clear_all_stations)) },
            text = { Text(UiText.text(R.string.clear_stations_confirmation) +
                "\n" + UiText.text(R.string.station_showing, allStations.size, allStations.size)) },
            confirmButton = {
                TextButton(onClick = {
                    if (!clearingStations) {
                        clearingStations = true
                        stationClearScope.launch {
                            val ok = runCatching { AprsRepository.clearStations() }.getOrDefault(false)
                            clearStationsResult = UiText.text(if (ok) R.string.clear_local_data_success
                                else R.string.clear_local_data_error)
                            clearingStations = false
                            clearStationsOpen = false
                        }
                    }
                }, enabled = !clearingStations) { Text(UiText.text(R.string.clear_all_stations)) }
            },
            dismissButton = {
                TextButton(onClick = { clearStationsOpen = false }, enabled = !clearingStations) {
                    Text(UiText.text(R.string.cancel))
                }
            },
        )
    }
    if (filterOpen) {
        val choices = listOf(
            "distance" to R.string.station_sort_distance,
            "heard" to R.string.station_sort_last,
            "first" to R.string.station_sort_first,
            "callsign" to R.string.station_sort_name,
        )
        val roleChoices = listOf("Estação", "Móvel", "Pessoa", "Digipeater", "iGate", "Estação meteorológica",
            "Aeronave", "Embarcação")
        AlertDialog(
            onDismissRequest = { filterOpen = false },
            containerColor = Pal.bar,
            title = { Text(UiText.text(R.string.station_filter_action)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(UiText.text(R.string.station_sort_label), fontWeight = FontWeight.Bold)
                    choices.forEach { (id, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = sort == id, onCheckedChange = { if (it) sort = id })
                            Text(UiText.text(label), modifier = Modifier.clickable { sort = id })
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = descending, onCheckedChange = { descending = it })
                        Text(UiText.text(if (descending) R.string.station_descending else R.string.station_ascending),
                            modifier = Modifier.clickable { descending = !descending })
                    }
                    Text(UiText.text(R.string.station_filter_types), fontWeight = FontWeight.Bold)
                    roleChoices.forEach { role ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = role in roles, onCheckedChange = { checked ->
                                roles = if (checked) roles + role else roles - role
                            })
                            Text(UiText.role(role), modifier = Modifier.clickable {
                                roles = if (role in roles) roles - role else roles + role
                            })
                        }
                    }
                    Text(UiText.text(R.string.station_filter_source), fontWeight = FontWeight.Bold)
                    listOf("all" to R.string.filter_all, "rf" to R.string.rf_direct,
                        "internet" to R.string.station_source_internet).forEach { (key, id) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = source == key, onCheckedChange = { if (it) source = key })
                            Text(UiText.text(id), modifier = Modifier.clickable { source = key })
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = favoritesOnly, onCheckedChange = { favoritesOnly = it })
                        Text(UiText.text(R.string.favorites_only), modifier = Modifier.clickable {
                            favoritesOnly = !favoritesOnly
                        })
                    }
                    OutlinedTextField(receivedFrom, { receivedFrom = it }, singleLine = true,
                        label = { Text(UiText.text(R.string.station_received_from)) }, placeholder = { Text("AAAA-MM-DD") })
                    OutlinedTextField(receivedUntil, { receivedUntil = it }, singleLine = true,
                        label = { Text(UiText.text(R.string.station_received_until)) }, placeholder = { Text("AAAA-MM-DD") })
                    OutlinedTextField(firstFrom, { firstFrom = it }, singleLine = true,
                        label = { Text(UiText.text(R.string.station_first_from)) }, placeholder = { Text("AAAA-MM-DD") })
                    OutlinedTextField(firstUntil, { firstUntil = it }, singleLine = true,
                        label = { Text(UiText.text(R.string.station_first_until)) }, placeholder = { Text("AAAA-MM-DD") })
                    OutlinedTextField(minDistance, { minDistance = it }, singleLine = true,
                        label = { Text(UiText.text(R.string.station_distance_min)) })
                    OutlinedTextField(maxDistance, { maxDistance = it }, singleLine = true,
                        label = { Text(UiText.text(R.string.station_distance_max)) })
                }
            },
            confirmButton = {
                TextButton(onClick = { saveFilters(); filterOpen = false }) {
                    Text(UiText.text(R.string.station_filter_apply))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    query = ""; sort = "distance"; descending = false; roles = emptySet()
                    source = "all"; favoritesOnly = false; receivedFrom = ""; receivedUntil = ""
                    firstFrom = ""; firstUntil = ""; minDistance = ""; maxDistance = ""
                    saveFilters(); filterOpen = false
                }) { Text(UiText.text(R.string.station_filter_clear)) }
            },
        )
    }
}

@Composable
fun StationDialog(call: String, onDismiss: () -> Unit, onMessage: () -> Unit) {
    val context = LocalContext.current
    val favorites by AprsRepository.favorites.collectAsState()
    val followed by AprsRepository.followCall.collectAsState()
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
                TextButton(onClick = { AprsRepository.toggleFavorite(call) }, modifier = Modifier.fillMaxWidth()) {
                    Text(UiText.text(if (call in favorites) R.string.favorite_remove else R.string.favorite_add))
                }
                TextButton(onClick = {
                    AprsRepository.followStation(if (followed == call) null else call)
                    onDismiss()
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(UiText.text(if (followed == call) R.string.stop_following else R.string.follow_station))
                }
                if (s != null) TextButton(onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("geo:${s.lat},${s.lon}?q=${s.lat},${s.lon}($call)")))
                }, modifier = Modifier.fillMaxWidth()) { Text(UiText.text(R.string.navigate)) }
            }
        },
        confirmButton = {
            Button(onClick = onMessage, modifier = Modifier.fillMaxWidth()) {
                Text(UiText.text(R.string.message))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(UiText.text(R.string.close)) }
        },
    )
}

// ---------- Tracker ----------

private fun gpx(points: List<com.pt2vhf.aprs.data.TrackPoint>, call: String): String {
    val iso = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.ROOT)
        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
    val sb = StringBuilder()
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
    sb.append("<gpx version=\"1.1\" creator=\"PT2VHF APRS Client for Android\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
    sb.append("<trk><name>").append(call.ifBlank { "PT2VHF APRS" }).append("</name><trkseg>\n")
    for (p in points) {
        sb.append(String.format(java.util.Locale.ROOT, "<trkpt lat=\"%.6f\" lon=\"%.6f\"><time>%s</time></trkpt>\n",
            p.lat, p.lon, iso.format(Date(p.recordedAt))))
    }
    sb.append("</trkseg></trk>\n</gpx>\n")
    return sb.toString()
}

@Composable
fun TrackerScreen(settings: Settings, onSettingsChanged: (Settings) -> Unit, onCarMode: () -> Unit = {}) {
    val context = LocalContext.current
    val status by AprsRepository.status.collectAsState()
    val track by AprsRepository.track.collectAsState()
    var trackEnabled by rememberSaveable {
        mutableStateOf(Settings.readOption(context, "tracklog_enabled", "true") == "true")
    }
    var trackStyle by rememberSaveable { mutableStateOf(Settings.readOption(context, "track_style", "solid")) }
    var trackColor by rememberSaveable { mutableStateOf(Settings.readOption(context, "track_color", "blue")) }
    var trackWidth by rememberSaveable { mutableStateOf(Settings.readOption(context, "track_width", "6").toIntOrNull() ?: 6) }
    var trackOpacity by rememberSaveable { mutableStateOf(Settings.readOption(context, "track_opacity", "100").toIntOrNull() ?: 100) }
    var trackDays by rememberSaveable { mutableStateOf(AprsRepository.trackRetentionDays(context)) }
    var exportResult by remember { mutableStateOf<String?>(null) }
    var pendingProfile by remember { mutableStateOf<String?>(null) }
    val exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/gpx+xml")) { uri ->
        if (uri != null) exportResult = runCatching {
            context.contentResolver.openOutputStream(uri)?.use { it.write(gpx(AprsRepository.track.value, settings.fullCall).toByteArray()) }
            UiText.text(R.string.gpx_export_done, AprsRepository.track.value.size)
        }.getOrElse { UiText.text(R.string.diag_export_failed) }
    }
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
        // #10: troca rápida de perfil, com confirmação porque altera a transmissão.
        SectionCard(UiText.text(R.string.profile_section)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Settings.PROFILES.forEach { p ->
                    OutlinedChoice(profileLabel(p), settings.profile == p) { if (p != settings.profile) pendingProfile = p }
                }
            }
            if (settings.rxOnly) Text(UiText.text(R.string.profile_rx_notice), color = Pal.orange, fontSize = 12.sp)
        }
        SectionCard(UiText.text(R.string.configuration)) {
            KeyValue(UiText.text(R.string.callsign), settings.fullCall)
            KeyValue(UiText.text(R.string.symbol), "${settings.symbolTable}${settings.symbol}")
            KeyValue(UiText.text(R.string.send_via), if (settings.rxOnly) UiText.text(R.string.profile_rx)
                else listOfNotNull("RF".takeIf { settings.txViaRf }, "APRS-IS".takeIf { settings.txViaInternet }).joinToString(" + "))
            KeyValue(UiText.text(R.string.rf_path), settings.rfPath)
            if (settings.ambiguity > 0) KeyValue(UiText.text(R.string.privacy_section), UiText.text(R.string.ambiguity_short, settings.ambiguity))
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
        OutlinedButton(onClick = onCarMode, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Text("🚗 " + UiText.text(R.string.car_mode))
        }
        if (settings.manualPosition) Text(UiText.text(R.string.position_source_manual) + ": %.5f, %.5f".format(
            java.util.Locale.ROOT, settings.manualLat, settings.manualLon), color = Pal.orange, fontSize = 12.sp)
        MyObjectsCard(settings)
        RoutesCard()
        BatteryCard(settings)
        // #5: tracklog com estilo configurável, retenção e exportação GPX.
        SectionCard(UiText.text(R.string.track_line)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(trackEnabled, {
                    trackEnabled = it
                    Settings.saveOption(context, "tracklog_enabled", it.toString())
                })
                Text(UiText.text(R.string.track_record))
            }
            Text(UiText.text(R.string.track_points, track.size), color = Pal.muted)
            Text(UiText.text(R.string.track_color), fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("blue", "red", "green", "yellow", "purple", "white").forEach { c ->
                    TextButton(onClick = { trackColor = c; Settings.saveOption(context, "track_color", c) }) {
                        Text(if (trackColor == c) "◉" else "●", color = Color(trackArgb(c, 100)), fontSize = 20.sp)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedChoice(UiText.text(R.string.track_solid), trackStyle == "solid") {
                    trackStyle = "solid"; Settings.saveOption(context, "track_style", "solid")
                }
                OutlinedChoice(UiText.text(R.string.track_dashed), trackStyle == "dashed") {
                    trackStyle = "dashed"; Settings.saveOption(context, "track_style", "dashed")
                }
            }
            Text(UiText.text(R.string.track_width, trackWidth), fontSize = 13.sp)
            Slider(trackWidth.toFloat(), { trackWidth = it.toInt() }, valueRange = 2f..14f, steps = 11,
                onValueChangeFinished = { Settings.saveOption(context, "track_width", trackWidth.toString()) })
            Text(UiText.text(R.string.track_opacity, trackOpacity), fontSize = 13.sp)
            Slider(trackOpacity.toFloat(), { trackOpacity = it.toInt() }, valueRange = 20f..100f, steps = 7,
                onValueChangeFinished = { Settings.saveOption(context, "track_opacity", trackOpacity.toString()) })
            Text(UiText.text(R.string.track_retention), fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf(1, 7, 30, 90, 365).forEach { d ->
                    OutlinedChoice(UiText.text(R.string.days_n, d), trackDays == d) {
                        trackDays = d
                        Settings.saveOption(context, "track_retention_days", d.toString())
                        AprsRepository.pruneTrack(d)
                    }
                }
            }
            Row {
                TextButton(enabled = track.isNotEmpty(), onClick = {
                    exportResult = null
                    exporter.launch("pt2vhf-tracklog.gpx")
                }) { Text(UiText.text(R.string.gpx_export)) }
                TextButton(onClick = { AprsRepository.clearTrack() }) {
                    Text(UiText.text(R.string.track_clear))
                }
            }
            exportResult?.let { Text(it, color = Pal.muted, fontSize = 12.sp) }
        }
    }
    pendingProfile?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingProfile = null },
            containerColor = Pal.bar,
            title = { Text(UiText.text(R.string.profile_switch_title, profileLabel(target))) },
            text = { Text(UiText.text(if (target == Settings.PROFILE_RX) R.string.profile_rx_notice else R.string.profile_switch_body)) },
            confirmButton = {
                TextButton(onClick = {
                    val trackerWasOn = AprsRepository.status.value.trackerOn
                    val next = Settings.forProfile(context, settings, target)
                    // Switching profile never starts transmitting by itself.
                    if (!trackerWasOn) Settings.setFlag(context, "tracker_explicit_off", true)
                    Settings.save(context, next)
                    onSettingsChanged(Settings.load(context))
                    AprsService.send(context, AprsService.ACTION_RELOAD)
                    pendingProfile = null
                }) { Text(UiText.text(R.string.profile_switch_confirm)) }
            },
            dismissButton = { TextButton(onClick = { pendingProfile = null }) { Text(UiText.text(R.string.cancel)) } },
        )
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    Column(modifier.background(Pal.ground, RoundedCornerShape(10.dp)).padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Pal.muted, fontSize = 11.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}
