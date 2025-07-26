package com.checkstand.domain.model

import android.graphics.Bitmap
import android.net.Uri

/**
 * Represents a receipt that is pending processing in the queue
 */
data class PendingReceipt(
    val id: String,
    val imageUri: Uri? = null,
    val imageBitmap: Bitmap? = null,
    val rawText: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
) {
    /**
     * Converts this pending receipt to a Receipt with PENDING status
     * Used for immediate UI display
     */
    fun toReceiptPlaceholder(): Receipt {
        return Receipt(
            id = id,
            merchantName = "Processing...",
            totalAmount = java.math.BigDecimal.ZERO,
            date = java.util.Date(createdAt),
            items = emptyList(),
            category = ExpenseCategory.UNCATEGORIZED,
            rawText = rawText ?: "",
            llmResponse = "",
            createdAt = createdAt,
            status = ReceiptStatus.PENDING,
            processingError = null,
            retryCount = retryCount
        )
    }
}
