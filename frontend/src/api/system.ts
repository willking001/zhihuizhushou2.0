import request from '@/utils/request'

// 系统配置相关接口

/**
 * 获取所有系统配置
 */
export function getAllSystemConfigs() {
  return request({
    url: '/api/system/config',
    method: 'get'
  })
}

/**
 * 分页查询系统配置
 */
export function getSystemConfigsPage(params: {
  page?: number
  size?: number
  sort?: string
  direction?: string
}) {
  return request({
    url: '/api/system/config/page',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取配置
 */
export function getSystemConfigById(id: number) {
  return request({
    url: `/api/system/config/${id}`,
    method: 'get'
  })
}

/**
 * 根据配置键名获取配置
 */
export function getSystemConfigByKey(configKey: string) {
  return request({
    url: `/api/system/config/key/${configKey}`,
    method: 'get'
  })
}

/**
 * 根据配置键名获取配置值
 */
export function getSystemConfigValue(configKey: string) {
  return request({
    url: `/api/system/config/value/${configKey}`,
    method: 'get'
  })
}

/**
 * 获取关键词触发阈值
 */
export function getKeywordThreshold() {
  return request({
    url: '/api/system/config/keyword-threshold',
    method: 'get'
  })
}

/**
 * 创建或更新配置
 */
export function saveSystemConfig(data: {
  id?: number
  configKey: string
  configValue: string
  description?: string
  configType: string
  enabled?: boolean
  remark?: string
}) {
  return request({
    url: '/api/system/config',
    method: 'post',
    data
  })
}

/**
 * 更新配置
 */
export function updateSystemConfig(id: number, data: {
  configKey: string
  configValue: string
  description?: string
  configType: string
  enabled?: boolean
  remark?: string
}) {
  return request({
    url: `/api/system/config/${id}`,
    method: 'put',
    data
  })
}

/**
 * 批量更新配置
 */
export function batchUpdateSystemConfigs(configs: Record<string, string>) {
  return request({
    url: '/api/system/config/batch',
    method: 'put',
    data: configs
  })
}

/**
 * 设置关键词触发阈值
 */
export function setKeywordThreshold(threshold: number) {
  return request({
    url: '/api/system/config/keyword-threshold',
    method: 'put',
    data: { threshold }
  })
}

/**
 * 获取关键词审核开关状态
 */
export function getKeywordAuditStatus() {
  return request({
    url: '/api/system/config/keyword-audit',
    method: 'get'
  })
}

/**
 * 设置关键词审核开关
 */
export function setKeywordAuditStatus(auditEnabled: boolean) {
  return request({
    url: '/api/system/config/keyword-audit',
    method: 'put',
    data: { auditEnabled }
  })
}

/**
 * 删除配置
 */
export function deleteSystemConfig(id: number) {
  return request({
    url: `/api/system/config/${id}`,
    method: 'delete'
  })
}

/**
 * 初始化默认配置
 */
export function initDefaultConfigs() {
  return request({
    url: '/api/system/config/init',
    method: 'post'
  })
}

// 系统信息相关接口

/**
 * 获取系统健康状态
 */
export function getSystemHealth() {
  return request({
    url: '/api/system/health',
    method: 'get'
  })
}

/**
 * 获取系统信息
 */
export function getSystemInfo() {
  return request({
    url: '/api/system/info',
    method: 'get'
  })
}