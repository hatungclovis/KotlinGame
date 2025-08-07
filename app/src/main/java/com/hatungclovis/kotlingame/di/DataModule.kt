package com.hatungclovis.kotlingame.di

import android.content.Context
import com.google.gson.Gson
import com.hatungclovis.kotlingame.data.local.GameDatabase
import com.hatungclovis.kotlingame.data.repository.GameRepositoryImpl
import com.hatungclovis.kotlingame.data.repository.WordRepositoryImpl
import com.hatungclovis.kotlingame.data.source.WordDataSource
import com.hatungclovis.kotlingame.domain.repository.GameRepository
import com.hatungclovis.kotlingame.domain.repository.WordRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for data layer components
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }
    
    @Provides
    @Singleton
    fun provideGameDatabase(
        @ApplicationContext context: Context
    ): GameDatabase {
        return GameDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideWordDataSource(
        @ApplicationContext context: Context
    ): WordDataSource {
        return WordDataSource(context)
    }
    
    @Provides
    @Singleton
    fun provideWordRepository(
        wordDataSource: WordDataSource
    ): WordRepository {
        return WordRepositoryImpl(wordDataSource)
    }
    
    @Provides
    @Singleton
    fun provideGameRepository(
        @ApplicationContext context: Context,
        gameDatabase: GameDatabase,
        gson: Gson
    ): GameRepository {
        return GameRepositoryImpl(context, gameDatabase, gson)
    }
}
