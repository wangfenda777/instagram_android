package com.qiwang.ins_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.qiwang.ins_android.data.model.Media
import com.qiwang.ins_android.ui.theme.InstagramBlue
import com.qiwang.ins_android.util.MediaUtil

/**
 * 图片/视频轮播组件。
 *
 * 支持多图横向滑动，底部圆点指示器（多图时显示）。
 * 使用 SubcomposeAsyncImage 显示加载状态。
 * 对应 Vue 项目的 components/common/ImageSwiper.vue。
 */
@Composable
fun ImageSwiper(
    mediaList: List<Media>,
    modifier: Modifier = Modifier
) {
    if (mediaList.isEmpty()) return

    Box(modifier = modifier) {
        if (mediaList.size == 1) {
            // 单图直接显示
            MediaImage(
                url = mediaList[0].url,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        } else {
            // 多图 HorizontalPager
            val pagerState = rememberPagerState(pageCount = { mediaList.size })

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) { page ->
                MediaImage(
                    url = mediaList[page].url,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 底部圆点指示器
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(mediaList.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 7.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) InstagramBlue
                                else Color.White.copy(alpha = 0.5f)
                            )
                    )
                }
            }
        }
    }
}

/**
 * 通用网络图片加载组件。
 *
 * 自动拼接 baseUrl，显示加载中和加载失败状态。
 */
@Composable
fun MediaImage(
    url: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(context)
            .data(MediaUtil.normalizeUrl(url))
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        },
        error = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF0F0F0))
            )
        }
    )
}
