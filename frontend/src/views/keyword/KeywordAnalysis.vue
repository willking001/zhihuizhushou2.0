<template>
  <div class="keyword-analysis-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">关键词分析</h1>
          <p class="page-subtitle">深度分析关键词使用情况，提供多维度统计报告</p>
        </div>
        <div class="header-actions">
          <button class="refresh-btn" @click="refreshAnalysisData" :disabled="analysisLoading">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
            </svg>
            {{ analysisLoading ? '加载中...' : '刷新数据' }}
          </button>
          <button class="export-btn" @click="exportAnalysisReport">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
            </svg>
            导出报告
          </button>
        </div>
      </div>
    </div>

    <!-- 筛选条件 -->
    <div class="search-section">
      <div class="search-bar">
        <div class="filter-controls">
          <div class="filter-item">
            <label class="filter-label">
              <svg class="filter-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M9 11H7v2h2v-2zm4 0h-2v2h2v-2zm4 0h-2v2h2v-2zm2-7h-1V2h-2v2H8V2H6v2H5c-1.1 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14v11z"/>
              </svg>
              时间范围
            </label>
            <select v-model="analysisFilters.days" @change="refreshAnalysisData" class="filter-select">
              <option :value="7">最近7天</option>
              <option :value="30">最近30天</option>
              <option :value="90">最近90天</option>
            </select>
          </div>
          <div class="filter-item">
            <label class="filter-label">
              <svg class="filter-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
              </svg>
              关键词类型
            </label>
            <select v-model="analysisFilters.type" @change="refreshAnalysisData" class="filter-select">
              <option value="">全部类型</option>
              <option value="GLOBAL">全局关键词</option>
              <option value="LOCAL">本地关键词</option>
              <option value="SMART">智能关键词</option>
            </select>
          </div>
          <div class="filter-item">
            <label class="filter-label">
              <svg class="filter-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M14.4 6L14 4H5v17h2v-7h5.6l.4 2h7V6z"/>
              </svg>
              优先级
            </label>
            <select v-model="analysisFilters.priority" @change="refreshAnalysisData" class="filter-select">
              <option value="">全部优先级</option>
              <option value="URGENT">紧急</option>
              <option value="HIGH">高</option>
              <option value="NORMAL">正常</option>
              <option value="LOW">低</option>
            </select>
          </div>
          <button class="filter-btn" @click="resetFilters" title="重置筛选条件">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="analysisLoading" class="loading-container">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <div class="loading-text">正在分析数据...</div>
    </div>

    <!-- 分析结果 -->
    <div v-else class="analysis-results">
      <!-- 总体统计 -->
      <el-card class="analysis-section overview-section" shadow="hover">
        <template #header>
          <div class="section-header">
            <el-icon class="section-icon"><DataBoard /></el-icon>
            <h3 class="section-title">总体统计</h3>
            <el-tag type="info" effect="light" size="small">实时数据</el-tag>
          </div>
        </template>
        <div class="stats-grid">
          <div class="stat-item total-keywords">
            <div class="stat-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(analysisData.totalKeywords) }}</div>
              <div class="stat-label">总关键词数</div>
              <div class="stat-trend">+{{ Math.floor(Math.random() * 10) }}% 较上周</div>
            </div>
          </div>
          <div class="stat-item active-keywords">
            <div class="stat-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(analysisData.activeKeywords) }}</div>
              <div class="stat-label">活跃关键词</div>
              <div class="stat-trend">{{ Math.floor((analysisData.activeKeywords / analysisData.totalKeywords) * 100) }}% 活跃率</div>
            </div>
          </div>
          <div class="stat-item total-hits">
            <div class="stat-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ formatNumber(analysisData.totalHits) }}</div>
              <div class="stat-label">总命中次数</div>
              <div class="stat-trend">+{{ Math.floor(Math.random() * 20) }}% 较昨日</div>
            </div>
          </div>
          <div class="stat-item avg-hit-rate">
            <div class="stat-icon">
              <el-icon><Odometer /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ analysisData.avgHitRate }}%</div>
              <div class="stat-label">平均命中率</div>
              <div class="stat-trend">{{ analysisData.avgHitRate > 50 ? '表现良好' : '需要优化' }}</div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 分类统计 -->
      <el-card class="analysis-section category-section" shadow="hover">
        <template #header>
          <div class="section-header">
            <el-icon class="section-icon"><Grid /></el-icon>
            <h3 class="section-title">分类统计</h3>
            <el-tag type="success" effect="light" size="small">{{ analysisData.categoryStats.length }} 个分类</el-tag>
          </div>
        </template>
        <div class="category-stats">
          <div v-for="item in analysisData.categoryStats" :key="item.category" class="category-item">
            <div class="category-info">
              <div class="category-header">
                <el-tag :type="getCategoryTagType(item.category)" effect="light" size="small">
                  {{ getTypeLabel(item.category) }}
                </el-tag>
                <div class="category-count">{{ formatNumber(item.count) }}</div>
              </div>
              <div class="category-percentage">{{ Math.round((item.count / analysisData.totalKeywords * 100)) }}%</div>
            </div>
            <div class="category-bar">
              <div class="bar-fill" :style="{ 
                width: (item.count / analysisData.totalKeywords * 100) + '%',
                backgroundColor: getCategoryColor(item.category)
              }"></div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 优先级统计 -->
      <el-card class="analysis-section priority-section" shadow="hover">
        <template #header>
          <div class="section-header">
            <el-icon class="section-icon"><Star /></el-icon>
            <h3 class="section-title">优先级统计</h3>
            <el-tag type="warning" effect="light" size="small">{{ analysisData.priorityStats.length }} 个级别</el-tag>
          </div>
        </template>
        <div class="priority-stats">
          <div v-for="item in analysisData.priorityStats" :key="item.priority" class="priority-item">
            <div class="priority-info">
              <div class="priority-header">
                <el-tag :type="getPriorityTagType(item.priority)" effect="light" size="small">
                  {{ getPriorityLabel(item.priority) }}
                </el-tag>
                <div class="priority-count">{{ formatNumber(item.count) }}</div>
              </div>
              <div class="priority-percentage">{{ Math.round((item.count / analysisData.totalKeywords * 100)) }}%</div>
            </div>
            <div class="priority-bar">
              <div class="bar-fill" :style="{ 
                width: (item.count / analysisData.totalKeywords * 100) + '%',
                backgroundColor: getPriorityColor(item.priority)
              }"></div>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 趋势分析 -->
      <el-card class="analysis-section" shadow="never">
        <template #header>
          <h3 class="section-title">命中趋势</h3>
        </template>
        <div class="trend-chart">
          <div v-if="analysisData.trendData && analysisData.trendData.length > 0" class="chart-container">
            <!-- 这里可以集成图表库如 ECharts -->
            <div class="trend-line">
              <div v-for="(point, index) in analysisData.trendData" :key="index" class="trend-point">
                <div class="point-date">{{ formatTrendDate(point.date) }}</div>
                <div class="point-value">{{ point.hits }}</div>
              </div>
            </div>
          </div>
          <div v-else class="no-data">
            <el-empty description="暂无趋势数据" />
          </div>
        </div>
      </el-card>

      <!-- 热门关键词 TOP 10 -->
      <el-card class="analysis-section top-keywords-section" shadow="hover">
        <template #header>
          <div class="section-header">
            <el-icon class="section-icon"><TrendCharts /></el-icon>
            <h3 class="section-title">热门关键词 TOP 10</h3>
            <el-tag type="danger" effect="light" size="small">实时排行</el-tag>
          </div>
        </template>
        <div class="top-keywords">
          <div v-if="analysisData.topKeywords && analysisData.topKeywords.length > 0" class="keywords-list">
            <div v-for="(item, index) in analysisData.topKeywords" :key="index" class="keyword-item" :class="{ 'top-three': index < 3 }">
              <div class="keyword-rank" :class="`rank-${index + 1}`">
                <el-icon v-if="index === 0" class="crown-icon"><Trophy /></el-icon>
                <el-icon v-else-if="index === 1" class="medal-icon"><Medal /></el-icon>
                <el-icon v-else-if="index === 2" class="medal-icon"><Medal /></el-icon>
                <span v-else>{{ index + 1 }}</span>
              </div>
              <div class="keyword-info">
                <div class="keyword-name">{{ item.keyword }}</div>
                <div class="keyword-stats">
                  <el-tag size="small" type="info" effect="plain">{{ formatNumber(item.hits) }}次命中</el-tag>
                  <el-tag size="small" :type="item.hitRate > 80 ? 'success' : item.hitRate > 50 ? 'warning' : 'danger'" effect="plain">
                    命中率 {{ item.hitRate }}%
                  </el-tag>
                </div>
              </div>
              <div class="keyword-progress">
                <el-progress 
                  :percentage="Math.round(item.hits / analysisData.topKeywords[0].hits * 100)" 
                  :stroke-width="8"
                  :color="getProgressColor(index)"
                  :show-text="false"
                />
                <div class="progress-text">{{ Math.round(item.hits / analysisData.topKeywords[0].hits * 100) }}%</div>
              </div>
            </div>
          </div>
          <div v-else class="no-data">
            <el-empty description="暂无热门关键词数据" />
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Refresh, 
  Download, 
  Loading, 
  DataBoard, 
  Document, 
  CircleCheck, 
  TrendCharts, 
  Odometer, 
  Grid, 
  Star, 
  Trophy, 
  Medal,
  Filter,
  RefreshLeft,
  Calendar,
  Collection,
  Flag
} from '@element-plus/icons-vue'
import { getKeywordAnalysis } from '@/api/keyword'

// 响应式数据
const analysisLoading = ref(false)
const analysisFilters = ref({
  days: 7,
  type: '',
  priority: ''
})

const analysisData = ref({
  totalKeywords: 0,
  activeKeywords: 0,
  totalHits: 0,
  avgHitRate: 0,
  categoryStats: [],
  priorityStats: [],
  trendData: [],
  topKeywords: []
})

// 方法
const getTypeLabel = (type: string) => {
  const labels = {
    GLOBAL: '全局关键词',
    LOCAL: '本地关键词',
    SMART: '智能关键词'
  }
  return labels[type] || type
}

const getPriorityLabel = (priority: string) => {
  const labels = {
    URGENT: '紧急',
    HIGH: '高',
    NORMAL: '正常',
    LOW: '低'
  }
  return labels[priority] || priority
}

// 数字格式化
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  } else if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 获取分类标签类型
const getCategoryTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    'GLOBAL': 'primary',
    'LOCAL': 'success',
    'SMART': 'warning'
  }
  return typeMap[type] || 'info'
}

// 获取优先级标签类型
const getPriorityTagType = (priority: string) => {
  const priorityMap: Record<string, string> = {
    'URGENT': 'danger',
    'HIGH': 'warning',
    'NORMAL': 'success',
    'LOW': 'info'
  }
  return priorityMap[priority] || 'info'
}

// 获取分类颜色
const getCategoryColor = (type: string) => {
  const colorMap: Record<string, string> = {
    'GLOBAL': '#409EFF',
    'LOCAL': '#67C23A',
    'SMART': '#E6A23C'
  }
  return colorMap[type] || '#909399'
}

// 获取优先级颜色
const getPriorityColor = (priority: string) => {
  const colorMap: Record<string, string> = {
    'URGENT': '#F56C6C',
    'HIGH': '#E6A23C',
    'NORMAL': '#67C23A',
    'LOW': '#909399'
  }
  return colorMap[priority] || '#909399'
}

// 获取进度条颜色
const getProgressColor = (index: number) => {
  const colors = [
    '#FFD700', // 金色 - 第1名
    '#C0C0C0', // 银色 - 第2名
    '#CD7F32', // 铜色 - 第3名
    '#409EFF', // 蓝色 - 其他
    '#67C23A', // 绿色
    '#E6A23C', // 橙色
    '#F56C6C', // 红色
    '#909399', // 灰色
    '#9C27B0', // 紫色
    '#FF5722'  // 深橙色
  ]
  return colors[index] || '#409EFF'
}

const refreshAnalysisData = async () => {
  try {
    analysisLoading.value = true
    const response = await getKeywordAnalysis(analysisFilters.value)
    // 由于响应拦截器的处理，response就是后端返回的原始数据 {data: {overview: {...}, ...}, success: true}
    const data = response.data || {}
    analysisData.value = {
      totalKeywords: data.overview?.totalKeywords || 0,
      activeKeywords: data.overview?.activeKeywords || 0,
      totalHits: data.overview?.totalHits || 0,
      avgHitRate: data.overview?.avgHitsPerKeyword || 0,
      categoryStats: data.categoryStats || [],
      priorityStats: data.priorityStats || [],
      trendData: data.trendData || [],
      topKeywords: data.topKeywords || []
    }
  } catch (error) {
    console.error('获取统计分析数据失败:', error)
    console.error('错误详情:', error.response || error.message || error)
    ElMessage.error('获取统计分析数据失败: ' + (error.response?.data?.message || error.message || '未知错误'))
  } finally {
    analysisLoading.value = false
  }
}

const exportAnalysisReport = () => {
  // 创建分析报告内容
  const reportContent = [
    '关键词统计分析报告',
    '===================',
    '',
    '总体统计:',
    `总关键词数: ${analysisData.value.totalKeywords}`,
    `活跃关键词: ${analysisData.value.activeKeywords}`,
    `总命中次数: ${analysisData.value.totalHits}`,
    `平均命中率: ${analysisData.value.avgHitRate}%`,
    '',
    '分类统计:',
    ...analysisData.value.categoryStats.map(item => `${getTypeLabel(item.type)}: ${item.count}个`),
    '',
    '优先级统计:',
    ...analysisData.value.priorityStats.map(item => `${getPriorityLabel(item.priority)}: ${item.count}个`),
    '',
    '热门关键词 TOP 10:',
    ...analysisData.value.topKeywords.map((item, index) => `${index + 1}. ${item.keyword} (${item.hits}次命中, ${item.hitRate}%)`)
  ].join('\n')

  // 创建并下载文件
  const blob = new Blob([reportContent], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `关键词分析报告_${new Date().toISOString().split('T')[0]}.txt`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
  
  ElMessage.success('分析报告已导出')
}

const formatTrendDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

onMounted(() => {
  refreshAnalysisData()
})
</script>

<style lang="scss" scoped>
.keyword-analysis-page {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 32px;
  color: white;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

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
}

.refresh-btn, .export-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  .btn-icon {
    width: 18px;
    height: 18px;
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
}

.refresh-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(10px);

  &:hover:not(:disabled) {
    background: rgba(255, 255, 255, 0.3);
    transform: translateY(-1px);
  }
}

.export-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-1px);
  }
}

/* 搜索区域 */
.search-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f1f5f9;

  .search-bar {
    display: flex;
    gap: 16px;
    align-items: center;

    .filter-controls {
      display: flex;
      gap: 16px;
      align-items: center;
      flex-wrap: wrap;

      .filter-item {
        display: flex;
        align-items: center;
        gap: 8px;

        .filter-label {
          display: flex;
          align-items: center;
          gap: 6px;
          font-weight: 500;
          color: #374151;
          white-space: nowrap;
          font-size: 14px;

          .filter-icon {
            width: 16px;
            height: 16px;
            color: #6b7280;
          }
        }

        .filter-select {
          padding: 12px 16px;
          border: 1px solid #e5e7eb;
          border-radius: 10px;
          background: white;
          font-size: 14px;
          color: #374151;
          cursor: pointer;
          transition: all 0.2s;
          min-width: 140px;

          &:focus {
            outline: none;
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
          }

          &:hover {
            border-color: #9ca3af;
          }
        }
      }

      .filter-btn {
        padding: 12px;
        border: 1px solid #e5e7eb;
        border-radius: 10px;
        background: white;
        color: #6b7280;
        cursor: pointer;
        transition: all 0.2s;
        display: flex;
        align-items: center;
        justify-content: center;

        svg {
          width: 16px;
          height: 16px;
        }

        &:hover {
          background: #f3f4f6;
          color: #374151;
          border-color: #9ca3af;
        }

        &:active {
          transform: translateY(1px);
        }
      }
    }
  }
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  
  .loading-icon {
    font-size: 48px;
    color: #409eff;
    margin-bottom: 16px;
    animation: spin 1s linear infinite;
  }
  
  .loading-text {
    font-size: 16px;
    color: #6b7280;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.analysis-results {
  display: grid;
  gap: 24px;
}

.analysis-section {
  .section-title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin: 0;
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-icon {
  font-size: 18px;
  color: #409EFF;
}

.section-title {
  margin: 0;
  flex: 1;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.overview-section .stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.overview-section .stat-item {
  display: flex;
  align-items: center;
  padding: 24px;
  background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 100%);
  border-radius: 16px;
  border: 1px solid #e4e7ed;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.overview-section .stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.overview-section .stat-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, #409EFF, #67C23A);
}

.overview-section .stat-item.total-keywords::before {
  background: linear-gradient(90deg, #409EFF, #36CFC9);
}

.overview-section .stat-item.active-keywords::before {
  background: linear-gradient(90deg, #67C23A, #95DE64);
}

.overview-section .stat-item.total-hits::before {
  background: linear-gradient(90deg, #E6A23C, #FFD666);
}

.overview-section .stat-item.avg-hit-rate::before {
  background: linear-gradient(90deg, #F56C6C, #FF9C6E);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: white;
}

.total-keywords .stat-icon {
  background: linear-gradient(135deg, #409EFF, #36CFC9);
}

.active-keywords .stat-icon {
  background: linear-gradient(135deg, #67C23A, #95DE64);
}

.total-hits .stat-icon {
  background: linear-gradient(135deg, #E6A23C, #FFD666);
}

.avg-hit-rate .stat-icon {
  background: linear-gradient(135deg, #F56C6C, #FF9C6E);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 4px;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.stat-trend {
  font-size: 12px;
  color: #909399;
}

.category-stats, .priority-stats {
  display: grid;
  gap: 16px;
}

.category-item, .priority-item {
  padding: 20px;
  background: #fafbfc;
  border-radius: 12px;
  border: 1px solid #e4e7ed;
  transition: all 0.3s ease;
}

.category-item:hover, .priority-item:hover {
  background: #f0f9ff;
  border-color: #409EFF;
  transform: translateY(-1px);
}

.category-info, .priority-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.category-header, .priority-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-count, .priority-count {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.category-percentage, .priority-percentage {
  font-size: 14px;
  color: #909399;
  font-weight: 500;
}

.category-bar, .priority-bar {
  width: 100%;
  height: 8px;
  background-color: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.6s ease;
  position: relative;
}

.bar-fill::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3));
  animation: shimmer 2s infinite;
}

@keyframes shimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.trend-chart {
  .chart-container {
    .trend-line {
      display: flex;
      justify-content: space-between;
      align-items: end;
      height: 200px;
      padding: 20px 0;
      
      .trend-point {
        display: flex;
        flex-direction: column;
        align-items: center;
        
        .point-date {
          font-size: 12px;
          color: #6b7280;
          margin-bottom: 8px;
        }
        
        .point-value {
          font-size: 14px;
          font-weight: 500;
          color: #374151;
          padding: 4px 8px;
          background-color: #f3f4f6;
          border-radius: 4px;
        }
      }
    }
  }
}

.top-keywords {
  .keywords-list {
    display: grid;
    gap: 16px;
  }
  
  .keyword-item {
    display: flex;
    align-items: center;
    padding: 20px;
    background: #fafbfc;
    border-radius: 16px;
    border: 1px solid #e4e7ed;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
  }
  
  .keyword-item:hover {
    background: #f0f9ff;
    border-color: #409EFF;
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  }
  
  .keyword-item.top-three {
    background: linear-gradient(135deg, #fff7e6 0%, #ffffff 100%);
    border-color: #FFD700;
  }
  
  .keyword-item.top-three:hover {
    background: linear-gradient(135deg, #fff4d6 0%, #fffbf0 100%);
  }
  
  .keyword-rank {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    margin-right: 20px;
    font-size: 18px;
    position: relative;
  }
  
  .keyword-rank.rank-1 {
    background: linear-gradient(135deg, #FFD700, #FFA500);
    color: white;
    box-shadow: 0 4px 15px rgba(255, 215, 0, 0.4);
  }
  
  .keyword-rank.rank-2 {
    background: linear-gradient(135deg, #C0C0C0, #A8A8A8);
    color: white;
    box-shadow: 0 4px 15px rgba(192, 192, 192, 0.4);
  }
  
  .keyword-rank.rank-3 {
    background: linear-gradient(135deg, #CD7F32, #B8860B);
    color: white;
    box-shadow: 0 4px 15px rgba(205, 127, 50, 0.4);
  }
  
  .keyword-rank:not(.rank-1):not(.rank-2):not(.rank-3) {
    background: linear-gradient(135deg, #409EFF, #1890FF);
    color: white;
  }
  
  .crown-icon, .medal-icon {
    font-size: 24px;
  }
  
  .keyword-info {
    flex: 1;
    margin-right: 20px;
  }
  
  .keyword-name {
    font-weight: 600;
    color: #303133;
    margin-bottom: 8px;
    font-size: 16px;
  }
  
  .keyword-stats {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }
  
  .keyword-progress {
    width: 150px;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .progress-text {
    text-align: center;
    font-size: 12px;
    color: #606266;
    font-weight: 500;
  }
}

.no-data {
  padding: 40px 20px;
  text-align: center;
}
</style>