import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const token = ref('')
  const refreshToken = ref('')
  const expiresIn = ref(0)

  const setUserInfo = (info) => {
    userInfo.value = info
  }

  const setAuth = (data) => {
    token.value = data.token
    refreshToken.value = data.refreshToken
    expiresIn.value = data.expiresIn
  }

  const clearAuth = () => {
    userInfo.value = null
    token.value = ''
    refreshToken.value = ''
    expiresIn.value = 0
  }

  return {
    userInfo,
    token,
    refreshToken,
    expiresIn,
    setUserInfo,
    setAuth,
    clearAuth
  }
}, {
  persist: true
})
