import Foundation

/// Modo demonstração (argumento de inicialização `-demo`): estações e mensagens de exemplo, sem conectar a nada.
/// Usado só para gerar as capturas de tela da App Store no simulador (GitHub Actions, job "Capturas de tela").
/// Indicativos fictícios, posições em volta de São Paulo.
enum DemoData {
    static var isOn: Bool { ProcessInfo.processInfo.arguments.contains("-demo") }

    /// Aba inicial pedida por `-tab N` (0 = Mapa … 4 = Ajustes).
    static var startTab: Int {
        let args = ProcessInfo.processInfo.arguments
        guard let i = args.firstIndex(of: "-tab"), i + 1 < args.count else { return 0 }
        return Int(args[i + 1]) ?? 0
    }

    /// Conversa aberta direto (`-chat`), para a captura da tela de mensagens.
    static var openChat: Bool { ProcessInfo.processInfo.arguments.contains("-chat") }

    static let chatPeer = "PY2DEM-7"

    struct Sample {
        let call: String
        let dLat: Double
        let dLon: Double
        let symbol: String
        let comment: String
        var course: Int? = nil
        var speed: Double? = nil
        var path = "WIDE1-1,WIDE2-1,qAR,PY2IGT"
    }

    static let center = (lat: -23.5505, lon: -46.6333)

    static let samples: [Sample] = [
        Sample(call: "PY2DEM-9", dLat: 0.018, dLon: -0.030, symbol: "/>", comment: "Móvel 145.570", course: 75, speed: 42),
        Sample(call: "PY2DEM-7", dLat: -0.012, dLon: 0.020, symbol: "/[", comment: "HT na Paulista"),
        Sample(call: "PU2EXP-10", dLat: 0.060, dLon: 0.045, symbol: "I&", comment: "iGate RX/TX 145.570MHz"),
        Sample(call: "PY2DIG", dLat: 0.110, dLon: -0.090, symbol: "/#", comment: "Digipeater Pico do Jaraguá"),
        Sample(call: "PU2TST-13", dLat: -0.050, dLon: -0.070, symbol: "/_", comment: "Estação meteorológica"),
        Sample(call: "PY2RPT", dLat: -0.085, dLon: 0.110, symbol: "/r", comment: "Repetidora 146.850 -0.600 T88.5"),
        Sample(call: "PU2CAR-9", dLat: 0.035, dLon: 0.085, symbol: "/k", comment: "Rumo ao litoral", course: 160, speed: 68),
        Sample(call: "PY2BIK-5", dLat: -0.028, dLon: -0.012, symbol: "/b", comment: "Pedal no Ibirapuera", course: 300, speed: 18),
        Sample(call: "PU2HOM", dLat: 0.075, dLon: -0.020, symbol: "/-", comment: "QTH fixo"),
        Sample(call: "PY2TRK-14", dLat: -0.120, dLon: -0.020, symbol: "/u", comment: "Transporte", course: 220, speed: 80),
        Sample(call: "PU2YAG", dLat: 0.140, dLon: 0.060, symbol: "/y", comment: "Yagi 2m"),
        Sample(call: "PY2SAT-6", dLat: -0.150, dLon: 0.150, symbol: "/S", comment: "Evento especial"),
    ]

    @MainActor
    static func load(into model: AppModel) {
        if model.callsign.isEmpty { model.callsign = "PT2VHF"; model.ssid = 9 }
        for s in samples {
            let table = s.symbol.first ?? "/", code = s.symbol.last ?? ">"
            let info = Aprs.positionInfo(lat: center.lat + s.dLat, lon: center.lon + s.dLon, table: table, code: code,
                                         courseDeg: s.course, speedKmh: s.speed, comment: s.comment)
            model.receive("\(s.call)>APZVHF,\(s.path):\(info)", via: s.call.hasSuffix("-7") ? .rf : .internet)
        }
        let own = Aprs.positionInfo(lat: center.lat, lon: center.lon, table: "/", code: ">", comment: model.comment)
        model.receive("\(model.fullCall)>APZVHF:\(own)", via: .own)
    }

    static func messages(now: Date = Date()) -> [ChatMessage] {
        var list: [ChatMessage] = []
        func add(_ peer: String, _ incoming: Bool, _ text: String, _ minutesAgo: Double, _ state: MessageState,
                 bulletin: Bool = false, from: String? = nil) {
            var m = ChatMessage(peer: peer, incoming: incoming, text: text, date: now.addingTimeInterval(-minutesAgo * 60),
                                msgId: String(list.count + 1), state: state, isBulletin: bulletin)
            m.from = from
            list.append(m)
        }
        add(chatPeer, true, "Bom dia! Ouvindo você pelo digi do Jaraguá.", 14, .received)
        add(chatPeer, false, "Bom dia! Aqui chegando 59, obrigado.", 13, .acked)
        add(chatPeer, true, "Vai no encontro de sábado?", 6, .received)
        add(chatPeer, false, "Vou sim, saio às 8h. Te vejo lá!", 5, .acked)
        add(chatPeer, false, "Leva o TNC novo para testarmos", 1, .pending)
        add("PU2CAR-9", true, "QSL, estou na Imigrantes", 32, .received)
        add("PU2CAR-9", false, "Boa viagem, 73!", 31, .acked)
        add("BLN1", true, "Rede APRS: encontro sábado 9h no clube", 60, .received, bulletin: true, from: "PY2RPT")
        add("CQ", true, "CQ CQ alguém pela região do ABC?", 90, .received, bulletin: true, from: "PU2HOM")
        return list
    }
}
