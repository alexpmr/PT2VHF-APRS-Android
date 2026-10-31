package com.pt2vhf.aprs.data

import android.content.Context
import com.pt2vhf.aprs.core.DEFAULT_FILTER
import com.pt2vhf.aprs.core.DEFAULT_PORT
import com.pt2vhf.aprs.core.DEFAULT_SERVER
import com.pt2vhf.aprs.core.aprsPasscode
import com.pt2vhf.aprs.core.fullCallsign

/** Preferências do app (SharedPreferences). O passcode nunca é pedido: vem do indicativo. */
data class Settings(
    val callsign: String = "",
    val ssid: Int = 9,
    val server: String = DEFAULT_SERVER,
    val port: Int = DEFAULT_PORT,
    val filter: String = DEFAULT_FILTER,
    val comment: String = "PT2VHF APRS www.PT2VHF.com",
    val symbolTable: Char = '/',
    val symbol: Char = '>',
    val rfPath: String = "WIDE1-1,WIDE2-1",
    val txViaRf: Boolean = true,
    val txViaInternet: Boolean = true,
    val connectAprsIs: Boolean = true,
    val beaconMode: String = "smart", // smart | periodic | manual
    val periodicIntervalSec: Int = 900,
    val smartSlowSpeedKmh: Int = 5,
    val smartFastSpeedKmh: Int = 90,
    val smartSlowIntervalSec: Int = 1800,
    val smartFastIntervalSec: Int = 60,
    val stoppedBeaconMinutes: Int = 30,
    val btAddress: String = "",
) {
    val fullCall: String get() = fullCallsign(callsign, ssid)
    val passcode: Int get() = aprsPasscode(callsign)
    val configured: Boolean get() = callsign.isNotBlank()

    companion object {
        private const val PREFS = "pt2vhf_aprs"

        fun load(context: Context): Settings {
            val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val d = Settings()
            return Settings(
                callsign = p.getString("callsign", d.callsign) ?: "",
                ssid = p.getInt("ssid", d.ssid),
                server = p.getString("server", d.server) ?: d.server,
                port = p.getInt("port", d.port),
                filter = p.getString("filter", d.filter) ?: d.filter,
                comment = p.getString("comment", d.comment) ?: d.comment,
                symbolTable = (p.getString("symbol_table", "/") ?: "/").firstOrNull() ?: '/',
                symbol = (p.getString("symbol", ">") ?: ">").firstOrNull() ?: '>',
                rfPath = p.getString("rf_path", d.rfPath) ?: d.rfPath,
                txViaRf = p.getBoolean("tx_rf", d.txViaRf),
                txViaInternet = p.getBoolean("tx_is", d.txViaInternet),
                connectAprsIs = p.getBoolean("connect_aprs_is", d.connectAprsIs),
                beaconMode = p.getString("beacon_mode", d.beaconMode).orEmpty()
                    .takeIf { it in setOf("smart", "periodic", "manual") } ?: d.beaconMode,
                periodicIntervalSec = p.getInt("periodic_interval", d.periodicIntervalSec).coerceIn(30, 86400),
                smartSlowSpeedKmh = p.getInt("smart_slow_speed", d.smartSlowSpeedKmh).coerceIn(1, 200),
                smartFastSpeedKmh = p.getInt("smart_fast_speed", d.smartFastSpeedKmh).coerceIn(5, 300),
                smartSlowIntervalSec = p.getInt("smart_slow_interval", d.smartSlowIntervalSec).coerceIn(30, 86400),
                smartFastIntervalSec = p.getInt("smart_fast_interval", d.smartFastIntervalSec).coerceIn(30, 86400),
                stoppedBeaconMinutes = p.getInt("stopped_minutes", d.stoppedBeaconMinutes),
                btAddress = p.getString("bt_address", "") ?: "",
            )
        }

        fun save(context: Context, s: Settings) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString("callsign", s.callsign.trim().uppercase())
                .putInt("ssid", s.ssid)
                .putString("server", s.server.trim())
                .putInt("port", s.port)
                .putString("filter", s.filter.trim())
                .putString("comment", s.comment)
                .putString("symbol_table", s.symbolTable.toString())
                .putString("symbol", s.symbol.toString())
                .putString("rf_path", s.rfPath.trim())
                .putBoolean("tx_rf", s.txViaRf)
                .putBoolean("tx_is", s.txViaInternet)
                .putBoolean("connect_aprs_is", s.connectAprsIs)
                .putString("beacon_mode", s.beaconMode)
                .putInt("periodic_interval", s.periodicIntervalSec.coerceIn(30, 86400))
                .putInt("smart_slow_speed", s.smartSlowSpeedKmh.coerceIn(1, 200))
                .putInt("smart_fast_speed", s.smartFastSpeedKmh.coerceIn(5, 300))
                .putInt("smart_slow_interval", s.smartSlowIntervalSec.coerceIn(30, 86400))
                .putInt("smart_fast_interval", s.smartFastIntervalSec.coerceIn(30, 86400))
                .putInt("stopped_minutes", s.stoppedBeaconMinutes)
                .putString("bt_address", s.btAddress)
                .apply()
        }

        fun readOption(context: Context, key: String, default: String): String =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key, default) ?: default

        fun saveOption(context: Context, key: String, value: String) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(key, value).apply()
        }

        fun flag(context: Context, key: String): Boolean =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(key, false)

        fun setFlag(context: Context, key: String, value: Boolean) =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(key, value).apply()
    }
}
