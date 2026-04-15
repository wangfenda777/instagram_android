import { request } from './index.js'

// 获取快拍列表
export const getStoryFeed = () => {
  return request({
    url: '/api/story/feed',
    method: 'GET'
  })
}
