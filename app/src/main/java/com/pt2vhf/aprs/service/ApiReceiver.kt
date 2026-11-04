package com.pt2vhf.aprs.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pt2vhf.aprs.data.Settings

/**
 * Commands from other apps (#15). Protected in the manifest by [ApiContract.PERMISSION].
 * Nothing happens unless the user enabled the API, and each class of command has its own switch.
 * Commands are only forwarded to a running service: the API never starts transmissions by itself.
 */
class ApiReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val s = Settings.load(context)
        if (!s.apiEnabled || !AprsService.running) return
        val action = when (intent.action) {
            ApiContract.COMMAND_REQUEST_STATUS -> AprsService.ACTION_API_STATUS
            ApiContract.COMMAND_TRACKER_START -> if (s.apiAllowControl && !s.rxOnly) AprsService.ACTION_TRACKER_ON else null
            ApiContract.COMMAND_TRACKER_STOP -> if (s.apiAllowControl) AprsService.ACTION_TRACKER_OFF else null
            ApiContract.COMMAND_SEND_BEACON -> if (s.apiAllowBeacon && !s.rxOnly) AprsService.ACTION_API_BEACON else null
            else -> null
        } ?: return
        // The app already holds a foreground service, so a plain startService is allowed here.
        runCatching { context.startService(Intent(context, AprsService::class.java).setAction(action)) }
    }
}
