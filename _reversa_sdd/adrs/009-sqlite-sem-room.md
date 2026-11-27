# ADR-009: SQLite direto com uma thread de disco

> Reconstruído pelo Detetive (Reversa) em 2026-11-27 a partir do código e do histórico. 🟢

**Situação:** aceita (em vigor na v1.4)

## Contexto
Poucas tabelas e desejo de poucas dependências.

## Decisão
`SQLiteOpenHelper` em WAL, com todas as escritas na thread `aprs-station-db`.

## Consequências
Sem geração de código e com ordem garantida. Migrações escritas à mão.

## Evidência
`data/*Store.kt`, `data/AprsRepository.kt`
