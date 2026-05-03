package com.chamaflow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chamaflow.data.models.ChatMessage
import com.chamaflow.data.repository.AuthResult
import com.chamaflow.data.repository.ChatRepository
import com.chamaflow.data.repository.FirestoreResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository
) : ViewModel() {
    private val _ui = MutableStateFlow(ChatUiState())
    val uiState = _ui.asStateFlow()

    fun loadMessages(chamaId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            repo.getMessagesFlow(chamaId)
                .catch { e -> _ui.update { it.copy(isLoading = false, errorMessage = e.message) } }
                .collect { list -> _ui.update { it.copy(messages = list, isLoading = false) } }
        }
    }

    fun sendMessage(chamaId: String, senderId: String, senderName: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val message = ChatMessage(
                senderId = senderId,
                senderName = senderName,
                message = text
            )
            repo.sendMessage(chamaId, message)
        }
    }
}
