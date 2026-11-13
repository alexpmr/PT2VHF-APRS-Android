# Changelog

## 1.0 — Publicada (13/11/2026; testes item a item pendentes)

Primeira versão 1.0 (versionCode 16), autorizada pelo mantenedor ("Implemente todos e suba no site"). Instala por
cima da 0.1.15 sem perder dados e é oferecida pelo aviso de atualização (OTA): o app já compara 1.0 > 0.1.15.

- **Botão Salvar sumido no Android 16 (#85):** a tela de Configurações agora respeita as barras do sistema e o
  teclado (modo borda a borda obrigatório no Android 15/16), então "Cancelar" e "Salvar" ficam acima da barra de
  navegação/tarefas em celular e tablet. O **✓ Salvar** também aparece no cabeçalho. A ficha da estação recebeu a
  mesma correção.
- **SSID que "voltava" para -9/-7 (#80):** tocar no perfil já marcado não recarrega mais nada; trocar de perfil nas
  Configurações mantém o SSID que você digitou; abaixo do campo aparece "Salvo no perfil …". **Voltar/Cancelar não
  descarta mais em silêncio:** com alterações pendentes o app pergunta "Salvar / Descartar". A troca rápida de
  perfil no Rastreador mostra o indicativo que vai entrar (ex.: PY2ARS-9 → PY2ARS-7).
- **"Qual SSID usar?" (#82):** janela com as 16 recomendações de SSID de WB4APR, destacando a sugestão do perfil e
  mostrando o indicativo completo. Abre sozinha na primeira edição do SSID (até marcar "Não abrir sozinha de novo")
  e sempre pelo ⓘ do campo; tocar numa linha preenche o SSID. É só orientação, nada é transmitido.
- **Aba Sobre (#79):** 8ª aba das Configurações com nome e versão do app, verificação de atualização, Enviar log,
  link para pt2vhf.com/aprs-android e os colaboradores **PU2PYC, PP5AU e PU2MUS**. A seção Sobre saiu do fim de Sistema.
- **Mensagens sem acentos (#77):** nova opção em Configurações → Mensagens, **ligada por padrão**: "Olá, tudo bem?
  Câmbio!" sai como "Ola, tudo bem? Cambio!", antes de contar os 67 caracteres; símbolos como º/ª, aspas e travessão
  viram equivalentes ASCII e emojis viram "?". Vale para conversas, respostas, boletins, check-ins e o boletim de
  versão; o balão mostra o texto como saiu. Mensagens recebidas não mudam.
- **Registro do indicativo na consulta de versão (#84):** a consulta de versão nova leva só o indicativo (sem SSID,
  posição ou locator) para a lista de estações do site; caixa em Configurações → Sistema para desligar.
- **ICOM ID-5200 (#78)** na lista de rádios que funcionam (TNC KISS por Bluetooth).
- **Android Auto (#68, primeira versão):** no painel do carro, lista das estações mais próximas com distância,
  estado APRS-IS/TNC/GPS, **Beacon agora** e **Última mensagem**. Usa só modelos da biblioteca Car App. Fora da Play
  Store o Android Auto exige o modo desenvolvedor com "Fontes desconhecidas"; **ainda não testado em carro/DHU**.
- Continuam dependendo de teste em aparelho, servidor e rádio reais (sem mudança de código nesta versão): P0 #60
  (envio que fica tentando) e #62 (fechamento ao apagar), #25, #37 e #54.
- Testes de unidade novos: remoção de acentos, tabela de SSID, comparação 1.0 > 0.1.15 e montagem da URL do #84.

## 0.1.15 — Publicada (10/11/2026, beta; testes item a item pendentes)

- **Banner de abertura (#75):** a arte "APRS · www.PT2VHF.com" aparece em tela cheia por cerca de 2 s ao abrir o app
  (só na abertura a frio; girar a tela, voltar do segundo plano ou abrir pela notificação não repete). Tocar pula.
  A imagem aparece inteira sobre o mesmo azul das bordas, inclusive na splash do sistema; conexão, rastreador e
  permissões continuam carregando por trás.
- **Configurações em 7 abas (#74):** Estação, APRS-IS, Beacon, Posição, Mapa, Mensagens e Sistema, em tela cheia com
  Salvar fixo embaixo. Nenhuma opção mudou de efeito, só de lugar; trocar de aba não perde o que foi digitado e um
  erro de validação abre a aba do campo. Na primeira configuração abre em Estação com o aviso do indicativo
  obrigatório; a última aba aberta é lembrada durante a sessão.
- **Mensagens: "Minhas / Todas" e "Agrupadas / Lista" (#73):** em Conversas, "Todas" mostra também as mensagens
  trocadas entre outras estações (só leitura: sem ACK, notificação ou resposta, com origem → destino); "Lista" mostra
  uma mensagem por linha em ordem cronológica. As duas escolhas ficam salvas. As mensagens de terceiros passam a ser
  guardadas (com a mesma retenção) para o histórico aparecer ao trocar para "Todas".
- **Busca sem cortar o texto (#72):** os campos de busca de Estações, Logs e Mensagens mostram o texto inteiro,
  inclusive com fonte grande.
- **Traduções (#76):** o erro "sem resposta do serviço de envio", o erro de escrita no APRS-IS e o formato de data
  dos filtros agora seguem o idioma do app. Tipos de estação, situação das mensagens e erros de conexão vêm de
  constantes únicas, com teste garantindo que todos têm tradução (dados gravados antes continuam reconhecidos).
- Fica para depois: **Android Auto (#68)** (biblioteca Car App, aprovação do Google e teste em carro/DHU). Os P0
  #60/#62 e os itens #25/#37/#54 continuam dependendo de teste em aparelho, servidor e rádio reais.
- Testes de unidade novos: mensagens de terceiros, agrupamento/lista e cobertura das traduções dos valores internos.

## 0.1.14 — Publicada (10/11/2026, beta; testes item a item pendentes)

- **Mensagens mais compactas e legíveis (#56, #65, #28):**
  - Causa do #56 encontrada: a janela do app não repassava a altura do teclado à tela, então o cabeçalho e a barra
    inferior continuavam visíveis e o campo de escrever virava uma faixa fina. Agora, com o teclado aberto, cabeçalho
    e barra inferior somem, o campo tem sempre uma linha inteira e cresce até 5 linhas; o histórico é que encolhe.
  - Cabeçalho em duas linhas baixas: só o **indicativo** (sem "PT2VHF APRS Client for Android"), selo "Somente
    recepção" quando for o caso, Enviar beacon, Configurações e chips APRS-IS/TNC/GPS menores.
  - Conversa: **Voltar em ícone (←)** na mesma linha do contato; **Enviar virou ícone** de avião de papel com área de
    toque de 48 dp e indicador de progresso; a linha "Para:" só aparece nos chats Geral e Boletins.
- **#64 Mensagens atualizam sozinhas:** a lista de conversas volta ao topo quando chega mensagem nova (ou mostra
  "↑ Nova mensagem" se você rolou para baixo); conversa, Geral e Boletins rolam para a nova se você estava no fim,
  senão aparece "↓ Nova mensagem".
- **#59 Só indicativos válidos no mundo:** tabela de blocos de prefixos da ITU embarcada (sem internet). Recusa
  palavras e prefixos inexistentes com o motivo; mostra o país do prefixo enquanto você digita; avisa quando o
  indicativo salvo ou o do backup é inválido (o app não conecta nem transmite). Destinos de mensagem (CQ, BLN,
  objetos, serviços) continuam livres, só com aviso. Prefixo válido não comprova licença.
- **Ficha da estação (#58.4):** novas abas **Trilha** (1 h · 6 h · 1 dia · 3 dias · 7 dias, mapa, pontos e
  distância total) e **Mensagens** (conversa com a estação e campo para responder); bandeira e país pelo prefixo.
- **Filtros das estações (#58.3):** busca por indicativo ou comentário, região (país pelo prefixo), só com posição
  conhecida, SSID 0–15, símbolo, visto por último (1 min a 30 dias) e contador de filtros ativos no botão.
- **Comunidade (#58.5):** nova aba em Mensagens com rodadas de check-in (APRS Thursday já cadastrada e rodadas
  próprias), data da rodada, busca, participantes, países com bandeira e lista por país; check-in pelo app com
  confirmação antes de transmitir. Conta só o que este app ouviu.
- **Mapa:** **agrupar estações próximas** num círculo com a contagem (tocar aproxima; favoritas e seguida ficam
  de fora) e **camada de clima** com temperatura e vento das estações WX (#66, #67), ambas em Camadas.
- **Ferramentas (#69):** botão 🛠 na aba Logs (e "Decodificar" nos detalhes do pacote) com **decodificador de
  pacote** campo a campo e caminho explicado, e **tabela de frequências APRS** por região.
- **Importar do APRSdroid (#70):** Configurações → Backup → Importar aceita o arquivo de preferências (XML) ou o
  perfil (JSON) do APRSdroid; mostra o que foi reconhecido antes de aplicar e nunca importa o passcode.
- Fica para depois: **Android Auto (#68)**, que exige a biblioteca Car App, aprovação de categoria pelo Google e
  teste em carro/DHU.
- Testes de unidade novos: prefixos ITU, acompanhamento de mensagens, APRSdroid, agrupamento, clima, decodificador,
  filtros, trilha e rodadas.

## 0.1.13 — Publicada (10/11/2026, beta; testes item a item pendentes)

Primeira versão pública depois da 0.1.9: inclui tudo das candidatas QA 0.1.10, 0.1.11 e 0.1.12 (abaixo) e as
novidades a seguir.

- **0.1.12 (QA):** expressão regular de ACK/REJ com reply-ack corrigida, evitando que um pacote malformado derrubasse
  a recepção APRS-IS; testes de mensagens, ACK, REJ e reply-ack recebidos.

- **#60 P0:** mensagem que estava "Enviando"/"Aguardando ACK" quando o serviço parou ou o app foi encerrado passa a
  "Interrompida" (✗, com Reenviar), em vez de ficar tentando para sempre; antes, nenhuma retentativa sobrevivia ao
  reinício e o balão permanecia pendente (e imune à retenção). ACK que chegar depois ainda confirma a mensagem.
- **#58.6 Ficha da estação em tela cheia** (substitui o diálogo): cabeçalho com símbolo, indicativo com o SSID
  em destaque, tipo, "ao vivo/inativa" e coordenadas; ações Mensagem, Seguir, Navegar e Favorito; abas
  **Visão geral · Caminho · Telemetria e clima · Bruto**. Tudo calculado a partir dos pacotes guardados na aba Logs
  (sobrevive a reiniciar o app, limitado pela retenção de logs); campo sem dado aparece como "não informado".
  - Visão geral: cartões de posição, grid, distância/rumo a partir de você, velocidade, rumo, altitude (`/A=`), tipo,
    último pacote e total, primeira recepção, transporte, símbolo e PHG; condições WX no topo para estações de clima.
  - **Recepção (24 h):** pacotes, iGates RF que a ouviram (qAR/qAr/qAo/qAO), iGate mais distante e distância,
    último salto (digi ou iGate) e distância, pacotes ouvidos pelo TNC deste aparelho (diretos) separados dos do APRS-IS.
  - **Outros SSIDs** do mesmo operador com "há X"; tocar abre a ficha daquele SSID.
  - **Caminho** do último pacote em linha do tempo: origem, digipeaters que repetiram, alias usados/não usados,
    construção q explicada, iGate e gateway de terceiros, com o número de saltos RF.
  - **Telemetria e clima:** gráficos com mínimo/máximo reais, unidade, horário, lacunas visíveis e valor ao tocar;
    períodos 1 h/6 h/24 h/7 d/30 d; chuva em barras (acumulado, sem interpolar). Telemetria APRS `T#` decodificada com
    PARM./UNIT./EQNS./BITS. (nome, unidade, valor escalonado e bruto, bits com rótulo e última mudança).
  - **Bruto:** pacotes da estação com data, "há X" e meio; tocar copia, botão compartilha todos.
- **#58.6 Ordenação em um toque** na aba Estações: **Tipo · Nome · Distância · Recentes**; tocar de novo no critério
  ativo inverte o sentido, com seta ↑/↓ e nome acessível; escolha salva. Ordem dos tipos: Móvel, Pessoa, Aeronave,
  Embarcação, Estação fixa, Clima, Digipeater, iGate, demais (dentro do grupo, A-Z). Ao ordenar por distância,
  estações sem distância conhecida ficam sempre no fim.
- **#63 Ícones do mapa maiores e nítidos:** símbolos do atlas de alta resolução (células de 128 px) reduzidos com
  filtragem para o tamanho real da tela, com sombra leve; **Configurações → Tela e mapa → Tamanho dos ícones**
  Pequeno (26 dp) / Médio (34 dp, padrão) / Grande (44 dp), aplicado na hora. Rótulo proporcional, área de toque de
  48 dp, estação seguida e favoritas desenhadas por cima. Antes os ícones saíam com 48 px físicos (≈16 dp em telas xxxhdpi).
- Testes de unidade: caminho/q-constructs, telemetria T#/PARM/EQNS/BITS, ficha (recepção, duplicatas, SSIDs), ordenação
  e tamanhos de ícone.

## 0.1.11 — candidata de correção QA (08/11/2026; não publicada)

- **#62 P0:** relato de fechamento do aplicativo após apagar mensagens ou estações na 0.1.10 QA.
- Executar a exclusão no SQLite **antes** de atualizar as listas da interface; em caso de falha, preservar os dados exibidos.
- Apagar mensagens usando um snapshot de IDs, preservando mensagens que cheguem durante a operação; testes de unidade adicionais.
- Registrar erros de exclusão no relatório diagnóstico compartilhável por Sobre → Enviar log.
- **#60 P0:** priorizar APRS-IS verificado para mensagens privadas, com fallback para TNC/RF, evitando TX duplicado por ambos os meios e o bloqueio do envio pela tentativa RF. Diferenciar escrita no transporte de confirmação ACK e registrar tentativas no log de conexão.
- A beta 0.1.10 foi retirada dos downloads oficiais após relato de instabilidade. A 0.1.9 permanece no manifesto como alternativa anterior, mas não é automaticamente instalável sobre 0.1.10.
- **Status:** candidata QA, CI e homologação física de RF/APRS-IS pendentes; não afirmar que as causas raiz foram comprovadas sem stack trace ou teste no dispositivo.


## 0.1.10 — candidata QA (08/11/2026; NÃO publicada)

- **#54/#60:** acrescentado filtro de mensagens dirigidas ao próprio indicativo (`g/BASE/BASE-*`), removendo o atalho regional inválido até haver fix GPS; recepção de mensagens APRS encapsuladas em pacotes de terceiros de iGate e interpretação separada de ID de mensagem/ACK piggyback.
- **Envio e ACK:** ACKs processados na fila de I/O respeitando as autorizações de transmissão; a composição deixa de ficar bloqueada indefinidamente quando não há retorno do serviço. Timeout de interface não garante que o pacote não tenha sido transmitido.
- **#56:** campo de texto com altura mínima e contraste explícito, mesmo com teclado aberto.
- **#57:** um check quando o transporte aceita o pacote; dois checks verdes apenas depois do ACK. Nunca representam leitura.
- **#61:** lixeira na aba Estações, com confirmação de exclusão de todas as estações locais; mensagens, favoritos e configurações preservados.
- **#58:** primeiro parser de relatórios meteorológicos APRS WX convencionais (temperatura, umidade, vento, pressão e chuva), séries temporais da sessão e gráficos na ficha de estação; sem persistência histórica ainda.
- **#59:** regra inicial de sintaxe para indicativo próprio (contra palavras arbitrárias); tabela global oficial de prefixos, exceções nacionais e verificação de atribuição **ainda pendentes**.
- Testes de unidade adicionados para parser WX, filtros, encapsulamento iGate, reply-ack e sintaxe de indicativos.

**PENDÊNCIAS / BLOQUEIOS:** todos os itens dependentes de rádio/TNC e servidor real precisam de homologação; o #52 (digipeater/iGate ativo), o #55 (notificações enriquecidas), a ficha completa #58 (telemetria T#/PARM/UNIT/EQNS/BITS e persistência), e o #59 completo não estão concluídos. Não liberar como estável sem CI verde, APK assinado, provas de TX/ACK e atualização validada do site/OTA.


## 0.1.9 — Publicada (05/11/2026, beta; testes item a item pendentes)

Implementa todos os itens pendentes do backlog. Itens que dependem de hardware (AFSK no ar, adaptadores USB, widget, consumo de bateria) foram testados só em código e precisam de teste físico.

- **#44** Modem AFSK 1200 (Bell 202) pelo áudio do celular, código próprio: recepção com 3 decodificadores em paralelo (alta qualidade) ou 1 (leve), transmissão com TX delay, tom de VOX e volume, tom de teste de 3 s, escolha de entrada/saída (padrão, sem processamento, fone com fio, placa de som USB, Bluetooth), indicador de nível. PTT só por VOX ou interface externa.
- **USB-OTG** TNC KISS por cabo USB: CDC-ACM (Mobilinkd, NinoTNC, rádios com USB nativo), FTDI e CP210x, com permissão, conexão automática ao plugar e velocidade configurável. CH340/PL2303 ainda não.
- **#51** Nova aba **Logs** (depois de Tracker) com todos os pacotes RX/TX de RF e APRS-IS em SQLite: filtros por direção, meio, tipo e texto, pausa, detalhes do pacote, copiar/compartilhar, exportar TXT/CSV, retenção (24 h a 30 dias) e limite de quantidade.
- **#39** Aviso de nova versão: verificação ao abrir/voltar (1 h) e pelo serviço a cada 12 h, notificação Android uma vez por versão, botão ⬆ no topo, "Ignorar esta versão" e estado da verificação em Sobre.
- **#40** Mensagens com data e hora, e tempo decorrido nas de hoje.
- **#41** Apagar mensagens: todas, só boletins/gerais ou só privadas, com contagem e confirmação.
- **#42** Retenção de mensagens com opção "24 h".
- **#43** Boletim automático anunciando a versão uma vez, só pelo APRS-IS verificado (nunca RF); pode ser desligado.
- **#45** Enviar log (diagnóstico anonimizado + último travamento) em Sobre e oferta automática após fechamento inesperado.
- **#46** Widget da tela inicial: APRS-IS/TNC/GPS, rastreador, último beacon, Beacon agora e Rastreador.
- **#47** Balão mostra tentativa N/M e contagem para o próximo reenvio; Reenviar e Cancelar retentativas; tentativas e intervalo configuráveis.
- **#48** Objetos e itens APRS: criar com toque longo no mapa, editar, pausar, repetir (10-60 min), encerrar (killed); enviados pelos meios autorizados.
- **#49** Economia de bateria (desligada, sempre, automática abaixo de X%): GPS menos preciso e intervalos maiores com a tela apagada; cartão Bateria com nível e consumo estimado.
- **#50** Backup e restauração das configurações (JSON), opcionalmente com mensagens; passcode nunca exportado.
- **#6 (restante)** Posição fixa digitada ou por toque longo no mapa, precisão do GPS (alta/equilibrada/baixo consumo) e localização pela rede.
- **Modo carro:** tela simplificada com velocidade grande, conexões, próximo beacon, última mensagem, botões grandes e leitura em voz alta opcional.
- **Rotas:** importar GPX, mostrar no mapa, distância restante e linha direta até a estação seguida.
- Textos novos em português, inglês, espanhol, francês e italiano.
- **Fora desta versão:** download em massa de mapas offline (a política de tiles do OpenStreetMap proíbe).


## 0.1.8 — Publicada (04/11/2026, beta; testes item a item pendentes)

Inclui a 0.1.7 QA (correção do P0 #37). Itens prontos para teste físico; nenhuma baixa sem o OK do mantenedor.

- **#10** Perfis Base/Móvel/Portátil/Somente recepção, cada um com suas escolhas de TX; troca rápida no Rastreador sem iniciar transmissão.
- **#20** Pressão longa no balão: responder com contexto, responder em público (CQ) ou em privado, mensagem direta e copiar.
- **#21** Nova mensagem com busca de indicativo parcial/SSID nas estações (inclusive cache) e no histórico.
- **#2** Filtros combináveis das conversas: direção, meio, situação e período, com a ordem.
- **#26** Histórico de mensagens em SQLite, migrado do `messages.json` (backup mantido), com retenção por prazo e quantidade por categoria; pendentes de ACK nunca são apagadas.
- **#27** Exibição de texto APRS reconhece UTF-8 válido, senão CP1252/Latin-1; controles marcados com "�".
- **#22** Brilho do mapa 50-150% aplicado só aos tiles.
- **#1** Indicativo ao lado do símbolo no mapa e ícone neutro "?" para símbolo desconhecido.
- **#4 / #14** Meus enlaces RF (somente TNC) em lista e no mapa; alcance e recordes RF direto/via digi; camada de cobertura RF.
- **#11** Trilha da estação seguida, painel com velocidade/rumo/distância e aviso de desatualização.
- **#5** Tracklog com cor, espessura, opacidade, retenção configurável e exportação GPX.
- **#6** Ambiguidade 0-4, rumo/velocidade e altitude opcionais, frequência de voz, corner pegging e intervalo parado, seletor visual de símbolo, tela ligada, idade máxima das estações, notificações e monitor configuráveis.
- **#12** Alertas de favoritas (primeira aparição, retorno, posição, RF direto, meu pacote digipeado) com cooldown e horário silencioso.
- **#13** Diagnóstico com transições de conexão, reconexões, ACK pendentes, filtro do monitor e exportação anonimizada.
- **#15** API opcional para outros apps com permissão própria (ver API.md).
- **#38** Removido o aviso de anúncio na abertura (substitui o #18); versão e site em Configurações → Sobre.
- KISS TCP (herdado da 0.1.0). Novos tipos de estação: pessoa, aeronave, embarcação.
- **Pendentes:** USB-OTG, AFSK, mapa offline em massa, modo carro e rotas. Homologação física de todos os itens.


## 0.1.7 — Candidata de homologação (não publicada)

- **P0 #37:** mensagens, beacons e retransmissões usam executor de I/O, sem escrita TCP na thread principal Android.
- Primeiro GPS fix atualiza o filtro APRS-IS na sessão existente; não reinicia login para atualizar posição.
- Primeiro callback de rede não força reconexão; escritor temporariamente indisponível não derruba socket saudável.
- **#33:** lista Estações em linha única: símbolo APRS, indicativo, último contato, descrição, velocidade km/h e distância fixa à direita.
- Testes de velocidade desconhecida/zero e sessão APRS-IS local após transmissão.
- **Pendente:** CI, homologação física com APRS-IS e TNC, e restante do backlog histórico. Não declarar 100% concluído.
## Em desenvolvimento — correção do envio APRS-IS (sem release pública)

- O indicador de APRS-IS é invalidado imediatamente quando a sessão de socket é reiniciada ou encerrada; falhas de escrita solicitam reconexão.
- A interface de mensagens recebe o resultado do serviço, preserva o rascunho em caso de falha e não confunde solicitação de envio com escrita bem-sucedida.
- Erros distinguem TX desabilitado, sessão não verificada, TNC desconectado e falha de transmissão, com traduções nos idiomas disponíveis.
- Necessário CI aprovado, APK QA assinado e homologação física do tráfego APRS-IS/TNC. A escrita no socket **não** significa ACK remoto.


## 0.1.5 — 2026-10-31

- Botão Enviar mensagem com área própria nos detalhes da estação (Mapa ou Estações).
- Mensagens privadas/Geral em ordem cronológica; lista de conversas mais recentes primeiro por padrão.
- Balões de mensagens responsivos, sem limite fixo de 300 dp, com alinhamento preservado.
- Interface compacta ao abrir o teclado: cabeçalho e navegação inferior recolhidos e filtro ocultado sem perda de pesquisa.
- Quando não houver rota TX APRS-IS/TNC autorizada e conectada, preservar rascunho e informar o motivo antes de tentar enviar.
- Rastreador ativado por padrão após configuração/consentimento informado, com opção nas Configurações e persistência do desligamento manual.
- Contabilizar beacons como enviados somente quando transmitidos a algum meio.
- Os demais itens do backlog continuam abertos; transmissão real e interação física precisam de homologação.

## 0.1.4 — 2026-10-31

- O comentário transmitido nos beacons de posição inclui sempre
  `www.PT2VHF.com`, como identificação do projeto, tanto por APRS-IS
  quanto por RF/TNC. Nenhum beacon extra é enviado por esta alteração.
- O domínio não é truncado ao limitar o comentário APRS a 43 caracteres;
  a descrição personalizada é abreviada somente na transmissão, mantendo
  intacta a preferência salva. Variantes de endereço existentes não são
  repetidas e a grafia final é padronizada.
- Testes unitários para comentários vazios, legados, personalizados,
  URLs já presentes, limite de caracteres e montagem do pacote APRS.
- Esta é uma correção pontual: as demais pendências continuam abertas no
  `BACKLOG.md` e precisam de homologação própria.

## 0.1.3 — 2026-10-31 (candidata, aguardando CI e homologação)

- Exibição da **versão instalada** no popup inicial de anúncio do site oficial
  www.PT2VHF.com, independente do aviso de atualização OTA.
- Preparação de atlas de símbolos APRS reais das tabelas primária e secundária, com overlays
  alfanuméricos (licenças/créditos em `THIRD_PARTY_APRS_SYMBOLS.md`).
- Filtros reais e combináveis de estações, classificação ASC/DESC, intervalos de datas,
  distância, origem e favoritas; migração SQLite não destrutiva para `first_seen`.
- Recuperação da vista anterior do mapa (centro e zoom) e botão de minha localização.
- Confirmação de saída, com distinção entre manter ou encerrar o tracker.
- Atualização de última recepção em estações conhecidas mesmo quando o pacote não tem posição.
- **Não é conclusão de todo o backlog**: continuam pendentes testes físicos,
  perfis de operação, USB-OTG, KISS TCP, recursos avançados de RF, offline,
  integrações e demais itens ainda descritos em `BACKLOG.md`.

## 0.1.2 — 2026-10-31

- Persistência SQLite de estações/enlaces e tracklog local; filtros básicos, opções APRS-IS,
  diagnóstico e modos de beacon, com homologações físicas pendentes.


## 0.1.0 - 2026-10-31

- Primeira versão: APRS-IS (servidor configurável, passcode automático, filtro `r/4000`), mapa com estações
  e enlaces RF, mensagens (conversas, CQ, boletins, ACK e notificações), botão Enviar beacon, modo tracker
  com SmartBeaconing em segundo plano, TNC KISS por Bluetooth com monitor, aviso de nova versão e
  "Downloads no site oficial".
- Pendente para versões seguintes: TNC por USB-OTG e KISS TCP, mapa offline, mapa de calor de cobertura,
  modo carro, rotas e recordes RF.
