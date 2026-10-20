package com.kvsm.memorymatch.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameEngineTest {

    private fun game(players: Int = 3, pairs: Int = 6, seed: Int = 7) =
        GameEngine.newGame(players, pairs, artCount = 16, random = Random(seed))

    private fun GameState.matchFor(index: Int) =
        cards.indices.first { it != index && cards[it].pairKey == cards[index].pairKey }

    private fun GameState.missFor(index: Int) =
        cards.indices.first { it != index && cards[it].pairKey != cards[index].pairKey && !cards[it].matched }

    @Test
    fun `deck has exactly two of each picture, all face down`() {
        for (d in Difficulty.entries) {
            val s = GameEngine.newGame(2, d.pairCount, 16, Random(1))
            assertEquals(d.cardCount, s.cards.size)
            assertTrue(s.cards.groupBy { it.pairKey }.values.all { it.size == 2 })
            assertEquals(d.pairCount, s.cards.map { it.pairKey }.distinct().size)
            assertTrue(s.cards.none { it.faceUp || it.matched })
            assertEquals(List(2) { 0 }, s.scores)
        }
    }

    @Test
    fun `match scores a point, clears the pair and keeps the turn`() {
        var s = game()
        s = GameEngine.pick(s, 0)
        s = GameEngine.pick(s, s.matchFor(0))
        assertTrue((s.phase as Phase.Revealing).isMatch)
        s = GameEngine.resolve(s)
        assertEquals(listOf(1, 0, 0), s.scores)
        assertEquals(0, s.currentPlayer)
        assertEquals(2, s.cards.count { it.matched })
        assertEquals(Phase.PickFirst, s.phase)
    }

    @Test
    fun `miss flips cards back and passes to the next player`() {
        var s = game()
        s = GameEngine.pick(s, 0)
        s = GameEngine.pick(s, s.missFor(0))
        assertFalse((s.phase as Phase.Revealing).isMatch)
        s = GameEngine.resolve(s)
        assertEquals(listOf(0, 0, 0), s.scores)
        assertEquals(1, s.currentPlayer)
        assertTrue(s.cards.none { it.faceUp })
    }

    @Test
    fun `turn wraps from last player back to first`() {
        var s = game(players = 2)
        repeat(2) {
            s = GameEngine.pick(s, 0)
            s = GameEngine.resolve(GameEngine.pick(s, s.missFor(0)))
        }
        assertEquals(0, s.currentPlayer)
    }

    @Test
    fun `taps on the same, matched or extra cards are ignored`() {
        var s = GameEngine.pick(game(), 0)
        assertSame(s, GameEngine.pick(s, 0))
        s = GameEngine.pick(s, s.matchFor(0))
        assertSame(s, GameEngine.pick(s, 5)) // third card while two are showing
        s = GameEngine.resolve(s)
        assertSame(s, GameEngine.pick(s, 0)) // already cleared
    }

    @Test
    fun `game ends when every pair is found and leaders handle ties`() {
        var s = game(players = 2, pairs = 4)
        var turn = 0
        while (!s.isOver) {
            val first = s.cards.indexOfFirst { !it.matched }
            if (turn % 2 == 1 && s.cards.count { !it.matched } > 2) {
                // force a miss to hand the turn over
                s = GameEngine.resolve(GameEngine.pick(GameEngine.pick(s, first), s.missFor(first)))
            } else {
                s = GameEngine.resolve(GameEngine.pick(GameEngine.pick(s, first), s.matchFor(first)))
            }
            turn++
        }
        assertEquals(4, s.scores.sum())
        assertTrue(s.cards.all { it.matched })
        val tie = s.copy(scores = listOf(2, 2))
        assertEquals(listOf(0, 1), tie.leaders)
    }
}

class SoloModeTest {

    @Test
    fun `solo turns only move on after a miss`() {
        var s = GameEngine.newGame(GameEngine.SOLO, 4, artCount = 16, random = Random(3))
        assertTrue(s.isSolo)
        // one miss, then clear the board perfectly
        val first = 0
        val miss = s.cards.indices.first { s.cards[it].pairKey != s.cards[first].pairKey }
        s = GameEngine.resolve(GameEngine.pick(GameEngine.pick(s, first), miss))
        assertEquals(0, s.currentPlayer)
        assertEquals(2, s.turns)
        while (!s.isOver) {
            val a = s.cards.indexOfFirst { !it.matched }
            val b = s.cards.indices.first { it != a && s.cards[it].pairKey == s.cards[a].pairKey }
            s = GameEngine.resolve(GameEngine.pick(GameEngine.pick(s, a), b))
        }
        assertEquals(2, s.turns) // matches are free goes
        assertEquals(4, s.pairsFound)
        assertEquals(listOf(4), s.scores)
    }
}
