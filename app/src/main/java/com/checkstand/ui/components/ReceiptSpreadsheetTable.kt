package com.checkstand.ui.components

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.checkstand.domain.model.Receipt
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

enum class SortColumn {
    DATE, MERCHANT, AMOUNT
}

enum class SortDirection {
    ASC, DESC
}

@Composable
fun ReceiptSpreadsheetTable(
    receipts: List<Receipt>,
    onDeleteReceipt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var sortColumn by remember { mutableStateOf(SortColumn.DATE) }
    var sortDirection by remember { mutableStateOf(SortDirection.DESC) }
    val context = LocalContext.current
    
    // Sort receipts based on current sorting criteria
    val sortedReceipts = remember(receipts, sortColumn, sortDirection) {
        receipts.sortedWith(compareBy<Receipt> {
            when (sortColumn) {
                SortColumn.DATE -> it.date.time
                SortColumn.MERCHANT -> it.merchantName.lowercase()
                SortColumn.AMOUNT -> it.totalAmount
            }
        }.let { comparator ->
            if (sortDirection == SortDirection.DESC) comparator.reversed() else comparator
        })
    }
    
    // Calculate running totals
    val totalAmount = remember(receipts) {
        receipts.sumOf { it.totalAmount }
    }

    Column(modifier = modifier) {
        // Header with statistics
        SpreadsheetStatistics(
            totalAmount = totalAmount,
            receiptCount = receipts.size,
            onExport = { exportReceipts(context, sortedReceipts) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Table header and content
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                // Table Header
                SpreadsheetHeader(
                    sortColumn = sortColumn,
                    sortDirection = sortDirection,
                    onSortChanged = { column ->
                        if (sortColumn == column) {
                            sortDirection = if (sortDirection == SortDirection.ASC) 
                                SortDirection.DESC else SortDirection.ASC
                        } else {
                            sortColumn = column
                            sortDirection = SortDirection.ASC
                        }
                    }
                )
                
                // Divider
                HorizontalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
                
                // Table Rows
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    itemsIndexed(sortedReceipts) { index, receipt ->
                        SpreadsheetRow(
                            receipt = receipt,
                            isEven = index % 2 == 0,
                            onDelete = { onDeleteReceipt(receipt.id) }
                        )
                        
                        if (index < sortedReceipts.size - 1) {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpreadsheetStatistics(
    totalAmount: BigDecimal,
    receiptCount: Int,
    onExport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Total Spent",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$${totalAmount}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Total Receipts",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        receiptCount.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Export Button
                if (receiptCount > 0) {
                    OutlinedButton(
                        onClick = onExport,
                        modifier = Modifier.size(width = 80.dp, height = 36.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Export",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpreadsheetHeader(
    sortColumn: SortColumn,
    sortDirection: SortDirection,
    onSortChanged: (SortColumn) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Date Column
        SpreadsheetHeaderCell(
            text = "Date",
            weight = 0.3f,
            sortColumn = SortColumn.DATE,
            currentSort = sortColumn,
            sortDirection = sortDirection,
            onClick = { onSortChanged(SortColumn.DATE) }
        )
        
        // Merchant Column
        SpreadsheetHeaderCell(
            text = "Merchant",
            weight = 0.4f,
            sortColumn = SortColumn.MERCHANT,
            currentSort = sortColumn,
            sortDirection = sortDirection,
            onClick = { onSortChanged(SortColumn.MERCHANT) }
        )
        
        // Amount Column
        SpreadsheetHeaderCell(
            text = "Amount",
            weight = 0.25f,
            sortColumn = SortColumn.AMOUNT,
            currentSort = sortColumn,
            sortDirection = sortDirection,
            onClick = { onSortChanged(SortColumn.AMOUNT) }
        )
        
        // Actions Column
        Box(
            modifier = Modifier.weight(0.05f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "⚙️",
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun RowScope.SpreadsheetHeaderCell(
    text: String,
    weight: Float,
    sortColumn: SortColumn,
    currentSort: SortColumn,
    sortDirection: SortDirection,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .weight(weight)
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        if (currentSort == sortColumn) {
            Icon(
                imageVector = if (sortDirection == SortDirection.ASC) 
                    Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun SpreadsheetRow(
    receipt: Receipt,
    isEven: Boolean,
    onDelete: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MM/dd", Locale.getDefault()) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isEven) Color.Transparent 
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date
        Text(
            text = dateFormatter.format(receipt.date),
            modifier = Modifier.weight(0.3f),
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // Merchant
        Text(
            text = receipt.merchantName,
            modifier = Modifier.weight(0.4f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // Amount
        Text(
            text = "$${receipt.totalAmount}",
            modifier = Modifier.weight(0.25f),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.End
        )
        
        // Delete Action
        IconButton(
            onClick = onDelete,
            modifier = Modifier
                .weight(0.05f)
                .size(20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete receipt",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun exportReceipts(context: Context, receipts: List<Receipt>) {
    try {
        val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        
        // Create CSV content with proper formatting and escaping
        val csvContent = buildString {
            // Header
            appendLine("Date,Merchant,Amount")
            
            // Data rows
            receipts.forEach { receipt ->
                val date = dateFormatter.format(receipt.date)
                val merchant = receipt.merchantName.replace("\"", "\"\"") // Escape quotes
                val amount = receipt.totalAmount.toString()
                appendLine("\"$date\",\"$merchant\",\"$amount\"")
            }
        }
        
        // Create temporary file
        val cacheDir = File(context.cacheDir, "exports")
        cacheDir.mkdirs()
        val file = File(cacheDir, "receipts_export_$timestamp.csv")
        
        // Write CSV content to file
        FileWriter(file).use { writer ->
            writer.write(csvContent)
        }
        
        // Create file URI using FileProvider
        val fileUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        // Create share intent with proper CSV file
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_SUBJECT, "Receipt Export - ${receipts.size} receipts")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(shareIntent, "Export receipts to...")
        context.startActivity(chooser)
        
    } catch (e: Exception) {
        // Fallback to simple text sharing if file creation fails
        val dateFormatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
        val tsvContent = buildString {
            appendLine("Date\tMerchant\tAmount")
            receipts.forEach { receipt ->
                val date = dateFormatter.format(receipt.date)
                val merchant = receipt.merchantName
                val amount = receipt.totalAmount.toString()
                appendLine("$date\t$merchant\t$amount")
            }
        }
        
        val fallbackIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, tsvContent)
            putExtra(Intent.EXTRA_SUBJECT, "Receipt Export - ${receipts.size} receipts")
        }
        
        val chooser = Intent.createChooser(fallbackIntent, "Export receipts to...")
        context.startActivity(chooser)
    }
}
