import { request } from './index.js'

// 搜索用户
export const searchUser = (keyword, page = 1, pageSize = 20) => {
  return request({
    url: '/api/search/user',
    method: 'GET',
    data: { keyword, page, pageSize }
  })
}

// 搜索标签
export const searchTag = (keyword, page = 1, pageSize = 20) => {
  return request({
    url: '/api/search/tag',
    method: 'GET',
    data: { keyword, page, pageSize }
  })
}

// 搜索帖子/标签
export const searchPost = (keyword, type = 'all', page = 1, pageSize = 20) => {
  return request({
    url: '/api/search/post',
    method: 'GET',
    data: { keyword, type, page, pageSize }
  })
}
