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
        loadUserData()
    }
    
    private fun checkAuthState() {
        val isLoggedIn = authRepository.isUserLoggedIn()
        _uiState.update { it.copy(isLoggedIn = isLoggedIn) }
    }
    
    private fun loadUserData() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            
            try {
                val user = userRepository.getUserById(userId)
                _uiState.update {
                    it.copy(
                        userName = user.name,
                        userEmail = user.email
                    )
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
                _uiState.update { it.copy(isLoggedIn = false) }
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
