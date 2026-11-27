# Requisitos — iGate e digipeater inteligente

> Gerado pelo Redator (Reversa) em 2026-11-27. Módulo `gateway-igate-digi` · `core/Igate.kt, core/SmartDigipeater.kt, service/AprsService.kt`

## Objetivo
Permitir que o celular funcione como iGate RF↔APRS-IS e como digipeater que respeita o canal.

## Requisitos funcionais

| ID | Requisito | Conf. |
|----|-----------|-------|
| RF-GD-01 | Enquanto o iGate estiver ligado, com indicativo válido, fora do modo RX e APRS-IS verificado, o sistema deve subir pacotes de RF com `qAR,<MEUCALL>`. | 🟢 |
| RF-GD-02 | O sistema não deve subir pacotes com `TCPIP`, `TCPXX`, `NOGATE`, `RFONLY`, q-construct, consulta `?` ou informação vazia. | 🟢 |
| RF-GD-03 | Quando IS→RF estiver ligado, o sistema deve descer só mensagens (e ack/rej) para estação ouvida em RF nos últimos N minutos (padrão 30), encapsuladas como terceiros. | 🟢 |
| RF-GD-04 | O sistema deve limitar IS→RF a N por minuto (padrão 6) e deduplicar por 30 s. | 🟢 |
| RF-GD-05 | Enquanto o digi estiver ligado, o sistema deve repetir o primeiro salto não usado que for o próprio indicativo, alias, `WIDE1-1` (fill-in) ou `WIDEn-N` até N máximo. | 🟢 |
| RF-GD-06 | O sistema deve reduzir ou descartar `WIDEn-N` acima do máximo (trap) conforme configuração. | 🟢 |
| RF-GD-07 | Para aliases genéricos, o sistema deve respeitar raio, carga do canal, 1 repetição por origem a cada 60 s, limite global por minuto e atraso viscoso. | 🟢 |
| RF-GD-08 | Pacotes de prioridade (mensagem, ack/rej, emergência) devem furar os limites locais, contando no global. | 🟢 |

## Requisitos não funcionais

- Desligado por padrão (ADR-011).
- Caminho final ≤ 8 saltos.

## Critérios de aceite

- **RF-GD-01**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-GD-02**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-GD-03**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
