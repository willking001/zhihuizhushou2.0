<template>
  <div class="system-logs-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>系统日志</h2>
      <p>查看和管理系统运行日志</p>
    </div>

    <!-- 查询条件 -->
    <el-card class="query-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="日志级别">
          <el-select v-model="queryForm.level" placeholder="请选择日志级别" clearable>
            <el-option label="全部" value="" />
            <el-option label="ERROR" value="ERROR" />
            <el-option label="WARN" value="WARN" />
            <el-option label="INFO" value="INFO" />
            <el-option label="DEBUG" value="DEBUG" />
          </el-select>
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="queryForm.module" placeholder="请选择模块" clearable>
            <el-option label="全部" value="" />
            <el-option label="用户管理" value="user" />
            <el-option label="消息处理" value="message" />
            <el-option label="关键词分析" value="keyword" />
            <el-option label="系统配置" value="system" />
            <el-option label="业务规则" value="rule" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="queryForm.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="queryForm.keyword"
            placeholder="请输入关键词"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchLogs">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="success" @click="exportLogs">导出</el-button>
          <el-button type="danger" @click="clearLogs">清空日志</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="stat in logStats" :key="stat.level">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: stat.color }">
              <i :class="stat.icon"></i>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.count }}</div>
              <div class="stat-label">{{ stat.level }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日志列表 -->
    <el-card class="logs-card">
      <template #header>
        <div class="card-header">
          <span>日志列表</span>
          <div class="header-actions">
            <el-button type="text" @click="refreshLogs">刷新</el-button>
            <el-switch
              v-model="autoRefresh"
              active-text="自动刷新"
              @change="toggleAutoRefresh"
            />
          </div>
        </div>
      </template>
      
      <el-table
        :data="logList"
        v-loading="loading"
        stripe
        height="500"
        style="width: 100%"
      >
        <el-table-column prop="timestamp" label="时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.timestamp) }}
          </template>
        </el-table-column>
        <el-table-column prop="level" label="级别" width="80">
          <template #default="{ row }">
            <el-tag :type="getLevelTagType(row.level)" size="small">
              {{ row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="120">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.module }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="日志内容" min-width="300">
          <template #default="{ row }">
            <div class="log-message" :title="row.message">
              {{ row.message }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="150" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewLogDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="日志详情"
      width="60%"
      :close-on-click-modal="false"
    >
      <div v-if="selectedLog" class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="时间">
            {{ formatDateTime(selectedLog.timestamp) }}
          </el-descriptions-item>
          <el-descriptions-item label="级别">
            <el-tag :type="getLevelTagType(selectedLog.level)">
              {{ selectedLog.level }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="模块">
            {{ selectedLog.module }}
          </el-descriptions-item>
          <el-descriptions-item label="来源">
            {{ selectedLog.source }}
          </el-descriptions-item>
          <el-descriptions-item label="用户ID">
            {{ selectedLog.userId || '系统' }}
          </el-descriptions-item>
          <el-descriptions-item label="请求ID">
            {{ selectedLog.requestId || '-' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="log-content">
          <h4>日志内容</h4>
          <el-input
            v-model="selectedLog.message"
            type="textarea"
            :rows="6"
            readonly
          />
        </div>
        
        <div v-if="selectedLog.stackTrace" class="stack-trace">
          <h4>堆栈信息</h4>
          <el-input
            v-model="selectedLog.stackTrace"
            type="textarea"
            :rows="8"
            readonly
          />
        </div>
      </div>
      
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

// 查询表单
const queryForm = reactive({
  level: '',
  module: '',
  timeRange: [],
  keyword: ''
})

// 日志统计
const logStats = ref([
  { level: 'ERROR', count: 0, color: '#f56565', icon: 'el-icon-warning' },
  { level: 'WARN', count: 0, color: '#ed8936', icon: 'el-icon-warning-outline' },
  { level: 'INFO', count: 0, color: '#4299e1', icon: 'el-icon-info' },
  { level: 'DEBUG', count: 0, color: '#48bb78', icon: 'el-icon-success' }
])

// 日志列表
const logList = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 加载状态
const loading = ref(false)

// 自动刷新
const autoRefresh = ref(false)
let refreshInterval: number | null = null

// 日志详情对话框
const detailDialogVisible = ref(false)
const selectedLog = ref(null)

// 获取级别标签类型
const getLevelTagType = (level: string) => {
  const typeMap = {
    'ERROR': 'danger',
    'WARN': 'warning',
    'INFO': 'primary',
    'DEBUG': 'success'
  }
  return typeMap[level] || 'info'
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 生成模拟日志数据
const generateMockLogs = () => {
  const levels = ['ERROR', 'WARN', 'INFO', 'DEBUG']
  const modules = ['user', 'message', 'keyword', 'system', 'rule']
  const sources = ['UserController', 'MessageService', 'KeywordAnalyzer', 'SystemConfig', 'RuleEngine']
  const messages = [
    '用户登录成功',
    '消息处理完成',
    '关键词匹配成功',
    '系统配置更新',
    '业务规则执行',
    '数据库连接异常',
    '网络请求超时',
    '文件读取失败',
    '权限验证失败',
    '参数校验错误'
  ]
  
  const logs = []
  for (let i = 0; i < 100; i++) {
    const level = levels[Math.floor(Math.random() * levels.length)]
    const module = modules[Math.floor(Math.random() * modules.length)]
    const source = sources[Math.floor(Math.random() * sources.length)]
    const message = messages[Math.floor(Math.random() * messages.length)]
    
    logs.push({
      id: i + 1,
      timestamp: new Date(Date.now() - Math.random() * 7 * 24 * 60 * 60 * 1000).toISOString(),
      level,
      module,
      source,
      message: `${message} - ${Math.random().toString(36).substr(2, 9)}`,
      userId: Math.random() > 0.5 ? Math.floor(Math.random() * 1000) : null,
      requestId: Math.random().toString(36).substr(2, 9),
      stackTrace: level === 'ERROR' ? 'java.lang.RuntimeException: 模拟异常\n\tat com.example.Service.method(Service.java:123)' : null
    })
  }
  
  return logs.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime())
}

// 查询日志
const searchLogs = async () => {
  loading.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    const allLogs = generateMockLogs()
    let filteredLogs = allLogs
    
    // 应用过滤条件
    if (queryForm.level) {
      filteredLogs = filteredLogs.filter(log => log.level === queryForm.level)
    }
    if (queryForm.module) {
      filteredLogs = filteredLogs.filter(log => log.module === queryForm.module)
    }
    if (queryForm.keyword) {
      filteredLogs = filteredLogs.filter(log => 
        log.message.toLowerCase().includes(queryForm.keyword.toLowerCase())
      )
    }
    
    // 分页
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    logList.value = filteredLogs.slice(start, end)
    total.value = filteredLogs.length
    
    // 更新统计
    updateLogStats(allLogs)
    
  } catch (error) {
    console.error('查询日志失败:', error)
    ElMessage.error('查询日志失败')
  } finally {
    loading.value = false
  }
}

// 更新日志统计
const updateLogStats = (logs: any[]) => {
  const stats = { ERROR: 0, WARN: 0, INFO: 0, DEBUG: 0 }
  logs.forEach(log => {
    if (stats.hasOwnProperty(log.level)) {
      stats[log.level]++
    }
  })
  
  logStats.value.forEach(stat => {
    stat.count = stats[stat.level] || 0
  })
}

// 重置查询条件
const resetQuery = () => {
  queryForm.level = ''
  queryForm.module = ''
  queryForm.timeRange = []
  queryForm.keyword = ''
  currentPage.value = 1
  searchLogs()
}

// 刷新日志
const refreshLogs = () => {
  searchLogs()
}

// 切换自动刷新
const toggleAutoRefresh = (enabled: boolean) => {
  if (enabled) {
    refreshInterval = window.setInterval(() => {
      searchLogs()
    }, 10000) // 每10秒刷新一次
  } else {
    if (refreshInterval !== null) {
      window.clearInterval(refreshInterval)
      refreshInterval = null
    }
  }
}

// 导出日志
const exportLogs = async () => {
  try {
    ElMessage.success('日志导出功能开发中...')
  } catch (error) {
    ElMessage.error('导出日志失败')
  }
}

// 清空日志
const clearLogs = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有日志吗？此操作不可恢复。',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.success('日志清空功能开发中...')
  } catch {
    // 用户取消
  }
}

// 查看日志详情
const viewLogDetail = (log: any) => {
  selectedLog.value = log
  detailDialogVisible.value = true
}

// 分页处理
const handleSizeChange = (size: number) => {
  pageSize.value = size
  searchLogs()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  searchLogs()
}

// 组件挂载时初始化
onMounted(() => {
  searchLogs()
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
.system-logs-container {
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

.query-card {
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

.logs-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.log-message {
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.log-detail {
  padding: 20px 0;
}

.log-content,
.stack-trace {
  margin-top: 20px;
}

.log-content h4,
.stack-trace h4 {
  margin: 0 0 12px 0;
  color: #1e293b;
  font-size: 16px;
  font-weight: 600;
}
</style>