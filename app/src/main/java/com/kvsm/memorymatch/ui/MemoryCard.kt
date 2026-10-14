package com.kvsm.memorymatch.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kvsm.memorymatch.game.CardArt

enum class Highlight { NONE, MATCH, MISS }

@Composable
fun MemoryCard(
    art: CardArt,
    faceUp: Boolean,
    matched: Boolean,
    highlight: Highlight,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val rotation by animateFloatAsState(
        targetValue = if (faceUp) 180f else 0f,
        animationSpec = tween(380, easing = FastOutSlowInEasing),
        label = "flip",
    )
    // Matched pairs pop and shrink away, leaving an empty gap in the grid.
    val presence by animateFloatAsState(
        targetValue = if (matched) 0f else 1f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "clear",
    )
    // A happy bounce for a match, a little wobble for a miss.
    val bounce = remember { Animatable(1f) }
    val wobble = remember { Animatable(0f) }
    LaunchedEffect(highlight) {
        when (highlight) {
            Highlight.MATCH -> {
                bounce.animateTo(1.12f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                bounce.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            }
            Highlight.MISS -> {
                for (angle in listOf(-6f, 5f, -3f, 0f)) wobble.animateTo(angle, tween(70))
            }
            Highlight.NONE -> {
                bounce.snapTo(1f)
                wobble.snapTo(0f)
            }
        }
    }

    val shape = RoundedCornerShape(14.dp)
    val showFace = rotation > 90f
    val ringColor = when (highlight) {
        Highlight.MATCH -> Palette.Match
        Highlight.MISS -> Palette.Miss
        Highlight.NONE -> Color.Transparent
    }

    Box(
        modifier
            .graphicsLayer {
                rotationY = rotation
                rotationZ = wobble.value
                cameraDistance = 14f * density.density
                val s = bounce.value * (0.6f + 0.4f * presence)
                scaleX = s
                scaleY = s
                alpha = presence
            }
            .semantics { contentDescription = if (showFace) art.label else "Face-down card" }
            .shadow(if (presence > 0.5f) 5.dp else 0.dp, shape)
            .clip(shape)
            .clickable(
                enabled = !faceUp && !matched,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (showFace) {
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f } // un-mirror the back half of the flip
                    .background(Palette.Panel, shape)
                    .border(4.dp, ringColor, shape),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(art.drawable),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                )
            }
        } else {
            CardBack(Modifier.fillMaxSize())
        }
    }
}

/** Polka-dot card back with a soft inner frame. */
@Composable
fun CardBack(modifier: Modifier = Modifier) {
    Canvas(modifier.background(Palette.CardBack)) {
        val inset = size.minDimension * 0.07f
        val dot = size.minDimension * 0.045f
        val step = size.minDimension * 0.19f
        var y = inset + step / 2
        var row = 0
        while (y < size.height - inset) {
            var x = inset + step / 2 + if (row % 2 == 1) step / 2 else 0f
            while (x < size.width - inset) {
                drawCircle(Palette.CardBackDots, dot, Offset(x, y))
                x += step
            }
            y += step * 0.8f
            row++
        }
        drawRoundRect(
            color = Color.White.copy(alpha = 0.75f),
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = CornerRadius(inset * 1.2f),
            style = Stroke(width = size.minDimension * 0.025f),
        )
        // A little star in the middle.
        val c = center
        val r = size.minDimension * 0.16f
        val star = androidx.compose.ui.graphics.Path().apply {
            for (i in 0 until 10) {
                val radius = if (i % 2 == 0) r else r * 0.45f
                val angle = Math.toRadians(-90.0 + i * 36.0)
                val px = c.x + (radius * kotlin.math.cos(angle)).toFloat()
                val py = c.y + (radius * kotlin.math.sin(angle)).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
        drawPath(star, Palette.Sunshine)
    }
}
