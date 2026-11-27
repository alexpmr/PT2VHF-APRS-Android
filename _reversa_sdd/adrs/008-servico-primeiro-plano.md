# ADR-008: Serviço em primeiro plano sempre ativo

> Reconstruído pelo Detetive (Reversa) em 2026-11-27 a partir do código e do histórico. 🟢

**Situação:** aceita (em vigor na v1.4)

## Contexto
APRS precisa receber e transmitir com a tela desligada.

## Decisão
`AprsService` `START_STICKY` com tipos location, connectedDevice, specialUse e microphone.

## Consequências
Funciona em segundo plano; exige justificativa do `specialUse` na Play Store e notificação fixa.

## Evidência
`AndroidManifest.xml`, `service/AprsService.kt`
