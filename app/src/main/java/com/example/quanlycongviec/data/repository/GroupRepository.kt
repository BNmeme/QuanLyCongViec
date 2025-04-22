package com.example.quanlycongviec.data.repository

import com.example.quanlycongviec.domain.model.Group
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GroupRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    
    suspend fun createGroup(group: Group): String {
        val documentRef = firestore.collection("groups").document()
        firestore.collection("groups").document(documentRef.id).set(group).await()
        return documentRef.id
    }
    
    suspend fun getGroupById(groupId: String): Group {
        val document = firestore.collection("groups").document(groupId).get().await()
        return document.toObject(Group::class.java)?.copy(id = document.id)
            ?: throw IllegalStateException("Group not found")
    }
    
    suspend fun getGroupsForUser(userId: String): List<Group> {
        val querySnapshot = firestore.collection("groups")
            .whereArrayContains("members", userId)
            .get()
            .await()
            
        return querySnapshot.documents.mapNotNull { document ->
            document.toObject(Group::class.java)?.copy(id = document.id)
        }
    }
    
    suspend fun updateGroup(group: Group) {
        firestore.collection("groups").document(group.id).set(group).await()
    }
    
    suspend fun deleteGroup(groupId: String) {
        firestore.collection("groups").document(groupId).delete().await()
    }
    
    suspend fun addMemberToGroup(groupId: String, userId: String) {
        val group = getGroupById(groupId)
        if (!group.members.contains(userId)) {
            val updatedMembers = group.members + userId
            firestore.collection("groups").document(groupId)
                .update("members", updatedMembers)
                .await()
        }
    }
    
    suspend fun removeMemberFromGroup(groupId: String, userId: String) {
        val group = getGroupById(groupId)
        if (group.members.contains(userId)) {
            val updatedMembers = group.members.filter { it != userId }
            firestore.collection("groups").document(groupId)
                .update("members", updatedMembers)
                .await()
        }
    }
}
