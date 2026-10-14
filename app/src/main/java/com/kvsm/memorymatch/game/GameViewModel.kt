package com.kvsm.memorymatch.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Setup : Screen
    data class Playing(val game: GameState, val difficulty: Difficulty) : Screen
}

data class SetupChoice(val players: Int = 2, val difficulty: Difficulty = Difficulty.MEDIUM)

class GameViewModel : ViewModel() {

    private val _screen = MutableStateFlow<Screen>(Screen.Setup)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _setup = MutableStateFlow(SetupChoice())
    val setup: StateFlow<SetupChoice> = _setup.asStateFlow()

    private var resolveJob: Job? = null

    fun choosePlayers(n: Int) {
        _setup.value = _setup.value.copy(players = n.coerceIn(GameEngine.MIN_PLAYERS, GameEngine.MAX_PLAYERS))
    }

    fun chooseDifficulty(d: Difficulty) {
        _setup.value = _setup.value.copy(difficulty = d)
    }

    fun startGame() {
        resolveJob?.cancel()
        val choice = _setup.value
        val game = GameEngine.newGame(choice.players, choice.difficulty.pairCount, CardArt.entries.size)
        _screen.value = Screen.Playing(game, choice.difficulty)
    }

    fun backToSetup() {
        resolveJob?.cancel()
        _screen.value = Screen.Setup
    }

    fun tapCard(index: Int) {
        val playing = _screen.value as? Screen.Playing ?: return
        val next = GameEngine.pick(playing.game, index)
        if (next === playing.game) return
        _screen.value = playing.copy(game = next)

        val phase = next.phase
        if (phase is Phase.Revealing) {
            resolveJob = viewModelScope.launch {
                // Give everyone a moment to see (and remember) the two pictures.
                delay(if (phase.isMatch) MATCH_PAUSE_MS else MISS_PAUSE_MS)
                val current = _screen.value as? Screen.Playing ?: return@launch
                _screen.value = current.copy(game = GameEngine.resolve(current.game))
            }
        }
    }

    companion object {
        const val MATCH_PAUSE_MS = 900L
        const val MISS_PAUSE_MS = 1600L
    }
}
