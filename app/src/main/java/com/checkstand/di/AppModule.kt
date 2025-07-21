package com.checkstand.di

import android.content.Context
import com.checkstand.data.repository.MediaPipeLLMRepository
import com.checkstand.domain.repository.LLMRepository
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
     * Provides a singleton instance of LLMRepository
     */
    @Provides
    @Singleton
    fun provideLLMRepository(
        @ApplicationContext context: Context
    ): LLMRepository {
        return MediaPipeLLMRepository(context)
    }
}
