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