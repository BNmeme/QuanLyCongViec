package com.example.quanlycongviec.ui.screens.tasks.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.TaskRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PersonalTaskDetailViewModel(
    private val taskId: String
) : ViewModel() {
    private val taskRepository = AppModule.provideTaskRepository()
    
    private val _uiState = MutableStateFlow(PersonalTaskDetailUiState())
    val uiState: StateFlow<PersonalTaskDetailUiState> = _uiState.asStateFlow()
    
    init {
        loadTask()
    }
    
    private fun loadTask() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val task = taskRepository.getTaskById(taskId)
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        task = task
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load task: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun toggleTaskCompletion(isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                taskRepository.toggleTaskCompletion(taskId, isCompleted)
                
                // Update local state
                _uiState.update { 
                    it.copy(
                        task = it.task?.copy(isCompleted = isCompleted)
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(errorMessage = "Failed to update task: ${e.message}") 
                }
            }
        }
    }
    
    fun showDeleteConfirmationDialog() {
        _uiState.update { it.copy(showDeleteConfirmationDialog = true) }
    }
    
    fun hideDeleteConfirmationDialog() {
        _uiState.update { it.copy(showDeleteConfirmationDialog = false) }
    }
    
    fun deleteTask(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                taskRepository.deleteTask(taskId)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        errorMessage = "Failed to delete task: ${e.message}",
                        showDeleteConfirmationDialog = false
                    ) 
                }
            }
        }
    }
    
    fun showEditTaskDialog() {
        _uiState.update { it.copy(showEditTaskDialog = true) }
    }
    
    fun hideEditTaskDialog() {
        _uiState.update { it.copy(showEditTaskDialog = false) }
    }
}

data class PersonalTaskDetailUiState(
    val isLoading: Boolean = false,
    val task: Task? = null,
    val errorMessage: String? = null,
    val showDeleteConfirmationDialog: Boolean = false,
    val showEditTaskDialog: Boolean = false
)

class PersonalTaskDetailViewModelFactory(private val taskId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PersonalTaskDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PersonalTaskDetailViewModel(taskId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
