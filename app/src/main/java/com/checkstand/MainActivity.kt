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
import com.checkstand.ui.screens.ChatScreen
import com.checkstand.ui.screens.DebugScreen
import com.checkstand.ui.screens.SetupScreen
import com.checkstand.ui.theme.CheckstandTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CheckstandTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CheckstandApp()
                }
            }
        }
    }
}

@Composable
fun CheckstandApp() {
    var showSetup by remember { mutableStateOf(true) }
    var showDebug by remember { mutableStateOf(false) }
    
    when {
        showSetup -> {
            SetupScreen(
                onContinueClick = { showSetup = false }
            )
        }
        showDebug -> {
            DebugScreen(
                onBackToChat = { showDebug = false }
            )
        }
        else -> {
            ChatScreen(
                onDebugClick = { showDebug = true }
            )
        }
    }
}