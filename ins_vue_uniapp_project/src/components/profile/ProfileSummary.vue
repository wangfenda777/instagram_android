<template>
  <view class="profile-summary">
    <view class="summary-main">
      <view class="avatar-section">
        <view class="avatar-wrapper">
          <image class="avatar" :src="avatar" mode="aspectFill" />
          <view v-if="showAvatarBadge" class="avatar-badge">
            <text class="avatar-badge-text">{{ avatarBadgeText }}</text>
          </view>
        </view>
      </view>

      <view class="stats-section">
        <view class="display-name-row">
          <text class="display-name">{{ displayName }}</text>
          <text v-if="isVerified" class="verified-badge">已认证</text>
        </view>
        <text v-if="bio" class="bio">{{ bio }}</text>
        <view class="stats">
          <view class="stat-item" @click="emitStatClick('posts')">
            <text class="stat-number">{{ posts }}</text>
            <text class="stat-label">帖子</text>
          </view>
          <view class="stat-item" @click="emitStatClick('followers')">
            <text class="stat-number">{{ followers }}</text>
            <text class="stat-label">粉丝</text>
          </view>
          <view class="stat-item" @click="emitStatClick('following')">
            <text class="stat-number">{{ following }}</text>
            <text class="stat-label">已关注</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
const props = defineProps({
  avatar: {
    type: String,
    default: '/static/images/avatar/1.jpg'
  },
  displayName: {
    type: String,
    default: ''
  },
  bio: {
    type: String,
    default: ''
  },
  isVerified: {
    type: Boolean,
    default: false
  },
  posts: {
    type: [String, Number],
    default: 0
  },
  followers: {
    type: [String, Number],
    default: 0
  },
  following: {
    type: [String, Number],
    default: 0
  },
  showAvatarBadge: {
    type: Boolean,
    default: false
  },
  avatarBadgeText: {
    type: String,
    default: '+'
  }
})

const emit = defineEmits(['stat-click'])

const emitStatClick = (type) => {
  emit('stat-click', type)
}
</script>

<style scoped>
.profile-summary {
  padding: 32rpx;
}

.summary-main {
  display: flex;
  gap: 40rpx;
}

.avatar-wrapper {
  position: relative;
  width: 180rpx;
  height: 180rpx;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
}

.avatar-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 48rpx;
  height: 48rpx;
  background: var(--theme-link);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 4rpx solid var(--page-bg);
}

.avatar-badge-text {
  color: #fff;
  font-size: 32rpx;
  font-weight: 300;
  line-height: 1;
}

.stats-section {
  flex: 1;
  padding-top: 20rpx;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.display-name-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex-wrap: wrap;
}

.display-name {
  font-size: 28rpx;
  font-weight: 600;
  color: var(--text-color);
}

.verified-badge {
  font-size: 20rpx;
  color: var(--theme-link);
}

.bio {
  font-size: 22rpx;
  color: var(--text-color);
  line-height: 1.5;
}

.stats {
  display: flex;
  gap: 32rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.stat-number {
  font-size: 30rpx;
  font-weight: 600;
  color: var(--text-color);
}

.stat-label {
  font-size: 18rpx;
  color: var(--text-color);
}
</style>
