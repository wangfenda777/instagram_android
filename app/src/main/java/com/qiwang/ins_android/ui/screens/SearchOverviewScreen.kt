/**
 * 搜索结果页。
 *
 * 按关键词展示搜索结果，包含三个 Tab：推荐帖子、账户、标签。
 * 每个 Tab 独立分页加载，支持滚动到底部加载更多。
 * 对应 Vue 项目的 pages/search/searchOverview.vue。
 */
package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.qiwang.ins_android.ui.components.MediaImage
import com.qiwang.ins_android.ui.theme.*
import com.qiwang.ins_android.ui.viewmodel.SearchOverviewViewModel
import com.qiwang.ins_android.util.MediaUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchOverviewScreen(
    keyword: String,
    onBack: () -> Unit,
    onNavigateToUserDetail: (String) -> Unit = {}
) {
    val viewModel = remember(keyword) { SearchOverviewViewModel(keyword) }
    val activeTab by viewModel.activeTab.collectAsState()

    val tabTitles = listOf("推荐", "账户", "标签")

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部栏
        TopAppBar(
            title = {
                Text(
                    text = keyword,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        // Tab 栏
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.White,
            contentColor = TextPrimary,
            divider = { InstagramDivider() }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { viewModel.switchTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        // Tab 内容
        when (activeTab) {
            0 -> PostsTab(viewModel)
            1 -> UsersTab(viewModel, onNavigateToUserDetail)
            2 -> TagsTab(viewModel)
        }
    }
}

/**
 * 推荐帖子 Tab — 三列网格。
 */
@Composable
private fun PostsTab(viewModel: SearchOverviewViewModel) {
    val postItems by viewModel.postItems.collectAsState()
    val postLoading by viewModel.postLoading.collectAsState()
    val postHasMore by viewModel.postHasMore.collectAsState()
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= postItems.size - 4 && postHasMore && !postLoading) {
                    viewModel.loadMore()
                }
            }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = gridState,
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(postItems) { item ->
            Box(modifier = Modifier.aspectRatio(1f)) {
                MediaImage(
                    url = item.coverUrl,
                    modifier = Modifier.fillMaxSize()
                )
                if (item.mediaCount > 1) {
                    Icon(
                        painter = painterResource(R.drawable.copy),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(14.dp),
                        tint = Color.White
                    )
                }
                if (item.mediaType == "video") {
                    Icon(
                        painter = painterResource(R.drawable.video),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(14.dp),
                        tint = Color.White
                    )
                }
            }
        }

        if (postLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

/**
 * 账户 Tab — 用户列表。
 */
@Composable
private fun UsersTab(
    viewModel: SearchOverviewViewModel,
    onNavigateToUserDetail: (String) -> Unit
) {
    val userItems by viewModel.userItems.collectAsState()
    val userLoading by viewModel.userLoading.collectAsState()
    val userHasMore by viewModel.userHasMore.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= userItems.size - 3 && userHasMore && !userLoading) {
                    viewModel.loadMore()
                }
            }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(userItems) { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToUserDetail(user.userId) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MediaImage(
                    url = user.avatar,
                    modifier = Modifier.size(50.dp).clip(CircleShape)
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
                    Text(
                        text = "${MediaUtil.formatCount(user.followersCount)} 粉丝",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        if (userLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}

/**
 * 标签 Tab — 标签列表。
 */
@Composable
private fun TagsTab(viewModel: SearchOverviewViewModel) {
    val tagItems by viewModel.tagItems.collectAsState()
    val tagLoading by viewModel.tagLoading.collectAsState()
    val tagHasMore by viewModel.tagHasMore.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= tagItems.size - 3 && tagHasMore && !tagLoading) {
                    viewModel.loadMore()
                }
            }
    }

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(tagItems) { tag ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${tag.name}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${MediaUtil.formatCount(tag.postCount ?: 0)} 帖子",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        if (tagLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}