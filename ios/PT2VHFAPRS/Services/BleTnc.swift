import CoreBluetooth
import Foundation

/// TNC KISS por Bluetooth LE. No iPhone não existe Bluetooth clássico (SPP); só TNCs BLE funcionam.
/// Serviços aceitos (os mesmos do Android #92): KISS BLE (Mobilinkd, PicoAPRS, B.B. Link, VR-N76 etc.)
/// e Nordic UART.
final class BleTnc: NSObject, ObservableObject {
    struct Profile {
        let service: CBUUID
        let write: CBUUID
        let notify: CBUUID
    }

    static let profiles = [
        Profile(service: CBUUID(string: "00000001-ba2a-46c9-ae49-01b0961f68bb"),
                write: CBUUID(string: "00000002-ba2a-46c9-ae49-01b0961f68bb"),
                notify: CBUUID(string: "00000003-ba2a-46c9-ae49-01b0961f68bb")),
        Profile(service: CBUUID(string: "6e400001-b5a3-f393-e0a9-e50e24dcca9e"),
                write: CBUUID(string: "6e400002-b5a3-f393-e0a9-e50e24dcca9e"),
                notify: CBUUID(string: "6e400003-b5a3-f393-e0a9-e50e24dcca9e")),
    ]

    struct Found: Identifiable, Equatable {
        let id: UUID
        let name: String
        let rssi: Int
    }

    enum Status: Equatable { case off, idle, scanning, connecting(String), connected(String), failed(String) }

    @Published private(set) var found: [Found] = []
    @Published private(set) var status: Status = .idle

    /// Pacote recebido do rádio, já em TNC2.
    var onPacket: ((String) -> Void)?

    /// Criado só quando o usuário usa o TNC, para não pedir permissão de Bluetooth ao abrir o app.
    private lazy var central: CBCentralManager = CBCentralManager(delegate: self, queue: .main)
    private var pendingScan = false
    private var peripherals: [UUID: CBPeripheral] = [:]
    private var current: CBPeripheral?
    private var writeChar: CBCharacteristic?
    private let decoder = Kiss.Decoder()
    private var pendingConnect: UUID?
    private let savedKey = "ble_tnc_id"

    var isConnected: Bool { if case .connected = status { return true } else { return false } }

    func scan() {
        guard central.state == .poweredOn else {
            if central.state == .unknown || central.state == .resetting { pendingScan = true } else { status = .off }
            return
        }
        found = []
        status = .scanning
        central.scanForPeripherals(withServices: Self.profiles.map(\.service), options: nil)
        DispatchQueue.main.asyncAfter(deadline: .now() + 15) { [weak self] in
            guard let self, self.status == .scanning else { return }
            self.central.stopScan()
            self.status = .idle
        }
    }

    func connect(_ id: UUID) {
        central.stopScan()
        UserDefaults.standard.set(id.uuidString, forKey: savedKey)
        guard let p = peripherals[id] ?? central.retrievePeripherals(withIdentifiers: [id]).first else {
            pendingConnect = id
            return
        }
        peripherals[id] = p
        current = p
        p.delegate = self
        status = .connecting(p.name ?? "TNC")
        central.connect(p, options: nil)
    }

    /// Reconecta ao último TNC usado, se houver.
    func reconnectSaved() {
        guard let s = UserDefaults.standard.string(forKey: savedKey), let id = UUID(uuidString: s) else { return }
        if central.state == .poweredOn { connect(id) } else { pendingConnect = id }
    }

    func disconnect() {
        UserDefaults.standard.removeObject(forKey: savedKey)
        if let p = current { central.cancelPeripheralConnection(p) }
        current = nil
        writeChar = nil
        status = .idle
    }

    /// Envia um pacote TNC2 pelo rádio, em pedaços do tamanho aceito pelo TNC.
    @discardableResult
    func send(tnc2: String) -> Bool {
        guard let p = current, let c = writeChar, let frame = try? Ax25.fromTnc2(tnc2) else { return false }
        let data = Kiss.encode(frame)
        let type: CBCharacteristicWriteType = c.properties.contains(.writeWithoutResponse) ? .withoutResponse : .withResponse
        let chunk = max(20, p.maximumWriteValueLength(for: type))
        var i = 0
        while i < data.count {
            let end = min(i + chunk, data.count)
            p.writeValue(Data(data[i..<end]), for: c, type: type)
            i = end
        }
        return true
    }
}

extension BleTnc: CBCentralManagerDelegate {
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        if central.state == .poweredOn {
            if case .off = status { status = .idle }
            if let id = pendingConnect { pendingConnect = nil; connect(id) }
            if pendingScan { pendingScan = false; scan() }
        } else {
            status = .off
        }
    }

    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral,
                        advertisementData: [String: Any], rssi RSSI: NSNumber) {
        peripherals[peripheral.identifier] = peripheral
        let name = peripheral.name ?? (advertisementData[CBAdvertisementDataLocalNameKey] as? String) ?? "TNC"
        let item = Found(id: peripheral.identifier, name: name, rssi: RSSI.intValue)
        if let i = found.firstIndex(where: { $0.id == item.id }) { found[i] = item } else { found.append(item) }
    }

    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        peripheral.discoverServices(Self.profiles.map(\.service))
    }

    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
        status = .failed(error?.localizedDescription ?? String(localized: "falha ao conectar"))
    }

    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        writeChar = nil
        guard current?.identifier == peripheral.identifier else { return }
        status = .failed(error?.localizedDescription ?? String(localized: "desconectado"))
        // Reconecta sozinho quando o TNC voltar ao alcance.
        central.connect(peripheral, options: nil)
    }
}

extension BleTnc: CBPeripheralDelegate {
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        for s in peripheral.services ?? [] {
            if let prof = Self.profiles.first(where: { $0.service == s.uuid }) {
                peripheral.discoverCharacteristics([prof.write, prof.notify], for: s)
            }
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        guard let prof = Self.profiles.first(where: { $0.service == service.uuid }) else { return }
        for c in service.characteristics ?? [] {
            if c.uuid == prof.write { writeChar = c }
            if c.uuid == prof.notify { peripheral.setNotifyValue(true, for: c) }
        }
        if writeChar != nil { status = .connected(peripheral.name ?? "TNC") }
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        guard let value = characteristic.value else { return }
        for frame in decoder.feed([UInt8](value)) {
            if let line = try? Ax25.toTnc2(frame) { onPacket?(line) }
        }
    }
}
