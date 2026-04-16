package com.qiwang.ins_android.data.model

/**
 * 帖子信息（包含统计数据和互动状态）。
 *
 * 后端 Feed 接口将帖子内容和统计数据合并在同一个对象中返回，
 * 不像开发文档中描述的 Post + PostStats 分离模式。
 */
data class Post(
    val postId: String = "",
    val userId: String = "",
    val username: String = "",
    val avatar: String = "",
    val isVerified: Boolean = false,
    val location: String? = null,
    val content: String = "",
    val mediaType: String = "image",
    val mediaList: List<Media>? = null,
    val mediaCount: Int = 0,
    val coverUrl: String? = null,
    val tags: List<Tag>? = null,
    val createdAt: Long = 0,
    // 统计数据（直接嵌在帖子对象中）
    val likesCount: Int = 0,
    val savedCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    // 当前用户互动状态
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val isFollowing: Boolean = false
)

/**
 * 媒体文件信息。
 */
data class Media(
    val url: String = "",
    val type: String = "image",
    val width: Int? = null,
    val height: Int? = null
)

/**
 * 标签信息。
 */
data class Tag(
    val tagId: String = "",
    val name: String = "",
    val heat: Int? = null,
    val postCount: Int? = null
)

/**
 * 上传文件响应（按 api_doc/common.md 第16-17节）。
 */
data class UploadResult(
    val url: String = "",
    val originalName: String? = null
)

/**
 * 发布帖子请求（按 api_doc/common.md 第13节）。
 */
data class CreatePostRequest(
    val content: String? = null,
    val location: String? = null,
    val mediaType: String,
    val mediaUrls: List<String>
)
data class FeedResponse(
    val list: List<Post> = emptyList(),
    val total: Int = 0,
    val page: Int = 0,
    val pageSize: Int = 0,
    val hasMore: Boolean = false,
    val lastId: Long = 0
)
