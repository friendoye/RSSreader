package com.friendoye.rss_reader.ui

import androidx.compose.Composable
import androidx.ui.foundation.isSystemInDarkTheme
import androidx.ui.graphics.Color
import androidx.ui.material.MaterialTheme
import androidx.ui.material.darkColorPalette
import androidx.ui.material.lightColorPalette

val ORANGE_500 = Color(0xFFFF9800)
val ORANGE_700 = Color(0xFFF57C00)
val AMBER_A400 = Color(0xFFFFC400)

val DEEP_PURPLE_500 = Color(0xFF673AB7)
val DEEP_PURPLE_700 = Color(0xFF512DA8)
val LIGHT_BLUE_A400 = Color(0xFF69E2FF)

val LIGHT_COLOR_PALETTE = lightColorPalette(
    primary = ORANGE_500,
    primaryVariant = ORANGE_700,
    secondary = AMBER_A400
)

val DARK_COLOR_PALETTE = darkColorPalette(
    primary = DEEP_PURPLE_500,
    primaryVariant = DEEP_PURPLE_700,
    secondary = LIGHT_BLUE_A400,
    onPrimary = Color.White
)

@Composable
fun RssReaderAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DARK_COLOR_PALETTE else LIGHT_COLOR_PALETTE,
        content = content
    )
}
