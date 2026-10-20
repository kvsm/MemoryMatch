package com.kvsm.memorymatch.game

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class HighScore(val name: String, val turns: Int, val timestamp: Long)

/** Pure top-10 rules: fewer turns ranks higher; on a tie, whoever got there first stays above. */
object HighScoreTable {
    const val SIZE = 10
    const val MAX_NAME = 12

    /** The 0-based position a score of [turns] would take, or null if it wouldn't make the table. */
    fun rankFor(table: List<HighScore>, turns: Int): Int? =
        table.count { it.turns <= turns }.takeIf { it < SIZE }

    fun insert(table: List<HighScore>, entry: HighScore): List<HighScore> =
        (table + entry).sortedWith(compareBy({ it.turns }, { it.timestamp })).take(SIZE)

    fun cleanName(raw: String): String =
        raw.trim().replace(Regex("\\s+"), " ").take(MAX_NAME).ifEmpty { "Player" }
}

/** Keeps a top-10 per grid size on the device, plus the last name typed in. */
class HighScoreStore(context: Context) {
    private val prefs = context.getSharedPreferences("high_scores", Context.MODE_PRIVATE)

    init {
        // Scores from v1.0.2 counted matches as turns; they're not comparable, so clear them.
        val old = prefs.all.keys.filter { it.startsWith("solo_") && !it.startsWith(PREFIX) }
        if (old.isNotEmpty()) prefs.edit().apply { old.forEach { remove(it) } }.apply()
    }

    fun load(difficulty: Difficulty): List<HighScore> {
        val raw = prefs.getString(key(difficulty), null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                HighScore(o.getString("name"), o.getInt("turns"), o.getLong("ts"))
            }
        }.getOrDefault(emptyList())
    }

    fun loadAll(): Map<Difficulty, List<HighScore>> = Difficulty.entries.associateWith { load(it) }

    fun save(difficulty: Difficulty, table: List<HighScore>) {
        val arr = JSONArray()
        table.forEach { arr.put(JSONObject().put("name", it.name).put("turns", it.turns).put("ts", it.timestamp)) }
        prefs.edit().putString(key(difficulty), arr.toString()).apply()
    }

    var lastName: String
        get() = prefs.getString("last_name", "") ?: ""
        set(value) = prefs.edit().putString("last_name", value).apply()

    private fun key(d: Difficulty) = PREFIX + d.name.lowercase()

    private companion object {
        const val PREFIX = "solo_v2_"
    }
}
