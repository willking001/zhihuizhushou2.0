<template>
  <div class="system-monitor-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>系统监控</h2>
      <p>实时监控系统运行状态和性能指标</p>
    </div>

    <!-- 系统状态卡片 -->
    <div class="status-cards">
      <el-row :gutter="20">
        <el-col :span="6" v-for="metric in systemMetrics" :key="metric.name">
          <el-card class="metric-card">
            <div class="metric-header">
              <span class="metric-name">{{ metric.name }}</span>
              <span class="metric-value" :style="{ color: getMetricColor(metric.value) }">
                {{ metric.value }}%
              </span>
            </div>
            <el-progress 
              :percentage="metric.value" 
              :color="getMetricColor(metric.value)"
              :show-text="false"
              :stroke-width="8"
            />
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 详细监控信息 -->
    <el-row :gutter="20" class="monitor-details">
      <!-- CPU详情 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>CPU详情</span>
              <el-button type="text" @click="refreshCpuData">刷新</el-button>
            </div>
          </template>
          <div class="detail-item">
            <span>使用率:</span>
            <span>{{ cpuDetails.usage }}%</span>
          </div>
          <div class="detail-item">
            <span>温度:</span>
            <span>{{ cpuDetails.temperature }}°C</span>
          </div>
          <div class="detail-item">
            <span>进程数:</span>
            <span>{{ cpuDetails.processes }}</span>
          </div>
        </el-card>
      </el-col>

      <!-- 内存详情 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>内存详情</span>
              <el-button type="text" @click="refreshMemoryData">刷新</el-button>
            </div>
          </template>
          <div class="detail-item">
            <span>总内存:</span>
            <span>{{ formatMemory(memoryDetails.total) }}</span>
          </div>
          <div class="detail-item">
            <span>已使用:</span>
            <span>{{ formatMemory(memoryDetails.used) }}</span>
          </div>
          <div class="detail-item">
            <span>可用:</span>
            <span>{{ formatMemory(memoryDetails.free) }}</span>
          </div>
        </el-card>
      </el-col>

      <!-- 磁盘详情 -->
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>磁盘详情</span>
              <el-button type="text" @click="refreshDiskData">刷新</el-button>
            </div>
          </template>
          <div class="detail-item">
            <span>总容量:</span>
            <span>{{ formatDisk(diskDetails.total) }}</span>
          </div>
          <div class="detail-item">
            <span>已使用:</span>
            <span>{{ formatDisk(diskDetails.used) }}</span>
          </div>
          <div class="detail-item">
            <span>可用:</span>
            <span>{{ formatDisk(diskDetails.free) }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 业务监控部分已隐藏 -->
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemStatus } from '@/api/monitor'

// 系统指标
const systemMetrics = ref([
  { name: 'CPU使用率', value: 0 },
  { name: '内存使用率', value: 0 },
  { name: '磁盘使用率', value: 0 },
  { name: '网络带宽', value: 0 }
])

// CPU详情
const cpuDetails = ref({
  usage: 0,
  temperature: 0,
  processes: 0
})

// 内存详情
const memoryDetails = ref({
  total: 0,
  used: 0,
  free: 0
})

// 磁盘详情
const diskDetails = ref({
  total: 0,
  used: 0,
  free: 0
})

// 业务监控数据已移除

// 自动刷新定时器
let refreshInterval: number | null = null

// 获取指标颜色
const getMetricColor = (value: number) => {
  if (value >= 80) return '#ef4444'
  if (value >= 60) return '#f97316'
  if (value >= 40) return '#eab308'
  return '#22c55e'
}

// 格式化内存大小
const formatMemory = (mb: number) => {
  if (mb >= 1024) {
    return `${(mb / 1024).toFixed(1)} GB`
  }
  return `${mb} MB`
}

// 格式化磁盘大小
const formatDisk = (mb: number) => {
  if (mb >= 1024 * 1024) {
    return `${(mb / (1024 * 1024)).toFixed(1)} TB`
  }
  if (mb >= 1024) {
    return `${(mb / 1024).toFixed(1)} GB`
  }
  return `${mb} MB`
}

// 获取系统状态数据
const fetchSystemStatus = async () => {
  try {
    const response = await getSystemStatus()
    if (response && response.data) {
      const data = response.data
      
      // 更新系统指标
      systemMetrics.value[0].value = data.cpu || 0
      systemMetrics.value[1].value = data.memory || 0
      systemMetrics.value[2].value = data.disk || 0
      systemMetrics.value[3].value = data.network || 0
      
      // 更新详细信息
      if (data.cpuDetails) {
        cpuDetails.value = data.cpuDetails
      }
      if (data.memoryDetails) {
        memoryDetails.value = data.memoryDetails
      }
      if (data.diskDetails) {
        diskDetails.value = data.diskDetails
      }
    }
  } catch (error) {
    console.error('获取系统状态失败:', error)
    ElMessage.error('获取系统状态失败')
  }
}

// 业务监控数据获取方法已移除

// 刷新CPU数据
const refreshCpuData = () => {
  fetchSystemStatus()
}

// 刷新内存数据
const refreshMemoryData = () => {
  fetchSystemStatus()
}

// 刷新磁盘数据
const refreshDiskData = () => {
  fetchSystemStatus()
}

// 组件挂载时初始化
onMounted(() => {
  fetchSystemStatus()
  
  // 设置自动刷新
  refreshInterval = window.setInterval(() => {
    fetchSystemStatus()
  }, 30000) // 每30秒刷新一次
})

// 组件卸载时清除定时器
onUnmounted(() => {
  if (refreshInterval !== null) {
    window.clearInterval(refreshInterval)
    refreshInterval = null
  }
})
</script>

<style scoped>
.system-monitor-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  color: #1e293b;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.status-cards {
  margin-bottom: 20px;
}

.metric-card {
  text-align: center;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.metric-name {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.metric-value {
  font-size: 18px;
  font-weight: 600;
}

.monitor-details {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}

.detail-item:last-child {
  border-bottom: none;
}

/* 业务监控相关样式已移除 */
</style>