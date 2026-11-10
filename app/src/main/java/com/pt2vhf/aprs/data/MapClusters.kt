package com.pt2vhf.aprs.data

import kotlin.math.PI
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.tan

/*
 * #66: agrupamento de estações no mapa por grade de pixels no zoom atual (Web Mercator, tiles de 256 px).
 * Lógica pura: a UI converte cada estação em pixels e desenha um círculo com a contagem por grupo.
 */

/** Coordenada em pixels do mundo (Web Mercator) para o zoom pedido. */
fun mercatorPx(lat: Double, lon: Double, zoom: Double): Pair<Double, Double> {
    val size = 256.0 * 2.0.pow(zoom)
    val clampedLat = lat.coerceIn(-85.05112878, 85.05112878)
    val x = (lon + 180.0) / 360.0 * size
    val rad = clampedLat * PI / 180.0
    val y = (1.0 - ln(tan(rad) + 1.0 / kotlin.math.cos(rad)) / PI) / 2.0 * size
    return x to y
}

data class ClusterPoint(val key: String, val lat: Double, val lon: Double)

/** Grupo de estações; com uma só chave é uma estação solta. */
data class StationCluster(val keys: List<String>, val lat: Double, val lon: Double) {
    val size: Int get() = keys.size
}

/**
 * Junta pontos que caem na mesma célula de [cellPx] pixels. Células com menos de [minSize] pontos
 * ficam soltas. A ordem de saída é estável (pela primeira aparição da célula).
 */
fun clusterStations(points: List<ClusterPoint>, zoom: Double, cellPx: Double, minSize: Int = 3): List<StationCluster> {
    if (cellPx <= 0.0) return points.map { StationCluster(listOf(it.key), it.lat, it.lon) }
    val cells = LinkedHashMap<Pair<Long, Long>, MutableList<ClusterPoint>>()
    for (p in points) {
        val (x, y) = mercatorPx(p.lat, p.lon, zoom)
        cells.getOrPut(floor(x / cellPx).toLong() to floor(y / cellPx).toLong()) { ArrayList() } += p
    }
    val out = ArrayList<StationCluster>()
    for (members in cells.values) {
        if (members.size < minSize) members.forEach { out += StationCluster(listOf(it.key), it.lat, it.lon) }
        else out += StationCluster(members.map { it.key }, members.sumOf { it.lat } / members.size,
            members.sumOf { it.lon } / members.size)
    }
    return out
}
