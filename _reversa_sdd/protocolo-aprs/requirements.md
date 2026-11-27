# Requisitos — Protocolo APRS

> Gerado pelo Redator (Reversa) em 2026-11-27. Módulo `protocolo-aprs` · `core/`

## Objetivo
Decodificar e codificar pacotes APRS e AX.25/KISS sem depender do Android.

## Requisitos funcionais

| ID | Requisito | Conf. |
|----|-----------|-------|
| RF-PA-01 | Quando receber uma linha TNC2 `ORIG>DEST,PATH:INFO`, o sistema deve separar origem, destino, caminho e informação; linhas iniciadas por `#` devem ser ignoradas. | 🟢 |
| RF-PA-02 | Quando a informação começar com `}`, o sistema deve desencapsular o pacote de terceiros uma única vez e guardar o gateway. | 🟢 |
| RF-PA-03 | O sistema deve decodificar posição não comprimida (`! = / @`), com ambiguidade (centro da caixa) e extensão curso/velocidade; minutos ≥ 60 devem ser rejeitados. | 🟢 |
| RF-PA-04 | O sistema deve decodificar Mic-E completo: latitude no destino, longitude, velocidade, curso, status, altitude e aparelho. | 🟢 |
| RF-PA-05 | O sistema deve decodificar mensagens `:DEST9____:texto{ID`, ACK/REJ tolerantes a espaços, reply-ack e boletins `BLN*`. | 🟢 |
| RF-PA-06 | O sistema deve decodificar clima, telemetria (com PARM/UNIT/EQNS/BITS), objetos e itens. | 🟢 |
| RF-PA-07 | O sistema deve codificar posição (`=` com mensagens), mensagem com destinatário de 9 colunas e texto ≤ 67, dividindo textos longos. | 🟢 |
| RF-PA-08 | O sistema deve calcular o passcode do APRS-IS a partir do indicativo sem SSID. | 🟢 |
| RF-PA-09 | O sistema deve validar indicativo pelo formato e pelos blocos de prefixo da ITU, com SSID 0–15. | 🟢 |
| RF-PA-10 | O sistema deve identificar o aparelho de origem pelo tocall ou pelo Mic-E usando a tabela aprs-deviceid. | 🟢 |
| RF-PA-11 | O sistema deve calcular o intervalo de beacon por SmartBeaconing (velocidade, curva, tempo mínimo). | 🟢 |
| RF-PA-12 | O sistema deve decodificar posição comprimida (base-91). | 🔴 não implementado na `main` (#109, PR #33) |

## Requisitos não funcionais

- Sem imports Android (testável na JVM).
- Texto exibido decodificado como ASCII, UTF-8 ou CP1252, sem caracteres de controle.

## Critérios de aceite

- **RF-PA-01**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-PA-02**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-PA-03**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- Itens marcados como lacuna ainda não têm comportamento a verificar no código atual.
