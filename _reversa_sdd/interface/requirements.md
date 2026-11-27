# Requisitos — Interface

> Gerado pelo Redator (Reversa) em 2026-11-27. Módulo `interface` · `ui/`

## Objetivo
Mostrar mapa, mensagens, estações, rádio, tracker e logs, e permitir configurar o app.

## Requisitos funcionais

| ID | Requisito | Conf. |
|----|-----------|-------|
| RF-UI-01 | O sistema deve oferecer seis abas: Mapa, Mensagens, Estações, Rádio, Tracker e Logs. | 🟢 |
| RF-UI-02 | O mapa deve agrupar estações próximas, oferecer tipos Padrão, Escuro, Cinza, Topo e Auto, e mostrar trilhas e rotas. | 🟢 |
| RF-UI-03 | Mensagens devem ter modos Minhas/Todas e Agrupado/Lista, com ações de reenviar, cancelar e desistir e reenviar. | 🟢 |
| RF-UI-04 | A ficha da estação deve mostrar caminho explicado, aparelho, clima, telemetria, PHG e links. | 🟢 |
| RF-UI-05 | Configurações devem abrir em tela cheia com 10 abas e respeitar barras do sistema e teclado. | 🟢 |
| RF-UI-06 | O envio de mensagem deve mostrar o resultado real lido de `sendResults`. | 🟢 |
| RF-UI-07 | O app deve estar em inglês, português, espanhol, francês e italiano. | 🟢 |
| RF-UI-08 | Deve haver modo carro, decodificador de pacotes e tela de comunidade. | 🟢 |

## Requisitos não funcionais

- Compose + Material 3.
- Sem testes de UI (DT3).

## Critérios de aceite

- **RF-UI-01**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-UI-02**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-UI-03**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
