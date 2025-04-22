package com.example.quanlycongviec.domain.model

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis(),
    val priority: Int = 3, // 1: High, 2: Medium, 3: Low
    val userId: String = "",
    val isGroupTask: Boolean = false,
    val groupId: String = "",
    val assignedTo: List<String> = emptyList()
)
