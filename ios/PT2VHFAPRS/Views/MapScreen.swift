import MapKit
import SwiftUI

struct MapScreen: View {
    @EnvironmentObject var model: AppModel
    @Environment(\.colorScheme) private var systemScheme
    @State private var camera: MapCameraPosition = DemoData.isOn
        ? .region(MKCoordinateRegion(center: CLLocationCoordinate2D(latitude: DemoData.center.lat, longitude: DemoData.center.lon),
                                     span: MKCoordinateSpan(latitudeDelta: 0.32, longitudeDelta: 0.32)))
        : .userLocation(fallback: .automatic)
    @State private var selected: Station?
    @State private var visibleRegion: MKCoordinateRegion?
    @AppStorage("map_labels") private var showLabels = true

    /// No máximo 400 estações desenhadas (as mais recentes dentro da área visível), para o mapa ficar leve.
    private var visible: [Station] {
        let all = model.stations.values
        let inView: [Station]
        if let r = visibleRegion {
            let latSpan = r.span.latitudeDelta * 0.6, lonSpan = r.span.longitudeDelta * 0.6
            inView = all.filter {
                abs($0.lat - r.center.latitude) <= latSpan && abs($0.lon - r.center.longitude) <= lonSpan
            }
        } else {
            inView = Array(all)
        }
        return Array(inView.sorted { $0.lastHeard > $1.lastHeard }.prefix(400))
    }

    private var isDark: Bool {
        switch model.mapKind {
        case .dark: return true
        case .auto: return systemScheme == .dark
        default: return false
        }
    }

    private var style: MapStyle {
        switch model.mapKind {
        case .satellite: return .imagery(elevation: .realistic)
        case .hybrid: return .hybrid(elevation: .realistic, pointsOfInterest: .excludingAll)
        default: return .standard(elevation: .realistic, pointsOfInterest: .excludingAll)
        }
    }

    var body: some View {
        Map(position: $camera) {
            UserAnnotation()
            ForEach(visible) { s in
                Annotation(showLabels ? s.call : "", coordinate: s.coordinate, anchor: .center) {
                    SymbolBadge(table: s.symbolTable, code: s.symbolCode, size: 28)
                        .opacity(Date().timeIntervalSince(s.lastHeard) > 3600 ? 0.55 : 1)
                        .onTapGesture { selected = s }
                }
            }
        }
        .mapStyle(style)
        .environment(\.colorScheme, isDark ? .dark : (model.mapKind == .standard ? .light : systemScheme))
        .mapControls {
            MapUserLocationButton()
            MapCompass()
            MapScaleView()
        }
        .onMapCameraChange(frequency: .onEnd) { ctx in visibleRegion = ctx.region }
        .safeAreaInset(edge: .top) { header }
        .sheet(item: $selected) { s in
            NavigationStack { StationDetail(call: s.call) }
                .presentationDetents([.medium, .large])
        }
    }

    private var header: some View {
        HStack(spacing: 8) {
            StatusPill(state: model.isState, rf: model.ble.isConnected)
            Spacer()
            Menu {
                Picker("Tipo de mapa", selection: $model.mapKind) {
                    ForEach(MapKind.allCases) { Text($0.label).tag($0) }
                }
                Toggle("Indicativos", isOn: $showLabels)
            } label: {
                Image(systemName: "square.3.layers.3d")
                    .padding(8)
                    .background(.ultraThinMaterial, in: Circle())
            }
            Button {
                model.beaconNow()
            } label: {
                Image(systemName: "dot.radiowaves.up.forward")
                    .padding(8)
                    .background(.ultraThinMaterial, in: Circle())
            }
            .accessibilityLabel("Beacon agora")
        }
        .padding(.horizontal)
        .padding(.top, 4)
    }
}

struct StatusPill: View {
    let state: AprsIsClient.State
    let rf: Bool

    var body: some View {
        HStack(spacing: 6) {
            Circle().fill(color).frame(width: 8, height: 8)
            Text(text).font(.caption.weight(.semibold))
            if rf {
                Text("· RF").font(.caption.weight(.semibold)).foregroundStyle(.green)
            }
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 6)
        .background(.ultraThinMaterial, in: Capsule())
    }

    private var color: Color {
        switch state {
        case .verified: return .green
        case .connected: return .yellow
        case .connecting: return .orange
        case .disconnected: return .red
        }
    }

    private var text: LocalizedStringKey {
        switch state {
        case .verified: return "APRS-IS verificado"
        case .connected: return "APRS-IS conectado"
        case .connecting: return "Conectando…"
        case .disconnected: return "APRS-IS desconectado"
        }
    }
}
