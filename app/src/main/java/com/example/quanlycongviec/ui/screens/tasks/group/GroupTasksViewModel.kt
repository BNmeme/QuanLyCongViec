package com.example.quanlycongviec.ui.screens.tasks.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.GroupRepository
import com.example.quanlycongviec.data.repository.TaskRepository
import com.example.quanlycongviec.data.repository.UserRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Group
import com.example.quanlycongviec.domain.model.GroupRole
import com.example.quanlycongviec.domain.model.Task
import com.example.quanlycongviec.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GroupFilter {
    ALL_GROUPS,
    MANAGED_GROUPS,
    MEMBER_GROUPS
}

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
                    val groupsWithManagePermission = mutableListOf<String>()
                    val managedGroups = mutableListOf<Group>()
                    val memberGroups = mutableListOf<Group>()

                    for (group in groups) {
                        val members = userRepository.getUsersByIds(group.members)
                        groupMembers[group.id] = members

                        // Check if current user can manage tasks in this group
                        if (group.canManageTasks(currentUserId)) {
                            groupsWithManagePermission.add(group.id)
                            managedGroups.add(group)
                        } else {
                            memberGroups.add(group)
                        }
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tasks = tasks,
                            currentUserId = currentUserId,
                            groups = groups,
                            groupMembers = groupMembers,
                            groupsWithManagePermission = groupsWithManagePermission,
                            managedGroups = managedGroups,
                            memberGroups = memberGroups,
                            filteredTasks = applyFilters(tasks, it.selectedGroupFilter, it.selectedGroupId)
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
                // Check if user has permission to add tasks to this group
                val group = _uiState.value.groups.find { it.id == task.groupId }
                if (group != null && !group.canManageTasks(_uiState.value.currentUserId)) {
                    _uiState.update {
                        it.copy(
                            isAddingTask = false,
                            errorMessage = "You don't have permission to add tasks to this group"
                        )
                    }
                    return@launch
                }

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

    fun setGroupFilter(groupId: String?) {
        _uiState.update {
            it.copy(
                selectedGroupId = groupId,
                filteredTasks = applyFilters(it.tasks, it.selectedGroupFilter, groupId)
            )
        }
    }

    fun setGroupTypeFilter(filter: GroupFilter) {
        _uiState.update {
            it.copy(
                selectedGroupFilter = filter,
                selectedGroupId = null, // Reset specific group selection when changing filter type
                filteredTasks = applyFilters(it.tasks, filter, null),
                isFilterMenuOpen = false
            )
        }
    }

    private fun applyFilters(tasks: List<Task>, groupFilter: GroupFilter, specificGroupId: String?): List<Task> {
        // If a specific group is selected, filter by that group
        if (specificGroupId != null) {
            return tasks.filter { it.groupId == specificGroupId }
        }

        // Otherwise, filter by group type
        return when (groupFilter) {
            GroupFilter.ALL_GROUPS -> tasks
            GroupFilter.MANAGED_GROUPS -> {
                val managedGroupIds = _uiState.value.managedGroups.map { it.id }
                tasks.filter { managedGroupIds.contains(it.groupId) }
            }
            GroupFilter.MEMBER_GROUPS -> {
                val memberGroupIds = _uiState.value.memberGroups.map { it.id }
                tasks.filter { memberGroupIds.contains(it.groupId) }
            }
        }
    }

    fun canManageTasksInGroup(groupId: String): Boolean {
        return _uiState.value.groupsWithManagePermission.contains(groupId)
    }

    fun toggleFilterMenu() {
        _uiState.update { it.copy(isFilterMenuOpen = !it.isFilterMenuOpen) }
    }

    fun closeFilterMenu() {
        _uiState.update { it.copy(isFilterMenuOpen = false) }
    }

    fun clearAllFilters() {
        _uiState.update {
            it.copy(
                selectedGroupFilter = GroupFilter.ALL_GROUPS,
                selectedGroupId = null,
                filteredTasks = it.tasks,
                isFilterMenuOpen = false
            )
        }
    }
}

data class GroupTasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val currentUserId: String = "",
    val groups: List<Group> = emptyList(),
    val managedGroups: List<Group> = emptyList(),
    val memberGroups: List<Group> = emptyList(),
    val groupMembers: Map<String, List<User>> = emptyMap(),
    val showAddTaskDialog: Boolean = false,
    val isAddingTask: Boolean = false,
    val selectedGroupId: String? = null,
    val selectedGroupFilter: GroupFilter = GroupFilter.ALL_GROUPS,
    val errorMessage: String? = null,
    val groupsWithManagePermission: List<String> = emptyList(),
    val isFilterMenuOpen: Boolean = false
)
