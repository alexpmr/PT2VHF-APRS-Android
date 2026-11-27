# Dicionário de Dados — PT2VHF-APRS-Android

> Gerado pelo Arqueólogo em 2026-11-27. Escala: 🟢 CONFIRMADO · 🟡 INFERIDO · 🔴 LACUNA

## 1. Bancos SQLite

### 1.1 `stations.db` (versão 3, WAL) — `data/StationStore.kt` 🟢

**`stations`**
| Coluna | Tipo | Obrig. | Descrição |
|--------|------|--------|-----------|
| `call` | TEXT PK | sim | Indicativo ou nome do objeto/item |
| `lat`, `lon` | REAL | sim | Última posição conhecida |
| `symbol_table`, `symbol` | TEXT | sim | Símbolo APRS (1 caractere cada) |
| `comment` | TEXT | sim | Comentário já decodificado para exibição |
| `heard` | INTEGER | sim | Última recepção (ms) |
| `first_seen` | INTEGER | sim | Primeira recepção (ms); migração v2 copiou `heard` |
| `medium` | TEXT | sim | `RF` ou `INTERNET` |
| `via` | TEXT | sim | Path bruto separado por vírgula |
| `speed` | REAL | não | km/h, só se veio no pacote |
| `course` | INTEGER | não | graus |
| `heard_by` | TEXT | sim | Digipeaters que entregaram por RF (primeiro salto) |

**`rf_links`** — `source`, `destination` (PK composta), `seen` INTEGER. Só de pacotes do TNC. 🟢
**`own_track`** — `id` autoincremento, `recorded`, `lat`, `lon`. 🟢
**`rf_events`** (v3) — `id`, `call`, `kind` (`heard`|`digi`), `via`, `direct` (0/1), `time`, `distance` (km), `lat`, `lon`, `own_lat`, `own_lon`. Retenção 365 dias. 🟢

Índices: `idx_track_recorded`, `idx_station_heard`, `idx_link_seen`, `idx_rf_events_time`. 🟢

### 1.2 `messages.db` (versão 1, WAL) — `data/MessageStore.kt` 🟢

**`messages`**
| Coluna | Tipo | Obrig. | Descrição |
|--------|------|--------|-----------|
| `id` | INTEGER PK | sim | Id local (baseado em `System.currentTimeMillis()` + incremento) |
| `from_call`, `to_call` | TEXT | sim | Origem e destinatário |
| `text` | TEXT | sim | Texto exibido (decodificado; enviado sem acentos se a opção estiver ligada) |
| `time` | INTEGER | sim | ms |
| `outgoing` | INTEGER | sim | 1 = enviada por mim |
| `medium` | TEXT | sim | `RF`, `INTERNET` ou vazio (enviada) |
| `via` | TEXT | sim | "RF direto", "RF via X", "RF via iGate X", "APRS-IS" |
| `msg_id` | TEXT | não | ID APRS (1–5 alfanuméricos); nulo em boletins/CQ |
| `status` | TEXT | sim | Valor interno em português (ver §3) |
| `kind` | TEXT | sim | `MESSAGE`, `BULLETIN` (ACK/REJ não são gravados como mensagem) |
| `reply_to` | INTEGER | não | Associação local à mensagem respondida (#20) |

Índices por `time`, `to_call`, `from_call`. 🟢

### 1.3 `packets.db` (versão 2, WAL) — `data/PacketLogStore.kt` 🟢
**`packets`** — `id` PK, `time`, `dir` (`RX`|`TX`), `medium`, `raw` (TNC2), `src`, `dst`, `type` (chave de `packetType`). Índices `idx_packets_time`, `idx_packets_src_time` (v2). Retenção padrão 7 dias e 20.000 linhas. 🟢

## 2. SharedPreferences `pt2vhf_aprs` (`data/Settings.kt`)

Campos principais (padrão; faixa aplicada na leitura e na gravação). 🟢

| Grupo | Chave | Padrão | Faixa / valores |
|-------|-------|--------|-----------------|
| Estação | `callsign` | "" | maiúsculo; válido pela ITU para `configured` |
| | `ssid` | 9 | 0–15 |
| | `symbol_table` / `symbol` | `/` `>` | 1 caractere |
| | `comment` | "PT2VHF APRS www.PT2VHF.com" | site sempre acrescentado na TX |
| | `profile` | `custom` | custom, base, mobile, portable, rx |
| APRS-IS | `server` / `port` | soam.aprs2.net / 14580 | host válido, 1–65535 |
| | `filter` | `r/4000` | expandido com a posição |
| | `connect_aprs_is` | true | |
| Transmissão | `tx_rf` / `tx_is` | true / true | |
| | `rf_path` | `WIDE1-1,WIDE2-1` | |
| Beacon | `beacon_mode` | smart | smart, periodic, manual |
| | `periodic_interval` | 900 s | 30–86400 |
| | `smart_slow_speed` / `smart_fast_speed` | 5 / 90 km/h | 1–200 / 5–300 |
| | `smart_slow_interval` / `smart_fast_interval` | 1800 / 60 s | 30–86400 |
| | `smart_turn_angle` / `smart_turn_slope` / `smart_turn_time` | 28° / 255 / 15 s | 5–90 / 0–1000 / 5–600 |
| | `stopped_minutes` | 30 | 0–1440 |
| | `tracker_auto_start` | true | |
| | `ambiguity` | 0 | 0–4 |
| | `send_course_speed` / `send_altitude` | true / false | |
| | `voice_frequency` | "" | `FFF.FFFMHz` |
| Posição | `position_source` | gps | gps, manual |
| | `manual_lat` / `manual_lon` | vazio | ±90 / ±180 |
| | `gps_precision` | high | high, balanced, low |
| | `use_network_location` | true | |
| TNC | `tnc_type` | bt | bt, ble, tcp, audio, usb |
| | `bt_address` / `ble_address` | "" | |
| | `tnc_host` / `tnc_port` | "" / 8001 | |
| | `usb_baud` | 9600 | 1200…115200 |
| Áudio | `audio_input` / `audio_output` | default | default, unprocessed, wired, usb, bt |
| | `audio_volume` / `audio_txdelay` / `audio_vox_lead` | 70 / 300 ms / 0 | 5–100 / 50–1500 / 0–1500 |
| | `audio_hq` | true | |
| PTT | `ptt_mode` | vox | vox, rts, dtr, cat |
| | `ptt_baud` / `cat_protocol` / `civ_address` | 9600 / kenwood / 0x94 | |
| Mensagens | `msg_retries` / `msg_retry_base` | 5 / 30 s | 1–10 / 10–300 |
| | `strip_accents` | true | |
| | `ret_*_days` / `ret_*_max` | 0 / 5000 | privada, geral, boletim |
| Notificações | `notify_messages` | true | |
| | `notify_bulletins` / `notify_group` | false / false | |
| | `sound_ack` / `sound_digi_echo` | true / false | |
| | `alert_fav_first/return/position/rf`, `alert_my_digi` | true/true/false/true, false | |
| | `alert_return_min` | 60 | 5–1440 |
| | `quiet_hours` / `quiet_start` / `quiet_end` | false / 22 / 7 | 0–23 |
| API | `api_enabled` / `api_allow_control` / `api_allow_beacon` | false | |
| Sistema | `battery_saver` / `battery_auto_percent` | off / 20 | off, on, auto / 5–80 |
| | `announce_version` | true | |
| | `update_notify` / `share_callsign` | true / true | |
| | `keep_screen_on`, `monitor_enabled`, `station_max_age_h` | false, true, 0 | 0–720 |
| iGate | `igate_enabled` / `igate_is_to_rf` | false / false | |
| | `igate_rf_path` / `igate_local_min` / `igate_max_per_min` | WIDE1-1 / 30 / 6 | 5–180 / 1–30 |
| Digi | `digi_enabled` | false | |
| | `digi_aliases`, `digi_fill_in`, `digi_wide_n`, `digi_max_n` | "", true, true, 2 | 1–7 |
| | `digi_trap_drop`, `digi_direct_only`, `digi_viscous`, `digi_viscous_sec` | false, true, true, 5 | 1–30 |
| | `digi_adaptive`, `digi_radius_km`, `digi_source_sec`, `digi_max_per_min` | true, 0, 60, 20 | 0–1000 / 0–3600 / 1–120 |

Outras chaves (fora da `data class`): `favorite_stations` (StringSet), `my_objects` (JSON), `profile_snapshot_<perfil>` (JSON), `track_retention_days` (7), `tracklog_enabled`, `log_retention_days` (7), `log_max` (20000), `map_type`, `notif_sound_*`, `notif_ver_*`, `update_*`, `announced_version`, flags `tracker_explicit_off`, `aprs_manual_pause`, `tnc_manual_pause`. 🟢

## 3. Valores internos gravados (`data/InternalText.kt`)

Mantidos em português para compatibilidade com dados antigos; a UI traduz por `UiTextKeys`. Nunca mudar sem migração. 🟢

| Categoria | Valores |
|-----------|---------|
| Tipo de estação | Estação, Digipeater, iGate, Estação meteorológica, Móvel, Pessoa, Aeronave, Embarcação |
| Situação de mensagem | Recebida, Enviando, `Enviada · <meio>`, `Aguardando ACK · <meio>`, `Aguardando ACK (Nª tentativa)`, ACK recebido, Rejeitada, Sem conexão para enviar, `Sem ACK após N tentativas`, Cancelada, Interrompida |
| Erros de conexão | Passcode não verificado…, Servidor encerrou…, Conexão Bluetooth encerrada, KISS TCP encerrada, Falha ao conectar, BLE encerrada, Aparelho sem serviço KISS BLE, Falha ao acionar o PTT, `Erro na escrita APRS-IS: <causa>`, e os de USB/áudio |
| Recepção | RF direto, `RF via <digi>` |

## 4. Modelos em memória (`data/Models.kt`)

| Modelo | Campos principais |
|--------|-------------------|
| `Station` | call, lat, lon, symbolTable, symbol, comment, lastHeard, medium, via, speedKmh?, course?, heardBy, firstSeen; `role` derivado do símbolo |
| `ChatMessage` | id, from, to, text, time, outgoing, medium?, via, msgId?, status, kind, replyToId? |
| `LinkStatus` | ~50 campos de estado (APRS-IS, TNC, GPS, tracker, contadores, áudio, PTT, bateria, iGate, digi) |
| `RfEvent` | call, kind, via, direct, time, distanceKm?, lat?, lon?, ownLat?, ownLon? |
| `MyObject` | id, name, item, lat, lon, symbolTable, symbol, comment, intervalMin (0 = uma vez), active, lastSent |
| `SavedRoute` | id, name, visible, points (até 20.000) |
| `PacketLogEntry` | id, time, direction, medium, raw, source, destination, type |

## 5. Arquivos em `filesDir`
`routes.json` (rotas), `messages.json` (legado, migrado), `last_crash.txt`, `last_crash_seen.txt`, `last_operation_error.txt`, cache de tiles do osmdroid. 🟢
