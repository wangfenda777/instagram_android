<template>
  <Layout>
    <view class="user-detail-page">
      <view class="header">
        <view class="header-left" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <view class="header-center">
          <text class="username">{{ profile.username }}</text>
        </view>
        <view class="header-right">
          <image class="icon" src="/static/icons/elip.svg" mode="aspectFit" />
        </view>
      </view>
      <!-- 头像和用户数据 -->
      <ProfileSummary
        :avatar="summary.avatar"
        :display-name="summary.displayName"
        :bio="summary.bio"
        :is-verified="summary.isVerified"
        :posts="summary.posts"
        :followers="summary.followers"
        :following="summary.following"
        @stat-click="handleStatClick"
      />

      <view class="action-buttons">
        <view
          v-for="button in actionButtons"
          :key="button.key"
          class="btn btn-primary"
          @click="handlePrimaryAction(button.key)"
        >
          {{ button.text }}
        </view>
      </view>

      <ProfileTabs :model-value="activeTab" :tabs="tabs" @update:model-value="switchTab" />

      <ProfileGrid
        v-if="activeTab === 0"
        :items="postItems"
        empty-text="暂无帖子"
        :show-multi-icon="true"
        @item-click="onPostClick"
      />

      <ProfileGrid
        v-else-if="activeTab === 1"
        :items="reelItems"
        empty-text="暂无 Reels"
        :show-video-icon="true"
      />

      <view v-else class="placeholder-panel">
        <text>该模块暂未开放</text>
      </view>
    </view>

    <!-- 帖子详情弹窗 -->
    <FullscreenPopup v-model:show="detailShowPopup" @scroll-top="detailLoadBefore" @scroll-bottom="detailLoadAfter">
      <view v-if="detailLoading" class="detail-loading">
        <text>加载中...</text>
      </view>
      <template v-else>
        <view v-if="detailLoadingBefore" class="detail-status">
          <text>加载中...</text>
        </view>
        <view v-else-if="!detailHasMoreBefore && detailPosts.length" class="detail-status">
          <text>没有更新的帖子了</text>
        </view>

        <PostCard
          v-for="p in detailPosts"
          :key="p.id"
          :post="p"
          @avatar-click="detailGoUserDetail"
          @follow="detailHandleFollow"
          @toggle-like="detailHandleToggleLike"
          @toggle-save="detailHandleToggleSave"
        />

        <view v-if="detailLoadingAfter" class="detail-status">
          <text>加载中...</text>
        </view>
        <view v-else-if="!detailHasMoreAfter && detailPosts.length" class="detail-status">
          <text>没有更多了</text>
        </view>
      </template>
    </FullscreenPopup>
  </Layout>
</template>

<script setup>
import Layout from '@/components/common/Layout.vue'
import FullscreenPopup from '@/components/common/FullscreenPopup.vue'
import PostCard from '@/components/common/PostCard.vue'
import ProfileSummary from '@/components/profile/ProfileSummary.vue'
import ProfileTabs from '@/components/profile/ProfileTabs.vue'
import ProfileGrid from '@/components/profile/ProfileGrid.vue'
import { useUserDetail } from '@/composables/useUserDetail'
import { useUserPostsDetail } from '@/composables/useUserPostsDetail'

const {
  tabs,
  activeTab,
  profile,
  summary,
  actionButtons,
  postItems,
  reelItems,
  switchTab,
  handlePrimaryAction,
  goFollowList
} = useUserDetail()

const {
  showPopup: detailShowPopup,
  posts: detailPosts,
  loading: detailLoading,
  loadingBefore: detailLoadingBefore,
  loadingAfter: detailLoadingAfter,
  hasMoreBefore: detailHasMoreBefore,
  hasMoreAfter: detailHasMoreAfter,
  openPost: detailOpenPost,
  loadBefore: detailLoadBefore,
  loadAfter: detailLoadAfter,
  handleFollow: detailHandleFollow,
  handleToggleLike: detailHandleToggleLike,
  handleToggleSave: detailHandleToggleSave,
  goUserDetail: detailGoUserDetail
} = useUserPostsDetail()

const goBack = () => {
  uni.navigateBack()
}

const handleStatClick = (type) => {
  if (type === 'followers') {
    goFollowList(0)
    return
  }
  if (type === 'following') {
    goFollowList(1)
  }
}

const onPostClick = (item) => {
  detailOpenPost(profile.value.userId, item.id)
}
</script>

<style scoped>
.user-detail-page {
  background: var(--page-bg);
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 32rpx;
  border-bottom: 1rpx solid var(--theme-border);
}

.header-left,
.header-right {
  width: 80rpx;
  display: flex;
  align-items: center;
}

.header-right {
  justify-content: flex-end;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.back-icon {
  font-size: 48rpx;
  color: var(--text-color);
  line-height: 1;
}

.username {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-color);
}

.icon {
  width: 44rpx;
  height: 44rpx;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 12rpx;
  padding: 0 32rpx 24rpx;
}

.btn {
  flex: 1;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16rpx;
  font-size: 24rpx;
  font-weight: 600;
}

.btn-primary {
  background: var(--btn-bg);
  color: var(--text-color);
}

.placeholder-panel {
  min-height: 240rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  color: var(--theme-secondary);
}

.detail-loading,
.detail-status {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40rpx 0;
  font-size: 24rpx;
  color: var(--theme-secondary);
}

.detail-loading {
  min-height: 400rpx;
  font-size: 28rpx;
}
</style>
