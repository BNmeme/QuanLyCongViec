package com.example.quanlycongviec.ui.screens.tasks.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.GroupRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Group
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
    
    init {
        loadGroups()
    }
    
    private fun loadGroups() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    val groups = groupRepository.getGroupsForUser(currentUserId)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            groups = groups
                        ) 
                    }
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
}

data class GroupsUiState(
    val isLoading: Boolean = false,
    val groups: List<Group> = emptyList(),
    val errorMessage: String? = null
)
