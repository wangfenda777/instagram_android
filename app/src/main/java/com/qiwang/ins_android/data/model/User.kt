package com.qiwang.ins_android.data.model

/**
 * 用户基础信息。
 *
 * 对应后端 User 实体，所有 ID 为 String 类型（后端 BIGINT 序列化为 String）。
 */
data class User(
    val userId: String,
    val username: String,
    val displayName: String,
    val avatar: String,
    val bio: String? = null,
    val isVerified: Boolean = false,
    val isPrivate: Boolean = false
)

/**
 * 用户统计数据。
 *
 * 与 User 分离，减少单接口数据量。
 */
data class UserStats(
    val userId: String,
    val postsCount: Int,
    val followersCount: Int,
    val followingCount: Int
)

/**
 * 登录响应。
 */
data class LoginResponse(
    val token: String,
    val refreshToken: String,
    val expiresIn: Int
)

/**
 * 登录请求。
 */
data class LoginRequest(
    val account: String,
    val password: String
)
