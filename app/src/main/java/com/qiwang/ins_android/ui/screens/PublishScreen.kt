package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 发布页。
 *
 * 负责图片/视频选择上传和文案编辑。
 * 对应 Vue 项目的 pages/publish/publish.vue。
 */
@Composable
fun PublishScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("发布页（待实现）")
    }
}
