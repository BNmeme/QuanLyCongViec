package com.example.quanlycongviec.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.quanlycongviec.TaskManagerApplication

// Light and dark neutral colors
val White = Color(0xFFFFFFFF)
val LightGray = Color(0xFFE0E0E0)
val DarkGray = Color(0xFF424242)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,           // Using the blue #1976D2
    secondary = PrimaryLight,         // Light blue variant
    tertiary = PrimaryVariant,        // Dark blue variant
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = LightGray,
    onSurface = LightGray
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,           // Using the blue #1976D2
    secondary = PrimaryLight,         // Light blue variant
    tertiary = PrimaryVariant,        // Dark blue variant
    background = LightBackground,
    surface = LightSurface,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = DarkGray,
    onSurface = DarkGray
)

@Composable
fun TaskManagerTheme(
    content: @Composable () -> Unit
) {
    // Get the current dark mode preference from ThemeManager
    val darkTheme by ThemeManager.darkThemeState

    // Determine the color scheme based on the dark mode preference
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Update the status bar color based on the theme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
