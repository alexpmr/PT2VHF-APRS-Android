# PT2VHF APRS Client for Android

Cliente APRS para Android (8.0 ou superior) do projeto PT2VHF: mapa com estações e enlaces RF,
mensagens, beacon, modo tracker com SmartBeaconing em segundo plano e TNC KISS por Bluetooth, TCP, USB-OTG
ou pelo próprio áudio do celular (modem AFSK 1200).

**Download oficial:** https://www.pt2vhf.com/android

## Recursos da versão 1.1

- APRS-IS com servidor à escolha (padrão `soam.aprs2.net:14580`, lista de servidores ou "Outro…"),
  passcode calculado automaticamente a partir do indicativo e filtro editável (padrão `r/4000`).
- Reconexão automática ao trocar entre Wi-Fi e dados móveis.
- Mapa (OpenStreetMap) com estações e enlaces RF comprovados; internet e redes digitais (DMR, D-STAR…)
  não contam como RF.
- Mensagens: conversas, canal geral (CQ) e boletins; ACK, novas tentativas e notificações.
- Botão **Enviar beacon** e **modo tracker** (SmartBeaconing) com notificação fixa.
- TNC KISS por Bluetooth (Mobilinkd, BTECH UV-Pro, Radtel RT-950 Pro, ICOM ID-5200 e similares) com monitor ao vivo.
- TNC KISS por **Bluetooth LE** (serviço KISS BLE ou Nordic UART) e, no modo AFSK, **PTT por RTS/DTR ou CAT** por
  adaptador USB-serial.
- Aviso de nova versão pelo site www.pt2vhf.com (o aviso de "Downloads no site oficial" na abertura saiu na 0.1.8).

## Novidades da 1.1

- **TNC KISS por Bluetooth LE**: Mobilinkd TNC4, PicoAPRS, trackers LoRa APRS e outros com serviço KISS BLE ou
  Nordic UART, sem parear (não testado em TNC BLE real).
- **PTT por RTS/DTR ou CAT** no modo AFSK, por adaptador USB-serial (Digirig, FTDI, CP210x): Kenwood/Elecraft,
  Yaesu e Icom CI-V (não testado com rádio real).
- Ficha da estação mostra o **rádio/aplicativo** usado (tabela aprs-deviceid), status Mic-E, PHG decodificado,
  frequência anunciada, status e caminho do último pacote. Estações **Mic-E** (Kenwood, Yaesu…) aparecem no mapa.
- E-mails e sites no comentário da estação viram **links**.
- **Sons** ao receber confirmação (ack) de mensagem e, pelo TNC, ao ouvir meu pacote repetido por um digipeater.
- Indicativo vai para a lista do site logo após instalar/atualizar ou trocar o indicativo; PT2VHF também entra.
- PP5PK entra nos colaboradores (PU2PYC, PP5AU, PU2MUS, PP5PK).

Fonte da identificação de aparelhos: [aprs-deviceid](https://github.com/aprsorg/aprs-deviceid) (Hessu OH7LZB,
CC BY-SA 2.0).

## Novidades da 1.0

- Configurações com **Salvar sempre visível** (também no cabeçalho) no Android 15/16, tablets e navegação por 3 botões.
- SSID não volta mais sozinho para -9/-7; ao sair com alterações o app pergunta se quer salvar.
- Janela **"Qual SSID usar?"** com as recomendações de WB4APR (ⓘ ao lado do campo SSID).
- Nova aba **Sobre** com versão, site e colaboradores (PU2PYC, PP5AU, PU2MUS).
- Mensagens saem **sem acentos** por padrão (opção em Configurações → Mensagens).
- **Android Auto**: estações próximas, Beacon agora e última mensagem no painel do carro (modo desenvolvedor do
  Android Auto para APK fora da Play Store).
- Indicativo informado na consulta de versão nova (lista de estações no site; pode desligar).
- ICOM ID-5200 na lista de rádios que funcionam.

## Novidades da 0.1.15

- Banner de abertura com a arte do APRS e www.PT2VHF.com por cerca de 2 s (toque pula).
- Configurações reorganizadas em 7 abas: Estação, APRS-IS, Beacon, Posição, Mapa, Mensagens e Sistema.
- Mensagens: botões **Minhas / Todas** (inclui mensagens entre outras estações, só leitura) e **Agrupadas / Lista**.
- Campos de busca sem cortar o texto e textos que faltavam traduzidos nos 5 idiomas.

## Novidades da 0.1.14

- Tela de Mensagens compacta e legível com o teclado aberto, Enviar em ícone e atualização na hora de mensagens novas.
- Só indicativos de radioamador válidos no mundo (tabela de prefixos da ITU), com o país do prefixo.
- Ficha da estação com abas **Trilha** e **Mensagens**; filtros por região, SSID, símbolo e visto por último.
- Nova aba **Comunidade** com rodadas de check-in (ex.: APRS Thursday), participantes e países.
- Mapa com agrupamento de estações próximas e camada de clima; decodificador de pacote e tabela de frequências APRS.
- Importação das configurações do APRSdroid.

## Novidades da 0.1.13

Primeira versão pública depois da 0.1.9 (reúne as candidatas QA 0.1.10 a 0.1.12):

- Mensagens: APRS-IS verificado tem prioridade para mensagens privadas com fallback RF, sem TX duplicado; envio
  interrompido vira "Interrompida" com Reenviar, em vez de ficar pendente para sempre; ACK/REJ e reply-ack corrigidos.
- Exclusão segura de mensagens e estações (lixeira em Estações), com o banco gravado antes da tela.
- Observações meteorológicas (WX) e validação básica do indicativo do operador.
- **Ficha da estação em tela cheia** com Visão geral (recepção nas últimas 24 h, iGates, último salto, outros SSIDs),
  **Caminho** do pacote salto a salto, **Telemetria e clima** com gráficos (T# com PARM/UNIT/EQNS/BITS) e pacotes brutos.
- **Ordenação em um toque** da lista de estações por Tipo, Nome, Distância ou Recentes; tocar de novo inverte.
- **Ícones do mapa maiores e nítidos**, com tamanho Pequeno/Médio/Grande em Configurações.

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
