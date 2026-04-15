package com.qiwang.ins_android.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.qiwang.ins_android.R
import com.qiwang.ins_android.ui.theme.InstagramPrimaryButton
import com.qiwang.ins_android.ui.theme.instagramTextFieldColors
import com.qiwang.ins_android.ui.viewmodel.LoginUiState
import com.qiwang.ins_android.ui.viewmodel.LoginViewModel

/**
 * 登录页。
 *
 * 负责用户名密码登录，Token 持久化。
 * 对应 Vue 项目的 pages/login/login.vue。
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val account by viewModel.account.collectAsState()
    val password by viewModel.password.collectAsState()

    // 监听登录状态
    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> {
                onLoginSuccess()
            }
            is LoginUiState.Error -> {
                Toast.makeText(
                    context,
                    (uiState as LoginUiState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instagram Logo
            Image(
                painter = painterResource(id = R.drawable.instagram),
                contentDescription = "Instagram Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 32.dp)
            )

            // 用户名输入框
            TextField(
                value = account,
                onValueChange = viewModel::onAccountChange,
                placeholder = { Text("用户名") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = instagramTextFieldColors(),
                shape = MaterialTheme.shapes.large
            )

            // 密码输入框
            TextField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = { Text("密码") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                colors = instagramTextFieldColors(),
                shape = MaterialTheme.shapes.large
            )

            // 登录按钮
            InstagramPrimaryButton(
                onClick = viewModel::login,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = account.isNotBlank() && password.isNotBlank() && uiState !is LoginUiState.Loading
            ) {
                if (uiState is LoginUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("登录")
                }
            }

            // 忘记密码（占位）
            TextButton(onClick = { /* TODO */ }) {
                Text(
                    "忘记密码？",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
