# Backlog

Itens registrados para as próximas versões. Não implementar até o pedido de versão nova.

## Onde estamos (31/10/2026)

- Versão publicada: **0.1.0** (release v0.1.0 no GitHub, APK assinado), testada e aprovada no aparelho.
- Nenhuma versão nova em andamento. Próximo passo: quando for pedida a versão 0.2.0, implementar os itens
  pendentes abaixo, mover os entregues para o CHANGELOG e gerar o release (commit `release:` na `main`).
- Também pendentes desde a 0.1.0 (ver CHANGELOG): TNC por USB-OTG e KISS TCP, mapa offline, mapa de calor de
  cobertura, modo carro, rotas e recordes RF.

## Pendentes

### 1. Mapa: ícones APRS por estação/objeto e site PT2VHF no comentário padrão
_Registrado em 31/10/2026 (referência: print do mapa do APRSdroid)._

- **Ícones APRS no mapa:** hoje todas as estações e objetos aparecem com o mesmo marcador padrão do osmdroid
  (`ui/Screens.kt`, laço `for (s in stations.values)`). Desenhar cada uma com o ícone do seu símbolo APRS,
  usando a tabela (`/` primária, `\` alternativa) e o código do símbolo já decodificados em
  `Position.symbolTable`/`symbol` e guardados na estação. Overlays (tabela alfanumérica sobreposta ao
  símbolo alternativo) devem mostrar o caractere por cima do ícone. Vale também para a posição própria ("Você"),
  com o símbolo configurado nas preferências, e para o indicativo exibido ao lado do ícone, como no APRSdroid.
  Precisa de um sprite set de símbolos APRS com licença compatível (ex.: conjunto aprs-symbols de hessu/aprs.fi).
- **Site PT2VHF no comentário:** no APRSdroid o comentário da posição sai com `https://aprsdroid.org/`. No nosso
  app o texto padrão enviado deve mostrar `www.PT2VHF.com`. Hoje o padrão é `"PT2VHF APRS Client for Android"`
  (`data/Settings.kt`). Ajustar o padrão para incluir `www.PT2VHF.com` dentro do limite de 43 caracteres
  (ex.: `PT2VHF APRS Android www.PT2VHF.com`) e decidir se o site é anexado sempre, mesmo quando o usuário
  edita o comentário.

### 2. Mensagens: filtro e ordenação
_Registrado em 31/10/2026._

- Adicionar filtro na aba Mensagens. Ordem padrão: por ordem de chegada, com opção de o usuário alterar a ordem
  (ex.: mais recentes primeiro ou mais antigas primeiro), lembrando a escolha nas preferências.

### 3. Estações: ordenação e filtro por tipo
_Registrado em 31/10/2026._

- **Ordenação** na aba Estações, configurável pelo usuário. Padrão: por proximidade (distância da minha posição).
  Outras opções sugeridas: ouvida por último, indicativo (A-Z).
- **Filtro por tipo** de estação: estações, iGates, digipeaters, objetos, itens, meteorológicas etc., reaproveitando
  as categorias de filtro já definidas no PT2VHF APRS Client (desktop). Padrão: tudo ligado. O app já classifica
  cada estação em `role` (`data/AprsRepository.kt`), que pode servir de base. Avaliar aplicar o mesmo filtro no mapa.

### 4. Mostrar os enlaces RF estabelecidos pelo próprio app
_Registrado em 31/10/2026._

- Hoje o mapa desenha em amarelo todos os enlaces RF deduzidos dos caminhos de qualquer pacote
  (`RfLink` em `data/AprsRepository.kt`), sem distinguir os meus. Mostrar à parte os enlaces que o app conseguiu
  estabelecer por RF: meu beacon repetido por digi/iGate (caminho com meu indicativo como origem) e estações
  ouvidas direto ou via digi pelo TNC.
- Destacar esses enlaces no mapa com cor própria e opção de exibir só os meus; listar também (estação, via
  direto/digi, quantidade, última vez, distância), como referência para alcance e recordes RF.

### 5. Tracklog (trajeto) com estilo configurável
_Registrado em 31/10/2026._

- Gravar o tracklog da minha posição (inclusive com o modo tracker em segundo plano) e desenhá-lo no mapa como linha.
- Na aba Configurações, permitir configurar o estilo da linha: espessura, cor, opacidade, tipo (contínua/tracejada)
  e por quanto tempo manter o trajeto. Incluir ligar/desligar a gravação e limpar o tracklog.
- Avaliar exportar o trajeto (GPX) e mostrar o trajeto das outras estações móveis com o mesmo estilo ou estilo próprio.

### 6. Configurações no nível do APRSdroid
_Registrado em 31/10/2026 (referência: preferências do APRSdroid, `res/xml/*.xml` em github.com/ge0rg/aprsdroid).
Só as ideias e opções; o APRSdroid é GPL, não copiar código._

Hoje a tela de Configurações tem indicativo, SSID, servidor/porta, filtro, comentário, símbolo, path RF e
transmitir por RF/APRS-IS. O que o APRSdroid oferece e falta aqui, agrupado por seção:

- **Fonte de posição:** escolher entre SmartBeaconing, periódica (intervalo em min + distância mínima em m) e
  manual (lat/lon digitadas, escolhidas no mapa ou "última posição conhecida", com envio automático periódico).
  Opções de GPS: precisão/consumo (alta, média, baixa) e usar localização da rede.
- **Parâmetros do SmartBeaconing editáveis:** velocidade rápida/lenta, intervalo rápido/lento e corner pegging
  (tempo mínimo de curva, ângulo mínimo, inclinação). Hoje são fixos em `core/Aprs.kt` (`SmartBeaconing`).
  Expor também o `stoppedBeaconMinutes`, que já existe em `data/Settings.kt` mas não aparece na tela.
  Incluir uma ajuda curta explicando cada parâmetro.
- **Seletor de símbolo visual:** grade de ícones APRS em vez de digitar tabela + símbolo (depende dos ícones do item 1).
- **Frequência de voz** no beacon (campo opcional, ex.: 146.520 MHz) e campo de status/comentário separado.
- **Privacidade:** ambiguidade de posição (0 a 4 dígitos), e ligar/desligar envio de velocidade/rumo e de altitude.
- **Conexão APRS-IS:** filtro por distância em km como atalho (gerar `r/lat/lon/dist` com minha posição),
  link para ajuda de filtros, timeout do socket TCP e opção de modo só recepção (UDP/HTTP para envio sem login).
- **Tipos de conexão/TNC:** escolher protocolo (APRS-IS, KISS, TNC2/texto, Kenwood com GPS do rádio, AFSK pelo
  áudio do celular) e meio (Bluetooth, USB-OTG com baud rate, TCP/IP). Para Bluetooth: modo cliente/servidor e
  canal. Para KISS: string de inicialização e atraso de init. Para AFSK: demodulador de alta qualidade,
  áudio via Bluetooth SCO, saída de áudio e prefixo de sincronismo. (USB-OTG e KISS TCP já estão pendentes
  desde a 0.1.0.)
- **Notificações configuráveis** separadas para mensagens, posições recebidas e meus pacotes digipeados:
  LED, vibração e toque de cada uma.
- **Tela e mapa:** manter tela ligada, tempo máximo de idade das estações exibidas, mostrar/ocultar objetos,
  mapa satélite, arquivo de mapa offline e tema de mapa (liga com o mapa offline pendente).
- **Log da conexão:** ligar/desligar registro detalhado da conexão para diagnóstico.

Itens do APRSdroid que já temos ou cobrimos melhor: passcode automático (lá é digitado), lista de servidores
APRS-IS, path RF e TNC KISS por Bluetooth. Organizar a tela de Configurações em seções (como no APRSdroid) em
vez de um único diálogo.

### 7. Persistência local das estações e histórico de enlaces RF entre reinicializações
_Registrado em 31/10/2026 (relato: ao fechar o app, o mapa e a lista de estações começam vazios e precisam ser repopulados)._

- **Diagnóstico atual:** `AprsRepository.kt` guarda `_stations` e `_links` apenas em `MutableStateFlow`,
  em memória; ao encerrar o processo essas coleções são perdidas. Apenas mensagens têm persistência
  local em `messages.json`. Não confundir fechar a tela com encerramento do processo pelo Android.
- **Persistir estações localmente:** usar banco Room/SQLite com chave única por indicativo/objeto, incluindo
  coordenadas, símbolo/tabela/overlay quando aplicável, comentário, tipo/role, meio, via, velocidade,
  rumo, informações de recepção e `lastHeard` real do último pacote. Gravar incrementalmente sem
  bloquear a thread principal, com transações e estratégia adequada a altas taxas de pacotes.
- **Carregamento na inicialização:** popular mapa e lista de estações imediatamente a partir do banco,
  antes de APRS-IS/TNC se reconectarem, inclusive offline. Pacotes novos devem atualizar os registros
  sem duplicá-los e sem sobrescrever com dados mais antigos.
- **Atualidade visível:** separar *última estação conhecida* de *estação recém-ouvida*. Identificar
  visualmente dados antigos (por exemplo, ícone atenuado, idade desde o último contato e filtro
  'ativas/recentes/todas'); nunca marcar estações restauradas como se tivessem sido recebidas agora.
  Exibição padrão sugerida: últimas 24 h, configurável.
- **Histórico e retenção:** sugerir 30 dias de retenção em disco, configuráveis; oferecer limpeza manual,
  política de expiração e limites de quantidade/tamanho, considerando filtros APRS-IS de grande alcance.
  A limpeza de cache/armazenamento pelo Android ou usuário pode excluir dados locais.
- **Enlaces RF:** se também forem persistidos, guardar `lastSeen`, origem e evidência do enlace, respeitar
  expiração própria e nunca representar um enlace histórico como contato RF ativo. Integrar ao item 4.
- **Teste de aceitação:** receber estações, encerrar completamente o processo, abrir o app sem rede:
  estações anteriores devem aparecer com horários originais; reconectar e verificar atualizações,
  sem duplicatas ou regressões; testar volume elevado de pacotes e limpeza configurada.
- **Escopo:** registrar para uma próxima versão; não implementar nem gerar APK até solicitação explícita.

### 8. Indicadores superiores de conexão: somente nome e cor (verde/vermelho)
_Registrado em 31/10/2026 (solicitação de simplificação do cabeçalho)._

- **Tela:** cabeçalho em `ui/App.kt`, componente `TopBar` e `Chip`.
  Hoje aparecem textos variáveis como `TNC desligado`, `APRS-IS desconectado`,
  `APRS-IS sem verificação`, `TNC <nome>` e `Sem GPS`.
- **Requisito:** exibir somente rótulos estáveis `APRS-IS`, `TNC` e `GPS`
  nos indicadores superiores. Remover palavras visíveis de estado
  (`ligado`, `desligado`, `conectado`, `desconectado`, `sem verificação`,
  `Sem GPS`). O estado deve ser sinalizado **pela cor**:
  **verde** quando operacional e **vermelho** quando inativo/indisponível.
  Alterar cor do indicador e do texto/ícone de maneira consistente, em tempo real.
- **Sem falsos positivos:** APRS-IS só fica verde após conexão **verificada**
  (`aprsIsVerified`); TNC somente com `tncConnected`; GPS somente
  com posicionamento disponível/atual (não somente último fix antigo), avaliando
  `gpsAccuracyM` e validade temporal do fix. Desconexões devem mudar para vermelho.
- **Acessibilidade:** cores são o único sinal **visual** pedido; disponibilizar
  estado textual para leitores de tela por `contentDescription`/semântica
  e deixar informações detalhadas (servidor, nome do TNC, precisão/erro) nas telas
  pertinentes, não no cabeçalho.
- **Teste:** verificar transições de cada indicador em conexão/desconexão,
  autenticação APRS-IS, perda/recuperação de GPS e Bluetooth.
- **Escopo:** apenas backlog; implementar quando for solicitada nova versão.

### 9. Conexão automática APRS-IS configurável (ativada por padrão)
_Registrado em 31/10/2026 (solicitação: conectar ao APRS-IS por padrão e permitir controle junto à configuração do servidor)._

- **Comportamento atual confirmado:** `MainActivity.kt` inicia o `AprsService` se o indicativo estiver
  configurado; `AprsService.onCreate()` chama `connectAprsIs()`, que conecta ao servidor
  configurado. Portanto, a tentativa automática de conexão **já existe** na v0.1.0.
  O que falta é um controle explícito de ativação/desativação dessa conexão.
- **Nova opção em Configurações → APRS-IS:** adicionar controle
  **"Conectar automaticamente ao APRS-IS"**, habilitado por padrão em novas instalações,
  junto aos campos de servidor, porta e filtro. Persistir em `data/Settings.kt`,
  respeitar escolha salva após fechar/reabrir o app ou reiniciar o serviço.
- **Independência entre conexão e transmissão:** não confundir com a opção já existente
  `txViaInternet` ("Transmitir pelo APRS-IS"). É possível manter conexão para
  **receber** mensagens/estações enquanto o envio pela internet está desativado.
  Ao desativar a nova opção de conexão, parar o cliente APRS-IS e não tentar reconectar
  automaticamente em mudanças de rede nem em recargas de configuração.
- **Ao reativar:** conectar ao servidor e porta configurados, com passcode e filtro
  existentes; manter reconexão automática quando houver queda ou troca Wi-Fi/dados
  móveis, desde que a opção permaneça habilitada. A conexão só inicia após configurar
  um indicativo válido e respeita disponibilidade de rede.
- **Terminologia:** o servidor atualmente configurado é **APRS-IS**;
  `aprs.fi` é um serviço distinto de consulta/visualização de dados APRS.
  Não substituir inadvertidamente o hostname do servidor APRS-IS pelo domínio
  `aprs.fi`. Manter os servidores predefinidos e a seleção de servidor customizado.
- **Teste de aceitação:** validar padrão ativo em instalação nova; persistência da
  escolha; desativação com desconexão efetiva; nenhuma reconexão quando desativado;
  recepção sem TX quando `txViaInternet=false`; restabelecimento da conexão após
  reativação e troca de redes; indicador do cabeçalho coerente com item 8.
- **Escopo:** somente registro no backlog; sem alteração funcional nem release
  até solicitação explícita de uma nova versão.
