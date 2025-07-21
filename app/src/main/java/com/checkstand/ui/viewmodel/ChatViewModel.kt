package com.checkstand.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkstand.domain.model.ChatMessage
import com.checkstand.domain.model.ChatState
import com.checkstand.domain.model.ModelState
import com.checkstand.domain.repository.LLMRepository
import com.checkstand.domain.usecase.CheckModelStatusUseCase
import com.checkstand.domain.usecase.DownloadModelUseCase
import com.checkstand.domain.usecase.InitializeModelUseCase
import com.checkstand.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Combined UI state for the chat screen
 */
data class ChatUiState(
    val chatState: ChatState = ChatState(),
    val modelState: ModelState = ModelState()
) {
    // Convenience properties for easier access
    val messages get() = chatState.messages
    val isGenerating get() = chatState.isGenerating
    val currentInput get() = chatState.currentInput
    val error get() = chatState.error ?: modelState.error
    val isModelLoaded get() = modelState.isLoaded
    val isModelAvailable get() = modelState.isAvailable
    val isLoading get() = modelState.isLoading
    val isDownloading get() = modelState.isDownloading
    val downloadProgress get() = modelState.downloadProgress
}

/**
 * ViewModel for chat functionality following clean architecture principles
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: LLMRepository
) : ViewModel() {
    
    // Use cases
    private val sendMessageUseCase = SendMessageUseCase(repository)
    private val initializeModelUseCase = InitializeModelUseCase(repository)
    private val checkModelStatusUseCase = CheckModelStatusUseCase(repository)
    private val downloadModelUseCase = DownloadModelUseCase(repository)
    
    // Internal state flows
    private val _chatState = MutableStateFlow(ChatState())
    private val _modelState = MutableStateFlow(ModelState())
    
    // Combined UI state
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    init {
        // Combine internal states into UI state
        viewModelScope.launch {
            combine(_chatState, _modelState) { chatState, modelState ->
                ChatUiState(chatState, modelState)
            }.collect { combinedState ->
                _uiState.value = combinedState
            }
        }
        
        // Check initial model status
        checkModelStatus()
    }
    
    /**
     * Checks the current model status and updates state accordingly
     */
    private fun checkModelStatus() {
        val status = checkModelStatusUseCase()
        _modelState.value = _modelState.value.copy(
            isAvailable = status.isAvailable,
            isLoaded = status.isReady,
            modelInfo = com.checkstand.domain.model.ModelInfo(
                name = status.modelInfo.name,
                version = "int4",
                size = status.modelInfo.size,
                path = status.modelInfo.path
            )
        )
        
        // Auto-initialize if model is available but not loaded
        if (status.isAvailable && !status.isReady) {
            initializeModel()
        }
    }
    
    /**
     * Initializes the LLM model
     */
    fun initializeModel() {
        viewModelScope.launch {
            _modelState.value = _modelState.value.copy(
                isLoading = true, 
                error = null
            )
            
            initializeModelUseCase().fold(
                onSuccess = {
                    _modelState.value = _modelState.value.copy(
                        isLoading = false,
                        isLoaded = true,
                        error = null
                    )
                },
                onFailure = { error ->
                    _modelState.value = _modelState.value.copy(
                        isLoading = false,
                        isLoaded = false,
                        error = "Failed to load model: ${error.message}"
                    )
                }
            )
        }
    }
    
    /**
     * Downloads the model if not available
     */
    fun downloadModel() {
        viewModelScope.launch {
            _modelState.value = _modelState.value.copy(
                isDownloading = true,
                downloadProgress = 0,
                error = null
            )
            
            downloadModelUseCase { progress ->
                _modelState.value = _modelState.value.copy(downloadProgress = progress)
            }.fold(
                onSuccess = {
                    _modelState.value = _modelState.value.copy(
                        isDownloading = false,
                        isAvailable = true,
                        error = null
                    )
                    // Auto-initialize after successful download
                    initializeModel()
                },
                onFailure = { error ->
                    _modelState.value = _modelState.value.copy(
                        isDownloading = false,
                        error = "Failed to download model: ${error.message}"
                    )
                }
            )
        }
    }
    
    /**
     * Sends a message and gets AI response
     */
    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) return
        
        // Add user message immediately
        val userMessage = ChatMessage(
            text = messageText.trim(),
            isUser = true
        )
        
        _chatState.value = _chatState.value.copy(
            messages = _chatState.value.messages + userMessage,
            currentInput = "",
            isGenerating = true,
            error = null
        )
        
        // Generate AI response
        viewModelScope.launch {
            sendMessageUseCase(messageText.trim()).collect { result ->
                result.fold(
                    onSuccess = { response ->
                        val aiMessage = ChatMessage(
                            text = response,
                            isUser = false
                        )
                        _chatState.value = _chatState.value.copy(
                            messages = _chatState.value.messages + aiMessage,
                            isGenerating = false
                        )
                    },
                    onFailure = { error ->
                        _chatState.value = _chatState.value.copy(
                            isGenerating = false,
                            error = "Failed to generate response: ${error.message}"
                        )
                    }
                )
            }
        }
    }
    
    /**
     * Updates the current input text
     */
    fun updateInput(input: String) {
        _chatState.value = _chatState.value.copy(currentInput = input)
    }
    
    /**
     * Clears all messages
     */
    fun clearMessages() {
        _chatState.value = _chatState.value.copy(messages = emptyList())
    }
    
    /**
     * Clears any error state
     */
    fun clearError() {
        _chatState.value = _chatState.value.copy(error = null)
        _modelState.value = _modelState.value.copy(error = null)
    }
    
    override fun onCleared() {
        super.onCleared()
        repository.cleanup()
    }
}
