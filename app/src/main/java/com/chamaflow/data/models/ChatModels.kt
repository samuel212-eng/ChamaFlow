package com.chamaflow.data.models

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isSystemMessage: Boolean = false
)
