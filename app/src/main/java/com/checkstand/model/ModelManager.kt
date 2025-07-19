package com.checkstand.model

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ModelManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ModelManager"
        private const val MODEL_FILENAME = "gemma-3n-E2B-it-int4.task"
        private const val MODEL_DEVICE_PATH = "/data/local/tmp/llm/gemma-3n-E2B-it-int4.task"
    }
    
    fun getModelFile(): File {
        return File(MODEL_DEVICE_PATH)
    }
    
    fun isModelAvailable(): Boolean {
        return getModelFile().exists()
    }
    
    suspend fun downloadModel(onProgress: (Int) -> Unit = {}): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val modelFile = getModelFile()
                if (modelFile.exists()) {
                    Log.d(TAG, "Model already exists at ${modelFile.absolutePath}")
                    return@withContext true
                }
                
                // For now, we'll just indicate that the model needs to be pushed via adb
                Log.d(TAG, "Model not found at ${modelFile.absolutePath}")
                Log.d(TAG, "Please push the model using: adb push <your-model-file> $MODEL_DEVICE_PATH")
                return@withContext false
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check model", e)
                false
            }
        }
    }
    
    fun deleteModel() {
        val modelFile = getModelFile()
        if (modelFile.exists()) {
            modelFile.delete()
            Log.d(TAG, "Model deleted")
        }
    }
    
    fun getModelSizeInMB(): Long {
        val modelFile = getModelFile()
        return if (modelFile.exists()) {
            modelFile.length() / (1024 * 1024)
        } else {
            0
        }
    }
}
