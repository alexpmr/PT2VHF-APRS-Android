# CLAUDE.md

App Android (Kotlin + Jetpack Compose, osmdroid) do PT2VHF APRS Client. Textos, commits e documentação em português (BR).

- Estado do projeto e próximos passos: ver `BACKLOG.md` (seção "Onde estamos").
- Mensagens do usuário que começam com "Backlog:" são itens a registrar em `BACKLOG.md`, numerados, sem implementar.
  Implementar só quando ele pedir versão nova.
- Versão nova: implementar itens do backlog, atualizar `CHANGELOG.md`, `README.md` e a versão do app; um commit na
  `main` cujo título começa com `release:` gera o APK assinado no GitHub Actions. Não usar o prefixo `release:`
  em commits que não sejam de versão.
- Antes de subir: `./gradlew testDebugUnitTest assembleDebug`.
