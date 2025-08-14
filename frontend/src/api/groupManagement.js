import request from '@/utils/request'

/**
 * 群组管理API
 */
export const groupManagementApi = {
  /**
   * 创建群组
   * @param {Object} groupData 群组数据
   * @returns {Promise}
   */
  createGroup(groupData) {
    return request({
      url: '/api/group-management/groups',
      method: 'post',
      data: groupData
    })
  },

  /**
   * 获取群组列表
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getGroupList(params = {}) {
    return request({
      url: '/api/group-management/groups',
      method: 'get',
      params
    })
  },

  /**
   * 获取群组详情
   * @param {string} chatRoom 群聊室ID
   * @returns {Promise}
   */
  getGroupDetail(chatRoom) {
    return request({
      url: `/api/group-management/groups/${chatRoom}`,
      method: 'get'
    })
  },

  /**
   * 更新群组配置
   * @param {string} chatRoom 群聊室ID
   * @param {Object} config 配置数据
   * @returns {Promise}
   */
  updateGroupConfig(chatRoom, config) {
    return request({
      url: `/api/group-management/groups/${encodeURIComponent(chatRoom)}/config`,
      method: 'put',
      data: config
    })
  },
  
  /**
   * 删除群组
   * @param {string} chatRoom 群聊室ID
   * @returns {Promise}
   */
  deleteGroup(chatRoom) {
    return request({
      url: `/api/group-management/groups/${chatRoom}`,
      method: 'delete'
    })
  },

  /**
   * 切换群组状态
   * @param {string} chatRoom 群聊室ID
   * @param {string} status 目标状态 (AUTO/MANUAL/PAUSED)
   * @param {string} reason 切换原因
   * @returns {Promise}
   */
  switchGroupStatus(chatRoom, status, reason = '') {
    return request({
      url: `/api/group-management/groups/${chatRoom}/status`,
      method: 'post',
      data: {
        status,
        reason
      }
    })
  },

  /**
   * 批量更新群组
   * @param {Object} batchData 批量操作数据
   * @returns {Promise}
   */
  batchUpdateGroups(batchData) {
    return request({
      url: '/api/group-management/groups/batch',
      method: 'post',
      data: batchData
    })
  },

  /**
   * 获取群组统计信息
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getGroupStats(params = {}) {
    return request({
      url: '/api/group-management/stats',
      method: 'get',
      params
    })
  },

  /**
   * 获取群组消息列表
   * @param {string} chatRoom 群聊室ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getGroupMessages(chatRoom, params = {}) {
    return request({
      url: `/api/group-management/groups/${chatRoom}/messages`,
      method: 'get',
      params
    })
  },

  /**
   * 获取接管日志
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getTakeoverLogs(params = {}) {
    return request({
      url: '/api/group-management/takeover-logs',
      method: 'get',
      params
    })
  },

  /**
   * 重置群组计数器
   * @param {string} chatRoom 群聊室ID
   * @returns {Promise}
   */
  resetGroupCounters(chatRoom) {
    return request({
      url: `/api/group-management/groups/${chatRoom}/reset-counters`,
      method: 'post'
    })
  },

  /**
   * 批量重置计数器
   * @param {Array} chatRooms 群聊室ID列表
   * @returns {Promise}
   */
  batchResetCounters(chatRooms) {
    return request({
      url: '/api/group-management/groups/batch-reset-counters',
      method: 'post',
      data: {
        chatRooms
      }
    })
  },

  /**
   * 获取群组业务规则
   * @param {string} chatRoom 群聊室ID
   * @returns {Promise}
   */
  getGroupRules(chatRoom) {
    return request({
      url: `/api/group-management/groups/${chatRoom}/rules`,
      method: 'get'
    })
  },

  /**
   * 更新群组业务规则
   * @param {string} chatRoom 群聊室ID
   * @param {Object} rules 业务规则数据
   * @returns {Promise}
   */
  updateGroupRules(chatRoom, rules) {
    return request({
      url: `/api/group-management/groups/${chatRoom}/rules`,
      method: 'put',
      data: rules
    })
  },

  /**
   * 获取群组关键词配置
   * @param {string} chatRoom 群聊室ID
   * @returns {Promise}
   */
  getGroupKeywords(chatRoom) {
    return request({
      url: `/api/group-management/groups/${chatRoom}/keywords`,
      method: 'get'
    })
  },

  /**
   * 更新群组关键词配置
   * @param {string} chatRoom 群聊室ID
   * @param {Object} keywords 关键词配置数据
   * @returns {Promise}
   */
  updateGroupKeywords(chatRoom, keywords) {
    return request({
      url: `/api/group-management/groups/${chatRoom}/keywords`,
      method: 'put',
      data: keywords
    })
  },

  /**
   * 导出群组数据
   * @param {Object} params 导出参数
   * @returns {Promise}
   */
  exportGroupData(params = {}) {
    return request({
      url: '/api/group-management/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },

  /**
   * 获取群组活动趋势
   * @param {string} chatRoom 群聊室ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getGroupActivityTrend(chatRoom, params = {}) {
    return request({
      url: `/api/group-management/groups/${chatRoom}/activity-trend`,
      method: 'get',
      params
    })
  },

  /**
   * 获取系统配置
   * @returns {Promise}
   */
  getSystemConfig() {
    return request({
      url: '/api/group-management/system-config',
      method: 'get'
    })
  },

  /**
   * 更新系统配置
   * @param {Object} config 系统配置数据
   * @returns {Promise}
   */
  updateSystemConfig(config) {
    return request({
      url: '/api/group-management/system-config',
      method: 'put',
      data: config
    })
  },

  // ========== 群组设置相关API ==========
  
  /**
   * 获取群组设置
   * @returns {Promise}
   */
  getGroupSettings() {
    return request({
      url: '/api/group-management/settings',
      method: 'get'
    })
  },

  /**
   * 更新群组设置
   * @param {Object} settings 群组设置数据
   * @returns {Promise}
   */
  updateGroupSettings(settings) {
    return request({
      url: '/api/group-management/settings',
      method: 'put',
      data: settings
    })
  },

  // ========== 网格员管理相关API ==========
  
  /**
   * 获取网格员列表
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getGridOfficers(params = {}) {
    return request({
      url: '/api/group-management/grid-officers',
      method: 'get',
      params
    })
  },

  /**
   * 分配网格员到群组
   * @param {number} groupId 群组ID
   * @param {Object} data 分配数据
   * @returns {Promise}
   */
  assignOfficerToGroup(groupId, data) {
    return request({
      url: `/api/groups/${groupId}/assign-officer`,
      method: 'put',
      data
    })
  },

  // ========== 群组统计相关API ==========
  
  /**
   * 获取群组概览统计
   * @param {number} days 时间范围（天数）
   * @returns {Promise}
   */
  getGroupOverview(days = 7) {
    return request({
      url: '/api/group-management/statistics/overview',
      method: 'get',
      params: { days }
    })
  },

  /**
   * 获取群组状态分布
   * @returns {Promise}
   */
  getGroupStatusDistribution() {
    return request({
      url: '/api/group-management/statistics/status',
      method: 'get'
    })
  },

  /**
   * 获取群组统计数据
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getGroupStatistics(params = {}) {
    return request({
      url: '/api/group-management/statistics/overall',
      method: 'get',
      params
    })
  },

  /**
   * 获取消息趋势数据
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getMessageTrend(params = {}) {
    return request({
      url: '/api/group-management/statistics/trends',
      method: 'get',
      params
    })
  },

  /**
   * 获取群组活跃度排行
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getGroupActivityRanking(params = {}) {
    return request({
      url: '/api/group-management/statistics/activity-ranking',
      method: 'get',
      params
    })
  },

  /**
   * 获取关键词命中统计
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getKeywordHitStats(params = {}) {
    return request({
      url: '/api/group-management/statistics/keyword-hits',
      method: 'get',
      params
    })
  }
}

export default groupManagementApi