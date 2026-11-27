# Design — iGate e digipeater inteligente

> Gerado pelo Redator (Reversa) em 2026-11-27.

## Arquivos

- `core/Igate.kt`
- `core/SmartDigipeater.kt`
- `service/AprsService.kt (≈905–1026)`

## Interfaces principais
- `rfToIs(raw, myCall): GateResult` (`core/Igate.kt:69`)
- `isToRf(raw, myCall, tocall, rfPath, isLocal): GateResult` (`core/Igate.kt:92`)
- `SmartDigipeater.onHeard(raw, now, myLat?, myLon?, …)` e `release(token)` para o atraso viscoso
- `isPriorityPacket(destination, info)`

## Fluxo
Ver `flowcharts/gateway-igate-digi.md`.

## Testes existentes
`IgateTest`, `SmartDigipeaterTest` (`core/`).

## Decisões relacionadas
Ver `adrs/` e `architecture.md`.
