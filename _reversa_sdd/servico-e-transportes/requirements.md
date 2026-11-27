# Requisitos — Serviço e transportes

> Gerado pelo Redator (Reversa) em 2026-11-27. Módulo `servico-e-transportes` · `service/`

## Objetivo
Manter as conexões ativas em segundo plano, transmitir beacon e mensagens e notificar o usuário.

## Requisitos funcionais

| ID | Requisito | Conf. |
|----|-----------|-------|
| RF-ST-01 | O sistema deve rodar como serviço em primeiro plano `START_STICKY` e aceitar ações por Intent. | 🟢 |
| RF-ST-02 | O sistema deve conectar ao APRS-IS com login `user CALL pass N vers … filter …` e reconectar de 5 a 120 s. | 🟢 |
| RF-ST-03 | O sistema deve expandir `r/<km>` com a posição e sempre incluir `g/BASE/BASE-*` no filtro. | 🟢 |
| RF-ST-04 | O sistema deve conectar a TNC por Bluetooth SPP, BLE, KISS TCP, USB-OTG ou áudio. | 🟢 |
| RF-ST-05 | O sistema deve decidir o beacon pelo modo (inteligente, periódico, manual) e nunca enfileirar dois. | 🟢 |
| RF-ST-06 | O sistema deve transmitir beacon por RF e APRS-IS conforme as chaves e o login. | 🟢 |
| RF-ST-07 | O sistema deve enviar mensagem privada por APRS-IS primeiro e RF como alternativa, com retentativas 30 s × n. | 🟢 |
| RF-ST-08 | O sistema deve responder ACK uma vez a cada 20 s por (remetente, ID), pelo meio de chegada. | 🟢 |
| RF-ST-09 | O sistema deve escolher a precisão do GPS (alta, equilibrada, baixa, economia) e declarar fix perdido após 2 min. | 🟢 |
| RF-ST-10 | Com economia ligada (ou automática ≤ 20%) e tela desligada, o sistema deve reduzir GPS e beacon. | 🟢 |
| RF-ST-11 | O sistema deve notificar mensagens, boletins, grupo, ACK, eco de digi e alertas de favoritas em canais próprios. | 🟢 |
| RF-ST-12 | O sistema deve oferecer widget de status e Android Auto com estações próximas, última mensagem e beacon. | 🟢 |
| RF-ST-13 | O transporte Bluetooth SPP deve reconectar sozinho após queda. | 🔴 não implementado (A2) |

## Requisitos não funcionais

- Escritas em rede/TNC numa única thread `aprs-tx`.
- Sem Google Play Services (ADR-007).

## Critérios de aceite

- **RF-ST-01**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-ST-02**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-ST-03**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- Itens marcados como lacuna ainda não têm comportamento a verificar no código atual.
