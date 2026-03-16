TV Live App - Android 电视直播应用

基于 Kotlin + Jetpack Compose + ExoPlayer 开发的电视直播应用，支持智能电视和手机双端使用。

## 功能特性

- ✅ **M3U 播放列表导入** - 支持标准 M3U8/M3U 格式
- ✅ **分类浏览** - 自动识别央视、卫视、地方、体育、电影等分类
- ✅ **收藏功能** - 快速访问常用频道
- ✅ **搜索功能** - 快速查找频道
- ✅ **播放历史** - 记录观看历史
- ✅ **双端适配** - 同时支持 TV 和手机
- ✅ **TV 遥控器支持** - 完整的 D-Pad 导航
- ✅ **全屏播放** - 横屏沉浸式体验
- ✅ **频道切换** - 侧边栏快速切换

## 技术栈

- **Kotlin** - 开发语言
- **Jetpack Compose** - UI框架
- **ExoPlayer (Media3)** - 播放器核心
- **Room** - 本地数据库
- **Hilt** - 依赖注入
- **Retrofit + OkHttp** - 网络请求
- **Glide** - 图片加载

## 项目结构

```
app/src/main/java/com/tvlive/
├── data/
│   ├── local/          # 本地数据库
│   ├── model/          # 数据模型
│   ├── repository/     # 数据仓库
│   └── source/         # 数据源（M3U解析等）
├── di/                 # 依赖注入模块
├── player/             # 播放器封装
├── service/            # 后台服务
├── ui/
│   ├── channel/        # 频道列表界面
│   ├── player/         # 播放器界面
│   ├── settings/       # 设置界面
│   ├── components/     # 可复用组件
│   └── theme/          # 主题配置
├── utils/              # 工具类
└── MainActivity.kt     # 主入口
```

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/TVLiveApp.git
cd TVLiveApp
```

### 2. 打开项目

使用 Android Studio 打开项目文件夹

### 3. 同步 Gradle

等待 Gradle 同步完成

### 4. 运行

连接设备或启动模拟器，点击运行按钮

## 使用说明

### 导入频道

1. 进入频道列表界面
2. 点击右上角 `+` 按钮
3. 输入 M3U 播放列表 URL
4. 点击导入

### 支持的 M3U 格式

```m3u
#EXTM3U
#EXTINF:-1 tvg-logo="https://logo.url" group-title="央视",CCTV-1
http://stream.url/channel1.m3u8
#EXTINF:-1 tvg-logo="https://logo.url" group-title="卫视",湖南卫视
http://stream.url/channel2.m3u8
```

### TV 操作

- **方向键** - 导航
- **确定键** - 选择/播放
- **返回键** - 返回/关闭控制栏
- **菜单键** - 显示频道列表

## 开发计划

- [ ] EPG 节目单支持
- [ ] 多源切换
- [ ] 时移回放
- [ ] 投屏功能
- [ ] 倍速播放
- [ ] 画面比例调整
- [ ] 夜间模式
- [ ] 多语言支持

## 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

## 免责声明

本项目仅供学习交流使用，请勿用于任何商业用途。使用者需自行承担使用本软件可能产生的任何法律责任。
