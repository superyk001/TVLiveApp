package com.tvlive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tvlive.ui.channel.ChannelListScreen
import com.tvlive.ui.player.PlayerScreen
import com.tvlive.ui.settings.SettingsScreen
import com.tvlive.ui.theme.TVLiveTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主Activity
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TVLiveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TVLiveApp()
                }
            }
        }
    }
}

/**
 * 应用导航
 */
@Composable
fun TVLiveApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.ChannelList.route
    ) {
        composable(Screen.ChannelList.route) {
            ChannelListScreen(
                onChannelClick = { channel ->
                    navController.navigate(Screen.Player.createRoute(channel.id))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(
            route = Screen.Player.route,
            arguments = Screen.Player.arguments
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getLong("channelId")
            PlayerScreen(
                channelId = channelId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

/**
 * 路由定义
 */
sealed class Screen(val route: String) {
    data object ChannelList : Screen("channels")
    data object Player : Screen("player/{channelId}") {
        fun createRoute(channelId: Long) = "player/$channelId"
        val arguments = listOf(
            androidx.navigation.navArgument("channelId") {
                type = androidx.navigation.NavType.LongType
            }
        )
    }
    data object Settings : Screen("settings")
}
