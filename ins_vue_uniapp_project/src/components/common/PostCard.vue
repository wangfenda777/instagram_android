<template>
  <view class="post-card">
    <!-- 帖子头部 -->
    <view class="post-header">
      <view class="post-user" @click="$emit('avatar-click', post.userId)">
        <image class="post-avatar" :src="post.avatar" mode="aspectFill" />
        <view class="post-user-info">
          <view class="post-name-wrap">
            <text class="post-username">{{ post.username }}</text>
            <image v-if="post.isVerified" class="vip-icon" src="/static/icons/vip.svg" mode="aspectFit" />
          </view>
          <text v-if="post.location" class="post-location">{{ post.location }}</text>
        </view>
      </view>
      <view class="post-header-right">
        <view class="follow-btn" v-if="post.showFollow" @click="$emit('follow', post.id)">
          <text class="follow-text">关注</text>
        </view>
        <view class="post-more" @click="$emit('more-click', post.id)">•••</view>
      </view>
    </view>

    <!-- 图片轮播 -->
    <view class="post-images">
      <ImageSwiper :images="post.images" :media-list="post.mediaList" />
    </view>

    <!-- 操作栏 -->
    <view class="post-actions">
      <view class="actions-left">
        <view class="action-item" @click="$emit('toggle-like', post.id)">
          <view class="action-icon-wrap" :class="{ 'feedback-like-animating': post.likeAnimating }">
            <image
              class="action-icon-img"
              :src="post.isLiked ? '/static/icons/like_selected.svg' : '/static/icons/like.svg'"
              mode="aspectFit"
            />
          </view>
          <text class="action-count" :class="{ 'feedback-liked-count': post.isLiked }">{{ post.likes }}</text>
        </view>
        <view class="action-item">
          <image class="action-icon-img" src="/static/icons/talk.svg" mode="aspectFit" />
          <text class="action-count">{{ post.comments }}</text>
        </view>
        <view class="action-item">
          <image class="action-icon-img" src="/static/icons/airport.svg" mode="aspectFit" />
          <text class="action-count">{{ post.shares }}</text>
        </view>
      </view>
      <view class="actions-right" :class="{ 'feedback-save-animating': post.saveAnimating }" @click="$emit('toggle-save', post.id)">
        <view class="save-action-inner feedback-save-inner">
          <image
            class="action-icon-img"
            :src="post.isSaved ? '/static/icons/collect_selected.svg' : '/static/icons/collect.svg'"
            mode="aspectFit"
          />
        </view>
      </view>
    </view>

    <!-- 文字内容 -->
    <view class="post-content">
      <text class="content-username">{{ post.username }}</text>
      <text class="content-text">
        <text
          v-for="(segment, index) in displaySegments"
          :key="`${post.id}-${index}`"
          :class="segment.type === 'tag' ? 'content-tag' : ''"
        >{{ segment.text }}</text>
        <text v-if="!expanded && isLong" class="content-expand" @click.stop="expanded = true"> ...展开</text>
      </text>
    </view>

    <!-- 日期 -->
    <view class="post-date">
      <text class="date-text">{{ post.date }}</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import ImageSwiper from '@/components/common/ImageSwiper.vue'

const CONTENT_MAX_LENGTH = 60

const props = defineProps({
  post: { type: Object, required: true }
})

defineEmits(['avatar-click', 'follow', 'toggle-like', 'toggle-save', 'more-click'])

const expanded = ref(false)

const isLong = computed(() => (props.post.content || '').length > CONTENT_MAX_LENGTH)

const displaySegments = computed(() => {
  const content = props.post.content || ''
  if (expanded.value || !isLong.value) {
    return formatContentSegments(content)
  }
  return formatContentSegments(content.slice(0, CONTENT_MAX_LENGTH))
})

function formatContentSegments(content = '') {
  const text = ` ${content || ''}`
  const regex = /#[^\s#]+/g
  const segments = []
  let lastIndex = 0
  let match = regex.exec(text)

  while (match) {
    const start = match.index
    const end = start + match[0].length

    if (start > lastIndex) {
      segments.push({ type: 'text', text: text.slice(lastIndex, start) })
    }

    segments.push({ type: 'tag', text: match[0] })
    lastIndex = end
    match = regex.exec(text)
  }

  if (lastIndex < text.length) {
    segments.push({ type: 'text', text: text.slice(lastIndex) })
  }

  return segments
}
</script>

<style scoped>
.post-card {
  margin-bottom: 16rpx;
}

.post-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
}

.post-user {
  display: flex;
  align-items: center;
}

.post-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  margin-right: 16rpx;
}

.post-user-info {
  display: flex;
  flex-direction: column;
}

.post-name-wrap {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.post-username {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-color);
}

.vip-icon {
  width: 22rpx;
  height: 22rpx;
}

.post-location {
  font-size: 22rpx;
  color: var(--theme-secondary);
}

.post-header-right {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.follow-btn {
  padding: 10rpx 28rpx;
  background: var(--theme-link);
  border-radius: 16rpx;
}

.follow-text {
  font-size: 24rpx;
  color: #fff;
  font-weight: 600;
}

.post-more {
  font-size: 28rpx;
  color: var(--text-color);
  letter-spacing: 4rpx;
}

.post-images {
  width: 100%;
}

.post-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
}

.actions-left {
  display: flex;
  align-items: center;
  gap: 28rpx;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.action-icon-img {
  width: 48rpx;
  height: 48rpx;
}

.action-count {
  font-size: 24rpx;
  color: var(--text-color);
}

.actions-right {
  display: flex;
  align-items: center;
}

.save-action-inner {
  display: flex;
  align-items: center;
  justify-content: center;
}

.post-content {
  padding: 0 24rpx 8rpx;
}

.content-username {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-color);
  margin-right: 16rpx;
}

.content-text {
  font-size: 26rpx;
  color: var(--text-color);
}

.content-tag {
  color: #00376b;
}

.content-expand {
  font-size: 26rpx;
  color: var(--theme-secondary);
  white-space: nowrap;
}

.post-date {
  padding: 0 24rpx 20rpx;
}

.date-text {
  font-size: 22rpx;
  color: var(--theme-secondary);
}
</style>
