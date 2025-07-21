package com.checkstand

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.checkstand.service.CameraService
import com.checkstand.ui.screens.InvoiceCaptureScreen
import com.checkstand.ui.screens.SetupScreen
import com.checkstand.ui.theme.CheckstandTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var cameraService: CameraService
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CheckstandTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CheckstandApp(cameraService)
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraService.shutdown()
    }
}

@Composable
fun CheckstandApp(cameraService: CameraService) {
    var showSetup by remember { mutableStateOf(false) } // Skip setup for now
    
    when {
        showSetup -> {
            SetupScreen(
                onContinueClick = { showSetup = false }
            )
        }
        else -> {
            InvoiceCaptureScreen(
                cameraService = cameraService
            )
        }
    }
}