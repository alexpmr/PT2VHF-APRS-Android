package com.pt2vhf.aprs.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.pt2vhf.aprs.core.AprsMessage
import org.json.JSONArray
import java.io.File

/**
 * SQLite message history (#26). Replaces messages.json, which was rewritten in full on every
 * packet and capped at 500 entries. Writes come from AprsRepository's single disk thread.
 */
internal class MessageStore(private val context: Context) : SQLiteOpenHelper(context, "messages.db", null, 1) {
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.enableWriteAheadLogging()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE messages(
            id INTEGER PRIMARY KEY NOT NULL,
            from_call TEXT NOT NULL, to_call TEXT NOT NULL, text TEXT NOT NULL,
            time INTEGER NOT NULL, outgoing INTEGER NOT NULL, medium TEXT NOT NULL,
            via TEXT NOT NULL, msg_id TEXT, status TEXT NOT NULL, kind TEXT NOT NULL,
            reply_to INTEGER
        )""")
        db.execSQL("CREATE INDEX idx_messages_time ON messages(time)")
        db.execSQL("CREATE INDEX idx_messages_to ON messages(to_call)")
        db.execSQL("CREATE INDEX idx_messages_from ON messages(from_call)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        check(newVersion == 1) { "Missing MessageStore migration $oldVersion to $newVersion" }
    }

    private fun values(m: ChatMessage) = ContentValues().apply {
        put("id", m.id); put("from_call", m.from); put("to_call", m.to); put("text", m.text)
        put("time", m.time); put("outgoing", if (m.outgoing) 1 else 0); put("medium", m.medium?.name ?: "")
        put("via", m.via); if (m.msgId != null) put("msg_id", m.msgId) else putNull("msg_id")
        put("status", m.status); put("kind", m.kind.name)
        if (m.replyToId != null) put("reply_to", m.replyToId) else putNull("reply_to")
    }

    fun readAll(): List<ChatMessage> {
        val out = ArrayList<ChatMessage>()
        readableDatabase.query("messages", null, null, null, null, null, "time ASC, id ASC").use { c ->
            fun idx(n: String) = c.getColumnIndexOrThrow(n)
            while (c.moveToNext()) {
                out += ChatMessage(
                    c.getLong(idx("id")), c.getString(idx("from_call")), c.getString(idx("to_call")),
                    c.getString(idx("text")), c.getLong(idx("time")), c.getInt(idx("outgoing")) == 1,
                    c.getString(idx("medium")).takeIf { it.isNotEmpty() }
                        ?.let { runCatching { Medium.valueOf(it) }.getOrNull() },
                    c.getString(idx("via")),
                    if (c.isNull(idx("msg_id"))) null else c.getString(idx("msg_id")),
                    c.getString(idx("status")),
                    runCatching { AprsMessage.Kind.valueOf(c.getString(idx("kind"))) }.getOrDefault(AprsMessage.Kind.MESSAGE),
                    if (c.isNull(idx("reply_to"))) null else c.getLong(idx("reply_to")),
                )
            }
        }
        return out
    }

    fun upsert(messages: List<ChatMessage>) {
        if (messages.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (m in messages) db.insertWithOnConflict("messages", null, values(m), SQLiteDatabase.CONFLICT_REPLACE)
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun delete(ids: Collection<Long>) {
        if (ids.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (chunk in ids.chunked(500)) {
                db.execSQL("DELETE FROM messages WHERE id IN (${chunk.joinToString(",")})")
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun clear() { writableDatabase.delete("messages", null, null) }

    fun sizeBytes(): Long = runCatching { context.getDatabasePath("messages.db").length() }.getOrDefault(0L)

    /**
     * One-time, non-destructive migration of the legacy messages.json: rows are inserted in a
     * transaction and the JSON is renamed to messages.json.migrated (kept as a backup) only after
     * the commit. If anything fails the JSON stays in place and the next start retries.
     */
    fun migrateLegacyJson(file: File): Int {
        if (!file.exists()) return 0
        val arr = JSONArray(file.readText())
        val list = (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            ChatMessage(o.getLong("id"), o.getString("from"), o.getString("to"), o.getString("text"),
                o.getLong("time"), o.getBoolean("out"),
                o.optString("medium").takeIf { it.isNotEmpty() }?.let { runCatching { Medium.valueOf(it) }.getOrNull() },
                o.optString("via"), o.optString("msgId").ifEmpty { null }, o.optString("status"),
                runCatching { AprsMessage.Kind.valueOf(o.optString("kind")) }.getOrDefault(AprsMessage.Kind.MESSAGE))
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            // IGNORE: re-running after a crash between commit and rename never duplicates rows.
            for (m in list) db.insertWithOnConflict("messages", null, values(m), SQLiteDatabase.CONFLICT_IGNORE)
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
        file.renameTo(File(file.parentFile, file.name + ".migrated"))
        return list.size
    }
}
