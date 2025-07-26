package com.checkstand.domain.model

import java.math.BigDecimal
import java.util.Date
import java.util.UUID

data class Receipt(
    val id: String = UUID.randomUUID().toString(),
    val merchantName: String,
    val totalAmount: BigDecimal,
    val date: Date,
    val items: List<ReceiptItem> = emptyList(),
    val category: ExpenseCategory = ExpenseCategory.UNCATEGORIZED,
    val rawText: String = "",
    val llmResponse: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: ReceiptStatus? = ReceiptStatus.COMPLETED,
    val processingError: String? = null,
    val retryCount: Int = 0
)
