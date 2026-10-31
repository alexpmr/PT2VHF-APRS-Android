package com.pt2vhf.aprs.data

import com.pt2vhf.aprs.core.haversineKm
import java.time.LocalDate
import java.time.ZoneId

/**
 * Predicados independentes da UI: ordenar NAO significa filtrar.
 * Intervalos sao inclusivos nas datas locais; dados indisponiveis nao ganham valores inventados.
 */
data class StationCriteria(
    val callsign: String = "",
    val sort: String = "distance", // distance, heard, first, callsign
    val descending: Boolean = false,
    val roleNames: Set<String> = emptySet(), // vazio = todas
    val source: String = "all", // all, rf, internet
    val favoritesOnly: Boolean = false,
    val receivedFrom: String = "",
    val receivedUntil: String = "",
    val firstFrom: String = "",
    val firstUntil: String = "",
    val minDistanceKm: Double? = null,
    val maxDistanceKm: Double? = null,
)

private fun startOfDay(value: String): Long? = runCatching {
    LocalDate.parse(value.trim()).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}.getOrNull()

private fun dayAfter(value: String): Long? = runCatching {
    LocalDate.parse(value.trim()).plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}.getOrNull()

fun selectStations(
    stations: Collection<Station>,
    criteria: StationCriteria,
    ownLat: Double?,
    ownLon: Double?,
    favorites: Set<String> = emptySet(),
): List<Station> {
    fun km(s: Station): Double? = if (ownLat != null && ownLon != null &&
        ownLat in -90.0..90.0 && ownLon in -180.0..180.0 &&
        s.lat in -90.0..90.0 && s.lon in -180.0..180.0)
        haversineKm(ownLat, ownLon, s.lat, s.lon) else null
    val from = startOfDay(criteria.receivedFrom)
    val until = dayAfter(criteria.receivedUntil)
    val firstFrom = startOfDay(criteria.firstFrom)
    val firstUntil = dayAfter(criteria.firstUntil)
    val selected = stations.filter { s ->
        val distance = km(s)
        (criteria.callsign.isBlank() || s.call.contains(criteria.callsign.trim(), ignoreCase = true)) &&
        (criteria.roleNames.isEmpty() || s.role in criteria.roleNames) &&
        (!criteria.favoritesOnly || s.call in favorites) &&
        (criteria.source == "all" || (criteria.source == "rf" && s.medium == Medium.RF) ||
            (criteria.source == "internet" && s.medium == Medium.INTERNET)) &&
        (from == null || s.lastHeard >= from) &&
        (until == null || s.lastHeard < until) &&
        (firstFrom == null || s.firstSeen >= firstFrom) &&
        (firstUntil == null || s.firstSeen < firstUntil) &&
        (criteria.minDistanceKm == null || (distance != null && distance >= criteria.minDistanceKm)) &&
        (criteria.maxDistanceKm == null || (distance != null && distance <= criteria.maxDistanceKm))
    }
    val compare: Comparator<Station> = when(criteria.sort) {
        "callsign" -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.call }
        "first" -> compareBy { it.firstSeen }
        "heard" -> compareBy { it.lastHeard }
        else -> if (ownLat == null || ownLon == null) compareBy { it.lastHeard }
            else compareBy { km(it) ?: Double.POSITIVE_INFINITY }
    }
    val order = if (criteria.descending) compare.reversed() else compare
    return selected.sortedWith(order.thenBy { it.call })
}
