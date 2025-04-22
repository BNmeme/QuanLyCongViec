package com.example.quanlycongviec.ui.screens.tasks.group

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

class EditGroupTaskViewModel(
    private val taskId: String
) : ViewModel() {
    private val taskRepository = AppModule.provideTaskRepository()
    
    private val _uiState = MutableStateFlow(EditGroupTaskUiState())
    val uiState: StateFlow<EditGroupTaskUiState> = _uiState.asStateFlow()
    
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
                        title = task.title,
                        description = task.description,
                        priority = task.priority,
                        dueDate = task.dueDate,
                        originalTask = task
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
    
    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title, titleError = null) }
    }
    
    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description, descriptionError = null) }
    }
    
    fun updatePriority(priority: Int) {
        _uiState.update { it.copy(priority = priority) }
    }
    
    fun updateDueDate(dueDate: Long) {
        _uiState.update { it.copy(dueDate = dueDate) }
    }
    
    fun saveTask(onSuccess: () -> Unit) {
        if (!validateInputs()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val originalTask = _uiState.value.originalTask ?: return@launch
                
                val updatedTask = originalTask.copy(
                    title = _uiState.value.title,
                    description = _uiState.value.description,
                    priority = _uiState.value.priority,
                    dueDate = _uiState.value.dueDate
                )
                
                taskRepository.updateTask(updatedTask)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to save task: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (_uiState.value.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title cannot be empty") }
            isValid = false
        }
        
        if (_uiState.value.description.isBlank()) {
            _uiState.update { it.copy(descriptionError = "Description cannot be empty") }
            isValid = false
        }
        
        return isValid
    }
}

data class EditGroupTaskUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val description: String = "",
    val priority: Int = 3,
    val dueDate: Long = System.currentTimeMillis(),
    val titleError: String? = null,
    val descriptionError: String? = null,
    val errorMessage: String? = null,
    val originalTask: Task? = null
)

class EditGroupTaskViewModelFactory(private val taskId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditGroupTaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditGroupTaskViewModel(taskId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
