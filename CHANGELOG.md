# Changelog

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
