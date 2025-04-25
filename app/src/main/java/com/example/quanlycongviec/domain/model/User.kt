package com.example.quanlycongviec.domain.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val preferences: UserPreferences? = null
)

data class UserPreferences(
    val darkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val reminderTimeBeforeDeadline: Int = 60 // minutes
)
