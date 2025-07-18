package com.checkstand.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.checkstand.service.LLMService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isModelLoaded: Boolean = false,
    val currentInput: String = "",
    val error: String? = null,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val isModelAvailable: Boolean = false
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    
    private val llmService = LLMService(application)
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    init {
        checkModelAvailability()
    }
    
    private fun checkModelAvailability() {
        val isAvailable = llmService.isModelAvailable()
        _uiState.value = _uiState.value.copy(isModelAvailable = isAvailable)
        
        if (isAvailable) {
            initializeModel()
        }
    }
    
    fun downloadModel() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloading = true, 
                downloadProgress = 0,
                error = null
            )
            
            val success = llmService.getModelManager().downloadModel { progress ->
                _uiState.value = _uiState.value.copy(downloadProgress = progress)
            }
            
            _uiState.value = _uiState.value.copy(
                isDownloading = false,
                isModelAvailable = success,
                error = if (success) null else "Failed to download model"
            )
            
            if (success) {
                initializeModel()
            }
        }
    }
    
    private fun initializeModel() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val success = llmService.initializeModel()
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                isModelLoaded = success,
                error = if (success) null else "Failed to load model. Please ensure the model file is available."
            )
        }
    }
    
    fun sendMessage(message: String) {
        if (message.isBlank() || !llmService.isModelReady()) return
        
        // Add user message
        val userMessage = ChatMessage(text = message, isUser = true)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            currentInput = "",
            isLoading = true
        )
        
        // Generate AI response
        viewModelScope.launch {
            llmService.generateResponse(message).collect { response ->
                val aiMessage = ChatMessage(text = response, isUser = false)
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    isLoading = false
                )
            }
        }
    }
    
    fun updateInput(input: String) {
        _uiState.value = _uiState.value.copy(currentInput = input)
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(messages = emptyList())
    }
    
    override fun onCleared() {
        super.onCleared()
        llmService.cleanup()
    }
}
