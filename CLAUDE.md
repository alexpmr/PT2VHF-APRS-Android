# CLAUDE.md

App Android (Kotlin + Jetpack Compose, osmdroid) do PT2VHF APRS Client. Textos, commits e documentação em português (BR).

- Estado do projeto e próximos passos: ver `BACKLOG.md` (seção "Onde estamos").
- Mensagens do usuário que começam com "Backlog:" são itens a registrar em `BACKLOG.md`, numerados, sem implementar.
  Implementar só quando ele pedir versão nova.
- Versão nova: implementar itens do backlog, atualizar `CHANGELOG.md`, `README.md` e a versão do app; um commit na
  `main` cujo título começa com `release:` gera o APK assinado no GitHub Actions. Não usar o prefixo `release:`
  em commits que não sejam de versão.
- Antes de subir: `./gradlew testDebugUnitTest assembleDebug`.

- **Regra permanente do mantenedor para toda versão nova:** a entrega do Android inclui
  **obrigatoriamente** a atualização e validação do site oficial no repositório
  `alexpmr/PT2VHF-Site` (página `/android`, `updates.json` produto `aprs-android`,
  tabela de downloads, versão/data, changelog das cinco últimas versões, requisitos
  e recursos alterados). Copiar para `downloads/aprs-android/` somente o APK
  release assinado e verificado; confirmar tamanho/hash e disponibilidade pública.
  Manter a chave de contador `android-apk` entre versões para preservar o
  acumulado, incluindo downloads manuais e OTA, sem contagem dupla. Verificar
  `update.php?product=aprs-android` e implantação da Hostinger. Não marcar
  release como concluída se o site público ainda não estiver validado; relatar
  qualquer falha de publicação. Esta regra não autoriza criar releases sem pedido.
