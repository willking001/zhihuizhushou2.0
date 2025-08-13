<template>
  <div class="template-config">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">消息模板配置</h1>
          <p class="page-subtitle">管理消息转发和回复的模板配置</p>
        </div>
        <div class="header-actions">
          <button class="action-btn secondary" @click="handleExportTemplates">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
            </svg>
            导出模板
          </button>
          <button class="action-btn primary" @click="showAddDialog = true">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
            </svg>
            添加模板
          </button>
        </div>
      </div>
    </div>

    <!-- 模板类型切换 -->
    <div class="template-tabs">
      <div 
        class="template-tab" 
        :class="{ active: activeTab === 'forward' }" 
        @click="activeTab = 'forward'"
      >
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M10 9V5l-7 7 7 7v-4.1c5 0 8.5 1.6 11 5.1-1-5-4-10-11-11z"/>
        </svg>
        <span>转发消息模板</span>
      </div>
      <div 
        class="template-tab" 
        :class="{ active: activeTab === 'reply' }" 
        @click="activeTab = 'reply'"
      >
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/>
        </svg>
        <span>回复消息模板</span>
      </div>
    </div>

    <!-- 转发模板配置 -->
    <div v-if="activeTab === 'forward'" class="template-section">
      <div class="section-header">
        <h2 class="section-title">转发消息模板</h2>
        <p class="section-description">定义向用户转发消息的格式模板，包括消息头、正文结构、附件处理规则、数据脱敏配置</p>
      </div>

      <!-- 模板列表 -->
      <div class="template-list">
        <div v-for="(template, id) in forwardTemplates" :key="id" class="template-card">
          <div class="template-card-header">
            <div class="template-name">{{ template.name }}</div>
            <div class="template-actions">
              <button class="template-action-btn edit" @click="editTemplate('forward', id)">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                </svg>
              </button>
              <button class="template-action-btn delete" @click="deleteTemplateHandler('forward', id)">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                </svg>
              </button>
            </div>
          </div>
          <div class="template-card-content">
            <div class="template-preview">
              <div class="preview-label">模板预览：</div>
              <div class="preview-content">{{ template.template }}</div>
            </div>
            <div class="template-info">
              <div class="info-item">
                <span class="info-label">关键词：</span>
                <div class="keywords-list">
                  <span v-for="(keyword, index) in template.keywords" :key="index" class="keyword-tag">
                    {{ keyword }}
                  </span>
                </div>
              </div>
              <div class="info-item">
                <span class="info-label">优先级：</span>
                <span class="priority-badge" :class="getPriorityClass(template.priority)">
                  {{ getPriorityText(template.priority) }}
                </span>
              </div>
              <div class="info-item">
                <span class="info-label">状态：</span>
                <span class="status-badge" :class="{ active: template.enabled }">
                  {{ template.enabled ? '启用' : '禁用' }}
                </span>
              </div>
            </div>
          </div>
          <div class="template-card-footer">
            <div class="template-settings">
              <div class="settings-item">
                <span class="settings-label">消息头：</span>
                <span class="settings-value">{{ template.header || '默认' }}</span>
              </div>
              <div class="settings-item">
                <span class="settings-label">附件处理：</span>
                <span class="settings-value">{{ template.attachmentRule || '默认' }}</span>
              </div>
              <div class="settings-item">
                <span class="settings-label">数据脱敏：</span>
                <span class="settings-value">{{ template.dataMasking ? '已启用' : '未启用' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 添加模板卡片 -->
        <div class="template-card add-card" @click="showAddDialog = true">
          <div class="add-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
            </svg>
          </div>
          <div class="add-text">添加新模板</div>
        </div>
      </div>
    </div>

    <!-- 回复模板配置 -->
    <div v-if="activeTab === 'reply'" class="template-section">
      <div class="section-header">
        <h2 class="section-title">回复消息模板</h2>
        <p class="section-description">定义自动回复消息的格式模板，包括不同场景的回复内容和触发条件</p>
      </div>

      <!-- 模板列表 -->
      <div class="template-list">
        <div v-for="(template, id) in replyTemplates" :key="id" class="template-card">
          <div class="template-card-header">
            <div class="template-name">{{ template.name }}</div>
            <div class="template-actions">
              <button class="template-action-btn edit" @click="editTemplate('reply', id)">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                </svg>
              </button>
              <button class="template-action-btn delete" @click="deleteTemplateHandler('reply', id)">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                </svg>
              </button>
            </div>
          </div>
          <div class="template-card-content">
            <div class="template-preview">
              <div class="preview-label">模板预览：</div>
              <div class="preview-content">{{ template.template }}</div>
            </div>
            <div class="template-info">
              <div class="info-item">
                <span class="info-label">触发条件：</span>
                <div class="conditions-list">
                  <span v-for="(condition, index) in template.conditions" :key="index" class="condition-tag">
                    {{ condition }}
                  </span>
                </div>
              </div>
              <div class="info-item">
                <span class="info-label">状态：</span>
                <span class="status-badge" :class="{ active: template.enabled }">
                  {{ template.enabled ? '启用' : '禁用' }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 添加模板卡片 -->
        <div class="template-card add-card" @click="showAddDialog = true">
          <div class="add-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
            </svg>
          </div>
          <div class="add-text">添加新模板</div>
        </div>
      </div>
    </div>

    <!-- 添加/编辑模板对话框 -->
    <div v-if="showAddDialog" class="modal-overlay">
      <div class="modal-container">
        <div class="modal-header">
          <h3 class="modal-title">{{ isEditing ? '编辑模板' : '添加模板' }}</h3>
          <button class="modal-close-btn" @click="showAddDialog = false">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>
        <div class="modal-content">
          <div class="form-group">
            <label>模板类型</label>
            <div class="radio-group">
              <label class="radio-label">
                <input type="radio" v-model="editingTemplateType" value="forward">
                <span>转发模板</span>
              </label>
              <label class="radio-label">
                <input type="radio" v-model="editingTemplateType" value="reply">
                <span>回复模板</span>
              </label>
            </div>
          </div>
          <div class="form-group">
            <label>模板名称</label>
            <input type="text" v-model="editingTemplate.name" placeholder="输入模板名称">
          </div>
          <div class="form-group">
            <label>模板内容</label>
            <textarea v-model="editingTemplate.template" placeholder="输入模板内容，可使用{变量名}作为占位符"></textarea>
            <div class="template-variables">
              <div class="variables-title">可用变量：</div>
              <div class="variables-list">
                <span class="variable-tag" @click="insertVariable('original_message')">原始消息</span>
                <span class="variable-tag" @click="insertVariable('sender')">发送者</span>
                <span class="variable-tag" @click="insertVariable('time')">时间</span>
                <span class="variable-tag" @click="insertVariable('date')">日期</span>
              </div>
            </div>
          </div>
          
          <!-- 转发模板特有字段 -->
          <template v-if="editingTemplateType === 'forward'">
            <div class="form-group">
              <label>关键词</label>
              <div class="tags-input">
                <div class="tags-container">
                  <span v-for="(keyword, index) in editingTemplate.keywords" :key="index" class="tag">
                    {{ keyword }}
                    <span class="tag-remove" @click="removeKeyword(index)">×</span>
                  </span>
                </div>
                <input 
                  type="text" 
                  v-model="newKeyword" 
                  @keyup.enter="addKeyword" 
                  placeholder="输入关键词并按回车添加"
                >
              </div>
            </div>
            <div class="form-group">
              <label>优先级</label>
              <select v-model="editingTemplate.priority">
                <option :value="1">高</option>
                <option :value="2">中</option>
                <option :value="3">低</option>
              </select>
            </div>
            <div class="form-group">
              <label>消息头</label>
              <input type="text" v-model="editingTemplate.header" placeholder="输入消息头格式">
            </div>
            <div class="form-group">
              <label>附件处理规则</label>
              <select v-model="editingTemplate.attachmentRule">
                <option value="include">包含所有附件</option>
                <option value="exclude">不包含附件</option>
                <option value="first">仅包含第一个附件</option>
                <option value="custom">自定义规则</option>
              </select>
            </div>
            <div class="form-group">
              <label>数据脱敏配置</label>
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input type="checkbox" v-model="editingTemplate.dataMasking">
                  <span>启用数据脱敏</span>
                </label>
              </div>
              <div v-if="editingTemplate.dataMasking" class="masking-rules">
                <div class="masking-rule" v-for="(rule, index) in editingTemplate.maskingRules || []" :key="index">
                  <input type="text" v-model="rule.pattern" placeholder="匹配模式（正则表达式）">
                  <input type="text" v-model="rule.replacement" placeholder="替换内容">
                  <button class="remove-rule-btn" @click="removeMaskingRule(index)">删除</button>
                </div>
                <button class="add-rule-btn" @click="addMaskingRule">添加脱敏规则</button>
              </div>
            </div>
          </template>
          
          <!-- 回复模板特有字段 -->
          <template v-if="editingTemplateType === 'reply'">
            <div class="form-group">
              <label>触发条件</label>
              <div class="tags-input">
                <div class="tags-container">
                  <span v-for="(condition, index) in editingTemplate.conditions" :key="index" class="tag">
                    {{ condition }}
                    <span class="tag-remove" @click="removeCondition(index)">×</span>
                  </span>
                </div>
                <input 
                  type="text" 
                  v-model="newCondition" 
                  @keyup.enter="addCondition" 
                  placeholder="输入触发条件并按回车添加"
                >
              </div>
            </div>
          </template>
          
          <div class="form-group">
            <label>状态</label>
            <div class="checkbox-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="editingTemplate.enabled">
                <span>启用模板</span>
              </label>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="modal-btn cancel" @click="showAddDialog = false">取消</button>
          <button class="modal-btn save" @click="saveTemplate">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElLoading } from 'element-plus'
import { getTemplateList, createTemplate, updateTemplate, deleteTemplate, exportTemplates, importTemplates, MessageTemplate } from '@/api/messageTemplate'

// 模板数据
const forwardTemplates = ref<Record<string, MessageTemplate>>({})
const replyTemplates = ref<Record<string, MessageTemplate>>({})

// 界面状态
const activeTab = ref('forward')
const showAddDialog = ref(false)
const isEditing = ref(false)
const editingTemplateId = ref('')
const editingTemplateType = ref('forward')
const newKeyword = ref('')
const newCondition = ref('')

// 编辑中的模板
const editingTemplate = reactive({
  name: '',
  template: '',
  keywords: [] as string[],
  conditions: [] as string[],
  priority: 2,
  enabled: true,
  header: '',
  attachmentRule: 'include',
  dataMasking: false,
  maskingRules: [] as Array<{pattern: string, replacement: string}>
})

// 获取优先级文本
const getPriorityText = (priority: number) => {
  switch (priority) {
    case 1: return '高'
    case 2: return '中'
    case 3: return '低'
    default: return '未知'
  }
}

// 获取优先级样式类
const getPriorityClass = (priority: number) => {
  switch (priority) {
    case 1: return 'high'
    case 2: return 'medium'
    case 3: return 'low'
    default: return ''
  }
}

// 编辑模板
const editTemplate = (type: string, id: string) => {
  isEditing.value = true
  editingTemplateId.value = id
  editingTemplateType.value = type
  
  // 复制模板数据到编辑表单
  const template = type === 'forward' ? forwardTemplates.value[id] : replyTemplates.value[id]
  
  // 重置编辑表单
  Object.keys(editingTemplate).forEach(key => {
    if (key in template) {
      // @ts-ignore
      editingTemplate[key] = JSON.parse(JSON.stringify(template[key]))
    } else {
      // @ts-ignore
      editingTemplate[key] = key === 'keywords' || key === 'conditions' || key === 'maskingRules' 
        ? [] 
        : key === 'priority' 
          ? 2 
          : key === 'enabled' || key === 'dataMasking' 
            ? false 
            : ''
    }
  })
  
  showAddDialog.value = true
}

// 删除模板
const deleteTemplateHandler = (type: string, id: string) => {
  ElMessageBox.confirm('确定要删除此模板吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const loading = ElLoading.service({
      lock: true,
      text: '正在删除...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    deleteTemplate(id)
      .then(() => {
        if (type === 'forward') {
          const newTemplates = { ...forwardTemplates.value }
          delete newTemplates[id]
          forwardTemplates.value = newTemplates
        } else {
          const newTemplates = { ...replyTemplates.value }
          delete newTemplates[id]
          replyTemplates.value = newTemplates
        }
        ElMessage.success('模板已删除')
      })
      .catch(error => {
        console.error('删除模板失败:', error)
        ElMessage.error('删除模板失败')
      })
      .finally(() => {
        loading.close()
      })
  }).catch(() => {
    // 取消删除
  })
}

// 添加关键词
const addKeyword = () => {
  if (newKeyword.value.trim()) {
    editingTemplate.keywords.push(newKeyword.value.trim())
    newKeyword.value = ''
  }
}

// 移除关键词
const removeKeyword = (index: number) => {
  editingTemplate.keywords.splice(index, 1)
}

// 添加触发条件
const addCondition = () => {
  if (newCondition.value.trim()) {
    editingTemplate.conditions.push(newCondition.value.trim())
    newCondition.value = ''
  }
}

// 移除触发条件
const removeCondition = (index: number) => {
  editingTemplate.conditions.splice(index, 1)
}

// 添加脱敏规则
const addMaskingRule = () => {
  if (!editingTemplate.maskingRules) {
    editingTemplate.maskingRules = []
  }
  editingTemplate.maskingRules.push({ pattern: '', replacement: '' })
}

// 移除脱敏规则
const removeMaskingRule = (index: number) => {
  editingTemplate.maskingRules.splice(index, 1)
}

// 插入变量
const insertVariable = (variable: string) => {
  const textarea = document.querySelector('textarea') as HTMLTextAreaElement
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const text = textarea.value
    const before = text.substring(0, start)
    const after = text.substring(end, text.length)
    editingTemplate.template = `${before}{${variable}}${after}`
    // 设置光标位置
    setTimeout(() => {
      textarea.focus()
      textarea.selectionStart = textarea.selectionEnd = start + variable.length + 2
    }, 0)
  } else {
    editingTemplate.template += `{${variable}}`
  }
}

// 保存模板
const saveTemplate = () => {
  // 验证表单
  if (!editingTemplate.name.trim()) {
    ElMessage.error('请输入模板名称')
    return
  }
  
  if (!editingTemplate.template.trim()) {
    ElMessage.error('请输入模板内容')
    return
  }
  
  // 生成模板ID
  let templateId = editingTemplateId.value
  if (!isEditing.value) {
    templateId = editingTemplate.name.toLowerCase().replace(/\s+/g, '_') + '_' + Date.now()
  }
  
  const loading = ElLoading.service({
    lock: true,
    text: isEditing.value ? '正在更新...' : '正在添加...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  // 准备模板数据
  let templateData: any = {}
  
  if (editingTemplateType.value === 'forward') {
    templateData = {
      name: editingTemplate.name,
      template: editingTemplate.template,
      keywords: editingTemplate.keywords,
      priority: editingTemplate.priority,
      enabled: editingTemplate.enabled,
      header: editingTemplate.header,
      attachmentRule: editingTemplate.attachmentRule,
      dataMasking: editingTemplate.dataMasking,
      maskingRules: editingTemplate.dataMasking ? editingTemplate.maskingRules : []
    }
  } else {
    templateData = {
      name: editingTemplate.name,
      template: editingTemplate.template,
      conditions: editingTemplate.conditions,
      enabled: editingTemplate.enabled
    }
  }
  
  // 调用API保存模板
  const apiPromise = isEditing.value 
    ? updateTemplate(templateId, templateData)
    : createTemplate(editingTemplateType.value, templateData)
  
  apiPromise
    .then(response => {
      console.log('保存模板响应:', response)
      
      // 检查响应状态
      if (!response || (response.code !== undefined && response.code !== 20000)) {
        const errorMsg = response?.message || '保存失败，请重试'
        console.error('保存模板失败:', errorMsg)
        ElMessage.error(errorMsg)
        return Promise.reject(new Error(errorMsg))
      }
      
      // 更新本地数据
      const savedTemplate = response.data || templateData
      const realTemplateId = savedTemplate.id ? savedTemplate.id.toString() : templateId
      
      if (editingTemplateType.value === 'forward') {
        forwardTemplates.value = {
          ...forwardTemplates.value,
          [realTemplateId]: savedTemplate
        }
      } else {
        replyTemplates.value = {
          ...replyTemplates.value,
          [realTemplateId]: savedTemplate
        }
      }
      
      // 关闭对话框
      showAddDialog.value = false
      ElMessage.success(isEditing.value ? '模板已更新' : '模板已添加')
      
      // 重置编辑状态
      isEditing.value = false
      editingTemplateId.value = ''
    })
    .catch(error => {
      console.error('保存模板失败:', error)
      ElMessage.error(error?.message || '保存模板失败，请检查网络连接或联系管理员')
    })
    .finally(() => {
      loading.close()
    })
}

// 导出模板
const handleExportTemplates = () => {
  const loading = ElLoading.service({
    lock: true,
    text: '正在导出模板...',
    background: 'rgba(0, 0, 0, 0.7)'
  })
  
  exportTemplates() // 调用API服务中的exportTemplates方法
    .then(response => {
      const blob = new Blob([response.data], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `消息模板配置_${new Date().toISOString().slice(0, 10)}.json`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      
      ElMessage.success('模板已导出')
    })
    .catch(error => {
      console.error('导出模板失败:', error)
      ElMessage.error('导出模板失败')
    })
    .finally(() => {
      loading.close()
    })
}

// 加载模板数据
const loadTemplates = () => {
  const loadingForward = ElLoading.service({
    target: '.template-section',
    text: '加载模板中...',
    background: 'rgba(255, 255, 255, 0.8)'
  })
  
  // 加载转发模板
  getTemplateList('forward')
    .then(response => {
      console.log('转发模板响应:', response)
      if (response && response.data) {
        forwardTemplates.value = response.data || {}
      } else {
        console.error('转发模板响应格式不正确:', response)
        ElMessage.warning('转发模板数据格式不正确')
        forwardTemplates.value = {}
      }
    })
    .catch(error => {
      console.error('加载转发模板失败:', error)
      ElMessage.error('加载转发模板失败: ' + (error.message || '未知错误'))
      forwardTemplates.value = {}
    })
    .finally(() => {
      loadingForward.close()
    })
  
  // 加载回复模板
  getTemplateList('reply')
    .then(response => {
      console.log('回复模板响应:', response)
      if (response && response.data) {
        replyTemplates.value = response.data || {}
      } else {
        console.error('回复模板响应格式不正确:', response)
        ElMessage.warning('回复模板数据格式不正确')
        replyTemplates.value = {}
      }
    })
    .catch(error => {
      console.error('加载回复模板失败:', error)
      ElMessage.error('加载回复模板失败: ' + (error.message || '未知错误'))
      replyTemplates.value = {}
    })
}

// 页面加载时获取模板数据
onMounted(() => {
  loadTemplates()
})
</script>

<style lang="scss" scoped>
.template-config {
  padding: 0;
  max-width: 100%;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

// 页面头部
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

// 模板类型切换
.template-tabs {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;

  .template-tab {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 24px;
    background: white;
    border-radius: 12px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
    border: 1px solid #f1f5f9;

    svg {
      width: 20px;
      height: 20px;
      color: #64748b;
    }

    span {
      color: #64748b;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    }

    &.active {
      background: #3b82f6;
      border-color: #3b82f6;

      svg, span {
        color: white;
      }
    }
  }
}

// 模板部分
.template-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f1f5f9;

  .section-header {
    margin-bottom: 24px;

    .section-title {
      font-size: 20px;
      font-weight: 600;
      color: #1e293b;
      margin: 0 0 8px 0;
    }

    .section-description {
      font-size: 14px;
      color: #64748b;
      margin: 0;
    }
  }

  .template-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
  }

  .template-card {
    background: #f8fafc;
    border-radius: 12px;
    overflow: hidden;
    border: 1px solid #e2e8f0;
    transition: all 0.2s;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
    }

    .template-card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      background: #f1f5f9;
      border-bottom: 1px solid #e2e8f0;

      .template-name {
        font-weight: 600;
        color: #334155;
        font-size: 16px;
      }

      .template-actions {
        display: flex;
        gap: 8px;

        .template-action-btn {
          width: 32px;
          height: 32px;
          border-radius: 8px;
          display: flex;
          align-items: center;
          justify-content: center;
          border: none;
          cursor: pointer;
          transition: all 0.2s;

          svg {
            width: 16px;
            height: 16px;
          }

          &.edit {
            background: #dbeafe;
            color: #2563eb;

            &:hover {
              background: #bfdbfe;
            }
          }

          &.delete {
            background: #fee2e2;
            color: #dc2626;

            &:hover {
              background: #fecaca;
            }
          }
        }
      }
    }

    .template-card-content {
      padding: 16px;

      .template-preview {
        margin-bottom: 16px;

        .preview-label {
          font-size: 12px;
          color: #64748b;
          margin-bottom: 4px;
        }

        .preview-content {
          background: white;
          padding: 12px;
          border-radius: 8px;
          font-size: 14px;
          color: #334155;
          border: 1px solid #e2e8f0;
          white-space: pre-line;
          max-height: 100px;
          overflow-y: auto;
        }
      }

      .template-info {
        .info-item {
          display: flex;
          align-items: flex-start;
          margin-bottom: 8px;

          .info-label {
            font-size: 12px;
            color: #64748b;
            width: 60px;
            flex-shrink: 0;
          }

          .keywords-list, .conditions-list {
            display: flex;
            flex-wrap: wrap;
            gap: 4px;
          }

          .keyword-tag, .condition-tag {
            font-size: 12px;
            padding: 2px 8px;
            border-radius: 4px;
            background: #e0f2fe;
            color: #0369a1;
          }

          .priority-badge {
            font-size: 12px;
            padding: 2px 8px;
            border-radius: 4px;

            &.high {
              background: #fee2e2;
              color: #dc2626;
            }

            &.medium {
              background: #fef3c7;
              color: #d97706;
            }

            &.low {
              background: #d1fae5;
              color: #059669;
            }
          }

          .status-badge {
            font-size: 12px;
            padding: 2px 8px;
            border-radius: 4px;
            background: #f1f5f9;
            color: #64748b;

            &.active {
              background: #dcfce7;
              color: #16a34a;
            }
          }
        }
      }
    }

    .template-card-footer {
      padding: 16px;
      border-top: 1px solid #e2e8f0;
      background: #f1f5f9;

      .template-settings {
        display: flex;
        flex-wrap: wrap;
        gap: 12px;

        .settings-item {
          font-size: 12px;

          .settings-label {
            color: #64748b;
            margin-right: 4px;
          }

          .settings-value {
            color: #334155;
            font-weight: 500;
          }
        }
      }
    }

    &.add-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 32px;
      background: white;
      border: 1px dashed #cbd5e1;
      cursor: pointer;

      .add-icon {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        background: #f1f5f9;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-bottom: 16px;

        svg {
          width: 24px;
          height: 24px;
          color: #64748b;
        }
      }

      .add-text {
        font-size: 14px;
        color: #64748b;
        font-weight: 500;
      }

      &:hover {
        background: #f8fafc;
        border-color: #94a3b8;

        .add-icon {
          background: #e2e8f0;

          svg {
            color: #334155;
          }
        }

        .add-text {
          color: #334155;
        }
      }
    }
  }
}

// 模态框
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal-container {
  width: 90%;
  max-width: 700px;
  max-height: 90vh;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e2e8f0;

  .modal-title {
    font-size: 18px;
    font-weight: 600;
    color: #1e293b;
    margin: 0;
  }

  .modal-close-btn {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    background: #f1f5f9;
    color: #64748b;
    cursor: pointer;
    transition: all 0.2s;

    svg {
      width: 20px;
      height: 20px;
    }

    &:hover {
      background: #e2e8f0;
      color: #334155;
    }
  }
}

.modal-content {
  padding: 24px;
  overflow-y: auto;
  max-height: calc(90vh - 140px);

  .form-group {
    margin-bottom: 20px;

    label {
      display: block;
      font-size: 14px;
      font-weight: 500;
      color: #334155;
      margin-bottom: 8px;
    }

    input[type="text"], textarea, select {
      width: 100%;
      padding: 10px 12px;
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      font-size: 14px;
      color: #1e293b;
      background: #f8fafc;
      transition: all 0.2s;

      &:focus {
        outline: none;
        border-color: #3b82f6;
        box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        background: white;
      }

      &::placeholder {
        color: #94a3b8;
      }
    }

    textarea {
      min-height: 100px;
      resize: vertical;
    }

    .radio-group, .checkbox-group {
      display: flex;
      gap: 16px;

      .radio-label, .checkbox-label {
        display: flex;
        align-items: center;
        gap: 6px;
        cursor: pointer;

        input {
          cursor: pointer;
        }

        span {
          font-size: 14px;
          color: #334155;
        }
      }
    }

    .template-variables {
      margin-top: 8px;

      .variables-title {
        font-size: 12px;
        color: #64748b;
        margin-bottom: 4px;
      }

      .variables-list {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;

        .variable-tag {
          font-size: 12px;
          padding: 4px 8px;
          border-radius: 4px;
          background: #e0f2fe;
          color: #0369a1;
          cursor: pointer;
          transition: all 0.2s;

          &:hover {
            background: #bae6fd;
          }
        }
      }
    }

    .tags-input {
      display: flex;
      flex-direction: column;
      gap: 8px;

      .tags-container {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
        min-height: 32px;
        padding: 4px;

        .tag {
          display: flex;
          align-items: center;
          gap: 4px;
          padding: 4px 8px;
          background: #e0f2fe;
          color: #0369a1;
          border-radius: 4px;
          font-size: 12px;

          .tag-remove {
            cursor: pointer;
            font-weight: bold;
            font-size: 14px;

            &:hover {
              color: #0c4a6e;
            }
          }
        }
      }

      input {
        padding: 8px 12px;
      }
    }

    .masking-rules {
      margin-top: 12px;
      display: flex;
      flex-direction: column;
      gap: 8px;

      .masking-rule {
        display: flex;
        gap: 8px;
        align-items: center;

        input {
          flex: 1;
        }

        .remove-rule-btn {
          padding: 6px 12px;
          border: none;
          border-radius: 6px;
          background: #fee2e2;
          color: #dc2626;
          font-size: 12px;
          cursor: pointer;
          transition: all 0.2s;

          &:hover {
            background: #fecaca;
          }
        }
      }

      .add-rule-btn {
        align-self: flex-start;
        padding: 6px 12px;
        border: none;
        border-radius: 6px;
        background: #dbeafe;
        color: #2563eb;
        font-size: 12px;
        cursor: pointer;
        transition: all 0.2s;
        margin-top: 4px;

        &:hover {
          background: #bfdbfe;
        }
      }
    }
  }
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;

  .modal-btn {
    padding: 10px 20px;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;

    &.cancel {
      background: #f1f5f9;
      color: #64748b;
      border: 1px solid #cbd5e1;

      &:hover {
        background: #e2e8f0;
        color: #334155;
      }
    }

    &.save {
      background: #3b82f6;
      color: white;
      border: none;

      &:hover {
        background: #2563eb;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .page-header .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
  }

  .template-tabs {
    flex-direction: column;
  }

  .template-list {
    grid-template-columns: 1fr;
  }

  .modal-container {
    width: 95%;
    max-height: 95vh;
  }
}
</style>