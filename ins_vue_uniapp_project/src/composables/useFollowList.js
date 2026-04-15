import { computed, ref } from 'vue'
import { getUserFollowers, getUserFollowing } from '@/api/user.js'
import { useAppStore } from '@/pinia/modules/appStore.js'

const PAGE_SIZE = 20

const createTabState = (count = 0) => ({
  count,
  list: [],
  page: 1,
  pageSize: PAGE_SIZE,
  total: count,
  hasMore: true,
  loading: false,
  loaded: false
})

export function useFollowList() {
  const appStore = useAppStore()

  const userId = ref('')
  const username = ref('')
  const activeTab = ref(0)

  const followers = ref(createTabState())
  const following = ref(createTabState())

  const currentTabState = computed(() => (activeTab.value === 0 ? followers.value : following.value))
  const currentList = computed(() => currentTabState.value.list)
  const currentTabKey = computed(() => (activeTab.value === 0 ? 'followers' : 'following'))

  const getAvatarUrl = (avatar) => {
    if (!avatar) return '/static/images/avatar/1.jpg'
    return avatar.startsWith('http') ? avatar : appStore.baseUrl + avatar
  }

  const normalizeUser = (item = {}) => ({
    id: item.userId || '',
    username: item.username || '',
    displayName: item.displayName || '',
    avatar: getAvatarUrl(item.avatar),
    isVerified: Boolean(item.isVerified),
    isFollowing: Boolean(item.isFollowing)
  })

  const getTabState = (tabIndex) => (tabIndex === 0 ? followers.value : following.value)

  const setTabCount = (tabIndex, count) => {
    const tabState = getTabState(tabIndex)
    const nextCount = Number(count) || 0
    tabState.count = nextCount
    tabState.total = tabState.loaded ? tabState.total : nextCount
  }

  const getTabText = (tabIndex) => {
    const tabState = getTabState(tabIndex)
    return tabIndex === 0 ? `${tabState.count}粉丝` : `${tabState.count}关注`
  }

  const loadTab = async (tabIndex, nextPage = 1) => {
    const tabState = getTabState(tabIndex)
    if (tabState.loading) return
    if (nextPage !== 1 && !tabState.hasMore) return
    if (!userId.value) return

    tabState.loading = true
    try {
      const api = tabIndex === 0 ? getUserFollowers : getUserFollowing
      const data = await api(userId.value, nextPage, PAGE_SIZE)
      const list = Array.isArray(data.list) ? data.list.map(normalizeUser) : []
      const total = Number(data.total)

      tabState.list = nextPage === 1 ? list : tabState.list.concat(list)
      tabState.page = Number(data.page) || nextPage
      tabState.pageSize = Number(data.pageSize) || PAGE_SIZE
      tabState.total = Number.isNaN(total) ? tabState.total : total
      tabState.count = Number.isNaN(total) ? tabState.count : total
      tabState.hasMore = typeof data.hasMore === 'boolean' ? data.hasMore : false
      tabState.loaded = true
    } finally {
      tabState.loading = false
    }
  }

  const loadInitialTab = async () => {
    await loadTab(activeTab.value, 1)
  }

  const switchTab = async (tabIndex) => {
    if (activeTab.value === tabIndex) return
    activeTab.value = tabIndex
    const tabState = getTabState(tabIndex)
    if (!tabState.loaded) {
      await loadTab(tabIndex, 1)
    }
  }

  const loadMore = async () => {
    const tabState = currentTabState.value
    if (tabState.loading || !tabState.hasMore) return
    await loadTab(activeTab.value, tabState.page + 1)
  }

  const initPage = async (query = {}) => {
    userId.value = query.userId || ''
    username.value = decodeURIComponent(query.username || '')
    activeTab.value = Number(query.tab) === 1 ? 1 : 0
    followers.value = createTabState(Number(query.followers) || 0)
    following.value = createTabState(Number(query.following) || 0)
    await loadInitialTab()
  }

  return {
    username,
    activeTab,
    followers,
    following,
    currentList,
    currentTabKey,
    currentTabState,
    getTabText,
    initPage,
    switchTab,
    loadMore,
    setTabCount
  }
}
