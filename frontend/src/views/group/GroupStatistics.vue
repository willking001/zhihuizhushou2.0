<template>
  <div class="group-statistics">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">群组统计</h1>
          <p class="page-subtitle">查看群组活动数据和统计分析</p>
        </div>
        <div class="header-actions">
          <select v-model="selectedTimeRange" class="time-range-select">
            <option value="today">今日</option>
            <option value="week">本周</option>
            <option value="month">本月</option>
            <option value="quarter">本季度</option>
          </select>
          <button class="action-btn primary" @click="refreshData">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
            </svg>
            刷新数据
          </button>
        </div>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="overview-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon total">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M16 4c0-1.11.89-2 2-2s2 .89 2 2-.89 2-2 2-2-.89-2-2zM4 18v-4h3v-3h2l3 2 4-2v3h3v4H4z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalGroups }}</div>
            <div class="stat-label">总群组数</div>
            <div class="stat-change positive">+{{ overview.newGroupsToday }} 今日新增</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon active">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.activeGroups }}</div>
            <div class="stat-label">活跃群组</div>
            <div class="stat-change positive">{{ ((overview.activeGroups / overview.totalGroups) * 100).toFixed(1) }}% 活跃率</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon messages">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M20 2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h4v3c0 .6.4 1 1 1 .2 0 .5-.1.7-.3L14.4 18H20c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(overview.totalMessages) }}</div>
            <div class="stat-label">总消息数</div>
            <div class="stat-change positive">+{{ formatNumber(overview.todayMessages) }} 今日</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon replies">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M10 9V5l-7 7 7 7v-4.1c5 0 8.5 1.6 11 5.1-1-5-4-10-11-11z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ formatNumber(overview.totalReplies) }}</div>
            <div class="stat-label">自动回复数</div>
            <div class="stat-change positive">{{ ((overview.totalReplies / overview.totalMessages) * 100).toFixed(1) }}% 回复率</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="charts-grid">
        <!-- 消息趋势图 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>消息趋势</h3>
            <div class="chart-legend">
              <span class="legend-item">
                <span class="legend-color messages"></span>
                消息数量
              </span>
              <span class="legend-item">
                <span class="legend-color replies"></span>
                回复数量
              </span>
            </div>
          </div>
          <div class="chart-container">
            <canvas ref="messagesTrendChart" width="400" height="200"></canvas>
          </div>
        </div>

        <!-- 群组状态分布 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>群组状态分布</h3>
          </div>
          <div class="chart-container">
            <div class="status-distribution">
              <div class="status-item" v-for="status in statusDistribution" :key="status.name">
                <div class="status-info">
                  <span class="status-name">{{ status.name }}</span>
                  <span class="status-count">{{ status.count }}</span>
                </div>
                <div class="status-bar">
                  <div 
                    class="status-progress" 
                    :class="status.type"
                    :style="{ width: status.percentage + '%' }"
                  ></div>
                </div>
                <span class="status-percentage">{{ status.percentage }}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 活跃度排行和关键词统计 -->
    <div class="analytics-section">
      <div class="analytics-grid">
        <!-- 群组活跃度排行 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>群组活跃度排行</h3>
            <div class="chart-actions">
              <select v-model="activityRankingDays" @change="handleActivityRankingDaysChange" class="period-select">
                <option value="7">最近7天</option>
                <option value="30">最近30天</option>
                <option value="90">最近90天</option>
              </select>
            </div>
          </div>
          <div class="chart-container">
            <div v-if="activityRanking.length > 0" class="ranking-list">
              <div 
                v-for="(item, index) in activityRanking" 
                :key="item.chatRoom" 
                class="ranking-item"
              >
                <div class="ranking-position">{{ index + 1 }}</div>
                <div class="ranking-info">
                  <div class="ranking-name">{{ item.groupName || item.chatRoom }}</div>
                  <div class="ranking-value">{{ item.messageCount }} 条消息</div>
                </div>
                <div class="ranking-bar">
                  <div 
                    class="ranking-progress" 
                    :style="{ width: (item.messageCount / Math.max(...activityRanking.map(r => r.messageCount)) * 100) + '%' }"
                  ></div>
                </div>
              </div>
            </div>
            <div v-else class="no-data">
              <span>暂无活跃度数据</span>
            </div>
          </div>
        </div>

        <!-- 关键词命中统计 -->
        <div class="chart-card">
          <div class="chart-header">
            <h3>关键词命中统计</h3>
            <div class="chart-actions">
              <select v-model="keywordStatsDays" @change="handleKeywordStatsDaysChange" class="period-select">
                <option value="7">最近7天</option>
                <option value="30">最近30天</option>
                <option value="90">最近90天</option>
              </select>
            </div>
          </div>
          <div class="chart-container">
            <div v-if="keywordStats.length > 0" class="keyword-stats-list">
              <div 
                v-for="keyword in keywordStats" 
                :key="keyword.keyword" 
                class="keyword-stat-item"
              >
                <div class="keyword-info">
                  <div class="keyword-name">{{ keyword.keyword }}</div>
                  <div class="keyword-metrics">
                    <span class="hit-count">{{ keyword.hitCount }} 次命中</span>
                    <span class="trigger-count">{{ keyword.triggerCount || 0 }} 次触发</span>
                  </div>
                </div>
                <div class="keyword-trend">
                  <span 
                    class="trend-indicator" 
                    :class="keyword.trend"
                  >
                    <svg v-if="keyword.trend === 'up'" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M7 14l5-5 5 5z"/>
                    </svg>
                    <svg v-else-if="keyword.trend === 'down'" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M7 10l5 5 5-5z"/>
                    </svg>
                    <svg v-else viewBox="0 0 24 24" fill="currentColor">
                      <path d="M5 12h14"/>
                    </svg>
                  </span>
                </div>
              </div>
            </div>
            <div v-else class="no-data">
              <span>暂无关键词统计数据</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 详细统计表格 -->
    <div class="table-section">
      <div class="table-card">
        <div class="table-header">
          <h3>群组详细统计</h3>
          <div class="table-actions">
            <input 
              v-model="searchQuery" 
              type="text" 
              placeholder="搜索群组..."
              class="search-input"
            >
            <select v-model="statusFilter" class="filter-select">
              <option value="">所有状态</option>
              <option value="AUTO">自动模式</option>
              <option value="MANUAL">人工接管</option>
              <option value="PAUSED">暂停服务</option>
            </select>
          </div>
        </div>
        <div class="table-container">
          <table class="stats-table">
            <thead>
              <tr>
                <th @click="sortBy('groupName')" class="sortable">
                  群组名称
                  <svg v-if="sortField === 'groupName'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M7 14l5-5 5 5z" v-if="sortOrder === 'asc'"/>
                    <path d="M7 10l5 5 5-5z" v-else/>
                  </svg>
                </th>
                <th>状态</th>
                <th @click="sortBy('messageCount')" class="sortable">
                  消息数
                  <svg v-if="sortField === 'messageCount'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M7 14l5-5 5 5z" v-if="sortOrder === 'asc'"/>
                    <path d="M7 10l5 5 5-5z" v-else/>
                  </svg>
                </th>
                <th @click="sortBy('replyCount')" class="sortable">
                  回复数
                  <svg v-if="sortField === 'replyCount'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M7 14l5-5 5 5z" v-if="sortOrder === 'asc'"/>
                    <path d="M7 10l5 5 5-5z" v-else/>
                  </svg>
                </th>
                <th @click="sortBy('replyRate')" class="sortable">
                  回复率
                  <svg v-if="sortField === 'replyRate'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M7 14l5-5 5 5z" v-if="sortOrder === 'asc'"/>
                    <path d="M7 10l5 5 5-5z" v-else/>
                  </svg>
                </th>
                <th @click="sortBy('lastActivity')" class="sortable">
                  最后活动
                  <svg v-if="sortField === 'lastActivity'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M7 14l5-5 5 5z" v-if="sortOrder === 'asc'"/>
                    <path d="M7 10l5 5 5-5z" v-else/>
                  </svg>
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="group in filteredGroups" :key="group.id">
                <td>
                  <div class="group-info">
                    <span class="group-name">{{ group.groupName }}</span>
                    <span class="group-id">{{ group.chatRoom }}</span>
                  </div>
                </td>
                <td>
                  <span class="status-badge" :class="group.status.toLowerCase()">
                    {{ getStatusText(group.status) }}
                  </span>
                </td>
                <td class="number-cell">{{ formatNumber(group.messageCount) }}</td>
                <td class="number-cell">{{ formatNumber(group.replyCount) }}</td>
                <td class="number-cell">{{ group.replyRate }}%</td>
                <td class="time-cell">{{ formatTime(group.lastActivity) }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <!-- 分页 -->
        <div v-if="filteredGroups.length > 0" class="pagination">
          <div class="pagination-info">
            显示 {{ (currentPage - 1) * pageSize + 1 }} - {{ Math.min(currentPage * pageSize, filteredGroups.length) }} 条，共 {{ filteredGroups.length }} 条
          </div>
          <div class="pagination-controls">
            <button 
              class="pagination-btn" 
              :disabled="currentPage === 1" 
              @click="currentPage--"
            >
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
              </svg>
            </button>
            <span class="page-numbers">
              <button 
                v-for="page in visiblePages" 
                :key="page"
                class="page-btn"
                :class="{ active: page === currentPage }"
                @click="currentPage = page"
              >
                {{ page }}
              </button>
            </span>
            <button 
              class="pagination-btn" 
              :disabled="currentPage === totalPages" 
              @click="currentPage++"
            >
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { groupManagementApi } from '@/api/groupManagement'

// 响应式数据
const selectedTimeRange = ref('today')
const searchQuery = ref('')
const statusFilter = ref('')
const sortField = ref('groupName')
const sortOrder = ref('asc')
const currentPage = ref(1)
const pageSize = ref(20)
const loading = ref(false)

const overview = reactive({
  totalGroups: 0,
  newGroupsToday: 0,
  activeGroups: 0,
  totalMessages: 0,
  todayMessages: 0,
  totalReplies: 0
})

const statusDistribution = ref([
  { name: '自动模式', type: 'auto', count: 0, percentage: 0 },
  { name: '人工接管', type: 'manual', count: 0, percentage: 0 },
  { name: '暂停服务', type: 'paused', count: 0, percentage: 0 }
])

const groupStats = ref([])
const messagesTrendChart = ref(null)

// 活跃度排行相关
const activityRankingDays = ref(7)
const activityRanking = ref([])

// 关键词统计相关
const keywordStatsDays = ref(7)
const keywordStats = ref([])

// 计算属性
const filteredGroups = computed(() => {
  // 确保 groupStats.value 是数组
  if (!Array.isArray(groupStats.value)) {
    return []
  }
  
  let filtered = [...groupStats.value]
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(group => 
      (group.groupName && group.groupName.toLowerCase().includes(query)) ||
      (group.chatRoom && group.chatRoom.toLowerCase().includes(query))
    )
  }
  
  // 状态过滤
  if (statusFilter.value) {
    filtered = filtered.filter(group => group.status === statusFilter.value)
  }
  
  // 排序
  if (filtered.length > 0) {
    filtered.sort((a, b) => {
      let aVal = a[sortField.value]
      let bVal = b[sortField.value]
      
      if (typeof aVal === 'string') {
        aVal = aVal.toLowerCase()
        bVal = bVal.toLowerCase()
      }
      
      if (sortOrder.value === 'asc') {
        return aVal > bVal ? 1 : -1
      } else {
        return aVal < bVal ? 1 : -1
      }
    })
  }
  
  return filtered
})

const totalPages = computed(() => {
  return Math.ceil(filteredGroups.value.length / pageSize.value)
})

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

// 将时间范围转换为天数
const getTimeRangeDays = (timeRange) => {
  const timeRangeMap = {
    'today': 1,
    'week': 7,
    'month': 30,
    'quarter': 90
  }
  return timeRangeMap[timeRange] || 7
}

// 方法
const loadStatistics = async () => {
  try {
    loading.value = true
    
    const days = getTimeRangeDays(selectedTimeRange.value)
    
    // 加载概览数据
    const overviewResponse = await groupManagementApi.getGroupOverview(days)
    if (overviewResponse.data) {
      Object.assign(overview, overviewResponse.data)
    }
    
    // 加载状态分布
    const distributionResponse = await groupManagementApi.getGroupStatusDistribution()
    if (Array.isArray(distributionResponse.data)) {
      statusDistribution.value = distributionResponse.data
    }
    
    // 加载群组统计
    const today = new Date()
    const startDate = new Date(today.getTime() - (days - 1) * 24 * 60 * 60 * 1000)
    const statsParams = {
      startDate: startDate.toISOString().split('T')[0],
      endDate: today.toISOString().split('T')[0]
    }
    const statsResponse = await groupManagementApi.getGroupStatistics(statsParams)
    groupStats.value = Array.isArray(statsResponse.data) ? statsResponse.data : []
    
    // 绘制图表
    await nextTick()
    await drawMessagesTrendChart()
    
    // 加载活跃度排行和关键词统计
    await loadActivityRanking()
    await loadKeywordStats()
    
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 重置为默认值，避免页面报错
    groupStats.value = []
    activityRanking.value = []
    keywordStats.value = []
    ElMessage.error('加载统计数据失败，请检查网络连接')
  } finally {
    loading.value = false
  }
}

// 加载活跃度排行
const loadActivityRanking = async () => {
  try {
    const params = {
      days: activityRankingDays.value,
      limit: 10
    }
    const response = await groupManagementApi.getGroupActivityRanking(params)
    activityRanking.value = response.data || []
  } catch (error) {
    console.error('加载活跃度排行失败:', error)
    activityRanking.value = []
  }
}

// 加载关键词统计
const loadKeywordStats = async () => {
  try {
    const params = {
      days: keywordStatsDays.value
    }
    const response = await groupManagementApi.getKeywordHitStats(params)
    keywordStats.value = response.data || []
  } catch (error) {
    console.error('加载关键词统计失败:', error)
    keywordStats.value = []
  }
}



const drawMessagesTrendChart = async () => {
  if (!messagesTrendChart.value) return
  
  const canvas = messagesTrendChart.value
  const ctx = canvas.getContext('2d')
  const width = canvas.width
  const height = canvas.height
  
  // 清空画布
  ctx.clearRect(0, 0, width, height)
  
  // 获取真实趋势数据
  const days = getTimeRangeDays(selectedTimeRange.value)
  let messagesData = []
  let repliesData = []
  
  try {
    const today = new Date()
    const startDate = new Date(today.getTime() - (days - 1) * 24 * 60 * 60 * 1000)
    
    const trendParams = {
      startDate: startDate.toISOString().split('T')[0],
      endDate: today.toISOString().split('T')[0]
    }
    
    const trendResponse = await groupManagementApi.getMessageTrend(trendParams)
    const trendData = trendResponse.data || []
    
    // 处理真实数据
    messagesData = trendData.map(item => item.messages || 0)
    repliesData = trendData.map(item => item.autoReplies || 0)
    
    // 如果没有数据，使用默认值
    if (messagesData.length === 0) {
      messagesData = Array(days).fill(0)
      repliesData = Array(days).fill(0)
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
    // 降级处理：使用空数据
    messagesData = Array(days).fill(0)
    repliesData = Array(days).fill(0)
  }
  
  const maxValue = Math.max(...messagesData, ...repliesData)
  const padding = 40
  const chartWidth = width - padding * 2
  const chartHeight = height - padding * 2
  
  // 绘制网格线
  ctx.strokeStyle = '#f1f5f9'
  ctx.lineWidth = 1
  
  for (let i = 0; i <= 5; i++) {
    const y = padding + (chartHeight / 5) * i
    ctx.beginPath()
    ctx.moveTo(padding, y)
    ctx.lineTo(width - padding, y)
    ctx.stroke()
  }
  
  // 绘制消息数据线
  ctx.strokeStyle = '#3b82f6'
  ctx.lineWidth = 2
  ctx.beginPath()
  
  messagesData.forEach((value, index) => {
    const x = padding + (chartWidth / (days - 1)) * index
    const y = padding + chartHeight - (value / maxValue) * chartHeight
    
    if (index === 0) {
      ctx.moveTo(x, y)
    } else {
      ctx.lineTo(x, y)
    }
  })
  
  ctx.stroke()
  
  // 绘制回复数据线
  ctx.strokeStyle = '#10b981'
  ctx.lineWidth = 2
  ctx.beginPath()
  
  repliesData.forEach((value, index) => {
    const x = padding + (chartWidth / (Math.max(messagesData.length, 1) - 1)) * index
    const y = padding + chartHeight - (value / Math.max(maxValue, 1)) * chartHeight
    
    if (index === 0) {
      ctx.moveTo(x, y)
    } else {
      ctx.lineTo(x, y)
    }
  })
  
  ctx.stroke()
}

const refreshData = () => {
  loadStatistics()
}

const exportData = () => {
  // 导出数据逻辑
  const data = filteredGroups.value.map(group => ({
    '群组名称': group.groupName,
    '群聊室ID': group.chatRoom,
    '状态': getStatusText(group.status),
    '消息数': group.messageCount,
    '回复数': group.replyCount,
    '回复率': group.replyRate + '%',
    '最后活动': formatTime(group.lastActivity)
  }))
  
  const csv = convertToCSV(data)
  downloadCSV(csv, `群组统计_${new Date().toISOString().split('T')[0]}.csv`)
}

const convertToCSV = (data) => {
  if (!data.length) return ''
  
  const headers = Object.keys(data[0])
  const csvContent = [
    headers.join(','),
    ...data.map(row => headers.map(header => `"${row[header]}"`).join(','))
  ].join('\n')
  
  return csvContent
}

const downloadCSV = (csv, filename) => {
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  
  if (link.download !== undefined) {
    const url = URL.createObjectURL(blob)
    link.setAttribute('href', url)
    link.setAttribute('download', filename)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  }
}

const sortBy = (field) => {
  if (sortField.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortOrder.value = 'asc'
  }
}

const getStatusText = (status) => {
  const statusMap = {
    'AUTO': '自动模式',
    'MANUAL': '人工接管',
    'PAUSED': '暂停服务'
  }
  return statusMap[status] || status
}

const formatNumber = (num) => {
  return new Intl.NumberFormat('zh-CN').format(num)
}

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

// 监听时间范围变化
const handleTimeRangeChange = () => {
  loadStatistics()
}

// 处理活跃度排行时间范围变化
const handleActivityRankingDaysChange = async () => {
  await loadActivityRanking()
}

// 处理关键词统计时间范围变化
const handleKeywordStatsDaysChange = async () => {
  await loadKeywordStats()
}

// 生命周期
onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.group-statistics {
  padding: 24px;
  background: #f8fafc;
  min-height: 100vh;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 32px;
  color: white;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-info {
      .page-title {
        font-size: 32px;
        font-weight: 700;
        margin: 0 0 8px 0;
        letter-spacing: -0.5px;
      }

      .page-subtitle {
        font-size: 16px;
        opacity: 0.9;
        margin: 0;
        font-weight: 400;
      }
    }

    .header-actions {
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

/* 统计概览 */
.overview-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
}

.stat-icon.total {
  background: #dbeafe;
  color: #1d4ed8;
}

.stat-icon.active {
  background: #d1fae5;
  color: #059669;
}

.stat-icon.messages {
  background: #fef3c7;
  color: #d97706;
}

.stat-icon.replies {
  background: #f3e8ff;
  color: #7c3aed;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-change {
  font-size: 12px;
  font-weight: 500;
}

.stat-change.positive {
  color: #059669;
}

/* 图表区域 */
.charts-section {
  margin-bottom: 24px;
}

.charts-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.chart-legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.legend-color.messages {
  background: #3b82f6;
}

.legend-color.replies {
  background: #10b981;
}

.chart-container {
  position: relative;
}

/* 状态分布 */
.status-distribution {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-info {
  min-width: 80px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.status-name {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.status-count {
  font-size: 12px;
  color: #6b7280;
}

.status-bar {
  flex: 1;
  height: 8px;
  background: #f1f5f9;
  border-radius: 4px;
  overflow: hidden;
}

.status-progress {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}

.status-progress.auto {
  background: #10b981;
}

.status-progress.manual {
  background: #f59e0b;
}

.status-progress.paused {
  background: #ef4444;
}

.status-percentage {
  min-width: 40px;
  text-align: right;
  font-size: 12px;
  color: #6b7280;
  font-weight: 500;
}

/* 表格区域 */
.table-section {
  margin-bottom: 24px;
}

.table-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}

.table-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.table-actions {
  display: flex;
  gap: 12px;
}

.search-input,
.filter-select {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.search-input {
  width: 200px;
}

.filter-select {
  width: 120px;
}

.table-container {
  overflow-x: auto;
}

.stats-table {
  width: 100%;
  border-collapse: collapse;
}

.stats-table th,
.stats-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f1f5f9;
}

.stats-table th {
  background: #f8fafc;
  font-weight: 600;
  color: #374151;
  font-size: 14px;
}

.stats-table th.sortable {
  cursor: pointer;
  user-select: none;
  position: relative;
}

.stats-table th.sortable:hover {
  background: #f1f5f9;
}

.stats-table th svg {
  width: 14px;
  height: 14px;
  margin-left: 4px;
  vertical-align: middle;
}

.stats-table td {
  font-size: 14px;
  color: #374151;
}

.group-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.group-name {
  font-weight: 500;
  color: #1e293b;
}

.group-id {
  font-size: 12px;
  color: #6b7280;
  font-family: 'Monaco', 'Menlo', monospace;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.auto {
  background: #d1fae5;
  color: #059669;
}

.status-badge.manual {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.paused {
  background: #fee2e2;
  color: #dc2626;
}

.number-cell {
  text-align: right;
  font-family: 'Monaco', 'Menlo', monospace;
}

.time-cell {
  color: #6b7280;
  font-size: 13px;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-top: 1px solid #f1f5f9;
}

.pagination-info {
  font-size: 14px;
  color: #6b7280;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: white;
  color: #374151;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination-btn svg {
  width: 16px;
  height: 16px;
}

.pagination-btn:hover:not(:disabled) {
  background: #f9fafb;
  border-color: #d1d5db;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

.page-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: white;
  color: #374151;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  font-weight: 500;
}

.page-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
}

.page-btn.active {
  background: #3b82f6;
  border-color: #3b82f6;
  color: white;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .group-statistics {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .table-actions {
    flex-direction: column;
  }
  
  .search-input {
    width: 100%;
  }
}

/* 活跃度排行和关键词统计样式 */
.analytics-section {
  margin-bottom: 32px;
}

.analytics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  transition: all 0.2s;
}

.ranking-item:hover {
  background: #f1f5f9;
  transform: translateY(-1px);
}

.ranking-position {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  font-weight: 600;
  font-size: 14px;
}

.ranking-info {
  flex: 1;
  min-width: 0;
}

.ranking-name {
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ranking-value {
  font-size: 12px;
  color: #6b7280;
}

.ranking-bar {
  width: 80px;
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
  overflow: hidden;
}

.ranking-progress {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6 0%, #1d4ed8 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.keyword-stats-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.keyword-stat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #f8fafc;
  border-radius: 8px;
  transition: all 0.2s;
}

.keyword-stat-item:hover {
  background: #f1f5f9;
  transform: translateY(-1px);
}

.keyword-info {
  flex: 1;
  min-width: 0;
}

.keyword-name {
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 4px;
}

.keyword-metrics {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #6b7280;
}

.hit-count {
  color: #059669;
}

.trigger-count {
  color: #d97706;
}

.keyword-trend {
  display: flex;
  align-items: center;
}

.trend-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
}

.trend-indicator.up {
  background: #d1fae5;
  color: #059669;
}

.trend-indicator.down {
  background: #fee2e2;
  color: #dc2626;
}

.trend-indicator.stable {
  background: #f3f4f6;
  color: #6b7280;
}

.trend-indicator svg {
  width: 12px;
  height: 12px;
}

.period-select {
  padding: 6px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  font-size: 12px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
}

.period-select:hover {
  border-color: #9ca3af;
}

.period-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

@media (max-width: 768px) {
  .analytics-grid {
    grid-template-columns: 1fr;
  }
  
  .ranking-item {
    gap: 12px;
  }
  
  .ranking-bar {
    width: 60px;
  }
}
</style>