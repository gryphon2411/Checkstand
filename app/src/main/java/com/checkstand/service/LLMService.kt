package com.checkstand.service

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.checkstand.model.ModelManager
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LLMService @Inject constructor(
    private val context: Context,
    private val modelStatusService: ModelStatusService
) {
    
    private var llmInference: LlmInference? = null
    private var isModelLoaded = false
    private val modelManager = ModelManager(context)
    
    companion object {
        private const val TAG = "LLMService"
        private const val MAX_IMAGE_COUNT = 5
        private const val MAX_TOKENS = 1024
    }
    
    suspend fun initializeModel(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting model initialization...")
                modelStatusService.updateStatus(ModelStatus.LOADING)
                modelStatusService.updateProgress(0.1f)
                
                if (!modelManager.isModelAvailable()) {
                    Log.e(TAG, "Model file not found at expected location")
                    modelStatusService.updateStatus(ModelStatus.ERROR)
                    modelStatusService.updateMessage("Model file not found")
                    return@withContext false
                }
                
                modelStatusService.updateProgress(0.2f)
                modelStatusService.updateMessage("Loading model file...")
                
                val modelFile = modelManager.getModelFile()
                Log.d(TAG, "Model file found at: ${modelFile.absolutePath}")
                Log.d(TAG, "Model file size: ${modelFile.length()} bytes")
                
                // Check if file is readable
                if (!modelFile.canRead()) {
                    Log.e(TAG, "Cannot read model file - permission issue")
                    modelStatusService.updateStatus(ModelStatus.ERROR)
                    modelStatusService.updateMessage("Cannot read model file")
                    return@withContext false
                }
                
                modelStatusService.updateProgress(0.4f)
                modelStatusService.updateMessage("Creating inference engine...")
                
                Log.d(TAG, "Creating LLM inference options...")
                val options = LlmInference.LlmInferenceOptions.builder()
                    .setModelPath(modelFile.absolutePath)
                    .setMaxTokens(MAX_TOKENS)
                    // Note: Image support may need different MediaPipe configuration
                    .build()
                
                modelStatusService.updateProgress(0.6f)
                modelStatusService.updateMessage("Initializing AI model...")
                
                Log.d(TAG, "Creating LLM inference instance...")
                llmInference = LlmInference.createFromOptions(context, options)
                
                modelStatusService.updateProgress(0.8f)
                modelStatusService.updateMessage("Model ready for processing...")
                
                Log.d(TAG, "LLM inference instance created successfully")
                
                // Force garbage collection after inference creation
                System.gc()
                
                modelStatusService.updateProgress(1.0f)
                isModelLoaded = true
                modelStatusService.updateStatus(ModelStatus.READY)
                modelStatusService.updateMessage("Model ready for processing")
                Log.d(TAG, "Model loaded successfully!")
                true
            } catch (e: OutOfMemoryError) {
                Log.e(TAG, "Out of memory while loading model", e)
                modelStatusService.updateStatus(ModelStatus.ERROR)
                modelStatusService.updateMessage("Out of memory loading model")
                false
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Native library loading failed", e)
                modelStatusService.updateStatus(ModelStatus.ERROR)
                modelStatusService.updateMessage("Native library loading failed")
                false
            } catch (e: SecurityException) {
                Log.e(TAG, "Security exception - permission issue", e)
                modelStatusService.updateStatus(ModelStatus.ERROR)
                modelStatusService.updateMessage("Permission denied")
                false
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize model", e)
                modelStatusService.updateStatus(ModelStatus.ERROR)
                modelStatusService.updateMessage("Model initialization failed: ${e.message}")
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
            
            // Create a fresh session for each request to avoid context contamination
            val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
                .setTopK(40)
                .setTopP(0.95f)
                .setTemperature(0.8f)
                .build()
                
            LlmInferenceSession.createFromOptions(llmInference!!, sessionOptions).use { session ->
                Log.d(TAG, "Created fresh session for receipt processing")
                session.addQueryChunk(prompt)
                val response = session.generateResponse()
                Log.d(TAG, "Generated response: ${response.take(100)}...")
                emit(response)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response", e)
            emit("Error: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)
    
    fun generateResponseWithImage(prompt: String, image: Bitmap): Flow<String> = flow {
        if (!isModelLoaded || llmInference == null) {
            Log.e(TAG, "Cannot generate response: model not loaded")
            emit("Error: Model not loaded")
            return@flow
        }
        
        try {
            Log.d(TAG, "Generating response with image for prompt: ${prompt.take(50)}...")
            Log.d(TAG, "Image dimensions: ${image.width}x${image.height}")
            
            // Create a fresh session for image processing
            val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
                .setTopK(40)
                .setTopP(0.95f)
                .setTemperature(0.8f)
                .build()
                
            LlmInferenceSession.createFromOptions(llmInference!!, sessionOptions).use { session ->
                Log.d(TAG, "Created fresh session for image processing")
                
                // For now, process as text-only since multimodal support requires specific setup
                // The image is processed via OCR in the repository layer
                session.addQueryChunk(prompt)
                val response = session.generateResponse()
                Log.d(TAG, "Generated response: ${response.take(100)}...")
                emit(response)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating response with image", e)
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
