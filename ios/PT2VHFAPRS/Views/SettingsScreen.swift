import SwiftUI

struct SettingsScreen: View {
    @EnvironmentObject var model: AppModel
    var body: some View {
        NavigationStack {
            SettingsForm().navigationTitle("Ajustes")
        }
        .onDisappear { model.reconnectIfIdentityChanged() }
    }
}

struct SettingsForm: View {
    @EnvironmentObject var model: AppModel
    @State private var showSsidHelp = false

    /// Recomendações de SSID (WB4APR), as mesmas do "Qual SSID usar?" do Android (#82).
    static let ssidHints: [Int: LocalizedStringKey] = [
        0: "-0 Estação fixa principal",
        1: "-1 Digipeater, WX ou estação fixa extra",
        2: "-2 Digipeater, WX ou estação fixa extra",
        3: "-3 Digipeater ou fixa extra",
        4: "-4 Digipeater ou fixa extra",
        5: "-5 Celular, smartphone, outras redes",
        6: "-6 Satélite, eventos especiais",
        7: "-7 Portátil (HT) ou pedestre",
        8: "-8 Barco, veículo de recreação",
        9: "-9 Móvel principal (carro)",
        10: "-10 Internet, iGate, Echolink",
        11: "-11 Balão, aeronave, espaço",
        12: "-12 Rastreador APRStt, DTMF, uso único",
        13: "-13 Estação meteorológica",
        14: "-14 Caminhão, transporte",
        15: "-15 Estação fixa extra",
    ]

    static let symbols: [(String, LocalizedStringKey)] = [
        ("/>", "Carro"), ("/k", "Caminhonete"), ("/u", "Caminhão"), ("/[", "Pedestre"), ("/b", "Bicicleta"),
        ("/<", "Moto"), ("/-", "Casa"), ("/Y", "Barco"), ("/'", "Avião pequeno"), ("/$", "Telefone/celular"),
        ("/_", "Estação meteorológica"), ("/;", "Acampamento"), ("/y", "Casa com Yagi"),
    ]

    var body: some View {
        Form {
            Section {
                TextField("Indicativo (ex.: PY2ABC)", text: $model.callsign)
                    .textInputAutocapitalization(.characters)
                    .autocorrectionDisabled()
                    .font(.body.monospaced())
                HStack {
                    Picker("SSID", selection: $model.ssid) {
                        ForEach(0...15, id: \.self) { n in
                            Text(Self.ssidHints[n] ?? "-\(n)").tag(n)
                        }
                    }
                    Button { showSsidHelp = true } label: { Image(systemName: "info.circle") }
                        .buttonStyle(.borderless)
                }
                Picker("Símbolo", selection: $model.symbol) {
                    ForEach(Self.symbols.indices, id: \.self) { i in
                        let code = Self.symbols[i].0
                        Label { Text(Self.symbols[i].1) } icon: {
                            SymbolBadge(table: code.first ?? "/", code: code.last ?? ">", size: 22)
                        }.tag(code)
                    }
                }
                TextField("Comentário", text: $model.comment)
                if !model.callsign.isEmpty && !model.hasValidCall {
                    Text("Indicativo inválido.").font(.caption).foregroundStyle(.red)
                }
            } header: {
                Text("Estação")
            } footer: {
                if model.hasValidCall {
                    Text("Você transmite como \(model.fullCall). O passcode APRS-IS é calculado automaticamente.")
                } else {
                    Text("Sem indicativo o app só ouve: mostra estações e mensagens, mas não transmite.")
                }
            }

            Section("APRS-IS") {
                TextField("Servidor", text: $model.server)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()
                Stepper("Porta \(String(model.port))", value: $model.port, in: 1...65535)
                Stepper("Raio de recepção: \(model.rangeKm) km", value: $model.rangeKm, in: 10...1000, step: 10)
                Toggle("Conectar ao abrir o app", isOn: $model.autoConnect)
            }

            Section {
                Stepper("Beacon a cada \(model.beaconMinutes) min", value: $model.beaconMinutes, in: 1...60)
                Toggle("Rastreador ligado", isOn: $model.trackerOn)
                TextField("Caminho RF (TNC)", text: $model.rfPath)
                    .textInputAutocapitalization(.characters)
                    .autocorrectionDisabled()
            } header: {
                Text("Beacon")
            } footer: {
                Text("Com o rastreador ligado e a permissão de localização \"Sempre\", o beacon continua com o app em segundo plano. Também envia beacon ao fazer curvas acentuadas.")
            }

            Section("Mensagens") {
                Toggle("Remover acentos ao enviar", isOn: $model.stripAccents)
            }

            Section {
                Toggle("Mensagens pessoais", isOn: $model.notifyPersonal)
                Toggle("Boletins (BLN)", isOn: $model.notifyBulletins)
                Toggle("Mensagens gerais (CQ, ALL, QST)", isOn: $model.notifyGroup)
                Toggle("Mensagem entregue (ACK)", isOn: $model.notifyAck)
                Button("Testar notificação") { model.testNotification() }
                Button("Som e estilo nos Ajustes do iPhone") {
                    if let url = URL(string: UIApplication.openNotificationSettingsURLString) {
                        UIApplication.shared.open(url)
                    }
                }
            } header: {
                Text("Notificações")
            } footer: {
                Text("Boletim repetido em 24 horas não avisa de novo. O som e o estilo dos avisos são escolhidos nos Ajustes do iPhone.")
            }

            Section {
                if model.blocked.isEmpty {
                    Text("Nenhum indicativo bloqueado.").foregroundStyle(.secondary)
                }
                ForEach(model.blocked, id: \.self) { call in
                    Text(call).font(.body.monospaced())
                        .swipeActions {
                            Button("Desbloquear") { model.unblock(call) }.tint(.green)
                        }
                }
            } header: {
                Text("Bloqueados")
            } footer: {
                Text("Para bloquear, deslize a conversa ou mantenha pressionada a mensagem. Pacotes, estações e mensagens do indicativo bloqueado deixam de aparecer.")
            }

            Section("Mapa") {
                Picker("Tipo de mapa", selection: $model.mapKind) {
                    ForEach(MapKind.allCases) { Text($0.label).tag($0) }
                }
            }

            Section("Sobre") {
                LabeledContent("Versão", value: AppModel.version)
                Link("www.PT2VHF.com", destination: URL(string: "https://www.pt2vhf.com/aprs-android")!)
                LabeledContent("Colaboradores", value: "PU2PYC, PP5AU, PU2MUS, PP5PK, PY6TL")
                Link("Política de privacidade", destination: URL(string: "https://www.pt2vhf.com/privacidade")!)
                Text("Gratuito, sem anúncios. Mesmo protocolo e TOCALL (APZVHF) do app Android.")
                    .font(.caption).foregroundStyle(.secondary)
            }
        }
        .sheet(isPresented: $showSsidHelp) {
            NavigationStack {
                List(0...15, id: \.self) { n in
                    Button {
                        model.ssid = n
                        showSsidHelp = false
                    } label: {
                        HStack {
                            Text(Self.ssidHints[n] ?? "-\(n)")
                            Spacer()
                            if model.ssid == n { Image(systemName: "checkmark") }
                        }
                    }
                }
                .navigationTitle("Qual SSID usar?")
                .navigationBarTitleDisplayMode(.inline)
            }
            .presentationDetents([.medium, .large])
        }
    }
}
