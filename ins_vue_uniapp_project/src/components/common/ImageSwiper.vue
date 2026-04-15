<template>
  <view class="image-swiper">
    <swiper
      class="swiper"
      :current="current"
      @change="onChange"
      :indicator-dots="false"
      :autoplay="false"
      :circular="false"
    >
      <swiper-item v-for="(item, index) in normalizedList" :key="index">
        <video
          v-if="item.type === 'video'"
          class="swiper-video"
          :src="item.url"
          :controls="true"
          :show-center-play-btn="true"
          :enable-progress-gesture="true"
          object-fit="cover"
        />
        <image v-else class="swiper-image" :src="item.url" mode="aspectFill" />
      </swiper-item>
    </swiper>
    <!-- 指示点 -->
    <view class="dots" v-if="normalizedList.length > 1">
      <view
        class="dot"
        v-for="(item, index) in normalizedList"
        :key="index"
        :class="{ active: index === current }"
      />
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  images: {
    type: Array,
    default: () => []
  },
  mediaList: {
    type: Array,
    default: () => []
  }
})

// 优先使用 mediaList（带 type），兼容旧的纯 images 数组
const normalizedList = computed(() => {
  if (props.mediaList && props.mediaList.length) {
    return props.mediaList
  }
  return props.images.map(url => ({ url, type: 'image' }))
})

const current = ref(0)

function onChange(e) {
  current.value = e.detail.current
}
</script>

<style scoped>
.image-swiper {
  position: relative;
  width: 100%;
}

.swiper {
  width: 100%;
  height: 750rpx;
}

.swiper-image {
  width: 100%;
  height: 100%;
}

.swiper-video {
  width: 100%;
  height: 100%;
}

.dots {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16rpx 0;
  gap: 8rpx;
}

.dot {
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background-color: #c4c4c4;
  transition: background-color 0.3s;
}

.dot.active {
  background-color: var(--theme-link);
}
</style>
