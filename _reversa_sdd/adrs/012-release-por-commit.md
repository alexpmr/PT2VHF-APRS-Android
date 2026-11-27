# ADR-012: Versão gerada por commit `release:` na main

> Reconstruído pelo Detetive (Reversa) em 2026-11-27 a partir do código e do histórico. 🟢

**Situação:** aceita (em vigor na v1.4)

## Contexto
Um único mantenedor; processo simples e reprodutível.

## Decisão
O GitHub Actions gera o APK assinado quando o título do commit na `main` começa com `release:`; a assinatura vem de segredos do repositório.

## Consequências
Release rastreável; risco de disparo acidental se o prefixo for usado fora de versão (regra no `CLAUDE.md`).

## Evidência
`.github/workflows/android.yml`, `CLAUDE.md`
