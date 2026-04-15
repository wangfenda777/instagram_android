package com.qiwang.ins_android.data.model

/**
 * 快拍信息。
 *
 * 显示在首页顶部的 Stories 横向列表中。
 * hasUnread 控制边框颜色：true 为渐变彩色，false 为灰色。
 */
data class Story(
    val storyId: String,
    val userId: String,
    val username: String,
    val avatar: String,
    val hasUnread: Boolean,
    val expiredAt: Long? = null
)

/**
 * 探索页内容项。
 *
 * 仅包含封面数据，点击后通过 postId 请求完整帖子详情。
 */
data class ExploreItem(
    val postId: String,
    val mediaType: String,
    val coverUrl: String,
    val mediaCount: Int,
    val width: Int? = null,
    val height: Int? = null
)
