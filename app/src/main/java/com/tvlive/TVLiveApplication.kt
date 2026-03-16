package com.tvlive

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用入口类
 * 使用 Hilt 进行依赖注入
 */
@HiltAndroidApp
class TVLiveApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: TVLiveApplication
            private set
    }
}
