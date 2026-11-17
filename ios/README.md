# PT2VHF APRS — iOS

App nativo para iPhone/iPad (SwiftUI, iOS 17+), irmão do app Android. Mesmo protocolo, mesmo TOCALL (`APZVHF`)
e passcode APRS-IS calculado do indicativo.

## O que tem nesta primeira versão (1.0)

- Mapa (Apple Mapas) com as estações; tipo de mapa **Padrão, Escuro, Satélite, Híbrido e Automático**.
- APRS-IS com reconexão automática e filtro pelo raio em volta da sua posição (atualizado ao se deslocar).
- Lista de estações com busca, ordem por recentes/próximas, ficha com distância, rumo, caminho e pacote.
- Mensagens: conversas, ack, até 5 reenvios (30 s, 60 s, 120 s, 240 s), boletins à parte, notificação.
- Beacon por GPS: "Beacon agora", rastreador por intervalo e em curvas; em segundo plano com permissão "Sempre".
- TNC KISS por **Bluetooth LE** (serviço KISS BLE e Nordic UART, os mesmos do Android #92).
- Pacotes recebidos/enviados, "Qual SSID usar?", remover acentos ao enviar.

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

## Publicar no TestFlight (uma vez só, pelo mantenedor)

1. Inscrever-se no **Apple Developer Program** (US$ 99/ano) em developer.apple.com/programs.
2. No App Store Connect: **Apps → +** novo app iOS, bundle ID `com.pt2vhf.aprs`, nome "PT2VHF APRS".
3. **Usuários e Acesso → Integrações → Chaves da API do App Store Connect**: criar chave com papel *App Manager*,
   baixar o `.p8` e anotar *Key ID* e *Issuer ID*.
4. No GitHub (Settings → Secrets and variables → Actions) criar os segredos:
   `APPLE_TEAM_ID`, `APP_STORE_CONNECT_API_KEY_P8` (conteúdo do .p8), `APP_STORE_CONNECT_KEY_ID`,
   `APP_STORE_CONNECT_ISSUER_ID`.
5. Actions → **iOS build** → *Run workflow* com "Enviar para o TestFlight" marcado.
6. No App Store Connect → TestFlight: preencher as informações de teste, criar um grupo de **teste externo** e
   ativar o **link público** do TestFlight. Esse link é o que vai no site.
