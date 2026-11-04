package com.pt2vhf.aprs.data

import android.content.Context
import com.pt2vhf.aprs.core.DEFAULT_FILTER
import com.pt2vhf.aprs.core.DEFAULT_PORT
import com.pt2vhf.aprs.core.DEFAULT_SERVER
import com.pt2vhf.aprs.core.aprsPasscode
import com.pt2vhf.aprs.core.fullCallsign
import org.json.JSONObject

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
    val trackerAutoStart: Boolean = true, // Configured users can disable automatic tracking
    val periodicIntervalSec: Int = 900,
    val smartSlowSpeedKmh: Int = 5,
    val smartFastSpeedKmh: Int = 90,
    val smartSlowIntervalSec: Int = 1800,
    val smartFastIntervalSec: Int = 60,
    val smartMinTurnAngle: Int = 28,
    val smartTurnSlope: Int = 255,
    val smartMinTurnTimeSec: Int = 15,
    val stoppedBeaconMinutes: Int = 30,
    val btAddress: String = "",
    // #10 perfis de operação
    val profile: String = PROFILE_CUSTOM,
    // #6 privacidade e conteúdo do beacon
    val ambiguity: Int = 0,
    val sendCourseSpeed: Boolean = true,
    val sendAltitude: Boolean = false,
    val voiceFrequency: String = "",
    // #6 tela, notificações e log
    val keepScreenOn: Boolean = false,
    val stationMaxAgeHours: Int = 0, // 0 = todas
    val notifyMessages: Boolean = true,
    val monitorEnabled: Boolean = true,
    // #12 alertas de favoritas
    val alertFavFirst: Boolean = true,
    val alertFavReturn: Boolean = true,
    val alertFavPosition: Boolean = false,
    val alertFavRf: Boolean = true,
    val alertMyDigi: Boolean = false,
    val alertReturnMinutes: Int = 60,
    val quietHours: Boolean = false,
    val quietStart: Int = 22,
    val quietEnd: Int = 7,
    // TNC KISS por TCP (herdado da 0.1.0)
    val tncType: String = "bt", // bt | tcp
    val tncHost: String = "",
    val tncPort: Int = 8001,
    // #15 API para outros apps (desligada por padrão)
    val apiEnabled: Boolean = false,
    val apiAllowControl: Boolean = false,
    val apiAllowBeacon: Boolean = false,
    // #26 retenção de mensagens: dias 0 = sem prazo; máximo 0 = sem limite
    val retPrivateDays: Int = 0,
    val retGeneralDays: Int = 0,
    val retBulletinDays: Int = 0,
    val retPrivateMax: Int = 5000,
    val retGeneralMax: Int = 5000,
    val retBulletinMax: Int = 5000,
) {
    val fullCall: String get() = fullCallsign(callsign, ssid)
    val passcode: Int get() = aprsPasscode(callsign)
    val configured: Boolean get() = callsign.isNotBlank()
    /** Perfil Somente recepção: nenhum beacon, mensagem ou ACK sai por nenhum meio. */
    val rxOnly: Boolean get() = profile == PROFILE_RX

    val retention: RetentionPolicy get() = RetentionPolicy(
        RetentionRule(retPrivateDays, retPrivateMax),
        RetentionRule(retGeneralDays, retGeneralMax),
        RetentionRule(retBulletinDays, retBulletinMax),
    )

    companion object {
        private const val PREFS = "pt2vhf_aprs"
        const val PROFILE_CUSTOM = "custom"
        const val PROFILE_BASE = "base"
        const val PROFILE_MOBILE = "mobile"
        const val PROFILE_PORTABLE = "portable"
        const val PROFILE_RX = "rx"
        val PROFILES = listOf(PROFILE_CUSTOM, PROFILE_BASE, PROFILE_MOBILE, PROFILE_PORTABLE, PROFILE_RX)

        fun load(context: Context): Settings {
            val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val d = Settings()
            fun b(key: String, def: Boolean) = p.getBoolean(key, def)
            fun i(key: String, def: Int) = runCatching { p.getInt(key, def) }.getOrDefault(def)
            fun s(key: String, def: String) = runCatching { p.getString(key, def) }.getOrNull() ?: def
            return Settings(
                callsign = s("callsign", d.callsign),
                ssid = i("ssid", d.ssid),
                server = s("server", d.server),
                port = i("port", d.port),
                filter = s("filter", d.filter),
                comment = s("comment", d.comment),
                symbolTable = s("symbol_table", "/").firstOrNull() ?: '/',
                symbol = s("symbol", ">").firstOrNull() ?: '>',
                rfPath = s("rf_path", d.rfPath),
                txViaRf = b("tx_rf", d.txViaRf),
                txViaInternet = b("tx_is", d.txViaInternet),
                connectAprsIs = b("connect_aprs_is", d.connectAprsIs),
                trackerAutoStart = b("tracker_auto_start", d.trackerAutoStart),
                beaconMode = s("beacon_mode", d.beaconMode)
                    .takeIf { it in setOf("smart", "periodic", "manual") } ?: d.beaconMode,
                periodicIntervalSec = i("periodic_interval", d.periodicIntervalSec).coerceIn(30, 86400),
                smartSlowSpeedKmh = i("smart_slow_speed", d.smartSlowSpeedKmh).coerceIn(1, 200),
                smartFastSpeedKmh = i("smart_fast_speed", d.smartFastSpeedKmh).coerceIn(5, 300),
                smartSlowIntervalSec = i("smart_slow_interval", d.smartSlowIntervalSec).coerceIn(30, 86400),
                smartFastIntervalSec = i("smart_fast_interval", d.smartFastIntervalSec).coerceIn(30, 86400),
                smartMinTurnAngle = i("smart_turn_angle", d.smartMinTurnAngle).coerceIn(5, 90),
                smartTurnSlope = i("smart_turn_slope", d.smartTurnSlope).coerceIn(0, 1000),
                smartMinTurnTimeSec = i("smart_turn_time", d.smartMinTurnTimeSec).coerceIn(5, 600),
                stoppedBeaconMinutes = i("stopped_minutes", d.stoppedBeaconMinutes).coerceIn(0, 1440),
                btAddress = s("bt_address", ""),
                profile = s("profile", d.profile).takeIf { it in PROFILES } ?: d.profile,
                ambiguity = i("ambiguity", d.ambiguity).coerceIn(0, 4),
                sendCourseSpeed = b("send_course_speed", d.sendCourseSpeed),
                sendAltitude = b("send_altitude", d.sendAltitude),
                voiceFrequency = s("voice_frequency", d.voiceFrequency),
                keepScreenOn = b("keep_screen_on", d.keepScreenOn),
                stationMaxAgeHours = i("station_max_age_h", d.stationMaxAgeHours).coerceIn(0, 720),
                notifyMessages = b("notify_messages", d.notifyMessages),
                monitorEnabled = b("monitor_enabled", d.monitorEnabled),
                alertFavFirst = b("alert_fav_first", d.alertFavFirst),
                alertFavReturn = b("alert_fav_return", d.alertFavReturn),
                alertFavPosition = b("alert_fav_position", d.alertFavPosition),
                alertFavRf = b("alert_fav_rf", d.alertFavRf),
                alertMyDigi = b("alert_my_digi", d.alertMyDigi),
                alertReturnMinutes = i("alert_return_min", d.alertReturnMinutes).coerceIn(5, 1440),
                quietHours = b("quiet_hours", d.quietHours),
                quietStart = i("quiet_start", d.quietStart).coerceIn(0, 23),
                quietEnd = i("quiet_end", d.quietEnd).coerceIn(0, 23),
                tncType = s("tnc_type", d.tncType).takeIf { it == "bt" || it == "tcp" } ?: d.tncType,
                tncHost = s("tnc_host", d.tncHost),
                tncPort = i("tnc_port", d.tncPort).coerceIn(1, 65535),
                apiEnabled = b("api_enabled", d.apiEnabled),
                apiAllowControl = b("api_allow_control", d.apiAllowControl),
                apiAllowBeacon = b("api_allow_beacon", d.apiAllowBeacon),
                retPrivateDays = i("ret_private_days", d.retPrivateDays).coerceAtLeast(0),
                retGeneralDays = i("ret_general_days", d.retGeneralDays).coerceAtLeast(0),
                retBulletinDays = i("ret_bulletin_days", d.retBulletinDays).coerceAtLeast(0),
                retPrivateMax = i("ret_private_max", d.retPrivateMax).coerceAtLeast(0),
                retGeneralMax = i("ret_general_max", d.retGeneralMax).coerceAtLeast(0),
                retBulletinMax = i("ret_bulletin_max", d.retBulletinMax).coerceAtLeast(0),
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
                .putBoolean("tracker_auto_start", s.trackerAutoStart)
                .putInt("periodic_interval", s.periodicIntervalSec.coerceIn(30, 86400))
                .putInt("smart_slow_speed", s.smartSlowSpeedKmh.coerceIn(1, 200))
                .putInt("smart_fast_speed", s.smartFastSpeedKmh.coerceIn(5, 300))
                .putInt("smart_slow_interval", s.smartSlowIntervalSec.coerceIn(30, 86400))
                .putInt("smart_fast_interval", s.smartFastIntervalSec.coerceIn(30, 86400))
                .putInt("smart_turn_angle", s.smartMinTurnAngle.coerceIn(5, 90))
                .putInt("smart_turn_slope", s.smartTurnSlope.coerceIn(0, 1000))
                .putInt("smart_turn_time", s.smartMinTurnTimeSec.coerceIn(5, 600))
                .putInt("stopped_minutes", s.stoppedBeaconMinutes.coerceIn(0, 1440))
                .putString("bt_address", s.btAddress)
                .putString("profile", s.profile)
                .putInt("ambiguity", s.ambiguity.coerceIn(0, 4))
                .putBoolean("send_course_speed", s.sendCourseSpeed)
                .putBoolean("send_altitude", s.sendAltitude)
                .putString("voice_frequency", s.voiceFrequency.trim())
                .putBoolean("keep_screen_on", s.keepScreenOn)
                .putInt("station_max_age_h", s.stationMaxAgeHours.coerceIn(0, 720))
                .putBoolean("notify_messages", s.notifyMessages)
                .putBoolean("monitor_enabled", s.monitorEnabled)
                .putBoolean("alert_fav_first", s.alertFavFirst)
                .putBoolean("alert_fav_return", s.alertFavReturn)
                .putBoolean("alert_fav_position", s.alertFavPosition)
                .putBoolean("alert_fav_rf", s.alertFavRf)
                .putBoolean("alert_my_digi", s.alertMyDigi)
                .putInt("alert_return_min", s.alertReturnMinutes.coerceIn(5, 1440))
                .putBoolean("quiet_hours", s.quietHours)
                .putInt("quiet_start", s.quietStart.coerceIn(0, 23))
                .putInt("quiet_end", s.quietEnd.coerceIn(0, 23))
                .putString("tnc_type", s.tncType)
                .putString("tnc_host", s.tncHost.trim())
                .putInt("tnc_port", s.tncPort.coerceIn(1, 65535))
                .putBoolean("api_enabled", s.apiEnabled)
                .putBoolean("api_allow_control", s.apiAllowControl)
                .putBoolean("api_allow_beacon", s.apiAllowBeacon)
                .putInt("ret_private_days", s.retPrivateDays)
                .putInt("ret_general_days", s.retGeneralDays)
                .putInt("ret_bulletin_days", s.retBulletinDays)
                .putInt("ret_private_max", s.retPrivateMax)
                .putInt("ret_general_max", s.retGeneralMax)
                .putInt("ret_bulletin_max", s.retBulletinMax)
                .apply()
            // Each profile keeps its own transmission-related choices (#10).
            saveProfileSnapshot(context, s.profile, s)
        }

        fun readOption(context: Context, key: String, default: String): String =
            runCatching { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(key, default) }
                .getOrNull() ?: default

        fun saveOption(context: Context, key: String, value: String) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(key, value).apply()
        }

        fun flag(context: Context, key: String): Boolean =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(key, false)

        fun setFlag(context: Context, key: String, value: Boolean) =
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(key, value).apply()

        // ---------- Perfis (#10) ----------

        private fun snapshotJson(s: Settings): String = JSONObject()
            .put("ssid", s.ssid).put("symbol_table", s.symbolTable.toString()).put("symbol", s.symbol.toString())
            .put("comment", s.comment).put("voice_frequency", s.voiceFrequency).put("rf_path", s.rfPath)
            .put("beacon_mode", s.beaconMode).put("periodic_interval", s.periodicIntervalSec)
            .put("smart_slow_speed", s.smartSlowSpeedKmh).put("smart_fast_speed", s.smartFastSpeedKmh)
            .put("smart_slow_interval", s.smartSlowIntervalSec).put("smart_fast_interval", s.smartFastIntervalSec)
            .put("smart_turn_angle", s.smartMinTurnAngle).put("smart_turn_slope", s.smartTurnSlope)
            .put("smart_turn_time", s.smartMinTurnTimeSec).put("stopped_minutes", s.stoppedBeaconMinutes)
            .put("tx_rf", s.txViaRf).put("tx_is", s.txViaInternet).put("connect_aprs_is", s.connectAprsIs)
            .put("filter", s.filter).put("ambiguity", s.ambiguity).put("send_course_speed", s.sendCourseSpeed)
            .put("send_altitude", s.sendAltitude).put("tracker_auto_start", s.trackerAutoStart)
            .put("tnc_type", s.tncType).put("tnc_host", s.tncHost).put("tnc_port", s.tncPort)
            .toString()

        private fun applySnapshot(base: Settings, json: String): Settings? = runCatching {
            val o = JSONObject(json)
            base.copy(
                ssid = o.optInt("ssid", base.ssid).coerceIn(0, 15),
                symbolTable = o.optString("symbol_table", base.symbolTable.toString()).firstOrNull() ?: base.symbolTable,
                symbol = o.optString("symbol", base.symbol.toString()).firstOrNull() ?: base.symbol,
                comment = o.optString("comment", base.comment),
                voiceFrequency = o.optString("voice_frequency", base.voiceFrequency),
                rfPath = o.optString("rf_path", base.rfPath),
                beaconMode = o.optString("beacon_mode", base.beaconMode)
                    .takeIf { it in setOf("smart", "periodic", "manual") } ?: base.beaconMode,
                periodicIntervalSec = o.optInt("periodic_interval", base.periodicIntervalSec).coerceIn(30, 86400),
                smartSlowSpeedKmh = o.optInt("smart_slow_speed", base.smartSlowSpeedKmh),
                smartFastSpeedKmh = o.optInt("smart_fast_speed", base.smartFastSpeedKmh),
                smartSlowIntervalSec = o.optInt("smart_slow_interval", base.smartSlowIntervalSec),
                smartFastIntervalSec = o.optInt("smart_fast_interval", base.smartFastIntervalSec),
                smartMinTurnAngle = o.optInt("smart_turn_angle", base.smartMinTurnAngle),
                smartTurnSlope = o.optInt("smart_turn_slope", base.smartTurnSlope),
                smartMinTurnTimeSec = o.optInt("smart_turn_time", base.smartMinTurnTimeSec),
                stoppedBeaconMinutes = o.optInt("stopped_minutes", base.stoppedBeaconMinutes),
                txViaRf = o.optBoolean("tx_rf", base.txViaRf),
                txViaInternet = o.optBoolean("tx_is", base.txViaInternet),
                connectAprsIs = o.optBoolean("connect_aprs_is", base.connectAprsIs),
                filter = o.optString("filter", base.filter),
                ambiguity = o.optInt("ambiguity", base.ambiguity).coerceIn(0, 4),
                sendCourseSpeed = o.optBoolean("send_course_speed", base.sendCourseSpeed),
                sendAltitude = o.optBoolean("send_altitude", base.sendAltitude),
                trackerAutoStart = o.optBoolean("tracker_auto_start", base.trackerAutoStart),
                tncType = o.optString("tnc_type", base.tncType).takeIf { it == "bt" || it == "tcp" } ?: base.tncType,
                tncHost = o.optString("tnc_host", base.tncHost),
                tncPort = o.optInt("tnc_port", base.tncPort).coerceIn(1, 65535),
            )
        }.getOrNull()

        private fun saveProfileSnapshot(context: Context, profile: String, s: Settings) {
            saveOption(context, "profile_snapshot_$profile", snapshotJson(s))
        }

        /**
         * Settings to show when the user picks [profile]: its own saved snapshot, or the preset
         * defaults the first time. Callsign, server and local data are shared by every profile.
         */
        fun forProfile(context: Context, current: Settings, profile: String): Settings {
            val base = current.copy(profile = profile)
            val saved = readOption(context, "profile_snapshot_$profile", "")
            if (saved.isNotBlank()) applySnapshot(base, saved)?.let { return it }
            return presetFor(base, profile)
        }

        fun presetFor(base: Settings, profile: String): Settings = when (profile) {
            PROFILE_BASE -> base.copy(ssid = 0, symbolTable = '/', symbol = '-', beaconMode = "periodic",
                periodicIntervalSec = 1800, sendCourseSpeed = false)
            PROFILE_MOBILE -> base.copy(ssid = 9, symbolTable = '/', symbol = '>', beaconMode = "smart",
                smartSlowSpeedKmh = 5, smartFastSpeedKmh = 90, smartSlowIntervalSec = 1800,
                smartFastIntervalSec = 60, sendCourseSpeed = true)
            PROFILE_PORTABLE -> base.copy(ssid = 7, symbolTable = '/', symbol = '[', beaconMode = "smart",
                smartSlowSpeedKmh = 3, smartFastSpeedKmh = 20, smartSlowIntervalSec = 1800,
                smartFastIntervalSec = 180, sendCourseSpeed = true)
            PROFILE_RX -> base.copy(trackerAutoStart = false)
            else -> base
        }
    }
}
