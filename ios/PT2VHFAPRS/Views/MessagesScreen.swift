import SwiftUI

struct MessagesScreen: View {
    @EnvironmentObject var model: AppModel
    @State private var newPeer = ""
    @State private var askPeer = false
    @State private var openPeer: String?

    var body: some View {
        NavigationStack {
            List {
                ForEach(model.conversations) { conv in
                    NavigationLink(value: conv.peer) {
                        HStack {
                            Image(systemName: conv.last.isBulletin ? "megaphone" : "person.circle")
                                .font(.title2).foregroundStyle(.tint)
                            VStack(alignment: .leading, spacing: 2) {
                                HStack {
                                    Text(conv.peer).font(.headline.monospaced())
                                    if model.unread.contains(conv.peer) {
                                        Circle().fill(.tint).frame(width: 8, height: 8)
                                    }
                                }
                                Text(conv.last.text).font(.subheadline).foregroundStyle(.secondary).lineLimit(1)
                            }
                            Spacer()
                            Text(conv.last.date, style: .time).font(.caption).foregroundStyle(.secondary)
                        }
                    }
                    .swipeActions {
                        Button(role: .destructive) { model.deleteConversation(conv.peer) } label: {
                            Label("Apagar", systemImage: "trash")
                        }
                    }
                }
            }
            .listStyle(.plain)
            .overlay {
                if model.conversations.isEmpty {
                    ContentUnavailableView("Sem mensagens", systemImage: "message",
                                           description: Text("Toque em ✎ para escrever para um indicativo."))
                }
            }
            .navigationTitle("Mensagens")
            .navigationDestination(for: String.self) { peer in ConversationView(peer: peer) }
            .navigationDestination(item: $openPeer) { peer in ConversationView(peer: peer) }
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { askPeer = true } label: { Image(systemName: "square.and.pencil") }
                }
            }
            .alert("Nova mensagem", isPresented: $askPeer) {
                TextField("Indicativo (ex.: PY2ABC-9)", text: $newPeer)
                    .textInputAutocapitalization(.characters)
                    .autocorrectionDisabled()
                Button("Abrir") {
                    let p = newPeer.trimmingCharacters(in: .whitespaces).uppercased()
                    newPeer = ""
                    if !p.isEmpty { openPeer = p }
                }
                Button("Cancelar", role: .cancel) { newPeer = "" }
            }
        }
    }
}

struct ConversationView: View {
    @EnvironmentObject var model: AppModel
    let peer: String
    @State private var draft = ""
    @FocusState private var focused: Bool

    private var thread: [ChatMessage] { model.messages.filter { $0.peer == peer } }
    private var isBulletin: Bool { thread.first?.isBulletin ?? false }

    var body: some View {
        VStack(spacing: 0) {
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 6) {
                        ForEach(thread) { m in
                            Bubble(message: m) { model.retry(m) }.id(m.id)
                        }
                    }
                    .padding(.horizontal, 10)
                    .padding(.vertical, 8)
                }
                .onAppear {
                    model.unread.remove(peer)
                    if let last = thread.last { proxy.scrollTo(last.id, anchor: .bottom) }
                }
                .onChange(of: thread.count) {
                    model.unread.remove(peer)
                    if let last = thread.last { withAnimation { proxy.scrollTo(last.id, anchor: .bottom) } }
                }
            }
            if !isBulletin {
                Divider()
                HStack(alignment: .bottom, spacing: 8) {
                    TextField("Mensagem", text: $draft, axis: .vertical)
                        .lineLimit(1...4)
                        .focused($focused)
                        .padding(8)
                        .background(Color(.secondarySystemBackground), in: RoundedRectangle(cornerRadius: 18))
                    Text("\(67 - draft.count)")
                        .font(.caption2.monospacedDigit())
                        .foregroundStyle(draft.count > 67 ? .red : .secondary)
                        .padding(.bottom, 10)
                    Button {
                        model.send(to: peer, text: draft)
                        draft = ""
                    } label: {
                        Image(systemName: "paperplane.fill").font(.title3)
                    }
                    .disabled(draft.trimmingCharacters(in: .whitespaces).isEmpty || !model.hasValidCall)
                    .padding(.bottom, 8)
                }
                .padding(.horizontal, 10)
                .padding(.vertical, 6)
            }
        }
        .navigationTitle(peer)
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct Bubble: View {
    let message: ChatMessage
    let onRetry: () -> Void

    var body: some View {
        HStack {
            if !message.incoming { Spacer(minLength: 40) }
            VStack(alignment: message.incoming ? .leading : .trailing, spacing: 3) {
                Text(message.text).textSelection(.enabled)
                HStack(spacing: 4) {
                    Text(message.date, format: .dateTime.day().month().hour().minute())
                    if !message.incoming { stateIcon }
                }
                .font(.caption2)
                .foregroundStyle(.secondary)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(message.incoming ? Color(.secondarySystemBackground) : Color.green.opacity(0.22),
                        in: RoundedRectangle(cornerRadius: 16))
            .contextMenu {
                Button { UIPasteboard.general.string = message.text } label: { Label("Copiar", systemImage: "doc.on.doc") }
                if !message.incoming && (message.state == .failed || message.state == .pending) {
                    Button(action: onRetry) { Label("Reenviar", systemImage: "arrow.clockwise") }
                }
            }
            if message.incoming { Spacer(minLength: 40) }
        }
    }

    @ViewBuilder private var stateIcon: some View {
        switch message.state {
        case .pending:
            Text("\(message.tries)/5")
            Image(systemName: "clock")
        case .acked:
            Image(systemName: "checkmark.circle.fill").foregroundStyle(.green)
        case .rejected:
            Image(systemName: "xmark.octagon.fill").foregroundStyle(.red)
        case .failed:
            Image(systemName: "exclamationmark.triangle.fill").foregroundStyle(.orange)
        case .received:
            EmptyView()
        }
    }
}
