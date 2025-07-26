package com.checkstand.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.checkstand.data.local.receiptDataStore
import com.checkstand.domain.model.Receipt
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing receipt data persistence using DataStore
 */
@Singleton
class ReceiptDataRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {

    private val receiptsKey = stringPreferencesKey("receipts_list")

    /**
     * Get all receipts as a Flow
     */
    fun getAllReceipts(): Flow<List<Receipt>> {
        return context.receiptDataStore.data.map { preferences ->
            val receiptsJson = preferences[receiptsKey] ?: "[]"
            try {
                val type = object : TypeToken<List<Receipt>>() {}.type
                gson.fromJson<List<Receipt>>(receiptsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Insert a new receipt
     */
    suspend fun insertReceipt(receipt: Receipt) {
        context.receiptDataStore.edit { preferences ->
            val currentReceiptsJson = preferences[receiptsKey] ?: "[]"
            val currentReceipts = try {
                val type = object : TypeToken<List<Receipt>>() {}.type
                gson.fromJson<List<Receipt>>(currentReceiptsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            
            val updatedReceipts = currentReceipts + receipt
            preferences[receiptsKey] = gson.toJson(updatedReceipts)
        }
    }

    /**
     * Update an existing receipt
     */
    suspend fun updateReceipt(receipt: Receipt) {
        context.receiptDataStore.edit { preferences ->
            val currentReceiptsJson = preferences[receiptsKey] ?: "[]"
            val currentReceipts = try {
                val type = object : TypeToken<List<Receipt>>() {}.type
                gson.fromJson<List<Receipt>>(currentReceiptsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            
            val updatedReceipts = currentReceipts.map { existingReceipt ->
                if (existingReceipt.id == receipt.id) receipt else existingReceipt
            }
            preferences[receiptsKey] = gson.toJson(updatedReceipts)
        }
    }

    /**
     * Delete a receipt by ID
     */
    suspend fun deleteReceiptById(receiptId: String) {
        context.receiptDataStore.edit { preferences ->
            val currentReceiptsJson = preferences[receiptsKey] ?: "[]"
            val currentReceipts = try {
                val type = object : TypeToken<List<Receipt>>() {}.type
                gson.fromJson<List<Receipt>>(currentReceiptsJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
            
            val updatedReceipts = currentReceipts.filter { it.id != receiptId }
            preferences[receiptsKey] = gson.toJson(updatedReceipts)
        }
    }

    /**
     * Get a specific receipt by ID
     */
    fun getReceiptById(receiptId: String): Flow<Receipt?> {
        return context.receiptDataStore.data.map { preferences ->
            val receiptsJson = preferences[receiptsKey] ?: "[]"
            try {
                val type = object : TypeToken<List<Receipt>>() {}.type
                val receipts = gson.fromJson<List<Receipt>>(receiptsJson, type) ?: emptyList()
                receipts.find { it.id == receiptId }
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Delete all receipts
     */
    suspend fun deleteAllReceipts() {
        context.receiptDataStore.edit { preferences ->
            preferences[receiptsKey] = "[]"
        }
    }

    /**
     * Get receipts count
     */
    fun getReceiptsCount(): Flow<Int> {
        return context.receiptDataStore.data.map { preferences ->
            val receiptsJson = preferences[receiptsKey] ?: "[]"
            try {
                val type = object : TypeToken<List<Receipt>>() {}.type
                val receipts = gson.fromJson<List<Receipt>>(receiptsJson, type) ?: emptyList()
                receipts.size
            } catch (e: Exception) {
                0
            }
        }
    }
}
