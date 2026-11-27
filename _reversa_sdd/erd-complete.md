# Modelo de Dados Completo (ERD)

> Gerado pelo Arquiteto (Reversa) em 2026-11-27. Não há chaves estrangeiras declaradas; as relações abaixo são lógicas (pelo indicativo). 🟡

```mermaid
erDiagram
    STATIONS {
        TEXT call PK
        REAL lat
        REAL lon
        TEXT symbol_table
        TEXT symbol
        TEXT comment
        INTEGER heard
        INTEGER first_seen
        TEXT medium
        TEXT via
        REAL speed
        INTEGER course
        TEXT heard_by
    }
    RF_LINKS {
        TEXT source PK
        TEXT destination PK
        INTEGER seen
    }
    OWN_TRACK {
        INTEGER id PK
        INTEGER recorded
        REAL lat
        REAL lon
    }
    RF_EVENTS {
        INTEGER id PK
        TEXT call
        TEXT kind
        TEXT via
        INTEGER direct
        INTEGER time
        REAL distance
        REAL lat
        REAL lon
        REAL own_lat
        REAL own_lon
    }
    MESSAGES {
        INTEGER id PK
        TEXT from_call
        TEXT to_call
        TEXT text
        INTEGER time
        INTEGER outgoing
        TEXT medium
        TEXT via
        TEXT msg_id
        TEXT status
        TEXT kind
        INTEGER reply_to
    }
    PACKETS {
        INTEGER id PK
        INTEGER time
        TEXT dir
        TEXT medium
        TEXT raw
        TEXT src
        TEXT dst
        TEXT type
    }
    STATIONS ||--o{ RF_LINKS : "source/destination"
    STATIONS ||--o{ RF_EVENTS : "call"
    STATIONS ||--o{ MESSAGES : "from_call/to_call"
    STATIONS ||--o{ PACKETS : "src"
    MESSAGES |o--o| MESSAGES : "reply_to"
```

| Banco | Tabelas | Versão | Retenção |
|-------|---------|--------|----------|
| `stations.db` | stations, rf_links, own_track, rf_events | 3 | 30 d / 30 d / 7 d (config.) / 365 d |
| `messages.db` | messages | 1 | por categoria (padrão sem prazo, 5000) |
| `packets.db` | packets | 2 | 7 d / 20.000 linhas (config.) |

Armazenamento fora do SQLite: SharedPreferences `pt2vhf_aprs` (configurações, favoritas, objetos em JSON, snapshots de perfil) e `filesDir/routes.json`. Detalhes em `data-dictionary.md`.
