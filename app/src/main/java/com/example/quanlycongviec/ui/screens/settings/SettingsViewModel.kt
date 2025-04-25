package com.example.quanlycongviec.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.UserRepository
import com.example.quanlycongviec.di.AppModule
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
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    val user = userRepository.getUserById(currentUserId)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            darkModeEnabled = user.preferences?.darkModeEnabled ?: false,
                            notificationsEnabled = user.preferences?.notificationsEnabled ?: true,
                            reminderTimeBeforeDeadline = user.preferences?.reminderTimeBeforeDeadline ?: 60
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load settings: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    userRepository.updateUserPreference(currentUserId, "darkModeEnabled", enabled)
                    _uiState.update { it.copy(darkModeEnabled = enabled) }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to update dark mode setting: ${e.message}") 
                }
            }
        }
    }
    
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    userRepository.updateUserPreference(currentUserId, "notificationsEnabled", enabled)
                    _uiState.update { it.copy(notificationsEnabled = enabled) }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to update notification setting: ${e.message}") 
                }
            }
        }
    }
    
    fun updateReminderTime(minutes: Int) {
        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    userRepository.updateUserPreference(currentUserId, "reminderTimeBeforeDeadline", minutes)
                    _uiState.update { it.copy(reminderTimeBeforeDeadline = minutes) }
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
