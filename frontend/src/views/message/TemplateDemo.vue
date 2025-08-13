<template>
  <div class="template-demo">
    <div class="page-header">
      <h1 class="page-title">消息模板示例</h1>
      <p class="page-subtitle">查看和测试消息模板效果</p>
    </div>

    <!-- 模板类型选择 -->
    <div class="template-type-selector">
      <div class="selector-label">选择模板类型：</div>
      <div class="selector-options">
        <div 
          class="selector-option" 
          :class="{ active: selectedType === 'forward' }" 
          @click="selectedType = 'forward'"
        >
          转发模板
        </div>
        <div 
          class="selector-option" 
          :class="{ active: selectedType === 'reply' }" 
          @click="selectedType = 'reply'"
        >
          回复模板
        </div>
      </div>
    </div>

    <!-- 模板选择 -->
    <div class="template-selector" v-if="templates && Object.keys(templates).length > 0">
      <div class="selector-label">选择模板：</div>
      <select v-model="selectedTemplateId" class="template-select">
        <option value="">-- 请选择模板 --</option>
        <option v-for="(template, id) in templates" :key="id" :value="id">
          {{ template.name }}
        </option>
      </select>
    </div>

    <!-- 模板预览 -->
    <div class="template-preview" v-if="selectedTemplate">
      <div class="preview-header">
        <h2 class="preview-title">模板预览</h2>
        <div class="template-info">
          <div class="info-item">
            <span class="info-label">模板名称：</span>
            <span class="info-value">{{ selectedTemplate.name }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">模板类型：</span>
            <span class="info-value">{{ selectedType === 'forward' ? '转发模板' : '回复模板' }}</span>
          </div>
          <div class="info-item" v-if="selectedTemplate.header">
            <span class="info-label">消息头：</span>
            <span class="info-value">{{ selectedTemplate.header }}</span>
          </div>
        </div>
      </div>

      <div class="preview-content">
        <div class="raw-template">
          <div class="content-label">原始模板：</div>
          <div class="content-box">{{ selectedTemplate.template }}</div>
        </div>

        <div class="rendered-template">
          <div class="content-label">渲染效果：</div>
          <div class="content-box" v-html="renderedTemplate"></div>
        </div>
      </div>

      <!-- 变量输入 -->
      <div class="template-variables">
        <h3 class="variables-title">模板变量</h3>
        <p class="variables-description">填写以下变量值，查看模板渲染效果</p>

        <div class="variables-form">
          <div v-for="(value, key) in templateVariables" :key="key" class="variable-input">
            <label :for="`var-${key}`" class="variable-label">{{ formatVariableName(key) }}：</label>
            <input 
              :id="`var-${key}`" 
              v-model="templateVariables[key]" 
              class="variable-field" 
              :placeholder="`输入${formatVariableName(key)}`"
            />
          </div>
        </div>
      </div>
    </div>

    <div class="no-template-message" v-else-if="templates && Object.keys(templates).length === 0">
      <div class="message-icon">📝</div>
      <div class="message-text">当前没有{{ selectedType === 'forward' ? '转发' : '回复' }}模板</div>
      <button class="create-template-btn" @click="goToTemplateConfig">创建模板</button>
    </div>

    <div class="loading-message" v-else>
      <div class="spinner"></div>
      <div class="message-text">加载模板中...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getTemplateList } from '@/api/messageTemplate'

const router = useRouter()
const selectedType = ref('forward')
const templates = ref(null)
const selectedTemplateId = ref('')
const templateVariables = ref({})

// 获取模板列表
const fetchTemplates = async () => {
  try {
    const response = await getTemplateList(selectedType.value)
    templates.value = response.data.data
  } catch (error) {
    console.error('获取模板失败:', error)
    templates.value = {}
  }
}

// 当选择的模板类型变化时，重新获取模板列表
watch(selectedType, () => {
  fetchTemplates()
  selectedTemplateId.value = ''
  templateVariables.value = {}
})

// 当选择的模板ID变化时，提取模板变量
watch(selectedTemplateId, () => {
  if (!selectedTemplateId.value || !templates.value) {
    templateVariables.value = {}
    return
  }

  const template = templates.value[selectedTemplateId.value]
  if (!template) return

  // 提取模板中的变量 {{variable_name}}
  const variableRegex = /\{\{([^}]+)\}\}/g
  const matches = template.template.matchAll(variableRegex)
  const variables = {}

  for (const match of matches) {
    const variableName = match[1].trim()
    variables[variableName] = ''
  }

  templateVariables.value = variables
})

// 计算选中的模板
const selectedTemplate = computed(() => {
  if (!selectedTemplateId.value || !templates.value) return null
  return templates.value[selectedTemplateId.value]
})

// 计算渲染后的模板
const renderedTemplate = computed(() => {
  if (!selectedTemplate.value) return ''

  let rendered = selectedTemplate.value.template
  
  // 替换模板变量
  for (const [key, value] of Object.entries(templateVariables.value)) {
    const regex = new RegExp(`\\{\\{${key}\\}\\}`, 'g')
    rendered = rendered.replace(regex, value || `<span class="missing-variable">${key}</span>`)
  }

  // 将换行符转换为HTML换行
  rendered = rendered.replace(/\n/g, '<br>')
  
  return rendered
})

// 格式化变量名称，将下划线替换为空格，每个单词首字母大写
const formatVariableName = (name) => {
  return name
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ')
}

// 跳转到模板配置页面
const goToTemplateConfig = () => {
  router.push('/message/template-config')
}

onMounted(() => {
  fetchTemplates()
})
</script>

<style scoped>
.template-demo {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #333;
}

.page-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.template-type-selector {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
}

.selector-label {
  font-weight: 500;
  margin-right: 16px;
  color: #333;
}

.selector-options {
  display: flex;
  gap: 12px;
}

.selector-option {
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  background-color: #f5f5f5;
  transition: all 0.2s;
}

.selector-option.active {
  background-color: #1890ff;
  color: white;
}

.template-selector {
  display: flex;
  align-items: center;
  margin-bottom: 32px;
}

.template-select {
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  min-width: 300px;
  font-size: 14px;
}

.template-preview {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 24px;
  margin-top: 24px;
}

.preview-header {
  margin-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 16px;
}

.preview-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 16px 0;
  color: #333;
}

.template-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-label {
  font-weight: 500;
  color: #666;
  margin-right: 8px;
}

.info-value {
  color: #333;
}

.preview-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

.content-label {
  font-weight: 500;
  margin-bottom: 8px;
  color: #333;
}

.content-box {
  padding: 16px;
  border-radius: 4px;
  border: 1px solid #f0f0f0;
  background-color: #fafafa;
  min-height: 120px;
  white-space: pre-wrap;
}

.rendered-template .content-box {
  background-color: #f6f8fa;
}

.template-variables {
  margin-top: 32px;
  border-top: 1px solid #f0f0f0;
  padding-top: 24px;
}

.variables-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #333;
}

.variables-description {
  color: #666;
  margin: 0 0 16px 0;
}

.variables-form {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.variable-input {
  display: flex;
  flex-direction: column;
}

.variable-label {
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.variable-field {
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
  font-size: 14px;
}

.missing-variable {
  color: #ff4d4f;
  background-color: #fff1f0;
  padding: 0 4px;
  border-radius: 2px;
}

.no-template-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background-color: #fafafa;
  border-radius: 8px;
  margin-top: 24px;
}

.message-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.message-text {
  font-size: 16px;
  color: #666;
  margin-bottom: 24px;
}

.create-template-btn {
  padding: 8px 16px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.create-template-btn:hover {
  background-color: #40a9ff;
}

.loading-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  border-top: 4px solid #1890ff;
  width: 32px;
  height: 32px;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>