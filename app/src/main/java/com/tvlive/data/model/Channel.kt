package com.tvlive.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.tvlive.utils.Converters

/**
 * 频道数据模型
 * @param id 唯一ID
 * @param name 频道名称
 * @param url 播放地址
 * @param logo 频道Logo URL
 * @param category 分类（央视、卫视、地方等）
 * @param groupName 分组名称
 * @param epgUrl EPG节目单地址
 * @param isFavorite 是否收藏
 * @param lastPlayed 上次播放时间
 * @param userAgent 自定义User-Agent
 * @param headers 自定义请求头
 * @param order 排序权重
 */
@Entity(tableName = "channels")
@TypeConverters(Converters::class)
data class Channel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val logo: String? = null,
    val category: String = "其他",
    val groupName: String? = null,
    val epgUrl: String? = null,
    val isFavorite: Boolean = false,
    val lastPlayed: Long? = null,
    val userAgent: String? = null,
    val headers: Map<String, String>? = null,
    val order: Int = 0
) {
    companion object {
        // 预设分类
        const val CATEGORY_CCTV = "央视"
        const val CATEGORY_SATELLITE = "卫视"
        const val CATEGORY_LOCAL = "地方"
        const val CATEGORY_MOVIE = "电影"
        const val CATEGORY_SPORTS = "体育"
        const val CATEGORY_KIDS = "少儿"
        const val CATEGORY_MUSIC = "音乐"
        const val CATEGORY_OTHER = "其他"
    }
}

/**
 * 播放历史记录
 */
@Entity(tableName = "play_history")
data class PlayHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val channelId: Long,
    val channelName: String,
    val playedAt: Long = System.currentTimeMillis(),
    val duration: Long = 0, // 播放时长（毫秒）
    val position: Long = 0  // 上次播放位置（毫秒）
)

/**
 * 节目单数据
 */
@Entity(tableName = "epg_programs")
data class EPGProgram(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val channelId: String,
    val title: String,
    val description: String? = null,
    val startTime: Long,
    val endTime: Long,
    val icon: String? = null
)

/**
 * 频道源配置
 */
@Entity(tableName = "channel_sources")
data class ChannelSource(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val url: String,
    val type: SourceType = SourceType.M3U,
    val isEnabled: Boolean = true,
    val lastUpdate: Long? = null
)

enum class SourceType {
    M3U,        // M3U8/M3U 格式
    JSON,       // JSON 格式
    XML,        // XML 格式
    XTREAM      // Xtream Codes API
}
