package com.pt2vhf.aprs.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MapIconSizeTest {
    @Test fun defaultIsMediumAndUnknownFallsBack() {
        assertEquals(MapIconSize.MEDIUM, MapIconSize.fromKey(null))
        assertEquals(MapIconSize.MEDIUM, MapIconSize.fromKey("enorme"))
        assertEquals(MapIconSize.LARGE, MapIconSize.fromKey("large"))
    }

    @Test fun sizesGrowAndUseRealDensity() {
        assertTrue(MapIconSize.SMALL.dp < MapIconSize.MEDIUM.dp && MapIconSize.MEDIUM.dp < MapIconSize.LARGE.dp)
        // Médio fica na faixa pedida (~32-36 dp) e muito acima dos 16 dp efetivos da v0.1.12 em xxxhdpi.
        assertTrue(MapIconSize.MEDIUM.dp in 32..36)
        assertEquals(34, mapIconPx(MapIconSize.MEDIUM, 1f))
        assertEquals(136, mapIconPx(MapIconSize.MEDIUM, 4f))
        assertEquals(12f, MapIconSize.MEDIUM.labelSp, 0.001f)
        assertTrue(MapIconSize.SMALL.labelSp >= 10f)
    }
}
