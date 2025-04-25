package com.example.quanlycongviec.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.TaskRepository
import com.example.quanlycongviec.data.repository.UserRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Task
import com.example.quanlycongviec.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    private val taskRepository = AppModule.provideTaskRepository()
    private val userRepository = AppModule.provideUserRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    // Load user data
                    val user = userRepository.getUserById(currentUserId)

                    // Load tasks
                    val personalTasks = taskRepository.getPersonalTasks(currentUserId)
                    val groupTasks = taskRepository.getGroupTasksForUser(currentUserId)

                    // Get recent tasks (5 most recent)
                    val allTasks = (personalTasks + groupTasks).sortedByDescending { it.createdAt }
                    val recentTasks = allTasks.take(5)

                    // Get today's tasks
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis

                    val tomorrow = today + 24 * 60 * 60 * 1000

                    val todayTasks = allTasks.filter {
                        it.dueDate in today until tomorrow
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            personalTasks = personalTasks,
                            groupTasks = groupTasks,
                            recentTasks = recentTasks,
                            todayTasks = todayTasks
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load data: ${e.message}"
                    )
                }
            }
        }
    }

    fun signOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                onComplete()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to sign out: ${e.message}") }
            }
        }
    }

    fun showAddTaskDialog() {
        _uiState.update { it.copy(showAddTaskDialog = true) }
    }

    fun hideAddTaskDialog() {
        _uiState.update { it.copy(showAddTaskDialog = false) }
    }

    fun refreshData() {
        loadData()
    }
}

data class HomeUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val personalTasks: List<Task> = emptyList(),
    val groupTasks: List<Task> = emptyList(),
    val recentTasks: List<Task> = emptyList(),
    val todayTasks: List<Task> = emptyList(),
    val showAddTaskDialog: Boolean = false,
    val errorMessage: String? = null
)
