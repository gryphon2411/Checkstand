package com.checkstand

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CheckstandApplication : Application() {
    
    companion object {
        private const val TAG = "CheckstandApp"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Checkstand application started")
        
        // Request large heap for model loading
        System.setProperty("dalvik.vm.heapsize", "512m")
        
        // Set thread priority for background operations
        Thread.currentThread().priority = Thread.NORM_PRIORITY
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "Low memory warning - consider freeing resources")
        // Force garbage collection
        System.gc()
    }
    
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.w(TAG, "Memory trim requested at level: $level")
        when (level) {
            TRIM_MEMORY_RUNNING_CRITICAL,
            TRIM_MEMORY_COMPLETE -> {
                // Aggressive memory cleanup
                System.gc()
            }
        }
    }
}
