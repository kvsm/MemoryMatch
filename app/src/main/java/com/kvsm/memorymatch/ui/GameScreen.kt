package com.kvsm.memorymatch.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.kvsm.memorymatch.game.CardArt
import com.kvsm.memorymatch.game.GameState
import com.kvsm.memorymatch.game.Phase
import com.kvsm.memorymatch.game.Screen
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    playing: Screen.Playing,
    onTapCard: (Int) -> Unit,
    onPlayAgain: () -> Unit,
    onNewGame: () -> Unit,
) {
    val game = playing.game
    var confirmQuit by remember { mutableStateOf(false) }
    BackHandler(enabled = !game.isOver) { confirmQuit = true }
    BackHandler(enabled = game.isOver) { onNewGame() }

    val haptics = LocalHapticFeedback.current
    val phase = game.phase
    LaunchedEffect(phase) {
        if (phase is Phase.Revealing && phase.isMatch) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Let the last pair finish shrinking away before the results appear.
    var showResults by remember(game.cards.size, game.isOver) { mutableStateOf(false) }
    LaunchedEffect(game.isOver) {
        if (game.isOver) {
            delay(550)
            showResults = true
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TurnBanner(game)
            Spacer(Modifier.height(10.dp))
            Scoreboard(game)
            Spacer(Modifier.height(12.dp))
            CardGrid(
                game = game,
                columns = playing.difficulty.columns,
                rows = playing.difficulty.rows,
                onTapCard = onTapCard,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
        }

        AnimatedVisibility(
            visible = showResults,
            enter = fadeIn(tween(300)) + scaleIn(initialScale = 0.9f),
            exit = fadeOut(),
        ) {
            ResultsOverlay(game, onPlayAgain = onPlayAgain, onNewGame = onNewGame)
        }
    }

    if (confirmQuit) {
        AlertDialog(
            onDismissRequest = { confirmQuit = false },
            title = { Text("Leave this game?", fontWeight = FontWeight.Bold) },
            text = { Text("The scores will be lost.") },
            confirmButton = {
                TextButton(onClick = { confirmQuit = false; onNewGame() }) {
                    Text("Leave", color = Palette.Miss, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmQuit = false }) {
                    Text("Keep playing", color = Palette.Ink)
                }
            },
            containerColor = Palette.Cream,
        )
    }
}

@Composable
private fun TurnBanner(game: GameState) {
    val phase = game.phase
    val player = game.currentPlayer
    val message = when {
        phase is Phase.Revealing && phase.isMatch -> "Match! ${playerName(player)} goes again"
        phase is Phase.Revealing -> "No match… ${playerName((player + 1) % game.playerCount)} next"
        phase == Phase.GameOver -> "All pairs found!"
        else -> "${playerName(player)}'s turn"
    }
    val color by animateColorAsState(
        targetValue = when {
            phase is Phase.Revealing && phase.isMatch -> Palette.Match
            phase == Phase.GameOver -> Palette.Sunshine
            else -> Palette.Players[player]
        },
        label = "bannerColor",
    )
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(50))
            .background(color, RoundedCornerShape(50))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = message,
            transitionSpec = {
                (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
            },
            label = "bannerText",
        ) { text ->
            Text(
                text,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun Scoreboard(game: GameState) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        for (p in 0 until game.playerCount) {
            val active = p == game.currentPlayer && !game.isOver
            val lift by animateDpAsState(if (active) 6.dp else 0.dp, label = "lift")
            val color = Palette.Players[p]
            Column(
                Modifier
                    .weight(1f)
                    .padding(top = 6.dp - lift)
                    .shadow(if (active) 6.dp else 1.dp, RoundedCornerShape(16.dp))
                    .background(if (active) color else Palette.Panel, RoundedCornerShape(16.dp))
                    .border(3.dp, color, RoundedCornerShape(16.dp))
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "P${p + 1}",
                    color = if (active) Color.White else color,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                )
                AnimatedContent(
                    targetState = game.scores[p],
                    transitionSpec = { (scaleIn(initialScale = 1.8f) + fadeIn()) togetherWith fadeOut() },
                    label = "score",
                ) { score ->
                    Text(
                        "$score",
                        color = if (active) Color.White else Palette.Ink,
                        fontWeight = FontWeight.Black,
                        fontSize = 26.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun CardGrid(
    game: GameState,
    columns: Int,
    rows: Int,
    onTapCard: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val phase = game.phase
    val arts = CardArt.entries
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        // Turn the grid sideways in landscape so cards stay a sensible size.
        val landscape = maxWidth > maxHeight
        val cols = if (landscape) rows else columns
        val rowCount = if (landscape) columns else rows
        val gap = 8.dp
        val aspect = 0.8f // width / height
        val cellW = (maxWidth - gap * (cols - 1)) / cols
        val cellH = (maxHeight - gap * (rowCount - 1)) / rowCount
        val cardW = min(cellW, cellH * aspect)
        val cardH = cardW / aspect

        Column(verticalArrangement = Arrangement.spacedBy(gap)) {
            for (r in 0 until rowCount) {
                Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                    for (c in 0 until cols) {
                        val index = r * cols + c
                        val card = game.cards[index]
                        val highlight = when {
                            phase is Phase.Revealing && (index == phase.first || index == phase.second) ->
                                if (phase.isMatch) Highlight.MATCH else Highlight.MISS
                            else -> Highlight.NONE
                        }
                        MemoryCard(
                            art = arts[card.pairKey],
                            faceUp = card.faceUp,
                            matched = card.matched,
                            highlight = highlight,
                            onClick = { onTapCard(index) },
                            modifier = Modifier
                                .width(cardW)
                                .height(cardH),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsOverlay(game: GameState, onPlayAgain: () -> Unit, onNewGame: () -> Unit) {
    val leaders = game.leaders
    val headline = if (leaders.size == 1) {
        "${playerName(leaders.first())} wins!"
    } else {
        "It's a tie!"
    }
    val subline = if (leaders.size == 1) {
        val s = game.scores[leaders.first()]
        "with $s ${if (s == 1) "pair" else "pairs"}"
    } else {
        leaders.joinToString(" & ") { playerName(it) } + " share the win"
    }
    val ranking = game.scores.indices.sortedByDescending { game.scores[it] }
    val headlineColor = if (leaders.size == 1) Palette.Players[leaders.first()] else Palette.Ink

    Box(
        Modifier
            .fillMaxSize()
            .background(Palette.Ink.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        Confetti(Modifier.fillMaxSize())
        Column(
            Modifier
                .padding(24.dp)
                .shadow(12.dp, RoundedCornerShape(28.dp))
                .background(Palette.Cream, RoundedCornerShape(28.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🏆", fontSize = 56.sp)
            Text(
                headline,
                color = headlineColor,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
            )
            Text(subline, color = Palette.InkSoft, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            for (p in ranking) {
                val isLeader = p in leaders
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(
                            if (isLeader) Palette.Players[p].copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(14.dp)
                            .background(Palette.Players[p], RoundedCornerShape(50)),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        playerName(p),
                        color = Palette.Ink,
                        fontWeight = if (isLeader) FontWeight.Black else FontWeight.Medium,
                        fontSize = 18.sp,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        "${game.scores[p]}",
                        color = Palette.Ink,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            BigButton("Play again", Palette.Sunshine, onClick = onPlayAgain)
            Spacer(Modifier.height(10.dp))
            BigButton("New game", Palette.Panel, onClick = onNewGame)
        }
    }
}
