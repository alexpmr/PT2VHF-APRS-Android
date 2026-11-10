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
        symbol == '#' -> InternalText.ROLE_DIGI
        symbol == '&' || (symbolTable == 'I' || symbolTable == 'R') -> InternalText.ROLE_IGATE
        symbol == '_' -> InternalText.ROLE_WEATHER
        (primary && symbol in setOf('\'', '^', 'X', 'g')) || (!primary && symbol in setOf('^', 'g')) -> InternalText.ROLE_AIRCRAFT
        (primary && symbol in setOf('s', 'Y', 'C')) || (!primary && symbol in setOf('s', 'C')) -> InternalText.ROLE_VESSEL
        primary && symbol in setOf('[', 'L', 'e') -> InternalText.ROLE_PERSON
        symbol in setOf('>', 'k', 'u', 'v', 'j', '<', 'b', 'U', 'R', 'O', 'f', 'a', 'P') -> InternalText.ROLE_MOBILE
        else -> InternalText.ROLE_STATION
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
    /** #44: nível RMS da entrada de áudio (0..1) e transmissão AFSK em andamento. */
    val audioLevel: Double = 0.0,
    val audioTransmitting: Boolean = false,
    /** Hora do último beacon próprio aceito por algum transporte (widget #46). */
    val lastBeaconAt: Long = 0L,
    /** #6: posição atual vem da posição fixa configurada, não do GPS. */
    val manualPosition: Boolean = false,
    /** #49: economia de bateria ativa neste momento (tela desligada + modo ligado/automático). */
    val batterySaverActive: Boolean = false,
    val batteryPercent: Int? = null,
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

/** Objeto/item APRS criado pelo usuário (#48). intervalMin 0 = transmitir uma vez. */
data class MyObject(
    val id: Long,
    val name: String,
    val item: Boolean,
    val lat: Double,
    val lon: Double,
    val symbolTable: Char,
    val symbol: Char,
    val comment: String,
    val intervalMin: Int,
    val active: Boolean,
    val lastSent: Long = 0L,
)

/** Rota importada de GPX (rotas). */
data class SavedRoute(val id: Long, val name: String, val visible: Boolean, val points: List<com.pt2vhf.aprs.core.RoutePoint>)

/** Uma linha da aba Logs (#51): pacote bruto TNC2 recebido ou enviado. */
data class PacketLogEntry(
    val id: Long,
    val time: Long,
    /** "RX" ou "TX". */
    val direction: String,
    val medium: Medium,
    val raw: String,
    val source: String,
    val destination: String,
    val type: String,
)
