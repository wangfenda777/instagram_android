/**
 * 用户详情页。
 *
 * 展示指定用户的个人信息、统计数据、帖子/Reels 网格，
 * 以及关注/取关操作。通过路由参数接收 userId。
 */
package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qiwang.ins_android.R
import com.qiwang.ins_android.ui.components.ProfileGrid
import com.qiwang.ins_android.ui.components.ProfileSummary
import com.qiwang.ins_android.ui.components.ProfileTabs
import com.qiwang.ins_android.ui.theme.*
import com.qiwang.ins_android.ui.viewmodel.UserDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    userId: String,
    onBack: () -> Unit = {},
    onNavigateToFollowList: (String) -> Unit = {},
    onNavigateToUserDetail: (String) -> Unit = {},
    onNavigateToUserPostsDetail: (String, String) -> Unit = { _, _ -> }
) {
    val viewModel = remember(userId) { UserDetailViewModel(userId) }

    val userInfo by viewModel.userInfo.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val reels by viewModel.reels.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = userInfo.username.ifEmpty { "用户详情" },
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
                    onFollowersClick = { onNavigateToFollowList(userId) },
                    onFollowingClick = { onNavigateToFollowList(userId) }
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

            // 关注按钮
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (userInfo.isFollowing) {
                        InstagramSecondaryButton(
                            onClick = { viewModel.toggleFollow() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("已关注", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        InstagramPrimaryButton(
                            onClick = { viewModel.toggleFollow() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("关注", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
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
                        onNavigateToUserPostsDetail(userId, postId)
                    }
                )
            }
        }
    }
}
