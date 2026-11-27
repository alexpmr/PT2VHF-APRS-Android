# ADR-005: Passcode do APRS-IS calculado a partir do indicativo

> Reconstruído pelo Detetive (Reversa) em 2026-11-27 a partir do código e do histórico. 🟢

**Situação:** aceita (em vigor na v1.4)

## Contexto
O passcode é um hash público; pedir ao usuário só gera erro de digitação.

## Decisão
`aprsPasscode` calcula na hora do login e nunca grava nem exporta.

## Consequências
Configuração mais simples. Login só é verificado com indicativo bem digitado.

## Evidência
`core/Aprs.kt:222`
