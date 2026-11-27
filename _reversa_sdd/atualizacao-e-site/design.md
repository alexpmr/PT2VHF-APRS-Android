# Design — Atualização e site

> Gerado pelo Redator (Reversa) em 2026-11-27.

## Arquivos

- `data/UpdateManager.kt`
- `data/UpdateCheck.kt`
- `core/Aprs.kt (`isNewerVersion`)`

## Interfaces principais
- `UpdateCheck.fetchResult(call?)` → `Available | UpToDate | Failure`; `endpoint(call)`; `reportedCallsign(settings)`; `reportKey(call, version)`
- `UpdateManager.maybeCheck(context, minIntervalMs, force, fromBackground)`; `ignore(context, version)`

## Fluxo
Ver `flowcharts/atualizacao-e-site.md`.

## Testes existentes
`CallsignReportTest` (registro de indicativo) e testes de `isNewerVersion` em `AprsTest`.

## Decisões relacionadas
Ver `adrs/` e `architecture.md`.
