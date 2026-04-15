import { request } from './index.js'

// 获取探索页推荐内容
export const getExploreFeed = (page = 1, pageSize = 24) => {
  return request({
    url: '/api/explore/feed',
    method: 'GET',
    data: { page, pageSize }
  })
}
