# ADR-006: Site oficial sempre no comentário do beacon

> Reconstruído pelo Detetive (Reversa) em 2026-11-27 a partir do código e do histórico. 🟢

**Situação:** aceita (em vigor na v1.4)

## Contexto
Divulgação do app na rede APRS.

## Decisão
`composeBeaconComment` remove URL do site digitada e acrescenta `www.PT2VHF.com` ao fim, dentro de 43 caracteres.

## Consequências
Divulgação garantida; reduz o espaço livre do comentário e o usuário não pode tirar.

## Evidência
`core/AprsText.kt:325`, `core/Aprs.kt:460`
