<template>
  <Layout>
    <view class="home">
    <!-- 顶部导航 -->
    <view class="header">
      <view class="header-left">
        <text class="logo">Instagram</text>
        <text class="logo-arrow">&#x25BE;</text>
      </view>
      <view class="header-right">
        <view class="icon-wrap">
          <image class="header-icon" src="/static/icons/like.svg" mode="aspectFit" />
          <view class="notification-dot" />
        </view>
        <image class="header-icon" src="/static/icons/airport.svg" mode="aspectFit" />
      </view>
    </view>

    <!-- Stories 横向滚动 -->
    <scroll-view class="stories" scroll-x :show-scrollbar="false">
      <!-- 自己的快拍 -->
      <view class="story-item">
        <view class="story-avatar-wrap story-mine">
          <image class="story-avatar" :src="myAvatar" mode="aspectFill" />
          <view class="story-add">
            <text class="story-add-icon">+</text>
          </view>
        </view>
        <text class="story-name">你的快拍</text>
      </view>
      <!-- 其他人的快拍 -->
      <view class="story-item" v-for="item in stories" :key="item.id">
        <view class="story-avatar-wrap" :style="{ background: item.borderColor }">
          <view class="story-avatar-inner">
            <image class="story-avatar" :src="item.avatar" mode="aspectFill" />
          </view>
        </view>
        <text class="story-name">{{ item.name }}</text>
      </view>
    </scroll-view>

    <!-- 帖子列表 -->
    <view class="post-card" v-for="post in posts" :key="post.id">
      <!-- 帖子头部 -->
      <view class="post-header">
        <view class="post-user">
          <image class="post-avatar" :src="post.avatar" mode="aspectFill" @click="goUserDetail(post.userId)" />
          <view class="post-user-info">
            <view class="post-name-wrap">
              <text class="post-username">{{ post.username }}</text>
              <image class="vip-icon" src="/static/icons/vip.svg" mode="aspectFit" />
            </view>
            <text class="post-location">{{ post.location }}</text>
          </view>
        </view>
        <view class="post-header-right">
          <view class="follow-btn" v-if="post.showFollow" @click="handleFollow(post.id)">
            <text class="follow-text">关注</text>
          </view>
          <text class="post-more">•••</text>
        </view>
      </view>

      <!-- 图片轮播 -->
      <view class="post-images">
        <ImageSwiper :images="post.images" />
      </view>

      <!-- 操作栏 -->
      <view class="post-actions">
        <view class="actions-left">
          <view class="action-item" @click="handleToggleLike(post.id)">
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
        <view class="actions-right" :class="{ 'feedback-save-animating': post.saveAnimating }" @click="handleToggleSave(post.id)">
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
            v-for="(segment, index) in getDisplaySegments(post)"
            :key="`${post.id}-${index}`"
            :class="segment.type === 'tag' ? 'content-tag' : ''"
          >{{ segment.text }}</text>
          <text v-if="!post.expanded && isContentLong(post.content)" class="content-expand" @click.stop="post.expanded = true"> ...展开</text>
        </text>
      </view>

      <!-- 日期 -->
      <view class="post-date">
        <text class="date-text">{{ post.date }}</text>
        <!-- <text class="translate-text"> · 查看翻译</text> -->
      </view>
    </view>

    <!-- 底部状态提示 -->
    <view v-if="loading" class="feed-status">
      <text class="feed-status-text">加载中...</text>
    </view>
    <view v-else-if="!hasMore && posts.length" class="feed-status">
      <text class="feed-status-text">没有更多了</text>
    </view>
    </view>
  </Layout>
</template>

<script setup>
import { onReachBottom } from '@dcloudio/uni-app'
import Layout from '@/components/common/Layout.vue'
import ImageSwiper from '@/components/common/ImageSwiper.vue'
import { useHome } from '@/composables/useHome'
import { useUserStore } from '@/pinia/modules/userStore.js'
import { useAppStore } from '@/pinia/modules/appStore.js'
import { computed } from 'vue'

const { stories, posts, hasMore, loading, fetchPosts, handleFollow, handleToggleLike, handleToggleSave, goUserDetail } = useHome()
const userStore = useUserStore()
const appStore = useAppStore()

const CONTENT_MAX_LENGTH = 60

const isContentLong = (content = '') => content.length > CONTENT_MAX_LENGTH

const getDisplaySegments = (post) => {
  const content = post.content || ''
  if (post.expanded || !isContentLong(content)) {
    return formatContentSegments(content)
  }
  return formatContentSegments(content.slice(0, CONTENT_MAX_LENGTH))
}

const formatContentSegments = (content = '') => {
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

const myAvatar = computed(() => {
  if (userStore.userInfo?.avatar) {
    return userStore.userInfo.avatar
  }
  return '/static/images/avatar/1.jpg'
})

onReachBottom(() => {
  fetchPosts()
})
</script>

<style scoped>
/* ===== Header ===== */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid var(--theme-border);
  background: var(--page-bg);
}

.header-left {
  display: flex;
  align-items: center;
}

.logo {
  font-family: 'Georgia', serif;
  font-size: 48rpx;
  font-weight: bold;
  font-style: italic;
}

.logo-arrow {
  font-size: 28rpx;
  margin-left: 4rpx;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 36rpx;
}

.icon-wrap {
  position: relative;
}

.header-icon {
  width: 48rpx;
  height: 48rpx;
}

.notification-dot {
  position: absolute;
  top: 2rpx;
  right: -4rpx;
  width: 14rpx;
  height: 14rpx;
  background-color: var(--theme-error);
  border-radius: 50%;
}

/* ===== Stories ===== */
.stories {
  display: flex;
  white-space: nowrap;
  padding: 20rpx 16rpx;
  border-bottom: 1rpx solid var(--theme-border);
}

.story-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  width: 160rpx;
  flex-shrink: 0;
}

.story-avatar-wrap {
  width: 136rpx;
  height: 136rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.story-mine {
  background: transparent !important;
  width: 136rpx;
  height: 136rpx;
}

.story-mine .story-avatar {
  width: 120rpx;
  height: 120rpx;
  border: 2rpx solid var(--theme-border);
}

.story-avatar-inner {
  width: 124rpx;
  height: 124rpx;
  border-radius: 50%;
  background: var(--page-bg);
  display: flex;
  align-items: center;
  justify-content: center;
}

.story-avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
}

.story-add {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 40rpx;
  height: 40rpx;
  background: var(--theme-link);
  border-radius: 50%;
  border: 3rpx solid #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.story-add-icon {
  color: #fff;
  font-size: 28rpx;
  font-weight: bold;
  line-height: 1;
}

.story-name {
  font-size: 22rpx;
  color: var(--text-color);
  margin-top: 8rpx;
  max-width: 140rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

/* ===== Post Card ===== */
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
  /* border: 1rpx solid var(--theme-border); */
  border-radius: 14rpx;
  padding: 10rpx 18rpx;
  background: var(--btn-bg);
  display: flex;
  align-items: center;
  justify-content: center;
}

.follow-text {
  font-size: 20rpx;
  font-weight: 600;
  color: var(--text-color);
}

.post-more {
  font-size: 28rpx;
  letter-spacing: 2rpx;
  color: var(--text-color);
}

/* ===== Images ===== */
.post-images {
  /* padding: 0 24rpx; */
}

/* ===== Actions ===== */
.post-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx;
}

.actions-left {
  display: flex;
  align-items: center;
  gap: 32rpx;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.action-icon-wrap,
.save-action-inner {
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transform-origin: center;
}

.action-icon-img {
  width: 44rpx;
  height: 44rpx;
}

.action-icon {
  font-size: 48rpx;
  color: var(--text-color);
}

.action-count {
  font-size: 26rpx;
  color: var(--text-color);
  transition: color 0.2s ease;
}

.actions-right {
  min-width: 68rpx;
  min-height: 68rpx;
  border-radius: 20rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s ease, transform 0.2s ease;
}

/* ===== Content ===== */
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

/* ===== Date ===== */
.post-date {
  padding: 0 24rpx 20rpx;
}

.date-text {
  font-size: 22rpx;
  color: var(--theme-secondary);
}

.translate-text {
  font-size: 22rpx;
  color: #00376b;
}

.feed-status {
  padding: 40rpx 0;
  text-align: center;
}

.feed-status-text {
  font-size: 24rpx;
  color: var(--theme-secondary);
}

</style>
