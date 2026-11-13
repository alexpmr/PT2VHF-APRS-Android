package com.pt2vhf.aprs.service

import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.car.app.CarAppService
import androidx.car.app.CarContext
import androidx.car.app.CarToast
import androidx.car.app.Screen
import androidx.car.app.Session
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Pane
import androidx.car.app.model.PaneTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.car.app.validation.HostValidator
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.Settings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/*
 * #68 Android Auto: tela no painel do carro com as estações próximas, a última mensagem recebida e
 * "Beacon agora". Usa só modelos (templates) da biblioteca Car App; o app no celular continua sendo
 * quem conecta ao APRS-IS/TNC. Para APK fora da Play Store, o Android Auto precisa do modo
 * desenvolvedor com "Fontes desconhecidas" ligado.
 */
class AprsCarAppService : CarAppService() {
    override fun createHostValidator(): HostValidator =
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        else HostValidator.Builder(applicationContext)
            .addAllowedHosts(androidx.car.app.R.array.hosts_allowlist_sample).build()

    override fun onCreateSession(): Session = object : Session() {
        override fun onCreateScreen(intent: Intent): Screen = CarStationsScreen(carContext)
    }
}

/** Distância e rumo até as estações, da mais próxima para a mais distante. */
internal fun nearbyStations(maxItems: Int): List<Pair<String, String>> {
    val status = AprsRepository.status.value
    val lat = status.lat
    val lon = status.lon
    val stations = AprsRepository.stations.value.values
    val sorted = if (lat != null && lon != null)
        stations.map { it to com.pt2vhf.aprs.core.haversineKm(lat, lon, it.lat, it.lon) }.sortedBy { it.second }
    else stations.sortedByDescending { it.lastHeard }.map { it to null }
    return sorted.take(maxItems).map { (s, km) ->
        val dist = km?.let { if (it < 10) "%.1f km".format(it) else "%.0f km".format(it) }
        s.call to listOfNotNull(dist, s.comment.takeIf { it.isNotBlank() }?.take(40)).joinToString(" · ")
    }
}

private class CarStationsScreen(carContext: CarContext) : Screen(carContext) {
    private var lastRefresh = 0L
    private var refreshJob: Job? = null

    init {
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                refreshJob = lifecycleScope.launch {
                    combine(AprsRepository.stations, AprsRepository.status, AprsRepository.messages) { _, _, _ -> }
                        .collect {
                            // O carro limita quantas vezes a tela pode ser redesenhada: no máximo a cada 10 s.
                            val wait = 10_000L - (System.currentTimeMillis() - lastRefresh)
                            if (wait > 0) delay(wait)
                            lastRefresh = System.currentTimeMillis()
                            invalidate()
                        }
                }
            }
            override fun onStop(owner: LifecycleOwner) { refreshJob?.cancel() }
        })
    }

    override fun onGetTemplate(): Template {
        val items = ItemList.Builder()
        // Título fixo: o carro só aceita atualizar a mesma tela se o título não mudar; o estado vai na 1ª linha.
        val s = AprsRepository.status.value
        fun onOff(b: Boolean) = UiText.text(if (b) R.string.car_on else R.string.car_off)
        items.addItem(Row.Builder().setTitle(UiText.text(R.string.car_status,
            onOff(s.aprsIsVerified), onOff(s.tncConnected), onOff(s.lat != null))).build())
        val near = nearbyStations(5)
        if (near.isEmpty()) items.addItem(Row.Builder().setTitle(UiText.text(R.string.car_no_stations)).build())
        near.forEach { (call, detail) ->
            val row = Row.Builder().setTitle(call)
            if (detail.isNotBlank()) row.addText(detail)
            items.addItem(row.build())
        }
        return ListTemplate.Builder()
            .setTitle(UiText.text(R.string.car_nearby))
            .setHeaderAction(Action.APP_ICON)
            .setSingleList(items.build())
            .setActionStrip(ActionStrip.Builder()
                .addAction(Action.Builder().setTitle(UiText.text(R.string.car_beacon_now))
                    .setOnClickListener { beaconNow(carContext) }.build())
                .addAction(Action.Builder().setTitle(UiText.text(R.string.car_last_message))
                    .setOnClickListener { screenManager.push(CarMessageScreen(carContext)) }.build())
                .build())
            .build()
    }
}

private class CarMessageScreen(carContext: CarContext) : Screen(carContext) {
    override fun onGetTemplate(): Template {
        val msg = AprsRepository.messages.value.filter { !it.outgoing }.maxByOrNull { it.time }
        val pane = Pane.Builder()
        if (msg == null) pane.addRow(Row.Builder().setTitle(UiText.text(R.string.car_no_message)).build())
        else pane.addRow(Row.Builder().setTitle("${msg.from} → ${msg.to}")
            .addText(msg.text.take(120))
            .addText(java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT).format(java.util.Date(msg.time)))
            .build())
        return PaneTemplate.Builder(pane.build())
            .setTitle(UiText.text(R.string.car_last_message))
            .setHeaderAction(Action.BACK)
            .build()
    }
}

/** Mesmo comando do widget; nada sai em Somente recepção ou sem estação configurada. */
private fun beaconNow(context: CarContext) {
    val settings = Settings.load(context)
    if (!settings.configured || settings.rxOnly) {
        CarToast.makeText(context, UiText.text(if (settings.rxOnly) R.string.rx_only_blocked else R.string.configure_callsign),
            CarToast.LENGTH_LONG).show()
        return
    }
    val ok = runCatching {
        context.startService(Intent(context, AprsService::class.java).setAction(AprsService.ACTION_BEACON))
    }.isSuccess
    CarToast.makeText(context, UiText.text(if (ok) R.string.car_beacon_sent else R.string.tx_send_failed),
        CarToast.LENGTH_SHORT).show()
}
