package com.pt2vhf.aprs.core

import java.util.Locale

/**
 * #95 iGate no celular: regras puras (sem Android) para encaminhar RF → APRS-IS e APRS-IS → RF.
 *
 * Regras seguidas (prática do APRS-IS para iGates):
 * - RF→IS: nada com TCPIP/TCPXX/NOGATE/RFONLY no caminho, nem consultas genéricas "?"; terceiros "}" sobem
 *   desencapsulados e só se o cabeçalho interno também estiver liberado; o servidor completa o "qAR,MEUCALL".
 * - IS→RF: só mensagens (e ack/rej) para estações ouvidas localmente em RF, de quem não está em RF local,
 *   encapsuladas como terceiros `}ORIGEM>DEST,TCPIP,MEUCALL*:info`.
 * - Dedupe e limite de taxa ficam em [GateDupeFilter] e [RateLimiter].
 */

/** Motivo de um pacote não ser encaminhado (contadores e logs). */
enum class GateSkip {
    MALFORMED, NO_GATE_PATH, QUERY, DUPLICATE, NOT_MESSAGE, BULLETIN, NOT_LOCAL, SENDER_IS_LOCAL, OWN,
    THIRD_PARTY, RATE_LIMIT,
}

sealed class GateResult {
    data class Send(val line: String) : GateResult()
    data class Skip(val reason: GateSkip) : GateResult()
}

/** Cabeçalho TNC2 já separado. */
data class Tnc2(val source: String, val destination: String, val path: List<String>, val info: String) {
    fun header(): String = source + ">" + destination + (if (path.isEmpty()) "" else "," + path.joinToString(","))
    fun line(): String = header() + ":" + info
    /** Chave de duplicata: origem, destino e conteúdo (o caminho muda a cada digipeater). */
    fun dupeKey(): String = "$source>$destination:${info.trimEnd(' ', '\r', '\n')}"

    companion object {
        fun parse(raw: String): Tnc2? {
            // Informação termina no primeiro CR/LF (regra do APRS-IS).
            val clean = raw.substringBefore('\r').substringBefore('\n')
            val colon = clean.indexOf(':')
            val gt = clean.indexOf('>')
            if (colon < 0 || gt < 1 || gt > colon) return null
            val parts = clean.substring(gt + 1, colon).split(',').map { it.trim() }
            if (parts.isEmpty() || parts[0].isEmpty() || parts.any { it.isEmpty() }) return null
            val source = clean.substring(0, gt).trim().uppercase(Locale.ROOT)
            if (source.isEmpty() || source.length > 9) return null
            return Tnc2(source, parts[0].uppercase(Locale.ROOT), parts.drop(1),
                clean.substring(colon + 1))
        }
    }
}

private val NO_GATE = setOf("TCPIP", "TCPXX", "NOGATE", "RFONLY")

/** Caminho com marca que proíbe passar do RF para a internet. */
fun hasNoGateHop(path: List<String>): Boolean = path.any { it.removeSuffix("*").uppercase(Locale.ROOT) in NO_GATE }

/** Saltos q (qAR, qAC...) só existem no APRS-IS; em RF indicam pacote mal formado. */
private fun hasQConstruct(path: List<String>): Boolean = path.any { it.length == 3 && it.startsWith("q") }

/** Mantém o '*' só no último salto usado (formato TNC2 usual dos iGates). */
fun normalizeUsedMarks(path: List<String>): List<String> {
    val last = path.indexOfLast { it.endsWith("*") }
    return path.mapIndexed { i, h -> if (i == last) h else h.removeSuffix("*") }
}

/**
 * RF→IS: linha a enviar ao APRS-IS para um quadro ouvido no TNC, ou o motivo de não enviar.
 * O servidor acrescenta "qAR,<indicativo do login>" ao receber de um cliente verificado; mandamos explícito.
 */
fun rfToIs(raw: String, myCall: String): GateResult {
    val outer = Tnc2.parse(raw) ?: return GateResult.Skip(GateSkip.MALFORMED)
    if (hasQConstruct(outer.path)) return GateResult.Skip(GateSkip.MALFORMED)
    if (hasNoGateHop(outer.path)) return GateResult.Skip(GateSkip.NO_GATE_PATH)
    var pkt = outer
    if (outer.info.startsWith("}")) {
        // Terceiros: sobe o pacote interno, se ele também puder subir.
        val inner = Tnc2.parse(outer.info.substring(1)) ?: return GateResult.Skip(GateSkip.MALFORMED)
        if (inner.info.startsWith("}")) return GateResult.Skip(GateSkip.MALFORMED)
        if (hasNoGateHop(inner.path)) return GateResult.Skip(GateSkip.NO_GATE_PATH)
        if (hasQConstruct(inner.path)) return GateResult.Skip(GateSkip.MALFORMED)
        pkt = inner
    }
    if (pkt.info.startsWith("?")) return GateResult.Skip(GateSkip.QUERY)
    if (pkt.info.isEmpty()) return GateResult.Skip(GateSkip.MALFORMED)
    val gated = pkt.copy(path = normalizeUsedMarks(pkt.path) + listOf("qAR", myCall.uppercase(Locale.ROOT)))
    return GateResult.Send(gated.line())
}

/**
 * IS→RF: quadro RF (terceiros) para uma linha recebida do APRS-IS, ou o motivo de não transmitir.
 * [isLocal] diz se a estação foi ouvida diretamente em RF por este aparelho há pouco tempo.
 */
fun isToRf(raw: String, myCall: String, tocall: String, rfPath: List<String>, isLocal: (String) -> Boolean): GateResult {
    if (raw.startsWith("#")) return GateResult.Skip(GateSkip.MALFORMED)
    val pkt = Tnc2.parse(raw) ?: return GateResult.Skip(GateSkip.MALFORMED)
    val me = myCall.uppercase(Locale.ROOT)
    if (pkt.info.startsWith("}")) return GateResult.Skip(GateSkip.THIRD_PARTY)
    val msg = parseMessage(pkt.info) ?: return GateResult.Skip(GateSkip.NOT_MESSAGE)
    if (msg.kind == AprsMessage.Kind.BULLETIN || msg.addressee.startsWith("BLN") || msg.addressee.startsWith("NWS"))
        return GateResult.Skip(GateSkip.BULLETIN)
    if (pkt.source == me || msg.addressee == me) return GateResult.Skip(GateSkip.OWN)
    if (!isLocal(msg.addressee)) return GateResult.Skip(GateSkip.NOT_LOCAL)
    // Quem mandou já está em RF local: a mensagem chega pelo rádio, sem precisar do iGate.
    if (isLocal(pkt.source)) return GateResult.Skip(GateSkip.SENDER_IS_LOCAL)
    val inner = Tnc2(pkt.source, pkt.destination, listOf("TCPIP", "$me*"), pkt.info)
    val outer = Tnc2(me, tocall, rfPath.map { it.trim().uppercase(Locale.ROOT) }.filter { it.isNotEmpty() },
        "}" + inner.line())
    return GateResult.Send(outer.line())
}

/** Duplicatas dentro de uma janela de tempo (padrão 30 s). Seguro entre threads. */
class GateDupeFilter(private val windowMs: Long = 30_000L, private val maxEntries: Int = 2000) {
    private val seen = LinkedHashMap<String, Long>()

    /** true se a chave já foi vista dentro da janela; senão registra e devolve false. */
    @Synchronized fun isDuplicate(key: String, now: Long): Boolean {
        val it = seen.entries.iterator()
        while (it.hasNext()) { if (now - it.next().value > windowMs) it.remove() else break }
        val last = seen[key]
        if (last != null && now - last <= windowMs) return true
        seen.remove(key)
        seen[key] = now
        while (seen.size > maxEntries) seen.remove(seen.keys.first())
        return false
    }

    /** Só consulta, sem registrar. */
    @Synchronized fun contains(key: String, now: Long): Boolean = seen[key]?.let { now - it <= windowMs } == true
}

/** No máximo [maxEvents] eventos por [windowMs]. */
class RateLimiter(private val maxEvents: Int, private val windowMs: Long = 60_000L) {
    private val times = ArrayDeque<Long>()

    @Synchronized fun tryAcquire(now: Long): Boolean {
        while (times.isNotEmpty() && now - times.first() >= windowMs) times.removeFirst()
        if (maxEvents <= 0 || times.size >= maxEvents) return false
        times.addLast(now)
        return true
    }

    @Synchronized fun count(now: Long): Int {
        while (times.isNotEmpty() && now - times.first() >= windowMs) times.removeFirst()
        return times.size
    }
}

/**
 * Estações ouvidas em RF por este aparelho (não via iGate/terceiros), com a hora.
 * "Local" = ouvida há no máximo [windowMs].
 */
class LocalHeard(private val windowMs: Long = 30 * 60_000L) {
    private val heard = HashMap<String, Long>()

    @Synchronized fun heard(call: String, now: Long) { heard[call.uppercase(Locale.ROOT)] = now }

    @Synchronized fun isLocal(call: String, now: Long): Boolean =
        heard[call.uppercase(Locale.ROOT).trim()]?.let { now - it <= windowMs } == true

    @Synchronized fun count(now: Long): Int {
        heard.entries.removeAll { now - it.value > windowMs }
        return heard.size
    }
}
