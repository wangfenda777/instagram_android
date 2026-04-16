package com.qiwang.ins_android.data.api

import com.qiwang.ins_android.data.model.ApiResponse
import com.qiwang.ins_android.data.model.UploadResult
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * 文件上传 API 接口。
 *
 * 按 api_doc/common.md 第16-17节定义。
 * 使用 Multipart 上传，Token 由 AuthInterceptor 自动注入。
 */
interface UploadApiService {

    /**
     * 上传图片。
     *
     * 支持 jpg/png/gif/webp 格式。
     * 返回文件访问路径（相对路径，需拼接 MEDIA_BASE_URL）。
     */
    @Multipart
    @POST("/api/upload/image")
    suspend fun uploadImage(@Part file: MultipartBody.Part): ApiResponse<UploadResult>

    /**
     * 上传视频。
     *
     * 支持 mp4/mov/avi/webm 格式，最大 50MB。
     * 返回文件访问路径。
     */
    @Multipart
    @POST("/api/upload/video")
    suspend fun uploadVideo(@Part file: MultipartBody.Part): ApiResponse<UploadResult>
}
