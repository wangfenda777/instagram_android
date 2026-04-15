<template>
  <view class="login-page">
    <view class="logo-section">
      <image class="logo" src="/static/icons/instagram.svg" mode="aspectFit" />
    </view>

    <view class="form-section">
      <input 
        class="input" 
        type="text" 
        v-model="username" 
        placeholder="电话、邮件或用户名" 
      />
      <input 
        class="input" 
        :type="showPassword ? 'text' : 'password'" 
        v-model="password" 
        placeholder="密码" 
      />
      <view class="login-btn" :class="{ active: canLogin }" @click="handleLogin">
        <text>登录</text>
      </view>
    </view>

    <view class="forgot-section">
      <text class="forgot-link">忘记密码？</text>
    </view>

    <view class="facebook-section">
      <image class="facebook-icon" src="/static/icons/facebook.svg" mode="aspectFit" />
      <text class="facebook-text">用 Facebook 账号登录</text>
    </view>

    <view class="divider">
      <view class="divider-line"></view>
      <text class="divider-text">或</text>
      <view class="divider-line"></view>
    </view>

    <view class="signup-section">
      <text class="signup-text">创建新账户</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useLogin } from '@/composables/useLogin'

const username = ref('')
const password = ref('')
const showPassword = ref(false)

const { login } = useLogin()

const canLogin = computed(() => {
  return username.value.trim() && password.value.trim()
})

const handleLogin = () => {
  if (canLogin.value) {
    // login(username.value, password.value)
    login(username.value, password.value)

  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60rpx 40rpx;
}

.logo-section {
  margin-bottom: 60rpx;
}

.logo {
  width: 240rpx;
  height: 80rpx;
}

.form-section {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.input {
  width: 100%;
  height: 90rpx;
  background: #fafafa;
  border: 2rpx solid #dbdbdb;
  border-radius: 10rpx;
  padding: 0 24rpx;
  font-size: 28rpx;
}

.login-btn {
  width: 100%;
  height: 90rpx;
  background: #0095f6;
  border-radius: 10rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 16rpx;
}

.login-btn text {
  color: #fff;
  font-size: 32rpx;
  font-weight: 600;
}

.login-btn.active {
  opacity: 1;
}

.login-btn:not(.active) {
  opacity: 0.3;
}

.forgot-section {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-top: 40rpx;
}

.forgot-link {
  font-size: 26rpx;
  color: #0095f6;
}

.facebook-section {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  margin-top: 50rpx;
}

.facebook-icon {
  width: 32rpx;
  height: 32rpx;
}

.facebook-text {
  font-size: 28rpx;
  color: #385185;
  font-weight: 600;
}

.divider {
  display: flex;
  align-items: center;
  width: 100%;
  margin-top: 50rpx;
  gap: 20rpx;
}

.divider-line {
  flex: 1;
  height: 2rpx;
  background: #dbdbdb;
}

.divider-text {
  font-size: 26rpx;
  color: #8e8e8e;
}

.signup-section {
  margin-top: 50rpx;
}

.signup-text {
  font-size: 28rpx;
  color: #0095f6;
  font-weight: 600;
}
</style>