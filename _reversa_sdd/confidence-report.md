# Relatório de Confiança — PT2VHF-APRS-Android

> Gerado pelo Revisor (Reversa) em 2026-11-27. Base `main` @ `79e6b39` (v1.4). Nível Completo, modo autônomo.

## 1. Resultado geral

| Marcador | Quantidade | % |
|----------|-----------:|--:|
| 🟢 Confirmado no código | 416 | 95,6% |
| 🟡 Inferido | 16 | 3,7% |
| 🔴 Lacuna | 3 | 0,7% |
| **Total** | **435** | |

**Confiança ponderada: 97,5%** (🟢 = 1, 🟡 = 0,5, 🔴 = 0).

As três lacunas não são dúvidas de leitura; são funções que **não existem** na `main`: posição comprimida (#109, aparece em RF-PA-12 e US-18) e reconexão automática do Bluetooth clássico (RF-ST-13).

## 2. Confiança por módulo

| Módulo | Confiança | Observação |
|--------|-----------|------------|
| protocolo-aprs | Alta | Kotlin puro, 10 arquivos de teste |
| modem-e-radio | Alta | Testado na JVM; desempenho real em rádio depende do aparelho |
| gateway-igate-digi | Alta no código / média em campo | Regras testadas; sem validação em RF real |
| estado-e-persistencia | Alta | Stores SQLite e `Settings` sem teste direto |
| servico-e-transportes | Média-alta | Sem testes automatizados; pontos de concorrência A1/A3 inferidos |
| atualizacao-e-site | Alta | Variante do Google Play fora da `main` |
| api-externa | Alta | Contrato documentado em `API.md` e no código |
| interface | Média | Lida por amostragem; sem testes de UI |

## 3. Verificações feitas pelo Revisor
- Nomes de função, arquivos de teste, permissões do manifest, contrato da API e resposta do `update.php` conferidos de novo no código; três afirmações corrigidas (tiles CARTO inexistentes, permissões não declaradas, nomes de testes).
- Diagramas Mermaid revisados quanto a blocos fechados.
- Nenhum arquivo fora de `.reversa/` e `_reversa_sdd/` foi alterado pelos agentes de análise (o instalador acrescentou o bloco Reversa ao `CLAUDE.md`).

## 4. Limites da análise
- Pasta `ios/` fora do escopo.
- Nada foi compilado nem executado (pedido do mantenedor durante o teste fechado do Google Play).
- PRs abertos (#31 Play, #33 LoRa) não foram analisados, só citados.
