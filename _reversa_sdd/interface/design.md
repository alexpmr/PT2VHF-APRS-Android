# Design — Interface

> Gerado pelo Redator (Reversa) em 2026-11-27.

## Arquivos

- `ui/App.kt`
- `ui/Screens.kt`
- `ui/MessagesUi.kt`
- `ui/SettingsUi.kt`
- `ui/StationDetailUi.kt`
- `ui/RadioUi.kt`
- `ui/LogsUi.kt`
- `ui/CommunityUi.kt`
- `ui/ToolsUi.kt`
- `ui/NotificationsUi.kt`, `ui/RadioLinksUi.kt`, `ui/Features019.kt`, `ui/StationMarker.kt`, `ui/TimeSeriesChart.kt`
- `ui/MapTypeUi.kt`
- `MainActivity.kt`
- `UiText.kt`
- `UiTextKeys.kt`

## Interfaces principais

- `App()` raiz
- Intents para `AprsService`
- Coleta de `AprsRepository.*` com `collectAsStateWithLifecycle`

## Fluxo
Ver `flowcharts/interface.md`.

## Testes existentes
Nenhum teste automatizado de UI.

## Decisões relacionadas
Ver `adrs/` e `architecture.md`.
