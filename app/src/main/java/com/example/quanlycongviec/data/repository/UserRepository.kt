package com.example.quanlycongviec.data.repository

import com.example.quanlycongviec.domain.model.User
import com.example.quanlycongviec.domain.model.UserPreferences
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun getUserById(userId: String): User {
        val document = firestore.collection("users").document(userId).get().await()
        return document.toObject(User::class.java) ?: throw IllegalStateException("User not found")
    }

    suspend fun updateUser(user: User) {
        firestore.collection("users").document(user.id).set(user).await()
    }

    suspend fun updateUserPreference(userId: String, preferenceName: String, value: Any) {
        val userDoc = firestore.collection("users").document(userId).get().await()
        val user = userDoc.toObject(User::class.java) ?: throw IllegalStateException("User not found")

        // Create preferences object if it doesn't exist
        val preferences = user.preferences ?: UserPreferences()

        // Update the specific preference
        val updatedPreferences = when (preferenceName) {
            "darkModeEnabled" -> preferences.copy(darkModeEnabled = value as Boolean)
            "notificationsEnabled" -> preferences.copy(notificationsEnabled = value as Boolean)
            "reminderTimeBeforeDeadline" -> preferences.copy(reminderTimeBeforeDeadline = value as Int)
            else -> preferences
        }

        // Update the user with new preferences
        val updatedUser = user.copy(preferences = updatedPreferences)
        firestore.collection("users").document(userId).set(updatedUser).await()
    }

    suspend fun getUsersByIds(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()

        val users = mutableListOf<User>()
        for (userId in userIds) {
            try {
                val user = getUserById(userId)
                users.add(user)
            } catch (e: Exception) {
                // Skip users that couldn't be fetched
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
}
