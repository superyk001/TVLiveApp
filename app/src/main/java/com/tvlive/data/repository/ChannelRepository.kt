package com.tvlive.data.repository

import com.tvlive.data.local.ChannelDao
import com.tvlive.data.model.Channel
import com.tvlive.data.model.EPGProgram
import com.tvlive.data.model.PlayHistory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 频道仓库类
 * 处理所有频道相关的数据操作
 */
@Singleton
class ChannelRepository @Inject constructor(
    private val channelDao: ChannelDao
) {
    
    // ==================== 频道操作 ====================
    
    fun getAllChannels(): Flow<List<Channel>> = channelDao.getAllChannels()
    
    fun getChannelsByCategory(category: String): Flow<List<Channel>> = 
        channelDao.getChannelsByCategory(category)
    
    fun getFavoriteChannels(): Flow<List<Channel>> = channelDao.getFavoriteChannels()
    
    suspend fun getChannelById(id: Long): Channel? = channelDao.getChannelById(id)
    
    fun getAllCategories(): Flow<List<String>> = channelDao.getAllCategories()
    
    fun searchChannels(query: String): Flow<List<Channel>> = channelDao.searchChannels(query)
    
    suspend fun addChannel(channel: Channel) {
        channelDao.insertChannel(channel)
    }
    
    suspend fun addChannels(channels: List<Channel>) {
        channelDao.insertChannels(channels)
    }
    
    suspend fun updateChannel(channel: Channel) {
        channelDao.updateChannel(channel)
    }
    
    suspend fun deleteChannel(channel: Channel) {
        channelDao.deleteChannel(channel)
    }
    
    suspend fun deleteAllChannels() {
        channelDao.deleteAllChannels()
    }
    
    suspend fun toggleFavorite(channel: Channel) {
        channelDao.updateFavoriteStatus(channel.id, !channel.isFavorite)
    }
    
    suspend fun updateLastPlayed(channelId: Long) {
        channelDao.updateLastPlayed(channelId, System.currentTimeMillis())
    }
    
    // ==================== 播放历史 ====================
    
    fun getPlayHistory(limit: Int = 50): Flow<List<PlayHistory>> = 
        channelDao.getPlayHistory(limit)
    
    suspend fun addPlayHistory(history: PlayHistory) {
        channelDao.insertPlayHistory(history)
    }
    
    suspend fun clearPlayHistory() {
        channelDao.clearPlayHistory()
    }
    
    // ==================== EPG ====================
    
    suspend fun getCurrentProgram(channelId: String): EPGProgram? =
        channelDao.getCurrentProgram(channelId)
    
    suspend fun getUpcomingPrograms(channelId: String): List<EPGProgram> =
        channelDao.getUpcomingPrograms(channelId)
    
    suspend fun savePrograms(programs: List<EPGProgram>) {
        channelDao.insertPrograms(programs)
    }
}
