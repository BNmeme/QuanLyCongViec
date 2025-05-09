package com.example.quanlycongviec.ui.screens.tasks.personal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.TaskRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AddPersonalTaskViewModel : ViewModel() {
    private val authRepository: AuthRepository = AppModule.provideAuthRepository()
    private val taskRepository: TaskRepository = AppModule.provideTaskRepository()
    private val _uiState = MutableStateFlow(AddPersonalTaskUiState())
    val uiState: StateFlow<AddPersonalTaskUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUserId()
    }

    private fun loadCurrentUserId() {
        viewModelScope.launch {
            try {
                val userId = authRepository.getCurrentUserId()
                if (userId != null) {
                    _uiState.update { it.copy(currentUserId = userId) }
                } else {
                    _uiState.update { it.copy(errorMessage = "User not logged in") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to load user: ${e.message}") }
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

    fun addTask(onSuccess: () -> Unit) {
        val currentState = _uiState.value
        var isValid = true

        // Validate inputs
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(titleError = "Title cannot be empty") }
            isValid = false
        }

        if (currentState.description.isBlank()) {
            _uiState.update { it.copy(descriptionError = "Description cannot be empty") }
            isValid = false
        }

        if (!isValid) return

        if (currentState.currentUserId.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "User not logged in") }
            return
        }

        val task = Task(
            id = UUID.randomUUID().toString(),
            title = currentState.title,
            description = currentState.description,
            isCompleted = false,
            createdAt = System.currentTimeMillis(),
            dueDate = currentState.dueDate,
            priority = currentState.priority,
            userId = currentState.currentUserId, // Gán userId từ auth
            isGroupTask = false
        )

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                taskRepository.createTask(task)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to add task: ${e.message}"
                    )
                }
            }
        }
    }
}

data class AddPersonalTaskUiState(
    val title: String = "",
    val description: String = "",
    val priority: Int = 3, // Default: Low priority
    val dueDate: Long = System.currentTimeMillis() + 86400000, // Tomorrow
    val currentUserId: String = "",
    val titleError: String? = null,
    val descriptionError: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false
)