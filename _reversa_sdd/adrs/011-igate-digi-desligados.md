# ADR-011: iGate e digipeater desligados por padrão e com limites

> Reconstruído pelo Detetive (Reversa) em 2026-11-27 a partir do código e do histórico. 🟢

**Situação:** aceita (em vigor na v1.4)

## Contexto
Repetir ou injetar pacotes sem critério congestiona a frequência.

## Decisão
Funções opcionais, com dedupe, limites por minuto, raio, carga do canal e atraso viscoso.

## Consequências
Comportamento conservador; o usuário precisa ligar e entender as opções.

## Evidência
`core/Igate.kt`, `core/SmartDigipeater.kt`
