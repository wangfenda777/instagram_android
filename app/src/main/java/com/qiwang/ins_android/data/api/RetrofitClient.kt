package com.qiwang.ins_android.data.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 网络客户端配置。
 *
 * 提供统一的 Retrofit 实例，包含：
 * - Gson JSON 解析（容错性好，兼容各种后端返回格式）
 * - OkHttp 客户端（Token 注入 + 401 处理 + 日志）
 * - 10 秒超时
 *
 * 使用方式：
 * ```
 * val authApi = RetrofitClient.create(AuthApiService::class.java)
 * ```
 */
object RetrofitClient {

    const val BASE_URL = "http://112.124.47.169"

    // AuthInterceptor 单例，后续 DataStore 集成后通过 setToken 注入 token
    val authInterceptor = AuthInterceptor()

    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(UnauthorizedInterceptor())
        .addInterceptor(loggingInterceptor)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    /**
     * 创建指定 API Service 的实例。
     *
     * @param T Retrofit Service 接口类型
     * @param serviceClass API Service 的 Class 对象
     * @return API Service 实例
     */
    fun <T> create(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
