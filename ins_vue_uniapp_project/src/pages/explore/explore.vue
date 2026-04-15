<template>
  <Layout>
    <view class="explore">
      <view class="search-bar">
        <view class="search-bar-inner">
          <view class="search-input">
            <image class="search-icon" src="/static/icons/search.svg" mode="aspectFit" />
            <input
              class="input"
              v-model="searchKeyword"
              placeholder="搜索"
              placeholder-class="placeholder"
              confirm-type="search"
              @focus="enterSearchMode"
              @input="handleSearchInput"
              @confirm="handleSearch"
            />
          </view>
          <text v-if="isSearchMode" class="cancel-text" @click="cancelSearch">取消</text>
        </view>
      </view>

      <view v-if="isSearchMode" class="search-panel">
        <view v-if="isSearching" class="search-status">
          <text>搜索中...</text>
        </view>

        <view v-else-if="searchError" class="search-status error-text">
          <text>{{ searchError }}</text>
        </view>

        <view v-else-if="!hasSearched" class="search-status">
          <text>搜索用户和标签</text>
        </view>

        <view v-else-if="!tagResults.length && !userResults.length" class="search-status">
          <text>暂无搜索结果</text>
        </view>

        <!-- 搜索后的标签列表 -->
        <view v-else class="search-result-list">
          <view v-if="tagResults.length" class="result-section">
            <text class="section-title">标签</text>
            <view
              v-for="tag in tagResults"
              :key="tag.id || tag.name"
              class="result-row tag-row"
              @click="goSearchOverview(tag)"
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

          <!-- 搜索后的用户列表 -->
          <view v-if="userResults.length" class="result-section">
            <text class="section-title">用户</text>
            <view
              v-for="user in userResults"
              :key="user.id || user.username"
              class="result-row user-row"
              @click="goUserDetail(user)"
            >
              <image class="user-avatar" :src="user.avatar" mode="aspectFill" />
              <view class="result-info user-info">
                <view class="user-name-row">
                  <text class="result-title">{{ user.username }}</text>
                  <text v-if="user.isVerified" class="verified-badge">已认证</text>
                </view>
                <text v-if="user.displayName" class="result-desc">{{ user.displayName }}</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-else class="waterfall">
        <view class="waterfall-column">
          <view
            class="waterfall-item"
            v-for="item in column1"
            :key="item.id"
            :class="{ 'item-video-wrap': item.type === 'video' }"
            @click="openPost(item.id)"
          >
            <view :class="item.type === 'video' ? 'item-video' : 'item-image'">
              <image class="media" :src="item.cover" mode="aspectFill" />
              <image
                v-if="item.type === 'image' && item.mediaCount > 1"
                class="multi-icon"
                src="/static/icons/copy.svg"
                mode="aspectFit"
              />
              <image
                v-if="item.type === 'video'"
                class="play-icon"
                src="/static/icons/video.svg"
                mode="aspectFit"
              />
            </view>
          </view>
        </view>

        <view class="waterfall-column">
          <view
            class="waterfall-item"
            v-for="item in column2"
            :key="item.id"
            :class="{ 'item-video-wrap': item.type === 'video' }"
            @click="openPost(item.id)"
          >
            <view :class="item.type === 'video' ? 'item-video' : 'item-image'">
              <image class="media" :src="item.cover" mode="aspectFill" />
              <image
                v-if="item.type === 'image' && item.mediaCount > 1"
                class="multi-icon"
                src="/static/icons/copy.svg"
                mode="aspectFit"
              />
              <image
                v-if="item.type === 'video'"
                class="play-icon"
                src="/static/icons/video.svg"
                mode="aspectFit"
              />
            </view>
          </view>
        </view>

        <view class="waterfall-column">
          <view
            class="waterfall-item"
            v-for="item in column3"
            :key="item.id"
            :class="{ 'item-video-wrap': item.type === 'video' }"
            @click="openPost(item.id)"
          >
            <view :class="item.type === 'video' ? 'item-video' : 'item-image'">
              <image class="media" :src="item.cover" mode="aspectFill" />
              <image
                v-if="item.type === 'image' && item.mediaCount > 1"
                class="multi-icon"
                src="/static/icons/copy.svg"
                mode="aspectFit"
              />
              <image
                v-if="item.type === 'video'"
                class="play-icon"
                src="/static/icons/video.svg"
                mode="aspectFit"
              />
            </view>
          </view>
        </view>
      </view>

      <!-- 底部状态提示 -->
      <view v-if="!isSearchMode && exploreLoading" class="explore-status">
        <text class="explore-status-text">加载中...</text>
      </view>
      <view v-else-if="!isSearchMode && !exploreHasMore && exploreList.length" class="explore-status">
        <text class="explore-status-text">没有更多了</text>
      </view>
    </view>

    <!-- 帖子详情弹窗 -->
    <FullscreenPopup v-model:show="showPopup">
      <view v-if="detailLoading" class="detail-loading">
        <text>加载中...</text>
      </view>
      <PostCard
        v-else-if="detailPost"
        :post="detailPost"
        @avatar-click="detailGoUserDetail"
        @follow="detailHandleFollow"
        @toggle-like="detailHandleToggleLike"
        @toggle-save="detailHandleToggleSave"
      />
    </FullscreenPopup>
  </Layout>
</template>

<script setup>
import { onReachBottom } from '@dcloudio/uni-app'
import Layout from '@/components/common/Layout.vue'
import FullscreenPopup from '@/components/common/FullscreenPopup.vue'
import PostCard from '@/components/common/PostCard.vue'
import { useExplore } from '@/composables/useExplore'
import { usePostDetail } from '@/composables/usePostDetail'

const {
  exploreList,
  exploreLoading,
  exploreHasMore,
  column1,
  column2,
  column3,
  fetchExplore,
  searchKeyword,
  isSearchMode,
  isSearching,
  hasSearched,
  searchError,
  tagResults,
  userResults,
  enterSearchMode,
  cancelSearch,
  handleSearchInput,
  handleSearch,
  goSearchOverview,
  goUserDetail
} = useExplore()

const {
  showPopup,
  post: detailPost,
  loading: detailLoading,
  openPost,
  closePost,
  handleFollow: detailHandleFollow,
  handleToggleLike: detailHandleToggleLike,
  handleToggleSave: detailHandleToggleSave,
  goUserDetail: detailGoUserDetail
} = usePostDetail()

onReachBottom(() => {
  if (!isSearchMode.value) {
    fetchExplore()
  }
})
</script>

<style scoped>
.explore {
  padding-bottom: 20rpx;
}

.search-bar {
  padding: 20rpx 24rpx;
  position: sticky;
  top: 0;
  background: var(--page-bg);
  z-index: 10;
}

.search-bar-inner {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.search-input {
  flex: 1;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16rpx;
  padding: 16rpx 24rpx;
  gap: 16rpx;
}

.search-icon {
  width: 36rpx;
  height: 36rpx;
}

.input {
  flex: 1;
  font-size: 28rpx;
  color: var(--text-color);
}

.placeholder {
  color: var(--theme-secondary);
}

.cancel-text {
  flex-shrink: 0;
  font-size: 28rpx;
  color: var(--text-color);
}

.search-panel {
  min-height: calc(100vh - 120rpx);
  padding: 0 24rpx 24rpx;
  background: var(--page-bg);
}

.search-status {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 240rpx;
  font-size: 28rpx;
  color: #8e8e93;
}

.error-text {
  color: #ff3b30;
}

.search-result-list {
  display: flex;
  flex-direction: column;
}

.result-section {
  display: flex;
  flex-direction: column;
}

.section-title {
  padding: 16rpx 0;
  font-size: 24rpx;
  font-weight: 600;
  color: #8e8e93;
}

.result-row {
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 20rpx 0;
}

.tag-row,
.user-row {
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

.result-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.result-title {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-color);
  line-height: 1.4;
}

.result-desc {
  font-size: 24rpx;
  color: #8e8e93;
  line-height: 1.4;
}

.user-avatar {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  flex-shrink: 0;
}

.user-info {
  justify-content: center;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  min-width: 0;
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

.waterfall {
  display: flex;
  gap: 2rpx;
  padding: 0 4rpx;
}

.waterfall-column {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2rpx;
}

.waterfall-item {
  position: relative;
  overflow: hidden;
}

.item-image {
  position: relative;
  width: 100%;
  padding-bottom: 100%;
  overflow: hidden;
}

.item-video {
  position: relative;
  width: 100%;
  padding-bottom: calc(200% + 2rpx);
  overflow: hidden;
}

.media {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.multi-icon {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  width: 32rpx;
  height: 32rpx;
  filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.3));
}

.play-icon {
  position: absolute;
  top: 16rpx;
  right: 16rpx;
  width: 40rpx;
  height: 40rpx;
  filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.3));
}

.explore-status {
  padding: 40rpx 0;
  text-align: center;
}

.explore-status-text {
  font-size: 24rpx;
  color: var(--theme-secondary);
}

.detail-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400rpx;
  font-size: 28rpx;
  color: var(--theme-secondary);
}
</style>
