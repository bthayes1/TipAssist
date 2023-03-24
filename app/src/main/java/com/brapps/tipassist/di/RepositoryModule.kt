package com.brapps.tipassist.di

import android.content.Context
import com.brapps.tipassist.repository.SettingsRepository
import com.brapps.tipassist.repository.SettingsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideDataStoreRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepositoryImpl(context)
    }
}