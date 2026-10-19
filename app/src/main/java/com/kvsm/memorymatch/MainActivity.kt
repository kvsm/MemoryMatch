package com.kvsm.memorymatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kvsm.memorymatch.game.GameViewModel
import com.kvsm.memorymatch.game.Screen
import com.kvsm.memorymatch.ui.GameScreen
import com.kvsm.memorymatch.ui.HighScoresScreen
import com.kvsm.memorymatch.ui.MemoryMatchTheme
import com.kvsm.memorymatch.ui.Palette
import com.kvsm.memorymatch.ui.SetupScreen

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MemoryMatchTheme {
                val screen by viewModel.screen.collectAsStateWithLifecycle()
                val setup by viewModel.setup.collectAsStateWithLifecycle()
                val highScores by viewModel.highScores.collectAsStateWithLifecycle()
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Palette.Cream)
                        .safeDrawingPadding()
                ) {
                    AnimatedContent(
                        targetState = screen,
                        contentKey = { it::class },
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen",
                    ) { current ->
                        when (current) {
                            Screen.Setup -> SetupScreen(
                                choice = setup,
                                onSolo = viewModel::chooseSolo,
                                onPlayers = viewModel::choosePlayers,
                                onDifficulty = viewModel::chooseDifficulty,
                                onStart = viewModel::startGame,
                                onHighScores = { viewModel.showHighScores() },
                            )
                            is Screen.Playing -> GameScreen(
                                playing = current,
                                highScores = highScores[current.difficulty].orEmpty(),
                                lastName = viewModel.lastName,
                                onTapCard = viewModel::tapCard,
                                onPlayAgain = viewModel::startGame,
                                onNewGame = viewModel::backToSetup,
                                onSubmitScore = viewModel::submitScore,
                            )
                            is Screen.HighScores -> HighScoresScreen(
                                difficulty = current.difficulty,
                                tables = highScores,
                                onDifficulty = { viewModel.showHighScores(it) },
                                onBack = viewModel::backToSetup,
                            )
                        }
                    }
                }
            }
        }
    }
}
