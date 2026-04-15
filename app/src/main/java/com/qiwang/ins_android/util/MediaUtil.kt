package com.qiwang.ins_android.util

import com.qiwang.ins_android.data.api.RetrofitClient

/**
 * 媒体 URL 工具类。
 *
 * 后端可能返回相对路径的媒体 URL，需要在加载前判断并拼接 baseUrl。
 * 对标 Vue 项目中 appStore.baseUrl 前缀拼接逻辑。
 */
object MediaUtil {

    /**
     * 将后端返回的媒体地址转换为可直接访问的完整 URL。
     *
     * 后端有时返回相对路径，因此这里需要在非 http/https 开头时拼接 baseUrl。
     */
    fun normalizeUrl(url: String?): String {
        if (url.isNullOrBlank()) return ""
        return if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "${RetrofitClient.BASE_URL}$url"
        }
    }

    /**
     * 格式化数字（如点赞数、粉丝数）。
     *
     * 超过 10000 显示为 "1.2万"，超过 100000000 显示为 "1.2亿"。
     */
    fun formatCount(count: Int): String {
        return when {
            count >= 100_000_000 -> String.format("%.1f亿", count / 100_000_000.0)
            count >= 10_000 -> String.format("%.1f万", count / 10_000.0)
            else -> count.toString()
        }
    }

    /**
     * 格式化时间戳为相对时间（如 "2小时前"）。
     */
    fun formatRelativeTime(timestamp: Long): String {
        return try {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val weeks = days / 7

            when {
                weeks > 0 -> "${weeks}周前"
                days > 0 -> "${days}天前"
                hours > 0 -> "${hours}小时前"
                minutes > 0 -> "${minutes}分钟前"
                else -> "刚刚"
            }
        } catch (e: Exception) {
            ""
        }
    }
}
