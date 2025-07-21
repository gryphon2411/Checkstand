package com.checkstand.data.repository

import android.content.Context
import android.util.Log
import com.checkstand.domain.repository.LLMRepository
import com.checkstand.domain.repository.ModelInfo
import com.checkstand.service.LLMService
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of LLMRepository using MediaPipe LLM Service
 */
class MediaPipeLLMRepository(
    private val context: Context
) : LLMRepository {

    private val llmService = LLMService(context)
    
    companion object {
        private const val TAG = "MediaPipeLLMRepository"
        private const val MODEL_NAME = "Gemma-3n E4B"
        private const val MODEL_VERSION = "int4"
    }

    override fun isModelAvailable(): Boolean {
        return llmService.isModelAvailable()
    }

    override fun isModelReady(): Boolean {
        return llmService.isModelReady()
    }

    override suspend fun initializeModel(): Boolean {
        Log.d(TAG, "Initializing model...")
        return try {
            llmService.initializeModel()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize model", e)
            false
        }
    }

    override fun generateResponse(prompt: String): Flow<String> {
        Log.d(TAG, "Generating response for prompt: ${prompt.take(50)}...")
        return llmService.generateResponse(prompt)
    }

    override suspend fun downloadModel(onProgress: (Int) -> Unit): Boolean {
        Log.d(TAG, "Starting model download...")
        return try {
            llmService.getModelManager().downloadModel(onProgress)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download model", e)
            false
        }
    }

    override fun getModelInfo(): ModelInfo {
        val modelManager = llmService.getModelManager()
        val modelFile = if (modelManager.isModelAvailable()) {
            modelManager.getModelFile()
        } else null
        
        return ModelInfo(
            name = MODEL_NAME,
            size = modelFile?.length() ?: 0L,
            path = modelFile?.absolutePath ?: "Not available",
            isLoaded = llmService.isModelReady()
        )
    }

    override fun cleanup() {
        Log.d(TAG, "Cleaning up repository resources...")
        llmService.cleanup()
    }
}
