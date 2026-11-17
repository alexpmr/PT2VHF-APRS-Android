import SwiftUI

/// Ícone simples por tipo de símbolo APRS (desenho próprio com SF Symbols, sem copiar outros apps).
struct SymbolBadge: View {
    let table: Character
    let code: Character
    var size: CGFloat = 30

    private var info: (String, Color) {
        switch code {
        case ">": return ("car.fill", .blue)
        case "j", "J": return ("car.side.fill", .blue)
        case "k", "u": return ("truck.box.fill", .indigo)
        case "U": return ("bus.fill", .indigo)
        case "b", "<": return ("bicycle", .teal)
        case "[": return ("figure.walk", .mint)
        case "'", "^", "g": return ("airplane", .cyan)
        case "X": return ("airplane", .cyan)
        case "Y", "s": return ("sailboat.fill", .teal)
        case "-": return ("house.fill", .green)
        case "#": return ("star.fill", .green)
        case "&": return ("diamond.fill", .orange)
        case "r": return ("antenna.radiowaves.left.and.right", .orange)
        case "_", "W", "w": return ("cloud.sun.fill", .gray)
        case "a", "+": return ("cross.case.fill", .red)
        case "f", "d": return ("flame.fill", .red)
        case ";": return ("tent.fill", .green)
        case "$": return ("phone.fill", .purple)
        case "y": return ("antenna.radiowaves.left.and.right", .purple)
        default: return ("mappin", .pink)
        }
    }

    /// Sobreposição (letra/número) na tabela alternativa, como "I" do iGate ou "D" de D-STAR.
    private var overlay: String? {
        guard table != "/" && table != "\\" else { return nil }
        return String(table)
    }

    var body: some View {
        let (name, color) = info
        ZStack {
            Circle().fill(color.gradient)
            Circle().stroke(.white, lineWidth: 1.5)
            Image(systemName: name)
                .font(.system(size: size * 0.5, weight: .semibold))
                .foregroundStyle(.white)
            if let o = overlay {
                Text(o)
                    .font(.system(size: size * 0.32, weight: .heavy))
                    .foregroundStyle(.black)
                    .padding(2)
                    .background(Circle().fill(.yellow))
                    .offset(x: size * 0.32, y: size * 0.32)
            }
        }
        .frame(width: size, height: size)
        .shadow(radius: 1.5)
    }
}
