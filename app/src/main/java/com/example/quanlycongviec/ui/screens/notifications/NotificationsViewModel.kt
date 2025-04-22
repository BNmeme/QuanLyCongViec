package com.example.quanlycongviec.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.NotificationRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {
    private val authRepository = AppModule.provideAuthRepository()
    private val notificationRepository = AppModule.provideNotificationRepository()
    
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()
    
    init {
        loadNotifications()
    }
    
    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    val notifications = notificationRepository.getNotificationsForUser(currentUserId)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            notifications = notifications
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load notifications: ${e.message}"
                    ) 
                }
            }
        }
    }
    
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markNotificationAsRead(notificationId)
                
                // Update local state
                val updatedNotifications = _uiState.value.notifications.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true)
                    } else {
                        notification
                    }
                }
                
                _uiState.update { 
                    it.copy(notifications = updatedNotifications) 
                }
                
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
}

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val errorMessage: String? = null
)
