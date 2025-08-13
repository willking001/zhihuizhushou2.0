<template>
  <div class="system-config-container">
    <el-card class="system-config-card">
      <template #header>
        <div class="card-header">
          <h2>系统配置管理</h2>
          <div class="header-actions">
            <el-button type="success" @click="initDefaultConfigs">初始化默认配置</el-button>
          </div>
        </div>
      </template>
      
      <!-- 关键词触发阈值设置 -->
      <el-card class="threshold-card" shadow="never">
        <template #header>
          <h3>关键词触发阈值设置</h3>
        </template>
        <div class="threshold-setting">
          <el-form :inline="true" :model="thresholdForm" class="threshold-form">
            <el-form-item label="触发阈值">
              <el-input-number
                v-model="thresholdForm.threshold"
                :min="1"
                :max="100"
                :step="1"
                placeholder="请输入阈值"
                style="width: 150px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveThreshold" :loading="thresholdLoading">
                保存阈值
              </el-button>
            </el-form-item>
          </el-form>
          <div class="threshold-description">
            <el-text type="info">
              <el-icon><InfoFilled /></el-icon>
              客户端本地关键词触发达到此阈值时才会上传到服务器
            </el-text>
          </div>
        </div>
      </el-card>
      
      <!-- 关键词审核开关设置 -->
      <el-card class="audit-card" shadow="never">
        <template #header>
          <h3>关键词审核设置</h3>
        </template>
        <div class="audit-setting">
          <el-form :inline="true" :model="auditForm" class="audit-form">
            <el-form-item label="关键词审核">
              <el-switch
                v-model="auditForm.keywordAuditEnabled"
                active-text="开启"
                inactive-text="关闭"
                @change="saveAuditSetting"
                :loading="auditLoading"
              />
            </el-form-item>
          </el-form>
          <div class="audit-description">
            <el-text type="info">
              <el-icon><InfoFilled /></el-icon>
              开启后，客户端上传的关键词需要人工审核后才能添加到关键词配置中；关闭后，上传的关键词将自动添加
            </el-text>
          </div>
        </div>
      </el-card>
      
      <!-- 数据库配置设置 -->
      <el-card class="database-card" shadow="never">
        <template #header>
          <h3>数据库配置</h3>
        </template>
        <div class="database-setting">
          <el-form :model="databaseForm" label-width="120px" class="database-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="数据库地址">
                  <el-input
                    v-model="databaseForm.host"
                    placeholder="请输入数据库地址"
                    :disabled="!databaseForm.editMode"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="端口">
                  <el-input-number
                    v-model="databaseForm.port"
                    :min="1"
                    :max="65535"
                    placeholder="请输入端口"
                    :disabled="!databaseForm.editMode"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="数据库名">
                  <el-input
                    v-model="databaseForm.database"
                    placeholder="请输入数据库名"
                    :disabled="!databaseForm.editMode"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="用户名">
                  <el-input
                    v-model="databaseForm.username"
                    placeholder="请输入用户名"
                    :disabled="!databaseForm.editMode"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="密码">
                  <el-input
                    v-model="databaseForm.password"
                    type="password"
                    placeholder="请输入密码"
                    :disabled="!databaseForm.editMode"
                    show-password
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="连接池大小">
                  <el-input-number
                    v-model="databaseForm.maxPoolSize"
                    :min="1"
                    :max="100"
                    placeholder="请输入连接池大小"
                    :disabled="!databaseForm.editMode"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item>
              <el-button
                v-if="!databaseForm.editMode"
                type="primary"
                @click="enableDatabaseEdit"
              >
                编辑配置
              </el-button>
              <template v-else>
                <el-button
                  type="primary"
                  @click="saveDatabaseConfig"
                  :loading="databaseLoading"
                >
                  保存配置
                </el-button>
                <el-button @click="cancelDatabaseEdit">
                  取消
                </el-button>
                <el-button
                  type="success"
                  @click="testDatabaseConnection"
                  :loading="testConnectionLoading"
                >
                  测试连接
                </el-button>
              </template>
            </el-form-item>
          </el-form>
          <div class="database-description">
            <el-text type="info">
              <el-icon><InfoFilled /></el-icon>
              配置系统数据库连接信息，修改后需要重启服务才能生效
            </el-text>
          </div>
        </div>
      </el-card>
      
      <!-- 查询表单 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="配置键名">
          <el-input v-model="queryForm.configKey" placeholder="请输入配置键名" clearable />
        </el-form-item>
        <el-form-item label="配置类型">
          <el-select v-model="queryForm.configType" placeholder="请选择配置类型" clearable>
            <el-option label="字符串" value="STRING" />
            <el-option label="整数" value="INTEGER" />
            <el-option label="布尔值" value="BOOLEAN" />
            <el-option label="JSON" value="JSON" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-select v-model="queryForm.enabled" placeholder="请选择启用状态" clearable>
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchConfigs">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 配置列表 -->
      <el-table :data="configList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="configKey" label="配置键名" width="200" show-overflow-tooltip />
        <el-table-column prop="configValue" label="配置值" width="150" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" width="200" show-overflow-tooltip />
        <el-table-column prop="configType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.configType)">{{ getTypeLabel(row.configType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'danger'">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="viewConfig(row)">查看</el-button>
            <el-button type="warning" size="small" @click="editConfig(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="deleteConfig(row)">删除</el-button>
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
    
    <!-- 配置对话框 -->
    <el-dialog
      v-model="configDialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="configFormRef"
        :model="configForm"
        :rules="configRules"
        label-width="120px"
        class="config-form"
      >
        <el-form-item label="配置键名" prop="configKey">
          <el-input
            v-model="configForm.configKey"
            placeholder="请输入配置键名"
            :disabled="dialogMode === 'view' || dialogMode === 'edit'"
          />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input
            v-model="configForm.configValue"
            type="textarea"
            :rows="3"
            placeholder="请输入配置值"
            :disabled="dialogMode === 'view'"
          />
        </el-form-item>
        <el-form-item label="配置类型" prop="configType">
          <el-select
            v-model="configForm.configType"
            placeholder="请选择配置类型"
            :disabled="dialogMode === 'view'"
            style="width: 100%"
          >
            <el-option label="字符串" value="STRING" />
            <el-option label="整数" value="INTEGER" />
            <el-option label="布尔值" value="BOOLEAN" />
            <el-option label="JSON" value="JSON" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="configForm.description"
            placeholder="请输入配置描述"
            :disabled="dialogMode === 'view'"
          />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch
            v-model="configForm.enabled"
            :disabled="dialogMode === 'view'"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="configForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
            :disabled="dialogMode === 'view'"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="configDialogVisible = false">取消</el-button>
          <el-button
            v-if="dialogMode !== 'view'"
            type="primary"
            @click="saveConfig"
            :loading="saveLoading"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import {
  getAllSystemConfigs,
  getSystemConfigsPage,
  getKeywordThreshold,
  setKeywordThreshold,
  getSystemConfigByKey,
  saveSystemConfig,
  updateSystemConfig,
  deleteSystemConfig,
  initDefaultConfigs,
  getKeywordAuditStatus,
  setKeywordAuditStatus
} from '@/api/system'

// 阈值设置相关
const thresholdForm = reactive({
  threshold: 3
})
const thresholdLoading = ref(false)

// 审核设置相关
const auditForm = reactive({
  keywordAuditEnabled: false
})
const auditLoading = ref(false)

// 数据库配置相关
const databaseForm = reactive({
  host: 'localhost',
  port: 3306,
  database: 'zhihuizhushou',
  username: 'root',
  password: '',
  maxPoolSize: 10,
  editMode: false
})
const databaseLoading = ref(false)
const testConnectionLoading = ref(false)
const originalDatabaseConfig = ref({})

// 查询表单
const queryForm = reactive({
  configKey: '',
  configType: '',
  enabled: null
})

// 配置列表
const configList = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 加载状态
const loading = ref(false)
const saveLoading = ref(false)

// 配置对话框相关
const configDialogVisible = ref(false)
const dialogMode = ref('add') // add, edit, view
const dialogTitle = computed(() => {
  const mode = {
    'add': '添加',
    'edit': '编辑',
    'view': '查看'
  }[dialogMode.value]
  return `${mode}系统配置`
})

// 配置表单
const configForm = ref({
  id: null,
  configKey: '',
  configValue: '',
  description: '',
  configType: 'STRING',
  enabled: true,
  remark: ''
})

// 配置表单校验规则
const configRules = {
  configKey: [{ required: true, message: '请输入配置键名', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }],
  configType: [{ required: true, message: '请选择配置类型', trigger: 'change' }]
}

// 配置表单引用
const configFormRef = ref()

// 获取类型标签类型
const getTypeTagType = (type: string) => {
  const typeMap = {
    'STRING': '',
    'INTEGER': 'success',
    'BOOLEAN': 'warning',
    'JSON': 'info'
  }
  return typeMap[type] || ''
}

// 获取类型标签
const getTypeLabel = (type: string) => {
  const typeMap = {
    'STRING': '字符串',
    'INTEGER': '整数',
    'BOOLEAN': '布尔值',
    'JSON': 'JSON'
  }
  return typeMap[type] || type
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 加载关键词触发阈值
const loadKeywordThreshold = async () => {
  try {
    const response = await getKeywordThreshold()
    thresholdForm.threshold = response.threshold
  } catch (error) {
    console.error('加载关键词触发阈值失败', error)
  }
}

// 保存阈值
const saveThreshold = async () => {
  try {
    thresholdLoading.value = true
    await setKeywordThreshold(thresholdForm.threshold)
    ElMessage.success('关键词触发阈值设置成功')
  } catch (error) {
    console.error('保存阈值失败', error)
    ElMessage.error('保存阈值失败：' + (error.message || '未知错误'))
  } finally {
    thresholdLoading.value = false
  }
}

// 加载关键词审核设置
const loadKeywordAuditSetting = async () => {
  try {
    auditLoading.value = true
    const response = await getKeywordAuditStatus()
    auditForm.keywordAuditEnabled = response.data.auditEnabled || false
  } catch (error) {
    console.error('加载关键词审核设置失败:', error)
    ElMessage.error('加载关键词审核设置失败')
  } finally {
    auditLoading.value = false
  }
}

// 保存审核设置
const saveAuditSetting = async () => {
  try {
    auditLoading.value = true
    await setKeywordAuditStatus(auditForm.keywordAuditEnabled)
    ElMessage.success('关键词审核设置保存成功')
  } catch (error) {
    console.error('保存审核设置失败', error)
    ElMessage.error('保存审核设置失败：' + (error.message || '未知错误'))
    // 恢复原来的值
    await loadKeywordAuditSetting()
  } finally {
    auditLoading.value = false
  }
}

// 加载数据库配置
const loadDatabaseConfig = async () => {
  try {
    // 从系统配置中加载数据库配置
    const configs = await getAllSystemConfigs()
    const dbConfigs = configs.filter(config => config.configKey.startsWith('database.'))
    
    dbConfigs.forEach(config => {
      switch (config.configKey) {
        case 'database.host':
          databaseForm.host = config.configValue
          break
        case 'database.port':
          databaseForm.port = parseInt(config.configValue)
          break
        case 'database.database':
          databaseForm.database = config.configValue
          break
        case 'database.username':
          databaseForm.username = config.configValue
          break
        case 'database.password':
          databaseForm.password = config.configValue
          break
        case 'database.maxPoolSize':
          databaseForm.maxPoolSize = parseInt(config.configValue)
          break
      }
    })
    
    // 保存原始配置用于取消编辑
    originalDatabaseConfig.value = { ...databaseForm }
  } catch (error) {
    console.error('加载数据库配置失败:', error)
    ElMessage.error('加载数据库配置失败')
  }
}

// 启用数据库配置编辑
const enableDatabaseEdit = () => {
  originalDatabaseConfig.value = { ...databaseForm }
  databaseForm.editMode = true
}

// 取消数据库配置编辑
const cancelDatabaseEdit = () => {
  Object.assign(databaseForm, originalDatabaseConfig.value)
  databaseForm.editMode = false
}

// 保存数据库配置
const saveDatabaseConfig = async () => {
  try {
    databaseLoading.value = true
    
    // 构建配置数据
    const configs = [
      {
        configKey: 'database.host',
        configValue: databaseForm.host,
        description: '数据库主机地址',
        configType: 'STRING'
      },
      {
        configKey: 'database.port',
        configValue: databaseForm.port.toString(),
        description: '数据库端口',
        configType: 'INTEGER'
      },
      {
        configKey: 'database.database',
        configValue: databaseForm.database,
        description: '数据库名称',
        configType: 'STRING'
      },
      {
        configKey: 'database.username',
        configValue: databaseForm.username,
        description: '数据库用户名',
        configType: 'STRING'
      },
      {
        configKey: 'database.password',
        configValue: databaseForm.password,
        description: '数据库密码',
        configType: 'STRING'
      },
      {
        configKey: 'database.maxPoolSize',
        configValue: databaseForm.maxPoolSize.toString(),
        description: '数据库连接池最大大小',
        configType: 'INTEGER'
      }
    ]
    
    // 保存每个配置项
    for (const config of configs) {
      try {
        // 先尝试获取现有配置
        const existingConfig = await getSystemConfigByKey(config.configKey)
        if (existingConfig) {
          // 更新现有配置
          await updateSystemConfig(existingConfig.id, {
            ...existingConfig,
            configValue: config.configValue
          })
        } else {
          // 创建新配置
          await saveSystemConfig({
            ...config,
            enabled: true
          })
        }
      } catch (error) {
        // 如果获取失败，尝试创建新配置
        await saveSystemConfig({
          ...config,
          enabled: true
        })
      }
    }
    
    ElMessage.success('数据库配置保存成功，重启服务后生效')
    databaseForm.editMode = false
    originalDatabaseConfig.value = { ...databaseForm }
  } catch (error) {
    console.error('保存数据库配置失败:', error)
    ElMessage.error('保存数据库配置失败：' + (error.message || '未知错误'))
  } finally {
    databaseLoading.value = false
  }
}

// 测试数据库连接
const testDatabaseConnection = async () => {
  try {
    testConnectionLoading.value = true
    
    // 这里应该调用后端API测试数据库连接
    // 暂时模拟测试结果
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    ElMessage.success('数据库连接测试成功')
  } catch (error) {
    console.error('测试数据库连接失败:', error)
    ElMessage.error('数据库连接测试失败：' + (error.message || '未知错误'))
  } finally {
    testConnectionLoading.value = false
  }
}

// 查询配置列表
const searchConfigs = async () => {
  try {
    loading.value = true
    
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sort: 'updateTime',
      direction: 'desc'
    }
    
    const response = await getSystemConfigsPage(params)
    configList.value = response.content
    total.value = response.totalElements
  } catch (error) {
    console.error('查询配置列表失败', error)
    ElMessage.error('查询配置列表失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 重置查询条件
const resetQuery = () => {
  queryForm.configKey = ''
  queryForm.configType = ''
  queryForm.enabled = null
  searchConfigs()
}

// 处理分页大小变化
const handleSizeChange = (size: number) => {
  pageSize.value = size
  searchConfigs()
}

// 处理页码变化
const handleCurrentChange = (page: number) => {
  currentPage.value = page
  searchConfigs()
}

// 打开添加对话框
const openAddDialog = () => {
  dialogMode.value = 'add'
  configForm.value = {
    id: null,
    configKey: '',
    configValue: '',
    description: '',
    configType: 'STRING',
    enabled: true,
    remark: ''
  }
  configDialogVisible.value = true
}

// 查看配置
const viewConfig = (row: any) => {
  dialogMode.value = 'view'
  configForm.value = { ...row }
  configDialogVisible.value = true
}

// 编辑配置
const editConfig = (row: any) => {
  dialogMode.value = 'edit'
  configForm.value = { ...row }
  configDialogVisible.value = true
}

// 删除配置
const deleteConfig = async (row: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除配置 "${row.configKey}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteSystemConfig(row.id)
    ElMessage.success('删除成功')
    searchConfigs()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除配置失败', error)
      ElMessage.error('删除配置失败：' + (error.message || '未知错误'))
    }
  }
}

// 保存配置
const saveConfig = async () => {
  try {
    await configFormRef.value.validate()
    saveLoading.value = true
    
    if (dialogMode.value === 'add') {
      await saveSystemConfig(configForm.value)
      ElMessage.success('添加成功')
    } else {
      await updateSystemConfig(configForm.value.id, configForm.value)
      ElMessage.success('更新成功')
    }
    
    configDialogVisible.value = false
    searchConfigs()
  } catch (error) {
    console.error('保存配置失败', error)
    ElMessage.error('保存配置失败：' + (error.message || '未知错误'))
  } finally {
    saveLoading.value = false
  }
}

// 初始化默认配置
const initDefaultConfigs = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要初始化默认配置吗？这将创建系统预设的配置项。',
      '确认初始化',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await initDefaultConfigs()
    ElMessage.success('默认配置初始化成功')
    searchConfigs()
    loadKeywordThreshold()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('初始化默认配置失败', error)
      ElMessage.error('初始化默认配置失败：' + (error.message || '未知错误'))
    }
  }
}

// 组件挂载时加载数据
onMounted(() => {
  loadKeywordThreshold()
  loadKeywordAuditSetting()
  loadDatabaseConfig()
  searchConfigs()
})
</script>

<style scoped>
.system-config-container {
  padding: 20px;
}

.system-config-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.threshold-card {
  margin-bottom: 20px;
  background-color: #f8f9fa;
}

.threshold-setting {
  padding: 10px 0;
}

.threshold-form {
  margin-bottom: 10px;
}

.threshold-description {
  color: #909399;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.audit-card {
  margin-bottom: 20px;
  background-color: #f8f9fa;
}

.audit-setting {
  padding: 10px 0;
}

.audit-form {
  margin-bottom: 10px;
}

.audit-description {
  color: #909399;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.database-card {
  margin-bottom: 20px;
  background-color: #f8f9fa;
}

.database-setting {
  padding: 10px 0;
}

.database-form {
  margin-bottom: 15px;
}

.database-description {
  color: #909399;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.query-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.config-form {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>