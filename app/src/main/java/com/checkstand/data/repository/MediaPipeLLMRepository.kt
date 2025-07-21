package com.checkstand.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.checkstand.domain.model.Receipt
import com.checkstand.domain.model.ExpenseCategory
import com.checkstand.domain.model.ReceiptItem
import com.checkstand.domain.repository.ReceiptRepository
import com.checkstand.domain.repository.ModelInfo
import com.checkstand.service.LLMService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of ReceiptRepository using MediaPipe LLM Service
 */
class MediaPipeReceiptRepository(
    private val llmService: LLMService
) : ReceiptRepository {
    
    companion object {
        private const val TAG = "MediaPipeReceiptRepo"
        private const val MODEL_NAME = "Gemma-3n E4B"
        private const val MODEL_VERSION = "int4"
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

    override fun analyzeReceiptText(rawText: String): Flow<Receipt> {
        Log.d(TAG, "Analyzing receipt text: ${rawText.take(100)}...")
        
        return flow {
            val prompt = buildReceiptAnalysisPrompt(rawText)
            
            llmService.generateResponse(prompt).collect { response ->
                try {
                    val receipt = parseReceiptFromResponse(response, rawText)
                    emit(receipt)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse receipt from response", e)
                    // Emit a basic receipt with raw text
                    emit(createBasicReceipt(rawText))
                }
            }
        }
    }
    
    override fun analyzeReceiptImage(image: Bitmap): Flow<Receipt> {
        Log.d(TAG, "Analyzing receipt image directly...")
        
        return flow {
            // For now, we'll use a text-based prompt since the current MediaPipe model
            // may not support multimodal input. This is prepared for future multimodal models.
            val prompt = """
                I have captured a receipt image that I need to analyze. 
                Please help me structure the receipt information in this format:
                
                MERCHANT: [merchant name]
                DATE: [date in yyyy-MM-dd format]
                TOTAL: [total amount as decimal number]
                ITEMS:
                - [item name] | [quantity] | [unit price] | [total price]
                
                Since I cannot provide the image directly to this text model, 
                please provide a template response that shows the expected format
                for receipt analysis.
            """.trimIndent()
            
            llmService.generateResponseWithImage(prompt, image).collect { response ->
                try {
                    // For now, create a basic receipt indicating image was processed
                    val receipt = Receipt(
                        id = java.util.UUID.randomUUID().toString(),
                        merchantName = "Image Captured",
                        totalAmount = java.math.BigDecimal.ZERO,
                        date = java.util.Date(),
                        items = listOf(
                            com.checkstand.domain.model.ReceiptItem(
                                name = "Receipt from image - pending analysis",
                                quantity = 1,
                                unitPrice = java.math.BigDecimal.ZERO
                            )
                        ),
                        category = com.checkstand.domain.model.ExpenseCategory.UNCATEGORIZED,
                        rawText = "Receipt processed from image"
                    )
                    emit(receipt)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to process receipt image", e)
                    emit(createBasicReceipt("Receipt image processed"))
                }
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
        val lines = response.lines()
        var merchantName = "Unknown Merchant"
        var date = Date()
        var total = BigDecimal.ZERO
        val items = mutableListOf<ReceiptItem>()
        
        for (line in lines) {
            when {
                line.startsWith("MERCHANT:") -> {
                    merchantName = line.substringAfter("MERCHANT:").trim()
                }
                line.startsWith("DATE:") -> {
                    val dateStr = line.substringAfter("DATE:").trim()
                    try {
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr) ?: Date()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse date: $dateStr")
                    }
                }
                line.startsWith("TOTAL:") -> {
                    val totalStr = line.substringAfter("TOTAL:").trim()
                    try {
                        total = BigDecimal(totalStr)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to parse total: $totalStr")
                    }
                }
                line.startsWith("- ") -> {
                    val parts = line.substring(2).split(" | ")
                    if (parts.size >= 4) {
                        try {
                            val item = ReceiptItem(
                                name = parts[0].trim(),
                                quantity = parts[1].trim().toIntOrNull() ?: 1,
                                unitPrice = BigDecimal(parts[2].trim()),
                                totalPrice = BigDecimal(parts[3].trim())
                            )
                            items.add(item)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to parse item: $line")
                        }
                    }
                }
            }
        }
        
        val category = categorizeByRules(merchantName, items.map { it.name })
        
        return Receipt(
            merchantName = merchantName,
            totalAmount = total,
            date = date,
            items = items,
            category = category,
            rawText = rawText
        )
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

    override suspend fun downloadModel(onProgress: (Int) -> Unit): Boolean {
        Log.d(TAG, "Starting model download...")
        return try {
            llmService.getModelManager().downloadModel(onProgress)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download model", e)
            false
        }
    }

    override fun getModelInfo(): ModelInfo {
        val modelManager = llmService.getModelManager()
        val modelFile = if (modelManager.isModelAvailable()) {
            modelManager.getModelFile()
        } else null
        
        return ModelInfo(
            name = MODEL_NAME,
            size = modelFile?.length() ?: 0L,
            path = modelFile?.absolutePath ?: "Not available",
            isLoaded = llmService.isModelReady()
        )
    }

    override fun cleanup() {
        Log.d(TAG, "Cleaning up receipt repository resources...")
        llmService.cleanup()
    }
}
