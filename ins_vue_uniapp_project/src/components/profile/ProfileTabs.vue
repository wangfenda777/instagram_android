<template>
  <view class="profile-tabs">
    <view
      v-for="tab in tabs"
      :key="tab.key"
      class="tab-item"
      :class="{ active: modelValue === tab.key }"
      @click="emit('update:modelValue', tab.key)"
    >
      <image class="tab-icon" :src="tab.icon" mode="aspectFit" />
    </view>
  </view>
</template>

<script setup>
defineProps({
  modelValue: {
    type: [String, Number],
    default: 0
  },
  tabs: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])
</script>

<style scoped>
.profile-tabs {
  display: flex;
  border-bottom: 1rpx solid var(--theme-border);
}

.tab-item {
  flex: 1;
  height: 100rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.tab-icon {
  width: 48rpx;
  height: 48rpx;
  opacity: 0.4;
}

.tab-item.active .tab-icon {
  opacity: 1;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 2rpx;
  background: var(--text-color);
}
</style>
