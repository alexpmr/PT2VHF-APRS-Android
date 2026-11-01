package com.pt2vhf.aprs.data

import kotlin.math.roundToInt

/** Show speed only when actually reported by APRS; unknown is not zero. */
fun stationSpeedLabel(speedKmh: Double?): String =
    if (speedKmh != null && speedKmh.isFinite() && speedKmh >= 0.0)
        "${speedKmh.roundToInt()} km/h"
    else "—"
