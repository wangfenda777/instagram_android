/**
 * 个人主页 ViewModel。
 *
 * 负责加载当前用户信息、统计数据、帖子网格、Reels、推荐用户，
 * 以及处理 Tab 切换、关注/取关推荐用户等交互。
 * 对应 Vue 项目的 composables/useProfile.js。
 */
package com.qiwang.ins_android.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qiwang.ins_android.data.api.RetrofitClient
import com.qiwang.ins_android.data.api.UserApiService
import com.qiwang.ins_android.data.local.UserPreferences
import com.qiwang.ins_android.data.model.Post
import com.qiwang.ins_android.data.model.User
import com.qiwang.ins_android.data.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ProfileViewModel(private val context: Context) : ViewModel() {

    private val userApi = RetrofitClient.create(UserApiService::class.java)
    private val userPreferences = UserPreferences(context)

    private val _userInfo = MutableStateFlow(User())
    val userInfo: StateFlow<User> = _userInfo

    private val _stats = MutableStateFlow(UserStats())
    val stats: StateFlow<UserStats> = _stats

    private val _discoverUsers = MutableStateFlow<List<User>>(emptyList())
    val discoverUsers: StateFlow<List<User>> = _discoverUsers

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _reels = MutableStateFlow<List<Post>>(emptyList())
    val reels: StateFlow<List<Post>> = _reels

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab

    private var reelsLoaded = false

    init {
        viewModelScope.launch {
            val userId = userPreferences.userId.firstOrNull() ?: return@launch
            val username = userPreferences.username.firstOrNull() ?: ""
            val displayName = userPreferences.displayName.firstOrNull() ?: ""
            val avatar = userPreferences.avatar.firstOrNull() ?: ""

            // 先用本地缓存填充基础信息
            _userInfo.value = User(
                userId = userId,
                username = username,
                displayName = displayName,
                avatar = avatar
            )

            // 并行加载远程数据
            launch { loadUserStats(userId) }
            launch { loadDiscoverUsers() }
            launch { loadUserPosts(userId) }
        }
    }

    private suspend fun loadUserStats(userId: String) {
        try {
            val response = userApi.getUserStats(userId)
            if (response.code == 200 && response.data != null) {
                _stats.value = response.data
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileViewModel", "统计数据加载失败", e)
        }
    }

    private suspend fun loadDiscoverUsers() {
        try {
            val response = userApi.getDiscoverUsers(limit = 5)
            if (response.code == 200 && response.data != null) {
                _discoverUsers.value = response.data
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileViewModel", "推荐用户加载失败", e)
        }
    }

    private suspend fun loadUserPosts(userId: String) {
        try {
            val response = userApi.getUserPosts(userId)
            if (response.code == 200 && response.data != null) {
                _posts.value = response.data.list
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileViewModel", "帖子加载失败", e)
        }
    }

    private suspend fun loadUserReels(userId: String) {
        try {
            val response = userApi.getUserReels(userId)
            if (response.code == 200 && response.data != null) {
                _reels.value = response.data.list
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileViewModel", "Reels 加载失败", e)
        }
    }

    /** 切换 Tab，懒加载 Reels */
    fun switchTab(index: Int) {
        _activeTab.value = index
        if (index == 1 && !reelsLoaded) {
            reelsLoaded = true
            viewModelScope.launch {
                val userId = _userInfo.value.userId
                if (userId.isNotEmpty()) loadUserReels(userId)
            }
        }
    }

    /** 关注/取关推荐用户（乐观更新） */
    fun toggleFollowDiscover(userId: String) {
        val current = _discoverUsers.value.find { it.userId == userId } ?: return
        val newFollowing = !current.isFollowing

        _discoverUsers.value = _discoverUsers.value.map {
            if (it.userId == userId) it.copy(isFollowing = newFollowing) else it
        }

        viewModelScope.launch {
            try {
                val body = mapOf("userId" to userId)
                if (newFollowing) userApi.followUser(body) else userApi.unfollowUser(body)
            } catch (e: Exception) {
                // 失败回滚
                _discoverUsers.value = _discoverUsers.value.map {
                    if (it.userId == userId) current else it
                }
            }
        }
    }

    /** 从推荐列表移除用户 */
    fun removeDiscoverUser(userId: String) {
        _discoverUsers.value = _discoverUsers.value.filter { it.userId != userId }
    }
}
