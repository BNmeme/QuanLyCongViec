package com.example.quanlycongviec.domain.model

import com.example.quanlycongviec.ui.screens.notifications.NotificationType

data class Notification(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val userId: String = "",
    val type: NotificationType = NotificationType.TASK_ASSIGNED,
    val relatedTaskId: String? = null,
    val relatedGroupId: String? = null
)
