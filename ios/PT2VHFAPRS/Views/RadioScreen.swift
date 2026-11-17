import SwiftUI

struct RadioScreen: View {
    @EnvironmentObject var model: AppModel
    @State private var beaconResult: String?

    var body: some View {
        NavigationStack {
            List {
                Section("APRS-IS (internet)") {
                    HStack {
                        StatusPill(state: model.isState, rf: false)
                        Spacer()
                        if model.isConnected || model.isState == .connecting {
                            Button("Desconectar") { model.disconnect() }
                        } else {
                            Button("Conectar") { model.connect() }.disabled(!model.hasValidCall)
                        }
                    }
                    if case .disconnected(let reason) = model.isState, !reason.isEmpty {
                        Text(reason).font(.caption).foregroundStyle(.secondary)
                    }
                    if !model.hasValidCall {
                        Text("Informe um indicativo válido em Ajustes para conectar e transmitir.")
                            .font(.caption).foregroundStyle(.orange)
                    }
                }

                Section {
                    BleSection(ble: model.ble)
                } header: {
                    Text("TNC KISS por Bluetooth LE")
                } footer: {
                    Text("O iPhone só conversa com TNCs Bluetooth LE (Mobilinkd TNC3/TNC4, PicoAPRS, B.B. Link, rádios com KISS BLE). TNCs Bluetooth clássico (SPP) não funcionam no iOS.")
                }

                Section("Beacon") {
                    Toggle("Rastreador (beacon automático)", isOn: $model.trackerOn)
                    Button {
                        beaconResult = model.beaconNow()
                            ? String(localized: "Beacon enviado")
                            : String(localized: "Sem GPS ou sem conexão para transmitir")
                    } label: { Label("Beacon agora", systemImage: "dot.radiowaves.up.forward") }
                    if let r = beaconResult { Text(r).font(.caption).foregroundStyle(.secondary) }
                    if let last = model.lastBeacon {
                        LabeledContent("Último beacon") { Text(last, style: .relative) }
                    }
                    if let l = model.location.location {
                        LabeledContent("GPS", value: String(format: "%.5f, %.5f ±%.0f m", l.coordinate.latitude,
                                                            l.coordinate.longitude, l.horizontalAccuracy))
                    } else {
                        Text("Aguardando GPS…").foregroundStyle(.secondary)
                    }
                }

                Section("Pacotes (últimos)") {
                    ForEach(Array(model.log.suffix(60).reversed())) { e in
                        VStack(alignment: .leading, spacing: 2) {
                            HStack {
                                Text(e.tx ? "TX" : "RX").bold().foregroundStyle(e.tx ? .orange : .green)
                                Text(e.via == .rf ? "RF" : (e.via == .own ? "—" : "IS"))
                                Spacer()
                                Text(e.date, style: .time)
                            }
                            .font(.caption2)
                            Text(e.raw).font(.caption.monospaced()).lineLimit(3).textSelection(.enabled)
                        }
                    }
                }
            }
            .navigationTitle("Rádio")
        }
    }
}

struct BleSection: View {
    @ObservedObject var ble: BleTnc

    var body: some View {
        switch ble.status {
        case .connected(let name):
            Label(name, systemImage: "checkmark.circle.fill").foregroundStyle(.green)
            Button("Desconectar", role: .destructive) { ble.disconnect() }
        case .connecting(let name):
            Label("Conectando a \(name)…", systemImage: "hourglass")
        case .scanning:
            HStack { ProgressView(); Text("Procurando TNCs…") }
            foundList
        case .off:
            Text("Bluetooth desligado ou sem permissão.").foregroundStyle(.orange)
            Button("Procurar TNC") { ble.scan() }
        case .failed(let reason):
            Text(reason).foregroundStyle(.orange)
            Button("Procurar TNC") { ble.scan() }
            foundList
        case .idle:
            Button("Procurar TNC") { ble.scan() }
            foundList
        }
    }

    @ViewBuilder private var foundList: some View {
        ForEach(ble.found) { f in
            Button {
                ble.connect(f.id)
            } label: {
                HStack {
                    Image(systemName: "antenna.radiowaves.left.and.right")
                    Text(f.name)
                    Spacer()
                    Text("\(f.rssi) dBm").font(.caption).foregroundStyle(.secondary)
                }
            }
        }
    }
}
