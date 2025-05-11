package com.example.quanlycongviec.domain.model

data class Group(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val createdBy: String = "",
    val members: List<String> = emptyList(),
    val memberRoles: Map<String, GroupRole> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val pendingInvitations: List<String> = emptyList()
) {
    fun getMemberRole(userId: String): GroupRole {
        return if (userId == createdBy) {
            GroupRole.LEADER
        } else {
            memberRoles[userId] ?: GroupRole.MEMBER
        }
    }

    fun canManageTasks(userId: String): Boolean {
        val role = getMemberRole(userId)
        return role == GroupRole.LEADER || role == GroupRole.DEPUTY
    }

    fun canManageMembers(userId: String): Boolean {
        return userId == createdBy
    }

    fun canManageRoles(userId: String): Boolean {
        return userId == createdBy
    }

    fun isUserLeader(userId: String): Boolean {
        return getMemberRole(userId) == GroupRole.LEADER
    }

    fun isUserDeputy(userId: String): Boolean {
        return getMemberRole(userId) == GroupRole.DEPUTY
    }

    fun isUserAdmin(userId: String): Boolean {
        return isUserLeader(userId) || isUserDeputy(userId)
    }
}

enum class GroupRole {
    LEADER,    // Group creator, has all permissions
    DEPUTY,    // Can manage tasks but not members or group settings
    MEMBER     // Regular member, can only view and complete tasks
}
