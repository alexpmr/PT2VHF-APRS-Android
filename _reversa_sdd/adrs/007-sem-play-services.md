# ADR-007: Sem Google Play Services, Firebase ou analytics

> Reconstruído pelo Detetive (Reversa) em 2026-11-27 a partir do código e do histórico. 🟢

**Situação:** aceita (em vigor na v1.4)

## Contexto
Distribuição por APK do site e privacidade.

## Decisão
Usar `LocationManager`, `HttpURLConnection` e APIs da plataforma.

## Consequências
App funciona em aparelhos sem Google; sem telemetria. Localização por rede depende do aparelho.

## Evidência
`service/AprsService.kt`, `dependencies.md`
