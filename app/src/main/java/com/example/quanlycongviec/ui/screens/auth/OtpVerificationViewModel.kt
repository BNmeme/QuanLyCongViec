package com.example.quanlycongviec.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OtpVerificationViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtpVerificationUiState())
    val uiState: StateFlow<OtpVerificationUiState> = _uiState.asStateFlow()

    fun setEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updateOtp(otp: String) {
        _uiState.update { it.copy(otp = otp, otpError = null) }
    }

    fun verifyOtp(onSuccess: () -> Unit) {
        if (uiState.value.otp.length != 6) {
            _uiState.update { it.copy(otpError = "Please enter a valid 6-digit code") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val isValid = authRepository.verifyPasswordResetOtp(uiState.value.email, uiState.value.otp)
                _uiState.update { it.copy(isLoading = false) }
                if (isValid) {
                    onSuccess()
                } else {
                    _uiState.update { it.copy(otpError = "Invalid verification code") }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to verify code"
                    )
                }
            }
        }
    }

    fun resendOtp() {
        val email = uiState.value.email

        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email is not valid") }
            return
        }

        _uiState.update { it.copy(isResending = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                authRepository.sendPasswordResetOtp(email)
                _uiState.update {
                    it.copy(
                        isResending = false,
                        otp = "",
                        otpError = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isResending = false,
                        errorMessage = e.message ?: "Failed to resend code"
                    )
                }
            }
        }
    }
}

data class OtpVerificationUiState(
    val email: String = "",
    val otp: String = "",
    val otpError: String? = null,
    val isLoading: Boolean = false,
    val isResending: Boolean = false,
    val errorMessage: String? = null
)
