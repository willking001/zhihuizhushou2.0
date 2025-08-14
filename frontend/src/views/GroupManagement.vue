<template>
  <div class="group-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">群组管理</h1>
      <p class="page-description">管理和监控所有群组的状态、消息处理和统计信息</p>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon active">
          <i class="el-icon-check"></i>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statistics.activeGroups }}</div>
          <div class="stat-label">活跃群组</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon total">
          <i class="el-icon-s-data"></i>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statistics.totalGroups }}</div>
          <div class="stat-label">总群组数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon messages">
          <i class="el-icon-chat-dot-round"></i>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statistics.todayMessages }}</div>
          <div class="stat-label">今日消息</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon processing">
          <i class="el-icon-loading"></i>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ statistics.processingGroups }}</div>
          <div class="stat-label">处理中群组</div>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="search-bar">
      <div class="search-left">
        <el-input
          v-model="searchQuery"
          placeholder="搜索群组名称或ID"
          prefix-icon="el-icon-search"
          clearable
          @input="handleSearch"
        />
        <el-select v-model="statusFilter" placeholder="状态筛选" clearable>
          <el-option label="全部" value="" />
          <el-option label="自动" value="AUTO" />
          <el-option label="手动" value="MANUAL" />
          <el-option label="暂停" value="PAUSED" />
        </el-select>
      </div>
      <div class="search-right">
        <el-button type="primary" @click="refreshData">
          <i class="el-icon-refresh"></i>
          刷新
        </el-button>
        <el-button type="success" @click="showBatchOperations = true">
          <i class="el-icon-s-operation"></i>
          批量操作
        </el-button>
      </div>
    </div>

    <!-- 群组列表 -->
    <div class="table-container">
      <el-table
        :data="filteredGroups"
        v-loading="loading"
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="groupId" label="群组ID" width="120" />
        <el-table-column prop="groupName" label="群组名称" min-width="200" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              size="small"
            >
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="成员数" width="100" />
        <el-table-column prop="todayMessages" label="今日消息" width="100" />
        <el-table-column label="负责网格员" width="150">
          <template #default="{ row }">
            <span v-if="row.gridOfficerId">
              {{ getOfficerName(row.gridOfficerId) }}
            </span>
            <el-tag v-else type="info" size="small">未分配</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastActivity" label="最后活动" width="150" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewGroupDetails(row)">
              详情
            </el-button>
            <el-button size="small" type="primary" @click="configureGroup(row)">
              配置
            </el-button>
            <el-dropdown @command="(command) => handleGroupAction(command, row)">
              <el-button size="small">
                更多<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="pause">暂停</el-dropdown-item>
                  <el-dropdown-item command="resume">恢复</el-dropdown-item>
                  <el-dropdown-item command="reset">重置计数</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalGroups"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 群组详情弹窗 -->
    <el-dialog
      v-model="showGroupDetails"
      title="群组详情"
      width="800px"
      :before-close="closeGroupDetails"
    >
      <div v-if="selectedGroup" class="group-details">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <label>群组ID:</label>
              <span>{{ selectedGroup.groupId }}</span>
            </div>
            <div class="detail-item">
              <label>群组名称:</label>
              <span>{{ selectedGroup.groupName }}</span>
            </div>
            <div class="detail-item">
              <label>当前状态:</label>
              <el-tag :type="getStatusType(selectedGroup.status)">
                {{ getStatusText(selectedGroup.status) }}
              </el-tag>
            </div>
            <div class="detail-item">
              <label>成员数量:</label>
              <span>{{ selectedGroup.memberCount }}</span>
            </div>
            <div class="detail-item">
              <label>负责网格员:</label>
              <span v-if="selectedGroup.gridOfficerId">
                {{ getOfficerName(selectedGroup.gridOfficerId) }}
              </span>
              <el-tag v-else type="info" size="small">未分配</el-tag>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h3>消息统计</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <label>今日消息:</label>
              <span>{{ selectedGroup.todayMessages }}</span>
            </div>
            <div class="detail-item">
              <label>本周消息:</label>
              <span>{{ selectedGroup.weekMessages }}</span>
            </div>
            <div class="detail-item">
              <label>总消息数:</label>
              <span>{{ selectedGroup.totalMessages }}</span>
            </div>
            <div class="detail-item">
              <label>最后活动:</label>
              <span>{{ selectedGroup.lastActivity }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="closeGroupDetails">关闭</el-button>
        <el-button type="primary" @click="configureGroup(selectedGroup)">
          配置群组
        </el-button>
      </template>
    </el-dialog>

    <!-- 群组配置弹窗 -->
    <el-dialog
      v-model="showGroupConfig"
      title="群组配置"
      width="600px"
      :before-close="closeGroupConfig"
    >
      <el-form
        v-if="configForm"
        :model="configForm"
        label-width="120px"
      >
        <el-form-item label="群组状态">
          <el-radio-group v-model="configForm.status">
            <el-radio label="AUTO">自动处理</el-radio>
            <el-radio label="MANUAL">手动处理</el-radio>
            <el-radio label="PAUSED">暂停处理</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="消息处理规则">
          <el-select v-model="configForm.processingRule" placeholder="选择处理规则">
            <el-option label="默认规则" value="default" />
            <el-option label="严格模式" value="strict" />
            <el-option label="宽松模式" value="loose" />
          </el-select>
        </el-form-item>
        <el-form-item label="自动回复">
          <el-switch v-model="configForm.autoReply" />
        </el-form-item>
        <el-form-item label="关键词监控">
          <el-switch v-model="configForm.keywordMonitoring" />
        </el-form-item>
        <el-form-item label="分配网格员">
          <el-select 
            v-model="configForm.gridOfficerId" 
            placeholder="选择负责网格员"
            clearable
            filterable
          >
            <el-option
              v-for="officer in gridOfficers"
              :key="officer.id"
              :label="`${officer.officerName} (${officer.phone || '无电话'})`"
              :value="officer.id"
            />
          </el-select>
          <div v-if="configForm.gridOfficerId" class="officer-info">
            <span class="officer-detail">
              当前网格员: {{ getOfficerName(configForm.gridOfficerId) }}
            </span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeGroupConfig">取消</el-button>
        <el-button type="primary" @click="saveGroupConfig">保存配置</el-button>
      </template>
    </el-dialog>

    <!-- 批量操作弹窗 -->
    <el-dialog
      v-model="showBatchOperations"
      title="批量操作"
      width="500px"
    >
      <div class="batch-operations">
        <p>已选择 {{ selectedGroups.length }} 个群组</p>
        <div class="batch-actions">
          <el-button type="success" @click="batchOperation('resume')">
            批量恢复
          </el-button>
          <el-button type="warning" @click="batchOperation('pause')">
            批量暂停
          </el-button>
          <el-button type="info" @click="batchOperation('reset')">
            批量重置计数
          </el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="showBatchOperations = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as groupApi from '@/api/groupManagement'

// 响应式数据
const loading = ref(false)
const searchQuery = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const totalGroups = ref(0)

// 群组数据
const groups = ref([])
const selectedGroups = ref([])
const selectedGroup = ref(null)

// 网格员数据
const gridOfficers = ref([])

// 弹窗控制
const showGroupDetails = ref(false)
const showGroupConfig = ref(false)
const showBatchOperations = ref(false)

// 统计数据
const statistics = reactive({
  activeGroups: 0,
  totalGroups: 0,
  todayMessages: 0,
  processingGroups: 0
})

// 配置表单
const configForm = ref(null)

// 计算属性
const filteredGroups = computed(() => {
  let result = groups.value
  
  if (searchQuery.value) {
    result = result.filter(group => 
      group.groupName.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      group.groupId.includes(searchQuery.value)
    )
  }
  
  if (statusFilter.value) {
    result = result.filter(group => group.status === statusFilter.value)
  }
  
  return result
})

// 方法
const loadGroups = async () => {
  loading.value = true
  try {
    const response = await groupApi.getGroupList({
      page: currentPage.value,
      size: pageSize.value,
      search: searchQuery.value,
      status: statusFilter.value
    })
    groups.value = response.data.content
    totalGroups.value = response.data.totalElements
  } catch (error) {
    ElMessage.error('加载群组列表失败')
  } finally {
    loading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const response = await groupApi.getGroupStatistics()
    Object.assign(statistics, response.data)
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  }
}

const refreshData = () => {
  loadGroups()
  loadStatistics()
}

const handleSearch = () => {
  currentPage.value = 1
  loadGroups()
}

const handleSelectionChange = (selection) => {
  selectedGroups.value = selection
}

const handleSizeChange = (size) => {
  pageSize.value = size
  loadGroups()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  loadGroups()
}

const viewGroupDetails = (group) => {
  selectedGroup.value = group
  showGroupDetails.value = true
}

const configureGroup = (group) => {
  configForm.value = {
    groupId: group.groupId,
    status: group.status,
    processingRule: group.processingRule || 'default',
    autoReply: group.autoReply || false,
    keywordMonitoring: group.keywordMonitoring || true,
    gridOfficerId: group.gridOfficerId || null
  }
  showGroupConfig.value = true
  // 加载网格员列表
  loadGridOfficers()
}

const closeGroupDetails = () => {
  showGroupDetails.value = false
  selectedGroup.value = null
}

const closeGroupConfig = () => {
  showGroupConfig.value = false
  configForm.value = null
}

const saveGroupConfig = async () => {
  try {
    // 保存群组配置
    await groupApi.updateGroupConfig(configForm.value.groupId, configForm.value)
    
    // 如果网格员有变化，单独处理网格员分配
    if (configForm.value.gridOfficerId) {
      await groupApi.assignOfficerToGroup(configForm.value.groupId, {
        gridOfficerId: configForm.value.gridOfficerId,
        reason: '通过群组配置界面分配'
      })
    }
    
    ElMessage.success('配置保存成功')
    closeGroupConfig()
    refreshData()
  } catch (error) {
    ElMessage.error('配置保存失败')
  }
}

const handleGroupAction = async (action, group) => {
  try {
    switch (action) {
      case 'pause':
        await groupApi.updateGroupStatus(group.groupId, 'PAUSED')
        ElMessage.success('群组已暂停')
        break
      case 'resume':
        await groupApi.updateGroupStatus(group.groupId, 'AUTO')
        ElMessage.success('群组已恢复')
        break
      case 'reset':
        await ElMessageBox.confirm('确定要重置该群组的消息计数吗？', '确认操作')
        await groupApi.resetGroupCounters(group.groupId)
        ElMessage.success('计数已重置')
        break
    }
    refreshData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const batchOperation = async (action) => {
  if (selectedGroups.value.length === 0) {
    ElMessage.warning('请先选择要操作的群组')
    return
  }
  
  try {
    const groupIds = selectedGroups.value.map(group => group.groupId)
    
    switch (action) {
      case 'pause':
        await groupApi.batchUpdateStatus(groupIds, 'PAUSED')
        ElMessage.success('批量暂停成功')
        break
      case 'resume':
        await groupApi.batchUpdateStatus(groupIds, 'AUTO')
        ElMessage.success('批量恢复成功')
        break
      case 'reset':
        await ElMessageBox.confirm(`确定要重置 ${groupIds.length} 个群组的消息计数吗？`, '确认操作')
        await groupApi.batchResetCounters(groupIds)
        ElMessage.success('批量重置成功')
        break
    }
    
    showBatchOperations.value = false
    refreshData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量操作失败')
    }
  }
}

const getStatusType = (status) => {
  const types = {
    AUTO: 'success',
    MANUAL: 'warning',
    PAUSED: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    AUTO: '自动',
    MANUAL: '手动',
    PAUSED: '暂停'
  }
  return texts[status] || '未知'
}

// 网格员相关方法
const loadGridOfficers = async () => {
  try {
    const response = await groupApi.getGridOfficers({
      isActive: true
    })
    gridOfficers.value = response.data.data || response.data
  } catch (error) {
    ElMessage.error('加载网格员列表失败')
  }
}

const getOfficerName = (officerId) => {
  const officer = gridOfficers.value.find(o => o.id === officerId)
  return officer ? `${officer.officerName} (${officer.phone || '无电话'})` : '未分配'
}

// 生命周期
onMounted(() => {
  refreshData()
})
</script>

<style lang="scss" scoped>
.group-management {
  padding: 24px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
  
  .page-title {
    font-size: 28px;
    font-weight: 600;
    color: #2c3e50;
    margin: 0 0 8px 0;
  }
  
  .page-description {
    color: #7f8c8d;
    font-size: 14px;
    margin: 0;
  }
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  }
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
  
  &.active {
    background: linear-gradient(135deg, #67b26f, #4ca2cd);
  }
  
  &.total {
    background: linear-gradient(135deg, #667eea, #764ba2);
  }
  
  &.messages {
    background: linear-gradient(135deg, #f093fb, #f5576c);
  }
  
  &.processing {
    background: linear-gradient(135deg, #4facfe, #00f2fe);
  }
}

.stat-content {
  .stat-number {
    font-size: 32px;
    font-weight: 700;
    color: #2c3e50;
    line-height: 1;
    margin-bottom: 4px;
  }
  
  .stat-label {
    font-size: 14px;
    color: #7f8c8d;
    font-weight: 500;
  }
}

.search-bar {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.search-left {
  display: flex;
  gap: 16px;
  flex: 1;
  
  .el-input {
    width: 300px;
  }
  
  .el-select {
    width: 150px;
  }
}

.search-right {
  display: flex;
  gap: 12px;
}

.table-container {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.group-details {
  .detail-section {
    margin-bottom: 24px;
    
    h3 {
      font-size: 16px;
      font-weight: 600;
      color: #2c3e50;
      margin: 0 0 16px 0;
      padding-bottom: 8px;
      border-bottom: 1px solid #eee;
    }
  }
  
  .detail-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .detail-item {
    display: flex;
    align-items: center;
    
    label {
      font-weight: 500;
      color: #7f8c8d;
      margin-right: 8px;
      min-width: 80px;
    }
    
    span {
      color: #2c3e50;
    }
  }
}

.batch-operations {
  text-align: center;
  
  p {
    margin-bottom: 20px;
    font-size: 16px;
    color: #2c3e50;
  }
  
  .batch-actions {
    display: flex;
    justify-content: center;
    gap: 12px;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .group-management {
    padding: 16px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .search-bar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .search-left {
    flex-direction: column;
    
    .el-input,
    .el-select {
      width: 100%;
    }
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .stat-card {
    padding: 16px;
  }
  
  .stat-icon {
    width: 48px;
    height: 48px;
    font-size: 20px;
  }
  
  .stat-content .stat-number {
    font-size: 24px;
  }
  
  .table-container {
    padding: 16px;
    overflow-x: auto;
  }
}
</style>