package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 个人主页。
 *
 * 负责展示个人信息、帖子/Reels/标记网格、推荐用户等。
 * 对应 Vue 项目的 pages/profile/profile.vue。
 */
@Composable
fun ProfileScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("个人主页（待实现）")
    }
}
