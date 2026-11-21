import Combine
import CoreLocation
import Foundation
import UserNotifications

/// Estado do app: configurações, estações, mensagens, APRS-IS, TNC BLE e beacon.
@MainActor
final class AppModel: ObservableObject {
    // MARK: Configurações (salvas na hora)
    @Published var callsign: String { didSet { save("callsign", callsign) } }
    @Published var ssid: Int { didSet { save("ssid", ssid) } }
    @Published var symbol: String { didSet { save("symbol", symbol) } }
    @Published var comment: String { didSet { save("comment", comment) } }
    @Published var server: String { didSet { save("server", server) } }
    @Published var port: Int { didSet { save("port", port) } }
    @Published var rangeKm: Int { didSet { save("range_km", rangeKm) } }
    @Published var beaconMinutes: Int { didSet { save("beacon_minutes", beaconMinutes) } }
    @Published var trackerOn: Bool { didSet { save("tracker_on", trackerOn); applyTracker() } }
    @Published var autoConnect: Bool { didSet { save("auto_connect", autoConnect) } }
    @Published var stripAccents: Bool { didSet { save("strip_accents", stripAccents) } }
    @Published var rfPath: String { didSet { save("rf_path", rfPath) } }
    @Published var mapKind: MapKind { didSet { save("map_kind", mapKind.rawValue) } }
    // #106: avisos por tipo. Boletins e CQ começam desligados, como no Android.
    @Published var notifyPersonal: Bool { didSet { save("notify_personal", notifyPersonal) } }
    @Published var notifyBulletins: Bool { didSet { save("notify_bulletins", notifyBulletins) } }
    @Published var notifyGroup: Bool { didSet { save("notify_group", notifyGroup) } }
    @Published var notifyAck: Bool { didSet { save("notify_ack", notifyAck) } }
    /// Indicativos bloqueados: pacotes, estações e mensagens deles são ignorados.
    @Published var blocked: [String] { didSet { save("blocked", blocked) } }

    // MARK: Estado
    @Published private(set) var stations: [String: Station] = [:]
    @Published private(set) var messages: [ChatMessage] = []
    @Published private(set) var log: [LogEntry] = []
    @Published private(set) var isState: AprsIsClient.State = .disconnected("")
    @Published private(set) var lastBeacon: Date?
    @Published var unread: Set<String> = []

    let aprsIs = AprsIsClient()
    let ble = BleTnc()
    let location = LocationService()

    private var timer: Timer?
    private var bag = Set<AnyCancellable>()
    private var filterCenter: CLLocation?
    private var lastBeaconLocation: CLLocation?
    private var lastBeaconCourse: Double?
    private var recentDupes: [String: Date] = [:]
    /// #101: um ACK por remetente/número a cada 20 s (a mesma cópia chega direto e por cada digi).
    private var lastAckAt: [String: Date] = [:]
    /// #106: boletim repetido em 24 h não avisa de novo.
    private var bulletinSeen: [String: Date] = [:]
    private let defaults = UserDefaults.standard

    static let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0"

    init() {
        let d = UserDefaults.standard
        callsign = d.string(forKey: "callsign") ?? ""
        ssid = d.object(forKey: "ssid") as? Int ?? 9
        symbol = d.string(forKey: "symbol") ?? "/>"
        comment = d.string(forKey: "comment") ?? "PT2VHF APRS iOS www.PT2VHF.com"
        server = d.string(forKey: "server") ?? "rotate.aprs2.net"
        port = d.object(forKey: "port") as? Int ?? 14580
        rangeKm = d.object(forKey: "range_km") as? Int ?? 100
        beaconMinutes = d.object(forKey: "beacon_minutes") as? Int ?? 10
        trackerOn = d.object(forKey: "tracker_on") as? Bool ?? false
        autoConnect = d.object(forKey: "auto_connect") as? Bool ?? true
        stripAccents = d.object(forKey: "strip_accents") as? Bool ?? true
        rfPath = d.string(forKey: "rf_path") ?? "WIDE1-1,WIDE2-1"
        mapKind = MapKind(rawValue: d.string(forKey: "map_kind") ?? "") ?? .standard
        notifyPersonal = d.object(forKey: "notify_personal") as? Bool ?? true
        notifyBulletins = d.object(forKey: "notify_bulletins") as? Bool ?? false
        notifyGroup = d.object(forKey: "notify_group") as? Bool ?? false
        notifyAck = d.object(forKey: "notify_ack") as? Bool ?? false
        blocked = d.stringArray(forKey: "blocked") ?? []
        if DemoData.isOn { messages = DemoData.messages(); unread = ["PU2CAR-9"] } else { loadMessages() }

        aprsIs.onLine = { [weak self] line in self?.receive(line, via: .internet) }
        aprsIs.onState = { [weak self] s in self?.isState = s }
        ble.onPacket = { [weak self] line in self?.receive(line, via: .rf) }
        ble.objectWillChange.sink { [weak self] _ in self?.objectWillChange.send() }.store(in: &bag)
        location.objectWillChange.sink { [weak self] _ in self?.objectWillChange.send() }.store(in: &bag)
        location.$location.compactMap { $0 }.sink { [weak self] loc in self?.moved(loc) }.store(in: &bag)

        timer = Timer.scheduledTimer(withTimeInterval: 10, repeats: true) { [weak self] _ in
            Task { @MainActor in self?.tick() }
        }
    }

    private func save(_ key: String, _ value: Any) { defaults.set(value, forKey: key) }

    // MARK: Identidade

    var baseCall: String { callsign.trimmingCharacters(in: .whitespaces).uppercased() }
    var fullCall: String { ssid > 0 ? "\(baseCall)-\(ssid)" : baseCall }
    var hasValidCall: Bool { Aprs.isValidCallsign(fullCall) }
    var symbolTable: Character { symbol.first ?? "/" }
    var symbolCode: Character { symbol.count > 1 ? symbol[symbol.index(after: symbol.startIndex)] : ">" }

    // MARK: Início

    func onLaunch() {
        if DemoData.isOn { DemoData.load(into: self); isState = .verified; lastBeacon = Date().addingTimeInterval(-90); return }
        location.start(background: trackerOn)
        if autoConnect { connect() }
        ble.reconnectSaved()
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
    }

    /// Sem indicativo válido o app entra só para ouvir (N0CALL, passcode -1): o APRS-IS mostra as estações e
    /// mensagens, mas não aceita nada enviado. Também é o que a revisão da App Store usa, sem licença de radioamador.
    var listenOnly: Bool { !hasValidCall }
    private var connectedCall: String?

    /// Ao sair dos Ajustes: se o indicativo mudou (ou deixou de ser só ouvir), reconecta com o novo login.
    func reconnectIfIdentityChanged() {
        guard autoConnect || isConnected || isState == .connecting else { return }
        let call = listenOnly ? "N0CALL" : fullCall
        if call != connectedCall || !(isConnected || isState == .connecting) { connect() }
    }

    func connect() {
        let call = listenOnly ? "N0CALL" : fullCall
        let pass = listenOnly ? -1 : Aprs.passcode(baseCall)
        connectedCall = call
        let login = "user \(call) pass \(pass) vers PT2VHF-APRS-iOS \(Self.version) filter \(currentFilter())"
        filterCenter = location.location
        aprsIs.start(host: server.trimmingCharacters(in: .whitespaces), port: UInt16(clamping: port), login: login)
    }

    func disconnect() { aprsIs.stop() }

    var isConnected: Bool {
        switch isState {
        case .connected, .verified: return true
        default: return false
        }
    }

    private func currentFilter() -> String {
        let r = max(10, min(rangeKm, 2000))
        if let l = location.location {
            return String(format: "r/%.2f/%.2f/%d m/%d", l.coordinate.latitude, l.coordinate.longitude, r, r)
        }
        return "m/\(r)"
    }

    // MARK: Recepção

    func receive(_ raw: String, via: Via) {
        appendLog(LogEntry(date: Date(), tx: false, via: via, raw: raw))
        guard var packet = AprsPacket.parse(raw) else { return }
        if let inner = packet.thirdParty { packet = inner }
        if blocked.contains(packet.source.uppercased()) { return }
        // #101: mensagens passam antes do filtro de duplicados: toda cópia para nós precisa de ACK.
        if let msg = AprsParser.message(packet.info) {
            handleMessage(msg, from: packet.source, via: via)
            return
        }
        // Duplicados (mesmo pacote por RF e internet em poucos segundos).
        let key = packet.source + ":" + packet.info
        let now = Date()
        if let seen = recentDupes[key], now.timeIntervalSince(seen) < 30 { return }
        recentDupes[key] = now
        if recentDupes.count > 2000 { recentDupes = recentDupes.filter { now.timeIntervalSince($0.value) < 30 } }

        if let pos = AprsParser.position(packet) {
            let call = pos.objectName ?? packet.source
            if pos.objectKilled { stations[call] = nil; return }
            var s = stations[call] ?? Station(call: call, lat: pos.lat, lon: pos.lon, symbolTable: pos.symbolTable,
                                              symbolCode: pos.symbolCode, comment: "", lastHeard: now, via: via,
                                              raw: raw, path: packet.path, isObject: pos.objectName != nil,
                                              owner: pos.objectName != nil ? packet.source : nil, packets: 0)
            s.lat = pos.lat
            s.lon = pos.lon
            s.symbolTable = pos.symbolTable
            s.symbolCode = pos.symbolCode
            if !pos.comment.trimmingCharacters(in: .whitespaces).isEmpty { s.comment = pos.comment }
            s.courseDeg = pos.courseDeg
            s.speedKmh = pos.speedKmh
            s.lastHeard = now
            s.via = packet.source.uppercased() == fullCall ? .own : via
            s.raw = raw
            s.path = packet.path
            s.packets += 1
            stations[call] = s
            pruneStations()
        }
    }

    private func pruneStations() {
        guard stations.count > 3000 else { return }
        let keep = stations.values.sorted { $0.lastHeard > $1.lastHeard }.prefix(2500)
        stations = Dictionary(uniqueKeysWithValues: keep.map { ($0.call, $0) })
    }

    private func handleMessage(_ m: AprsMessage, from source: String, via: Via) {
        let src = source.uppercased()
        let to = m.addressee.uppercased()
        let forMe = !listenOnly && (to == fullCall || (to == baseCall && !baseCall.isEmpty))
        if let acked = m.ackedId {
            guard forMe else { return }
            if let i = messages.lastIndex(where: { !$0.incoming && $0.msgId == acked &&
                                                   Aprs.ackMatches(dest: $0.peer, ackFrom: src) }) {
                let already = messages[i].state == .acked
                messages[i].state = m.isAck ? .acked : .rejected
                messages[i].nextTry = nil
                saveMessages()
                if m.isAck && !already && notifyAck {
                    notify(id: "ack-\(messages[i].id)", title: src,
                           body: String(localized: "Mensagem entregue (ACK): \(messages[i].text)"))
                }
            }
            return
        }
        let bulletin = to.hasPrefix("BLN") || to.hasPrefix("NWS")
        let group = to == "CQ" || to == "ALL" || to == "QST"
        guard forMe || bulletin || group else { return }
        // #101: toda cópia é confirmada (também as repetidas), pelo mesmo caminho e do endereço que o remetente usou.
        if forMe, let id = m.id, shouldAck(from: src, id: id) {
            transmit(Aprs.ackInfo(to: src, id: id), source: Aprs.ackSource(addressee: to, ownCall: fullCall), only: via)
        }
        let peer = forMe ? src : to
        // Mesma mensagem repetida pelo remetente (reenvio) dentro de 1 hora: não duplica.
        let hourAgo = Date().addingTimeInterval(-3600)
        if messages.contains(where: { $0.incoming && $0.peer == peer && $0.date > hourAgo && $0.text == m.text &&
                                      $0.msgId == m.id && (forMe || $0.from == src) }) { return }
        var msg = ChatMessage(peer: peer, incoming: true, text: m.text, date: Date(), msgId: m.id,
                              state: .received, isBulletin: !forMe)
        msg.from = forMe ? nil : src
        messages.append(msg)
        unread.insert(peer)
        saveMessages()
        if forMe {
            if notifyPersonal { notify(id: msg.id.uuidString, title: src, body: m.text) }
        } else if bulletin ? notifyBulletins : notifyGroup {
            let key = "\(src)|\(to)|\(m.text)"
            if let seen = bulletinSeen[key], Date().timeIntervalSince(seen) < 86_400 { return }
            bulletinSeen[key] = Date()
            notify(id: msg.id.uuidString, title: "\(to) · \(src)", body: m.text)
        }
    }

    private func shouldAck(from: String, id: String) -> Bool {
        let now = Date()
        lastAckAt = lastAckAt.filter { now.timeIntervalSince($0.value) < 600 }
        let key = from + "|" + id
        if let last = lastAckAt[key], now.timeIntervalSince(last) < 20 { return false }
        lastAckAt[key] = now
        return true
    }

    private func notify(id: String, title: String, body: String) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default
        UNUserNotificationCenter.current().add(UNNotificationRequest(identifier: id, content: content, trigger: nil))
    }

    /// Botão "Testar" dos avisos.
    func testNotification() {
        notify(id: UUID().uuidString, title: "PT2VHF APRS", body: String(localized: "Teste de notificação."))
    }

    // MARK: Transmissão

    /// Envia pelo APRS-IS (se verificado) e pelo TNC BLE (se conectado). Retorna se saiu por algum caminho.
    /// `only`: o ACK volta pelo mesmo meio por onde a mensagem chegou.
    @discardableResult
    func transmit(_ info: String, source: String? = nil, only: Via? = nil) -> Bool {
        guard hasValidCall else { return false }
        let from = source ?? fullCall
        var sent = false
        if case .verified = isState, only != .rf || !ble.isConnected {
            let line = "\(from)>\(Aprs.tocall),TCPIP*:\(info)"
            aprsIs.send(line)
            appendLog(LogEntry(date: Date(), tx: true, via: .internet, raw: line))
            sent = true
        }
        if ble.isConnected, only != .internet || !sent {
            let path = rfPath.trimmingCharacters(in: .whitespaces)
            let line = "\(from)>\(Aprs.tocall)" + (path.isEmpty ? "" : ",\(path)") + ":\(info)"
            if ble.send(tnc2: line) {
                appendLog(LogEntry(date: Date(), tx: true, via: .rf, raw: line))
                sent = true
            }
        }
        return sent
    }

    @discardableResult
    func beaconNow() -> Bool {
        guard let l = location.location else { return false }
        let speed = l.speed >= 0 ? l.speed * 3.6 : nil
        let course = l.course >= 0 ? Int(l.course.rounded()) : nil
        let info = Aprs.positionInfo(lat: l.coordinate.latitude, lon: l.coordinate.longitude,
                                     table: symbolTable, code: symbolCode, courseDeg: course,
                                     speedKmh: speed, comment: comment)
        guard transmit(info) else { return false }
        lastBeacon = Date()
        lastBeaconLocation = l
        lastBeaconCourse = l.course >= 0 ? l.course : nil
        receive("\(fullCall)>\(Aprs.tocall):\(info)", via: .own)
        return true
    }

    private func applyTracker() {
        location.start(background: trackerOn)
    }

    private func moved(_ loc: CLLocation) {
        // Atualiza o filtro do servidor quando o usuário se desloca 1/4 do raio.
        if isConnected, let c = filterCenter, loc.distance(from: c) > Double(rangeKm) * 250 {
            filterCenter = loc
            aprsIs.send("#filter \(currentFilter())")
        } else if filterCenter == nil, isConnected {
            filterCenter = loc
            aprsIs.send("#filter \(currentFilter())")
        }
        guard trackerOn, let last = lastBeacon else {
            if trackerOn && lastBeacon == nil { beaconNow() }
            return
        }
        // Beacon inteligente simples: curva acentuada em movimento ou intervalo cumprido.
        let elapsed = Date().timeIntervalSince(last)
        if loc.speed > 3, let prev = lastBeaconCourse, loc.course >= 0, elapsed > 15 {
            var diff = abs(loc.course - prev)
            if diff > 180 { diff = 360 - diff }
            if diff > 30 { beaconNow() }
        }
    }

    private func tick() {
        if trackerOn, beaconMinutes > 0 {
            let due = lastBeacon.map { Date().timeIntervalSince($0) >= Double(beaconMinutes) * 60 } ?? true
            if due { beaconNow() }
        }
        retryMessages()
    }

    // MARK: Mensagens

    private var nextMessageId: Int {
        get { defaults.integer(forKey: "msg_id") }
        set { defaults.set(newValue, forKey: "msg_id") }
    }

    func send(to peer: String, text: String) {
        let dest = peer.trimmingCharacters(in: .whitespaces).uppercased()
        let body = stripAccents ? Aprs.asciiFold(text) : text
        guard !dest.isEmpty, !body.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        nextMessageId = (nextMessageId % 99999) + 1
        let id = String(nextMessageId)
        var msg = ChatMessage(peer: dest, incoming: false, text: String(body.prefix(67)), date: Date(), msgId: id,
                              state: .pending)
        msg.tries = 1
        msg.nextTry = Date().addingTimeInterval(30)
        messages.append(msg)
        saveMessages()
        transmit(Aprs.messageInfo(to: dest, text: msg.text, id: id))
    }

    /// #105: para as tentativas e manda de novo como mensagem nova (novo número, novo ciclo de ACK).
    func resendAsNew(_ message: ChatMessage) {
        giveUpAck(message)
        send(to: message.peer, text: message.text)
    }

    /// #105: para as tentativas sem reenviar.
    func giveUpAck(_ message: ChatMessage) {
        guard let i = messages.firstIndex(where: { $0.id == message.id }), messages[i].state == .pending else { return }
        messages[i].state = .cancelled
        messages[i].nextTry = nil
        saveMessages()
    }

    /// Até 5 tentativas: 30 s, 60 s, 120 s, 240 s; depois marca como falha (o usuário pode reenviar).
    private func retryMessages() {
        let now = Date()
        var changed = false
        for i in messages.indices where messages[i].state == .pending {
            guard let next = messages[i].nextTry, next <= now else { continue }
            if messages[i].tries >= 5 {
                messages[i].state = .failed
                messages[i].nextTry = nil
            } else {
                messages[i].tries += 1
                messages[i].nextTry = now.addingTimeInterval(30 * pow(2, Double(messages[i].tries - 1)))
                transmit(Aprs.messageInfo(to: messages[i].peer, text: messages[i].text, id: messages[i].msgId))
            }
            changed = true
        }
        if changed { saveMessages() }
    }

    func deleteConversation(_ peer: String) {
        messages.removeAll { $0.peer == peer }
        unread.remove(peer)
        saveMessages()
    }

    var conversations: [Conversation] {
        Dictionary(grouping: messages, by: \.peer)
            .compactMap { (peer: String, list: [ChatMessage]) -> Conversation? in
                guard let last = list.max(by: { $0.date < $1.date }) else { return nil }
                return Conversation(peer: peer, last: last)
            }
            .sorted { $0.last.date > $1.last.date }
    }

    private var messagesURL: URL {
        FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0].appendingPathComponent("messages.json")
    }

    private func loadMessages() {
        guard let data = try? Data(contentsOf: messagesURL),
              let list = try? JSONDecoder().decode([ChatMessage].self, from: data) else { return }
        messages = list
    }

    private func saveMessages() {
        if DemoData.isOn { return }
        if messages.count > 2000 { messages.removeFirst(messages.count - 2000) }
        if let data = try? JSONEncoder().encode(messages) { try? data.write(to: messagesURL, options: .atomic) }
    }

    func clearStations() { stations.removeAll() }

    func block(_ call: String) {
        let c = call.trimmingCharacters(in: .whitespaces).uppercased()
        guard !c.isEmpty, !blocked.contains(c) else { return }
        blocked.append(c)
        stations[c] = nil
        messages.removeAll { $0.peer == c || $0.from == c }
        unread.remove(c)
        saveMessages()
    }

    func unblock(_ call: String) { blocked.removeAll { $0 == call } }

    private func appendLog(_ e: LogEntry) {
        log.append(e)
        if log.count > 500 { log.removeFirst(log.count - 500) }
    }
}
