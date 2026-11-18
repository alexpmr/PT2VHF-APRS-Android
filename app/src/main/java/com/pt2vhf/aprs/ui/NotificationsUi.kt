package com.pt2vhf.aprs.ui

import android.app.Activity
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.service.NotificationType
import com.pt2vhf.aprs.service.NotificationTypes

/**
 * #106: som de um tipo de aviso. A escolha vale na hora (o canal do Android é recriado com o som novo), não depende
 * do "Salvar". "Testar" mostra um aviso de exemplo no mesmo canal; "Android" abre vibração/prioridade do canal.
 */
@Composable
internal fun NotificationSoundRow(type: NotificationType) {
    val context = LocalContext.current
    var label by remember(type) { mutableStateOf(NotificationTypes.soundLabel(context, type)) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val uri: Uri? = if (Build.VERSION.SDK_INT >= 33)
            result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
        else @Suppress("DEPRECATION") result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        NotificationTypes.setSound(context, type, NotificationTypes.valueFromPicker(uri))
        label = NotificationTypes.soundLabel(context, type)
    }
    Column(Modifier.padding(start = 48.dp)) {
        Text(UiText.text(R.string.notif_sound_current, label), color = Pal.muted, fontSize = 12.sp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            TextButton(onClick = {
                runCatching { picker.launch(NotificationTypes.pickerIntent(context, type)) }
            }) { Text(UiText.text(R.string.notif_sound_choose), fontSize = 13.sp) }
            TextButton(onClick = { NotificationTypes.test(context, type) }) {
                Text(UiText.text(R.string.notif_test), fontSize = 13.sp)
            }
            TextButton(onClick = {
                NotificationTypes.ensureChannels(context)
                runCatching { context.startActivity(NotificationTypes.channelSettingsIntent(context, type)) }
            }) { Text(UiText.text(R.string.notif_android_settings), fontSize = 13.sp) }
        }
    }
}
