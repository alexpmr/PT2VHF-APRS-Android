# Design — Serviço e transportes

> Gerado pelo Redator (Reversa) em 2026-11-27.

## Arquivos

- `service/AprsService.kt`
- `service/Connections.kt`
- `service/BleTnc.kt`
- `service/UsbTnc.kt`
- `service/AudioTnc.kt`
- `service/NotificationTypes.kt`
- `service/StatusWidget.kt`
- `service/CarApp.kt`

## Interfaces principais

- Ações: `reload`, `beacon`, `message`, `tracker_on/off`, `bt_connect`, `ble_connect`, `msg_resend`, `msg_cancel`, `msg_give_up_resend`, `object_send/kill`, `stop`
- `AprsIsClient(host, port, login)`
- Interface comum de TNC: `connect`, `send(frame)`, `close`

## Fluxo
Ver `flowcharts/servico-e-transportes.md`.

## Testes existentes
Só `AprsIsConnectionTest` (`service/`); serviço, TNCs e notificações sem teste automatizado.

## Decisões relacionadas
Ver `adrs/` e `architecture.md`.
