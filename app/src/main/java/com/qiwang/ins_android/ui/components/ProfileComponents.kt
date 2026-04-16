/**
 * 个人主页共享组件：ProfileSummary、ProfileTabs、ProfileGrid。
 *
 * 用于个人主页和用户详情页的头像统计区、Tab 切换栏、帖子网格。
 */
package com.qiwang.ins_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qiwang.ins_android.R
import com.qiwang.ins_android.data.model.Post
import com.qiwang.ins_android.data.model.User
import com.qiwang.ins_android.data.model.UserStats
import com.qiwang.ins_android.ui.theme.TextPrimary
import com.qiwang.ins_android.ui.theme.TextSecondary
import com.qiwang.ins_android.ui.theme.OutlineGray

/**
 * 个人主页头像 + 统计区域。
 *
 * 左侧头像，右侧帖子数/粉丝数/关注数。
 */
@Composable
fun ProfileSummary(
    user: User,
    stats: UserStats,
    onFollowersClick: () -> Unit = {},
    onFollowingClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 头像
        MediaImage(
            url = user.avatar,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(24.dp))

        // 统计数据
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(count = stats.postsCount, label = "帖子")
            StatItem(
                count = stats.followersCount,
                label = "粉丝",
                onClick = onFollowersClick
            )
            StatItem(
                count = stats.followingCount,
                label = "关注",
                onClick = onFollowingClick
            )
        }
    }
}

@Composable
private fun StatItem(count: Int, label: String, onClick: (() -> Unit)? = null) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    ) {
        Text(
            text = formatCount(count),
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = TextPrimary
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}

private fun formatCount(count: Int): String = when {
    count >= 10000 -> String.format("%.1fw", count / 10000.0)
    count >= 1000 -> String.format("%.1fk", count / 1000.0)
    else -> count.toString()
}

/**
 * 帖子/Reels/标记 Tab 切换栏。
 */
@Composable
fun ProfileTabs(
    activeTab: Int,
    onTabChange: (Int) -> Unit
) {
    TabRow(
        selectedTabIndex = activeTab,
        containerColor = Color.White,
        contentColor = TextPrimary,
        indicator = { tabPositions ->
            if (activeTab < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.BottomStart)
                        .offset(x = tabPositions[activeTab].left)
                        .width(tabPositions[activeTab].width),
                    color = TextPrimary
                )
            }
        },
        divider = { HorizontalDivider(thickness = 1.dp, color = OutlineGray) }
    ) {
        Tab(
            selected = activeTab == 0,
            onClick = { onTabChange(0) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.copy),
                    contentDescription = "帖子",
                    modifier = Modifier.size(22.dp)
                )
            }
        )
        Tab(
            selected = activeTab == 1,
            onClick = { onTabChange(1) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.video),
                    contentDescription = "Reels",
                    modifier = Modifier.size(22.dp)
                )
            }
        )
        Tab(
            selected = activeTab == 2,
            onClick = { onTabChange(2) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.book),
                    contentDescription = "标记",
                    modifier = Modifier.size(22.dp)
                )
            }
        )
    }
}

/**
 * 帖子网格（3 列正方形缩略图）。
 *
 * 使用固定高度的 LazyVerticalGrid，嵌套在 LazyColumn 中需要固定高度。
 */
@Composable
fun ProfileGrid(
    posts: List<Post>,
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit = {}
) {
    if (posts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无内容", color = TextSecondary, fontSize = 14.sp)
        }
        return
    }

    // 计算网格高度：每行 3 个，每个 1:1 比例
    val rows = (posts.size + 2) / 3
    val itemSize = 130.dp // 近似值，实际由屏幕宽度决定
    val gridHeight = itemSize * rows + (1.dp * (rows - 1))

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.height(gridHeight),
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        userScrollEnabled = false
    ) {
        items(posts) { post ->
            val coverUrl = post.coverUrl ?: post.mediaList?.firstOrNull()?.url ?: ""
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onItemClick(post.postId) }
            ) {
                MediaImage(
                    url = coverUrl,
                    modifier = Modifier.fillMaxSize()
                )
                // 多图标记
                if (post.mediaCount > 1) {
                    Icon(
                        painter = painterResource(R.drawable.copy),
                        contentDescription = null,
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
}
