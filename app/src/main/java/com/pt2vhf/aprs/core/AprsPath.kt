package com.pt2vhf.aprs.core

import java.util.Locale

/**
 * Caminho de um pacote APRS explicado salto a salto (#58.6).
 * Só interpreta o que está no cabeçalho; não inventa saltos nem posições.
 */
data class PathHop(
    val call: String,
    val kind: Kind,
    /** Para [Kind.Q_CONSTRUCT]: código q (qAR, qAC...). */
    val qCode: String? = null,
) {
    enum class Kind {
        /** Estação que gerou o pacote. */
        ORIGIN,
        /** Digipeater que repetiu o pacote por RF (marcado com '*'). */
        DIGIPEATER,
        /** Alias genérico já consumido (WIDE1*, WIDE2* etc.), sem indicativo real. */
        ALIAS_USED,
        /** Alias ainda não usado: o pacote pedia mais saltos que não aconteceram. */
        ALIAS_UNUSED,
        /** Elemento não-RF (TCPIP, DMR...). */
        NETWORK,
        /** Construção q do APRS-IS. */
        Q_CONSTRUCT,
        /** Gateway (iGate ou servidor) que entregou o pacote ao APRS-IS. */
        IGATE,
        /** Gateway de pacote terceiro (cabeçalho "}") ouvido por RF. */
        THIRD_PARTY,
    }
}

data class PathSummary(
    val hops: List<PathHop>,
    /** Repetições por RF efetivamente marcadas no path. */
    val rfHops: Int,
    /** Indicativo do último digipeater real, se identificado. */
    val lastDigipeater: String?,
    /** iGate que injetou o pacote no APRS-IS (depois de qAR/qAr/qAo/qAO/qAS...). */
    val igate: String?,
    val qCode: String?,
    /** true quando o pacote saiu direto da estação para o iGate/rádio, sem digipeater. */
    val direct: Boolean,
)

private val GENERIC_ALIAS = Regex("^(WIDE|TRACE|RELAY|TEMP|GATE|ECHO)\\d?(-\\d{1,2})?$")

private fun isGenericAlias(hop: String): Boolean {
    val name = hop.uppercase(Locale.ROOT).removeSuffix("*")
    return GENERIC_ALIAS.matches(name) || name.startsWith("WIDE") || name.startsWith("TRACE")
}

fun describePath(packet: AprsPacket): PathSummary {
    val hops = ArrayList<PathHop>()
    hops += PathHop(packet.source, PathHop.Kind.ORIGIN)
    var qCode: String? = null
    var igate: String? = null
    var lastDigi: String? = null
    // O '*' do último salto usado vale para todos os anteriores (AX.25 "has-been-repeated").
    val beforeQ = packet.path.takeWhile { !it.startsWith("q") }
    val lastUsed = beforeQ.indexOfLast { it.endsWith("*") }
    var namedDigis = 0
    var usedAliases = 0
    beforeQ.forEachIndexed { i, raw ->
        val name = raw.removeSuffix("*")
        val used = i <= lastUsed
        val kind = when {
            isGenericAlias(name) -> if (used) PathHop.Kind.ALIAS_USED else PathHop.Kind.ALIAS_UNUSED
            isNonRfHop(name) -> PathHop.Kind.NETWORK
            used -> PathHop.Kind.DIGIPEATER
            else -> PathHop.Kind.ALIAS_UNUSED
        }
        when (kind) {
            PathHop.Kind.DIGIPEATER -> { namedDigis++; lastDigi = name }
            PathHop.Kind.ALIAS_USED -> usedAliases++
            else -> Unit
        }
        hops += PathHop(name, kind)
    }
    val afterQ = packet.path.drop(beforeQ.size)
    if (afterQ.isNotEmpty()) {
        qCode = afterQ.first()
        hops += PathHop(afterQ.first(), PathHop.Kind.Q_CONSTRUCT, qCode)
        afterQ.drop(1).lastOrNull()?.let { gw ->
            igate = gw
            hops += PathHop(gw, PathHop.Kind.IGATE)
        }
    }
    packet.thirdPartyGateway?.let { hops += PathHop(it, PathHop.Kind.THIRD_PARTY) }
    // Com indicativos inseridos pelos digis, cada indicativo é um salto. Sem eles, um alias
    // consumido (WIDE1*, WIDE2*) prova ao menos um salto de digipeater não identificado.
    val rfHops = if (namedDigis > 0) namedDigis else minOf(usedAliases, 1)
    return PathSummary(hops, rfHops, lastDigi, igate, qCode, rfHops == 0)
}

/** Significado da construção q (APRS-IS q-construct). Chave para texto traduzido na UI. */
enum class QMeaning { RF_IGATE, RF_IGATE_UNVERIFIED, CLIENT_VERIFIED, CLIENT_UNVERIFIED, SERVER, INTERNET, OTHER }

fun qMeaning(code: String?): QMeaning? = when (code) {
    null -> null
    "qAR", "qAo" -> QMeaning.RF_IGATE
    "qAr", "qAO" -> QMeaning.RF_IGATE_UNVERIFIED
    "qAC" -> QMeaning.CLIENT_VERIFIED
    "qAX", "qAU" -> QMeaning.CLIENT_UNVERIFIED
    "qAS" -> QMeaning.SERVER
    "qAI" -> QMeaning.INTERNET
    else -> QMeaning.OTHER
}

/** q-constructs que indicam que o pacote veio de RF por um iGate. */
fun qIsRfGate(code: String?): Boolean = code in setOf("qAR", "qAr", "qAo", "qAO")
