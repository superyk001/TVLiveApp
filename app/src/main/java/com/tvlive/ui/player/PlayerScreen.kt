package com.tvlive.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.tvlive.data.model.Channel
import com.tvlive.player.ExoPlayerManager
import com.tvlive.viewmodel.ChannelViewModel
import kotlinx.coroutines.delay

/**
 * 播放器界面
 * 支持全屏播放、频道切换、控制面板
 */
@Composable
fun PlayerScreen(
    channelId: Long?,
    onBackClick: () -> Unit,
    viewModel: ChannelViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val channels by viewModel.channels.collectAsState()
    val currentChannel by viewModel.currentChannel.collectAsState()
    
    var showControls by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showChannelList by remember { mutableStateOf(false) }
    
    val playerManager = remember { ExoPlayerManager.getInstance(context) }
    
    // 查找当前频道
    LaunchedEffect(channelId) {
        channelId?.let { id ->
            channels.find { it.id == id }?.let { channel ->
                viewModel.selectChannel(channel)
            }
        }
    }
    
    // 播放当前频道
    LaunchedEffect(currentChannel) {
        currentChannel?.let { channel ->
            isLoading = true
            errorMessage = null
            playerManager.play(channel.url, channel.headers)
        }
    }
    
    // 播放器状态监听
    DisposableEffect(Unit) {
        val listener = object : ExoPlayerManager.PlayerStateListener {
            override fun onPlaybackStateChanged(state: Int) {
                isLoading = false
            }
            
            override fun onError(error: String) {
                isLoading = false
                errorMessage = error
            }
            
            override fun onBuffering(isBuffering: Boolean) {
                isLoading = isBuffering
            }
        }
        playerManager.addListener(listener)
        playerManager.initialize()
        
        onDispose {
            playerManager.removeListener(listener)
            if ((context as? Activity)?.isFinishing == true) {
                playerManager.stop()
            }
        }
    }
    
    // 全屏处理
    LaunchedEffect(isFullscreen) {
        val activity = context as? Activity ?: return@LaunchedEffect
        val window = activity.window
        
        if (isFullscreen) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            )
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
    
    // 返回处理
    BackHandler {
        if (showChannelList) {
            showChannelList = false
        } else {
            playerManager.stop()
            onBackClick()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 视频播放器
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    playerManager.setPlayerView(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // 点击区域（显示/隐藏控制栏）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { showControls = !showControls }
        )
        
        // 加载指示器
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        
        // 错误提示
        errorMessage?.let { error ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(error, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            errorMessage = null
                            currentChannel?.let {
                                playerManager.play(it.url, it.headers)
                            }
                        }) {
                            Text("重试")
                        }
                    }
                }
            }
        }
        
        // 控制栏
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PlayerControls(
                channel = currentChannel,
                onBackClick = onBackClick,
                onToggleFullscreen = { isFullscreen = !isFullscreen },
                onToggleFavorite = {
                    currentChannel?.let { viewModel.toggleFavorite(it) }
                },
                onShowChannelList = { showChannelList = true },
                isFullscreen = isFullscreen
            )
        }
        
        // 频道列表侧边栏
        AnimatedVisibility(
            visible = showChannelList,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ChannelListSidebar(
                channels = channels,
                currentChannel = currentChannel,
                onChannelSelected = { channel ->
                    viewModel.selectChannel(channel)
                    showChannelList = false
                },
                onDismiss = { showChannelList = false }
            )
        }
    }
}

/**
 * 播放器控制栏
 */
@Composable
private fun PlayerControls(
    channel: Channel?,
    onBackClick: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShowChannelList: () -> Unit,
    isFullscreen: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部控制栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White
                    )
                }
                
                channel?.let {
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = it.name,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = it.category,
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            Row {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (channel?.isFavorite == true)
                            Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "收藏",
                        tint = if (channel?.isFavorite == true) Color.Red else Color.White
                    )
                }
                
                IconButton(onClick = onShowChannelList) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "频道列表",
                        tint = Color.White
                    )
                }
                
                IconButton(onClick = onToggleFullscreen) {
                    Icon(
                        imageVector = if (isFullscreen)
                            Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = "全屏",
                        tint = Color.White
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // 底部控制栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // 可以在这里添加更多控制按钮
            Text(
                text = "直播模式",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * 频道列表侧边栏
 */
@Composable
private fun ChannelListSidebar(
    channels: List<Channel>,
    currentChannel: Channel?,
    onChannelSelected: (Channel) -> Unit,
    onDismiss: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 背景遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss)
        )
        
        // 侧边栏
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(320.dp)
                .align(Alignment.CenterEnd),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "频道列表",
                        style = MaterialTheme.typography.titleMedium
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null)
                    }
                }
                
                Divider()
                
                // 频道列表
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(channels) { channel ->
                        val isCurrent = channel.id == currentChannel?.id
                        
                        ListItem(
                            headlineContent = {
                                Text(
                                    channel.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isCurrent)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            },
                            supportingContent = {
                                Text(
                                    channel.category,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            leadingContent = if (isCurrent) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else null,
                            modifier = Modifier.clickable {
                                onChannelSelected(channel)
                            }
                        )
                    }
                }
            }
        }
    }
}
