package com.pt2vhf.aprs.data

import com.pt2vhf.aprs.core.AprsPacket
import com.pt2vhf.aprs.core.AprsWeather
import com.pt2vhf.aprs.core.PathSummary
import com.pt2vhf.aprs.core.TelemetryDefinitions
import com.pt2vhf.aprs.core.TelemetryFrame
import com.pt2vhf.aprs.core.applyTelemetryDefinition
import com.pt2vhf.aprs.core.describePath
import com.pt2vhf.aprs.core.haversineKm
import com.pt2vhf.aprs.core.parseAprsWeather
import com.pt2vhf.aprs.core.parsePosition
import com.pt2vhf.aprs.core.parseTelemetryFrame
import com.pt2vhf.aprs.core.qIsRfGate
import com.pt2vhf.aprs.core.telemetryDefinitionTarget
import java.util.Locale

/**
 * Ficha da estação (#58.4/#58.6) calculada só a partir dos pacotes brutos guardados localmente
 * (aba Logs, #51). Nada é estimado: campo sem dado fica nulo e a UI mostra "não informado".
 * Sem dependências Android para ser testável na JVM.
 */
data class ReceptionSummary(
    val windowMs: Long,
    val packetsInWindow: Int,
    /** iGates RF (qAR/qAr/qAo/qAO) que entregaram pacotes desta estação no período, mais recentes primeiro. */
    val igates: List<String>,
    val farthestIgate: String?,
    val farthestIgateKm: Double?,
    /** Último salto do último pacote: último digipeater identificado ou, se direto, o iGate. */
    val lastHop: String?,
    val lastHopKm: Double?,
    val lastIgate: String?,
    /** Pacotes recebidos pelo TNC deste aparelho (RF local), separados dos do APRS-IS. */
    val localRfPackets: Int,
    val localRfDirect: Int,
    val internetPackets: Int,
)

data class StationInsights(
    val call: String,
    val totalPackets: Int,
    val firstPacketAt: Long?,
    val lastPacketAt: Long?,
    val lastPacket: PacketLogEntry?,
    val lastPath: PathSummary?,
    val reception: ReceptionSummary,
    val weather: List<AprsWeather>,
    val telemetry: List<TelemetryFrame>,
    val definitions: TelemetryDefinitions,
    val altitudeM: Double?,
    val phg: String?,
    /** Pacotes da estação, mais recentes primeiro (aba Bruto). */
    val packets: List<PacketLogEntry>,
    /** Posições transmitidas pela estação, mais antigas primeiro (aba Trilha, #58.4). */
    val positions: List<TrackPoint> = emptyList(),
)

/** Pontos da trilha dentro do período (#58.4: 1 h, 6 h, 1 d, 3 d, 7 d), em ordem de tempo. */
fun trailWithin(points: List<TrackPoint>, now: Long, periodMs: Long): List<TrackPoint> =
    points.filter { now - it.recordedAt in 0..periodMs }.sortedBy { it.recordedAt }

/** Distância total percorrida somando os trechos entre pontos consecutivos. */
fun trailDistanceKm(points: List<TrackPoint>): Double =
    points.zipWithNext().sumOf { (a, b) -> haversineKm(a.lat, a.lon, b.lat, b.lon) }

private val ALTITUDE = Regex("/A=(-?\\d{6})")
private val PHG = Regex("PHG(\\d{4})")

/** Indicativo sem SSID (PT2VHF-9 → PT2VHF). */
fun baseCallsign(call: String): String = call.uppercase(Locale.ROOT).substringBefore('-')

/** Outros SSIDs vistos do mesmo operador (#58.4), mais recentes primeiro. */
fun otherSsids(call: String, stations: Collection<Station>): List<Station> {
    val base = baseCallsign(call)
    return stations.filter { it.call != call && baseCallsign(it.call) == base }
        .sortedByDescending { it.lastHeard }
}

/**
 * Pacotes que pertencem a esta estação: os que ela originou e as definições de telemetria
 * (PARM/UNIT/EQNS/BITS) endereçadas a ela, mesmo quando enviadas por outra estação.
 */
fun belongsToStation(entry: PacketLogEntry, call: String): Boolean {
    if (entry.direction != "RX") return false
    if (entry.source.equals(call, ignoreCase = true)) return true
    val packet = AprsPacket.parse(entry.raw) ?: return false
    return telemetryDefinitionTarget(packet.info)?.first == call.uppercase(Locale.ROOT)
}

fun buildStationInsights(
    call: String,
    entries: List<PacketLogEntry>,
    stations: Map<String, Station>,
    now: Long,
    windowMs: Long = 24 * 3_600_000L,
    duplicateWindowMs: Long = 30_000L,
): StationInsights {
    val target = call.uppercase(Locale.ROOT)
    val mine = entries.filter { belongsToStation(it, target) }.sortedBy { it.time }
    val parsed = mine.mapNotNull { e -> AprsPacket.parse(e.raw)?.let { e to it } }
    val own = parsed.filter { (_, p) -> p.source.equals(target, ignoreCase = true) }

    // Duplicatas (mesmo conteúdo por RF e APRS-IS, ou repetido por vários digis) contam uma vez nas séries.
    val lastSeenInfo = HashMap<String, Long>()
    val unique = own.filter { (e, p) ->
        val previous = lastSeenInfo[p.info]
        lastSeenInfo[p.info] = e.time
        previous == null || e.time - previous > duplicateWindowMs
    }

    var definitions = TelemetryDefinitions()
    parsed.forEach { (_, p) ->
        telemetryDefinitionTarget(p.info)?.takeIf { it.first == target }?.let { (_, text) ->
            definitions = applyTelemetryDefinition(definitions, text) ?: definitions
        }
    }
    val weather = unique.mapNotNull { (e, p) -> parseAprsWeather(p.info, e.time) }
    val telemetry = unique.mapNotNull { (e, p) -> parseTelemetryFrame(p.info, e.time) }

    val stationPos = stations[target]?.let { it.lat to it.lon }
    fun positionOf(p: AprsPacket): Pair<Double, Double>? =
        parsePosition(p.info)?.let { it.lat to it.lon } ?: stationPos
    fun km(from: Pair<Double, Double>?, call: String?): Double? {
        val other = call?.let { stations[it] } ?: return null
        from ?: return null
        if (from.first !in -90.0..90.0 || other.lat !in -90.0..90.0) return null
        return haversineKm(from.first, from.second, other.lat, other.lon)
    }

    val recent = own.filter { (e, _) -> now - e.time in 0..windowMs }
    val igateOrder = LinkedHashMap<String, Long>()
    var farthest: String? = null
    var farthestKm: Double? = null
    for ((e, p) in recent) {
        val path = describePath(p)
        val gate = path.igate?.takeIf { qIsRfGate(path.qCode) } ?: continue
        igateOrder[gate] = e.time
        val d = km(positionOf(p), gate)
        if (d != null && (farthestKm == null || d > farthestKm)) { farthestKm = d; farthest = gate }
    }
    val last = own.lastOrNull()
    val lastPath = last?.second?.let { describePath(it) }
    val lastIgate = lastPath?.igate?.takeIf { qIsRfGate(lastPath.qCode) }
    val lastHop = lastPath?.lastDigipeater ?: lastIgate
    val localRf = recent.filter { (e, p) -> e.medium == Medium.RF && p.thirdPartyGateway == null }

    val positionComment = own.lastOrNull { (_, p) -> parsePosition(p.info) != null }?.second?.let { parsePosition(it.info)?.comment }
    val anyText = own.asReversed().map { it.second.info }
    val altitude = (listOfNotNull(positionComment) + anyText).firstNotNullOfOrNull { ALTITUDE.find(it) }
        ?.groupValues?.get(1)?.toIntOrNull()?.let { it * 0.3048 }
    val phg = anyText.firstNotNullOfOrNull { PHG.find(it) }?.groupValues?.get(1)

    return StationInsights(
        call = target,
        totalPackets = own.size,
        firstPacketAt = own.firstOrNull()?.first?.time,
        lastPacketAt = last?.first?.time,
        lastPacket = last?.first,
        lastPath = lastPath,
        reception = ReceptionSummary(
            windowMs = windowMs,
            packetsInWindow = recent.size,
            igates = igateOrder.entries.sortedByDescending { it.value }.map { it.key },
            farthestIgate = farthest,
            farthestIgateKm = farthestKm,
            lastHop = lastHop,
            lastHopKm = last?.let { km(positionOf(it.second), lastHop) },
            lastIgate = lastIgate,
            localRfPackets = localRf.size,
            localRfDirect = localRf.count { (_, p) -> p.rfDigipeaters.isEmpty() },
            internetPackets = recent.count { (e, _) -> e.medium == Medium.INTERNET },
        ),
        weather = weather,
        telemetry = telemetry,
        definitions = definitions,
        altitudeM = altitude,
        phg = phg,
        packets = mine.asReversed(),
        positions = unique.mapNotNull { (e, p) -> parsePosition(p.info)?.let { TrackPoint(it.lat, it.lon, e.time) } }
            .filterNot { it.lat == 0.0 && it.lon == 0.0 },
    )
}
