<template>
  <view class="follow-list-page">
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-arrow">‹</text>
      </view>
      <text class="nav-title">{{ username }}</text>
      <view class="nav-right"></view>
    </view>

    <view class="tabs">
      <view
        class="tab-item"
        :class="{ active: activeTab === 0 }"
        @click="handleSwitchTab(0)"
      >
        <text class="tab-text">{{ getTabText(0) }}</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 1 }"
        @click="handleSwitchTab(1)"
      >
        <text class="tab-text">{{ getTabText(1) }}</text>
      </view>
    </view>

    <view class="list-wrap">
      <view v-if="currentList.length" class="user-list">
        <view v-for="user in currentList" :key="user.id" class="user-item">
          <image class="user-avatar" :src="user.avatar" mode="aspectFill" />
          <view class="user-info">
            <view class="user-name-row">
              <text class="user-name">{{ user.username }}</text>
              <text v-if="user.isVerified" class="verified-badge">已认证</text>
            </view>
            <text v-if="user.displayName" class="user-display-name">{{ user.displayName }}</text>
          </view>
          <view v-if="currentTabKey === 'followers' || user.isFollowing" class="follow-btn" :class="{ following: user.isFollowing }">
            <text class="follow-btn-text">{{ user.isFollowing ? '已关注' : '关注' }}</text>
          </view>
        </view>
      </view>

      <view v-else-if="currentTabState.loaded" class="empty-state">
        <text>{{ activeTab === 0 ? '暂无粉丝' : '暂无关注' }}</text>
      </view>

      <view v-if="currentList.length" class="list-footer-status">
        <text v-if="currentTabState.loading">加载中...</text>
        <text v-else-if="!currentTabState.hasMore">已经到底啦~</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { useFollowList } from '@/composables/useFollowList.js'

const {
  username,
  activeTab,
  currentList,
  currentTabKey,
  currentTabState,
  getTabText,
  initPage,
  switchTab,
  loadMore
} = useFollowList()

onLoad(async (query) => {
  await initPage(query)
})

onReachBottom(async () => {
  await loadMore()
})

const handleSwitchTab = async (tabIndex) => {
  await switchTab(tabIndex)
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.follow-list-page {
  min-height: 100vh;
  background: var(--page-bg);
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
  border-bottom: 1rpx solid var(--theme-border);
  background: var(--page-bg);
}

.nav-back,
.nav-right {
  width: 80rpx;
  display: flex;
  align-items: center;
}

.nav-right {
  justify-content: flex-end;
}

.back-arrow {
  font-size: 56rpx;
  line-height: 1;
  color: var(--text-color);
  font-weight: 300;
}

.nav-title {
  flex: 1;
  text-align: center;
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-color);
}

.tabs {
  display: flex;
  align-items: center;
  background: var(--page-bg);
  border-bottom: 1rpx solid var(--theme-border);
}

.tab-item {
  position: relative;
  flex: 1;
  display: flex;
  justify-content: center;
  padding: 24rpx 0;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 88rpx;
  height: 4rpx;
  background: var(--text-color);
  transform: translateX(-50%);
  border-radius: 999rpx;
}

.tab-text {
  font-size: 28rpx;
  color: #8e8e93;
}

.tab-item.active .tab-text {
  color: var(--text-color);
  font-weight: 600;
}

.list-wrap {
  padding: 0 32rpx 32rpx;
}

.user-list {
  display: flex;
  flex-direction: column;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f2f2f7;
}

.user-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  min-width: 0;
}

.user-name {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-color);
  line-height: 1.4;
}

.verified-badge {
  flex-shrink: 0;
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  background: #eef6ff;
  color: #0095f6;
  font-size: 20rpx;
  line-height: 1.2;
}

.user-display-name {
  font-size: 24rpx;
  color: #8e8e93;
  line-height: 1.4;
}

.follow-btn {
  min-width: 128rpx;
  height: 64rpx;
  padding: 0 24rpx;
  border-radius: 16rpx;
  background: #0095f6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.follow-btn.following {
  background: #efeff4;
}

.follow-btn-text {
  font-size: 24rpx;
  font-weight: 600;
  color: #ffffff;
}

.follow-btn.following .follow-btn-text {
  color: var(--text-color);
}

.empty-state,
.list-footer-status {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 48rpx 0;
  font-size: 24rpx;
  color: #8e8e93;
}
</style>
