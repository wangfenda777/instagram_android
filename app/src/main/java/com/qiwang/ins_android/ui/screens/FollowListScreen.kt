/**
 * 粉丝/关注列表页面。
 *
 * 展示指定用户的粉丝和关注列表，支持 Tab 切换和分页加载。
 * 通过路由参数接收 userId。
 */
package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qiwang.ins_android.R
import com.qiwang.ins_android.data.model.User
import com.qiwang.ins_android.ui.components.MediaImage
import com.qiwang.ins_android.ui.theme.*
import com.qiwang.ins_android.ui.viewmodel.FollowListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowListScreen(
    userId: String,
    onBack: () -> Unit = {},
    onNavigateToUserDetail: (String) -> Unit = {}
) {
    val viewModel = remember(userId) { FollowListViewModel(userId) }

    val followers by viewModel.followers.collectAsState()
    val following by viewModel.following.collectAsState()
    val followersLoading by viewModel.followersLoading.collectAsState()
    val followingLoading by viewModel.followingLoading.collectAsState()
    val followersHasMore by viewModel.followersHasMore.collectAsState()
    val followingHasMore by viewModel.followingHasMore.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("粉丝和关注", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(painter = painterResource(R.drawable.back), contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        InstagramDivider()

        // Tab 栏
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.White,
            contentColor = TextPrimary,
            divider = { HorizontalDivider(thickness = 1.dp, color = OutlineGray) }
        ) {
            Tab(selected = activeTab == 0, onClick = { viewModel.switchTab(0) }) {
                Text("粉丝", modifier = Modifier.padding(vertical = 14.dp), fontSize = 14.sp,
                    fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal)
            }
            Tab(selected = activeTab == 1, onClick = { viewModel.switchTab(1) }) {
                Text("关注", modifier = Modifier.padding(vertical = 14.dp), fontSize = 14.sp,
                    fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal)
            }
        }

        // 列表
        val users = if (activeTab == 0) followers else following
        val isLoading = if (activeTab == 0) followersLoading else followingLoading
        val hasMore = if (activeTab == 0) followersHasMore else followingHasMore
        val listState = rememberLazyListState()

        // 滚动到底部加载更多
        LaunchedEffect(listState, activeTab) {
            snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
                .collect { lastIndex ->
                    if (lastIndex != null && lastIndex >= users.size - 3 && hasMore && !isLoading) {
                        viewModel.loadMore()
                    }
                }
        }

        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(users) { user ->
                FollowListItem(
                    user = user,
                    onToggleFollow = { viewModel.toggleFollow(user.userId) },
                    onUserClick = { onNavigateToUserDetail(user.userId) }
                )
                InstagramDivider()
            }

            if (isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowListItem(
    user: User,
    onToggleFollow: () -> Unit,
    onUserClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MediaImage(
            url = user.avatar,
            modifier = Modifier.size(40.dp).clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.username,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (user.displayName.isNotEmpty()) {
                Text(
                    text = user.displayName,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (user.isFollowing) {
            InstagramSecondaryButton(onClick = onToggleFollow) {
                Text("已关注", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            InstagramPrimaryButton(onClick = onToggleFollow) {
                Text("关注", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
