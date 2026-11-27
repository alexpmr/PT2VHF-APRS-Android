# ADR-004: Modem AFSK e HDLC próprios, sem código do APRSdroid

> Reconstruído pelo Detetive (Reversa) em 2026-11-27 a partir do código e do histórico. 🟢

**Situação:** aceita (em vigor na v1.4)

## Contexto
Permitir TNC por áudio sem herdar a licença GPL do APRSdroid.

## Decisão
Implementar Bell 202, HDLC, NRZI e demodulador com PLL e 3 canais de ênfase em `core/Afsk.kt`.

## Consequências
Liberdade de licença e testes JVM. Custo: manutenção própria do modem.

## Evidência
`core/Afsk.kt`
