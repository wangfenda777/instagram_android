/**
 * 用户帖子滚动列表页。
 *
 * 从个人主页或用户详情页点击帖子进入，支持上下滚动浏览该用户的所有帖子。
 * 初次加载后端返回锚点帖子的前后各5条，通过 anchorIndex 直接定位到锚点位置。
 * 后续滚动触发双向游标加载。
 */
package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qiwang.ins_android.R
import com.qiwang.ins_android.ui.components.PostCard
import com.qiwang.ins_android.ui.theme.InstagramDivider
import com.qiwang.ins_android.ui.viewmodel.UserPostsDetailViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPostsDetailScreen(
    userId: String,
    postId: String,
    onBack: () -> Unit = {},
    onNavigateToUserDetail: (String) -> Unit = {}
) {
    val viewModel = remember(userId, postId) { UserPostsDetailViewModel(userId, postId) }

    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasMoreBefore by viewModel.hasMoreBefore.collectAsState()
    val hasMoreAfter by viewModel.hasMoreAfter.collectAsState()
    val anchorIndex by viewModel.anchorIndex.collectAsState()

    val listState = rememberLazyListState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    // 初次加载完成后，滚动到锚点帖子位置（只执行一次）
    var hasScrolledToAnchor by remember { mutableStateOf(false) }
    LaunchedEffect(anchorIndex, posts.size) {
        if (posts.isNotEmpty() && !hasScrolledToAnchor && anchorIndex > 0) {
            listState.scrollToItem(anchorIndex)
            hasScrolledToAnchor = true
        } else if (posts.isNotEmpty() && !hasScrolledToAnchor) {
            hasScrolledToAnchor = true
        }
    }

    // 监听滚动到顶部，触发向前加载
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { firstIndex ->
                if (firstIndex <= 1 && hasMoreBefore && !isLoading) {
                    viewModel.loadBefore()
                }
            }
    }

    // 监听滚动到底部，触发向后加载
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .map { lastIndex ->
                lastIndex != null && lastIndex >= posts.size - 2
            }
            .distinctUntilChanged()
            .collect { isNearBottom ->
                if (isNearBottom && hasMoreAfter && !isLoading) {
                    viewModel.loadAfter()
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "帖子",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "返回"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        InstagramDivider()

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = screenHeight)
        ) {
            // 顶部加载指示器
            if (isLoading && hasMoreBefore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }

            // 帖子列表
            items(posts, key = { it.postId }) { post ->
                PostCard(
                    post = post,
                    onLike = { viewModel.toggleLike(post.postId) },
                    onSave = { viewModel.toggleSave(post.postId) },
                    onFollow = { /* 不显示关注按钮 */ },
                    onUserClick = { onNavigateToUserDetail(post.userId) },
                    expandContent = true,
                    showFollowButton = false
                )
                InstagramDivider()
            }

            // 底部加载指示器
            if (isLoading && hasMoreAfter) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
            }

            // 没有更多内容提示
            if (!hasMoreAfter && posts.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "没有更多内容了",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
