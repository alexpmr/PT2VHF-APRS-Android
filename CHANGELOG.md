# Changelog

## Em desenvolvimento (10/11/2026; sem versão, não publicada)

- **#60 P0:** mensagem que estava "Enviando"/"Aguardando ACK" quando o serviço parou ou o app foi encerrado passa a
  "Interrompida" (✗, com Reenviar), em vez de ficar tentando para sempre; antes, nenhuma retentativa sobrevivia ao
  reinício e o balão permanecia pendente (e imune à retenção). ACK que chegar depois ainda confirma a mensagem.

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
