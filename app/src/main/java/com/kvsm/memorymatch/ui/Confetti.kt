package com.kvsm.memorymatch.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

private class Piece(
    val x: Float,
    val speed: Float,
    val phase: Float,
    val sway: Float,
    val spin: Float,
    val size: Float,
    val color: Color,
)

/** Endless, gently falling confetti for the winner's screen. */
@Composable
fun Confetti(modifier: Modifier = Modifier, count: Int = 70) {
    val colors = Palette.Players + Palette.Sunshine
    val pieces = remember {
        val rnd = Random(42)
        List(count) {
            Piece(
                x = rnd.nextFloat(),
                speed = 0.6f + rnd.nextFloat() * 0.8f,
                phase = rnd.nextFloat(),
                sway = 0.01f + rnd.nextFloat() * 0.03f,
                spin = 180f + rnd.nextFloat() * 360f,
                size = 6f + rnd.nextFloat() * 8f,
                color = colors[rnd.nextInt(colors.size)],
            )
        }
    }
    // A steadily rising clock (in 6-second units) so every piece loops smoothly at its own pace.
    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameMillis { it }
        while (true) {
            withFrameMillis { now -> t = (now - start) / 6000f }
        }
    }
    Canvas(modifier) {
        for (p in pieces) {
            val progress = (p.phase + t * p.speed) % 1f
            val y = -20f + progress * (size.height + 40f)
            val x = (p.x + sin((progress * 12f + p.phase * 6f).toDouble()).toFloat() * p.sway) * size.width
            val w = p.size * density / 2f
            rotate(degrees = (t * p.spin * 2f) % 360f, pivot = Offset(x, y)) {
                drawRect(p.color, topLeft = Offset(x - w, y - w * 0.6f), size = Size(w * 2, w * 1.2f))
            }
        }
    }
}
