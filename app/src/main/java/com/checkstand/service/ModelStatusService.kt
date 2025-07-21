package com.checkstand.service

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelStatusService @Inject constructor() {
    
    private val _modelStatus = MutableStateFlow(ModelStatus.NOT_LOADED)
    val modelStatus: StateFlow<ModelStatus> = _modelStatus.asStateFlow()
    
    private val _loadingProgress = MutableStateFlow(0f)
    val loadingProgress: StateFlow<Float> = _loadingProgress.asStateFlow()
    
    private val _statusMessage = MutableStateFlow("Model not loaded")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()
    
    companion object {
        private const val TAG = "ModelStatusService"
    }
    
    fun updateStatus(status: ModelStatus) {
        Log.d(TAG, "Model status updated to: $status")
        _modelStatus.value = status
        
        _statusMessage.value = when (status) {
            ModelStatus.NOT_LOADED -> "Model not loaded"
            ModelStatus.LOADING -> "Loading AI model..."
            ModelStatus.READY -> "Model ready for processing"
            ModelStatus.ERROR -> "Model failed to load"
        }
    }
    
    fun updateProgress(progress: Float) {
        _loadingProgress.value = progress.coerceIn(0f, 1f)
    }
    
    fun updateMessage(message: String) {
        _statusMessage.value = message
    }
    
    fun isReady(): Boolean = _modelStatus.value == ModelStatus.READY
    
    fun isLoading(): Boolean = _modelStatus.value == ModelStatus.LOADING
}

enum class ModelStatus {
    NOT_LOADED,
    LOADING,
    READY,
    ERROR
}
