package com.qiwang.ins_android.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Instagram 风格形状定义。
 *
 * 减弱 Material Design 3 默认的"圆润感"，采用小圆角设计。
 * - 按钮：4-8dp 小圆角，更显干练
 * - 输入框/搜索框：10-12dp 圆角
 * - 头像：CircleShape（在使用时单独指定）
 */
val InstagramShapes = Shapes(
    // 小圆角 — 用于按钮
    small = RoundedCornerShape(4.dp),
    // 中圆角 — 用于按钮、卡片
    medium = RoundedCornerShape(8.dp),
    // 大圆角 — 用于搜索框、输入框
    large = RoundedCornerShape(12.dp)
)
