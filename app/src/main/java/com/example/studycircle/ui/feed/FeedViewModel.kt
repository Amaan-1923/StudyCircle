package com.example.studycircle.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycircle.data.repository.PostRepository
import com.example.studycircle.domain.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedSubject: String = "All",
    val userPostCount: Int = 0
)

class FeedViewModel(
    private val repository: PostRepository = PostRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState: StateFlow<FeedUiState> = _uiState

    val subjects = listOf(
        "All", "Math", "Physics", "Chemistry",
        "CS", "OS", "DBMS", "DSA", "Other"
    )

    init {
        loadPosts()
        loadUserPostCount()
    }

    fun loadPosts(subject: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getFeedPosts(subject)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { posts ->
                    _uiState.value = _uiState.value.copy(
                        posts = posts,
                        isLoading = false
                    )
                }
        }
    }

    fun selectSubject(subject: String) {
        _uiState.value = _uiState.value.copy(selectedSubject = subject)
        loadPosts(if (subject == "All") null else subject)
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            repository.likePost(postId)
        }
    }

    fun createPost(post: Post, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val success = repository.createPost(post)
            if (success) {
                loadUserPostCount()
                onSuccess()
            } else {
                onError()
            }
        }
    }

    private fun loadUserPostCount() {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance()
                .currentUser?.uid ?: return@launch
            try {
                val snapshot = FirebaseFirestore.getInstance()
                    .collection("posts")
                    .whereEqualTo("authorId", currentUserId)
                    .get()
                    .await()
                _uiState.value = _uiState.value.copy(
                    userPostCount = snapshot.size()
                )
            } catch (e: Exception) {
                // keep default 0
            }
        }
    }
}