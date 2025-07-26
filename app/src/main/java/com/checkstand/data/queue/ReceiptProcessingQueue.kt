package com.checkstand.data.queue

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.checkstand.data.repository.ReceiptDataRepository
import com.checkstand.domain.model.PendingReceipt
import com.checkstand.domain.model.Receipt
import com.checkstand.domain.model.ReceiptStatus
import com.checkstand.domain.usecase.ProcessReceiptUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the background processing queue for receipts
 */
@Singleton
class ReceiptProcessingQueue @Inject constructor(
    private val processReceiptUseCase: ProcessReceiptUseCase,
    private val receiptDataRepository: ReceiptDataRepository
) {
    companion object {
        private const val TAG = "ReceiptProcessingQueue"
        private const val MAX_RETRY_COUNT = 3
        private const val PROCESSING_TIMEOUT_MS = 120_000L // 2 minutes total timeout
    }

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val processingMutex = Mutex()
    
    private val _pendingQueue = mutableListOf<PendingReceipt>()
    private val _isProcessing = MutableStateFlow(false)
    private val _currentProcessingId = MutableStateFlow<String?>(null)
    
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    val currentProcessingId: StateFlow<String?> = _currentProcessingId.asStateFlow()

    /**
     * Adds a receipt image to the processing queue
     * Returns immediately with a pending receipt
     */
    suspend fun addImageToQueue(bitmap: Bitmap): Receipt {
        val pendingReceipt = PendingReceipt(
            id = UUID.randomUUID().toString(),
            imageBitmap = bitmap
        )
        
        return addPendingReceiptToQueue(pendingReceipt)
    }

    /**
     * Adds a receipt image URI to the processing queue
     */
    suspend fun addImageUriToQueue(imageUri: Uri): Receipt {
        val pendingReceipt = PendingReceipt(
            id = UUID.randomUUID().toString(),
            imageUri = imageUri
        )
        
        return addPendingReceiptToQueue(pendingReceipt)
    }

    /**
     * Adds raw text to the processing queue
     */
    suspend fun addTextToQueue(text: String): Receipt {
        val pendingReceipt = PendingReceipt(
            id = UUID.randomUUID().toString(),
            rawText = text
        )
        
        return addPendingReceiptToQueue(pendingReceipt)
    }

    private suspend fun addPendingReceiptToQueue(pendingReceipt: PendingReceipt): Receipt {
        // Create placeholder receipt for immediate UI display
        val placeholderReceipt = pendingReceipt.toReceiptPlaceholder()
        
        // Save placeholder to repository immediately
        receiptDataRepository.insertReceipt(placeholderReceipt)
        
        // Add to processing queue
        processingMutex.withLock {
            _pendingQueue.add(pendingReceipt)
            Log.d(TAG, "Added receipt ${pendingReceipt.id} to queue. Queue size: ${_pendingQueue.size}")
        }
        
        // Start processing if not already running
        if (!_isProcessing.value) {
            startProcessing()
        }
        
        return placeholderReceipt
    }

    private fun startProcessing() {
        coroutineScope.launch {
            processingMutex.withLock {
                if (_isProcessing.value) return@withLock
                _isProcessing.value = true
            }
            
            Log.d(TAG, "Starting queue processing")
            
            try {
                while (true) {
                    val nextReceipt = processingMutex.withLock {
                        _pendingQueue.removeFirstOrNull()
                    } ?: break
                    
                    processReceipt(nextReceipt)
                }
            } finally {
                _isProcessing.value = false
                _currentProcessingId.value = null
                Log.d(TAG, "Queue processing completed")
            }
        }
    }

    private suspend fun processReceipt(pendingReceipt: PendingReceipt) {
        val receiptId = pendingReceipt.id
        _currentProcessingId.value = receiptId
        
        Log.d(TAG, "Processing receipt $receiptId")
        
        try {
            // Update status to PROCESSING
            updateReceiptStatus(receiptId, ReceiptStatus.PROCESSING)

            // Process the receipt based on input type
            val result = when {
                pendingReceipt.imageBitmap != null -> processReceiptUseCase(pendingReceipt.imageBitmap)
                pendingReceipt.imageUri != null -> processReceiptUseCase(pendingReceipt.imageUri)
                pendingReceipt.rawText != null -> processReceiptUseCase(pendingReceipt.rawText)
                else -> {
                    Log.e(TAG, "No valid input for receipt $receiptId")
                    throw IllegalStateException("No valid input for processing")
                }
            }
            
            // Collect the result with timeout to prevent infinite processing
            try {
                withTimeout(PROCESSING_TIMEOUT_MS) {
                    result.collect { processResult ->
                        processResult.fold(
                            onSuccess = { processedReceipt ->
                                // Update with processed data
                                val completedReceipt = processedReceipt.copy(
                                    id = receiptId,
                                    status = ReceiptStatus.COMPLETED,
                                    processingError = null,
                                    retryCount = pendingReceipt.retryCount
                                )
                                receiptDataRepository.updateReceipt(completedReceipt)
                                Log.d(TAG, "Successfully processed receipt $receiptId")
                            },
                            onFailure = { error ->
                                Log.e(TAG, "Failed to process receipt $receiptId", error)
                                handleProcessingError(pendingReceipt, error.message ?: "Unknown error")
                            }
                        )
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.w(TAG, "Processing timed out for receipt $receiptId after ${PROCESSING_TIMEOUT_MS}ms")
                handleProcessingError(pendingReceipt, "Processing timed out")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception processing receipt $receiptId", e)
            handleProcessingError(pendingReceipt, e.message ?: "Processing exception")
        }
    }

    private suspend fun handleProcessingError(pendingReceipt: PendingReceipt, errorMessage: String) {
        val receiptId = pendingReceipt.id
        val newRetryCount = pendingReceipt.retryCount + 1
        
        if (newRetryCount < MAX_RETRY_COUNT) {
            Log.d(TAG, "Retrying receipt $receiptId (attempt $newRetryCount)")
            
            // Add back to queue for retry
            val retryReceipt = pendingReceipt.copy(retryCount = newRetryCount)
            processingMutex.withLock {
                _pendingQueue.add(retryReceipt)
            }
            
            // Update status to show retry
            updateReceiptStatus(receiptId, ReceiptStatus.PENDING, "Retrying... (attempt $newRetryCount)")
        } else {
            Log.e(TAG, "Max retries exceeded for receipt $receiptId")
            updateReceiptStatus(receiptId, ReceiptStatus.FAILED, errorMessage)
        }
    }

    private suspend fun updateReceiptStatus(receiptId: String, status: ReceiptStatus, error: String? = null) {
        try {
            // Get current receipt data - use first() to avoid infinite loop
            val receipts = receiptDataRepository.getAllReceipts().first()
            val receipt = receipts.find { it.id == receiptId }
            if (receipt != null) {
                val updatedReceipt = receipt.copy(
                    status = status,
                    processingError = error
                )
                receiptDataRepository.updateReceipt(updatedReceipt)
            } else {
                Log.w(TAG, "Receipt $receiptId not found in repository for status update")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update receipt status for $receiptId", e)
        }
    }

    /**
     * Gets the current queue size
     */
    fun getQueueSize(): Int {
        return _pendingQueue.size
    }

    /**
     * Clears all pending items from the queue
     */
    suspend fun clearQueue() {
        processingMutex.withLock {
            _pendingQueue.clear()
            Log.d(TAG, "Queue cleared")
        }
    }
}
