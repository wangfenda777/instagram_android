/**
 * 个人主页。
 *
 * 负责展示个人信息、帖子/Reels/标记网格、推荐用户等。
 * 对应 Vue 项目的 pages/profile/profile.vue。
 */
package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qiwang.ins_android.R
import com.qiwang.ins_android.data.model.User
import com.qiwang.ins_android.ui.components.MediaImage
import com.qiwang.ins_android.ui.components.ProfileGrid
import com.qiwang.ins_android.ui.components.ProfileSummary
import com.qiwang.ins_android.ui.components.ProfileTabs
import com.qiwang.ins_android.ui.theme.*
import com.qiwang.ins_android.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateToFollowList: (String) -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToUserPostsDetail: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val viewModel = remember { ProfileViewModel(context) }

    val userInfo by viewModel.userInfo.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val discoverUsers by viewModel.discoverUsers.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val reels by viewModel.reels.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部栏
        TopAppBar(
            title = {
                Text(
                    text = userInfo.username.ifEmpty { "个人主页" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            actions = {
                IconButton(onClick = { /* TODO: 菜单 */ }) {
                    Icon(
                        painter = painterResource(R.drawable.three_o),
                        contentDescription = "菜单"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        InstagramDivider()

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // 头像 + 统计
            item {
                ProfileSummary(
                    user = userInfo,
                    stats = stats,
                    onFollowersClick = { onNavigateToFollowList(userInfo.userId) },
                    onFollowingClick = { onNavigateToFollowList(userInfo.userId) }
                )
            }

            // 显示名称和简介
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (userInfo.displayName.isNotEmpty()) {
                        Text(
                            text = userInfo.displayName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                    if (!userInfo.bio.isNullOrEmpty()) {
                        Text(
                            text = userInfo.bio!!,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // 编辑主页 + 分享主页 按钮
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InstagramSecondaryButton(
                        onClick = onNavigateToEditProfile,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("编辑主页", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    InstagramSecondaryButton(
                        onClick = { /* TODO: 分享主页 */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("分享主页", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 推荐用户横向滚动
            if (discoverUsers.isNotEmpty()) {
                item {
                    DiscoverUsersRow(
                        users = discoverUsers,
                        onFollow = { viewModel.toggleFollowDiscover(it) },
                        onRemove = { viewModel.removeDiscoverUser(it) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Tab 切换栏
            item {
                ProfileTabs(
                    activeTab = activeTab,
                    onTabChange = { viewModel.switchTab(it) }
                )
            }

            // 帖子网格
            item {
                val currentList = when (activeTab) {
                    0 -> posts
                    1 -> reels
                    else -> emptyList()
                }
                ProfileGrid(
                    posts = currentList,
                    onItemClick = { postId ->
                        onNavigateToUserPostsDetail(userInfo.userId, postId)
                    }
                )
            }
        }
    }
}

/**
 * 推荐用户横向滚动区域。
 */
@Composable
private fun DiscoverUsersRow(
    users: List<User>,
    onFollow: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
        Text(
            text = "你可能认识的人",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(users) { user ->
            DiscoverUserCard(
                user = user,
                onFollow = { onFollow(user.userId) },
                onRemove = { onRemove(user.userId) }
            )
        }
    }
}

/**
 * 单个推荐用户卡片。
 */
@Composable
private fun DiscoverUserCard(
    user: User,
    onFollow: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.width(150.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // 关闭按钮
            Icon(
                painter = painterResource(R.drawable.close),
                contentDescription = "移除",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(14.dp)
                    .clickable { onRemove() },
                tint = TextSecondary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MediaImage(
                url = user.avatar,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user.username,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (user.isFollowing) {
                InstagramSecondaryButton(
                    onClick = onFollow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("已关注", fontSize = 12.sp)
                }
            } else {
                InstagramPrimaryButton(
                    onClick = onFollow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("关注", fontSize = 12.sp)
                }
            }
        }
    }
}
