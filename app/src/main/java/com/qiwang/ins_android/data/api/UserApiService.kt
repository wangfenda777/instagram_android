package com.qiwang.ins_android.data.api

import com.qiwang.ins_android.data.model.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 用户关系相关 API 接口。
 */
interface UserApiService {

    /**
     * 关注用户。
     */
    @POST("/api/user/follow")
    suspend fun followUser(@Body body: Map<String, String>): ApiResponse<Unit>

    /**
     * 取消关注。
     */
    @POST("/api/user/unfollow")
    suspend fun unfollowUser(@Body body: Map<String, String>): ApiResponse<Unit>
}
