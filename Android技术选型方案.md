# Android Instagram 克隆 App 技术选型方案

> 项目名称：ins_android  
> 目标：构建一个类似 Instagram 的社交分享 Android 原生应用  
> 后端：Spring Boot + MySQL（已完成，可用状态）  
> 日期：2026-04-15

---

## 一、项目基础配置

| 技术项 | 版本 | 说明 |
|-------|------|------|
| Android Gradle Plugin | 8.13.1 | 构建工具 |
| Kotlin | 2.0.21 | 主开发语言 |
| JVM Target | 11 | 字节码版本 |
| Min SDK | 24 (Android 7.0) | 最低支持版本 |
| Target / Compile SDK | 36 (Android 15) | 目标版本 |
| Jetpack Compose BOM | 2024.09.00 | 声明式 UI 框架 |
| Material3 | BOM 管理 | UI 组件库 |

---

## 二、技术选型

### 2.1 网络请求

| 库 | 版本 | 用途 |
|---|-----|------|
| `retrofit2:retrofit` | 2.9.0 | REST API 请求框架 |
| `retrofit2:converter-moshi` | 2.9.0 | Moshi 转换器 |
| `okhttp3:okhttp` | 4.12.0 | HTTP 客户端 |
| `okhttp3:logging-interceptor` | 4.12.0 | 调试请求日志 |
| `moshi:moshi-kotlin` | 1.15.0 | JSON 解析 |

- Retrofit 定义接口，OkHttp Interceptor 统一注入 `Authorization: Bearer <token>`
- Moshi 比 Gson 对 Kotlin data class 支持更好（null 安全、默认值）

### 2.2 图片加载

| 库 | 版本 | 用途 |
|---|-----|------|
| `coil-kt:coil-compose` | 2.5.0 | 网络图片加载 + 内存/磁盘缓存 |

- Kotlin-first，与 Compose 深度集成，一行代码加载网络图片

### 2.3 页面导航

| 库 | 版本 | 用途 |
|---|-----|------|
| `navigation:navigation-compose` | 2.7.0 | 页面路由 + 底部 Tab 导航 |

- Jetpack 官方方案，管理 5 个主 Tab + 子页面返回栈

### 2.4 异步处理

| 库 | 版本 | 用途 |
|---|-----|------|
| `kotlinx-coroutines-android` | 1.7.3 | 协程 Android 调度器 |

- 网络请求、文件上传等异步操作，配合 `viewModelScope.launch` 使用

### 2.5 ViewModel

| 库 | 版本 | 用途 |
|---|-----|------|
| `lifecycle:lifecycle-viewmodel-compose` | 2.7.0 | Compose 中使用 ViewModel |

- 管理页面 UI 状态，屏幕旋转不丢数据

### 2.6 本地存储

| 库 | 版本 | 用途 |
|---|-----|------|
| `datastore:datastore-preferences` | 1.0.0 | 存储 Token、用户信息等 KV 数据 |

- 替代 SharedPreferences，协程安全

### 2.7 视频播放

| 库 | 版本 | 用途 |
|---|-----|------|
| `media3:media3-exoplayer` | 1.3.0 | 视频播放核心 |
| `media3:media3-ui` | 1.3.0 | 播放器 UI 控件 |

- Feed 内嵌视频播放、视频帖子全屏播放
- 原生 VideoView 不支持流媒体缓冲，ExoPlayer 是 Google 官方推荐方案

### 2.8 视频/图片选择

使用 Android 原生 Photo Picker，无需额外依赖：

```kotlin
val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.PickVisualMedia()
) { uri -> /* 处理选中的文件 */ }
```

---

## 三、暂不引入（后期按需添加）

| 库 | 说明 |
|---|------|
| Hilt（依赖注入） | 初期手动创建对象够用，项目规模大后再引入 |
| Room（本地数据库） | 初期不需要离线缓存，消息功能开发时再加 |
| CameraX（相机） | 先用系统相册选图/视频，拍照/录像功能后期再加 |

---

## 四、完整依赖清单

```kotlin
// app/build.gradle.kts
dependencies {
    // 已有
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // 网络
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")

    // 图片
    implementation("io.coil-kt:coil-compose:2.5.0")

    // 导航
    implementation("androidx.navigation:navigation-compose:2.7.0")

    // 协程
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // 本地存储
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // 视频播放
    implementation("androidx.media3:media3-exoplayer:1.3.0")
    implementation("androidx.media3:media3-ui:1.3.0")
}
```

---

## 五、架构设计

### 5.1 MVVM 分层

```
Screen (Composable)   ← 纯 UI 渲染，观察 UiState
        ↓
   ViewModel          ← 持有 UiState，处理用户事件
        ↓
   Repository         ← 数据获取与缓存策略
    ↙        ↘
Retrofit API    DataStore
（远程数据）    （本地存储）
```

### 5.2 包结构

```
com.qiwang.ins_android/
├── data/
│   ├── api/           # Retrofit Service 接口 + DTO
│   ├── local/         # DataStore
│   └── repository/    # 数据仓库
├── ui/
│   ├── theme/         # Material3 主题（已有）
│   ├── components/    # 通用 Compose 组件
│   ├── navigation/    # NavHost + 路由定义
│   └── screens/
│       ├── login/
│       ├── home/
│       ├── explore/
│       ├── publish/
│       ├── profile/
│       └── userdetail/
└── util/              # 工具类
```

---

## 六、与后端对接要点

| 要点 | 处理方式 |
|-----|---------|
| 统一响应格式 `{ code, message, data }` | 定义 `ApiResponse<T>` 泛型类，Repository 层拆包 |
| ID 字段（后端 BIGINT 序列化为 String） | DTO 中 ID 一律用 `String`，不转 `Long` |
| Token 注入 | OkHttp Interceptor 自动加 `Authorization: Bearer <token>` |
| 401 处理 | OkHttp Authenticator 自动刷新 token |
| 媒体 URL 相对路径 | 加载前判断是否以 `http` 开头，否则拼接 baseUrl |
| 后端地址 | `http://112.124.47.169:8081` |
