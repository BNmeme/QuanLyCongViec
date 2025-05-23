package com.example.quanlycongviec.ui.screens.main

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

class MainViewModel : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    private val userRepository = AppModule.provideUserRepository()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    // This function will be called when the screen becomes visible
    fun refreshUserData() {
        checkAuthState()
        loadUserData()
    }

    private fun checkAuthState() {
        val isLoggedIn = authRepository.isUserLoggedIn()
        _uiState.update { it.copy(isLoggedIn = isLoggedIn) }

        if (isLoggedIn) {
            loadUserData()
        } else {
            // Clear user data when logged out
            _uiState.update {
                it.copy(
                    userName = "",
                    userEmail = ""
                )
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()

            if (userId == null) {
                _uiState.update {
                    it.copy(
                        isLoggedIn = false,
                        userName = "",
                        userEmail = ""
                    )
                }
                return@launch
            }

            try {
                val user = userRepository.getUserById(userId)
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            userName = user.name,
                            userEmail = user.email
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _uiState.update {
                    it.copy(
                        isLoggedIn = false,
                        userName = "",
                        userEmail = ""
                    )
                }
                onComplete()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

data class MainUiState(
    val isLoggedIn: Boolean = false,
    val userName: String = "",
    val userEmail: String = ""
)
