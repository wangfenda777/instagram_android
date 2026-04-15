package com.qiwang.ins_android.data.api

import com.qiwang.ins_android.data.model.ApiResponse
import com.qiwang.ins_android.data.model.Story
import retrofit2.http.GET

/**
 * 快拍相关 API 接口。
 */
interface StoryApiService {

    /**
     * 获取 Stories 列表。
     *
     * 显示在首页顶部的横向滚动区域。
     */
    @GET("/api/story/feed")
    suspend fun getStoryFeed(): ApiResponse<List<Story>>
}
