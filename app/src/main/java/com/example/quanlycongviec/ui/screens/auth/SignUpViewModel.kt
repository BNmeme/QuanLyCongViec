package com.example.quanlycongviec.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.di.AppModule
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()
    
    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }
    
    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }
    
    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }
    
    fun signUp(onSuccess: () -> Unit) {
        if (!validateInputs()) return
        
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            try {
                authRepository.signUp(
                    _uiState.value.email,
                    _uiState.value.password,
                    _uiState.value.name
                )
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: FirebaseAuthUserCollisionException) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "This email is already in use. Please sign in instead."
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "An error occurred: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (_uiState.value.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name cannot be empty") }
            isValid = false
        }
        
        if (_uiState.value.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email cannot be empty") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) {
            _uiState.update { it.copy(emailError = "Please enter a valid email address") }
            isValid = false
        }
        
        if (_uiState.value.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Password cannot be empty") }
            isValid = false
        } else if (_uiState.value.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Password must be at least 6 characters") }
            isValid = false
        }
        
        return isValid
    }
}

data class SignUpUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null
)
