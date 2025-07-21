package com.checkstand.service

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class OCRService @Inject constructor(
    private val context: Context
) {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun extractTextFromImage(imageUri: Uri): String = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromFilePath(context, imageUri)
            
            textRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extractedText = visionText.text
                    continuation.resume(extractedText)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        } catch (e: Exception) {
            continuation.resumeWithException(e)
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
