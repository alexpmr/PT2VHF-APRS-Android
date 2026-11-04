# API para outros apps Android (item #15) — versão 1

Desligada por padrão. Para usar: no PT2VHF APRS, **Configurações → Integração com outros apps → Ativar API**;
o app cliente declara e pede ao usuário a permissão `com.pt2vhf.aprs.permission.API` (nível *dangerous*).
O passcode APRS-IS e as mensagens nunca são expostos. Não é possível enviar mensagens pela API.

## Eventos (broadcasts enviados pelo PT2VHF APRS)

Só recebidos por quem tem a permissão, com o serviço ativo. Registre o receiver em tempo de execução.

| Ação | Extras |
|---|---|
| `com.pt2vhf.aprs.event.POSITION` | `api_version` (int), `call`, `lat`, `lon` (double), `symbol` (tabela+código), `comment`, `medium` (`RF`/`INTERNET`), `time` (ms) |
| `com.pt2vhf.aprs.event.STATUS` | `api_version`, `call`, `aprs_is_verified`, `tnc_connected`, `tracker_on`, `rx_only` (boolean), `station_count` (int) |

## Comandos (broadcast explícito para `com.pt2vhf.aprs/.service.ApiReceiver`)

Exigem a permissão e o serviço já em execução; a API nunca inicia o app nem transmite por conta própria.

| Ação | Requer em Configurações |
|---|---|
| `com.pt2vhf.aprs.command.REQUEST_STATUS` | API ativada (responde com `event.STATUS`) |
| `com.pt2vhf.aprs.command.TRACKER_START` / `TRACKER_STOP` | "Permitir que apps liguem/desliguem o rastreador" |
| `com.pt2vhf.aprs.command.SEND_BEACON` | "Permitir que apps peçam beacon" (bloqueado no perfil Somente recepção) |

Exemplo (Kotlin, app cliente):

```kotlin
sendBroadcast(Intent("com.pt2vhf.aprs.command.REQUEST_STATUS")
    .setClassName("com.pt2vhf.aprs", "com.pt2vhf.aprs.service.ApiReceiver"))
```

Mudanças incompatíveis aumentam `api_version`.
