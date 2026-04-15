package com.qiwang.ins_android.data.api

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Token 注入拦截器。
 *
 * 自动在所有请求头中注入 Authorization: Bearer <token>。
 * Token 从 DataStore 中读取（后续集成 DataStore 后补充）。
 */
class AuthInterceptor : Interceptor {

    // TODO: 后续从 DataStore 读取 token
    private var token: String? = null

    fun setToken(newToken: String?) {
        token = newToken
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 如果没有 token，直接放行
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // 注入 Authorization 头
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}
