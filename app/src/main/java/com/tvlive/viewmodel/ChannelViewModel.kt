package com.tvlive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tvlive.data.model.Channel
import com.tvlive.data.model.PlayHistory
import com.tvlive.data.repository.ChannelRepository
import com.tvlive.data.source.M3UParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 频道列表 ViewModel
 */
@HiltViewModel
class ChannelViewModel @Inject constructor(
    private val repository: ChannelRepository,
    private val m3uParser: M3UParser
) : ViewModel() {
    
    // ==================== 状态 ====================
    
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()
    
    private val _favorites = MutableStateFlow<List<Channel>>(emptyList())
    val favorites: StateFlow<List<Channel>> = _favorites.asStateFlow()
    
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()
    
    private val _currentChannel = MutableStateFlow<Channel?>(null)
    val currentChannel: StateFlow<Channel?> = _currentChannel.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()
    
    // ==================== 初始化 ====================
    
    init {
        loadChannels()
        loadFavorites()
        loadCategories()
    }
    
    // ==================== 加载数据 ====================
    
    fun loadChannels() {
        viewModelScope.launch {
            repository.getAllChannels()
                .catch { e ->
                    _error.value = e.message
                }
                .collect { list ->
                    _channels.value = list
                }
        }
    }
    
    fun loadChannelsByCategory(category: String) {
        _selectedCategory.value = category
        viewModelScope.launch {
            repository.getChannelsByCategory(category)
                .catch { e ->
                    _error.value = e.message
                }
                .collect { list ->
                    _channels.value = list
                }
        }
    }
    
    fun loadFavorites() {
        viewModelScope.launch {
            repository.getFavoriteChannels()
                .collect { list ->
                    _favorites.value = list
                }
        }
    }
    
    fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories()
                .collect { list ->
                    _categories.value = list
                }
        }
    }
    
    // ==================== 频道操作 ====================
    
    fun selectChannel(channel: Channel) {
        _currentChannel.value = channel
        viewModelScope.launch {
            repository.updateLastPlayed(channel.id)
            repository.addPlayHistory(
                PlayHistory(
                    channelId = channel.id,
                    channelName = channel.name
                )
            )
        }
    }
    
    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            repository.toggleFavorite(channel)
        }
    }
    
    fun search(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            loadChannels()
            return
        }
        viewModelScope.launch {
            repository.searchChannels(query)
                .collect { list ->
                    _channels.value = list
                }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    // ==================== 导入频道 ====================
    
    fun importFromM3U(url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val channels = m3uParser.parseFromUrl(url)
                if (channels.isNotEmpty()) {
                    repository.addChannels(channels)
                    _isLoading.value = false
                } else {
                    _error.value = "未解析到频道"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "导入失败"
                _isLoading.value = false
            }
        }
    }
    
    fun importFromM3UContent(content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val channels = m3uParser.parseContent(content)
            if (channels.isNotEmpty()) {
                repository.addChannels(channels)
            }
            _isLoading.value = false
        }
    }
    
    fun clearAllChannels() {
        viewModelScope.launch {
            repository.deleteAllChannels()
        }
    }
}
