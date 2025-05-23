package com.example.quanlycongviec.ui.screens.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quanlycongviec.data.repository.AuthRepository
import com.example.quanlycongviec.data.repository.GroupRepository
import com.example.quanlycongviec.data.repository.NotificationRepository
import com.example.quanlycongviec.data.repository.UserRepository
import com.example.quanlycongviec.di.AppModule
import com.example.quanlycongviec.domain.model.Notification
import com.example.quanlycongviec.ui.navigation.Screen
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

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val currentUserId = authRepository.getCurrentUserId()
                Log.d("NotificationsViewModel", "Current user ID: $currentUserId")

                if (currentUserId != null) {
                    val notifications = notificationRepository.getNotificationsForUser(currentUserId)
                    Log.d("NotificationsViewModel", "Loaded ${notifications.size} notifications")

                    // Sort notifications by timestamp (newest first)
                    val sortedNotifications = notifications.sortedByDescending { it.timestamp }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = sortedNotifications,
                            filteredNotifications = sortedNotifications,
                            filterType = NotificationFilterType.ALL
                        )
                    }
                } else {
                    Log.e("NotificationsViewModel", "Current user ID is null")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "User not logged in"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Error loading notifications: ${e.message}", e)
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
                    it.copy(
                        notifications = updatedNotifications,
                        filteredNotifications = filterNotificationsByType(updatedNotifications, it.filterType)
                    )
                }

                Log.d("NotificationsViewModel", "Marked notification $notificationId as read")
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Error marking notification as read: ${e.message}", e)
            }
        }
    }

    fun markInvitationAsResponded(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markInvitationAsResponded(notificationId)

                // Update local state
                val updatedNotifications = _uiState.value.notifications.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true, isResponded = true)
                    } else {
                        notification
                    }
                }

                _uiState.update {
                    it.copy(
                        notifications = updatedNotifications,
                        filteredNotifications = filterNotificationsByType(updatedNotifications, it.filterType)
                    )
                }

                Log.d("NotificationsViewModel", "Marked invitation $notificationId as responded")
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Error marking invitation as responded: ${e.message}", e)
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                notificationRepository.deleteNotification(notificationId)

                // Update local state
                val updatedNotifications = _uiState.value.notifications.filter { it.id != notificationId }

                _uiState.update {
                    it.copy(
                        notifications = updatedNotifications,
                        filteredNotifications = filterNotificationsByType(updatedNotifications, it.filterType)
                    )
                }

                Log.d("NotificationsViewModel", "Deleted notification $notificationId")
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Error deleting notification: ${e.message}", e)
                _uiState.update {
                    it.copy(errorMessage = "Failed to delete notification: ${e.message}")
                }
            }
        }
    }

    // For testing purposes
    fun createTestNotification() {
        viewModelScope.launch {
            try {
                val currentUserId = authRepository.getCurrentUserId() ?: return@launch

                notificationRepository.createTaskDeadlineNotification(
                    userId = currentUserId,
                    taskTitle = "Test Task",
                    taskId = "test-task-id",
                    dueDate = System.currentTimeMillis() + 86400000 // 1 day from now
                )

                Log.d("NotificationsViewModel", "Created test notification")

                // Reload notifications
                loadNotifications()
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Error creating test notification: ${e.message}", e)
            }
        }
    }

    fun filterNotifications(filterType: NotificationFilterType) {
        val allNotifications = _uiState.value.notifications
        val filtered = filterNotificationsByType(allNotifications, filterType)

        _uiState.update {
            it.copy(
                filteredNotifications = filtered,
                filterType = filterType
            )
        }
    }

    private fun filterNotificationsByType(notifications: List<Notification>, filterType: NotificationFilterType): List<Notification> {
        return when (filterType) {
            NotificationFilterType.ALL -> notifications
            NotificationFilterType.UNREAD -> notifications.filter { !it.isRead }
            NotificationFilterType.TASK -> notifications.filter {
                it.type == NotificationType.TASK_ASSIGNED ||
                        it.type == NotificationType.TASK_DEADLINE ||
                        it.type == NotificationType.TASK_COMPLETED
            }
            NotificationFilterType.GROUP -> notifications.filter {
                it.type == NotificationType.GROUP_INVITATION ||
                        it.type == NotificationType.GROUP_INVITATION_ACCEPTED ||
                        it.type == NotificationType.GROUP_INVITATION_DECLINED
            }
        }
    }
}

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val filteredNotifications: List<Notification> = emptyList(),
    val filterType: NotificationFilterType = NotificationFilterType.ALL,
    val errorMessage: String? = null
)

enum class NotificationFilterType {
    ALL, UNREAD, TASK, GROUP
}
