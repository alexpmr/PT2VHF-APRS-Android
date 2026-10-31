# Backlog

Itens registrados para as próximas versões. Não implementar até o pedido de versão nova.

## Onde estamos (31/10/2026)

- Release mais recente publicada: **v0.1.3**, APK assinado; o site oficial também publica v0.1.3. Há validação física pendente dos itens do backlog, incluindo falhas anteriormente observadas na v0.1.2.
- Continuar correções a partir do backlog e só baixar os requisitos validados. Distribuição pública no site requer confirmação separada do manifesto e do arquivo.
- Também pendentes desde a 0.1.0 (ver CHANGELOG): TNC por USB-OTG e KISS TCP, mapa offline, mapa de calor de
  cobertura, modo carro, rotas e recordes RF.

## Acompanhamento de implementação e baixa (31/10/2026)

Aprovada a implementação integral, mas **não confundir código no PR com funcionalidade homologada**.
A release **v0.1.3** foi gerada e assinada no GitHub ([tag v0.1.3](../../releases/tag/v0.1.3)) e disponibilizada no site.
O backlog permanece parcialmente implementado, com homologação física pendente. Não afirmar que os 23 itens foram concluídos. Os critérios, roteiros e evidências ficam no
[HOMOLOGACAO.md](HOMOLOGACAO.md).

| Item | Estado atual | Evidência necessária para baixa |
|---|---|---|
| 1 | **FALHA CONFIRMADA NA v0.1.2**: símbolos do mapa genéricos/incorretos; comentário padrão alterado | Renderização real de símbolos APRS, tabela primária/alternativa, overlays, ícone próprio e testes físicos |
| 2 | **Ampliação pendente (v0.1.3):** filtro de Conversas hoje é só texto livre e a ordenação inicial é do mais antigo para o mais recente | Ordenação padrão por **última atividade decrescente**; ícone/painel de filtros completos, combináveis, persistentes; testes de mensagens novas, SSID e conversas filtradas |
| 3 | **Pendente (v0.1.2)**: lista sem atualização em tempo real; filtro/ordenação incompletos | Ícone com critérios de ordenação ASC/DESC e filtros efetivos por data, indicativo, primeira/última recepção, distância e tipo, atualizados em tempo real |
| 4 | Parcial: origem de RX RF corrigida | Evidência no TNC e apresentação dos meus enlaces |
| 5 | Parcial: tracklog SQLite/mapa | Uso real, estilo completo, retenção e GPX |
| 6 | Parcial: beacon manual/periódico/Smart | Teste GPS e parâmetros; demais opções pendentes |
| 7 | Implementado no PR; **teste pendente** | Reiniciar offline com estações e RF preservados |
| 8 | Implementado no PR; **teste pendente** | Estados reais APRS-IS/TNC/GPS |
| 9 | Implementado no PR; **teste pendente** | Desconexão persistente, reconexão e somente RX |
| 10 | Pendente | Perfis completos Base/Móvel/Portátil/RX |
| 11 | Parcial: seguir/pausar/retomar | Estação móvel real e trajetória histórica |
| 12 | Parcial: favoritos persistentes | Notificações por evento faltam |
| 13 | Parcial: indicadores/monitor | Exportação e diagnóstico detalhado faltam |
| 14 | Pendente | Estatísticas/recordes RF comprovados |
| 15 | Pendente | API segura e testes de permissões |
| 16 | **Pendente**: confirmar fechamento do aplicativo | Diálogo ao sair pela navegação Android, cancelar/sair e tratamento explícito de tracker/TX em segundo plano |
| 17 | **Pendente**: restaurar mapa na abertura | Persistir centro e zoom do último acesso; ícone para localização atual; não sobrescrever a vista restaurada por GPS automático |
| 18 | **Implementado no PR #8; CI e teste físico pendentes**: versão instalada no aviso do site oficial | Ao abrir, popup com versão real da instalação e link www.PT2VHF.com, uma vez por abertura |
| 19 | **Falha de usabilidade relatada na v0.1.3**: ação Mensagem já existe no código, mas não está claramente acessível no popup de detalhes da estação | Botão destacado Enviar mensagem, sempre visível, navegação direta à conversa com destinatário preenchido; teste Android real em telas pequenas |
| 20 | **Pendente — v0.1.3**: responder a uma mensagem mantendo-a pressionada | Long press em bolha recebida, ação Responder e compositor com contexto/destino correto; não transmitir antes de Enviar |
| 21 | **Pendente — v0.1.3**: procurar indicativos para iniciar conversa nova | Busca por indicativo completo/parcial em estações e contatos recentes, seleção de SSID exato e abertura da conversa sem TX automático |
| 22 | **Pendente — v0.1.3**: controle de brilho do mapa inexistente | Ícone de sol com ajuste independente das configurações globais do Android, intensidade persistente, padrão neutro e teste de camadas/legibilidade |
| 23 | **Pendente — v0.1.3**: ordenar cada mensagem pessoal ou geral pelo horário de recepção | Linha do tempo cronológica (antigas acima, recentes abaixo), desempate estável e atualização reativa; independente da ordem da lista de Conversas (#2) |


**Baixa definitiva:** marcar `Concluído` somente após CI aprovado,
teste físico aplicável validado pelo usuário e requisitos da seção correspondente
integralmente atendidos. Corrigir eventuais falhas e registrar commit específico
para cada lote de aprovações. Pendências não implementadas **não são baixadas**.

## Pendentes

### 1. Mapa: ícones APRS por estação/objeto e site PT2VHF no comentário padrão
_Registrado em 31/10/2026 (referência: print do mapa do APRSdroid)._

**Regressão/pendência confirmada pelo usuário na APK v0.1.2 (31/10/2026):** o mapa continua
sem exibir os símbolos gráficos correspondentes às estações reais. O ajuste do comentário de
beacon **não resolve** a renderização dos ícones. **Status: aberto / correção prioritária /
teste físico obrigatório. Não baixar este item apenas por compilação.**

**Critérios objetivos de aceite para a próxima implementação:**

- Utilizar os campos APRS do pacote de posição **`symbolTable` e `symbol`** (não um marcador
  genérico, nem somente `role`/tipo de estação). Implementar mapeamento completo das
  tabelas primária `/` e alternativa `\\`, com as variantes de overlay alfanumérico
  definidas pelo protocolo, inclusive símbolos de objetos e itens quando forem recebidos.
- Mostrar os símbolos convencionais corretos para estação fixa, móvel/carro, motocicleta,
  portátil/pessoa, digipeater, iGate, meteorológica, aeronave etc.; quando um símbolo for
  desconhecido, usar fallback visível e **não inventar outro símbolo como se fosse real**.
- Exibir o **símbolo da própria estação** conforme suas Configurações, sem confundi-lo com
  símbolos de estações remotas; manter o indicativo legível junto ao ícone.
- Obter sprite/atlas com licença de redistribuição verificada (ou criar símbolos próprios
  conforme a especificação) e garantir que imagens estejam embutidas no APK para funcionar
  também sem internet; cuidar de escala, densidade de tela, cache, atualização dos marcadores
  e desempenho com muitas estações.
- **Teste de aceitação em aparelho real:** comparar lado a lado os mesmos indicativos/objetos
  com mapa APRSdroid ou referência APRS conhecida, nas tabelas primária, alternativa e com
  overlay; verificar mudanças de símbolo recebidas em novos beacons, símbolo próprio,
  zoom, reinicialização do aplicativo e funcionamento offline. Anexar captura antes/depois.
- **Não publicar como concluído** enquanto o usuário ainda enxergar marcadores genéricos
  ou símbolos errados na APK instalada.


- **Ícones APRS no mapa:** hoje todas as estações e objetos aparecem com o mesmo marcador padrão do osmdroid
  (`ui/Screens.kt`, laço `for (s in stations.values)`). Desenhar cada uma com o ícone do seu símbolo APRS,
  usando a tabela (`/` primária, `\` alternativa) e o código do símbolo já decodificados em
  `Position.symbolTable`/`symbol` e guardados na estação. Overlays (tabela alfanumérica sobreposta ao
  símbolo alternativo) devem mostrar o caractere por cima do ícone. Vale também para a posição própria ("Você"),
  com o símbolo configurado nas preferências, e para o indicativo exibido ao lado do ícone, como no APRSdroid.
  Precisa de um sprite set de símbolos APRS com licença compatível (ex.: conjunto aprs-symbols de hessu/aprs.fi).
- **Marca única no campo Informação dos nossos beacons:** por orientação explícita
  do mantenedor (31/10/2026), **sempre** incluir exatamente
  **`www.PT2VHF.com`** (sem `https://` e sem caminho `/aprs` ou
  `/aprs-android`) no **comentário transmitido** de cada pacote de
  posição, tanto no Android quanto no cliente desktop. Isso vale para
  beacon manual, periódico, SmartBeaconing e qualquer outra rotina de
  posição permitida, por APRS-IS ou TNC/RF. O texto não deve depender
  de abertura do popup no mapa nem do tipo/SSID da estação.
- **Comentário personalizado + URL obrigatória:** preservar o valor original
  que o usuário digitou em Configurações. **Na montagem do pacote**, e
  somente nela, compor `<comentário útil> www.PT2VHF.com`, sem repetir
  o domínio caso já apareça no comentário e com `www.PT2VHF.com` no
  final. O campo de comentário do beacon transmitido tem teto de
  **43 caracteres** no caminho `positionInfo()`: reservar os
  14 caracteres do domínio e, quando houver descrição, 1 espaço; permitir
  no máximo 28 caracteres de descrição, com truncamento informado na
  interface/pré-visualização se necessário. **Nunca** cortar ou omitir o
  domínio por aplicar `.take(43)` ao final indiscriminadamente.
  Se a descrição estiver vazia, transmitir apenas `www.PT2VHF.com`.
  Não truncar ou sobrescrever o comentário salvo permanentemente.
- **Compatibilidade de configurações existentes:** o default Android atual
  é `PT2VHF APRS www.PT2VHF.com` (`data/Settings.kt`). Preservar
  comentário customizado e evitar duplicar o domínio em perfis legados.
  O comentário transmitido deve ser montado em **ponto central comum**
  às rotas de TX, sem produzir beacons novos apenas por iniciar ou
  atualizar o aplicativo.
- **Exibição fiel de estações recebidas:** o campo Informação do popup de
  qualquer estação apresenta **o comentário realmente recebido por APRS**,
  inclusive URLs de terceiros, sem inserir `www.PT2VHF.com` nas
  informações de rádios/programas alheios. O requisito de sempre mostrar
  o domínio se refere à transmissão de **nossos** beacons.
- **Critérios de aceite:** capturar payload bruto de ambos os aplicativos,
  em APRS-IS e RF/TNC, com descrição padrão, vazia, personalizada curta,
  personalizada longa e já contendo o domínio (maiúsculas/minúsculas);
  comprovar `www.PT2VHF.com` **uma vez e integralmente** em cada
  beacon, tamanho dentro do limite, mensagem de truncamento na UI e
  comentário persistido intacto. Comparar no receptor independente
  PT2VHF/APRSdroid sem depender de renderização local.
  Não transmitir posição por causa de migração ou simples abertura.

### 2. Mensagens → Conversas: filtros completos e ordenação por última atividade
_Registrado em 31/10/2026; ampliado por solicitação expressa em 31/10/2026._

**Problema identificado na v0.1.3:** `MessagesScreen()` em
`app/src/main/java/com/pt2vhf/aprs/ui/Screens.kt` oferece apenas
`filter` textual e um botão cíclico para ordenar. A opção inicial é
`Settings.readOption(context, "messages_order", "oldest")`, fazendo
`newestFirst = false` para instalações sem preferência anterior.
No modo "oldest" a lista usa `minOf { it.time }` por conversa,
em vez da **última atividade**. Atualmente, o filtro é aplicado às
mensagens **antes** do agrupamento por contato, podendo levar a uma
prévia/hora diferente da mensagem mais recente real da conversa.

**Comportamento exigido:**

- **Padrão de ordenação:** na lista `Mensagens → Conversas`, apresentar
  sempre **mais recente primeiro**, ordenando por `maxOf(message.time)`
  de cada contato, considerando **mensagens recebidas e enviadas**.
  Recepção ou envio novo deve reordenar a lista **imediatamente**, sem
  recarregar a tela, mesmo se a conversa estiver fora do trecho visível.
  Empates resolvidos de modo determinístico pelo indicativo completo.
  O padrão deve valer em instalações novas e nas antigas que nunca
  escolheram explicitamente uma ordem; **respeitar preferência manual
  previamente gravada**. Permitir ordem inversa, A→Z/Z→A e outras
  opções úteis, sempre com direção explícita e persistente.
- **Painel único de filtros:** substituir a ambiguidade do controle
  atual por **ícone claro de filtros/ordenação** ao lado da busca, com
  seletores diretos, indicador de filtro ativo, **Aplicar**, **Limpar**
  e opção de restaurar o padrão. Não usar botão que muda o critério
  somente a cada toque sem indicar todas as opções.
- **Critérios independentes, combináveis (AND):**
  - **Indicativo/contato**: substring, sem distinguir maiúsculas e
    minúsculas; considerar o indicativo completo e **SSID** exato.
  - **Texto da mensagem**: busca por trecho do conteúdo recebido
    ou enviado, sem eliminar a conversa inteira quando existir
    correspondência em qualquer mensagem de seu histórico.
  - **Data e horário**: início/fim inclusivos de atividade ou de
    mensagens correspondentes, com interpretação no **fuso local**;
    permitir filtros de atividade recente, por exemplo 24 h, 7 dias,
    30 dias, além de intervalo personalizado.
  - **Direção**: todas, recebidas, enviadas; não confundir a origem do
    pacote com o interlocutor da conversa.
  - **Transporte de recepção/transmissão**: todos, APRS-IS, RF/TNC,
    apenas quando `ChatMessage.medium` realmente possuir evidência;
    mensagens sem meio conhecido não devem ser falsamente classificadas.
  - **Estado do envio**: pendente/aguardando ACK, confirmado, falho,
    quando representado em `ChatMessage.status`. Não inventar ACK
    para mensagens que não o possuem.
  - **Não lidas**: disponibilizar apenas junto com controle real de
    leitura persistente; o modelo `ChatMessage` atual não contém
    indicador `read`/`unread`. Implementar esse controle e sua
    persistência antes de oferecer o filtro ou ocultá-lo até existir.
- **Ordenação x filtro são diferentes:** manter ordem padrão pelo
  carimbo da **última mensagem real da conversa**, e mostrar prévia
  correspondente; o filtro por texto/direção/data decide quais
  conversas aparecem, mas não deve fazer uma mensagem antiga parecer
  a última. Opcionalmente destacar o trecho correspondente como
  resultado de pesquisa, sem substituir os metadados da última
  mensagem da conversa.
- **Operação e persistência:** exibir total de conversas e quantidade
  filtrada; combinação de filtros não apaga mensagens nem altera
  estados. Persistir preferências de ordenação e filtros no Android
  após fechar/abrir o aplicativo. Filtro e ordenação devem reagir às
  novas mensagens sem atualização manual. Evitar processamento custoso
  a cada digitação em históricos grandes.
- **Separação de funções:** pesquisar/consolidar **conversas que já
  têm mensagens** não substitui `Mensagens → Nova mensagem`, cuja
  pesquisa de indicativos conhecidos e seleção de destinatário está
  prevista no **item #21**. Não misturar o filtro do histórico com
  a busca global de contatos.
- **Teste T21:** criar três conversas com horários distintos; conferir
  ordem por última mensagem recebida/enviada, inversão e critério
  alfabético; enviar/receber pacote com a lista aberta e conferir
  reordenação imediata; filtrar por parte do indicativo, SSID, texto
  de mensagem antiga, datas limite, direção, meio RF/APRS-IS e
  estados existentes; aplicar filtros combinados, limpar e reiniciar
  o aplicativo; testar valores de campo ausentes, horário local e
  nenhuma correspondência. Caso habilitado, validar não lidas em
  banco persistente. Abrir uma conversa filtrada e assegurar que
  seu histórico completo permaneça acessível.
- **Status:** melhoria de especificação/backlog, **não implementada
  nem homologada nesta solicitação**.

### 3. Estações: ordenação e filtro por tipo
_Registrado em 31/10/2026._

**Falha relatada pelo usuário na v0.1.2 (31/10/2026):** a aba **Estações não
se atualiza em tempo real**, inclusive quando chega uma estação nova ou um pacote
com atualização de uma estação já listada. Solicitação adicional: criar **ícone
visível de classificação/filtro** que abre um painel de opções explícitas, evitando
o mecanismo atual de botões que apenas alternam ciclicamente entre as opções.
**Status: aberto, prioridade alta; nenhuma baixa sem validação no aparelho.**

- **Atualização imediata:** a lista deve refletir, sem recarregar tela, trocar de aba
  ou reiniciar o aplicativo, toda inserção e alteração relevante recebida por
  APRS-IS ou TNC RF: indicativo, tipo, última recepção, posição, distância,
  velocidade, rumo, comentário, transporte e histórico associado quando aplicável.
  Se um pacote não tiver posição, manter a estação identificada de modo apropriado
  (ex.: "sem posição"), em vez de descartá-la silenciosamente; não inventar posição.
- **Reatividade real:** investigar de ponta a ponta o processamento de pacotes,
  atualização de `AprsRepository.stations`/`StateFlow`, coleta no Compose,
  emissão de mudanças de registros existentes e renderização de `LazyColumn`.
  O código já chama `collectAsState()`, o que **não comprova** que a chegada de
  cada pacote esteja produzindo os dados/estados corretos. Evitar listas
  memorizadas ou mutações que não emitam novo estado. Diferenciar não receber
  pacotes de receber sem refletir alterações na interface.
**Ampliação solicitada em 31/10/2026 — filtros de fato, não apenas ordenação:**

- Disponibilizar um **único ícone claramente visível de filtros/classificação** na aba Estações. Ao tocar, abrir painel estruturado com duas seções independentes:
  - **Ordenar por**: nome/indicativo, data/hora da **primeira recepção** ("quando chegou"), data/hora da **última recepção** (último pacote), distância até minha posição e outros campos com metadados confiáveis; para **cada critério**, direção **crescente/ascendente** ou **decrescente/descendente**, não apenas A–Z/Z–A nem um ciclo de opções.
  - **Filtrar resultados**: indicativo/nome (busca parcial, sem distinção de maiúsculas/minúsculas), data/horário inicial e final da primeira recepção, data/horário inicial e final da última recepção, distância mínima/máxima em km, tipo de estação (múltiplos tipos), fonte APRS-IS/TNC RF, favoritas e atividade/idade; **combinar os filtros simultaneamente (interseção/AND)**.
- **Diferenciar primeira recepção de última recepção:** usar carimbos de tempo reais, preservar `firstSeen` na inserção inicial e `lastHeard` nos pacotes posteriores; persistir os dois com migração de banco compatível (registros antigos sem `firstSeen` devem ser identificados como desconhecidos ou migrados com regra explicitada, sem inventar histórico).
- **Critérios com campo indisponível:** não atribuir distância fictícia quando falta GPS ou posição da estação; mostrar aviso/valor indisponível e fallback definido. Nunca confundir momento do pacote na rede com hora da primeira recepção local.
- **Comportamento:** aplicar, limpar filtros, restaurar padrões, visualizar filtros ativos e quantidade de estações visíveis versus total. Manter configurações após reiniciar o app. A inserção ou atualização em tempo real deve respeitar os filtros sem esconder silenciosamente dados novos quando a opção estiver ativa.
- **Teste físico e automatizado:** ordenar ASC/DESC por cada campo; usar intervalos de data/hora, substrings de indicativo e raios mínimo/máximo; combinar filtros; confirmar inclusão/exclusão correta quando chega pacote novo e quando um indicativo conhecido muda de distância/última recepção, com a tela aberta e também após reiniciar. Incluir casos sem GPS/sem posição e limites de horário/fuso (horários exibidos no fuso local).
- **Escopo:** apenas registrar agora; implementar na próxima versão solicitada, preservando os critérios anteriores do item #3.

- **Ordenação instantânea:** após inserção ou mudança de posição/horário,
  recalcular a ordem selecionada (proximidade, última recepção e indicativo)
  sem ação manual, mantendo filtros, favoritos e estado da tela. Atualizar
  o indicador de tempo decorrido mesmo quando não chegam pacotes novos.
- **Ícone próprio de classificar/filtrar** (ex.: `list-filter` ou `sliders-horizontal`)
  na barra da aba Estações, com nome acessível. Ao tocar, abrir um popup/painel
  com controles **explícitos** e opção de aplicar, limpar/restaurar padrão:
  - **Classificar por:** distância (padrão quando há localização válida),
    última recepção (mais recentes primeiro), indicativo (A–Z/Z–A);
    indicar qual opção está ativa.
  - **Filtrar tipos:** estações, móvel/tracker, iGate, digipeater, estação
    meteorológica, objetos e itens, com seleção múltipla e "Todos".
  - **Filtros adicionais úteis:** favoritos, origem APRS-IS ou TNC RF,
    estações recentes/ativas e busca por indicativo; explicar quando um
    filtro esconde uma estação recém-chegada.
  - Persistir as opções de classificação e filtro entre sessões, mostrar
    visualmente quando algum filtro está ativo e oferecer "Limpar filtros".
- **Critérios de aceite:** (1) manter aba Estações aberta, gerar/injetar
  novos pacotes APRS por APRS-IS e TNC e ver novos indicativos surgirem sem
  interação; (2) atualizar a posição e última recepção de um indicativo
  existente e verificar dados e reordenação imediatamente; (3) abrir o
  ícone, escolher diretamente qualquer classificação e combinação de
  tipos, aplicar e desfazer; (4) fechar/reabrir o app e conferir persistência;
  (5) comparar contagens RX do diagnóstico com estações elegíveis e verificar
  ausência de duplicatas, perda de atualizações, travamentos ou uso excessivo
  de CPU durante fluxo intenso. Registrar prints/vídeo e origem dos pacotes.
- **Escopo:** apenas registrar no backlog; implementar e testar na próxima
  versão explicitamente solicitada.

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

### 10. Perfis de operação APRS: Base, Móvel, Portátil e Somente recepção
_Aprovado em 31/10/2026 após comparação funcional com o APRSdroid._

- Disponibilizar perfis nomeáveis, selecionáveis rapidamente, incluindo predefinições Base,
  Móvel, Portátil e Somente recepção. Cada perfil conserva indicativo/SSID, símbolo APRS,
  comentário, frequência de voz quando pertinente, path RF, políticas de beacon,
  localização (GPS/manual), configurações APRS-IS, filtro, TNC/meio de conexão e
  autorizações de TX.
- Trocar de perfil sem reconstruir a configuração manualmente; persistir e restaurar
  o último perfil ativo após reiniciar. Confirmar alterações com impacto em transmissão
  antes de aplicá-las; não iniciar TX inesperadamente ao trocar de perfil.
- Perfil Somente recepção impede beacon e mensagens de saída em *todos* os meios,
  inclusive RF, sem impedir receber estações e mensagens APRS-IS/TNC.
- Integrar à opção de conexão automática do item 9 e aos controles SmartBeaconing,
  posicionamento manual e privacidade do item 6; não duplicar configurações.

### 11. Seguir estação móvel no mapa e acompanhar deslocamentos
_Aprovado em 31/10/2026._

- Comando "Seguir estação" no mapa, lista e ficha do indicativo; manter a estação
  escolhida no centro enquanto recebe novas posições e permitir parar de seguir.
- Exibir deslocamento/trajeto histórico do alvo, velocidade, rumo, distância,
  última posição e tempo desde último contato, sem confundir dados restaurados do
  cache com posição nova. Integrar com os itens 5 e 7.
- Não forçar recentering se o usuário estiver explorando manualmente o mapa; oferecer
  retomada explícita do seguimento. Mostrar aviso se o alvo perder atualização.
- Preservar o estado de acompanhamento conforme configuração; operação com mapa
  offline quando o recurso de mapas offline estiver disponível.

### 12. Estações favoritas e alertas por eventos
_Aprovado em 31/10/2026._

- Marcar e desmarcar estações como favoritas, persistindo indicativos e preferências.
- Notificações configuráveis por estação e por tipo de evento: primeira aparição,
  retorno após período sem contato, nova posição, recepção RF direta comprovada
  e meu pacote retransmitido por digi/iGate (quando houver evidência suficiente).
- Limites de frequência/cooldown e horário silencioso; evitar notificações em
  massa após restauração de cache ou reconexão APRS-IS.
- Separar claramente eventos de APRS-IS dos observados por RF; integrar aos filtros
  de estações (item 3), notificações (item 6) e persistência (item 7).
- Testar consumo de bateria e comportamento em segundo plano.

### 13. Centro de diagnóstico de rádio, conexões, pacotes e mensagens
_Aprovado em 31/10/2026._

- Tela de diagnóstico com estados detalhados APRS-IS, GPS e TNC: servidor,
  modo de conexão, tempo conectado, reconexões, erros e transições recentes.
- Monitor separado RX/TX com horário, origem (APRS-IS/RF), tipo e tamanho de
  pacote, digipeaters do caminho, origem de recepção, ACK, retransmissões,
  tentativas sem sucesso e erros KISS/AX.25 quando disponíveis.
- Métricas por sessão: pacotes recebidos/transmitidos, descartes, duplicatas,
  mensagens aguardando ACK, sucesso de ACK e atividade direta/via digi;
  deixar explícito quando a métrica não é observável no transporte escolhido.
- Histórico local limitado, filtros por indicativo/meio/erro, exportação de
  diagnóstico com anonimização opcional de indicativos, localização e dados
  sensíveis. Não exportar passcode ou credenciais.
- Complementar, sem duplicar, o log detalhado já previsto no item 6 e o
  histórico de enlaces RF dos itens 4 e 7.

### 14. Análise de alcance e recordes RF comprovados
_Aprovado em 31/10/2026._

- Estatísticas de alcance RF **direto** e **via digipeater** separadas, com
  indicativo, distância, data/hora, caminho de recepção e última confirmação.
- Mostrar recordes RF por período, estação e transporte; permitir ver no mapa
  o trajeto dos saltos somente quando houver evidência apropriada.
- Mapa de calor e histórico de cobertura RF calculados exclusivamente a partir
  de recepções por rádio/TNC, jamais marcando APRS-IS como contato RF direto.
- Integrar ao item 4 (meus enlaces) e ao histórico do item 7. Retenção,
  armazenamento e exportação configuráveis; sinalizar limitações das evidências
  de digipeating e não inferir enlace direto apenas de caminho APRS-IS.

### 15. API de integração Android com outros aplicativos
_Aprovado em 31/10/2026, inspirado na interface de aplicativos do APRSdroid._

- Oferecer interface documentada para integrações locais no Android (por exemplo,
  Intents/Broadcasts/serviço protegido), após avaliação de permissões e segurança.
- Permitir consultar status, estações e eventos de recepção; solicitar ações
  permitidas como iniciar/parar acompanhamento e, sob autorização explícita,
  transmitir beacon ou mensagem.
- Restringir transmissão e leitura de dados sensíveis por permissões e
  confirmação do usuário; nunca expor passcode APRS-IS ou permitir transmissão
  silenciosa por aplicativos arbitrários.
- Versionar contratos, documentar formatos de eventos e criar testes de
  compatibilidade e segurança. Não copiar código de terceiros sob licença
  incompatível.

### 16. Confirmar antes de sair do aplicativo
_Solicitado em 31/10/2026, após uso da versão v0.1.2._

- **Ao tentar fechar o app pelo botão Voltar/gesto de retorno na tela principal** ou pelo comando explícito **Sair** (caso exista), mostrar diálogo modal: **"Deseja sair do PT2VHF APRS Client?"** com ações **Cancelar** (padrão, mantém a aplicação aberta) e **Sair** (confirma o fechamento da interface). Não mostrar confirmação ao navegar entre abas, fechar submenus/diálogos ou retornar de outras telas.
- **Com tracker, TNC, GPS em segundo plano ou TX programado ativo**, a confirmação deve deixar claro se haverá continuidade. Preferir ações inequívocas: **Cancelar**, **Sair e manter o rastreamento em segundo plano** e **Sair e interromper o rastreamento**, se os serviços estiverem ativos. Não desligar nem continuar transmissão RF/APRS-IS silenciosamente por ambiguidade.
- No caminho **Sair e interromper o rastreamento**, solicitar parada ordenada do serviço e das automações de beacon, respeitar transições de estado e persistir preferências/histórico; não transmitir beacon extra ao encerrar. No caminho **manter rastreamento**, preservar o foreground service e sua notificação visível, conforme permissões e regras do Android.
- Integrar ao tratamento de **Back** (inclusive gesto preditivo, se suportado) em `MainActivity.kt`/Compose; não interceptar botão Home, tela de recentes, encerramento forçado pelo sistema ou comando **Forçar parada**, pois essas ações não oferecem garantia de diálogo pelo Android.
- **Teste de aceitação em celular real:** (1) tentar Voltar na tela principal e escolher **Cancelar**, verificando que continua na mesma tela; (2) confirmar **Sair**, verificando encerramento adequado; (3) com tracker/TNC conectado e beacon ativo, verificar as duas opções de continuidade/parada, ausência de TX inadvertido e notificação coerente; (4) abrir novamente e comprovar preservação de configurações, estações e mensagens; (5) repetir em navegação por gestos e por três botões, quando disponível.
- **Status:** backlog apenas; não implementar nem publicar APK até solicitação explícita de nova versão.

### 17. Mapa: restaurar centro e nível de zoom da última visualização ao abrir
_Registrado em 31/10/2026 (solicitação do usuário na v0.1.2)._

- **Defeito observado no código:** `ui/Screens.kt`, função `MapScreen`, define na fábrica do `MapView` um centro fixo em `GeoPoint(-15.78, -47.93)` e zoom `9.0`. Quando chega a primeira posição GPS, o bloco `!centered` pode recentralizar a visualização, sobrescrevendo a área que o usuário prefere.
- **Persistir última vista do mapa:** guardar localmente latitude, longitude e zoom efetivos do **centro da visualização** após o usuário mover/aproximar/afastar o mapa (não somente a posição própria do GPS); recuperar esses valores ao voltar à aba e após fechar/reabrir o aplicativo, inclusive sem internet/GPS. Persistir também ao pausar/sair, sem gravações excessivas a cada frame.
- **Precedência na abertura:** (1) se há centro/zoom salvos e válidos, restaurar exatamente o último enquadramento e **não recentralizar por GPS automaticamente**; (2) se for primeiro uso, sem vista salva, centralizar na localização atual quando disponível; (3) se o GPS ainda não estiver disponível, mostrar um enquadramento neutro/abrangente, sem usar Brasília como ponto fixo universal. Não bloquear mapa aguardando GPS.
- **Mostrar onde estou:** manter marcador de posição própria (GPS) quando disponível e incluir ação/ícone **"Minha localização"** que recentralize sob comando explícito do usuário, conservando zoom útil; após interação manual com o mapa, não voltar a forçar centralização pelo GPS. Não transmitir localização por APRS apenas por abrir o mapa.
- **Compatibilidade com "Seguir estação":** comando explícito de seguir alvo pode temporariamente prevalecer; ao cancelar o seguimento, retornar ao último enquadramento escolhido pelo usuário ou à vista restaurada, sem perder o estado anterior.
- **Robustez:** validar limites de coordenadas e zoom; aplicar viewport somente depois de o `MapView` estar pronto; persistir estado sem operações pesadas na UI; respeitar rotação de tela e recriação de Activity; não expor localização ao servidor para salvar preferências.
- **Critérios de aceite:** (1) abrir mapa e navegar para outra região com zoom personalizado; fechar totalmente e reabrir: mesmo centro/zoom, sem deslocamento automático pelo primeiro GPS; (2) usar "Minha localização" e verificar recentralização na posição atual; (3) mover o mapa manualmente e confirmar que o GPS não o recentraliza sozinho; (4) repetir sem GPS/internet e após mudança de aba/rotação; (5) testar Seguimento de estação e retorno ao enquadramento manual; (6) instalação limpa (sem preferências salvas) deve usar posição atual quando disponível.
- **Status:** pendente no backlog; implementar somente mediante solicitação de nova versão.

### 18. Anúncio do site oficial com a versão atualmente instalada
_Solicitado em 31/10/2026 durante o desenvolvimento da v0.1.3._

- **Na abertura do app**, apresentar o popup já existente de anúncio de downloads no site oficial, acrescentando uma linha **"Versão instalada: X.Y.Z"**, obtida dinamicamente de `BuildConfig.VERSION_NAME`, sem número fixo no texto.
- Mostrar claramente o endereço **www.PT2VHF.com**, com link para o site. Manter o aviso apenas **uma vez por abertura**, independentemente de troca de abas ou mudanças de configuração.
- Preservar o popup independente de **nova versão disponível**: o anúncio do site é informativo; a verificação OTA segue separada, com indicação de versão remota.
- Disponibilizar as traduções da nova linha em PT/EN/ES/FR/IT; nunca confundir versão instalada com versão anunciada no servidor.
- **Critério de aceitação:** iniciar com v0.1.3, conferir texto, endereço, versão exata da APK e abertura do link; fechar aviso, trocar de abas (não deve reaparecer), forçar novo lançamento do app (deve aparecer); conferir a separação do aviso OTA, sem duplicidade.
- **Implementação** adicionada no PR [#8](../../pull/8); **não baixar** até CI aprovado e teste no aparelho.

### 19. Detalhes da estação: ação evidente de enviar mensagem APRS diretamente
_Solicitado em 31/10/2026 após selecionar uma estação na v0.1.3._

**Diagnóstico técnico:** `StationDialog` em `ui/Screens.kt` já contém a ação
`TextButton(onClick = onMessage)` com rótulo **"Mensagem"**, e `App.kt`
já direciona `onMessage` a `chatWith = call; tab = Tab.MESSAGES`.
A `Conversation` existente endereça o envio a `contact`. Portanto,
**não é uma capacidade de transmissão inexistente**: a falha relatada é
de **descoberta, apresentação e acessibilidade do comando**, possivelmente
causada por vários botões concorrendo por espaço no `AlertDialog`.

**Ajustes necessários:**

- No popup de detalhes aberto a partir de **Estações** ou do **mapa**, incluir
  botão primário, destacado e inequivocamente visível **"Enviar mensagem"**,
  com ícone de mensagem (por exemplo, balão). Preferir linha de ação com
  largura útil ou área de ações organizada verticalmente; não comprimir a
  ação em uma linha de cinco botões.
- Ao tocar, fechar o popup e abrir diretamente **Mensagens → conversa privada**
  com o **indicativo completo, inclusive SSID**, da estação selecionada já
  preenchido como destinatário. Permitir digitar e enviar usando o fluxo
  APRS existente; preservar o histórico com aquela estação.
- A simples abertura da conversa **não pode enviar automaticamente** nenhum
  pacote por RF ou APRS-IS. O TX só deve acontecer quando o usuário tocar
  em Enviar, observando configuração, conexão e restrições de transmissão.
- Não confundir com **CQ**, boletins ou mensagens gerais; o destino da
  mensagem deve ser a estação selecionada, nunca o próprio indicativo.
- **Teste de aceitação (T17):** selecionar estação na aba Estações e no mapa,
  abrir detalhes, conferir botão em aparelho pequeno e grande (inclusive
  fontes ampliadas), tocar e verificar destinatário exato e histórico;
  testar indicativos com e sem SSID, cancelar/voltar e abrir nova estação,
  certificar que nada foi transmitido antes de Enviar e comprovar envio via
  APRS-IS/TNC quando configurados e autorizados. Conferir todos os idiomas.
- **Status:** item aberto para correção de interface e homologação em
  celular. Não considerar resolvido apenas porque o callback já existe.

### 20. Mensagens: responder mantendo uma mensagem pressionada
_Solicitado em 31/10/2026, observação na versão v0.1.3._

**Diagnóstico no código:** o composable `Bubble(m: ChatMessage, ...)` em
`app/src/main/java/com/pt2vhf/aprs/ui/Screens.kt` mostra as mensagens,
mas não implementa `onLongClick` ou menu de contexto para responder.

- Permitir **pressionar e segurar** qualquer bolha de mensagem recebida em
  **conversa privada**, **canal geral (CQ)** ou **boletim**, abrindo menu
  acessível com ação explícita **Responder**, sem disparar envio imediatamente.
  Para mensagens próprias, não apresentar "Responder ao próprio indicativo"
  como se fosse uma mensagem recebida.
- Em conversa privada, **Responder** abre/focaliza o compositor daquela
  conversa, com destinatário definido como o **indicativo exato do remetente,
  incluindo SSID**. Para mensagem recebida em CQ/boletim, oferecer
  **resposta privada ao remetente** como caminho padrão e não confundir
  resposta dirigida com transmissão pública a CQ/BLN0.
- Mostrar no compositor um **contexto visual** da mensagem selecionada
  (remetente e trecho do texto original), com botão **Cancelar resposta**;
  preservar o texto que o usuário já estava digitando. A UI pode guardar
  localmente a associação da resposta ao `ChatMessage.id`, mas **APRS
  convencional não transporta encadeamento de respostas como o WhatsApp**:
  não prometer threading remoto. Não incluir citação longa automaticamente
  no payload e respeitar o limite do formato (até 67 caracteres de texto
  por mensagem no fluxo atual); se houver opção de citação breve, exigir
  pré-visualização explícita e dimensionamento correto.
- Depois de tocar em **Enviar**, usar o mecanismo existente de envio
  APRS-IS/TNC, ACK, status e histórico; não duplicar pacotes pela ação de
  pressionar, selecionar ou cancelar.
- Garantir `combinedClickable`/gesto equivalente de longa duração,
  acessibilidade, feedback tátil quando disponível, funcionamento com
  seleção de texto/rolagem e ausência de acionamentos acidentais.
- **Teste de aceitação T18:** manter pressionada mensagem recebida em
  conversa privada, CQ e boletim; verificar menu, destinatário, contexto,
  cancelar, texto pré-existente e envio único; conferir diferentes SSIDs,
  mensagens próprias, telas pequenas, fontes grandes e modo sem conexão.
  Comprovar que não há TX até o comando Enviar.
- **Status:** backlog apenas; ainda não implementado nem homologado.

### 21. Mensagens: pesquisa de indicativos para iniciar conversa direta
_Solicitado em 31/10/2026, observação na versão v0.1.3._

**Diagnóstico no código:** em `MessagesScreen`, a caixa de filtro existente
filtra **mensagens/conversas já registradas**. O campo `newCall` exige
`CALLSIGN_RE.matches(newCall)` para abrir uma conversa, sem lista de
estações pesquisáveis nem autocomplete. Logo, digitar parte do indicativo
não permite localizar ou selecionar uma estação conhecida.

- Na aba **Mensagens → Conversas**, destacar entrada **"Nova mensagem"**
  com campo **"Pesquisar indicativo ou parte dele"**, que apresente uma
  lista de resultados atualizada enquanto o usuário digita, sem exigir
  correspondência exata ou pressionar Enter.
- Pesquisar inicialmente nos **indicativos presentes em
  `AprsRepository.stations` (incluindo cache local)** e nos
  **participantes do histórico de conversas**, mesmo que não tenham
  posição. Permitir buscar pelo indicativo completo ou **substring**
  (ex.: `PT2`, `VHF`, `-9`), case-insensitive; eliminar duplicidades,
  mostrar indicativo completo/SSID, último contato e origem APRS-IS/RF
  somente quando conhecidos. Não anunciar cobertura de **diretório global
  APRS** nem inventar resultados de estações não observadas.
- Ao selecionar um resultado, abrir diretamente a **conversa privada**
  com o indicativo exato selecionado, inclusive SSID, histórico existente
  e campo pronto para digitar. Nunca escolher automaticamente `-0`
  no lugar de `-9`, nem trocar destinatário depois da seleção.
- Manter caminho alternativo **"Digitar indicativo manualmente"**,
  validado com `CALLSIGN_RE`, para contatar estações ainda não observadas,
  com mensagem clara quando a pesquisa não encontrar resultados.
- A pesquisa pode funcionar **offline** sobre o cache; resultados devem
  reagir às novas estações recebidas. Abrir conversa ou selecionar
  estação **não transmite**; só a confirmação Enviar aciona APRS-IS/TNC.
- Ao criar conversa nova, não confundir a busca de destinatário com o
  filtro do **conteúdo das conversas**; são funções distintas, com
  indicadores e rótulos claros. A busca não exige a estação ter enviado
  uma mensagem anteriormente.
- **Teste de aceitação T19:** procurar indicativo parcial e completo,
  case-insensitive, com diferentes SSIDs; escolher estação com/sem
  conversa anterior, recebida apenas por APRS-IS ou TNC, e estação
  conhecida apenas pelo histórico; verificar ausência de duplicação,
  correspondência exata após seleção, cache offline, nenhum TX antes
  de Enviar, fallback manual, campos vazios e resultado sem coincidências.
- **Status:** backlog apenas; ainda não implementado nem homologado.

### 22. Mapa: controle de brilho da cartografia
_Registrado em 31/10/2026. Solicitação: "Backlog: ajuste de brilho do mapa"._

**Estado atual:** a tela `MapScreen` usa `osmdroid MapView` com tiles
`TileSourceFactory.MAPNIK`, sem ajuste próprio de luminosidade.
Não alterar o brilho global do Android nem requerer permissão de
modificação das configurações do sistema.

- Disponibilizar na tela Mapa um ícone de **sol/brilho** próximo de
  "Minha localização", discreto, acessível e sem obstruir controles
  ou marcadores. Ao tocar, abrir controle deslizante de **50% a 150%**,
  com valor **100% (normal)** e ação **Restaurar padrão**; permitir
  ajuste fino e mostrar porcentagem exata.
- Aplicar mudança **em tempo real somente à camada cartográfica de
  fundo** (tiles OSM ou outras fontes, quando suportadas), com
  escurecimento abaixo de 100% e clareamento acima de 100%. Ajustar
  renderização/cor do tile ou um tratamento equivalente **abaixo
  das sobreposições APRS**; não desbotar nem reduzir legibilidade dos
  ícones de estações, indicativos, rastros, links RF, popups ou botões.
  Não modificar o estilo do mapa sem relação com a luminosidade.
- **Persistência:** guardar o percentual em `Settings`/preferências
  locais; reaplicar ao trocar de aba, reiniciar o aplicativo ou recriar
  a `Activity`. Usar 100% em instalações novas; carregar valores
  anteriores válidos e tratar preferências corrompidas sem travamentos.
  O controle não deve interferir no centro e zoom memorizados (item
  #17), nem na opção de seguir estação.
- Compatibilidade com dispositivos modestos e funcionamento
  offline/tiles em cache. Evitar tratamento caro por frame e
  alteração permanente da imagem armazenada em cache. Se a fonte
  cartográfica limitar realce acima de 100%, avaliar tratamento
  apropriado e explicar limitações, sem simular aumento do brilho
  físico da tela.
- **Teste de aceite T22:** abrir mapa em 100%, ajustar 50%, 75%,
  125% e 150%, verificar diferença visual real e responsiva apenas
  no fundo cartográfico; confirmar marcadores APRS, linhas RF,
  tracklogs, controles e textos com contraste íntegro; acionar
  "Minha localização" e mover/alterar zoom; trocar de aba e reabrir
  o aplicativo, confirmando persistência; restaurar 100%, testar
  rotação, modo claro/escuro do sistema, cache offline, aparelho
  de tela pequena e fonte ampliada. Comprovar que o brilho do
  dispositivo permanece inalterado.
- **Status:** backlog registrado, **ainda não implementado**. Não
  gerar novo APK por este registro.

### 23. Mensagens privadas e gerais: ordem cronológica de recepção
_Solicitado em 31/10/2026, relatado na v0.1.3._

**Objetivo:** na visualização das **mensagens individuais** de uma conversa
privada e da aba **Geral (CQ)**, organizar todas as bolhas em **ordem de
recebimento/registro cronológico**, da mais antiga no topo até a mais
recente embaixo, como em uma linha do tempo. Vale para mensagens
recebidas e enviadas, mantendo a conversa coerente.

**Diagnóstico técnico:** `ui/Screens.kt`, função `Conversation()`,
renderiza `messages.filter { ... }` diretamente, sem
`sortedWith` explícito. `GroupChat()` recebe uma lista ordenada
por `newestFirst`, opção compartilhada com a lista de Conversas;
assim o usuário pode ver a sequência invertida na aba Geral, e
uma reidratação/atualização de histórico fora de sequência pode
aparecer em posição errada. `ChatMessage.time` registra o timestamp
local do evento de entrada/saída, e `ChatMessage.id` permite desempate
estável; não assumir que a ordem física na lista é sempre confiável.

**Requisitos:**

- Em **Mensagens → Conversas → conversa particular**, ordenar as bolhas
  por `(time asc, id asc)`, respeitando a cronologia de mensagens
  recebidas e enviadas; manter o destinatário/SSID correto.
- Em **Mensagens → Geral**, exibir mensagens CQ de todos os remetentes
  em uma única sequência cronológica ascendente, sem agrupá-las ou
  reordená-las arbitrariamente por estação, mantendo identificação do
  remetente e horário em cada bolha. Mensagens transmitidas ao grupo
  devem integrar a mesma linha do tempo.
- A ordem usa **horário local de registro/recepção**, não horário
  alegado no conteúdo ou texto do pacote; timestamps inválidos e
  empates precisam de ordenação determinística sem descartar mensagens.
  Não transformar ACK, alteração de status ou atualização de UI em
  uma nova recepção capaz de mover a bolha de lugar.
- Com nova mensagem enquanto a tela está aberta, inserir no ponto
  correto **automaticamente**. Rolar para a última somente se o
  usuário já estiver no final; se estiver lendo mensagens anteriores,
  preservar a posição de leitura e oferecer indicação de novas mensagens
  ou ação para ir ao fim. Ao abrir a conversa, mostrar o histórico
  recente por padrão.
- **Separar três regras de apresentação:** (a) a **lista de Conversas**
  permanece ordenada da **última atividade mais recente para a mais
  antiga** (item #2); (b) **dentro de cada conversa**, mensagens
  aparecem da mais antiga para a mais recente; (c) na aba **Geral**,
  usar igualmente a cronologia crescente. O controle de classificação
  do item #2 não pode inverter inadvertidamente as bolhas internas.
- Manter o comportamento correto com filtros de conteúdo, histórico
  restaurado, alteração de conexão APRS-IS/TNC, mensagens duplicadas,
  confirmação ACK e timestamps iguais; **nenhum envio automático**.
- **Critério de aceite T23:** receber/enviar mensagens de diferentes
  contatos e no canal CQ, intencionalmente fora de sequência no
  carregamento do histórico, validar `time/id` ascendente nas
  conversas e na aba Geral, checar que a lista de contatos continua
  com mais recentes no topo; testar novas mensagens com a tela
  aberta, desempates, ACK, rolagem quando o usuário está no fim ou
  lendo mensagens antigas, reinício e cache local. Conferir mensagens
  com e sem SSID e origem RF/APRS-IS.
- **Status:** registrado para próxima implementação; **não corrigido
  nem homologado nesta solicitação**.

## Consolidação das sugestões aprovadas com itens já existentes

A comparação com o APRSdroid também aprovou os recursos abaixo, que **já estavam
no backlog** e permanecem com o mesmo número (não criar tickets duplicados):

- **Item 6:** SmartBeaconing com parâmetros editáveis, beacon periódico e
  posição manual/fixa, configuração de precisão/consumo do GPS, ambiguidade
  de posição e controle de transmissão de rumo/velocidade/altitude,
  frequência de voz no beacon, AFSK por áudio, opções de notificações e
  log detalhado de conexão.
- **Item 5:** tracklog e estilo configurável; o seguimento de outras estações
  passa a ter requisitos próprios no item 11.
- **Item 4:** visualização e distinção dos meus enlaces RF comprovados; a
  análise de alcance e recordes recebe detalhamento próprio no item 14.
- **Pendências herdadas da v0.1.0:** TNC por USB-OTG, KISS TCP, mapas offline,
  mapa de calor de cobertura e modos de operação móvel. AFSK exige testes em
  dispositivos reais, incluindo áudio e políticas de transmissão.
- **Itens 7, 8 e 9:** persistência, indicadores de conexão e conexão APRS-IS
  automática seguem como pré-requisitos fundamentais.

## Sequenciamento sugerido — aprovado como direção de produto

As faixas são **metas de planejamento**, não versões publicadas nem garantias
de prazo. Não implementar nem gerar APK até a solicitação de nova versão;
ajustar escopo conforme testes e dependências e não declarar algo entregue
sem evidência.

- **v0.2 (fundamentos):** persistência local, ícones APRS, indicadores de
  conexão, opção APRS-IS, filtros, usabilidade e internacionalização
  PT/EN/ES/FR/IT (preparada em PR específico; confirmar CI/merge antes de
  considerar concluída).
- **v0.3 (rastreamento):** perfis de operação, SmartBeaconing configurável,
  posição manual/periódica, privacidade, tracklog, seguir estação móvel.
- **v0.4 (rádio/diagnóstico):** USB-OTG, KISS TCP, diagnóstico de conexão e RF,
  estatísticas/recordes de alcance, avaliação AFSK em hardware real.
- **v0.5 (offline/integrações):** mapas offline, mapa de calor RF/histórico,
  favoritos e alertas, API de integração Android; revisar dependências para
  antecipar itens caso necessário.

**Critério transversal:** manter compatibilidade de preferências existentes,
não transmitir por RF/APRS-IS sem permissão, otimizar bateria/armazenamento,
testar com rádio/TNC reais, preservar a separação entre RF comprovado e
dados APRS-IS e, em cada release, publicar APK assinado + atualização
coordenada de www.pt2vhf.com/android (incluindo OTA/changelog/contadores).
