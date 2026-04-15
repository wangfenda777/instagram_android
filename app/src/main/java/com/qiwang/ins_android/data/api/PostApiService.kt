package com.qiwang.ins_android.data.api

import com.qiwang.ins_android.data.model.ApiResponse
import com.qiwang.ins_android.data.model.FeedResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * 帖子相关 API 接口。
 *
 * 包含 Feed 流、点赞、收藏等功能。
 */
interface PostApiService {

    /**
     * 获取首页 Feed 帖子流。
     *
     * 使用游标分页（lastId），而非页码分页。
     * @param lastId 上一页最后一条帖子的 ID，首次加载传 "0"
     * @param pageSize 每页数量，默认 6
     */
    @GET("/api/post/feed")
    suspend fun getFeed(
        @Query("lastId") lastId: String = "0",
        @Query("pageSize") pageSize: Int = 6
    ): ApiResponse<FeedResponse>

    /**
     * 点赞帖子。
     */
    @POST("/api/post/like")
    suspend fun likePost(@Body body: Map<String, String>): ApiResponse<Unit>

    /**
     * 取消点赞。
     */
    @POST("/api/post/unlike")
    suspend fun unlikePost(@Body body: Map<String, String>): ApiResponse<Unit>

    /**
     * 收藏帖子。
     */
    @POST("/api/post/save")
    suspend fun savePost(@Body body: Map<String, String>): ApiResponse<Unit>

    /**
     * 取消收藏。
     */
    @POST("/api/post/unsave")
    suspend fun unsavePost(@Body body: Map<String, String>): ApiResponse<Unit>
}
