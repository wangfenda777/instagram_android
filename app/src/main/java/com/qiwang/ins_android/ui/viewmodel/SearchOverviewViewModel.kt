/**
 * 搜索结果页 ViewModel。
 *
 * 负责按关键词加载搜索结果，支持三个 Tab（推荐帖子、账户、标签），
 * 每个 Tab 独立分页，切换时懒加载。
 * 对应 Vue 项目的 composables/useSearchOverview.js。
 */
package com.qiwang.ins_android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qiwang.ins_android.data.api.RetrofitClient
import com.qiwang.ins_android.data.api.SearchApiService
import com.qiwang.ins_android.data.model.ExploreItem
import com.qiwang.ins_android.data.model.Tag
import com.qiwang.ins_android.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchOverviewViewModel(private val keyword: String) : ViewModel() {

    private val searchApi = RetrofitClient.create(SearchApiService::class.java)

    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab

    // 推荐帖子
    private val _postItems = MutableStateFlow<List<ExploreItem>>(emptyList())
    val postItems: StateFlow<List<ExploreItem>> = _postItems
    private var postPage = 1
    private val _postHasMore = MutableStateFlow(true)
    val postHasMore: StateFlow<Boolean> = _postHasMore
    private val _postLoading = MutableStateFlow(false)
    val postLoading: StateFlow<Boolean> = _postLoading

    // 账户
    private val _userItems = MutableStateFlow<List<User>>(emptyList())
    val userItems: StateFlow<List<User>> = _userItems
    private var userPage = 1
    private val _userHasMore = MutableStateFlow(true)
    val userHasMore: StateFlow<Boolean> = _userHasMore
    private val _userLoading = MutableStateFlow(false)
    val userLoading: StateFlow<Boolean> = _userLoading

    // 标签
    private val _tagItems = MutableStateFlow<List<Tag>>(emptyList())
    val tagItems: StateFlow<List<Tag>> = _tagItems
    private var tagPage = 1
    private val _tagHasMore = MutableStateFlow(true)
    val tagHasMore: StateFlow<Boolean> = _tagHasMore
    private val _tagLoading = MutableStateFlow(false)
    val tagLoading: StateFlow<Boolean> = _tagLoading

    private var usersLoaded = false
    private var tagsLoaded = false

    init {
        loadPosts()
    }

    /** 切换 Tab，懒加载对应数据 */
    fun switchTab(index: Int) {
        _activeTab.value = index
        when (index) {
            0 -> if (_postItems.value.isEmpty()) loadPosts()
            1 -> if (!usersLoaded) { usersLoaded = true; loadUsers() }
            2 -> if (!tagsLoaded) { tagsLoaded = true; loadTags() }
        }
    }

    /** 加载推荐帖子 */
    fun loadPosts() {
        if (_postLoading.value || !_postHasMore.value) return
        viewModelScope.launch {
            _postLoading.value = true
            try {
                val response = searchApi.searchPost(
                    keyword = keyword, type = "all", page = postPage, pageSize = 24
                )
                if (response.code == 200 && response.data != null) {
                    _postItems.value = _postItems.value + response.data.list
                    _postHasMore.value = response.data.hasMore
                    postPage++
                }
            } catch (e: Exception) {
                android.util.Log.e("SearchOverviewVM", "帖子搜索失败", e)
            } finally {
                _postLoading.value = false
            }
        }
    }

    /** 加载用户列表 */
    fun loadUsers() {
        if (_userLoading.value || !_userHasMore.value) return
        viewModelScope.launch {
            _userLoading.value = true
            try {
                val response = searchApi.searchUser(
                    keyword = keyword, page = userPage, pageSize = 20
                )
                if (response.code == 200 && response.data != null) {
                    _userItems.value = _userItems.value + response.data.list
                    _userHasMore.value = response.data.hasMore
                    userPage++
                }
            } catch (e: Exception) {
                android.util.Log.e("SearchOverviewVM", "用户搜索失败", e)
            } finally {
                _userLoading.value = false
            }
        }
    }

    /** 加载标签列表 */
    fun loadTags() {
        if (_tagLoading.value || !_tagHasMore.value) return
        viewModelScope.launch {
            _tagLoading.value = true
            try {
                val response = searchApi.searchTag(
                    keyword = keyword, page = tagPage, pageSize = 20
                )
                if (response.code == 200 && response.data != null) {
                    _tagItems.value = _tagItems.value + response.data.list
                    _tagHasMore.value = response.data.hasMore
                    tagPage++
                }
            } catch (e: Exception) {
                android.util.Log.e("SearchOverviewVM", "标签搜索失败", e)
            } finally {
                _tagLoading.value = false
            }
        }
    }

    /** 当前 Tab 加载更多 */
    fun loadMore() {
        when (_activeTab.value) {
            0 -> loadPosts()
            1 -> loadUsers()
            2 -> loadTags()
        }
    }
}
