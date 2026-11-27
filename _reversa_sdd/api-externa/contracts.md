# Contratos — API para outros apps (versão 1)

> Fonte: `service/ApiContract.kt` e `API.md`. 🟢

Permissão: `com.pt2vhf.aprs.permission.API` (`protectionLevel="dangerous"`). Desligada por padrão. Passcode e mensagens nunca expostos; não é possível enviar mensagens pela API.

## Eventos (broadcast do app, só para quem tem a permissão, com o serviço ativo)

| Ação | Extras |
|------|--------|
| `com.pt2vhf.aprs.event.POSITION` | `api_version` int, `call`, `lat` double, `lon` double, `symbol` (tabela+código), `comment`, `medium` (`RF`/`INTERNET`), `time` ms |
| `com.pt2vhf.aprs.event.STATUS` | `api_version`, `call`, `aprs_is_verified`, `tnc_connected`, `tracker_on`, `rx_only` boolean, `station_count` int |

## Comandos (broadcast explícito para `com.pt2vhf.aprs/.service.ApiReceiver`)

| Ação | Exige |
|------|-------|
| `com.pt2vhf.aprs.command.REQUEST_STATUS` | API ativada; responde com `event.STATUS` |
| `com.pt2vhf.aprs.command.TRACKER_START` / `TRACKER_STOP` | `api_allow_control` |
| `com.pt2vhf.aprs.command.SEND_BEACON` | `api_allow_beacon`; bloqueado no perfil Somente recepção |

A API nunca inicia o app. Mudanças incompatíveis aumentam `api_version`.
