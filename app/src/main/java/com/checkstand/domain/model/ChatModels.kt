package com.checkstand.domain.model

/**
 * Domain model for chat messages
 */
data class ChatMessage(
    val id: String = generateId(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT
) {
    companion object {
        private fun generateId(): String = System.currentTimeMillis().toString()
    }
}

enum class MessageStatus {
    SENDING,
    SENT,
    ERROR
}

/**
 * Represents the state of a chat conversation
 */
data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val currentInput: String = "",
    val error: String? = null
)

/**
 * Represents the model loading state
 */
data class ModelState(
    val isAvailable: Boolean = false,
    val isLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val error: String? = null,
    val modelInfo: ModelInfo? = null
)

data class ModelInfo(
    val name: String,
    val version: String,
    val size: Long,
    val path: String
)
