import { computed, reactive, ref } from 'vue'
import { searchPost, searchTag, searchUser } from '@/api/search.js'
import { useAppStore } from '@/pinia/modules/appStore.js'

const DEFAULT_TAB = 'recommend'
const PAGE_SIZE = 20
const TAB_KEYS = {
  recommend: 'recommend',
  account: 'account',
  tag: 'tag'
}

const createTabState = () => ({
  list: [],
  page: 1,
  pageSize: PAGE_SIZE,
  loading: false,
  hasMore: true,
  initialized: false,
  error: ''
})

const getList = (payload) => {
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.list)) return payload.list
  return []
}

export function useSearchOverview() {
  const appStore = useAppStore()
  const keyword = ref(decodeURIComponent(getRouteOptions().keyword || '').trim())
  const activeTab = ref(normalizeTab(getRouteOptions().tab))
  const tabs = [
    { key: TAB_KEYS.recommend, label: '为你推荐' },
    { key: TAB_KEYS.account, label: '账户' },
    { key: TAB_KEYS.tag, label: '标签' }
  ]

  const tabStates = reactive({
    [TAB_KEYS.recommend]: createTabState(),
    [TAB_KEYS.account]: createTabState(),
    [TAB_KEYS.tag]: createTabState()
  })

  const currentState = computed(() => tabStates[activeTab.value])

  const normalizeAsset = (url, fallback = '/static/images/avatar/1.jpg') => {
    if (!url) return fallback
    return url.startsWith('http') ? url : appStore.baseUrl + url
  }

  const normalizePost = (item = {}) => ({
    id: item.postId || item.id || '',
    cover: normalizeAsset(item.coverUrl || item.cover || item.image, ''),
    mediaType: item.mediaType || 'image',
    mediaCount: Number(item.mediaCount || 0)
  })

  const normalizeUser = (item = {}) => ({
    id: item.userId || item.id || '',
    username: item.username || '',
    displayName: item.displayName || '',
    avatar: normalizeAsset(item.avatar),
    isVerified: Boolean(item.isVerified),
    followersCount: Number(item.followersCount || 0)
  })

  const normalizeTag = (item = {}) => ({
    id: item.tagId || item.id || item.name || item.tagName || '',
    name: item.tagName || item.name || '',
    postCount: Number(item.postCount || item.count || 0),
    cover: normalizeAsset(item.coverUrl || item.cover || item.thumbnail || '', '')
  })

  const getFetcher = (tab) => {
    if (tab === TAB_KEYS.recommend) {
      return (page, pageSize) => searchPost(keyword.value, 'all', page, pageSize)
    }

    if (tab === TAB_KEYS.account) {
      return (page, pageSize) => searchUser(keyword.value, page, pageSize)
    }

    return (page, pageSize) => searchTag(keyword.value, page, pageSize)
  }

  const getNormalizer = (tab) => {
    if (tab === TAB_KEYS.recommend) return normalizePost
    if (tab === TAB_KEYS.account) return normalizeUser
    return normalizeTag
  }

  const loadTab = async (tab, reset = false) => {
    const state = tabStates[tab]
    if (!keyword.value) return
    if (state.loading) return
    if (!reset && !state.hasMore) return

    if (reset) {
      state.page = 1
      state.hasMore = true
      state.error = ''
      state.list = []
    }

    state.loading = true
    state.error = ''

    try {
      const payload = await getFetcher(tab)(state.page, state.pageSize)
      const normalized = getList(payload).map(getNormalizer(tab))
      state.list = reset ? normalized : [...state.list, ...normalized]
      state.initialized = true
      state.hasMore = normalized.length >= state.pageSize && payload?.hasMore !== false
      state.page += 1
    } catch (error) {
      state.error = '加载失败，请稍后重试'
    } finally {
      state.loading = false
    }
  }

  const switchTab = async (tab) => {
    activeTab.value = normalizeTab(tab)
    const state = tabStates[activeTab.value]
    if (!state.initialized) {
      await loadTab(activeTab.value, true)
    }
  }

  const loadMore = async () => {
    await loadTab(activeTab.value)
  }

  const refreshCurrentTab = async () => {
    await loadTab(activeTab.value, true)
  }

  const initialize = async () => {
    await loadTab(activeTab.value, true)
  }

  return {
    keyword,
    tabs,
    activeTab,
    currentState,
    recommendList: computed(() => tabStates[TAB_KEYS.recommend].list),
    accountList: computed(() => tabStates[TAB_KEYS.account].list),
    tagList: computed(() => tabStates[TAB_KEYS.tag].list),
    initialize,
    switchTab,
    loadMore,
    refreshCurrentTab
  }
}

function getRouteOptions() {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  return currentPage?.options || {}
}

function normalizeTab(tab) {
  if (tab === TAB_KEYS.account || tab === TAB_KEYS.tag || tab === TAB_KEYS.recommend) {
    return tab
  }
  return DEFAULT_TAB
}
