package com.pt2vhf.aprs.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.derivedStateOf
import com.pt2vhf.aprs.data.followNewestAtEnd
import com.pt2vhf.aprs.data.followNewestAtTop
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pt2vhf.aprs.R
import com.pt2vhf.aprs.UiText
import com.pt2vhf.aprs.core.AprsMessage
import com.pt2vhf.aprs.core.CALLSIGN_RE
import com.pt2vhf.aprs.data.AprsRepository
import com.pt2vhf.aprs.data.ChatMessage
import com.pt2vhf.aprs.data.INTERRUPTED_STATUS
import com.pt2vhf.aprs.data.MessageCriteria
import com.pt2vhf.aprs.data.OutboundSendResult
import com.pt2vhf.aprs.data.Settings
import com.pt2vhf.aprs.data.conversations
import com.pt2vhf.aprs.data.matchesMessage
import com.pt2vhf.aprs.data.searchCallsigns
import com.pt2vhf.aprs.service.AprsService
import java.util.UUID
import kotlinx.coroutines.launch

// ---------- Mensagens ----------

private fun loadCriteria(context: android.content.Context) = MessageCriteria(
    text = Settings.readOption(context, "messages_filter", ""),
    direction = Settings.readOption(context, "messages_direction", "all"),
    medium = Settings.readOption(context, "messages_medium", "all"),
    status = Settings.readOption(context, "messages_status", "all"),
    period = Settings.readOption(context, "messages_period", "all"),
)

private fun saveCriteria(context: android.content.Context, c: MessageCriteria) {
    Settings.saveOption(context, "messages_filter", c.text)
    Settings.saveOption(context, "messages_direction", c.direction)
    Settings.saveOption(context, "messages_medium", c.medium)
    Settings.saveOption(context, "messages_status", c.status)
    Settings.saveOption(context, "messages_period", c.period)
}

@Composable
fun MessagesScreen(
    settings: Settings,
    chatWith: String?,
    replyToId: Long?,
    onOpenChat: (String?, Long?) -> Unit,
    onStation: (String) -> Unit,
) {
    if (chatWith != null) {
        Conversation(settings, chatWith, replyToId, onReplyChange = { onOpenChat(chatWith, it) },
            onBack = { onOpenChat(null, null) }, onStation = onStation)
        return
    }
    val messages by AprsRepository.messages.collectAsState()
    val context = LocalContext.current
    var criteria by remember { mutableStateOf(loadCriteria(context)) }
    var newestFirst by rememberSaveable {
        mutableStateOf(Settings.readOption(context, "messages_order", "newest") == "newest")
    }
    var tab by rememberSaveable { mutableStateOf(0) }
    var filterOpen by remember { mutableStateOf(false) }
    var newChatOpen by rememberSaveable { mutableStateOf(false) }
    var deleteOpen by remember { mutableStateOf(false) }
    var deleteResult by remember { mutableStateOf<String?>(null) }
    val clock = minuteClock()
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    Column(Modifier.fillMaxSize()) {
        if (!keyboardVisible) Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            OutlinedTextField(criteria.text, {
                criteria = criteria.copy(text = it); saveCriteria(context, criteria)
            }, placeholder = {
                Text(UiText.text(R.string.messages_filter), fontSize = 12.sp, maxLines = 1)
            }, modifier = Modifier.weight(1f).height(48.dp), singleLine = true)
            TextButton(onClick = { filterOpen = true },
                modifier = Modifier.semantics { contentDescription = UiText.text(R.string.messages_filters_title) }) {
                Text("☷" + if (criteria.copy(text = "").active) " ●" else "", fontSize = 20.sp)
            }
            // #41: apagar todas, só boletins/gerais ou só pessoais.
            TextButton(onClick = { deleteOpen = true; deleteResult = null },
                modifier = Modifier.semantics { contentDescription = UiText.text(R.string.msg_delete_title) }) {
                Text("🗑", fontSize = 18.sp)
            }
        }
        deleteResult?.let { Text(it, color = Pal.muted, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp)) }
        TabRow(selectedTabIndex = tab, containerColor = Pal.bar, contentColor = Pal.accent) {
            listOf(UiText.text(R.string.chats), UiText.text(R.string.general), UiText.text(R.string.bulletins),
                UiText.text(R.string.community)).forEachIndexed { i, label ->
                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label, maxLines = 1, fontSize = 13.sp) })
            }
        }
        when (tab) {
            0 -> {
                val byContact = conversations(messages, settings.fullCall, criteria, newestFirst)
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { newChatOpen = true }) { Text("✉ " + UiText.text(R.string.new_message)) }
                }
                if (byContact.isEmpty()) Empty(UiText.text(if (criteria.active) R.string.no_chats_filtered else R.string.no_chats))
                // #64: mensagem nova aparece na hora; se a pessoa rolou para baixo, aviso "nova mensagem ↑".
                val listState = rememberLazyListState()
                val newestKey = byContact.firstOrNull()?.let { (c, l) -> c + "#" + l.maxOf { m -> m.id } }
                var unseenTop by remember { mutableStateOf(false) }
                LaunchedEffect(newestKey, newestFirst) {
                    if (newestKey == null) return@LaunchedEffect
                    if (!newestFirst) {
                        if (byContact.isNotEmpty()) listState.scrollToItem(byContact.size - 1)
                    } else if (followNewestAtTop(listState.firstVisibleItemIndex)) {
                        listState.scrollToItem(0); unseenTop = false
                    } else unseenTop = true
                }
                val atTop by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
                LaunchedEffect(atTop) { if (atTop) unseenTop = false }
                val pillScope = androidx.compose.runtime.rememberCoroutineScope()
                Box(Modifier.fillMaxSize()) {
                LazyColumn(state = listState) {
                    items(byContact, key = { it.first }) { (contact, list) ->
                        val last = list.maxBy { it.time }
                        Column(Modifier.fillMaxWidth().clickable { onOpenChat(contact, null) }.padding(16.dp, 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(contact, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1)
                                Text(messageTimeLabel(last.time, clock), color = Pal.muted, fontSize = 12.sp, maxLines = 1)
                            }
                            Text((if (last.outgoing) "↗ " else "") + last.text, color = Pal.muted, maxLines = 1,
                                overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
                        }
                    }
                }
                if (unseenTop) NewMessagePill("↑ " + UiText.text(R.string.new_message_arrived), Modifier.align(Alignment.TopCenter)) {
                    unseenTop = false
                    pillScope.launch { listState.animateScrollToItem(0) }
                }
                }
            }
            3 -> CommunityScreen(settings, onStation)
            else -> {
                val list = messages.filter {
                    if (tab == 1) it.kind == AprsMessage.Kind.MESSAGE && (it.to == "CQ" || it.to.startsWith("CQ"))
                    else it.kind == AprsMessage.Kind.BULLETIN
                }
                val filtered = list.filter { matchesMessage(it, criteria) }
                    .sortedWith(compareBy<ChatMessage> { it.time }.thenBy { it.id })
                GroupChat(filtered, settings, if (tab == 1) "CQ" else "BLN0", onStation,
                    onPrivate = { call, reply -> onOpenChat(call, reply) })
            }
        }
    }
    if (filterOpen) MessageFilterDialog(criteria, newestFirst, onDismiss = { filterOpen = false }) { c, newest ->
        criteria = c; newestFirst = newest
        saveCriteria(context, c)
        Settings.saveOption(context, "messages_order", if (newest) "newest" else "oldest")
        filterOpen = false
    }
    if (deleteOpen) DeleteMessagesDialog(onDismiss = { deleteOpen = false }, onResult = { deleteResult = it })
    if (newChatOpen) NewMessageDialog(settings, onDismiss = { newChatOpen = false }) { call ->
        newChatOpen = false
        onOpenChat(call, null)
    }
}

@Composable
private fun ChoiceGroup(title: String, value: String, options: List<Pair<String, Int>>, onChange: (String) -> Unit) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    options.forEach { (key, label) ->
        Row(Modifier.fillMaxWidth().clickable { onChange(key) }, verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = value == key, onClick = { onChange(key) })
            Text(UiText.text(label), fontSize = 14.sp)
        }
    }
}

/** #2: filtros completos e combináveis das conversas, com ordenação. */
@Composable
private fun MessageFilterDialog(
    initial: MessageCriteria,
    newestFirst: Boolean,
    onDismiss: () -> Unit,
    onApply: (MessageCriteria, Boolean) -> Unit,
) {
    var c by remember { mutableStateOf(initial) }
    var newest by remember { mutableStateOf(newestFirst) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pal.bar,
        title = { Text(UiText.text(R.string.messages_filters_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ChoiceGroup(UiText.text(R.string.station_sort_label), if (newest) "newest" else "oldest",
                    listOf("newest" to R.string.sort_newest, "oldest" to R.string.sort_oldest)) { newest = it == "newest" }
                ChoiceGroup(UiText.text(R.string.msg_filter_direction), c.direction, listOf(
                    "all" to R.string.filter_all, "in" to R.string.msg_filter_received, "out" to R.string.msg_filter_sent,
                )) { c = c.copy(direction = it) }
                ChoiceGroup(UiText.text(R.string.msg_filter_medium), c.medium, listOf(
                    "all" to R.string.filter_all, "rf" to R.string.msg_filter_rf, "internet" to R.string.station_source_internet,
                )) { c = c.copy(medium = it) }
                ChoiceGroup(UiText.text(R.string.msg_filter_status), c.status, listOf(
                    "all" to R.string.filter_all, "pending" to R.string.msg_filter_pending, "failed" to R.string.msg_filter_failed,
                )) { c = c.copy(status = it) }
                ChoiceGroup(UiText.text(R.string.msg_filter_period), c.period, listOf(
                    "all" to R.string.filter_all, "24h" to R.string.period_24h, "7d" to R.string.period_7d, "30d" to R.string.period_30d,
                )) { c = c.copy(period = it) }
            }
        },
        confirmButton = { TextButton(onClick = { onApply(c, newest) }) { Text(UiText.text(R.string.station_filter_apply)) } },
        dismissButton = {
            TextButton(onClick = { onApply(MessageCriteria(), true) }) { Text(UiText.text(R.string.station_filter_clear)) }
        },
    )
}

/** #21: pesquisa de indicativo (parcial, com SSID) para iniciar conversa. Abrir nunca transmite. */
@Composable
private fun NewMessageDialog(settings: Settings, onDismiss: () -> Unit, onOpen: (String) -> Unit) {
    val stations by AprsRepository.stations.collectAsState()
    val messages by AprsRepository.messages.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    val hits = remember(query, stations, messages) {
        searchCallsigns(query, stations.values, messages, settings.fullCall)
    }
    val manual = query.trim().uppercase()
    val manualValid = CALLSIGN_RE.matches(manual) && manual != settings.fullCall
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Pal.bar,
        title = { Text(UiText.text(R.string.new_message)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(query, { query = it.uppercase().take(12) }, singleLine = true,
                    label = { Text(UiText.text(R.string.search_callsign_part)) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth())
                // #59: destino que não parece indicativo é só avisado (CQ, BLN, objetos e serviços continuam válidos).
                if (manual.isNotBlank() && CALLSIGN_RE.matches(manual) && !com.pt2vhf.aprs.core.plausibleAmateurCallsign(manual))
                    Text(UiText.text(R.string.dest_not_callsign), color = Pal.muted, fontSize = 12.sp)
                if (query.isNotBlank() && hits.isEmpty())
                    Text(UiText.text(R.string.search_no_results), color = Pal.muted, fontSize = 13.sp)
                LazyColumn(Modifier.heightIn(max = 320.dp)) {
                    items(hits, key = { it.call }) { hit ->
                        Column(Modifier.fillMaxWidth().clickable { onOpen(hit.call) }.padding(vertical = 8.dp)) {
                            Text(hit.call, fontWeight = FontWeight.Bold)
                            val meta = listOfNotNull(
                                hit.lastContact?.let { ago(it) },
                                hit.medium?.label,
                                UiText.text(R.string.search_in_history).takeIf { hit.inHistory },
                            ).joinToString(" · ")
                            if (meta.isNotEmpty()) Text(meta, color = Pal.muted, fontSize = 12.sp)
                        }
                    }
                }
                Text(UiText.text(R.string.search_scope_notice), color = Pal.muted, fontSize = 11.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = { if (manualValid) onOpen(manual) }, enabled = manualValid) {
                Text(UiText.text(R.string.open_typed_callsign, manual.ifBlank { "…" }))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(UiText.text(R.string.cancel)) } },
    )
}

/** Ações da pressão longa (#20). Nenhuma delas transmite; só o botão Enviar transmite. */
private enum class BubbleAction { REPLY, REPLY_PUBLIC, REPLY_PRIVATE, DIRECT, COPY, RESEND, CANCEL }

/** #47: ícone, cor e texto do estado de entrega; null para mensagens recebidas ou sem confirmação. */
private fun deliveryIcon(m: ChatMessage): Pair<String, androidx.compose.ui.graphics.Color>? = when {
    !m.outgoing -> null
    m.status == "ACK recebido" -> "✓✓" to Pal.green
    m.status == "Rejeitada" -> "⛔" to androidx.compose.ui.graphics.Color(0xFFF26F70)
    m.status.startsWith("Sem ACK") || m.status == "Sem conexão para enviar" || m.status == INTERRUPTED_STATUS -> "✗" to Pal.orange
    m.status == "Cancelada" -> "⊘" to Pal.muted
    m.status.startsWith("Aguardando ACK") || m.status.startsWith("Enviada") -> "✓" to Pal.green
    m.status == "Enviando" -> "🕓" to Pal.yellow
    else -> null
}

private fun canResend(m: ChatMessage) = m.outgoing && m.msgId != null &&
    (m.status.startsWith("Sem ACK") || m.status == "Rejeitada" || m.status == "Sem conexão para enviar" || m.status == "Cancelada" ||
        m.status == INTERRUPTED_STATUS)

private fun canCancel(m: ChatMessage) = m.outgoing && (m.status.startsWith("Aguardando") || m.status == "Enviando")

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Bubble(
    m: ChatMessage,
    showSender: Boolean,
    actions: List<BubbleAction>,
    onStation: (String) -> Unit,
    clock: Long = System.currentTimeMillis(),
    retryInfo: Pair<Int, Long>? = null,
    maxAttempts: Int = 5,
    onAction: (BubbleAction) -> Unit,
) {
    var menu by remember { mutableStateOf(false) }
    val quoted = AprsRepository.message(m.replyToId)
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (m.outgoing) Alignment.End else Alignment.Start) {
        BoxWithConstraints(Modifier.fillMaxWidth(),
            contentAlignment = if (m.outgoing) Alignment.TopEnd else Alignment.TopStart) {
            val bubbleMax = maxWidth * 0.90f
            Box {
                Column(
                    Modifier.widthIn(max = bubbleMax)
                        .background(if (m.outgoing) Pal.mine else Pal.card, RoundedCornerShape(14.dp))
                        .combinedClickable(onClick = {}, onLongClick = { if (actions.isNotEmpty()) menu = true },
                            onLongClickLabel = UiText.text(R.string.message_actions))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    if (showSender && !m.outgoing) Text(m.from, color = Pal.accent, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                        modifier = Modifier.clickable { onStation(m.from) })
                    if (quoted != null) Text("↪ ${quoted.from}: ${quoted.text}", color = Pal.muted, fontSize = 12.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(m.text, fontSize = 15.sp)
                    val stamp = messageTimeLabel(m.time, clock)
                    val retry = retryInfo?.takeIf { m.status.startsWith("Aguardando") }
                    val retryText = retry?.let { (attempt, at) ->
                        " · " + UiText.text(R.string.msg_attempt, attempt, maxAttempts) + " · " +
                            UiText.text(R.string.msg_next_retry, ((at - clock) / 1000).coerceAtLeast(0))
                    }.orEmpty()
                    val meta = if (m.outgoing) "$stamp · ${m.to} · ${UiText.messageStatus(m.status)}$retryText"
                        else "$stamp · ${UiText.receptionVia(m.via)}"
                    val icon = deliveryIcon(m)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (icon != null) Text(icon.first + " ", color = icon.second, fontSize = 13.sp,
                            modifier = Modifier.clickable(enabled = actions.contains(BubbleAction.RESEND) ||
                                actions.contains(BubbleAction.CANCEL)) { menu = true })
                        Text(meta, color = icon?.second?.takeIf { m.status != "Enviando" } ?: Pal.muted, fontSize = 11.sp)
                    }
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    actions.forEach { action ->
                        DropdownMenuItem(text = {
                            Text(when (action) {
                                BubbleAction.RESEND -> UiText.text(R.string.msg_resend)
                                BubbleAction.CANCEL -> UiText.text(R.string.msg_cancel_retries)
                                BubbleAction.REPLY -> UiText.text(R.string.reply_to_message)
                                BubbleAction.REPLY_PUBLIC -> UiText.text(R.string.reply_public_cq)
                                BubbleAction.REPLY_PRIVATE -> UiText.text(R.string.reply_private, m.from)
                                BubbleAction.DIRECT -> UiText.text(R.string.direct_message_to, m.from)
                                BubbleAction.COPY -> UiText.text(R.string.copy_text)
                            })
                        }, onClick = { menu = false; onAction(action) })
                    }
                }
            }
        }
    }
}

@Composable
private fun Composer(
    placeholder: String,
    destination: String,
    replyContext: ChatMessage?,
    onCancelReply: () -> Unit,
    showDestination: Boolean = true,
    onSend: (String, String) -> Unit,
) {
    var text by rememberSaveable(placeholder) { mutableStateOf("") }
    var pendingId by remember(placeholder) { mutableStateOf<String?>(null) }
    var pendingText by remember(placeholder) { mutableStateOf("") }
    var error by remember(placeholder) { mutableStateOf<String?>(null) }
    val results by AprsRepository.sendResults.collectAsState()
    LaunchedEffect(results, pendingId) {
        val result = pendingId?.let { results[it] }
        if (result != null) {
            if (result.submitted) {
                // Preserve text edited while the service processed the previous send request.
                if (text == pendingText) text = ""
                error = null
                onCancelReply()
            } else {
                error = result.reason.ifBlank { UiText.text(R.string.tx_route_unavailable) }
            }
            pendingId = null
        }
    }
    // Do not strand the composer forever if Android kills/restarts the service or its
    // serial TX worker blocks. The draft stays intact; an absent result is NOT proof of TX failure.
    LaunchedEffect(pendingId) {
        val request = pendingId
        if (request != null) {
            kotlinx.coroutines.delay(15_000L)
            if (pendingId == request && AprsRepository.sendResults.value[request] == null) {
                error = "Sem resposta do serviço de envio. Confira a aba Logs antes de tentar novamente."
                pendingId = null
            }
        }
    }
    Column(Modifier.fillMaxWidth().background(Pal.bar)) {
        if (replyContext != null) Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text("↪ " + UiText.text(R.string.replying_to, replyContext.from) + ": " + replyContext.text,
                color = Pal.accent, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f))
            TextButton(onClick = onCancelReply) { Text("✕") }
        }
        // #65: "Para:" só nos chats de grupo (na conversa o contato já está no cabeçalho), em linha pequena.
        if (showDestination) Text(UiText.text(R.string.message_destination, destination), color = Pal.muted, fontSize = 10.sp,
            maxLines = 1, modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 2.dp))
        error?.let { Text(it, color = Pal.orange, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 12.dp)) }
        if (text.length > 67) Text(UiText.text(R.string.message_split_notice, (text.length + 66) / 67), fontSize = 11.sp,
            color = Pal.muted, modifier = Modifier.padding(horizontal = 12.dp))
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 2.dp, top = 2.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            // #56: o campo fica com no mínimo uma linha inteira e cresce até 5; o histórico é que encolhe.
            OutlinedTextField(text, { text = it; error = null }, placeholder = { Text(placeholder, maxLines = 1,
                overflow = TextOverflow.Ellipsis) },
                modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                textStyle = androidx.compose.ui.text.TextStyle(color = Pal.text, fontSize = 16.sp),
                minLines = 1, maxLines = 5)
            val sending = pendingId != null
            IconButton(onClick = {
                if (text.isNotBlank() && pendingId == null) {
                    val id = UUID.randomUUID().toString()
                    pendingId = id
                    pendingText = text
                    onSend(text, id)
                }
            }, enabled = !sending && text.isNotBlank(), modifier = Modifier.size(48.dp)) {
                if (sending) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = Pal.accent)
                else Icon(Icons.AutoMirrored.Filled.Send, contentDescription = UiText.text(R.string.send),
                    tint = if (text.isNotBlank()) Pal.accent else Pal.muted)
            }
        }
    }
}

internal fun dispatchMessage(context: android.content.Context, to: String, text: String, requestId: String, replyTo: Long?) {
    val extras = mutableMapOf(AprsService.EXTRA_TO to to, AprsService.EXTRA_TEXT to text,
        AprsService.EXTRA_REQUEST_ID to requestId)
    if (replyTo != null) extras[AprsService.EXTRA_REPLY_TO] = replyTo.toString()
    val dispatched = runCatching { AprsService.send(context, AprsService.ACTION_MESSAGE, extras) }.isSuccess
    if (!dispatched) AprsRepository.reportSendResult(OutboundSendResult(requestId, false,
        UiText.text(R.string.tx_dispatch_failed)))
}

@Composable
internal fun Conversation(
    settings: Settings,
    contact: String,
    replyToId: Long?,
    onReplyChange: (Long?) -> Unit,
    onBack: () -> Unit,
    onStation: (String) -> Unit,
    showHeader: Boolean = true,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val messages by AprsRepository.messages.collectAsState()
    val stations by AprsRepository.stations.collectAsState()
    val list = messages.filter { it.kind == AprsMessage.Kind.MESSAGE &&
        ((it.outgoing && it.to == contact) ||
            (!it.outgoing && it.from == contact &&
                (it.to == settings.fullCall || it.to == settings.callsign.uppercase()))) }
        .sortedWith(compareBy<ChatMessage> { it.time }.thenBy { it.id })
    val replyContext = messages.firstOrNull { it.id == replyToId }
    val retries by AprsRepository.retryAt.collectAsState()
    var tick by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(retries.isNotEmpty()) {
        while (retries.isNotEmpty()) { kotlinx.coroutines.delay(1000); tick = System.currentTimeMillis() }
    }
    val clock = maxOf(minuteClock(), tick)
    val state = rememberLazyListState()
    val unseen = followNewest(state, list)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Column(Modifier.fillMaxSize()) {
        // #65: barra da conversa baixa, com Voltar em ícone na mesma linha do contato.
        if (showHeader) Row(Modifier.fillMaxWidth().background(Pal.bar).padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = UiText.text(R.string.back), tint = Pal.accent)
            }
            Row(Modifier.weight(1f).clickable { onStation(contact) }, verticalAlignment = Alignment.CenterVertically) {
                Text(contact, fontWeight = FontWeight.Bold, maxLines = 1)
                stations[contact]?.let { Text("  ${it.medium.label} · ${ago(it.lastHeard)}", color = Pal.green, fontSize = 12.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis) }
            }
        }
        Box(Modifier.weight(1f)) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp), state = state, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.id }) { m ->
                val actions = if (m.outgoing) listOfNotNull(
                    BubbleAction.RESEND.takeIf { canResend(m) && !settings.rxOnly },
                    BubbleAction.CANCEL.takeIf { canCancel(m) }, BubbleAction.COPY)
                    else listOf(BubbleAction.REPLY, BubbleAction.DIRECT, BubbleAction.COPY)
                Bubble(m, false, actions, onStation, clock, retries[m.id], settings.msgRetries) { action ->
                    when (action) {
                        BubbleAction.REPLY -> onReplyChange(m.id)
                        BubbleAction.DIRECT -> onReplyChange(null)
                        BubbleAction.COPY -> clipboard.setText(AnnotatedString(m.text))
                        BubbleAction.RESEND -> AprsService.send(context, AprsService.ACTION_MSG_RESEND,
                            mapOf(AprsService.EXTRA_MESSAGE_ID to m.id.toString()))
                        BubbleAction.CANCEL -> AprsService.send(context, AprsService.ACTION_MSG_CANCEL,
                            mapOf(AprsService.EXTRA_MESSAGE_ID to m.id.toString()))
                        else -> {}
                    }
                }
            }
        }
        if (unseen.value) NewMessagePill("↓ " + UiText.text(R.string.new_message_arrived), Modifier.align(Alignment.BottomCenter)) {
            unseen.value = false
            scope.launch { if (list.isNotEmpty()) state.animateScrollToItem(list.size - 1) }
        }
        }
        if (contact == settings.fullCall) Text(UiText.text(R.string.cannot_message_self), color = Pal.orange,
            modifier = Modifier.padding(12.dp))
        else Composer(UiText.text(R.string.message_to, contact), contact, replyContext, { onReplyChange(null) },
            showDestination = false) { message, requestId ->
            dispatchMessage(context, contact, message, requestId, replyContext?.id)
        }
    }
}

@Composable
private fun GroupChat(
    list: List<ChatMessage>,
    settings: Settings,
    defaultTo: String,
    onStation: (String) -> Unit,
    onPrivate: (String, Long?) -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val state = rememberLazyListState()
    var publicReplyId by rememberSaveable(defaultTo) { mutableStateOf<Long?>(null) }
    val publicReply = list.firstOrNull { it.id == publicReplyId }
    val isCq = defaultTo == "CQ"
    val clock = minuteClock()
    val unseen = followNewest(state, list, settings.fullCall)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    Column(Modifier.fillMaxSize()) {
        if (list.isEmpty()) Empty(if (isCq) UiText.text(R.string.no_general) else UiText.text(R.string.no_bulletins))
        Box(Modifier.weight(1f)) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 12.dp), state = state, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.id }) { m ->
                val mine = m.outgoing || m.from == settings.fullCall
                val actions = when {
                    mine -> listOf(BubbleAction.COPY)
                    isCq -> listOf(BubbleAction.REPLY_PUBLIC, BubbleAction.REPLY_PRIVATE, BubbleAction.DIRECT, BubbleAction.COPY)
                    // Bulletins: never answer to BLN0 silently; the safe reply is private to the sender.
                    else -> listOf(BubbleAction.REPLY_PRIVATE, BubbleAction.DIRECT, BubbleAction.COPY)
                }
                Bubble(m, true, actions, onStation, clock) { action ->
                    when (action) {
                        BubbleAction.REPLY_PUBLIC -> publicReplyId = m.id
                        BubbleAction.REPLY_PRIVATE -> onPrivate(m.from, m.id)
                        BubbleAction.DIRECT -> onPrivate(m.from, null)
                        BubbleAction.COPY -> clipboard.setText(AnnotatedString(m.text))
                        else -> {}
                    }
                }
            }
        }
        if (unseen.value) NewMessagePill("↓ " + UiText.text(R.string.new_message_arrived), Modifier.align(Alignment.BottomCenter)) {
            unseen.value = false
            scope.launch { if (list.isNotEmpty()) state.animateScrollToItem(list.size - 1) }
        }
        }
        Composer(
            if (isCq) UiText.text(R.string.message_all) else UiText.text(R.string.bulletin_default),
            if (isCq) UiText.text(R.string.destination_public_cq) else UiText.text(R.string.destination_bulletin),
            publicReply, { publicReplyId = null },
        ) { message, requestId ->
            dispatchMessage(context, defaultTo, message, requestId, publicReply?.id)
        }
    }
}

@Composable
internal fun OutlinedChoice(label: String, selected: Boolean, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, border = androidx.compose.foundation.BorderStroke(1.dp,
        if (selected) Pal.green else Pal.line)) {
        Text(label, color = if (selected) Pal.green else Pal.muted, fontSize = 12.sp)
    }
}

/** #64: aviso flutuante de mensagem nova fora da área visível. */
@Composable
internal fun NewMessagePill(label: String, modifier: Modifier, onClick: () -> Unit) {
    Text(label, color = Pal.ground, fontSize = 13.sp, fontWeight = FontWeight.Bold,
        modifier = modifier.padding(8.dp).background(Pal.accent, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 6.dp))
}

/**
 * #64: rola até a mensagem nova quando a pessoa já está no fim (ou quando ela mesma enviou);
 * senão devolve true no estado para mostrar o aviso "nova mensagem ↓".
 */
@Composable
private fun followNewest(state: LazyListState, list: List<ChatMessage>, ownCall: String? = null): androidx.compose.runtime.MutableState<Boolean> {
    val unseen = remember { mutableStateOf(false) }
    val lastId = list.lastOrNull()?.id
    val lastMine = list.lastOrNull()?.let { it.outgoing || (ownCall != null && it.from == ownCall) } ?: false
    LaunchedEffect(lastId) {
        if (lastId == null) return@LaunchedEffect
        val info = state.layoutInfo
        val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
        if (followNewestAtEnd(lastVisible, info.totalItemsCount, lastMine)) {
            state.scrollToItem(list.size - 1); unseen.value = false
        } else unseen.value = true
    }
    val atEnd by remember { derivedStateOf { !state.canScrollForward } }
    LaunchedEffect(atEnd) { if (atEnd) unseen.value = false }
    return unseen
}
