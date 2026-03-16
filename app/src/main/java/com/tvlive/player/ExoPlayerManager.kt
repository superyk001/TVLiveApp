package com.tvlive.player

import android.content.Context
import android.util.Log
import android.view.SurfaceView
import android.view.TextureView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * ExoPlayer 管理器
 * 封装播放器核心功能，提供简洁的API
 */
@OptIn(UnstableApi::class)
class ExoPlayerManager private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "ExoPlayerManager"
        
        @Volatile
        private var instance: ExoPlayerManager? = null
        
        fun getInstance(context: Context): ExoPlayerManager {
            return instance ?: synchronized(this) {
                instance ?: ExoPlayerManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var currentUrl: String? = null
    private var isRetrying = false
    private var retryCount = 0
    private val maxRetries = 3
    
    // 播放状态监听
    private val listeners = mutableListOf<PlayerStateListener>()
    
    interface PlayerStateListener {
        fun onPlaybackStateChanged(state: Int) {}
        fun onError(error: String) {}
        fun onBuffering(isBuffering: Boolean) {}
    }
    
    /**
     * 初始化播放器
     */
    fun initialize() {
        if (player != null) return
        
        val trackSelector = DefaultTrackSelector(context).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
            .setUserAgent("TVLiveApp/1.0")
        
        player = ExoPlayer.Builder(context)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        Log.d(TAG, "播放状态改变: $state")
                        when (state) {
                            Player.STATE_IDLE -> {}
                            Player.STATE_BUFFERING -> {
                                listeners.forEach { it.onBuffering(true) }
                            }
                            Player.STATE_READY -> {
                                listeners.forEach { 
                                    it.onPlaybackStateChanged(state)
                                    it.onBuffering(false) 
                                }
                                retryCount = 0
                            }
                            Player.STATE_ENDED -> {
                                listeners.forEach { it.onPlaybackStateChanged(state) }
                            }
                        }
                    }
                    
                    override fun onPlayerError(error: PlaybackException) {
                        Log.e(TAG, "播放错误: ${error.message}")
                        handleError(error)
                    }
                    
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Log.d(TAG, "播放状态: $isPlaying")
                    }
                })
            }
        
        playerView?.player = player
    }
    
    /**
     * 播放指定URL
     */
    fun play(url: String, headers: Map<String, String>? = null) {
        currentUrl = url
        retryCount = 0
        
        if (player == null) {
            initialize()
        }
        
        try {
            val mediaItem = MediaItem.Builder()
                .setUri(url)
                .setMimeType(getMimeType(url))
                .build()
            
            player?.apply {
                stop()
                clearMediaItems()
                setMediaItem(mediaItem)
                prepare()
                play()
            }
            
            Log.d(TAG, "开始播放: $url")
        } catch (e: Exception) {
            Log.e(TAG, "播放失败: ${e.message}")
            listeners.forEach { it.onError("播放失败: ${e.message}") }
        }
    }
    
    /**
     * 获取媒体类型
     */
    private fun getMimeType(url: String): String? {
        return when {
            url.contains(".m3u8") || url.contains("hls") -> "application/x-mpegURL"
            url.contains(".mpd") -> "application/dash+xml"
            url.contains(".mp4") -> "video/mp4"
            url.contains(".ts") -> "video/mp2t"
            else -> null
        }
    }
    
    /**
     * 处理播放错误
     */
    private fun handleError(error: PlaybackException) {
        if (retryCount < maxRetries && currentUrl != null) {
            retryCount++
            Log.d(TAG, "重试播放 ($retryCount/$maxRetries)")
            player?.apply {
                stop()
                prepare()
                play()
            }
        } else {
            listeners.forEach { it.onError("播放失败，请检查网络或频道源") }
        }
    }
    
    /**
     * 暂停/继续
     */
    fun togglePlayPause() {
        player?.let {
            if (it.isPlaying) {
                it.pause()
            } else {
                it.play()
            }
        }
    }
    
    /**
     * 停止播放
     */
    fun stop() {
        player?.stop()
        currentUrl = null
        retryCount = 0
    }
    
    /**
     * 释放播放器
     */
    fun release() {
        player?.release()
        player = null
        playerView = null
        currentUrl = null
    }
    
    /**
     * 绑定 PlayerView
     */
    fun setPlayerView(view: PlayerView) {
        playerView = view
        playerView?.player = player
    }
    
    /**
     * 添加状态监听
     */
    fun addListener(listener: PlayerStateListener) {
        listeners.add(listener)
    }
    
    /**
     * 移除状态监听
     */
    fun removeListener(listener: PlayerStateListener) {
        listeners.remove(listener)
    }
    
    /**
     * 是否在播放
     */
    fun isPlaying(): Boolean = player?.isPlaying ?: false
    
    /**
     * 获取当前位置
     */
    fun getCurrentPosition(): Long = player?.currentPosition ?: 0
    
    /**
     * 获取时长（直播通常为 C.TIME_UNSET）
     */
    fun getDuration(): Long = player?.duration ?: C.TIME_UNSET
}
