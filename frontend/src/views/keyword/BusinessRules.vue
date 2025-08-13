<template>
  <div class="business-rules-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="title-section">
          <h1 class="page-title">
            <el-icon class="title-icon"><Setting /></el-icon>
            智能业务规则管理
          </h1>
          <p class="page-description">配置和管理智能业务规则，提升系统自动化处理能力</p>
        </div>
        <div class="action-section">
          <el-button type="primary" size="large" @click="openAddDialog">
            <el-icon><Plus /></el-icon>
            新增规则
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <el-icon><Document /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.total }}</div>
                <div class="stat-label">总规则数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon enabled">
                <el-icon><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.enabled }}</div>
                <div class="stat-label">启用规则</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon disabled">
                <el-icon><CircleClose /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.disabled }}</div>
                <div class="stat-label">禁用规则</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-icon execution">
                <el-icon><DataAnalysis /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.executions }}</div>
                <div class="stat-label">今日执行</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 查询表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="queryForm" :inline="true" class="search-form">
        <el-form-item label="规则名称">
          <el-input
            v-model="queryForm.ruleName"
            placeholder="请输入规则名称"
            clearable
            style="width: 200px"
            @keyup.enter="searchRules"
          />
        </el-form-item>
        <el-form-item label="规则类型">
          <el-select
            v-model="queryForm.ruleType"
            placeholder="请选择规则类型"
            clearable
            style="width: 180px"
          >
            <el-option label="消息转发" value="MESSAGE_FORWARD" />
            <el-option label="自动回复" value="AUTO_REPLY" />
            <el-option label="关键词触发" value="KEYWORD_TRIGGER" />
            <el-option label="内容过滤" value="CONTENT_FILTER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="queryForm.enabled"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchRules" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="exportRules">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 规则列表 -->
    <el-card class="table-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">规则列表</span>
          <div class="header-actions">
            <el-button-group>
              <el-button 
                :type="viewMode === 'table' ? 'primary' : ''"
                @click="viewMode = 'table'"
                size="small"
              >
                <el-icon><Grid /></el-icon>
                表格视图
              </el-button>
              <el-button 
                :type="viewMode === 'card' ? 'primary' : ''"
                @click="viewMode = 'card'"
                size="small"
              >
                <el-icon><Postcard /></el-icon>
                卡片视图
              </el-button>
            </el-button-group>
          </div>
        </div>
      </template>

      <!-- 表格视图 -->
      <div v-if="viewMode === 'table'">
        <el-table 
          :data="ruleList" 
          v-loading="loading"
          stripe
          border
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column prop="id" label="ID" width="80" sortable />
          <el-table-column prop="ruleName" label="规则名称" min-width="150">
            <template #default="{ row }">
              <div class="rule-name">
                <el-tag :type="getRuleTypeColor(row.ruleType)" size="small" class="rule-type-tag">
                  {{ getRuleTypeLabel(row.ruleType) }}
                </el-tag>
                <span class="name-text">{{ row.ruleName }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="ruleDescription" label="规则描述" min-width="200" show-overflow-tooltip />
          <el-table-column prop="priority" label="优先级" width="100" sortable>
            <template #default="{ row }">
              <el-tag :type="getPriorityColor(row.priority)" size="small">
                {{ row.priority }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="enabled" label="状态" width="100">
            <template #default="{ row }">
              <el-switch
                v-model="row.enabled"
                @change="toggleRuleStatus(row)"
                :loading="row.statusLoading"
                active-color="#13ce66"
                inactive-color="#ff4949"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="openEditDialog(row)">
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button type="info" size="small" @click="viewRuleDetails(row)">
                <el-icon><View /></el-icon>
                详情
              </el-button>
              <el-button type="danger" size="small" @click="deleteRule(row)">
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 卡片视图 -->
      <div v-else class="card-view">
        <el-row :gutter="20" v-loading="loading">
          <el-col :span="8" v-for="rule in ruleList" :key="rule.id" class="rule-card-col">
            <el-card class="rule-card" shadow="hover">
              <template #header>
                <div class="rule-card-header">
                  <div class="rule-title">
                    <el-tag :type="getRuleTypeColor(rule.ruleType)" size="small">
                      {{ getRuleTypeLabel(rule.ruleType) }}
                    </el-tag>
                    <span class="rule-name">{{ rule.ruleName }}</span>
                  </div>
                  <div class="rule-actions">
                    <el-switch
                      v-model="rule.enabled"
                      @change="toggleRuleStatus(rule)"
                      :loading="rule.statusLoading"
                      size="small"
                    />
                  </div>
                </div>
              </template>
              <div class="rule-card-content">
                <p class="rule-description">{{ rule.ruleDescription || '暂无描述' }}</p>
                <div class="rule-meta">
                  <div class="meta-item">
                    <span class="meta-label">优先级:</span>
                    <el-tag :type="getPriorityColor(rule.priority)" size="small">
                      {{ rule.priority }}
                    </el-tag>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label">创建时间:</span>
                    <span class="meta-value">{{ formatDateTime(rule.createTime) }}</span>
                  </div>
                </div>
                <div class="rule-card-actions">
                  <el-button type="primary" size="small" @click="openEditDialog(rule)">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button type="info" size="small" @click="viewRuleDetails(rule)">
                    <el-icon><View /></el-icon>
                    详情
                  </el-button>
                  <el-button type="danger" size="small" @click="deleteRule(rule)">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑规则对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      :close-on-click-modal="false"
      class="rule-dialog"
    >
      <el-form
        ref="ruleFormRef"
        :model="ruleForm"
        :rules="ruleFormRules"
        label-width="120px"
        class="rule-form"
      >
        <el-tabs v-model="activeTab" class="form-tabs">
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="规则名称" prop="ruleName">
                  <el-input v-model="ruleForm.ruleName" placeholder="请输入规则名称" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="规则类型" prop="ruleType">
                  <el-select v-model="ruleForm.ruleType" placeholder="请选择规则类型" style="width: 100%">
                    <el-option label="消息转发" value="MESSAGE_FORWARD" />
                    <el-option label="自动回复" value="AUTO_REPLY" />
                    <el-option label="关键词触发" value="KEYWORD_TRIGGER" />
                    <el-option label="内容过滤" value="CONTENT_FILTER" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="优先级" prop="priority">
                  <el-input-number v-model="ruleForm.priority" :min="1" :max="100" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="启用状态">
                  <el-switch v-model="ruleForm.enabled" active-text="启用" inactive-text="禁用" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="规则描述" prop="ruleDescription">
              <el-input
                v-model="ruleForm.ruleDescription"
                type="textarea"
                :rows="3"
                placeholder="请输入规则描述"
              />
            </el-form-item>
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="生效时间">
                  <el-time-picker
                    v-model="ruleForm.startTime"
                    placeholder="开始时间"
                    format="HH:mm:ss"
                    value-format="HH:mm:ss"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="失效时间">
                  <el-time-picker
                    v-model="ruleForm.endTime"
                    placeholder="结束时间"
                    format="HH:mm:ss"
                    value-format="HH:mm:ss"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="生效日期">
              <el-checkbox-group v-model="ruleForm.effectiveDaysArray">
                <el-checkbox label="1">周一</el-checkbox>
                <el-checkbox label="2">周二</el-checkbox>
                <el-checkbox label="3">周三</el-checkbox>
                <el-checkbox label="4">周四</el-checkbox>
                <el-checkbox label="5">周五</el-checkbox>
                <el-checkbox label="6">周六</el-checkbox>
                <el-checkbox label="7">周日</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-tab-pane>
          
          <el-tab-pane label="触发条件" name="conditions">
            <div class="conditions-section">
              <div class="section-header">
                <span class="section-title">触发条件配置</span>
                <el-button type="primary" size="small" @click="addCondition">
                  <el-icon><Plus /></el-icon>
                  添加条件
                </el-button>
              </div>
              <div v-if="ruleForm.conditions.length === 0" class="empty-state">
                <el-empty description="暂无触发条件" />
              </div>
              <div v-else class="conditions-list">
                <div v-for="(condition, index) in ruleForm.conditions" :key="index" class="condition-item">
                  <el-card shadow="never" class="condition-card">
                    <template #header>
                      <div class="condition-header">
                        <span>条件 {{ index + 1 }}</span>
                        <el-button type="danger" size="small" @click="removeCondition(index)">
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </div>
                    </template>
                    <el-row :gutter="20">
                      <el-col :span="8">
                        <el-form-item label="条件类型">
                          <el-select v-model="condition.conditionType" placeholder="选择条件类型" style="width: 100%">
                            <el-option label="关键词匹配" value="KEYWORD_MATCH" />
                            <el-option label="短语匹配" value="PHRASE_MATCH" />
                            <el-option label="正则匹配" value="REGEX_MATCH" />
                            <el-option label="时间条件" value="TIME_CONDITION" />
                          </el-select>
                        </el-form-item>
                      </el-col>
                      <el-col :span="8">
                        <el-form-item label="匹配模式">
                          <el-select v-model="condition.matchMode" placeholder="选择匹配模式" style="width: 100%">
                            <el-option label="包含" value="CONTAINS" />
                            <el-option label="等于" value="EQUALS" />
                            <el-option label="开始于" value="STARTS_WITH" />
                            <el-option label="结束于" value="ENDS_WITH" />
                          </el-select>
                        </el-form-item>
                      </el-col>
                      <el-col :span="8">
                        <el-form-item label="权重">
                          <el-input-number v-model="condition.weight" :min="0" :max="10" :step="0.1" style="width: 100%" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-form-item label="条件值">
                      <el-input
                        v-model="condition.conditionValue"
                        type="textarea"
                        :rows="2"
                        placeholder="请输入条件值，多个值用逗号分隔"
                      />
                    </el-form-item>
                    <el-form-item>
                      <el-checkbox v-model="condition.caseSensitive">区分大小写</el-checkbox>
                    </el-form-item>
                  </el-card>
                </div>
              </div>
            </div>
          </el-tab-pane>
          
          <el-tab-pane label="执行动作" name="actions">
            <div class="actions-section">
              <div class="section-header">
                <span class="section-title">执行动作配置</span>
                <el-button type="primary" size="small" @click="addAction">
                  <el-icon><Plus /></el-icon>
                  添加动作
                </el-button>
              </div>
              <div v-if="ruleForm.actions.length === 0" class="empty-state">
                <el-empty description="暂无执行动作" />
              </div>
              <div v-else class="actions-list">
                <div v-for="(action, index) in ruleForm.actions" :key="index" class="action-item">
                  <el-card shadow="never" class="action-card">
                    <template #header>
                      <div class="action-header">
                        <span>动作 {{ index + 1 }}</span>
                        <el-button type="danger" size="small" @click="removeAction(index)">
                          <el-icon><Delete /></el-icon>
                        </el-button>
                      </div>
                    </template>
                    <el-row :gutter="20">
                      <el-col :span="12">
                        <el-form-item label="动作类型">
                          <el-select v-model="action.actionType" placeholder="选择动作类型" style="width: 100%">
                            <el-option label="消息转发" value="MESSAGE_FORWARD" />
                            <el-option label="自动回复" value="AUTO_REPLY" />
                            <el-option label="发送通知" value="SEND_NOTIFICATION" />
                            <el-option label="记录日志" value="LOG_ACTION" />
                          </el-select>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="执行顺序">
                          <el-input-number v-model="action.executionOrder" :min="1" style="width: 100%" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-form-item label="动作配置">
                      <el-input
                        v-model="action.actionConfig"
                        type="textarea"
                        :rows="3"
                        placeholder="请输入动作配置（JSON格式）"
                      />
                    </el-form-item>
                  </el-card>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveRule" :loading="saveLoading">
            {{ ruleForm.id ? '更新' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 规则详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="规则详情"
      width="800px"
      class="detail-dialog"
    >
      <div v-if="selectedRule" class="rule-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="规则名称">{{ selectedRule.ruleName }}</el-descriptions-item>
          <el-descriptions-item label="规则类型">
            <el-tag :type="getRuleTypeColor(selectedRule.ruleType)">
              {{ getRuleTypeLabel(selectedRule.ruleType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityColor(selectedRule.priority)">
              {{ selectedRule.priority }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedRule.enabled ? 'success' : 'danger'">
              {{ selectedRule.enabled ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间" :span="2">
            {{ formatDateTime(selectedRule.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="规则描述" :span="2">
            {{ selectedRule.ruleDescription || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-section" v-if="selectedRule.conditions && selectedRule.conditions.length > 0">
          <h4>触发条件</h4>
          <el-table :data="selectedRule.conditions" border>
            <el-table-column prop="conditionType" label="条件类型" />
            <el-table-column prop="conditionValue" label="条件值" />
            <el-table-column prop="matchMode" label="匹配模式" />
            <el-table-column prop="weight" label="权重" />
          </el-table>
        </div>
        
        <div class="detail-section" v-if="selectedRule.actions && selectedRule.actions.length > 0">
          <h4>执行动作</h4>
          <el-table :data="selectedRule.actions" border>
            <el-table-column prop="actionType" label="动作类型" />
            <el-table-column prop="executionOrder" label="执行顺序" />
            <el-table-column prop="actionConfig" label="动作配置" show-overflow-tooltip />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search, Refresh, Plus, Edit, Delete, View, Download,
  Setting, Document, CircleCheck, CircleClose, DataAnalysis,
  Grid, Postcard
} from '@element-plus/icons-vue'
import { businessRuleApi } from '@/api/businessRule'
import { formatDateTime } from '@/utils/date'

// 响应式数据
const loading = ref(false)
const saveLoading = ref(false)
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const viewMode = ref('table')
const activeTab = ref('basic')
const ruleFormRef = ref()
const selectedRows = ref([])
const selectedRule = ref(null)

// 查询表单
const queryForm = reactive({
  ruleName: '',
  ruleType: '',
  enabled: null
})

// 分页
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 规则列表
const ruleList = ref([])

// 统计数据
const statistics = reactive({
  total: 0,
  enabled: 0,
  disabled: 0,
  executions: 0
})

// 规则表单
const ruleForm = reactive({
  id: null,
  ruleName: '',
  ruleDescription: '',
  ruleType: '',
  priority: 1,
  enabled: true,
  startTime: null,
  endTime: null,
  effectiveDays: '',
  effectiveDaysArray: [],
  conditions: [],
  actions: []
})

// 表单验证规则
const ruleFormRules = {
  ruleName: [
    { required: true, message: '请输入规则名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  ruleType: [
    { required: true, message: '请选择规则类型', trigger: 'change' }
  ],
  ruleDescription: [
    { required: true, message: '请输入规则描述', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请输入优先级', trigger: 'blur' }
  ]
}

// 计算属性
const dialogTitle = computed(() => {
  return ruleForm.id ? '编辑规则' : '新增规则'
})

// 方法
const searchRules = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...queryForm
    }
    const response = await businessRuleApi.getBusinessRules(params)
    if (response.code === 20000) {
      // 确保正确处理分页数据结构
      if (response.data && response.data.content) {
        ruleList.value = response.data.content
        pagination.total = response.data.totalElements || 0
      } else if (Array.isArray(response.data)) {
        // 兼容直接返回数组的情况
        ruleList.value = response.data
        pagination.total = response.data.length
      } else {
        ruleList.value = []
        pagination.total = 0
      }
      updateStatistics()
    } else {
      ElMessage.error(response.message || '查询失败')
    }
  } catch (error) {
    console.error('查询业务规则失败:', error)
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const updateStatistics = () => {
  statistics.total = ruleList.value.length
  statistics.enabled = ruleList.value.filter(rule => rule.enabled).length
  statistics.disabled = ruleList.value.filter(rule => !rule.enabled).length
  // 这里可以添加今日执行次数的统计逻辑
  statistics.executions = Math.floor(Math.random() * 100) // 模拟数据
}

const resetQuery = () => {
  Object.assign(queryForm, {
    ruleName: '',
    ruleType: '',
    enabled: null
  })
  pagination.page = 1
  searchRules()
}

const openAddDialog = () => {
  resetRuleForm()
  dialogVisible.value = true
}

const openEditDialog = (rule: any) => {
  Object.assign(ruleForm, {
    ...rule,
    effectiveDaysArray: rule.effectiveDays ? rule.effectiveDays.split(',') : [],
    conditions: rule.conditions || [],
    actions: rule.actions || []
  })
  dialogVisible.value = true
}

const resetRuleForm = () => {
  Object.assign(ruleForm, {
    id: null,
    ruleName: '',
    ruleDescription: '',
    ruleType: '',
    priority: 1,
    enabled: true,
    startTime: null,
    endTime: null,
    effectiveDays: '',
    effectiveDaysArray: [],
    conditions: [],
    actions: []
  })
  activeTab.value = 'basic'
  ruleFormRef.value?.clearValidate()
}

const saveRule = async () => {
  if (!ruleFormRef.value) return
  
  try {
    await ruleFormRef.value.validate()
    saveLoading.value = true
    
    // 处理生效日期
    ruleForm.effectiveDays = ruleForm.effectiveDaysArray.join(',')
    
    const response = ruleForm.id 
      ? await businessRuleApi.updateBusinessRule(ruleForm.id, ruleForm)
      : await businessRuleApi.createBusinessRule(ruleForm)
    
    if (response.code === 20000) {
      ElMessage.success(ruleForm.id ? '更新成功' : '创建成功')
      dialogVisible.value = false
      searchRules()
    } else {
      ElMessage.error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存业务规则失败:', error)
    ElMessage.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

const toggleRuleStatus = async (rule: any) => {
  rule.statusLoading = true
  try {
    const response = await businessRuleApi.updateBusinessRule(rule.id, {
      ...rule,
      enabled: rule.enabled
    })
    if (response.code === 20000) {
      ElMessage.success('状态更新成功')
      updateStatistics()
    } else {
      rule.enabled = !rule.enabled // 回滚状态
      ElMessage.error(response.message || '状态更新失败')
    }
  } catch (error) {
    rule.enabled = !rule.enabled // 回滚状态
    console.error('更新规则状态失败:', error)
    ElMessage.error('状态更新失败')
  } finally {
    rule.statusLoading = false
  }
}

const deleteRule = async (rule: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除规则 "${rule.ruleName}" 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await businessRuleApi.deleteBusinessRule(rule.id)
    if (response.code === 20000) {
      ElMessage.success('删除成功')
      searchRules()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除业务规则失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const viewRuleDetails = (rule: any) => {
  selectedRule.value = rule
  detailDialogVisible.value = true
}

const exportRules = () => {
  // 导出功能实现
  ElMessage.info('导出功能开发中...')
}

const handleSelectionChange = (selection: any[]) => {
  selectedRows.value = selection
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  searchRules()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  searchRules()
}

const addCondition = () => {
  ruleForm.conditions.push({
    conditionType: '',
    conditionValue: '',
    matchMode: 'CONTAINS',
    caseSensitive: false,
    weight: 1.0
  })
}

const removeCondition = (index: number) => {
  ruleForm.conditions.splice(index, 1)
}

const addAction = () => {
  ruleForm.actions.push({
    actionType: '',
    actionConfig: '',
    executionOrder: ruleForm.actions.length + 1
  })
}

const removeAction = (index: number) => {
  ruleForm.actions.splice(index, 1)
  // 重新排序
  ruleForm.actions.forEach((action, idx) => {
    action.executionOrder = idx + 1
  })
}

const getRuleTypeLabel = (type: string) => {
  const typeMap = {
    'MESSAGE_FORWARD': '消息转发',
    'AUTO_REPLY': '自动回复',
    'KEYWORD_TRIGGER': '关键词触发',
    'CONTENT_FILTER': '内容过滤'
  }
  return typeMap[type] || type
}

const getRuleTypeColor = (type: string) => {
  const colorMap = {
    'MESSAGE_FORWARD': 'primary',
    'AUTO_REPLY': 'success',
    'KEYWORD_TRIGGER': 'warning',
    'CONTENT_FILTER': 'danger'
  }
  return colorMap[type] || 'info'
}

const getPriorityColor = (priority: number) => {
  if (priority <= 3) return 'danger'
  if (priority <= 6) return 'warning'
  return 'success'
}

// 生命周期
onMounted(() => {
  searchRules()
})
</script>

<style scoped>
.business-rules-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px;
  border-radius: 12px;
  color: white;
}

.title-section {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
}

.title-icon {
  margin-right: 12px;
  font-size: 32px;
}

.page-description {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.action-section {
  flex-shrink: 0;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
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
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.stat-icon.enabled {
  background: linear-gradient(135deg, #84fab0, #8fd3f4);
}

.stat-icon.disabled {
  background: linear-gradient(135deg, #ffecd2, #fcb69f);
}

.stat-icon.execution {
  background: linear-gradient(135deg, #a8edea, #fed6e3);
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #2c3e50;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
  margin-top: 4px;
}

.search-card {
  margin-bottom: 20px;
  border-radius: 12px;
  border: none;
}

.search-form {
  margin: 0;
}

.table-card {
  border-radius: 12px;
  border: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.rule-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rule-type-tag {
  margin-right: 8px;
}

.name-text {
  font-weight: 500;
}

.card-view {
  min-height: 400px;
}

.rule-card-col {
  margin-bottom: 20px;
}

.rule-card {
  height: 100%;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.rule-card:hover {
  transform: translateY(-2px);
}

.rule-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.rule-title {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

.rule-card-content {
  padding: 0;
}

.rule-description {
  color: #666;
  margin-bottom: 16px;
  line-height: 1.5;
}

.rule-meta {
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.meta-label {
  font-weight: 500;
  margin-right: 8px;
  color: #666;
}

.meta-value {
  color: #999;
  font-size: 14px;
}

.rule-card-actions {
  display: flex;
  gap: 8px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.rule-dialog :deep(.el-dialog__body) {
  padding: 20px;
}

.form-tabs {
  margin-top: 20px;
}

.conditions-section,
.actions-section {
  padding: 20px 0;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
}

.empty-state {
  text-align: center;
  padding: 40px 0;
}

.condition-item,
.action-item {
  margin-bottom: 16px;
}

.condition-card,
.action-card {
  border: 1px solid #e4e7ed;
}

.condition-header,
.action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.dialog-footer {
  text-align: right;
}

.detail-dialog .rule-detail {
  padding: 20px 0;
}

.detail-section {
  margin-top: 24px;
}

.detail-section h4 {
  margin-bottom: 16px;
  color: #2c3e50;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }
  
  .search-form {
    flex-direction: column;
  }
  
  .search-form .el-form-item {
    margin-right: 0;
    margin-bottom: 16px;
  }
  
  .rule-card-col {
    span: 24;
  }
}
</style>