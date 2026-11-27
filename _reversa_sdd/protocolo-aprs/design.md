# Design — Protocolo APRS

> Gerado pelo Redator (Reversa) em 2026-11-27.

## Arquivos

- `core/Aprs.kt`
- `core/Kiss.kt`
- `core/AprsDevice.kt`
- `core/AprsExtras.kt`
- `core/AprsPath.kt`
- `core/AprsText.kt`
- `core/AprsTelemetry.kt`
- `core/AprsWeather.kt`
- `core/PacketDecoder.kt`
- `core/CallsignPrefixes.kt`
- `core/DeviceIdTable.kt`
- `core/AsciiFold.kt`

## Interfaces principais

- `AprsPacket.parse(line): AprsPacket?`
- `parsePosition(info): Position?`
- `parseMicE(dest, info)`
- `parseMessage(info): AprsMessage?`
- `positionInfo(...)`, `messageInfo(to, text, id?)`, `splitMessage(text, 67)`
- `aprsPasscode(call): Int`, `fullCallsign(call, ssid)`
- `checkAmateurCallsign(call)`
- `SmartBeaconing`
- `Kiss.encode`, `Kiss.Decoder`, `Ax25.fromTnc2/toTnc2`

## Fluxo
Ver `flowcharts/protocolo-aprs.md`.

## Testes existentes
`AprsTest`, `AprsDeviceTest`, `AprsExtrasTest`, `AprsPathTest`, `AprsTelemetryTest`, `AprsTextTest`, `AprsWeatherTest`, `AsciiFoldTest`, `CallsignPrefixesTest`, `PacketDecoderTest` (`app/src/test/.../core/`).

## Decisões relacionadas
Ver `adrs/` e `architecture.md`.
