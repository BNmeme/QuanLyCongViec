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

class ProfileViewModel : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    private val userRepository = AppModule.provideUserRepository()
    private val taskRepository = AppModule.provideTaskRepository()
    
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    init {
        loadUserProfile()
        loadTaskStatistics()
    }
    
    private fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    val user = userRepository.getUserById(currentUserId)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            user = user
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
    
    private fun loadTaskStatistics() {
        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId() ?: return@launch
                
                // Load personal tasks
                val personalTasks = taskRepository.getPersonalTasks(currentUserId)
                
                // Load group tasks
                val groupTasks = taskRepository.getGroupTasksForUser(currentUserId)
                
                // Calculate completed tasks
                val completedTasks = personalTasks.count { it.isCompleted } + groupTasks.count { it.isCompleted }
                
                _uiState.update { 
                    it.copy(
                        personalTasksCount = personalTasks.size,
                        groupTasksCount = groupTasks.size,
                        completedTasksCount = completedTasks
                    ) 
                }
                
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val personalTasksCount: Int = 0,
    val groupTasksCount: Int = 0,
    val completedTasksCount: Int = 0,
    val errorMessage: String? = null
)
