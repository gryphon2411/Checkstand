package com.checkstand.domain.usecase

import com.checkstand.domain.model.ChatMessage
import com.checkstand.domain.repository.LLMRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Use case for sending a message and getting an AI response
 */
class SendMessageUseCase(
    private val repository: LLMRepository
) {
    
    /**
     * Sends a message and returns the AI response
     * @param message The user message
     * @return Flow of AI response text
     */
    operator fun invoke(message: String): Flow<Result<String>> {
        if (message.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(Result.failure(IllegalArgumentException("Message cannot be blank")))
        }
        
        if (!repository.isModelReady()) {
            return kotlinx.coroutines.flow.flowOf(Result.failure(IllegalStateException("Model is not ready")))
        }
        
        return repository.generateResponse(message)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
    }
}

/**
 * Use case for initializing the LLM model
 */
class InitializeModelUseCase(
    private val repository: LLMRepository
) {
    
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val success = repository.initializeModel()
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to initialize model"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for checking model availability and status
 */
class CheckModelStatusUseCase(
    private val repository: LLMRepository
) {
    
    data class ModelStatus(
        val isAvailable: Boolean,
        val isReady: Boolean,
        val modelInfo: com.checkstand.domain.repository.ModelInfo
    )
    
    operator fun invoke(): ModelStatus {
        return ModelStatus(
            isAvailable = repository.isModelAvailable(),
            isReady = repository.isModelReady(),
            modelInfo = repository.getModelInfo()
        )
    }
}

/**
 * Use case for downloading the model
 */
class DownloadModelUseCase(
    private val repository: LLMRepository
) {
    
    suspend operator fun invoke(onProgress: (Int) -> Unit): Result<Unit> {
        return try {
            val success = repository.downloadModel(onProgress)
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to download model"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
