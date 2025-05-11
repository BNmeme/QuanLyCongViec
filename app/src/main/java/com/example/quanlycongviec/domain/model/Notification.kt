package com.example.quanlycongviec.domain.model

import com.example.quanlycongviec.ui.screens.notifications.NotificationType

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0L,
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.TASK_ASSIGNED,
    val relatedTaskId: String? = null,
    val relatedGroupId: String? = null,
    val isResponded: Boolean = false  // New field to track if an invitation has been responded to
)
