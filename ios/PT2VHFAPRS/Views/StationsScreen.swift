import MapKit
import SwiftUI

struct StationsScreen: View {
    @EnvironmentObject var model: AppModel
    @State private var search = ""
    @State private var byDistance = false

    private var list: [Station] {
        let q = search.trimmingCharacters(in: .whitespaces).uppercased()
        let base = model.stations.values.filter { q.isEmpty || $0.call.uppercased().contains(q) || $0.comment.uppercased().contains(q) }
        if byDistance, let me = model.location.location {
            return base.sorted {
                Geo.distanceKm(me.coordinate.latitude, me.coordinate.longitude, $0.lat, $0.lon) <
                    Geo.distanceKm(me.coordinate.latitude, me.coordinate.longitude, $1.lat, $1.lon)
            }
        }
        return base.sorted { $0.lastHeard > $1.lastHeard }
    }

    var body: some View {
        NavigationStack {
            List(list) { s in
                NavigationLink {
                    StationDetail(call: s.call)
                } label: {
                    StationRow(station: s, me: model.location.location)
                }
            }
            .listStyle(.plain)
            .overlay {
                if model.stations.isEmpty {
                    ContentUnavailableView("Nenhuma estação ainda", systemImage: "antenna.radiowaves.left.and.right",
                                           description: Text("Conecte ao APRS-IS ou a um TNC BLE na aba Rádio."))
                }
            }
            .searchable(text: $search, prompt: "Indicativo ou texto")
            .navigationTitle("Estações (\(model.stations.count))")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Menu {
                        Picker("Ordenar", selection: $byDistance) {
                            Text("Mais recentes").tag(false)
                            Text("Mais próximas").tag(true)
                        }
                        Button("Limpar lista", role: .destructive) { model.clearStations() }
                    } label: { Image(systemName: "arrow.up.arrow.down") }
                }
            }
        }
    }
}

struct StationRow: View {
    let station: Station
    let me: CLLocation?

    var body: some View {
        HStack(spacing: 12) {
            SymbolBadge(table: station.symbolTable, code: station.symbolCode, size: 34)
            VStack(alignment: .leading, spacing: 2) {
                HStack {
                    Text(station.call).font(.headline.monospaced())
                    if station.via == .rf {
                        Text("RF").font(.caption2.bold()).padding(.horizontal, 4)
                            .background(Color.green.opacity(0.25), in: Capsule())
                    }
                }
                if !station.comment.isEmpty {
                    Text(station.comment).font(.caption).foregroundStyle(.secondary).lineLimit(1)
                }
            }
            Spacer()
            VStack(alignment: .trailing, spacing: 2) {
                if let me {
                    Text(String(format: "%.1f km", Geo.distanceKm(me.coordinate.latitude, me.coordinate.longitude,
                                                                  station.lat, station.lon)))
                        .font(.caption.monospacedDigit())
                }
                Text(station.lastHeard, style: .relative).font(.caption2).foregroundStyle(.secondary)
            }
        }
    }
}

struct StationDetail: View {
    @EnvironmentObject var model: AppModel
    let call: String
    @State private var chatWith: String?

    var body: some View {
        Group {
            if let s = model.stations[call] {
                List {
                    Section {
                        HStack(spacing: 14) {
                            SymbolBadge(table: s.symbolTable, code: s.symbolCode, size: 48)
                            VStack(alignment: .leading) {
                                Text(s.call).font(.title2.bold().monospaced())
                                if let o = s.owner { Text("Objeto de \(o)").font(.caption).foregroundStyle(.secondary) }
                                Text(s.lastHeard, style: .relative).font(.caption).foregroundStyle(.secondary)
                            }
                        }
                        if !s.comment.isEmpty {
                            Text(LocalizedStringKey(linkified(s.comment))).textSelection(.enabled)
                        }
                    }
                    Section("Posição") {
                        LabeledContent("Coordenadas", value: String(format: "%.5f, %.5f", s.lat, s.lon))
                        if let me = model.location.location {
                            let d = Geo.distanceKm(me.coordinate.latitude, me.coordinate.longitude, s.lat, s.lon)
                            LabeledContent("Distância", value: String(format: "%.1f km", d))
                        }
                        if let sp = s.speedKmh {
                            LabeledContent("Velocidade", value: String(format: "%.0f km/h", sp))
                        }
                        if let c = s.courseDeg {
                            LabeledContent("Rumo", value: "\(Geo.compass(c)) \(c)°")
                        }
                        LabeledContent("Recebida por", value: s.via == .rf ? "RF (TNC)" : (s.via == .own ? String(localized: "Própria") : "Internet (APRS-IS)"))
                        if !s.path.isEmpty { LabeledContent("Caminho", value: s.path.joined(separator: ",")) }
                    }
                    Section("Pacote") {
                        Text(s.raw).font(.caption.monospaced()).textSelection(.enabled)
                    }
                    Section {
                        if !s.isObject {
                            Button { chatWith = s.call } label: { Label("Enviar mensagem", systemImage: "message") }
                        }
                        Button {
                            let item = MKMapItem(placemark: MKPlacemark(coordinate: s.coordinate))
                            item.name = s.call
                            item.openInMaps()
                        } label: { Label("Abrir no Mapas", systemImage: "map") }
                        if let url = URL(string: "https://aprs.fi/#!call=a%2F\(s.call)") {
                            Link(destination: url) { Label("Ver no aprs.fi", systemImage: "safari") }
                        }
                    }
                }
            } else {
                ContentUnavailableView("Estação não está mais na lista", systemImage: "questionmark.circle")
            }
        }
        .navigationTitle(call)
        .navigationBarTitleDisplayMode(.inline)
        .navigationDestination(item: $chatWith) { peer in ConversationView(peer: peer) }
    }

    /// E-mails e sites no comentário viram links (como o #88 do Android).
    private func linkified(_ text: String) -> String {
        var out = text.replacingOccurrences(of: "[", with: "(").replacingOccurrences(of: "]", with: ")")
        let detector = try? NSDataDetector(types: NSTextCheckingResult.CheckingType.link.rawValue)
        let ns = out as NSString
        let matches = detector?.matches(in: out, range: NSRange(location: 0, length: ns.length)) ?? []
        for m in matches.reversed() {
            guard let url = m.url else { continue }
            let label = ns.substring(with: m.range)
            out = (out as NSString).replacingCharacters(in: m.range, with: "[\(label)](\(url.absoluteString))")
        }
        return out
    }
}
