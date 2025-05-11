package com.example.quanlycongviec.domain.model

data class UserPreferences(
    val darkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val reminderTimeBeforeDeadline: Int = 60 // minutes before deadline
)
