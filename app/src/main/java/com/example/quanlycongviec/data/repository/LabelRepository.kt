package com.example.quanlycongviec.data.repository

import com.example.quanlycongviec.domain.model.Label
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class LabelRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    
    suspend fun createLabel(label: Label): String {
        val documentRef = firestore.collection("labels").document()
        val labelWithId = label.copy(id = documentRef.id)
        documentRef.set(labelWithId).await()
        return documentRef.id
    }
    
    suspend fun getLabelsForUser(userId: String): List<Label> {
        val querySnapshot = firestore.collection("labels")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            
        return querySnapshot.documents.mapNotNull { document ->
            document.toObject(Label::class.java)
        }
    }
    
    suspend fun getLabelsForGroup(groupId: String): List<Label> {
        val querySnapshot = firestore.collection("labels")
            .whereEqualTo("groupId", groupId)
            .whereEqualTo("isShared", true)
            .get()
            .await()
            
        return querySnapshot.documents.mapNotNull { document ->
            document.toObject(Label::class.java)
        }
    }
    
    suspend fun updateLabel(label: Label) {
        firestore.collection("labels").document(label.id)
            .set(label)
            .await()
    }
    
    suspend fun deleteLabel(labelId: String) {
        firestore.collection("labels").document(labelId)
            .delete()
            .await()
    }
    
    suspend fun getLabelsByIds(labelIds: List<String>): List<Label> {
        if (labelIds.isEmpty()) return emptyList()
        
        val labels = mutableListOf<Label>()
        
        for (labelId in labelIds) {
            val documentSnapshot = firestore.collection("labels").document(labelId)
                .get()
                .await()
                
            documentSnapshot.toObject(Label::class.java)?.let {
                labels.add(it)
            }
        }
        
        return labels
    }
}
