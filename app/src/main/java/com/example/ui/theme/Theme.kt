package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CyberColorScheme = darkColorScheme(
    primary = CyberTeal,
    secondary = CyberBlue,
    tertiary = CyberGreen,
    background = SlateDark,
    surface = SlateCard,
    onPrimary = SlateDark,
    onSecondary = SlateDark,
    onTertiary = SlateDark,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = CyberRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force a sleek tech/dark console theme for Cybersecurity by default
    dynamicColor: Boolean = false, // Disable default dynamic device coloring to preserve high fidelity cyber identity
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
