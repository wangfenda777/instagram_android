<template>
  <Layout>
    <scroll-view
      class="search-overview-page"
      scroll-y
      :lower-threshold="160"
      @scrolltolower="loadMore"
    >
      <view class="header">
        <view class="header-left" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <view class="header-search">
          <image class="search-icon" src="/static/icons/search.svg" mode="aspectFit" />
          <text class="keyword-text">{{ keyword }}</text>
        </view>
      </view>

      <view class="tabs">
        <view
          v-for="tab in tabs"
          :key="tab.key"
          class="tab-item"
          :class="{ active: activeTab === tab.key }"
          @click="switchTab(tab.key)"
        >
          <text>{{ tab.label }}</text>
        </view>
      </view>

      <view v-if="currentState.error" class="status-wrap error-text">
        <text>{{ currentState.error }}</text>
      </view>

      <view v-else-if="!keyword" class="status-wrap">
        <text>请输入搜索关键词</text>
      </view>

      <template v-else>
        <ProfileGrid
          v-if="activeTab === 'recommend'"
          :items="recommendList"
          empty-text="暂无推荐内容"
          :show-multi-icon="true"
        />

        <view v-else-if="activeTab === 'account'" class="list-section">
          <view v-if="accountList.length" class="result-list">
            <view
              v-for="user in accountList"
              :key="user.id || user.username"
              class="result-row user-row"
            >
              <image class="user-avatar" :src="user.avatar" mode="aspectFill" />
              <view class="result-info user-info">
                <view class="user-name-row">
                  <text class="result-title">{{ user.username }}</text>
                  <text v-if="user.isVerified" class="verified-badge">已认证</text>
                </view>
                <text v-if="user.displayName" class="result-desc">{{ user.displayName }}</text>
                <text v-if="user.followersCount" class="result-desc">{{ user.followersCount }} 位粉丝</text>
              </view>
            </view>
          </view>
          <view v-else class="status-wrap">
            <text>暂无相关账户</text>
          </view>
        </view>

        <view v-else class="list-section">
          <view v-if="tagList.length" class="result-list">
            <view
              v-for="tag in tagList"
              :key="tag.id || tag.name"
              class="result-row tag-row"
            >
              <view class="tag-icon-wrap">
                <image
                  v-if="tag.cover"
                  class="tag-cover"
                  :src="tag.cover"
                  mode="aspectFill"
                />
                <image
                  v-else
                  class="tag-search-icon"
                  src="/static/icons/search.svg"
                  mode="aspectFit"
                />
              </view>
              <view class="result-info">
                <text class="result-title">{{ tag.name }}</text>
                <text v-if="tag.postCount" class="result-desc">{{ tag.postCount }} 条内容</text>
              </view>
            </view>
          </view>
          <view v-else class="status-wrap">
            <text>暂无相关标签</text>
          </view>
        </view>

        <view v-if="currentState.loading" class="status-wrap">
          <text>加载中...</text>
        </view>

        <view v-else-if="currentState.initialized && !currentState.hasMore && currentState.list.length" class="status-wrap end-text">
          <text>没有更多了</text>
        </view>
      </template>
    </scroll-view>
  </Layout>
</template>

<script setup>
import { onMounted } from 'vue'
import Layout from '@/components/common/Layout.vue'
import ProfileGrid from '@/components/profile/ProfileGrid.vue'
import { useSearchOverview } from '@/composables/useSearchOverview'

const {
  keyword,
  tabs,
  activeTab,
  currentState,
  recommendList,
  accountList,
  tagList,
  initialize,
  switchTab,
  loadMore
} = useSearchOverview()

const goBack = () => {
  uni.navigateBack()
}

onMounted(() => {
  initialize()
})
</script>

<style scoped>
.search-overview-page {
  height: 100vh;
  background: var(--page-bg);
}

.header {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  position: sticky;
  top: 0;
  background: var(--page-bg);
  z-index: 10;
}

.header-left {
  width: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  font-size: 48rpx;
  color: var(--text-color);
  line-height: 1;
}

.header-search {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16rpx;
  min-height: 72rpx;
  padding: 0 24rpx;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 18rpx;
}

.search-icon {
  width: 36rpx;
  height: 36rpx;
}

.keyword-text {
  font-size: 28rpx;
  color: var(--text-color);
}

.tabs {
  display: flex;
  align-items: center;
  padding: 0 24rpx;
  border-bottom: 1rpx solid var(--theme-border);
  background: var(--page-bg);
}

.tab-item {
  position: relative;
  padding: 24rpx 20rpx;
  margin-right: 28rpx;
  font-size: 26rpx;
  color: var(--theme-secondary);
}

.tab-item.active {
  color: var(--text-color);
  font-weight: 600;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  left: 20rpx;
  right: 20rpx;
  bottom: 0;
  height: 4rpx;
  background: var(--text-color);
  border-radius: 999rpx;
}

.list-section {
  padding: 0 24rpx 24rpx;
}

.result-list {
  display: flex;
  flex-direction: column;
}

.result-row {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f2f2f7;
}

.tag-icon-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  border: 1rpx solid #dcdce0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
  background: #ffffff;
}

.tag-cover {
  width: 100%;
  height: 100%;
}

.tag-search-icon {
  width: 40rpx;
  height: 40rpx;
}

.user-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.result-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.user-info {
  gap: 6rpx;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.result-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-color);
  line-height: 1.4;
}

.result-desc {
  font-size: 24rpx;
  color: var(--theme-secondary);
}

.verified-badge {
  font-size: 20rpx;
  color: var(--theme-link);
}

.status-wrap {
  min-height: 120rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  color: var(--theme-secondary);
}

.error-text {
  color: var(--theme-error);
}

.end-text {
  min-height: 96rpx;
}
</style>
