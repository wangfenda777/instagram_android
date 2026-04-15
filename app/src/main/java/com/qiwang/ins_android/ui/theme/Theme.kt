package com.qiwang.ins_android.ui.theme

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Instagram 风格主题配置。
 *
 * 基于 Material Design 3，统一覆盖颜色、字体、形状和组件默认行为，
 * 实现极简、高对比度、扁平化的 Instagram 视觉效果。
 *
 * 所有风格变更集中在此文件和同目录下的 Color.kt / Type.kt / Shape.kt 中。
 * 页面中使用 MaterialTheme.colorScheme / typography / shapes 引用主题值，
 * 不要在页面中硬编码颜色、字号或圆角。
 */

// Instagram 颜色方案 — 极致黑白化，不使用动态色彩
private val InstagramColorScheme = lightColorScheme(
    primary = InstagramBlue,
    onPrimary = Color.White,
    background = BackgroundWhite,
    onBackground = TextPrimary,
    surface = BackgroundWhite,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = OutlineGray,
    surfaceVariant = InputBackground
)

/**
 * App 入口主题。
 *
 * 所有页面都应被此主题包裹，确保风格统一。
 * 通过 CompositionLocalProvider 将 TonalElevation 设为 0dp，
 * 实现全面拍扁效果。
 */
@Composable
fun InsAndroidTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = InstagramColorScheme,
        typography = InstagramTypography,
        shapes = InstagramShapes
    ) {
        CompositionLocalProvider(
            // 全面拍扁：去掉所有组件的 Tonal Elevation
            LocalAbsoluteTonalElevation provides 0.dp
        ) {
            content()
        }
    }
}

// ============================================================
// 以下为统一封装的组件样式，页面中应使用这些封装而非手动配置样式
// ============================================================

/**
 * Instagram 风格 TextField 颜色配置。
 *
 * 统一用于所有搜索框和输入框，背景色为浅灰色，去掉默认下划线。
 * 使用方式：TextField(colors = instagramTextFieldColors(), ...)
 */
@Composable
fun instagramTextFieldColors(): TextFieldColors = TextFieldDefaults.colors(
    focusedContainerColor = InputBackground,
    unfocusedContainerColor = InputBackground,
    disabledContainerColor = InputBackground,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary
)

/**
 * Instagram Primary 按钮（蓝色填充 + 白色文字）。
 *
 * 用于关注、登录等主要操作按钮。
 * 禁用状态下透明度降低为 0.3。
 */
@Composable
fun InstagramPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = InstagramBlue,
            contentColor = Color.White,
            disabledContainerColor = InstagramBlue.copy(alpha = 0.3f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ),
        shape = InstagramShapes.medium,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        content()
    }
}

/**
 * Instagram Secondary 按钮（灰色填充 + 深色文字）。
 *
 * 用于编辑主页、分享主页等次要操作按钮。
 */
@Composable
fun InstagramSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = InputBackground,
            contentColor = TextPrimary
        ),
        shape = InstagramShapes.medium,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp
        )
    ) {
        content()
    }
}

/**
 * Instagram 风格分割线。
 *
 * 统一使用 1dp 极浅灰色线，用于列表项之间的分隔。
 */
@Composable
fun InstagramDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = OutlineGray
    )
}
