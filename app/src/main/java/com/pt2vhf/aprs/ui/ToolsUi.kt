package com.pt2vhf.aprs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.core.APRS_FREQUENCIES
import com.pt2vhf.aprs.core.DecodedKey
import com.pt2vhf.aprs.core.PathHop
import com.pt2vhf.aprs.core.countryName
import com.pt2vhf.aprs.core.decodePacket
import com.pt2vhf.aprs.core.flagEmoji

private fun decodedLabel(key: DecodedKey): String = UiText.text(when (key) {
    DecodedKey.SOURCE -> R.string.logs_source
    DecodedKey.DESTINATION -> R.string.logs_destination
    DecodedKey.THIRD_PARTY -> R.string.dec_third_party
    DecodedKey.TYPE -> R.string.logs_type
    DecodedKey.LATITUDE -> R.string.dec_latitude
    DecodedKey.LONGITUDE -> R.string.dec_longitude
    DecodedKey.GRID -> R.string.dec_grid
    DecodedKey.SYMBOL -> R.string.symbol
    DecodedKey.COURSE -> R.string.dec_course
    DecodedKey.SPEED -> R.string.speed
    DecodedKey.ALTITUDE -> R.string.dec_altitude
    DecodedKey.PHG -> R.string.dec_phg
    DecodedKey.COMMENT -> R.string.dec_comment
    DecodedKey.STATUS -> R.string.dec_status
    DecodedKey.OBJECT_NAME -> R.string.type_object
    DecodedKey.OBJECT_STATE -> R.string.dec_object_state
    DecodedKey.ADDRESSEE -> R.string.dec_addressee
    DecodedKey.MESSAGE_TEXT -> R.string.dec_message_text
    DecodedKey.MESSAGE_ID -> R.string.dec_message_id
    DecodedKey.ACK_ID -> R.string.dec_ack
    DecodedKey.REPLY_ACK -> R.string.dec_reply_ack
    DecodedKey.TEMPERATURE -> R.string.dec_temperature
    DecodedKey.HUMIDITY -> R.string.dec_humidity
    DecodedKey.PRESSURE -> R.string.dec_pressure
    DecodedKey.WIND -> R.string.dec_wind
    DecodedKey.GUST -> R.string.dec_gust
    DecodedKey.RAIN_1H -> R.string.dec_rain_1h
    DecodedKey.RAIN_24H -> R.string.dec_rain_24h
    DecodedKey.RAIN_MIDNIGHT -> R.string.dec_rain_midnight
    DecodedKey.TELEMETRY_SEQUENCE -> R.string.dec_telemetry_seq
    DecodedKey.TELEMETRY_ANALOG -> R.string.dec_telemetry_analog
    DecodedKey.TELEMETRY_BITS -> R.string.dec_telemetry_bits
    DecodedKey.NOT_DECODED -> R.string.dec_not_decoded
    DecodedKey.DEVICE -> R.string.dev_device
    DecodedKey.FREQUENCY -> R.string.dev_frequency
})

/** #69: ferramentas — decodificador de pacote (nada é transmitido) e tabela de frequências APRS. */
@Composable
fun ToolsDialog(initialRaw: String, onDismiss: () -> Unit) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var raw by rememberSaveable { mutableStateOf(initialRaw) }
    val decoded = remember(raw) { if (raw.isBlank()) null else decodePacket(raw) }
    val locale = UiText.locale()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pal.bar,
        title = { Text(UiText.text(R.string.tools_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TabRow(selectedTabIndex = tab, containerColor = Pal.bar, contentColor = Pal.accent) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(UiText.text(R.string.tools_decoder), fontSize = 13.sp) })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(UiText.text(R.string.tools_frequencies), fontSize = 13.sp) })
                }
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (tab == 0) {
                        OutlinedTextField(raw, { raw = it }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4,
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = Pal.text),
                            placeholder = { Text("PT2VHF-9>APRS,WIDE1-1:!1548.00S/04806.60W>…", fontSize = 12.sp) })
                        Text(UiText.text(R.string.tools_decoder_hint), color = Pal.muted, fontSize = 11.sp)
                        if (raw.isNotBlank() && decoded == null)
                            Text(UiText.text(R.string.tools_decoder_invalid), color = Pal.orange, fontSize = 13.sp)
                        decoded?.let { d ->
                            SelectionContainer {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    d.fields.forEach { f ->
                                        Row {
                                            Text(decodedLabel(f.key), color = Pal.muted, fontSize = 12.sp, modifier = Modifier.width(110.dp))
                                            Text(if (f.key == DecodedKey.TYPE) packetTypeLabel(f.value) else f.value,
                                                color = Pal.text, fontSize = 13.sp)
                                        }
                                    }
                                    Text(UiText.text(R.string.logs_path), color = Pal.accent, fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                                    d.path.hops.forEach { hop ->
                                        Text("• " + hop.call + "  " + hopKindLabel(hop.kind) + (hop.qCode?.let { " ($it)" } ?: ""),
                                            fontSize = 12.sp, color = Pal.text)
                                    }
                                }
                            }
                        }
                    } else {
                        APRS_FREQUENCIES.forEach { f ->
                            Column(Modifier.fillMaxWidth().background(Pal.card, RoundedCornerShape(8.dp)).padding(8.dp)) {
                                Text(f.mhz + " MHz", fontWeight = FontWeight.Bold, color = Pal.text)
                                Text(f.regions.joinToString(", ") { r ->
                                    when (r) {
                                        "*EU" -> UiText.text(R.string.region_europe_africa)
                                        "*SAT" -> UiText.text(R.string.region_iss)
                                        else -> (flagEmoji(r) + " " + (countryName(r, locale) ?: r)).trim()
                                    }
                                }, color = Pal.muted, fontSize = 12.sp)
                            }
                        }
                        Text(UiText.text(R.string.tools_frequencies_notice), color = Pal.orange, fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(UiText.text(R.string.close)) } },
    )
}

private fun hopKindLabel(kind: PathHop.Kind): String = UiText.text(when (kind) {
    PathHop.Kind.ORIGIN -> R.string.hop_origin
    PathHop.Kind.DIGIPEATER -> R.string.hop_digi
    PathHop.Kind.ALIAS_USED -> R.string.hop_alias_used
    PathHop.Kind.ALIAS_UNUSED -> R.string.hop_alias_unused
    PathHop.Kind.NETWORK -> R.string.hop_network
    PathHop.Kind.Q_CONSTRUCT -> R.string.dec_q_construct
    PathHop.Kind.IGATE -> R.string.hop_igate
    PathHop.Kind.THIRD_PARTY -> R.string.hop_third_party
})
