# PT2VHF APRS Client for Android

Cliente APRS para Android (8.0 ou superior) do projeto PT2VHF: mapa com estações e enlaces RF,
mensagens, beacon, modo tracker com SmartBeaconing em segundo plano e TNC KISS por Bluetooth, TCP, USB-OTG
ou pelo próprio áudio do celular (modem AFSK 1200).

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
- Aviso de nova versão pelo site www.pt2vhf.com (o aviso de "Downloads no site oficial" na abertura saiu na 0.1.8).

## Candidata 0.1.10 (QA — ainda não publicada)

Aprimora tratamento de mensagens RF/APRS-IS, pacotes de terceiros e reply-ack, corrige bloqueio visual
do compositor, oferece lixeira de Estações, introduz visualização WX da sessão e validação básica de
formato do indicativo do operador. Requer testes de campo e a publicação coordenada do site.
A versão pública continua 0.1.9 até homologação e liberação explícita.

## Novidades da 0.1.9

- **Modem AFSK 1200 pelo áudio** do celular (o celular vira o TNC; PTT por VOX ou interface) e **TNC KISS por USB-OTG**
  (CDC-ACM, FTDI, CP210x).
- Aba **Logs** com todos os pacotes, filtros, detalhes e exportação TXT/CSV.
- Objetos e itens APRS criados pelo mapa; posição fixa; rotas GPX com distância restante; **modo carro**.
- Aviso de nova versão com notificação e "Ignorar esta versão"; boletim de versão só via APRS-IS.
- Widget na tela inicial, economia de bateria, backup das configurações, enviar log, reenvio de mensagens visível
  e configurável, apagar mensagens por tipo, data e tempo decorrido nas mensagens.

## Novidades da 0.1.8

- Perfis Base, Móvel, Portátil e **Somente recepção** (nenhum beacon, mensagem ou ACK sai).
- Mensagens em SQLite com retenção configurável; pressão longa para responder ou mandar mensagem direta;
  busca de indicativo para nova conversa; filtros combináveis; acentos UTF-8/CP1252 exibidos corretamente.
- Mapa: brilho ajustável, indicativo ao lado do símbolo, meus enlaces RF, cobertura RF do meu TNC e trilha
  da estação seguida.
- Rádio: KISS por TCP (Direwolf etc.), meus enlaces RF, alcance e recordes RF, diagnóstico exportável.
- Tracklog com cor, espessura, opacidade, retenção e exportação GPX.
- Beacon com ambiguidade de posição, frequência de voz, altitude e corner pegging editável; seletor visual
  de símbolo; alertas de estações favoritas; API opcional para outros apps ([API.md](API.md)).

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
