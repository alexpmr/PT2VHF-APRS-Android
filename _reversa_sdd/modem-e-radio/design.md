# Design — Modem e controle do rádio

> Gerado pelo Redator (Reversa) em 2026-11-27.

## Arquivos

- `core/Afsk.kt`
- `core/RadioControl.kt`
- `service/AudioTnc.kt`
- `service/UsbTnc.kt`
- `service/BleTnc.kt`

## Interfaces principais
- `Hdlc.crc16`, `withFcs`, `fcsOk`, `encodeBits`, `nrzi`
- `AfskModulator.modulate(frame, txDelayMs, voxLeadMs, tailMs, …)`, `testTone(3.0)`
- `AfskDemodulator(sampleRate, highQuality).feed(samples): List<ByteArray>`
- `BleKiss.pick`, `chunks(data, mtu)`; `CatPtt.command(protocol, transmit, civAddress)`
- `usbControlLines(chip, dtr, rts, …)`, `pttLines(mode, transmit)`

## Fluxo
Ver `flowcharts/modem-e-radio.md`.

## Testes existentes
`AfskTest`, `RadioControlTest` (`core/`).

## Decisões relacionadas
Ver `adrs/` e `architecture.md`.
