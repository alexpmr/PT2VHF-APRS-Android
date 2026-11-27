# ADR-001: Valores internos gravados em português

> Reconstruído pelo Detetive (Reversa) em 2026-11-27 a partir do código e do histórico. 🟢

**Situação:** aceita (em vigor na v1.4)

## Contexto
Situação de mensagem, tipo de estação e erros já estavam gravados em português no SQLite antes da tradução da UI para 5 idiomas.

## Decisão
Manter os valores em português, centralizados em `data/InternalText.kt`, e traduzir só na exibição por `UiTextKeys`.

## Consequências
Dados antigos continuam válidos sem migração. Custo: qualquer mudança de texto interno exige migração; código mistura português e inglês.

## Evidência
`data/InternalText.kt`, `UiTextKeys.kt`
