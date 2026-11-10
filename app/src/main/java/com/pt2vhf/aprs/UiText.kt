package com.pt2vhf.aprs

import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
import java.util.Locale

/** Translations use Android resource qualifiers. The default values are English. */
class Pt2vhfApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UiText.initialize(this)
        com.pt2vhf.aprs.data.CrashLog.install(this) // #45
    }
}

object UiText {
    private lateinit var context: Context

    fun initialize(application: Context) {
        context = application.applicationContext
    }

    fun locale(): Locale = context.resources.configuration.locales[0]

    fun text(@StringRes id: Int, vararg arguments: Any): String =
        if (arguments.isEmpty()) context.getString(id) else context.getString(id, *arguments)

    private fun resolve(res: TextRes?, raw: String): String =
        res?.let { text(it.id, *it.args.toTypedArray()) } ?: raw

    // #76: o mapeamento valor interno → recurso fica em UiTextKeys (puro, coberto por teste de unidade).
    fun role(raw: String): String = text(UiTextKeys.role(raw) ?: R.string.role_station)

    fun serverLabel(raw: String): String = UiTextKeys.serverLabel(raw)?.let { text(it) } ?: raw

    /** Wire-format values, callsigns and raw APRS packets are never translated. */
    fun messageStatus(raw: String): String = resolve(UiTextKeys.messageStatus(raw), raw)

    fun receptionVia(raw: String): String = resolve(UiTextKeys.receptionVia(raw), raw)

    fun connectionError(raw: String): String = resolve(UiTextKeys.connectionError(raw), raw)
}
