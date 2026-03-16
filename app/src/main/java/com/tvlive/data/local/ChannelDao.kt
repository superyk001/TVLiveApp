package com.tvlive.data.local

import androidx.room.*
import com.tvlive.data.model.Channel
import com.tvlive.data.model.PlayHistory
import com.tvlive.data.model.EPGProgram
import com.tvlive.data.model.ChannelSource
import kotlinx.coroutines.flow.Flow

/**
 * 频道数据访问对象
 */
@Dao
interface ChannelDao {
    
    // ==================== 频道操作 ====================
    
    @Query("SELECT * FROM channels ORDER BY `order` ASC, name ASC")
    fun getAllChannels(): Flow<List<Channel>>
    
    @Query("SELECT * FROM channels WHERE category = :category ORDER BY `order` ASC, name ASC")
    fun getChannelsByCategory(category: String): Flow<List<Channel>>
    
    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteChannels(): Flow<List<Channel>>
    
    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun getChannelById(id: Long): Channel?
    
    @Query("SELECT DISTINCT category FROM channels ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
    
    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchChannels(query: String): Flow<List<Channel>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: Channel): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<Channel>)
    
    @Update
    suspend fun updateChannel(channel: Channel)
    
    @Delete
    suspend fun deleteChannel(channel: Channel)
    
    @Query("DELETE FROM channels")
    suspend fun deleteAllChannels()
    
    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)
    
    @Query("UPDATE channels SET lastPlayed = :timestamp WHERE id = :id")
    suspend fun updateLastPlayed(id: Long, timestamp: Long)
    
    // ==================== 播放历史 ====================
    
    @Query("SELECT * FROM play_history ORDER BY playedAt DESC LIMIT :limit")
    fun getPlayHistory(limit: Int = 50): Flow<List<PlayHistory>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayHistory(history: PlayHistory)
    
    @Query("DELETE FROM play_history WHERE channelId = :channelId")
    suspend fun deleteHistoryByChannel(channelId: Long)
    
    @Query("DELETE FROM play_history")
    suspend fun clearPlayHistory()
    
    // ==================== 频道源 ====================
    
    @Query("SELECT * FROM channel_sources WHERE isEnabled = 1")
    fun getEnabledSources(): Flow<List<ChannelSource>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: ChannelSource)
    
    @Delete
    suspend fun deleteSource(source: ChannelSource)
    
    // ==================== EPG ====================
    
    @Query("SELECT * FROM epg_programs WHERE channelId = :channelId AND startTime <= :now AND endTime > :now")
    suspend fun getCurrentProgram(channelId: String, now: Long = System.currentTimeMillis()): EPGProgram?
    
    @Query("SELECT * FROM epg_programs WHERE channelId = :channelId AND startTime > :now ORDER BY startTime ASC LIMIT 5")
    suspend fun getUpcomingPrograms(channelId: String, now: Long = System.currentTimeMillis()): List<EPGProgram>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<EPGProgram>)
    
    @Query("DELETE FROM epg_programs WHERE endTime < :timestamp")
    suspend fun deleteExpiredPrograms(timestamp: Long = System.currentTimeMillis())
}
