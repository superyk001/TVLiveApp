package com.tvlive.data.source

import android.util.Log
import com.tvlive.data.model.Channel
import com.tvlive.data.model.SourceType
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.StringReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * M3U 解析器
 * 支持标准 M3U8/M3U 格式解析
 */
@Singleton
class M3UParser @Inject constructor() {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    companion object {
        private const val TAG = "M3UParser"
    }
    
    /**
     * 从 URL 解析 M3U 列表
     */
    suspend fun parseFromUrl(url: String): List<Channel> {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("HTTP ${response.code}")
                }
                val content = response.body?.string() ?: ""
                parseContent(content)
            }
        } catch (e: Exception) {
            Log.e(TAG, "解析失败: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 从本地内容解析
     */
    fun parseContent(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val reader = BufferedReader(StringReader(content))
        
        var currentName = ""
        var currentLogo: String? = null
        var currentGroup: String? = null
        var currentCategory = Channel.CATEGORY_OTHER
        var order = 0
        
        reader.useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()
                
                when {
                    // 跳过文件头
                    trimmed.startsWith("#EXTM3U") -> { }
                    
                    // 解析频道信息行
                    trimmed.startsWith("#EXTINF:") -> {
                        val info = parseExtInf(trimmed)
                        currentName = info.name
                        currentLogo = info.logo
                        currentGroup = info.group
                        currentCategory = inferCategory(currentName, currentGroup)
                    }
                    
                    // URL 行
                    trimmed.isNotBlank() && !trimmed.startsWith("#") -> {
                        if (currentName.isNotBlank()) {
                            channels.add(
                                Channel(
                                    name = currentName,
                                    url = trimmed,
                                    logo = currentLogo,
                                    category = currentCategory,
                                    groupName = currentGroup,
                                    order = order++
                                )
                            )
                        }
                        // 重置
                        currentName = ""
                        currentLogo = null
                        currentGroup = null
                    }
                }
            }
        }
        
        return channels
    }
    
    /**
     * 解析 #EXTINF 行
     * 格式: #EXTINF:-1 tvg-logo="xxx" group-title="xxx",频道名称
     */
    private fun parseExtInf(line: String): ExtInfInfo {
        var name = ""
        var logo: String? = null
        var group: String? = null
        
        // 提取频道名称（逗号后面的内容）
        val commaIndex = line.lastIndexOf(',')
        if (commaIndex > 0) {
            name = line.substring(commaIndex + 1).trim()
        }
        
        // 提取 tvg-logo
        val logoMatch = Regex("""tvg-logo=[\"']([^\"']*)[\"']""").find(line)
        logo = logoMatch?.groupValues?.get(1)
        
        // 提取 group-title
        val groupMatch = Regex("""group-title=[\"']([^\"']*)[\"']""").find(line)
        group = groupMatch?.groupValues?.get(1)
        
        // 如果没有 group-title，尝试其他属性
        if (group.isNullOrBlank()) {
            val titleMatch = Regex("""title=[\"']([^\"']*)[\"']""").find(line)
            group = titleMatch?.groupValues?.get(1)
        }
        
        return ExtInfInfo(name, logo, group)
    }
    
    /**
     * 根据名称推断分类
     */
    private fun inferCategory(name: String, group: String?): String {
        val lowerName = name.lowercase()
        val lowerGroup = group?.lowercase() ?: ""
        
        return when {
            // 央视
            lowerName.contains("cctv") ||
            lowerName.contains("央视") ||
            lowerGroup.contains("央视") ||
            lowerGroup.contains("cctv") -> Channel.CATEGORY_CCTV
            
            // 卫视
            lowerName.contains("卫视") ||
            lowerGroup.contains("卫视") ||
            lowerName.contains("tv") ||
            lowerName.endsWith("台") -> Channel.CATEGORY_SATELLITE
            
            // 体育
            lowerName.contains("体育") ||
            lowerName.contains("sport") ||
            lowerName.contains("espn") ||
            lowerGroup.contains("体育") ||
            lowerGroup.contains("sport") -> Channel.CATEGORY_SPORTS
            
            // 电影
            lowerName.contains("电影") ||
            lowerName.contains("movie") ||
            lowerName.contains("影院") ||
            lowerGroup.contains("电影") ||
            lowerGroup.contains("movie") -> Channel.CATEGORY_MOVIE
            
            // 少儿
            lowerName.contains("少儿") ||
            lowerName.contains("卡通") ||
            lowerName.contains("动画") ||
            lowerName.contains("kids") ||
            lowerName.contains("cartoon") ||
            lowerGroup.contains("少儿") ||
            lowerGroup.contains("kids") -> Channel.CATEGORY_KIDS
            
            // 音乐
            lowerName.contains("音乐") ||
            lowerName.contains("music") ||
            lowerName.contains("mtv") ||
            lowerName.contains("mv") ||
            lowerGroup.contains("音乐") ||
            lowerGroup.contains("music") -> Channel.CATEGORY_MUSIC
            
            // 地方台
            lowerName.contains("台") ||
            lowerGroup.contains("地方") -> Channel.CATEGORY_LOCAL
            
            else -> Channel.CATEGORY_OTHER
        }
    }
    
    private data class ExtInfInfo(
        val name: String,
        val logo: String?,
        val group: String?
    )
}
