package com.qiwang.ins_android.data.api

import com.qiwang.ins_android.data.model.ApiResponse
import com.qiwang.ins_android.data.model.CreatePostRequest
import com.qiwang.ins_android.data.model.FeedResponse
import com.qiwang.ins_android.data.model.Post
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 帖子相关 API 接口。
 *
 * 按 api_doc/home.md 和 api_doc/common.md 定义。
 */
interface PostApiService {

    /** 获取首页 Feed 帖子流（游标分页） */
    @GET("/api/post/feed")
    suspend fun getFeed(
        @Query("lastId") lastId: String = "0",
        @Query("pageSize") pageSize: Int = 6
    ): ApiResponse<FeedResponse>

    /** 获取帖子详情（按 api_doc/explore.md 第6节） */
    @GET("/api/post/detail")
    suspend fun getPostDetail(@Query("postId") postId: String): ApiResponse<Post>

    /** 发布帖子（按 api_doc/common.md 第13节） */
    @POST("/api/post/create")
    suspend fun createPost(@Body request: CreatePostRequest): ApiResponse<Post>

    /** 点赞帖子 */
    @POST("/api/post/like")
    suspend fun likePost(@Body body: Map<String, String>): ApiResponse<Unit>

    /** 取消点赞 */
    @POST("/api/post/unlike")
    suspend fun unlikePost(@Body body: Map<String, String>): ApiResponse<Unit>

    /** 收藏帖子 */
    @POST("/api/post/save")
    suspend fun savePost(@Body body: Map<String, String>): ApiResponse<Unit>

    /** 取消收藏 */
    @POST("/api/post/unsave")
    suspend fun unsavePost(@Body body: Map<String, String>): ApiResponse<Unit>
}
