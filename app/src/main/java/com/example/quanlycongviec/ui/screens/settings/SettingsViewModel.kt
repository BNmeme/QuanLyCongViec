package com.example.quanlycongviec.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.TaskManagerApplication
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.UserRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.UserPreferences
import com.example.quanlycongviec.ui.theme.ThemeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    private val userRepository = AppModule.provideUserRepository()

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserSettings()
    }

    private fun loadUserSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Get current user ID
                val currentUserId = authRepository.getCurrentUserId()

                if (currentUserId != null) {
                    // First try to get user preferences from Firebase (source of truth)
                    val user = userRepository.getUserById(currentUserId)
                    val preferences = user?.preferences ?: UserPreferences()

                    // Get the dark mode setting from user preferences in Firebase
                    val darkModeFromFirebase = preferences.darkModeEnabled

                    // Update SharedPreferences to match Firebase (to keep them in sync)
                    TaskManagerApplication.setDarkModeEnabled(darkModeFromFirebase)

                    // Update UI state with user preferences from Firebase
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            darkModeEnabled = darkModeFromFirebase,
                            notificationsEnabled = preferences.notificationsEnabled,
                            reminderTimeBeforeDeadline = preferences.reminderTimeBeforeDeadline
                        )
                    }

                    // Update global theme state
                    ThemeManager.setDarkTheme(darkModeFromFirebase)
                } else {
                    // If no user is logged in, fall back to SharedPreferences
                    val darkModePreference = TaskManagerApplication.isDarkModeEnabled()

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            darkModeEnabled = darkModePreference
                        )
                    }

                    // Update global theme state
                    ThemeManager.setDarkTheme(darkModePreference)
                }

            } catch (e: Exception) {
                // Get dark mode from SharedPreferences as fallback in case of error
                val darkModePreference = TaskManagerApplication.isDarkModeEnabled()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        darkModeEnabled = darkModePreference,
                        errorMessage = "Failed to load settings: ${e.message}"
                    )
                }

                // Update global theme state with fallback value
                ThemeManager.setDarkTheme(darkModePreference)
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        // Update UI state immediately for responsive toggle
        _uiState.update { it.copy(darkModeEnabled = enabled) }

        // Update global theme state immediately
        ThemeManager.setDarkTheme(enabled)

        viewModelScope.launch {
            try {
                // First update user preferences in Firebase (source of truth)
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    // Update the preference in the database
                    userRepository.updateUserPreference(currentUserId, "darkModeEnabled", enabled)
                }

                // Then save to SharedPreferences (app-level preference)
                TaskManagerApplication.setDarkModeEnabled(enabled)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to save dark mode setting: ${e.message}")
                }
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        // Update UI state immediately
        _uiState.update { it.copy(notificationsEnabled = enabled) }

        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    userRepository.updateUserPreference(currentUserId, "notificationsEnabled", enabled)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to update notification setting: ${e.message}")
                }
            }
        }
    }

    fun updateReminderTime(minutes: Int) {
        // Update UI state immediately
        _uiState.update { it.copy(reminderTimeBeforeDeadline = minutes) }

        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    userRepository.updateUserPreference(currentUserId, "reminderTimeBeforeDeadline", minutes)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to update reminder time: ${e.message}")
                }
            }
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                onComplete()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Failed to sign out: ${e.message}")
                }
            }
        }
    }
}

data class SettingsUiState(
    val isLoading: Boolean = false,
    val darkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val reminderTimeBeforeDeadline: Int = 60, // minutes
    val errorMessage: String? = null
)
