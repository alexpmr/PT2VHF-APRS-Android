import SwiftUI

@main
struct PT2VHFAPRSApp: App {
    @StateObject private var model = AppModel()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(model)
                .tint(Color("AccentColor"))
        }
    }
}

struct ContentView: View {
    @EnvironmentObject var model: AppModel
    @State private var tab = DemoData.startTab
    @State private var showSetup = false

    var body: some View {
        TabView(selection: $tab) {
            MapScreen()
                .tabItem { Label("Mapa", systemImage: "map") }.tag(0)
            StationsScreen()
                .tabItem { Label("Estações", systemImage: "list.bullet") }.tag(1)
            MessagesScreen()
                .tabItem { Label("Mensagens", systemImage: "message") }.tag(2)
                .badge(model.unread.count)
            RadioScreen()
                .tabItem { Label("Rádio", systemImage: "antenna.radiowaves.left.and.right") }.tag(3)
            SettingsScreen()
                .tabItem { Label("Ajustes", systemImage: "gearshape") }.tag(4)
        }
        .onAppear {
            model.onLaunch()
            if !model.hasValidCall && !DemoData.isOn { showSetup = true }
        }
        .sheet(isPresented: $showSetup) {
            NavigationStack {
                SettingsForm()
                    .navigationTitle("Bem-vindo")
                    .toolbar {
                        ToolbarItem(placement: .confirmationAction) {
                            Button("Pronto") {
                                showSetup = false
                                model.reconnectIfIdentityChanged()
                            }
                        }
                    }
            }
        }
    }
}
