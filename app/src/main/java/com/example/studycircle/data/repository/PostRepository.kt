package com.example.studycircle.data.repository

import com.example.studycircle.domain.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PostRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // Real-time feed using Flow
    fun getFeedPosts(subject: String? = null): Flow<List<Post>> = callbackFlow {
        val query = if (subject != null && subject != "All") {
            firestore.collection("posts")
                .whereEqualTo("subject", subject)
                .orderBy("timestamp", Query.Direction.DESCENDING)
        } else {
            firestore.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
        }

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            val posts = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Post::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(posts)
        }
        awaitClose { listener.remove() }
    }

    suspend fun createPost(post: Post): Boolean {
        return try {
            val docRef = firestore.collection("posts").document()
            firestore.collection("posts").document(docRef.id)
                .set(post.copy(id = docRef.id)).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun likePost(postId: String): Boolean {
        return try {
            val ref = firestore.collection("posts").document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(ref)
                val currentLikes = snapshot.getLong("likes") ?: 0
                transaction.update(ref, "likes", currentLikes + 1)
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }
}