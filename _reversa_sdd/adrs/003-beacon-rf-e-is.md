# ADR-003: Beacon por RF e APRS-IS ao mesmo tempo

> Reconstruído pelo Detetive (Reversa) em 2026-11-27 a partir do código e do histórico. 🟢

**Situação:** aceita (em vigor na v1.4)

## Contexto
A posição deve aparecer na rede mesmo sem iGate próximo e também nos rádios locais.

## Decisão
`transmit` envia por RF (se TNC e TX RF) e por APRS-IS (`TCPIP*`, se verificado).

## Consequências
Mais visibilidade; o APRS-IS descarta a cópia repetida. Usuário controla por `tx_rf`/`tx_is`.

## Evidência
`service/AprsService.kt` (`transmit`)
