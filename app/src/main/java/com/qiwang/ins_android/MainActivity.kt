package com.qiwang.ins_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.qiwang.ins_android.ui.navigation.MainNavigation
import com.qiwang.ins_android.ui.theme.InsAndroidTheme

/**
 * App 主入口 Activity。
 *
 * 负责初始化 Compose UI 并应用 Instagram 主题。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InsAndroidTheme {
                MainNavigation()
            }
        }
    }
}