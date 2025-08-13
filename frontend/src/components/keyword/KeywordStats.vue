<template>
  <div class="keyword-stats-container">
    <div v-if="loading" class="loading-container">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>加载统计数据中...</span>
    </div>
    
    <div v-else-if="!stats || Object.keys(stats).length === 0" class="no-data">
      <el-icon><InfoFilled /></el-icon>
      <span>暂无统计数据</span>
    </div>
    
    <div v-else class="stats-content">
      <!-- 基本信息 -->
      <div class="stats-header">
        <div class="keyword-info">
          <h3>{{ stats.keyword }}</h3>
          <div class="keyword-meta">
            <span class="category-badge" :class="stats.category.toLowerCase()">
              {{ stats.category }}
            </span>
            <span class="priority-badge" :class="`priority-${stats.priority}`">
              优先级 {{ stats.priority }}
            </span>
            <span class="status-badge" :class="stats.status === 'active' ? 'active' : 'inactive'">
              {{ stats.status === 'active' ? '已启用' : '已禁用' }}
            </span>
          </div>
        </div>
        
        <div class="stats-summary">
          <div class="stat-item">
            <div class="stat-value">{{ stats.totalHits || 0 }}</div>
            <div class="stat-label">总命中次数</div>
          </div>
          
          <div class="stat-item">
            <div class="stat-value">{{ stats.uniqueUsers || 0 }}</div>
            <div class="stat-label">影响用户数</div>
          </div>
          
          <div class="stat-item">
            <div class="stat-value">{{ stats.lastHitDate ? formatDate(stats.lastHitDate) : '无' }}</div>
            <div class="stat-label">最近命中时间</div>
          </div>
        </div>
      </div>
      
      <!-- 趋势图 -->
      <div class="trend-section">
        <h4>命中趋势 (最近7天)</h4>
        <div class="trend-chart">
          <div v-if="stats.hitTrend && stats.hitTrend.length > 0" class="chart-container">
            <!-- 这里可以使用图表库如ECharts或Chart.js -->
            <div class="bar-chart">
              <div 
                v-for="(day, index) in stats.hitTrend" 
                :key="index"
                class="chart-bar"
                :style="{ height: `${calculateBarHeight(day.count)}px` }"
                :title="`${day.date}: ${day.count}次命中`"
              >
                <div class="bar-value">{{ day.count }}</div>
              </div>
            </div>
            <div class="chart-labels">
              <div 
                v-for="(day, index) in stats.hitTrend" 
                :key="index"
                class="chart-label"
              >
                {{ formatTrendDate(day.date) }}
              </div>
            </div>
          </div>
          <div v-else class="no-trend-data">
            <el-icon><DataLine /></el-icon>
            <span>暂无趋势数据</span>
          </div>
        </div>
      </div>
      
      <!-- 用户分布 -->
      <div class="distribution-section">
        <h4>用户分布</h4>
        <div v-if="stats.userDistribution && stats.userDistribution.length > 0" class="distribution-chart">
          <div 
            v-for="(item, index) in stats.userDistribution" 
            :key="index"
            class="distribution-item"
          >
            <div class="distribution-label">{{ item.name }}</div>
            <div class="distribution-bar-container">
              <div 
                class="distribution-bar"
                :style="{ width: `${calculateDistributionWidth(item.value)}%` }"
              ></div>
              <span class="distribution-value">{{ item.value }}</span>
            </div>
          </div>
        </div>
        <div v-else class="no-distribution-data">
          <el-icon><PieChart /></el-icon>
          <span>暂无用户分布数据</span>
        </div>
      </div>
      
      <!-- 相关关键词 -->
      <div class="related-keywords-section">
        <h4>相关关键词</h4>
        <div v-if="stats.relatedKeywords && stats.relatedKeywords.length > 0" class="related-keywords-list">
          <div 
            v-for="(keyword, index) in stats.relatedKeywords" 
            :key="index"
            class="related-keyword-item"
          >
            <span class="keyword-text">{{ keyword.text }}</span>
            <span class="keyword-count">{{ keyword.count }}次</span>
          </div>
        </div>
        <div v-else class="no-related-keywords">
          <el-icon><Connection /></el-icon>
          <span>暂无相关关键词数据</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, defineProps, computed } from 'vue'
import { Loading, InfoFilled, DataLine, PieChart, Connection } from '@element-plus/icons-vue'

const props = defineProps({
  stats: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// 格式化日期
const formatDate = (date: string | Date) => {
  if (!date) return '暂无数据'
  
  const dateObj = typeof date === 'string' ? new Date(date) : date
  
  return dateObj.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化趋势图日期标签
const formatTrendDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

// 计算柱状图高度
const calculateBarHeight = (count: number) => {
  const maxHeight = 150 // 最大高度
  const minHeight = 5   // 最小高度
  
  // 获取趋势数据中的最大值
  const maxCount = Math.max(...(props.stats.hitTrend || []).map((day: any) => day.count))
  
  if (maxCount === 0) return minHeight
  
  // 计算高度比例
  const height = (count / maxCount) * maxHeight
  return Math.max(height, minHeight) // 确保至少有最小高度
}

// 计算分布图宽度百分比
const calculateDistributionWidth = (value: number) => {
  const maxValue = Math.max(...(props.stats.userDistribution || []).map((item: any) => item.value))
  
  if (maxValue === 0) return 0
  
  return (value / maxValue) * 100
}
</script>

<style lang="scss" scoped>
.keyword-stats-container {
  padding: 20px;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.loading-container,
.no-data {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
  color: #64748b;
  gap: 16px;
  
  .el-icon {
    font-size: 48px;
    color: #94a3b8;
  }
  
  span {
    font-size: 16px;
  }
}

.loading-icon {
  animation: spin 1.5s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.stats-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 20px;
  border-bottom: 1px solid #e2e8f0;
  
  .keyword-info {
    h3 {
      font-size: 24px;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 12px 0;
    }
    
    .keyword-meta {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }
  }
  
  .stats-summary {
    display: flex;
    gap: 24px;
    
    .stat-item {
      text-align: center;
      
      .stat-value {
        font-size: 24px;
        font-weight: 700;
        color: #1e293b;
      }
      
      .stat-label {
        font-size: 14px;
        color: #64748b;
        margin-top: 4px;
      }
    }
  }
}

.category-badge,
.priority-badge,
.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.category-badge {
  &.news {
    background: #dbeafe;
    color: #1d4ed8;
  }
  
  &.social {
    background: #fef3c7;
    color: #d97706;
  }
  
  &.security {
    background: #fee2e2;
    color: #dc2626;
  }
  
  &.general {
    background: #f3f4f6;
    color: #6b7280;
  }
}

.priority-badge {
  &.priority-1 {
    background: #fee2e2;
    color: #dc2626;
  }
  
  &.priority-2 {
    background: #fef3c7;
    color: #d97706;
  }
  
  &.priority-3 {
    background: #dbeafe;
    color: #1d4ed8;
  }
  
  &.priority-4 {
    background: #d1fae5;
    color: #059669;
  }
  
  &.priority-5 {
    background: #f3f4f6;
    color: #6b7280;
  }
}

.status-badge {
  &.active {
    background: #d1fae5;
    color: #059669;
  }
  
  &.inactive {
    background: #f3f4f6;
    color: #6b7280;
  }
}

.trend-section,
.distribution-section,
.related-keywords-section {
  h4 {
    font-size: 18px;
    font-weight: 600;
    color: #1e293b;
    margin: 0 0 16px 0;
  }
}

.trend-chart {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  height: 220px;
  
  .chart-container {
    height: 100%;
    display: flex;
    flex-direction: column;
  }
  
  .bar-chart {
    flex: 1;
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 12px;
    padding-bottom: 8px;
  }
  
  .chart-bar {
    flex: 1;
    background: linear-gradient(180deg, #3b82f6 0%, #1d4ed8 100%);
    border-radius: 4px 4px 0 0;
    position: relative;
    transition: all 0.3s;
    min-height: 5px;
    
    &:hover {
      background: linear-gradient(180deg, #60a5fa 0%, #3b82f6 100%);
    }
    
    .bar-value {
      position: absolute;
      top: -24px;
      left: 50%;
      transform: translateX(-50%);
      font-size: 12px;
      font-weight: 600;
      color: #1e293b;
    }
  }
  
  .chart-labels {
    display: flex;
    justify-content: space-between;
    margin-top: 8px;
    
    .chart-label {
      flex: 1;
      text-align: center;
      font-size: 12px;
      color: #64748b;
    }
  }
}

.no-trend-data,
.no-distribution-data,
.no-related-keywords {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 150px;
  color: #94a3b8;
  gap: 12px;
  
  .el-icon {
    font-size: 32px;
  }
  
  span {
    font-size: 14px;
  }
}

.distribution-chart {
  display: flex;
  flex-direction: column;
  gap: 16px;
  
  .distribution-item {
    display: flex;
    align-items: center;
    gap: 16px;
    
    .distribution-label {
      width: 100px;
      font-size: 14px;
      font-weight: 500;
      color: #1e293b;
    }
    
    .distribution-bar-container {
      flex: 1;
      display: flex;
      align-items: center;
      gap: 12px;
      
      .distribution-bar {
        height: 12px;
        background: linear-gradient(90deg, #3b82f6 0%, #1d4ed8 100%);
        border-radius: 6px;
        transition: width 0.3s;
      }
      
      .distribution-value {
        font-size: 14px;
        font-weight: 600;
        color: #1e293b;
        min-width: 30px;
      }
    }
  }
}

.related-keywords-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  
  .related-keyword-item {
    background: #f1f5f9;
    border-radius: 8px;
    padding: 8px 12px;
    display: flex;
    align-items: center;
    gap: 8px;
    transition: all 0.2s;
    
    &:hover {
      background: #e2e8f0;
    }
    
    .keyword-text {
      font-size: 14px;
      color: #1e293b;
    }
    
    .keyword-count {
      font-size: 12px;
      font-weight: 600;
      color: #64748b;
      background: rgba(100, 116, 139, 0.1);
      padding: 2px 6px;
      border-radius: 4px;
    }
  }
}

@media (max-width: 768px) {
  .stats-header {
    flex-direction: column;
    gap: 20px;
    
    .stats-summary {
      width: 100%;
      justify-content: space-between;
    }
  }
  
  .distribution-item .distribution-label {
    width: 80px;
  }
}
</style>