package com.kvsm.memorymatch.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HighScoreTableTest {

    private fun table(vararg turns: Int) = turns.mapIndexed { i, t -> HighScore("P$i", t, i.toLong()) }

    @Test
    fun `empty table takes anything at the top`() {
        assertEquals(0, HighScoreTable.rankFor(emptyList(), 99))
    }

    @Test
    fun `fewer turns ranks higher and ties go below the earlier score`() {
        val t = table(8, 10, 12)
        assertEquals(0, HighScoreTable.rankFor(t, 7))
        assertEquals(2, HighScoreTable.rankFor(t, 10))
        val after = HighScoreTable.insert(t, HighScore("New", 10, 100))
        assertEquals(listOf("P0", "P1", "New", "P2"), after.map { it.name })
    }

    @Test
    fun `full table only accepts beating the tenth score and stays at ten`() {
        val full = table(5, 6, 7, 8, 9, 10, 11, 12, 13, 14)
        assertNull(HighScoreTable.rankFor(full, 14))
        assertNull(HighScoreTable.rankFor(full, 20))
        assertEquals(9, HighScoreTable.rankFor(full, 13))
        val after = HighScoreTable.insert(full, HighScore("New", 13, 100))
        assertEquals(10, after.size)
        assertEquals("New", after.last().name)
    }

    @Test
    fun `names are tidied and never blank`() {
        assertEquals("Player", HighScoreTable.cleanName("   "))
        assertEquals("Kev Smith", HighScoreTable.cleanName("  Kev   Smith "))
        assertEquals(12, HighScoreTable.cleanName("A".repeat(30)).length)
    }
}
