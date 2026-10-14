package com.kvsm.memorymatch.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object Palette {
    val Cream = Color(0xFFFFF6E6)
    val Ink = Color(0xFF3B2F3F)
    val InkSoft = Color(0xFF7A6C7E)
    val CardBack = Color(0xFF2F6F8F)
    val CardBackDots = Color(0xFF5FA8C7)
    val Sunshine = Color(0xFFFFB547)
    val Match = Color(0xFF5DBB63)
    val Miss = Color(0xFFEF5B5B)
    val Panel = Color(0xFFFFFFFF)

    /** One colour per player seat. */
    val Players = listOf(
        Color(0xFFEF5B5B), // coral
        Color(0xFF4F9DDE), // blue
        Color(0xFF5DBB63), // green
        Color(0xFF9B6FD6), // purple
        Color(0xFFF7882F), // orange
    )
}

fun playerName(index: Int) = "Player ${index + 1}"

@Composable
fun MemoryMatchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Palette.Sunshine,
            onPrimary = Palette.Ink,
            background = Palette.Cream,
            onBackground = Palette.Ink,
            surface = Palette.Panel,
            onSurface = Palette.Ink,
        ),
        content = content,
    )
}
