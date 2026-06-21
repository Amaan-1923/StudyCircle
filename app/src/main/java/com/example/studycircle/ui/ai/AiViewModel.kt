package com.example.studycircle.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studycircle.data.repository.AiRepository
import com.example.studycircle.data.repository.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AiUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "Hi! I'm StudyBot 🤖\nAsk me anything — concepts, problems, definitions, or exam tips. I'm here to help you study smarter!",
            isFromUser = false
        )
    ),
    val isLoading: Boolean = false,
    val currentStreamText: String = ""
)

class AiViewModel(
    private val repository: AiRepository = AiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiUiState())
    val uiState: StateFlow<AiUiState> = _uiState

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        // Add user message immediately
        val userMessage = ChatMessage(text = text, isFromUser = true)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isLoading = true,
            currentStreamText = ""
        )

        viewModelScope.launch {
            var streamedText = ""

            // Stream the response token by token
            repository.sendMessageStream(text).collect { chunk ->
                streamedText += chunk
                _uiState.value = _uiState.value.copy(
                    currentStreamText = streamedText,
                    isLoading = false
                )
            }

            // Once streaming is done, add the full message to chat
            if (streamedText.isNotEmpty()) {
                val aiMessage = ChatMessage(text = streamedText, isFromUser = false)
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    currentStreamText = "",
                    isLoading = false
                )
            }
        }
    }

    fun clearChat() {
        repository.clearHistory()
        _uiState.value = AiUiState()
    }
}