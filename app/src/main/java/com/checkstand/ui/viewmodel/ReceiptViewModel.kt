package com.checkstand.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkstand.domain.model.Receipt
import com.checkstand.domain.repository.ReceiptRepository
import com.checkstand.domain.usecase.ProcessReceiptUseCase
import com.checkstand.domain.usecase.CategorizeExpenseUseCase
import com.checkstand.service.ModelStatusService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptViewModel @Inject constructor(
    private val processReceiptUseCase: ProcessReceiptUseCase,
    private val categorizeExpenseUseCase: CategorizeExpenseUseCase,
    private val modelStatusService: ModelStatusService,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptUiState())
    val uiState: StateFlow<ReceiptUiState> = _uiState.asStateFlow()

    private val _receipts = MutableStateFlow<List<Receipt>>(emptyList())
    val receipts: StateFlow<List<Receipt>> = _receipts.asStateFlow()
    
    // Expose model status from the service
    val modelStatus = modelStatusService.modelStatus
    val loadingProgress = modelStatusService.loadingProgress
    val statusMessage = modelStatusService.statusMessage
    
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
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                currentImageUri = null
            )

            processReceiptUseCase(bitmap).collect { result ->
                result.fold(
                    onSuccess = { receipt ->
                        _receipts.value = _receipts.value + receipt
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            currentImageUri = null,
                            lastProcessedReceipt = receipt
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            currentImageUri = null,
                            errorMessage = error.message
                        )
                    }
                )
            }
        }
    }

    fun processReceiptImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                currentImageUri = imageUri
            )

            processReceiptUseCase(imageUri).collect { result ->
                result.fold(
                    onSuccess = { receipt ->
                        _receipts.value = _receipts.value + receipt
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            currentImageUri = null,
                            lastProcessedReceipt = receipt
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            currentImageUri = null,
                            errorMessage = error.message
                        )
                    }
                )
            }
        }
    }

    fun processReceiptText(text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)

            processReceiptUseCase(text).collect { result ->
                result.fold(
                    onSuccess = { receipt ->
                        _receipts.value = _receipts.value + receipt
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            lastProcessedReceipt = receipt
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            errorMessage = error.message
                        )
                    }
                )
            }
        }
    }

    fun categorizeExpense(receipt: Receipt) {
        viewModelScope.launch {
            categorizeExpenseUseCase(receipt).collect { result ->
                result.fold(
                    onSuccess = { categorizedReceipt ->
                        val updatedReceipts = _receipts.value.map { r ->
                            if (r.id == categorizedReceipt.id) categorizedReceipt else r
                        }
                        _receipts.value = updatedReceipts
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
        _receipts.value = _receipts.value.filter { it.id != receiptId }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearLastProcessedReceipt() {
        _uiState.value = _uiState.value.copy(lastProcessedReceipt = null)
    }
}

data class ReceiptUiState(
    val isProcessing: Boolean = false,
    val currentImageUri: Uri? = null,
    val lastProcessedReceipt: Receipt? = null,
    val errorMessage: String? = null
)
