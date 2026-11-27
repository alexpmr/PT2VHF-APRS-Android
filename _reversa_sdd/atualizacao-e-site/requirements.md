# Requisitos — Atualização e site

> Gerado pelo Redator (Reversa) em 2026-11-27. Módulo `atualizacao-e-site` · `data/UpdateManager.kt, data/UpdateCheck.kt`

## Objetivo
Avisar sobre nova versão publicada no site oficial e registrar o indicativo para a lista de estações.

## Requisitos funcionais

| ID | Requisito | Conf. |
|----|-----------|-------|
| RF-AS-01 | O sistema deve consultar `https://www.pt2vhf.com/update.php?product=aprs-android` (com `&call=` se permitido), sem seguir redirecionamento e com timeout de 10 s. | 🟢 |
| RF-AS-02 | O sistema só deve aceitar link de APK do prefixo `https://www.pt2vhf.com/`. | 🟢 |
| RF-AS-03 | O sistema deve consultar 1 h ao voltar ao app, 12 h em segundo plano, 30 min após falha, e na hora se o par indicativo|versão ainda não foi informado. | 🟢 |
| RF-AS-04 | O sistema deve notificar uma vez por versão e nunca instalar sozinho. | 🟢 |
| RF-AS-05 | O sistema deve permitir "Verificar agora" em Configurações → Sobre. | 🟢 |
| RF-AS-06 | Na versão do Google Play, o aviso de atualização deve ficar desligado e o registro do indicativo mantido. | 🟡 PR #31, fora da `main` |

## Requisitos não funcionais

- Comparação de versão numérica `x.y.z` (1.0 > 0.1.15).

## Critérios de aceite

- **RF-AS-01**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-AS-02**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
- **RF-AS-03**: dado o cenário descrito, o comportamento observável corresponde ao requisito e ao teste/código citado em `design.md`.
