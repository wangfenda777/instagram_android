package com.qiwang.ins_android.data.model

/**
 * 后端统一响应格式。
 *
 * 所有接口返回格式为 { code, message, data, timestamp }。
 * code = 200 表示成功，其他值表示业务错误。
 *
 * @param T 业务数据类型
 */
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?,
    val timestamp: Long
)

/**
 * 分页响应数据结构。
 *
 * 用于列表接口，data 字段包含 list、total、page、pageSize、hasMore。
 */
data class PageResponse<T>(
    val list: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val hasMore: Boolean
)
