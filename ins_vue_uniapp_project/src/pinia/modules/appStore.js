import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const baseUrl = ref('http://112.124.47.169:8081')

  return {
    baseUrl
  }
}, {
  persist: true
})
