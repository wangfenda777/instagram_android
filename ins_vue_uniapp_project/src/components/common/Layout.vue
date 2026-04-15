<template>
  <view class="layout" :style="{ paddingTop: topSafe + 'px', paddingBottom: bottomSafe + 'px' }">
    <slot />
  </view>
</template>

<script setup>
import { ref } from 'vue'

const topSafe = ref(0)
const bottomSafe = ref(0)

const sysInfo = uni.getSystemInfoSync()
topSafe.value = sysInfo.safeArea?.top || sysInfo.statusBarHeight || 0
bottomSafe.value = sysInfo.screenHeight - (sysInfo.safeArea?.bottom || sysInfo.screenHeight)
</script>

<style scoped>
.layout {
  min-height: 100vh;
  background-color: var(--page-bg);
  color: var(--text-color);
  box-sizing: border-box;
}
</style>
