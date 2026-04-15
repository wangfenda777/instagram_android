import { ref, onMounted } from 'vue'
import { getStoryFeed } from '@/api/story.js'
import { getPostFeed, likePost, unlikePost, savePost, unsavePost } from '@/api/post.js'
import { followUser as followUserApi } from '@/api/user.js'
import { useAppStore } from '@/pinia/modules/appStore.js'
import { useUserStore } from '@/pinia/modules/userStore.js'
import { useActionFeedback } from '@/composables/useActionFeedback.js'

const FEED_PAGE_SIZE = 6

export function useHome() {
  const appStore = useAppStore()
  const userStore = useUserStore()
  const { triggerFeedback } = useActionFeedback()
  const stories = ref([])
  const posts = ref([])
  const lastId = ref(0)
  const hasMore = ref(true)
  const loading = ref(false)

  // 获取快拍列表
  const fetchStories = async () => {
    try {
      const data = await getStoryFeed()
      stories.value = data.map(item => ({
        id: item.storyId,
        name: item.username,
        avatar: appStore.baseUrl + item.avatar,
        borderColor: item.hasUnread
          ? 'linear-gradient(45deg, #f09433, #e6683c, #dc2743, #cc2366, #bc1888)'
          : '#dbdbdb'
      }))
    } catch (e) {
      console.error('获取快拍失败', e)
    }
  }

  // 获取帖子列表
  const fetchPosts = async () => {
    if (loading.value || !hasMore.value) return
    loading.value = true
    try {
      const data = await getPostFeed(lastId.value, FEED_PAGE_SIZE)
      const rawList = getList(data)
      const list = rawList.map(item => {
        const likesCount = Number(item.likesCount || 0)
        const commentsCount = Number(item.commentsCount || 0)
        const sharesCount = Number(item.sharesCount || 0)

        return {
          id: item.postId,
          userId: item.userId,
          username: item.username,
          location: item.location,
          avatar: appStore.baseUrl + item.avatar,
          isVerified: item.isVerified,
          showFollow: !item.isFollowing,
          images: item.mediaList.map(m => appStore.baseUrl + m.url),
          likesCount,
          commentsCount,
          sharesCount,
          likes: formatCount(likesCount),
          comments: formatCount(commentsCount),
          shares: formatCount(sharesCount),
          content: item.content || '',
          isLiked: Boolean(item.isLiked),
          isSaved: Boolean(item.isSaved),
          likePending: false,
          savePending: false,
          likeAnimating: false,
          saveAnimating: false,
          expanded: false,
          date: formatDate(item.createdAt)
        }
      })

      posts.value.push(...list)

      hasMore.value = Boolean(data?.hasMore)

      if (!rawList.length || !hasMore.value) {
        return
      }

      if (data?.lastId === null || data?.lastId === undefined || String(data.lastId) === String(lastId.value)) {
        hasMore.value = false
        return
      }

      lastId.value = data.lastId
    } catch (e) {
      console.error('获取帖子失败', e)
    } finally {
      loading.value = false
    }
  }

  // 关注用户
  const handleFollow = async (postId) => {
    const post = posts.value.find(p => p.id === postId)
    if (!post) return
    try {
      await followUserApi({ userId: post.userId })
      post.showFollow = false
    } catch (e) {
      console.error('关注失败', e)
    }
  }

  const handleToggleLike = async (postId) => {
    const post = posts.value.find(item => item.id === postId)
    if (!post || post.likePending) return

    post.likePending = true
    try {
      if (post.isLiked) {
        await unlikePost({ postId })
        post.isLiked = false
        post.likesCount = Math.max(0, post.likesCount - 1)
      } else {
        await likePost({ postId })
        post.isLiked = true
        post.likesCount += 1
        triggerFeedback(post, 'likeAnimating')
      }
      post.likes = formatCount(post.likesCount)
    } catch (e) {
      console.error('点赞操作失败', e)
    } finally {
      post.likePending = false
    }
  }

  const handleToggleSave = async (postId) => {
    const post = posts.value.find(item => item.id === postId)
    if (!post || post.savePending) return

    post.savePending = true
    try {
      if (post.isSaved) {
        await unsavePost({ postId })
        post.isSaved = false
      } else {
        await savePost({ postId })
        post.isSaved = true
        triggerFeedback(post, 'saveAnimating')
      }
    } catch (e) {
      console.error('收藏操作失败', e)
    } finally {
      post.savePending = false
    }
  }

  const goUserDetail = (userId) => {
    const targetUserId = String(userId || '')
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

  onMounted(() => {
    fetchStories()
    fetchPosts()
  })

  return {
    stories,
    posts,
    hasMore,
    loading,
    fetchPosts,
    handleFollow,
    handleToggleLike,
    handleToggleSave,
    goUserDetail
  }
}

function getList(payload) {
  if (Array.isArray(payload)) return payload
  if (Array.isArray(payload?.list)) return payload.list
  return []
}

function formatCount(num) {
  if (num >= 10000) return (num / 10000).toFixed(1).replace(/\.0$/, '') + '万'
  if (num >= 1000) return num.toLocaleString()
  return String(num)
}

function formatDate(timestamp) {
  const d = new Date(timestamp)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}
