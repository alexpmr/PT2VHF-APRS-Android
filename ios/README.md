# PT2VHF APRS — iOS

App nativo para iPhone/iPad (SwiftUI, iOS 17+), irmão do app Android. Mesmo protocolo, mesmo TOCALL (`APZVHF`)
e passcode APRS-IS calculado do indicativo.

## O que tem nesta versão (1.4)

Mesma numeração do Android: a primeira versão para a App Store sai como **1.4**, com os recursos do Android 1.4
que o iPhone permite.

- Mapa (Apple Mapas) com as estações; tipo de mapa **Padrão, Escuro, Satélite, Híbrido e Automático**.
- APRS-IS com reconexão automática e filtro pelo raio em volta da sua posição (atualizado ao se deslocar).
- Lista de estações com busca, ordem por recentes/próximas, ficha com distância, rumo, caminho e pacote.
- Mensagens: conversas, ack, até 5 reenvios (30 s, 60 s, 120 s, 240 s), boletins à parte, notificação.
- Beacon por GPS: "Beacon agora", rastreador por intervalo e em curvas; em segundo plano com permissão "Sempre".
- TNC KISS por **Bluetooth LE** (serviço KISS BLE e Nordic UART, os mesmos do Android #92).
- Pacotes recebidos/enviados, "Qual SSID usar?", remover acentos ao enviar.
- ACK como no Android 1.4 (#101): toda cópia repetida é confirmada (no máximo 1 ACK a cada 20 s), pelo mesmo meio
  por onde chegou e do endereço que o remetente usou; ACK de `PT2ATC-7` confirma mensagem para `PT2ATC`;
  `ack12 `, `ack12}` e `ack12}AB` são reconhecidos.
- "Desistir do ACK e reenviar" (#105) ao manter pressionada a mensagem enviada.
- Notificações por tipo (#106): pessoais, boletins, gerais (CQ/ALL/QST) e ACK; boletim repetido em 24 h não avisa.
- **Só ouvir sem indicativo**: sem indicativo válido o app entra no APRS-IS como `N0CALL` (passcode -1), mostra
  estações e mensagens e não transmite nada. É o que permite a revisão da Apple usar o app sem licença.
- **Bloquear indicativo** (exigência da App Store para apps com mensagens): deslizar a conversa ou manter
  pressionada a mensagem; a lista fica em Ajustes → Bloqueados.
- **Idiomas:** português, inglês, espanhol, francês e italiano (os mesmos do Android), em
  `Localizable.xcstrings` e `InfoPlist.xcstrings` (textos das permissões). O app segue o idioma do iPhone.
- Manifesto de privacidade (`PrivacyInfo.xcprivacy`) com as mesmas respostas do Google Play.

## Limitações do iOS

- **Bluetooth clássico (SPP) não existe para apps no iPhone**: TNCs só por BLE.
- Sem USB-OTG nem modem AFSK pelo áudio nesta versão.
- O iPhone não instala app baixado de site (como o APK). A distribuição é pelo **TestFlight** (beta) ou App Store.

## Compilar

```sh
brew install xcodegen
cd ios && xcodegen generate && open PT2VHFAPRS.xcodeproj
```

O GitHub Actions (`.github/workflows/ios.yml`) gera o projeto, roda os testes no simulador e compila para
aparelho sem assinatura em todo PR que mexe em `ios/`.

**Capturas de tela da App Store:** Actions → **iOS build** → *Run workflow* com "Gerar as capturas de tela"
marcado. O app abre no simulador em modo demonstração (`-demo`, estações e mensagens fictícias, sem conectar) e o
artefato `app-store-telas` (e a pasta `docs/app-store/telas/` da branch) traz 6 telas do iPhone 6,9" e 6 do iPad 13", já no tamanho pedido pela Apple.
Não precisa de Mac nem de conta Apple.

## Publicar no TestFlight (uma vez só, pelo mantenedor)

1. Inscrever-se no **Apple Developer Program** (US$ 99/ano) em developer.apple.com/programs.
2. Em developer.apple.com → Certificates, IDs & Profiles → **Identifiers → +** registrar o App ID
   `com.pt2vhf.aprs` (sem capacidades extras). Depois, no App Store Connect: **Apps → +** novo app iOS, nome
   "PT2VHF APRS Client", idioma principal Português (Brasil), bundle ID `com.pt2vhf.aprs`, SKU `pt2vhf-aprs`.
3. **Usuários e Acesso → Integrações → Chaves da API do App Store Connect**: criar chave com papel *Admin* (a assinatura
   na nuvem, que cria o certificado de distribuição sem Mac, exige Admin),
   baixar o `.p8` e anotar *Key ID* e *Issuer ID*.
4. No GitHub (Settings → Secrets and variables → Actions) criar os segredos:
   `APPLE_TEAM_ID`, `APP_STORE_CONNECT_API_KEY_P8` (conteúdo do .p8), `APP_STORE_CONNECT_KEY_ID`,
   `APP_STORE_CONNECT_ISSUER_ID`.
5. Actions → **iOS build** → *Run workflow* com "Enviar para o TestFlight" marcado. A versão vem de
   `MARKETING_VERSION` (`project.yml`) e o build é o número da execução, sempre crescente.
6. No App Store Connect → TestFlight: preencher as informações de teste, criar um grupo de **teste externo** e
   ativar o **link público** do TestFlight. Esse link é o que vai no site.
7. Para a App Store: textos, privacidade, classificação etária e notas de revisão prontos em
   `docs/app-store/ficha-da-loja.md`.
