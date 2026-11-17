package com.pt2vhf.aprs.ui

import android.content.Context
import android.content.res.Configuration
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.data.MapColors
import com.pt2vhf.aprs.data.MapType
import com.pt2vhf.aprs.data.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import org.osmdroid.tileprovider.tilesource.ITileSource
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView

/** #94: tipo de mapa escolhido no mapa ou em Configurações, compartilhado com o minimapa da ficha. */
internal object MapTypePrefs {
    val current = MutableStateFlow<MapType?>(null)

    fun read(context: Context): MapType =
        current.value ?: MapType.fromKey(Settings.readOption(context, MapType.OPTION_KEY, MapType.STANDARD.key))
            .also { current.value = it }

    fun save(context: Context, type: MapType) {
        Settings.saveOption(context, MapType.OPTION_KEY, type.key)
        current.value = type
    }
}

internal fun systemIsDark(context: Context): Boolean =
    (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

internal fun mapTypeLabel(type: MapType): String = UiText.text(when (type) {
    MapType.STANDARD -> R.string.map_type_standard
    MapType.DARK -> R.string.map_type_dark
    MapType.GRAY -> R.string.map_type_gray
    MapType.TOPO -> R.string.map_type_topo
    MapType.AUTO -> R.string.map_type_auto
})

/** Fonte de tiles do tipo resolvido; cada fonte tem cache próprio no osmdroid (pelo nome da fonte). */
internal fun tileSourceFor(type: MapType): ITileSource =
    if (type == MapType.TOPO) TileSourceFactory.OpenTopo else TileSourceFactory.MAPNIK

/** Aplica tipo + brilho só na camada de tiles, sem tocar nos ícones, rótulos e trilhas. Não muda posição/zoom. */
internal fun applyMapType(map: MapView, type: MapType, brightness: Int) {
    val source = tileSourceFor(type)
    if (map.tileProvider.tileSource.name() != source.name()) map.setTileSource(source)
    map.overlayManager.tilesOverlay.setColorFilter(
        MapColors.forMap(type, brightness)?.let {
            android.graphics.ColorMatrixColorFilter(android.graphics.ColorMatrix(it))
        })
    // Fundo enquanto os tiles carregam: escuro no Escuro, para não piscar branco à noite.
    map.overlayManager.tilesOverlay.loadingBackgroundColor =
        if (type == MapType.DARK) 0xFF161A1E.toInt() else android.graphics.Color.rgb(216, 208, 208)
    map.overlayManager.tilesOverlay.loadingLineColor =
        if (type == MapType.DARK) 0xFF20262C.toInt() else android.graphics.Color.rgb(200, 192, 192)
}
