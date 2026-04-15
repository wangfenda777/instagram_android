# Instagram Android 原生 App 开发文档

> 基于 ins_vue_uniapp_project 前端项目功能分析  
> 后端：Spring Boot + MySQL（地址 http://112.124.47.169:8081）  
> 日期：2026-04-15

---

## 目录

1. [功能模块总览](#一功能模块总览)
2. [Material Design 3 风格定制](#二material-design-3-风格定制)
3. [页面详细需求](#三页面详细需求)
4. [通用组件清单](#四通用组件清单)
5. [API 接口清单](#五api-接口清单)
6. [数据模型定义](#六数据模型定义)
7. [开发优先级建议](#七开发优先级建议)

---

## 一、功能模块总览

| 模块 | 页面 | 核心功能 |
|------|------|---------|
| 认证 | 登录页 | 用户名密码登录，Token 持久化 |
| 首页 | Feed 页 | Stories 横滑 + 帖子无限滚动流 |
| 探索 | 探索页 | 瀑布流推荐 + 实时搜索 |
| 发布 | 发布页 | 图片/视频选择上传 + 文案编辑 |
| 消息 | 消息页 | 占位页面（暂不实现） |
| 个人主页 | 主页 | 个人信息 + 帖子/Reels/标记网格 |
| 用户详情 | 详情页 | 他人主页 + 关注/取关 |
| 搜索结果 | 搜索页 | 推荐/账户/标签三栏搜索 |
| 子页面 | 编辑资料、编辑字段、粉丝关注列表 | 资料修改 + 关注关系浏览 |

底部导航栏 5 个 Tab：首页、探索、发布、消息、个人主页。

---

## 二、Material Design 3 风格定制

本项目基于 Material Design 3 组件库开发，但在视觉风格上进行了 Instagram 化的定制重写，以实现极简、高对比度、扁平化的视觉效果。

### 2.1 颜色系统 (ColorScheme) —— 极致黑白化

不使用壁纸生成的动态色彩（Dynamic Color），手动定义一套高对比度色板：

| 色值 | 用途 | 说明 |
|------|------|------|
| `#FFFFFF` | Background / Surface | 纯白背景 |
| `#0095F6` | Primary | Instagram 经典蓝，仅用于"关注"按钮和链接 |
| `#262626` | OnSurface | 深灰黑正文色，比纯黑更有质感 |
| `#8E8E8E` | OnSurfaceVariant | 中灰色，用于辅助信息、时间戳 |
| `#DBDBDB` | Outline | 极浅灰色，用于细分割线（不使用粗边框） |

```kotlin
val InstagramColorScheme = lightColorScheme(
    primary = Color(0xFF0095F6),
    background = Color.White,
    surface = Color.White,
    onSurface = Color(0xFF262626),
    onSurfaceVariant = Color(0xFF8E8E8E),
    outline = Color(0xFFDBDBDB)
)
```

### 2.2 海拔与阴影 (Elevation) —— 全面"拍扁"

MD3 默认给按钮、搜索框带了海拔感（Tonal Elevation），会破坏极简的平面感。

| 规则 | 说明 |
|------|------|
| Tonal Elevation | 全部设为 `0dp` |
| Shadow Elevation | 全部设为 `0dp` |
| 例外 | 仅在对话框（Dialog）和底部弹出菜单（BottomSheet）保留轻微阴影 |
| 区分方式 | 普通页面模块通过 `1dp` 的 Outline 颜色（极浅灰线）或间距区分 |

实践要点：
- MD3 的 `Card` 默认有阴影，改用 `Surface` 或 `Box` 配上 `Modifier.drawBehind` 画一条细底边
- 搜索框、输入框去掉默认的 Tonal Elevation

### 2.3 形状系统 (Shapes) —— 减弱 MD3 的"圆润感"

MD3 默认按钮和输入框非常圆（如 28dp），看起来比较"肉"。

| 元素 | 圆角 | 说明 |
|------|------|------|
| Buttons | `4dp - 8dp` | 小圆角更显干练，Instagram 按钮不是全圆角 |
| Text Fields / 搜索框 | `10dp - 12dp` | 背景色 `#EFEFEF`，去掉默认下划线（Indicator） |
| Avatars | `CircleShape` | 全圆，唯一的全圆元素，用于突出人物 |

```kotlin
MaterialTheme(
    colorScheme = InstagramColorScheme,
    typography = InstagramTypography,
    shapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(8.dp)
    )
) {
    // App 内容
}
```

### 2.4 字体排版 (Typography) —— 清晰的粗细对比

Instagram 视觉高级感的秘诀在于 FontWeight 的极差：

| 层级 | 字重 | 字号 | 颜色 | 用途 |
|------|------|------|------|------|
| Title Large / Medium | Bold / SemiBold (600-700) | 16sp | OnSurface | 用户名 |
| Body Large | Normal (400) | 14-15sp | OnSurface | 状态描述、帖子内容 |
| Label Small | Normal (400) | 12sp | OnSurfaceVariant（灰色） | 点赞数、发布时间 |

### 2.5 组件级别的风格覆盖规则

| 规则 | 说明 |
|------|------|
| 去掉所有卡片阴影 | `Card` 改用 `Surface` 或 `Box` + 细底边 |
| 图标使用 Outlined 变体 | 优先 `Icons.Outlined` 系列，不用 `Filled` |
| 大间距 | 列表项与屏幕边缘至少 `12dp - 16dp` Padding |
| 分割线 | 使用 `1dp` 的 `Outline` 颜色线，不使用粗边框 |
| 按钮 | Primary 按钮用 `#0095F6` 蓝色填充 + 白色文字，Secondary 按钮用 `#EFEFEF` 灰色填充 |

### 2.6 图标资源

项目使用 ins_vue_uniapp_project 中的 SVG 图标，复制到 Android 项目的 `app/src/main/res/drawable/` 目录（转换为 Vector Drawable）。

可用图标清单：

| 图标文件 | 用途 |
|---------|------|
| `instagram.svg` / `instagram_simple.svg` | App Logo / 顶部导航 Logo |
| `home.svg` / `home-selected.svg` | 底部 Tab - 首页 |
| `direction.svg` / `direction-select.svg` | 底部 Tab - 探索 |
| `add.svg` | 底部 Tab - 发布 |
| `video.svg` / `video-selected.svg` | 底部 Tab - 消息 |
| `my-selected.svg` | 底部 Tab - 个人主页 |
| `like.svg` / `like_selected.svg` | 点赞（空心/实心） |
| `collect.svg` / `collect_selected.svg` | 收藏（空心/实心） |
| `talk.svg` | 评论 |
| `airport.svg` | 分享 |
| `search.svg` / `search_bar.svg` | 搜索图标 |
| `close.svg` | 关闭按钮 |
| `delete.svg` | 删除 |
| `edit.svg` | 编辑 |
| `three_o.svg` | 更多菜单（三个点） |
| `elip.svg` | 省略号 |
| `star.svg` | 星标/推荐 |
| `vip.svg` | 认证标识 |
| `copy.svg` | 复制 |
| `refresh.svg` | 刷新 |
| `book.svg` | 书签/标记 |
| `oclock.svg` | 时间 |

### 2.7 统一风格配置原则

**重要：所有风格定制必须在主题层面统一配置，不要在每个组件使用时单独处理。**

#### 配置位置

所有风格配置集中在以下文件：

```
app/src/main/java/com/qiwang/ins_android/ui/theme/
├── Color.kt          # 颜色定义
├── Type.kt           # 字体排版定义
├── Shape.kt          # 形状定义（新增）
└── Theme.kt          # 主题组装 + 组件默认样式覆盖
```

#### 统一配置策略

| 配置项 | 实现方式 | 说明 |
|--------|---------|------|
| **颜色** | `lightColorScheme()` | 在 `Color.kt` 定义色值常量，在 `Theme.kt` 中组装 ColorScheme |
| **字体** | `Typography()` | 在 `Type.kt` 定义完整的 Typography，覆盖所有层级（titleLarge/bodyLarge/labelSmall 等） |
| **形状** | `Shapes()` | 在 `Shape.kt` 定义 small/medium/large 圆角，应用到所有组件 |
| **组件默认样式** | `CompositionLocalProvider` | 在 `Theme.kt` 中使用 `LocalXxx.provides()` 覆盖组件默认行为 |

#### 需要统一覆盖的组件默认样式

在 `Theme.kt` 的 `MaterialTheme` 外层包裹 `CompositionLocalProvider`：

```kotlin
@Composable
fun InstagramTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = InstagramColorScheme,
        typography = InstagramTypography,
        shapes = InstagramShapes
    ) {
        CompositionLocalProvider(
            // 1. 去掉所有 Card 的阴影
            LocalAbsoluteTonalElevation provides 0.dp,
            
            // 2. TextField 默认样式（搜索框、输入框）
            // 使用 TextFieldDefaults.colors() 覆盖默认颜色
            // 背景色 #EFEFEF，去掉下划线 indicator
            
            // 3. Button 默认样式
            // FilledTonalButton 改为无阴影、小圆角
        ) {
            content()
        }
    }
}
```

#### 具体实现示例

**1. TextField 统一样式（搜索框）**

在 `Theme.kt` 中定义扩展函数：

```kotlin
@Composable
fun instagramTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color(0xFFEFEFEF),
    unfocusedContainerColor = Color(0xFFEFEFEF),
    disabledContainerColor = Color(0xFFEFEFEF),
    focusedIndicatorColor = Color.Transparent,  // 去掉下划线
    unfocusedIndicatorColor = Color.Transparent,
    focusedTextColor = Color(0xFF262626),
    unfocusedTextColor = Color(0xFF262626)
)

// 使用时：
TextField(
    value = text,
    onValueChange = { text = it },
    colors = instagramTextFieldColors(),  // 统一调用
    shape = RoundedCornerShape(12.dp)     // 从 Theme.shapes.medium 继承
)
```

**2. Button 统一样式**

定义 Primary 和 Secondary 两种按钮样式：

```kotlin
// Primary 按钮（蓝色填充）
@Composable
fun InstagramPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF0095F6),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF0095F6).copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp),  // 小圆角
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)  // 无阴影
    ) {
        content()
    }
}

// Secondary 按钮（灰色填充）
@Composable
fun InstagramSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFEFEFEF),
            contentColor = Color(0xFF262626)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
    ) {
        content()
    }
}
```

**3. Divider 统一样式**

```kotlin
@Composable
fun InstagramDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = Color(0xFFDBDBDB)  // Outline 颜色
    )
}
```

**4. 图标统一使用 Outlined 变体**

在 `Theme.kt` 中定义图标映射：

```kotlin
object InstagramIcons {
    val Home = Icons.Outlined.Home
    val Search = Icons.Outlined.Search
    val Add = Icons.Outlined.Add
    val Favorite = Icons.Outlined.FavoriteBorder
    val FavoriteFilled = Icons.Filled.Favorite
    val Bookmark = Icons.Outlined.BookmarkBorder
    val BookmarkFilled = Icons.Filled.Bookmark
    // ... 其他图标
}
```

#### 开发规范

| 规范 | 说明 |
|------|------|
| ✅ **推荐** | 使用 `InstagramPrimaryButton` / `InstagramSecondaryButton` / `instagramTextFieldColors()` 等封装好的组件 |
| ✅ **推荐** | 颜色引用 `MaterialTheme.colorScheme.primary` 而非硬编码 `Color(0xFF0095F6)` |
| ✅ **推荐** | 字体引用 `MaterialTheme.typography.titleLarge` 而非手动设置 `fontSize = 16.sp` |
| ❌ **禁止** | 在页面中单独设置 `elevation`、`shadowElevation`、`tonalElevation` |
| ❌ **禁止** | 在页面中单独设置 `shape = RoundedCornerShape(8.dp)`，应从 `MaterialTheme.shapes` 继承 |
| ❌ **禁止** | 使用 `Icons.Filled` 系列图标（除了 Favorite/Bookmark 的选中态） |

#### 后续维护

如需调整风格（如改变主题色、圆角大小），只需修改以下文件：

- 改颜色 → `Color.kt` + `Theme.kt` 的 `InstagramColorScheme`
- 改字体 → `Type.kt` 的 `InstagramTypography`
- 改圆角 → `Shape.kt` 的 `InstagramShapes`
- 改组件默认行为 → `Theme.kt` 的封装函数（如 `InstagramPrimaryButton`）

**所有页面自动生效，无需逐个修改。**

#### Material Design 3 官方文档

组件使用参考：https://m3.material.io/components

**重要提示**：如果在使用 MD3 组件时遇到疑问或问题（如参数不清楚、样式覆盖方式不明确），请先查阅上述官方文档，了解组件的标准用法和可配置属性，再结合本项目的风格定制进行调整。

### 2.8 文件说明与关键方法注释规范

为方便后期维护，项目中所有新建文件和关键公共方法都需要添加必要的说明性注释，但只写“说明用途”的注释，不写冗余废话。

#### 文件说明规范

每个新建文件顶部都应添加当前文件说明，描述：
- 这是什么文件
- 用来做什么
- 在项目中的职责是什么

推荐写法示例：

```kotlin
/**
 * 首页 Feed 页面。
 *
 * 负责展示 Stories 横向列表和帖子流，并处理点赞、收藏、关注等交互。
 * 对应 Vue 项目的 `pages/index/index.vue`。
 */
@Composable
fun HomeScreen() {
    // ...
}
```

```kotlin
/**
 * 用户仓库，负责统一处理用户相关的数据请求。
 *
 * 包括当前用户信息、用户统计、关注关系、粉丝/关注列表等接口调用，
 * 对外提供给 ViewModel 使用。
 */
class UserRepository {
    // ...
}
```

#### 方法注释规范

以下方法建议补充注释：
- 重要方法
- 通用方法
- 复用性强的方法
- 有业务规则的方法
- 参数或返回值不够直观的方法

重点是说明：
- 方法是做什么的
- 为什么这样做
- 参数的业务含义
- 特殊边界或注意事项

推荐写法示例：

```kotlin
/**
 * 加载首页帖子流。
 *
 * 使用 lastId 游标分页，而不是 page 分页。
 * 当 lastId 为 0 时表示首次加载；后续使用上一页最后一条帖子的 postId 继续请求。
 */
suspend fun loadFeed(lastId: String): FeedResponse<Post>
```

```kotlin
/**
 * 将后端返回的媒体地址转换为可直接访问的完整 URL。
 *
 * 后端有时返回相对路径，因此这里需要在非 http/https 开头时拼接 baseUrl。
 */
fun normalizeMediaUrl(url: String, baseUrl: String): String
```

#### 不需要注释的情况

以下内容不需要额外注释：
- 非常直白、看名字就能理解的局部变量
- 只有一两行且语义非常明确的简单方法
- 单纯为了“有注释而注释”的重复描述

#### 执行要求

| 规则 | 说明 |
|------|------|
| 每个新建文件 | 必须有文件职责说明 |
| Repository / ViewModel / Screen / 通用组件 | 建议在文件顶部写说明 |
| 工具类中的公共方法 | 建议写 KDoc 注释 |
| 复杂业务方法 | 必须写注释说明业务规则 |
| 简单 setter / getter / 显而易见的方法 | 不强制写注释 |

这样做的目标是：**让后期维护者快速看懂每个文件的职责，以及关键逻辑为什么这样实现。**

---

## 三、页面详细需求

### 2.1 登录页 (LoginScreen)

**UI 描述：**
- 居中显示 Instagram 风格 Logo
- 用户名输入框（placeholder: "用户名"）
- 密码输入框（密码遮掩，placeholder: "密码"）
- 登录按钮（蓝色圆角，无输入时置灰不可点击）

**交互逻辑：**
1. 用户输入账号密码，点击登录
2. 调用 `POST /api/auth/login`，请求体 `{ account, password }`
3. 成功返回 `{ token, refreshToken, expiresIn }`，存入 DataStore
4. 立即调用 `GET /api/user/me` 获取用户信息，存入 DataStore
5. 跳转到首页 Feed

**调用接口：**
- `POST /api/auth/login`
- `GET /api/user/me`

---

### 2.2 首页 Feed (HomeScreen)

**UI 描述：**
- 顶部固定导航栏：左侧 Instagram Logo，右侧通知图标 + 私信图标
- Stories 区域：横向可滚动圆形头像列表
  - 未读 Story：彩色渐变边框（`#f09433 → #e6683c → #dc2743 → #cc2366 → #bc1888`）
  - 已读 Story：灰色边框 `#dbdbdb`
  - 下方显示用户名
- 帖子流（LazyColumn 无限滚动）：每个帖子使用 PostCard 组件
- 底部触底加载更多

**交互逻辑：**
1. 页面加载时并行请求 Stories 和第一页帖子
2. 帖子流使用游标分页：首次 `lastId=0`，每页 6 条，后续用返回的 `lastId`
3. `hasMore=false` 时停止加载
4. 点赞/收藏采用乐观更新：先改 UI 状态再发请求，失败时回滚
5. 点赞/收藏触发弹跳动画（260ms）
6. 点击头像/用户名：自己 → 个人主页 Tab，他人 → 用户详情页
7. 帖子头部显示"关注"按钮（仅未关注用户），点击后隐藏

**调用接口：**
- `GET /api/story/feed`
- `GET /api/post/feed` → `{ lastId, pageSize=6 }`
- `POST /api/post/like` / `POST /api/post/unlike`
- `POST /api/post/save` / `POST /api/post/unsave`
- `POST /api/user/follow`

---

### 2.3 探索页 (ExploreScreen)

**UI 描述：**
- 默认模式：顶部搜索框（未聚焦），下方双列瀑布流网格
  - 视频类型右上角显示播放图标
  - 多图类型右上角显示多图图标
- 搜索模式：搜索框聚焦后切换
  - 上半部分标签结果（标签名 + 帖子数）
  - 下半部分用户结果（头像 + 用户名 + 认证标识）
  - 搜索框右侧出现"取消"按钮

**交互逻辑：**
1. 页面加载请求探索推荐，每页 24 条，支持分页
2. 搜索框聚焦进入搜索模式，输入 350ms 防抖触发搜索
3. 搜索同时请求标签和用户两个接口（一个失败不影响另一个）
4. 点击标签 → 搜索结果页，点击用户 → 用户详情页
5. 清空搜索框或点击取消退出搜索模式

**调用接口：**
- `GET /api/explore/feed` → `{ page, pageSize=24 }`
- `GET /api/search/tag` → `{ keyword, page, pageSize }`
- `GET /api/search/user` → `{ keyword, page, pageSize }`

---

### 2.4 发布页 (PublishScreen)

**UI 描述：**
- 顶部导航栏：返回箭头 + 标题 + 分享按钮（蓝色，无内容时半透明）
- 模式切换 Tab：图片 | 视频（Reels）
- 图片模式：3 列网格预览，每张右上角删除按钮，最后一格"+"添加（最多 9 张）
- 视频模式：视频预览播放器 + 删除按钮，或"+"选择按钮
- 文案输入区：`#hashtag` 蓝色高亮，输入 `#` 后触发标签自动补全面板
- 上传中全屏半透明遮罩 + 进度提示

**交互逻辑：**
1. 切换模式时清空已选内容
2. 图片选择：系统相册，最多 9 张
3. 视频选择：系统相册，mp4/mov，最大 50MB
4. 文案输入时实时解析 `#tag` 片段蓝色高亮
5. `#` 后输入 250ms 防抖搜索标签，显示补全面板
6. 选择补全标签后自动替换并加空格
7. 返回时如有内容弹出确认对话框
8. 发布流程：逐个上传媒体文件 → 收集 URL → 调用 createPost

**调用接口：**
- `POST /api/upload/image` → FormData
- `POST /api/upload/video` → FormData
- `POST /api/post/create` → `{ content, location, mediaType, mediaUrls[], coverUrl? }`
- `GET /api/search/tag` → `{ keyword }`（标签补全）

---

### 2.5 消息页 (MessagesScreen)

占位页面，居中显示"消息"文字，后续迭代实现。

---

### 2.6 个人主页 (ProfileScreen)

**UI 描述：**
- 顶部：用户名 + 菜单图标
- ProfileSummary：左侧大头像，右侧三列统计（帖子数 / 粉丝 / 关注），可点击
- 显示名 + 简介
- 两个按钮：编辑主页 | 分享主页
- 发现用户横向滚动卡片（头像 + 用户名 + 关注按钮 + 关闭按钮）
- 三栏 Tab：帖子网格 / Reels 网格 / 已标记
- 帖子网格：三列等宽正方形封面

**交互逻辑：**
1. 页面加载并行请求：用户统计、推荐用户（5个）、帖子列表
2. 用户基础信息从 DataStore 读取，统计数据从接口获取
3. 切换 Reels Tab 时懒加载视频列表
4. 点击粉丝/关注数 → 粉丝关注列表页
5. 点击编辑主页 → 编辑资料页
6. 推荐用户卡片：关注/取关切换，关闭从列表移除

**调用接口：**
- `GET /api/user/stats` → `{ userId }`
- `GET /api/user/discover` → `{ limit=5 }`
- `GET /api/user/posts` → `{ userId, page, pageSize, mediaType? }`
- `POST /api/user/follow` / `POST /api/user/unfollow`

---

### 2.7 用户详情页 (UserDetailScreen)

**UI 描述：**
- 顶部：返回按钮 + 用户名 + 菜单图标
- ProfileSummary：头像 + 统计数据 + 显示名 + 简介 + 认证标识
- 动态操作按钮：
  - 未关注：蓝色"关注"按钮
  - 已关注：灰色"已关注"按钮
  - 互相关注：灰色"互相关注"按钮
- 三栏 Tab：帖子网格 / Reels 网格 / 其他
- 点击帖子缩略图弹出全屏帖子详情流

**交互逻辑：**
1. 接收 `userId` 参数，加载用户信息和统计数据
2. 默认加载帖子网格（18 条/页）
3. 点击帖子缩略图 → 弹出 FullscreenPopup，显示完整帖子卡片
4. 弹窗内支持双向分页：向上滑动加载更早帖子，向下滑动加载更新帖子
5. 关注/取关操作立即更新按钮状态和粉丝数

**调用接口：**
- `GET /api/user/info` → `{ userId }`
- `GET /api/user/stats` → `{ userId }`
- `GET /api/user/posts` → `{ userId, page, pageSize }`
- `GET /api/user/posts/detail` → `{ userId, postId, direction? }`（双向分页）
- `POST /api/user/follow` / `POST /api/user/unfollow`
- `POST /api/post/like` / `POST /api/post/unlike`
- `POST /api/post/save` / `POST /api/post/unsave`

---

### 2.8 搜索结果页 (SearchOverviewScreen)

**UI 描述：**
- 顶部：返回按钮 + 搜索关键词展示
- 三栏 Tab：推荐 / 账户 / 标签
- 推荐 Tab：三列网格帖子封面
- 账户 Tab：列表（头像 + 用户名 + 粉丝数 + 认证标识 + 关注按钮）
- 标签 Tab：列表（封面图 + 标签名 + 帖子数）
- 滚动到底部触发分页加载

**交互逻辑：**
1. 接收 `keyword` 参数
2. 默认显示推荐 Tab，懒加载其他 Tab
3. 每个 Tab 独立分页（20 条/页）
4. 点击帖子 → 帖子详情，点击用户 → 用户详情页，点击标签 → 刷新当前页面（新关键词）

**调用接口：**
- `GET /api/search/post` → `{ keyword, type='all', page, pageSize }`
- `GET /api/search/user` → `{ keyword, page, pageSize }`
- `GET /api/search/tag` → `{ keyword, page, pageSize }`

---

### 2.9 编辑资料页 (EditProfileScreen)

**UI 描述：**
- 顶部：返回 + 标题"编辑主页" + 完成按钮（蓝色）
- 居中大头像 + "更换头像照片"链接
- 表单列表：
  - 用户名（可编辑）
  - 显示名（可编辑）
  - 简介（可编辑，多行）
  - 每项右侧箭头，点击进入单字段编辑页

**交互逻辑：**
1. 从 DataStore 读取当前用户信息预填充
2. 点击头像 → 系统相册选择 → 上传 → 更新头像
3. 点击字段 → 跳转单字段编辑页（EditFieldScreen）
4. 点击完成 → 调用更新接口 → 更新 DataStore → 返回

**调用接口：**
- `POST /api/upload/avatar` → FormData
- `POST /api/user/profile/update` → `{ displayName?, bio?, ... }`

---

### 2.10 单字段编辑页 (EditFieldScreen)

**UI 描述：**
- 顶部：返回 + 字段名称 + 完成按钮
- 单个输入框（自动聚焦）
- 字符计数提示（如简介 150 字符限制）

**交互逻辑：**
1. 接收 `fieldName` 和 `currentValue` 参数
2. 输入框预填充当前值
3. 点击完成 → 返回上一页并传递新值

**调用接口：** 无（由上一页统一提交）

---

### 2.11 粉丝关注列表页 (FollowListScreen)

**UI 描述：**
- 顶部：返回 + 用户名
- 两栏 Tab：粉丝 / 关注
- 列表项：头像 + 用户名 + 显示名 + 关注按钮（自己除外）
- 滚动到底部分页加载

**交互逻辑：**
1. 接收 `userId` 和 `initialTab`（默认粉丝）
2. 切换 Tab 时懒加载对应列表
3. 关注/取关操作立即更新按钮状态
4. 点击用户 → 用户详情页

**调用接口：**
- `GET /api/user/followers` → `{ userId, page, pageSize }`
- `GET /api/user/following` → `{ userId, page, pageSize }`
- `POST /api/user/follow` / `POST /api/user/unfollow`

---

## 三、通用组件清单

### 3.1 PostCard（帖子卡片）

**功能：** 完整的帖子展示组件，首页 Feed 和用户详情弹窗复用

**UI 结构：**
- 头部：头像（40dp 圆形）+ 用户名 + 地点 + 关注按钮 + 更多菜单（`•••`）
- 内容区：ImageSwiper 组件（多图/视频轮播）
- 操作栏：点赞图标 + 评论图标 + 分享图标（左侧），收藏图标（右侧）
- 文字区：点赞数 + 内容文字（超 60 字符折叠，"展开"链接）+ 标签高亮（蓝色）+ 发布时间

**Props：**
- `post: Post` — 帖子数据
- `stats: PostStats` — 统计数据
- `onLike: () -> Unit`
- `onSave: () -> Unit`
- `onFollow: () -> Unit`
- `onUserClick: () -> Unit`

---

### 3.2 ImageSwiper（图片/视频轮播）

**功能：** 支持多图横向滑动 + 视频播放

**UI 结构：**
- HorizontalPager 实现滑动
- 图片使用 Coil AsyncImage
- 视频使用 ExoPlayer + AndroidView
- 底部圆点指示器（多图时显示，当前页高亮）

**Props：**
- `mediaList: List<Media>` — `{ url, type, width, height }`
- `aspectRatio: Float` — 默认 1f（正方形）

---

### 3.3 ProfileSummary（用户资料摘要）

**功能：** 个人主页和用户详情页复用

**UI 结构：**
- 左侧：大头像（80dp 圆形）
- 右侧：三列统计数据（帖子数 / 粉丝 / 关注），每列可点击
- 下方：显示名 + 认证标识 + 简介

**Props：**
- `user: User`
- `stats: UserStats`
- `onStatsClick: (type: String) -> Unit` — type: "posts" | "followers" | "following"

---

### 3.4 ProfileGrid（帖子网格）

**功能：** 三列等宽网格，正方形封面

**UI 结构：**
- LazyVerticalGrid (columns = 3, spacing = 2dp)
- 每个 item：正方形封面图 + 右上角类型角标（多图/视频图标）

**Props：**
- `items: List<ExploreItem>`
- `onItemClick: (postId: String) -> Unit`

---

### 3.5 FullscreenPopup（全屏弹窗）

**功能：** 从底部滑入的全屏弹窗容器

**UI 结构：**
- 背景半透明遮罩（点击关闭）
- 内容区从底部滑入（300ms 动画）
- 内部 LazyColumn 支持滚动到边界触发回调

**Props：**
- `show: Boolean`
- `onDismiss: () -> Unit`
- `onScrollToTop: () -> Unit` — 向上滚动到顶部触发
- `onScrollToBottom: () -> Unit` — 向下滚动到底部触发
- `content: @Composable () -> Unit`

---

## 四、API 接口清单

### 4.1 认证模块 (auth)

| 接口 | 方法 | 参数 | 返回 |
|-----|------|------|------|
| `/api/auth/login` | POST | `{ account, password }` | `{ token, refreshToken, expiresIn }` |
| `/api/auth/refresh` | POST | `{ refreshToken }` | `{ token, refreshToken, expiresIn }` |
| `/api/auth/logout` | POST | 无 | — |

---

### 4.2 用户模块 (user)

| 接口 | 方法 | 参数 | 返回 |
|-----|------|------|------|
| `/api/user/me` | GET | 无 | `User` |
| `/api/user/info` | GET | `{ userId }` | `User` |
| `/api/user/stats` | GET | `{ userId }` | `UserStats` |
| `/api/user/follow` | POST | `{ userId }` | — |
| `/api/user/unfollow` | POST | `{ userId }` | — |
| `/api/user/posts` | GET | `{ userId, page, pageSize, mediaType? }` | 分页 `Post[]` |
| `/api/user/reels` | GET | `{ userId, page, pageSize }` | 分页 `Post[]` |
| `/api/user/discover` | GET | `{ limit }` | `User[]` |
| `/api/user/followers` | GET | `{ userId, page, pageSize }` | 分页 `User[]` |
| `/api/user/following` | GET | `{ userId, page, pageSize }` | 分页 `User[]` |
| `/api/user/profile/update` | POST | `{ displayName?, bio?, ... }` | — |
| `/api/user/posts/detail` | GET | `{ userId, postId, direction? }` | `Post + PostStats` |

---

### 4.3 帖子模块 (post)

| 接口 | 方法 | 参数 | 返回 |
|-----|------|------|------|
| `/api/post/feed` | GET | `{ lastId, pageSize }` | `Post[] + hasMore` |
| `/api/post/detail` | GET | `{ postId }` | `Post + PostStats` |
| `/api/post/like` | POST | `{ postId }` | — |
| `/api/post/unlike` | POST | `{ postId }` | — |
| `/api/post/save` | POST | `{ postId }` | — |
| `/api/post/unsave` | POST | `{ postId }` | — |
| `/api/post/create` | POST | `{ content, mediaType, mediaUrls[], location?, coverUrl? }` | `Post` |
| `/api/post/update` | POST | `{ postId, content, ... }` | — |
| `/api/post/delete` | POST | `{ postId }` | — |

---

### 4.4 其他模块

| 模块 | 接口 | 方法 | 参数 | 返回 |
|-----|-----|------|------|------|
| story | `/api/story/feed` | GET | 无 | `Story[]` |
| explore | `/api/explore/feed` | GET | `{ page, pageSize }` | 分页 `ExploreItem[]` |
| search | `/api/search/user` | GET | `{ keyword, page, pageSize }` | 分页 `User[]` |
| search | `/api/search/tag` | GET | `{ keyword, page, pageSize }` | 分页 `Tag[]` |
| search | `/api/search/post` | GET | `{ keyword, type, page, pageSize }` | 分页 `Post[]` |
| upload | `/api/upload/image` | POST | FormData `{ file }` | `{ url }` |
| upload | `/api/upload/avatar` | POST | FormData `{ file }` | `{ url }` |
| upload | `/api/upload/video` | POST | FormData `{ file }` | `{ url }` |

---

## 五、数据模型定义

### 5.1 核心模型

```kotlin
data class User(
    val userId: String,
    val username: String,
    val displayName: String,
    val avatar: String,
    val bio: String?,
    val isVerified: Boolean,
    val isPrivate: Boolean
)

data class UserStats(
    val userId: String,
    val postsCount: Int,
    val followersCount: Int,
    val followingCount: Int
)

data class Post(
    val postId: String,
    val userId: String,
    val username: String,
    val avatar: String,
    val isVerified: Boolean,
    val location: String?,
    val content: String,
    val mediaType: String, // "image" | "video"
    val mediaList: List<Media>,
    val tags: List<Tag>,
    val createdAt: Long
)

data class Media(
    val url: String,
    val width: Int,
    val height: Int,
    val type: String // "image" | "video"
)

data class PostStats(
    val postId: String,
    val likesCount: Int,
    val savedCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val isLiked: Boolean,
    val isSaved: Boolean
)

data class Story(
    val storyId: String,
    val userId: String,
    val username: String,
    val avatar: String,
    val hasUnread: Boolean,
    val expiredAt: Long
)

data class ExploreItem(
    val postId: String,
    val mediaType: String,
    val coverUrl: String,
    val mediaCount: Int,
    val width: Int,
    val height: Int
)

data class Tag(
    val tagId: String,
    val name: String, // 带 # 前缀
    val heat: Int,
    val postCount: Int
)
```

### 5.2 网络响应包装

```kotlin
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?,
    val timestamp: Long
)

data class PagedResponse<T>(
    val list: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean
)
```

**关键注意事项：**
- 所有 ID 字段为 `String` 类型（后端 BIGINT 序列化为 String）
- 媒体 URL 为相对路径时需拼接 `baseUrl`（`http://112.124.47.169:8081`）
- 后端返回的 list 可能是数组或 `{ list: [] }` 对象，Repository 层需兼容处理

---

## 六、开发优先级建议

### 第一阶段：基础骨架（3-4 天）

1. **项目初始化**：包结构搭建（data/ui/util）
2. **网络层**：Retrofit Service 定义、OkHttp Interceptor（Token 注入 + 401 处理）、ApiResponse 拆包
3. **DataStore**：Token/UserInfo 持久化读写
4. **Navigation**：5 个底部 Tab + 子页面路由表
5. **登录页**：完整实现（第一个可运行的闭环）

### 第二阶段：核心浏览（5-6 天）

6. **PostCard 组件**：最核心的复用组件，先做好
7. **ImageSwiper 组件**：多图轮播 + 圆点指示器 + 视频播放
8. **首页 Feed**：Stories + 帖子流 + 无限滚动 + 点赞/收藏
9. **个人主页**：ProfileSummary + ProfileGrid + Tab 切换

### 第三阶段：社交互动（4-5 天）

10. **用户详情页**：复用 Profile 组件 + 关注/取关
11. **粉丝关注列表页**
12. **帖子详情弹窗**：FullscreenPopup + 双向分页

### 第四阶段：探索与搜索（3-4 天）

13. **探索页**：瀑布流 + 搜索模式切换
14. **搜索结果页**：三栏 Tab + 分页加载

### 第五阶段：内容创作（5-6 天）

15. **发布页**：图片/视频选择 + 上传 + 文案编辑 + 标签补全
16. **编辑资料页** + **单字段编辑页**

### 第六阶段：优化与完善（3-4 天）

17. 动画效果优化（点赞弹跳、页面转场）
18. 错误处理与加载状态
19. 离线缓存策略（可选）
20. 性能优化（图片加载、列表滚动）

---

**总计预估：23-29 天**（按每天 6-8 小时工作量）

启哥，开发文档已完成！包含 11 个页面详细需求、5 个通用组件、32 个 API 接口、完整数据模型和分阶段开发计划。确认没问题的话，我就可以开始搭建项目骨架了。
