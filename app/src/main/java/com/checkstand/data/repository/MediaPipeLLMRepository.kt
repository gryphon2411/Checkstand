package com.checkstand.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.checkstand.domain.model.Receipt
import com.checkstand.domain.model.ExpenseCategory
import com.checkstand.domain.model.ReceiptItem
import com.checkstand.domain.repository.ReceiptRepository
import com.checkstand.domain.repository.ModelInfo
import com.checkstand.service.LLMService
import com.checkstand.service.OCRService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of ReceiptRepository using MediaPipe LLM Service
 */
class MediaPipeReceiptRepository(
    private val llmService: LLMService,
    private val ocrService: OCRService
) : ReceiptRepository {

    companion object {
        private const val TAG = "MediaPipeReceiptRepo"
        private const val MODEL_NAME = "Gemma-3n E4B"
        private const val MODEL_VERSION = "int4"
        private const val PROCESSING_TIMEOUT_MS = 60_000L // 60 seconds timeout
    }

    override fun isModelAvailable(): Boolean {
        return llmService.isModelAvailable()
    }

    override fun isModelReady(): Boolean {
        return llmService.isModelReady()
    }

    override suspend fun initializeModel(): Boolean {
        Log.d(TAG, "Initializing receipt processing model...")
        return try {
            llmService.initializeModel()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize model", e)
            false
        }
    }

    override fun processReceiptText(text: String): Flow<Receipt> {
        return flow {
            val prompt = buildReceiptAnalysisPrompt(text)

            try {
                // Add timeout to prevent infinite processing
                withTimeout(PROCESSING_TIMEOUT_MS) {
                    llmService.generateResponse(prompt).collect { response ->
                        try {
                            val receipt = parseReceiptFromResponse(response, text)
                            emit(receipt)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse receipt from response", e)
                            // Try to extract some basic info even if parsing fails
                            val fallbackReceipt = createFallbackReceipt(text, response)
                            emit(fallbackReceipt)
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.w(TAG, "LLM processing timed out after ${PROCESSING_TIMEOUT_MS}ms")
                // Create fallback receipt when timeout occurs
                val fallbackReceipt = createFallbackReceipt(text, "Processing timed out")
                emit(fallbackReceipt)
            } catch (e: Exception) {
                Log.e(TAG, "Error during LLM processing", e)
                // Create fallback receipt for any other errors
                val fallbackReceipt = createFallbackReceipt(text, "Error: ${e.message}")
                emit(fallbackReceipt)
            }
        }
    }

    override fun processReceiptImage(bitmap: Bitmap): Flow<Receipt> {
        return flow {
            try {
                val rawText = ocrService.extractTextFromImage(bitmap)
                
                if (rawText.isNotBlank()) {
                    // Now that we have the text, delegate to the text processing function
                    processReceiptText(rawText).collect { receipt ->
                        // We can enhance the receipt with the image info if needed
                        // For now, just emit the processed receipt
                        emit(receipt)
                    }
                } else {
                    Log.w(TAG, "OCR service returned empty text from image.")
                    emit(createFallbackReceipt("Could not extract text from image.", "No OCR text available"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process receipt image", e)
                emit(createFallbackReceipt("Error processing receipt image.", "Error: ${e.message}"))
            }
        }
    }

    override fun processReceiptImage(imageUri: Uri): Flow<Receipt> {
        return flow {
            try {
                val rawText = ocrService.extractTextFromImage(imageUri)
                
                if (rawText.isNotBlank()) {
                    // Now that we have the text, delegate to the text processing function
                    processReceiptText(rawText).collect { receipt ->
                        // Emit the processed receipt
                        emit(receipt)
                    }
                } else {
                    Log.w(TAG, "OCR service returned empty text from image URI.")
                    emit(createFallbackReceipt("Could not extract text from image.", "No OCR text available"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process receipt image from URI", e)
                emit(createFallbackReceipt("Error processing receipt image.", "Error: ${e.message}"))
            }
        }
    }

    override suspend fun categorizeExpense(merchantName: String, items: List<String>): ExpenseCategory {
        Log.d(TAG, "Categorizing expense for merchant: $merchantName")

        return try {
            val prompt = buildCategorizationPrompt(merchantName, items)

            // For now, use simple rules-based categorization
            // In the future, this could use the LLM for more sophisticated categorization
            categorizeByRules(merchantName, items)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to categorize expense", e)
            ExpenseCategory.UNCATEGORIZED
        }
    }

    private fun buildReceiptAnalysisPrompt(rawText: String): String {
        return """
            Analyze this receipt text and extract structured information. Return the data in this exact format:
            
            MERCHANT: [merchant name]
            DATE: [date in yyyy-MM-dd format]
            TOTAL: [total amount as decimal number]
            ITEMS:
            - [item name] | [quantity] | [unit price] | [total price]
            - [item name] | [quantity] | [unit price] | [total price]
            
            Receipt text:
            $rawText
            
            Please extract the information accurately. If any field is unclear, use reasonable defaults.
        """.trimIndent()
    }
    
    private fun buildReceiptImageAnalysisPrompt(): String {
        return """
            Analyze this receipt image and extract structured information. Return the data in this exact format:
            
            MERCHANT: [merchant name]
            DATE: [date in yyyy-MM-dd format]
            TOTAL: [total amount as decimal number]
            ITEMS:
            - [item name] | [quantity] | [unit price] | [total price]
            - [item name] | [quantity] | [unit price] | [total price]
            
            Please look at the receipt image and extract all visible information accurately. If any field is unclear from the image, use reasonable defaults.
        """.trimIndent()
    }

    private fun buildCategorizationPrompt(merchantName: String, items: List<String>): String {
        return """
            Categorize this purchase into one of these categories:
            FOOD_DINING, GROCERIES, TRANSPORTATION, UTILITIES, OFFICE_SUPPLIES, 
            ENTERTAINMENT, HEALTH_MEDICAL, SHOPPING, SERVICES, OTHER
            
            Merchant: $merchantName
            Items: ${items.joinToString(", ")}
            
            Return only the category name.
        """.trimIndent()
    }

    private fun parseReceiptFromResponse(response: String, rawText: String): Receipt {
        Log.d(TAG, "Parsing LLM response for receipt data...")
        val lines = response.lines()
        var merchantName = "Unknown Merchant"
        var date = Date()
        var total = BigDecimal.ZERO
        val items = mutableListOf<ReceiptItem>()
        
        for (line in lines) {
            val trimmedLine = line.trim()
            when {
                trimmedLine.startsWith("MERCHANT:", ignoreCase = true) -> {
                    merchantName = trimmedLine.substringAfter(":").trim()
                    Log.d(TAG, "Parsed merchant: $merchantName")
                }
                trimmedLine.startsWith("DATE:", ignoreCase = true) -> {
                    val dateStr = trimmedLine.substringAfter(":").trim()
                    try {
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr) ?: Date()
                        Log.d(TAG, "Parsed date: $date")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse date: $dateStr")
                    }
                }
                trimmedLine.startsWith("TOTAL:", ignoreCase = true) -> {
                    val totalStr = trimmedLine.substringAfter(":").trim()
                    try {
                        // Remove currency symbols and parse
                        val cleanTotal = totalStr.replace("$", "").replace(",", "").trim()
                        total = BigDecimal(cleanTotal)
                        Log.d(TAG, "Parsed total: $total")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse total: $totalStr")
                        // Try to extract amount using regex
                        extractAmountFromLine(totalStr)?.let { 
                            total = it
                            Log.d(TAG, "Extracted total using regex: $total")
                        }
                    }
                }
                trimmedLine.startsWith("- ") -> {
                    val parts = trimmedLine.substring(2).split(" | ")
                    if (parts.size >= 4) {
                        try {
                            val item = ReceiptItem(
                                name = parts[0].trim(),
                                quantity = parts[1].trim().toIntOrNull() ?: 1,
                                unitPrice = BigDecimal(parts[2].trim().replace("$", "")),
                                totalPrice = BigDecimal(parts[3].trim().replace("$", ""))
                            )
                            items.add(item)
                            Log.d(TAG, "Parsed item: ${item.name}")
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to parse item: $trimmedLine")
                        }
                    }
                }
            }
        }
        
        // If we still have zero total, try to extract from the raw text or response
        if (total == BigDecimal.ZERO) {
            Log.d(TAG, "Total is still zero, attempting fallback extraction...")
            extractAmountFromLine(response)?.let { 
                total = it
                Log.d(TAG, "Found total in response: $total")
            } ?: run {
                extractAmountFromLine(rawText)?.let { 
                    total = it
                    Log.d(TAG, "Found total in raw text: $total")
                }
            }
        }
        
        val category = categorizeByRules(merchantName, items.map { it.name })
        
        val receipt = Receipt(
            merchantName = merchantName,
            totalAmount = total,
            date = date,
            items = items,
            category = category,
            rawText = rawText
        )
        
        Log.d(TAG, "Final parsed receipt: merchant='${receipt.merchantName}', total='${receipt.totalAmount}', items=${receipt.items.size}")
        return receipt
    }

    private fun createBasicReceipt(rawText: String): Receipt {
        return Receipt(
            merchantName = "Unknown Merchant",
            totalAmount = BigDecimal.ZERO,
            date = Date(),
            rawText = rawText,
            category = ExpenseCategory.UNCATEGORIZED
        )
    }

    private fun createFallbackReceipt(rawText: String, llmResponse: String): Receipt {
        Log.d(TAG, "Creating fallback receipt from raw text and LLM response")
        
        // Try to extract basic information even when structured parsing fails
        var merchantName = "Unknown Merchant"
        var total = BigDecimal.ZERO
        val items = mutableListOf<ReceiptItem>()
        
        // Look for merchant patterns in both raw text and LLM response
        val allText = "$rawText\n$llmResponse"
        val lines = allText.lines()
        
        // Try to find total amount with various patterns
        for (line in lines) {
            val lineLower = line.lowercase()
            
            // Look for merchant name patterns
            if (merchantName == "Unknown Merchant") {
                when {
                    line.contains("store", ignoreCase = true) ||
                    line.contains("market", ignoreCase = true) ||
                    line.contains("shop", ignoreCase = true) -> {
                        merchantName = line.trim().take(50)
                    }
                }
            }
            
            // Look for total patterns - more flexible than the strict parser
            when {
                lineLower.contains("total") && lineLower.contains("$") -> {
                    extractAmountFromLine(line)?.let { total = it }
                }
                lineLower.startsWith("total:") -> {
                    extractAmountFromLine(line.substringAfter(":"))?.let { total = it }
                }
                line.matches(Regex(".*\\$\\d+\\.\\d{2}.*")) -> {
                    extractAmountFromLine(line)?.let { 
                        if (total == BigDecimal.ZERO) total = it 
                    }
                }
            }
        }
        
        Log.d(TAG, "Fallback receipt created: merchant='$merchantName', total='$total'")
        
        return Receipt(
            merchantName = merchantName,
            totalAmount = total,
            date = Date(),
            items = items,
            rawText = rawText,
            category = ExpenseCategory.UNCATEGORIZED
        )
    }
    
    private fun extractAmountFromLine(line: String): BigDecimal? {
        // Look for dollar amounts in various formats
        val patterns = listOf(
            Regex("\\$(\\d+\\.\\d{2})"),
            Regex("(\\d+\\.\\d{2})"),
            Regex("\\$(\\d+)")
        )
        
        for (pattern in patterns) {
            pattern.find(line)?.let { match ->
                try {
                    return BigDecimal(match.groupValues[1])
                } catch (e: Exception) {
                    // Continue to next pattern
                }
            }
        }
        return null
    }

    private fun categorizeByRules(merchantName: String, items: List<String>): ExpenseCategory {
        val merchantLower = merchantName.lowercase()
        val itemsText = items.joinToString(" ").lowercase()
        
        return when {
            merchantLower.contains("walmart") || 
            merchantLower.contains("target") || 
            merchantLower.contains("kroger") ||
            merchantLower.contains("safeway") -> ExpenseCategory.GROCERIES
            
            merchantLower.contains("restaurant") ||
            merchantLower.contains("cafe") ||
            merchantLower.contains("pizza") ||
            merchantLower.contains("burger") -> ExpenseCategory.FOOD_DINING
            
            merchantLower.contains("gas") ||
            merchantLower.contains("shell") ||
            merchantLower.contains("exxon") ||
            merchantLower.contains("uber") ||
            merchantLower.contains("lyft") -> ExpenseCategory.TRANSPORTATION
            
            merchantLower.contains("office") ||
            merchantLower.contains("staples") ||
            itemsText.contains("paper") ||
            itemsText.contains("pen") -> ExpenseCategory.OFFICE_SUPPLIES
            
            merchantLower.contains("pharmacy") ||
            merchantLower.contains("medical") ||
            merchantLower.contains("doctor") ||
            merchantLower.contains("hospital") -> ExpenseCategory.HEALTH_MEDICAL
            
            else -> ExpenseCategory.OTHER
        }
    }

    override fun getModelInfo(): ModelInfo {
        return ModelInfo(
            name = MODEL_NAME,
            version = MODEL_VERSION,
            isReady = llmService.isModelReady()
        )
    }

    // Additional methods not in interface but used internally
    suspend fun downloadModel(onProgress: (Int) -> Unit): Boolean {
        Log.d(TAG, "Starting model download...")
        return try {
            llmService.getModelManager().downloadModel(onProgress)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download model", e)
            false
        }
    }

    fun cleanup() {
        Log.d(TAG, "Cleaning up receipt repository resources...")
        llmService.cleanup()
    }
}
