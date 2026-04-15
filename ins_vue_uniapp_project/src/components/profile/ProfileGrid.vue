<template>
  <view class="profile-grid-wrap">
    <view v-if="items.length" class="posts-grid">
      <view class="post-item" v-for="item in items" :key="item.id" @click="$emit('item-click', item)">
        <image class="post-image" :src="item.cover" mode="aspectFill" />
        <image
          v-if="showMultiIcon && Number(item.mediaCount || 0) > 1"
          class="multi-icon"
          src="/static/icons/copy.svg"
          mode="aspectFit"
        />
        <image
          v-if="showVideoIcon || item.mediaType === 'video'"
          class="video-icon"
          src="/static/icons/video.svg"
          mode="aspectFit"
        />
      </view>
    </view>
    <view v-else class="empty-state">
      <text>{{ emptyText }}</text>
    </view>
  </view>
</template>

<script setup>
defineProps({
  items: {
    type: Array,
    default: () => []
  },
  emptyText: {
    type: String,
    default: '暂无内容'
  },
  showMultiIcon: {
    type: Boolean,
    default: false
  },
  showVideoIcon: {
    type: Boolean,
    default: false
  }
})

defineEmits(['item-click'])
</script>

<style scoped>
.profile-grid-wrap {
  padding: 4rpx 0;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 4rpx;
}

.post-item {
  aspect-ratio: 1;
  overflow: hidden;
  position: relative;
}

.post-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.multi-icon,
.video-icon {
  position: absolute;
  top: 12rpx;
  right: 12rpx;
  width: 28rpx;
  height: 28rpx;
}

.empty-state {
  min-height: 240rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  color: var(--theme-secondary);
}
</style>
