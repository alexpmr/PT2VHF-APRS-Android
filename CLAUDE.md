# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

App Android (Kotlin + Jetpack Compose, osmdroid) do PT2VHF APRS Client. Textos, commits e documentação em português (BR).

## Comandos

```bash
bash scripts/fetch-aprs-symbols.sh        # baixa os sprites de símbolos APRS (não versionados) para app/src/main/assets/aprs/
./gradlew testDebugUnitTest assembleDebug # obrigatório antes de subir (é o mesmo passo do CI)
./gradlew testDebugUnitTest --tests 'com.pt2vhf.aprs.core.AprsTest'            # uma classe de teste
./gradlew testDebugUnitTest --tests 'com.pt2vhf.aprs.core.AprsTest.nomeDoTeste' # um teste
```

- JDK 17, compileSdk/targetSdk 35, minSdk 26. Sem lint/ktlint configurado.
- Só há testes de unidade JVM (`app/src/test`, JUnit 4, `unitTests.isReturnDefaultValues = true`); não há testes
  instrumentados. AFSK no ar, USB-OTG, widget e bateria só se validam no aparelho (roteiros em `HOMOLOGACAO.md`).
- Sem os sprites o app compila, mas o mapa fica sem ícones de símbolo (`AprsSymbolIcons` devolve `null`).
- Versão do app: `appVersionName`/`appVersionCode` no topo de `app/build.gradle.kts` (o CI lê `appVersionName` por regex).

## Arquitetura

Módulo único `:app`, pacote `com.pt2vhf.aprs`, sem DI nem ViewModel:

- **`core/`** — protocolo puro, sem Android, coberto por testes: parser/encoder APRS (`Aprs.kt`, `AprsExtras.kt`,
  `AprsText.kt` com acentos UTF-8/CP1252), framing KISS/AX.25 (`Kiss.kt`) e modem AFSK 1200 (`Afsk.kt`).
- **`service/AprsService`** — foreground service que concentra todo o I/O: APRS-IS (`AprsIsClient`), TNCs
  (`BluetoothTnc`, `KissTcpTnc` em `Connections.kt`, `UsbTnc`, `AudioTnc`), GPS/SmartBeaconing, beacons, fila e
  reenvio de mensagens/ACK, notificações. A UI e o widget falam com ele só por intents:
  `AprsService.send(context, ACTION_*, extras)` (constantes no `companion object`). Pacotes recebidos passam por
  `AprsRepository.ingest(packet, medium, ...)`, onde `Medium` é `RF` ou `INTERNET` (só RF conta para enlaces/cobertura).
- **`data/AprsRepository`** — singleton (`object`) que guarda o estado do app em `StateFlow`s (estações, enlaces RF,
  mensagens, status, logs...) e persiste em SQLite (`StationStore`, `MessageStore`, `PacketLogStore`) e
  SharedPreferences. `Settings` é uma data class carregada/salva em SharedPreferences (`Settings.load`).
  Regras testáveis ficam em arquivos separados (`MessageLogic`, `StationCriteria`, `RfStats`, `StationFormatting`).
- **`ui/`** — Compose observando os `StateFlow`s do repositório; `App.kt` tem as abas (`Tab`), `Screens.kt` o mapa
  osmdroid. Arquivos por versão (`Features019.kt`) concentram telas daquela entrega.
- **API para outros apps:** `ApiContract`/`ApiReceiver` (broadcasts), documentada em `API.md`.
- **Strings:** padrão em inglês em `res/values/`, traduções em `values-pt|es|fr|it`. Textos novos de cada versão
  vão em `strings_vXXX.xml`, e toda string nova precisa existir nos cinco idiomas. Acesso fora de Compose via
  `UiText.text(R.string...)`. Pacotes APRS, indicativos e constantes de protocolo não são traduzidos.
- Comentários `#NN` no código referem-se a itens do `BACKLOG.md`.

## CI (`.github/workflows/android.yml`)

- Todo PR/push: testes + APK debug. PR de branch do próprio repositório também gera um APK QA assinado
  (artefato `android-signed-qa`) para teste físico, sem release.
- Push na `main` com título `release:` (ou execução manual) gera o APK release assinado e a release `vX.Y.Z` no GitHub.
- Os workflows `*-aprs-013.yml` são utilitários antigos da exportação da v0.1.3 para o site.

## Regras do mantenedor

- Estado do projeto e próximos passos: ver `BACKLOG.md` (seção "Onde estamos").
- Mensagens do usuário que começam com "Backlog:" são itens a registrar em `BACKLOG.md`, numerados, sem implementar.
  Implementar só quando ele pedir versão nova.
- Versão nova: implementar itens do backlog, atualizar `CHANGELOG.md`, `README.md` e a versão do app; um commit na
  `main` cujo título começa com `release:` gera o APK assinado no GitHub Actions. Não usar o prefixo `release:`
  em commits que não sejam de versão.
- Nunca dar baixa em item só porque compilou: cada item aguarda teste físico e OK do mantenedor (`HOMOLOGACAO.md`).

- **Regra permanente do mantenedor para toda versão nova:** a entrega do Android inclui
  **obrigatoriamente** a atualização e validação do site oficial no repositório
  `alexpmr/PT2VHF-Site` (página `/android`, `updates.json` produto `aprs-android`,
  tabela de downloads, versão/data, changelog das cinco últimas versões, requisitos
  e recursos alterados). Copiar para `downloads/aprs-android/` somente o APK
  release assinado e verificado; confirmar tamanho/hash e disponibilidade pública.
  Manter a chave de contador `android-apk` entre versões para preservar o
  acumulado, incluindo downloads manuais e OTA, sem contagem dupla. Verificar
  `update.php?product=aprs-android` e implantação da Hostinger. Não marcar
  release como concluída se o site público ainda não estiver validado; relatar
  qualquer falha de publicação. Esta regra não autoriza criar releases sem pedido.

- **Todo novo item de backlog deve ser registrado imediatamente no GitHub:** atualizar `BACKLOG.md` na branch principal `main` e realizar commit de documentação no mesmo atendimento (`docs(backlog):`, nunca `release:`). Não basta mencionar a pendência na conversa. Confirmar o commit/link; não implementar o item nem gerar versão até pedido explícito. Se o commit falhar, informar claramente que o registro não foi concluído.
