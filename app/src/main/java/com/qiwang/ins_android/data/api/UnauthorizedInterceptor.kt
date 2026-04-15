package com.qiwang.ins_android.data.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 401 响应处理拦截器。
 *
 * 当后端返回 401 时，说明 token 失效或未登录。
 * 当前阶段先记录日志，后续集成 DataStore 和登录流程后，
 * 在这里补充清除本地登录态和跳转登录页的逻辑。
 */
class UnauthorizedInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        if (response.code == 401) {
            Log.w("UnauthorizedInterceptor", "Received 401 unauthorized response")
            // TODO: 后续补充清除 auth 状态和跳转登录页逻辑
        }

        return response
    }
}
