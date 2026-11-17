package com.pt2vhf.aprs.core

import java.util.Locale

/**
 * #52 digi inteligente: decide cada pacote ouvido no TNC (RF→RF), sem Android.
 *
 * Base: indicativo próprio e aliases (trace), WIDE1-1 (fill-in), WIDEn-N até N máximo, dedupe, nunca repete o
 * próprio pacote, caminho de no máximo 8 saltos. Por cima disso, para os aliases genéricos (WIDE):
 * 1. viscoso com aprendizado: se um digi vizinho já cobre a origem, espera e cancela se ouvir o vizinho repetir;
 * 2. adaptativo à carga do canal (pacotes/min e tempo de ar estimado a 1200 bd);
 * 3. fill-in só de quem foi ouvido diretamente;
 * 4. prioridade para mensagens, ACK/REJ e emergência, que furam os limites;
 * 5. raio geográfico; 6. trap de WIDEn-N acima do máximo (reduz ou descarta); 7. motivo de cada decisão.
 */
data class DigiConfig(
    val myCall: String,
    val aliases: Set<String> = emptySet(),
    val fillIn: Boolean = true,
    val wideN: Boolean = true,
    val maxN: Int = 2,
    val trapDrop: Boolean = false,
    val directOnlyFillIn: Boolean = true,
    val viscous: Boolean = true,
    val viscousDelayMs: Long = 5_000L,
    val adaptive: Boolean = true,
    val busyPerMin: Int = 30,
    val saturatedPerMin: Int = 60,
    val radiusKm: Int = 0,
    val perSourceSec: Int = 60,
    val maxPerMinute: Int = 20,
    val dupeSec: Int = 30,
)

enum class DigiReason(val repeated: Boolean) {
    OWN_CALL(true), ALIAS(true), FILL_IN(true), WIDE(true), WIDE_TRAPPED(true),
    NOT_FOR_ME(false), OWN_PACKET(false), DUPLICATE(false), COVERED(false), BUSY(false), SATURATED(false),
    OUT_OF_RADIUS(false), SOURCE_RATE(false), GLOBAL_RATE(false), EXCESS_PATH(false), NOT_DIRECT(false),
    NON_RF(false), MALFORMED(false),
}

sealed class DigiDecision {
    /** Transmitir [line] depois de [delayMs] (0 = já). Com atraso, confirmar com [SmartDigipeater.release]. */
    data class Repeat(val line: String, val delayMs: Long, val reason: DigiReason, val priority: Boolean,
                      val coveredBy: String?, val token: Long) : DigiDecision()
    data class Drop(val reason: DigiReason) : DigiDecision()
}

/** Digipeater vizinho aprendido: quantos pacotes repetidos ouvimos dele e de quantas origens. */
data class DigiNeighbor(val call: String, val lastHeard: Long, val packets: Int, val sources: Int)

private val WIDE_N = Regex("^WIDE([1-7])-([1-7])$")
private const val MAX_DIGIS = 8
private const val COVER_WINDOW_MS = 15 * 60_000L
private const val LOAD_WINDOW_MS = 60_000L
/** Bytes extras por quadro AX.25 (endereços, flags, FCS, preâmbulo aproximado) para o tempo de ar. */
private const val FRAME_OVERHEAD_BYTES = 40

/** Mensagem, ACK/REJ ou emergência: passa na frente dos limites. Boletins não são prioridade. */
fun isPriorityPacket(destination: String, info: String): Boolean {
    val body = if (info.startsWith("}")) Tnc2.parse(info.substring(1))?.info ?: info else info
    val msg = parseMessage(body)
    if (msg != null && msg.kind != AprsMessage.Kind.BULLETIN && !msg.addressee.startsWith("BLN") &&
        !msg.addressee.startsWith("NWS")) return true
    if (body.uppercase(Locale.ROOT).contains("EMERGENCY")) return true
    return runCatching { parseMicE(destination, body)?.status == MicEStatus.EMERGENCY }.getOrDefault(false)
}

class SmartDigipeater(config: DigiConfig, private val random: () -> Double = Math::random) {
    private class Pending(val key: String, val line: String, val reason: DigiReason, var cancelledBy: String? = null,
                          var cancelled: Boolean = false)
    private class NeighborStats(var lastHeard: Long, var packets: Int, val sources: MutableSet<String> = HashSet())

    var config: DigiConfig = config
        @Synchronized set(value) {
            field = value
            dupes = GateDupeFilter(value.dupeSec * 1000L)
            global = RateLimiter(value.maxPerMinute)
        }

    private var dupes = GateDupeFilter(config.dupeSec * 1000L)
    private var global = RateLimiter(config.maxPerMinute)
    private val load = ArrayDeque<Pair<Long, Int>>()
    private val neighbors = HashMap<String, NeighborStats>()
    private val coveredBy = HashMap<String, Pair<String, Long>>()
    private val lastRepeatBySource = HashMap<String, Long>()
    private val pending = HashMap<Long, Pending>()
    private var nextToken = 1L
    private val stats = HashMap<DigiReason, Int>()

    /**
     * Decide o que fazer com um quadro TNC2 ouvido no TNC. [myLat]/[myLon] = posição do aparelho;
     * [knownPosition] devolve a última posição conhecida de uma estação (lat, lon) quando o pacote não traz.
     */
    @Synchronized
    fun onHeard(raw: String, now: Long, myLat: Double? = null, myLon: Double? = null,
                knownPosition: (String) -> Pair<Double, Double>? = { null }): DigiDecision {
        val pkt = Tnc2.parse(raw) ?: return drop(DigiReason.MALFORMED)
        val cfg = config
        val me = cfg.myCall.uppercase(Locale.ROOT)
        recordLoad(now, raw.length)
        learn(pkt, now, me)

        if (pkt.source == me) return drop(DigiReason.OWN_PACKET)
        if (pkt.path.any { it.removeSuffix("*").uppercase(Locale.ROOT) in setOf("TCPIP", "TCPXX") })
            return drop(DigiReason.NON_RF)
        if (pkt.path.any { it.length == 3 && it.startsWith("q") }) return drop(DigiReason.MALFORMED)

        val i = pkt.path.indexOfFirst { !it.endsWith("*") }
        if (i < 0) return drop(DigiReason.NOT_FOR_ME)
        val hop = pkt.path[i].uppercase(Locale.ROOT)
        val aliases = cfg.aliases.map { it.trim().uppercase(Locale.ROOT) }.filter { it.isNotEmpty() }.toSet()
        val replacement: List<String>
        val reason: DigiReason
        val wide = WIDE_N.matchEntire(hop)
        when {
            hop == me -> { replacement = listOf("$me*"); reason = DigiReason.OWN_CALL }
            hop in aliases -> { replacement = listOf("$me*"); reason = DigiReason.ALIAS }
            wide != null -> {
                var n = wide.groupValues[1].toInt()
                var s = wide.groupValues[2].toInt().coerceAtMost(n)
                if (n == 1) {
                    if (!cfg.fillIn) return drop(DigiReason.NOT_FOR_ME)
                    if (cfg.directOnlyFillIn && i > 0) return drop(DigiReason.NOT_DIRECT)
                    replacement = listOf("$me*", "WIDE1*"); reason = DigiReason.FILL_IN
                } else {
                    if (!cfg.wideN) return drop(DigiReason.NOT_FOR_ME)
                    val max = cfg.maxN.coerceIn(1, 7)
                    var trapped = false
                    if (n > max) {
                        if (cfg.trapDrop) return drop(DigiReason.EXCESS_PATH)
                        n = max; s = s.coerceAtMost(max); trapped = true
                    }
                    val left = s - 1
                    replacement = if (left == 0) listOf("$me*", "WIDE$n*") else listOf("$me*", "WIDE$n-$left")
                    reason = if (trapped) DigiReason.WIDE_TRAPPED else DigiReason.WIDE
                }
            }
            else -> return drop(DigiReason.NOT_FOR_ME)
        }
        val newPath = pkt.path.take(i) + replacement + pkt.path.drop(i + 1)
        if (newPath.size > MAX_DIGIS) return drop(DigiReason.EXCESS_PATH)

        val key = pkt.dupeKey()
        if (dupes.contains(key, now) || pending.values.any { it.key == key }) return drop(DigiReason.DUPLICATE)

        val generic = reason == DigiReason.FILL_IN || reason == DigiReason.WIDE || reason == DigiReason.WIDE_TRAPPED
        val priority = isPriorityPacket(pkt.destination, pkt.info)
        var delay = 0L
        var covered: String? = null
        if (generic && !priority) {
            if (cfg.radiusKm > 0 && myLat != null && myLon != null) {
                val pos = runCatching {
                    parsePosition(pkt.info)?.let { it.lat to it.lon }
                        ?: parseMicE(pkt.destination, pkt.info)?.position?.let { it.lat to it.lon }
                }.getOrNull() ?: knownPosition(pkt.source)
                if (pos != null && haversineKm(myLat, myLon, pos.first, pos.second) > cfg.radiusKm)
                    return drop(DigiReason.OUT_OF_RADIUS)
            }
            if (cfg.adaptive) {
                val (ppm, air) = channelLoadLocked(now)
                if (ppm >= cfg.saturatedPerMin || air >= 0.6) return drop(DigiReason.SATURATED)
                val directFillIn = reason == DigiReason.FILL_IN && i == 0
                if ((ppm >= cfg.busyPerMin || air >= 0.3) && !directFillIn) return drop(DigiReason.BUSY)
            }
            if (cfg.perSourceSec > 0) {
                val last = lastRepeatBySource[pkt.source]
                if (last != null && now - last < cfg.perSourceSec * 1000L) return drop(DigiReason.SOURCE_RATE)
            }
            if (!global.tryAcquire(now)) return drop(DigiReason.GLOBAL_RATE)
            if (cfg.viscous) {
                val c = coveredBy[pkt.source]
                if (c != null && now - c.second <= COVER_WINDOW_MS) {
                    covered = c.first
                    delay = cfg.viscousDelayMs + (random() * 1000).toLong()
                }
            }
        } else if (priority) {
            global.tryAcquire(now) // conta no limite, mas não bloqueia
        }

        dupes.isDuplicate(key, now)
        if (generic) lastRepeatBySource[pkt.source] = now
        val line = pkt.copy(path = newPath).line()
        val token = nextToken++
        if (delay > 0) pending[token] = Pending(key, line, reason) else count(reason)
        return DigiDecision.Repeat(line, delay, reason, priority, covered, token)
    }

    /** Fim do atraso viscoso: devolve a linha a transmitir, ou null se um vizinho já repetiu (cancelado). */
    @Synchronized
    fun release(token: Long): String? {
        val p = pending.remove(token) ?: return null
        if (p.cancelled) { count(DigiReason.COVERED); return null }
        count(p.reason)
        return p.line
    }

    /** Pacotes por minuto e fração estimada de tempo de ar (1200 bd) no último minuto. */
    @Synchronized fun channelLoad(now: Long): Pair<Int, Double> = channelLoadLocked(now)

    @Synchronized fun stats(): Map<DigiReason, Int> = HashMap(stats)

    @Synchronized fun neighbors(now: Long): List<DigiNeighbor> = neighbors
        .filter { now - it.value.lastHeard <= 6 * 3600_000L }
        .map { (call, n) -> DigiNeighbor(call, n.lastHeard, n.packets, n.sources.size) }
        .sortedByDescending { it.lastHeard }

    private fun drop(reason: DigiReason): DigiDecision.Drop { count(reason); return DigiDecision.Drop(reason) }

    private fun count(reason: DigiReason) { stats[reason] = (stats[reason] ?: 0) + 1 }

    private fun recordLoad(now: Long, bytes: Int) {
        load.addLast(now to bytes + FRAME_OVERHEAD_BYTES)
        trimLoad(now)
    }

    private fun trimLoad(now: Long) {
        while (load.isNotEmpty() && now - load.first().first >= LOAD_WINDOW_MS) load.removeFirst()
    }

    private fun channelLoadLocked(now: Long): Pair<Int, Double> {
        trimLoad(now)
        val bits = load.sumOf { it.second } * 8.0
        return load.size to (bits / 1200.0) / (LOAD_WINDOW_MS / 1000.0)
    }

    /** Aprende vizinhos: o último digi real que repetiu o pacote cobre a origem; cancela repetições pendentes. */
    private fun learn(pkt: Tnc2, now: Long, me: String) {
        val used = pkt.path.filter { it.endsWith("*") }.map { it.removeSuffix("*").uppercase(Locale.ROOT) }
        if (used.isEmpty()) return
        val digi = used.lastOrNull { !isNonRfHop(it) && it != me } ?: return
        val n = neighbors.getOrPut(digi) { NeighborStats(now, 0) }
        n.lastHeard = now; n.packets++
        if (n.sources.size < 500) n.sources += pkt.source
        coveredBy[pkt.source] = digi to now
        val key = pkt.dupeKey()
        pending.values.filter { it.key == key && !it.cancelled }.forEach { it.cancelled = true; it.cancelledBy = digi }
        if (coveredBy.size > 2000) coveredBy.entries.removeAll { now - it.value.second > COVER_WINDOW_MS }
        if (lastRepeatBySource.size > 2000) lastRepeatBySource.entries.removeAll { now - it.value > 3600_000L }
    }
}
