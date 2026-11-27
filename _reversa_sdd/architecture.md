# Arquitetura — PT2VHF-APRS-Android

> Gerado pelo Arquiteto (Reversa) em 2026-11-27. Base `main` @ `79e6b39` (v1.4).

## 1. Estilo arquitetural
App Android de módulo único (`:app`) organizado em quatro camadas por pacote: 🟢

| Camada | Pacote | Depende de | Regra |
|--------|--------|------------|-------|
| Protocolo | `core/` | nada (Kotlin puro) | sem imports Android; testável na JVM |
| Estado e dados | `data/` | `core/` | `AprsRepository` (singleton) concentra o estado em `StateFlow`; SQLite e prefs |
| Serviço | `service/` | `core/`, `data/` | `AprsService` em primeiro plano é o único que fala com rede, TNC e GPS |
| Interface | `ui/` | `data/`, `core/` | Compose lê `StateFlow` e manda ações ao serviço por Intent |

Padrão de comunicação: **UI → Intent → Serviço → Repositório (StateFlow) → UI**. Não há ViewModel nem injeção de dependência; o repositório é um `object` global. 🟢

## 2. Concorrência 🟢
| Thread | Papel |
|--------|-------|
| principal | Handler do serviço, `ticker` de 5 s, Compose |
| `aprs-tx` | todas as escritas em rede e TNC (fila única) |
| `aprs-is` | leitura do socket do APRS-IS |
| uma por TNC | leitura BT/BLE/TCP/USB/áudio |
| `aprs-station-db` | todas as gravações em SQLite |

Pontos frágeis: `nextId++` não atômico (A1) e `checkAlerts` baseado no tamanho da lista (A3).

## 3. Integrações externas
| Sistema | Protocolo | Direção | Falha tratada | Conf. |
|---------|-----------|---------|---------------|-------|
| Servidores APRS-IS | TCP texto, porta 14580 | duas vias | reconexão 5→120 s | 🟢 |
| TNC Bluetooth SPP | RFCOMM + KISS | duas vias | sem reconexão automática | 🟢 |
| TNC BLE | GATT KISS / Nordic UART | duas vias | backoff até 60 s | 🟢 |
| TNC KISS TCP | TCP + KISS | duas vias | backoff até 60 s | 🟢 |
| TNC USB / PTT | USB host (FTDI, CP210x, CDC) | duas vias | evento de USB | 🟢 |
| Rádio por áudio | AFSK 1200 bd + VOX/RTS/DTR/CAT | duas vias | espera canal livre 3 s | 🟢 |
| Site `www.pt2vhf.com/update.php` | HTTPS GET | saída | nova tentativa em 30 min | 🟢 |
| Tiles OSM (MAPNIK; escuro e cinza por filtro de cor) e OpenTopoMap | HTTPS (osmdroid) | entrada | cache em disco | 🟢 |
| Android Auto | Car App Library | saída | — | 🟢 |
| Outros apps | Broadcast Intents com permissão | duas vias | só com serviço ativo | 🟢 |
| Sistema (GPS/rede) | `LocationManager` | entrada | "fix perdido" após 2 min | 🟢 |

## 4. Build e entrega 🟢
- Gradle wrapper, AGP 8.7.3, Java 17, sem minificação.
- CI: `testDebugUnitTest assembleDebug` em todo PR; APK assinado de QA em PR do próprio repo; release quando o commit começa com `release:`.
- Distribuição: APK no site oficial (com aviso de atualização) e, a partir de 2026-11, Google Play em teste fechado (PR #31, fora da `main`).

## 5. Dívida técnica
| # | Item | Impacto | Sugestão |
|---|------|---------|----------|
| DT1 | `AprsService.kt` e `AprsRepository.kt` muito grandes (vários papéis no mesmo arquivo) | manutenção e revisão difíceis | extrair beacon, mensagens, iGate/digi em classes próprias |
| DT2 | Estado global em `object` sem ViewModel | testes de UI e de serviço difíceis | expor interfaces e injetar |
| DT3 | Sem testes instrumentados nem de UI | regressões só aparecem no aparelho | ao menos testes de fumaça do serviço |
| DT4 | Contador de id não atômico (A1) | ids repetidos sob carga | `AtomicLong` |
| DT5 | BT clássico sem reconexão (A2) | usuário precisa reconectar à mão | reaproveitar o backoff dos outros transportes |
| DT6 | Posição comprimida ausente (A8, #109) | estações LoRa fora do mapa | integrar PR #33 |
| DT7 | `allowBackup=true` (A7) | dados pessoais no backup do Google | regras de backup ou desligar |
| DT8 | Release sem R8 (A9) | APK ~10 MB | avaliar minificação com regras de keep |
| DT9 | Validador de host do Android Auto de exemplo (A6) | rejeição na loja / segurança | lista de hosts própria |
| DT10 | Workflows de exportação antigos no `.github/` | ruído no CI | remover se não usados |
