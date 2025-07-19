package com.checkstand.service

import android.content.Context
import android.util.Log
import com.checkstand.model.ModelManager
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class LLMService(private val context: Context) {
    
    private var llmInference: LlmInference? = null
    private var isModelLoaded = false
    private val modelManager = ModelManager(context)
    
    companion object {
        private const val TAG = "LLMService"
    }
    
    suspend fun initializeModel(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting model initialization...")
                
                if (!modelManager.isModelAvailable()) {
                    Log.e(TAG, "Model file not found at expected location")
                    return@withContext false
                }
                
                val modelFile = modelManager.getModelFile()
                Log.d(TAG, "Model file found at: ${modelFile.absolutePath}")
                Log.d(TAG, "Model file size: ${modelFile.length()} bytes")
                
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(1024)
                    .setTopK(40)
                    .setTemperature(0.8f)
                    .setRandomSeed(101)
                    .build()
                
                Log.d(TAG, "Creating LLM inference instance...")
                llmInference = LlmInference.createFromOptions(context, options)
                isModelLoaded = true
                Log.d(TAG, "Model loaded successfully!")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize model", e)
                false
            }
        }
    }
    
    fun generateResponse(prompt: String): Flow<String> = flow {
        if (!isModelLoaded || llmInference == null) {
            Log.e(TAG, "Cannot generate response: model not loaded")
            emit("Error: Model not loaded")
            return@flow
        }
        
        try {
            Log.d(TAG, "Generating response for prompt: ${prompt.take(50)}...")
            val fullPrompt = "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"
            
            llmInference?.let { inference ->
                val response = inference.generateResponse(fullPrompt)
                Log.d(TAG, "Generated response: ${response.take(100)}...")
                emit(response)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response", e)
            emit("Error: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)
    
    fun isModelReady(): Boolean = isModelLoaded
    
    fun isModelAvailable(): Boolean = modelManager.isModelAvailable()
    
    fun getModelManager(): ModelManager = modelManager
    
    fun cleanup() {
        llmInference?.close()
        llmInference = null
        isModelLoaded = false
    }
}
