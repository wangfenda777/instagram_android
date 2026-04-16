/**
 * 探索页 ViewModel。
 *
 * 负责加载探索页推荐瀑布流内容和实时搜索（标签 + 用户）。
 * 搜索输入带 350ms 防抖，避免频繁请求。
 * 对应 Vue 项目的 composables/useExplore.js。
 */
package com.qiwang.ins_android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qiwang.ins_android.data.api.ExploreApiService
import com.qiwang.ins_android.data.api.RetrofitClient
import com.qiwang.ins_android.data.api.SearchApiService
import com.qiwang.ins_android.data.model.ExploreItem
import com.qiwang.ins_android.data.model.Tag
import com.qiwang.ins_android.data.model.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExploreViewModel : ViewModel() {

    private val exploreApi = RetrofitClient.create(ExploreApiService::class.java)
    private val searchApi = RetrofitClient.create(SearchApiService::class.java)

    private val _exploreItems = MutableStateFlow<List<ExploreItem>>(emptyList())
    val exploreItems: StateFlow<List<ExploreItem>> = _exploreItems

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore

    private var page = 1

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode

    private val _searchKeyword = MutableStateFlow("")
    val searchKeyword: StateFlow<String> = _searchKeyword

    private val _searchTags = MutableStateFlow<List<Tag>>(emptyList())
    val searchTags: StateFlow<List<Tag>> = _searchTags

    private val _searchUsers = MutableStateFlow<List<User>>(emptyList())
    val searchUsers: StateFlow<List<User>> = _searchUsers

    private var searchJob: Job? = null

    init {
        loadExploreFeed()
    }

    /** 加载探索页推荐内容（分页） */
    fun loadExploreFeed() {
        if (_isLoading.value || !_hasMore.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = exploreApi.getExploreFeed(page = page, pageSize = 24)
                if (response.code == 200 && response.data != null) {
                    _exploreItems.value = _exploreItems.value + response.data.list
                    _hasMore.value = response.data.hasMore
                    page++
                }
            } catch (e: Exception) {
                android.util.Log.e("ExploreVM", "探索内容加载失败", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** 加载下一页 */
    fun loadMore() {
        loadExploreFeed()
    }

    /** 更新搜索关键词，350ms 防抖后执行搜索 */
    fun onSearchKeywordChange(keyword: String) {
        _searchKeyword.value = keyword
        searchJob?.cancel()
        if (keyword.isBlank()) {
            _searchTags.value = emptyList()
            _searchUsers.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(350)
            performSearch()
        }
    }

    /** 并行搜索标签和用户，一个失败不影响另一个 */
    private suspend fun performSearch() {
        val keyword = _searchKeyword.value
        if (keyword.isBlank()) return

        val tagJob = viewModelScope.launch {
            try {
                val response = searchApi.searchTag(keyword = keyword, page = 1, pageSize = 20)
                if (response.code == 200 && response.data != null) {
                    _searchTags.value = response.data.list
                }
            } catch (e: Exception) {
                android.util.Log.e("ExploreVM", "标签搜索失败", e)
            }
        }

        val userJob = viewModelScope.launch {
            try {
                val response = searchApi.searchUser(keyword = keyword, page = 1, pageSize = 20)
                if (response.code == 200 && response.data != null) {
                    _searchUsers.value = response.data.list
                }
            } catch (e: Exception) {
                android.util.Log.e("ExploreVM", "用户搜索失败", e)
            }
        }

        tagJob.join()
        userJob.join()
    }

    /** 进入搜索模式 */
    fun enterSearchMode() {
        _isSearchMode.value = true
    }

    /** 退出搜索模式，清空搜索结果 */
    fun exitSearchMode() {
        _isSearchMode.value = false
        _searchKeyword.value = ""
        _searchTags.value = emptyList()
        _searchUsers.value = emptyList()
        searchJob?.cancel()
    }
}
