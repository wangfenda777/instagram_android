package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qiwang.ins_android.R
import com.qiwang.ins_android.data.model.Story
import com.qiwang.ins_android.ui.components.MediaImage
import com.qiwang.ins_android.ui.components.PostCard
import com.qiwang.ins_android.ui.theme.InstagramDivider
import com.qiwang.ins_android.ui.theme.OutlineGray
import com.qiwang.ins_android.ui.viewmodel.HomeViewModel

/**
 * 首页 Feed 页面。
 *
 * 负责展示 Stories 横向列表和帖子流，并处理点赞、收藏、关注等交互。
 * 对应 Vue 项目的 pages/index/index.vue。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToUserDetail: (String) -> Unit = {}
) {
    val stories by viewModel.stories.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()

    val listState = rememberLazyListState()

    // 监听滚动到底部，触发加载更多
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && lastVisibleIndex >= posts.size - 2 && hasMore && !isLoading) {
                    viewModel.loadMorePosts()
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部导航栏
        TopAppBar(
            title = {
                Icon(
                    painter = painterResource(id = R.drawable.instagram_simple),
                    contentDescription = "Instagram",
                    modifier = Modifier.size(120.dp),
                    tint = Color.Unspecified
                )
            },
            actions = {
                IconButton(onClick = { /* TODO: 通知 */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.like),
                        contentDescription = "通知"
                    )
                }
                IconButton(onClick = { /* TODO: 私信 */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.airport),
                        contentDescription = "私信"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        InstagramDivider()

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            // Stories 横向滚动区
            if (stories.isNotEmpty()) {
                item {
                    StoriesRow(stories = stories)
                    InstagramDivider()
                }
            }

            // 帖子列表
            items(posts) { post ->
                PostCard(
                    post = post,
                    onLike = { viewModel.toggleLike(post.postId) },
                    onSave = { viewModel.toggleSave(post.postId) },
                    onFollow = { viewModel.followUser(post.userId) },
                    onUserClick = { onNavigateToUserDetail(post.userId) },
                    showFollowButton = !post.isFollowing
                )
                InstagramDivider()
            }

            // 加载更多指示器
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

/**
 * Stories 横向滚动区域。
 */
@Composable
private fun StoriesRow(stories: List<Story>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        items(stories) { story ->
            StoryItem(story = story)
        }
    }
}

/**
 * 单个 Story 项。
 */
@Composable
private fun StoryItem(story: Story) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        // 头像 + 渐变边框
        Box(
            modifier = Modifier
                .size(72.dp)
                .border(
                    width = 2.dp,
                    brush = if (story.hasUnread) {
                        // 未读：彩色渐变边框
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFf09433),
                                Color(0xFFe6683c),
                                Color(0xFFdc2743),
                                Color(0xFFcc2366),
                                Color(0xFFbc1888)
                            )
                        )
                    } else {
                        // 已读：灰色边框
                        Brush.linearGradient(colors = listOf(OutlineGray, OutlineGray))
                    },
                    shape = CircleShape
                )
                .padding(3.dp)
        ) {
            MediaImage(
                url = story.avatar,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 用户名
        Text(
            text = story.username,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
        )
    }
}
