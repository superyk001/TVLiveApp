package com.tvlive.ui.channel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tvlive.data.model.Channel
import com.tvlive.ui.components.ChannelCard
import com.tvlive.ui.components.ChannelListItem
import com.tvlive.viewmodel.ChannelViewModel

/**
 * 频道列表界面
 * 支持分类筛选、搜索、收藏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelListScreen(
    onChannelClick: (Channel) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ChannelViewModel = hiltViewModel()
) {
    val channels by viewModel.channels.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showSearch by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("电视直播") },
                actions = {
                    // 切换视图
                    IconButton(onClick = { 
                        viewMode = if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID 
                    }) {
                        Icon(
                            imageVector = if (viewMode == ViewMode.GRID) 
                                Icons.Default.ViewList 
                            else 
                                Icons.Default.ViewModule,
                            contentDescription = "切换视图"
                        )
                    }
                    // 搜索
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                    // 导入
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "导入")
                    }
                    // 设置
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 搜索栏
            AnimatedVisibility(visible = showSearch) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = viewModel::search,
                    onClose = { showSearch = false }
                )
            }
            
            // 分类标签
            CategoryTabs(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    if (category.isBlank()) {
                        viewModel.loadChannels()
                    } else {
                        viewModel.loadChannelsByCategory(category)
                    }
                }
            )
            
            // 收藏频道快捷访问
            if (favorites.isNotEmpty() && searchQuery.isBlank() && selectedCategory.isBlank()) {
                FavoritesRow(
                    favorites = favorites,
                    onChannelClick = onChannelClick,
                    onFavoriteClick = viewModel::toggleFavorite
                )
            }
            
            // 频道列表
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    channels.isEmpty() -> {
                        EmptyState(
                            onImportClick = { showImportDialog = true }
                        )
                    }
                    else -> {
                        if (viewMode == ViewMode.GRID) {
                            ChannelGrid(
                                channels = channels,
                                onChannelClick = onChannelClick,
                                onFavoriteClick = viewModel::toggleFavorite
                            )
                        } else {
                            ChannelList(
                                channels = channels,
                                onChannelClick = onChannelClick,
                                onFavoriteClick = viewModel::toggleFavorite
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 导入对话框
    if (showImportDialog) {
        ImportDialog(
            onDismiss = { showImportDialog = false },
            onImport = { url ->
                viewModel.importFromM3U(url)
                showImportDialog = false
            }
        )
    }
    
    // 错误提示
    error?.let { msg ->
        ErrorSnackbar(
            message = msg,
            onDismiss = viewModel::clearError
        )
    }
}

/**
 * 搜索栏
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("搜索频道...") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, null)
            }
        },
        singleLine = true
    )
}

/**
 * 分类标签
 */
@Composable
private fun CategoryTabs(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = if (selectedCategory.isBlank()) 0 else categories.indexOf(selectedCategory) + 1,
        modifier = Modifier.fillMaxWidth()
    ) {
        Tab(
            selected = selectedCategory.isBlank(),
            onClick = { onCategorySelected("") },
            text = { Text("全部") }
        )
        categories.forEach { category ->
            Tab(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                text = { Text(category) }
            )
        }
    }
}

/**
 * 收藏频道横向列表
 */
@Composable
private fun FavoritesRow(
    favorites: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    onFavoriteClick: (Channel) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "我的收藏",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favorites) { channel ->
                ChannelCard(
                    channel = channel,
                    onClick = { onChannelClick(channel) },
                    onFavoriteClick = { onFavoriteClick(channel) },
                    modifier = Modifier.width(120.dp)
                )
            }
        }
    }
}

/**
 * 网格视图
 */
@Composable
private fun ChannelGrid(
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    onFavoriteClick: (Channel) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(channels) { channel ->
            ChannelCard(
                channel = channel,
                onClick = { onChannelClick(channel) },
                onFavoriteClick = { onFavoriteClick(channel) }
            )
        }
    }
}

/**
 * 列表视图
 */
@Composable
private fun ChannelList(
    channels: List<Channel>,
    onChannelClick: (Channel) -> Unit,
    onFavoriteClick: (Channel) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(channels) { channel ->
            ChannelListItem(
                channel = channel,
                onClick = { onChannelClick(channel) },
                onFavoriteClick = { onFavoriteClick(channel) }
            )
        }
    }
}

/**
 * 空状态
 */
@Composable
private fun EmptyState(onImportClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Tv,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无频道",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击右上角导入按钮添加频道",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onImportClick) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("导入频道")
        }
    }
}

/**
 * 导入对话框
 */
@Composable
private fun ImportDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入频道") },
        text = {
            Column {
                Text("输入 M3U 播放列表 URL")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = url,
                    onValueChange = { url = it },
                    placeholder = { Text("https://example.com/playlist.m3u") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onImport(url) },
                enabled = url.isNotBlank()
            ) {
                Text("导入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 错误提示
 */
@Composable
private fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit
) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        action = {
            TextButton(onClick = onDismiss) {
                Text("确定")
            }
        }
    ) {
        Text(message)
    }
}

enum class ViewMode { GRID, LIST }
