# Backlog

Itens registrados para as próximas versões. Não implementar até o pedido de versão nova.

## Onde estamos (10/11/2026)

- **v0.1.15** (10/11/2026, autorizada pelo mantenedor: "Sugestões aceitas. Implemente todos os itens do backlog e gere
  nova versão"): #72 (busca), #73 (Minhas/Todas e Agrupadas/Lista), #74 (Configurações em 7 abas), #75 (banner de
  abertura) e #76 (traduções/constantes internas). As ideias do APRS World #66/#67/#69/#70 já tinham saído na 0.1.14.
  **Ficaram de fora:** #68 Android Auto (Car App, aprovação do Google, teste em carro/DHU) e os itens que dependem de
  aparelho/servidor/rádio reais (#60, #62, #25, #37, #54, #44, #52). Tudo aguarda teste físico e OK do mantenedor.
- **v0.1.14** (10/11/2026, autorizada pelo mantenedor: "Implemente os 4 itens e gere uma nova versão"): pacote de
  Mensagens (#56/#64/#65/#28), #59 com tabela ITU, #58.3/#58.4/#58.5, #66/#67/#69/#70 (PR #21). **#68 Android Auto ficou
  de fora.** Itens aguardam teste físico e OK do mantenedor.
- Última versão **publicada**: **v0.1.13** (10/11/2026, beta, autorizada pelo mantenedor como "versão completa para
  todos"): reúne as QA 0.1.10 a 0.1.12 (#54/#56/#57/#59 parcial/#60/#61/#62, WX) e #58.6 + #63 (PRs #15, #16, #17, #19).
  #60 e #62 ainda dependem de teste em aparelho. Anterior: v0.1.9 (05/11/2026).
- **Nenhum item tem baixa definitiva**: cada um aguarda teste físico e OK do mantenedor, item a item, pelas tabelas
  "Status para teste — 0.1.9" (novos) e "Status para teste — 0.1.8 QA" (anteriores, ainda valem).
- Dependem de hardware e **não puderam ser validados aqui** (só testes de unidade do código): AFSK no ar (#44),
  adaptadores USB-OTG, widget (#46) e consumo de bateria (#49). O modem AFSK passa nos testes com ruído,
  de-ênfase e 44,1 kHz, mas nível de áudio, VOX e rádios reais precisam do teste T52.
- O P0 **#37** (queda do APRS-IS ao transmitir) ainda precisa de confirmação em servidor real.
- **Nova falha P0 (#60, 08/11): envio de mensagens fica tentando sem concluir.** Investigar junto com #25, #37 e #54 antes de publicar nova versão; falha relatada, ainda não reproduzida em teste.
- Fora do escopo: download em massa de mapas offline (a política de tiles do OpenStreetMap proíbe; o cache
  do osmdroid já guarda as áreas visitadas).
- Numeração: o item "31" duplicado (remover aviso de abertura, 04/11) passou a ser o **#38**. Não há #32.

## Em desenvolvimento — candidata 0.1.10 (08/11/2026)

**Branch:** feature/android-020-messaging-stability. Não há nova publicação pública.
Implementação **parcial / precisa de CI e teste em aparelhos**:
- #54 e #60: filtro de mensagens para o próprio indicativo, parser de pacotes terceiros
  de iGate, reply-ack, liberação da composição por timeout e ACK no worker de I/O.
  Pendente validação real das rotas TX/ACK por RF e APRS-IS.
- #56 e #57: contraste/altura do campo e checks simples/duplos nos balões;
  pendem testes de usabilidade, telas pequenas e entrega real de ACK.
- #61: lixeira na aba Estações reaproveitando limpeza existente; testar em aparelho.
- #58: parser WX convencional, gráficos de medições **somente da sessão atual**.
  Ainda faltam persistência, telemetria T#/PARM/UNIT/EQNS/BITS, outras variantes WX,
  ficha completa e comunidade.
- #59: somente validação de formato plausível do indicativo do operador;
  ainda faltam tabela global de prefixos e exceções internacionais.
- #52 (digi/iGate), #55 (notificações completas) e demais homologações anteriores:
  **não finalizados**. Nenhuma baixa por código não testado.

## Status para teste — 0.1.9 (05/11/2026)

Mesma legenda da tabela 0.1.8. Roteiros T50-T66 em [HOMOLOGACAO.md](HOMOLOGACAO.md).
**Hardware** = depende de equipamento real; não validado fora do aparelho.

| # | Item | Status 0.1.9 | O que mudou / o que testar | Teste |
|---|---|---|---|---|
| 6 | Configurações nível APRSdroid (restante) | Pronto para teste | Posição fixa digitada ou por toque longo no mapa, "usar a última posição do GPS", precisão do GPS (alta/equilibrada/baixo consumo) e localização pela rede | T50 |
| 39 | Aviso de nova versão | Pronto para teste | Verifica ao abrir/voltar (máx. 1×/h) e a cada 12 h pelo serviço; notificação uma vez por versão; botão ⬆ no topo; "Ignorar esta versão"; Sobre mostra última verificação e "Verificar agora" | T51 |
| 44 | Modem AFSK 1200 pelo áudio | Pronto para teste (**hardware**) | Rádio → Áudio (AFSK): entrada/saída, volume, TX delay, tom de VOX, decodificador alta qualidade/leve, nível de entrada, tom de teste; PTT por VOX/interface | T52 |
| — | TNC KISS por USB-OTG | Pronto para teste (**hardware**) | Rádio → USB-OTG: CDC-ACM, FTDI, CP210x; permissão, reconexão ao plugar, baud 1200-115200. CH340/PL2303 não suportados | T53 |
| 51 | Aba Logs | Pronto para teste | Nova aba depois de Tracker: todos os pacotes RX/TX (RF e APRS-IS), filtros, pausa, detalhes, copiar/compartilhar, exportar TXT/CSV, retenção e limite | T54 |
| 40 | Data e tempo decorrido | Pronto para teste | Conversas e balões: data + hora; nas de hoje também "há X h Y min" | T55 |
| 41 | Apagar mensagens por tipo | Pronto para teste | Lixeira em Mensagens e Configurações: todas, boletins/gerais ou privadas, com contagem e confirmação | T56 |
| 42 | Retenção "24 h" | Pronto para teste | Nova opção 24 h em "Manter por" | T56 |
| 43 | Boletim de versão | Pronto para teste | Uma vez por versão, 5 s após APRS-IS verificado, **só APRS-IS** (BLN0); opção para desligar | T57 |
| 45 | Enviar log | Pronto para teste | Sobre → Enviar log; após fechamento inesperado o app oferece o envio | T58 |
| 46 | Widget | Pronto para teste (**hardware**) | Status APRS-IS/TNC/GPS, rastreador, último beacon; botões Beacon agora e Rastreador | T59 |
| 47 | ACK e reenvio no balão | Pronto para teste | Tentativa N/M, contagem do próximo reenvio, Reenviar, Cancelar retentativas; tentativas e 1º intervalo em Configurações | T60 |
| 48 | Objetos e itens | Pronto para teste | Toque longo no mapa → Criar objeto aqui; Tracker → Meus objetos (editar, pausar, enviar agora, encerrar) | T61 |
| 49 | Economia de bateria | Pronto para teste (**hardware**) | Desligada / sempre / automática abaixo de X%; ativa com a tela apagada; cartão Bateria no Tracker | T62 |
| 50 | Backup das configurações | Pronto para teste | Configurações → Backup: exportar (com/sem mensagens), importar com prévia; passcode nunca exportado | T63 |
| — | Modo carro | Pronto para teste | Tracker → Modo carro: velocidade grande, conexões, próximo beacon, última mensagem, leitura em voz alta | T64 |
| — | Rotas | Pronto para teste | Tracker → Rotas (GPX): importar, mostrar/ocultar, distância restante no mapa; linha direta até a estação seguida | T65 |
| — | Textos em 5 idiomas | Pronto para teste | Todos os textos novos em pt, en, es, fr, it | T66 |

## Status para teste — 0.1.8 QA (04/11/2026)

Legenda: **Pronto para teste** = implementado e com CI; falta o teste físico e o OK do mantenedor.
**Parcial** = parte entregue, o restante está descrito. Ao dar OK, mudar para `Validado pelo usuário`.
Roteiros de teste (Txx) em [HOMOLOGACAO.md](HOMOLOGACAO.md).

| # | Item | Status 0.1.8 | O que mudou / o que testar | Teste |
|---|---|---|---|---|
| 1 | Símbolos APRS no mapa | Pronto para teste | Indicativo ao lado do ícone (camada "Rótulos"), símbolo próprio com o indicativo, "?" neutro para símbolo desconhecido | T01-T20, T39 |
| 2 | Conversas: filtros e ordem | Pronto para teste | Painel ☷ com ordem, direção, meio, situação (aguardando ACK/falha) e período, combináveis e persistidos | T21 |
| 3 | Estações: ordenação e tipos | Pronto para teste | Novos tipos Pessoa, Aeronave e Embarcação no filtro; lista reativa | T07, T13 |
| 4 | Meus enlaces RF | Pronto para teste | Rádio → "Meus enlaces RF (TNC)" com via, quantidade, última vez e distância; no mapa em verde (ouvi) e roxo (meu pacote repetido); camada "Somente meus enlaces" | T40 |
| 5 | Tracklog e estilo | Pronto para teste | Cor (6), espessura, opacidade, contínua/tracejada, retenção 1-365 dias e exportação GPX | T08, T41 |
| 6 | Configurações nível APRSdroid | Parcial | Feito: seções, seletor visual de símbolo, frequência de voz, ambiguidade 0-4, rumo/velocidade e altitude opcionais, corner pegging e intervalo parado, tela ligada, idade máxima das estações, notificações, monitor. Falta: posição manual digitada/no mapa, precisão do GPS, AFSK e USB-OTG | T42 |
| 7 | Persistência de estações/enlaces | Pronto para teste | Sem mudança na 0.1.8 | T06 |
| 8 | Cabeçalho verde/vermelho | Pronto para teste | Sem mudança na 0.1.8 | T02-T04 |
| 9 | APRS-IS automático | Pronto para teste | Sem mudança na 0.1.8 | T03 |
| 10 | Perfis Base/Móvel/Portátil/Somente recepção | Pronto para teste | Perfis em Configurações e troca rápida no Rastreador (com confirmação, sem ligar TX); cada perfil guarda SSID, símbolo, comentário, beacon, path, TX e TNC; Somente recepção bloqueia beacon, mensagem e ACK | T43 |
| 11 | Seguir estação móvel | Pronto para teste | Trilha tracejada da estação seguida (recebida nesta sessão), painel com velocidade, rumo, distância e idade, aviso após 10 min sem atualização | T09, T44 |
| 12 | Favoritos e alertas | Pronto para teste | Alertas de primeira aparição, retorno após N min, nova posição, RF direto e meu pacote digipeado; cooldown de 10 min, silêncio no 1º minuto após conectar e horário silencioso | T45 |
| 13 | Centro de diagnóstico | Pronto para teste | Verificado desde, reconexões, ACK pendentes, transições recentes, filtro do monitor (RX/TX/RF/texto) e exportação .txt com anonimização | T46 |
| 14 | Alcance e recordes RF | Pronto para teste | Rádio → "Alcance e recordes RF" (direto e via digi, por período) e camada "Cobertura RF" no mapa, só com pacotes do TNC | T47 |
| 15 | API para outros apps | Pronto para teste | Desligada por padrão; permissão `com.pt2vhf.aprs.permission.API`; eventos de posição/estado e comandos de status, rastreador e beacon, cada um com chave própria. Contrato em [API.md](API.md) | T48 |
| 16 | Confirmar saída | Pronto para teste | Sem mudança | T14 |
| 17 | Restaurar mapa | Pronto para teste | Sem mudança | T15 |
| 18 | Aviso do site com versão | **Cancelado** | Substituído pelo #38; versão instalada e link do site agora em Configurações → Sobre | — |
| 19 | Enviar mensagem pela estação | Pronto para teste | Sem mudança | T17 |
| 20 | Pressão longa: responder ou mensagem direta | Pronto para teste | Menu ao segurar o balão: Responder (com faixa de contexto), Responder em público (CQ), Responder em privado, Mensagem direta e Copiar; destino sempre visível; nada transmite até Enviar | T18 |
| 21 | Buscar indicativo para nova conversa | Pronto para teste | "Nova mensagem" com busca parcial/SSID em estações (inclusive cache) e histórico, mais opção de abrir o indicativo digitado | T19 |
| 22 | Brilho do mapa | Pronto para teste | Botão ☀ com 50-150%, Restaurar 100%; só os tiles mudam | T22 |
| 23 | Ordem cronológica das mensagens | Pronto para teste | Sem mudança | T23 |
| 24 | Largura dos balões | Pronto para teste | Sem mudança | T24 |
| 25 | Envio pelo APRS-IS | Pronto para teste | Sem mudança | T25 |
| 26 | Retenção de mensagens | Pronto para teste | Histórico migrado do `messages.json` para SQLite (backup `messages.json.migrated`); prazo e quantidade por categoria ou iguais para todas; prévia e confirmação; pendentes de ACK protegidas | T26 |
| 27 | Caracteres estranhos | Pronto para teste | Exibição reconhece UTF-8 válido, senão CP1252/Latin-1; controles viram "�"; pacote bruto e ACK intactos | T27 |
| 28 | Teclado em Mensagens | Pronto para teste | Sem mudança | T28 |
| 29 | www.PT2VHF.com no comentário | Pronto para teste | Mantido; frequência e altitude entram antes da descrição dentro dos 43 caracteres | T29 |
| 30 | Rastreador ligado por padrão | Pronto para teste | Sem mudança (no perfil Somente recepção não liga) | T30 |
| 33 | Estações em linha única | Pronto para teste | Sem mudança | T33 |
| 34 | Chips APRS-IS/TNC clicáveis | Pronto para teste | Chip TNC também retoma KISS TCP | T34 |
| 35 | Busca compacta em Estações | Pronto para teste | Sem mudança | T35 |
| 36 | Cor do botão Enviar beacon | Pronto para teste | Botão desabilitado em Somente recepção | T36 |
| 37 | **P0** queda do APRS-IS ao transmitir | Pronto para teste | Correção da 0.1.7 mantida; **bloqueia publicação** até teste real | T37 |
| 38 | Remover aviso de abertura (ex-31) | Pronto para teste | Popup removido; aviso OTA mantido; versão e site em Configurações → Sobre | T38 |
| — | KISS TCP (herdado 0.1.0) | Pronto para teste | Rádio → KISS TCP com servidor/porta (ex.: Direwolf 8001), reconexão automática | T49 |
| — | USB-OTG, AFSK (agora item 44, prioridade), mapa offline em massa, modo carro, rotas | Não implementado | Dependem de hardware ou de fonte de mapas que permita download | — |

## Acompanhamento de implementação e baixa (31/10/2026)

> Tabela histórica (até a 0.1.7). O status vigente está em "Status para teste — 0.1.8 QA" acima.

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

### 18. Anúncio do site oficial com a versão atualmente instalada (CANCELADO — substituído pelo #38)
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
**Status 0.1.14:** implementado na 0.1.14 junto com #56/#65 (teclado repassado à tela, cabeçalho/barra somem com o teclado). Aguarda teste T28 em aparelho.

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

### 38. Remover o aviso de anúncio exibido ao abrir o aplicativo (antigo 31 duplicado)
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
- **Status:** implementado na 0.1.8 QA (PR #13); aguarda teste físico (T38). Item 18 cancelado/substituído.

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
- **Status:** implementado na **0.1.9**, aguardando teste físico e OK do mantenedor (ver "Status para teste — 0.1.9").

### 40. Mensagens: mostrar a data, além da hora, e tempo decorrido nas de hoje
_Solicitado em 04/11/2026: "backlog: Mensagens, indicar a data, além da hora. Se a data for dentro do dia corrente, mostre a data e quanto tempo foi, ex: há 4 minutos, há 3h e 45min"._

**Como está hoje (v0.1.8):**
- Nos balões da conversa (`ui/MessagesUi.kt`, ~linha 309), o rodapé mostra só a
  hora via `hhmm(m.time)` (ex.: "14:32 · APRS-IS"); a data não aparece, então
  mensagens de dias diferentes ficam iguais.
- Na lista de conversas (~linha 147), a última mensagem usa `ago()` de
  `ui/Screens.kt`: "agora", "X min", "X h" (só horas inteiras) e, após 24 h,
  data/hora curtas.

**Comportamento exigido:**
- Toda mensagem (balões das conversas privadas e gerais) mostra **data e hora**
  de recepção/envio, no formato curto do idioma do app (ex.: PT "04/11/2026 14:32").
- Se a mensagem for **do dia corrente** (data local do aparelho), mostrar a data,
  a hora e o **tempo decorrido**: "há 4 minutos", "há 3h e 45min"; abaixo de
  1 min, "agora". Combinar horas e minutos (não arredondar para horas inteiras).
- Mensagens de dias anteriores: só data e hora, sem tempo decorrido.
- O tempo decorrido **se atualiza sozinho** com a tela aberta (ao menos a cada
  minuto) e muda para o formato de dia anterior após a meia-noite.
- Aplicar o mesmo critério à lista de conversas (última mensagem) para manter
  coerência, sem quebrar a linha em telas estreitas (truncar o comentário antes).
- Textos e plurais nas cinco línguas (PT/EN/ES/FR/IT); preservar via/estado
  (APRS-IS/RF, enviada/confirmada) no rodapé do balão.
- **Critério de aceite:** mensagem de agora → "04/11/2026 14:32 · há 0–1 min";
  de 3h45 atrás no mesmo dia → "… · há 3h e 45min"; de ontem → só data e hora;
  virada de meia-noite com o app aberto; fuso/horário de verão do aparelho.
- **Status:** implementado na **0.1.9**, aguardando teste físico e OK do mantenedor (ver "Status para teste — 0.1.9").

### 41. Mensagens: apagar todas, só boletins ou só mensagens pessoais
_Solicitado em 04/11/2026: "Backlog: Opção para apagar todas as mensagens, boletins e mensagens pessoais."_

**Como está hoje (v0.1.8):**
- Configurações já tem **Apagar todas as mensagens** (`ui/SettingsUi.kt`, ~linha 440 →
  `AprsRepository.clearMessages()`), com confirmação; apaga tudo de uma vez:
  privadas, CQ, boletins e pendentes (cancela retransmissões).
- Não há como apagar **só boletins** ou **só mensagens pessoais**, nem atalho na
  própria aba Mensagens.

**Comportamento exigido:**
- Oferecer três opções: **Apagar todas as mensagens**, **Apagar boletins**
  (boletins/anúncios BLN e mensagens gerais/CQ) e **Apagar mensagens pessoais**
  (conversas privadas, incluindo enviadas e pendentes).
- Disponível em Configurações → Dados locais (onde já está o botão atual) e também na aba **Mensagens** (menu ⋮
  ou ícone de lixeira), na aba/visão correspondente.
- Confirmação explícita em cada opção, dizendo o que será apagado e o que fica;
  ação irreversível; retorno de sucesso/falha (reaproveitar textos atuais).
- Apagar pessoais cancela as retransmissões pendentes dessas mensagens; apagar
  boletins não mexe nas conversas privadas, e vice-versa.
- Apagar da memória e do banco (`MessageStore`), sem reaparecer ao reiniciar;
  preservar estações, favoritos e configurações. Coerente com a retenção (item 26).
- Textos nas cinco línguas (PT/EN/ES/FR/IT).
- **Critério de aceite:** com boletins e pessoais armazenados, cada opção apaga só
  o seu tipo; reiniciar o app e conferir; cancelar a confirmação não apaga nada.
- **Status:** implementado na **0.1.9**, aguardando teste físico e OK do mantenedor (ver "Status para teste — 0.1.9").

### 42. Configurações → Mensagens: retenção do histórico: opção "24 h" em "Manter por"
_Solicitado em 04/11/2026: "Backlog: configurações, manter por, adicionar 24h."_

**Como está hoje (v0.1.8):** em Configurações → "Mensagens: retenção do histórico", o seletor
"Manter por" (`ui/SettingsUi.kt`, ~linha 278, `dayOptions`) oferece **7, 30, 90,
180 e 365 dias** ou **sem limite**; vale para todas ou separado por Conversas,
Geral e Boletins (item 26).

**Comportamento exigido:**
- Acrescentar **24 h** como primeira opção (antes de 7 dias), nas linhas de
  retenção comum e nas separadas (Conversas/Geral/Boletins).
- Rótulo "24 h" (não "1 dia") nas cinco línguas.
- Contar 24 h corridas a partir do horário de cada mensagem (não "até a meia-noite");
  a limpeza automática precisa rodar com frequência suficiente (ao menos a cada
  hora e na abertura do app) para essa opção fazer sentido.
- Manter a confirmação atual quando a mudança apagar mensagens na hora, com a
  prévia de quantas serão apagadas.
- Preferências existentes continuam válidas (sem migração de valores).
- **Critério de aceite:** com "24 h", mensagens com mais de 24 h somem (memória e
  banco) sem reiniciar o app; as mais novas ficam; reiniciar e conferir.
- **Status:** implementado na **0.1.9**, aguardando teste físico e OK do mantenedor (ver "Status para teste — 0.1.9").

### 43. Boletim automático anunciando a versão na 1ª conexão APRS-IS
_Solicitado em 04/11/2026: "Backlog: ao logar no aprs-is pela primeira vez na versão, o app irá enviar um boletim no idioma corrente, anunciando que está usando versão X, com download em www.PT2VHF.com"._

**Como está hoje (v0.1.8):** o app já envia e recebe mensagens gerais e boletins
(aba Mensagens → Boletins usa o destino `BLN0`, `ui/MessagesUi.kt` ~linha 162),
mas não envia nada automaticamente ao conectar.

**Comportamento exigido:**
- Na **primeira conexão APRS-IS verificada** (login aceito, `logresp verified`)
  depois de instalar ou atualizar para uma versão, enviar **um** boletim no idioma
  atual do app anunciando a versão e o download em www.PT2VHF.com.
  Ex. PT: "Usando PT2VHF APRS Android v0.1.9 - download www.PT2VHF.com".
- Gravar a última versão anunciada (ex.: `lastAnnouncedVersion`); só marcar como
  enviada após escrita bem-sucedida no APRS-IS. Reconexões, reinícios e novas
  conexões na mesma versão **não** repetem o envio.
- Mostrar o boletim enviado na aba Boletins como mensagem própria.

**Observações para a implementação:**
- Texto do boletim APRS limitado a **67 caracteres**; conferir nas cinco línguas
  (PT/EN/ES/FR/IT) com o número de versão mais longo previsto.
- Formato `:BLNn     :texto` (destino de 9 caracteres); boletim não tem ACK.
  Sugerir `BLN0` ou um identificador dedicado, a definir.
- Só enviar com passcode válido e TX por APRS-IS autorizado; **nunca por RF**
  (evitar anúncio em frequência compartilhada) e não enviar em modo "Somente recepção".
- Sugere-se uma opção em Configurações para desligar o anúncio (padrão ligado),
  para respeitar quem não quer transmitir automaticamente.
- Não repetir o boletim (sem retransmissões periódicas), para não gerar ruído na rede.
- **Critério de aceite:** atualizar a versão, conectar → um boletim no APRS-IS
  (conferir em aprs.fi ou outro cliente); reconectar e reiniciar → nada novo;
  sem passcode/TX desativado → nada enviado e nenhuma marcação feita.
- **Status:** implementado na **0.1.9**, aguardando teste físico e OK do mantenedor (ver "Status para teste — 0.1.9").

### 44. Conexão pela interface de áudio (modem AFSK 1200), como no APRSdroid — PRIORIDADE próxima versão
_Solicitado em 04/11/2026: "adicione conexão pela interface de áudio, como no aprsdroid"._

**Como está hoje (v0.1.8):** o app só fala com rádio por TNC KISS via Bluetooth
(e APRS-IS pela internet). AFSK aparece apenas como sub-item do item 6 (tipos de
conexão) e ficou fora da 0.1.8 ("depende de hardware", tabela de status). Não há
código de áudio/modem no app. Este item passa a ser o registro próprio do AFSK
e tem **prioridade para a próxima versão**.

**Comportamento exigido:**
- Novo tipo de conexão **"Áudio (AFSK)"** em Configurações → Conexão/TNC, ao lado
  do KISS Bluetooth: o celular faz papel de TNC, sem hardware extra além do cabo
  de áudio (ou interface tipo APRS-K1 / cabo com VOX, Bluetooth de áudio do rádio).
- **RX:** demodular AFSK Bell 202 1200 bd (1200/2200 Hz) do microfone/entrada de
  áudio, decodificar AX.25/HDLC e entregar ao mesmo fluxo de pacotes RF existente
  (estações, mensagens, enlaces RF, origem "RF").
- **TX:** gerar AFSK do pacote AX.25 na saída de áudio, com **PTT por VOX** (tom
  de pré-chave/preâmbulo configurável, TXDELAY) e respeitando as autorizações de
  TX por RF já existentes; beacons e mensagens funcionam como no KISS.
- Configurações: fonte de áudio (microfone interno, fone com fio, USB, Bluetooth
  SCO), volume/nível de saída, preâmbulo/TXDELAY, qualidade do demodulador
  (alto desempenho x economia de bateria), medidor de nível de entrada e botão
  de teste de tom para ajustar o rádio.
- Indicador "TNC" no cabeçalho mostra o estado do modem de áudio (ouvindo /
  transmitindo / erro de permissão).
- Funcionar em segundo plano com o serviço atual (permissão de microfone e
  serviço em primeiro plano do tipo adequado no Android 14+).

**Observações para a implementação:**
- APRSdroid é GPL: usar só como referência de comportamento, **sem copiar código**.
  Implementar demodulador próprio ou usar biblioteca de licença compatível.
- Núcleo do modem (modulação/demodulação, HDLC, bit-stuffing, CRC) em código
  puro testável na JVM com arquivos WAV de referência (ex.: faixas de teste TNC).
- Exige teste com rádio real: decodificação de pacotes no ar, TX ouvido por outro
  TNC/digipeater, ajuste de nível, VOX, ruído e consumo de bateria.
- **Critério de aceite:** com rádio + cabo, decodificar pacotes reais e mostrá-los
  como RF; enviar beacon e mensagem que apareçam em aprs.fi via igate próximo;
  modo somente RX sem acionar VOX; troca entre KISS e Áudio sem reinstalar.
- **Status:** implementado na **0.1.9**, aguardando teste físico e OK do mantenedor (ver "Status para teste — 0.1.9").

### 45. Sobre: botão "Enviar log" para relatório de erros
_Sugestão aprovada pelo usuário em 04/11/2026 ("Todas.")._

**Como está hoje (v0.1.8):** a aba Rádio → Diagnóstico (item 13) já exporta um
.txt anonimizado (reconexões, ACK pendentes, transições, monitor), salvo pelo
seletor de arquivos. Não há captura de travamentos nem envio direto.

**Comportamento exigido:**
- Em Configurações → Sobre, botão **Enviar log** que monta o relatório (o mesmo
  .txt do diagnóstico + versão do app, Android, aparelho, configurações sem
  passcode) e abre o compartilhamento do Android (e-mail, WhatsApp, Drive).
- Guardar o último travamento (exceção não tratada, com pilha) e incluí-lo no
  relatório; na abertura seguinte, oferecer "O app fechou inesperadamente. Enviar log?".
- Anonimização igual à atual (passcode nunca; posição/indicativo opcionais).
- Útil para casos como a queda do APRS-IS (#37).
- **Critério de aceite:** relatório compartilhado por e-mail contém versão, eventos
  recentes e o último crash, sem passcode.
- **Status:** implementado na **0.1.9**, aguardando teste físico e OK do mantenedor (ver "Status para teste — 0.1.9").

### 46. Widget na tela inicial: último beacon, "beacon agora" e status da conexão
_Sugestão aprovada pelo usuário em 04/11/2026 ("Todas.")._

**Como está hoje (v0.1.8):** não há widget. Existe a API de integração (item 15,
`ApiReceiver`) com comandos SEND_BEACON, TRACKER_START/STOP e REQUEST_STATUS,
que o widget pode reaproveitar.

**Comportamento exigido:**
- Widget redimensionável mostrando: indicadores APRS-IS/TNC/GPS (verde/vermelho),
  tracker ligado/desligado, horário e há quanto tempo foi o último beacon.
- Botões **Beacon agora** e **Tracker liga/desliga**; tocar no resto abre o app.
- Atualiza quando o estado muda (sem polling que gaste bateria); respeita as
  autorizações de TX existentes; tema claro/escuro; cinco línguas.
- **Critério de aceite:** com o app fechado, beacon pelo widget aparece em aprs.fi;
  status muda no widget ao desconectar o APRS-IS.
- **Status:** implementado na **0.1.9**, aguardando teste físico e OK do mantenedor (ver "Status para teste — 0.1.9").

### 47. Mensagens: confirmação (ACK/REJ) e reenvio mais visíveis no balão
_Sugestão aprovada pelo usuário em 04/11/2026 ("Todas.")._

**Como está hoje (v0.1.8):** já existe reenvio automático e o estado aparece em
texto no rodapé do balão: "Aguardando ACK · APRS-IS", "Aguardando ACK (2ª
tentativa)", "ACK recebido", "Rejeitada", "Sem ACK após 5 tentativas"
(`service/AprsService.kt` `sendWithRetry`: 5 tentativas, intervalo 30 s × n).
Este item **complementa** o que existe, não cria outro mecanismo.

**Comportamento exigido:**
- Ícone de estado no balão (relógio = aguardando, ✓ = ACK, ✗ = rejeitada/sem ACK),
  com cor, além do texto.
- Contador explícito "tentativa 2/5" e tempo até a próxima.
- Após falha ou REJ: ações **Reenviar** e **Cancelar** (pressão longa / toque no
  ícone); cancelar interrompe os reenvios pendentes.
- Número de tentativas e intervalo configuráveis (padrão atual 5 / 30 s × n).
- **Critério de aceite:** mensagem sem destino responsivo mostra 1/5…5/5 e depois
  ✗ com Reenviar; ACK muda para ✓ na hora; REJ mostra ✗ distinto.
- **Status:** implementado na **0.1.9**, aguardando teste físico e OK do mantenedor (ver "Status para teste — 0.1.9").

### 48. Objetos e itens APRS: criar e transmitir pelo mapa
_Sugestão aprovada pelo usuário em 04/11/2026 ("Todas.")._

**Como está hoje (v0.1.8):** o app recebe e mostra objetos/itens de outras
estações, mas não cria nem transmite os próprios.

**Comportamento exigido:**
- Pressão longa no mapa → **Criar objeto aqui**: nome (até 9 caracteres), símbolo
  (seletor visual existente), comentário, frequência (ex.: repetidora "146.910-
  T123"), tipo objeto ou item.
- Transmitir pelo APRS-IS e/ou RF conforme autorizações; repetição periódica
  configurável (ex.: a cada 10–30 min) enquanto ativo.
- Lista "Meus objetos" para editar, mover, pausar e **apagar/encerrar** (enviar o
  objeto como "killed", formato APRS).
- Formatos conforme especificação APRS (`;NOME_____*DDHHMMz...` e `)NOME!...`).
- **Critério de aceite:** objeto criado aparece em aprs.fi com símbolo e
  comentário certos; editar move; encerrar remove dos outros clientes.
- **Status:** implementado na **0.1.9**, aguardando teste físico e OK do mantenedor (ver "Status para teste — 0.1.9").

### 49. Modo economia de bateria
_Sugestão aprovada pelo usuário em 04/11/2026 ("Todas.")._

**Como está hoje (v0.1.8):** não há modo dedicado; SmartBeaconing e intervalos
(item 6) já reduzem beacons, mas GPS e mapa seguem no mesmo ritmo.

**Comportamento exigido:**
- Opção **Economia de bateria** (manual ou automática abaixo de X% de carga):
  com a tela desligada, reduzir a frequência/precisão do GPS e parar refresh do
  mapa e animações; aumentar intervalos mínimos do beacon.
- Mostrar **consumo estimado** do app (por hora, com base no uso real do Android
  quando disponível) e o que o modo está economizando.
- Nunca desligar recepção de mensagens nem o APRS-IS conectado; avisar que a
  precisão da trilha cai.
- **Critério de aceite:** em 1 h de tracker com tela desligada, consumo menor que
  no modo normal (medir); mensagens continuam chegando.
- **Status:** implementado na **0.1.9**, aguardando teste físico e OK do mantenedor (ver "Status para teste — 0.1.9").

### 50. Backup e restauração das configurações em arquivo
_Sugestão aprovada pelo usuário em 04/11/2026 ("Todas.")._

**Como está hoje (v0.1.8):** não há exportação manual. O manifesto tem
`allowBackup="true"` (backup automático do Android na conta Google), que não é
controlável pelo usuário nem serve para trocar de celular de forma garantida.

**Comportamento exigido:**
- Em Configurações, **Exportar configurações** → arquivo (.json) via seletor de
  arquivos/compartilhamento; **Importar configurações** → lê e aplica após
  confirmação mostrando o que será substituído.
- Incluir indicativo, SSID, símbolo, comentários, beacon/SmartBeaconing, TNC,
  APRS-IS (servidor/filtro), favoritos, retenção, mapa e idioma. **Passcode
  opcional**, desmarcado por padrão; avisar se incluído.
- Opcional: incluir mensagens/estações/tracklog.
- Versão do formato no arquivo; importar de versões antigas sem quebrar.
- **Critério de aceite:** exportar no celular A, importar no B: mesmas
  configurações; arquivo inválido mostra erro sem alterar nada.
- **Status:** implementado na **0.1.9**, aguardando teste físico e OK do mantenedor (ver "Status para teste — 0.1.9").

### 51. Tela principal: nova aba "Logs" com todos os pacotes, depois de Tracker
_Solicitado em 04/11/2026: "Backlog: página principal, adicione uma aba Logs, que mostra todas os pacotes, depois de Tracker."_

**Como está hoje (v0.1.8):** a barra inferior tem Mapa, Mensagens, Estações,
Rádio e Tracker (`ui/App.kt`, `enum class Tab`). Os pacotes aparecem só no
cartão **Monitor ao vivo** dentro da aba Rádio (`ui/RadioUi.kt` ~linha 222),
com filtro Todos/RX/TX/RF e texto; guarda apenas as **últimas 200 linhas em
memória** (`AprsRepository.monitorLine`) e some ao reiniciar. A exportação .txt
do diagnóstico (item 13) e o "Enviar log" (item 45) são relatórios, não a lista.

**Comportamento exigido:**
- Sexta aba **Logs** na barra inferior, **logo após Tracker**, com ícone próprio.
- Lista de **todos os pacotes**, recebidos e enviados, de APRS-IS e RF (TNC/AFSK),
  um por linha: horário (data+hora), direção RX/TX, meio (APRS-IS/RF, via/digis),
  origem→destino, tipo (posição, mensagem, objeto, status, telemetria, clima…) e
  o pacote bruto (TNC2), com quebra/truncamento legível.
- Mais recentes no topo, rolagem fluida com milhares de linhas; pausar rolagem
  automática; tocar no pacote mostra detalhes decodificados e atalho para a
  estação/mensagem; copiar/compartilhar o pacote.
- Filtros: RX/TX, APRS-IS/RF, tipo, indicativo/texto (reaproveitar os do Monitor).
- Guardar em banco local com retenção configurável (ex.: 24 h / 7 dias / quantidade),
  sobrevivendo a reinícios; botão limpar com confirmação; exportar (.txt/.csv).
- Com seis abas, conferir largura em 320/360 dp (rótulos curtos ou só ícone no
  selecionado) e cinco línguas. Decidir se o Monitor ao vivo da aba Rádio fica
  (resumo) ou passa a apontar para a aba Logs.
- **Critério de aceite:** beacon enviado e pacotes recebidos de APRS-IS e TNC
  aparecem na aba Logs com direção e meio corretos; reiniciar o app mantém o
  histórico conforme a retenção; filtros combinados funcionam.
- **Status:** implementado na **0.1.9**, aguardando teste físico e OK do mantenedor (ver "Status para teste — 0.1.9").

### 52. Digipeater e iGate no app, configuráveis
_Solicitado em 05/11/2026: "Backlog: implemente um digi e igate ao app, configuráveis."_

**Como está hoje (v0.1.9):** o app só **observa** digipeaters e iGates de terceiros:
classifica estações como Digipeater/iGate (filtro de tipos), conta "meu pacote
digipeado" e mostra enlaces RF (`data/RfStats.kt`, `AprsRepository` ~linha 447).
Não retransmite pacotes de outros em RF nem encaminha RF↔APRS-IS. Pacotes do TNC
entram só para exibição (`AprsService` ~linha 120).

**Comportamento exigido — Digipeater (RF→RF):**
- Liga/desliga em Configurações → Digipeater (padrão **desligado**); usa o TNC
  ativo (KISS Bluetooth/TCP e, quando houver, AFSK do item 44).
- Aliases configuráveis: próprio indicativo, `WIDE1-1` (fill-in, padrão), opção
  `WIDEn-N` com limite de N (ex.: até WIDE2-2), aliases extras (ex.: regionais).
- Marcar `*`/substituir o alias pelo indicativo (trace) conforme a prática APRS;
  **dedupe** (não repetir o mesmo pacote em ~30 s, tempo configurável);
  descartar pacotes com path excessivo; não digipetar os próprios pacotes de volta.
- Atraso/slot e limite de taxa para não congestionar a frequência.

**Comportamento exigido — iGate:**
- Liga/desliga em Configurações → iGate (padrão **desligado**); **exige APRS-IS
  verificado com passcode válido**.
- **RF→IS:** encaminhar pacotes ouvidos no TNC ao APRS-IS no formato de gateway
  (`...,qAR,MEUCALL:`), sem pacotes com `TCPIP`/`NOGATE`/`RFONLY` no path,
  sem terceiros (`}`) mal formados, e com dedupe.
- **IS→RF (opcional, padrão desligado):** só mensagens para estações ouvidas
  recentemente em RF (janela configurável, ex.: 30 min), encapsuladas como
  terceiros, com limite de taxa; opcionalmente posição de quem enviou a mensagem.
- Beacon próprio de iGate/digi (símbolo `I`/`#` com overlay, comentário,
  intervalo), separado do beacon do tracker, ou aviso para usar perfil "Base".

**Telas e monitoramento:**
- Contadores: digipetados, RF→IS, IS→RF, descartados (por motivo); visíveis em
  Rádio e na aba Logs (item 51) com marcação "DIGI"/"IGATE".
- Indicação no cabeçalho quando digi/iGate estão ativos.
- Bloqueado no perfil "Somente recepção"; respeita autorizações de TX RF.

**Observações:**
- Exige app em primeiro plano/serviço contínuo e celular ligado à energia
  (aviso de consumo); combinar com o item 49 (economia de bateria) desativando-o.
- Responsabilidade do operador (licença, frequência e regras locais); mostrar
  aviso ao ligar.
- Referência de comportamento: APRSdroid/aprx/Direwolf **só como ideias**, sem
  copiar código GPL.
- **Critério de aceite:** com rádio real, pacote WIDE1-1 de outra estação é
  digipetado uma vez (sem loop/duplicata); pacote RF aparece em aprs.fi com
  `qAR,MEUCALL`; com IS→RF ligado, mensagem para estação ouvida localmente sai em RF
  e para estação não ouvida não sai; desligado, nada é retransmitido.
- **Status:** registrado no backlog, **não implementado**. Nenhum APK novo gerado.

### 53. ~~Modo satélite: passagens, apontador de antena (bússola + inclinômetro) e contatos APRS/voz~~ (CANCELADO — fora do escopo deste app)

> **Cancelado em 05/11/2026** a pedido do usuário: "Melhor, tire essa parte de sat do app. Irei pensar em um outro específico para sat, para concorrer com o iss detector."
> Não implementar no PT2VHF APRS Android. O texto abaixo e as telas de inspiração em
> `docs/inspiracao/` ficam só como **referência para o futuro app de satélite**.
_Solicitado em 05/11/2026: "BACKLOG: Sim, pode registrar. Implemente algum recurso que use a bússola e inclinômetro para ajudar a apontar uma antena portátil e fazer contatos por aprs e voz. Use os satélites que operam em aprs e voz", com captura de tela da lista de satélites; IO-86 acrescentado a pedido em 05/11/2026._
Inspiração: app ISS Detector (só a ideia, **sem copiar** nome, visual ou código).
O mantenedor tem o **PT2VHF Sat Tool**: reaproveitar as ideias/algoritmos de
previsão de lá.

**Como está hoje (v0.1.9):** não há nada de satélite no app nem uso de sensores
(bússola/acelerômetro). APRS via satélite só funcionaria hoje por acaso, com o
path e a frequência configurados à mão no rádio.

**Satélites iniciais (catálogo NORAD, lista do usuário):**

| Satélite | NORAD | Modo (conferir status AMSAT antes de implementar) |
|---|---|---|
| ISS (ZARYA) | 25544 | **APRS digi** 145.825 MHz FM 1200 bd (path `ARISS`/`APRSAT`); **voz FM** repetidora cross-band (sobe 145.990, tom 67 Hz / desce 437.800) quando ativa |
| SONATE-2 | 59112 | **APRS digi** 145.825 MHz FM 1200 bd (path `APRSAT`/`WIDE1-1`, conferir) |
| SAUDISAT 1C (SO-50) | 27607 | **Voz FM**: sobe 145.850 (tom 67 Hz; 74,4 Hz arma o timer) / desce 436.795 |
| JAS-2 (FO-29) | 24278 | Transponder linear **SSB/CW** (sobe 145.900–146.000 / desce 435.800–435.900, invertido) |
| RS-44 | 44909 | Transponder linear **SSB/CW** (sobe 145.935–145.995 / desce 435.610–435.670, invertido) |
| OSCAR 7 (AO-7) | 7530 | Transponder linear **SSB/CW** modos A e B; só funciona iluminado pelo sol (intermitente) |
| LAPAN-A2 (IO-86) | 40931 | **Voz FM** (sobe 145.880, tom 88,5 Hz / desce 435.880) e **APRS digi** (sobe 145.825 / desce 435.880); órbita equatorial (~6°), só passa para latitudes até ~±20–25° (bom para o Norte/Nordeste do Brasil, raro no Sul) |

Frequências acima são de referência e **devem ser conferidas** (AMSAT / fontes
oficiais) antes da implementação; guardar em tabela editável, não fixa no código.
A lista é editável: o usuário pode adicionar/remover satélites por NORAD.

**Comportamento exigido:**
1. **Próximas passagens:** lista por satélite com início/fim (AOS/LOS), elevação
   máxima, azimute de entrada/saída e duração, para a posição atual (GPS) ou
   fixa; filtro por elevação mínima (ex.: 10°).
2. **Alerta antes da passagem** (minutos configuráveis), notificação por satélite.
3. **Radar do céu:** trajeto da passagem em gráfico polar (azimute × elevação),
   posição atual do satélite e contagem regressiva.
4. **Apontador de antena portátil:** usar **bússola (magnetômetro) e inclinômetro
   (acelerômetro/vetor de rotação)** do celular preso/alinhado à antena; mostrar
   seta "gire para a esquerda/direita" e "incline para cima/baixo", com indicação
   de acerto (cor/vibração/bip opcional). Corrigir **declinação magnética** pela
   posição (`GeomagneticField`), tela de calibração da bússola e aviso de
   interferência magnética (metal da antena, rádio).
5. **Doppler e frequências na passagem:** mostrar a frequência de subida e
   descida corrigida em tempo real (passos de ajuste para rádio manual) para FM,
   SSB e APRS. Opcional futuro: controle do rádio por CAT/CI-V (fora deste item).
6. **APRS via satélite:** durante a passagem de satélite com digi APRS, modo de
   envio que usa path do satélite (ex.: `ARISS`), beacon de posição e mensagens
   curtas pelo TNC/AFSK; marcar no Logs e no mapa o que foi **ouvido via satélite**
   (digipeado pelo indicativo do satélite, ex.: `RS0ISS`). Lembrete para mudar o
   rádio para 145.825 MHz e voltar à frequência terrestre ao final.
7. **Voz:** para satélites FM/SSB, a tela mostra só o guia (frequências, Doppler,
   tom, apontamento); a voz é feita pelo rádio.


**Telas de inspiração (enviadas pelo usuário em 05/11/2026):**
[Detalhes](docs/inspiracao/item53-iss-detector-detalhes.png) e
[Radar](docs/inspiracao/item53-iss-detector-radar.png), capturas do ISS Detector.
São **referência de conteúdo e organização**; o app deve ter layout, ícones e
cores próprios (tema PT2VHF), sem reproduzir o visual daquele app.

- **Cabeçalho comum** às abas do satélite: nome + descrição, contagem regressiva
  até a próxima passagem ("Visível em 5d 5h 42m 56s") e duração; ações
  compartilhar e adicionar ao calendário.
- **Aba Detalhes:** data da passagem; grade com hora inicial/final, duração,
  elevação inicial/máxima/final, direção inicial/final (ponto cardeal + graus,
  ex.: "NNO (344,5°)"); **mapa** com a trajetória (ground track) e o círculo da
  área de cobertura do satélite, botões tela cheia / minha posição; bloco
  **"Agora"** com latitude, longitude, altitude, azimute, elevação, distância,
  velocidade, hora local e UTC; abaixo, dados do TLE (idade, época).
  No nosso app acrescentar ao bloco: **modo/frequências com Doppler** e
  **path APRS** do satélite.
- **Aba Radar:** gráfico polar (anéis de elevação 0/30/60°, cardeais N/L/S/O),
  trajetória da passagem com ponto de início (AOS), posição atual e fim; rodapé
  com direção e elevação iniciais. O radar **gira com a bússola** (o "N" fica
  onde o norte real está em relação ao celular) e uma **seta** mostra para onde
  apontar. No nosso caso, a seta e um indicador de inclinação vêm da **bússola +
  inclinômetro** (apontador do passo 4), com aviso de acerto quando a antena
  estiver no azimute/elevação do satélite.
- Abas sugeridas no nosso app: **Radar**, **Detalhes** e **Operar** (frequências
  com Doppler, envio APRS e log de estações ouvidas via satélite), no lugar de
  "Vídeo ao vivo".
**Observações:**
- TLEs de fonte pública (ex.: **CelesTrak**), atualização automática ao abrir e a
  cada 1–2 dias, funcionando offline com o último TLE (mostrar idade do TLE).
- Propagação SGP4 com biblioteca de licença compatível ou implementação própria;
  validar contra o PT2VHF Sat Tool / previsões públicas (erro de minutos = falha).
- Celulares sem magnetômetro ou sem sensor de rotação: esconder o apontador e
  manter a lista/radar.
- Desligar beacon automático terrestre durante o modo satélite para não poluir o
  canal (opcional, configurável). Responsabilidade do operador quanto à licença.
- **Critério de aceite:** previsão de passagem da ISS coincide (± 1 min) com fonte
  pública; com o celular na antena, a seta leva ao azimute/elevação corretos
  (conferir com bússola real); numa passagem real, beacon APRS aparece em aprs.fi
  via `RS0ISS`/satélite, e o QSO de voz no SO-50 é possível seguindo as frequências
  e o apontamento mostrados.
- **Status:** **cancelado / fora de escopo** (vai para um app de satélite separado). Não implementar aqui.

### 54. Mensagens: mensagem pessoal vinda de RF (via iGate) não aparece no APRS-IS + filtro pessoais/todas
_Solicitado em 06/11/2026: "Backlog: eu conectado por aprs-is não recebo uma mensage de um amigo que está conectado por rf na rede que tem um igate. Ele recebe a minha mensagem mas só olhando o pacote. Não aparece como um pacote recebido. Eu também deixo ligado por horas e não recebo nada além dos boletins. No log, vejo tudo chegando de todos. Seria bom na parte mensagens, filtrar mensagem pessoal ou de todos, empilhada ou concatenada por emissor."_

**Bug (P1).** Cenário: eu no app só por APRS-IS; amigo só por RF, numa rede com iGate.
- A mensagem dele para mim **não aparece** como recebida em Mensagens (nem notificação/ACK).
- A minha mensagem chega ao rádio/app dele, mas ele **só vê o pacote bruto**, não como mensagem recebida.
- Em horas ligado, em Mensagens só aparecem **boletins**; na aba Logs chegam pacotes de todos.

**Complemento em 06/11/2026:** "este meu amigo me mandou para o indicativo correto e não recebi. Ele recebeu o meu olhando os pacotes que passaram pelo igate da rede. Ambos com mesma versão do app android." → a hipótese de **SSID errado foi descartada**.

**Análise do código (v0.1.9, sem teste em servidor real; confirmar antes de corrigir):**
1. **Lado RF (amigo) — confirmado no código: pacote de terceiros `}` não é decodificado.**
   Quando o iGate passa a minha mensagem IS→RF, ela vai encapsulada
   (`IGATE>APxxxx,WIDE..:}PT2VHF>APxxxx,TCPIP,IGATE*::AMIGO    :texto{1`).
   `AprsPacket.parse` usa o cabeçalho externo (origem = iGate) e `parseMessage`
   (`core/Aprs.kt` ~linha 166) só aceita info começando com `:`; o `}` cai fora e o pacote
   fica só no Log/Monitor. É exatamente o sintoma dele. Também não sai ACK, então o meu app
   reenvia até "Sem ACK".
2. **Lado APRS-IS (eu) — filtro não pede mensagens para mim.** Padrão `r/4000`, expandido para
   `r/lat/lon/4000` só após o 1º fix de GPS (antes disso vai o `r/4000` literal, que não é
   filtro válido). O filtro de alcance casa pacotes **com posição**; mensagem não tem posição,
   então só chega se o servidor repassar por conta própria a mensagem para o indicativo logado,
   o que depende do servidor (aprsc/javAPRSSrvr) e de a estação ter sido "ouvida" naquela
   conexão. Causa provável nº 1 do meu lado. Correção: incluir sempre `g/<MEUCALL>*`
   (mensagens para mim, qualquer SSID) e, opcionalmente, `b/<contatos recentes>` no filtro de
   login **e** no `#filter` após o GPS (`AprsService` ~linhas 343 e 525, `Connections.kt` ~45).
3. **Diagnóstico pendente com o usuário:** na aba Logs, procurar o pacote do amigo
   (`AMIGO>...::PT2VHF-x:...`). Se **não aparece**, a mensagem não chegou ao app (filtro/servidor,
   item 2). Se **aparece** e não virou mensagem, é parse/ingestão e precisa do pacote cru para
   reproduzir em teste de unidade.
4. **Comparação exata do destinatário** (`AprsRepository.ingest` ~linha 531: `addressee == fullCall`):
   não é a causa deste caso, mas continua descartando em silêncio mensagens para o indicativo
   base/outro SSID meu. Manter na correção.
5. **Reply-ack (`{MM}AA`) mal interpretado:** o ID vira `MM}AA` (`take(5)` após o último `{`) e o
   ACK devolvido (`ackMM}AA`) não confirma a mensagem original. Comum em Kenwood/Yaesu.
6. Mensagens entre **outras** estações (nem para mim, nem CQ/BLN) são descartadas: por isso a
   tela só mostra boletins/CQ, embora o Log mostre tudo.

**Correção esperada:**
- Filtro APRS-IS com `g/<MEUCALL>*` sempre (sem duplicar se o usuário já tiver), no login e
  no `#filter`, e mostrar o filtro efetivamente enviado no diagnóstico (Sobre → Enviar log).
- Reconhecer como pessoal a mensagem para o meu indicativo **com e sem SSID** (opção em Configurações:
  "aceitar mensagens para o indicativo base/qualquer SSID", padrão ligado), mantendo o ACK com o
  destinatário exato do pacote.
- Desencapsular pacotes de terceiros `}` (origem/destino/path internos) para mensagens, ACK/REJ e
  posições, marcando o meio de chegada como RF via iGate.
- Tratar reply-ack (`{MM}AA`): ID = `MM`, e `AA` confirma a mensagem que eu enviei.

**Melhoria — tela Mensagens: pessoais × todas, empilhada × por emissor:**
- Filtro/aba **"Pessoais"** (para mim + enviadas por mim) e **"Todas"** (inclui mensagens entre
  terceiros ouvidas por RF/APRS-IS, CQ e boletins), padrão Pessoais. Mensagens de terceiros só
  para leitura, sem ACK nem notificação.
- Modo de exibição: **empilhada** (linha do tempo única, ordem de chegada, com "DE → PARA" no
  balão) ou **agrupada por emissor** (uma conversa por indicativo, expansível). Persistir a escolha.
- Combinar com os filtros já existentes (direção, meio, status, período, busca — itens 2/31) e
  com a retenção (itens 26/42) e o apagar por tipo (item 41), incluindo o tipo "de terceiros".

**Critério de aceite:** com amigo em RF + iGate e eu só no APRS-IS: a mensagem dele para
`MEUCALL` (com e sem SSID) aparece em Pessoais, notifica e recebe ACK que o rádio dele aceita
(para de reenviar); minha mensagem aparece como mensagem (não só pacote) no app dele em RF;
reply-ack confirma o balão; em "Todas" aparecem mensagens de terceiros vistas no Log; testes de
unidade para SSID, `}` e `{MM}AA`.
- **Status:** registrado no backlog, **não implementado**. Nenhum APK novo gerado.

### 55. Mensagens: aviso visual e sonoro ao chegar mensagem
_Solicitado em 06/11/2026: "Backlog: notificação visual e sonora quando chegar msg."_

**Como está hoje (v0.1.9):** já existe notificação do Android para mensagem pessoal
(`AprsService.notifyMessage`, canal "Mensagens" com importância alta, som padrão do sistema),
ligada por Configurações → "Notificar mensagens" (padrão ligado). Limitações:
- Só dispara para mensagem **endereçada a mim** que passou pela ingestão; com o bug do item 54
  ela nunca dispara (por isso o usuário não percebe nada).
- Sem aviso **dentro do app** com ele aberto: nenhum contador de não lidas na aba Mensagens,
  nenhum banner/snackbar, nenhum destaque na conversa.
- Sem escolha de som/vibração no app (só pelas configurações do canal no Android) e sem
  opção para CQ/boletins.
- Exige permissão POST_NOTIFICATIONS (Android 13+); se negada, nada aparece e o app não avisa.

**Comportamento exigido:**
- **Visual no app:** contador de não lidas na aba Mensagens e em cada conversa; banner/snackbar
  "Mensagem de X" com toque para abrir a conversa; marcar como lida ao abrir.
- **Sonoro:** som próprio curto (padrão) ou som do sistema/silencioso, vibração liga/desliga,
  tocando também com o app aberto. Respeitar o modo silencioso/Não perturbe do Android e o
  horário de silêncio já existente (item 12).
- **Por tipo:** pessoais (padrão ligado), CQ/gerais e boletins (padrão desligado), mensagens de
  terceiros do item 54 nunca notificam.
- Notificação do sistema com ações **Responder** (resposta direta) e **Marcar como lida**;
  agrupar várias mensagens do mesmo emissor.
- Se a permissão de notificação estiver negada, mostrar aviso em Mensagens com atalho para
  concedê-la. Sem duplicar aviso de mensagem repetida (reenvio com o mesmo ID).

**Dependência:** item 54 (sem ele, a mensagem nem chega a ser reconhecida).
**Critério de aceite:** mensagem recebida com app aberto mostra banner + contador + som; com
app em segundo plano/tela apagada, notificação com som/vibração e Responder funcionando; boletim
não toca por padrão; reenvio do mesmo ID não toca de novo.
- **Status:** registrado no backlog, **não implementado**. Nenhum APK novo gerado.

### 56. Mensagens: texto digitado não aparece no campo de escrever
**Status 0.1.14:** corrigido na 0.1.14: a causa era a janela não repassar a altura do teclado ao Compose (WindowInsets.ime = 0); agora cabeçalho e barra inferior somem e o campo tem sempre uma linha inteira. Aguarda teste em aparelho.
**Validado em aparelho (10/11/2026):** Alex testou a 0.1.14 e aprovou o tamanho do campo de envio de mensagens ("ficou bom").

_Solicitado em 06/11/2026: "Backlog: Quando escrevo msg, não consigo ler o que estou escrevendo"_ — print:
[item56-campo-mensagem-ilegivel.png](docs/inspiracao/item56-campo-mensagem-ilegivel.png)
(conversa com PT2ATC-1, teclado aberto; o teclado sugere "Tes/Teste", mas no campo só aparece o cursor).

**Análise rápida (v0.1.9):** o campo é um `OutlinedTextField` em `MessagesUi.kt` (~linha 413), sem
cores próprias, ao lado do botão Enviar. No print ele está bem **mais baixo que a altura mínima
do Material (56 dp)**, então o texto provavelmente está sendo **cortado na vertical** (só o
cursor aparece), efeito colateral da compactação com teclado aberto (item 28) ou do layout que
encolhe a barra quando o teclado sobe. Conferir também contraste (texto/cursor/placeholder no
tema escuro `Pal.bar`) em aparelhos com fonte do sistema ampliada.

**Correção esperada:** texto digitado sempre legível (cor `Pal.text` explícita, contraste AA),
campo com altura mínima suficiente para 1 linha e crescendo até 4, sem cortar letras; contador
de caracteres/aviso de divisão em 67 visível; mesmo comportamento na conversa, nas mensagens
gerais (CQ) e nos boletins.
**Critério de aceite:** em 320/360/412 dp, fonte 100% e 130%, teclado aberto, o texto digitado
aparece inteiro e legível no campo.
- **Status:** registrado no backlog, **não implementado**. Nenhum APK novo gerado.

**REABERTO — P1 (10/11/2026, v0.1.13):** a correção da v0.1.10 QA (`heightIn(min = 56.dp)`, cor `Pal.text`,
1 a 4 linhas) **não resolveu**. Novo relato: _"Veja como não consigo ler o que estou escrevendo ao enviar uma
mensagem."_ Captura: [item56-reaberto-v0113.png](docs/relatos/item56-reaberto-v0113.png) (conversa com PT2YW-9,
teclado aberto; o campo virou uma faixa fina e só se vê um traço/cursor).

**Causa provável (análise do código, a confirmar no aparelho):** não é cor nem altura mínima do campo, e sim
**falta de altura na tela**. Com o teclado aberto sobram cerca de 330 dp, e quase tudo é ocupado por elementos
fixos: `TopBar()` global (indicativo + "PT2VHF APRS Client for Android" em duas linhas + Enviar beacon + linha de
chips), barra Voltar/contato, linha "Para:" e a `NavigationBar` inferior, que continua visível acima do teclado.
Na `Column` de `Conversation()`, os filhos sem peso são medidos com a altura que resta; quando ela acaba, o
último (o compositor) recebe uma altura máxima menor que 56 dp e o `heightIn(min)` é ignorado, porque não pode
passar do limite que o pai impõe. O histórico (`weight(1f)`) já fica com zero.

**Correção exigida (prioridade: o texto digitado tem de ficar legível):**
- Com o teclado aberto, **esconder a `NavigationBar`** e reduzir o `TopBar()` global a uma linha (ou escondê-lo)
  na aba Mensagens; ver #28 e #65, que tratam do mesmo espaço e devem ser feitos juntos.
- Reservar altura para o compositor **antes** do histórico (o histórico é que encolhe), com no mínimo 1 linha
  inteira visível e crescimento até 4 linhas.
- A linha "Para:" pode virar dica do campo com o teclado aberto (o destino continua visível, #20).
- Teste automatizado de layout (Compose UI test) simulando altura pequena (ex.: 320 dp de altura útil) que
  verifique que o texto digitado aparece inteiro.
**Critério de aceite:** no aparelho do relato e em 320/360/412 dp, fonte 100%/130%, teclado aberto, conversa,
Geral e Boletins: o texto digitado aparece inteiro e legível enquanto se digita.

### 57. Mensagens: balões no estilo WhatsApp com 1 ou 2 checks verdes
_Solicitado em 06/11/2026: "Backlog: formato das mensagens estilo whatsapp, com 1 ou 2 checks verdes"_

**Como está hoje (v0.1.9):** balões com uma linha de metadados com texto de status e um ícone
(`MessagesUi.kt` ~linha 293): 🕓 aguardando, ✓ ACK recebido, ✗ sem ACK, ⛔ rejeitada, ⊘ cancelada.

**Comportamento exigido:**
- Balões estilo app de conversa: enviadas à direita (cor própria do tema PT2VHF), recebidas à
  esquerda, com "rabinho", hora no canto inferior do balão e agrupamento por dia ("Hoje",
  "Ontem", data). Visual próprio, sem copiar a identidade do WhatsApp.
- Checks no canto do balão enviado:
  - 🕓 relógio: na fila / enviando.
  - **1 check (✓)**: enviada a um transporte (gravada no socket APRS-IS verificado ou aceita pelo TNC).
  - **2 checks verdes (✓✓)**: **ACK recebido** do destinatário.
  - Opcional: 1 check verde quando o próprio pacote for ouvido digipetado/gateado (eco no RF ou
    no APRS-IS), indicando que saiu no ar.
  - Vermelho/✗ para sem ACK, rejeitada ou sem conexão, mantendo Reenviar/Cancelar (item 47).
- Toque no check mostra o detalhe (tentativas, meio, hora do ACK, path).
- Mensagens sem ID (CQ/boletins) não têm ACK: mostrar só 1 check, sem prometer entrega.
- **Não** afirmar entrega/leitura sem ACK; não existe "lido" no APRS.

**Dependências:** item 54 (ACK e reply-ack `{MM}AA` corretos) e item 47.
**Critério de aceite:** mensagem pessoal mostra 🕓 → ✓ ao sair → ✓✓ verde ao chegar o ACK
(testado RF e APRS-IS); sem ACK após as tentativas mostra falha; CQ/boletim mostram só ✓.
- **Status:** registrado no backlog, **não implementado**. Nenhum APK novo gerado.

### 58. Estações, ficha da estação e comunidade inspiradas no app APRS World
**Status 0.1.14:** abas Trilha e Mensagens da ficha (58.4), painel de filtros (58.3) e Comunidade (58.5) implementados; chips de tipo com contagem sobre o mapa e busca por lugar (58.1) ainda não. Aguarda teste em aparelho.

_Solicitado em 06/11/2026: "Backlog: Aproveite os recursos do aprs world para colocar neste aplicativo. Algumas telas de exemplo"_ —
12 prints de referência em `docs/inspiracao/item58-*.png`:
[mapa](docs/inspiracao/item58-mapa.png), [lista](docs/inspiracao/item58-lista-estacoes.png),
[filtros](docs/inspiracao/item58-filtros.png), [visão geral](docs/inspiracao/item58-detalhe-visao-geral.png),
[recepção](docs/inspiracao/item58-detalhe-recepcao.png), [SSIDs e caminho](docs/inspiracao/item58-detalhe-ssids-caminho.png),
[trilha](docs/inspiracao/item58-detalhe-trilha.png), [telemetria](docs/inspiracao/item58-detalhe-telemetria.png),
[mensagens](docs/inspiracao/item58-detalhe-mensagens.png), [bruto](docs/inspiracao/item58-detalhe-bruto.png),
[comunidade](docs/inspiracao/item58-comunidade.png).

**Regra:** só ideias de recursos e organização de tela. Nada de marca, nome, logotipo, arte, ícones,
textos ou código do APRS World; visual e cores próprios do PT2VHF.

**Como está hoje (v0.1.9):** a aba Estações tem lista com busca/filtro/ordenação (item 3) e, ao tocar,
abre um **diálogo** (`StationDialog` em `Screens.kt`) com distância, rumo, posição, grid, velocidade,
comentário, via, "ouvida por RF", favorito, seguir e navegar. Não há tela de detalhe com abas, histórico
por período, telemetria, pacotes brutos por estação nem visão de comunidade.

**58.1 Mapa**
- Chips de tipo **com contagem** sobre o mapa (Todos N, Móvel N, Fixo N, Clima N, Digi, iGate…), rolagem
  horizontal; tocar filtra o mapa e a lista juntos.
- Busca única "indicativo ou lugar" (lugar via geocodificação de baixo custo/offline quando possível).
- Selo de fonte e estado ao vivo ("APRS-IS · ao vivo", "RF · ao vivo") no topo do mapa.
- Botões laterais: zoom, minha posição, camadas/estilo do mapa e um botão de **raio/alcance** com o
  número de estações dentro do raio configurado.

**58.2 Lista de estações**
- Topo com contador "visíveis / total" e botão **pausar atualização** (congela a lista para ler sem os itens pularem).
- Alternância Estações | Mensagens no topo da aba (atalho para o tráfego de mensagens das estações).
- Busca por indicativo **ou comentário**, botão de filtro com **contador de filtros ativos**.
- Chips de tipo com contagem e ordenação explícita em chips: **Recentes · Distância · Indicativo**.
- Cada linha: ícone do símbolo APRS, indicativo, **etiqueta colorida do tipo** (DIGI, IGATE, FIXAS, MÓVEL,
  CLIMA, OUTRAS), início do comentário, "há X min" e distância à direita, seta para a ficha.

**58.3 Painel de filtros (folha inferior)**
- Buscar (indicativo/comentário).
- Localização: **região** (Tudo / BR / US / … conforme prefixo ou posição) e "apenas estações com posição conhecida".
- Tipo: **SSID 0-15** em grade de seleção múltipla e **símbolo** (tabela/código, ex.: `/_`, `\&`).
- Tempo: **visto por último** (último minuto, 15 min, 1 h, 6 h, 24 h, 7 d, 30 d, tudo).
- Botões **Limpar tudo** e **Aplicar**; filtros persistidos. Complementa o item 3 (não duplicar).

**58.4 Ficha da estação (tela cheia, substitui o diálogo)**
- Cabeçalho: ícone do símbolo, indicativo base + **SSID em destaque**, bandeira/país pelo prefixo,
  etiqueta do tipo, indicador **ao vivo/inativo** + "há X min", coordenadas.
  Ações: **Seguir** (item 11/12), Rotas/Navegar, Enviar mensagem (item 19), Favorito.
- Abas: **Visão geral · Trilha · Telemetria · Mensagens · Bruto**.
- **Visão geral:** cartões Localização, Velocidade, Rumo (graus + nome: norte…), Altitude, Tipo,
  Último pacote (há X + **total de pacotes recebidos**); comentário/status expansível; e:
  - **Recepção (últimas 24 h):** distância máxima de recepção e iGate mais distante, distância do
    último salto e iGate do último salto, "ouvida por N iGates" (calculado com o path/qAR dos pacotes
    APRS-IS e com o que o próprio app ouviu por RF, separando as duas fontes — ver itens 4 e 14).
  - **Outros SSIDs do mesmo operador:** chips com cada SSID visto e "há X", tocando abre a ficha.
  - **Histórico de posição:** minimapa com os pontos das últimas 6 h e contagem de pontos.
  - **Caminho:** linha do tempo do path do último pacote (origem, WIDEn-N, digis, qAR/qAC/qAO e
    iGate), com o significado de cada salto e número de saltos.
- **Trilha:** período **1 h · 6 h · 1 d · 3 d · 7 d**, mapa com a trilha e número de pontos, resumo
  com nº de pontos e **distância total** percorrida (integra com item 5).
- **Telemetria:** gráficos de telemetria (T#) e clima (temperatura, vento, chuva, pressão, umidade)
  quando houver; senão, aviso "sem dados de telemetria ou clima".
- **Mensagens:** mensagens de/para esta estação em balões (integra itens 54/57), campo para responder.
- **Bruto:** lista dos pacotes brutos recebidos dessa estação com "há X", copiar e compartilhar
  (reaproveita a aba Logs, item 51, filtrada pelo indicativo).
- Dados vêm do histórico local (item 7) e, opcionalmente, de consulta online; sempre indicar a fonte.

**58.4.1 Telemetria e metadados detalhados (ampliação solicitada em 08/11/2026)**
_Solicitado: "Backlog: implementar gráficos de telemetria das estações, quando selecionadas, ex: se for uma wx, mostrar as informações do clima de forma amigável com gráficos, etc.. Mostrar todos os metadados da estação, quando houver."_

- **Acesso contextual:** ao selecionar qualquer estação no mapa ou na lista, abrir sua ficha completa
  com indicadores e gráficos adequados **aos dados efetivamente recebidos**, sem mostrar campos
  fictícios. Para estação WX, destacar no topo as condições meteorológicas; para outras,
  destacar telemetria técnica ou de movimento conforme disponibilidade.
- **Estações WX:** cartões com última temperatura, umidade relativa, pressão barométrica,
  vento (velocidade, direção e rajadas), precipitação (acumulados informados em 1 h/24 h/
  desde meia-noite quando transmitidos), luminosidade e demais grandezas que o protocolo
  ou o pacote efetivamente fornecer. Exibir unidades, horário da observação, idade do dado
  e indicação de sensor ausente. Não confundir medida instantânea com acumulado.
- **Gráficos históricos:** curvas de temperatura, umidade, pressão, vento/rajadas e chuva,
  com séries temporais independentes, zoom e escolha de período (1 h, 6 h, 24 h, 7 d,
  30 d, respeitando a retenção disponível); legenda, eixos com unidades, mínimos/máximos
  reais, valor ao tocar no ponto e lacunas explícitas quando não houver amostras. Para
  chuva acumulada, usar visualização coerente com o tipo de medição; **não inventar
  pontos intermediários**.
- **Telemetria APRS:** decodificar quadros T# e, quando recebidos, definições PARM.,
  UNIT., EQNS. e BITS. Exibir até 5 canais analógicos com nome, unidade, valor
  bruto e valor escalonado pela equação aplicável; bits/estados digitais com rótulos
  e histórico de mudança. Mostrar dados adicionais de bateria, voltagem, sensores
  e outros campos disponíveis, inclusive valores não reconhecidos em seção técnica.
- **Metadados completos:** exibir identificador/indicativo/SSID, símbolo APRS,
  tipo, descrição/status, posição, precisão/ambiguidade, grid, altitude, velocidade,
  rumo, primeira e última recepção, origem RF versus APRS-IS, path, digipeaters,
  iGate identificado, saltos, RSSI/SNR **somente se medidos pelo transporte**,
  frequência/PHG quando presentes, informações de equipamento/software **apenas se
  efetivamente identificadas**, último pacote bruto e histórico local com timestamps.
  Campos desconhecidos devem aparecer como "não informado", sem suposição.
- **Proveniência e persistência:** enriquecer parsing/armazenamento histórico local
  de observações meteorológicas e telemetria por indicativo/SSID, com instante do
  pacote e instante de recepção separados; deduplicar repetições, tolerar pacotes
  incompletos, preservar dados crus e aplicar retenção configurável sem afetar
  mensagens e tracklog. Não misturar valores de estações/SSIDs diferentes.
- **UX:** ficha responsiva em telas pequenas, abas Visão geral / Clima ou Telemetria /
  Trilha / Mensagens / Bruto, atualização automática com pacotes novos e estado
  "sem dados suficientes para gráfico" quando só existir uma amostra. Exportar as
  séries e metadados para CSV ou JSON se viável, com timestamps e unidades.
- **Testes de aceite:** injetar pacotes APRS WX completos/incompletos, T#,
  PARM./UNIT./EQNS./BITS. e estações sem telemetria; conferir interpretação de
  unidades e escalonamento, gráficos em diferentes períodos, persistência após
  reiniciar, dados ausentes, comparação com os pacotes brutos e distinção RF/IS.
  Testar em Android físico com tráfego de estação WX real.
- **Situação:** detalhamento incorporado ao **item #58**; ainda não implementado
  nem homologado. Não criar item paralelo, pois complementa a ficha de estação
  e a aba Telemetria já previstas no #58.


**58.5 Comunidade / rodadas APRS**
- Nova área "Comunidade" com rodadas semanais (ex.: rodadas de check-in por mensagem APRS como a
  "Thursday Net") e rodadas configuráveis pelo usuário (indicativo/palavra-chave de check-in, dia, horário).
- Seletor de data da rodada, busca de indicativo, cartões **Participantes** e **Países**,
  **classificação por país** (bandeira + quantidade) e lista de participantes por país.
- Opção de fazer **check-in** na rodada pelo app (mensagem APRS pré-preenchida, com confirmação antes de transmitir).
- Fonte dos dados: mensagens ouvidas pelo app/APRS-IS ou fonte pública da rodada, se existir e permitir;
  avaliar antes de implementar.

**58.6 Reforço solicitado em 10/11/2026**
_Solicitado: "Backlog: Alguns itens que gostei são as informações sobre as estações, caminhos, gráficos de
telemetria, etc.. Ordenação das estações, por tipo, nome, distância e clicando de novo em cima da ordem, a
ordem é invertida. Ícones das estações no mapa são maiores e mais nítidas."_ Referência: as mesmas 11 telas de
`docs/inspiracao/item58-*.png` (reenviadas, idênticas).

- **Prioridade dentro do #58:** a ficha da estação com informações completas (58.4), o **caminho** do
  pacote (saltos, digis, iGate) e os **gráficos de telemetria/clima** (58.4.1) são as partes preferidas pelo
  mantenedor e devem vir primeiro quando o #58 for implementado.
- **Ordenação da lista de estações em um toque:** opções visíveis (chips ou cabeçalho) **Tipo · Nome
  (indicativo) · Distância**, além de Recentes já prevista em 58.2. Tocar em outra opção ordena por ela;
  **tocar de novo na opção ativa inverte a ordem** (A-Z ↔ Z-A, mais perto ↔ mais longe, tipo crescente ↔
  decrescente), com seta ↑/↓ indicando o sentido e nome acessível ("Distância, crescente").
  - **Tipo:** agrupar pelo tipo da estação (Móvel, Fixa, Clima, Digi, iGate, Objeto/Item, Outras) e, dentro
    do grupo, por indicativo; ordem dos tipos estável e documentada.
  - Desempate estável por indicativo; estações sem posição ficam no fim ao ordenar por distância, nos dois
    sentidos, com "—".
  - Critério e sentido persistidos entre sessões e reaplicados em tempo real quando chegam pacotes
    (mesmas regras do item 3; **não duplicar** o painel do item 3, só oferecer o atalho de um toque).
- **Ícones no mapa:** ver item **#63** (ícones maiores e mais nítidos).
- **Testes de aceite:** alternar cada critério e inverter com segundo toque; conferir seta, persistência após
  reiniciar e reordenação com pacotes novos; distância sem GPS; telas 320/360/412 dp e fonte ampliada.

**Dependências/relacionados:** itens 3, 5, 7, 11, 12, 14, 19, 51, 54, 57.
**Critério de aceite:** lista com chips + contadores, ordenação e painel de filtros completo funcionando
em tempo real; toque abre a ficha em tela cheia com as 5 abas preenchidas a partir do histórico local;
recepção/caminho coerentes com os pacotes brutos; trilha por período desenhada; comunidade mostrando
participantes e países de uma rodada. Nenhum elemento visual copiado do app de referência.
- **Status:** **58.6 implementado em 10/11/2026** na [PR #19](https://github.com/alexpmr/PT2VHF-APRS-Android/pull/19)
  (draft, empilhada sobre a #17): ficha em tela cheia (Visão geral com recepção 24 h e outros SSIDs, Caminho,
  Telemetria e clima com gráficos, Bruto) e ordenação Tipo/Nome/Distância/Recentes com toque para inverter.
  Ficam para depois: abas Trilha e Mensagens da ficha, chips com contagem no mapa, filtros 58.3 e Comunidade 58.5.
  Aguarda CI, teste físico e OK do mantenedor; nenhuma versão publicada.

### 59. Indicativo: aceitar só indicativos de radioamador válidos no mundo
**Status 0.1.14:** implementado na 0.1.14: tabela de blocos de prefixos da ITU embarcada, motivo da recusa, país do prefixo, aviso de indicativo salvo/backup inválido e aviso (sem bloqueio) para destinos táticos. Estado (c), cadastro oficial, não implementado.

_Solicitado em 07/11/2026: "Backlog: Permitir apenas indicativos válidos no mundo, para evitar de qualquer pessoa escrever uma palavra e o app aceitar."_

**Como está hoje (v0.1.9):** Configurações valida o indicativo só com `CALLSIGN_RE`
(`core/Aprs.kt`: `^[A-Z0-9]{1,6}(-([0-9]|1[0-5]))?$`), ou seja, aceita qualquer palavra de 1 a 6
letras/números (ex.: `CASA`, `TESTE`, `ABC`), desde que o SSID fique entre 0 e 15.

**O que fazer**
- Validar o **formato** do indicativo base conforme a estrutura internacional de radioamador:
  prefixo (1 a 3 caracteres) + **um algarismo** + sufixo de 1 a 4 letras (ex.: `PT2VHF`, `PY2ABC`,
  `K1ABC`, `W1AW`, `DL1ABC`, `2E0ABC`, `VK2ABC`), com SSID opcional `-0` a `-15`.
- Validar o **prefixo** contra a tabela de blocos de prefixos da ITU (alocações por país, incluindo
  prefixos especiais/eventuais usados por radioamadores). Tabela embarcada no app (sem internet),
  em arquivo de dados próprio, fácil de atualizar; a mesma tabela serve para bandeira/país do item 58.
- Rejeitar palavras arbitrárias e formatos sem algarismo ou sem sufixo, com mensagem clara
  ("Indicativo inválido: use o seu indicativo de radioamador, ex.: PT2VHF-9") nos 5 idiomas.
- Aplicar no indicativo do usuário (Configurações, perfis do item 10, importação de backup do item 50
  e assistente de primeiro uso). Indicativo inválido não conecta ao APRS-IS nem transmite por RF.
- Configurações antigas com indicativo inválido: avisar e pedir correção, sem apagar o valor salvo.
- Campos de destino de mensagem (Nova mensagem, item 21) e busca: **avisar** quando o formato não
  parecer indicativo, mas sem bloquear, porque o APRS também usa destinos táticos/objetos (ex.: `CQ`,
  `BLN1`, `EMAIL`, `WLNK-1`, nomes de serviço). Pacotes recebidos de outras estações não são filtrados.
- Casos especiais a avaliar: indicativos com barra (`PT2VHF/P`, `CT/PY2ABC`) não cabem no campo de
  origem AX.25 (6 caracteres); orientar o uso do SSID. Indicativos de escuta/SWL e de clubes seguem
  a mesma regra de formato.

**Testes:** unidade no core JVM com lista de válidos (vários países, prefixos com algarismo inicial,
SSID 0-15) e inválidos (`CASA`, `TESTE`, `ABC123`, `12345`, `PT2VHF-16`, prefixo inexistente).
**Critério de aceite:** palavras comuns são recusadas com mensagem clara; indicativos reais de vários
países são aceitos com e sem SSID; mensagens para destinos táticos continuam possíveis.
**Complemento de 08/11/2026 — requisito reforçado pelo mantenedor:**
"Permitir apenas indicativos válidos do mundo todo, para evitar que coloquem qualquer palavra e aceite."

- **Não usar uma expressão regular genérica como único critério.** Validar sintaxe e comprimento
  compatíveis com APRS/AX.25, SSID de 0 a 15, alocações internacionais de prefixos
  (ITU) e regras nacionais quando disponíveis; manter lista de exceções para
  prefixos especiais, clubes, indicativos temporários e formatos legítimos
  de países diferentes. Não aplicar a regra brasileira a todos os países.
- **Distinguir três estados:** (a) formato inválido/rejeitado,
  (b) formato e prefixo plausíveis mas titularidade não verificada,
  (c) indicativo efetivamente confirmado por cadastro oficial disponível.
  Prefixo ITU válido **não comprova** que o indicativo tenha sido atribuído.
  Não prometer uma base mundial completa de licenças, pois nem todos os
  reguladores oferecem cadastros públicos ou APIs.
- **Operação sem rede:** base local versionada, atualizável junto com o aplicativo;
  se houver fonte oficial confiável para determinada jurisdição, admitir consulta
  opcional, com cache, proteção de privacidade e tratamento de indisponibilidade.
  Ausência de cadastro online não deve rejeitar automaticamente um indicativo
  internacional legítimo que passou nas regras documentadas.
- **Separar identidade TX de destinos:** bloquear configuração do próprio indicativo
  sintaticamente inválido e uso indevido no transmissor; manter suporte a destinos
  de mensagens APRS e identificadores táticos válidos que não são licenças
  de radioamador (CQ, boletins, serviços, objetos etc.), sem impedir RX de terceiros.
- **Critério mínimo de aceitação:** `CASA`, `TESTE`, palavras arbitrárias e prefixos
  inexistentes são recusados; exemplos legítimos de diferentes países (inclusive
  indicativos especiais e com dígito inicial) não sofrem falsos bloqueios.
  Retornar motivo claro, país/prefixo identificado quando possível, e testes
  automatizados com casos positivos, negativos e limítrofes.

**Complemento de 10/11/2026 — terceiro pedido do mantenedor:**
"Backlog: apenas permitir indicativos válidos no mundo, para evitar que qualquer pessoa use qualquer
palavra e consiga entrar."

- **Objetivo explícito: impedir a entrada.** Com indicativo inválido o app não deve concluir a
  configuração nem **entrar** em nada: sem login/conexão APRS-IS, sem TX por RF/TNC/AFSK, sem beacon.
  O app não tem conta própria; "entrar" = conectar ao APRS-IS e transmitir.
- **O passcode não protege:** `aprsPasscode()` (`core/Aprs.kt`) gera o passcode a partir do próprio
  indicativo, com algoritmo público. Qualquer palavra recebe passcode "verificado" pelo servidor, então
  a única barreira possível no app é a validação do indicativo antes de conectar.
- **SSID:** manter `-0` a `-15` (padrão APRS), validado à parte do indicativo base; o passcode
  continua calculado sobre o indicativo sem SSID.
- **Situação nas branches de QA (PR #16, v0.1.12, não publicada):** já existe
  `plausibleAmateurCallsign()` (formato prefixo + algarismo + sufixo, aceita dígito inicial como `2E0ABC`),
  usada em Configurações e em `Settings.configured`, que bloqueia a conexão. Recusa `CASA`/`TESTE`,
  mas **aceita** palavras com algarismo no meio (ex.: `AB1CD`, `XX9XX`) e prefixos inexistentes.
  **Falta:** tabela embarcada de blocos de prefixos ITU (com prefixos de algarismo inicial e exceções),
  motivo da recusa e país identificado, aviso claro quando o indicativo já salvo for inválido
  (hoje o app apenas não conecta), validação na importação de backup (#50)
  e testes com prefixos não alocados.
- **Próximo passo quando for implementado:** partir da branch da PR #16, não da `main`.

- **Status:** requisito reforçado três vezes (07/11, 08/11 e 10/11); **parcial** na PR #16
  (só formato); tabela ITU **não implementada**. Nenhum APK novo gerado.

### 60. Mensagens: envio não conclui, permanece tentando indefinidamente (P0)
_Solicitado em 08/11/2026: "Backlog: O app não consegue enviar msg...fica tentando.."_

**Falha relatada (v0.1.9; reprodução ainda não coletada):** ao enviar mensagem, a interface permanece
no estado de tentativa/envio e o usuário não consegue confirmar que a mensagem saiu. **Não assumir**
que seja apenas ausência de ACK: pode ser falha antes da escrita no transporte, ausência de resultado
do serviço, fila travada, retransmissão sem limite ou mensagem transmitida mas sem ACK remoto.

**Prioridade:** **P0 — bloqueador do uso básico do APRS Client Android.**
**Relacionados:** #25 (APRS-IS conectado mas sem TX), #37 (queda de APRS-IS durante envio),
#47 (tentativas/ACK no balão), #54 (mensagens RF↔iGate↔APRS-IS, reply-ack) e #56 (campo de composição).

**Atualização do teste em 08/11/2026:** o mantenedor tentou enviar mensagem para uma estação
sabidamente online e acessível tanto por RF quanto por APRS-IS. O aplicativo permaneceu tentando,
sem concluir o envio. O relato **não identifica qual transporte foi efetivamente utilizado** nem se
houve escrita no socket/TNC, recepção no destino ou ACK. Registrar esses eventos separadamente para
isolar falha de TX, roteamento, confirmação ou interface. É evidência adicional de falha funcional,
mas não prova ainda a causa raiz.

**Diagnóstico e correção exigidos:**
- Reproduzir com **APRS-IS verificado e TX via Internet ativada**, depois repetir por TNC KISS/AFSK
  em ambiente controlado; separar falha de APRS-IS, RF e falha de interface.
- Rastrear ponta a ponta: clique Enviar → requestId → despacho da Intent → serviço →
  fila serial TX → socket APRS-IS/TNC → resultado entregue à UI → ID da mensagem →
  recepção de ACK/REJ (quando aplicável). Cada etapa deve ter estado e erro observáveis.
- Checar pendingId/OutboundSendResult em ui/MessagesUi.kt, ACTION_MESSAGE,
  txWorker e tratamento de exceções em AprsService.kt, AprsIsClient.send,
  estados de conexão/autenticação, persistência de retries e processamento do ACK.
- **Timeout explícito** para submissão ao serviço/transporte; se falhar, exibir motivo concreto,
  desbloquear a composição e preservar texto/destino. Não deixar o botão nem o balão
  eternamente em "Enviando..." após falha ou interrupção do serviço.
- Distinguir claramente **Na fila**, **Enviando**, **Enviado ao transporte (sem ACK)**,
  **Confirmado por ACK**, **Rejeitado (REJ)** e **Falha / sem ACK**. Escrita no socket
  **não** equivale a confirmação de recepção; mensagens sem ID não têm ACK.
- Limitar tentativas ao número e intervalo configurados (#47), cancelar ao obter ACK/REJ,
  impedir reenvio paralelo/duplicado após reconexão e permitir **Reenviar** manual.
- Registrar no Logs/diagnóstico meio, requestId, ID APRS, tentativas, último erro,
  autenticação verificada, status de socket e mudanças de estado, **sem expor credenciais**.

**Critérios de aceite:** em dois clientes reais (um APRS-IS e outro RF via iGate),
uma mensagem passa de Enviando → Enviado → ACK confirmado, quando o destinatário
responde com ACK; se a conexão falhar, retornar erro e liberar a composição;
sem ACK, esgotar o limite e mostrar "Sem ACK", sem loop infinito. Verificar
envio após troca Wi-Fi/dados móveis, queda/reconexão e app em primeiro/segundo plano.
Registrar pacotes brutos e carimbos de tempo do emissor e receptor para distinguir
falha de TX, roteamento e ACK. Cobrir com testes de unidade/instrumentação.
**Progresso em 10/11/2026 (branch claude/project-thread-tny6jj, sobre a QA 0.1.12):** causa adicional
comprovada no código: retentativas vivem só no `Handler` do serviço; se o serviço parar ou o processo morrer, a
mensagem ficava "Enviando"/"Aguardando ACK" para sempre. Agora vira "Interrompida" (falha com Reenviar) ao parar
o serviço e ao reabrir o app. Teste de unidade em `MessageLogicTest`.
- **Status:** relatado e registrado; **não corrigido, não homologado e nenhum APK novo gerado**.

### 61. Estações: ícone de lixeira para apagar estações armazenadas
_Solicitado em 08/11/2026: "Backlog: implementar apagar estações com ícone de lixeira na aba de estações"._

**Como está na v0.1.9:** já há exclusão de todas as estações em
Configurações → Dados locais → Apagar todas as estações, usando
AprsRepository.clearStations() e StationStore.clearStationsAndLinks().
A aba Estações (StationsScreen em ui/Screens.kt) ainda não dispõe de acesso direto
à operação por ícone de lixeira.

**Comportamento solicitado:**
- Incluir **ícone de lixeira** na barra de ações da aba **Estações**, ao lado
  dos filtros/busca, com rótulo acessível "Apagar estações".
- Ao tocar, abrir confirmação clara: **"Apagar todas as estações armazenadas?"**,
  com quantidade total e botões **Cancelar** / **Apagar**. A exclusão é apenas
  local, não afeta estações na rede APRS.
- Com filtros aplicados, explicitar que a operação apaga **todas as estações
  armazenadas**, não apenas as visíveis no filtro; não executar silenciosamente
  exclusão parcial ou por critério de filtro.
- Reutilizar a função transacional de Configurações; eliminar também enlaces RF
  derivados do cache, sem apagar mensagens, preferências, favoritos, rastreamento
  GPS, histórico próprio ou configurações de conexão.
- Atualizar a lista e o contador imediatamente após sucesso; mostrar mensagem
  de resultado ou erro. Se APRS-IS/TNC continuarem ativos, novas estações
  recebidas podem reaparecer; explicar isso na confirmação.
- Evitar cliques duplicados, permitir cancelar e traduzir os textos em
  português, inglês, espanhol, francês e italiano.

**Critérios de aceite:** apagar com e sem filtros ativos, confirmar/cancelar,
verificar lista vazia e banco após reiniciar, preservação dos outros dados;
com conexão ativa, verificar reaparecimento apenas por novos pacotes.
- **Status:** registrado no backlog; **interface ainda não implementada**,
  nenhum APK novo gerado.

### 62. P0 — Falha crítica: fechar ao apagar mensagens ou estações (v0.1.10 QA)
_Relatado em 08/11/2026 após uso real da beta. Ao apagar mensagem ou estação, o aplicativo encerra._

**Impacto:** crash no aparelho e risco operacional na exclusão. A causa exata depende
da pilha de exceção ainda não coletada. Registrado separadamente do #60 (envio não concluído).

**Contenção:** retirar temporariamente a v0.1.10 QA do manifesto oficial; manter o
download de v0.1.9, sem orientar desinstalação com perda de dados.

**Candidata de correção v0.1.11 (branch hotfix/android-0111-deletion-crash):**
- Persistir no SQLite antes de alterar os dados em memória; se houver erro, preservar a lista.
- Exclusão de mensagens por IDs do momento da confirmação, preservando novas recepções.
- Não apagar favoritos, configurações ou tracklog próprio ao limpar estações.
- Erros recuperáveis passam a ser incluídos no relatório **Sobre → Enviar log**.
- Confirmar sucesso com testes de unidade e de integração em aparelho físico.

**Critérios de aceite (ainda pendentes):**
- Excluir todas as estações com filtros ativos/inativos e conexão RF/APRS-IS ativa/inativa.
- Excluir mensagens individuais/conversas, todas, privadas e boletins, com atualização
  simultânea por recepção e reenvio; reiniciar e verificar banco de dados.
- Nenhum crash, nenhum dado perdido após erro SQLite, nenhuma exclusão de dado recém-chegado.
- Coletar CrashLog e/ou `adb logcat` com stack trace para confirmar a causa raiz.

**Situação:** correção preventiva em branch; não homologada.

### 63. Mapa: ícones das estações maiores e mais nítidos
_Solicitado em 10/11/2026 (junto com o reforço do #58): "Ícones das estações no mapa são maiores e mais
nítidas."_ Referência funcional: [mapa](docs/inspiracao/item58-mapa.png). Só a ideia; arte e símbolos
continuam os do próprio app (item 1).

**Problema:** os símbolos APRS no mapa aparecem pequenos e pouco nítidos (serrilhados/borrados), difíceis de
reconhecer, principalmente em telas de alta densidade e com o mapa em movimento.

**Comportamento exigido:**
- **Tamanho maior por padrão** (alvo ~32-36 dp por símbolo, contra o tamanho atual) e opção em Configurações
  → Mapa: **Tamanho dos ícones** Pequeno / Médio / Grande (padrão Médio), aplicada na hora e persistida.
- **Nitidez:** desenhar os símbolos na resolução real da tela (densidade do aparelho), a partir de fonte de
  alta resolução (sprites em resolução maior ou vetor), sem ampliar bitmap pequeno; filtragem adequada ao
  redimensionar; mesma nitidez em overlays (letra/número sobre o símbolo) e no "?" de símbolo desconhecido.
- **Legibilidade:** contorno/sombra leve para destacar o ícone sobre qualquer tile e com o brilho do mapa
  (item 22); rótulo do indicativo (camada "Rótulos", item 1) proporcional ao novo tamanho.
- **Área de toque** de pelo menos 48 dp por estação; em áreas cheias, evitar que ícones maiores escondam
  vizinhos (deslocamento leve ou prioridade para a estação selecionada/seguida/favorita).
- **Desempenho:** cache dos bitmaps por símbolo+overlay+tamanho+densidade; sem queda de fluidez com
  centenas de estações; memória sob controle.
- Mesma regra para o próprio ícone, objetos/itens (item 48), estação seguida (item 11) e widget/modo carro
  quando usarem os mesmos símbolos.

**Dependências/relacionados:** itens 1, 22, 48, 58.
**Critérios de aceite:** comparação antes/depois em telas de baixa e alta densidade (mdpi a xxxhdpi),
zoom próximo e distante, três tamanhos, tema/brilho diferentes, símbolos das tabelas primária e alternativa
com overlay; capturas anexadas; toque correto em estações próximas; rolagem fluida com muitas estações.
- **Status:** **implementado em 10/11/2026** na [PR #19](https://github.com/alexpmr/PT2VHF-APRS-Android/pull/19):
  atlas de 128 px reduzido com filtragem para a densidade real, sombra leve, Pequeno 26 / Médio 34 (padrão) /
  Grande 44 dp, rótulo proporcional, toque de 48 dp, seguida/favoritas por cima. Aguarda CI, capturas e teste
  físico; nenhuma versão publicada.
- Numeração: o **#62** está reservado ao P0 registrado na PR #16 (v0.1.10/0.1.12 QA), por isso este item é o #63.

### 64. Mensagens: tela não atualiza sozinha com mensagens novas
**Status 0.1.14:** implementado na 0.1.14: listas acompanham a mensagem nova ou mostram "Nova mensagem ↑/↓". Aguarda teste em aparelho (inclusive com tela desligada).

_Relatado em 10/11/2026 (v0.1.13): "tela de mensagens não está atualizando em background. Só atualiza quando eu
mudo para outra aba como mapa e volto."_

**Problema:** com a aba Mensagens aberta, mensagens que chegam (APRS-IS ou RF) não aparecem na tela; elas só
surgem depois de trocar de aba (ex.: Mapa) e voltar. Não confundir com #55 (aviso de chegada) nem com #2/#23
(ordenação): aqui a mensagem já está no histórico, mas a tela não mostra.

**Investigação inicial no código (hipóteses, não confirmadas em aparelho):**
- O fluxo de dados é reativo: `AprsRepository.add()` faz `_messages.update { it + message }` e
  `MessagesScreen`/`Conversation` observam `AprsRepository.messages.collectAsState()`; o serviço roda no mesmo
  processo. Não há recarga do SQLite ao trocar de aba (`init()` só roda uma vez). Ou seja, a lista em memória
  provavelmente já está atualizada e o problema é de apresentação.
- **Causa mais provável — lista Conversas:** `ui/MessagesUi.kt` usa `LazyColumn` sem estado próprio com
  `items(byContact, key = { it.first })`, ordenada da conversa mais recente para a mais antiga (padrão). O Compose
  preserva a posição de rolagem pela **chave** do primeiro item visível: quando chega mensagem de um contato novo
  (entra no índice 0) ou um contato existente sobe para o topo, a lista continua ancorada no item que estava no
  topo e a conversa atualizada fica **acima da área visível**, parecendo que nada mudou. Ao sair da aba e voltar,
  o estado da lista é recriado no índice 0 e a mensagem "aparece". Ao trocar de aba o app também fecha a conversa
  aberta (`App.kt`: `chatWith = null`), o que reforça a impressão de "recarregar".
- **Conversa aberta / Geral / Boletins:** a rolagem automática depende de `LaunchedEffect(list.size)`; ela não
  dispara quando o tamanho não muda (ex.: limpeza por retenção no mesmo instante) e rola sempre, mesmo quando o
  usuário está lendo mensagens antigas. Verificar se a mensagem nova entra fora da área visível sem indicação.
- Se o relato for com o app **em segundo plano** (tela desligada) e a tela não mostrar as novas ao voltar,
  verificar também se o serviço continua recebendo nesse período (economia de bateria/Doze) e se o
  `collectAsState` retoma ao voltar ao primeiro plano.

**Comportamento exigido:**
- Mensagem nova aparece **na hora** na aba aberta (Conversas, conversa privada, Geral e Boletins), sem trocar de aba.
- Na lista Conversas, se o usuário estiver no topo, manter o topo visível (rolar para a conversa nova/atualizada);
  se estiver rolado para baixo, não pular a tela, mas mostrar indicação "nova mensagem ↑" que leva ao topo.
- Na conversa e nos chats de grupo, rolar para a nova só se o usuário já estiver no fim; caso contrário, mostrar
  indicação "nova mensagem ↓".
- Atualização de status (ACK, retentativa, falha) refletida no balão sem sair da tela.

**Critérios de aceite:** com a aba Mensagens aberta em cada sub-aba e dentro de uma conversa, receber mensagens
por APRS-IS e por RF (contato novo, contato existente fora do topo, CQ e boletim) e ver a atualização sem trocar
de aba; repetir com app em segundo plano e tela desligada, voltando ao app; teste de unidade/UI que simule a
chegada de mensagem com a lista rolada.
- **Status:** registrado em 10/11/2026; não implementado.

### 65. Mensagens: tela mais compacta (cabeçalho menor e ícone de enviar)
**Status 0.1.14:** implementado na 0.1.14: cabeçalho em duas linhas baixas só com o indicativo, Voltar e Enviar em ícone. Aguarda captura antes/depois em aparelho.

_Solicitado em 10/11/2026 (v0.1.13), com [captura](docs/relatos/item65-mensagens.png) de uma conversa aberta:
"otimize essa tela para caber mais coisas. A parte superior está ocupando 1/4 de toda a tela. Não precisa da
descrição da estação, só do indicativo dela. 'enviar' pode ser substituído por um ícone pequeno, de enviar,
usando em outros apps."_

**Problema (na captura):** acima das mensagens ficam o indicativo (PT2YW-9), o texto "PT2VHF APRS Client for
Android" quebrado em três linhas ao lado do botão Enviar beacon, a linha de chips APRS-IS/TNC/GPS e a barra
"‹ Voltar PT2VHF-9", cerca de 1/4 da tela. Embaixo, a linha "Para:", o campo de duas linhas e o botão "Enviar"
largo tomam mais espaço. Sobram só três balões visíveis.

**Onde está no código:** `ui/App.kt` → `TopBar()`, que é global (indicativo + texto fixo "PT2VHF APRS Client for
Android", Enviar beacon, Configurações e, em outra linha, os chips). `ui/MessagesUi.kt` → `Conversation()` (barra
Voltar + contato) e `Composer()` (linha "Para:", `OutlinedTextField` e `Button` com o texto `R.string.send`).

**Comportamento exigido:**
- **Cabeçalho:** mostrar só o **indicativo**, sem o texto "PT2VHF APRS Client for Android" (manter o aviso de
  perfil "Somente recepção" de forma curta, ex.: selo pequeno ao lado do indicativo). Indicativo, Enviar beacon,
  Configurações e chips APRS-IS/TNC/GPS em **uma ou no máximo duas linhas baixas**, sem quebra de texto. Avaliar
  levar o mesmo cabeçalho compacto às demais abas, para não "pular" ao trocar de aba.
- **Barra da conversa:** Voltar como ícone (←) na mesma linha do contato, altura reduzida; tocar no contato
  continua abrindo a ficha da estação.
- **Enviar:** trocar o botão "Enviar" por **ícone pequeno de enviar** (avião de papel, `Icons.AutoMirrored.Filled.Send`),
  como em apps de mensagens, com área de toque de 48 dp, `contentDescription` "Enviar" e estados
  desabilitado/enviando visíveis (ex.: indicador de progresso no lugar do ícone).
- **Compositor:** campo de uma linha que cresce até 3–5 linhas ao digitar; a linha "Para:" fica menor ou vira
  dica do próprio campo, sem perder o destino visível (exigência do #20).
- Resultado esperado: bem mais balões visíveis na mesma tela; nada cortado em 320/360/412 dp e fonte ampliada.

**Relacionados:** #28 (espaço com teclado aberto; este item vale também com o teclado fechado), #24 (largura dos
balões), #36 (estados do Enviar beacon, que precisam continuar visíveis no cabeçalho compacto) e #64.

**Critérios de aceite:** captura antes/depois na mesma conversa mostrando mais mensagens; teclado aberto e
fechado; retrato e paisagem; fonte 150%/200%; TalkBack lê "Enviar" e "Voltar"; chips e Enviar beacon continuam
funcionando.
- **Status:** registrado em 10/11/2026; não implementado.

### 66. Mapa: agrupar estações em áreas cheias
**Status 0.1.14:** implementado na 0.1.14 (camada "Agrupar estações próximas", ligada por padrão). Aguarda teste em aparelho.

_Registrado em 10/11/2026 a partir das ideias do APRS World aprovadas pelo mantenedor ("Implemente os 4 itens")._

- Com muitas estações próximas, juntar os ícones que se sobrepõem num **círculo com a contagem** conforme o zoom;
  tocar no grupo aproxima o mapa até separar as estações. Estação seguida, favoritas e a própria posição nunca
  entram em grupo.
- Opção na lista de camadas do mapa ("Agrupar estações"), ligada por padrão e lembrada entre sessões.
- **Teste:** unidade do agrupamento (grade em pixels por zoom, estações isoladas continuam soltas).

### 67. Mapa: camada de clima com as estações meteorológicas
**Status 0.1.14:** implementado na 0.1.14 (camada "Clima", desligada por padrão). Aguarda teste em aparelho.

_Registrado em 10/11/2026 (ideia do APRS World aprovada)._

- Camada opcional que mostra, sobre cada estação WX recebida, a **temperatura** e o **vento** do último relatório
  (dados APRS próprios, sem serviço externo nem radar de terceiros). Valores antigos (mais de 2 h) aparecem
  esmaecidos; estação sem dado de clima não ganha rótulo.
- **Teste:** unidade da formatação do rótulo (temperatura em °C, vento em km/h e rumo, dado ausente).

### 68. Android Auto
**Status 0.1.15:** continua fora (mesmos motivos); o modo carro do app segue valendo.
**Status 0.1.14:** **não implementado na 0.1.14** (exige biblioteca Car App, aprovação de categoria pelo Google e teste em carro/DHU).

_Registrado em 10/11/2026 (ideia do APRS World aprovada)._

- Tela própria no painel do carro (mapa/lista das estações próximas, última mensagem, beacon agora).
- **Atenção:** exige a biblioteca Car App do Android, categoria de app aceita pelo Google e, para APK fora da
  Play Store, o modo desenvolvedor do Android Auto. Precisa de teste num carro ou na unidade principal de
  desenvolvimento (DHU). O "modo carro" atual do app continua valendo enquanto isso.

### 69. Ferramentas: decodificador de pacote APRS e tabela de frequências
**Status 0.1.14:** implementado na 0.1.14 (botão 🛠 na aba Logs e "Decodificar" nos detalhes do pacote). Mic-E e posição comprimida ainda só são identificados, não decodificados.

_Registrado em 10/11/2026 (ideia do APRS World aprovada)._

- **Decodificador:** colar ou digitar um pacote bruto (`ORIGEM>DESTINO,PATH:info`) e ver campo a campo: origem,
  destino, caminho com o significado de cada salto, tipo do pacote, posição, símbolo, curso/velocidade,
  altitude, clima, telemetria, mensagem/ACK e comentário. Nada é transmitido.
- Atalho "Decodificar" a partir de um pacote da aba Logs e da aba Bruto da ficha.
- **Tabela de frequências APRS** por região (ex.: 145,570 MHz no Brasil, 144,390 MHz nas Américas do Norte,
  144,800 MHz na Europa/África), com aviso para conferir o plano de banda local.
- **Teste:** unidade do decodificador com pacotes de posição, Mic-E/sem posição, clima, telemetria e mensagem.

### 70. Importar configurações do APRSdroid
**Status 0.1.14:** implementado na 0.1.14 (Configurações → Backup → Importar). Precisa de teste com um arquivo real exportado do APRSdroid.

_Registrado em 10/11/2026 (ideia do APRS World aprovada)._

- Em Configurações → Backup, aceitar também o arquivo de perfil/preferências do APRSdroid e aproveitar o que
  tem equivalente: indicativo, SSID, símbolo, comentário/status, caminho digipeater, servidor e filtro APRS-IS,
  intervalo de beacon e SmartBeaconing ligado/desligado.
- Mostrar antes de aplicar o que foi reconhecido; não importar passcode (é calculado do indicativo) e validar o
  indicativo pela regra do #59. Chaves desconhecidas são ignoradas, sem apagar as configurações atuais.
- **Teste:** unidade da conversão com arquivo XML de preferências e com JSON de perfil.

### 71. ~~Ícones com fundo transparente~~ (CANCELADO — não há fundo branco; engano confirmado pelo mantenedor em 10/11/2026)
_Relatado em 10/11/2026 (após a v0.1.14): "ícones com fundo transparente. Atualmente estão com fundo branco."_

**Problema:** o mantenedor vê os ícones com fundo branco; devem aparecer com fundo transparente, deixando ver
o mapa ou o fundo da tela por trás.

**O que o código mostra hoje (verificado em 10/11/2026, v0.1.14):**
- Os atlas de símbolos (`aprs-symbols-128-0/1.png`, hessu/aprs-symbols) são RGBA com transparência fora do
  desenho, e `AprsSymbolIcons` recorta com `ARGB_8888`, sem pintar fundo. No mapa, `mapIcon` só acrescenta a
  sombra (#63); nas listas, ficha, objeto e seletor o `Image` não tem fundo próprio (o seletor usa o fundo do
  tema, `Pal.ground`/`Pal.mine`, que é escuro).
- Alguns símbolos têm uma **área branca que faz parte do próprio desenho** (cerca de 25-30 % do ícone):
  tabela primária `/0`, `/2`, `/3`, `/5`, `/6`, `/8`, `/9`, `/c` e alternativa `\+`, `\A`, `\Q`, `\R`
  (círculos/quadrados numerados, caixa, etc.). Provável origem do "fundo branco" percebido.
- O ícone do app (launcher) usa fundo `ground` (#0B131C, escuro) e o widget tem fundo próprio (`widget_bg`).

**Comportamento exigido:**
1. Confirmar com o mantenedor **quais ícones e em qual tela** aparecem com fundo branco (captura de tela), e
   em qual aparelho/tema.
2. Nenhum ícone de estação, objeto, seletor de símbolo ou ficha pode ter quadrado/círculo branco que não seja
   parte do desenho: o que estiver fora do símbolo fica transparente, no mapa e nas listas, em tema claro e
   escuro.
3. Se o branco for do próprio desenho dos símbolos, decidir com o mantenedor entre manter a arte original
   APRS, substituir o branco interno por transparente/cor do tema ou usar outra fonte de símbolos.
4. Se a queixa for do ícone do app ou do widget, usar ícone adaptativo/monocromático com fundo adequado ao
   lançador.

**Critérios de aceite:** capturas antes/depois no mapa (tiles claros e escuros), lista de estações, ficha e
seletor de símbolo; símbolos das duas tabelas e com overlay; tema claro/escuro; sem regressão de nitidez (#63).

**Dependências/relacionados:** itens 1, 58, 63.
- **Status:** **cancelado em 10/11/2026** pelo mantenedor: "Esquece o background branco. Falha minha. Está tudo certo." Nada a implementar.

### 72. Estações: campo de busca corta o texto digitado
**Status 0.1.15:** implementado (campo compacto `CompactSearchField` em Estações, Logs e Mensagens, só com altura mínima). Aguarda teste em aparelho com fonte grande.
_Relatado em 10/11/2026 (após a v0.1.14), com captura: "campo na aba estações está cortando."_

**Problema:** na aba Estações, o texto digitado no campo de busca aparece cortado na parte de baixo (na
captura, "pt2" fica com a base das letras escondida). O campo está baixo demais para o texto.

**Causa provável (verificado no código em 10/11/2026, v0.1.14):** `ui/Screens.kt`, cabeçalho da lista de
estações (~linha 648): o `OutlinedTextField` da busca tem `Modifier.weight(1f).height(48.dp)`. O
`OutlinedTextField` do Material 3 tem altura mínima de 56 dp com preenchimento interno de 16 dp em cima e
embaixo; forçado a 48 dp, a área do texto fica menor que a linha e a base é recortada. O placeholder usa
`fontSize = 12.sp`, mas o texto digitado usa o tamanho padrão (16 sp), o que piora o corte.

**Comportamento exigido:**
1. O texto digitado e o placeholder aparecem inteiros, centralizados na vertical, em qualquer tamanho de fonte
   do sistema (incluindo fonte grande de acessibilidade).
2. Manter o cabeçalho compacto (#58/#65): campo, botão de filtro ☷ e lixeira 🗑 na mesma linha.
3. Solução sugerida: tirar a altura fixa (usar `heightIn(min = …)`) ou trocar por um campo compacto
   (`BasicTextField` com `OutlinedTextFieldDefaults.DecorationBox` e `contentPadding` menor), com
   `textStyle` no mesmo tamanho do placeholder.
4. O mesmo padrão existe na busca da aba Logs (`ui/LogsUi.kt` ~linha 138, `height(48.dp)`): corrigir junto.

**Critérios de aceite:** capturas antes/depois da aba Estações com texto digitado, em fonte normal e grande,
tema claro e escuro; sem regressão no filtro e na lixeira.

**Dependências/relacionados:** itens 56 e 58.

### 73. Mensagens: alternar "minhas / todas" e "agrupadas por emissor / lista normal"
**Status 0.1.15:** implementado em Mensagens → Conversas (botões Minhas/Todas e Agrupadas/Lista, salvos). Terceiros são guardados e mostrados só para leitura (diálogo do par A ↔ B). Aguarda teste com tráfego real.
_Pedido do mantenedor em 10/11/2026 (após a v0.1.14): "Uma sugestão é um botão para alternar entre minhas
mensagens e todas as mensagens. Também outro botão para alternar entre mensagens agrupadas por emissor ou normal."_

**Situação atual (verificado no código em 10/11/2026, v0.1.14):** a aba Mensagens só mostra mensagens de/para o
próprio indicativo (com ou sem SSID), boletins (BLN) e gerais (CQ...). As mensagens entre outras estações chegam
ao app (o filtro padrão `r/4000` do APRS-IS já as entrega e elas aparecem na aba Logs, tipo "Mensagem"), mas são
descartadas antes de ir para a lista: `data/AprsRepository.kt` ~linha 587, `if (!forUs && !general) return null`.

**Comportamento exigido:**
1. **Botão "Minhas / Todas"** no cabeçalho da aba Mensagens. "Minhas" (padrão) mantém o comportamento atual.
   "Todas" inclui também as mensagens trocadas entre outras estações (monitor), recebidas por RF e APRS-IS.
2. Mensagens de terceiros são **só leitura**: não geram ACK, não disparam notificação nem contador de não lidas,
   e não abrem campo de resposta como se fossem conversa própria. Identificar claramente origem → destino.
3. Guardar as mensagens de terceiros mesmo com o modo "Minhas" ativo (para que "Todas" mostre o histórico),
   respeitando a retenção existente e sem duplicar (mesma origem/ID/texto). ACK/REJ de terceiros não viram balão.
4. **Botão "Agrupadas / Lista"**: "Agrupadas" mostra as conversas agrupadas por emissor (ou par origem↔destino,
   no caso de terceiros); "Lista" mostra todas as mensagens em ordem cronológica, uma por linha.
5. As duas escolhas são independentes, ficam salvas entre aberturas do app e mantêm o cabeçalho compacto (#65).
6. Textos nos idiomas do app.

**Critérios de aceite:** com "Minhas", nada muda em relação à v0.1.14; com "Todas", mensagens entre terceiros
recebidas aparecem e conferem com a aba Logs; nenhuma notificação/ACK para terceiros; alternância de agrupamento
funciona nos dois modos; testes de unidade para o filtro (próprias, gerais, boletins, terceiros, ACK/REJ).

**Dependências/relacionados:** itens 54, 56, 64 e 65.

### 74. Configurações: reorganizar por assunto, em abas
**Status 0.1.15:** implementado (tela cheia, 7 abas, Salvar único no rodapé, erro abre a aba do campo). Aguarda teste em aparelho.
_Pedido do mantenedor em 10/11/2026 (após a v0.1.14): "refaça a parte de configuração. Está tudo junto em uma coisa
comprida. Organize por assuntos e use abas."_

**Situação atual (verificado no código em 10/11/2026, v0.1.14):** a tela é um único `AlertDialog`
(`ui/SettingsUi.kt`, `SettingsDialog`, ~linha 154) com uma coluna rolável de ~17 seções em sequência: Perfil,
Estação, APRS-IS, Beacon/SmartBeaconing, Posição, Bateria, Privacidade, Exibição, Notificações/alertas de
favoritos, Retenção/mensagens, API local, Diagnóstico, Dados locais, Backup, Sobre. Um botão "Salvar" no rodapé
valida e grava tudo de uma vez.

**Proposta de abas** (rótulos curtos, `ScrollableTabRow`; cada aba com rolagem própria):
1. **Estação** — perfil de uso (#10); indicativo, SSID, passcode; símbolo; comentário do beacon; frequência de voz.
2. **APRS-IS** — servidor (lista/outro), porta, conectar automaticamente, filtro.
3. **Beacon** — caminho RF; transmitir por RF / APRS-IS; iniciar rastreador ao abrir; modo (smart/periódico/manual),
   intervalo e parâmetros do SmartBeaconing.
4. **Posição** — fonte (GPS/manual) e coordenadas manuais; precisão do GPS e localização pela rede; privacidade
   (ambiguidade, enviar rumo/velocidade, altitude); economia de bateria.
5. **Mapa** — manter tela ligada, idade máxima das estações, tamanho dos ícones.
6. **Mensagens** — notificar mensagens; alertas de favoritos e horário silencioso; tentativas/intervalo de reenvio;
   anunciar versão; retenção (por tipo) e apagar todas as mensagens.
7. **Sistema** — aviso de atualização; API local; diagnóstico (monitor); apagar estações; backup
   exportar/importar (inclui APRSdroid, #70); sobre, versão e enviar log.

**Comportamento exigido:**
1. Nenhuma opção some nem muda de efeito: só muda o lugar. Mesmas chaves salvas; backup/importação seguem iguais.
2. Trocar de aba **não perde** o que foi digitado; o "Salvar" continua único e grava todas as abas.
3. Se a validação falhar (indicativo, servidor, símbolo, frequência, coordenadas, filtro vazio, confirmação de
   retenção), abrir automaticamente a aba do campo com erro e mostrar a mensagem junto ao campo.
4. Primeira configuração (app ainda não configurado): abrir na aba Estação e indicar o que é obrigatório.
5. Preferir tela cheia (ou diálogo em tela cheia) com barra de título, abas e botões Salvar/Cancelar fixos,
   em vez do `AlertDialog` atual, para caber as abas no celular em pé.
6. Lembrar a última aba aberta durante a sessão; textos das abas nos idiomas do app.

**Critérios de aceite:** todas as opções da v0.1.14 encontradas na aba proposta; editar campos em várias abas e
salvar grava tudo; erro de validação leva à aba certa; sem rolagem horizontal da página; testes de unidade
existentes de configurações continuam passando.

**Dependências/relacionados:** itens 10, 38, 58, 70.

### 75. Banner de abertura ao iniciar o app
**Status 0.1.15:** implementado (WebP de 47 KB em `drawable-nodpi`, ~2 s, toque pula, tema inicial e splash do Android 12+ no mesmo azul, sem nova biblioteca). Aguarda teste de abertura a frio em aparelho.
_Pedido do mantenedor em 10/11/2026: "Backlog: Adicione este banner de entrada para mostrar assim que o app é aberto,
por alguns segundos". Arte substituída no mesmo dia pela versão correta ("Backlog: o banner correto é este")._

**Imagem enviada:** [`docs/inspiracao/item75-banner-abertura.png`](docs/inspiracao/item75-banner-abertura.png)
(1199 × 1312 px, **quase quadrada**, ~1,8 MB): fundo azul-escuro, torre com ondas laranja, satélite, "APRS",
"Automatic Position Reporting System", globo com marcadores/carro/rádio e "www.PT2VHF.com". Substitui a primeira arte
vertical enviada (que dizia "Packet").

**O que fazer:**
1. Ao abrir o app (partida a frio), mostrar o banner em tela cheia por **alguns segundos** (sugestão: ~2 s) e então
   seguir para o mapa.
2. Toque no banner pula a espera. Não mostrar de novo ao girar a tela, ao voltar do segundo plano nem ao reabrir pela
   notificação/widget com o app já em memória.
3. Não atrasar o que já roda na abertura: conexão APRS-IS, rastreador e pedidos de permissão continuam em paralelo;
   o banner só cobre a interface.
4. **Como fica em tela cheia:** como a arte é quase quadrada, ela não cobre a tela do celular em pé. Mostrar a imagem
   inteira, centralizada e sem distorcer (`ContentScale.Fit`, largura total em pé / altura total deitado), sobre um
   fundo da **mesma cor azul-escura das bordas da arte** (≈ `#011236`; medido nos cantos: #001032 em cima, #00123E
   embaixo), de preferência um degradê vertical entre esses dois tons para a emenda não aparecer. Nada de cortar
   (`Crop`) a arte, que perderia o texto ou o site. Respeitar barras do sistema (fundo também atrás delas).

**Notas técnicas:**
- No Android 12+ (API 31) o sistema sempre mostra a splash nativa (ícone sobre cor de fundo) e ela **não aceita
  imagem em tela cheia**. Caminho provável: `androidx.core:core-splashscreen` com fundo na mesma cor azul do banner e
  `installSplashScreen()` saindo logo (fundo da splash = mesmo azul, ver passo 4), seguido de uma tela Compose própria com o banner (estado em `App.kt` controlado
  por `rememberSaveable`/ViewModel para não repetir na recriação). minSdk 26: a biblioteca cobre 26–30 igual.
- Otimizar a imagem antes de embutir (WebP em `res/drawable-nodpi`, alvo < 300 KB) para não pesar o APK.
- Não confundir com o #38 (remoção do aviso "Downloads no site oficial" ao abrir): o banner é só visual, sem botão.

**Critérios de aceite:** banner aparece uma vez por abertura a frio, some sozinho após o tempo definido ou ao toque;
não reaparece em rotação/retorno; sem tela branca/preta entre a splash do sistema e o banner; abertura do mapa não
fica mais lenta que o tempo do banner; aumento do APK pequeno.

**Dependências/relacionados:** itens 38 e 74.

### 76. Traduções: textos fixos no código que aparecem sempre em português
**Status 0.1.15:** implementado (`InternalText` com os valores internos, `UiTextKeys` puro e teste `Features0115Test` cobrindo todos). Aguarda conferência nos 5 idiomas.
_Pedido do mantenedor em 10/11/2026, após a verificação das traduções ("backlog")._

**Situação atual (verificado no código em 10/11/2026, v0.1.14):** os 5 idiomas (en padrão, pt, es, fr, it) têm as
mesmas 213 strings em `values*/strings.xml`, sem chaves faltando, e `locales_config.xml` está configurado. Restam
textos fixos no código que não passam pelos recursos:

1. `ui/MessagesUi.kt` (~linha 450): erro "Sem resposta do serviço de envio. Confira a aba Logs antes de tentar
   novamente." aparece sempre em português.
2. `service/Connections.kt` (~linha 96): "Erro na escrita APRS-IS: <exceção>" não é tratado por
   `UiText.connectionError` e aparece sempre em português.
3. `ui/Screens.kt` (~linhas 797–803): o exemplo de data "AAAA-MM-DD" nos filtros de estações é fixo em português
   (em inglês "YYYY-MM-DD"; cada idioma com o seu).

**Fragilidade a corrigir junto:** tipos de estação, status de mensagens e erros de conexão são guardados como frases
em português (`data/Models.kt`, `service/Connections.kt`, `service/UsbTnc.kt`, `data/MessageLogic.kt`) e só são
traduzidos na exibição por comparação de texto em `UiText.kt` (`role`, `messageStatus`, `connectionError`,
`serverLabel`). Se alguém mudar uma dessas frases no código, a tradução quebra sem aviso (cai no `else -> raw`).
Preferir códigos estáveis (enum/constante) mapeados para `R.string`, mantendo compatibilidade com os valores já
gravados no banco/preferências; no mínimo, constantes compartilhadas e um teste de unidade que garanta que todo
valor interno tem tradução.

**Critérios de aceite:** com o app em en/es/fr/it, os três textos acima aparecem no idioma escolhido; nenhuma
frase interna em português chega à tela; mensagens e estações gravadas por versões anteriores continuam exibidas
corretamente; testes de unidade passam.

**Dependências/relacionados:** regra T66 (textos novos nos 5 idiomas).

### 77. Mensagens: remover acentos antes de enviar (configurável, ligado por padrão)
_Pedido do mantenedor em 11/11/2026 ("Backlog")._

**Problema:** mensagens com acentos (ç, ã, é etc.) chegam com caracteres estranhos em clientes/rádios do outro lado
que não decodificam UTF-8 (muitos rádios e clientes antigos só exibem ASCII).

**Pedido:** nova opção nas Configurações (aba de Mensagens, #74) para tirar todos os acentos do texto **antes de
enviar**, convertendo para o equivalente sem acento (ex.: "ação" → "acao", "Ç" → "C", "º"/"ª" → "o"/"a").
**Padrão: ligada.** Desligada, o texto vai como hoje.

**Observações técnicas:**
- Aplicar no ponto único de montagem do pacote (`core/Aprs.kt`: `messageInfo`, que corta em 67 caracteres, e
  `splitMessage`, que divide texto longo), para valer em mensagens diretas, respostas, boletins, check-in de rede
  (`ui/CommunityUi.kt`) e partes de mensagens longas. Remover acento **antes** de contar os 67 caracteres.
- Caminho provável: `java.text.Normalizer` (NFD) removendo marcas combinantes, mais um mapa para casos que a
  normalização não cobre (ex.: "ß", "æ", "ø", aspas tipográficas, "–"); o que sobrar fora de ASCII imprimível vira
  equivalente ou "?".
- O balão no histórico pode mostrar o texto como foi realmente enviado (sem acento), para não enganar o usuário.
- Só afeta o envio; a exibição de mensagens recebidas (`core/AprsText.kt`, #27) continua igual.
- Textos novos nos 5 idiomas (regra T66) e incluir a chave no backup/restauração das configurações (#50).

**Critérios de aceite:** com a opção ligada (padrão em instalação nova e em atualização), "Olá, tudo bem? Câmbio!"
sai no ar/APRS-IS como "Ola, tudo bem? Cambio!"; com a opção desligada, sai com acentos; limite de 67 caracteres e
ACK/IDs continuam corretos; teste de unidade cobrindo a conversão.

**Dependências/relacionados:** itens 27 (exibição de texto), 50 (backup), 74 (abas das Configurações).

### 78. Rádios compatíveis: incluir o ICOM ID-5200 na lista de rádios que funcionam
_Pedido do mantenedor em 11/11/2026 ("Backlog")._

**Pedido:** acrescentar o **ICOM ID-5200** à lista de rádios homologados/que funcionam com o app.

**Onde a lista aparece hoje** (Mobilinkd, BTECH UV-Pro, Radtel RT-950 Pro e similares):
- `README.md`, seção de recursos: "TNC KISS por Bluetooth (Mobilinkd, BTECH UV-Pro, Radtel RT-950 Pro e similares)".
- `app/src/main/java/com/pt2vhf/aprs/service/Connections.kt`, comentário da classe `BluetoothTnc` (só documentação
  interna, não aparece para o usuário).
- Site oficial `alexpmr/PT2VHF-Site`, página `/android` (requisitos/recursos): conferir se repete a lista e
  atualizar junto, conforme a regra de versão.
- Não há hoje lista de rádios dentro do app (a dica de USB, `usb_hint` nos 5 idiomas, cita só tipos de chip).
  Se for criada uma lista visível no app, traduzir nos 5 idiomas (regra T66).

**Observações:** registrar no texto o tipo de conexão usado com o ID-5200 (Bluetooth ou cabo USB, e modo/menu do
rádio usado), para orientar outros usuários; anotar em `HOMOLOGACAO.md` quem testou e quando.

**Critérios de aceite:** ICOM ID-5200 citado no README, no site `/android` e em qualquer lista de rádios do app,
com a forma de conexão correta; sem mudança de código de comunicação.

### 79. Guia "Sobre" com versão, site e radioamadores colaboradores
_Pedido do mantenedor em 11/11/2026 ("Backlog")._

**Pedido:** ter uma guia **Sobre** que mostre a versão do app, o site oficial e a lista dos radioamadores que
contribuíram para o melhoramento do app: **PU2PYC, PP5AU, PU2MUS**.

**O que já existe:** não há guia própria. Há só uma **seção "Sobre"** no fim da aba **Sistema** das Configurações
(`ui/SettingsUi.kt` ~linha 604, criada no #38 e movida no #74), com a versão instalada e a verificação de atualização
(`UpdateStatusSection()` em `ui/Features019.kt`), o botão "Enviar log" e o link "www.PT2VHF.com"
(`UpdateCheck.SITE` = https://www.pt2vhf.com/aprs-android). Não existe lista de colaboradores.

**Proposta:**
1. Criar a 8ª aba **Sobre** nas Configurações (ao lado das 7 do #74) e mover para ela a seção "Sobre" atual:
   nome do app, versão (`BuildConfig.VERSION_NAME`), verificação de atualização e "Enviar log".
2. Link clicável para o site **pt2vhf.com/aprs-android** (usar `UpdateCheck.SITE`, sem duplicar a URL).
3. Seção **Colaboradores** com os indicativos PU2PYC, PP5AU e PU2MUS (lista fácil de ampliar, num único lugar
   no código). Os indicativos não são traduzidos; o título e a frase de agradecimento, sim.
4. A aba Sobre não tem campos: o "Salvar" do rodapé não precisa mudar de comportamento ao abri-la.
5. Textos novos nos 5 idiomas (regra T66).

**Critérios de aceite:** a aba Sobre aparece nas Configurações e mostra a versão instalada correta, o link abre
o site oficial, os três indicativos aparecem; a aba Sistema deixa de ter a seção Sobre duplicada; nada muda nas
configurações salvas nem no backup (#50).

**Dependências/relacionados:** itens 38 (Sobre/atualização), 39 (verificação), 74 (abas das Configurações).

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

### 80. SSID alterado "não salva" e volta para -9/-7 (perfis)
_Relato de usuário (PY2ARS, Mairiporã-SP) repassado pelo mantenedor em 11/11/2026. Bug de usabilidade, P1._

**Relato:** "ele usa -9 ou -7, mas se eu precisar mudar consigo, porém não consigo salvar minha opção. Só fica 7 ou 9".

**Análise do código (0.1.15):** a gravação em si funciona: `Settings.save()` grava a chave `ssid` e também o
retrato do perfil ativo (`profile_snapshot_<perfil>`, #10). O SSID volta para -9/-7 por três caminhos:
1. **Cada perfil tem o seu SSID.** Tocar em um perfil (Configurações → Estação, ou a troca rápida no Rastreador)
   chama `Settings.forProfile()`, que carrega o retrato salvo daquele perfil ou, na primeira vez, o padrão de
   `presetFor()`: Móvel = -9, Portátil = -7, Base = sem SSID. Quem digita o SSID e depois toca no perfil
   (o seletor fica acima do campo) perde o que digitou; quem troca de perfil no Rastreador recebe o SSID do outro.
2. **Tocar no perfil que já está marcado** também recarrega o retrato e apaga o que foi digitado
   (`loadProfile()` em `ui/SettingsUi.kt` não ignora o perfil atual).
3. **Voltar (botão/gesto do Android) ou "Cancelar" descarta tudo sem aviso** (`onDismissRequest = onDismiss`
   no `Dialog` de Configurações). Só o botão **Salvar** no rodapé grava.

**Como contornar hoje (orientação ao usuário):** Configurações → aba Estação → escolher **primeiro** o perfil,
**depois** digitar o SSID e tocar em **Salvar** no rodapé (não usar Voltar). O SSID fica gravado naquele perfil.
Não trocar de perfil pelo Rastreador; se trocar, repetir o ajuste uma vez no novo perfil, que passa a lembrar.

**Proposta de correção:**
1. Tocar no perfil já selecionado não faz nada.
2. Ao trocar de perfil nas Configurações, preservar o SSID que o usuário digitou nesta edição (só aplicar o
   SSID do perfil se o campo não foi mexido).
3. **Decisão do mantenedor (11/11/2026):** "Acho que não precisaria pedir para salvar. Todas as alterações começariam
   a valer ao mudar algum parâmetro, ou ao mudar de aba, perguntar se quer salvar as alterações ou não, algo assim".
   Ou seja, acabar com a dependência do botão Salvar: cada alteração válida passa a valer ao ser feita (salvamento
   automático do campo/opção, com validação na hora); para o que exigir confirmação (ex.: retenção que apaga
   mensagens, filtro vazio, mudança que afeta TX), perguntar "Salvar / Descartar" ao trocar de aba ou sair.
   Voltar/gesto nunca descarta alteração em silêncio. Definir na implementação qual das duas formas usar
   (salvar na hora ou perguntar ao mudar de aba/sair), sem transmitir nada por causa do salvamento.
4. Abaixo do campo SSID, dica curta: "Salvo no perfil <nome>". Na confirmação de troca rápida do Rastreador,
   mostrar o SSID que vai entrar (ex.: "PY2ARS-9 → PY2ARS-7").
5. Avaliar com o mantenedor: SSID comum a todos os perfis (como o indicativo) em vez de um por perfil.
6. Textos novos nos 5 idiomas (T66) e teste de unidade de `forProfile()`/retrato preservando SSID personalizado.

**Critérios de aceite:** escolher perfil Móvel, mudar o SSID para -5, salvar, fechar e reabrir o app: continua -5;
trocar para Portátil e voltar para Móvel: volta -5; tocar de novo em Móvel não altera o campo; alterar o SSID e
sair por Voltar ou trocar de aba não perde a alteração (fica salva ou o app pergunta antes).
