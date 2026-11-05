package com.pt2vhf.aprs.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Registro persistente de todos os pacotes RX/TX (#51). Escrito pela thread de disco do repositório;
 * a aba Logs lê o espelho em memória e a exportação lê daqui.
 */
internal class PacketLogStore(private val context: Context) : SQLiteOpenHelper(context, "packets.db", null, 1) {
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.enableWriteAheadLogging()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE packets(
            id INTEGER PRIMARY KEY NOT NULL, time INTEGER NOT NULL, dir TEXT NOT NULL, medium TEXT NOT NULL,
            raw TEXT NOT NULL, src TEXT NOT NULL, dst TEXT NOT NULL, type TEXT NOT NULL)""")
        db.execSQL("CREATE INDEX idx_packets_time ON packets(time)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        check(newVersion == 1) { "Missing PacketLogStore migration $oldVersion to $newVersion" }
    }

    fun insert(entries: List<PacketLogEntry>) {
        if (entries.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (e in entries) {
                db.insertWithOnConflict("packets", null, ContentValues().apply {
                    put("id", e.id); put("time", e.time); put("dir", e.direction); put("medium", e.medium.name)
                    put("raw", e.raw); put("src", e.source); put("dst", e.destination); put("type", e.type)
                }, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    /** Mais recentes primeiro. */
    fun readRecent(limit: Int): List<PacketLogEntry> {
        val out = ArrayList<PacketLogEntry>()
        readableDatabase.query("packets", null, null, null, null, null, "time DESC, id DESC", limit.toString()).use { c ->
            fun idx(n: String) = c.getColumnIndexOrThrow(n)
            while (c.moveToNext()) {
                out += PacketLogEntry(
                    c.getLong(idx("id")), c.getLong(idx("time")), c.getString(idx("dir")),
                    runCatching { Medium.valueOf(c.getString(idx("medium"))) }.getOrDefault(Medium.INTERNET),
                    c.getString(idx("raw")), c.getString(idx("src")), c.getString(idx("dst")), c.getString(idx("type")),
                )
            }
        }
        return out
    }

    fun prune(olderThan: Long, maxRows: Int) {
        val db = writableDatabase
        if (olderThan > 0) db.delete("packets", "time < ?", arrayOf(olderThan.toString()))
        if (maxRows > 0) db.execSQL("DELETE FROM packets WHERE id NOT IN (SELECT id FROM packets ORDER BY time DESC, id DESC LIMIT $maxRows)")
    }

    fun count(): Long = runCatching {
        readableDatabase.rawQuery("SELECT COUNT(*) FROM packets", null).use { c -> if (c.moveToFirst()) c.getLong(0) else 0L }
    }.getOrDefault(0L)

    fun clear() { writableDatabase.delete("packets", null, null) }

    fun sizeBytes(): Long = runCatching { context.getDatabasePath("packets.db").length() }.getOrDefault(0L)
}
