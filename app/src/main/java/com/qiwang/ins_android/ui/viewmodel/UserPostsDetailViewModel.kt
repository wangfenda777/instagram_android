/**
 * 用户帖子滚动列表 ViewModel。
 *
 * 支持双向游标加载（向前/向后），用于个人主页和用户详情页点击帖子后的滚动浏览。
 * 按 api_doc/profile.md 第2节实现。
 *
 * 初次加载：后端一次返回锚点帖子的前5条 + 锚点 + 后5条（共最多11条）。
 * 后续滚动：向上触发 loadBefore()，向下触发 loadAfter()。
 */
package com.qiwang.ins_android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qiwang.ins_android.data.api.PostApiService
import com.qiwang.ins_android.data.api.RetrofitClient
import com.qiwang.ins_android.data.api.UserApiService
import com.qiwang.ins_android.data.model.Post
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserPostsDetailViewModel(
    private val userId: String,
    private val initialPostId: String
) : ViewModel() {

    private val userApi = RetrofitClient.create(UserApiService::class.java)
    private val postApi = RetrofitClient.create(PostApiService::class.java)

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasMoreBefore = MutableStateFlow(true)
    val hasMoreBefore: StateFlow<Boolean> = _hasMoreBefore

    private val _hasMoreAfter = MutableStateFlow(true)
    val hasMoreAfter: StateFlow<Boolean> = _hasMoreAfter

    /** 初次加载完成后，锚点帖子在列表中的索引，用于 UI 层初始化滚动位置 */
    private val _anchorIndex = MutableStateFlow(0)
    val anchorIndex: StateFlow<Int> = _anchorIndex

    init {
        loadInitialPosts()
    }

    /**
     * 初次加载：后端返回锚点帖子的前后各5条（共最多11条）。
     * 加载完成后计算锚点帖子在列表中的索引。
     */
    private fun loadInitialPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = userApi.getUserPostsDetail(
                    userId = userId,
                    postId = initialPostId,
                    direction = null
                )
                if (response.code == 200 && response.data != null) {
                    _posts.value = response.data.list
                    _hasMoreAfter.value = response.data.hasMore

                    // 找到锚点帖子的索引
                    val index = response.data.list.indexOfFirst { it.postId == initialPostId }
                    _anchorIndex.value = if (index >= 0) index else 0
                }
            } catch (e: Exception) {
                android.util.Log.e("UserPostsDetailVM", "初次加载失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** 向前加载：返回比当前第一条更新的5条。 */
    fun loadBefore() {
        if (_isLoading.value || !_hasMoreBefore.value || _posts.value.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val firstPostId = _posts.value.first().postId
                val response = userApi.getUserPostsDetail(
                    userId = userId,
                    postId = firstPostId,
                    direction = "before"
                )
                if (response.code == 200 && response.data != null) {
                    if (response.data.list.isNotEmpty()) {
                        _posts.value = response.data.list + _posts.value
                    }
                    _hasMoreBefore.value = response.data.hasMore
                }
            } catch (e: Exception) {
                android.util.Log.e("UserPostsDetailVM", "向前加载失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** 向后加载：返回比当前最后一条更老的5条。 */
    fun loadAfter() {
        if (_isLoading.value || !_hasMoreAfter.value || _posts.value.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val lastPostId = _posts.value.last().postId
                val response = userApi.getUserPostsDetail(
                    userId = userId,
                    postId = lastPostId,
                    direction = "after"
                )
                if (response.code == 200 && response.data != null) {
                    if (response.data.list.isNotEmpty()) {
                        _posts.value = _posts.value + response.data.list
                    }
                    _hasMoreAfter.value = response.data.hasMore
                }
            } catch (e: Exception) {
                android.util.Log.e("UserPostsDetailVM", "向后加载失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** 点赞/取消点赞（乐观更新）。 */
    fun toggleLike(postId: String) {
        val currentPost = _posts.value.find { it.postId == postId } ?: return
        val newIsLiked = !currentPost.isLiked

        _posts.value = _posts.value.map { post ->
            if (post.postId == postId) {
                post.copy(
                    isLiked = newIsLiked,
                    likesCount = if (newIsLiked) post.likesCount + 1 else post.likesCount - 1
                )
            } else post
        }

        viewModelScope.launch {
            try {
                if (newIsLiked) postApi.likePost(mapOf("postId" to postId))
                else postApi.unlikePost(mapOf("postId" to postId))
            } catch (e: Exception) {
                _posts.value = _posts.value.map { if (it.postId == postId) currentPost else it }
            }
        }
    }

    /** 收藏/取消收藏（乐观更新）。 */
    fun toggleSave(postId: String) {
        val currentPost = _posts.value.find { it.postId == postId } ?: return
        val newIsSaved = !currentPost.isSaved

        _posts.value = _posts.value.map { post ->
            if (post.postId == postId) post.copy(isSaved = newIsSaved) else post
        }

        viewModelScope.launch {
            try {
                if (newIsSaved) postApi.savePost(mapOf("postId" to postId))
                else postApi.unsavePost(mapOf("postId" to postId))
            } catch (e: Exception) {
                _posts.value = _posts.value.map { if (it.postId == postId) currentPost else it }
            }
        }
    }
}
