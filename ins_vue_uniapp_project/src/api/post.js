import { request } from './index.js'

// 获取首页帖子 Feed 流
export const getPostFeed = (lastId = 0, pageSize = 6) => {
  return request({
    url: '/api/post/feed',
    method: 'GET',
    data: { lastId, pageSize }
  })
}

// 获取帖子详情
export const getPostDetail = (postId) => {
  return request({
    url: '/api/post/detail',
    method: 'GET',
    data: { postId }
  })
}

// 帖子点赞
export const likePost = (data) => {
  return request({
    url: '/api/post/like',
    method: 'POST',
    data
  })
}

// 取消点赞
export const unlikePost = (data) => {
  return request({
    url: '/api/post/unlike',
    method: 'POST',
    data
  })
}

// 收藏帖子
export const savePost = (data) => {
  return request({
    url: '/api/post/save',
    method: 'POST',
    data
  })
}

// 取消收藏
export const unsavePost = (data) => {
  return request({
    url: '/api/post/unsave',
    method: 'POST',
    data
  })
}

// 发布帖子
export const createPost = (data) => {
  return request({
    url: '/api/post/create',
    method: 'POST',
    data
  })
}

// 编辑帖子
export const updatePost = (data) => {
  return request({
    url: '/api/post/update',
    method: 'POST',
    data
  })
}

// 删除帖子
export const deletePost = (data) => {
  return request({
    url: '/api/post/delete',
    method: 'POST',
    data
  })
}
