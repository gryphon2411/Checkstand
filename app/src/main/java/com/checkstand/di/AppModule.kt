package com.checkstand.di

import android.content.Context
import com.checkstand.data.repository.MediaPipeReceiptRepository
import com.checkstand.domain.repository.ReceiptRepository
import com.checkstand.service.CameraService
import com.checkstand.service.LLMService
import com.checkstand.service.ModelStatusService
import com.checkstand.service.OCRService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing application-level dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides a singleton instance of LLMService
     */
    @Provides
    @Singleton
    fun provideLLMService(
        @ApplicationContext context: Context,
        modelStatusService: ModelStatusService
    ): LLMService {
        return LLMService(context, modelStatusService)
    }

    /**
     * Provides a singleton instance of ReceiptRepository
     */
    @Provides
    @Singleton
    fun provideReceiptRepository(llmService: LLMService): ReceiptRepository {
        return MediaPipeReceiptRepository(llmService)
    }
    
    /**
     * Provides a singleton instance of CameraService
     */
    @Provides
    @Singleton
    fun provideCameraService(@ApplicationContext context: Context): CameraService {
        return CameraService(context)
    }
    
    /**
     * Provides a singleton instance of OCRService
     */
    @Provides
    @Singleton
    fun provideOCRService(@ApplicationContext context: Context): OCRService {
        return OCRService(context)
    }
}
