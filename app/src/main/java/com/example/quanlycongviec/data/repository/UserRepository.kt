package com.example.quanlycongviec.data.repository

import com.example.quanlycongviec.domain.model.User
import com.example.quanlycongviec.domain.model.UserPreferences
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getUserById(userId: String): User? {
        val document = firestore.collection("users").document(userId).get().await()
        return if (document.exists()) {
            document.toObject(User::class.java)?.copy(id = document.id)
        } else {
            null
        }
    }

    suspend fun getUsersByIds(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()

        val users = mutableListOf<User>()
        for (userId in userIds) {
            val user = getUserById(userId)
            if (user != null) {
                users.add(user)
            }
        }
        return users
    }

    suspend fun getUserByEmail(email: String): User? {
        val querySnapshot = firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .await()

        return if (querySnapshot.documents.isNotEmpty()) {
            val document = querySnapshot.documents.first()
            document.toObject(User::class.java)?.copy(id = document.id)
        } else {
            null
        }
    }

    suspend fun createUser(user: User): String {
        val documentRef = firestore.collection("users").document(user.id)
        firestore.collection("users").document(user.id).set(user).await()
        return documentRef.id
    }

    suspend fun updateUser(user: User) {
        firestore.collection("users").document(user.id).set(user).await()
    }

    suspend fun updateUserPreference(userId: String, preferenceName: String, value: Any) {
        val user = getUserById(userId)
        if (user != null) {
            firestore.collection("users").document(userId)
                .update("preferences.$preferenceName", value)
                .await()
        }
    }

    suspend fun updateUserPreferences(userId: String, preferences: UserPreferences) {
        firestore.collection("users").document(userId)
            .update("preferences", preferences)
            .await()
    }

    suspend fun searchUsersByEmail(email: String): List<User> {
        val querySnapshot = firestore.collection("users")
            .whereGreaterThanOrEqualTo("email", email)
            .whereLessThanOrEqualTo("email", email + "\uf8ff")
            .get()
            .await()

        return querySnapshot.documents.mapNotNull { document ->
            document.toObject(User::class.java)?.copy(id = document.id)
        }
    }

    suspend fun deleteUser(userId: String) {
        firestore.collection("users").document(userId).delete().await()
    }
}
