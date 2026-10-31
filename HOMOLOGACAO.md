# Homologação do PT2VHF APRS Client for Android

Atualizado em 31/10/2026. **Esta matriz não é um release.**

- Release **v0.1.3** gerada e assinada no GitHub e distribuída pelo site oficial; homologação física de itens do backlog continua pendente.
- Desenvolvimento integrado à `main` por meio do PR [#7](../../pull/7); release [v0.1.2](../../releases/tag/v0.1.2).
- Versão para instalação: **0.1.3**, APK de produção assinado e verificado no GitHub Actions. A homologação física pelo usuário permanece obrigatória; não marcar os 22 itens como totalmente concluídos.
- Status: `Não iniciado` → `Implementação parcial` → `Pronto para teste físico` → `Validado pelo usuário` → `Concluído/baixado`.
- **Regra de baixa:** nunca baixar um item apenas porque o código compilou; registrar evidência, validar todas as partes exigidas e separar pendências residuais. Atualizar `BACKLOG.md` e registrar commit a cada baixa.
- **Segurança:** use baixa potência e frequência/canal autorizado; não envie beacons de posição residencial sem querer. O perfil 'somente RX' ainda não está implementado; desligue TX RF e APRS-IS para testes exclusivamente de recepção.

## Estado por item (sem alegação de conclusão)

| # | Funcionalidade | Situação no PR #7 | Teste obrigatório / restrição |
|---|---|---|---|
| 1 | Símbolos APRS e comentário padrão | **Falha observada pelo usuário na v0.1.2**: mapa ainda apresenta ícones genéricos/incorretos | Renderizar símbolos APRS de ambas as tabelas e overlays; comparar em aparelho real com referência APRSdroid e registrar prints |
| 2 | Conversas: filtros completos e ordem da atividade mais recente primeiro | **Ampliação de backlog, pendente na v0.1.3** | T21: ordem por último RX/TX, filtros combinados por contato/SSID, texto, data, direção, meio, status, persistência; não lidas somente com estado de leitura real |
| 3 | Estações: atualização, ordenação e filtragem avançadas | **Pendente na v0.1.2** | Novos pacotes atualizam lista; filtro real por nome, períodos de primeira/última recepção, distância mínima/máxima, fonte/tipo; ordem ASC/DESC; critérios combinados e persistidos |
| 4 | Meus enlaces RF | Parcial: origem RF por TNC corrigida e teste unitário | Validar pacote TNC versus APRS-IS; interface própria de enlaces pendente |
| 5 | Tracklog e estilo | Parcial: GPS/SQLite/linha e controles básicos | Verificar gravação, pausa, permanência, estilo e limpeza; GPX/ajustes avançados pendentes |
| 6 | Configurações APRSdroid | Parcial: modo manual/periódico/Smart com parâmetros | Testar intervalos em movimento/parado; demais recursos permanecem pendentes |
| 7 | Persistência de estações/enlaces | Código para QA | Forçar encerramento, reabrir sem internet e verificar horários originais |
| 8 | Cabeçalho verde/vermelho | Código para QA | Simular quedas/reconexões APRS-IS, TNC e GPS |
| 9 | APRS-IS automático configurável | Código para QA | Desligar, reiniciar/trocar redes e assegurar nenhuma reconexão; RX sem TX |
| 10 | Perfis Base/Móvel/Portátil/RX | Não implementado | Não homologar |
| 11 | Seguir estação móvel | Parcial: centrar, pausar, retomar | Verificar posições sucessivas com estação real; trajeto remoto pendente |
| 12 | Favoritos/alertas | Parcial: favoritos persistentes | Verificar marcação e filtro; alertas por eventos pendentes |
| 13 | Centro de diagnóstico | Parcial: métricas APRS-IS/TNC/monitor | Validar RX/TX e origem; exportação/diagnóstico avançado pendentes |
| 14 | Recordes e cobertura RF | Não implementado | Requer controle rigoroso de evidência TNC |
| 15 | API Android externa | Não implementado | Requer auditoria de permissões e testes de segurança |
| 16 | Confirmação de saída | **Não implementado; backlog registrado** | Confirmar/cancelar Back e verificar continuidade ou parada explícita do tracker/TNC/TX |
| 17 | Restaurar mapa ao abrir | **Pendente na v0.1.2; centro e zoom fixos identificados no código** | Recuperar centro e zoom após reabrir; não recentralizar pelo GPS; botão Minha localização; cenário sem GPS |
| 18 | Versão instalada no aviso de site oficial | Implementado no PR #8; CI/homologação pendentes | Popup inicial mostra BuildConfig.VERSION_NAME e www.PT2VHF.com; OTA permanece separado |
| 19 | Enviar mensagem nos detalhes da estação | **Ação no código existente; usabilidade não homologada na v0.1.3** | Botão de envio direto sempre visível; abre conversa privada com indicativo/SSID correto; nenhum TX até o comando Enviar |
| 20 | Responder por pressão prolongada em mensagem | **Não implementado na v0.1.3; backlog #20** | Long press, menu Responder, destinatário privado correto, texto existente preservado e ausência de TX automático |
| 21 | Pesquisar estação para nova mensagem | **Não implementado na v0.1.3; backlog #21** | Busca por indicativo parcial/completo em estações + histórico, seleção exata de SSID, opção manual e cache offline |
| 22 | Brilho independente do mapa | **Pendente v0.1.3; backlog #22** | T22: ajuste 50–150%, 100% neutro, somente fundo cartográfico, persistência e legibilidade dos elementos APRS |


## Roteiro de testes físicos, em ordem

**T01 — Instalação/atualização:** instalar o APK v0.1.2 assinado sobre v0.1.0, sem desinstalar. Confirmar versão 0.1.2, configurações antigas e mensagens preservadas. Se a assinatura não corresponder, **não desinstalar para tentar corrigir**; registrar a falha.

**T02 — GPS/cabeçalho:** com localização autorizada, abrir em área com fix, conferir GPS verde; desativar localização ou perder fix e verificar GPS vermelho; restaurar. Aguardar pelo menos 2 minutos na perda de fix.

**T03 — APRS-IS:** habilitar conexão automática e verificar login 'verified' (verde); desligar opção nas Configurações, salvar, alternar Wi-Fi/dados móveis, fechar/reabrir: deve permanecer vermelho e sem reconectar. Reativar e confirmar verde. Desativar 'Transmitir pelo APRS-IS' mantendo conexão: ainda deve receber estações.

**T04 — TNC Bluetooth KISS:** conectar um rádio/TNC real, conferir TNC verde, pacotes RX e contadores. Desligar Bluetooth/TNC e confirmar vermelho; religar e reconectar. Usar somente frequências e potência autorizadas.

**T05 — Envio RF controlado:** com equipamento e configuração confirmados, transmitir beacon manual, conferir contador TX do TNC e recepção por uma segunda estação/receptor independente. Registrar indicativo, via e evidência real, evitando que retransmissão via APRS-IS seja interpretada como recepção direta.

**T06 — Persistência offline:** receber ao menos cinco estações e um enlace RF real; fechar completamente o processo; desativar internet e reabrir. Conferir estações, horários antigos, favoritos e histórico; atualizar depois da reconexão sem duplicar.

**T07 — Mensagens/estações:** testar ordenação e filtros (distância, último contato, indicativo, tipos, texto e hora), seleção por ícone/painel de classificação e filtro com múltiplas categorias, e se escolhas persistem após reiniciar. Durante a aba Estações aberta, verificar inserção automática de novas estações e atualização de estações já presentes sem qualquer refresh; comparar APRS-IS e TNC RF. Registrar vídeo com a lista e o contador RX para evidenciar a chegada de pacotes.

**T08 — Tracker/tracklog:** deslocar-se em local seguro com tracker ligado e áudio/radio verificado; gravar trecho curto; verificar linha, limpeza e retenção após reiniciar. Conferir modos manual, periódico e SmartBeaconing evitando transmissão excessiva.

**T09 — Seguir estação:** escolher móvel real com pelo menos três posições novas, testar recentralização, arrastar mapa para pausar, retomar e cancelar.

**T10 — Idiomas:** mudar o idioma por aplicativo no Android (se disponível) para inglês, espanhol, francês e italiano; conferir mapa, mensagens, rádio, tracker e configurações. Em idioma não suportado, confirmar fallback inglês.

**T11 — Favoritos/diagnóstico:** marcar favoritas, fechar e reabrir, filtrar, conferir contadores separados de APRS-IS/TNC e o monitor.

**T12 — Publicação OTA:** após o APK v0.1.2 real estar hospedado, comparar versão/manifesto, atualização in-app, URL e contador cumulativo `android-apk` na Hostinger.


**T13 — Reatividade e filtros avançados das estações (falha observada v0.1.2):** com a aba Estações aberta e filtros em "Todos", receber pelo menos três novos indicativos, sem tocar na tela; todos devem aparecer automaticamente. Receber novas posições/horários dos mesmos indicativos e conferir atualização dos detalhes e reordenação (mais recente/proximidade) sem trocar de aba. Abrir o ícone de classificação/filtro, escolher explicitamente categoria(s), favoritos e ordenação, aplicar, limpar, persistir após reinicializar e conferir que estações filtradas não foram apagadas. Comparar os pacotes recebidos no monitor/diagnóstico e distinguir falha de recepção da falha de exibição. Anexar evidências de RX/APRS-IS e de TNC RF quando disponível. Verificar separadamente filtros reais por indicativo, primeira recepção e última recepção (intervalos de datas/horas), distância mínima/máxima e tipo, combinados; conferir ordem ascendente e descendente de cada critério, valores sem GPS e persistência após reiniciar.

**T14 — Confirmação de saída (item #16):** na tela principal, acionar o botão/gesto Voltar, conferir diálogo e escolher **Cancelar** — a interface deve continuar aberta. Repetir e escolher **Sair** — a interface deve encerrar. Com tracker/beacon ativo, validar as alternativas de sair mantendo o serviço ou interrompendo o rastreamento, verificando notificação e ausência de TX inesperado. Conferir persistência de estações/configurações após reabrir e que a confirmação não apareça ao fechar menus ou trocar de aba. Não esperar diálogo ao usar Home, encerramento forçado ou ao remover o processo pelo sistema.

**T15 — Restauração da vista do mapa (item #17):** com GPS ativo, navegar manualmente para uma área diferente e ajustar o zoom; sair totalmente e reabrir o aplicativo, conferindo **mesmo centro e zoom** sem recentralização automática ao receber novo fix GPS. Tocar no ícone **Minha localização** e conferir deslocamento para a posição real; arrastar novamente e verificar que o GPS não recentraliza sozinho. Repetir após trocar de aba, girar a tela e reabrir sem internet/GPS. Em instalação limpa, a primeira abertura deve usar a posição atual quando disponível; testar interação com "Seguir estação" e cancelamento sem perder a vista manual. Registrar capturas antes/depois, valores aproximados do centro e zoom, versão do Android e modelo do aparelho.

**T16 — Versão atual no aviso do site oficial (item #18):** instalar APK v0.1.3 no aparelho, abrir do zero e conferir o popup de boas-vindas com **"Versão instalada: 0.1.3"**, endereço/link **www.PT2VHF.com**, sem mostrar versão do servidor como instalada. Fechar aviso e trocar de abas: não pode reaparecer. Encerrar e abrir novamente: deve reaparecer. Com atualização OTA detectada, garantir popup separado após o anúncio inicial. Conferir as traduções PT/EN/ES/FR/IT.

**T17 — Mensagem direta nos detalhes da estação (item #19):** selecionar uma estação na aba Estações, abrir os detalhes, localizar o botão destacado **Enviar mensagem** sem rolar horizontalmente, tocar e verificar que abre a conversa privada com o indicativo correto, inclusive SSID, histórico preservado e campo de digitação pronto. Repetir selecionando estação pelo mapa, em celulares com telas pequenas e fonte grande, e em todos os idiomas. Confirmar que abrir a conversa **não transmite nada**; somente enviar quando o usuário acionar Enviar e a configuração permitir. Conferir APRS-IS e TNC físico quando disponíveis, além dos caminhos de cancelar/voltar e selecionar outra estação. Anexar print e registro do TX quando aplicável.

**T18 — Pressionar mensagem para responder (item #20):** em Mensagens, abrir conversa privada com mensagem recebida e pressionar a bolha por aproximadamente um segundo. Confirmar que surge opção **Responder** e que o compositor identifica o remetente correto (incluindo SSID), exibe contexto da mensagem e permite cancelar. Repetir em mensagens CQ/boletim e confirmar que a resposta abre um diálogo **privado** com o remetente, sem enviar CQ involuntário. Testar mensagens enviadas pelo próprio usuário, texto já em digitação, rolagem, fonte ampliada e cenário offline. Atestar ausência de TX ao abrir o menu, selecionar Responder e cancelar; somente Enviar deve originar transmissão, respeitando limite de payload e ACK existentes. Não exigir citação automática nem threading remoto, inexistentes no formato APRS convencional.

**T19 — Pesquisar indicativo para iniciar conversa nova (item #21):** em Mensagens → Conversas, tocar em **Nova mensagem** e procurar por `PT2`, `VHF`, `-9` e um indicativo completo, confirmando resultados sem distinção de maiúsculas/minúsculas. Os resultados devem combinar estações recebidas por APRS-IS/TNC (do cache) e indicativos do histórico de mensagens; sem duplicatas. Selecionar indicativos com diferentes SSIDs e verificar abertura do contato exato, histórico correto e ausência de transmissão automática. Repetir sem internet com cache, com lista recém-atualizada, lista sem correspondência e com digitação manual de indicativo válido e inválido. Conferir que filtro de conversas não se confunde com pesquisa de destinatário e que apenas o botão Enviar transmite.

**T20 — Domínio obrigatório no comentário transmitido (item #1, extensão):** nos dois aplicativos PT2VHF APRS Client (Android e desktop), preparar descrição de beacon padrão, personalizada curta, personalizada longa, vazia e que já contenha `www.PT2VHF.com` (inclusive grafia diferente). Com transmissões autorizadas, inspecionar o pacote APRS **bruto** e uma estação receptora independente em APRS-IS e RF/TNC: cada beacon deve anunciar **exatamente uma vez** `www.PT2VHF.com` na grafia indicada, sem `https://`, `/aprs` ou `/aprs-android`. No Android, confirmar o teto efetivo de 43 caracteres no comentário de `positionInfo()`, reservando 14 para a URL e mais 1 para espaço quando houver descrição; nunca truncar o domínio, nem sobrescrever no armazenamento o comentário personalizado, apenas adaptar sua cópia transmitida e indicar cortes ao usuário. Verificar que abrir/atualizar o aplicativo **não envia** beacon extra. O popup de qualquer estação deve exibir fielmente o dado recebido, inclusive URLs de terceiros, e não colocar artificialmente o domínio PT2VHF na informação dessas estações. Não declarar o requisito concluído antes de captura do payload e teste físico.

**T21 — Filtros completos e ordem por última atividade nas Conversas (item #2):** com 3 ou mais conversas contendo horários diferentes, abrir Mensagens → Conversas e verificar a ordem **mais recente → menos recente**, calculada pela última mensagem recebida **ou enviada** de cada interlocutor, mesmo após reabrir o app (sem preferência anterior: mais recente como padrão; com preferência anterior explicitamente escolhida: respeitá-la). Com a lista aberta, receber/enviar uma mensagem nova em conversa antiga e confirmar que ela sobe automaticamente para o topo. Selecionar no ícone de filtro ordem inversa e ordenação alfabética com direção explícita. Aplicar simultaneamente busca por indicativo parcial/SSID, texto de mensagem antiga, período com limites inclusivos e fuso local, mensagens recebidas/enviadas, meio APRS-IS/RF quando identificado e estado de transmissão/ACK quando existente. Conferir contagem de conversas filtradas, indicação clara de filtros ativos, limpar/restaurar padrões, persistência após reinício, ausência de correspondências e histórico preservado ao abrir conversa filtrada. A prévia e horário da conversa devem refletir a **última mensagem real**, e não uma mensagem antiga que correspondeu ao texto pesquisado. Só exigir filtro "não lidas" após implantação e teste de indicador de leitura persistente; não inventar estado de leitura. Não confundir este filtro de **conversas existentes** com a pesquisa de destinatários para nova conversa (item #21). Testar com lista extensa e fontes grandes.

**T22 — Ajuste de brilho apenas do mapa (item #22):** em Mapa, conferir ícone com símbolo de sol e slider de 50% a 150%, padrão 100%. Ajustar 50/75/125/150% e observar o fundo cartográfico alterando imediatamente, sem escurecer/clarear marcadores APRS, trilhas, enlaces, rótulos, botões nem modais. Conferir contraste com tema claro/escuro, mapas em cache e internet desconectada. Alterar zoom, mover mapa, recentralizar na própria posição e usar Seguir estação; trocar de aba, fechar/reabrir, girar a tela e confirmar brilho salvo. Restaurar 100%, validar porcentagem exata e garantir que o brilho do Android não mudou, inclusive sem permissão para alterar configurações do dispositivo. Testar com aparelho de tela pequena, fonte ampliada e fontes cartográficas disponíveis. Registrar capturas dos percentuais e observações de desempenho.

## Como informar o resultado

Responder com códigos e evidência objetiva, por exemplo:

`T01 OK — atualização preservou mensagens; T02 OK — GPS verde/vermelho; T03 FALHA — reconectou após desabilitar, log/imagem...`

Após cada resultado, reavaliar o item, corrigir erros se houver e atualizar os dois arquivos `BACKLOG.md` e `HOMOLOGACAO.md` com o commit correspondente. Baixar somente os itens totalmente validados.
