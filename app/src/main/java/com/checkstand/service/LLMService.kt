package com.checkstand.service

import android.content.Context
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
    
    suspend fun initializeModel(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!modelManager.isModelAvailable()) {
                    return@withContext false
                }
                
                val modelFile = modelManager.getModelFile()
                
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(1024)
                    .setTopK(40)
                    .setTemperature(0.8f)
                    .setRandomSeed(101)
                    .build()
                
                llmInference = LlmInference.createFromOptions(context, options)
                isModelLoaded = true
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    fun generateResponse(prompt: String): Flow<String> = flow {
        if (!isModelLoaded || llmInference == null) {
            emit("Error: Model not loaded")
            return@flow
        }
        
        try {
            val fullPrompt = "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"
            
            llmInference?.let { inference ->
                val response = inference.generateResponse(fullPrompt)
                emit(response)
            }
        } catch (e: Exception) {
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
