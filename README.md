# PT2VHF APRS Client for Android

Cliente APRS para Android (8.0 ou superior) do projeto PT2VHF: mapa com estações e enlaces RF,
mensagens, beacon, modo tracker com SmartBeaconing em segundo plano e TNC KISS por Bluetooth.

**Download oficial:** https://www.pt2vhf.com/android

## Recursos da versão 0.1

- APRS-IS com servidor à escolha (padrão `soam.aprs2.net:14580`, lista de servidores ou "Outro…"),
  passcode calculado automaticamente a partir do indicativo e filtro editável (padrão `r/4000`).
- Reconexão automática ao trocar entre Wi-Fi e dados móveis.
- Mapa (OpenStreetMap) com estações e enlaces RF comprovados; internet e redes digitais (DMR, D-STAR…)
  não contam como RF.
- Mensagens: conversas, canal geral (CQ) e boletins; ACK, novas tentativas e notificações.
- Botão **Enviar beacon** e **modo tracker** (SmartBeaconing) com notificação fixa.
- TNC KISS por Bluetooth (Mobilinkd, BTECH UV-Pro, Radtel RT-950 Pro e similares) com monitor ao vivo.
- Aviso de nova versão e "Downloads no site oficial", pelo site www.pt2vhf.com.

## Desenvolvimento

```bash
./gradlew testDebugUnitTest assembleDebug
```

O APK de release é assinado no GitHub Actions com os segredos `ANDROID_KEYSTORE_BASE64`,
`ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` e `ANDROID_KEY_PASSWORD`. Commits na `main` cujo título
começa com `release:` geram o APK assinado.

## Publicação de novas versões e site oficial

Toda nova versão do Android deve ser acompanhada da atualização do repositório
[`PT2VHF-Site`](https://github.com/alexpmr/PT2VHF-Site): APK assinado em
`downloads/aprs-android/`, metadados do produto `aprs-android` em `updates.json`,
página `/android`, changelog, recursos/requisitos relevantes, tabela de downloads
e endpoint OTA `update.php?product=aprs-android`.

O identificador de contador de downloads do APK é `android-apk` e **não muda**
com a versão: os downloads manuais e OTA compõem uma contagem acumulada,
sem duplicação. Verifique o arquivo público e a implantação Hostinger antes de
considerar a publicação concluída. Uma release no GitHub isoladamente **não**
constitui uma publicação completa no site.

## Internacionalização (preparada para a próxima versão)

O aplicativo utiliza recursos nativos do Android para os idiomas **português,
inglês, espanhol, francês e italiano**. Na inicialização, o idioma é escolhido
automaticamente pelas preferências de idioma do dispositivo. Idiomas não
suportados usam **inglês**, definido em `res/values/strings.xml`, como padrão.

As traduções estão em `res/values-pt/`, `values-es/`, `values-fr/` e
`values-it/`. Em Android 13 ou superior, os idiomas são anunciados ao sistema
por `res/xml/locales_config.xml`, permitindo também a seleção no menu de
idioma por aplicativo do próprio Android. O conteúdo dos pacotes APRS,
indicativos, endereços de servidores e constantes do protocolo não é traduzido.

**Atenção:** este suporte faz parte das alterações em desenvolvimento.
O APK **v0.1.0** já publicado continua com a interface original até que uma
nova release seja solicitada, compilada e publicada também no site oficial.
