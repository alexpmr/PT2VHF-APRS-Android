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
    val sort: String = "distance", // distance, heard, first, callsign, type
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
    // #58.3 painel de filtros
    /** Países (ISO) pelo prefixo do indicativo; vazio = todos. */
    val countries: Set<String> = emptySet(),
    /** SSIDs 0-15 (sem SSID = 0); vazio = todos. */
    val ssids: Set<Int> = emptySet(),
    /** Símbolo "tabela+código" (ex.: "/_") ou só o código ("_"); vazio = todos. */
    val symbol: String = "",
    /** Visto nos últimos N minutos; 0 = qualquer época. */
    val lastSeenMinutes: Int = 0,
    val withPositionOnly: Boolean = false,
)

/** SSID do indicativo; sem SSID conta como 0. */
fun stationSsid(call: String): Int = call.substringAfter('-', "").toIntOrNull()?.takeIf { it in 0..15 } ?: 0

/** Posição conhecida: coordenadas válidas e diferentes de 0,0 (estação sem posição nunca é plotada em 0,0). */
fun hasKnownPosition(s: Station): Boolean =
    s.lat in -90.0..90.0 && s.lon in -180.0..180.0 && !(s.lat == 0.0 && s.lon == 0.0)

/** Quantos filtros estão ativos (#58.2: contador no botão de filtro). Ordenação não conta. */
fun activeStationFilterCount(c: StationCriteria): Int = listOf(
    c.callsign.isNotBlank(), c.roleNames.isNotEmpty(), c.source != "all", c.favoritesOnly,
    c.receivedFrom.isNotBlank() || c.receivedUntil.isNotBlank(), c.firstFrom.isNotBlank() || c.firstUntil.isNotBlank(),
    c.minDistanceKm != null || c.maxDistanceKm != null, c.countries.isNotEmpty(), c.ssids.isNotEmpty(),
    c.symbol.isNotBlank(), c.lastSeenMinutes > 0, c.withPositionOnly,
).count { it }

private fun symbolMatches(s: Station, wanted: String): Boolean = when (wanted.length) {
    0 -> true
    1 -> s.symbol == wanted[0]
    else -> s.symbolTable == wanted[0] && s.symbol == wanted[1]
}

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
    now: Long = System.currentTimeMillis(),
    countryOf: (String) -> String? = { com.pt2vhf.aprs.core.callsignCountry(it) },
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
        (criteria.callsign.isBlank() || s.call.contains(criteria.callsign.trim(), ignoreCase = true) ||
            s.comment.contains(criteria.callsign.trim(), ignoreCase = true)) &&
        (criteria.countries.isEmpty() || countryOf(s.call) in criteria.countries) &&
        (criteria.ssids.isEmpty() || stationSsid(s.call) in criteria.ssids) &&
        symbolMatches(s, criteria.symbol.trim()) &&
        (criteria.lastSeenMinutes <= 0 || now - s.lastHeard <= criteria.lastSeenMinutes * 60_000L) &&
        (!criteria.withPositionOnly || hasKnownPosition(s)) &&
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
    val byCall = compareBy<Station, String>(String.CASE_INSENSITIVE_ORDER) { it.call }
    // #58.6: estações sem distância conhecida ficam no fim nos dois sentidos, com desempate estável por indicativo.
    if (criteria.sort == "distance") {
        val (known, unknown) = selected.partition { km(it) != null }
        val byKm = compareBy<Station> { km(it)!! }.let { if (criteria.descending) it.reversed() else it }
        return known.sortedWith(byKm.then(byCall)) + unknown.sortedWith(byCall)
    }
    // Por tipo: o sentido inverte a ordem dos grupos; dentro do grupo, sempre A-Z.
    if (criteria.sort == "type") {
        val byType = compareBy<Station> { stationTypeRank(it.role) }
        return selected.sortedWith((if (criteria.descending) byType.reversed() else byType).then(byCall))
    }
    val compare: Comparator<Station> = when(criteria.sort) {
        "callsign" -> byCall
        "first" -> compareBy { it.firstSeen }
        else -> compareBy { it.lastHeard }
    }
    val order = if (criteria.descending) compare.reversed() else compare
    return selected.sortedWith(order.then(byCall))
}

/**
 * Ordem estável dos tipos para "ordenar por tipo" (#58.6): móveis primeiro, depois fixas,
 * clima, infraestrutura (digi, iGate) e por fim as demais. Documentada no CHANGELOG.
 */
val STATION_TYPE_ORDER = listOf(InternalText.ROLE_MOBILE, InternalText.ROLE_PERSON,
    InternalText.ROLE_AIRCRAFT, InternalText.ROLE_VESSEL, InternalText.ROLE_STATION, InternalText.ROLE_WEATHER,
    InternalText.ROLE_DIGI, InternalText.ROLE_IGATE)

fun stationTypeRank(role: String): Int = STATION_TYPE_ORDER.indexOf(role).let { if (it < 0) STATION_TYPE_ORDER.size else it }

/**
 * Atalho de ordenação em um toque (#58.6): tocar em outro critério ordena por ele em sentido
 * natural; tocar de novo no critério ativo inverte o sentido.
 */
fun toggleStationSort(currentSort: String, currentDescending: Boolean, tapped: String): Pair<String, Boolean> =
    if (tapped == currentSort) tapped to !currentDescending
    else tapped to (tapped == "heard") // "Recentes" começa pela mais recente.
