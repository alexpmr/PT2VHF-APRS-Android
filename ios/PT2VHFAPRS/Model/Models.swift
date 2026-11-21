import CoreLocation
import Foundation

enum Via: String, Codable { case internet, rf, own }

struct Station: Identifiable, Equatable {
    var id: String { call }
    let call: String
    var lat: Double
    var lon: Double
    var symbolTable: Character
    var symbolCode: Character
    var comment: String
    var courseDeg: Int?
    var speedKmh: Double?
    var lastHeard: Date
    var via: Via
    var raw: String
    var path: [String]
    var isObject: Bool
    var owner: String?
    var packets: Int

    var coordinate: CLLocationCoordinate2D { CLLocationCoordinate2D(latitude: lat, longitude: lon) }
}

enum MessageState: String, Codable { case received, pending, acked, rejected, failed, cancelled }

struct ChatMessage: Identifiable, Codable, Equatable {
    var id = UUID()
    var peer: String
    var incoming: Bool
    var text: String
    var date: Date
    var msgId: String?
    var state: MessageState
    var tries: Int = 0
    var nextTry: Date?
    /// Boletins e mensagens gerais (BLN*, CQ, ALL etc.) ficam numa conversa à parte.
    var isBulletin: Bool = false
    /// Remetente de boletim/CQ (a conversa é pelo destino, ex.: BLN1 ou CQ).
    var from: String?
}

struct LogEntry: Identifiable {
    let id = UUID()
    let date: Date
    let tx: Bool
    let via: Via
    let raw: String
}

enum MapKind: String, CaseIterable, Identifiable {
    case standard, dark, satellite, hybrid, auto
    var id: String { rawValue }
    var label: String {
        switch self {
        case .standard: return String(localized: "Padrão")
        case .dark: return String(localized: "Escuro")
        case .satellite: return String(localized: "Satélite")
        case .hybrid: return String(localized: "Híbrido")
        case .auto: return String(localized: "Automático")
        }
    }
}

struct Conversation: Identifiable {
    var id: String { peer }
    let peer: String
    let last: ChatMessage
}
