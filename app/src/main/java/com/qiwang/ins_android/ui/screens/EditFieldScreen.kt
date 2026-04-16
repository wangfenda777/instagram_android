/**
 * 编辑单个字段页面。
 *
 * 接收字段名和当前值，提供 TextField 编辑，
 * 点击完成按钮调用 updateProfile 保存。
 */
package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qiwang.ins_android.R
import com.qiwang.ins_android.data.api.RetrofitClient
import com.qiwang.ins_android.data.api.UserApiService
import com.qiwang.ins_android.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFieldScreen(
    fieldName: String,
    currentValue: String,
    onBack: () -> Unit = {}
) {
    val userApi = remember { RetrofitClient.create(UserApiService::class.java) }
    val scope = rememberCoroutineScope()
    var value by remember { mutableStateOf(currentValue) }
    var isSaving by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    val displayLabel = when (fieldName) {
        "username" -> "用户名"
        "displayName" -> "显示名称"
        "bio" -> "简介"
        else -> fieldName
    }

    // 自动聚焦（延迟执行避免崩溃）
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            android.util.Log.e("EditFieldScreen", "聚焦失败", e)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(displayLabel, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(painter = painterResource(R.drawable.back), contentDescription = "返回")
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        isSaving = true
                        scope.launch {
                            try {
                                val body = mapOf(fieldName to value)
                                val response = userApi.updateProfile(body)
                                if (response.code == 200) {
                                    onBack()
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("EditFieldScreen", "保存失败", e)
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    enabled = !isSaving && value != currentValue
                ) {
                    Text(
                        "完成",
                        color = if (!isSaving && value != currentValue) InstagramBlue
                        else InstagramBlue.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        InstagramDivider()

        TextField(
            value = value,
            onValueChange = { value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .focusRequester(focusRequester),
            colors = instagramTextFieldColors(),
            label = { Text(displayLabel) },
            singleLine = fieldName != "bio",
            maxLines = if (fieldName == "bio") 4 else 1
        )
    }
}
