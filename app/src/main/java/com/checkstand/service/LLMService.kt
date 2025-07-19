package com.checkstand.service

import android.content.Context
import android.util.Log
import com.checkstand.model.ModelManager
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class LLMService(private val context: Context) {
    
    private var llmInference: LlmInference? = null
    private var llmSession: LlmInferenceSession? = null
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
                
                // Check if file is readable
                if (!modelFile.canRead()) {
                    Log.e(TAG, "Cannot read model file - permission issue")
                    return@withContext false
                }
                
                Log.d(TAG, "Creating LLM inference options...")
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(256)  // Following Gallery app pattern
                    .build()
                
                Log.d(TAG, "Creating LLM inference instance...")
                llmInference = LlmInference.createFromOptions(context, options)
                
                Log.d(TAG, "Creating LLM session...")
                llmSession = LlmInferenceSession.createFromOptions(
                    llmInference!!,
                    LlmInferenceSession.LlmInferenceSessionOptions.builder()
                        .setTopK(40)          // Standard values from Gallery app
                        .setTopP(0.95f)       
                        .setTemperature(0.7f)
                        .build()
                )
                
                // Force garbage collection after session creation
                System.gc()
                
                isModelLoaded = true
                Log.d(TAG, "Model and session loaded successfully!")
                true
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "Out of memory while loading model", e)
                false
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Native library loading failed", e)
                false
            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception - permission issue", e)
                false
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize model", e)
                false
            }
        }
    }
    
    fun generateResponse(prompt: String): Flow<String> = flow {
        if (!isModelLoaded || llmSession == null) {
            Log.e(TAG, "Cannot generate response: model not loaded")
            emit("Error: Model not loaded")
            return@flow
        }
        
        try {
            Log.d(TAG, "Generating response for prompt: ${prompt.take(50)}...")
            val fullPrompt = "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"
            
            llmSession?.let { session ->
                // First add the prompt to the session
                session.addQueryChunk(fullPrompt)
                // Then generate response without parameters
                val response = session.generateResponse()
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
        llmSession?.close()
        llmInference?.close()
        llmSession = null
        llmInference = null
        isModelLoaded = false
    }
}
