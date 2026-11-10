package com.pt2vhf.aprs.data

import android.content.Context
import com.pt2vhf.aprs.core.AprsMessage
import com.pt2vhf.aprs.core.AprsPacket
import com.pt2vhf.aprs.core.decodeAprsText
import com.pt2vhf.aprs.core.haversineKm
import com.pt2vhf.aprs.core.localRfEvidenceLinks
import com.pt2vhf.aprs.core.parseMessage
import com.pt2vhf.aprs.core.parseObject
import com.pt2vhf.aprs.core.parsePosition
import com.pt2vhf.aprs.core.parseAprsWeather
import com.pt2vhf.aprs.core.AprsWeather
import com.pt2vhf.aprs.core.packetType
import com.pt2vhf.aprs.core.RoutePoint
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

/** Estado do app compartilhado entre o serviço e a interface. */
object AprsRepository {
    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()
    private val _followCall = MutableStateFlow<String?>(null)
    val followCall: StateFlow<String?> = _followCall.asStateFlow()
    private var preferences: android.content.SharedPreferences? = null

    private val _stations = MutableStateFlow<Map<String, Station>>(emptyMap())
    val stations: StateFlow<Map<String, Station>> = _stations.asStateFlow()

    private val _links = MutableStateFlow<List<RfLink>>(emptyList())
    val links: StateFlow<List<RfLink>> = _links.asStateFlow()

    private val _track = MutableStateFlow<List<TrackPoint>>(emptyList())
    val track: StateFlow<List<TrackPoint>> = _track.asStateFlow()

    /** Live trail of each station received in this session (#11); not restored from cache. */
    private val _trails = MutableStateFlow<Map<String, List<TrackPoint>>>(emptyMap())
    val trails: StateFlow<Map<String, List<TrackPoint>>> = _trails.asStateFlow()

    /** Local RF evidence (TNC only) for my links and range records (#4, #14). */
    private val _rfEvents = MutableStateFlow<List<RfEvent>>(emptyList())
    val rfEvents: StateFlow<List<RfEvent>> = _rfEvents.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _status = MutableStateFlow(LinkStatus())
    val status: StateFlow<LinkStatus> = _status.asStateFlow()

    private val _monitor = MutableStateFlow<List<String>>(emptyList())
    val monitor: StateFlow<List<String>> = _monitor.asStateFlow()

    private val _connectionLog = MutableStateFlow<List<ConnectionEvent>>(emptyList())
    val connectionLog: StateFlow<List<ConnectionEvent>> = _connectionLog.asStateFlow()

    // Keep results per request so rapid navigation or two compositors cannot lose an acknowledgement.
    /** WX observations received during this process; persisted history is a later #58 milestone. */
    private val _weatherHistory = MutableStateFlow<Map<String, List<AprsWeather>>>(emptyMap())
    val weatherHistory: StateFlow<Map<String, List<AprsWeather>>> = _weatherHistory.asStateFlow()

    private val _sendResults = MutableStateFlow<Map<String, OutboundSendResult>>(emptyMap())
    val sendResults: StateFlow<Map<String, OutboundSendResult>> = _sendResults.asStateFlow()

    fun reportSendResult(result: OutboundSendResult) {
        _sendResults.update { (it + (result.requestId to result)).entries.toList().takeLast(64).associate { e -> e.key to e.value } }
    }

    private val _beaconResults = MutableStateFlow<Map<String, OutboundBeaconResult>>(emptyMap())
    val beaconResults: StateFlow<Map<String, OutboundBeaconResult>> = _beaconResults.asStateFlow()
    fun reportBeaconResult(result: OutboundBeaconResult) {
        _beaconResults.update {
            (it + (result.requestId to result)).entries.toList().takeLast(32).associate { e -> e.key to e.value }
        }
    }

    // ---------- 0.1.9: logs (#51), objetos (#48), rotas, reenvio (#47), bateria (#49) ----------
    private val _packetLog = MutableStateFlow<List<PacketLogEntry>>(emptyList())
    /** Mais recentes primeiro; espelho em memória das últimas linhas do banco. */
    val packetLog: StateFlow<List<PacketLogEntry>> = _packetLog.asStateFlow()
    private const val PACKET_LOG_MEMORY = 3000
    private var packetLogStore: PacketLogStore? = null
    private val packetIds = AtomicLong(System.currentTimeMillis() * 10)
    private var logsSincePrune = 0

    private val _myObjects = MutableStateFlow<List<MyObject>>(emptyList())
    val myObjects: StateFlow<List<MyObject>> = _myObjects.asStateFlow()

    private val _routes = MutableStateFlow<List<SavedRoute>>(emptyList())
    val routes: StateFlow<List<SavedRoute>> = _routes.asStateFlow()

    /** Próximo reenvio agendado por mensagem local (#47): id → (tentativa atual 1-based, horário). */
    private val _retryAt = MutableStateFlow<Map<Long, Pair<Int, Long>>>(emptyMap())
    val retryAt: StateFlow<Map<Long, Pair<Int, Long>>> = _retryAt.asStateFlow()
    fun setRetry(id: Long, attempt: Int, at: Long?) {
        _retryAt.update { if (at == null) it - id else it + (id to (attempt to at)) }
    }

    private val _batterySamples = MutableStateFlow<List<Pair<Long, Int>>>(emptyList())
    val batterySamples: StateFlow<List<Pair<Long, Int>>> = _batterySamples.asStateFlow()
    fun addBatterySample(level: Int, now: Long = System.currentTimeMillis()) {
        _batterySamples.update { list ->
            val last = list.lastOrNull()
            // Charging resets the series so the estimate never mixes charge and discharge.
            val base = if (last != null && level > last.second) emptyList() else list
            (base + (now to level)).filter { now - it.first <= 6 * 3_600_000L }.takeLast(100)
        }
    }

    private var nextId = System.currentTimeMillis()
    private var stationStore: StationStore? = null
    private var messageStore: MessageStore? = null
    private var appContext: Context? = null
    private val diskWorker = Executors.newSingleThreadExecutor { r -> Thread(r, "aprs-station-db").apply { isDaemon = true } }
    private val stationCacheEpoch = AtomicLong(0L)
    private const val RETENTION_MS = 30L * 24 * 60 * 60 * 1000
    private const val RF_EVENTS_RETENTION_MS = 365L * 24 * 60 * 60 * 1000
    @Volatile var retentionPolicy: RetentionPolicy = RetentionPolicy()
    private var addsSinceRetention = 0

    fun trackRetentionDays(context: Context): Int =
        Settings.readOption(context, "track_retention_days", "7").toIntOrNull()?.coerceIn(1, 365) ?: 7

    @Synchronized fun init(context: Context) {
        if (stationStore != null) return
        appContext = context.applicationContext
        preferences = context.applicationContext.getSharedPreferences("pt2vhf_aprs", Context.MODE_PRIVATE)
        _favorites.value = preferences?.getStringSet("favorite_stations", emptySet())?.toSet().orEmpty()
        retentionPolicy = Settings.load(context).retention
        stationStore = StationStore(context.applicationContext)
        val msgStore = MessageStore(context.applicationContext)
        messageStore = msgStore
        // Messages are read synchronously so a conversation never opens empty after an update.
        runCatching { msgStore.migrateLegacyJson(File(context.filesDir, "messages.json")) }
        runCatching {
            val all = msgStore.readAll()
            val victims = retentionVictims(all, retentionPolicy)
            if (victims.isNotEmpty()) msgStore.delete(victims)
            val kept = all.filter { it.id !in victims }
            // #60: no retry survives a process restart, so a pending send must not stay "tentando".
            val settled = interruptPending(kept)
            val interrupted = settled.filterIndexed { i, m -> m !== kept[i] }
            if (interrupted.isNotEmpty()) msgStore.upsert(interrupted)
            _messages.value = settled
            nextId = maxOf(nextId, (kept.maxOfOrNull { it.id } ?: 0) + 1)
        }
        packetLogStore = PacketLogStore(context.applicationContext)
        loadObjects(); loadRoutes()
        val trackDays = trackRetentionDays(context)
        val restoreEpoch = stationCacheEpoch.get()
        diskWorker.execute {
            runCatching {
                val store = stationStore ?: return@execute
                val now = System.currentTimeMillis()
                val since = now - RETENTION_MS
                val trackSince = now - trackDays * 86_400_000L
                store.prune(since, trackSince, now - RF_EVENTS_RETENTION_MS)
                val previousStations = store.readStations(since)
                val previousLinks = store.readLinks(since)
                val previousTrack = store.readTrack(trackSince)
                val previousEvents = store.readRfEvents(now - RF_EVENTS_RETENTION_MS)
                // A packet received during restore must never be replaced by older cache data.
                _stations.update { live ->
                    if (restoreEpoch != stationCacheEpoch.get()) return@update live
                    val combined = previousStations.toMutableMap()
                    for ((call, fresh) in live) {
                        if ((combined[call]?.lastHeard ?: 0L) <= fresh.lastHeard) combined[call] = fresh
                    }
                    combined
                }
                _links.update { current ->
                    if (restoreEpoch != stationCacheEpoch.get()) return@update current
                    val combined = (previousLinks + current).groupBy { it.from to it.to }
                    combined.values.map { list -> list.maxBy { it.lastSeen } }.takeLast(2000)
                }
                _track.update { current ->
                    (previousTrack + current).distinctBy { it.recordedAt }.sortedBy { it.recordedAt }.takeLast(10000)
                }
                _rfEvents.update { current -> (previousEvents + current).sortedBy { it.time }.takeLast(20000) }
            }
            runCatching {
                val logStore = packetLogStore ?: return@execute
                logStore.prune(logRetentionSince(), logMaxRows())
                val stored = logStore.readRecent(PACKET_LOG_MEMORY)
                _packetLog.update { live -> (live + stored).distinctBy { it.id }.sortedByDescending { it.time }.take(PACKET_LOG_MEMORY) }
            }
        }
    }

    // ---------- Aba Logs (#51) ----------

    fun logRetentionDays(): Int = appContext?.let { Settings.readOption(it, "log_retention_days", "7").toIntOrNull() } ?: 7
    fun logMaxRows(): Int = appContext?.let { Settings.readOption(it, "log_max", "20000").toIntOrNull() } ?: 20000
    private fun logRetentionSince(): Long = logRetentionDays().let { if (it <= 0) 0L else System.currentTimeMillis() - it * 86_400_000L }

    fun logPacket(direction: String, medium: Medium, raw: String, now: Long = System.currentTimeMillis()) {
        val clean = raw.trimEnd('\r', '\n')
        if (clean.isEmpty() || clean.startsWith("#")) return
        val packet = AprsPacket.parse(clean)
        val entry = PacketLogEntry(packetIds.incrementAndGet(), now, direction, medium, clean, packet?.source ?: "",
            packet?.destination ?: "", packet?.let { packetType(it.info) } ?: "other")
        _packetLog.update { (listOf(entry) + it).take(PACKET_LOG_MEMORY) }
        diskWorker.execute {
            runCatching {
                packetLogStore?.insert(listOf(entry))
                if (++logsSincePrune >= 500) { logsSincePrune = 0; packetLogStore?.prune(logRetentionSince(), logMaxRows()) }
            }
        }
    }

    fun applyLogRetention() {
        val since = logRetentionSince()
        _packetLog.update { list -> list.filter { it.time >= since }.take(logMaxRows().takeIf { it > 0 } ?: PACKET_LOG_MEMORY) }
        diskWorker.execute { runCatching { packetLogStore?.prune(since, logMaxRows()) } }
    }

    suspend fun clearPacketLog(): Boolean {
        _packetLog.value = emptyList()
        val store = packetLogStore ?: return false
        return withContext(Dispatchers.IO) { runCatching { diskWorker.submit<Boolean> { store.clear(); true }.get() }.getOrDefault(false) }
    }

    /** Todo o histórico guardado (mais recentes primeiro), para exportação. */
    suspend fun allPacketLog(): List<PacketLogEntry> {
        val store = packetLogStore ?: return _packetLog.value
        return withContext(Dispatchers.IO) {
            runCatching { diskWorker.submit<List<PacketLogEntry>> { store.readRecent(Int.MAX_VALUE) }.get() }
                .getOrDefault(_packetLog.value)
        }
    }

    /**
     * #58.6: pacotes guardados de uma estação (e definições de telemetria para ela), mais recentes
     * primeiro, somando os que ainda estão só na memória. Lido na fila de disco, depois das gravações.
     */
    suspend fun stationPackets(call: String, limit: Int = 5000): List<PacketLogEntry> {
        val live = _packetLog.value.filter { belongsToStation(it, call) }
        val store = packetLogStore ?: return live
        val stored = withContext(Dispatchers.IO) {
            runCatching { diskWorker.submit<List<PacketLogEntry>> { store.readForStation(call, limit) }.get() }
                .getOrElse { emptyList() }
        }
        return (live + stored).distinctBy { it.id }.sortedByDescending { it.time }.take(limit)
    }

    fun packetLogCount(): Long = packetLogStore?.count() ?: 0L

    // ---------- Meus objetos (#48) ----------

    private fun loadObjects() {
        val ctx = appContext ?: return
        _myObjects.value = runCatching {
            val arr = JSONArray(Settings.readOption(ctx, "my_objects", "[]"))
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                MyObject(o.getLong("id"), o.getString("name"), o.optBoolean("item"), o.getDouble("lat"), o.getDouble("lon"),
                    o.optString("table", "/").firstOrNull() ?: '/', o.optString("symbol", "r").firstOrNull() ?: 'r',
                    o.optString("comment"), o.optInt("interval", 10), o.optBoolean("active", true), o.optLong("last", 0L))
            }
        }.getOrDefault(emptyList())
    }

    private fun saveObjects() {
        val ctx = appContext ?: return
        val arr = JSONArray()
        _myObjects.value.forEach { o ->
            arr.put(JSONObject().put("id", o.id).put("name", o.name).put("item", o.item).put("lat", o.lat).put("lon", o.lon)
                .put("table", o.symbolTable.toString()).put("symbol", o.symbol.toString()).put("comment", o.comment)
                .put("interval", o.intervalMin).put("active", o.active).put("last", o.lastSent))
        }
        Settings.saveOption(ctx, "my_objects", arr.toString())
    }

    fun upsertObject(obj: MyObject) {
        _myObjects.update { list -> if (list.any { it.id == obj.id }) list.map { if (it.id == obj.id) obj else it } else list + obj }
        saveObjects()
    }

    fun removeObject(id: Long) { _myObjects.update { list -> list.filter { it.id != id } }; saveObjects() }

    fun markObjectSent(id: Long, at: Long) {
        _myObjects.update { list -> list.map { if (it.id == id) it.copy(lastSent = at) else it } }
        saveObjects()
    }

    // ---------- Rotas (GPX) ----------

    private fun routesFile(): File? = appContext?.let { File(it.filesDir, "routes.json") }

    private fun loadRoutes() {
        val file = routesFile() ?: return
        if (!file.exists()) return
        _routes.value = runCatching {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                val pts = o.getJSONArray("points")
                SavedRoute(o.getLong("id"), o.getString("name"), o.optBoolean("visible", true),
                    (0 until pts.length()).map { j -> pts.getJSONArray(j).let { RoutePoint(it.getDouble(0), it.getDouble(1)) } })
            }
        }.getOrDefault(emptyList())
    }

    private fun saveRoutes() {
        val file = routesFile() ?: return
        val arr = JSONArray()
        _routes.value.forEach { r ->
            val pts = JSONArray()
            r.points.forEach { pts.put(JSONArray().put(it.lat).put(it.lon)) }
            arr.put(JSONObject().put("id", r.id).put("name", r.name).put("visible", r.visible).put("points", pts))
        }
        val snapshot = arr.toString()
        diskWorker.execute { runCatching { file.writeText(snapshot) } }
    }

    fun addRoute(name: String, points: List<RoutePoint>) {
        _routes.update { it + SavedRoute(System.currentTimeMillis(), name, true, points) }
        saveRoutes()
    }

    fun setRouteVisible(id: Long, visible: Boolean) {
        _routes.update { list -> list.map { if (it.id == id) it.copy(visible = visible) else it } }
        saveRoutes()
    }

    fun removeRoute(id: Long) { _routes.update { list -> list.filter { it.id != id } }; saveRoutes() }

    fun toggleFavorite(call: String) {
        _favorites.update { current -> if (call in current) current - call else current + call }
        preferences?.edit()?.putStringSet("favorite_stations", _favorites.value.toSet())?.apply()
    }

    fun followStation(call: String?) { _followCall.value = call }

    fun recordTrack(lat: Double, lon: Double, whenMs: Long = System.currentTimeMillis()) {
        val p = TrackPoint(lat, lon, whenMs)
        val last = _track.value.lastOrNull()
        if (last != null && whenMs - last.recordedAt < 15_000L &&
            haversineKm(last.lat, last.lon, lat, lon) < 0.01) return
        _track.update { (it + p).takeLast(10000) }
        diskWorker.execute { runCatching { stationStore?.saveTrack(p) } }
    }

    fun clearTrack() {
        _track.value = emptyList()
        diskWorker.execute { runCatching { stationStore?.clearTrack() } }
    }

    /** Applies a new track retention immediately (#5). */
    fun pruneTrack(days: Int) {
        val since = System.currentTimeMillis() - days.coerceIn(1, 365) * 86_400_000L
        _track.update { list -> list.filter { it.recordedAt >= since } }
        diskWorker.execute {
            runCatching {
                val now = System.currentTimeMillis()
                stationStore?.prune(now - RETENTION_MS, since, now - RF_EVENTS_RETENTION_MS)
            }
        }
    }

    fun clearRfEvents() {
        _rfEvents.value = emptyList()
        diskWorker.execute { runCatching { stationStore?.clearRfEvents() } }
    }

    /**
     * Remove station cache only after SQLite commits. On failure the UI keeps the old data,
     * and any station received during deletion is preserved. Shared by Settings and Stations.
     */
    suspend fun clearStations(): Boolean {
        val store = stationStore ?: return false
        val startedAt = System.currentTimeMillis()
        stationCacheEpoch.incrementAndGet()
        val success = withContext(Dispatchers.IO) {
            try {
                diskWorker.submit<Boolean> { store.clearStationsAndLinks(); true }.get()
            } catch (error: Exception) {
                appContext?.let { CrashLog.recordRecoverable(it, "clearStations", error) }
                false
            }
        }
        if (!success) return false
        _stations.update { map -> map.filterValues { it.lastHeard > startedAt } }
        _weatherHistory.update { history ->
            history.mapValues { (_, samples) -> samples.filter { it.timestamp > startedAt } }
                .filterValues { it.isNotEmpty() }
        }
        _links.update { it.filter { link -> link.lastSeen > startedAt } }
        _trails.update { history ->
            history.mapValues { (_, points) -> points.filter { it.recordedAt > startedAt } }
                .filterValues { it.isNotEmpty() }
        }
        _followCall.value = null
        return true
    }

    /**
     * #41: Delete only the IDs present at confirmation time, on the serialized database
     * worker, before removing them from the displayed list. Messages received during
     * deletion remain untouched, and failure must not pretend the deletion succeeded.
     */
    suspend fun clearMessages(category: MessageCategory? = null): Boolean {
        val store = messageStore ?: return false
        val victims = deletionVictimIds(_messages.value, category)
        if (victims.isEmpty()) return true
        val success = withContext(Dispatchers.IO) {
            try {
                diskWorker.submit<Boolean> { store.delete(victims); true }.get()
            } catch (error: Exception) {
                appContext?.let { CrashLog.recordRecoverable(it, "clearMessages", error) }
                false
            }
        }
        if (!success) return false
        _messages.update { list -> list.filterNot { it.id in victims } }
        _retryAt.update { map -> map.filterKeys { it !in victims } }
        return true
    }


    fun countMessages(category: MessageCategory?): Int = _messages.value.count { m ->
        val c = messageCategory(m)
        when (category) {
            null -> true
            MessageCategory.PRIVATE -> c == MessageCategory.PRIVATE
            else -> c != MessageCategory.PRIVATE
        }
    }

    /** #50: importa mensagens de um backup sem duplicar ids existentes. */
    fun importMessages(list: List<ChatMessage>): Int {
        val existing = _messages.value.map { it.id }.toHashSet()
        val fresh = list.filter { it.id !in existing }
        if (fresh.isEmpty()) return 0
        _messages.update { (it + fresh).sortedWith(compareBy<ChatMessage> { m -> m.time }.thenBy { m -> m.id }) }
        nextId = maxOf(nextId, (fresh.maxOfOrNull { it.id } ?: 0) + 1)
        diskWorker.execute { runCatching { messageStore?.upsert(fresh) } }
        return fresh.size
    }

    // ---------- Retenção (#26) ----------

    fun retentionPreview(policy: RetentionPolicy): Int = retentionVictims(_messages.value, policy).size

    /** Applies [policy] now; returns how many messages were removed. Never touches pending sends. */
    fun applyRetention(policy: RetentionPolicy = retentionPolicy): Int {
        retentionPolicy = policy
        val victims = retentionVictims(_messages.value, policy)
        if (victims.isEmpty()) return 0
        _messages.update { list -> list.filter { it.id !in victims } }
        diskWorker.execute { runCatching { messageStore?.delete(victims) } }
        return victims.size
    }

    fun messageStorageBytes(): Long = messageStore?.sizeBytes() ?: 0L

    fun containsMessage(id: Long): Boolean = _messages.value.any { it.id == id }

    fun message(id: Long?): ChatMessage? = id?.let { wanted -> _messages.value.firstOrNull { it.id == wanted } }

    fun updateStatus(change: (LinkStatus) -> LinkStatus) = _status.update(change)

    fun monitorLine(line: String) = _monitor.update { (it + line).takeLast(200) }

    fun logConnection(source: String, text: String) {
        _connectionLog.update { (it + ConnectionEvent(System.currentTimeMillis(), source, text)).takeLast(300) }
    }

    private fun recordTrail(call: String, lat: Double, lon: Double, now: Long) {
        _trails.update { map ->
            val list = map[call].orEmpty()
            val last = list.lastOrNull()
            if (last != null && haversineKm(last.lat, last.lon, lat, lon) < 0.02) map
            else map + (call to (list + TrackPoint(lat, lon, now)).takeLast(500))
        }
    }

    /** Processa um pacote recebido; devolve a mensagem endereçada a nós, se houver. */
    fun ingest(packet: AprsPacket, medium: Medium, ownCall: String, now: Long = System.currentTimeMillis()): ChatMessage? {
        _status.update { it.copy(rxPackets = it.rxPackets + 1) }
        val digis = packet.rfDigipeaters
        // A trilha de um pacote APRS-IS descreve um caminho anunciado, não uma recepção RF
        // feita pelo telefone. Somente o TNC físico pode registrar evidência RF local.
        val observedRfPath = localRfEvidenceLinks(packet, medium == Medium.RF)
        if (observedRfPath.isNotEmpty()) {
            val newLinks = observedRfPath.map { (a, b) -> RfLink(a, b, now) }
            _links.update { old ->
                (old.filter { l -> newLinks.none { it.from == l.from && it.to == l.to } } + newLinks).takeLast(2000)
            }
            diskWorker.execute { runCatching { stationStore?.saveLinks(newLinks) } }
            if (packet.source == ownCall) _status.update { it.copy(beaconsDigipeated = it.beaconsDigipeated + 1) }
        }
        // #48: objetos e itens aparecem pelo nome; objeto encerrado sai do mapa.
        parseObject(packet.info)?.let { obj ->
            if (!obj.alive) {
                _stations.update { it - obj.name }
            } else {
                val pos = obj.position
                var persisted: Station? = null
                _stations.update { map ->
                    val existing = map[obj.name]
                    val station = Station(obj.name, pos.lat, pos.lon, pos.symbolTable, pos.symbol,
                        decodeAprsText(pos.comment), now, medium, packet.path.joinToString(","), pos.speedKmh, pos.course,
                        existing?.heardBy ?: emptySet(), firstSeen = existing?.firstSeen ?: now)
                    persisted = station
                    map + (obj.name to station)
                }
                persisted?.let { st -> diskWorker.execute { runCatching { stationStore?.saveStation(st) } } }
            }
        }
        parseAprsWeather(packet.info, now)?.let { observation ->
            _weatherHistory.update { history ->
                val previous = history[packet.source].orEmpty().filter { it.timestamp >= now - 7 * 86_400_000L }
                history + (packet.source to (previous + observation).takeLast(1000))
            }
        }
        val position = parsePosition(packet.info)
        if (medium == Medium.RF && packet.thirdPartyGateway == null) {
            val own = _status.value
            val known = _stations.value
            rfEventFor(packet, ownCall, position?.lat ?: known[packet.source]?.lat,
                position?.lon ?: known[packet.source]?.lon, own.lat, own.lon,
                { call -> known[call]?.let { it.lat to it.lon } }, now)?.let { event ->
                _rfEvents.update { (it + event).takeLast(20000) }
                diskWorker.execute { runCatching { stationStore?.saveRfEvent(event) } }
            }
        }
        position?.let { pos ->
            var persisted: Station? = null
            _stations.update { map ->
                val existing = map[packet.source]
                val heardBy = (existing?.heardBy ?: emptySet()) +
                    (if (medium == Medium.RF && packet.thirdPartyGateway == null)
                        digis.take(1) else emptyList())
                val station = Station(
                    packet.source, pos.lat, pos.lon, pos.symbolTable, pos.symbol, decodeAprsText(pos.comment), now, medium,
                    packet.path.joinToString(","), pos.speedKmh, pos.course, heardBy,
                    firstSeen = existing?.firstSeen ?: now,
                )
                persisted = station
                map + (packet.source to station)
            }
            recordTrail(packet.source, pos.lat, pos.lon, now)
            persisted?.let { station -> diskWorker.execute { runCatching { stationStore?.saveStation(station) } } }
        } ?: run {
            // A station can send a message/status without new coordinates.
            // Keep its last known position but refresh reception metadata.
            // Unknown stations without position are not plotted at (0,0).
            var refreshed: Station? = null
            _stations.update { existing ->
                val previous = existing[packet.source]
                if (previous == null || now <= previous.lastHeard) existing
                else {
                    previous.copy(lastHeard = now, medium = medium,
                        via = packet.path.joinToString(",")).let {
                        refreshed = it
                        existing + (packet.source to it)
                    }
                }
            }
            refreshed?.let { station ->
                diskWorker.execute { runCatching { stationStore?.saveStation(station) } }
            }
        }
        val msg = parseMessage(packet.info) ?: return null
        // Receive traffic for the configured full callsign and for the bare callsign.
        val ownBase = ownCall.substringBefore('-')
        val forUs = msg.addressee == ownCall || msg.addressee == ownBase
        // Reply-ack {MM}AA confirms our previously transmitted AA, independently of MM.
        if (forUs && msg.replyAckId != null) markAck(packet.source, msg.replyAckId, true)
        return when (msg.kind) {
            AprsMessage.Kind.ACK, AprsMessage.Kind.REJ -> {
                if (forUs) markAck(packet.source, msg.id, msg.kind == AprsMessage.Kind.ACK)
                null
            }
            else -> {
                val general = msg.kind == AprsMessage.Kind.BULLETIN || msg.addressee == "CQ" ||
                    msg.addressee.startsWith("CQ")
                // #73: mensagens entre outras estações também são guardadas (só leitura, modo "Todas"),
                // mas nunca geram ACK, notificação ou contador: só "forUs" é devolvido ao serviço.
                val thirdParty = !forUs && !general
                val via = if (medium == Medium.RF)
                    packet.thirdPartyGateway?.let { InternalText.VIA_RF_PREFIX + "iGate $it" } ?:
                    packet.rfDigipeaters.firstOrNull()?.let { InternalText.VIA_RF_PREFIX + it } ?: InternalText.VIA_RF_DIRECT
                else "APRS-IS"
                // Display text only (#27): IDs/ACK and the raw packet keep the original bytes.
                val text = decodeAprsText(msg.text)
                val duplicate = _messages.value.any {
                    !it.outgoing && it.from == packet.source && it.msgId != null && it.msgId == msg.id && it.text == text
                } || (thirdParty && msg.id == null && _messages.value.any {
                    !it.outgoing && it.from == packet.source && it.to == msg.addressee && it.text == text &&
                        now - it.time < 10 * 60_000L
                })
                if (duplicate) return null
                val chat = ChatMessage(nextId++, packet.source, msg.addressee, text, now, false, medium, via, msg.id,
                    InternalText.MSG_RECEIVED, msg.kind)
                add(chat)
                if (forUs) chat else null
            }
        }
    }

    fun add(message: ChatMessage) {
        _messages.update { it + message }
        diskWorker.execute { runCatching { messageStore?.upsert(listOf(message)) } }
        // Retention is evaluated on load and every 100 new records, not by a periodic job.
        if (++addsSinceRetention >= 100) {
            addsSinceRetention = 0
            applyRetention()
        }
    }

    fun newId(): Long = nextId++

    fun setStatus(id: Long, status: String) {
        var changed: ChatMessage? = null
        _messages.update { list ->
            list.map { if (it.id == id) it.copy(status = status).also { c -> changed = c } else it }
        }
        changed?.let { m -> diskWorker.execute { runCatching { messageStore?.upsert(listOf(m)) } } }
    }

    private fun markAck(from: String, msgId: String?, ack: Boolean) {
        if (msgId == null) return
        val changed = ArrayList<ChatMessage>()
        _messages.update { list ->
            changed.clear()
            list.map {
                if (it.outgoing && it.to == from && it.msgId == msgId)
                    it.copy(status = if (ack) InternalText.MSG_ACKED else InternalText.MSG_REJECTED).also { c -> changed += c }
                else it
            }
        }
        val snapshot = changed.toList()
        snapshot.forEach { setRetry(it.id, 0, null) }
        diskWorker.execute { runCatching { messageStore?.upsert(snapshot) } }
    }

    /** #60: chamado quando o serviço para; as retentativas agendadas morrem junto com ele. */
    fun interruptPendingMessages() {
        val changed = ArrayList<ChatMessage>()
        _messages.update { list ->
            changed.clear()
            interruptPending(list).also { out -> out.forEachIndexed { i, m -> if (m !== list[i]) changed += m } }
        }
        if (changed.isEmpty()) return
        _retryAt.update { it - changed.map { m -> m.id }.toSet() }
        val snapshot = changed.toList()
        diskWorker.execute { runCatching { messageStore?.upsert(snapshot) } }
    }

    fun isAcked(localId: Long): Boolean =
        _messages.value.firstOrNull { it.id == localId }?.status?.let {
            it == InternalText.MSG_ACKED || it == InternalText.MSG_REJECTED || it == InternalText.MSG_CANCELLED
        } == true
}
