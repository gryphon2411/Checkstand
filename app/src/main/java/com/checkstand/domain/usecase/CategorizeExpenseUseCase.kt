package com.checkstand.domain.usecase

import com.checkstand.domain.model.Receipt
import com.checkstand.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategorizeExpenseUseCase @Inject constructor(
    private val receiptRepository: ReceiptRepository
) {
    operator fun invoke(receipt: Receipt): Flow<Result<Receipt>> = flow {
        val category = receiptRepository.categorizeExpense(
            receipt.merchantName,
            receipt.items.map { it.name }
        )
        emit(receipt.copy(category = category))
    }
        .map { Result.success(it) }
        .catch { emit(Result.failure(it)) }
}
