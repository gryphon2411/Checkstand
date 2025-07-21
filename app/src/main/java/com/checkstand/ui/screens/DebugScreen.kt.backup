package com.checkstand.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.checkstand.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    viewModel: ChatViewModel = viewModel(),
    onBackToChat: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var testPrompt by remember { mutableStateOf("Hello! How are you today?") }
    var lastResponse by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Debug Mode",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(onClick = onBackToChat) {
                Text("Back to Chat")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Model Status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    uiState.isModelLoaded -> MaterialTheme.colorScheme.primaryContainer
                    uiState.isModelAvailable -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Model Status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Available: ${uiState.isModelAvailable}",
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Loaded: ${uiState.isModelLoaded}",
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "Loading: ${uiState.isLoading}",
                    fontFamily = FontFamily.Monospace
                )
                
                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick Test
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Quick Test",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = testPrompt,
                    onValueChange = { testPrompt = it },
                    label = { Text("Test Prompt") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { 
                        viewModel.sendMessage(testPrompt)
                        // Get the last message for display
                        lastResponse = uiState.messages.lastOrNull { !it.isUser }?.text ?: "No response yet"
                    },
                    enabled = uiState.isModelLoaded && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Send Test Message")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Recent Messages
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Recent Messages (${uiState.messages.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (uiState.messages.isEmpty()) {
                    Text(
                        text = "No messages yet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    uiState.messages.takeLast(3).forEach { message ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (message.isUser) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else 
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    text = if (message.isUser) "User" else "AI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = message.text,
                                    fontSize = 14.sp,
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }
                
                if (uiState.messages.size > 3) {
                    Text(
                        text = "... and ${uiState.messages.size - 3} more messages",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = viewModel::clearMessages,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Clear Messages")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Model Actions
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Model Actions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (!uiState.isModelAvailable) {
                    Button(
                        onClick = viewModel::downloadModel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Check for Model")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { 
                        // Force re-check model availability
                        viewModel.downloadModel()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Refresh Model Status")
                }
            }
        }
    }
}
