# Análise de Código — PT2VHF-APRS-Android

> Gerado pelo Arqueólogo (Reversa) em 2026-11-27, nível Completo. Base: `main` @ `79e6b39` (v1.4).
> Escala: 🟢 CONFIRMADO (lido no código) · 🟡 INFERIDO · 🔴 LACUNA
> Caminhos relativos a `app/src/main/java/com/pt2vhf/aprs/`.

---

## 1. Módulo `protocolo-aprs` (`core/`)

Lógica APRS em Kotlin puro, sem imports Android, coberta por testes JVM. 🟢

### 1.1 Funções principais

| Função | Arquivo | Assinatura | Comportamento | Conf. |
|--------|---------|-----------|---------------|-------|
| `aprsPasscode` | `core/Aprs.kt:222` | `(callsign: String): Int` | Hash público APRS-IS: semente `0x73e2`, XOR de pares de caracteres do indicativo sem SSID, máscara `0x7fff` | 🟢 |
| `fullCallsign` | `core/Aprs.kt:234` | `(callsign, ssid)` | Maiúsculo, remove SSID digitado; acrescenta `-ssid` só se 1..15 | 🟢 |
| `effectiveAprsFilter` | `core/Aprs.kt:278` | `(filter, lat, lon, ownCall)` | Expande `r/<km>` em `r/lat/lon/km` com 3 casas; remove o atalho sem GPS; acrescenta sempre `g/BASE/BASE-*` para receber mensagens de todos os SSIDs | 🟢 |
| `AprsPacket.parse` | `core/Aprs.kt:315` | `(line): AprsPacket?` | TNC2 `ORIG>DEST,PATH:INFO`; ignora `#`; desencapsula terceiros `}` uma única vez (profundidade 1) e guarda `thirdPartyGateway` | 🟢 |
| `parsePosition` | `core/Aprs.kt:384` | `(info): Position?` | Só posição **não comprimida** `! = / @`; aceita ambiguidade (espaços nos minutos → centro da caixa); rejeita minutos ≥ 60; extensão `CCC/SSS` (nós→km/h ×1,852) exceto para símbolo de clima `_` | 🟢 |
| `parseMessage` | `core/Aprs.kt:417` | `(info): AprsMessage?` | `:DEST9____:texto{ID}AA`; ack/rej tolerante a espaços e `}` vazio (#101); boletim se destinatário começa com `BLN` | 🟢 |
| `positionInfo` | `core/Aprs.kt:470` | `(lat, lon, table, symbol, comment, course?, speed?, messaging, ambiguity)` | `=` (com mensagens) ou `!`; comentário cortado em 43 caracteres | 🟢 |
| `messageInfo` | `core/Aprs.kt:480` | `(to, text, id?)` | Destinatário com 9 colunas, troca `| ~ {` por espaço, texto ≤ 67 | 🟢 |
| `splitMessage` | `core/Aprs.kt:489` | `(text, limit = 67)` | Divide preferindo espaços; corte forçado se o espaço ficar antes da metade | 🟢 |
| `beaconCommentWithSite` / `composeBeaconComment` | `core/Aprs.kt:460`, `core/AprsText.kt:325` | | Remove qualquer URL do pt2vhf.com digitada e **sempre** termina o comentário com `www.PT2VHF.com`, com frequência de voz e altitude antes, dentro de 43 caracteres | 🟢 |
| `SmartBeaconing` | `core/Aprs.kt:533` | classe | Parâmetros HamHUD: lento 5 km/h/1800 s, rápido 90 km/h/60 s, curva 28° + 255/v, tempo mínimo de curva 15 s, intervalo parado opcional | 🟢 |
| `isNewerVersion` | `core/Aprs.kt:566` | | Compara `x.y.z` numericamente; `1.0` > `0.1.15` | 🟢 |
| `Kiss.encode` / `Kiss.Decoder` | `core/Kiss.kt` | | FEND/FESC; só quadros de dados porta 0 | 🟢 |
| `Ax25.fromTnc2` / `toTnc2` | `core/Kiss.kt` | | UI frame `0x03 0xF0`, até 8 digis, remove `q*`/`TCPIP`; marca `*` pelo bit H | 🟢 |
| `describePath` / `qMeaning` | `core/AprsPath.kt` | | Explica salto a salto (origem, digi, alias usado/não usado, rede, q-construct, iGate, terceiro) | 🟢 |
| `parseMicE` | `core/AprsDevice.kt:207` | `(dest, info)` | Mic-E completo: latitude no destino, longitude/velocidade/curso nos bytes, status (8 padrões + custom), altitude base-91 `xxx}`, aparelho pelo prefixo/sufixo | 🟢 |
| `deviceByTocall` / `identifyDevice` | `core/AprsDevice.kt` | | Tabela `aprs-deviceid` com curingas `? n *`; padrão mais específico vence; `APZVHF` = este app | 🟢 |
| `decodePhg` / `announcedFrequency` | `core/AprsDevice.kt` | | PHG (potência p², altura 10·2^h pés, ganho, direção ×45°) e `146.520MHz T088 +060` | 🟢 |
| `packetType` | `core/AprsExtras.kt:16` | | Classifica pelo primeiro caractere (position, weather, message, ack, bulletin, telemetry, object, item, status, mice, query…) | 🟢 |
| `objectInfo` / `parseObject` | `core/AprsExtras.kt` | | Objeto `;NOME_____*DDHHMMz…` e item `)NOME!…`, vivo/encerrado | 🟢 |
| `parseAprsWeather` | `core/AprsWeather.kt` | | Clima não comprimido; °F→°C, mph→km/h, centésimos de polegada→mm; faixas de validade (pressão 700–1100 hPa, umidade `h00`=100%) | 🟢 |
| `parseTelemetryFrame` / `applyTelemetryDefinition` | `core/AprsTelemetry.kt` | | `T#seq,a1..a5,bbbbbbbb`; PARM/UNIT/EQNS/BITS; equação a·x²+b·x+c | 🟢 |
| `decodeAprsText` | `core/AprsText.kt:249` | | Só exibição: ASCII puro, UTF-8 válido, senão CP1252; controles viram U+FFFD | 🟢 |
| `applyAmbiguity` | `core/AprsText.kt:289` | | Níveis 0..4 trocam dígitos dos minutos por espaço | 🟢 |
| `stripAccents` | `core/AsciiFold.kt` | | ASCII imprimível; tabela de especiais; emojis viram `?` | 🟢 |
| `checkAmateurCallsign` | `core/CallsignPrefixes.kt:684` | | Formato 3–6 caracteres, prefixo alocado pela ITU, algarismo separador, sufixo terminando em letra, SSID 0–15 | 🟢 |
| `decodePacket` | `core/PacketDecoder.kt:475` | | Decodificador da aba Ferramentas: reaproveita os parsers; o que não entende vira `NOT_DECODED` | 🟢 |

### 1.2 Algoritmos não triviais
- **Passcode APRS-IS** (público), derivado do indicativo; o app nunca pede a senha ao usuário. 🟢
- **Ambiguidade de posição na leitura**: espaços nos minutos viram o centro da caixa (+0,05, +0,5, +5 ou +30 minutos). 🟢
- **Mic-E**: decodificação de 6 caracteres do destino com 3 bits de mensagem padrão/custom; correções de longitude 180–189/190–199; velocidade ≥ 800 e curso ≥ 400. 🟢
- **Classificação de tocall** pelo número de caracteres fixos ×10 + comprimento do padrão. 🟢

### 1.3 Limites e lacunas conhecidas
- Posição **comprimida** (base-91) não é decodificada: estações LoRa APRS aparecem só no Log (backlog #109, PR #33 fora da `main`). 🟢
- Clima e objetos comprimidos também não. 🟢
- `formatLatitude` arredonda minutos 59,995+ para o grau seguinte. 🟢

---

## 2. Módulo `modem-e-radio` (`core/Afsk.kt`, `core/RadioControl.kt`)

| Componente | Comportamento | Conf. |
|------------|---------------|-------|
| `Hdlc` | CRC-16/X.25 (FCS), bit-stuffing, flags `0x7E`, NRZI, decodificador com limite de 400 bytes | 🟢 |
| `AfskModulator` | Bell 202, 48 kHz, 1200/2200 Hz em fase contínua; TXDELAY em flags (mín. 2), tom de VOX opcional, cauda; tom de teste de 3 s | 🟢 |
| `AfskDemodulator` | Correlação I/Q por bit, discriminador normalizado, PLL digital; modo alta qualidade com 3 canais de ênfase (1,0/2,0/0,5) e dedupe de 1 s; nível RMS a cada 100 ms; `busy` = algum canal no meio de quadro | 🟢 |
| `BleKiss` | Perfis KISS BLE (`00000001-ba2a-…`) e Nordic UART; MTU pedido 247, fragmentação em MTU−3 | 🟢 |
| `CatPtt` | Kenwood `TX;`/`RX;`, Yaesu `TX1;`/`TX0;`, Yaesu antigo 5 bytes, Icom CI-V `FE FE addr E0 1C 00 01|00 FD` (padrão 0x94 IC-7300) | 🟢 |
| `usbControlLines` / `pttLines` | DTR/RTS por chip (CDC SET_CONTROL_LINE_STATE, FTDI SET_MODEM_CTRL, CP210x SET_MHS); só a linha escolhida sobe durante TX | 🟢 |
| `ftdiBaudDivisor` | Divisor FTDI 3 MHz em oitavos | 🟢 |

Implementação própria declarada no comentário, sem código do APRSdroid (GPL). 🟢

---

## 3. Módulo `gateway-igate-digi` (`core/Igate.kt`, `core/SmartDigipeater.kt`, `service/AprsService.kt:905-1026`)

### 3.1 iGate
| Regra | Evidência | Conf. |
|-------|-----------|-------|
| RF→IS rejeita caminho com `TCPIP/TCPXX/NOGATE/RFONLY`, q-construct em RF, consultas `?`, info vazia | `core/Igate.kt:794` | 🟢 |
| Terceiros `}` sobem desencapsulados, só se o interno também puder subir | `core/Igate.kt:799` | 🟢 |
| Linha enviada com `qAR,<MEUCALL>` explícito e `*` só no último salto usado | `core/Igate.kt:809` | 🟢 |
| IS→RF só mensagens (inclui ack/rej) para estação ouvida localmente em RF na janela (padrão 30 min), nunca boletins/NWS, nunca se o remetente também é local; encapsula `}ORIG>DEST,TCPIP,MEUCALL*:info` | `core/Igate.kt:817` | 🟢 |
| Dedupe de 30 s e limite IS→RF por minuto (padrão 6) | `service/AprsService.kt:93-95,981` | 🟢 |
| Ativo só com iGate ligado, indicativo válido, fora do modo RX, APRS-IS **verificado**; IS→RF exige também TX por RF e TNC | `service/AprsService.kt:907-909` | 🟢 |

### 3.2 Digipeater inteligente (`SmartDigipeater.onHeard`)
1. Descarta: próprio pacote, `TCPIP/TCPXX`, q-construct, caminho já todo usado. 🟢
2. Primeiro salto não usado: próprio indicativo ou alias → `MEUCALL*`; `WIDE1-1` (fill-in) → `MEUCALL*,WIDE1*` só se for o primeiro salto (direto) quando `directOnlyFillIn`; `WIDEn-N` até `maxN` (padrão 2), acima disso reduz (trap) ou descarta. 🟢
3. Caminho final ≤ 8 saltos; dedupe 30 s também contra pendentes. 🟢
4. Para aliases genéricos sem prioridade: raio geográfico, carga do canal (≥ 60 pacotes/min ou ≥ 60% de ar = saturado; ≥ 30/min ou ≥ 30% = ocupado, exceto fill-in direto), 1 repetição por origem a cada 60 s, limite global 20/min, atraso viscoso de 5 s + até 1 s aleatório se um vizinho já cobre a origem (cancela se ouvir o vizinho). 🟢
5. Prioridade (mensagem, ack/rej, texto "EMERGENCY", Mic-E de emergência) fura os limites, mas conta no limite global. 🟢
6. Tempo de ar estimado a 1200 bd com 40 bytes de sobrecarga por quadro. 🟢

---

## 4. Módulo `estado-e-persistencia` (`data/`)

### 4.1 `AprsRepository` (singleton `object`)
Estado compartilhado entre serviço e UI por `MutableStateFlow`: estações, enlaces RF, trilha própria, trilhas de outras estações (só sessão), eventos RF, mensagens, status, monitor (200 linhas), log de conexões (300), histórico de clima (sessão, 7 dias, 1000 por estação), log de pacotes (3000 em memória), objetos, rotas, retentativas, amostras de bateria. 🟢

Gravação em disco por **uma única thread** `aprs-station-db` (fila serializada). 🟢

`ingest(packet, medium, ownCall)` — fluxo central de recepção:
1. Conta RX; se veio do TNC e não é terceiro, grava enlaces RF (origem→digis). Se a origem sou eu, conta "meu beacon repetido" e toca o som de eco (uma vez por info a cada 60 s). 🟢
2. Objeto/item: vivo entra no mapa pelo nome; encerrado sai. 🟢
3. Clima: guarda a observação. 🟢
4. Evento RF (recebido pelo TNC, não terceiro): `heard` direto/via digi, ou `digi` para o meu pacote. 🟢
5. Posição (não comprimida ou Mic-E): atualiza a estação, `heardBy` = primeiro digi se RF, trilha da sessão. Sem posição: só atualiza horário/meio de estação já conhecida (nunca plota em 0,0). 🟢
6. Mensagem: para mim se destinatário = indicativo completo ou base; reply-ack `{MM}AA` confirma `AA`; ack/rej marca a enviada; dedupe de 1 h por (origem, ID, texto) — cópia repetida para mim volta como `duplicate` para receber ACK de novo (#101); terceiros sem ID dedupe de 10 min; boletins/CQ dedupe de 24 h. 🟢

### 4.2 Regras de mensagens (`MessageLogic.kt`)
- Conversas privadas agrupadas pelo outro indicativo, ordem pela última atividade. 🟢
- Modo "Todas" inclui conversas de terceiros (chave `A ↔ B`), só leitura. 🟢
- Retenção por categoria (privada, geral, boletim): dias e máximo juntos; mensagens pendentes nunca apagadas; padrão sem prazo e 5000 por categoria. 🟢
- ACK casa por ID (sem diferenciar maiúsculas) e destino igual ou mesmo indicativo base quando um lado está sem SSID. 🟢
- O ACK sai do endereço que o remetente usou (base ou completo). 🟢

### 4.3 Persistência
- Mensagens lidas de forma **síncrona** na inicialização (para a conversa nunca abrir vazia); migração do antigo `messages.json`; envios pendentes viram "Interrompida" ao reiniciar (#60). 🟢
- Estações/enlaces 30 dias, trilha própria configurável (padrão 7 dias), eventos RF 365 dias, log de pacotes 7 dias / 20.000 linhas (configuráveis). 🟢
- Restauração do cache em segundo plano não sobrescreve pacote recebido durante a restauração (epoch). 🟢
- Apagar estações/mensagens só remove da tela depois do commit no SQLite. 🟢

### 4.4 Configurações (`Settings.kt`)
~130 campos, cada um com padrão e faixa (`coerceIn`) na leitura e na gravação. Perfis (`custom`, `base`, `mobile`, `portable`, `rx`) com snapshot próprio dos campos de transmissão. Detalhes no `data-dictionary.md`. 🟢

### 4.5 Outros
- `SettingsBackup`: JSON versionado (`FORMAT = 1`), exclui estado de sessão e endereços Bluetooth; passcode nunca entra. 🟢
- `AprsDroidImport`: lê XML de shared_prefs ou JSON plano do APRSdroid; exige chave `callsign`; descarta valores fora de faixa. 🟢
- `Community`: rodadas (nets), padrão APRS Thursday (ANSRVR, HOTG, quinta-feira UTC); check-ins contados só com o que o app ouviu. 🟢
- `StationInsights`, `StationCriteria`, `MapClusters`, `RfStats`: ficha, filtros, agrupamento por grade de pixels (Web Mercator) e recordes de alcance. 🟢
- `CrashLog`: grava a exceção não tratada em `last_crash.txt` e oferece envio na abertura seguinte. 🟢

---

## 5. Módulo `servico-e-transportes` (`service/`)

### 5.1 `AprsService`
Serviço em primeiro plano `START_STICKY`, com ações por Intent (`reload`, `beacon`, `message`, `tracker_on/off`, `bt_connect`, `ble_connect`, `msg_resend`, `msg_cancel`, `msg_give_up_resend`, `object_send/kill`, `api_*`, `stop`…). 🟢

Threads: principal (Handler com `ticker` a cada 5 s), `aprs-tx` (todas as escritas de rede/TNC), `aprs-is` (leitor), uma por transporte TNC, `aprs-station-db`. 🟢

**Beacon**: `checkTracker` a cada fix de GPS e a cada 5 s; modo `smart` usa SmartBeaconing, `periodic` (ou posição fixa) usa intervalo (≥ 300 s com economia ativa); `manual` nunca automático. `queueBeacon` impede dois beacons na fila (`AtomicBoolean`). Curso/velocidade só se ≥ 5 km/h e sem posição fixa. 🟢

**Transmissão (`transmit`)**: RF se TX por RF e TNC conectado (caminho configurado, padrão `WIDE1-1,WIDE2-1`), **e também** APRS-IS se TX por internet e login verificado (`TCPIP*`). Somente recepção bloqueia tudo. 🟢

**Mensagem privada (`transmitPrivateMessage`)**: APRS-IS primeiro; RF só como alternativa, nunca os dois (evita duplicata com o mesmo ID). 🟢

**Retentativas (`sendWithRetry`)**: padrão 5 tentativas, intervalo base 30 s × n (30, 60, 90, 120 s…); depois da última espera mais um intervalo e marca "Sem ACK após N tentativas"; geração por mensagem permite cancelar ou reenviar (#105). 🟢

**ACK recebido**: um ACK por (remetente, ID) a cada 20 s; sai por RF se a mensagem chegou por RF, senão por APRS-IS. 🟢

**Boletim de versão (#43)**: uma vez por versão, só APRS-IS verificado, destino `BLN0`, 5 s após verificar o login. 🟢

**GPS**: alta 2 s/0 m, equilibrada 10 s/20 m, baixa 60 s/100 m (ou só rede), economia 30 s/50 m; ignora fix de rede até 30 s depois de um fix de GPS; "fix perdido" após 2 min. 🟢

**Economia de bateria (#49)**: `on` ou `auto` (≤ 20%) e **tela desligada**; amostra a cada 5 min. 🟢

**Alertas de favoritas (#12)**: primeira vez, volta após N minutos (padrão 60), mudança de posição ≥ 100 m, ouvida direto em RF; silêncio de 60 s após conectar, horário de silêncio, 10 min por chave. 🟢

### 5.2 Transportes
| Transporte | Classe | Reconexão | Conf. |
|------------|--------|-----------|-------|
| APRS-IS | `AprsIsClient` (`Connections.kt`) | Sim, 5 s dobrando até 120 s; timeout de conexão 15 s, leitura 120 s; login `user CALL pass N vers PT2VHF-APRS-Android X filter …`; filtro trocado em sessão por `#filter` | 🟢 |
| Bluetooth clássico SPP | `BluetoothTnc` | **Não** tem laço de reconexão próprio | 🟢 |
| KISS TCP | `KissTcpTnc` | Sim, 5 s dobrando até 60 s | 🟢 |
| Bluetooth LE | `BleTnc` | Sim, backoff até 60 s | 🟢 |
| USB-OTG | `UsbSerialTnc` | Por evento de USB; FTDI (0x0403), CP210x (0x10C4) ou CDC | 🟢 |
| Áudio AFSK | `AudioTnc` | Laço RX; espera até 3 s com canal ocupado antes de transmitir; PTT por VOX ou porta USB serial separada | 🟢 |

Prioridade de envio quando há mais de um: áudio, USB, BLE, TCP, BT. 🟢

### 5.3 Notificações, widget e Android Auto
- Canais por tipo: pessoal (alta), boletim, grupo, ACK, digi, alertas; som escolhido pelo usuário recria o canal com sufixo de versão. 🟢
- Widget com status e botões de beacon e tracker. 🟢
- Android Auto: lista de estações próximas, última mensagem, beacon agora; validador de host usa a lista de exemplo da biblioteca em release. 🟡

---

## 6. Módulo `atualizacao-e-site` (`data/UpdateManager.kt`, `data/UpdateCheck.kt`)
- Consulta `https://www.pt2vhf.com/update.php?product=aprs-android[&call=INDICATIVO]`, sem seguir redirecionamento, timeouts de 10 s. 🟢
- Só aceita link de APK que começa com `https://www.pt2vhf.com/`. 🟢
- Intervalos: 1 h ao voltar ao app, 12 h em segundo plano, 30 min após falha; consulta imediata quando o par indicativo|versão ainda não foi informado (#86). 🟢
- Indicativo só sem SSID, nunca posição; desligável (`share_callsign`). 🟢
- Notificação uma vez por versão; nunca instala sozinho. 🟢

## 7. Módulo `api-externa` (`service/ApiContract.kt`, `ApiReceiver.kt`)
- Permissão própria `com.pt2vhf.aprs.permission.API` com `protectionLevel="dangerous"`. 🟢
- Eventos `POSITION` (a cada pacote com posição nova) e `STATUS`; comandos `REQUEST_STATUS`, `TRACKER_START/STOP`, `SEND_BEACON`, cada classe com chave própria e só com o serviço rodando. 🟢
- API desligada por padrão. 🟢

## 8. Módulo `interface` (`ui/`)
- Seis abas: Mapa, Mensagens, Estações, Rádio, Tracker, Logs. Configurações em diálogo de tela cheia com 10 abas (Estação, APRS-IS, Beacon, Posição, Mapa, Mensagens, Notificações, iGate/Digi, Sistema, Sobre). 🟢
- Ficha da estação (`StationSheet`), decodificador (`ToolsDialog`), comunidade, modo carro, banner de abertura só na abertura a frio. 🟢
- Envio de mensagem com `request_id` e resultado lido em `AprsRepository.sendResults` (escrita no socket ≠ entrega). 🟢
- Sem testes de UI. 🟢

---

## 9. Pontos de atenção encontrados na leitura

| # | Observação | Evidência | Conf. |
|---|------------|-----------|-------|
| A1 | `nextId++` em `AprsRepository` não é atômico e é chamado do leitor APRS-IS, das threads de TNC e da `aprs-tx`; pode gerar ids repetidos em pico de tráfego | `data/AprsRepository.kt:118,621,643` | 🟡 |
| A2 | `BluetoothTnc` não reconecta sozinho quando a conexão cai (os outros transportes reconectam) | `service/Connections.kt:146` | 🟢 |
| A3 | `handleLine` é chamado em várias threads e `checkAlerts` deduz evento novo pelo tamanho da lista, o que pode atribuir o evento a outro pacote em concorrência | `service/AprsService.kt:1036,1086` | 🟡 |
| A4 | ACK enviado não conta em `aprsTx`/`tncTx` (os demais envios contam) | `service/AprsService.kt:1052-1066` | 🟢 |
| A5 | Notificação de mensagem usa `m.id.toInt()` (id baseado em milissegundos) como id da notificação | `service/AprsService.kt:1233` | 🟢 |
| A6 | Android Auto em release valida o host pela lista de exemplo da biblioteca (`hosts_allowlist_sample`) | `service/CarApp.kt:38-41` | 🟡 |
| A7 | `allowBackup="true"`: preferências (com indicativo e configurações) entram no backup do Android | `AndroidManifest.xml` | 🟢 |
| A8 | Posição comprimida não decodificada (backlog #109) | `core/Aprs.kt:384` | 🟢 |
| A9 | Release sem minificação (APK maior, código legível) | `app/build.gradle.kts` | 🟢 |
| A10 | Ao reiniciar o serviço, as retentativas agendadas morrem e a mensagem vira "Interrompida" (comportamento desejado, #60) | `data/AprsRepository.kt:685` | 🟢 |
