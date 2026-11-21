import Foundation
import Network

/// Cliente APRS-IS (TCP 14580) com reconexão automática e espera crescente, como no Android.
final class AprsIsClient {
    enum State: Equatable {
        case disconnected(String)
        case connecting
        case connected
        case verified
    }

    var onLine: ((String) -> Void)?
    var onState: ((State) -> Void)?

    private let queue = DispatchQueue(label: "aprs-is")
    private var connection: NWConnection?
    private var buffer = Data()
    private var running = false
    private var backoff: TimeInterval = 5
    private var epoch = 0
    private var host = ""
    private var port: UInt16 = 14580
    private var login = ""

    func start(host: String, port: UInt16, login: String) {
        queue.async {
            self.stopLocked()
            self.host = host
            self.port = port
            self.login = login
            self.running = true
            self.backoff = 5
            self.open()
        }
    }

    func stop() {
        queue.async {
            self.stopLocked()
            self.emit(.disconnected(""))
        }
    }

    /// Envia uma linha (TNC2 ou comando "#filter ..."). Retorna falso se não houver conexão.
    func send(_ line: String) {
        queue.async {
            guard let c = self.connection else { return }
            let data = (line + "\r\n").data(using: .isoLatin1, allowLossyConversion: true) ?? Data()
            c.send(content: data, completion: .contentProcessed { _ in })
        }
    }

    private func stopLocked() {
        running = false
        epoch += 1
        connection?.cancel()
        connection = nil
        buffer.removeAll()
    }

    private func emit(_ s: State) {
        DispatchQueue.main.async { self.onState?(s) }
    }

    private func open() {
        guard running, let nwPort = NWEndpoint.Port(rawValue: port) else { return }
        epoch += 1
        let session = epoch
        let tcp = NWProtocolTCP.Options()
        tcp.enableKeepalive = true
        tcp.keepaliveIdle = 60
        tcp.connectionTimeout = 15
        let c = NWConnection(host: NWEndpoint.Host(host), port: nwPort, using: NWParameters(tls: nil, tcp: tcp))
        connection = c
        emit(.connecting)
        c.stateUpdateHandler = { [weak self] state in
            guard let self, session == self.epoch else { return }
            switch state {
            case .ready:
                self.backoff = 5
                self.emit(.connected)
                self.send(self.login)
                self.receive(c, session: session)
            case .failed(let error):
                self.closeAndRetry(session, reason: error.localizedDescription)
            case .waiting(let error):
                self.closeAndRetry(session, reason: error.localizedDescription)
            default:
                break
            }
        }
        c.start(queue: queue)
    }

    private func receive(_ c: NWConnection, session: Int) {
        c.receive(minimumIncompleteLength: 1, maximumLength: 16384) { [weak self] data, _, complete, error in
            guard let self, session == self.epoch else { return }
            if let data, !data.isEmpty {
                self.buffer.append(data)
                self.drainLines()
            }
            if complete || error != nil {
                self.closeAndRetry(session, reason: error?.localizedDescription ?? String(localized: "servidor fechou a conexão"))
                return
            }
            self.receive(c, session: session)
        }
    }

    private func drainLines() {
        while let nl = buffer.firstIndex(of: 0x0A) {
            var lineData = buffer[buffer.startIndex..<nl]
            if lineData.last == 0x0D { lineData = lineData.dropLast() }
            buffer.removeSubrange(buffer.startIndex...nl)
            let line = String(data: Data(lineData), encoding: .utf8)
                ?? String(data: Data(lineData), encoding: .isoLatin1) ?? ""
            if line.hasPrefix("#") {
                if line.contains("logresp") {
                    if line.contains(" verified") { emit(.verified) }
                }
                continue
            }
            if !line.isEmpty {
                DispatchQueue.main.async { self.onLine?(line) }
            }
        }
        if buffer.count > 65536 { buffer.removeAll() }
    }

    private func closeAndRetry(_ session: Int, reason: String) {
        guard session == epoch else { return }
        connection?.cancel()
        connection = nil
        buffer.removeAll()
        emit(.disconnected(reason))
        guard running else { return }
        let wait = backoff
        backoff = min(backoff * 2, 120)
        queue.asyncAfter(deadline: .now() + wait) { [weak self] in
            guard let self, self.running, session == self.epoch else { return }
            self.open()
        }
    }
}
