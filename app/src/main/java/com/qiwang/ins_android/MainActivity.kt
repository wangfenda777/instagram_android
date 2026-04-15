package com.qiwang.ins_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qiwang.ins_android.ui.theme.Ins_androidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Ins_androidTheme {
                GreetingScreen()
            }
        }
    }
}

@Composable
fun GreetingScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(text = "欢迎来到 Instagram 风格 App11111111134567")
        Button(
            onClick = { /* 点击事件后续添加 */ }
        ) {
            Text(text = "点击测试12222")
        }
    }
}