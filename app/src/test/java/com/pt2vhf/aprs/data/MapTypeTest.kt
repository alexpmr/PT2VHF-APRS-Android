package com.pt2vhf.aprs.data

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MapTypeTest {
    @Test fun defaultIsStandardAndAutoFollowsSystem() {
        assertEquals(MapType.STANDARD, MapType.fromKey(null))
        assertEquals(MapType.STANDARD, MapType.fromKey("satelite"))
        assertEquals(MapType.DARK, MapType.fromKey("dark"))
        assertEquals(MapType.DARK, MapType.AUTO.resolve(systemDark = true))
        assertEquals(MapType.STANDARD, MapType.AUTO.resolve(systemDark = false))
        assertEquals(MapType.TOPO, MapType.TOPO.resolve(systemDark = true))
    }

    @Test fun attributionAlwaysCreditsOsm() {
        MapType.entries.forEach { assertTrue(it.attribution.contains("OpenStreetMap")) }
        assertTrue(MapType.TOPO.attribution.contains("OpenTopoMap"))
    }

    @Test fun standardAtFullBrightnessHasNoFilter() {
        assertNull(MapColors.forMap(MapType.STANDARD, 100))
        assertNull(MapColors.forMap(MapType.TOPO, 100))
    }

    @Test fun darkTurnsOsmBackgroundNearBlackAndTextLight() {
        val m = MapColors.forMap(MapType.DARK, 100)!!
        val land = MapColors.apply(m, 242, 239, 233) // fundo do OSM
        assertTrue(land.all { it < 40 })
        val text = MapColors.apply(m, 0, 0, 0) // nomes de cidades
        assertTrue(text.all { it > 190 })
        val water = MapColors.apply(m, 170, 211, 223) // água: azul-acinzentado escuro
        assertTrue(water[2] > water[0] && water[2] < 120)
    }

    @Test fun brightnessCombinesWithType() {
        val dim = MapColors.apply(MapColors.forMap(MapType.DARK, 50)!!, 0, 0, 0)
        val full = MapColors.apply(MapColors.forMap(MapType.DARK, 100)!!, 0, 0, 0)
        assertTrue(dim[0] < full[0])
        assertArrayEquals(MapColors.IDENTITY, MapColors.concat(MapColors.IDENTITY, MapColors.IDENTITY), 0f)
        val gray = MapColors.apply(MapColors.forMap(MapType.GRAY, 100)!!, 200, 50, 50)
        assertEquals(gray[0], gray[1]); assertEquals(gray[1], gray[2])
    }
}
