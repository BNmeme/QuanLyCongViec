package com.example.quanlycongviec.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun setEmailAndOtp(email: String, otp: String) {
        _uiState.update { it.copy(email = email, otp = otp) }
    }

    fun updateNewPassword(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                newPassword = password,
                newPasswordError = validatePassword(password),
                confirmPasswordError = validatePasswordMatch(password, currentState.confirmPassword)
            )
        }
    }

    fun updateConfirmPassword(password: String) {
        _uiState.update { currentState ->
            currentState.copy(
                confirmPassword = password,
                confirmPasswordError = validatePasswordMatch(currentState.newPassword, password)
            )
        }
    }

    fun resetPassword(onSuccess: () -> Unit) {
        val newPassword = uiState.value.newPassword
        val confirmPassword = uiState.value.confirmPassword
        val email = uiState.value.email
        val otp = uiState.value.otp

        // Validate passwords
        val newPasswordError = validatePassword(newPassword)
        val confirmPasswordError = validatePasswordMatch(newPassword, confirmPassword)

        if (newPasswordError != null || confirmPasswordError != null) {
            _uiState.update {
                it.copy(
                    newPasswordError = newPasswordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

        viewModelScope.launch {
            try {
                // Call the repository to reset the password
                authRepository.resetPassword(email, otp, newPassword)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Password reset successfully"
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to reset password"
                    )
                }
            }
        }
    }

    private fun validatePassword(password: String): String? {
        if (password.isBlank()) return null // Don't show error for empty field initially

        return when {
            password.length < 6 -> "Password must be at least 6 characters"
            !password.any { it.isDigit() } -> "Password must contain at least one number"
            !password.any { it.isLetter() } -> "Password must contain at least one letter"
            else -> null
        }
    }

    private fun validatePasswordMatch(password: String, confirmPassword: String): String? {
        if (confirmPassword.isBlank()) return null // Don't show error for empty field initially

        return when {
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }
}

data class ResetPasswordUiState(
    val email: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
