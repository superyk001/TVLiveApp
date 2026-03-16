package com.tvlive.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tvlive.data.model.Channel
import com.tvlive.data.model.EPGProgram
import com.tvlive.data.model.PlayHistory
import com.tvlive.data.model.ChannelSource

/**
 * Room 数据库
 */
@Database(
    entities = [
        Channel::class,
        PlayHistory::class,
        EPGProgram::class,
        ChannelSource::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun channelDao(): ChannelDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tvlive_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
