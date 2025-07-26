package com.checkstand.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Extension to create DataStore instance
val Context.receiptDataStore: DataStore<Preferences> by preferencesDataStore(name = "receipts")
