import { ref } from 'vue'
import { getPostDetail, likePost, unlikePost, savePost, unsavePost } from '@/api/post.js'
import { followUser as followUserApi } from '@/api/user.js'
import { useAppStore } from '@/pinia/modules/appStore.js'
import { useUserStore } from '@/pinia/modules/userStore.js'
import { useActionFeedback } from '@/composables/useActionFeedback.js'

export function usePostDetail() {
  const appStore = useAppStore()
  const userStore = useUserStore()
  const { triggerFeedback } = useActionFeedback()

  const showPopup = ref(false)
  const post = ref(null)
  const loading = ref(false)

  const openPost = async (postId) => {
    if (!postId || loading.value) return
    loading.value = true
    showPopup.value = true

    try {
      const data = await getPostDetail(postId)
      const likesCount = Number(data.likesCount || 0)
      const commentsCount = Number(data.commentsCount || 0)
      const sharesCount = Number(data.sharesCount || 0)

      post.value = {
        id: data.postId,
        userId: data.userId,
        username: data.username,
        location: data.location || '',
        avatar: normalizeUrl(data.avatar),
        isVerified: Boolean(data.isVerified),
        showFollow: !data.isFollowing,
        images: (data.mediaList || []).map(m => normalizeUrl(m.url)),
        likesCount,
        commentsCount,
        sharesCount,
        likes: formatCount(likesCount),
        comments: formatCount(commentsCount),
        shares: formatCount(sharesCount),
        content: data.content || '',
        isLiked: Boolean(data.isLiked),
        isSaved: Boolean(data.isSaved),
        likePending: false,
        savePending: false,
        likeAnimating: false,
        saveAnimating: false,
        date: formatDate(data.createdAt)
      }
    } catch (e) {
      console.error('获取帖子详情失败', e)
      showPopup.value = false
    } finally {
      loading.value = false
    }
  }

  const closePost = () => {
    showPopup.value = false
  }

  const handleFollow = async () => {
    if (!post.value) return
    try {
      await followUserApi({ userId: post.value.userId })
      post.value.showFollow = false
    } catch (e) {
      console.error('关注失败', e)
    }
  }

  const handleToggleLike = async () => {
    if (!post.value || post.value.likePending) return
    post.value.likePending = true
    try {
      if (post.value.isLiked) {
        await unlikePost({ postId: post.value.id })
        post.value.isLiked = false
        post.value.likesCount = Math.max(0, post.value.likesCount - 1)
      } else {
        await likePost({ postId: post.value.id })
        post.value.isLiked = true
        post.value.likesCount += 1
        triggerFeedback(post.value, 'likeAnimating')
      }
      post.value.likes = formatCount(post.value.likesCount)
    } catch (e) {
      console.error('点赞操作失败', e)
    } finally {
      post.value.likePending = false
    }
  }

  const handleToggleSave = async () => {
    if (!post.value || post.value.savePending) return
    post.value.savePending = true
    try {
      if (post.value.isSaved) {
        await unsavePost({ postId: post.value.id })
        post.value.isSaved = false
      } else {
        await savePost({ postId: post.value.id })
        post.value.isSaved = true
        triggerFeedback(post.value, 'saveAnimating')
      }
    } catch (e) {
      console.error('收藏操作失败', e)
    } finally {
      post.value.savePending = false
    }
  }

  const goUserDetail = (userId) => {
    const targetUserId = String(userId || '')
    const currentUserId = String(userStore.userInfo?.userId || '')
    if (!targetUserId) return

    closePost()

    if (targetUserId === currentUserId) {
      uni.switchTab({ url: '/pages/profile/profile' })
      return
    }

    uni.navigateTo({
      url: `/pages/user-detail/user-detail?userId=${encodeURIComponent(targetUserId)}`
    })
  }

  function normalizeUrl(url) {
    if (!url) return ''
    return url.startsWith('http') ? url : appStore.baseUrl + url
  }

  return {
    showPopup,
    post,
    loading,
    openPost,
    closePost,
    handleFollow,
    handleToggleLike,
    handleToggleSave,
    goUserDetail
  }
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
