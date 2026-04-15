package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 探索页。
 *
 * 负责展示瀑布流推荐帖子和实时搜索功能。
 * 对应 Vue 项目的 pages/explore/explore.vue。
 */
@Composable
fun ExploreScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("探索页（待实现）")
    }
}
