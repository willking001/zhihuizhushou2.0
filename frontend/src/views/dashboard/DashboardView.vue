<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1 class="welcome-title">欢迎回来，管理员</h1>
          <p class="welcome-subtitle">今天是 {{ currentDate }}，系统运行正常</p>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div v-for="(stat, index) in stats" :key="index" class="stat-card">
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <component :is="stat.icon" />
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
          <div class="stat-change" :class="stat.changeType">
            <svg v-if="stat.changeType === 'increase'" viewBox="0 0 24 24" fill="currentColor">
              <path d="M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z"/>
            </svg>
            <svg v-else viewBox="0 0 24 24" fill="currentColor">
              <path d="M16 18l2.29-2.29-4.88-4.88-4 4L2 7.41 3.41 6l6 6 4-4 6.3 6.29L22 12v6z"/>
            </svg>
            {{ stat.change }}
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-row">
        <!-- 消息趋势图 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">消息趋势</h3>
            <div class="chart-actions">
              <select v-model="messageChartPeriod" class="period-select" @change="changeMessagePeriod(messageChartPeriod)">
            <option value="7d">最近7天</option>
            <option value="30d">最近30天</option>
            <option value="90d">最近90天</option>
          </select>
            </div>
          </div>
          <div class="chart-content">
            <canvas ref="messageChart" class="chart-canvas"></canvas>
          </div>
        </div>

        <!-- 用户分布图 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">用户分布</h3>
          </div>
          <div class="chart-content">
            <canvas ref="userChart" class="chart-canvas"></canvas>
          </div>
        </div>
      </div>

      <div class="chart-row">
        <!-- 关键词命中率 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">关键词命中率</h3>
          </div>
          <div class="chart-content">
            <div class="keyword-stats">
              <div v-for="keyword in keywordStats" :key="keyword.name" class="keyword-item">
                <div class="keyword-info">
                  <span class="keyword-name">{{ keyword.name }}</span>
                  <span class="keyword-count">{{ keyword.count }}次</span>
                </div>
                <div class="keyword-bar">
                  <div class="keyword-progress" :style="{ width: keyword.percentage + '%', background: keyword.color }"></div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 系统状态 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3 class="chart-title">系统状态</h3>
          </div>
          <div class="chart-content">
            <div class="system-metrics">
              <div v-for="metric in systemMetrics" :key="metric.name" class="metric-item">
                <div class="metric-header">
                  <span class="metric-name">{{ metric.name }}</span>
                  <span class="metric-value">{{ metric.value }}%</span>
                </div>
                <div class="metric-bar">
                  <div 
                    class="metric-progress" 
                    :style="{ 
                      width: metric.value + '%', 
                      background: getMetricColor(metric.value) 
                    }"
                  ></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="activity-section">
      <div class="activity-card">
        <div class="activity-header">
          <h3 class="activity-title">最近活动</h3>
          <button class="view-all-btn">查看全部</button>
        </div>
        <div class="activity-list">
          <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
            <div class="activity-icon" :class="activity.type">
              <component :is="activity.icon" />
            </div>
            <div class="activity-content">
              <div class="activity-text">{{ activity.text }}</div>
              <div class="activity-time">{{ activity.time }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick, defineComponent } from 'vue'
import { Chart, registerables } from 'chart.js'
import { getDashboardData, getMessageStats, getMessageTrend, getUserDistribution, getKeywordHitRate, getSystemStatus, getRecentActivities } from '@/api/dashboard'
import { ElLoading, ElMessage } from 'element-plus'

// 注册Chart.js组件
Chart.register(...registerables)

const messageChart = ref<HTMLCanvasElement>()
const userChart = ref<HTMLCanvasElement>()
const messageChartPeriod = ref('7d')
const loading = ref(false)
const messageChartInstance = ref<Chart | null>(null)
const userChartInstance = ref<Chart | null>(null)

// 当前日期
const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  });
})

// 统计数据
const stats = ref([
  {
    icon: 'MessageIcon',
    value: '0',
    label: '总消息数',
    change: '0%',
    changeType: 'increase',
    gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
  },
  {
    icon: 'UserIcon',
    value: '0',
    label: '活跃用户',
    change: '0%',
    changeType: 'increase',
    gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)'
  },
  {
    icon: 'ForwardIcon',
    value: '0',
    label: '转发消息',
    change: '0%',
    changeType: 'increase',
    gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  },
  {
    icon: 'AlertIcon',
    value: '0',
    label: '待处理警报',
    change: '0%',
    changeType: 'decrease',
    gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)'
  }
])

// 关键词统计
const keywordStats = ref([
  { name: '紧急事件', count: 0, percentage: 0, color: '#ef4444' },
  { name: '设备故障', count: 0, percentage: 0, color: '#f97316' },
  { name: '安全隐患', count: 0, percentage: 0, color: '#eab308' },
  { name: '环境问题', count: 0, percentage: 0, color: '#22c55e' },
  { name: '其他', count: 0, percentage: 0, color: '#6366f1' }
])

// 系统指标
const systemMetrics = ref([
  { name: 'CPU使用率', value: 0 },
  { name: '内存使用率', value: 0 },
  { name: '磁盘使用率', value: 0 },
  { name: '网络带宽', value: 0 }
])

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    type: 'message',
    icon: 'MessageIcon',
    text: '加载中...',
    time: ''
  }
])

// 获取指标颜色
const getMetricColor = (value: number) => {
  if (value >= 80) return '#ef4444';
  if (value >= 60) return '#f97316';
  if (value >= 40) return '#eab308';
  return '#22c55e';
}

// 获取仪表盘数据
const fetchDashboardData = async () => {
  loading.value = true;
  try {
    const response = await getDashboardData();
    if (response && response.data) {
      const data = response.data;
      
      // 更新统计数据
      if (data.stats) {
        // 确保数据存在且为数字类型
        const totalMessages = typeof data.stats.totalMessages === 'number' ? data.stats.totalMessages : 0;
        const forwardedMessages = typeof data.stats.forwardedMessages === 'number' ? data.stats.forwardedMessages : 0;
        const pendingMessages = typeof data.stats.pendingMessages === 'number' ? data.stats.pendingMessages : 0;
        
        stats.value[0].value = totalMessages.toLocaleString();
        stats.value[2].value = forwardedMessages.toLocaleString();
        stats.value[3].value = pendingMessages.toLocaleString();
      }
      
      // 更新系统状态
      if (data.systemStatus) {
        // 确保数据存在且为数字类型
        systemMetrics.value[0].value = typeof data.systemStatus.cpu === 'number' ? data.systemStatus.cpu : 0;
        systemMetrics.value[1].value = typeof data.systemStatus.memory === 'number' ? data.systemStatus.memory : 0;
        systemMetrics.value[2].value = typeof data.systemStatus.disk === 'number' ? data.systemStatus.disk : 0;
        systemMetrics.value[3].value = typeof data.systemStatus.network === 'number' ? data.systemStatus.network : 0;
      }
      
      // 更新最近活动 - 暂时注释以解决语法错误
      /*
      if (data.recentActivities && Array.isArray(data.recentActivities) && data.recentActivities.length > 0) {
        // 后续会实现活动数据处理
      }
      */
      
      // 更新图表数据
      updateCharts(data);
    }
  } catch (error) {
    console.error('获取仪表盘数据失败:', error);
    ElMessage.error('获取仪表盘数据失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

// 更新图表数据
const updateCharts = (data: any) => {
  // 更新消息趋势图
  if (data.messageTrend && messageChart.value) {
    if (messageChartInstance.value) {
      // 确保数据存在且格式正确
      const labels = Array.isArray(data.messageTrend.dates) ? data.messageTrend.dates : [];
      const values = Array.isArray(data.messageTrend.values) ? data.messageTrend.values : [];
      
      messageChartInstance.value.data.labels = labels;
      messageChartInstance.value.data.datasets[0].data = values;
      messageChartInstance.value.update();
    } else {
      initMessageChart(
        Array.isArray(data.messageTrend.dates) ? data.messageTrend.dates : [], 
        Array.isArray(data.messageTrend.values) ? data.messageTrend.values : []
      );
    }
  }
  
  // 更新用户分布图
  if (data.userDistribution && userChart.value) {
    if (userChartInstance.value) {
      // 确保数据存在且格式正确
      const labels = Array.isArray(data.userDistribution.labels) ? data.userDistribution.labels : [];
      const values = Array.isArray(data.userDistribution.values) ? data.userDistribution.values : [];
      
      userChartInstance.value.data.labels = labels;
      userChartInstance.value.data.datasets[0].data = values;
      userChartInstance.value.update();
    } else {
      initUserChart(
        Array.isArray(data.userDistribution.labels) ? data.userDistribution.labels : [], 
        Array.isArray(data.userDistribution.values) ? data.userDistribution.values : []
      );
    }
  }
  
  // 更新关键词统计
  if (data.keywordHitRate && Array.isArray(data.keywordHitRate.keywords) && data.keywordHitRate.keywords.length > 0) {
    const total = Array.isArray(data.keywordHitRate.hitCounts) ? 
      data.keywordHitRate.hitCounts.reduce((sum: number, count: number) => sum + count, 0) : 0;
    
    keywordStats.value = data.keywordHitRate.keywords.map((keyword: string, index: number) => {
      const count = data.keywordHitRate.hitCounts[index] || 0;
      const percentage = total > 0 ? Math.round((count / total) * 100) : 0;
      return {
        name: keyword,
        count,
        percentage,
        color: getKeywordColor(index)
      };
    });
  }
};

// 获取关键词颜色
const getKeywordColor = (index: number) => {
  const colors = ['#ef4444', '#f97316', '#eab308', '#22c55e', '#6366f1'];
  return colors[index % colors.length];
};

// 格式化时间
const formatTime = (timestamp: string) => {
  const date = new Date(timestamp);
  const now = new Date();
  const diff = Math.floor((now.getTime() - date.getTime()) / 1000); // 秒数差
  
  if (diff < 60) return `${diff}秒前`;
  if (diff < 3600) return `${Math.floor(diff / 60)}分钟前`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}小时前`;
  if (diff < 2592000) return `${Math.floor(diff / 86400)}天前`;
  
  return date.toLocaleDateString('zh-CN');
};

// 初始化消息趋势图
const initMessageChart = (labels: string[], data: number[]) => {
  if (!messageChart.value) return;
  
  // 确保数据格式正确
  const safeLabels = Array.isArray(labels) ? labels : ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
  const safeData = Array.isArray(data) ? data : [0, 0, 0, 0, 0, 0, 0];
  
  try {
    messageChartInstance.value = new Chart(messageChart.value, {
      type: 'line',
      data: {
        labels: safeLabels,
        datasets: [{
          label: '消息数量',
          data: safeData,
          borderColor: '#3b82f6',
          backgroundColor: 'rgba(59, 130, 246, 0.1)',
          borderWidth: 3,
          fill: true,
          tension: 0.4
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            enabled: true
          }
        },
        scales: {
          y: {
            beginAtZero: true,
            grid: {
              color: '#f1f5f9'
            }
          },
          x: {
            grid: {
              display: false
            }
          }
        },
        events: ['mousemove', 'mouseout', 'click', 'touchstart', 'touchmove']
      }
    });
  } catch (error) {
    console.error('初始化消息趋势图失败:', error);
  }
};

// 初始化用户分布图
const initUserChart = (labels: string[], data: number[]) => {
  if (!userChart.value) return;
  
  // 确保数据格式正确
  const safeLabels = Array.isArray(labels) ? labels : ['网格员', '管理员', '普通用户', '访客'];
  const safeData = Array.isArray(data) ? data : [0, 0, 0, 0];
  
  try {
    userChartInstance.value = new Chart(userChart.value, {
      type: 'doughnut',
      data: {
        labels: safeLabels,
        datasets: [{
          data: safeData,
          backgroundColor: ['#3b82f6', '#ef4444', '#22c55e', '#f59e0b'],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              padding: 20,
              boxWidth: 12
            }
          },
          tooltip: {
            enabled: true
          }
        },
        cutout: '70%',
        events: ['mousemove', 'mouseout', 'click', 'touchstart', 'touchmove']
      }
    });
  } catch (error) {
    console.error('初始化用户分布图失败:', error);
  }
};

// 切换消息趋势图周期
const changeMessagePeriod = async (period: string) => {
  messageChartPeriod.value = period;
  try {
    const response = await getMessageTrend(period);
    if (response && response.data) {
      // 确保数据存在且格式正确
      const dates = Array.isArray(response.data.dates) ? response.data.dates : [];
      const values = Array.isArray(response.data.values) ? response.data.values : [];
      
      if (messageChartInstance.value) {
        messageChartInstance.value.data.labels = dates;
        messageChartInstance.value.data.datasets[0].data = values;
        messageChartInstance.value.update();
      }
    }
  } catch (error) {
    console.error('获取消息趋势数据失败:', error);
  }
};

// 初始化图表
const initCharts = async () => {
  await nextTick();
  
  // 初始化空图表
  initMessageChart([], []);
  initUserChart([], []);
  
  // 获取实际数据
  fetchDashboardData();
};

// 定义一个变量来存储定时器ID
let refreshInterval: number | null = null;

onMounted(() => {
  initCharts();
  
  // 设置定时刷新
  refreshInterval = window.setInterval(() => {
    fetchDashboardData();
  }, 60000); // 每分钟刷新一次
});

// 组件卸载时清除定时器和销毁图表实例
onUnmounted(() => {
  // 清除定时器
  if (refreshInterval !== null) {
    window.clearInterval(refreshInterval);
    refreshInterval = null;
  }
  
  // 销毁图表实例
  if (messageChartInstance.value) {
    try {
      messageChartInstance.value.destroy();
      messageChartInstance.value = null;
    } catch (error) {
      console.error('销毁消息图表实例失败:', error);
    }
  }
  
  if (userChartInstance.value) {
    try {
      userChartInstance.value.destroy();
      userChartInstance.value = null;
    } catch (error) {
      console.error('销毁用户图表实例失败:', error);
    }
  }
});

// 图标组件
const MessageIcon = {
  template: `
    <svg viewBox="0 0 24 24" fill="currentColor">
      <path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/>
    </svg>
  `
};

const UserIcon = {
  template: `
    <svg viewBox="0 0 24 24" fill="currentColor">
      <path d="M16 7c0-2.21-1.79-4-4-4S8 4.79 8 7s1.79 4 4 4 4-1.79 4-4zm-4 7c-2.67 0-8 1.34-8 4v3h16v-3c0-2.66-5.33-4-8-4z"/>
    </svg>
  `
};

const ForwardIcon = {
  template: `
    <svg viewBox="0 0 24 24" fill="currentColor">
      <path d="M12 8V4l8 8-8 8v-4H4V8z"/>
    </svg>
  `
};

const AlertIcon = {
  template: `
    <svg viewBox="0 0 24 24" fill="currentColor">
      <path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/>
    </svg>
  `
};

const SystemIcon = {
  template: `
    <svg viewBox="0 0 24 24" fill="currentColor">
      <path d="M19.14,12.94c0.04-0.3,0.06-0.61,0.06-0.94c0-0.32-0.02-0.64-0.07-0.94l2.03-1.58c0.18-0.14,0.23-0.41,0.12-0.61 l-1.92-3.32c-0.12-0.22-0.37-0.29-0.59-0.22l-2.39,0.96c-0.5-0.38-1.03-0.7-1.62-0.94L14.4,2.81c-0.04-0.24-0.24-0.41-0.48-0.41 h-3.84c-0.24,0-0.43,0.17-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29L5.24,5.33c-0.22-0.08-0.47,0-0.59,0.22L2.74,8.87 C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58C4.84,11.36,4.8,11.69,4.8,12s0.02,0.64,0.07,0.94l-2.03,1.58 c-0.18,0.14-0.23,0.41-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54 c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.44-0.17,0.47-0.41l0.36-2.54c0.59-0.24,1.13-0.56,1.62-0.94l2.39,0.96 c0.22,0.08,0.47,0,0.59-0.22l1.92-3.32c0.12-0.22,0.07-0.47-0.12-0.61L19.14,12.94z M12,15.6c-1.98,0-3.6-1.62-3.6-3.6 s1.62-3.6,3.6-3.6s3.6,1.62,3.6,3.6S13.98,15.6,12,15.6z"/>
    </svg>
  `
};
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 0;
  max-width: 100%;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

// 欢迎区域
.welcome-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 32px;
  color: white;

  .welcome-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .welcome-text {
      .welcome-title {
        font-size: 32px;
        font-weight: 700;
        margin: 0 0 8px 0;
        letter-spacing: -0.5px;
      }

      .welcome-subtitle {
        font-size: 16px;
        opacity: 0.9;
        margin: 0;
        font-weight: 400;
      }
    }

    .welcome-actions {
      display: flex;
      gap: 12px;

      .action-btn {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 12px 20px;
        border: none;
        border-radius: 10px;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.2s;
        font-size: 14px;

        svg {
          width: 18px;
          height: 18px;
        }

        &.primary {
          background: rgba(255, 255, 255, 0.2);
          color: white;
          backdrop-filter: blur(10px);

          &:hover {
            background: rgba(255, 255, 255, 0.3);
            transform: translateY(-1px);
          }
        }

        &.secondary {
          background: rgba(255, 255, 255, 0.1);
          color: white;
          border: 1px solid rgba(255, 255, 255, 0.2);

          &:hover {
            background: rgba(255, 255, 255, 0.2);
            transform: translateY(-1px);
          }
        }
      }
    }
  }
}

// 统计卡片网格
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f1f5f9;
  transition: all 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  }

  .stat-icon {
    width: 64px;
    height: 64px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    flex-shrink: 0;

    svg {
      width: 28px;
      height: 28px;
    }
  }

  .stat-content {
    flex: 1;

    .stat-value {
      font-size: 28px;
      font-weight: 700;
      color: #1e293b;
      line-height: 1.2;
      margin-bottom: 4px;
    }

    .stat-label {
      font-size: 14px;
      color: #64748b;
      font-weight: 500;
      margin-bottom: 8px;
    }

    .stat-change {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      font-weight: 600;

      svg {
        width: 14px;
        height: 14px;
      }

      &.increase {
        color: #22c55e;
      }

      &.decrease {
        color: #ef4444;
      }
    }
  }
}

// 图表区域
.charts-section {
  margin-bottom: 32px;
}

.chart-row {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
  margin-bottom: 24px;

  &:last-child {
    margin-bottom: 0;
  }
}

.chart-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f1f5f9;

  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .chart-title {
      font-size: 18px;
      font-weight: 600;
      color: #1e293b;
      margin: 0;
    }

    .chart-actions {
      .period-select {
        padding: 8px 12px;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        background: white;
        font-size: 14px;
        color: #64748b;
        outline: none;
        cursor: pointer;

        &:focus {
          border-color: #3b82f6;
        }
      }
    }
  }

  .chart-content {
    .chart-canvas {
      width: 100%;
      height: 300px;
    }
  }
}

// 关键词统计
.keyword-stats {
  .keyword-item {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }

    .keyword-info {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .keyword-name {
        font-size: 14px;
        font-weight: 500;
        color: #374151;
      }

      .keyword-count {
        font-size: 12px;
        color: #6b7280;
      }
    }

    .keyword-bar {
      height: 8px;
      background: #f3f4f6;
      border-radius: 4px;
      overflow: hidden;

      .keyword-progress {
        height: 100%;
        border-radius: 4px;
        transition: width 0.3s ease;
      }
    }
  }
}

// 系统指标
.system-metrics {
  .metric-item {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }

    .metric-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;

      .metric-name {
        font-size: 14px;
        font-weight: 500;
        color: #374151;
      }

      .metric-value {
        font-size: 14px;
        font-weight: 600;
        color: #1e293b;
      }
    }

    .metric-bar {
      height: 8px;
      background: #f3f4f6;
      border-radius: 4px;
      overflow: hidden;

      .metric-progress {
        height: 100%;
        border-radius: 4px;
        transition: width 0.3s ease;
      }
    }
  }
}

// 活动区域
.activity-section {
  .activity-card {
    background: white;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    border: 1px solid #f1f5f9;

    .activity-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      .activity-title {
        font-size: 18px;
        font-weight: 600;
        color: #1e293b;
        margin: 0;
      }

      .view-all-btn {
        background: none;
        border: none;
        color: #3b82f6;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
        padding: 8px 12px;
        border-radius: 6px;
        transition: all 0.2s;

        &:hover {
          background: #f1f5f9;
        }
      }
    }

    .activity-list {
      .activity-item {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 16px 0;
        border-bottom: 1px solid #f1f5f9;

        &:last-child {
          border-bottom: none;
          padding-bottom: 0;
        }

        .activity-icon {
          width: 40px;
          height: 40px;
          border-radius: 10px;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          flex-shrink: 0;

          svg {
            width: 20px;
            height: 20px;
          }

          &.message {
            background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
          }

          &.user {
            background: linear-gradient(135deg, #10b981 0%, #059669 100%);
          }

          &.alert {
            background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
          }

          &.system {
            background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
          }
        }

        .activity-content {
          flex: 1;

          .activity-text {
            font-size: 14px;
            color: #374151;
            font-weight: 500;
            margin-bottom: 4px;
          }

          .activity-time {
            font-size: 12px;
            color: #9ca3af;
          }
        }
      }
    }
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .chart-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .welcome-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .welcome-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .chart-card {
    padding: 16px;
  }

  .activity-card {
    padding: 16px;
  }
}
</style>