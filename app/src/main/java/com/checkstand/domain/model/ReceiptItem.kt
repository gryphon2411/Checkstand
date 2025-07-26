package com.checkstand.domain.model

import java.math.BigDecimal

data class ReceiptItem(
    val name: String,
    val quantity: Int = 1,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal = unitPrice.multiply(BigDecimal(quantity))
)
