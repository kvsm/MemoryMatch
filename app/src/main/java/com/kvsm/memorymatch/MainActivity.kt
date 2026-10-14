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
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Palette.Cream)
                        .safeDrawingPadding()
                ) {
                    AnimatedContent(
                        targetState = screen is Screen.Setup,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "screen",
                    ) { onSetup ->
                        val current = screen
                        if (onSetup || current !is Screen.Playing) {
                            SetupScreen(
                                choice = setup,
                                onPlayers = viewModel::choosePlayers,
                                onDifficulty = viewModel::chooseDifficulty,
                                onStart = viewModel::startGame,
                            )
                        } else {
                            GameScreen(
                                playing = current,
                                onTapCard = viewModel::tapCard,
                                onPlayAgain = viewModel::startGame,
                                onNewGame = viewModel::backToSetup,
                            )
                        }
                    }
                }
            }
        }
    }
}
