package com.checkstand.service

import android.content.Context
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraService @Inject constructor(
    private val context: Context
) {
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    init {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    fun setupCamera(
        lifecycleOwner: LifecycleOwner,
        preview: Preview,
        onCameraReady: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                // Image capture use case
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                // Select back camera as default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // Unbind any existing use cases before rebinding
                    cameraProvider.unbindAll()

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )

                    onCameraReady()
                } catch (exc: Exception) {
                    onError(exc)
                }
            } catch (exc: Exception) {
                onError(exc)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun capturePhoto(
        onImageCaptured: (Uri) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val imageCapture = imageCapture ?: run {
            onError(IllegalStateException("Camera not initialized"))
            return
        }

        // Create time stamped name and MediaStore entry
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
            .format(System.currentTimeMillis())
        
        val photoFile = File(
            context.getExternalFilesDir(null),
            "receipt_$name.jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    onImageCaptured(Uri.fromFile(photoFile))
                }

                override fun onError(exception: ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }

    fun shutdown() {
        cameraExecutor.shutdown()
    }
}
