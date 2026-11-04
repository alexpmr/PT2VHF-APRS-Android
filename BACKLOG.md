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
O backlog permanece parcialmente implementado, com homologação física pendente. Não afirmar que os 30 itens foram concluídos. Os critérios, roteiros e evidências ficam no
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
| 19 | **P1 — FALHA VISUAL CONFIRMADA NA v0.1.4**: em detalhes da estação, ações Favoritos/Seguir/Navegar/Fechar aparecem, mas Enviar mensagem não aparece; comando está comprimido na Row do AlertDialog | Botão principal Enviar mensagem em área fixa e visível, separado das ações secundárias; conversa privada abre com indicativo/SSID exato sem TX automático; teste em aparelho real |
| 20 | **Pendente — v0.1.3**: pressão longa na mensagem com duas ações distintas | Menu **Responder à mensagem** (com contexto) ou **Mensagem direta ao remetente** (privada sem citação), SSID exato e nenhum TX automático |
| 21 | **Pendente — v0.1.3**: procurar indicativos para iniciar conversa nova | Busca por indicativo completo/parcial em estações e contatos recentes, seleção de SSID exato e abertura da conversa sem TX automático |
| 22 | **Pendente — v0.1.3**: controle de brilho do mapa inexistente | Ícone de sol com ajuste independente das configurações globais do Android, intensidade persistente, padrão neutro e teste de camadas/legibilidade |
| 23 | **Pendente — v0.1.3**: ordenar cada mensagem pessoal ou geral pelo horário de recepção | Linha do tempo cronológica (antigas acima, recentes abaixo), desempate estável e atualização reativa; independente da ordem da lista de Conversas (#2) |
| 24 | **Pendente — v0.1.3**: balões estreitos, com espaço horizontal não aproveitado | Largura responsiva até ~90% da área útil, mensagens curtas compactas, longas com quebra correta, alinhamento enviado/recebido e legibilidade de metadados |
| 25 | **P1 — FALHA REPORTADA NA v0.1.4**: APRS-IS indica conexão verificada, porém usuário não consegue enviar mensagem | Diagnosticar habilitação TX Internet/RF, serviço, chamada ao envio, retorno de aprsIs.send/tnc.send, status real e ACK; feedback claro ao usuário, sem falso sucesso, teste físico com TX autorizado |
| 26 | **Pendente — v0.1.4**: retenção de mensagens não configurável; histórico limitado a 1.000 em memória e 500 persistidas | Configurações → Mensagens → Retenção: prazo e quantidade por tipo, limpeza previsível, proteção de pendências/ACK, migração segura e testes de persistência | 
| 27 | **P2, v0.1.4** — mensagens CQ exibem caracteres estranhos | Inspecionar bytes APRS-IS/RF, reconhecer texto UTF-8 válido e texto Latin-1/CP1252 ou binário sem inventar caracteres; prévia legível e testes de regressão |
| 28 | **P2, v0.1.4** — tela de mensagens fica espremida ao digitar | Compositor fixo acima do teclado, barra superior compacta, filtros recolhíveis, navegação inferior adaptável, histórico rolável, Enviar acessível em telas pequenas |
| 29 | **P1, v0.1.4** — incluir `www.PT2VHF.com` no texto de Informação transmitido/exibido das estações PT2VHF | Preservar comentário útil e acrescentar domínio canônico sem duplicá-lo; validar no beacon bruto e no receptor; não adulterar informações recebidas de terceiros |
| 30 | **Pendente — v0.1.4**: rastreador está desligado a cada inicialização, embora o padrão de beacon seja SmartBeaconing | Ativar tracker automaticamente por padrão **após configuração/autorização**, persistir escolha manual de desligar, respeitar GPS/TX/privacidade e estado do serviço; testar reinício e background |
| 31 | **P2 — Pendente (31/10/2026):** caixa "Filtrar indicativo ou texto" ocupa altura/largura excessivas e opção "Mais antigas primeiro" ocupa espaço horizontal desnecessário | Tornar pesquisa compacta (altura mínima adequada ao toque, densidade responsiva), substituir botão textual de ordenação por ícone de filtro/ordenação; tocar abre painel/popover com busca, filtros e ordenação Mais recentes/Mais antigas, persistindo seleção, com labels acessíveis e funcionamento com teclado, telas pequenas e rotação |
| 33 | **Implementado no código da v0.1.7 QA (PR #12, CI aprovado); homologação física/visual pendente:** ajustar a apresentação compacta das estações para mostrar velocidade antes da distância, sempre à direita | Na aba **Estações**, apresentar **uma estação por linha, nesta ordem exata: (1) ícone APRS real da estação, conforme símbolo/tabela/overlay recebidos → (2) indicativo com SSID → (3) tempo relativo do último contato/recepção (há X min/h/dias) → (4) descrição/comentário APRS da estação → (5) velocidade da estação em km/h → (6) distância em km, fixada e alinhada à borda direita da lista**. Velocidade deve utilizar exclusivamente o último valor recebido de velocidade da própria estação, quando disponível e válido, exibido em **km/h** (converter do formato APRS quando necessário); se não houver velocidade conhecida, mostrar **“—”**, não presumir 0 km/h. Estação comprovadamente parada pode mostrar **0 km/h**. Alinhar velocidade na penúltima coluna e distância na coluna final, com espaços estáveis. Distância deve ter coluna final com espaço reservado e **não mudar de alinhamento** quando a descrição crescer, faltar ou variar; mostrar **“—”** se posição da estação ou localização de referência estiver ausente, sem inventar valor. **Descrição é obrigatoriamente parte dessa mesma linha**, usando o texto efetivamente recebido da estação; sem comentário, mostrar vazio/discreto, sem deslocar velocidade ou distância. Truncar a descrição primeiro com reticências em telas estreitas; preservar ícone, indicativo, tempo, velocidade e distância legíveis, com informações completas acessíveis no detalhe ao tocar; não criar segunda linha, cartão ou espaçamento entre estações. **Preservar a estrela amarela/dourada para favoritos ao lado do indicativo**, sem confundi-la com o ícone APRS. Manter linhas próximas (padding vertical de 2–4 dp, altura visual alvo próxima de 48 dp e área tocável de pelo menos 48 dp), atualização reativa do tempo/descrição/velocidade/distância ao receber pacotes, filtros/ordenação, temas e acessibilidade. Homologar telas de 320/360/412 dp, rotação e fontes ampliadas, com descrições vazias, longas e caracteres especiais. |
| 34 | **P2 — Pendente (31/10/2026):** indicadores APRS-IS e TNC do cabeçalho são apenas visuais; toque não conecta/desconecta | Transformar **chips APRS-IS e TNC em controles clicáveis e acessíveis**: toque em APRS-IS conectado/desconectado → desconectar/conectar; toque em TNC conectado/desconectado → desconectar/conectar ao dispositivo previamente selecionado; se não houver TNC configurado, abrir seleção de dispositivo Bluetooth em vez de falhar silenciosamente. Exibir estado real durante conexão/reconexão/falha e dar retorno imediato, sem afirmar conexão antes de validação (APRS-IS `verified` e TNC efetivamente conectado). **Desconexão manual deve bloquear reconexão automática/rede/serviço até nova ordem explícita**, sem mudar autorizações TX por APRS-IS/RF nem ligar o tracker; **preservar dispositivo TNC pareado/selecionado ao desconectar**. Respeitar permissão Bluetooth, evitar ações duplicadas por toques rápidos, manter cores/semântica de estado, testar perda de sinal e retorno. GPS permanece somente indicador, sem ação de toque. |
| 35 | **P2 — Pendente (31/10/2026):** campo “Buscar indicativo” da aba Estações está grande e ocupa espaço excessivo | Compactar a barra de busca **somente da aba Estações**: reduzir principalmente a altura visual, os paddings externos e internos e a largura consumida sem comprometer a digitação; preferir campo de pesquisa de uma linha com dica curta “Indicativo”, ícone de busca e ação de limpar. Manter campo e controle de filtros/ordenação na mesma linha quando houver espaço; em telas estreitas, adaptar responsivamente sem sobreposição. Preservar pesquisa parcial e por SSID, filtros combinados, teclado, estado/persistência existentes e área de toque acessível. Validar Android 320/360/412 dp, fontes ampliadas, rotação e número de estações visíveis na tela. Não confundir com campo de busca em Mensagens (item #31). |
| 36 | **P2 — Pendente (31/10/2026):** botão superior **Enviar beacon** permanece amarelo ao tocar, sem indicar envio em andamento ou falha | Ao tocar, mudar **imediatamente** a cor do botão de amarelo para cor de atividade (ex.: azul), com estado visual e acessível **“Enviando…”**; impedir toques duplicados durante a tentativa. **Voltar ao amarelo somente após o serviço retornar sucesso verificável da entrega local ao(s) transporte(s) APRS autorizado(s)** (escrita bem-sucedida no socket APRS-IS verificado e/ou envio aceito pelo TNC/KISS), correlacionando o resultado à solicitação manual específica; não tomar o incremento de contadores, o simples clique nem o sucesso de outros beacons automáticos como confirmação desta ação. Caso não haja rota TX/GPS, haja erro ou timeout, mostrar estado de falha distinto (vermelho + mensagem específica) sem simular sucesso; permitir nova tentativa. Em envio parcial APRS-IS/RF, indicar quais transportes tiveram sucesso/falha. Como beacon APRS não tem ACK obrigatório, **não afirmar recepção por terceiros ou transmissão RF comprovada no ar**; informar exatamente o nível de confirmação disponível. Preservar botão amarelo como estado de repouso, configurações TX, tracker e envio por ambos os canais, sem beacons extras; testar conexão/desconexão no meio do TX, latência, clique rápido, falha e sessão não verificada. |
| 37 | **P0 — Correção candidata integrada na v0.1.7 QA (PR #12, CI aprovado); confirmação em APRS-IS/TNC reais pendente:** ao estar conectado e autenticado no APRS-IS, **qualquer tentativa de TX (mensagem ou beacon manual/automático) derruba a conexão APRS-IS** | **Corrigir antes de nova release.** Reproduzir com o TNC desligado e depois com TNC ativo; capturar sequência de estado APRS-IS, `onState`, motivo de fechamento do socket, login `logresp`, escrita/flush TX, endereço/porta, temporizadores e eventos de rede, sem expor passcode ou dados sensíveis. Inspecionar especialmente `Connections.kt` → `AprsIsClient.send()` (se `sent=false`, chama `reconnectNow()` e fecha o socket), `start()/stop()/reconnectNow()` (concorrência, época da sessão e sincronização de escrita/fechamento), `soTimeout=120_000` e `AprsService.kt` (autoconexão, filtro no primeiro GPS fix e `NetworkCallback`), além de possível encerramento pelo servidor por pacote TNC2 inválido. **Estas são hipóteses, não causas confirmadas.** Preservar a sessão APRS-IS após envios válidos, sem forçar reconexão a cada TX; quando houver erro genuíno, registrar diagnóstico útil, sinalizar desconectado e reconectar conforme política, sem perda silenciosa nem duplicação de mensagens/beacons. Validar 10 beacons e 10 mensagens consecutivos, intervalados e em sequência rápida, retorno/ACK quando aplicável, tráfego RX contínuo, alternância Wi-Fi/dados, modo apenas APRS-IS e APRS-IS+TNC, foreground/background, relógio de 120 s, server logresp e manutenção do estado conectado após o TX. Não marcar resolvido com base apenas em testes unitários: exigir teste real contra servidor APRS-IS e dispositivo Android. |


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

**Complemento solicitado em 31/10/2026 — campo Informação:** manter o comentário
descritivo enviado pela aplicação e **acrescentar** `www.PT2VHF.com`
ao texto APRS de posição, para que o campo **Informação** de outros clientes
exiba, por exemplo, `PT2VHF APRS Client www.PT2VHF.com` (respeitando o
limite real do comentário). Não substituir toda a informação apenas pelo
domínio, nem adicionar o endereço ficticiamente ao comentário recebido de
uma estação de terceiros. Ver item #29 e teste T29.

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

### 19. Aba Estações: envio direto de mensagem à estação selecionada
_Solicitado em 31/10/2026; falha persistente e ilustrada por captura do usuário na v0.1.4._

**Falha funcional observada em aparelho real (v0.1.4):** após selecionar uma estação
na aba **Estações** e abrir seus detalhes, **não é possível enviar
uma mensagem àquela estação**. Tratar como **defeito prioritário P1 de
fluxo de interação**, e não apenas como sugestão estética. Ainda não há
reprodução instrumentada nem causa-raiz comprovada.

**Evidência visual nova:** a captura da estação PT2VHF (tela Android) apresenta apenas **Remover favorito**, **Seguir estação**, **Navegar até** e **Fechar**; a ação **Enviar mensagem não aparece**, mesmo havendo espaço vazio na parte inferior do diálogo. Isso corrobora a hipótese de limitação dos botões pela Row/AlertDialog; não significa que o callback funcione ao toque.\n\n**Evidência de código:** `StationDialog()` em `ui/Screens.kt` possui
`TextButton(onClick = onMessage)` com rótulo `Mensagem`, no mesmo
`Row` horizontal de Favoritos e Seguir, e o `AlertDialog` inclui
também outros botões de Navegar/Fechar. `App.kt` passa o callback que
define `selectedStation = null; chatWith = call; tab = Tab.MESSAGES`.
O compositor de `Conversation()` usa `contact` para endereçar a
mensagem. **Esse encadeamento nominal no código não comprova
funcionamento no aparelho:** investigar visibilidade/corte do botão,
comportamento do AlertDialog/Row, eventos de toque, navegação e
estado da conversa; não afirmar que o problema foi corrigido sem teste.

**Correção exigida:**

- Em **Estações → selecionar uma estação → detalhes**, oferecer uma
  ação primária **Enviar mensagem**, destacada, com ícone e área de
  toque adequada, totalmente visível mesmo em tela pequena, zoom
  de fonte e orientações diferentes. **Posicionar na área fixa inferior do diálogo, em linha exclusiva e acima/ao lado de Fechar, independente da largura das outras ações.** Não posicionar junto a botões
  que possam comprimi-la ou ocultá-la. Repetir o comportamento a
  partir de **Mapa → estação → detalhes**.
- Tocar no botão deve fechar os detalhes e abrir **Mensagens →
  conversa privada** com `chatWith` igual ao **indicativo exato
  selecionado, incluindo SSID**. Apresentar destinatário no cabeçalho,
  histórico correto e campo de digitação + botão **Enviar** realmente
  utilizáveis. Não abrir CQ nem boletins; não alterar o destinatário.
- O simples toque em Enviar mensagem (nos detalhes) **não transmite
  nada**. Somente o botão Enviar do compositor, mediante ação
  explícita, usa o fluxo de APRS-IS ou TNC/RF permitido. Distinguir
  falhas de interface de falhas de transporte, permissões ou de
  conexão; quando o envio não for possível, mostrar uma indicação
  compreensível do motivo, sem falsa confirmação de entrega.
- **T17 (obrigatório):** testar com indicativos reais e simulados,
  incluindo SSIDs distintos, via aba Estações e via Mapa, em
  aparelho pequeno e com fontes ampliadas. Confirmar que o botão
  é visível, abre a conversa correta e permite digitar/enviar
  quando a conexão APRS-IS/TNC está verificada e TX autorizado.
  Verificar envio no log TX e recepção por estação independente,
  quando viável; conferir offline/desconectado, somente RX,
  retorno/fechamento do diálogo, múltiplas estações consecutivas
  e ausência de TX antes do toque em Enviar. Registrar evidência
  visual e de tráfego.
- **Status:** FALHA FUNCIONAL ABERTA, P1, agora comprovada por captura da v0.1.4; ainda não corrigida,
  não homologada e sem novo APK. Não baixar pelo mero fato de existir
  `onMessage` no código.

### 20. Mensagens: pressão prolongada — responder à mensagem ou diretamente ao remetente
_Solicitado em 31/10/2026; detalhado novamente pelo usuário na v0.1.3._

**Diagnóstico:** `Bubble(m: ChatMessage, ...)` em `ui/Screens.kt`
não implementa ação de pressão longa. O usuário quer **duas opções
distintas**, e não apenas um botão genérico de responder.

- **Gesto:** ao **manter pressionado um balão recebido** em conversa
  privada, mensagens gerais (CQ) ou boletins, apresentar um menu de
  contexto com duas ações visíveis:
  1. **Responder à mensagem** — abrir/focalizar o compositor com uma
     faixa de contexto indicando **remetente + trecho da mensagem
     selecionada**, mantendo associação local ao `ChatMessage.id`,
     botão de cancelar e preservação de texto já digitado. Quando a
     mensagem for de uma conversa privada, responder naquele
     contato; em **CQ**, indicar explicitamente se a resposta irá ao
     próprio canal (pública) ou oferecer seleção de destino antes do
     envio, evitando broadcast inesperado. Em boletins, não enviar
     silenciosamente ao endereço `BLN0`: a resposta dirigida ao
     remetente é a opção segura.
  2. **Mensagem direta ao remetente** — abrir **conversa privada**
     com o `from` completo, inclusive **SSID**, sem citação/contexto
     associado, preservando o histórico e permitindo escrever uma
     mensagem nova para aquela estação. Deve funcionar também a
     partir de mensagens recebidas no canal Geral/CQ ou boletins.
- **Diferença funcional:** a primeira opção é uma resposta
  **contextual à bolha escolhida**; a segunda inicia/abre uma
  **conversa privada comum com o autor**, sem responder a um texto
  específico. Em qualquer caso, mostrar o destinatário efetivo
  antes do envio e impedir que o usuário responda ao próprio
  indicativo por engano.
- **Limitação APRS:** o contexto da primeira opção é uma associação
  visual/local, porque o protocolo APRS clássico **não disponibiliza
  threading remoto nativo**. Não inserir automaticamente a mensagem
  inteira no pacote; se houver citação curta, mostrar prévia e
  respeitar os limites de payload da transmissão (texto de até
  67 caracteres no fluxo atual), sem truncamento silencioso.
- **Proteções:** pressionar, abrir menu, selecionar opção ou cancelar
  **nunca transmite**; somente o botão **Enviar** dispara o fluxo
  existente de APRS-IS ou TNC/RF conforme permissões. Não duplicar
  mensagens, alterar indevidamente ACK/status nem perder rascunhos.
  Garantir acessibilidade, textos traduzidos e menu funcional em
  telas pequenas, com fonte ampliada e rolagem.
- **Teste de aceitação T18:** pressionar mensagem recebida em
  conversas privadas, CQ e boletins; verificar **ambas as opções**,
  contexto presente apenas na primeira, destino correto inclusive
  SSID, diferenciação visual de resposta pública e privada,
  cancelamento/rascunho, respostas a mensagens próprias,
  histórico conservado, envio único somente após confirmação,
  conexão offline e transmissão permitida APRS-IS/RF.
- **Situação:** documentado no backlog, **ainda não implementado**.

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

### 24. Mensagens: aproveitar melhor a largura disponível dos balões
_Solicitado em 31/10/2026, observado na v0.1.3._

**Problema identificado no código:** `ui/Screens.kt`, composable
`Bubble()`, limita cada balão a `Modifier.widthIn(max = 300.dp)`.
Em celulares com telas mais largas e, especialmente, na orientação
paisagem/tablet, isso cria quebras de linha desnecessárias e deixa
grande faixa lateral vazia. O contêiner já ocupa a largura da tela,
alinhando as mensagens próprias à direita e as recebidas à esquerda.

**Requisito de interface:**

- Substituir teto fixo de `300.dp` por largura **responsiva** relativa
  à área útil da conversa, com limite aproximado de **88–92%** da
  largura disponível e margens laterais consistentes. Respeitar
  `WindowInsets`, telas pequenas, recortes/notch, orientação
  retrato/paisagem e fontes ampliadas.
- Manter **balões de mensagens curtas compactos**, expandindo apenas
  quando o conteúdo exigir (não esticar todos os balões a 90%).
  Mensagens mais longas devem usar melhor a área horizontal para
  reduzir a quantidade de linhas, sem corte ou overflow.
- Preservar os dois alinhamentos: **mensagens recebidas à esquerda**
  e **enviadas à direita**, com cores e cantos originais e
  separação perceptível entre os interlocutores. Em **Geral/CQ**,
  boletins e conversas privadas, o mesmo comportamento deve valer.
- Quebrar textos de modo natural, incluindo URLs e sequências
  longas/sem espaços; preservar espaços legíveis e emojis Unicode,
  sem adicionar rolagem horizontal. Não sobrepor **horário, status,
  ACK, indicativo do remetente** nem controles de resposta por
  pressão prolongada (item #20).
- Evitar layouts com dimensões rígidas ou custo elevado de medição a
  cada mensagem; não alterar histórico, conteúdo transmitido,
  ordenação cronológica (item #23) ou regras APRS.
- **Teste de aceitação T24:** comparar telas de 320/360/412 dp,
  paisagem e tablet; enviar mensagens curtas, longas, com URL,
  emojis e palavras sem espaços; conferir aproveitamento da
  largura disponível, alinhamento correto, leitura integral,
  ausência de texto cortado, metadados visíveis e interação de
  resposta. Repetir com fontes grandes e aba Geral, boletins,
  conversa privada, teclado aberto e lista rolando.
- **Status:** especificação registrada, implementação e teste
  físico pendentes. Nenhuma versão gerada nesta solicitação.

### 25. Mensagens: APRS-IS conectado/verificado, mas não consegue transmitir
_Solicitado em 31/10/2026; usuário forneceu captura relatando que está conectado e não consegue enviar no Android v0.1.4._

**Severidade P1 — falha funcional reportada, causa raiz ainda não identificada.**
Investigar sem confundir **não haver botão Enviar mensagem nos detalhes
(item #19)** com **abrir a conversa e o comando Enviar falhar**.
O teste tem de reproduzir os dois caminhos; conexão de recepção não
comprova autorização e sucesso de TX.

**Diagnóstico inicial do código atual:**

- Cabeçalho em `ui/App.kt` mostra `Chip("APRS-IS", status.aprsIsVerified)`,
  ou seja, verde significa sessão verificada, **não que TX Internet
  esteja autorizado**.
- `data/Settings.kt` tem `txViaInternet` e `txViaRf` (ambos true
  por padrão, mas persistidos pelo usuário). `AprsService.transmit()`
  só chama `aprsIs.send(...)` quando `txViaInternet` **e**
  `status.aprsIsVerified`; se TX estiver desligado, pode retornar
  `null`, mesmo com APRS-IS verde. TNC segue condições próprias.
- `Composer()` em `ui/Screens.kt` limpa o texto depois de chamar
  `AprsService.send()` **antes de comprovar TX efetivo**.
  `sendMessage()` cria o item local como "Enviando" e
  `sendWithRetry()` marca "Sem conexão para enviar" quando
  `transmit()` retorna null, mas essa condição não fornece
  um **motivo claro** (TX Internet desabilitado, sessão, RF,
  serviço indisponível ou falha de socket). Falta retorno confiável
  e visível de sucesso/falha na própria tela.
- `AprsIsClient.send` pode falhar ou retornar false; é necessário
  instrumentar retorno, erro, estado/identidade da sessão e logs
  sem expor passcode, coordenadas privadas ou dados sensíveis.
  **Sem ACK** não é o mesmo que **sem envio**: distinguir pacote
  entregue à rede APRS-IS, confirmação do destinatário e
  indisponibilidade do meio.

**Correção exigida:**

- No compositor, expor status **"APRS-IS verificado"** e
  **"Transmissão via APRS-IS habilitada/desabilitada"** separadamente,
  com feedback de RF/TNC. Se não houver meio autorizado e apto,
  bloquear envio com motivo específico e atalho para Configurações.
- Ao pressionar Enviar, validar o destinatário (inclusive SSID),
  passcode/sessão, TX permitido, conexão, serviço e transporte
  selecionado, relatando a causa sem limpar o texto do rascunho
  quando não houver submissão real.
- Registrar no histórico de forma coerente: **não enviado**,
  **transmitido via APRS-IS**, **transmitido via RF**, **aguardando ACK**,
  **ACK recebido**, **sem ACK** ou **falhou**, de acordo com
  evidência; não apresentar "Enviada" ou sucesso quando a API
  de transporte não confirmou a transmissão.
- Preservar os limites APRS de 9 caracteres para o destinatário,
  partes de até 67 caracteres na mensagem, retry e deduplicação,
  sem TX indevido. Nada deve ser transmitido ao apenas abrir a tela.
- **T25 (obrigatório):** sessão APRS-IS verificada verde com TX
  Internet ativo e desativado; conectada mas não verificada;
  sessão desconectada; TNC ligado/desligado; RF-only e Internet-only;
  ao menos um teste de envio para outro indicativo com SSID,
  confirmando log TX bruto/recepção independente quando autorizado.
  Testar envio com o aplicativo em foreground, restauração
  pós-interrupção do serviço, falha de socket, timeout/ACK,
  erro visível e **rascunho preservado** na falha.
  Comparar usar botão Enviar nas Conversas e envio iniciado a
  partir de Estações/Mapa (item #19). Documentar as configurações
  de TX e os estados de conexão no momento da falha.
- **Status:** backlog P1 aberto para diagnóstico e correção
  física; **não resolvido apenas por APK compilado**.

### 26. Configurações: retenção e limpeza de mensagens
_Solicitado em 31/10/2026: "Backlog: configurações, retenção das mensagens"._

**Diagnóstico confirmado no código:** em
`data/AprsRepository.kt`, `add()` executa `takeLast(1000)`
na memória e `save()` grava **somente as últimas 500 mensagens**
no arquivo `messages.json`. Portanto, mesmo sem controle visível,
mensagens antigas já são descartadas na operação e ao reiniciar.
`data/Settings.kt` não oferece preferências de prazo/quantidade.
**Não prometer recuperação de mensagens já descartadas**.

**Comportamento desejado:**

- Criar seção **Configurações → Mensagens → Retenção de histórico**,
  com parâmetros independentes, aplicados a **conversas privadas**,
  **mensagens gerais/CQ** e **boletins**. Oferecer um valor **Igual
  para todos** e permitir personalização por categoria.
- **Prazo de retenção:** 7, 30, 90, 180 ou 365 dias e **Sem prazo
  de expiração**; usar o momento de recebimento/envio registrado,
  com tratamento consistente de fuso horário, mudança de relógio
  e timestamps inválidos. **Padrão para instalações existentes:
  sem exclusão automática por idade**, até escolha explícita do
  usuário, preservando dados já retidos.
- **Quantidade máxima de mensagens:** opções claras como 500,
  1.000, 5.000 ou **sem limite por quantidade**, especificando
  que a limpeza pode ser por **prazo e/ou quantidade**, com
  critérios previsíveis (o que vencer primeiro). Exibir estimativa
  de mensagens armazenadas e espaço ocupado, com alerta quando
  o histórico ficar muito grande.
- **Persistência:** remover os limites rígidos atuais `takeLast(1000)`
  em memória / `takeLast(500)` ao salvar, substituindo por
  política configurável e armazenamento escalável (preferir SQLite
  com consultas indexadas por data, contato e categoria, para
  evitar reescrever todo `messages.json` a cada pacote).
  Migrar de `messages.json` sem perder nem duplicar o histórico
  ainda existente, preservando `id`, horários, indicativos/SSID,
  via RF/APRS-IS, status e identificadores de mensagem/ACK;
  fazer backup/rollback seguro durante a migração.
- **Segurança da limpeza:** nunca remover automaticamente mensagens
  **ainda em envio, retry ou aguardando ACK**; proteger também as
  mensagens cuja confirmação acabou de chegar até sua gravação
  persistente. Mensagens finalizadas podem ser descartadas apenas
  conforme a política. Não alterar lógica de TX, deduplicação,
  ordem cronológica (item #23) ou contagem de conversas (item #2).
- **Controles explícitos:** mostrar data prevista da próxima
  limpeza e quantidade estimada que seria excluída; oferecer
  **Limpar agora** com prévia e confirmação (separada da limpeza
  automática), possibilidade de excluir um período ou categoria,
  sem apagar inadvertidamente estações, configurações, tracklogs
  ou mensagens pendentes. Mudanças de preferência que causariam
  exclusão imediata precisam ser confirmadas antes da aplicação.
- **Execução eficiente:** avaliar retenção ao carregar, ao
  persistir novos registros e ocasionalmente com aplicativo
  ativo, sem serviço periódico energético desnecessário; preservar
  desempenho e evitar corrupção com escrita atômica/transações.
  Registrar contagens de remoção e erros localmente, sem enviar
  conteúdo de mensagens a servidores externos.
- **T26 — Teste de aceitação:** migração de instalação com
  `messages.json` legado de até 500 entradas; retenção distinta
  entre privado/CQ/boletim; prazos 7/30/90/180/365/indefinido;
  tetos 500/1.000/5.000/ilimitado; períodos-limite, fuso,
  reinício, grande volume de mensagens, saída forçada durante
  gravação, ACK/retry pendente, preview/confirmar/cancelar limpeza,
  contagem e espaço em disco. Comprovar preservação de mensagens
  ainda existentes e que **nenhuma transmissão é provocada pela
  limpeza**. Explicitar dados antigos não recuperáveis.
- **Status:** registro no backlog; **não implementado nesta
  solicitação** e **sem novo APK**.

### 27. Mensagens: caracteres corrompidos e charset no APRS-IS
_Solicitado em 31/10/2026; captura real da aba Mensagens → Geral/CQ na v0.1.4._

**Evidência:** mensagens recebidas por APRS-IS, incluindo `CE4JWI-10`
e `XR4MAUI-10`, apresentam sequências estranhas misturadas ao trecho
"HALLOWEEN". É possível haver UTF-8 interpretado como Latin-1,
caracteres de controle ou conteúdo não textual na origem; **não é
possível afirmar a causa exata somente pela captura**.

- No Android, `service/Connections.kt` usa
  `InputStreamReader(..., Charsets.ISO_8859_1)` para todas as linhas
  APRS-IS e `core/Kiss.kt` converte AX.25 para String Latin-1.
  Inspecionar payload **bruto antes de qualquer decodificação**,
  inclusive hex local de pacote de teste, sem divulgar dados sensíveis.
- Implementar decodificação consciente de protocolo e meio: reconhecer
  **sequências UTF-8 válidas** quando realmente presentes; manter
  caracteres Latin-1/CP1252 quando aplicáveis; sinalizar bytes
  não imprimíveis/indecodificáveis de forma segura, sem remover a
  informação original nem inventar acentos. Não transformar
  indiscriminadamente todo o tráfego APRS em UTF-8.
- Separar **texto apresentado na interface** de bytes originais,
  mantendo origem, indicativo, horário, via APRS-IS/RF e payload
  inspecionável para diagnóstico. Não alterar IDs, ACK, posição,
  campos de controle ou dados binários/telemetria.
- Evitar que caracteres de controle corrompam quebras de linha,
  desalinhem os balões, acionem links inesperados ou prejudiquem
  buscas. Se o texto transmitido originalmente for inválido,
  explicar que a conversão não recupera conteúdo inexistente.
- **T27:** reproduzir mensagens ASCII, UTF-8 com acentos e emojis,
  ISO-8859-1, CP1252, bytes inválidos, controles e dados não
  textuais; comparar byte a byte com fonte independente via
  APRS-IS/RF, conferir tela Geral/CQ, conversa privada, histórico
  salvo/reaberto, filtros, ACK e nenhum impacto em TX.
- **Status:** backlog registrado; correção e homologação pendentes.

### 28. Mensagens: otimizar o espaço da tela com teclado aberto
_Solicitado em 31/10/2026; relato com captura de interface comprimida ao enviar mensagens._

**Diagnóstico do layout atual:** `App.kt` mantém `TopBar()`
(cabeçalho de duas linhas, Enviar beacon, Configurações e chips),
além da `NavigationBar`; `Screens.kt` mantém campo de busca,
botão de ordenação e `TabRow` acima das mensagens.
O `Composer()` usa um campo e botão na mesma linha com
`imePadding()`. Com `windowSoftInputMode="adjustResize"`,
o teclado reduz a área disponível, comprimindo a conversa.

- Reorganizar a aba Mensagens em modo **edição/teclado aberto**:
  reduzir/recolher o cabeçalho global a uma barra compacta e ocultar
  ou recolher os controles secundários (busca, filtros e ordenação)
  **sem apagar seus valores**. Preservar identificação do contato
  ou categoria (Geral/CQ, Boletins, conversa privada).
- Considerar **ocultar a navegação inferior somente enquanto o
  teclado estiver aberto**, garantindo retorno ao modo normal ao
  fechá-lo; evitar múltiplos `imePadding` que criem espaços vazios
  ou façam o compositor subir excessivamente.
- Deixar **campo de texto e botão Enviar sempre acessíveis acima do
  teclado** (altura mínima de toque, contraste e visualização do
  conteúdo digitado), sem sobreposição de teclado/navegação.
  Campo pode crescer até 3–5 linhas com limite definido, com
  rolagem interna para textos maiores. Em largura reduzida,
  dimensionar ou reposicionar o botão sem esmagar o campo.
- O histórico deve ocupar o **espaço restante e continuar
  rolável**, preservar a posição do leitor e não perder rascunho
  ou seleção/SSID ao alternar foco, aba, orientação ou fechar
  teclado. Considerar modo de leitura em tela cheia, sem esconder
  status de transmissão/falha.
- **T28:** validar retrato, paisagem, aparelhos de 320/360/412 dp,
  fonte 150%/200%, teclado Gboard e alternativo, barra de gestos,
  IME aberto/fechado, teclado físico e acessibilidade. Confirmar
  campo e Enviar visíveis, nenhum elemento cortado, rascunho
  preservado, histórico rolável e envio sem perda de texto
  (ver falha P1 do item #25).
- **Status:** backlog registrado; não implementado.

### 29. Informação da estação: acrescentar www.PT2VHF.com ao texto existente
_Solicitado em 31/10/2026, após capturas dos detalhes da estação._

**Requisito explícito:** exibir o endereço **`www.PT2VHF.com`**
como complemento do conteúdo do campo **Informação** das estações
que transmitirem beacons pelo **PT2VHF APRS Client Android**.
Exemplo desejado: `PT2VHF APRS Client www.PT2VHF.com`.
**Não** substituir toda a descrição por apenas o domínio.

- Esta informação precisa estar **no comentário real do beacon de
  posição**, transmitido por APRS-IS e RF/TNC com a configuração
  autorizada. Não apenas decorar o popup local; outros programas
  devem receber o mesmo conteúdo. O código da v0.1.4 já usa
  `beaconCommentWithSite` antes de `positionInfo()`, mas o
  funcionamento real precisa de comprovação.
- Garantir o sufixo `www.PT2VHF.com` **exatamente uma vez**,
  mantendo texto de descrição útil à esquerda; conservar
  preferências pessoais intactas e reservar caracteres para o
  domínio antes de truncar. Se comentário do usuário for extenso,
  encurtar **somente a cópia enviada**, dando visibilidade ao
  limite APRS. Não transmitir beacons adicionais pelo simples
  fato de abrir detalhes.
- Nos detalhes da estação e na lista Estações, exibir o
  **comentário recebido** fielmente, inclusive quando ainda
  reflete beacon antigo de outra estação PT2VHF; ele somente
  mudará após aquela instalação transmitir com código atualizado.
  **Não acrescentar domínio a estações de terceiros** ou a pacotes
  antigos como se a estação o tivesse anunciado. Quando apropriado,
  tornar a URL recebida clicável sem alterar seu valor.
- **T29:** capturar pacote de posição bruto da v0.1.4/versão
  corrigida, verificar `www.PT2VHF.com` após a descrição,
  conferir o campo Informação no próprio cliente, no APRSdroid
  e em outro receptor, tanto RF quanto APRS-IS. Testar comentário
  vazio, personalizado, longo, padrão e domínio já existente,
  sem duplicação, sem truncar URL e sem adulterar dados de
  outras estações.
- **Status:** item aberto para homologação e eventual correção
  da exibição; não declarar concluído apenas pelo código.

### 30. Rastreador: iniciar ativado por padrão
_Solicitado em 31/10/2026: "Backlog: iniciar o rastreamento ligado por default"._

**Diagnóstico no código v0.1.4:**
- `data/Settings.kt` já usa `beaconMode = "smart"` como modo de beacon
  padrão; isso **não ativa o tracker**. O estado
  `LinkStatus.trackerOn` em `data/AprsRepository.kt` inicia
  **false** e não é persistido.
- `AprsService.onCreate()` inicia conexão/GPS, mas não restaura
  `trackerOn`. Apenas `ACTION_TRACKER_ON` marca true; em
  `ACTION_TRACKER_OFF` ele volta a false. Reiniciar o processo
  desliga o rastreador mesmo após ativação manual.

**Comportamento exigido:**

- O estado inicial desejado do tracker é **LIGADO** nas instalações
  já configuradas e autorizadas, **sem necessidade de tocar em
  "Iniciar rastreamento" a cada abertura**. Com beacon em modo
  `smart` (padrão), o rastreamento seguirá as regras de
  SmartBeaconing; preservar modos periódico/manual configurados.
- Persistir uma preferência de operação **`tracker_enabled`**,
  com padrão verdadeiro **apenas depois de concluída a configuração
  e a autorização explícita da transmissão de posição**.
  Um usuário que desligar manualmente deve continuar com o
  rastreador **desligado após fechar, reabrir, reiniciar o
  serviço ou o celular**, até ativá-lo novamente. Não sobrescrever
  `OFF` explícito com o valor padrão.
- **Privacidade e primeiro uso:** não transmitir coordenadas
  automaticamente antes de indicativo válido, consentimento
  informado para compartilhar localização, permissões de GPS
  relevantes e ao menos um canal TX permitido e apto. Mostrar
  indicador claro **Rastreador ligado / aguardando posição ou
  conexão / desligado**, diferenciando seguir localização
  local de transmitir beacons em APRS-IS/RF. A permissão
  de localização Android, isoladamente, não equivale a autorização
  para publicar localização na rede APRS.
- A ativação/restauração deve acontecer em **um ponto único do
  ciclo de vida do serviço**, de forma idempotente, sem gerar
  beacons duplicados ou TX extra por recriação de Activity,
  reconexão, notificação, rotação da tela ou mudança de aba.
  Respeitar o intervalo configurado e o primeiro fix GPS válido.
  Sem GPS ou conexão, continuar em estado aguardando, sem
  anunciar coordenadas antigas como atuais.
- No botão **Parar rastreamento** e na saída do aplicativo
  (item #16), persistir corretamente o estado e respeitar
  **"Sair e interromper"** versus **"Sair e manter em
  segundo plano"**. Reinício do dispositivo: seguir as
  regras Android sobre inicialização de foreground services,
  permissões e notificação persistente; não prometer
  autoarranque após boot se a plataforma o proibir.
- Não confundir esta opção com o ajuste existente
  `tracklog_enabled`, que apenas habilita o **registro do
  percurso**: o usuário pode gravar tracklog sem transmitir,
  e a política de localização/controle de TX deve continuar
  explícita.
- **T30 — Teste de aceite:** em instalação nova ainda não
  configurada, validar ausência de transmissão; concluir
  setup/consentimento e verificar tracker inicial ligado,
  posicionamento e primeiro beacon respeitando regras.
  Em instalação previamente autorizada sem preferência
  explícita, testar o padrão ligado; desligar manualmente,
  fechar/reabrir aplicativo e serviço, reiniciar aparelho e
  verificar permanência de OFF. Reativar e confirmar retorno.
  Testar GPS desligado, permissão negada, APRS-IS
  desconectado, RF/TNC desligado, TX desabilitado, modo
  manual/periódico/SmartBeaconing, segundo plano, rotação
  e encerramento com/sem continuidade. Confirmar sem
  beacons em duplicidade e sem transmissão não autorizada.
- **Status:** registrado no backlog, **não implementado e
  não homologado** nesta solicitação. Nenhum APK novo gerado.

### 31. Remover o aviso de anúncio exibido ao abrir o aplicativo
_Solicitado em 04/11/2026: "Backlog: Tire a mensagem de anúncio ao abrir o aplicativo"._

**Onde está no código (main atual):**
- `ui/App.kt`: estado `siteNotice` (`rememberSaveable { mutableStateOf(true) }`,
  ~linha 109), tratado no `BackHandler` (~linha 118) e exibido em
  `if (!showSettings && siteNotice) OfficialSiteNotice(...)` (~linha 208).
- `ui/App.kt`: composable privado `OfficialSiteNotice` (~linha 329), o popup
  "Downloads no site oficial" com o texto de distribuição pelo site, a linha
  "Versão instalada: X.Y.Z" e o link www.PT2VHF.com.
- Strings `official_downloads`, `official_notice` e `current_installed_version`
  em `res/values*/strings.xml` (PT/EN/ES/FR/IT), usadas só por esse popup.

**Comportamento exigido:**
- Ao abrir o app, **não mostrar** mais o popup de anúncio do site oficial.
- Remover o estado `siteNotice`, o ramo do `BackHandler`, a chamada e o composable
  `OfficialSiteNotice`; o diálogo de **nova versão disponível** (OTA, `update`)
  deve continuar funcionando e passa a ser o primeiro aviso possível na abertura.
- Remover as strings órfãs nas cinco línguas, ou reaproveitá-las se o link do site
  e a versão instalada forem mantidos em outro lugar (ex.: Configurações/Sobre).
- **Substitui o item 18** (anúncio do site com a versão instalada na abertura):
  ao implementar, marcar o item 18 como cancelado/substituído por este.
- **Critério de aceite:** instalação nova e atualização: abrir o app, sem popup de
  anúncio; botão Voltar não precisa fechar aviso extra; com versão remota mais
  nova, o aviso OTA aparece normalmente; sem strings não usadas no lint.
- **Status:** registrado no backlog, **não implementado**. Nenhum APK novo gerado.

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

## Itens registrados após a 0.1.8 QA

> Seção no fim do arquivo para não conflitar com o PR #13 (0.1.8 QA), que altera
> a tabela e o item 38. Ao mesclar, mover para "Pendentes" e para a tabela.

### 39. Aviso no app quando houver nova versão (melhoria do aviso OTA atual)
_Solicitado em 04/11/2026: "backlog para a próxima versão: aviso no app quando houver uma nova versão."_

**O que já existe (main, v0.1.7 QA):**
- `data/UpdateCheck.kt` consulta `https://www.pt2vhf.com/update.php?product=aprs-android`
  (mesma API do desktop, alimentada pelo `updates.json` do site), compara com
  `isNewerVersion` e aceita APK somente de `https://www.pt2vhf.com/`.
- `ui/App.kt` chama a verificação **uma única vez, ao abrir o app**
  (`LaunchedEffect(Unit)`), e mostra o diálogo "Nova versão X" com notas,
  **Baixar e instalar** (abre o navegador) e **Mais tarde**.

**Limitações do atual (o que este item deve resolver):**
- Só verifica na abertura da tela; com o app aberto por horas, ou rodando em
  segundo plano com o tracker, a nova versão não é percebida.
- Falha de rede/servidor é silenciosa (`null`), sem nova tentativa.
- "Mais tarde" só vale para aquela abertura; não há aviso permanente e discreto
  depois de dispensar o diálogo, nem forma de verificar manualmente.

**Comportamento exigido:**
- **Indicador persistente** enquanto houver versão mais nova: selo/ícone no
  cabeçalho (ou em Configurações) que, ao tocar, reabre o diálogo com notas e
  download. Some quando a versão instalada alcançar a remota.
- **Verificação periódica** além da abertura: ao voltar ao primeiro plano e no
  máximo a cada ~12 h (WorkManager ou equivalente, só com rede), com nova
  tentativa após falha; sem consumo relevante de bateria/dados.
- **Notificação do Android** opcional quando a nova versão for detectada com o
  app em segundo plano (canal próprio "Atualizações", respeitando permissão de
  notificações; uma por versão).
- **"Mais tarde"/"Ignorar esta versão"**: Mais tarde volta a avisar na próxima
  abertura; Ignorar esta versão silencia o diálogo até sair outra mais nova
  (o indicador discreto continua).
- **Verificar agora** em Configurações/Sobre, mostrando versão instalada, última
  verificação e resultado (atualizado / nova versão / falha de rede).
- Manter: fonte única no site oficial, APK só de `www.pt2vhf.com`, nenhuma
  instalação automática; textos nas cinco línguas (PT/EN/ES/FR/IT).
- **Critério de aceite:** com `updates.json` publicando versão maior, o aviso
  aparece sem reabrir o app (primeiro plano e segundo plano); indicador some
  após atualizar; sem rede, nenhuma mensagem de erro intrusiva e nova tentativa.
- **Status:** registrado no backlog, **não implementado**. Nenhum APK novo gerado.
