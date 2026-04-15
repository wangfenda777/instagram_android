package com.qiwang.ins_android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qiwang.ins_android.data.api.PostApiService
import com.qiwang.ins_android.data.api.RetrofitClient
import com.qiwang.ins_android.data.api.StoryApiService
import com.qiwang.ins_android.data.api.UserApiService
import com.qiwang.ins_android.data.model.Post
import com.qiwang.ins_android.data.model.Story
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 首页 Feed ViewModel。
 *
 * 负责加载 Stories、帖子流、处理点赞收藏关注等交互。
 * 对应 Vue 项目的 composables/useHome.js。
 */
class HomeViewModel : ViewModel() {

    private val storyApi = RetrofitClient.create(StoryApiService::class.java)
    private val postApi = RetrofitClient.create(PostApiService::class.java)
    private val userApi = RetrofitClient.create(UserApiService::class.java)

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore

    private var lastId = "0"

    init {
        loadInitialData()
    }

    /**
     * 加载初始数据（Stories + 第一页帖子）。
     */
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                // 加载 Stories
                val storiesResponse = storyApi.getStoryFeed()
                if (storiesResponse.code == 200 && storiesResponse.data != null) {
                    _stories.value = storiesResponse.data
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Stories 加载失败", e)
            }

            // 加载第一页帖子
            loadMorePosts()
        }
    }

    /**
     * 加载更多帖子（游标分页）。
     */
    fun loadMorePosts() {
        if (_isLoading.value || !_hasMore.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                android.util.Log.d("HomeViewModel", "请求 Feed: lastId=$lastId")
                val response = postApi.getFeed(lastId = lastId, pageSize = 6)
                android.util.Log.d("HomeViewModel", "Feed 响应: code=${response.code}, data=${response.data}")

                if (response.code == 200 && response.data != null) {
                    val feedData = response.data

                    // 追加帖子
                    _posts.value = _posts.value + feedData.list
                    android.util.Log.d("HomeViewModel", "帖子数量: ${_posts.value.size}")

                    // 更新分页状态
                    lastId = feedData.lastId.toString()
                    _hasMore.value = feedData.hasMore
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "Feed 加载失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 点赞/取消点赞（乐观更新）。
     */
    fun toggleLike(postId: String) {
        val currentPost = _posts.value.find { it.postId == postId } ?: return
        val newIsLiked = !currentPost.isLiked

        // 乐观更新 UI
        _posts.value = _posts.value.map { post ->
            if (post.postId == postId) {
                post.copy(
                    isLiked = newIsLiked,
                    likesCount = if (newIsLiked) post.likesCount + 1 else post.likesCount - 1
                )
            } else {
                post
            }
        }

        // 发送请求
        viewModelScope.launch {
            try {
                if (newIsLiked) {
                    postApi.likePost(mapOf("postId" to postId))
                } else {
                    postApi.unlikePost(mapOf("postId" to postId))
                }
            } catch (e: Exception) {
                // 失败时回滚
                _posts.value = _posts.value.map { post ->
                    if (post.postId == postId) {
                        currentPost
                    } else {
                        post
                    }
                }
                e.printStackTrace()
            }
        }
    }

    /**
     * 收藏/取消收藏（乐观更新）。
     */
    fun toggleSave(postId: String) {
        val currentPost = _posts.value.find { it.postId == postId } ?: return
        val newIsSaved = !currentPost.isSaved

        // 乐观更新 UI
        _posts.value = _posts.value.map { post ->
            if (post.postId == postId) {
                post.copy(isSaved = newIsSaved)
            } else {
                post
            }
        }

        // 发送请求
        viewModelScope.launch {
            try {
                if (newIsSaved) {
                    postApi.savePost(mapOf("postId" to postId))
                } else {
                    postApi.unsavePost(mapOf("postId" to postId))
                }
            } catch (e: Exception) {
                // 失败时回滚
                _posts.value = _posts.value.map { post ->
                    if (post.postId == postId) {
                        currentPost
                    } else {
                        post
                    }
                }
                e.printStackTrace()
            }
        }
    }

    /**
     * 关注用户。
     */
    fun followUser(userId: String) {
        viewModelScope.launch {
            try {
                userApi.followUser(mapOf("userId" to userId))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
