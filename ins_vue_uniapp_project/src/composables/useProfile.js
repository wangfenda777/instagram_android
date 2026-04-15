import { ref, onMounted } from 'vue'
import { getUserStats, getDiscoverUsers, getUserPosts, followUser as followUserApi, unfollowUser as unfollowUserApi } from '@/api/user.js'
import { useUserStore } from '@/pinia/modules/userStore.js'
import { useAppStore } from '@/pinia/modules/appStore.js'

export function useProfile() {
  const userStore = useUserStore()
  const appStore = useAppStore()

  // 用户信息（从 pinia 读取）
  const userInfo = ref({
    username: '',
    displayName: '',
    avatar: '',
    posts: 0,
    followers: 0,
    following: 0,
    hasNotification: false
  })

  // 发现用户列表
  const discoverUsers = ref([])

  // 当前选中的tab
  const activeTab = ref(0)

  // 用户帖子（图片网格）
  const userPosts = ref([])

  // 用户视频（Reels）
  const userVideos = ref([])
  const videoPage = ref(1)
  const videoHasMore = ref(true)
  const videoLoading = ref(false)

  // 关联内容（他人标记）
  const userTagged = ref([])

  // 完善主页任务
  const profileTasks = ref([
    {
      id: 1,
      title: '添加你的姓名',
      description: '帮助朋友找到你的账户',
      iconSrc: '/static/icons/user.svg',
      completed: true,
      buttonText: '编辑姓名'
    },
    {
      id: 2,
      title: '添加头像',
      description: '选择一张头像照片',
      iconSrc: '/static/icons/add.svg',
      completed: true,
      buttonText: '更换头像'
    },
    {
      id: 3,
      title: '添加个性签名',
      description: '介绍一下你自己',
      iconSrc: '/static/icons/elip.svg',
      completed: false,
      buttonText: '添加个性签名'
    },
    {
      id: 4,
      title: '查找用户并关注',
      description: '关注至少5个账户',
      iconSrc: '/static/icons/direction.svg',
      completed: false,
      buttonText: '查找更多'
    }
  ])

  // 拼接头像地址
const getAvatarUrl = (avatar) => {
  if (!avatar) return '/static/images/avatar/1.jpg'
  return avatar.startsWith('http') ? avatar : appStore.baseUrl + avatar
}

// 获取用户统计数据
  const fetchUserStats = async () => {
    try {
      const info = userStore.userInfo || {}
      const userId = info.userId
      if (!userId) return

      const stats = await getUserStats(userId)
      userInfo.value = {
        userId,
        username: info.username || '',
        displayName: info.displayName || '',
        avatar: getAvatarUrl(info.avatar),
        posts: stats.postsCount || 0,
        followers: stats.followersCount || 0,
        following: stats.followingCount || 0,
        hasNotification: false
      }
    } catch (e) {
      console.error('获取用户统计失败', e)
      // 失败时使用 pinia 中的基础信息
      const info = userStore.userInfo || {}
      userInfo.value = {
        userId: info.userId || '',
        username: info.username || '',
        displayName: info.displayName || '',
        avatar: getAvatarUrl(info.avatar),
        posts: 0,
        followers: 0,
        following: 0,
        hasNotification: false
      }
    }
  }

  // 获取推荐用户列表
  const fetchDiscoverUsers = async () => {
    try {
      const data = await getDiscoverUsers(5)
      discoverUsers.value = data.map(item => ({
        id: item.userId,
        username: item.username,
        avatar: appStore.baseUrl + item.avatar,
        tag: item.tag || '为你推荐',
        isFollowing: item.isFollowing || false
      }))
    } catch (e) {
      console.error('获取推荐用户失败', e)
    }
  }

  // 获取用户帖子列表
  const fetchUserPosts = async () => {
    try {
      const info = userStore.userInfo || {}
      if (!info.userId) return
      const data = await getUserPosts(info.userId)
      userPosts.value = data.list.map(item => ({
        id: item.postId,
        image: appStore.baseUrl + item.coverUrl,
        type: item.mediaType,
        mediaCount: item.mediaCount
      }))
    } catch (e) {
      console.error('获取用户帖子失败', e)
    }
  }

  // 获取用户视频列表（Reels，分页）
  const fetchUserVideos = async () => {
    const info = userStore.userInfo || {}
    if (!info.userId || videoLoading.value || !videoHasMore.value) return
    videoLoading.value = true
    try {
      const data = await getUserPosts(info.userId, videoPage.value, 18, 'video')
      const list = (data?.list || []).map(item => ({
        id: item.postId,
        cover: appStore.baseUrl + item.coverUrl,
        mediaType: 'video',
        mediaCount: item.mediaCount || 0
      }))
      userVideos.value.push(...list)
      videoHasMore.value = Boolean(data?.hasMore)
      videoPage.value++
    } catch (e) {
      console.error('获取用户视频失败', e)
    } finally {
      videoLoading.value = false
    }
  }

  // 关注用户
  const followUser = async (userId) => {
    const user = discoverUsers.value.find(u => u.id === userId)
    if (!user) return

    try {
      if (user.isFollowing) {
        await unfollowUserApi({ userId })
        user.isFollowing = false
      } else {
        await followUserApi({ userId })
        user.isFollowing = true
      }
    } catch (e) {
      console.error('关注操作失败', e)
    }
  }

  // 移除发现用户
  const removeDiscoverUser = (userId) => {
    const index = discoverUsers.value.findIndex(u => u.id === userId)
    if (index > -1) {
      discoverUsers.value.splice(index, 1)
    }
  }

  // 切换tab
  const switchTab = (index) => {
    activeTab.value = index
    if (index === 1 && !userVideos.value.length && !videoLoading.value) {
      fetchUserVideos()
    }
  }

  onMounted(() => {
    fetchUserStats()
    fetchDiscoverUsers()
    fetchUserPosts()
  })

  return {
    userInfo,
    discoverUsers,
    activeTab,
    userPosts,
    userVideos,
    videoHasMore,
    videoLoading,
    fetchUserVideos,
    userTagged,
    profileTasks,
    followUser,
    removeDiscoverUser,
    switchTab
  }
}
