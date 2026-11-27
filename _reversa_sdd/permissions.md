# Permissões e Controle de Acesso — PT2VHF-APRS-Android

> Gerado pelo Detetive (Reversa) em 2026-11-27.

## 1. Permissões Android (`AndroidManifest.xml`)

| Permissão | Para quê | Pedida em | Conf. |
|-----------|----------|-----------|-------|
| `INTERNET`, `ACCESS_NETWORK_STATE` | APRS-IS, KISS TCP, tiles, atualização | instalação | 🟢 |
| `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` | GPS e rede para beacon | abertura (`MainActivity`) | 🟢 |
| `BLUETOOTH_CONNECT`, `BLUETOOTH_SCAN` (`neverForLocation`), `BLUETOOTH`/`BLUETOOTH_ADMIN` até API 30 | TNC Bluetooth clássico e BLE | ao escolher TNC | 🟢 |
| `RECORD_AUDIO`, `MODIFY_AUDIO_SETTINGS` | TNC por áudio (AFSK) e SCO | ao escolher TNC áudio | 🟢 |
| `POST_NOTIFICATIONS` | Mensagens, alertas, atualização | abertura (33+) | 🟢 |
| `FOREGROUND_SERVICE` + `_LOCATION`, `_CONNECTED_DEVICE`, `_SPECIAL_USE`, `_MICROPHONE` | Serviço sempre ativo (tipos `location|connectedDevice|specialUse|microphone`) | — | 🟢 |
| `ACCESS_BACKGROUND_LOCATION`, `RECEIVE_BOOT_COMPLETED`, `WAKE_LOCK` | **Não declaradas**: o tracker depende do serviço em primeiro plano; o app não inicia sozinho no boot | — | 🟢 |
| Feature `usb.host` (opcional; também bluetooth, bluetooth_le, gps, microphone, todas `required=false`) | TNC/PTT por USB-OTG | evento de USB | 🟢 |
| `com.pt2vhf.aprs.permission.API` (`dangerous`) | Outros apps usarem a API | o app cliente pede ao usuário | 🟢 |

## 2. Chaves funcionais (autorização dentro do app)

| Chave | Efeito | Padrão | Conf. |
|-------|--------|--------|-------|
| Indicativo válido | Libera qualquer transmissão | — | 🟢 |
| Perfil `rx` | Bloqueia toda transmissão | — | 🟢 |
| `tx_rf` / `tx_is` | Meios de transmissão permitidos | ligados | 🟢 |
| Login APRS-IS verificado | Libera TX por internet e iGate | — | 🟢 |
| `igate_enabled`, `igate_is_to_rf` | iGate RF→IS e IS→RF | desligados | 🟢 |
| `digi_enabled` | Digipeater | desligado | 🟢 |
| `api_enabled` | Liga eventos e comandos da API | desligado | 🟢 |
| `api_allow_control` | Comandos do tracker pela API | desligado | 🟢 |
| `api_allow_beacon` | Beacon pela API | desligado | 🟢 |
| `share_callsign` | Envia indicativo na consulta de versão | ligado | 🟢 |

## 3. Matriz de quem pode fazer o quê

| Ação | Usuário no app | Outro app (API) | Rede APRS |
|------|----------------|-----------------|-----------|
| Ver mapa e mensagens | sim | eventos de posição/status se API ligada | — |
| Enviar beacon | sim (exceto RX) | só com `api_allow_beacon` | — |
| Ligar/desligar tracker | sim | só com `api_allow_control` | — |
| Enviar mensagem | sim (exceto RX) | não | — |
| Repetir pacote (digi) | liga a função | não | qualquer estação, dentro dos limites |
| Injetar mensagem em RF (iGate IS→RF) | liga a função | não | remetente da internet, só para estação local |

## 4. Dados sensíveis

| Dado | Onde | Proteção | Conf. |
|------|------|----------|-------|
| Indicativo, posição, mensagens | SharedPreferences, SQLite | sandbox do app; `allowBackup="true"` inclui no backup | 🟢 |
| Passcode | calculado na hora, nunca gravado nem exportado | — | 🟢 |
| Endereços Bluetooth | prefs; fora do backup de configurações | — | 🟢 |
| Posição | só para APRS (pública por natureza); nunca para o site | — | 🟢 |
