package com.checkstand.domain.model

/**
 * Represents the processing status of a receipt
 */
enum class ReceiptStatus {
    /**
     * Receipt has been captured but not yet processed
     */
    PENDING,
    
    /**
     * Receipt is currently being processed by the LLM
     */
    PROCESSING,
    
    /**
     * Receipt has been successfully processed and data extracted
     */
    COMPLETED,
    
    /**
     * Receipt processing failed, retry available
     */
    FAILED
}
