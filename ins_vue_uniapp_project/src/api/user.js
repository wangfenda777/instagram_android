import { request } from './index.js'

// 获取当前用户基础信息
export const getUserMe = () => {
  return request({
    url: '/api/user/me',
    method: 'GET'
  })
}

// 获取指定用户基础信息
export const getUserInfo = (userId) => {
  return request({
    url: '/api/user/info',
    method: 'GET',
    data: { userId }
  })
}

// 获取用户统计信息
export const getUserStats = (userId) => {
  return request({
    url: '/api/user/stats',
    method: 'GET',
    data: { userId }
  })
}

// 关注用户
export const followUser = (data) => {
  return request({
    url: '/api/user/follow',
    method: 'POST',
    data
  })
}

// 取消关注
export const unfollowUser = (data) => {
  return request({
    url: '/api/user/unfollow',
    method: 'POST',
    data
  })
}

// 获取用户帖子列表（网格）
export const getUserPosts = (userId, page = 1, pageSize = 18, mediaType) => {
  const params = { userId, page, pageSize }
  if (mediaType) params.mediaType = mediaType
  return request({
    url: '/api/user/posts',
    method: 'GET',
    data: params
  })
}

// 获取用户视频列表（Reels）
export const getUserReels = (userId, page = 1, pageSize = 18) => {
  return request({
    url: '/api/user/reels',
    method: 'GET',
    data: { userId, page, pageSize }
  })
}

// 获取推荐用户列表
export const getDiscoverUsers = (limit = 10) => {
  return request({
    url: '/api/user/discover',
    method: 'GET',
    data: { limit }
  })
}

// 获取粉丝列表
export const getUserFollowers = (userId, page = 1, pageSize = 20) => {
  return request({
    url: '/api/user/followers',
    method: 'GET',
    data: { userId, page, pageSize }
  })
}

// 获取关注列表
export const getUserFollowing = (userId, page = 1, pageSize = 20) => {
  return request({
    url: '/api/user/following',
    method: 'GET',
    data: { userId, page, pageSize }
  })
}

// 编辑用户资料
export const updateProfile = (data) => {
  return request({
    url: '/api/user/profile/update',
    method: 'POST',
    data
  })
}

// 获取用户帖子详情列表（游标加载）
export const getUserPostsDetail = (userId, postId, direction) => {
  const params = { userId, postId }
  if (direction) params.direction = direction
  return request({
    url: '/api/user/posts/detail',
    method: 'GET',
    data: params
  })
}
