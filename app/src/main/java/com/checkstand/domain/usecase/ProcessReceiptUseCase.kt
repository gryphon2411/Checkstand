package com.checkstand.domain.usecase

import android.graphics.Bitmap
import android.net.Uri
import com.checkstand.domain.model.Receipt
import com.checkstand.domain.repository.ReceiptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProcessReceiptUseCase @Inject constructor(
    private val receiptRepository: ReceiptRepository
) {
    operator fun invoke(bitmap: Bitmap): Flow<Result<Receipt>> {
        return receiptRepository.processReceiptImage(bitmap)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
    }

    operator fun invoke(imageUri: Uri): Flow<Result<Receipt>> {
        return receiptRepository.processReceiptImage(imageUri)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
    }

    operator fun invoke(text: String): Flow<Result<Receipt>> {
        return receiptRepository.processReceiptText(text)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
    }
}
