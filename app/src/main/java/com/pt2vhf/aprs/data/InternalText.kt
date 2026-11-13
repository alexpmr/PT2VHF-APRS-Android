package com.pt2vhf.aprs.data

/**
 * #76: valores internos gravados no banco/preferências (tipo de estação, situação da mensagem, erro de conexão).
 * Continuam em português para manter compatibilidade com o que versões anteriores já gravaram, mas agora vêm de um
 * só lugar: a tela traduz pelo mapeamento em `UiTextKeys`, e um teste de unidade garante que todos têm tradução.
 * Nunca mudar um valor daqui sem migrar os dados gravados.
 */
object InternalText {
    // Tipos de estação (#3)
    const val ROLE_STATION = "Estação"
    const val ROLE_DIGI = "Digipeater"
    const val ROLE_IGATE = "iGate"
    const val ROLE_WEATHER = "Estação meteorológica"
    const val ROLE_MOBILE = "Móvel"
    const val ROLE_PERSON = "Pessoa"
    const val ROLE_AIRCRAFT = "Aeronave"
    const val ROLE_VESSEL = "Embarcação"
    val ROLES = listOf(ROLE_STATION, ROLE_DIGI, ROLE_IGATE, ROLE_WEATHER, ROLE_MOBILE, ROLE_PERSON, ROLE_AIRCRAFT, ROLE_VESSEL)

    // Situação das mensagens
    const val MSG_RECEIVED = "Recebida"
    const val MSG_ACKED = "ACK recebido"
    const val MSG_REJECTED = "Rejeitada"
    const val MSG_SENDING = "Enviando"
    const val MSG_NO_CONNECTION = "Sem conexão para enviar"
    const val MSG_CANCELLED = "Cancelada"
    const val MSG_AWAITING_STEM = "Aguardando ACK"
    const val MSG_SENT_PREFIX = "Enviada · "
    const val MSG_AWAITING_PREFIX = "Aguardando ACK · "
    const val MSG_RETRY_PREFIX = "Aguardando ACK ("
    const val MSG_RETRY_SUFFIX = "ª tentativa)"
    const val MSG_NO_ACK_PREFIX = "Sem ACK após "
    const val MSG_NO_ACK_SUFFIX = " tentativas"
    fun sent(via: String) = MSG_SENT_PREFIX + via
    fun awaiting(via: String) = MSG_AWAITING_PREFIX + via
    fun retry(attempt: Int) = "$MSG_RETRY_PREFIX$attempt$MSG_RETRY_SUFFIX"
    fun noAck(retries: Int) = "$MSG_NO_ACK_PREFIX$retries$MSG_NO_ACK_SUFFIX"

    // Erros de conexão (USB e áudio ficam em UsbTnc/AudioTnc)
    const val ERR_UNVERIFIED = "Passcode não verificado pelo servidor"
    const val ERR_SERVER_CLOSED = "Servidor encerrou a conexão"
    const val ERR_BT_CLOSED = "Conexão Bluetooth encerrada"
    const val ERR_KISS_CLOSED = "Conexão KISS TCP encerrada"
    const val ERR_CONNECT_FAILED = "Falha ao conectar"
    const val ERR_BLE_CLOSED = "Conexão Bluetooth LE encerrada"
    const val ERR_BLE_NO_KISS = "Aparelho sem serviço KISS BLE"
    const val ERR_PTT_FAILED = "Falha ao acionar o PTT"
    const val ERR_WRITE_PREFIX = "Erro na escrita APRS-IS: "
    fun writeError(cause: String) = ERR_WRITE_PREFIX + cause

    // Caminho de recepção das mensagens
    const val VIA_RF_DIRECT = "RF direto"
    const val VIA_RF_PREFIX = "RF via "
}
