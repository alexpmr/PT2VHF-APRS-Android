# Dependências — PT2VHF-APRS-Android

> Gerado pelo Scout em 2026-11-27. Fonte: `build.gradle.kts` e `app/build.gradle.kts`. Sem catálogo `libs.versions.toml`.

## Plugins de build

| Plugin | Versão | Confiança |
|--------|--------|-----------|
| `com.android.application` (AGP) | 8.7.3 | 🟢 |
| `org.jetbrains.kotlin.android` | 2.0.21 | 🟢 |
| `org.jetbrains.kotlin.plugin.compose` | 2.0.21 | 🟢 |

## Bibliotecas de runtime (`implementation`)

| Biblioteca | Versão | Uso no código | Confiança |
|------------|--------|---------------|-----------|
| `androidx.compose:compose-bom` | 2024.12.01 | Versões de todo o Compose | 🟢 |
| `androidx.core:core-ktx` | 1.15.0 | `ContextCompat`, `NotificationCompat`, `ServiceCompat`, `WindowCompat` | 🟢 |
| `androidx.activity:activity-compose` | 1.9.3 | `ComponentActivity`, `setContent`, pedidos de permissão | 🟢 |
| `androidx.lifecycle:lifecycle-runtime-compose` | 2.8.7 | Coleta de `StateFlow` nas telas | 🟢 |
| `androidx.compose.ui:ui` | (BOM) | UI | 🟢 |
| `androidx.compose.material3:material3` | (BOM) | Componentes Material 3 | 🟢 |
| `androidx.compose.material:material-icons-core` | (BOM) | Ícones da barra inferior | 🟢 |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.9.0 | `StateFlow`, `withContext(Dispatchers.IO)` | 🟢 |
| `org.osmdroid:osmdroid-android` | 6.1.20 | Mapa, cache de tiles em `filesDir` | 🟢 |
| `androidx.car.app:app` | 1.4.0 | Android Auto (`CarAppService`, templates) | 🟢 |

## Testes (`testImplementation`)

| Biblioteca | Versão | Confiança |
|------------|--------|-----------|
| `junit:junit` | 4.13.2 | 🟢 |

## APIs da plataforma usadas diretamente (sem biblioteca)

| API | Onde | Confiança |
|-----|------|-----------|
| `java.net.Socket` (APRS-IS e KISS TCP) | `service/Connections.kt` | 🟢 |
| `HttpURLConnection` (consulta de versão) | `data/UpdateCheck.kt` | 🟢 |
| Bluetooth clássico RFCOMM/SPP | `service/Connections.kt` (`BluetoothTnc`) | 🟢 |
| Bluetooth LE GATT | `service/BleTnc.kt` | 🟢 |
| USB Host (FTDI, CP210x, CDC-ACM, driver próprio) | `service/UsbTnc.kt` | 🟢 |
| `AudioRecord`/`AudioTrack` + SCO | `service/AudioTnc.kt` | 🟢 |
| `LocationManager` (GPS e rede, sem Play Services) | `service/AprsService.kt` | 🟢 |
| `SQLiteOpenHelper` (sem Room) | `data/*Store.kt` | 🟢 |
| `org.json` | `data/Settings.kt`, `SettingsBackup.kt`, `AprsRepository.kt` | 🟢 |

## Dados de terceiros embarcados

| Dado | Origem | Licença | Onde |
|------|--------|---------|------|
| Tabela de tocalls e Mic-E | projeto `aprs-deviceid` (OH7LZB) | CC BY-SA 2.0 | `core/DeviceIdTable.kt` gerado por `scripts/gen-deviceid.py` 🟢 |
| Sprites de símbolos APRS | baixados no CI | ver `THIRD_PARTY_APRS_SYMBOLS.md` | `scripts/fetch-aprs-symbols.sh` 🟢 |
| Blocos de prefixos ITU | Apêndice 42 do RR da ITU | domínio público (regulamento) | `core/CallsignPrefixes.kt` 🟢 |

## Observações

- Não há Google Play Services, Firebase, analytics nem SDKs de anúncio. 🟢
- Sem R8/minificação na release (`isMinifyEnabled = false`). 🟢
- Sem gerenciador de dependências de versões (catálogo TOML); versões fixas no próprio build. 🟢
- As versões são de dezembro de 2024; não há dependência sabidamente vulnerável identificada nesta leitura. 🟡
