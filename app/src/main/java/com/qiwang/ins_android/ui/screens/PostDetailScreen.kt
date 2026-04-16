/**
 * 帖子详情页。
 *
 * 展示单个帖子的完整内容，从探索页点击帖子进入。
 * 支持点赞、收藏、关注等交互操作。
 */
package com.qiwang.ins_android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.qiwang.ins_android.R
import com.qiwang.ins_android.data.api.PostApiService
import com.qiwang.ins_android.data.api.RetrofitClient
import com.qiwang.ins_android.data.model.Post
import com.qiwang.ins_android.ui.components.PostCard
import com.qiwang.ins_android.ui.theme.InstagramDivider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit = {},
    onNavigateToUserDetail: (String) -> Unit = {}
) {
    val viewModel = remember(postId) { PostDetailViewModel(postId) }

    val post by viewModel.post.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "帖子",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(R.drawable.back),
                        contentDescription = "返回"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        InstagramDivider()

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (post != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                PostCard(
                    post = post!!,
                    onLike = { viewModel.toggleLike() },
                    onSave = { viewModel.toggleSave() },
                    onFollow = { /* 帖子详情页不显示关注按钮 */ },
                    onUserClick = { onNavigateToUserDetail(post!!.userId) },
                    expandContent = true,
                    showFollowButton = false
                )
            }
        }
    }
}

/**
 * 帖子详情 ViewModel。
 */
private class PostDetailViewModel(private val postId: String) : ViewModel() {

    private val postApi = RetrofitClient.create(PostApiService::class.java)

    private val _post = MutableStateFlow<Post?>(null)
    val post: StateFlow<Post?> = _post

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadPostDetail()
    }

    private fun loadPostDetail() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = postApi.getPostDetail(postId)
                if (response.code == 200 && response.data != null) {
                    _post.value = response.data
                }
            } catch (e: Exception) {
                android.util.Log.e("PostDetailViewModel", "加载帖子详情失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike() {
        val currentPost = _post.value ?: return
        val newIsLiked = !currentPost.isLiked

        // 乐观更新
        _post.value = currentPost.copy(
            isLiked = newIsLiked,
            likesCount = if (newIsLiked) currentPost.likesCount + 1 else currentPost.likesCount - 1
        )

        viewModelScope.launch {
            try {
                if (newIsLiked) {
                    postApi.likePost(mapOf("postId" to postId))
                } else {
                    postApi.unlikePost(mapOf("postId" to postId))
                }
            } catch (e: Exception) {
                // 失败时回滚
                _post.value = currentPost
                e.printStackTrace()
            }
        }
    }

    fun toggleSave() {
        val currentPost = _post.value ?: return
        val newIsSaved = !currentPost.isSaved

        // 乐观更新
        _post.value = currentPost.copy(isSaved = newIsSaved)

        viewModelScope.launch {
            try {
                if (newIsSaved) {
                    postApi.savePost(mapOf("postId" to postId))
                } else {
                    postApi.unsavePost(mapOf("postId" to postId))
                }
            } catch (e: Exception) {
                // 失败时回滚
                _post.value = currentPost
                e.printStackTrace()
            }
        }
    }
}
