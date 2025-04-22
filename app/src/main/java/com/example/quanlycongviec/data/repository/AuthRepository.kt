package com.example.quanlycongviec.data.repository

import com.example.quanlycongviec.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    
    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }
    
    suspend fun signUp(email: String, password: String, name: String) {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val userId = authResult.user?.uid ?: throw IllegalStateException("User ID is null")
        
        // Create user document in Firestore
        val user = User(
            id = userId,
            name = name,
            email = email
        )
        
        firestore.collection("users").document(userId).set(user).await()
    }
    
    suspend fun signOut() {
        auth.signOut()
    }
    
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}
