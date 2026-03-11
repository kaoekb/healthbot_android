package com.antonkiselev.healthcompanion.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Clay,
    onPrimary = Mist,
    secondary = Teal,
    onSecondary = Mist,
    tertiary = LimeSoft,
    background = Blush,
    onBackground = Ink,
    surface = Mist,
    onSurface = Ink,
    error = Danger,
)

private val DarkColors = darkColorScheme(
    primary = Sand,
    onPrimary = ClayDark,
    secondary = Teal,
    onSecondary = Mist,
    tertiary = LimeSoft,
    background = Ink,
    onBackground = Mist,
    surface = ClayDark,
    onSurface = Mist,
    error = Danger,
)

@Composable
fun HealthCompassTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors: ColorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = HealthTypography,
        content = content,
    )
}
