# Domínio e Regras de Negócio — PT2VHF-APRS-Android

> Gerado pelo Detetive (Reversa) em 2026-11-27. Escala: 🟢 CONFIRMADO · 🟡 INFERIDO · 🔴 LACUNA

## 1. Glossário

| Termo | Significado no sistema | Conf. |
|-------|------------------------|-------|
| APRS | Automatic Packet Reporting System: rede de radioamador para posição, mensagens, clima e telemetria | 🟢 |
| APRS-IS | Rede de servidores na internet que espelha o tráfego APRS (padrão `soam.aprs2.net:14580`) | 🟢 |
| Indicativo / SSID | Identificação do radioamador (ex. `PT2VHF`) e sufixo 0–15 que distingue aparelhos (`-9` = móvel) | 🟢 |
| Passcode | Número público derivado do indicativo, exigido no login do APRS-IS para poder transmitir | 🟢 |
| Login verificado | Servidor confirmou o passcode (`verified`); sem isso o app só recebe pela internet | 🟢 |
| TNC | Modem de pacotes (no rádio ou externo) falando KISS; o app também tem um TNC por áudio próprio | 🟢 |
| KISS | Protocolo de enquadramento entre app e TNC | 🟢 |
| Beacon | Pacote de posição da própria estação | 🟢 |
| SmartBeaconing | Algoritmo que ajusta a frequência do beacon pela velocidade e pelas curvas | 🟢 |
| Path / digipeater | Caminho de repetição (`WIDE1-1,WIDE2-1`); digipeater é a estação que repete | 🟢 |
| iGate | Estação que leva pacotes de RF para o APRS-IS (e, opcionalmente, mensagens de volta) | 🟢 |
| q-construct | Marca `qAR`, `qAC` etc. que o APRS-IS acrescenta ao caminho | 🟢 |
| Mic-E | Formato compacto de posição usado por rádios Kenwood/Yaesu, com dados no destino | 🟢 |
| Objeto / item | Posição publicada em nome de outra coisa (evento, repetidora) | 🟢 |
| Boletim | Mensagem para `BLN*`, sem ACK | 🟢 |
| ACK / REJ | Confirmação ou recusa de mensagem com ID | 🟢 |
| Perfil | Conjunto de ajustes: personalizado, base, móvel, portátil, somente recepção | 🟢 |
| Favorita | Estação marcada para alertas | 🟢 |
| Rodada (net) | Encontro periódico por mensagens (ex. APRS Thursday) | 🟢 |

## 2. Entidades de domínio

| Entidade | Identidade | Ciclo de vida | Conf. |
|----------|------------|---------------|-------|
| Estação | indicativo (ou nome do objeto) | Criada na primeira posição recebida; atualizada a cada pacote; removida após 30 dias sem ouvir, por idade máxima configurada ou por ação do usuário | 🟢 |
| Mensagem | id local + ID APRS | Recebida ou criada no envio; muda de situação até ACK, recusa, desistência; apagada por retenção ou pelo usuário | 🟢 |
| Enlace RF | (origem, destino) | Gravado a cada pacote do TNC; 30 dias | 🟢 |
| Evento RF | id | Estação ouvida direto/via digi, ou meu pacote repetido; 365 dias | 🟢 |
| Trilha própria | id | Ponto por fix com tracker ligado; padrão 7 dias | 🟢 |
| Pacote do log | id | Todo RX/TX; padrão 7 dias e 20.000 linhas | 🟢 |
| Objeto próprio | id | Criado pelo usuário, reenviado no intervalo, encerrado com "kill" | 🟢 |
| Rota | id | Importada de GPX, até 20.000 pontos, visível ou oculta | 🟢 |

## 3. Regras de negócio

### 3.1 Identidade e transmissão
- RN-01 Sem indicativo válido pela ITU o app não transmite nada (nem RF nem APRS-IS); recebe normalmente. 🟢
- RN-02 O SSID é acrescentado só de 1 a 15; `0` significa sem sufixo. 🟢
- RN-03 O perfil "Somente recepção" bloqueia toda transmissão: beacon, mensagem, ACK, objeto, iGate IS→RF e digi. 🟢
- RN-04 Beacon sai por RF (se TX por RF ligado e TNC conectado) **e** por APRS-IS (se TX por internet ligado e login verificado). 🟢
- RN-05 Mensagem privada sai por APRS-IS primeiro; RF só se a internet não estiver disponível; nunca pelos dois, para não duplicar o ID. 🟢
- RN-06 O comentário do beacon sempre termina com `www.PT2VHF.com`; URLs do site digitadas pelo usuário são removidas para não repetir; total ≤ 43 caracteres. 🟢
- RN-07 Curso e velocidade só entram no beacon com velocidade ≥ 5 km/h e posição vinda do GPS. 🟢
- RN-08 Ambiguidade de posição 0–4 troca dígitos dos minutos por espaço na transmissão. 🟢

### 3.2 Beacon
- RN-09 Modo inteligente: SmartBeaconing com os parâmetros configurados; modo periódico: intervalo fixo (mínimo 300 s com economia de bateria); modo manual: só pelo botão. 🟢
- RN-10 Nunca há dois beacons na fila de transmissão. 🟢
- RN-11 Com "minutos parado" > 0, o beacon desacelera para o intervalo lento quando a estação está parada. 🟡

### 3.3 Mensagens
- RN-12 Texto acima de 67 caracteres é dividido em partes, preferindo espaço. 🟢
- RN-13 Acentos removidos no envio quando a opção estiver ligada (padrão). 🟢
- RN-14 Retentativas: 5 tentativas com espera 30 s × n; sem ACK vira "Sem ACK após N tentativas". 🟢
- RN-15 O usuário pode cancelar tentativas, reenviar, ou desistir e reenviar em um toque. 🟢
- RN-16 Mensagem recebida para mim (indicativo completo ou base) recebe ACK no máximo 1 vez a cada 20 s por (remetente, ID); o ACK sai pelo mesmo meio em que a mensagem chegou e do endereço que o remetente usou. 🟢
- RN-17 Cópia repetida da mesma mensagem (dedupe de 1 h) não aparece de novo, mas recebe ACK outra vez. 🟢
- RN-18 Reply-ack `{MM}AA` confirma a mensagem `AA`. 🟢
- RN-19 Boletins não têm ACK e têm dedupe de 24 h. 🟢
- RN-20 Envios pendentes que estavam em curso quando o serviço parou viram "Interrompida". 🟢
- RN-21 Retenção por categoria (privada, geral, boletim) em dias e quantidade; mensagens pendentes nunca são apagadas pela retenção. 🟢

### 3.4 Recepção e mapa
- RN-22 Pacote sem posição nunca cria estação nem plota em (0,0); só atualiza a já conhecida. 🟢
- RN-23 Terceiros `}` são desencapsulados uma vez e o gateway é lembrado. 🟢
- RN-24 Posição comprimida (base-91) não é decodificada hoje (backlog #109). 🟢
- RN-25 `heardBy` guarda o primeiro digipeater que entregou por RF. 🟢
- RN-26 Objeto vivo entra no mapa pelo nome; objeto encerrado sai. 🟢

### 3.5 iGate e digipeater (desligados por padrão)
- RN-27 iGate RF→IS só com APRS-IS verificado e fora do modo RX; descarta `TCPIP`, `NOGATE`, `RFONLY`, consultas e pacotes já com q-construct. 🟢
- RN-28 iGate IS→RF só mensagens para estação ouvida em RF nos últimos 30 min, nunca boletins, no máximo 6 por minuto. 🟢
- RN-29 Digipeater repete `WIDEn-N` até N máximo 2; acima disso reduz ou descarta (trap); fill-in só em salto direto. 🟢
- RN-30 Digi adapta-se à carga do canal (ocupado ≥ 30/min, saturado ≥ 60/min), limita 1 repetição por origem a cada 60 s e 20 por minuto; mensagens e emergências têm prioridade. 🟢
- RN-31 Atraso viscoso de 5 s: se outro digipeater repetir antes, o app desiste. 🟢

### 3.6 Alertas e notificações
- RN-32 Alertas de favoritas: primeira vez ouvida, volta após 60 min, mudança de posição ≥ 100 m, ouvida direto em RF; silêncio de 60 s depois de conectar; 10 min por chave; horário de silêncio. 🟢
- RN-33 Notificações por tipo (pessoal, boletim, grupo, ACK, eco de digi), cada uma com som próprio. 🟢

### 3.7 Atualização e privacidade
- RN-34 O app consulta a versão no site oficial e só aceita APK do domínio `https://www.pt2vhf.com/`; nunca instala sozinho. 🟢
- RN-35 O indicativo (sem SSID) é enviado na consulta de versão se "compartilhar indicativo" estiver ligado (padrão); a posição nunca. 🟢
- RN-36 Uma vez por versão, anuncia a versão com boletim `BLN0` pelo APRS-IS verificado, se a opção estiver ligada. 🟢
- RN-37 A API para outros apps fica desligada por padrão e cada classe de comando tem chave própria. 🟢

### 3.8 Bateria
- RN-38 Economia de bateria (ligada, ou automática ≤ 20%) só age com a tela desligada: GPS 30 s/50 m e beacon periódico ≥ 300 s. 🟢

## 4. Regras implícitas (inferidas)
- O app é pensado para o radioamador brasileiro (textos, servidor e comentários padrão), mas aceita indicativos de qualquer país. 🟡
- O site oficial é ao mesmo tempo canal de atualização, de estatística (registro de indicativos) e de divulgação (comentário do beacon). 🟡
- A segurança da transmissão depende do usuário ser radioamador licenciado; o app só valida formato e prefixo do indicativo. 🟡
