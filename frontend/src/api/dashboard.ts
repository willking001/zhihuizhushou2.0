import request from '@/utils/request';

// 仪表盘数据接口定义
export interface DashboardData {
  stats: {
    totalMessages: number;
    pendingMessages: number;
    processedMessages: number;
    forwardedMessages: number;
  };
  messageTrend: {
    dates: string[];
    values: number[];
  };
  userDistribution: {
    labels: string[];
    values: number[];
  };
  keywordHitRate: {
    keywords: string[];
    hitCounts: number[];
  };
  systemStatus: {
    cpu: number;
    memory: number;
    disk: number;
    network: number;
  };
  recentActivities: Array<{
    id: number;
    type: string;
    content: string;
    user: string;
    timestamp: string;
  }>;
}

// 获取仪表盘数据
export const getDashboardData = () => {
  return request<DashboardData>({
    url: '/api/monitor/dashboard',
    method: 'get'
  });
};

// 获取消息统计数据
export const getMessageStats = () => {
  return request({
    url: '/api/messages/stats',
    method: 'get'
  });
};

// 获取消息趋势数据
export const getMessageTrend = (period: string = 'week') => {
  return request({
    url: '/api/analytics/trends',
    method: 'get',
    params: {
      metric: 'messages',
      period
    }
  });
};

// 获取用户分布数据
export const getUserDistribution = () => {
  return request({
    url: '/api/analytics/user-distribution',
    method: 'get'
  });
};

// 获取关键词命中率数据
export const getKeywordHitRate = () => {
  return request({
    url: '/api/keywords/hit-rate',
    method: 'get'
  });
};

// 获取系统状态数据
export const getSystemStatus = () => {
  return request({
    url: '/api/monitor/system',
    method: 'get'
  });
};

// 获取最近活动数据
export const getRecentActivities = (limit: number = 5) => {
  return request({
    url: '/api/audit/logs',
    method: 'get',
    params: {
      limit
    }
  });
};