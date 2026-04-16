/**
 * 发布页 ViewModel。
 *
 * 负责图片/视频选择、文件上传、标签搜索、帖子发布等逻辑。
 * 对应 Vue 项目的 composables/usePublish.js。
 */
package com.qiwang.ins_android.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qiwang.ins_android.data.api.PostApiService
import com.qiwang.ins_android.data.api.RetrofitClient
import com.qiwang.ins_android.data.api.SearchApiService
import com.qiwang.ins_android.data.api.UploadApiService
import com.qiwang.ins_android.data.model.CreatePostRequest
import com.qiwang.ins_android.data.model.Tag
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class PublishViewModel(private val context: Context) : ViewModel() {

    private val uploadApi = RetrofitClient.create(UploadApiService::class.java)
    private val postApi = RetrofitClient.create(PostApiService::class.java)
    private val searchApi = RetrofitClient.create(SearchApiService::class.java)

    private val _mediaMode = MutableStateFlow("image")
    val mediaMode: StateFlow<String> = _mediaMode

    private val _selectedImageUris = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImageUris: StateFlow<List<Uri>> = _selectedImageUris

    private val _selectedVideoUri = MutableStateFlow<Uri?>(null)
    val selectedVideoUri: StateFlow<Uri?> = _selectedVideoUri

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content

    private val _location = MutableStateFlow("")
    val location: StateFlow<String> = _location

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _uploadProgress = MutableStateFlow("")
    val uploadProgress: StateFlow<String> = _uploadProgress

    private val _tagSuggestions = MutableStateFlow<List<Tag>>(emptyList())
    val tagSuggestions: StateFlow<List<Tag>> = _tagSuggestions

    private val _publishSuccess = MutableStateFlow(false)
    val publishSuccess: StateFlow<Boolean> = _publishSuccess

    private var searchJob: Job? = null

    /** 切换图片/视频模式，清空已选媒体 */
    fun switchMode(mode: String) {
        _mediaMode.value = mode
        _selectedImageUris.value = emptyList()
        _selectedVideoUri.value = null
    }

    /** 添加图片（总数不超过9张） */
    fun addImages(uris: List<Uri>) {
        val current = _selectedImageUris.value
        val remaining = 9 - current.size
        _selectedImageUris.value = current + uris.take(remaining)
    }

    /** 删除指定位置的图片 */
    fun removeImage(index: Int) {
        _selectedImageUris.value = _selectedImageUris.value.toMutableList().apply {
            if (index in indices) removeAt(index)
        }
    }

    /** 设置视频 */
    fun setVideo(uri: Uri) {
        _selectedVideoUri.value = uri
    }

    /** 删除视频 */
    fun removeVideo() {
        _selectedVideoUri.value = null
    }

    /** 更新文案 */
    fun onContentChange(text: String) {
        _content.value = text
    }

    /** 更新位置 */
    fun onLocationChange(text: String) {
        _location.value = text
    }

    /** 250ms 防抖搜索标签 */
    fun searchTags(keyword: String) {
        searchJob?.cancel()
        if (keyword.isBlank()) {
            _tagSuggestions.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(250)
            try {
                val response = searchApi.searchTag(keyword, page = 1, pageSize = 10)
                if (response.code == 200 && response.data != null) {
                    _tagSuggestions.value = response.data.list
                }
            } catch (_: Exception) { }
        }
    }

    /** 清空标签建议 */
    fun clearTagSuggestions() {
        _tagSuggestions.value = emptyList()
    }

    /** 发布帖子 */
    fun publish() {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val mediaUrls = mutableListOf<String>()
                val mediaType = _mediaMode.value

                if (mediaType == "image") {
                    val uris = _selectedImageUris.value
                    uris.forEachIndexed { index, uri ->
                        _uploadProgress.value = "上传中 ${index + 1}/${uris.size}..."
                        val part = uriToMultipartPart(context, uri, "file")
                        val resp = uploadApi.uploadImage(part)
                        if (resp.code == 200 && resp.data != null) {
                            mediaUrls.add(resp.data.url)
                        } else {
                            throw Exception("图片上传失败: ${resp.message}")
                        }
                    }
                } else {
                    val uri = _selectedVideoUri.value ?: throw Exception("未选择视频")
                    _uploadProgress.value = "上传视频中..."
                    val part = uriToMultipartPart(context, uri, "file", "video/*", "upload_${System.currentTimeMillis()}.mp4")
                    val resp = uploadApi.uploadVideo(part)
                    if (resp.code == 200 && resp.data != null) {
                        mediaUrls.add(resp.data.url)
                    } else {
                        throw Exception("视频上传失败: ${resp.message}")
                    }
                }

                _uploadProgress.value = "发布中..."
                val request = CreatePostRequest(
                    content = _content.value.ifBlank { null },
                    location = _location.value.ifBlank { null },
                    mediaType = mediaType,
                    mediaUrls = mediaUrls
                )
                val createResp = postApi.createPost(request)
                if (createResp.code == 200) {
                    _publishSuccess.value = true
                } else {
                    throw Exception(createResp.message)
                }
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "发布失败", Toast.LENGTH_SHORT).show()
            } finally {
                _isUploading.value = false
                _uploadProgress.value = ""
            }
        }
    }

    private fun uriToMultipartPart(
        context: Context,
        uri: Uri,
        fieldName: String,
        mimeType: String = "image/*",
        fileName: String = "upload_${System.currentTimeMillis()}.jpg"
    ): MultipartBody.Part {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri) ?: throw Exception("无法读取文件")
        val requestBody = inputStream.readBytes().toRequestBody(mimeType.toMediaType())
        return MultipartBody.Part.createFormData(fieldName, fileName, requestBody)
    }
}
