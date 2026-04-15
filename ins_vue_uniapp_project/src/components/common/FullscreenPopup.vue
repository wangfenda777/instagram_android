<template>
  <view v-if="visible" class="fullscreen-popup" @touchmove.stop.prevent @click.self="close">
    <view
      class="fullscreen-popup-content"
      :class="{ 'popup-enter': animating, 'popup-leave': leaving }"
    >
      <view class="popup-header">
        <view class="popup-back" @click="close">
          <text class="popup-back-icon">‹</text>
        </view>
        <view class="popup-drag-bar" />
        <view class="popup-header-right" />
      </view>
      <scroll-view
        class="popup-body"
        scroll-y
        :upper-threshold="80"
        :lower-threshold="160"
        @scrolltoupper="$emit('scroll-top')"
        @scrolltolower="$emit('scroll-bottom')"
      >
        <slot />
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  show: { type: Boolean, default: false }
})

const emit = defineEmits(['update:show', 'scroll-top', 'scroll-bottom'])

const visible = ref(false)
const animating = ref(false)
const leaving = ref(false)

watch(() => props.show, (val) => {
  if (val) {
    visible.value = true
    document.body.style.overflow = 'hidden'
    setTimeout(() => { animating.value = true }, 20)
  } else {
    leaving.value = true
    animating.value = false
    setTimeout(() => {
      visible.value = false
      leaving.value = false
      document.body.style.overflow = ''
    }, 300)
  }
})

const close = () => {
  emit('update:show', false)
}
</script>

<style scoped>
.fullscreen-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
  background: rgba(0, 0, 0, 0.5);
}

.fullscreen-popup-content {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 100%;
  background: var(--page-bg, #000);
  border-radius: 24rpx 24rpx 0 0;
  transform: translateY(100%);
  transition: transform 0.3s ease-out;
  display: flex;
  flex-direction: column;
}

.popup-enter {
  transform: translateY(0);
}

.popup-leave {
  transform: translateY(100%);
  transition: transform 0.3s ease-in;
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16rpx 24rpx 8rpx;
  flex-shrink: 0;
}

.popup-back {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.popup-back-icon {
  font-size: 56rpx;
  color: var(--text-color, #fff);
  line-height: 1;
}

.popup-drag-bar {
  width: 80rpx;
  height: 8rpx;
  border-radius: 4rpx;
  background: var(--theme-secondary, #666);
}

.popup-header-right {
  width: 56rpx;
  height: 56rpx;
}

.popup-body {
  flex: 1;
  overflow: hidden;
}
</style>
