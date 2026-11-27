# Contratos — Protocolos externos

## APRS-IS (TCP, padrão `soam.aprs2.net:14580`) 🟢
- Login: `user <CALL-SSID> pass <passcode> vers PT2VHF-APRS-Android <versão> filter <filtro>`
- Filtro: `r/<lat>/<lon>/<km>` (expandido de `r/<km>`) + `g/<BASE>/<BASE>-*`; troca em sessão com `#filter …`.
- Resposta `# logresp <CALL> verified` libera transmissão; `unverified` = só recepção.
- Linhas TNC2 terminadas em `\r\n`; linhas `#` são comentários do servidor.
- Timeouts: conexão 15 s, leitura 120 s; reconexão 5 s dobrando até 120 s.

## KISS (BT SPP, BLE, TCP, USB) 🟢
- `FEND 0xC0`, `FESC 0xDB`, `TFEND 0xDC`, `TFESC 0xDD`; comando `0x00` (dados, porta 0).
- Quadro AX.25 UI: endereços de 7 bytes, controle `0x03`, PID `0xF0`, até 8 digis.
- BLE: serviço KISS `00000001-ba2a-46c9-ae49-01b0961f68bb` ou Nordic UART; MTU pedido 247.

## Site oficial 🟢
- `GET https://www.pt2vhf.com/update.php?product=aprs-android[&call=<BASE>]` sem redirecionamento, timeout 10 s.
- User-Agent `PT2VHF-APRS-Android/<versão>`. HTTP ≠ 200 = falha.
- JSON: `status` (`available` = há versão), `version`, `notes`, `page`, `files[]` com `name`/`url`; usa o primeiro `.apk` cujo `url` começa com `https://www.pt2vhf.com/`, senão a página do site.

## Intents internas do serviço 🟢
`reload`, `beacon`, `message`, `tracker_on`, `tracker_off`, `bt_connect`, `ble_connect`, `msg_resend`, `msg_cancel`, `msg_give_up_resend`, `object_send`, `object_kill`, `api_*`, `stop`.
