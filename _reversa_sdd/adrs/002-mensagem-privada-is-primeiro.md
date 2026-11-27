# ADR-002: Mensagem privada pelo APRS-IS primeiro, RF como alternativa

> Reconstruído pelo Detetive (Reversa) em 2026-11-27 a partir do código e do histórico. 🟢

**Situação:** aceita (em vigor na v1.4)

## Contexto
Enviar a mesma mensagem com o mesmo ID por RF e pela internet gera duplicata e ACKs confusos.

## Decisão
`transmitPrivateMessage` usa APRS-IS verificado; só sem internet usa o TNC. Nunca os dois.

## Consequências
Entrega mais confiável e sem duplicata. Em área sem internet depende de iGate local para alcançar a rede.

## Evidência
`service/AprsService.kt` (`transmitPrivateMessage`)
