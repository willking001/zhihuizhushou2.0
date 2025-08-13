import request from '@/utils/request'

/**
 * 获取仪表盘数据
 */
export function getDashboardData() {
  return request({
    url: '/api/monitor/dashboard',
    method: 'get'
  })
}

/**
 * 获取系统状态数据
 */
export function getSystemStatus() {
  return request({
    url: '/api/monitor/system',
    method: 'get'
  })
}

/**
 * 获取业务监控数据
 */
export function getBusinessMonitor() {
  return request({
    url: '/api/monitor/business',
    method: 'get'
  })
}

/**
 * 获取系统日志
 */
export function getSystemLogs(params?: any) {
  return request({
    url: '/api/system/logs',
    method: 'get',
    params
  })
}

/**
 * 导出系统日志
 */
export function exportSystemLogs(params?: any) {
  return request({
    url: '/api/system/logs/export',
    method: 'post',
    data: params,
    responseType: 'blob'
  })
}

/**
 * 清空系统日志
 */
export function clearSystemLogs() {
  return request({
    url: '/api/system/logs/clear',
    method: 'delete'
  })
}

/**
 * 获取日志统计
 */
export function getLogStats(params?: any) {
  return request({
    url: '/api/system/logs/stats',
    method: 'get',
    params
  })
}