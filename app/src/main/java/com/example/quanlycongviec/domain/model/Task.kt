package com.example.quanlycongviec.domain.model

import com.google.firebase.firestore.PropertyName

data class Task(
    @get:PropertyName("id")
    val id: String = "",
    val title: String = "",
    val description: String = "",

    @get:PropertyName("isCompleted")
    val isCompleted: Boolean = false,
    val createdAt: Long = 0,
    val dueDate: Long = 0,
    val priority: Int = 3, // 1: High, 2: Medium, 3: Low
    val userId: String = "",

    @get:PropertyName("isGroupTask")
    @set:PropertyName("isGroupTask")
    var isGroupTask: Boolean = false,

    val groupId: String = "",
    val assignedTo: List<String> = emptyList(),
    val labels: List<String> = emptyList(),

    // Map of user IDs to their completion status
    @get:PropertyName("completionConfirmations")
    val completionConfirmations: Map<String, Boolean> = emptyMap()
) {
    fun hasUserConfirmedCompletion(userId: String): Boolean {
        return completionConfirmations[userId] == true
    }

    fun allAssignedMembersConfirmed(): Boolean {
        if (assignedTo.isEmpty()) return false
        return assignedTo.all { userId -> completionConfirmations[userId] == true }
    }

    fun getConfirmationCount(): Int {
        return completionConfirmations.count { it.value }
    }
}
