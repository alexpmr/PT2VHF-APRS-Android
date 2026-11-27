# ADR-010: Atualização só pelo domínio oficial

> Reconstruído pelo Detetive (Reversa) em 2026-11-27 a partir do código e do histórico. 🟢

**Situação:** aceita (em vigor na v1.4)

## Contexto
Evitar que uma resposta adulterada aponte para APK de terceiros.

## Decisão
Aceitar link de APK só com prefixo `https://www.pt2vhf.com/`, sem seguir redirecionamento, e nunca instalar sozinho.

## Consequências
Superfície de ataque menor. Na versão do Google Play o aviso de atualização deve ser desligado (PR #31).

## Evidência
`data/UpdateCheck.kt`, `data/UpdateManager.kt`
