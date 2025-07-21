package com.checkstand.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for LLM operations.
 * Provides an abstraction layer between the ViewModel and the actual LLM implementation.
 */
interface LLMRepository {
    
    /**
     * Checks if the model file is available on the device
     */
    fun isModelAvailable(): Boolean
    
    /**
     * Checks if the model is currently loaded and ready for inference
     */
    fun isModelReady(): Boolean
    
    /**
     * Initializes and loads the LLM model
     * @return true if successful, false otherwise
     */
    suspend fun initializeModel(): Boolean
    
    /**
     * Generates a response for the given prompt
     * @param prompt The user input prompt
     * @return Flow of response text (for streaming responses)
     */
    fun generateResponse(prompt: String): Flow<String>
    
    /**
     * Downloads the model if not available
     * @param onProgress Callback for download progress (0-100)
     * @return true if successful, false otherwise
     */
    suspend fun downloadModel(onProgress: (Int) -> Unit): Boolean
    
    /**
     * Gets information about the current model
     */
    fun getModelInfo(): ModelInfo
    
    /**
     * Cleans up resources
     */
    fun cleanup()
}

/**
 * Information about the current model
 */
data class ModelInfo(
    val name: String,
    val size: Long,
    val path: String,
    val isLoaded: Boolean
)
