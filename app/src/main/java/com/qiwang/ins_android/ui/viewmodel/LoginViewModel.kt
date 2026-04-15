package com.qiwang.ins_android.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qiwang.ins_android.data.api.AuthApiService
import com.qiwang.ins_android.data.api.RetrofitClient
import com.qiwang.ins_android.data.local.UserPreferences
import com.qiwang.ins_android.data.model.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 登录页 ViewModel。
 *
 * 负责处理登录逻辑、Token 存储、用户信息获取。
 */
class LoginViewModel(private val context: Context) : ViewModel() {

    private val authApi = RetrofitClient.create(AuthApiService::class.java)
    private val userPreferences = UserPreferences(context)

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _account = MutableStateFlow("")
    val account: StateFlow<String> = _account

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    fun onAccountChange(newAccount: String) {
        _account.value = newAccount
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    /**
     * 执行登录。
     *
     * 流程：
     * 1. 调用 /api/auth/login 获取 token
     * 2. 存储 token 到 DataStore
     * 3. 调用 /api/user/me 获取用户信息
     * 4. 存储用户信息到 DataStore
     * 5. 更新 AuthInterceptor 的 token
     * 6. 跳转首页
     */
    fun login() {
        if (_account.value.isBlank() || _password.value.isBlank()) {
            _uiState.value = LoginUiState.Error("请输入账号和密码")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            try {
                // 1. 登录获取 token
                val loginResponse = authApi.login(
                    LoginRequest(
                        account = _account.value,
                        password = _password.value
                    )
                )

                if (loginResponse.code != 200 || loginResponse.data == null) {
                    _uiState.value = LoginUiState.Error(loginResponse.message)
                    return@launch
                }

                val tokenData = loginResponse.data

                // 2. 存储 token
                userPreferences.saveToken(tokenData.token, tokenData.refreshToken)

                // 3. 更新 AuthInterceptor
                RetrofitClient.authInterceptor.setToken(tokenData.token)

                // 4. 获取用户信息
                val userResponse = authApi.getUserMe()

                if (userResponse.code != 200 || userResponse.data == null) {
                    _uiState.value = LoginUiState.Error("获取用户信息失败")
                    return@launch
                }

                val user = userResponse.data

                // 5. 存储用户信息
                userPreferences.saveUserInfo(
                    userId = user.userId,
                    username = user.username,
                    displayName = user.displayName,
                    avatar = user.avatar
                )

                // 6. 登录成功
                _uiState.value = LoginUiState.Success

            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "登录失败")
            }
        }
    }
}

/**
 * 登录页 UI 状态。
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
