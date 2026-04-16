package com.qiwang.ins_android.data.api

import com.qiwang.ins_android.data.model.ApiResponse
import com.qiwang.ins_android.data.model.PageResponse
import com.qiwang.ins_android.data.model.Post
import com.qiwang.ins_android.data.model.User
import com.qiwang.ins_android.data.model.UserStats
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 用户相关 API 接口。
 *
 * 包含用户信息、统计、关注关系、帖子列表、推荐用户等功能。
 */
interface UserApiService {

    /** 获取当前登录用户信息 */
    @GET("/api/user/me")
    suspend fun getUserMe(): ApiResponse<User>

    /** 获取指定用户信息 */
    @GET("/api/user/info")
    suspend fun getUserInfo(@Query("userId") userId: String): ApiResponse<User>

    /** 获取用户统计（帖子数、粉丝数、关注数） */
    @GET("/api/user/stats")
    suspend fun getUserStats(@Query("userId") userId: String): ApiResponse<UserStats>

    /** 关注用户 */
    @POST("/api/user/follow")
    suspend fun followUser(@Body body: Map<String, String>): ApiResponse<Unit>

    /** 取消关注 */
    @POST("/api/user/unfollow")
    suspend fun unfollowUser(@Body body: Map<String, String>): ApiResponse<Unit>

    /** 获取用户帖子列表（网格用） */
    @GET("/api/user/posts")
    suspend fun getUserPosts(
        @Query("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 18,
        @Query("mediaType") mediaType: String? = null
    ): ApiResponse<PageResponse<Post>>

    /** 获取用户视频列表 */
    @GET("/api/user/reels")
    suspend fun getUserReels(
        @Query("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 18
    ): ApiResponse<PageResponse<Post>>

    /** 获取推荐用户 */
    @GET("/api/user/discover")
    suspend fun getDiscoverUsers(@Query("limit") limit: Int = 5): ApiResponse<List<User>>

    /** 获取粉丝列表 */
    @GET("/api/user/followers")
    suspend fun getUserFollowers(
        @Query("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<PageResponse<User>>

    /** 获取关注列表 */
    @GET("/api/user/following")
    suspend fun getUserFollowing(
        @Query("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<PageResponse<User>>

    /** 更新用户资料 */
    @POST("/api/user/profile/update")
    suspend fun updateProfile(@Body body: Map<String, String>): ApiResponse<Unit>
}
