# Requisitos — Modem e controle do rádio

> Gerado pelo Redator (Reversa) em 2026-11-27. Módulo `modem-e-radio` · `core/Afsk.kt, core/RadioControl.kt`

## Objetivo
Transmitir e receber pacotes pelo áudio do celular e acionar o PTT do rádio.

## Requisitos funcionais

| ID | Requisito | Conf. |
|----|-----------|-------|
| RF-MR-01 | O sistema deve modular AFSK Bell 202 (1200/2200 Hz, 1200 bd) a 48 kHz em fase contínua, com TXDELAY em flags e cauda. | 🟢 |
| RF-MR-02 | O sistema deve demodular AFSK com PLL digital e, no modo alta qualidade, com três canais de ênfase e dedupe de 1 s. | 🟢 |
| RF-MR-03 | O sistema deve montar e conferir quadros HDLC com FCS CRC-16/X.25, bit-stuffing e NRZI, limitando 400 bytes. | 🟢 |
| RF-MR-04 | Quando o canal estiver ocupado, o sistema deve esperar até 3 s antes de transmitir. | 🟢 |
| RF-MR-05 | O sistema deve acionar o PTT por VOX (tom opcional), RTS, DTR ou CAT (Kenwood, Yaesu, Yaesu antigo, Icom CI-V). | 🟢 |
| RF-MR-06 | O sistema deve falar KISS por BLE (perfil KISS e Nordic UART), fragmentando pela MTU. | 🟢 |
| RF-MR-07 | O sistema deve oferecer um tom de teste de 3 s para ajuste de nível. | 🟢 |

## Requisitos não funcionais

- Código próprio, sem GPL do APRSdroid (ADR-004).
- Nível de entrada medido a cada 100 ms para a UI.

## Critérios de aceite

- **RF-MR-01**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-MR-02**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-MR-03**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
