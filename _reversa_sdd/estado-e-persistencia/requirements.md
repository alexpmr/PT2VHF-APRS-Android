# Requisitos — Estado e persistência

> Gerado pelo Redator (Reversa) em 2026-11-27. Módulo `estado-e-persistencia` · `data/`

## Objetivo
Manter o estado do app em memória e em disco, aplicar as regras de mensagens e retenção.

## Requisitos funcionais

| ID | Requisito | Conf. |
|----|-----------|-------|
| RF-EP-01 | O sistema deve expor estações, mensagens, status, monitor e logs como `StateFlow` para a UI. | 🟢 |
| RF-EP-02 | Ao receber um pacote, o sistema deve executar o fluxo `ingest` (contadores, enlaces RF, objetos, clima, eventos RF, posição, mensagem). | 🟢 |
| RF-EP-03 | O sistema não deve criar estação a partir de pacote sem posição. | 🟢 |
| RF-EP-04 | O sistema deve deduplicar mensagens: 1 h por (origem, ID, texto), 10 min para terceiros sem ID, 24 h para boletins. | 🟢 |
| RF-EP-05 | O sistema deve casar ACK por ID sem diferenciar maiúsculas e por indicativo base quando um lado está sem SSID. | 🟢 |
| RF-EP-06 | O sistema deve aplicar retenção por categoria (privada, geral, boletim), sem apagar mensagens pendentes. | 🟢 |
| RF-EP-07 | Na inicialização, o sistema deve ler as mensagens de forma síncrona e marcar envios em curso como `Interrompida`. | 🟢 |
| RF-EP-08 | O sistema deve gravar em SQLite por uma única thread e só remover da tela depois do commit. | 🟢 |
| RF-EP-09 | O sistema deve ler e gravar configurações com padrão e faixa válidos, com perfis e snapshot por perfil. | 🟢 |
| RF-EP-10 | O sistema deve exportar e importar configurações em JSON versionado, sem passcode nem endereços Bluetooth, e importar do APRSdroid. | 🟢 |
| RF-EP-11 | O sistema deve guardar a exceção não tratada e oferecer envio na próxima abertura. | 🟢 |

## Requisitos não funcionais

- Valores internos em português (ADR-001).
- SQLite em WAL sem Room (ADR-009).

## Critérios de aceite

- **RF-EP-01**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-EP-02**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-EP-03**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
