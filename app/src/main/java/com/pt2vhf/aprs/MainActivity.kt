package com.pt2vhf.aprs

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.Settings
import com.pt2vhf.aprs.service.AprsService
import com.pt2vhf.aprs.ui.App
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    private val permissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        // Reinicia o serviço com as permissões concedidas (GPS, Bluetooth, notificações).
        AprsService.send(this, AprsService.ACTION_RELOAD)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = "PT2VHF-APRS-Android/${BuildConfig.VERSION_NAME}"
        Configuration.getInstance().osmdroidBasePath = filesDir
        AprsRepository.init(this)
        requestPermissions()
        if (Settings.load(this).configured) AprsService.send(this, AprsService.ACTION_RELOAD)
        setContent { App(onExit = { finish() }) }
    }

    fun requestPermissions() {
        val wanted = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= 33) wanted += Manifest.permission.POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= 31) wanted += Manifest.permission.BLUETOOTH_CONNECT
        permissions.launch(wanted.toTypedArray())
    }
}
