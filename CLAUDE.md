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

- **Todo novo item de backlog deve ser registrado imediatamente no GitHub:** atualizar `BACKLOG.md` na branch principal `main` e realizar commit de documentação no mesmo atendimento (`docs(backlog):`, nunca `release:`). Não basta mencionar a pendência na conversa. Confirmar o commit/link; não implementar o item nem gerar versão até pedido explícito. Se o commit falhar, informar claramente que o registro não foi concluído.


---

# Reversa

> Framework de Engenharia Reversa instalado neste projeto.

## Como usar

Use o fluxo adequado no chat:

- `/reversa` — descobrir e documentar um sistema existente
- `/reversa-new` — criar PRD e specs para um projeto novo
- `/reversa-forward` — implementar ou evoluir código a partir das specs
- `/reversa-migrate` — planejar a migração de um sistema legado
- `/reversa-docs` — gerar o mini-site visual da documentação
- `/impeccable` — design de front-end: criticar, auditar e polir interfaces (Impeccable, Apache 2.0)
- `/reversa-agents-help` — consultar o catálogo completo de agentes

## Comportamento ao ativar

Quando o usuário digitar `/reversa` ou a palavra `reversa` sozinha em uma mensagem:

1. Ative o skill `reversa` disponível em `.claude/skills/reversa/SKILL.md`
2. Se não encontrar em `.claude/skills/`, tente `.agents/skills/reversa/SKILL.md`
3. Leia o SKILL.md na íntegra e siga exatamente as instruções do Reversa

## Regra não-negociável

Por padrão, nunca apague, modifique ou sobrescreva arquivos pré-existentes do projeto legado:
o Reversa escreve apenas em `.reversa/`, `_reversa_sdd/`, `_reversa_docs/`, `_reversa_forward/`, `_reversa_bugs/` e `_reversa_refactor/`.
A única exceção é a política configurável abaixo, controlada exclusivamente pelo usuário.

Antes de criar, modificar ou apagar qualquer arquivo fora das pastas próprias do Reversa, leia `.reversa/reversa-config.json` e obedeça ao resultado:

- Arquivo ausente, JSON inválido ou campo com tipo errado: trate como `allowLegacyEdits: false` (falha segura, nenhuma escrita fora das pastas do Reversa).
- `allowLegacyEdits: false`: recuse a escrita, informando o caminho recusado, o estado atual da config e o que o usuário deve editar para liberar.
- `allowLegacyEdits: true` com `allowedPaths` não vazio: escreva apenas em caminhos que casem com algum glob da lista (globs relativos à raiz do projeto, com `/`, suportando `*` e `**`).
- `allowLegacyEdits: true` com `allowedPaths` vazio ou ausente: projeto liberado; avise uma vez por sessão que a liberação é irrestrita.

Nunca crie nem edite `.reversa/reversa-config.json` por iniciativa própria: pedido na conversa não é liberação implícita, alterações nesse arquivo são ato exclusivo do usuário.
