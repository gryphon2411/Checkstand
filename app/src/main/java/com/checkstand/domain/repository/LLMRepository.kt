package com.checkstand.domain.repository

import android.graphics.Bitmap
import com.checkstand.domain.model.Receipt
import com.checkstand.domain.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for receipt processing operations.
 * Provides an abstraction layer for receipt analysis using LLM.
 */
interface ReceiptRepository {
    
    /**
     * Checks if the AI model is available on the device
     */
    fun isModelAvailable(): Boolean
    
    /**
     * Checks if the model is currently loaded and ready for inference
     */
    fun isModelReady(): Boolean
    
    /**
     * Initializes and loads the LLM model for receipt processing
     * @return true if successful, false otherwise
     */
    suspend fun initializeModel(): Boolean
    
    /**
     * Analyzes extracted text from a receipt and structures the data
     * @param rawText The OCR extracted text from receipt image
     * @return Flow of structured receipt data
     */
    fun analyzeReceiptText(rawText: String): Flow<Receipt>
    
    /**
     * Analyzes a receipt image directly using multimodal AI
     * @param image The bitmap image of the receipt
     * @return Flow of structured receipt data
     */
    fun analyzeReceiptImage(image: Bitmap): Flow<Receipt>
    
    /**
     * Categorizes an expense based on merchant name and items
     * @param merchantName The name of the merchant/store
     * @param items List of purchased items
     * @return Predicted expense category
     */
    suspend fun categorizeExpense(merchantName: String, items: List<String>): ExpenseCategory
    
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
