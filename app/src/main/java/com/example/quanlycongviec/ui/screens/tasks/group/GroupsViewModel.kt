package com.example.quanlycongviec.ui.screens.tasks.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.GroupRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Group
import com.example.quanlycongviec.domain.model.GroupRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupsViewModel : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    private val groupRepository = AppModule.provideGroupRepository()

    private val _uiState = MutableStateFlow(GroupsUiState())
    val uiState: StateFlow<GroupsUiState> = _uiState.asStateFlow()

    private var allGroups: List<Group> = emptyList()
    private val currentUserId: String? get() = authRepository.getCurrentUserId()

    init {
        loadGroups()
    }

    private fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val userId = currentUserId
                if (userId != null) {
                    allGroups = groupRepository.getGroupsForUser(userId)
                    applyFilters()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load groups: ${e.message}"
                    )
                }
            }
        }
    }

    fun setRoleFilter(filter: RoleFilter) {
        _uiState.update { it.copy(roleFilter = filter) }
        applyFilters()
    }

    private fun applyFilters() {
        val userId = currentUserId ?: return
        val filteredGroups = when (_uiState.value.roleFilter) {
            RoleFilter.ALL -> allGroups
            RoleFilter.MANAGED -> allGroups.filter { group ->
                val userRole = group.getMemberRole(userId)
                userRole == GroupRole.LEADER || userRole == GroupRole.DEPUTY
            }
            RoleFilter.MEMBER_ONLY -> allGroups.filter { group ->
                group.getMemberRole(userId) == GroupRole.MEMBER
            }
        }

        _uiState.update {
            it.copy(
                isLoading = false,
                groups = filteredGroups
            )
        }
    }
}

data class GroupsUiState(
    val isLoading: Boolean = false,
    val groups: List<Group> = emptyList(),
    val errorMessage: String? = null,
    val roleFilter: RoleFilter = RoleFilter.ALL
)

enum class RoleFilter {
    ALL, MANAGED, MEMBER_ONLY
}
