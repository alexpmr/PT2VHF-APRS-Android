# Matriz Código ↔ Spec

| Arquivo | Módulo | Requisitos | Teste |
|---------|--------|------------|-------|
| `core/Aprs.kt` | protocolo-aprs | RF-PA-01..03, 05, 07, 08, 11 | `AprsTest` |
| `core/AprsDevice.kt`, `DeviceIdTable.kt` | protocolo-aprs | RF-PA-04, 10 | `AprsDeviceTest` |
| `core/AprsExtras.kt` | protocolo-aprs | RF-PA-06 | `AprsExtrasTest` |
| `core/AprsWeather.kt`, `AprsTelemetry.kt` | protocolo-aprs | RF-PA-06 | `AprsWeatherTest`, `AprsTelemetryTest` |
| `core/AprsPath.kt` | protocolo-aprs / interface | RF-UI-04 | `AprsPathTest` |
| `core/AprsText.kt`, `AsciiFold.kt` | protocolo-aprs | RNF exibição, RN-06, RN-13 | `AprsTextTest`, `AsciiFoldTest` |
| `core/CallsignPrefixes.kt` | protocolo-aprs | RF-PA-09 | `CallsignPrefixesTest` |
| `core/Kiss.kt` | protocolo-aprs | RF-ST-04 | (em `AprsTest`) 🟡 |
| `core/PacketDecoder.kt` | protocolo-aprs | RF-UI-08 | `PacketDecoderTest` |
| `core/Afsk.kt` | modem-e-radio | RF-MR-01..04, 07 | `AfskTest` |
| `core/RadioControl.kt` | modem-e-radio | RF-MR-05, 06 | `RadioControlTest` |
| `core/Igate.kt` | gateway-igate-digi | RF-GD-01..04 | `IgateTest` |
| `core/SmartDigipeater.kt` | gateway-igate-digi | RF-GD-05..08 | `SmartDigipeaterTest` |
| `data/AprsRepository.kt` | estado-e-persistencia | RF-EP-01..05, 07, 08 | indireto |
| `data/MessageLogic.kt` | estado-e-persistencia | RF-EP-05, 06 | `MessageLogicTest` |
| `data/*Store.kt` | estado-e-persistencia | RF-EP-08 | — |
| `data/Settings.kt` | estado-e-persistencia | RF-EP-09 | — |
| `data/SettingsBackup.kt`, `AprsDroidImport.kt` | estado-e-persistencia | RF-EP-10 | `Features*Test` 🟡 |
| `data/CrashLog.kt` | estado-e-persistencia | RF-EP-11 | — |
| `data/UpdateCheck.kt`, `UpdateManager.kt` | atualizacao-e-site | RF-AS-01..05 | `CallsignReportTest` |
| `service/AprsService.kt` | servico-e-transportes / gateway | RF-ST-01, 05..11 | — |
| `service/Connections.kt` | servico-e-transportes | RF-ST-02..04, 13 | `AprsIsConnectionTest` |
| `service/BleTnc.kt`, `UsbTnc.kt`, `AudioTnc.kt` | servico-e-transportes / modem | RF-ST-04, RF-MR-04..06 | — |
| `service/StatusWidget.kt`, `CarApp.kt` | servico-e-transportes | RF-ST-12 | — |
| `service/ApiContract.kt`, `ApiReceiver.kt` | api-externa | RF-AE-01..04 | — |
| `ui/*` | interface | RF-UI-01..08 | — |
