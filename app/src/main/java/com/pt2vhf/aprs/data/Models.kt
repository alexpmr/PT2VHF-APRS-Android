package com.pt2vhf.aprs.data

import com.pt2vhf.aprs.core.AprsMessage

/*
 * Plain data models shared by the service, repository and UI.
 * No Android imports here, so the logic built on them stays unit-testable on the JVM.
 */

/** Station category used by filters (#3). Wire values stay in Portuguese for saved preferences. */
fun stationRole(symbolTable: Char, symbol: Char): String {
    val primary = symbolTable == '/'
    return when {
        symbol == '#' -> "Digipeater"
        symbol == '&' || (symbolTable == 'I' || symbolTable == 'R') -> "iGate"
        symbol == '_' -> "Estação meteorológica"
        (primary && symbol in setOf('\'', '^', 'X', 'g')) || (!primary && symbol in setOf('^', 'g')) -> "Aeronave"
        (primary && symbol in setOf('s', 'Y', 'C')) || (!primary && symbol in setOf('s', 'C')) -> "Embarcação"
        primary && symbol in setOf('[', 'L', 'e') -> "Pessoa"
        symbol in setOf('>', 'k', 'u', 'v', 'j', '<', 'b', 'U', 'R', 'O', 'f', 'a', 'P') -> "Móvel"
        else -> "Estação"
    }
}

enum class Medium(val label: String) { RF("RF"), INTERNET("APRS-IS") }

data class Station(
    val call: String,
    val lat: Double,
    val lon: Double,
    val symbolTable: Char,
    val symbol: Char,
    val comment: String,
    val lastHeard: Long,
    val medium: Medium,
    val via: String,
    val speedKmh: Double?,
    val course: Int?,
    val heardBy: Set<String> = emptySet(),
    val firstSeen: Long = lastHeard,
) {
    val role: String get() = stationRole(symbolTable, symbol)
}

data class RfLink(val from: String, val to: String, val lastSeen: Long)

data class TrackPoint(val lat: Double, val lon: Double, val recordedAt: Long)

data class ChatMessage(
    val id: Long,
    val from: String,
    val to: String,
    val text: String,
    val time: Long,
    val outgoing: Boolean,
    val medium: Medium?,
    val via: String = "",
    val msgId: String? = null,
    val status: String = "",
    val kind: AprsMessage.Kind = AprsMessage.Kind.MESSAGE,
    /** Local-only association to the bubble being answered (#20); APRS has no remote threading. */
    val replyToId: Long? = null,
)

/** Result of an explicit send request; a socket write is not an APRS message ACK. */
data class OutboundSendResult(val requestId: String, val submitted: Boolean, val reason: String = "")
data class OutboundBeaconResult(val requestId: String, val submitted: Boolean)


data class LinkStatus(
    val aprsIsConnected: Boolean = false,
    val aprsIsVerified: Boolean = false,
    val aprsIsServer: String = "",
    val aprsIsError: String = "",
    val tncConnected: Boolean = false,
    val tncName: String = "",
    val tncError: String = "",
    val gpsAccuracyM: Float? = null,
    val gpsFixTimeMs: Long = 0L,
    val lat: Double? = null,
    val lon: Double? = null,
    val speedKmh: Double = 0.0,
    val course: Int? = null,
    val trackerOn: Boolean = false,
    val beaconsSent: Int = 0,
    val beaconsDigipeated: Int = 0,
    val nextBeaconSec: Long? = null,
    val rxPackets: Int = 0,
    val aprsRx: Int = 0,
    val aprsTx: Int = 0,
    val tncRx: Int = 0,
    val tncTx: Int = 0,
    val altitudeM: Double? = null,
    /** Diagnostics (#13): when the current verified session started and how many reconnections. */
    val aprsIsSince: Long = 0L,
    val aprsIsReconnects: Int = 0,
    val aprsIsEverConnected: Boolean = false,
)

/** Local RF evidence (#4, #14): only packets that arrived through this device's TNC. */
data class RfEvent(
    val call: String,
    /** "heard" = I received this station; "digi" = my own packet came back through [via]. */
    val kind: String,
    val via: String,
    val direct: Boolean,
    val time: Long,
    val distanceKm: Double?,
    val lat: Double?,
    val lon: Double?,
    val ownLat: Double?,
    val ownLon: Double?,
)

/** Connection/state transitions for the diagnostics centre (#13). */
data class ConnectionEvent(val time: Long, val source: String, val text: String)
