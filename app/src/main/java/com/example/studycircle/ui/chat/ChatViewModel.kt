package com.example.studycircle.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycircle.data.repository.ChatRepository
import com.example.studycircle.domain.model.ChatRoom
import com.example.studycircle.domain.model.Message
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class ChatListUiState(
    val rooms: List<ChatRoom> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class ChatRoomUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _chatListState = MutableStateFlow(ChatListUiState())
    val chatListState: StateFlow<ChatListUiState> = _chatListState

    private val _chatRoomState = MutableStateFlow(ChatRoomUiState())
    val chatRoomState: StateFlow<ChatRoomUiState> = _chatRoomState

    private val auth = FirebaseAuth.getInstance()

    init {
        initializeRooms()
        loadChatRooms()
    }

    private fun initializeRooms() {
        viewModelScope.launch {
            repository.initializeDefaultRooms()
        }
    }

    fun loadChatRooms() {
        viewModelScope.launch {
            repository.getChatRooms()
                .catch { e ->
                    _chatListState.value = _chatListState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { rooms ->
                    _chatListState.value = _chatListState.value.copy(
                        rooms = rooms.sortedByDescending { it.lastMessageTime },
                        isLoading = false
                    )
                }
        }
    }

    fun loadMessages(roomId: String) {
        viewModelScope.launch {
            _chatRoomState.value = ChatRoomUiState(isLoading = true)
            repository.getMessages(roomId)
                .catch { e ->
                    _chatRoomState.value = _chatRoomState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { messages ->
                    _chatRoomState.value = _chatRoomState.value.copy(
                        messages = messages,
                        isLoading = false
                    )
                }
        }
    }

    fun sendMessage(roomId: String, text: String) {
        if (text.isBlank()) return
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            _chatRoomState.value = _chatRoomState.value.copy(isSending = true)
            val message = Message(
                senderId = currentUser.uid,
                senderName = currentUser.displayName
                    ?: currentUser.email?.substringBefore("@")
                    ?: "Anonymous",
                text = text.trim(),
                timestamp = System.currentTimeMillis()
            )
            repository.sendMessage(roomId, message)
            _chatRoomState.value = _chatRoomState.value.copy(isSending = false)
        }
    }
}