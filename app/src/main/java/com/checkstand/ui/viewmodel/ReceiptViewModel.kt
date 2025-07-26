package com.checkstand.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkstand.domain.model.Receipt
import com.checkstand.domain.repository.ReceiptRepository
import com.checkstand.domain.usecase.ProcessReceiptUseCase
import com.checkstand.domain.usecase.CategorizeExpenseUseCase
import com.checkstand.data.repository.ReceiptDataRepository
import com.checkstand.data.queue.ReceiptProcessingQueue
import com.checkstand.service.ModelStatusService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val processReceiptUseCase: ProcessReceiptUseCase,
    private val categorizeExpenseUseCase: CategorizeExpenseUseCase,
    private val modelStatusService: ModelStatusService,
    private val receiptRepository: ReceiptRepository,
    private val receiptDataRepository: ReceiptDataRepository,
    private val receiptProcessingQueue: ReceiptProcessingQueue
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()

    // Use the database repository for receipts
    val receipts: StateFlow<List<Receipt>> = receiptDataRepository.getAllReceipts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // Expose model status from the service
    val modelStatus = modelStatusService.modelStatus
    val loadingProgress = modelStatusService.loadingProgress
    val statusMessage = modelStatusService.statusMessage
    
    // Expose queue processing status
    val isQueueProcessing = receiptProcessingQueue.isProcessing
    val currentProcessingId = receiptProcessingQueue.currentProcessingId
    
    init {
        // Initialize the model when ViewModel is created
        initializeModel()
    }
    
    private fun initializeModel() {
        viewModelScope.launch {
            try {
                receiptRepository.initializeModel()
            } catch (e: Exception) {
                // Model initialization will be handled by the ModelStatusService
            }
        }
    }

    fun processReceiptImage(bitmap: Bitmap) {
        viewModelScope.launch {
            try {
                // Add to queue - returns immediately with placeholder receipt
                val placeholderReceipt = receiptProcessingQueue.addImageToQueue(bitmap)
                
                _uiState.value = _uiState.value.copy(
                    lastProcessedReceipt = placeholderReceipt,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add receipt to processing queue: ${e.message}"
                )
            }
        }
    }

    fun processReceiptImage(imageUri: Uri) {
        viewModelScope.launch {
            try {
                // Add to queue - returns immediately with placeholder receipt
                val placeholderReceipt = receiptProcessingQueue.addImageUriToQueue(imageUri)
                
                _uiState.value = _uiState.value.copy(
                    currentImageUri = null,
                    lastProcessedReceipt = placeholderReceipt,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    currentImageUri = null,
                    errorMessage = "Failed to add receipt to processing queue: ${e.message}"
                )
            }
        }
    }

    fun processReceiptText(text: String) {
        viewModelScope.launch {
            try {
                // Add to queue - returns immediately with placeholder receipt
                val placeholderReceipt = receiptProcessingQueue.addTextToQueue(text)
                
                _uiState.value = _uiState.value.copy(
                    lastProcessedReceipt = placeholderReceipt,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add receipt to processing queue: ${e.message}"
                )
            }
        }
    }

    fun categorizeExpense(receipt: Receipt) {
        viewModelScope.launch {
            categorizeExpenseUseCase(receipt).collect { result ->
                result.fold(
                    onSuccess = { categorizedReceipt ->
                        // Update in database
                        receiptDataRepository.updateReceipt(categorizedReceipt)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = error.message
                        )
                    }
                )
            }
        }
    }

    fun deleteReceipt(receiptId: String) {
        viewModelScope.launch {
            receiptDataRepository.deleteReceiptById(receiptId)
        }
    }

    fun getQueueSize(): Int {
        return receiptProcessingQueue.getQueueSize()
    }

    fun clearQueue() {
        viewModelScope.launch {
            receiptProcessingQueue.clearQueue()
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearLastProcessedReceipt() {
        _uiState.value = _uiState.value.copy(lastProcessedReceipt = null)
    }
}

data class ReceiptUiState(
    val currentImageUri: Uri? = null,
    val lastProcessedReceipt: Receipt? = null,
    val errorMessage: String? = null
)
