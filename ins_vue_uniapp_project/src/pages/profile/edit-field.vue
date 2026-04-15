<template>
  <view class="edit-field-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-arrow">‹</text>
      </view>
      <text class="nav-title">{{ label }}</text>
      <view class="nav-right" @click="handleDone">
        <text class="done-text" :class="{ disabled: saving }">完成</text>
      </view>
    </view>

    <!-- 输入区域 -->
    <view class="input-section">
      <textarea
        v-if="field === 'bio'"
        class="field-textarea"
        v-model="inputValue"
        :placeholder="'请输入' + label"
        :maxlength="150"
        auto-height
      />
      <input
        v-else
        class="field-input"
        v-model="inputValue"
        :placeholder="'请输入' + label"
        :focus="true"
      />
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/pinia/modules/userStore.js'
import { updateProfile } from '@/api/user.js'

const userStore = useUserStore()
const field = ref('')
const label = ref('')
const inputValue = ref('')
const saving = ref(false)

onLoad((query) => {
  field.value = query.field || ''
  label.value = decodeURIComponent(query.label || '')
  inputValue.value = decodeURIComponent(query.value || '')
})

const handleDone = async () => {
  if (saving.value) return
  saving.value = true
  try {
    const params = { [field.value]: inputValue.value }
    await updateProfile(params)
    // 更新 pinia
    userStore.setUserInfo({ ...userStore.userInfo, ...params })
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 500)
  } catch (e) {
    console.error('保存失败', e)
  } finally {
    saving.value = false
  }
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style scoped>
.edit-field-page {
  background: var(--page-bg);
  min-height: 100vh;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
  border-bottom: 1rpx solid var(--theme-border);
}

.nav-back {
  width: 60rpx;
  display: flex;
  align-items: center;
}

.back-arrow {
  font-size: 56rpx;
  color: var(--text-color);
  line-height: 1;
  font-weight: 300;
}

.nav-title {
  font-size: 32rpx;
  font-weight: 600;
  color: var(--text-color);
}

.nav-right {
  width: 100rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.done-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #0095f6;
}

.done-text.disabled {
  opacity: 0.5;
}

.input-section {
  padding: 32rpx;
}

.field-input {
  width: 100%;
  font-size: 32rpx;
  color: var(--text-color);
  border: none;
  border-bottom: 2rpx solid var(--text-color);
  padding-bottom: 16rpx;
  outline: none;
  background: transparent;
}

.field-textarea {
  width: 100%;
  font-size: 32rpx;
  color: var(--text-color);
  border: none;
  border-bottom: 2rpx solid var(--text-color);
  padding-bottom: 16rpx;
  outline: none;
  background: transparent;
  min-height: 120rpx;
}
</style>
