# Backlog

Itens registrados para as próximas versões. Não implementar até o pedido de versão nova.

## Pendentes

### 1. Mapa: ícones APRS por estação/objeto e site PT2VHF no comentário padrão
_Registrado em 31/10/2026 (referência: print do mapa do APRSdroid)._

- **Ícones APRS no mapa:** hoje todas as estações e objetos aparecem com o mesmo marcador padrão do osmdroid
  (`ui/Screens.kt`, laço `for (s in stations.values)`). Desenhar cada uma com o ícone do seu símbolo APRS,
  usando a tabela (`/` primária, `\` alternativa) e o código do símbolo já decodificados em
  `Position.symbolTable`/`symbol` e guardados na estação. Overlays (tabela alfanumérica sobreposta ao
  símbolo alternativo) devem mostrar o caractere por cima do ícone. Vale também para a posição própria ("Você"),
  com o símbolo configurado nas preferências, e para o indicativo exibido ao lado do ícone, como no APRSdroid.
  Precisa de um sprite set de símbolos APRS com licença compatível (ex.: conjunto aprs-symbols de hessu/aprs.fi).
- **Site PT2VHF no comentário:** no APRSdroid o comentário da posição sai com `https://aprsdroid.org/`. No nosso
  app o texto padrão enviado deve mostrar `www.PT2VHF.com`. Hoje o padrão é `"PT2VHF APRS Client for Android"`
  (`data/Settings.kt`). Ajustar o padrão para incluir `www.PT2VHF.com` dentro do limite de 43 caracteres
  (ex.: `PT2VHF APRS Android www.PT2VHF.com`) e decidir se o site é anexado sempre, mesmo quando o usuário
  edita o comentário.
