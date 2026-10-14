package com.kvsm.memorymatch.game

import kotlin.random.Random

enum class Difficulty(val label: String, val columns: Int, val rows: Int) {
    EASY("Easy", 3, 4),
    MEDIUM("Medium", 4, 5),
    HARD("Hard", 5, 6);

    val cardCount: Int get() = columns * rows
    val pairCount: Int get() = cardCount / 2
}

data class Card(
    val id: Int,
    val pairKey: Int,
    val faceUp: Boolean = false,
    val matched: Boolean = false,
)

sealed interface Phase {
    /** Waiting for the current player to turn over their first card. */
    data object PickFirst : Phase

    /** One card is showing; waiting for the second. */
    data class PickSecond(val first: Int) : Phase

    /** Two cards are showing; the game pauses so everyone can see them before resolving. */
    data class Revealing(val first: Int, val second: Int, val isMatch: Boolean) : Phase

    data object GameOver : Phase
}

data class GameState(
    val cards: List<Card>,
    val scores: List<Int>,
    val currentPlayer: Int,
    val phase: Phase,
) {
    val playerCount: Int get() = scores.size
    val isOver: Boolean get() = phase == Phase.GameOver

    /** Indices of every player sharing the top score (more than one means a tie). */
    val leaders: List<Int>
        get() {
            val best = scores.maxOrNull() ?: return emptyList()
            return scores.indices.filter { scores[it] == best }
        }
}

/**
 * Pure game rules, free of Android so they can be unit tested.
 * Every function returns a new state and leaves the old one untouched.
 */
object GameEngine {
    const val MIN_PLAYERS = 2
    const val MAX_PLAYERS = 5

    /** Builds a shuffled deck: [pairCount] distinct pictures, two cards each. */
    fun newGame(players: Int, pairCount: Int, artCount: Int, random: Random = Random): GameState {
        require(players in MIN_PLAYERS..MAX_PLAYERS) { "players must be $MIN_PLAYERS-$MAX_PLAYERS" }
        require(pairCount in 1..artCount) { "not enough pictures for $pairCount pairs" }
        val keys = (0 until artCount).shuffled(random).take(pairCount)
        val cards = (keys + keys).shuffled(random).mapIndexed { i, key -> Card(id = i, pairKey = key) }
        return GameState(cards, List(players) { 0 }, currentPlayer = 0, phase = Phase.PickFirst)
    }

    /** Turns a card face up. Ignored if the tap isn't allowed right now. */
    fun pick(state: GameState, index: Int): GameState {
        val card = state.cards.getOrNull(index) ?: return state
        if (card.faceUp || card.matched) return state
        return when (val phase = state.phase) {
            Phase.PickFirst -> state.copy(
                cards = state.cards.flip(index, up = true),
                phase = Phase.PickSecond(index),
            )
            is Phase.PickSecond -> state.copy(
                cards = state.cards.flip(index, up = true),
                phase = Phase.Revealing(
                    first = phase.first,
                    second = index,
                    isMatch = state.cards[phase.first].pairKey == card.pairKey,
                ),
            )
            is Phase.Revealing, Phase.GameOver -> state
        }
    }

    /**
     * Settles a revealed pair. A match clears both cards, scores a point and keeps the turn;
     * a miss turns them back over and passes play to the next player.
     */
    fun resolve(state: GameState): GameState {
        val phase = state.phase as? Phase.Revealing ?: return state
        return if (phase.isMatch) {
            val cards = state.cards.mapIndexed { i, c ->
                if (i == phase.first || i == phase.second) c.copy(matched = true, faceUp = true) else c
            }
            val scores = state.scores.toMutableList().also { it[state.currentPlayer]++ }
            state.copy(
                cards = cards,
                scores = scores,
                phase = if (cards.all { it.matched }) Phase.GameOver else Phase.PickFirst,
            )
        } else {
            state.copy(
                cards = state.cards.flip(phase.first, up = false).flip(phase.second, up = false),
                currentPlayer = (state.currentPlayer + 1) % state.playerCount,
                phase = Phase.PickFirst,
            )
        }
    }

    private fun List<Card>.flip(index: Int, up: Boolean) =
        mapIndexed { i, c -> if (i == index) c.copy(faceUp = up) else c }
}
