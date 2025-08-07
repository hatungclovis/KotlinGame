package com.hatungclovis.kotlingame.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context

/**
 * Room database for the game
 */
@Database(
    entities = [
        GameSettingsEntity::class,
        GameStatisticsEntity::class,
        GameHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GameDatabase : RoomDatabase() {
    
    abstract fun gameSettingsDao(): GameSettingsDao
    abstract fun gameStatisticsDao(): GameStatisticsDao
    abstract fun gameHistoryDao(): GameHistoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null
        
        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "game_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
