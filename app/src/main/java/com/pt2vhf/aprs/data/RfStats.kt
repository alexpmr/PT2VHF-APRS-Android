package com.pt2vhf.aprs.data

import com.pt2vhf.aprs.core.AprsPacket
import com.pt2vhf.aprs.core.haversineKm

/*
 * Alcance RF e "meus enlaces" (#4, #14). Somente eventos vindos do TNC deste aparelho:
 * APRS-IS nunca vira contato RF direto.
 */

/**
 * Evidência local de um pacote recebido pelo TNC.
 *  - origem = eu: meu pacote voltou pelo último digipeater marcado com '*';
 *  - sem digipeater marcado: ouvi a estação direto;
 *  - com digipeater: ouvi a estação via o último digipeater que retransmitiu.
 */
fun rfEventFor(
    packet: AprsPacket,
    ownCall: String,
    stationLat: Double?,
    stationLon: Double?,
    ownLat: Double?,
    ownLon: Double?,
    digiPosition: (String) -> Pair<Double, Double>?,
    now: Long,
): RfEvent? {
    val digis = packet.rfDigipeaters
    val lastHop = digis.lastOrNull()
    fun dist(lat: Double?, lon: Double?): Double? =
        if (lat != null && lon != null && ownLat != null && ownLon != null) haversineKm(ownLat, ownLon, lat, lon) else null
    if (packet.source.equals(ownCall, ignoreCase = true)) {
        val digi = lastHop ?: return null // my own frame without '*' is just local echo
        val pos = digiPosition(digi)
        return RfEvent(digi, "digi", digi, false, now, dist(pos?.first, pos?.second),
            pos?.first, pos?.second, ownLat, ownLon)
    }
    val direct = lastHop == null
    return RfEvent(packet.source, "heard", lastHop ?: "", direct, now,
        dist(stationLat, stationLon),
        stationLat, stationLon, ownLat, ownLon)
}

data class OwnLinkSummary(
    val call: String,
    val kind: String,
    val via: String,
    val direct: Boolean,
    val count: Int,
    val lastSeen: Long,
    val maxDistanceKm: Double?,
)

fun ownLinkSummaries(events: List<RfEvent>, since: Long = 0L): List<OwnLinkSummary> =
    events.filter { it.time >= since }
        .groupBy { Triple(it.call, it.kind, it.direct) }
        .map { (key, list) ->
            val last = list.maxBy { it.time }
            OwnLinkSummary(key.first, key.second, last.via, key.third, list.size, last.time,
                list.mapNotNull { it.distanceKm }.maxOrNull())
        }
        .sortedByDescending { it.lastSeen }

data class RangeRecord(val call: String, val distanceKm: Double, val time: Long, val direct: Boolean, val via: String)

/** Maiores distâncias por estação no período, separadas em RF direto e via digipeater. */
fun rangeRecords(events: List<RfEvent>, since: Long, direct: Boolean, limit: Int = 10): List<RangeRecord> =
    events.asSequence()
        .filter { it.kind == "heard" && it.direct == direct && it.time >= since && it.distanceKm != null }
        .groupBy { it.call }
        .map { (_, list) -> list.maxBy { it.distanceKm!! } }
        .map { RangeRecord(it.call, it.distanceKm!!, it.time, it.direct, it.via) }
        .sortedByDescending { it.distanceKm }
        .take(limit)
