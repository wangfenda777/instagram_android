package com.qiwang.ins_android.data.api

import com.qiwang.ins_android.data.model.ApiResponse
import com.qiwang.ins_android.data.model.ExploreItem
import com.qiwang.ins_android.data.model.PageResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * 探索页 API 接口。
 *
 * 按 api_doc/explore.md 定义。
 */
interface ExploreApiService {

    /**
     * 获取探索页推荐内容（瀑布流）。
     *
     * 排除自己发的帖子，从候选集中随机返回卡片内容。
     * 只返回封面图和类型等必要信息。
     */
    @GET("/api/explore/feed")
    suspend fun getExploreFeed(
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 24
    ): ApiResponse<PageResponse<ExploreItem>>
}
