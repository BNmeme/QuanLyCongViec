package com.example.quanlycongviec.domain.model

import androidx.compose.ui.graphics.Color

data class Label(
    val id: String = "",
    val name: String = "",
    val color: String = "#4CAF50", // Default green color
    val userId: String = "",
    val isShared: Boolean = false,
    val groupId: String? = null
) {
    fun getColorValue(): Color {
        return try {
            Color(android.graphics.Color.parseColor(color))
        } catch (e: Exception) {
            Color(0xFF4CAF50) // Default to green if parsing fails
        }
    }
}
