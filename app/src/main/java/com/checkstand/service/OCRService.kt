package com.checkstand.service

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class OCRService @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "OCRService"
        private const val OCR_TIMEOUT_MS = 30_000L // 30 seconds timeout for OCR
    }

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractTextFromImage(imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                val image = InputImage.fromFilePath(context, imageUri)
                val result = withTimeout(OCR_TIMEOUT_MS) {
                    textRecognizer.process(image).await()
                }
                result.text
            } catch (e: TimeoutCancellationException) {
                Log.w(TAG, "OCR extraction from URI timed out after ${OCR_TIMEOUT_MS}ms")
                ""
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting text from image URI", e)
                ""
            }
        }
    }

    suspend fun extractTextFromImage(bitmap: Bitmap): String {
        return withContext(Dispatchers.IO) {
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val result = withTimeout(OCR_TIMEOUT_MS) {
                    textRecognizer.process(image).await()
                }
                result.text
            } catch (e: TimeoutCancellationException) {
                Log.w(TAG, "OCR extraction from bitmap timed out after ${OCR_TIMEOUT_MS}ms")
                ""
            } catch (e: Exception) {
                Log.e(TAG, "Error extracting text from bitmap", e)
                ""
            }
        }
    }

    suspend fun extractStructuredTextFromImage(imageUri: Uri): StructuredTextResult = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val blocks = visionText.textBlocks.map { block ->
                        TextBlock(
                            text = block.text,
                            confidence = 1.0f, // ML Kit doesn't provide confidence scores
                            boundingBox = block.boundingBox,
                            lines = block.lines.map { line ->
                                TextLine(
                                    text = line.text,
                                    confidence = 1.0f,
                                    boundingBox = line.boundingBox
                                )
                            }
                        )
                    }
                    
                    val result = StructuredTextResult(
                        fullText = visionText.text,
                        blocks = blocks
                    )
                    
                    continuation.resume(result)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }

    fun shutdown() {
        textRecognizer.close()
    }
}

/**
 * Extension function to use await with Tasks
 */
suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
    addOnCanceledListener {
        continuation.cancel()
    }
}

data class StructuredTextResult(
    val fullText: String,
    val blocks: List<TextBlock>
)

data class TextBlock(
    val text: String,
    val confidence: Float,
    val boundingBox: android.graphics.Rect?,
    val lines: List<TextLine>
)

data class TextLine(
    val text: String,
    val confidence: Float,
    val boundingBox: android.graphics.Rect?
)
