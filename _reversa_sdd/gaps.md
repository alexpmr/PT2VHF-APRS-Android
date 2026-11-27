# Lacunas — PT2VHF-APRS-Android

> Gerado pelo Revisor (Reversa) em 2026-11-27.

## 1. Funções ausentes na `main`
| # | Lacuna | Impacto | Onde está a solução |
|---|--------|---------|---------------------|
| G1 | Posição comprimida (base-91), também em clima e objetos | Estações LoRa APRS só aparecem no Log | PR #33 (backlog #109) |
| G2 | Reconexão automática do Bluetooth clássico | Usuário reconecta à mão | — |
| G3 | Variante Google Play (sem aviso OTA) | `main` ainda é a versão do site | PR #31 |

## 2. Lacunas de teste
| # | Área | Situação |
|---|------|----------|
| T1 | `AprsService` (beacon, mensagens, retentativas, alertas) | sem teste |
| T2 | Transportes BT, BLE, USB, áudio | sem teste; só `AprsIsConnectionTest` |
| T3 | Stores SQLite e migrações | sem teste |
| T4 | `Settings` (faixas e perfis) | sem teste direto |
| T5 | `Kiss`/`Ax25` | sem arquivo de teste próprio 🟡 |
| T6 | UI Compose, widget, Android Auto | sem teste; sem `androidTest` |
| T7 | iGate e digi em RF real | não validados em campo |

## 3. Lacunas de documentação
| # | Item |
|---|------|
| D1 | Não há documento de arquitetura no repositório além deste `_reversa_sdd/` |
| D2 | Regras de retenção e limites (iGate, digi) só no código e no `BACKLOG.md` |
| D3 | Ver `questions.md` para as decisões que o código não explica |

## 4. Pontos de atenção (de `code-analysis.md`)
A1 id não atômico · A2 BT sem reconexão · A3 alerta por tamanho de lista · A4 ACK fora dos contadores · A5 id de notificação por `toInt()` · A6 allowlist de exemplo no Auto · A7 `allowBackup` · A8 posição comprimida · A9 sem R8 · A10 envios viram "Interrompida" ao reiniciar (desejado).
