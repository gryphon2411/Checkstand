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
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checkstand.R
import com.checkstand.domain.model.Receipt
import com.checkstand.domain.model.ReceiptStatus
import com.checkstand.service.CameraService
import com.checkstand.service.ModelStatus
import com.checkstand.ui.components.ReceiptSpreadsheetTable
import com.checkstand.ui.viewmodel.ReceiptViewModel
import com.checkstand.utils.ImageUtils
import java.text.SimpleDateFormat
import java.util.*

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
    
    // Queue processing states
    val isQueueProcessing by viewModel.isQueueProcessing.collectAsStateWithLifecycle()
    val currentProcessingId by viewModel.currentProcessingId.collectAsStateWithLifecycle()
    
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
            title = { Text("Checkstand") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        
        // Enhanced Model Status Chip with processing state
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            AssistChip(
                onClick = { },
                label = { 
                    Text(
                        when {
                            isQueueProcessing -> "Gemma 3n"
                            modelStatus == ModelStatus.READY -> "Gemma 3n"
                            modelStatus == ModelStatus.LOADING -> "Loading Gemma 3n"
                            modelStatus == ModelStatus.ERROR -> "Model Error"
                            modelStatus == ModelStatus.NOT_LOADED -> "Loading"
                            else -> "Unknown"
                        }
                    )
                },
                leadingIcon = {
                    when {
                        isQueueProcessing -> CircularProgressIndicator(
                            modifier = Modifier.size(16.dp)
                        )
                        modelStatus == ModelStatus.READY -> Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Ready",
                            modifier = Modifier.size(16.dp)
                        )
                        modelStatus == ModelStatus.LOADING -> CircularProgressIndicator(
                            progress = { loadingProgress },
                            modifier = Modifier.size(16.dp)
                        )
                        modelStatus == ModelStatus.ERROR -> Icon(
                            Icons.Default.Warning,
                            contentDescription = "Error",
                            modifier = Modifier.size(16.dp)
                        )
                        modelStatus == ModelStatus.NOT_LOADED -> CircularProgressIndicator(
                            modifier = Modifier.size(16.dp)
                        )
                        else -> null
                    }
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = when {
                        isQueueProcessing -> MaterialTheme.colorScheme.secondaryContainer
                        modelStatus == ModelStatus.READY -> MaterialTheme.colorScheme.primaryContainer
                        modelStatus == ModelStatus.LOADING -> MaterialTheme.colorScheme.secondaryContainer
                        modelStatus == ModelStatus.ERROR -> MaterialTheme.colorScheme.errorContainer
                        modelStatus == ModelStatus.NOT_LOADED -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            )
        }
        
        if (showCamera && hasCameraPermission) {
            // Camera Preview Section (without button overlay)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
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
            }
            
            // Camera controls below preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery button with universally supported emoji
                FloatingActionButton(
                    onClick = { 
                        if (modelStatus == ModelStatus.READY) {
                            galleryLauncher.launch("image/*")
                        }
                    },
                    modifier = Modifier.size(56.dp),
                    containerColor = if (modelStatus == ModelStatus.READY) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = "Gallery",
                        tint = MaterialTheme.colorScheme.onPrimary
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
                    Icon(
                        Icons.Default.Camera,
                        contentDescription = "Capture Photo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Toggle camera/list button
                FloatingActionButton(
                    onClick = { showCamera = false },
                    modifier = Modifier.size(56.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = "View receipts",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        
        // Compact success message for processed receipt (only show completed receipts)
        uiState.lastProcessedReceipt?.let { receipt ->
            if (receipt.status == ReceiptStatus.COMPLETED && receipt.merchantName != "Processing...") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AssistChip(
                        onClick = { viewModel.clearLastProcessedReceipt() },
                        label = { 
                            Text("${receipt.merchantName} • $${receipt.totalAmount}")
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.primary,
                            trailingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
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
        
        // Financial Spreadsheet or camera toggle
        if (!showCamera || !hasCameraPermission) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Recent Receipts",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (hasCameraPermission) {
                    FloatingActionButton(
                        onClick = { showCamera = true },
                        modifier = Modifier.size(56.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Camera",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            // New Educational Spreadsheet Table
            ReceiptSpreadsheetTable(
                receipts = receipts,
                onDeleteReceipt = { receiptId -> viewModel.deleteReceipt(receiptId) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptCard(
    receipt: Receipt,
    onDelete: () -> Unit,
    isNew: Boolean = false
) {
    val isRecentlyProcessed = System.currentTimeMillis() - receipt.createdAt < 10000 // 10 seconds
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isRecentlyProcessed) 8.dp else 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRecentlyProcessed) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // "NEW" badge for recently processed receipts
            if (isRecentlyProcessed) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text("NEW", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Text("✨") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            labelColor = MaterialTheme.colorScheme.onPrimary,
                            leadingIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
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
                        formatDate(receipt.date),
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
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

// Helper function to format date without time
private fun formatDate(date: Date): String {
    val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    return formatter.format(date)
}
