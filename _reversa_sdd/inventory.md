# Inventário do Projeto — PT2VHF-APRS-Android

> Gerado pelo Scout (Reversa 1.3.6, modo autônomo, nível Completo) em 2026-11-27.
> Base analisada: branch `main`, commit `79e6b39` (versão do app 1.4, versionCode 20).
> Escala: 🟢 CONFIRMADO · 🟡 INFERIDO · 🔴 LACUNA

## 1. Visão geral

| Item | Valor | Confiança |
|------|-------|-----------|
| Produto | PT2VHF APRS Client para Android (cliente APRS para radioamadores) | 🟢 |
| Pacote / applicationId | `com.pt2vhf.aprs` | 🟢 |
| Versão | `1.4` / versionCode `20` (`app/build.gradle.kts`) | 🟢 |
| Linguagem principal | Kotlin 2.0.21 | 🟢 |
| UI | Jetpack Compose (BOM 2024.12.01) + Material 3 | 🟢 |
| Mapa | osmdroid 6.1.20 (tiles OpenStreetMap / OpenTopoMap) | 🟢 |
| SDK | minSdk 26, targetSdk 35, compileSdk 35, Java 17 | 🟢 |
| Build | Gradle (wrapper), Android Gradle Plugin 8.7.3, módulo único `:app` | 🟢 |
| Persistência | SQLite (`SQLiteOpenHelper`), SharedPreferences, arquivos JSON em `filesDir` | 🟢 |
| Idiomas da UI | Inglês (padrão), português, espanhol, francês e italiano (`values-*`) | 🟢 |
| App irmão | App iOS nativo (SwiftUI) na pasta `ios/`, fora do escopo desta análise | 🟢 |

## 2. Estrutura de pastas (sem build, .git e pastas do Reversa)

```
PT2VHF-APRS-Android/
├── .github/workflows/          CI: android.yml (build, QA assinado, release), ios.yml e 4 workflows de exportação
├── app/
│   ├── build.gradle.kts        versão do app, assinatura por variáveis de ambiente, dependências
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/pt2vhf/aprs/
│       │   │   ├── MainActivity.kt, UiText.kt, UiTextKeys.kt    (entrada e textos)
│       │   │   ├── core/      17 arquivos — protocolo APRS puro (sem Android)
│       │   │   ├── data/      23 arquivos — estado, regras, persistência
│       │   │   ├── service/   10 arquivos — serviço em primeiro plano e transportes
│       │   │   └── ui/        17 arquivos — telas Compose
│       │   └── res/           strings em 5 idiomas, temas, widget, Android Auto, ícones
│       └── test/java/com/pt2vhf/aprs/   27 arquivos de teste JVM (JUnit 4), 182 casos @Test
├── ios/                        app iOS (15 arquivos Swift, ~2.000 linhas) + project.yml (XcodeGen)
├── scripts/                    fetch-aprs-symbols.sh (sprites), gen-deviceid.py (tabela de aparelhos)
├── docs/inspiracao/, docs/relatos/   imagens de referência e relatos de usuários
├── API.md, BACKLOG.md, CHANGELOG.md, HOMOLOGACAO.md, README.md, THIRD_PARTY_APRS_SYMBOLS.md, CLAUDE.md
└── build.gradle.kts, settings.gradle.kts, gradle.properties, gradlew*
```

## 3. Contagem por linguagem (arquivos versionados)

| Linguagem | Extensões | Arquivos | Linhas (aprox.) |
|-----------|-----------|----------|-----------------|
| Kotlin | `.kt` | 97 (70 main + 27 test) | 17.484 |
| XML (recursos/manifest) | `.xml` | 84 | — |
| Swift (iOS, fora do escopo) | `.swift` | 15 | 1.994 |
| PNG | `.png` | 25 | — |
| Markdown | `.md` | 8 | — |
| YAML | `.yml` | 7 | — |
| Gradle Kotlin DSL | `.kts` | 3 | — |

## 4. Módulos identificados

| Módulo lógico | Pasta | Arquivos principais | Propósito |
|---------------|-------|---------------------|-----------|
| `protocolo-aprs` | `core/` | `Aprs.kt`, `Kiss.kt`, `AprsDevice.kt`, `AprsExtras.kt`, `AprsPath.kt`, `AprsText.kt`, `AprsTelemetry.kt`, `AprsWeather.kt`, `PacketDecoder.kt`, `CallsignPrefixes.kt`, `DeviceIdTable.kt`, `AsciiFold.kt` | Parser/encoder APRS, KISS/AX.25, Mic-E, telemetria, clima, objetos, validação de indicativo (ITU), SmartBeaconing |
| `modem-e-radio` | `core/` | `Afsk.kt`, `RadioControl.kt` | Modem AFSK 1200 bd próprio, HDLC, KISS-BLE, PTT RTS/DTR/CAT, USB serial |
| `gateway-igate-digi` | `core/` + `service/` | `Igate.kt`, `SmartDigipeater.kt`, parte de `AprsService.kt` | iGate RF↔APRS-IS e digipeater inteligente |
| `estado-e-persistencia` | `data/` | `AprsRepository.kt`, `Models.kt`, `Settings.kt`, `MessageLogic.kt`, `StationStore.kt`, `MessageStore.kt`, `PacketLogStore.kt`, `SettingsBackup.kt` | Estado em memória (StateFlow), regras de mensagens, retenção, SQLite e preferências |
| `servico-e-transportes` | `service/` | `AprsService.kt`, `Connections.kt`, `BleTnc.kt`, `UsbTnc.kt`, `AudioTnc.kt`, `NotificationTypes.kt`, `StatusWidget.kt`, `CarApp.kt` | Serviço em primeiro plano, APRS-IS, TNC (BT, BLE, TCP, USB, áudio), notificações, widget, Android Auto |
| `atualizacao-e-site` | `data/` | `UpdateManager.kt`, `UpdateCheck.kt` | Consulta de versão ao site oficial e registro do indicativo |
| `api-externa` | `service/` | `ApiContract.kt`, `ApiReceiver.kt`, `API.md` | Intents públicos para outros apps (permissão perigosa própria) |
| `interface` | `ui/` | `App.kt`, `Screens.kt`, `MessagesUi.kt`, `SettingsUi.kt`, `StationDetailUi.kt`, `RadioUi.kt`, `LogsUi.kt`, `CommunityUi.kt`, `Features019.kt`, `ToolsUi.kt` | 6 abas (Mapa, Mensagens, Estações, Rádio, Tracker, Logs), Configurações em 10 abas, ficha da estação, modo carro |

## 5. Pontos de entrada

| Caminho | Tipo | Observação |
|---------|------|------------|
| `app/src/main/java/com/pt2vhf/aprs/UiText.kt` (`Pt2vhfApplication`) | Application | Inicializa textos e o registro de travamentos 🟢 |
| `app/src/main/java/com/pt2vhf/aprs/MainActivity.kt` | Activity LAUNCHER | Pede permissões, inicia o serviço, abre `App()` 🟢 |
| `service/AprsService.kt` | Foreground Service | Tipos `location|connectedDevice|specialUse|microphone` 🟢 |
| `service/ApiReceiver.kt` | BroadcastReceiver exportado | Protegido por `com.pt2vhf.aprs.permission.API` 🟢 |
| `service/CarApp.kt` (`AprsCarAppService`) | CarAppService (Android Auto, categoria POI) | 🟢 |
| `service/StatusWidget.kt` | AppWidgetProvider | Widget de status 🟢 |

## 6. Configuração

| Arquivo | Conteúdo |
|---------|----------|
| `app/build.gradle.kts` | Versão única (`appVersionName`/`appVersionCode`), assinatura release por variáveis `ANDROID_KEYSTORE_*`, `isMinifyEnabled = false` 🟢 |
| `gradle.properties` | `-Xmx3g`, AndroidX, `nonTransitiveRClass` 🟢 |
| `res/xml/locales_config.xml` | Idiomas por app 🟢 |
| `res/xml/automotive_app_desc.xml` | Android Auto 🟢 |
| `res/xml/widget_status_info.xml` | Widget 🟢 |
| Sem `.env`; segredos só no GitHub Actions | 🟢 |

## 7. CI/CD

| Workflow | Gatilho | Função |
|----------|---------|--------|
| `.github/workflows/android.yml` | PR, push na `main`, manual | `testDebugUnitTest assembleDebug`; job `qa-signed` (APK assinado de QA só em PR do próprio repo); job `release` quando o commit começa com `release:` 🟢 |
| `.github/workflows/ios.yml` | — | Build do app iOS 🟢 |
| `consolidate-aprs-013.yml`, `export-aprs-0110-transport.yml`, `export-aprs-0111-transport.yml`, `export-aprs-android-013.yml` | — | Exportações pontuais de versões antigas 🟡 (parecem históricas) |
| `scripts/fetch-aprs-symbols.sh` | Chamado no CI | Baixa e verifica os sprites de símbolos APRS 🟢 |
| Sem Dockerfile | — | n/a |

## 8. Banco de dados (superficial)

| Arquivo | Banco | Versão do schema | Tabelas |
|---------|-------|------------------|---------|
| `data/StationStore.kt` | `stations.db` | 3 | `stations`, `rf_links`, `own_track`, `rf_events` 🟢 |
| `data/MessageStore.kt` | `messages.db` | 1 | `messages` 🟢 |
| `data/PacketLogStore.kt` | `packets.db` | 2 | `packets` 🟢 |
| SharedPreferences `pt2vhf_aprs` | — | — | ~130 chaves de configuração, favoritos, objetos (`my_objects` em JSON), snapshots de perfil 🟢 |
| `filesDir/routes.json` | — | — | Rotas GPX importadas 🟢 |

## 9. Testes

| Item | Valor |
|------|-------|
| Framework | JUnit 4.13.2, testes JVM (`testOptions.unitTests.isReturnDefaultValues = true`) 🟢 |
| Arquivos | 14 em `core/`, 12 em `data/`, 1 em `service/` 🟢 |
| Casos | 182 métodos `@Test` 🟢 |
| Cobertura | Lógica pura bem coberta (parser, Mic-E, iGate, digi, AFSK, mensagens, retenção, filtros); serviço Android, transportes reais e UI sem teste automatizado 🟡 |
| Teste instrumentado (androidTest) | Ausente 🟢 |

## 10. Observações do Scout

- O código mantém a lógica de protocolo em Kotlin puro (pacote `core/` e boa parte de `data/`), o que permite testes na JVM. 🟢
- Os valores internos gravados (situação de mensagem, tipo de estação, erros) estão em português de propósito e centralizados em `data/InternalText.kt`. 🟢
- A `main` analisada é a versão do site (APK com aviso de atualização). A preparação para o Google Play (PR #31) não está na `main`. 🟡
- `parsePosition` só decodifica posição não comprimida; posição comprimida (LoRa APRS) está no backlog #109 com correção no PR #33, ainda fora da `main`. 🟢
