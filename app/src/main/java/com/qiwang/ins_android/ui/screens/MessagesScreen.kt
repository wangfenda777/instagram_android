package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 消息页（占位页面）。
 *
 * 暂不实现，后续迭代补充。
 * 对应 Vue 项目的 pages/messages/messages.vue。
 */
@Composable
fun MessagesScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("消息")
    }
}
