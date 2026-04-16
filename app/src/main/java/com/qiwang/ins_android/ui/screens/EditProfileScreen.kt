/**
 * 编辑个人资料页面。
 *
 * 展示头像（可点击）和可编辑字段列表（用户名、显示名、简介），
 * 点击字段跳转到 EditFieldScreen 进行编辑。
 */
package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qiwang.ins_android.R
import com.qiwang.ins_android.data.api.AuthApiService
import com.qiwang.ins_android.data.api.RetrofitClient
import com.qiwang.ins_android.data.local.UserPreferences
import com.qiwang.ins_android.ui.components.MediaImage
import com.qiwang.ins_android.ui.theme.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit = {},
    onNavigateToEditField: (fieldName: String, currentValue: String) -> Unit = { _, _ -> },
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val authApi = remember { RetrofitClient.create(AuthApiService::class.java) }
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var avatar by remember { mutableStateOf("") }

    // 加载用户信息
    LaunchedEffect(Unit) {
        username = userPreferences.username.firstOrNull() ?: ""
        displayName = userPreferences.displayName.firstOrNull() ?: ""
        avatar = userPreferences.avatar.firstOrNull() ?: ""
        // bio 暂时没有存储在 DataStore，后续可以从接口获取
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("编辑主页", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(painter = painterResource(R.drawable.back), contentDescription = "返回")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        InstagramDivider()

        // 头像区域
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MediaImage(
                url = avatar,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .clickable { /* TODO: 更换头像 */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "更换头像",
                color = InstagramBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { /* TODO: 更换头像 */ }
            )
        }

        InstagramDivider()

        // 字段列表
        EditProfileField(
            label = "用户名",
            value = username,
            onClick = { onNavigateToEditField("username", username) }
        )
        InstagramDivider()
        EditProfileField(
            label = "显示名称",
            value = displayName,
            onClick = { onNavigateToEditField("displayName", displayName) }
        )
        InstagramDivider()
        EditProfileField(
            label = "简介",
            value = bio,
            onClick = { onNavigateToEditField("bio", bio) }
        )
        InstagramDivider()

        Spacer(modifier = Modifier.height(32.dp))

        // 退出登录按钮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedButton(
                onClick = {
                    scope.launch {
                        try {
                            authApi.logout()
                        } catch (e: Exception) {
                            // 即使服务端登出失败，也要清除本地登录态
                            android.util.Log.e("EditProfileScreen", "服务端登出失败", e)
                        } finally {
                            userPreferences.clearAuth()
                            RetrofitClient.authInterceptor.setToken(null)
                            onLogout()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.Red
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color.Red.copy(alpha = 0.3f))
                )
            ) {
                Text("退出登录")
            }
        }
    }
}

@Composable
private fun EditProfileField(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextPrimary,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value.ifEmpty { "未设置" },
            fontSize = 14.sp,
            color = if (value.isEmpty()) TextSecondary else TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(R.drawable.back),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = TextSecondary
        )
    }
}
