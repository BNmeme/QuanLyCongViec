package com.example.quanlycongviec.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val photoUrl: String? = null,
    val preferences: UserPreferences = UserPreferences()
)
