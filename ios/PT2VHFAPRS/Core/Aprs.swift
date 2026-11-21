import Foundation

/// Núcleo APRS do app iOS: mesmo protocolo e mesmo TOCALL do app Android.
enum Aprs {
    /// TOCALL do PT2VHF APRS (igual ao Android).
    static let tocall = "APZVHF"

    /// Passcode APRS-IS calculado do indicativo (sem SSID). Nunca é pedido ao usuário.
    static func passcode(_ callsign: String) -> Int {
        let base = callsign.uppercased().split(separator: "-").first.map(String.init) ?? ""
        let bytes = Array(base.utf8)
        var hash = 0x73e2
        var i = 0
        while i < bytes.count {
            hash ^= Int(bytes[i]) << 8
            if i + 1 < bytes.count { hash ^= Int(bytes[i + 1]) }
            i += 2
        }
        return hash & 0x7fff
    }

    /// Indicativo de radioamador plausível (prefixo + número + sufixo), com SSID opcional 0–15.
    static func isValidCallsign(_ call: String) -> Bool {
        let parts = call.uppercased().split(separator: "-", omittingEmptySubsequences: false)
        guard parts.count <= 2, let base = parts.first, (3...6).contains(base.count) else { return false }
        guard base.allSatisfy({ $0.isASCII && ($0.isLetter || $0.isNumber) }),
              base.contains(where: { $0.isNumber }), base.contains(where: { $0.isLetter }) else { return false }
        if parts.count == 2 {
            guard let ssid = Int(parts[1]), (0...15).contains(ssid) else { return false }
        }
        return true
    }

    /// Remove acentos antes de enviar (APRS é ASCII), como a opção #77 do Android.
    static func asciiFold(_ text: String) -> String {
        let folded = text.folding(options: [.diacriticInsensitive, .widthInsensitive], locale: Locale(identifier: "en_US_POSIX"))
        var out = String.UnicodeScalarView()
        out.append(contentsOf: folded.unicodeScalars.filter { $0.isASCII && $0.value >= 0x20 })
        return String(out)
    }

    // MARK: - Codificação

    static func formatLat(_ lat: Double) -> String {
        let a = min(abs(lat), 89.99999)
        let deg = Int(a)
        let minutes = (a - Double(deg)) * 60
        return String(format: "%02d%05.2f%@", deg, minutes, lat >= 0 ? "N" : "S")
    }

    static func formatLon(_ lon: Double) -> String {
        let a = min(abs(lon), 179.99999)
        let deg = Int(a)
        let minutes = (a - Double(deg)) * 60
        return String(format: "%03d%05.2f%@", deg, minutes, lon >= 0 ? "E" : "W")
    }

    /// Posição sem timestamp, com suporte a mensagens ("="), símbolo, curso/velocidade e comentário.
    static func positionInfo(lat: Double, lon: Double, table: Character, code: Character,
                             courseDeg: Int? = nil, speedKmh: Double? = nil, comment: String) -> String {
        var info = "=" + formatLat(lat) + String(table) + formatLon(lon) + String(code)
        if let c = courseDeg, let s = speedKmh, s >= 1 {
            let course = c <= 0 ? 360 : min(c, 360)
            let knots = min(Int((s / 1.852).rounded()), 999)
            info += String(format: "%03d/%03d", course, knots)
        }
        return info + String(asciiFold(comment).prefix(43))
    }

    /// Mensagem APRS: ":DEST     :texto{id". Texto limitado a 67 caracteres, sem "|", "~" e "{".
    static func messageInfo(to addressee: String, text: String, id: String?) -> String {
        let dest = addressee.uppercased().padding(toLength: 9, withPad: " ", startingAt: 0)
        let clean = asciiFold(text).replacingOccurrences(of: "{", with: "(")
            .replacingOccurrences(of: "|", with: "/").replacingOccurrences(of: "~", with: "-")
        let body = String(clean.prefix(67))
        return ":" + dest + ":" + body + (id.map { "{" + $0 } ?? "")
    }

    static func ackInfo(to addressee: String, id: String) -> String {
        ":" + addressee.uppercased().padding(toLength: 9, withPad: " ", startingAt: 0) + ":ack" + id
    }
}

/// Pacote no formato TNC2: ORIGEM>DESTINO,CAMINHO:info
struct AprsPacket: Equatable {
    let source: String
    let destination: String
    let path: [String]
    let info: String

    static func parse(_ raw: String) -> AprsPacket? {
        guard let gt = raw.firstIndex(of: ">") else { return nil }
        guard let colon = raw[gt...].firstIndex(of: ":") else { return nil }
        let source = String(raw[..<gt]).trimmingCharacters(in: .whitespaces)
        let header = raw[raw.index(after: gt)..<colon].split(separator: ",").map(String.init)
        guard !source.isEmpty, source.count <= 9, let dest = header.first, !dest.isEmpty else { return nil }
        return AprsPacket(source: source, destination: dest, path: Array(header.dropFirst()),
                          info: String(raw[raw.index(after: colon)...]))
    }

    var tnc2: String {
        source + ">" + ([destination] + path).joined(separator: ",") + ":" + info
    }

    /// Pacote de terceiros (iGate → RF): "}ORIG>DEST,...:info".
    var thirdParty: AprsPacket? {
        guard info.hasPrefix("}") else { return nil }
        return AprsPacket.parse(String(info.dropFirst()))
    }
}

struct AprsPosition: Equatable {
    var lat: Double
    var lon: Double
    var symbolTable: Character
    var symbolCode: Character
    var comment: String
    var courseDeg: Int?
    var speedKmh: Double?
    /// Nome do objeto/item (";" ou ")"), quando o pacote é de um objeto.
    var objectName: String?
    var objectKilled: Bool = false
}

struct AprsMessage: Equatable {
    var addressee: String
    var text: String
    var id: String?
    var isAck: Bool { ackParts?.rej == false }
    var isRej: Bool { ackParts?.rej == true }
    var ackedId: String? { ackParts?.id }

    /// #101: "ack12", "ack12}", "ack12}AB" e "ack12 " (espaço/CR no fim) são todos ACK do número 12.
    private var ackParts: (rej: Bool, id: String)? {
        let t = text.trimmingCharacters(in: .whitespacesAndNewlines)
        guard id == nil, t.count >= 4, t.hasPrefix("ack") || t.hasPrefix("rej") else { return nil }
        let rest = t.dropFirst(3)
        let num = rest.split(separator: "}", maxSplits: 1, omittingEmptySubsequences: false).first.map(String.init) ?? ""
        let tail = rest.dropFirst(num.count).dropFirst()
        guard (1...5).contains(num.count), num.allSatisfy({ $0.isASCII && ($0.isLetter || $0.isNumber) }),
              tail.count <= 5, tail.allSatisfy({ $0.isASCII && ($0.isLetter || $0.isNumber) }) else { return nil }
        return (t.hasPrefix("rej"), num)
    }
}

extension Aprs {
    /// Indicativo sem SSID ("PT2VHF-9" → "PT2VHF").
    static func baseCall(_ call: String) -> String {
        String(call.trimmingCharacters(in: .whitespaces).uppercased().split(separator: "-").first ?? "")
    }

    /// #101: o ACK vindo de `ackFrom` confirma mensagem enviada para `dest`? Igual, ou mesmo indicativo base quando um
    /// dos lados está sem SSID (mensagem para PT2ATC respondida por PT2ATC-7, ou o contrário).
    static func ackMatches(dest: String, ackFrom: String) -> Bool {
        let d = dest.trimmingCharacters(in: .whitespaces).uppercased()
        let f = ackFrom.trimmingCharacters(in: .whitespaces).uppercased()
        if d == f { return true }
        let db = baseCall(d), fb = baseCall(f)
        return !db.isEmpty && db == fb && (!d.contains("-") || !f.contains("-"))
    }

    /// #101: o ACK sai do endereço que o remetente usou: mensagem para "PT2VHF" recebida por PT2VHF-9 é confirmada
    /// como PT2VHF; caso contrário, com o indicativo completo.
    static func ackSource(addressee: String, ownCall: String) -> String {
        let a = addressee.trimmingCharacters(in: .whitespaces).uppercased()
        let own = ownCall.trimmingCharacters(in: .whitespaces).uppercased()
        return a != own && !a.isEmpty && a == baseCall(own) ? a : own
    }
}

enum AprsParser {
    static func message(_ info: String) -> AprsMessage? {
        let chars = Array(info)
        guard chars.count >= 11, chars[0] == ":", chars[10] == ":" else { return nil }
        let addressee = String(chars[1..<10]).trimmingCharacters(in: .whitespaces)
        var text = String(chars[11...])
        var id: String? = nil
        if let brace = text.lastIndex(of: "{") {
            let raw = String(text[text.index(after: brace)...])
            // Reply-ack (APRS 1.1): "{MM}AA" — o id é a parte antes do "}".
            id = raw.split(separator: "}", omittingEmptySubsequences: false).first.map(String.init)
            text = String(text[..<brace])
            if id?.isEmpty == true { id = nil }
        }
        guard !addressee.isEmpty else { return nil }
        return AprsMessage(addressee: addressee, text: text.trimmingCharacters(in: .whitespacesAndNewlines), id: id)
    }

    static func position(_ packet: AprsPacket) -> AprsPosition? {
        let info = packet.info
        guard let type = info.first else { return nil }
        switch type {
        case "!", "=":
            return body(String(info.dropFirst()))
        case "/", "@":
            guard info.count > 8 else { return nil }
            return body(String(info.dropFirst(8)))
        case ";":
            let c = Array(info)
            guard c.count > 18 else { return nil }
            let name = String(c[1..<10]).trimmingCharacters(in: .whitespaces)
            guard var p = body(String(c[18...])) else { return nil }
            p.objectName = name
            p.objectKilled = c[10] == "_"
            return p
        case ")":
            let rest = info.dropFirst()
            guard let end = rest.firstIndex(where: { $0 == "!" || $0 == "_" }) else { return nil }
            let name = String(rest[..<end])
            guard (3...9).contains(name.count), var p = body(String(rest[rest.index(after: end)...])) else { return nil }
            p.objectName = name
            p.objectKilled = rest[end] == "_"
            return p
        case "`", "'", "\u{1c}", "\u{1d}":
            return micE(destination: packet.destination, info: info)
        default:
            // Posição sem identificador pode vir até no 40º caractere ("!" no meio do texto).
            if let bang = info.firstIndex(of: "!"), info.distance(from: info.startIndex, to: bang) < 40 {
                return body(String(info[info.index(after: bang)...]))
            }
            return nil
        }
    }

    /// Corpo de posição: não comprimida ("4903.50N/07201.75W>") ou comprimida ("/5L!!<*e7>").
    static func body(_ s: String) -> AprsPosition? {
        let c = Array(s)
        guard let first = c.first else { return nil }
        if first.isNumber || first == " " {
            guard c.count >= 19 else { return nil }
            let latStr = String(c[0..<8]).replacingOccurrences(of: " ", with: "0")
            let lonStr = String(c[9..<18]).replacingOccurrences(of: " ", with: "0")
            guard let lat = parseLat(latStr), let lon = parseLon(lonStr) else { return nil }
            var p = AprsPosition(lat: lat, lon: lon, symbolTable: c[8], symbolCode: c[18],
                                 comment: String(c[19...]))
            if p.symbolCode != "_" { extractCourseSpeed(&p) } // em WX, "ddd/sss" é o vento
            return p
        }
        guard c.count >= 13 else { return nil }
        let table = c[0]
        guard let y = base91(c[1..<5]), let x = base91(c[5..<9]) else { return nil }
        let lat = 90 - Double(y) / 380926
        let lon = -180 + Double(x) / 190463
        guard (-90...90).contains(lat), (-180...180).contains(lon) else { return nil }
        var p = AprsPosition(lat: lat, lon: lon, symbolTable: table == "a" || table.isNumber ? "\\" : table,
                             symbolCode: c[9], comment: String(c[13...]))
        let cs = c[10], sp = c[11], t = c[12]
        if cs != " ", let tv = t.asciiValue, ((Int(tv) - 33) & 0x18) != 0x10,
           let cv = cs.asciiValue, let sv = sp.asciiValue, cv >= 33, cv - 33 <= 89 {
            p.courseDeg = Int(cv - 33) * 4
            p.speedKmh = (pow(1.08, Double(Int(sv) - 33)) - 1) * 1.852
        }
        return p
    }

    private static func base91<C: Collection>(_ chars: C) -> Int? where C.Element == Character {
        var v = 0
        for ch in chars {
            guard let a = ch.asciiValue, a >= 33, a <= 124 else { return nil }
            v = v * 91 + Int(a - 33)
        }
        return v
    }

    private static func parseLat(_ s: String) -> Double? {
        let c = Array(s)
        guard c.count == 8, let d = Double(String(c[0..<2])), let m = Double(String(c[2..<7])), m < 60 else { return nil }
        let v = d + m / 60
        guard v <= 90 else { return nil }
        switch c[7] {
        case "N", "n": return v
        case "S", "s": return -v
        default: return nil
        }
    }

    private static func parseLon(_ s: String) -> Double? {
        let c = Array(s)
        guard c.count == 9, let d = Double(String(c[0..<3])), let m = Double(String(c[3..<8])), m < 60 else { return nil }
        let v = d + m / 60
        guard v <= 180 else { return nil }
        switch c[8] {
        case "E", "e": return v
        case "W", "w": return -v
        default: return nil
        }
    }

    /// "ccc/sss" no início do comentário: curso em graus e velocidade em nós.
    private static func extractCourseSpeed(_ p: inout AprsPosition) {
        let c = Array(p.comment)
        guard c.count >= 7, c[3] == "/",
              let course = Int(String(c[0..<3])), let knots = Int(String(c[4..<7])) else { return }
        if course > 0 && course <= 360 { p.courseDeg = course % 360 }
        p.speedKmh = Double(knots) * 1.852
        p.comment = String(c[7...])
    }

    /// Mic-E: latitude no campo de destino, longitude/velocidade/curso no info.
    static func micE(destination: String, info: String) -> AprsPosition? {
        let d = Array(destination.split(separator: "-").first ?? "")
        let i = Array(info.unicodeScalars)
        guard d.count == 6, i.count >= 9 else { return nil }
        var digits = [Int]()
        for ch in d {
            guard let a = ch.asciiValue else { return nil }
            switch a {
            case 48...57: digits.append(Int(a - 48))
            case 65...74: digits.append(Int(a - 65))
            case 80...89: digits.append(Int(a - 80))
            case 75, 76, 90: digits.append(0)
            default: return nil
            }
        }
        func custom(_ ch: Character) -> Bool { (ch.asciiValue ?? 0) >= 80 }
        var lat = Double(digits[0] * 10 + digits[1]) + (Double(digits[2] * 10 + digits[3]) + Double(digits[4] * 10 + digits[5]) / 100) / 60
        if !custom(d[3]) { lat = -lat }
        func v(_ k: Int) -> Int { Int(i[k].value) - 28 }
        var lonDeg = v(1)
        if custom(d[4]) { lonDeg += 100 }
        if (180...189).contains(lonDeg) { lonDeg -= 80 } else if (190...199).contains(lonDeg) { lonDeg -= 190 }
        var lonMin = v(2)
        if lonMin >= 60 { lonMin -= 60 }
        var lon = Double(lonDeg) + (Double(lonMin) + Double(v(3)) / 100) / 60
        if custom(d[5]) { lon = -lon }
        guard (-90...90).contains(lat), (-180...180).contains(lon) else { return nil }
        var speed = v(4) * 10 + v(5) / 10
        if speed >= 800 { speed -= 800 }
        var course = (v(5) % 10) * 100 + v(6)
        if course >= 400 { course -= 400 }
        var tail = String.UnicodeScalarView()
        tail.append(contentsOf: i[9...])
        var comment = String(tail)
        // Remove o tipo do rádio no início (">" ou "]") e o sufixo do fabricante ("=" do Kenwood etc.).
        if let f = comment.first, f == ">" || f == "]" || f == "`" || f == "'" { comment.removeFirst() }
        var p = AprsPosition(lat: lat, lon: lon, symbolTable: Character(i[8]), symbolCode: Character(i[7]),
                             comment: comment)
        if speed > 0 { p.speedKmh = Double(speed) * 1.852 }
        if course > 0 && course <= 360 { p.courseDeg = course % 360 }
        return p
    }
}

enum Geo {
    static func distanceKm(_ lat1: Double, _ lon1: Double, _ lat2: Double, _ lon2: Double) -> Double {
        let r = 6371.0
        let dLat = (lat2 - lat1) * .pi / 180
        let dLon = (lon2 - lon1) * .pi / 180
        let a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1 * .pi / 180) * cos(lat2 * .pi / 180) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }

    static func compass(_ deg: Int) -> String {
        let names = ["N", "NE", "L", "SE", "S", "SO", "O", "NO"]
        return names[Int((Double(deg % 360) / 45).rounded()) % 8]
    }
}
