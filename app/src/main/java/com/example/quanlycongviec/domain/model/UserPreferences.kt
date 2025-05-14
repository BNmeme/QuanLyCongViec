package com.example.quanlycongviec.domain.model
import com.google.firebase.firestore.PropertyName
data class UserPreferences(
    @get:PropertyName("darkModeEnabled")
    val darkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val reminderTimeBeforeDeadline: Int = 60 // minutes before deadline
)
