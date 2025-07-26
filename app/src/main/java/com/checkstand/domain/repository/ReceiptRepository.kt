package com.checkstand.domain.repository

import android.graphics.Bitmap
import android.net.Uri
import com.checkstand.domain.model.ExpenseCategory
import com.checkstand.domain.model.Receipt
import kotlinx.coroutines.flow.Flow

data class ModelInfo(
    val name: String,
    val version: String,
    val isReady: Boolean
)

interface ReceiptRepository {
    fun isModelAvailable(): Boolean
    fun isModelReady(): Boolean
    suspend fun initializeModel(): Boolean
    fun processReceiptImage(bitmap: Bitmap): Flow<Receipt>
    fun processReceiptImage(imageUri: Uri): Flow<Receipt>
    fun processReceiptText(text: String): Flow<Receipt>
    suspend fun categorizeExpense(merchantName: String, items: List<String>): ExpenseCategory
    fun getModelInfo(): ModelInfo
}
