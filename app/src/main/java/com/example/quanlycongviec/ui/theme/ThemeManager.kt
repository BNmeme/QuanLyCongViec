package com.example.quanlycongviec.ui.theme

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    private val _darkThemeState = mutableStateOf(false)
    val darkThemeState = _darkThemeState

    fun setDarkTheme(isDarkTheme: Boolean) {
        _darkThemeState.value = isDarkTheme
    }
}
