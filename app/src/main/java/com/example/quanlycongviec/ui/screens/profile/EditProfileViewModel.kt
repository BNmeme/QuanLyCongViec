package com.example.quanlycongviec.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.TaskRepository
import com.example.quanlycongviec.data.repository.UserRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditProfileViewModel : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    private val userRepository = AppModule.provideUserRepository()
    private val taskRepository = AppModule.provideTaskRepository()

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var originalUser: User? = null

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    val user = userRepository.getUserById(currentUserId)
                    if (user != null) {
                        originalUser = user
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                name = user.name,
                                email = user.email
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "User not found"
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Not logged in"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load profile: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = validateName(name)
            )
        }
    }

    fun updateCurrentPassword(password: String) {
        _uiState.update {
            it.copy(
                currentPassword = password,
                currentPasswordError = validateCurrentPassword(password)
            )
        }
    }

    fun updateNewPassword(password: String) {
        val passwordError = validateNewPassword(password)
        _uiState.update {
            it.copy(
                newPassword = password,
                newPasswordError = passwordError,
                // Also update confirm password error if it's not empty
                confirmPasswordError = if (it.confirmPassword.isNotEmpty()) {
                    validatePasswordMatch(password, it.confirmPassword)
                } else {
                    it.confirmPasswordError
                }
            )
        }
    }

    fun updateConfirmPassword(password: String) {
        _uiState.update {
            it.copy(
                confirmPassword = password,
                confirmPasswordError = validatePasswordMatch(it.newPassword, password)
            )
        }
    }

    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "Name cannot be empty"
            name.length < 2 -> "Name must be at least 2 characters"
            name.length > 50 -> "Name must be less than 50 characters"
            else -> null
        }
    }

    private fun validateCurrentPassword(password: String): String? {
        return when {
            password.isBlank() -> "Current password cannot be empty"
            else -> null
        }
    }

    private fun validateNewPassword(password: String): String? {
        return when {
            password.isBlank() -> "New password cannot be empty"
            password.length < 6 -> "Password must be at least 6 characters"
            !password.any { it.isDigit() } -> "Password must contain at least one number"
            !password.any { it.isLetter() } -> "Password must contain at least one letter"
            else -> null
        }
    }

    private fun validatePasswordMatch(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "Confirm password cannot be empty"
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        val currentState = _uiState.value

        // Validate inputs
        val nameError = validateName(currentState.name)
        if (nameError != null) {
            _uiState.update { it.copy(nameError = nameError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null && originalUser != null) {
                    // Create updated user object
                    val updatedUser = originalUser!!.copy(
                        name = currentState.name.trim()
                    )

                    // Save to repository
                    userRepository.updateUser(updatedUser)

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            successMessage = "Profile updated successfully"
                        )
                    }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "User not found or not logged in"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Failed to save profile: ${e.message}"
                    )
                }
            }
        }
    }

    fun changePassword() {
        val currentState = _uiState.value

        // Validate all password fields
        val currentPasswordError = validateCurrentPassword(currentState.currentPassword)
        val newPasswordError = validateNewPassword(currentState.newPassword)
        val confirmPasswordError = validatePasswordMatch(currentState.newPassword, currentState.confirmPassword)

        if (currentPasswordError != null || newPasswordError != null || confirmPasswordError != null) {
            _uiState.update {
                it.copy(
                    currentPasswordError = currentPasswordError,
                    newPasswordError = newPasswordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPassword = true) }

            try {
                authRepository.changePassword(
                    currentState.currentPassword,
                    currentState.newPassword
                )

                // Clear password fields after successful change
                _uiState.update {
                    it.copy(
                        isChangingPassword = false,
                        currentPassword = "",
                        newPassword = "",
                        confirmPassword = "",
                        successMessage = "Password changed successfully"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isChangingPassword = false,
                        errorMessage = "Failed to change password: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }

            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    // Delete user's tasks
//                    taskRepository.deleteUserTasks(currentUserId)

                    // Delete user from Firestore
                    userRepository.deleteUser(currentUserId)

                    // Delete user from Firebase Auth
                    authRepository.deleteAccount()

                    _uiState.update { it.copy(isDeleting = false) }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            errorMessage = "User not found or not logged in"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        errorMessage = "Failed to delete account: ${e.message}"
                    )
                }
            }
        }
    }
}

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isChangingPassword: Boolean = false,
    val isDeleting: Boolean = false,
    val name: String = "",
    val email: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val nameError: String? = null,
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
