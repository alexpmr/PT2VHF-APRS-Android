# Fluxograma — gateway-igate-digi

> Arqueólogo, 2026-11-27. 🟢 `core/Igate.kt`, `core/SmartDigipeater.kt`, `service/AprsService.kt:931-1005`.

## Pacote ouvido no TNC

```mermaid
flowchart TD
    A[linha do TNC] --> B[anota estação local se não for terceiro nem NOGATE]
    B --> C{iGate permitido?<br/>ligado + configurado + não RX + APRS-IS verificado}
    C -- sim --> D[rfToIs]
    D -- Send --> E{duplicata em 30 s?}
    E -- não --> F[envia ao APRS-IS com qAR,MEUCALL]
    C -- não --> G
    D -- Skip --> G
    E -- sim --> G
    G{digi permitido?<br/>ligado + TX RF + TNC} -- sim --> H[SmartDigipeater.onHeard]
    H -- Repeat atraso 0 --> I[transmite]
    H -- Repeat com atraso --> J[espera viscosa] --> K{vizinho repetiu?}
    K -- não --> I
    K -- sim --> L[cancela COVERED]
    H -- Drop --> M[conta motivo]
```

## Linha do APRS-IS (IS→RF)

```mermaid
flowchart TD
    A[linha APRS-IS] --> B{IS→RF ligado + TX RF + TNC?}
    B -- não --> Z[nada]
    B -- sim --> C{é mensagem/ack, não boletim/NWS, não própria?}
    C -- não --> Z
    C -- sim --> D{destinatário ouvido local em RF?}
    D -- não --> Z
    D -- sim --> E{remetente também local?}
    E -- sim --> Z
    E -- não --> F{dupe 30 s ou acima do limite/min?}
    F -- sim --> Z
    F -- não --> G["transmite }ORIG>DEST,TCPIP,MEUCALL*:info"]
```
