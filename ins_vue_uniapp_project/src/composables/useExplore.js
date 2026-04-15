import { ref, computed, onMounted } from 'vue'
import { searchTag, searchUser } from '@/api/search.js'
import { getExploreFeed } from '@/api/explore.js'
import { useAppStore } from '@/pinia/modules/appStore.js'
import { useUserStore } from '@/pinia/modules/userStore.js'

const SEARCH_DEBOUNCE_DELAY = 350
const EXPLORE_PAGE_SIZE = 24

export function useExplore() {
  const appStore = useAppStore()
  const userStore = useUserStore()
  const exploreList = ref([])
  const explorePage = ref(1)
  const exploreHasMore = ref(true)
  const exploreLoading = ref(false)

  const normalizeAsset = (url) => {
    if (!url) return ''
    return url.startsWith('http') ? url : appStore.baseUrl + url
  }

  const normalizeExploreItem = (item = {}) => ({
    id: item.postId || item.id || '',
    type: item.mediaType || 'image',
    cover: normalizeAsset(item.coverUrl || item.cover || ''),
    mediaCount: Number(item.mediaCount || 0)
  })

  const fetchExplore = async () => {
    if (exploreLoading.value || !exploreHasMore.value) return
    exploreLoading.value = true
    try {
      const data = await getExploreFeed(explorePage.value, EXPLORE_PAGE_SIZE)
      const rawList = getList(data)
      const list = rawList.map(normalizeExploreItem)
      exploreList.value.push(...list)
      exploreHasMore.value = Boolean(data?.hasMore)
      explorePage.value++
    } catch (e) {
      console.error('获取探索页失败', e)
    } finally {
      exploreLoading.value = false
    }
  }

  const searchKeyword = ref('')
  const isSearchMode = ref(false)
  const isSearching = ref(false)
  const hasSearched = ref(false)
  const searchError = ref('')
  const tagResults = ref([])
  const userResults = ref([])

  let searchTimer = null
  let activeRequestId = 0

  const getAvatarUrl = (avatar) => {
    if (!avatar) return '/static/images/avatar/1.jpg'
    return avatar.startsWith('http') ? avatar : appStore.baseUrl + avatar
  }

  const getList = (payload) => {
    if (Array.isArray(payload)) return payload
    if (Array.isArray(payload?.list)) return payload.list
    return []
  }

  const normalizeTag = (item = {}) => ({
    id: item.tagId || item.id || item.tagName || item.name || '',
    name: item.tagName || item.name || '',
    postCount: Number(item.postCount || item.count || item.noteCount || 0),
    cover: item.cover || item.coverUrl || item.thumbnail || ''
  })

  const normalizeUser = (item = {}) => ({
    id: item.userId || item.id || '',
    username: item.username || '',
    displayName: item.displayName || item.bio || '',
    avatar: getAvatarUrl(item.avatar),
    isVerified: Boolean(item.isVerified)
  })

  const clearSearchResult = () => {
    tagResults.value = []
    userResults.value = []
    searchError.value = ''
  }

  const runSearch = async () => {
    const keyword = searchKeyword.value.trim()
    const requestId = ++activeRequestId

    if (!keyword) {
      isSearching.value = false
      hasSearched.value = false
      clearSearchResult()
      return
    }

    isSearching.value = true
    searchError.value = ''
    hasSearched.value = true

    const [tagResult, userResult] = await Promise.allSettled([
      searchTag(keyword),
      searchUser(keyword)
    ])

    if (requestId !== activeRequestId) return

    const nextTags = tagResult.status === 'fulfilled'
      ? getList(tagResult.value).map(normalizeTag)
      : []
    const nextUsers = userResult.status === 'fulfilled'
      ? getList(userResult.value).map(normalizeUser)
      : []

    tagResults.value = nextTags
    userResults.value = nextUsers

    if (tagResult.status === 'rejected' && userResult.status === 'rejected') {
      searchError.value = '搜索失败，请稍后重试'
    }

    isSearching.value = false
  }

  const scheduleSearch = () => {
    if (searchTimer) {
      clearTimeout(searchTimer)
    }

    const keyword = searchKeyword.value.trim()
    if (!keyword) {
      activeRequestId += 1
      isSearching.value = false
      hasSearched.value = false
      clearSearchResult()
      return
    }

    searchTimer = setTimeout(() => {
      runSearch()
    }, SEARCH_DEBOUNCE_DELAY)
  }

  const enterSearchMode = () => {
    isSearchMode.value = true
  }

  const cancelSearch = () => {
    if (searchTimer) {
      clearTimeout(searchTimer)
      searchTimer = null
    }
    activeRequestId += 1
    searchKeyword.value = ''
    isSearchMode.value = false
    isSearching.value = false
    hasSearched.value = false
    clearSearchResult()
  }

  const handleSearchInput = () => {
    if (!isSearchMode.value) {
      enterSearchMode()
    }
    scheduleSearch()
  }

  const goSearchOverview = (tag) => {
    const keyword = String(tag?.name || tag?.tagName || '').trim()
    if (!keyword) return

    uni.navigateTo({
      url: `/pages/search-overview/search-overview?keyword=${encodeURIComponent(keyword)}&tab=recommend`
    })
  }

  const goUserDetail = (user) => {
    const targetUserId = String(user?.id || user?.userId || '')
    const currentUserId = String(userStore.userInfo?.userId || '')

    if (!targetUserId) return

    if (targetUserId === currentUserId) {
      uni.switchTab({ url: '/pages/profile/profile' })
      return
    }

    uni.navigateTo({
      url: `/pages/user-detail/user-detail?userId=${encodeURIComponent(targetUserId)}`
    })
  }

  // 瀑布流分列
  const column1 = computed(() => {
    const result = []
    const heights = [0, 0, 0]
    exploreList.value.forEach(item => {
      const minIndex = heights.indexOf(Math.min(...heights))
      if (minIndex === 0) result.push(item)
      heights[minIndex] += item.type === 'video' ? 2 : 1
    })
    return result
  })

  const column2 = computed(() => {
    const result = []
    const heights = [0, 0, 0]
    exploreList.value.forEach(item => {
      const minIndex = heights.indexOf(Math.min(...heights))
      if (minIndex === 1) result.push(item)
      heights[minIndex] += item.type === 'video' ? 2 : 1
    })
    return result
  })

  const column3 = computed(() => {
    const result = []
    const heights = [0, 0, 0]
    exploreList.value.forEach(item => {
      const minIndex = heights.indexOf(Math.min(...heights))
      if (minIndex === 2) result.push(item)
      heights[minIndex] += item.type === 'video' ? 2 : 1
    })
    return result
  })

  function handleSearch() {
    enterSearchMode()
    if (searchTimer) {
      clearTimeout(searchTimer)
      searchTimer = null
    }
    runSearch()
  }

  onMounted(() => {
    fetchExplore()
  })

  return {
    exploreList,
    exploreLoading,
    exploreHasMore,
    column1,
    column2,
    column3,
    fetchExplore,
    searchKeyword,
    isSearchMode,
    isSearching,
    hasSearched,
    searchError,
    tagResults,
    userResults,
    enterSearchMode,
    cancelSearch,
    handleSearchInput,
    handleSearch,
    goSearchOverview,
    goUserDetail
  }
}
