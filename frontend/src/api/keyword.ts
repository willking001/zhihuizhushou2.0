import request from '@/utils/request'

// 获取关键词列表
export function getKeywordList(params: any) {
  return request({
    url: '/api/keywords',
    method: 'get',
    params
  })
}

// 获取关键词详情
export function getKeywordDetail(id: number) {
  return request({
    url: `/api/keywords/${id}`,
    method: 'get'
  })
}

// 创建关键词
export function createKeyword(data: any) {
  return request({
    url: '/api/keywords',
    method: 'post',
    data
  })
}

// 更新关键词
export function updateKeyword(id: number, data: any) {
  return request({
    url: `/api/keywords/${id}`,
    method: 'put',
    data
  })
}

// 删除关键词
export function deleteKeyword(id: number) {
  return request({
    url: `/api/keywords/${id}`,
    method: 'delete'
  })
}

// 切换关键词状态
export function toggleKeywordStatus(id: number, isActive: boolean) {
  return request({
    url: `/api/keywords/${id}/toggle`,
    method: 'put',
    data: { isActive }
  })
}

// 批量操作关键词
export function batchOperateKeywords(operation: string, ids: number[]) {
  return request({
    url: `/api/keywords/batch/${operation}`,
    method: 'post',
    data: { ids }
  })
}

// 获取关键词统计数据
export function getKeywordStats(id: number) {
  return request({
    url: `/api/keywords/${id}/stats`,
    method: 'get'
  })
}

// 导出关键词配置
export function exportKeywordConfig() {
  return request({
    url: '/api/keywords/export',
    method: 'get',
    responseType: 'blob'
  })
}

// 获取关键词命中率数据
export function getKeywordHitRate() {
  return request({
    url: '/api/keywords/hit-rate',
    method: 'get'
  })
}

// 获取关键词分析报告
export function getKeywordAnalysis(params: any) {
  return request({
    url: '/api/keywords/analysis',
    method: 'get',
    params
  })
}