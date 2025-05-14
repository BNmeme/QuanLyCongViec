package com.example.quanlycongviec.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quanlycongviec.ui.screens.settings.SettingsViewModel

// Dark theme color scheme
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    primaryContainer = PrimaryVariant,
    secondary = SecondaryColor,
    secondaryContainer = SecondaryVariant,
    tertiary = AccentColor,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = ErrorColor
)

// Light theme color scheme
private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    primaryContainer = PrimaryLight,
    secondary = SecondaryColor,
    secondaryContainer = SecondaryLight,
    tertiary = AccentColor,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = ErrorColor
)

@Composable
fun TaskManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color to ensure consistent colors across devices
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Get the settings from SettingsViewModel to check if dark mode is enabled
    val settingsViewModel: SettingsViewModel = viewModel()
    val uiState by settingsViewModel.uiState.collectAsState()

    // Use the dark mode setting from preferences if available, otherwise use system setting
    val isDarkTheme = if (uiState.isLoading) {
        darkTheme
    } else {
        uiState.darkModeEnabled
    }

    // Choose the color scheme based on the dark mode setting
    val colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        // Update the status bar color and appearance when theme changes
        DisposableEffect(isDarkTheme) {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
            onDispose {}
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
