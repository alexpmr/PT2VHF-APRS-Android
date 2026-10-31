package com.pt2vhf.aprs.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import org.json.JSONArray

/**
 * Indexed, persistent station and RF-link cache.
 * All writes are dispatched from AprsRepository's single-thread executor.
 * Raw APRS packets, passwords and passcodes are deliberately not stored here.
 */
internal class StationStore(context: Context) : SQLiteOpenHelper(context, "stations.db", null, 2) {
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.enableWriteAheadLogging()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE stations(
            call TEXT PRIMARY KEY NOT NULL,
            lat REAL NOT NULL, lon REAL NOT NULL,
            symbol_table TEXT NOT NULL, symbol TEXT NOT NULL,
            comment TEXT NOT NULL, heard INTEGER NOT NULL, first_seen INTEGER NOT NULL,
            medium TEXT NOT NULL, via TEXT NOT NULL,
            speed REAL, course INTEGER, heard_by TEXT NOT NULL
        )""")
        db.execSQL("""CREATE TABLE rf_links(
            source TEXT NOT NULL, destination TEXT NOT NULL, seen INTEGER NOT NULL,
            PRIMARY KEY(source, destination)
        )""")
        db.execSQL("""CREATE TABLE own_track(
            id INTEGER PRIMARY KEY AUTOINCREMENT, recorded INTEGER NOT NULL,
            lat REAL NOT NULL, lon REAL NOT NULL
        )""")
        db.execSQL("CREATE INDEX idx_track_recorded ON own_track(recorded)")
        db.execSQL("CREATE INDEX idx_station_heard ON stations(heard)")
        db.execSQL("CREATE INDEX idx_link_seen ON rf_links(seen)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Older stations did not record first reception. The existing "heard" timestamp
        // is an estimated upper bound; preserve history instead of wiping the database.
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE stations ADD COLUMN first_seen INTEGER NOT NULL DEFAULT 0")
            db.execSQL("UPDATE stations SET first_seen = heard WHERE first_seen = 0")
        }
        check(newVersion == 2) { "Missing StationStore migration $oldVersion to $newVersion" }
    }

    fun readStations(since: Long): Map<String, Station> {
        val result = LinkedHashMap<String, Station>()
        readableDatabase.query("stations", null, "heard >= ?", arrayOf(since.toString()),
            null, null, "heard DESC", "10000").use { cursor ->
            while (cursor.moveToNext()) {
                fun str(name: String) = cursor.getString(cursor.getColumnIndexOrThrow(name))
                fun nullableDouble(name: String): Double? {
                    val i = cursor.getColumnIndexOrThrow(name)
                    return if (cursor.isNull(i)) null else cursor.getDouble(i)
                }
                fun nullableInt(name: String): Int? {
                    val i = cursor.getColumnIndexOrThrow(name)
                    return if (cursor.isNull(i)) null else cursor.getInt(i)
                }
                val medium = runCatching { Medium.valueOf(str("medium")) }.getOrDefault(Medium.INTERNET)
                val heardBy = runCatching {
                    val a = JSONArray(str("heard_by"))
                    (0 until a.length()).map { a.getString(it) }.toSet()
                }.getOrDefault(emptySet())
                val call = str("call")
                result[call] = Station(
                    call, cursor.getDouble(cursor.getColumnIndexOrThrow("lat")),
                    cursor.getDouble(cursor.getColumnIndexOrThrow("lon")),
                    str("symbol_table").firstOrNull() ?: '/',
                    str("symbol").firstOrNull() ?: '>',
                    str("comment"), cursor.getLong(cursor.getColumnIndexOrThrow("heard")),
                    medium, str("via"), nullableDouble("speed"), nullableInt("course"), heardBy,
                    cursor.getLong(cursor.getColumnIndexOrThrow("first_seen")),
                )
            }
        }
        return result
    }

    fun readLinks(since: Long): List<RfLink> {
        val out = ArrayList<RfLink>()
        readableDatabase.query("rf_links", null, "seen >= ?", arrayOf(since.toString()),
            null, null, "seen DESC", "2000").use { cursor ->
            while (cursor.moveToNext()) {
                out.add(RfLink(cursor.getString(0), cursor.getString(1), cursor.getLong(2)))
            }
        }
        return out
    }

    fun saveStation(station: Station) {
        val c = ContentValues().apply {
            put("call", station.call)
            put("lat", station.lat); put("lon", station.lon)
            put("symbol_table", station.symbolTable.toString())
            put("symbol", station.symbol.toString())
            put("comment", station.comment); put("heard", station.lastHeard)
            put("first_seen", station.firstSeen)
            put("medium", station.medium.name); put("via", station.via)
            if (station.speedKmh != null) put("speed", station.speedKmh) else putNull("speed")
            if (station.course != null) put("course", station.course) else putNull("course")
            put("heard_by", JSONArray(station.heardBy.toList()).toString())
        }
        val db = writableDatabase
        db.beginTransaction()
        try {
            val latest = db.rawQuery("SELECT heard, first_seen FROM stations WHERE call = ?", arrayOf(station.call)).use {
                if (it.moveToFirst()) Pair(it.getLong(0), it.getLong(1))
                else Pair(0L, station.firstSeen)
            }
            if (station.lastHeard >= latest.first) {
                c.put("first_seen", minOf(station.firstSeen, latest.second))
                db.insertWithOnConflict("stations", null, c, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun saveLinks(links: List<RfLink>) {
        if (links.isEmpty()) return
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (link in links) {
                val c = ContentValues().apply {
                    put("source", link.from); put("destination", link.to); put("seen", link.lastSeen)
                }
                db.insertWithOnConflict("rf_links", null, c, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun readTrack(since: Long): List<TrackPoint> {
        val out = ArrayList<TrackPoint>()
        readableDatabase.query("own_track", arrayOf("lat", "lon", "recorded"),
            "recorded >= ?", arrayOf(since.toString()), null, null,
            "recorded ASC", "10000").use { cursor ->
            while (cursor.moveToNext()) out.add(
                TrackPoint(cursor.getDouble(0), cursor.getDouble(1), cursor.getLong(2))
            )
        }
        return out
    }

    fun saveTrack(point: TrackPoint) {
        val c = ContentValues().apply {
            put("recorded", point.recordedAt)
            put("lat", point.lat); put("lon", point.lon)
        }
        val db = writableDatabase
        db.insert("own_track", null, c)
        db.execSQL("""DELETE FROM own_track WHERE id NOT IN
            (SELECT id FROM own_track ORDER BY recorded DESC LIMIT 10000)""")
    }

    /** Removes the station cache and its derived RF links, preserving the own GPS track. */
    fun clearStationsAndLinks() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("stations", null, null)
            db.delete("rf_links", null, null)
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    fun clearTrack() { writableDatabase.delete("own_track", null, null) }

    fun prune(olderThan: Long) {
        writableDatabase.delete("stations", "heard < ?", arrayOf(olderThan.toString()))
        writableDatabase.delete("rf_links", "seen < ?", arrayOf(olderThan.toString()))
        writableDatabase.delete("own_track", "recorded < ?", arrayOf((System.currentTimeMillis() - 7L * 86400000).toString()))
    }
}
