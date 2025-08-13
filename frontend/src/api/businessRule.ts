import request from '@/utils/request'

// 业务规则相关API
export const businessRuleApi = {
  // 获取业务规则列表
  getBusinessRules: (params: any) => {
    return request({
      url: '/api/business-rules/page',
      method: 'get',
      params
    })
  },

  // 根据ID获取业务规则
  getBusinessRuleById: (id: number) => {
    return request({
      url: `/api/business-rules/${id}`,
      method: 'get'
    })
  },

  // 创建业务规则
  createBusinessRule: (data: any) => {
    return request({
      url: '/api/business-rules',
      method: 'post',
      data
    })
  },

  // 更新业务规则
  updateBusinessRule: (id: number, data: any) => {
    return request({
      url: `/api/business-rules/${id}`,
      method: 'put',
      data
    })
  },

  // 删除业务规则
  deleteBusinessRule: (id: number) => {
    return request({
      url: `/api/business-rules/${id}`,
      method: 'delete'
    })
  },

  // 批量删除业务规则
  batchDeleteBusinessRules: (ids: number[]) => {
    return request({
      url: '/api/business-rules/batch',
      method: 'delete',
      data: { ids }
    })
  },

  // 启用/禁用业务规则
  toggleBusinessRuleStatus: (id: number, enabled: boolean) => {
    return request({
      url: `/api/business-rules/${id}/status`,
      method: 'patch',
      data: { enabled }
    })
  },

  // 获取规则类型列表
  getRuleTypes: () => {
    return request({
      url: '/api/business-rules/types',
      method: 'get'
    })
  },

  // 测试规则
  testRule: (ruleId: number, testData: any) => {
    return request({
      url: `/api/business-rules/${ruleId}/test`,
      method: 'post',
      data: testData
    })
  },

  // 获取规则执行统计
  getRuleStatistics: (ruleId: number, params?: any) => {
    return request({
      url: `/api/business-rules/${ruleId}/statistics`,
      method: 'get',
      params
    })
  }
}

export default businessRuleApi