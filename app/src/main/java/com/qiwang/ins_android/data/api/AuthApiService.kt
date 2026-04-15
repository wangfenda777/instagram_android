package com.qiwang.ins_android.data.api

import com.qiwang.ins_android.data.model.ApiResponse
import com.qiwang.ins_android.data.model.LoginRequest
import com.qiwang.ins_android.data.model.LoginResponse
import com.qiwang.ins_android.data.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * 认证相关 API 接口。
 *
 * 包含登录、登出、Token 刷新等功能。
 */
interface AuthApiService {

    /**
     * 用户登录。
     *
     * @param request 登录请求（账号 + 密码）
     * @return Token 信息
     */
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    /**
     * 获取当前用户信息。
     *
     * 登录成功后立即调用，获取用户基础信息并持久化。
     */
    @GET("/api/user/me")
    suspend fun getUserMe(): ApiResponse<User>

    /**
     * 登出。
     */
    @POST("/api/auth/logout")
    suspend fun logout(): ApiResponse<Unit>
}
