package com.checkstand.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checkstand.domain.model.Receipt
import com.checkstand.service.CameraService
import com.checkstand.service.ModelStatus
import com.checkstand.ui.viewmodel.ReceiptViewModel
import com.checkstand.utils.ImageUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCaptureScreen(
    viewModel: ReceiptViewModel = hiltViewModel(),
    cameraService: CameraService
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val receipts by viewModel.receipts.collectAsStateWithLifecycle()
    val modelStatus by viewModel.modelStatus.collectAsStateWithLifecycle()
    val loadingProgress by viewModel.loadingProgress.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    
    var hasCameraPermission by remember { mutableStateOf(false) }
    var showCamera by remember { mutableStateOf(false) }
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    
    // Camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            showCamera = true
        }
    }
    
    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            val bitmap = ImageUtils.uriToBitmap(context, it, rotateForPortrait = false)
            bitmap?.let { bmp ->
                viewModel.processReceiptImage(bmp)
            }
        }
    }
    
    LaunchedEffect(Unit) {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Receipt Scanner") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        // Model Status Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (modelStatus) {
                    ModelStatus.READY -> MaterialTheme.colorScheme.primaryContainer
                    ModelStatus.LOADING -> MaterialTheme.colorScheme.secondaryContainer
                    ModelStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                    ModelStatus.NOT_LOADED -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (modelStatus) {
                        ModelStatus.READY -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Ready",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        ModelStatus.LOADING -> {
                            CircularProgressIndicator(
                                progress = { loadingProgress },
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        ModelStatus.ERROR -> {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        ModelStatus.NOT_LOADED -> {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Not loaded",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (modelStatus == ModelStatus.READY) FontWeight.Bold else FontWeight.Normal,
                        color = when (modelStatus) {
                            ModelStatus.READY -> MaterialTheme.colorScheme.onPrimaryContainer
                            ModelStatus.LOADING -> MaterialTheme.colorScheme.onSecondaryContainer
                            ModelStatus.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                            ModelStatus.NOT_LOADED -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                if (modelStatus == ModelStatus.LOADING) {
                    LinearProgressIndicator(
                        progress = { loadingProgress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                
                if (modelStatus != ModelStatus.READY) {
                    Text(
                        text = when (modelStatus) {
                            ModelStatus.LOADING -> "Please wait while the AI model loads..."
                            ModelStatus.ERROR -> "Cannot process receipts until model loads successfully"
                            ModelStatus.NOT_LOADED -> "Capture buttons will be enabled when model is ready"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        if (showCamera && hasCameraPermission) {
            // Camera Preview Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also { preview ->
                            previewView = preview
                            val cameraPreview = Preview.Builder().build()
                            cameraPreview.setSurfaceProvider(preview.surfaceProvider)
                            
                            cameraService.setupCamera(
                                lifecycleOwner = lifecycleOwner,
                                preview = cameraPreview,
                                onCameraReady = {},
                                onError = { /* Handle error */ }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Camera controls overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Gallery button
                        FloatingActionButton(
                            onClick = { 
                                if (modelStatus == ModelStatus.READY) {
                                    galleryLauncher.launch("image/*")
                                }
                            },
                            modifier = Modifier.size(56.dp),
                            containerColor = if (modelStatus == ModelStatus.READY) 
                                MaterialTheme.colorScheme.secondary 
                            else 
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        ) {
                            Text(
                                "📷", 
                                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                                color = if (modelStatus == ModelStatus.READY) 
                                    MaterialTheme.colorScheme.onSecondary 
                                else 
                                    MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
                            )
                        }
                        
                        // Capture button
                        FloatingActionButton(
                            onClick = {
                                if (modelStatus == ModelStatus.READY) {
                                    cameraService.capturePhoto(
                                        onImageCaptured = { uri -> 
                                            val bitmap = ImageUtils.uriToBitmap(context, uri, rotateForPortrait = true)
                                            bitmap?.let { bmp ->
                                                viewModel.processReceiptImage(bmp)
                                            }
                                        },
                                        onError = { /* Handle error */ }
                                    )
                                }
                            },
                            modifier = Modifier.size(72.dp),
                            containerColor = if (modelStatus == ModelStatus.READY) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = CircleShape
                        ) {
                            Text(
                                "📸", 
                                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                color = if (modelStatus == ModelStatus.READY) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                            )
                        }
                        
                        // Toggle camera/list button
                        FloatingActionButton(
                            onClick = { showCamera = false },
                            modifier = Modifier.size(56.dp),
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ) {
                            Icon(
                                Icons.Default.List,
                                contentDescription = "View receipts",
                                tint = MaterialTheme.colorScheme.onTertiary
                            )
                        }
                    }
                }
            }
        }
        
        // Processing indicator
        if (uiState.isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Processing receipt...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        // Error message
        uiState.errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { viewModel.clearError() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        // Receipts list or camera toggle
        if (!showCamera || !hasCameraPermission) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Receipts",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                if (hasCameraPermission) {
                    IconButton(onClick = { showCamera = true }) {
                        Text("📸")
                    }
                }
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(receipts) { receipt ->
                    ReceiptCard(
                        receipt = receipt,
                        onDelete = { viewModel.deleteReceipt(receipt.id) },
                        onCategorize = { viewModel.categorizeExpense(receipt) }
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptCard(
    receipt: Receipt,
    onDelete: () -> Unit,
    onCategorize: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        receipt.merchantName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        receipt.date.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    "$${receipt.totalAmount}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Category chip
            if (receipt.category != null) {
                AssistChip(
                    onClick = onCategorize,
                    label = { Text(receipt.category.name) },
                    leadingIcon = {
                        Text("🏷️")
                    }
                )
            } else {
                AssistChip(
                    onClick = onCategorize,
                    label = { Text("Categorize") },
                    leadingIcon = {
                        Text("✨")
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}