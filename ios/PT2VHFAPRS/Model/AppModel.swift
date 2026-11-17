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
        loadMessages()

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
        location.start(background: trackerOn)
        if autoConnect && hasValidCall { connect() }
        ble.reconnectSaved()
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
    }

    func connect() {
        guard hasValidCall else { return }
        let pass = Aprs.passcode(baseCall)
        let login = "user \(fullCall) pass \(pass) vers PT2VHF-APRS-iOS \(Self.version) filter \(currentFilter())"
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
        // Duplicados (mesmo pacote por RF e internet em poucos segundos).
        let key = packet.source + ":" + packet.info
        let now = Date()
        if let seen = recentDupes[key], now.timeIntervalSince(seen) < 30 { return }
        recentDupes[key] = now
        if recentDupes.count > 2000 { recentDupes = recentDupes.filter { now.timeIntervalSince($0.value) < 30 } }

        if let msg = AprsParser.message(packet.info) {
            handleMessage(msg, from: packet.source)
            return
        }
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

    private func handleMessage(_ m: AprsMessage, from source: String) {
        let me = fullCall
        let to = m.addressee.uppercased()
        if let acked = m.ackedId, to == me {
            if let i = messages.firstIndex(where: { !$0.incoming && $0.msgId == acked && $0.peer == source.uppercased() }) {
                messages[i].state = m.isAck ? .acked : .rejected
                messages[i].nextTry = nil
                saveMessages()
            }
            return
        }
        let bulletin = to.hasPrefix("BLN") || to == "CQ" || to == "ALL" || to.hasPrefix("NWS")
        guard to == me || bulletin else { return }
        if to == me, let id = m.id { transmit(Aprs.ackInfo(to: source, id: id)) }
        let peer = bulletin ? to : source.uppercased()
        // Mesma mensagem repetida pelo remetente (reenvio): não duplica.
        if messages.contains(where: { $0.incoming && $0.peer == peer && $0.msgId != nil && $0.msgId == m.id && $0.text == m.text }) { return }
        let msg = ChatMessage(peer: peer, incoming: true, text: m.text, date: Date(), msgId: m.id,
                              state: .received, isBulletin: bulletin)
        messages.append(msg)
        unread.insert(peer)
        saveMessages()
        if to == me { notify(from: source, text: m.text) }
    }

    private func notify(from: String, text: String) {
        let content = UNMutableNotificationContent()
        content.title = from
        content.body = text
        content.sound = .default
        UNUserNotificationCenter.current().add(UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: nil))
    }

    // MARK: Transmissão

    /// Envia pelo APRS-IS (se verificado) e pelo TNC BLE (se conectado). Retorna se saiu por algum caminho.
    @discardableResult
    func transmit(_ info: String) -> Bool {
        guard hasValidCall else { return false }
        var sent = false
        if case .verified = isState {
            let line = "\(fullCall)>\(Aprs.tocall),TCPIP*:\(info)"
            aprsIs.send(line)
            appendLog(LogEntry(date: Date(), tx: true, via: .internet, raw: line))
            sent = true
        }
        if ble.isConnected {
            let path = rfPath.trimmingCharacters(in: .whitespaces)
            let line = "\(fullCall)>\(Aprs.tocall)" + (path.isEmpty ? "" : ",\(path)") + ":\(info)"
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

    func retry(_ message: ChatMessage) {
        guard let i = messages.firstIndex(where: { $0.id == message.id }) else { return }
        messages[i].state = .pending
        messages[i].tries = 1
        messages[i].nextTry = Date().addingTimeInterval(30)
        transmit(Aprs.messageInfo(to: messages[i].peer, text: messages[i].text, id: messages[i].msgId))
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
        if messages.count > 2000 { messages.removeFirst(messages.count - 2000) }
        if let data = try? JSONEncoder().encode(messages) { try? data.write(to: messagesURL, options: .atomic) }
    }

    func clearStations() { stations.removeAll() }

    private func appendLog(_ e: LogEntry) {
        log.append(e)
        if log.count > 500 { log.removeFirst(log.count - 500) }
    }
}
