# Design — Estado e persistência

> Gerado pelo Redator (Reversa) em 2026-11-27.

## Arquivos

- `data/AprsRepository.kt`
- `data/Models.kt`
- `data/Settings.kt`
- `data/MessageLogic.kt`
- `data/StationStore.kt`
- `data/MessageStore.kt`
- `data/PacketLogStore.kt`
- `data/SettingsBackup.kt`
- `data/AprsDroidImport.kt`
- `data/InternalText.kt`
- `data/Community.kt`
- `data/CrashLog.kt`

## Interfaces principais
- `AprsRepository.ingest(packet, medium, ownCall, now): IncomingMessage?` (`data/AprsRepository.kt:493`)
- `StateFlow` de estações, mensagens, status, monitor e logs no `AprsRepository`
- `Settings.load(context)` / `Settings.save(context, s)`
- `conversations(...)`, `interruptPending`, `retentionVictims(messages, policy)` (`data/MessageLogic.kt`)
- `SettingsBackup.export / preview / apply`; `parseFlatPrefs`, `convertAprsDroid`

## Fluxo
Ver `flowcharts/estado-e-persistencia.md`.

## Testes existentes
`MessageLogicTest`, `StationCriteriaTest`, `StationFormattingTest`, `StationInsightsTest`, `RfStatsTest`, `MapTypeTest`, `MapIconSizeTest`, `SsidAdviceTest`, `CallsignReportTest`, `Features0114Test`, `Features0115Test`, `Features140Test` (`data/`). Stores SQLite e `Settings` sem teste direto.

## Decisões relacionadas
Ver `adrs/` e `architecture.md`.
