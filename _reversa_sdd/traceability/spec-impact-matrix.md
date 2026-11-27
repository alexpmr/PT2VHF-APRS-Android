# Matriz de Impacto entre Módulos

> Se mudar o módulo da linha, quais módulos da coluna precisam ser revistos. ● forte · ○ fraco

| Muda ↓ / Afeta → | protocolo | modem | gateway | estado | serviço | atualização | api | interface |
|------------------|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
| protocolo-aprs | — | ○ | ● | ● | ● | ○ | ○ | ● |
| modem-e-radio | ○ | — | | | ● | | | ○ |
| gateway-igate-digi | ○ | | — | ○ | ● | | | ○ |
| estado-e-persistencia | | | ○ | — | ● | ○ | ● | ● |
| servico-e-transportes | | ○ | ● | ● | — | ○ | ● | ● |
| atualizacao-e-site | | | | ○ | ○ | — | | ○ |
| api-externa | | | | ○ | ● | | — | |
| interface | | | | ○ | ○ | | | — |

Pontos de maior risco: mudanças em `InternalText` (valores gravados), no `ingest` do repositório e em `transmit` do serviço atingem quase tudo.
