<template>
  <Layout>
    <view class="profile-page">
      <!-- 顶部导航 -->
      <view class="header">
        <view class="header-center">
          <text class="username">{{ userInfo.username }}</text>
          <image class="dropdown-icon" src="/static/icons/direction.svg" mode="aspectFit" />
          <view v-if="userInfo.hasNotification" class="notification-dot"></view>
        </view>
        <view class="header-right">
          <image class="icon" src="/static/icons/add.svg" mode="aspectFit" />
          <image class="icon" src="/static/icons/elip.svg" mode="aspectFit" />
        </view>
      </view>
      <!-- 头像和用户数据 -->
      <ProfileSummary
        :avatar="userInfo.avatar"
        :display-name="userInfo.displayName"
        :posts="userInfo.posts"
        :followers="userInfo.followers"
        :following="userInfo.following"
        :show-avatar-badge="true"
        avatar-badge-text="+"
        @stat-click="handleStatClick"
      />

      <view class="action-buttons">
        <view class="btn btn-primary" @click="goEditProfile">编辑主页</view>
        <view class="btn btn-primary">分享主页</view>
        <view class="btn btn-icon">
          <image src="/static/icons/direction.svg" mode="aspectFit" />
        </view>
      </view>

      <!-- 发现用户 -->
      <view class="discover-section">
        <view class="section-header">
          <text class="section-title">发现用户</text>
        </view>
        <scroll-view class="discover-scroll" scroll-x>
          <view class="discover-list">
            <view class="discover-item" v-for="user in discoverUsers" :key="user.id">
              <view class="discover-close" @click="removeDiscoverUser(user.id)">
                <image src="/static/icons/close.svg" mode="aspectFit" />
              </view>
              <image class="discover-avatar" :src="user.avatar" mode="aspectFill" />
              <text class="discover-username">{{ user.username }}</text>
              <text class="discover-tag">{{ user.tag }}</text>
              <view class="discover-btn" @click="followUser(user.id)">
                <text>{{ user.isFollowing ? '已关注' : '关注' }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>

      <ProfileTabs :model-value="activeTab" :tabs="profileTabs" @update:model-value="switchTab" />

      <ProfileGrid
        v-if="activeTab === 0"
        :items="normalizedUserPosts"
        empty-text="暂无帖子"
        :show-multi-icon="true"
        @item-click="onPostClick"
      />

      <ProfileGrid
        v-else-if="activeTab === 1"
        :items="normalizedUserVideos"
        empty-text="暂无 Reels"
        :show-video-icon="true"
      />
      <view v-if="activeTab === 1 && videoLoading" class="tab-status">
        <text>加载中...</text>
      </view>
      <view v-else-if="activeTab === 1 && !videoHasMore && normalizedUserVideos.length" class="tab-status">
        <text>没有更多了</text>
      </view>

      <ProfileGrid
        v-else
        :items="normalizedUserTagged"
        empty-text="暂无内容"
      />

      <!-- 完善主页 -->
      <view class="profile-tasks">
        <view class="tasks-header">
          <view class="tasks-title">完善主页</view>
          <view class="tasks-progress">{{ profileTasks.filter(t => t.completed).length }}/{{ profileTasks.length }}已完成</view>
        </view>
        <view class="progress-bar-wrap">
          <view class="progress-bar-fill" :style="{ width: (profileTasks.filter(t => t.completed).length / profileTasks.length * 100) + '%' }"></view>
        </view>
        <scroll-view class="tasks-scroll" scroll-x>
          <view class="tasks-list">
            <view
              class="task-card"
              :class="{ 'completed-card': task.completed }"
              v-for="task in profileTasks"
              :key="task.id"
            >
              <view class="task-icon-wrap" :class="{ done: task.completed }">
                <image :src="task.iconSrc" mode="aspectFit" />
                <view class="check-badge" v-if="task.completed">
                  <text class="check-text">✓</text>
                </view>
              </view>
              <text class="task-title">{{ task.title }}</text>
              <text class="task-desc">{{ task.description }}</text>
              <view class="task-btn">
                <text>{{ task.buttonText }}</text>
              </view>
            </view>
          </view>
        </scroll-view>
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
          @more-click="showMoreMenu"
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
import { computed } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import Layout from '@/components/common/Layout.vue'
import FullscreenPopup from '@/components/common/FullscreenPopup.vue'
import PostCard from '@/components/common/PostCard.vue'
import ProfileSummary from '@/components/profile/ProfileSummary.vue'
import ProfileTabs from '@/components/profile/ProfileTabs.vue'
import ProfileGrid from '@/components/profile/ProfileGrid.vue'
import { useProfile } from '@/composables/useProfile'
import { useUserPostsDetail } from '@/composables/useUserPostsDetail'

const profileTabs = [
  { key: 0, icon: '/static/icons/book.svg' },
  { key: 1, icon: '/static/icons/video.svg' },
  { key: 2, icon: '/static/icons/star.svg' }
]

const {
  userInfo,
  discoverUsers,
  activeTab,
  userPosts,
  userVideos,
  videoHasMore,
  videoLoading,
  fetchUserVideos,
  userTagged,
  profileTasks,
  followUser,
  removeDiscoverUser,
  switchTab
} = useProfile()

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
  goUserDetail: detailGoUserDetail,
  handleDeletePost: detailHandleDeletePost
} = useUserPostsDetail()

const showMoreMenu = (postId) => {
  uni.showActionSheet({
    itemList: ['删除帖子'],
    itemColor: '#ED4956',
    success: (res) => {
      if (res.tapIndex === 0) {
        detailHandleDeletePost(postId)
      }
    }
  })
}

const normalizedUserPosts = computed(() => userPosts.value.map(post => ({
  id: post.id,
  cover: post.image,
  mediaType: 'image',
  mediaCount: Number(post.mediaCount || 0)
})))

const normalizedUserVideos = computed(() => userVideos.value.map(post => ({
  id: post.id,
  cover: post.cover,
  mediaType: 'video',
  mediaCount: Number(post.mediaCount || 0)
})))

const normalizedUserTagged = computed(() => userTagged.value.map(post => ({
  id: post.id,
  cover: post.image,
  mediaType: 'image'
})))

const goEditProfile = () => {
  uni.navigateTo({ url: '/pages/profile/edit-profile' })
}

const goFollowList = (tab) => {
  const info = userInfo.value || {}
  const query = [
    `userId=${encodeURIComponent(info.userId || '')}`,
    `username=${encodeURIComponent(info.username || '')}`,
    `followers=${encodeURIComponent(info.followers || 0)}`,
    `following=${encodeURIComponent(info.following || 0)}`,
    `tab=${tab}`
  ].join('&')

  uni.navigateTo({
    url: `/pages/profile/follow-list?${query}`
  })
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
  detailOpenPost(userInfo.value.userId, item.id)
}

onReachBottom(() => {
  if (activeTab.value === 1) {
    fetchUserVideos()
  }
})
</script>

<style scoped>
.profile-page {
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

.header-center {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.username {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-color);
}

.dropdown-icon {
  width: 24rpx;
  height: 24rpx;
}

.notification-dot {
  position: absolute;
  top: -4rpx;
  right: -4rpx;
  width: 16rpx;
  height: 16rpx;
  background: var(--theme-error);
  border-radius: 50%;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 32rpx;
}

.header-right .icon {
  width: 48rpx;
  height: 48rpx;
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

.btn-icon {
  flex: 0 0 64rpx;
  background: var(--btn-bg);
}

.btn-icon image {
  width: 32rpx;
  height: 32rpx;
}

.discover-section {
  padding: 24rpx 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 32rpx 16rpx;
}

.section-title {
  font-size: 20rpx;
  font-weight: 600;
  color: var(--text-color);
}

.discover-scroll {
  white-space: nowrap;
  padding: 0 32rpx;
}

.discover-list {
  display: inline-flex;
  gap: 4rpx;
}

.discover-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 6rpx;
  width: 260rpx;
  border: 1rpx solid var(--theme-border);
  border-radius: 12rpx;
  padding: 24rpx;
  position: relative;
}

.discover-close {
  position: absolute;
  top: 8rpx;
  right: 8rpx;
  width: 32rpx;
  height: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.discover-close image {
  width: 20rpx;
  height: 20rpx;
}

.discover-avatar {
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  border: 2rpx solid var(--theme-border);
}

.discover-username {
  font-size: 24rpx;
  font-weight: 600;
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
  text-align: center;
}

.discover-tag {
  font-size: 16rpx;
  color: var(--text-secondary);
}

.discover-btn {
  width: 100%;
  height: 56rpx;
  background: var(--theme-link);
  border-radius: 16rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20rpx;
  font-weight: 600;
  color: #fff;
  margin-top: 28rpx;
}

.profile-tasks {
  padding: 32rpx;
  margin-top: 24rpx;
}

.tasks-header {
  margin-bottom: 24rpx;
}

.tasks-title {
  font-size: 20rpx;
  font-weight: 600;
  color: var(--text-color);
}

.tasks-progress {
  font-size: 16rpx;
  color: var(--text-secondary);
}

/* 进度条 */
.progress-bar-wrap {
  height: 4rpx;
  background: var(--theme-secondary);
  border-radius: 4rpx;
  margin-bottom: 32rpx;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background: var(--theme-link);
  border-radius: 4rpx;
  transition: width 0.3s;
}

/* 任务卡片横向滚动 */
.tasks-scroll {
  white-space: nowrap;
  overflow: hidden;
}

.tasks-list {
  display: inline-flex;
  gap: 16rpx;
  padding-bottom: 8rpx;
}

.task-card {
  display: inline-flex;
  flex-direction: column;
  width: 280rpx;
  border: 2rpx solid var(--theme-border);
  border-radius: 20rpx;
  padding: 32rpx 20rpx;
  gap: 16rpx;
  vertical-align: top;
  white-space: normal;
  flex-shrink: 0;
  box-sizing: border-box;
  align-items: center;
  text-align: center;
  justify-content: center;
  min-height: 280rpx;
}

.task-card.completed-card {
  opacity: 0.5;
}

.task-icon-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: 50%;
  background: var(--theme-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.task-icon-wrap.done {
  background: rgba(0,149,246,0.15);
}

.task-icon-wrap image {
  width: 48rpx;
  height: 48rpx;
}

.check-badge {
  position: absolute;
  bottom: -4rpx;
  right: -4rpx;
  width: 28rpx;
  height: 28rpx;
  background: var(--theme-link);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3rpx solid var(--page-bg);
}

.check-text {
  color: #fff;
  font-size: 20rpx;
  font-weight: 700;
  line-height: 1;
}

.task-title {
  font-size: 26rpx;
  font-weight: 600;
  color: var(--text-color);
  text-align: center;
}

.task-desc {
  font-size: 24rpx;
  color: var(--text-secondary);
  text-align: center;
}

.task-btn {
  display: block;
  padding: 8rpx 16rpx;
  background: var(--theme-link);
  border-radius: 12rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  color: #fff;
  margin-top: auto;
}

/* 故事横向滚动（tab=0时底部） */
.stories-scroll {
  white-space: nowrap;
  padding: 16rpx 0;
  border-bottom: 1rpx solid var(--theme-border);
}

.stories-list {
  display: inline-flex;
  gap: 0;
  padding: 0 8rpx;
}

.story-thumb {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  width: 120rpx;
}

.story-thumb-img {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  border: 2rpx solid var(--theme-border);
}

.story-thumb-label {
  font-size: 22rpx;
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
  text-align: center;
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

.tab-status {
  display: flex;
  justify-content: center;
  padding: 40rpx 0;
  font-size: 24rpx;
  color: var(--theme-secondary);
}
</style>