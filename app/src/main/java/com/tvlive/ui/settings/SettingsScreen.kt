package com.tvlive.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.tvlive.viewmodel.ChannelViewModel

/**
 * 设置界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: ChannelViewModel = hiltViewModel()
) {
    var showClearConfirm by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 数据管理
            item {
                SettingsSection(title = "数据管理") {
                    SettingsItem(
                        icon = Icons.Default.Delete,
                        title = "清除所有频道",
                        subtitle = "删除本地保存的所有频道数据",
                        onClick = { showClearConfirm = true }
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Refresh,
                        title = "重置收藏",
                        subtitle = "取消所有频道的收藏状态",
                        onClick = { /* TODO */ }
                    )
                }
            }
            
            // 播放设置
            item {
                SettingsSection(title = "播放设置") {
                    var autoPlay by remember { mutableStateOf(true) }
                    var hardwareDecode by remember { mutableStateOf(true) }
                    
                    SettingsSwitch(
                        icon = Icons.Default.PlayCircle,
                        title = "自动播放",
                        subtitle = "切换到频道时自动开始播放",
                        checked = autoPlay,
                        onCheckedChange = { autoPlay = it }
                    )
                    
                    SettingsSwitch(
                        icon = Icons.Default.Memory,
                        title = "硬件解码",
                        subtitle = "使用硬件加速解码视频（推荐开启）",
                        checked = hardwareDecode,
                        onCheckedChange = { hardwareDecode = it }
                    )
                }
            }
            
            // 关于
            item {
                SettingsSection(title = "关于") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "版本",
                        subtitle = "1.0.0",
                        onClick = {}
                    )
                    
                    SettingsItem(
                        icon = Icons.Default.Code,
                        title = "开源项目",
                        subtitle = "GitHub 上的 TVLiveApp",
                        onClick = {}
                    )
                }
            }
        }
    }
    
    // 清除确认对话框
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("确认清除") },
            text = { Text("确定要删除所有频道数据吗？此操作不可恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllChannels()
                        showClearConfirm = false
                    }
                ) {
                    Text("确定", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 设置分组
 */
@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        content()
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

/**
 * 设置项
 */
@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        trailingContent = {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

/**
 * 带开关的设置项
 */
@Composable
fun SettingsSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(icon, contentDescription = null)
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}
