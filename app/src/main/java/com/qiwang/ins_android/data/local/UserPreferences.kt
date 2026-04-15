package com.qiwang.ins_android.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 本地持久化存储，负责 Token 和用户基础信息的读写。
 *
 * 使用 DataStore Preferences 替代 SharedPreferences，协程安全。
 * 对标 Vue 项目中 Pinia userStore 的持久化功能。
 */
class UserPreferences(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_prefs")

        val KEY_TOKEN = stringPreferencesKey("token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        val KEY_AVATAR = stringPreferencesKey("avatar")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[KEY_REFRESH_TOKEN] }
    val userId: Flow<String?> = context.dataStore.data.map { it[KEY_USER_ID] }
    val username: Flow<String?> = context.dataStore.data.map { it[KEY_USERNAME] }

    /** 保存登录 token */
    suspend fun saveToken(token: String, refreshToken: String) {
        context.dataStore.edit {
            it[KEY_TOKEN] = token
            it[KEY_REFRESH_TOKEN] = refreshToken
        }
    }

    /** 保存用户基础信息 */
    suspend fun saveUserInfo(userId: String, username: String, displayName: String, avatar: String) {
        context.dataStore.edit {
            it[KEY_USER_ID] = userId
            it[KEY_USERNAME] = username
            it[KEY_DISPLAY_NAME] = displayName
            it[KEY_AVATAR] = avatar
        }
    }

    /** 清除所有登录态（退出登录时调用） */
    suspend fun clearAuth() {
        context.dataStore.edit { it.clear() }
    }
}
