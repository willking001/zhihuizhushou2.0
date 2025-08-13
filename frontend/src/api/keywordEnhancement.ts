import request from '@/utils/request'

// 关键词学习相关API
export const keywordLearningAPI = {
  // 记录关键词检测
  recordDetection: (data: {
    keyword: string
    context: string
    gridArea?: string
    confidence: number
  }) => {
    return request({
      url: '/api/keyword-enhancement/learning/detect',
      method: 'post',
      data
    })
  },

  // 获取待审核推荐
  getPendingRecommendations: (params: {
    gridArea?: string
    limit?: number
  }) => {
    return request({
      url: '/api/keyword-enhancement/learning/recommendations',
      method: 'get',
      params
    })
  },

  // 审核推荐
  reviewRecommendation: (recommendationId: number, data: {
    approved: boolean
    reviewedBy: number
    reviewComment?: string
  }) => {
    return request({
      url: `/api/keyword-enhancement/learning/recommendations/${recommendationId}/review`,
      method: 'post',
      data
    })
  },

  // 获取学习统计
  getLearningStats: (params: {
    gridArea?: string
    days?: number
  }) => {
    return request({
      url: '/api/keyword-enhancement/learning/stats',   
      method: 'get',
      params
    })
  },

  // 获取热门关键词
  getHotKeywords: (params: {
    gridArea?: string
    days?: number
    limit?: number
  }) => {
    return request({
      url: '/api/keyword-enhancement/learning/hot-keywords',
      method: 'get',
      params
    })
  }
}

// 关键词同步相关API
export const keywordSyncAPI = {
  // 开始同步
  startSync: (data: {
    clientId: string
    syncType: 'FULL' | 'INCREMENTAL'
    syncDirection: 'DOWNLOAD' | 'UPLOAD' | 'BIDIRECTIONAL'
  }) => {
    return request({
      url: '/api/keyword-enhancement/sync/start',
      method: 'post',
      data
    })
  },

  // 获取同步状态
  getSyncStatus: (params: {
    gridArea?: string
  }) => {
    return request({
      url: '/api/keyword-enhancement/sync/status',
      method: 'get',
      params
    })
  },

  // 获取同步历史
  getSyncHistory: (params: {
    gridArea?: string
    limit?: number
  }) => {
    return request({
      url: '/api/keyword-enhancement/sync/history',
      method: 'get',
      params
    })
  },

  // 获取同步统计
  getSyncStats: (params: {
    gridArea?: string
    days?: number
  }) => {
    return request({
      url: '/api/keyword-enhancement/sync/stats',
      method: 'get',
      params
    })
  }
}

// 关键词分析相关API
export const keywordAnalysisAPI = {
  // 获取分析报告
  getAnalysisReport: (params: {
    gridArea?: string
    days?: number
  }) => {
    return request({
      url: '/api/keyword-enhancement/analysis/report',
      method: 'get',
      params
    })
  },

  // 获取新兴关键词
  getEmergingKeywords: (params: {
    gridArea?: string
    days?: number
  }) => {
    return request({
      url: '/api/keyword-enhancement/analysis/emerging',
      method: 'get',
      params
    })
  },

  // 预测关键词趋势
  predictKeywordTrend: (keywordId: number, params: {
    futureDays?: number
  }) => {
    return request({
      url: `/api/keyword-enhancement/analysis/predict/${keywordId}`,
      method: 'get',
      params
    })
  },

  // 获取优化建议
  getOptimizationSuggestions: (params: {
    gridArea?: string
  }) => {
    return request({
      url: '/api/keyword-enhancement/analysis/suggestions',
      method: 'get',
      params
    })
  }
}

// 关键词管理相关API
export const keywordManagementAPI = {
  // 检测关键词冗余
  detectRedundancy: (params: {
    gridArea?: string
  }) => {
    return request({
      url: '/api/keyword-enhancement/management/redundancy',
      method: 'get',
      params
    })
  },

  // 自动分类关键词
  autoClassifyKeywords: (params: {
    gridArea?: string
  }) => {
    return request({
      url: '/api/keyword-enhancement/management/auto-classify',
      method: 'post',
      params
    })
  },

  // 获取关键词推荐
  getKeywordRecommendations: (params: {
    gridArea?: string
    limit?: number
  }) => {
    return request({
      url: '/api/keyword-enhancement/management/recommendations',
      method: 'get',
      params
    })
  },

  // 批量处理冗余关键词
  batchProcessRedundancy: (data: Array<{
    action: 'MERGE' | 'DELETE' | 'KEEP'
    keywordIds: number[]
    targetKeywordId?: number
  }>) => {
    return request({
      url: '/api/keyword-enhancement/management/redundancy/batch-process',  
      method: 'post',
      data
    })
  },

  // 获取管理统计
  getManagementStats: (params: {
    gridArea?: string
  }) => {
    return request({
      url: '/api/keyword-enhancement/management/stats',
      method: 'get',
      params
    })
  }
}

// 关键词触发相关API
export const keywordTriggerAPI = {
  // 检测并触发关键词
  detectAndTrigger: (data: {
    text: string
    gridArea?: string
    context?: string
  }) => {
    return request({
      url: '/api/keyword-enhancement/trigger/detect',
      method: 'post',
      data
    })
  },

  // 创建触发规则
  createTriggerRule: (data: {
    keywordId: number
    triggerType: string
    triggerAction: string
    actionConfig: object
    priority: number
    isActive: boolean
  }) => {
    return request({
      url: '/api/keyword-enhancement/trigger/rules',
      method: 'post',
      data
    })
  },

  // 获取触发统计
  getTriggerStats: (params: {
    gridArea?: string
    days?: number
  }) => {
    return request({
      url: '/api/keyword-enhancement/trigger/stats',
      method: 'get',
      params
    })
  },

  // 获取热门触发关键词
  getHotTriggerKeywords: (params: {
    gridArea?: string
    days?: number
    limit?: number
  }) => {
    return request({
      url: '/api/keyword-enhancement/trigger/hot-keywords',
      method: 'get',
      params
    })
  }
}

// 综合API
export const keywordEnhancementAPI = {
  // 获取功能概览
  getOverview: (params: {
    gridArea?: string
    days?: number
  }) => {
    return request({
      url: '/api/keyword-enhancement/overview',
      method: 'get',
      params
    })
  },

  // 健康检查
  healthCheck: () => {
    return request({
      url: '/api/keyword-enhancement/health',
      method: 'get'
    })
  }
}

// 关键词兼容模式相关API
export const keywordCompatibilityAPI = {
  // 记录客户端关键词触发
  recordClientTrigger: (data: {
    keyword: string
    gridArea: string
    userId: number
    context?: string
  }) => {
    return request({
      url: '/api/keyword-compatibility/trigger',
      method: 'post',
      params: data
    })
  },

  // 获取关键词优先级
  getKeywordPriority: (params: {
    keyword: string
    gridArea?: string
  }) => {
    return request({
      url: '/api/keyword-compatibility/priority',
      method: 'get',
      params
    })
  },

  // 获取待审核的服务器提交
  getPendingSubmissions: (params: {
    gridArea?: string
  }) => {
    return request({
      url: '/api/keyword-compatibility/submissions/pending',
      method: 'get',
      params
    })
  },

  // 批准服务器提交
  approveSubmission: (submissionId: number, data: {
    reviewerId: number
    reviewNotes?: string
  }) => {
    return request({
      url: `/api/keyword-compatibility/submissions/${submissionId}/approve`,
      method: 'post',
      params: data
    })
  },

  // 拒绝服务器提交
  rejectSubmission: (submissionId: number, data: {
    reviewerId: number
    reviewNotes?: string
  }) => {
    return request({
      url: `/api/keyword-compatibility/submissions/${submissionId}/reject`,
      method: 'post',
      params: data
    })
  },

  // 手动提交关键词到服务器审核
  manualSubmission: (data: {
    keyword: string
    gridArea: string
    submitterId: number
  }) => {
    return request({
      url: '/api/keyword-compatibility/submissions/manual',
      method: 'post',
      params: data
    })
  },

  // 获取兼容模式统计信息
  getCompatibilityStats: (params: {
    gridArea?: string
  }) => {
    return request({
      url: '/api/keyword-compatibility/stats',
      method: 'get',
      params
    })
  },

  // 批量操作待审核提交
  batchOperateSubmissions: (data: {
    submissionIds: number[]
    operation: 'approve' | 'reject'
    reviewerId: number
    reviewNotes?: string
  }) => {
    return request({
      url: '/api/keyword-compatibility/submissions/batch',
      method: 'post',
      data: data.submissionIds,
      params: {
        operation: data.operation,
        reviewerId: data.reviewerId,
        reviewNotes: data.reviewNotes
      }
    })
  }
}

// 导出所有API
export default {
  learning: keywordLearningAPI,
  sync: keywordSyncAPI,
  analysis: keywordAnalysisAPI,
  management: keywordManagementAPI,
  trigger: keywordTriggerAPI,
  overview: keywordEnhancementAPI,
  compatibility: keywordCompatibilityAPI
}