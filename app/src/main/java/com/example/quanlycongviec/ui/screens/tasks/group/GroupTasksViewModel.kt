package com.example.quanlycongviec.ui.screens.tasks.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.GroupRepository
import com.example.quanlycongviec.data.repository.TaskRepository
import com.example.quanlycongviec.data.repository.UserRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Group
import com.example.quanlycongviec.domain.model.Task
import com.example.quanlycongviec.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupTasksViewModel : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    private val taskRepository = AppModule.provideTaskRepository()
    private val groupRepository = AppModule.provideGroupRepository()
    private val userRepository = AppModule.provideUserRepository()
    
    private val _uiState = MutableStateFlow(GroupTasksUiState())
    val uiState: StateFlow<GroupTasksUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    // Load tasks
                    val tasks = taskRepository.getGroupTasksForUser(currentUserId)
                    
                    // Load groups
                    val groups = groupRepository.getGroupsForUser(currentUserId)
                    
                    // Load members for each group
                    val groupMembers = mutableMapOf<String, List<User>>()
                    for (group in groups) {
                        val members = userRepository.getUsersByIds(group.members)
                        groupMembers[group.id] = members
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            tasks = tasks,
                            currentUserId = currentUserId,
                            groups = groups,
                            groupMembers = groupMembers
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
    
    fun showAddTaskDialog() {
        _uiState.update { it.copy(showAddTaskDialog = true) }
    }
    
    fun hideAddTaskDialog() {
        _uiState.update { it.copy(showAddTaskDialog = false) }
    }
    
    fun addTask(task: Task, onComplete: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAddingTask = true) }
            
            try {
                taskRepository.createTask(task)
                loadData()
                _uiState.update { it.copy(isAddingTask = false, showAddTaskDialog = false) }
                onComplete()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isAddingTask = false,
                        errorMessage = "Failed to add task: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun filterTasks(groupId: String?) {
        _uiState.update { 
            it.copy(
                selectedGroupFilter = groupId
            ) 
        }
    }
}

data class GroupTasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val currentUserId: String = "",
    val groups: List<Group> = emptyList(),
    val groupMembers: Map<String, List<User>> = emptyMap(),
    val showAddTaskDialog: Boolean = false,
    val isAddingTask: Boolean = false,
    val selectedGroupFilter: String? = null,
    val errorMessage: String? = null
) {
    val filteredTasks: List<Task>
        get() = if (selectedGroupFilter != null) {
            tasks.filter { it.groupId == selectedGroupFilter }
        } else {
            tasks
        }
}
