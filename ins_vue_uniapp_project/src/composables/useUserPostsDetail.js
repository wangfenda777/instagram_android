import { ref } from 'vue'
import { getUserPostsDetail } from '@/api/user.js'
import { likePost, unlikePost, savePost, unsavePost, deletePost } from '@/api/post.js'
import { followUser as followUserApi } from '@/api/user.js'
import { useAppStore } from '@/pinia/modules/appStore.js'
import { useUserStore } from '@/pinia/modules/userStore.js'
import { useActionFeedback } from '@/composables/useActionFeedback.js'

export function useUserPostsDetail() {
  const appStore = useAppStore()
  const userStore = useUserStore()
  const { triggerFeedback } = useActionFeedback()

  const showPopup = ref(false)
  const posts = ref([])
  const loading = ref(false)
  const loadingBefore = ref(false)
  const loadingAfter = ref(false)
  const hasMoreBefore = ref(true)
  const hasMoreAfter = ref(true)
  const currentUserId = ref('')

  const normalizeUrl = (url) => {
    if (!url) return ''
    return url.startsWith('http') ? url : appStore.baseUrl + url
  }

  const normalizePost = (item) => ({
    id: item.postId,
    userId: item.userId,
    username: item.username,
    location: item.location || '',
    avatar: normalizeUrl(item.avatar),
    isVerified: Boolean(item.isVerified),
    showFollow: false,
    mediaList: (item.mediaList || []).map(m => ({ url: normalizeUrl(m.url), type: m.type || 'image' })),
    images: (item.mediaList || []).map(m => normalizeUrl(m.url)),
    likesCount: Number(item.likesCount || 0),
    commentsCount: Number(item.commentsCount || 0),
    sharesCount: Number(item.sharesCount || 0),
    likes: formatCount(Number(item.likesCount || 0)),
    comments: formatCount(Number(item.commentsCount || 0)),
    shares: formatCount(Number(item.sharesCount || 0)),
    content: item.content || '',
    isLiked: Boolean(item.isLiked),
    isSaved: Boolean(item.isSaved),
    likePending: false,
    savePending: false,
    likeAnimating: false,
    saveAnimating: false,
    date: formatDate(item.createdAt)
  })

  const openPost = async (userId, postId) => {
    if (!userId || !postId || loading.value) return
    currentUserId.value = userId
    posts.value = []
    hasMoreBefore.value = true
    hasMoreAfter.value = true
    loading.value = true
    showPopup.value = true

    try {
      // 初次加载：不传 direction，返回锚点帖子 + 更老的5条（最多6条）
      const data = await getUserPostsDetail(userId, postId)
      const rawList = getList(data)
      posts.value = rawList.map(normalizePost)
      hasMoreAfter.value = Boolean(data?.hasMore)
      // 上面还可能有更新的数据
      hasMoreBefore.value = true
    } catch (e) {
      console.error('获取帖子详情失败', e)
      showPopup.value = false
    } finally {
      loading.value = false
    }
  }

  const loadBefore = async () => {
    if (loadingBefore.value || !hasMoreBefore.value || !posts.value.length) return
    loadingBefore.value = true

    try {
      const firstPostId = posts.value[0].id
      // 滚动到顶部：before = 加载更新的5条
      const data = await getUserPostsDetail(currentUserId.value, firstPostId, 'before')
      const rawList = getList(data)
      if (rawList.length) {
        const newPosts = rawList.map(normalizePost)
        posts.value = [...newPosts, ...posts.value]
      }
      hasMoreBefore.value = Boolean(data?.hasMore) && rawList.length > 0
    } catch (e) {
      console.error('加载更早帖子失败', e)
    } finally {
      loadingBefore.value = false
    }
  }

  const loadAfter = async () => {
    if (loadingAfter.value || !hasMoreAfter.value || !posts.value.length) return
    loadingAfter.value = true

    try {
      const lastPostId = posts.value[posts.value.length - 1].id
      // 滚动到底部：after = 加载更老的5条
      const data = await getUserPostsDetail(currentUserId.value, lastPostId, 'after')
      const rawList = getList(data)
      if (rawList.length) {
        posts.value.push(...rawList.map(normalizePost))
      }
      hasMoreAfter.value = Boolean(data?.hasMore) && rawList.length > 0
    } catch (e) {
      console.error('加载更新帖子失败', e)
    } finally {
      loadingAfter.value = false
    }
  }

  const closePost = () => {
    showPopup.value = false
  }

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
    const post = posts.value.find(p => p.id === postId)
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
    const post = posts.value.find(p => p.id === postId)
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
    const myUserId = String(userStore.userInfo?.userId || '')
    if (!targetUserId) return
    closePost()
    if (targetUserId === myUserId) {
      uni.switchTab({ url: '/pages/profile/profile' })
      return
    }
    uni.navigateTo({
      url: `/pages/user-detail/user-detail?userId=${encodeURIComponent(targetUserId)}`
    })
  }

  const handleDeletePost = async (postId) => {
    const post = posts.value.find(p => p.id === postId)
    if (!post) return

    // 二次确认
    const delPostId = post.id
    uni.showModal({
      title: '提示',
      content: '确定要删除此帖子吗？',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await deletePost({ postId: delPostId })
          // 从列表中移除
          posts.value = posts.value.filter(p => p.id !== delPostId)
          triggerFeedback({ id: delPostId }, 'deleted')
          uni.showToast({ title: '删除成功', icon: 'success' })
        } catch (e) {
          console.error('删除失败', e)
          uni.showToast({ title: '删除失败', icon: 'none' })
        }
      }
    })
  }

  return {
    showPopup,
    posts,
    loading,
    loadingBefore,
    loadingAfter,
    hasMoreBefore,
    hasMoreAfter,
    openPost,
    closePost,
    loadBefore,
    loadAfter,
    handleFollow,
    handleToggleLike,
    handleToggleSave,
    goUserDetail,
    handleDeletePost
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
