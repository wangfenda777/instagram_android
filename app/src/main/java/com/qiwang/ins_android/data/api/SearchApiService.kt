package com.qiwang.ins_android.data.api

import com.qiwang.ins_android.data.model.ApiResponse
import com.qiwang.ins_android.data.model.ExploreItem
import com.qiwang.ins_android.data.model.PageResponse
import com.qiwang.ins_android.data.model.Tag
import com.qiwang.ins_android.data.model.User
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 搜索相关 API 接口。
 *
 * 按 api_doc/explore.md 第2-4节定义。
 */
interface SearchApiService {

    /**
     * 搜索用户。
     *
     * 返回的 User 额外包含 followersCount 字段。
     */
    @GET("/api/search/user")
    suspend fun searchUser(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<PageResponse<User>>

    /**
     * 搜索标签。
     *
     * 支持输入 "travel" 或 "#travel"。
     */
    @GET("/api/search/tag")
    suspend fun searchTag(
        @Query("keyword") keyword: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<PageResponse<Tag>>

    /**
     * 搜索帖子。
     *
     * type 可选值：all / tag / content。
     * 当 type=tag 时，支持 "travel" 和 "#travel" 两种写法。
     */
    @GET("/api/search/post")
    suspend fun searchPost(
        @Query("keyword") keyword: String,
        @Query("type") type: String = "all",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 24
    ): ApiResponse<PageResponse<ExploreItem>>
}
