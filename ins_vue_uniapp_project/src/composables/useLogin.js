import { ref } from 'vue'
import { login as loginApi } from '@/api/auth.js'
import { getUserMe } from '@/api/user.js'
import { useUserStore } from '@/pinia/modules/userStore.js'
import { useAppStore } from '@/pinia/modules/appStore.js'

export const useLogin = () => {
  const isLoggedIn = ref(false)

  const login = async (account, password) => {
    if (account && password) {
      try {
        const res = await loginApi({ account, password })
        const userStore = useUserStore()
        userStore.setAuth(res)
        isLoggedIn.value = true

        // 登录成功后获取用户信息
        try {
          const appStore = useAppStore()
          const userInfo = await getUserMe()
          // 头像拼接服务器地址
          if (userInfo.avatar) {
            userInfo.avatar = appStore.baseUrl + userInfo.avatar
          }
          userStore.setUserInfo(userInfo)
        } catch (e) {
          console.error('获取用户信息失败', e)
        }

        uni.switchTab({
          url: '/pages/index/index'
        })
      } catch (e) {
        // 接口封装层已有 toast 提示
      }
    }
  }

  const logout = () => {
    const userStore = useUserStore()
    userStore.clearAuth()
    isLoggedIn.value = false
  }

  const checkLogin = () => {
    const userStore = useUserStore()
    if (userStore.token) {
      isLoggedIn.value = true
      return true
    }
    return false
  }

  return {
    isLoggedIn,
    login,
    logout,
    checkLogin
  }
}
