package com.checkstand.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import java.io.IOException

object ImageUtils {
    private const val TAG = "ImageUtils"
    private const val MAX_IMAGE_WIDTH = 1024
    private const val MAX_IMAGE_HEIGHT = 1024
    
    /**
     * Converts URI to Bitmap with proper sizing for AI processing
     * @param context Application context
     * @param uri Image URI
     * @param rotateForPortrait Whether to rotate landscape images to portrait
     * @return Bitmap or null if conversion fails
     */
    fun uriToBitmap(
        context: Context,
        uri: Uri,
        rotateForPortrait: Boolean = false
    ): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (originalBitmap == null) {
                Log.e(TAG, "Failed to decode image from URI: $uri")
                return null
            }
            
            var processedBitmap = originalBitmap
            
            // Handle rotation for camera images if requested
            if (rotateForPortrait && originalBitmap.width > originalBitmap.height) {
                Log.d(TAG, "Rotating landscape image to portrait")
                val matrix = Matrix()
                matrix.postRotate(90f)
                processedBitmap = Bitmap.createBitmap(
                    originalBitmap, 0, 0, 
                    originalBitmap.width, originalBitmap.height, 
                    matrix, true
                )
                originalBitmap.recycle()
            }
            
            // Resize if needed to optimize for AI processing
            val resizedBitmap = resizeBitmapForAI(processedBitmap)
            
            if (resizedBitmap != processedBitmap) {
                processedBitmap.recycle()
            }
            
            Log.d(TAG, "Successfully converted URI to bitmap: ${resizedBitmap.width}x${resizedBitmap.height}")
            resizedBitmap
            
        } catch (e: IOException) {
            Log.e(TAG, "IOException while converting URI to bitmap", e)
            null
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException - permission denied", e)
            null
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory while processing image", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error converting URI to bitmap", e)
            null
        }
    }
    
    /**
     * Resizes bitmap to optimal dimensions for AI processing
     * @param bitmap Input bitmap
     * @return Resized bitmap optimized for AI
     */
    fun resizeBitmapForAI(bitmap: Bitmap): Bitmap {
        val originalWidth = bitmap.width
        val originalHeight = bitmap.height
        
        // Calculate scaling ratio
        val ratio = minOf(
            MAX_IMAGE_WIDTH.toFloat() / originalWidth,
            MAX_IMAGE_HEIGHT.toFloat() / originalHeight
        )
        
        // If image is already smaller than max dimensions, return as-is
        if (ratio >= 1f) {
            Log.d(TAG, "Image already optimal size: ${originalWidth}x${originalHeight}")
            return bitmap
        }
        
        val newWidth = (originalWidth * ratio).toInt()
        val newHeight = (originalHeight * ratio).toInt()
        
        Log.d(TAG, "Resizing image from ${originalWidth}x${originalHeight} to ${newWidth}x${newHeight}")
        
        return try {
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Out of memory while resizing bitmap", e)
            bitmap // Return original if resizing fails
        }
    }
    
    /**
     * Safely recycles bitmap if not null
     * @param bitmap Bitmap to recycle
     */
    fun recycleBitmap(bitmap: Bitmap?) {
        bitmap?.takeIf { !it.isRecycled }?.recycle()
    }
}
