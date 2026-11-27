# Requisitos — API para outros apps

> Gerado pelo Redator (Reversa) em 2026-11-27. Módulo `api-externa` · `service/ApiContract.kt, service/ApiReceiver.kt, API.md`

## Objetivo
Permitir que outros apps recebam posições e status e comandem o tracker e o beacon.

## Requisitos funcionais

| ID | Requisito | Conf. |
|----|-----------|-------|
| RF-AE-01 | O sistema deve proteger a API com a permissão `com.pt2vhf.aprs.permission.API` de nível `dangerous`. | 🟢 |
| RF-AE-02 | Enquanto `api_enabled` estiver ligado, o sistema deve emitir `POSITION` a cada posição nova e `STATUS` quando pedido ou quando mudar. | 🟢 |
| RF-AE-03 | O sistema deve aceitar `TRACKER_START/STOP` só com `api_allow_control` e `SEND_BEACON` só com `api_allow_beacon`. | 🟢 |
| RF-AE-04 | Comandos só devem valer com o serviço rodando. | 🟢 |

## Requisitos não funcionais

- API desligada por padrão.

## Critérios de aceite

- **RF-AE-01**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-AE-02**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-AE-03**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
