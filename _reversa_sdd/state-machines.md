# Máquinas de Estado — PT2VHF-APRS-Android

> Gerado pelo Detetive (Reversa) em 2026-11-27.

## 1. Situação de mensagem enviada 🟢

```mermaid
stateDiagram-v2
    [*] --> Enviando
    Enviando --> SemConexao: nenhum meio disponível
    Enviando --> AguardandoACK: escrita no APRS-IS ou TNC
    Enviando --> Enviada: boletim / sem ID
    AguardandoACK --> AguardandoACK_N: sem ACK no intervalo (30 s × n)
    AguardandoACK_N --> AguardandoACK_N: nova tentativa
    AguardandoACK --> ACKRecebido: ack / reply-ack
    AguardandoACK_N --> ACKRecebido: ack / reply-ack
    AguardandoACK --> Rejeitada: rej
    AguardandoACK_N --> Rejeitada: rej
    AguardandoACK_N --> SemACK: esgotou as tentativas
    AguardandoACK --> Cancelada: usuário cancela
    AguardandoACK_N --> Cancelada: usuário cancela
    AguardandoACK --> Interrompida: serviço reiniciou
    AguardandoACK_N --> Interrompida: serviço reiniciou
    SemACK --> Enviando: reenviar
    Cancelada --> Enviando: reenviar
    Interrompida --> Enviando: reenviar
    SemConexao --> Enviando: reenviar
    ACKRecebido --> [*]
    Rejeitada --> [*]
```

Valores gravados (`InternalText`): `Enviando`, `Enviada · <meio>`, `Aguardando ACK · <meio>`, `Aguardando ACK (Nª tentativa)`, `ACK recebido`, `Rejeitada`, `Sem conexão para enviar`, `Sem ACK após N tentativas`, `Cancelada`, `Interrompida`. Mensagem recebida: `Recebida`.

## 2. Conexão APRS-IS 🟢

```mermaid
stateDiagram-v2
    [*] --> Desligado
    Desligado --> Conectando: connect_aprs_is = true
    Conectando --> Conectado: socket aberto, login enviado
    Conectando --> Espera: falha (timeout 15 s)
    Conectado --> Verificado: servidor responde "verified"
    Conectado --> SoRecepcao: "unverified" (indicativo inválido)
    Verificado --> Espera: queda / leitura sem dados 120 s
    SoRecepcao --> Espera: queda
    Espera --> Conectando: backoff 5 s → 120 s
    Conectado --> Desligado: pausa manual / desligar
    Verificado --> Desligado: pausa manual / desligar
```

## 3. Conexão TNC 🟢

```mermaid
stateDiagram-v2
    [*] --> Desconectado
    Desconectado --> Conectando: tipo escolhido + aparelho
    Conectando --> Conectado
    Conectando --> Falha
    Conectado --> Desconectado: queda
    Falha --> Conectando: backoff (TCP 60 s, BLE 60 s)
    Desconectado --> Conectando: reconexão automática (exceto BT clássico)
    Conectado --> Pausado: pausa manual
    Pausado --> Conectando: retomar
```

Observação: o Bluetooth clássico (SPP) não volta sozinho para `Conectando` (ponto de atenção A2).

## 4. Tracker (beacon automático) 🟢

```mermaid
stateDiagram-v2
    [*] --> Parado
    Parado --> Ativo: usuário liga / auto-start na abertura
    Ativo --> Parado: usuário desliga (tracker_explicit_off)
    Ativo --> AguardandoFix: sem GPS
    AguardandoFix --> Ativo: fix recebido
    Ativo --> FixPerdido: 2 min sem fix
    FixPerdido --> Ativo: novo fix
    Ativo --> Economia: tela desligada + economia ligada/auto
    Economia --> Ativo: tela ligada
```

## 5. Ciclo do envio de beacon 🟢

```mermaid
stateDiagram-v2
    [*] --> Ocioso
    Ocioso --> NaFila: SmartBeaconing / intervalo / botão
    NaFila --> NaFila: novo pedido ignorado (AtomicBoolean)
    NaFila --> Transmitindo: thread aprs-tx
    Transmitindo --> Ocioso: RF e/ou APRS-IS
    Transmitindo --> Ocioso: bloqueado (RX, sem indicativo)
```

## 6. Pacote no digipeater 🟢

```mermaid
stateDiagram-v2
    [*] --> Avaliar
    Avaliar --> Descartado: próprio, TCPIP, q-construct, caminho usado, trap
    Avaliar --> Prioritario: mensagem / emergência
    Avaliar --> Limites: alias genérico
    Limites --> Descartado: raio, carga, origem < 60 s, 20/min
    Limites --> Viscoso: vizinho já cobre
    Limites --> Repetir
    Viscoso --> Descartado: vizinho repetiu em 5 s
    Viscoso --> Repetir: tempo esgotado
    Prioritario --> Repetir
    Repetir --> [*]
    Descartado --> [*]
```

## 7. Notificação de atualização 🟢

`Sem consulta` → `Consultando` (1 h ao voltar, 12 h em segundo plano) → `Sem novidade` | `Nova versão` (notifica uma vez por versão) | `Falha` (tenta de novo em 30 min).
