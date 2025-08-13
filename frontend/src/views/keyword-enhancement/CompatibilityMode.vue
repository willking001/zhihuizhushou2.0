<template>
  <div class="compatibility-mode">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">模式管理</h1>
          <p class="page-subtitle">管理服务器权重1、客户端权重2的兼容模式，支持本地保存和自动上传审核</p>
        </div>
        <div class="header-actions">
          <!-- 可以在这里添加操作按钮 -->
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ stats.serverKeywords || 0 }}</div>
              <div class="stat-label">服务器关键词</div>
              <div class="stat-desc">权重: 1</div>
            </div>
            <el-icon class="stat-icon server"><Monitor /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ stats.clientKeywords || 0 }}</div>
              <div class="stat-label">客户端关键词</div>
              <div class="stat-desc">权重: 2</div>
            </div>
            <el-icon class="stat-icon client"><Monitor /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ stats.pendingSubmissions || 0 }}</div>
              <div class="stat-label">待审核提交</div>
              <div class="stat-desc">需要处理</div>
            </div>
            <el-icon class="stat-icon pending"><Clock /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ stats.totalTriggers || 0 }}</div>
              <div class="stat-label">总触发次数</div>
              <div class="stat-desc">累计统计</div>
            </div>
            <el-icon class="stat-icon triggers"><DataAnalysis /></el-icon>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 功能区域 -->
    <div class="function-area">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 关键词优先级查询 -->
        <el-tab-pane label="关键词优先级" name="priority">
          <div class="priority-section">
            <div class="search-form">
              <el-form :model="priorityForm" inline>
                <el-form-item label="关键词">
                  <el-input 
                    v-model="priorityForm.keyword" 
                    placeholder="请输入关键词"
                    style="width: 200px"
                  />
                </el-form-item>
                <el-form-item label="网格区域">
                  <el-input 
                    v-model="priorityForm.gridArea" 
                    placeholder="请输入网格区域"
                    style="width: 200px"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="searchPriority">查询优先级</el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <el-table 
              :data="priorityResults" 
              v-loading="priorityLoading"
              style="margin-top: 20px"
            >
              <el-table-column prop="keyword" label="关键词" />
              <el-table-column prop="sourceType" label="来源类型">
                <template #default="{ row }">
                  <el-tag :type="row.sourceType === 'SERVER' ? 'success' : 'info'">
                    {{ row.sourceType === 'SERVER' ? '服务器' : '客户端' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="weight" label="权重" />
              <el-table-column prop="hitCount" label="触发次数" />
              <el-table-column prop="gridArea" label="网格区域" />
              <el-table-column prop="priority" label="优先级">
                <template #default="{ row }">
                  <el-tag :type="getPriorityType(row.priority)">{{ row.priority }}</el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>

        <!-- 待审核提交管理 -->
        <el-tab-pane label="待审核提交" name="submissions">
          <div class="submissions-section">
            <div class="toolbar">
              <el-form :model="submissionFilter" inline>
                <el-form-item label="网格区域">
                  <el-input 
                    v-model="submissionFilter.gridArea" 
                    placeholder="筛选网格区域"
                    style="width: 200px"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="loadPendingSubmissions">刷新</el-button>
                  <el-button 
                    type="success" 
                    :disabled="selectedSubmissions.length === 0"
                    @click="batchApprove"
                  >
                    批量批准
                  </el-button>
                  <el-button 
                    type="danger" 
                    :disabled="selectedSubmissions.length === 0"
                    @click="batchReject"
                  >
                    批量拒绝
                  </el-button>
                </el-form-item>
              </el-form>
            </div>

            <el-table 
              :data="pendingSubmissions" 
              v-loading="submissionsLoading"
              @selection-change="handleSelectionChange"
              style="margin-top: 20px"
            >
              <el-table-column type="selection" width="55" />
              <el-table-column prop="keyword" label="关键词" />
              <el-table-column prop="gridArea" label="网格区域" />
              <el-table-column prop="triggerCount" label="触发次数" />
              <el-table-column prop="submissionTime" label="提交时间">
                <template #default="{ row }">
                  {{ formatDate(row.submissionTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="clientDescription" label="客户端描述" />
              <el-table-column label="操作" width="200">
                <template #default="{ row }">
                  <el-button 
                    type="success" 
                    size="small" 
                    @click="approveSubmission(row)"
                  >
                    批准
                  </el-button>
                  <el-button 
                    type="danger" 
                    size="small" 
                    @click="rejectSubmission(row)"
                  >
                    拒绝
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>


      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Monitor, Clock, DataAnalysis } from '@element-plus/icons-vue'
import keywordAPI from '@/api/keywordEnhancement'

// 响应式数据
const activeTab = ref('priority')
const stats = ref({
  serverKeywords: 0,
  clientKeywords: 0,
  pendingSubmissions: 0,
  totalTriggers: 0
})
const priorityForm = ref({ keyword: '', gridArea: '' })
const priorityResults = ref([])
const priorityLoading = ref(false)
const submissionFilter = ref({ gridArea: '' })
const pendingSubmissions = ref([])
const submissionsLoading = ref(false)
const selectedSubmissions = ref([])

// 加载统计信息
const loadStats = async () => {
  try {
    const response = await keywordAPI.compatibility.getCompatibilityStats({
      gridArea: submissionFilter.value.gridArea || undefined
    })
    stats.value = response.data || {
      serverKeywords: 0,
      clientKeywords: 0,
      pendingSubmissions: 0,
      totalTriggers: 0
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
    ElMessage.error('加载统计信息失败')
    // 保持默认值
    stats.value = {
      serverKeywords: 0,
      clientKeywords: 0,
      pendingSubmissions: 0,
      totalTriggers: 0
    }
  }
}

// 查询关键词优先级
const searchPriority = async () => {
  if (!priorityForm.value.keyword) {
    ElMessage.warning('请输入关键词')
    return
  }
  
  priorityLoading.value = true
  try {
    const response = await keywordAPI.compatibility.getKeywordPriority({
      keyword: priorityForm.value.keyword,
      gridArea: priorityForm.value.gridArea || undefined
    })
    priorityResults.value = response.data
  } catch (error) {
    console.error('查询优先级失败:', error)
    ElMessage.error('查询失败')
  } finally {
    priorityLoading.value = false
  }
}

// 加载待审核提交
const loadPendingSubmissions = async () => {
  submissionsLoading.value = true
  try {
    const response = await keywordAPI.compatibility.getPendingSubmissions({
      gridArea: submissionFilter.value.gridArea || undefined
    })
    pendingSubmissions.value = response.data
  } catch (error) {
    console.error('加载待审核提交失败:', error)
    ElMessage.error('加载失败')
  } finally {
    submissionsLoading.value = false
  }
}

// 批准提交
const approveSubmission = async (submission: any) => {
  try {
    await ElMessageBox.prompt('请输入审核备注（可选）', '批准提交', {
      confirmButtonText: '批准',
      cancelButtonText: '取消',
      inputPattern: /.*/,
      inputPlaceholder: '审核备注'
    })
    
    await keywordAPI.compatibility.approveSubmission(submission.id, {
      reviewerId: 1, // 这里应该从用户上下文获取
      reviewNotes: ''
    })
    
    ElMessage.success('批准成功')
    await loadPendingSubmissions()
    await loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批准失败:', error)
      ElMessage.error('批准失败')
    }
  }
}

// 拒绝提交
const rejectSubmission = async (submission: any) => {
  try {
    const { value: reviewNotes } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝提交', {
      confirmButtonText: '拒绝',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入拒绝原因',
      inputPlaceholder: '拒绝原因'
    })
    
    await keywordAPI.compatibility.rejectSubmission(submission.id, {
      reviewerId: 1, // 这里应该从用户上下文获取
      reviewNotes
    })
    
    ElMessage.success('拒绝成功')
    await loadPendingSubmissions()
    await loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('拒绝失败:', error)
      ElMessage.error('拒绝失败')
    }
  }
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  selectedSubmissions.value = selection
}

// 批量批准
const batchApprove = async () => {
  try {
    await ElMessageBox.confirm('确定要批量批准选中的提交吗？', '批量批准', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const submissionIds = selectedSubmissions.value.map((item: any) => item.id)
    await keywordAPI.compatibility.batchOperateSubmissions({
      submissionIds,
      operation: 'approve',
      reviewerId: 1, // 这里应该从用户上下文获取
      reviewNotes: '批量批准'
    })
    
    ElMessage.success('批量批准成功')
    await loadPendingSubmissions()
    await loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量批准失败:', error)
      ElMessage.error('批量批准失败')
    }
  }
}

// 批量拒绝
const batchReject = async () => {
  try {
    const { value: reviewNotes } = await ElMessageBox.prompt('请输入批量拒绝原因', '批量拒绝', {
      confirmButtonText: '拒绝',
      cancelButtonText: '取消',
      inputPattern: /.+/,
      inputErrorMessage: '请输入拒绝原因',
      inputPlaceholder: '拒绝原因'
    })
    
    const submissionIds = selectedSubmissions.value.map((item: any) => item.id)
    await keywordAPI.compatibility.batchOperateSubmissions({
      submissionIds,
      operation: 'reject',
      reviewerId: 1, // 这里应该从用户上下文获取
      reviewNotes
    })
    
    ElMessage.success('批量拒绝成功')
    await loadPendingSubmissions()
    await loadStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量拒绝失败:', error)
      ElMessage.error('批量拒绝失败')
    }
  }
}



// 获取优先级类型
const getPriorityType = (priority: string) => {
  const typeMap: Record<string, string> = {
    'LOW': 'info',
    'NORMAL': '',
    'HIGH': 'warning',
    'URGENT': 'danger'
  }
  return typeMap[priority] || ''
}

// 格式化日期
const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

// 组件挂载时加载数据
onMounted(() => {
  loadStats()
  loadPendingSubmissions()
})
</script>

<style scoped>
.compatibility-mode {
  padding: 20px;
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
    }
  }
}

.stats-cards {
  margin-bottom: 30px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-content {
  padding: 20px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
  margin-bottom: 4px;
}

.stat-desc {
  font-size: 12px;
  color: #909399;
}

.stat-icon {
  position: absolute;
  right: 20px;
  top: 20px;
  font-size: 40px;
  opacity: 0.3;
}

.stat-icon.server {
  color: #67c23a;
}

.stat-icon.client {
  color: #409eff;
}

.stat-icon.pending {
  color: #e6a23c;
}

.stat-icon.triggers {
  color: #f56c6c;
}

.function-area {
  background: #fff;
  border-radius: 4px;
}

.search-form {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.toolbar {
  background: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.manual-section {
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .stats-cards {
    .el-row {
      .el-col {
        margin-bottom: 20px;
      }
    }
  }
}
</style>