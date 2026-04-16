package com.qiwang.ins_android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.qiwang.ins_android.R
import com.qiwang.ins_android.data.model.Post
import com.qiwang.ins_android.ui.theme.InstagramBlue
import com.qiwang.ins_android.ui.theme.TextSecondary
import com.qiwang.ins_android.util.MediaUtil

/**
 * 帖子卡片组件。
 *
 * 完整的帖子展示组件，首页 Feed 和用户详情弹窗复用。
 * 对应 Vue 项目的 components/common/PostCard.vue。
 */
@Composable
fun PostCard(
    post: Post,
    onLike: () -> Unit,
    onSave: () -> Unit,
    onFollow: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier,
    showFollowButton: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // 头部：头像 + 用户名 + 地点 + 关注按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            MediaImage(
                url = post.avatar,
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable(onClick = onUserClick)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 用户名 + 地点
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.clickable(onClick = onUserClick)
                    )
                    if (post.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.vip),
                            contentDescription = "认证",
                            modifier = Modifier.size(14.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
                if (!post.location.isNullOrBlank()) {
                    Text(
                        text = post.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            // 关注按钮
            if (showFollowButton) {
                TextButton(onClick = onFollow) {
                    Text(
                        "关注",
                        style = MaterialTheme.typography.labelLarge,
                        color = InstagramBlue
                    )
                }
            }

            // 更多菜单
            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.three_o),
                    contentDescription = "更多",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 图片/视频轮播
        ImageSwiper(mediaList = post.mediaList ?: emptyList())

        // 操作栏：点赞、评论、分享、收藏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 点赞
            IconButton(onClick = onLike) {
                Icon(
                    painter = painterResource(
                        id = if (post.isLiked) R.drawable.like_selected else R.drawable.like
                    ),
                    contentDescription = "点赞",
                    tint = if (post.isLiked) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }

            // 评论
            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.talk),
                    contentDescription = "评论",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // 分享
            IconButton(onClick = { /* TODO */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.airport),
                    contentDescription = "分享",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 收藏
            IconButton(onClick = onSave) {
                Icon(
                    painter = painterResource(
                        id = if (post.isSaved) R.drawable.collect_selected else R.drawable.collect
                    ),
                    contentDescription = "收藏",
                    tint = if (post.isSaved) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 点赞数
        if (post.likesCount > 0) {
            Text(
                text = "${MediaUtil.formatCount(post.likesCount)} 次赞",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // 内容文字（支持折叠展开 + 标签高亮）
        if (post.content.isNotBlank()) {
            val displayContent = if (!expanded && post.content.length > 60) {
                post.content.substring(0, 60) + "..."
            } else {
                post.content
            }

            val annotatedString = buildAnnotatedString {
                // 简单的标签高亮（#tag）
                val parts = displayContent.split(" ")
                parts.forEachIndexed { index, part ->
                    if (part.startsWith("#")) {
                        withStyle(style = SpanStyle(color = InstagramBlue)) {
                            append(part)
                        }
                    } else {
                        append(part)
                    }
                    if (index < parts.size - 1) append(" ")
                }
            }

            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = post.username,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = annotatedString,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!expanded && post.content.length > 60) {
                        Text(
                            text = "展开",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            modifier = Modifier.clickable { expanded = true }
                        )
                    }
                }
            }
        }

        // 发布时间
        Text(
            text = MediaUtil.formatRelativeTime(post.createdAt),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
