package com.kvsm.memorymatch.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kvsm.memorymatch.game.CardArt
import com.kvsm.memorymatch.game.Difficulty
import com.kvsm.memorymatch.game.GameEngine
import com.kvsm.memorymatch.game.SetupChoice

@Composable
fun SetupScreen(
    choice: SetupChoice,
    onSolo: (Boolean) -> Unit,
    onPlayers: (Int) -> Unit,
    onDifficulty: (Difficulty) -> Unit,
    onStart: () -> Unit,
    onHighScores: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(12.dp))
        CardFan()
        Spacer(Modifier.height(16.dp))
        Text("Memory Match", fontSize = 40.sp, fontWeight = FontWeight.Black, color = Palette.Ink)
        Text(
            "Find the pairs of toys and animals!",
            fontSize = 16.sp,
            color = Palette.InkSoft,
        )

        Spacer(Modifier.height(28.dp))
        ModeToggle(solo = choice.solo, onSolo = onSolo)

        AnimatedContent(
            targetState = choice.solo,
            transitionSpec = { fadeIn() togetherWith fadeOut() using SizeTransform(clip = false) },
            label = "mode",
        ) { solo ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (solo) {
                    Text(
                        "Clear the board in as few turns as you can.\nFind a pair and you get a free go.\nBeat the top 10 to get your name on the board!",
                        fontSize = 16.sp,
                        color = Palette.InkSoft,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    SectionTitle("How many players?")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (n in GameEngine.MIN_PLAYERS..GameEngine.MAX_PLAYERS) {
                            PlayerCountButton(n, selected = n == choice.players, onClick = { onPlayers(n) })
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (p in 0 until choice.players) {
                            Box(
                                Modifier
                                    .size(14.dp)
                                    .background(Palette.Players[p], CircleShape),
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionTitle("How many cards?")
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            for (d in Difficulty.entries) {
                DifficultyButton(
                    d,
                    selected = d == choice.difficulty,
                    onClick = { onDifficulty(d) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        BigButton("Let's play!", Palette.Sunshine, onClick = onStart)
        Spacer(Modifier.height(12.dp))
        BigButton("🏆  Top 10 scores", Palette.Panel, onClick = onHighScores)
        Spacer(Modifier.height(16.dp))
    }
}

/** Two-segment pill: Solo or Party. */
@Composable
private fun ModeToggle(solo: Boolean, onSolo: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(3.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(Palette.Panel)
            .border(3.dp, Palette.Ink, RoundedCornerShape(50))
            .padding(5.dp),
    ) {
        for ((label, isSolo) in listOf("🧠  Solo" to true, "🎉  Party" to false)) {
            val selected = solo == isSolo
            val bg by animateColorAsState(if (selected) Palette.Sunshine else Color.Transparent, label = "seg")
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(bg)
                    .clickable(role = Role.RadioButton) { onSolo(isSolo) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    fontSize = 18.sp,
                    fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                    color = if (selected) Palette.Ink else Palette.InkSoft,
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Palette.Ink,
        modifier = Modifier.padding(bottom = 12.dp),
    )
}

/** Three cards fanned out, gently bobbing. */
@Composable
private fun CardFan() {
    val bob = rememberInfiniteTransition(label = "bob")
    val t by bob.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "t",
    )
    val arts = listOf(CardArt.CAT, CardArt.DUCK, CardArt.ROBOT)
    Box(Modifier.height(150.dp), contentAlignment = Alignment.Center) {
        arts.forEachIndexed { i, art ->
            val angle = (i - 1) * 14f
            val dx = (i - 1) * 74
            val dy = if (i == 1) 0f else 14f
            Box(
                Modifier
                    .offset(x = dx.dp, y = (dy + t * if (i == 1) -5f else 4f).dp)
                    .rotate(angle + t * (i - 1) * 2f)
                    .width(96.dp)
                    .aspectRatio(0.8f)
                    .shadow(6.dp, RoundedCornerShape(14.dp))
                    .background(Palette.Panel, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(art.drawable), contentDescription = null, Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
private fun PlayerCountButton(n: Int, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) Palette.Players[n - 2] else Palette.Panel, label = "bg")
    val scale by animateFloatAsState(if (selected) 1.12f else 1f, label = "scale")
    Box(
        Modifier
            .size(60.dp)
            .scale(scale)
            .shadow(if (selected) 6.dp else 2.dp, CircleShape)
            .clip(CircleShape)
            .background(bg)
            .border(3.dp, Palette.Players[n - 2], CircleShape)
            .clickable(role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$n",
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = if (selected) Color.White else Palette.Ink,
        )
    }
}

@Composable
private fun DifficultyButton(
    d: Difficulty,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(if (selected) Palette.CardBack else Palette.Panel, label = "bg")
    Column(
        modifier
            .shadow(if (selected) 6.dp else 2.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(bg)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            d.label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = if (selected) Color.White else Palette.Ink,
        )
        Text(
            "${d.cardCount} cards",
            fontSize = 14.sp,
            color = if (selected) Color.White.copy(alpha = 0.85f) else Palette.InkSoft,
        )
    }
}

@Composable
fun BigButton(text: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(6.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(color)
            .border(3.dp, Palette.Ink, RoundedCornerShape(50))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Palette.Ink)
    }
}
