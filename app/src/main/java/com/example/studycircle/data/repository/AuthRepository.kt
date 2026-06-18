package com.example.studycircle.data.repository

import com.example.studycircle.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return AuthResult.Error("Login failed. Try again.")

            // Fetch user profile from Firestore
            val doc = firestore.collection("users").document(firebaseUser.uid).get().await()
            val user = if (doc.exists()) {
                doc.toObject(User::class.java) ?: User(uid = firebaseUser.uid, email = email)
            } else {
                User(uid = firebaseUser.uid, email = email)
            }

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e.message))
        }
    }

    suspend fun register(name: String, email: String, password: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return AuthResult.Error("Registration failed. Try again.")

            val newUser = User(
                uid = firebaseUser.uid,
                name = name,
                email = email
            )

            // Save user profile to Firestore
            firestore.collection("users").document(firebaseUser.uid).set(newUser).await()

            AuthResult.Success(newUser)
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e.message))
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    private fun mapFirebaseError(message: String?): String {
        return when {
            message == null -> "Something went wrong. Please try again."
            message.contains("badly formatted") -> "Please enter a valid email address."
            message.contains("no user record") -> "No account found with this email."
            message.contains("password is invalid") -> "Incorrect password."
            message.contains("already in use") -> "An account already exists with this email."
            message.contains("network error") -> "Network error. Check your connection."
            message.contains("weak password") -> "Password is too weak. Use at least 6 characters."
            else -> "Authentication failed. Please try again."
        }
    }
}