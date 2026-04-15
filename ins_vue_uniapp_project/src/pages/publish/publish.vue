<template>
  <view class="publish-page">
    <!-- 顶部导航栏 -->
    <view class="navbar">
      <view class="navbar-left" @click="handleBack">
        <text class="icon">←</text>
      </view>
      <view class="navbar-title">{{ publishMode === 'image' ? '新帖子' : 'Reels' }}</view>
      <view class="navbar-right" :class="{ active: canPublish }" @click="handlePublish">
        <text>分享</text>
      </view>
    </view>

    <!-- 模式切换 tab -->
    <view class="mode-tabs">
      <view
        class="mode-tab"
        :class="{ active: publishMode === 'image' }"
        @click="switchMode('image')"
      >
        <text>图片</text>
      </view>
      <view
        class="mode-tab"
        :class="{ active: publishMode === 'video' }"
        @click="switchMode('video')"
      >
        <text>视频</text>
      </view>
    </view>

    <!-- 图片选择区 -->
    <view v-if="publishMode === 'image'" class="image-section">
      <view class="image-grid">
        <view
          v-for="(img, index) in imageList"
          :key="index"
          class="image-item"
        >
          <image :src="img.preview" mode="aspectFill" class="image" />
          <view class="delete-btn" @click="removeImage(index)">
            <text>×</text>
          </view>
        </view>
        <view v-if="imageList.length < 9" class="add-image" @click="chooseImage">
          <text class="add-icon">+</text>
        </view>
      </view>
    </view>

    <!-- 视频选择区 -->
    <view v-else class="image-section">
      <view v-if="videoFile" class="video-preview-wrap">
        <video
          class="video-preview"
          :src="videoFile.preview"
          controls
          :show-fullscreen-btn="false"
        />
        <view class="delete-btn video-delete" @click="removeVideo">
          <text>×</text>
        </view>
      </view>
      <view v-else class="add-image add-video" @click="chooseVideo">
        <text class="add-icon">+</text>
        <text class="add-video-hint">选择视频（mp4/mov，最大50MB）</text>
      </view>
    </view>

    <!-- 文案输入 -->
    <view class="input-section">
      <view class="caption-editor" :style="{ minHeight: `${textareaHeight}rpx` }">
        <view class="caption-highlight" :style="{ minHeight: `${textareaHeight}rpx` }">
          <text
            v-for="(segment, index) in formattedSegments"
            :key="`${segment.type}-${index}`"
            :class="segment.type === 'tag' ? 'segment-tag' : 'segment-text'"
          >{{ segment.type === 'text' && !segment.text ? ' ' : segment.text }}</text>
        </view>
        <textarea
          class="textarea"
          :value="content"
          :cursor="caretPosition"
          placeholder="写一段文案..."
          maxlength="2000"
          :auto-height="true"
          @input="handleContentInput"
          @blur="handleContentBlur"
        />
      </view>

      <view v-if="shouldShowSuggestions" class="tag-suggestion-panel">
        <view v-if="isSearchingTags" class="tag-suggestion-status">
          <text>标签搜索中...</text>
        </view>
        <view v-else-if="tagSearchError" class="tag-suggestion-status error-text">
          <text>{{ tagSearchError }}</text>
        </view>
        <view v-else>
          <view
            v-for="tag in tagSuggestions"
            :key="tag.id || tag.name"
            class="tag-suggestion-item"
            @mousedown.prevent="applyTagSuggestion(tag)"
            @click="applyTagSuggestion(tag)"
          >
            <view class="tag-suggestion-info">
              <text class="tag-suggestion-name">{{ tag.name }}</text>
              <text v-if="tag.postCount" class="tag-suggestion-count">{{ tag.postCount }} 条内容</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 位置输入 -->
    <view class="location-section">
      <input
        class="location-input"
        v-model="location"
        placeholder="添加位置"
      />
    </view>

    <!-- 加载提示 -->
    <view v-if="uploading" class="loading-mask">
      <view class="loading-box">
        <text>上传中... {{ uploadProgress }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { uploadImage, uploadVideo } from '@/api/upload.js'
import { createPost } from '@/api/post.js'
import { usePublishTagAutocomplete } from '@/composables/usePublishTagAutocomplete.js'

const publishMode = ref('image') // 'image' | 'video'
const imageList = ref([])
const videoFile = ref(null)
const content = ref('')
const location = ref('')
const uploading = ref(false)
const uploadProgress = ref('')

const {
  textareaHeight,
  caretPosition,
  formattedSegments,
  tagSuggestions,
  isSearchingTags,
  tagSearchError,
  shouldShowSuggestions,
  handleContentInput,
  applyTagSuggestion,
  hideSuggestions
} = usePublishTagAutocomplete(content)

const canPublish = computed(() => {
  if (uploading.value) return false
  if (publishMode.value === 'image') return imageList.value.length > 0
  return videoFile.value !== null
})

const switchMode = (mode) => {
  if (publishMode.value === mode) return
  publishMode.value = mode
  imageList.value = []
  videoFile.value = null
}

const handleContentBlur = () => {
  setTimeout(() => {
    hideSuggestions()
  }, 200)
}

// 选择图片
const chooseImage = () => {
  uni.chooseImage({
    count: 9 - imageList.value.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFiles = res.tempFiles || res.tempFilePaths.map(path => ({ path }))
      tempFiles.forEach(file => {
        imageList.value.push({
          preview: file.path || file.tempFilePath,
          file: file
        })
      })
    }
  })
}

const removeImage = (index) => {
  imageList.value.splice(index, 1)
}

// 选择视频，选完后自动截取首帧作为封面
const chooseVideo = () => {
  uni.chooseVideo({
    sourceType: ['album', 'camera'],
    maxDuration: 60,
    success: (res) => {
      videoFile.value = {
        preview: res.tempFilePath,
        path: res.tempFilePath,
        size: res.size,
        coverBlob: null
      }
      captureVideoCover(res.tempFilePath)
    }
  })
}

const removeVideo = () => {
  videoFile.value = null
}

// 截取视频首帧作为封面
const captureVideoCover = (videoPath) => {
  const video = document.createElement('video')
  video.src = videoPath
  video.crossOrigin = 'anonymous'
  video.currentTime = 0.1 // 取第0.1秒的帧

  video.addEventListener('loadeddata', () => {
    const canvas = document.createElement('canvas')
    canvas.width = video.videoWidth
    canvas.height = video.videoHeight
    const ctx = canvas.getContext('2d')
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)

    canvas.toBlob((blob) => {
      if (blob && videoFile.value) {
        videoFile.value.coverBlob = blob
      }
    }, 'image/jpeg', 0.8)
  })

  video.load()
}

// 返回
const handleBack = () => {
  const hasContent = imageList.value.length > 0 || videoFile.value || content.value
  if (hasContent) {
    uni.showModal({
      title: '提示',
      content: '确定要放弃发布吗？',
      success: (res) => {
        if (res.confirm) {
          uni.switchTab({ url: '/pages/index/index' })
        }
      }
    })
  } else {
    uni.switchTab({ url: '/pages/index/index' })
  }
}

// 发布
const handlePublish = async () => {
  if (!canPublish.value) return

  uploading.value = true
  uploadProgress.value = '0%'

  try {
    if (publishMode.value === 'image') {
      const mediaUrls = []
      for (let i = 0; i < imageList.value.length; i++) {
        uploadProgress.value = `${i + 1}/${imageList.value.length}`
        const file = await getFileFromPath(imageList.value[i].preview, 'image')
        const result = await uploadImage(file)
        mediaUrls.push(result.url)
      }

      uploadProgress.value = '发布中...'
      await createPost({
        content: content.value,
        location: location.value,
        mediaType: 'image',
        mediaUrls
      })
    } else {
      uploadProgress.value = '上传视频...'
      const file = await getFileFromPath(videoFile.value.preview, 'video')
      const result = await uploadVideo(file)

      let coverUrl = ''
      if (videoFile.value.coverBlob) {
        uploadProgress.value = '上传封面...'
        const coverFile = new File([videoFile.value.coverBlob], 'cover.jpg', { type: 'image/jpeg' })
        const coverResult = await uploadImage(coverFile)
        coverUrl = coverResult.url
      }

      uploadProgress.value = '发布中...'
      await createPost({
        content: content.value,
        location: location.value,
        mediaType: 'video',
        mediaUrls: [result.url],
        ...(coverUrl ? { coverUrl } : {})
      })
    }

    uni.showToast({ title: '发布成功', icon: 'success' })
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 1500)
  } catch (e) {
    console.error('发布失败', e)
    uni.showToast({ title: '发布失败，请重试', icon: 'none' })
  } finally {
    uploading.value = false
  }
}

const getFileFromPath = (path, mediaType = 'image') => {
  return new Promise((resolve, reject) => {
    if (path.startsWith('blob:')) {
      fetch(path)
        .then(res => res.blob())
        .then(blob => {
          const isVideo = mediaType === 'video'
          const ext = isVideo ? 'mp4' : 'jpg'
          const type = isVideo ? 'video/mp4' : 'image/jpeg'
          const file = new File([blob], `file.${ext}`, { type })
          resolve(file)
        })
        .catch(reject)
    } else {
      reject(new Error('Unsupported path format'))
    }
  })
}
</script>

<style scoped>
.publish-page {
  min-height: 100vh;
  background: #fff;
  padding-top: 132rpx;
}

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 88rpx;
  background: #fff;
  border-bottom: 2rpx solid #dbdbdb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32rpx;
  z-index: 100;
}

.navbar-left {
  width: 80rpx;
}

.navbar-left .icon {
  font-size: 48rpx;
  color: #000;
}

.navbar-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #000;
}

.navbar-right {
  width: 80rpx;
  text-align: right;
  color: #0095f6;
  font-size: 28rpx;
  font-weight: 600;
  opacity: 0.3;
}

.navbar-right.active {
  opacity: 1;
}

.mode-tabs {
  position: fixed;
  top: 88rpx;
  left: 0;
  right: 0;
  height: 88rpx;
  background: #fff;
  border-bottom: 2rpx solid #dbdbdb;
  display: flex;
  z-index: 99;
}

.mode-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28rpx;
  font-weight: 600;
  color: #8e8e8e;
  border-bottom: 4rpx solid transparent;
}

.mode-tab.active {
  color: #000;
  border-bottom-color: #000;
}

.image-section {
  padding: 32rpx;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.image-item {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  background: #fafafa;
  border-radius: 8rpx;
  overflow: hidden;
}

.image-item .image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.delete-btn {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 48rpx;
  height: 48rpx;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.delete-btn text {
  color: #fff;
  font-size: 36rpx;
  line-height: 1;
}

.add-image {
  width: 100%;
  padding-bottom: 100%;
  background: #fafafa;
  border: 2rpx dashed #dbdbdb;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.add-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 64rpx;
  color: #8e8e8e;
}

.add-video {
  padding-bottom: 56%;
  flex-direction: column;
  gap: 16rpx;
}

.add-video-hint {
  position: absolute;
  bottom: 30%;
  left: 50%;
  transform: translateX(-50%);
  font-size: 22rpx;
  color: #8e8e8e;
  white-space: nowrap;
}

.video-preview-wrap {
  position: relative;
  width: 100%;
  border-radius: 8rpx;
  overflow: hidden;
}

.video-preview {
  width: 100%;
  height: 560rpx;
  background: #000;
}

.video-delete {
  top: 16rpx;
  right: 16rpx;
}

.input-section {
  padding: 32rpx;
  border-top: 2rpx solid #f1f1f1;
}

.caption-editor {
  position: relative;
}

.caption-highlight,
.textarea {
  width: 100%;
  min-height: 200rpx;
  padding: 0;
  font-size: 28rpx;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.caption-highlight {
  pointer-events: none;
  color: transparent;
}

.segment-text {
  color: #111111;
}

.segment-tag {
  color: #0095f6;
  padding-right: 16rpx;
}

.textarea {
  position: absolute;
  top: 0;
  left: 0;
  background: transparent;
  color: rgba(0, 0, 0, 0.02);
  caret-color: #111111;
  z-index: 2;
}

.tag-suggestion-panel {
  margin-top: 24rpx;
  border: 2rpx solid #f1f1f1;
  border-radius: 20rpx;
  background: #fff;
  overflow: hidden;
}

.tag-suggestion-status {
  padding: 24rpx;
  font-size: 26rpx;
  color: #8e8e93;
}

.error-text {
  color: #ff3b30;
}

.tag-suggestion-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx;
  border-bottom: 1rpx solid #f2f2f7;
}

.tag-suggestion-item:last-child {
  border-bottom: none;
}

.tag-suggestion-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.tag-suggestion-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #111111;
}

.tag-suggestion-count {
  font-size: 24rpx;
  color: #8e8e93;
}

.location-section {
  padding: 0 32rpx 32rpx;
}

.location-input {
  width: 100%;
  height: 80rpx;
  background: #fafafa;
  border: 2rpx solid #dbdbdb;
  border-radius: 8rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
}

.loading-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.loading-box {
  background: #fff;
  padding: 40rpx 60rpx;
  border-radius: 16rpx;
}

.loading-box text {
  font-size: 28rpx;
  color: #000;
}
</style>
