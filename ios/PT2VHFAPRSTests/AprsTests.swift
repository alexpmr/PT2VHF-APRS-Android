import XCTest
@testable import PT2VHFAPRS

/// Mesmos casos dos testes do app Android, para os dois apps falarem APRS igual.
final class AprsTests: XCTestCase {
    func testPasscode() {
        XCTAssertEqual(Aprs.passcode("N0CALL"), 13023)
        XCTAssertEqual(Aprs.passcode("PT2VHF"), Aprs.passcode("pt2vhf-9"))
    }

    func testCallsignValidation() {
        XCTAssertTrue(Aprs.isValidCallsign("PT2VHF-9"))
        XCTAssertTrue(Aprs.isValidCallsign("PY2ABC"))
        XCTAssertFalse(Aprs.isValidCallsign("PT2VHF-16"))
        XCTAssertFalse(Aprs.isValidCallsign("ABCDEF"))
        XCTAssertFalse(Aprs.isValidCallsign(""))
    }

    func testPositionCourseSpeedAndDigis() throws {
        let p = try XCTUnwrap(AprsPacket.parse("PT2CAR-9>APRS,PT2DIG-1*,WIDE2-1,qAR,PT2IGT:!1548.00S/04806.60W>090/025Carro"))
        XCTAssertEqual(p.source, "PT2CAR-9")
        XCTAssertEqual(p.path.first, "PT2DIG-1*")
        let pos = try XCTUnwrap(AprsParser.position(p))
        XCTAssertEqual(pos.lat, -15.8, accuracy: 1e-6)
        XCTAssertEqual(pos.lon, -48.11, accuracy: 1e-6)
        XCTAssertEqual(pos.courseDeg, 90)
        XCTAssertEqual(pos.speedKmh ?? 0, 25 * 1.852, accuracy: 1e-6)
        XCTAssertEqual(pos.comment, "Carro")
        XCTAssertEqual(pos.symbolCode, ">")
    }

    func testRejectsInvalidMinutes() {
        let p = AprsPacket(source: "X", destination: "APRS", path: [], info: "!2677.84S/04812.00W-x")
        XCTAssertNil(AprsParser.position(p))
    }

    func testWeatherWindIsNotCourse() throws {
        let p = AprsPacket(source: "WX", destination: "APRS", path: [], info: "!2330.00S/04630.00W_090/012g020t077")
        let pos = try XCTUnwrap(AprsParser.position(p))
        XCTAssertNil(pos.courseDeg)
    }

    func testCompressed() throws {
        // Exemplo da especificação APRS 1.01: 49°30'N 72°45'W.
        let p = AprsPacket(source: "X", destination: "APRS", path: [], info: "=/5L!!<*e7>7P[")
        let pos = try XCTUnwrap(AprsParser.position(p))
        XCTAssertEqual(pos.lat, 49.5, accuracy: 0.001)
        XCTAssertEqual(pos.lon, -72.75, accuracy: 0.001)
        XCTAssertEqual(pos.symbolCode, ">")
    }

    func testMicE() throws {
        let p = AprsPacket(source: "PY2MIC-9", destination: "S32U6T", path: [], info: "`(_fn\"O>/Oi")
        let pos = try XCTUnwrap(AprsParser.position(p))
        XCTAssertEqual(pos.lat, 33 + 25.64 / 60, accuracy: 1e-6)
        XCTAssertEqual(pos.lon, -(12 + 7.74 / 60), accuracy: 1e-6)
        XCTAssertEqual(pos.courseDeg, 251)
        XCTAssertEqual(pos.speedKmh ?? 0, 20 * 1.852, accuracy: 1e-6)
        XCTAssertEqual(pos.symbolCode, ">")
        XCTAssertEqual(pos.symbolTable, "/")
    }

    func testObject() throws {
        let p = AprsPacket(source: "PT2VHF", destination: "APZVHF", path: [], info: ";REPETIDOR*111111z1548.00S/04806.60Wr145.290MHz")
        let pos = try XCTUnwrap(AprsParser.position(p))
        XCTAssertEqual(pos.objectName, "REPETIDOR")
        XCTAssertFalse(pos.objectKilled)
    }

    func testMessagesAndReplyAck() throws {
        let m = try XCTUnwrap(AprsParser.message(":PT2VHF-9 :Bom dia{12"))
        XCTAssertEqual(m.addressee, "PT2VHF-9")
        XCTAssertEqual(m.text, "Bom dia")
        XCTAssertEqual(m.id, "12")
        XCTAssertEqual(AprsParser.message(":PT2VHF-9 :Oi{42}31")?.id, "42")
        let ack = try XCTUnwrap(AprsParser.message(":PT2VHF-9 :ack42"))
        XCTAssertTrue(ack.isAck)
        XCTAssertEqual(ack.ackedId, "42")
    }

    func testThirdParty() throws {
        let outer = try XCTUnwrap(AprsPacket.parse("IGATE>APRS,TCPIP*:}PT2ABC-7>APRS,TCPIP,IGATE*::PT2VHF-9 :Teste{1"))
        let inner = try XCTUnwrap(outer.thirdParty)
        XCTAssertEqual(inner.source, "PT2ABC-7")
        XCTAssertEqual(AprsParser.message(inner.info)?.text, "Teste")
    }

    func testEncodeRoundTrip() throws {
        let info = Aprs.positionInfo(lat: -15.8, lon: -48.11, table: "/", code: ">", courseDeg: 90, speedKmh: 46.3, comment: "Olá São Paulo")
        XCTAssertTrue(info.hasPrefix("=1548.00S/04806.60W>090/025"))
        XCTAssertTrue(info.hasSuffix("Ola Sao Paulo"))
        let pos = try XCTUnwrap(AprsParser.position(AprsPacket(source: "X", destination: "APZVHF", path: [], info: info)))
        XCTAssertEqual(pos.lat, -15.8, accuracy: 1e-4)
        XCTAssertEqual(Aprs.messageInfo(to: "py2abc", text: "Olá{x}", id: "7"), ":PY2ABC   :Ola(x}{7")
        XCTAssertEqual(Aprs.ackInfo(to: "PY2ABC", id: "7"), ":PY2ABC   :ack7")
    }

    func testKissAx25RoundTrip() throws {
        let raw = "PT2VHF-9>APZVHF,WIDE1-1,WIDE2-1:=1548.00S/04806.60W>teste"
        let frame = try Ax25.fromTnc2(raw)
        let kiss = Kiss.encode(frame)
        XCTAssertEqual(kiss.first, 0xC0)
        // Entrega em pedaços, como chega por notificações BLE.
        let d = Kiss.Decoder()
        var out: [[UInt8]] = []
        out += d.feed(Array(kiss[0..<10]))
        out += d.feed(Array(kiss[10...]))
        XCTAssertEqual(out.count, 1)
        XCTAssertEqual(try Ax25.toTnc2(out[0]), raw)
        // Escape de FEND dentro do conteúdo.
        XCTAssertEqual(Kiss.encode([0xC0, 0xDB]), [0xC0, 0x00, 0xDB, 0xDC, 0xDB, 0xDD, 0xC0])
    }

    func testDistance() {
        XCTAssertEqual(Geo.distanceKm(-15.78, -47.93, -23.55, -46.63), 873, accuracy: 10)
    }

    /// #101: mesmas regras de ACK do Android 1.4.
    func testAckVariants() throws {
        for body in ["ack12", "ack12 ", "ack12}", "ack12}AB"] {
            let m = try XCTUnwrap(AprsParser.message(":PT2VHF-9 :" + body), body)
            XCTAssertTrue(m.isAck, body)
            XCTAssertEqual(m.ackedId, "12", body)
        }
        let rej = try XCTUnwrap(AprsParser.message(":PT2VHF-9 :rej7"))
        XCTAssertTrue(rej.isRej)
        XCTAssertEqual(rej.ackedId, "7")
        let normal = try XCTUnwrap(AprsParser.message(":PT2VHF-9 :ack recebido{3"))
        XCTAssertNil(normal.ackedId)
        XCTAssertEqual(normal.id, "3")
    }

    func testAckMatchesBaseCall() {
        XCTAssertTrue(Aprs.ackMatches(dest: "PT2ATC", ackFrom: "PT2ATC-7"))
        XCTAssertTrue(Aprs.ackMatches(dest: "PT2ATC-7", ackFrom: "PT2ATC"))
        XCTAssertTrue(Aprs.ackMatches(dest: "pt2atc-7", ackFrom: "PT2ATC-7"))
        XCTAssertFalse(Aprs.ackMatches(dest: "PT2ATC-7", ackFrom: "PT2ATC-9"))
        XCTAssertFalse(Aprs.ackMatches(dest: "PT2ATC", ackFrom: "PT2ATD"))
        XCTAssertEqual(Aprs.ackSource(addressee: "PT2VHF", ownCall: "PT2VHF-9"), "PT2VHF")
        XCTAssertEqual(Aprs.ackSource(addressee: "PT2VHF-9", ownCall: "PT2VHF-9"), "PT2VHF-9")
        XCTAssertEqual(Aprs.ackSource(addressee: "PY2ABC", ownCall: "PT2VHF-9"), "PT2VHF-9")
    }
}
