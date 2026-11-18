# PT2VHF APRS Client na Google Play

Guia para publicar o app Android na Google Play. Nada aqui publica sozinho: o envio à Play Console é manual
e depende da conta de desenvolvedor do mantenedor.

## Como o build da Play difere do APK do site

| | APK do site | AAB da Google Play |
|---|---|---|
| Comando | `./gradlew assembleRelease` | `./gradlew bundleRelease -Pplay=true` |
| Aviso de nova versão (#39) | ligado (baixa do site) | **desligado**: a Play proíbe atualização fora da loja |
| Lista de estações (#84) | informa o indicativo ao site | igual (só o indicativo, se a opção estiver ligada) |
| applicationId / assinatura | `com.pt2vhf.aprs`, chave própria | igual (ver "Chave de assinatura") |

O CI gera o `.aab` assinado no job **Signed AAB for Google Play** (artefato `google-play-aab`), em PRs
deste repositório, em commits `release:` e por execução manual. O `.aab` não vai para o site nem para a release.

## Chave de assinatura (decisão importante)

Use a **mesma chave** do APK do site no Play App Signing (opção "Usar chave existente / exportar com PEPK").
Assim quem instalou pelo site pode atualizar pela Play e vice-versa, sem desinstalar e sem perder dados.
Se deixar o Google gerar uma chave nova, os dois canais ficam incompatíveis no mesmo aparelho.
Essa escolha não pode ser desfeita depois do primeiro envio.

## Checklist da Play Console

1. Conta de desenvolvedor (taxa única de US$ 25) e verificação de identidade.
   Conta pessoal criada após nov/2023: teste fechado com **12 testadores por 14 dias** antes da produção.
2. Criar o app: nome **PT2VHF APRS Client**, idioma padrão português (Brasil), app gratuito.
3. Play App Signing com a chave existente (seção acima).
4. Enviar o `.aab` para **Teste interno** primeiro.
5. Ficha da loja: textos em [ficha-da-loja.md](ficha-da-loja.md); ícone 512×512, imagem de destaque
   1024×500 e de 2 a 8 capturas de tela de celular (sugestão: mapa, mensagens, rádio/TNC, configurações).
6. Política de privacidade: `https://www.pt2vhf.com/privacidade` (site PR #37); exclusão de dados em `#exclusao`.
7. Segurança dos dados: respostas abaixo.
8. Declarações de permissões e serviços em primeiro plano: textos abaixo (a Play pede um vídeo curto para cada tipo).
9. Classificação de conteúdo (questionário IARC): ferramenta/utilitário, sem conteúdo sensível. Público-alvo: 18+
   (radioamadorismo exige licença; evita as regras de apps infantis).
10. Android Auto: o app declara um serviço de carro (categoria POI). A Play faz uma revisão separada para
    Android Auto; se ela reprovar, o resto do app pode ser publicado sem essa parte.

## Requisitos técnicos atendidos

- `targetSdk 36` (Android 16), exigido para apps novos e atualizações desde 31/08/2026.
- Formato Android App Bundle (`.aab`) assinado.
- Sem `REQUEST_INSTALL_PACKAGES` e sem atualização própria no build da Play.
- Sem localização em segundo plano (`ACCESS_BACKGROUND_LOCATION`): a posição é usada por um serviço em primeiro
  plano iniciado pelo usuário, com notificação visível.

## Declarações de serviço em primeiro plano

- **location**: o usuário liga o rastreador APRS; o app envia a posição da estação em intervalos (beacon) enquanto
  a notificação está visível. Desligar o rastreador encerra o uso.
- **connectedDevice**: mantém a conexão com o TNC do rádio (Bluetooth clássico, Bluetooth LE ou USB) para receber
  e transmitir pacotes APRS com a tela desligada.
- **microphone**: modo TNC por áudio (AFSK 1200): o app decodifica o áudio do rádio. Só é usado quando o usuário
  escolhe esse modo de conexão.
- **specialUse**: mantém a conexão com a rede APRS-IS e o recebimento de mensagens com a tela desligada
  (cliente APRS para radioamadores).

## Segurança dos dados (respostas sugeridas)

- **Localização aproximada e precisa**: coletada e compartilhada. Finalidade: funcionalidade do app. É o próprio
  propósito do APRS: a posição é transmitida pelo rádio e/ou pela rede APRS-IS, pública por natureza, somente
  quando o usuário liga o beacon ou o rastreador. Não é opcional para o beacon, é opcional para usar o app.
- **Identificadores pessoais (indicativo de radioamador)**: compartilhado na rede APRS-IS e, se a opção
  "Aparecer na lista de estações" estiver ligada, enviado ao site pt2vhf.com (só o indicativo, sem SSID nem posição).
- **Mensagens**: as mensagens APRS são públicas por natureza (rádio/APRS-IS); ficam guardadas só no aparelho.
- **Áudio**: o áudio do modo AFSK é processado no aparelho e não é gravado nem enviado.
- **Relatórios de erro**: só se o usuário tocar em "Enviar log", pelo app de e-mail/compartilhamento dele.
- Dados criptografados em trânsito: **não** (respondido assim na Play): o site usa HTTPS, mas a APRS-IS e o rádio são abertos por definição.
- Exclusão de dados: tudo fica no aparelho; desinstalar apaga. Para sair da lista do site, desligar a opção.
- Sem anúncios, sem SDKs de analytics ou rastreamento.
