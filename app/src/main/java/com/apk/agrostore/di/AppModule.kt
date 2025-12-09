package com.apk.agrostore.di

import android.content.Context
import com.apk.agrostore.data.repository.AgroRepositoryImpl
import com.apk.agrostore.domain.repository.AgroRepository
import com.apk.agrostore.presentation.healthbot.HealthBotModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency Injection Module for AgroStore application.
 * Provides repository implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the AgroRepository implementation.
     * Using real API implementation with MySQL backend.
     */
    @Provides
    @Singleton
    fun provideAgroRepository(): AgroRepository {
        return AgroRepositoryImpl()
    }

    /**
     * Provides the HealthBotModel instance.
     */
    @Provides
    @Singleton
    fun provideHealthBotModel(@ApplicationContext context: Context): HealthBotModel {
        return HealthBotModel(context)
    }
}