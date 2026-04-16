/**
 * 探索页。
 *
 * 默认模式展示双列瀑布流推荐内容，搜索模式展示标签和用户搜索结果。
 * 对应 Vue 项目的 pages/explore/explore.vue。
 */
package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qiwang.ins_android.R
import com.qiwang.ins_android.data.model.ExploreItem
import com.qiwang.ins_android.data.model.Tag
import com.qiwang.ins_android.data.model.User
import com.qiwang.ins_android.ui.components.MediaImage
import com.qiwang.ins_android.ui.theme.*
import com.qiwang.ins_android.ui.viewmodel.ExploreViewModel
import com.qiwang.ins_android.util.MediaUtil

/**
 * 三列瀑布流网格。
 *
 * 图片帖子为正方形，视频帖子宽度相同但高度为图片的2倍。
 * 使用 LazyVerticalStaggeredGrid 实现真正的瀑布流，每列独立堆叠，不留空白。
 */
@Composable
private fun ExploreGrid(
    items: List<ExploreItem>,
    isLoading: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onSearchFocus: () -> Unit
) {
    val gridState = rememberLazyStaggeredGridState()

    // 滚动到底部加载更多
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= items.size - 4 && hasMore && !isLoading) {
                    onLoadMore()
                }
            }
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalItemSpacing = 2.dp
    ) {
        items(items) { item ->
            ExploreGridItem(item = item)
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

/**
 * 单个探索网格项。
 *
 * 图片帖子：正方形（aspectRatio = 1f）
 * 视频帖子：宽度相同，高度为图片的2倍（aspectRatio = 0.5f）
 */
@Composable
private fun ExploreGridItem(item: ExploreItem) {
    val aspectRatio = if (item.mediaType == "video") 0.5f else 1f

    Box(modifier = Modifier.aspectRatio(aspectRatio)) {
        MediaImage(
            url = item.coverUrl,
            modifier = Modifier.fillMaxSize()
        )

        // 右上角角标
        when {
            item.mediaType == "video" -> {
                Icon(
                    painter = painterResource(R.drawable.video),
                    contentDescription = "视频",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(16.dp),
                    tint = Color.White
                )
            }
            item.mediaCount > 1 -> {
                Icon(
                    painter = painterResource(R.drawable.copy),
                    contentDescription = "多图",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(16.dp),
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * 搜索结果列表（标签 + 用户）。
 */
@Composable
private fun SearchResultList(
    tags: List<Tag>,
    users: List<User>,
    onTagClick: (Tag) -> Unit,
    onUserClick: (User) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // 标签结果
        if (tags.isNotEmpty()) {
            item {
                Text(
                    text = "标签",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            items(tags) { tag ->
                TagSearchItem(tag = tag, onClick = { onTagClick(tag) })
            }
        }

        // 用户结果
        if (users.isNotEmpty()) {
            item {
                Text(
                    text = "用户",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
            items(users) { user ->
                UserSearchItem(user = user, onClick = { onUserClick(user) })
            }
        }
    }
}

/**
 * 标签搜索结果项。
 */
@Composable
private fun TagSearchItem(tag: Tag, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // # 图标圆形背景
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(InputBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("#", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = TextPrimary)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "#${tag.name}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextPrimary
            )
            Text(
                text = "${MediaUtil.formatCount(tag.postCount ?: 0)} 帖子",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

/**
 * 用户搜索结果项。
 */
@Composable
private fun UserSearchItem(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MediaImage(
            url = user.avatar,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.username,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                if (user.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.vip),
                        contentDescription = "认证",
                        modifier = Modifier.size(14.dp),
                        tint = InstagramBlue
                    )
                }
            }
            if (user.displayName.isNotBlank()) {
                Text(
                    text = user.displayName,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = remember { ExploreViewModel() },
    onNavigateToUserDetail: (String) -> Unit = {},
    onNavigateToSearchOverview: (String) -> Unit = {}
) {
    val exploreItems by viewModel.exploreItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()
    val isSearchMode by viewModel.isSearchMode.collectAsState()
    val searchKeyword by viewModel.searchKeyword.collectAsState()
    val searchTags by viewModel.searchTags.collectAsState()
    val searchUsers by viewModel.searchUsers.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // 搜索框
        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()

        LaunchedEffect(isFocused) {
            if (isFocused && !isSearchMode) {
                viewModel.enterSearchMode()
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = searchKeyword,
                onValueChange = { viewModel.onSearchKeywordChange(it) },
                modifier = Modifier.weight(1f),
                interactionSource = interactionSource,
                placeholder = {
                    Text("搜索", color = TextSecondary, fontSize = 14.sp)
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = "搜索",
                        modifier = Modifier.size(20.dp),
                        tint = TextSecondary
                    )
                },
                colors = instagramTextFieldColors(),
                shape = MaterialTheme.shapes.large,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )

            if (isSearchMode) {
                TextButton(
                    onClick = { viewModel.exitSearchMode() },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text("取消", color = TextPrimary, fontSize = 14.sp)
                }
            }
        }

        if (isSearchMode) {
            // 搜索结果列表
            SearchResultList(
                tags = searchTags,
                users = searchUsers,
                onTagClick = { tag -> onNavigateToSearchOverview(tag.name) },
                onUserClick = { user -> onNavigateToUserDetail(user.userId) }
            )
        } else {
            // 瀑布流网格
            ExploreGrid(
                items = exploreItems,
                isLoading = isLoading,
                hasMore = hasMore,
                onLoadMore = { viewModel.loadMore() },
                onSearchFocus = { viewModel.enterSearchMode() }
            )
        }
    }
}
