package com.checkstand.domain.model

import java.math.BigDecimal
import java.util.Date

/**
 * Domain model for business receipts/invoices
 */
data class Receipt(
    val id: String = generateId(),
    val merchantName: String,
    val totalAmount: BigDecimal,
    val currency: String = "USD",
    val date: Date,
    val items: List<ReceiptItem> = emptyList(),
    val category: ExpenseCategory = ExpenseCategory.UNCATEGORIZED,
    val imagePath: String? = null,
    val rawText: String? = null,
    val status: ProcessingStatus = ProcessingStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        private fun generateId(): String = System.currentTimeMillis().toString()
    }
}

data class ReceiptItem(
    val name: String,
    val quantity: Int = 1,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal = unitPrice.multiply(BigDecimal(quantity))
)

enum class ExpenseCategory {
    UNCATEGORIZED,
    FOOD_DINING,
    GROCERIES,
    TRANSPORTATION,
    UTILITIES,
    OFFICE_SUPPLIES,
    ENTERTAINMENT,
    HEALTH_MEDICAL,
    SHOPPING,
    SERVICES,
    OTHER
}

enum class ProcessingStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    ERROR
}

/**
 * Represents the state of receipt processing
 */
data class ReceiptState(
    val receipts: List<Receipt> = emptyList(),
    val isProcessing: Boolean = false,
    val currentReceipt: Receipt? = null,
    val error: String? = null
)

/**
 * Represents the model loading state
 */
data class ModelState(
    val isAvailable: Boolean = false,
    val isLoaded: Boolean = false,
    val isLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Int = 0,
    val error: String? = null,
    val modelInfo: ModelInfo? = null
)

data class ModelInfo(
    val name: String,
    val version: String,
    val size: Long,
    val path: String
)
