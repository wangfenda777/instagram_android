package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 首页 Feed 页面。
 *
 * 负责展示 Stories 横向列表和帖子流，并处理点赞、收藏、关注等交互。
 * 对应 Vue 项目的 pages/index/index.vue。
 */
@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("首页 Feed（待实现）")
    }
}
