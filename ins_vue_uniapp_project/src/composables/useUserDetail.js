import { computed, onMounted, ref, watch } from 'vue'
import { getUserInfo, getUserStats, getUserPosts, getUserReels, followUser as followUserApi, unfollowUser as unfollowUserApi } from '@/api/user.js'
import { useAppStore } from '@/pinia/modules/appStore.js'
import { useUserStore } from '@/pinia/modules/userStore.js'

const TABS = [
  { key: 0, icon: '/static/icons/book.svg' },
  { key: 1, icon: '/static/icons/video.svg' },
  { key: 2, icon: '/static/icons/star.svg' }
]

const EMPTY_LIST = []

const getList = (payload) => {
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.list)) return payload.list
  return EMPTY_LIST
}

export function useUserDetail() {
  const appStore = useAppStore()
  const userStore = useUserStore()
  const activeTab = ref(0)
  const loading = ref(false)
  const currentUserId = computed(() => String(userStore.userInfo?.userId || ''))
  const routeUserId = computed(() => String(getRouteUserId() || ''))
  const isSelf = computed(() => Boolean(routeUserId.value) && routeUserId.value === currentUserId.value)

  const profile = ref({
    userId: '',
    username: '',
    displayName: '',
    avatar: '/static/images/avatar/1.jpg',
    bio: '',
    isVerified: false,
    isPrivate: false,
    posts: 0,
    followers: 0,
    following: 0,
    isFollowing: false
  })

  const postItems = ref([])
  const reelItems = ref([])
  const taggedItems = ref([])

  const summary = computed(() => ({
    avatar: profile.value.avatar,
    displayName: profile.value.displayName || profile.value.username,
    bio: profile.value.bio,
    isVerified: profile.value.isVerified,
    posts: profile.value.posts,
    followers: profile.value.followers,
    following: profile.value.following
  }))

  const actionButtons = computed(() => {
    if (isSelf.value) {
      return [
        { key: 'edit', text: '编辑主页' },
        { key: 'share', text: '分享主页' }
      ]
    }

    return [
      { key: 'follow', text: profile.value.isFollowing ? '已关注' : '关注' },
      { key: 'message', text: '发消息' }
    ]
  })

  const normalizeAsset = (url) => {
    if (!url) return '/static/images/avatar/1.jpg'
    return url.startsWith('http') ? url : appStore.baseUrl + url
  }

  const normalizeGridItem = (item = {}, type = 'image') => ({
    id: item.postId || item.id || '',
    cover: normalizeAsset(item.coverUrl || item.cover || item.image || ''),
    mediaType: item.mediaType || type,
    mediaCount: Number(item.mediaCount || 0),
    duration: Number(item.duration || 0),
    viewsCount: Number(item.viewsCount || 0)
  })

  const fetchProfileBase = async () => {
    const userId = routeUserId.value
    if (!userId) return

    const [info, stats] = await Promise.all([
      getUserInfo(userId),
      getUserStats(userId)
    ])

    profile.value = {
      ...profile.value,
      userId: String(info?.userId || userId),
      username: info?.username || '',
      displayName: info?.displayName || '',
      avatar: normalizeAsset(info?.avatar),
      bio: info?.bio || '',
      isVerified: Boolean(info?.isVerified),
      isPrivate: Boolean(info?.isPrivate),
      posts: Number(stats?.postsCount || 0),
      followers: Number(stats?.followersCount || 0),
      following: Number(stats?.followingCount || 0),
      isFollowing: Boolean(info?.isFollowing)
    }
  }

  const fetchTabData = async (tab = activeTab.value) => {
    const userId = routeUserId.value
    if (!userId) return

    if (tab === 0) {
      const data = await getUserPosts(userId)
      postItems.value = getList(data).map(item => normalizeGridItem(item, 'image'))
      return
    }

    if (tab === 1) {
      const data = await getUserReels(userId)
      reelItems.value = getList(data).map(item => normalizeGridItem(item, 'video'))
      return
    }

    taggedItems.value = []
  }

  const initialize = async () => {
    loading.value = true
    try {
      await fetchProfileBase()
      await fetchTabData(0)
    } finally {
      loading.value = false
    }
  }

  const switchTab = async (tab) => {
    if (tab === 0 && postItems.value.length) {
      activeTab.value = tab
      return
    }
    if (tab === 1 && reelItems.value.length) {
      activeTab.value = tab
      return
    }
    if (tab === 2) {
      activeTab.value = tab
      taggedItems.value = []
      return
    }

    activeTab.value = tab
    loading.value = true
    try {
      await fetchTabData(tab)
    } finally {
      loading.value = false
    }
  }

  const toggleFollow = async () => {
    if (!profile.value.userId || isSelf.value) return

    if (profile.value.isFollowing) {
      await unfollowUserApi({ userId: profile.value.userId })
      profile.value.isFollowing = false
      profile.value.followers = Math.max(0, Number(profile.value.followers || 0) - 1)
      return
    }

    await followUserApi({ userId: profile.value.userId })
    profile.value.isFollowing = true
    profile.value.followers = Number(profile.value.followers || 0) + 1
  }

  const handlePrimaryAction = async (key) => {
    if (key === 'follow') {
      await toggleFollow()
      return
    }

    if (key === 'edit') {
      uni.navigateTo({ url: '/pages/profile/edit-profile' })
    }
  }

  const goFollowList = (tab) => {
    const info = profile.value || {}
    const query = [
      `userId=${encodeURIComponent(info.userId || '')}`,
      `username=${encodeURIComponent(info.username || '')}`,
      `followers=${encodeURIComponent(info.followers || 0)}`,
      `following=${encodeURIComponent(info.following || 0)}`,
      `tab=${tab}`
    ].join('&')

    uni.navigateTo({
      url: `/pages/profile/follow-list?${query}`
    })
  }

  onMounted(() => {
    initialize()
  })

  return {
    tabs: TABS,
    activeTab,
    loading,
    isSelf,
    profile,
    summary,
    actionButtons,
    postItems,
    reelItems,
    taggedItems,
    switchTab,
    handlePrimaryAction,
    goFollowList
  }
}

function getRouteUserId() {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  return currentPage?.options?.userId || ''
}
