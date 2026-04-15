<template>
  <view class="edit-profile-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <text class="back-arrow">‹</text>
      </view>
      <text class="nav-title">编辑主页</text>
      <view class="nav-right"></view>
    </view>

    <!-- 头像区域 -->
    <view class="avatar-section">
      <view class="avatar-row">
        <view class="avatar-circle" @click="chooseAvatar">
          <image class="avatar-img" :src="previewAvatar" mode="aspectFill" />
        </view>
        <view class="avatar-circle avatar-placeholder">
          <image class="emoji-icon" src="/static/icons/emoji.svg" mode="aspectFit" />
        </view>
      </view>
      <text class="edit-avatar-text" @click="chooseAvatar">编辑头像或虚拟形象</text>
    </view>

    <!-- 表单区域 -->
    <view class="form-section">
      <view class="form-item" @click="goEditField('displayName', '姓名', form.displayName)">
        <text class="form-label">姓名</text>
        <text class="form-value" :class="{ placeholder: !form.displayName }">{{ form.displayName || '姓名' }}</text>
      </view>
      <view class="form-item" @click="goEditField('username', '帐号', form.username)">
        <text class="form-label">帐号</text>
        <text class="form-value" :class="{ placeholder: !form.username }">{{ form.username || '帐号' }}</text>
      </view>
      <view class="form-item" @click="goEditField('pronouns', '人称代词', form.pronouns)">
        <text class="form-label">人称代词</text>
        <text class="form-value" :class="{ placeholder: !form.pronouns }">{{ form.pronouns || '人称代词' }}</text>
      </view>
      <view class="form-item" @click="goEditField('bio', '个性签名', form.bio)">
        <text class="form-label">个性签名</text>
        <text class="form-value" :class="{ placeholder: !form.bio }">{{ form.bio || '个性签名' }}</text>
      </view>
      <view class="form-item form-item-arrow">
        <text class="form-label">链接</text>
        <view class="form-value-row">
          <text class="form-value placeholder">添加链接</text>
          <text class="arrow">›</text>
        </view>
      </view>
      <view class="form-item form-item-arrow last">
        <text class="form-label">横幅</text>
        <view class="form-value-row">
          <text class="form-value placeholder">添加横幅</text>
          <text class="arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 性别 -->
    <view class="form-section">
      <view class="form-item form-item-arrow last">
        <text class="form-label">性别</text>
        <view class="form-value-row">
          <text class="form-value">{{ form.gender || '男' }}</text>
          <text class="arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 底部链接 -->
    <view class="links-section">
      <view class="link-item">
        <text class="link-text">切换为专业帐户</text>
      </view>
      <view class="link-item">
        <text class="link-text">个人信息设置</text>
      </view>
      <view class="link-item">
        <text class="link-text">展示你的主页已获得认证</text>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-section">
      <view class="logout-btn" @click="handleLogout">
        <text class="logout-text">退出登录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useUserStore } from '@/pinia/modules/userStore.js'
import { useAppStore } from '@/pinia/modules/appStore.js'
import { updateProfile } from '@/api/user.js'
import { uploadImage } from '@/api/upload.js'

const userStore = useUserStore()
const appStore = useAppStore()

const avatarUrl = computed(() => {
  if (userStore.userInfo?.avatar) {
    const av = userStore.userInfo.avatar
    return av.startsWith('http') ? av : appStore.baseUrl + av
  }
  return '/static/images/avatar/1.jpg'
})

const previewAvatar = ref(avatarUrl.value)

const form = reactive({
  displayName: userStore.userInfo?.displayName || '',
  username: userStore.userInfo?.username || '',
  pronouns: userStore.userInfo?.pronouns || '',
  bio: userStore.userInfo?.bio || '',
  gender: userStore.userInfo?.gender || '男'
})

// 选择头像 → 上传 → 立即调接口保存
const chooseAvatar = () => {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempPath = res.tempFilePaths[0]
      previewAvatar.value = tempPath
      try {
        const file = await getFileFromPath(tempPath)
        const data = await uploadImage(file)
        const avatarPath = data.url || ''
        // 立即调用更新接口保存头像
        await updateProfile({ avatar: avatarPath })
        // 更新 pinia
        userStore.setUserInfo({ ...userStore.userInfo, avatar: avatarPath })
        uni.showToast({ title: '头像已更新', icon: 'success' })
      } catch (e) {
        console.error('头像上传失败', e)
        previewAvatar.value = avatarUrl.value
      }
    }
  })
}

// H5 环境：将 blob URL 转为 File 对象
const getFileFromPath = (path) => {
  return new Promise((resolve, reject) => {
    if (path.startsWith('blob:')) {
      fetch(path)
        .then(res => res.blob())
        .then(blob => {
          const file = new File([blob], 'avatar.jpg', { type: 'image/jpeg' })
          resolve(file)
        })
        .catch(reject)
    } else {
      reject(new Error('Unsupported path format'))
    }
  })
}

// 跳转到编辑字段子页面
const goEditField = (field, label, value) => {
  uni.navigateTo({
    url: `/pages/profile/edit-field?field=${field}&label=${encodeURIComponent(label)}&value=${encodeURIComponent(value || '')}`
  })
}

// 从编辑子页面返回时刷新数据
const refreshForm = () => {
  const info = userStore.userInfo || {}
  form.displayName = info.displayName || ''
  form.username = info.username || ''
  form.pronouns = info.pronouns || ''
  form.bio = info.bio || ''
  form.gender = info.gender || '男'
  // 刷新头像
  previewAvatar.value = avatarUrl.value
}

// 页面显示时刷新（从子页面返回）
uni.$on('profileUpdated', refreshForm)

import { onShow, onUnload } from '@dcloudio/uni-app'
onShow(() => {
  refreshForm()
})
onUnload(() => {
  uni.$off('profileUpdated', refreshForm)
})

const goBack = () => {
  uni.navigateBack()
}

const handleLogout = () => {
  userStore.clearAuth()
  userStore.setUserInfo({})
  uni.reLaunch({ url: '/pages/login/login' })
}
</script>

<style scoped>
.edit-profile-page {
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
  width: 60rpx;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40rpx 0 24rpx;
}

.avatar-row {
  display: flex;
  align-items: center;
  gap: 32rpx;
}

.avatar-circle {
  width: 200rpx;
  height: 200rpx;
  border-radius: 50%;
  overflow: hidden;
  border: 2rpx solid var(--theme-border);
}

.avatar-img {
  width: 100%;
  height: 100%;
}

.avatar-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--page-bg);
}

.emoji-icon {
  width: 80rpx;
  height: 80rpx;
  opacity: 0.4;
}

.edit-avatar-text {
  margin-top: 20rpx;
  font-size: 26rpx;
  color: #0095f6;
  font-weight: 600;
}

.form-section {
  padding: 0 32rpx;
  border-top: 1rpx solid var(--theme-border);
}

.form-item {
  display: flex;
  align-items: center;
  min-height: 96rpx;
  border-bottom: 1rpx solid var(--theme-border);
}

.form-item.last {
  border-bottom: none;
}

.form-label {
  width: 160rpx;
  font-size: 28rpx;
  color: var(--text-color);
  flex-shrink: 0;
}

.form-value {
  flex: 1;
  font-size: 28rpx;
  color: var(--text-color);
}

.form-value.placeholder {
  color: #c7c7cc;
}

.form-item-arrow {
  cursor: pointer;
}

.form-value-row {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.arrow {
  font-size: 40rpx;
  color: #c7c7cc;
  line-height: 1;
}

.links-section {
  padding: 0 32rpx;
  border-top: 1rpx solid var(--theme-border);
}

.link-item {
  min-height: 96rpx;
  display: flex;
  align-items: center;
  border-bottom: 1rpx solid var(--theme-border);
}

.link-item:last-child {
  border-bottom: none;
}

.link-text {
  font-size: 28rpx;
  color: #0095f6;
}

.logout-section {
  padding: 48rpx 32rpx;
}

.logout-btn {
  width: 100%;
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1rpx solid #ed4956;
  border-radius: 16rpx;
}

.logout-text {
  font-size: 28rpx;
  font-weight: 600;
  color: #ed4956;
}
</style>
