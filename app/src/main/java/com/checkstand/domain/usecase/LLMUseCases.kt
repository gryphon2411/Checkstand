package com.checkstand.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.checkstand.domain.model.Receipt
import com.checkstand.domain.model.ExpenseCategory
import com.checkstand.domain.repository.ReceiptRepository
import com.checkstand.service.OCRService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for processing receipt text and extracting structured data
 */
class ProcessReceiptUseCase @Inject constructor(
    private val repository: ReceiptRepository,
    private val ocrService: OCRService
) {
    
    /**
     * Processes receipt image directly using multimodal AI
     * @param image The bitmap image of the receipt
     * @return Flow of structured receipt data
     */
    operator fun invoke(image: Bitmap): Flow<Result<Receipt>> = flow {
        try {
            if (!repository.isModelReady()) {
                emit(Result.failure(IllegalStateException("Model is not ready")))
                return@flow
            }
            
            // Process the image directly through MediaPipe multimodal
            repository.analyzeReceiptImage(image)
                .map { Result.success(it) }
                .catch { emit(Result.failure(it)) }
                .collect { emit(it) }
                
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Processes receipt image and returns structured receipt data
     * @param imageUri The URI of the receipt image
     * @return Flow of structured receipt data
     */
    operator fun invoke(imageUri: Uri): Flow<Result<Receipt>> = flow {
        try {
            if (!repository.isModelReady()) {
                emit(Result.failure(IllegalStateException("Model is not ready")))
                return@flow
            }
            
            // Extract text from image using OCR as fallback
            val extractedText = ocrService.extractTextFromImage(imageUri)
            
            if (extractedText.isBlank()) {
                emit(Result.failure(IllegalArgumentException("Could not extract text from image")))
                return@flow
            }
            
            // Process the extracted text through MediaPipe
            repository.analyzeReceiptText(extractedText)
                .map { Result.success(it) }
                .catch { emit(Result.failure(it)) }
                .collect { emit(it) }
                
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    /**
     * Processes raw OCR text and returns structured receipt data
     * @param rawText The extracted text from receipt image
     * @return Flow of structured receipt data
     */
    operator fun invoke(rawText: String): Flow<Result<Receipt>> {
        if (rawText.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(Result.failure(IllegalArgumentException("Receipt text cannot be blank")))
        }
        
        if (!repository.isModelReady()) {
            return kotlinx.coroutines.flow.flowOf(Result.failure(IllegalStateException("Model is not ready")))
        }
        
        return repository.analyzeReceiptText(rawText)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
    }
}

/**
 * Use case for categorizing expenses automatically
 */
class CategorizeExpenseUseCase @Inject constructor(
    private val repository: ReceiptRepository
) {
    
    operator fun invoke(receipt: Receipt): Flow<Result<Receipt>> = flow {
        try {
            if (!repository.isModelReady()) {
                emit(Result.failure(IllegalStateException("Model is not ready")))
                return@flow
            }
            
            val itemDescriptions = receipt.items.map { it.name }
            val category = repository.categorizeExpense(receipt.merchantName, itemDescriptions)
            
            val categorizedReceipt = receipt.copy(category = category)
            emit(Result.success(categorizedReceipt))
            
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend operator fun invoke(merchantName: String, items: List<String>): Result<ExpenseCategory> {
        return try {
            if (!repository.isModelReady()) {
                return Result.failure(IllegalStateException("Model is not ready"))
            }
            
            val category = repository.categorizeExpense(merchantName, items)
            Result.success(category)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for initializing the receipt processing model
 */
class InitializeReceiptModelUseCase(
    private val repository: ReceiptRepository
) {
    
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val success = repository.initializeModel()
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to initialize receipt processing model"))
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
    private val repository: ReceiptRepository
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
    private val repository: ReceiptRepository
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
