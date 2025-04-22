package com.example.quanlycongviec.ui.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Group
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateGroupViewModel : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    private val groupRepository = AppModule.provideGroupRepository()
    
    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()
    
    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }
    
    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description, descriptionError = null) }
    }
    
    fun createGroup(onSuccess: () -> Unit) {
        if (!validateInputs()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    val group = Group(
                        name = _uiState.value.name,
                        description = _uiState.value.description,
                        createdBy = currentUserId,
                        members = listOf(currentUserId),
                        createdAt = System.currentTimeMillis()
                    )
                    
                    groupRepository.createGroup(group)
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to create group: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (_uiState.value.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Group name cannot be empty") }
            isValid = false
        }
        
        if (_uiState.value.description.isBlank()) {
            _uiState.update { it.copy(descriptionError = "Description cannot be empty") }
            isValid = false
        }
        
        return isValid
    }
}

data class CreateGroupUiState(
    val name: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val descriptionError: String? = null,
    val errorMessage: String? = null
)
