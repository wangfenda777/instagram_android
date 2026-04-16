/**
 * 粉丝/关注列表 ViewModel。
 *
 * 负责加载指定用户的粉丝和关注列表，支持分页加载和关注/取关操作。
 */
package com.qiwang.ins_android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qiwang.ins_android.data.api.RetrofitClient
import com.qiwang.ins_android.data.api.UserApiService
import com.qiwang.ins_android.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FollowListViewModel(private val userId: String) : ViewModel() {

    private val userApi = RetrofitClient.create(UserApiService::class.java)

    // 粉丝列表状态
    private val _followers = MutableStateFlow<List<User>>(emptyList())
    val followers: StateFlow<List<User>> = _followers
    private val _followersLoading = MutableStateFlow(false)
    val followersLoading: StateFlow<Boolean> = _followersLoading
    private val _followersHasMore = MutableStateFlow(true)
    val followersHasMore: StateFlow<Boolean> = _followersHasMore
    private var followersPage = 1
    private var followersLoaded = false

    // 关注列表状态
    private val _following = MutableStateFlow<List<User>>(emptyList())
    val following: StateFlow<List<User>> = _following
    private val _followingLoading = MutableStateFlow(false)
    val followingLoading: StateFlow<Boolean> = _followingLoading
    private val _followingHasMore = MutableStateFlow(true)
    val followingHasMore: StateFlow<Boolean> = _followingHasMore
    private var followingPage = 1
    private var followingLoaded = false

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab

    init {
        loadFollowers()
    }

    /** 切换 Tab，懒加载关注列表 */
    fun switchTab(index: Int) {
        _activeTab.value = index
        if (index == 1 && !followingLoaded) {
            loadFollowing()
        }
    }

    /** 加载更多（根据当前 Tab） */
    fun loadMore() {
        if (_activeTab.value == 0) loadFollowers() else loadFollowing()
    }

    private fun loadFollowers() {
        if (_followersLoading.value || !_followersHasMore.value) return
        followersLoaded = true
        viewModelScope.launch {
            _followersLoading.value = true
            try {
                val response = userApi.getUserFollowers(userId, followersPage, 20)
                if (response.code == 200 && response.data != null) {
                    _followers.value = _followers.value + response.data.list
                    _followersHasMore.value = response.data.hasMore
                    followersPage++
                }
            } catch (e: Exception) {
                android.util.Log.e("FollowListVM", "粉丝列表加载失败", e)
            } finally {
                _followersLoading.value = false
            }
        }
    }

    private fun loadFollowing() {
        if (_followingLoading.value || !_followingHasMore.value) return
        followingLoaded = true
        viewModelScope.launch {
            _followingLoading.value = true
            try {
                val response = userApi.getUserFollowing(userId, followingPage, 20)
                if (response.code == 200 && response.data != null) {
                    _following.value = _following.value + response.data.list
                    _followingHasMore.value = response.data.hasMore
                    followingPage++
                }
            } catch (e: Exception) {
                android.util.Log.e("FollowListVM", "关注列表加载失败", e)
            } finally {
                _followingLoading.value = false
            }
        }
    }

    /** 关注/取关用户（乐观更新） */
    fun toggleFollow(targetUserId: String) {
        updateUserList(_followers) { it.userId == targetUserId }
        updateUserList(_following) { it.userId == targetUserId }

        viewModelScope.launch {
            try {
                val user = (_followers.value + _following.value).find { it.userId == targetUserId }
                val body = mapOf("userId" to targetUserId)
                if (user?.isFollowing == true) userApi.followUser(body) else userApi.unfollowUser(body)
            } catch (e: Exception) {
                // 失败回滚
                updateUserList(_followers) { it.userId == targetUserId }
                updateUserList(_following) { it.userId == targetUserId }
            }
        }
    }

    private fun updateUserList(
        list: MutableStateFlow<List<User>>,
        predicate: (User) -> Boolean
    ) {
        list.value = list.value.map {
            if (predicate(it)) it.copy(isFollowing = !it.isFollowing) else it
        }
    }
}
