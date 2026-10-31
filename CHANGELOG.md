# Changelog

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
