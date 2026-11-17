import Foundation

/// KISS (FEND/FESC), igual ao TNC do Android e do desktop.
enum Kiss {
    static let fend: UInt8 = 0xC0
    static let fesc: UInt8 = 0xDB
    static let tfend: UInt8 = 0xDC
    static let tfesc: UInt8 = 0xDD

    static func encode(_ payload: [UInt8], port: UInt8 = 0) -> [UInt8] {
        var out: [UInt8] = [fend, (port & 0x0F) << 4]
        for b in payload {
            switch b {
            case fend: out += [fesc, tfend]
            case fesc: out += [fesc, tfesc]
            default: out.append(b)
            }
        }
        out.append(fend)
        return out
    }

    /// Decodificador de fluxo: recebe pedaços (notificações BLE) e devolve os frames de dados completos.
    final class Decoder {
        private var buffer: [UInt8] = []
        private var inFrame = false
        private var escaped = false

        func feed(_ chunk: [UInt8]) -> [[UInt8]] {
            var frames: [[UInt8]] = []
            for b in chunk {
                if b == Kiss.fend {
                    if inFrame, buffer.count > 1, (buffer[0] & 0x0F) == 0 {
                        frames.append(Array(buffer.dropFirst()))
                    }
                    buffer.removeAll(keepingCapacity: true)
                    inFrame = true
                    escaped = false
                    continue
                }
                guard inFrame else { continue }
                if escaped {
                    buffer.append(b == Kiss.tfend ? Kiss.fend : (b == Kiss.tfesc ? Kiss.fesc : b))
                    escaped = false
                } else if b == Kiss.fesc {
                    escaped = true
                } else {
                    buffer.append(b)
                }
            }
            return frames
        }
    }
}

/// Quadro AX.25 UI <-> texto TNC2.
enum Ax25 {
    enum Failure: Error { case invalid }

    private static func address(_ value: String, last: Bool) -> [UInt8] {
        let repeated = value.hasSuffix("*")
        let clean = (repeated ? String(value.dropLast()) : value).uppercased()
        let parts = clean.split(separator: "-", maxSplits: 1).map(String.init)
        let call = Array(String((parts.first ?? "").prefix(6)).padding(toLength: 6, withPad: " ", startingAt: 0).utf8)
        let ssid = UInt8(min(max(Int(parts.count > 1 ? parts[1] : "0") ?? 0, 0), 15))
        var out = call.map { $0 << 1 }
        var s: UInt8 = 0x60 | (ssid << 1)
        if repeated { s |= 0x80 }
        if last { s |= 0x01 }
        out.append(s)
        return out
    }

    /// "ORIG>DEST,PATH:info" → frame AX.25 (sem os elementos q/TCPIP do APRS-IS).
    static func fromTnc2(_ raw: String) throws -> [UInt8] {
        guard let p = AprsPacket.parse(raw) else { throw Failure.invalid }
        let path = p.path.filter { !$0.hasPrefix("q") && !$0.hasPrefix("TCPIP") }.prefix(8)
        let addresses = [p.destination, p.source] + path
        var out: [UInt8] = []
        for (i, a) in addresses.enumerated() { out += address(a, last: i == addresses.count - 1) }
        out += [0x03, 0xF0]
        out += Array(p.info.data(using: .isoLatin1, allowLossyConversion: true) ?? Data())
        return out
    }

    static func toTnc2(_ frame: [UInt8]) throws -> String {
        guard frame.count >= 16 else { throw Failure.invalid }
        var addresses: [String] = []
        var offset = 0
        while offset + 7 <= frame.count {
            let call = String(bytes: frame[offset..<offset + 6].map { $0 >> 1 }, encoding: .ascii)?
                .trimmingCharacters(in: .whitespaces) ?? ""
            let s = frame[offset + 6]
            let ssid = (s >> 1) & 0x0F
            let name = ssid > 0 ? "\(call)-\(ssid)" : call
            addresses.append(addresses.count >= 2 && (s & 0x80) != 0 ? name + "*" : name)
            offset += 7
            if (s & 0x01) != 0 { break }
            guard addresses.count < 10 else { throw Failure.invalid }
        }
        guard addresses.count >= 2, offset + 2 <= frame.count else { throw Failure.invalid }
        let info = String(bytes: frame[(offset + 2)...], encoding: .isoLatin1) ?? ""
        let path = addresses.dropFirst(2)
        return addresses[1] + ">" + addresses[0] + (path.isEmpty ? "" : "," + path.joined(separator: ",")) + ":" + info
    }
}
