package com.pt2vhf.aprs.service

/**
 * Public contract for other Android apps (#15). Documented in API.md; bump [VERSION] on changes.
 * Every broadcast and command requires the runtime permission [PERMISSION], which the user grants
 * to the client app, and the API must also be enabled in Settings. The passcode is never exposed.
 */
object ApiContract {
    const val VERSION = 1
    const val PERMISSION = "com.pt2vhf.aprs.permission.API"

    // Events sent by PT2VHF APRS (sendBroadcast with PERMISSION)
    const val EVENT_POSITION = "com.pt2vhf.aprs.event.POSITION"
    const val EVENT_STATUS = "com.pt2vhf.aprs.event.STATUS"

    // Commands accepted by ApiReceiver (sender must hold PERMISSION)
    const val COMMAND_REQUEST_STATUS = "com.pt2vhf.aprs.command.REQUEST_STATUS"
    const val COMMAND_TRACKER_START = "com.pt2vhf.aprs.command.TRACKER_START"
    const val COMMAND_TRACKER_STOP = "com.pt2vhf.aprs.command.TRACKER_STOP"
    const val COMMAND_SEND_BEACON = "com.pt2vhf.aprs.command.SEND_BEACON"

    const val EXTRA_VERSION = "api_version"
    const val EXTRA_CALL = "call"
    const val EXTRA_LAT = "lat"
    const val EXTRA_LON = "lon"
    const val EXTRA_SYMBOL = "symbol"
    const val EXTRA_COMMENT = "comment"
    const val EXTRA_MEDIUM = "medium"
    const val EXTRA_TIME = "time"
    const val EXTRA_APRS_IS = "aprs_is_verified"
    const val EXTRA_TNC = "tnc_connected"
    const val EXTRA_TRACKER = "tracker_on"
    const val EXTRA_STATIONS = "station_count"
    const val EXTRA_RX_ONLY = "rx_only"
}
