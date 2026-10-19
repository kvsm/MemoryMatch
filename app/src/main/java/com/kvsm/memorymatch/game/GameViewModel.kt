package com.kvsm.memorymatch.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Setup : Screen

    data class Playing(
        val game: GameState,
        val difficulty: Difficulty,
        /** The solo score saved to the top 10 from this game, once the player has entered a name. */
        val submitted: HighScore? = null,
    ) : Screen

    data class HighScores(val difficulty: Difficulty) : Screen
}

data class SetupChoice(
    val solo: Boolean = false,
    val players: Int = 2,
    val difficulty: Difficulty = Difficulty.MEDIUM,
)

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val store = HighScoreStore(app)

    private val _screen = MutableStateFlow<Screen>(Screen.Setup)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _setup = MutableStateFlow(SetupChoice())
    val setup: StateFlow<SetupChoice> = _setup.asStateFlow()

    private val _highScores = MutableStateFlow(store.loadAll())
    val highScores: StateFlow<Map<Difficulty, List<HighScore>>> = _highScores.asStateFlow()

    val lastName: String get() = store.lastName

    private var resolveJob: Job? = null

    fun chooseSolo(solo: Boolean) {
        _setup.value = _setup.value.copy(solo = solo)
    }

    fun choosePlayers(n: Int) {
        _setup.value = _setup.value.copy(players = n.coerceIn(GameEngine.MIN_PLAYERS, GameEngine.MAX_PLAYERS))
    }

    fun chooseDifficulty(d: Difficulty) {
        _setup.value = _setup.value.copy(difficulty = d)
    }

    fun startGame() {
        resolveJob?.cancel()
        val choice = _setup.value
        val players = if (choice.solo) GameEngine.SOLO else choice.players
        val game = GameEngine.newGame(players, choice.difficulty.pairCount, CardArt.entries.size)
        _screen.value = Screen.Playing(game, choice.difficulty)
    }

    fun backToSetup() {
        resolveJob?.cancel()
        _screen.value = Screen.Setup
    }

    fun showHighScores(difficulty: Difficulty = _setup.value.difficulty) {
        resolveJob?.cancel()
        _screen.value = Screen.HighScores(difficulty)
    }

    /** Saves a finished solo game to its grid size's top 10 under [name]. */
    fun submitScore(name: String) {
        val playing = _screen.value as? Screen.Playing ?: return
        val game = playing.game
        if (!game.isSolo || !game.isOver || playing.submitted != null) return
        val table = _highScores.value[playing.difficulty].orEmpty()
        if (HighScoreTable.rankFor(table, game.turns) == null) return

        val clean = HighScoreTable.cleanName(name)
        val entry = HighScore(clean, game.turns, System.currentTimeMillis())
        val updated = HighScoreTable.insert(table, entry)
        store.save(playing.difficulty, updated)
        store.lastName = clean
        _highScores.value = _highScores.value + (playing.difficulty to updated)
        _screen.value = playing.copy(submitted = entry)
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
                val pause = when {
                    phase.isMatch -> MATCH_PAUSE_MS
                    next.isSolo -> SOLO_MISS_PAUSE_MS
                    else -> MISS_PAUSE_MS
                }
                delay(pause)
                val current = _screen.value as? Screen.Playing ?: return@launch
                _screen.value = current.copy(game = GameEngine.resolve(current.game))
            }
        }
    }

    companion object {
        const val MATCH_PAUSE_MS = 900L
        const val MISS_PAUSE_MS = 1600L
        const val SOLO_MISS_PAUSE_MS = 1200L
    }
}
