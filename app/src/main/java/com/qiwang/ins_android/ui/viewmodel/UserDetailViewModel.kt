/**
 * 用户详情页 ViewModel。
 *
 * 负责加载指定用户的信息、统计数据、帖子/Reels 网格，
 * 以及处理 Tab 切换和关注/取关操作。
 */
package com.qiwang.ins_android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qiwang.ins_android.data.api.RetrofitClient
import com.qiwang.ins_android.data.api.UserApiService
import com.qiwang.ins_android.data.model.Post
import com.qiwang.ins_android.data.model.User
import com.qiwang.ins_android.data.model.UserStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserDetailViewModel(private val userId: String) : ViewModel() {

    private val userApi = RetrofitClient.create(UserApiService::class.java)

    private val _userInfo = MutableStateFlow(User())
    val userInfo: StateFlow<User> = _userInfo

    private val _stats = MutableStateFlow(UserStats())
    val stats: StateFlow<UserStats> = _stats

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _reels = MutableStateFlow<List<Post>>(emptyList())
    val reels: StateFlow<List<Post>> = _reels

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab

    private var reelsLoaded = false

    init {
        viewModelScope.launch {
            launch { loadUserInfo() }
            launch { loadUserStats() }
            launch { loadUserPosts() }
        }
    }

    private suspend fun loadUserInfo() {
        try {
            val response = userApi.getUserInfo(userId)
            if (response.code == 200 && response.data != null) {
                _userInfo.value = response.data
            }
        } catch (e: Exception) {
            android.util.Log.e("UserDetailVM", "用户信息加载失败", e)
        }
    }

    private suspend fun loadUserStats() {
        try {
            val response = userApi.getUserStats(userId)
            if (response.code == 200 && response.data != null) {
                _stats.value = response.data
            }
        } catch (e: Exception) {
            android.util.Log.e("UserDetailVM", "统计数据加载失败", e)
        }
    }

    private suspend fun loadUserPosts() {
        try {
            val response = userApi.getUserPosts(userId)
            if (response.code == 200 && response.data != null) {
                _posts.value = response.data.list
            }
        } catch (e: Exception) {
            android.util.Log.e("UserDetailVM", "帖子加载失败", e)
        }
    }

    private suspend fun loadUserReels() {
        try {
            val response = userApi.getUserReels(userId)
            if (response.code == 200 && response.data != null) {
                _reels.value = response.data.list
            }
        } catch (e: Exception) {
            android.util.Log.e("UserDetailVM", "Reels 加载失败", e)
        }
    }

    /** 切换 Tab，懒加载 Reels */
    fun switchTab(index: Int) {
        _activeTab.value = index
        if (index == 1 && !reelsLoaded) {
            reelsLoaded = true
            viewModelScope.launch { loadUserReels() }
        }
    }

    /** 关注/取关（乐观更新粉丝数） */
    fun toggleFollow() {
        val current = _userInfo.value
        val newFollowing = !current.isFollowing
        val currentStats = _stats.value

        // 乐观更新
        _userInfo.value = current.copy(isFollowing = newFollowing)
        _stats.value = currentStats.copy(
            followersCount = if (newFollowing) currentStats.followersCount + 1
            else currentStats.followersCount - 1
        )

        viewModelScope.launch {
            try {
                val body = mapOf("userId" to userId)
                if (newFollowing) userApi.followUser(body) else userApi.unfollowUser(body)
            } catch (e: Exception) {
                // 失败回滚
                _userInfo.value = current
                _stats.value = currentStats
            }
        }
    }
}
