/**
 * 发布页。
 *
 * 支持图片（最多9张）和视频两种模式，包含文案编辑、#标签补全、位置输入。
 * 对应 Vue 项目的 pages/publish/publish.vue。
 */
package com.qiwang.ins_android.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.qiwang.ins_android.R
import com.qiwang.ins_android.ui.theme.*
import com.qiwang.ins_android.ui.viewmodel.PublishViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel = remember { PublishViewModel(context) }

    val mediaMode by viewModel.mediaMode.collectAsState()
    val selectedImageUris by viewModel.selectedImageUris.collectAsState()
    val selectedVideoUri by viewModel.selectedVideoUri.collectAsState()
    val content by viewModel.content.collectAsState()
    val location by viewModel.location.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val tagSuggestions by viewModel.tagSuggestions.collectAsState()
    val publishSuccess by viewModel.publishSuccess.collectAsState()

    val canPublish = (mediaMode == "image" && selectedImageUris.isNotEmpty()) ||
            (mediaMode == "video" && selectedVideoUri != null)

    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9)
    ) { uris -> viewModel.addImages(uris) }

    // 视频选择器
    val videoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { viewModel.setVideo(it) } }

    // 发布成功后返回
    LaunchedEffect(publishSuccess) {
        if (publishSuccess) onBack()
    }

    // 监听文案中 # 触发标签搜索
    LaunchedEffect(content) {
        val hashIndex = content.lastIndexOf('#')
        if (hashIndex >= 0) {
            val afterHash = content.substring(hashIndex + 1)
            val spaceIndex = afterHash.indexOf(' ')
            val keyword = if (spaceIndex < 0) afterHash else ""
            if (keyword.isNotEmpty()) {
                viewModel.searchTags(keyword)
            } else {
                viewModel.clearTagSuggestions()
            }
        } else {
            viewModel.clearTagSuggestions()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部栏
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.close), contentDescription = "返回")
                    }
                },
                title = { Text(if (mediaMode == "video") "Reels" else "新帖子") },
                actions = {
                    InstagramPrimaryButton(
                        onClick = { viewModel.publish() },
                        enabled = canPublish && !isUploading,
                        modifier = Modifier.padding(end = 8.dp)
                    ) { Text("分享") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )

            // 模式切换 Tab
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("image" to "图片", "video" to "视频").forEach { (mode, label) ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.switchMode(mode) }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            color = if (mediaMode == mode) TextPrimary else TextSecondary,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(if (mediaMode == mode) TextPrimary else Color.Transparent)
                        )
                    }
                }
            }

            InstagramDivider()

            // 媒体选择区域
            if (mediaMode == "image") {
                ImageGrid(
                    uris = selectedImageUris,
                    onRemove = { viewModel.removeImage(it) },
                    onAdd = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            } else {
                VideoSelector(
                    videoUri = selectedVideoUri,
                    onRemove = { viewModel.removeVideo() },
                    onSelect = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                )
            }

            InstagramDivider()

            // 文案输入
            TextField(
                value = content,
                onValueChange = { viewModel.onContentChange(it) },
                placeholder = { Text("写点什么...", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                colors = instagramTextFieldColors(),
                visualTransformation = HashTagVisualTransformation()
            )

            // 标签补全面板
            if (tagSuggestions.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .background(Color.White)
                ) {
                    items(tagSuggestions) { tag ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val hashIndex = content.lastIndexOf('#')
                                    if (hashIndex >= 0) {
                                        val newContent = content.substring(0, hashIndex) + "#${tag.name} "
                                        viewModel.onContentChange(newContent)
                                    }
                                    viewModel.clearTagSuggestions()
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("#${tag.name}", color = TextPrimary)
                            Text("${tag.postCount ?: 0} 帖子", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                InstagramDivider()
            }

            // 位置输入
            TextField(
                value = location,
                onValueChange = { viewModel.onLocationChange(it) },
                placeholder = { Text("添加位置", color = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = instagramTextFieldColors()
            )
        }

        // 上传遮罩
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(Modifier.height(12.dp))
                    Text(uploadProgress, color = Color.White)
                }
            }
        }
    }
}

/**
 * 视频选择区域。
 */
@Composable
private fun VideoSelector(
    videoUri: Uri?,
    onRemove: () -> Unit,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (videoUri != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = videoUri,
                    contentDescription = "视频预览",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.close),
                        contentDescription = "删除",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(InputBackground)
                    .clickable { onSelect() },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painterResource(R.drawable.add),
                    contentDescription = "选择视频",
                    tint = TextSecondary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text("选择视频", color = TextSecondary)
            }
        }
    }
}

/**
 * #标签蓝色高亮 VisualTransformation。
 */
private class HashTagVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val original = text.text
        val builder = AnnotatedString.Builder()
        val regex = Regex("#\\S+")
        var lastEnd = 0
        for (match in regex.findAll(original)) {
            if (match.range.first > lastEnd) {
                builder.append(original.substring(lastEnd, match.range.first))
            }
            builder.pushStyle(SpanStyle(color = InstagramBlue))
            builder.append(match.value)
            builder.pop()
            lastEnd = match.range.last + 1
        }
        if (lastEnd < original.length) {
            builder.append(original.substring(lastEnd))
        }
        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }
}
@Composable
private fun ImageGrid(
    uris: List<Uri>,
    onRemove: (Int) -> Unit,
    onAdd: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // 已选图片
        itemsIndexed(uris) { index, uri ->
            Box(modifier = Modifier.aspectRatio(1f)) {
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // 右上角删除按钮
                IconButton(
                    onClick = { onRemove(index) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.close),
                        contentDescription = "删除",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        // 添加按钮
        if (uris.size < 9) {
            item {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .background(InputBackground)
                        .clickable { onAdd() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painterResource(R.drawable.add),
                        contentDescription = "添加图片",
                        tint = TextSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}