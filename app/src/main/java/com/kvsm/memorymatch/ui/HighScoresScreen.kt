package com.kvsm.memorymatch.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kvsm.memorymatch.game.Difficulty
import com.kvsm.memorymatch.game.HighScore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Gold = Color(0xFFF5B800)
private val Silver = Color(0xFFB8C2CC)
private val Bronze = Color(0xFFD08A4E)

@Composable
fun HighScoresScreen(
    difficulty: Difficulty,
    tables: Map<Difficulty, List<HighScore>>,
    onDifficulty: (Difficulty) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🏆", fontSize = 52.sp)
        Text("Top 10", fontSize = 38.sp, fontWeight = FontWeight.Black, color = Palette.Ink)
        Text("Solo games — fewest turns wins.\nA match gives you a free go.", textAlign = TextAlign.Center, fontSize = 16.sp, color = Palette.InkSoft)
        Spacer(Modifier.height(20.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (d in Difficulty.entries) {
                val selected = d == difficulty
                val bg by animateColorAsState(if (selected) Palette.CardBack else Palette.Panel, label = "tab")
                Box(
                    Modifier
                        .weight(1f)
                        .shadow(if (selected) 5.dp else 1.dp, RoundedCornerShape(50))
                        .clip(RoundedCornerShape(50))
                        .background(bg)
                        .clickable(role = Role.Tab) { onDifficulty(d) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        d.label,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = if (selected) Color.White else Palette.Ink,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        AnimatedContent(
            targetState = difficulty,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "table",
        ) { d ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(22.dp))
                    .background(Palette.Panel, RoundedCornerShape(22.dp))
                    .padding(12.dp),
            ) {
                ScoreTable(tables[d].orEmpty(), highlight = null, showDates = true)
            }
        }

        Spacer(Modifier.height(24.dp))
        BigButton("Back", Palette.Sunshine, onClick = onBack)
    }
}

/** Ranked list of scores; [highlight] is drawn in sunshine yellow (a score just saved). */
@Composable
fun ScoreTable(table: List<HighScore>, highlight: HighScore?, showDates: Boolean = false) {
    if (table.isEmpty()) {
        Text(
            "No scores yet.\nPlay a solo game to set the first one!",
            color = Palette.InkSoft,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
        )
        return
    }
    val dateFormat = SimpleDateFormat("d MMM", Locale.UK)
    table.forEachIndexed { i, score ->
        val isNew = score == highlight
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .background(
                    if (isNew) Palette.Sunshine.copy(alpha = 0.35f) else Color.Transparent,
                    RoundedCornerShape(12.dp),
                )
                .then(if (isNew) Modifier.border(2.dp, Palette.Sunshine, RoundedCornerShape(12.dp)) else Modifier)
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val medal = when (i) {
                0 -> Gold
                1 -> Silver
                2 -> Bronze
                else -> null
            }
            Box(
                Modifier
                    .size(30.dp)
                    .background(medal ?: Palette.Cream, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${i + 1}",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = if (medal != null) Color.White else Palette.InkSoft,
                )
            }
            Spacer(Modifier.width(12.dp))
            Text(
                score.name,
                color = Palette.Ink,
                fontWeight = if (isNew) FontWeight.Black else FontWeight.SemiBold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (showDates) {
                Text(
                    dateFormat.format(Date(score.timestamp)),
                    color = Palette.InkSoft,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(end = 10.dp),
                )
            }
            Text(
                "${score.turns}",
                color = Palette.Ink,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
            )
            Text(
                if (score.turns == 1) " turn" else " turns",
                color = Palette.InkSoft,
                fontSize = 13.sp,
            )
        }
    }
}
