<template>
  <div class="group-settings">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">群组设置</h1>
          <p class="page-subtitle">配置群组的全局设置和默认行为</p>
        </div>
        <div class="header-actions">
          <button class="action-btn secondary" @click="resetToDefaults">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
            </svg>
            重置默认
          </button>
          <button class="action-btn primary" @click="saveSettings" :disabled="!hasChanges">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z"/>
            </svg>
            保存设置
          </button>
        </div>
      </div>
    </div>

    <!-- 设置内容 -->
    <div class="settings-content">
      <!-- 基本设置 -->
      <div class="settings-section">
        <div class="section-header">
          <h2>基本设置</h2>
          <p>配置群组管理的基本参数</p>
        </div>
        <div class="settings-grid">
          <div class="setting-item">
            <div class="setting-label">
              <label>默认群组状态</label>
              <span class="setting-desc">新加入群组的默认工作状态</span>
            </div>
            <div class="setting-control">
              <select v-model="settings.defaultGroupStatus" class="setting-select">
                <option value="AUTO">自动模式</option>
                <option value="MANUAL">人工接管</option>
                <option value="PAUSED">暂停服务</option>
              </select>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-label">
              <label>自动回复开关</label>
              <span class="setting-desc">是否默认启用群组自动回复功能</span>
            </div>
            <div class="setting-control">
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.defaultAutoReply">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-label">
              <label>关键词监控</label>
              <span class="setting-desc">是否默认启用关键词监控功能</span>
            </div>
            <div class="setting-control">
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.defaultKeywordMonitoring">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-label">
              <label>消息转发</label>
              <span class="setting-desc">是否默认启用消息转发功能</span>
            </div>
            <div class="setting-control">
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.defaultMessageForwarding">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- 自动回复设置 -->
      <div class="settings-section">
        <div class="section-header">
          <h2>自动回复设置</h2>
          <p>配置群组自动回复的默认行为</p>
        </div>
        <div class="settings-grid">
          <div class="setting-item full-width">
            <div class="setting-label">
              <label>默认回复模板</label>
              <span class="setting-desc">选择系统消息管理中的回复模板</span>
            </div>
            <div class="setting-control">
              <select 
                v-model="settings.defaultReplyTemplateId" 
                class="setting-select"
                :disabled="templatesLoading"
              >
                <option value="">请选择回复模板</option>
                <option 
                  v-for="(template, id) in replyTemplates" 
                  :key="id" 
                  :value="id"
                >
                  {{ template.name }}
                </option>
              </select>
            </div>
          </div>

          <div class="setting-item full-width">
            <div class="setting-label">
              <label>默认转发模板</label>
              <span class="setting-desc">选择系统消息管理中的转发模板</span>
            </div>
            <div class="setting-control">
              <select 
                v-model="settings.defaultForwardTemplateId" 
                class="setting-select"
                :disabled="templatesLoading"
              >
                <option value="">请选择转发模板</option>
                <option 
                  v-for="(template, id) in forwardTemplates" 
                  :key="id" 
                  :value="id"
                >
                  {{ template.name }}
                </option>
              </select>
            </div>
          </div>

          <div class="setting-item full-width">
            <div class="setting-label">
              <label>模拟人工回复延迟</label>
              <span class="setting-desc">设置回复延迟时间范围，系统将随机选择该范围内的时间进行回复</span>
            </div>
            <div class="setting-control reply-delay-range">
              <div class="delay-input-group">
                <label class="delay-label">最小延迟：</label>
                <input 
                  type="number" 
                  v-model.number="settings.replyDelayMin" 
                  class="setting-input delay-input"
                  min="1"
                  max="300"
                  placeholder="最小秒数"
                >
                <span class="delay-unit">秒</span>
              </div>
              <div class="delay-separator">到</div>
              <div class="delay-input-group">
                <label class="delay-label">最大延迟：</label>
                <input 
                  type="number" 
                  v-model.number="settings.replyDelayMax" 
                  class="setting-input delay-input"
                  min="1"
                  max="300"
                  placeholder="最大秒数"
                >
                <span class="delay-unit">秒</span>
              </div>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-label">
              <label>每日回复限制</label>
              <span class="setting-desc">每个群组每日最大回复次数</span>
            </div>
            <div class="setting-control">
              <input 
                type="number" 
                v-model.number="settings.dailyReplyLimit" 
                class="setting-input"
                min="0"
                placeholder="回复限制"
              >
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-label">
              <label>智能回复</label>
              <span class="setting-desc">是否启用AI智能回复功能</span>
            </div>
            <div class="setting-control">
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.enableSmartReply">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>
        </div>
      </div>

      <!-- 监控设置 -->
      <div class="settings-section">
        <div class="section-header">
          <h2>监控设置</h2>
          <p>配置群组监控和告警参数</p>
        </div>
        <div class="settings-grid">
          <div class="setting-item">
            <div class="setting-label">
              <label>消息频率告警</label>
              <span class="setting-desc">每小时消息数超过此值时告警</span>
            </div>
            <div class="setting-control">
              <input 
                type="number" 
                v-model.number="settings.messageFrequencyAlert" 
                class="setting-input"
                min="0"
                placeholder="消息数量"
              >
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-label">
              <label>异常关键词告警</label>
              <span class="setting-desc">检测到敏感关键词时是否告警</span>
            </div>
            <div class="setting-control">
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.keywordAlert">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-label">
              <label>群组活跃度监控</label>
              <span class="setting-desc">监控群组活跃度变化</span>
            </div>
            <div class="setting-control">
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.activityMonitoring">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-label">
              <label>告警通知方式</label>
              <span class="setting-desc">选择告警通知的发送方式</span>
            </div>
            <div class="setting-control">
              <select v-model="settings.alertNotificationMethod" class="setting-select">
                <option value="email">邮件通知</option>
                <option value="sms">短信通知</option>
                <option value="wechat">微信通知</option>
                <option value="all">全部方式</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <!-- 高级设置 -->
      <div class="settings-section">
        <div class="section-header">
          <h2>高级设置</h2>
          <p>配置群组管理的高级功能</p>
        </div>
        <div class="settings-grid">
          <div class="setting-item">
            <div class="setting-label">
              <label>数据保留期限</label>
              <span class="setting-desc">群组消息数据保留天数</span>
            </div>
            <div class="setting-control">
              <input 
                type="number" 
                v-model.number="settings.dataRetentionDays" 
                class="setting-input"
                min="1"
                placeholder="保留天数"
              >
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-label">
              <label>自动清理</label>
              <span class="setting-desc">是否自动清理过期数据</span>
            </div>
            <div class="setting-control">
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.autoCleanup">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-label">
              <label>API访问限制</label>
              <span class="setting-desc">每分钟API调用次数限制</span>
            </div>
            <div class="setting-control">
              <input 
                type="number" 
                v-model.number="settings.apiRateLimit" 
                class="setting-input"
                min="1"
                placeholder="调用次数"
              >
            </div>
          </div>

          <div class="setting-item">
            <div class="setting-label">
              <label>调试模式</label>
              <span class="setting-desc">启用调试模式以获取详细日志</span>
            </div>
            <div class="setting-control">
              <label class="toggle-switch">
                <input type="checkbox" v-model="settings.debugMode">
                <span class="toggle-slider"></span>
              </label>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 保存确认对话框 -->
    <div v-if="showSaveDialog" class="save-dialog">
      <div class="dialog-overlay" @click="showSaveDialog = false"></div>
      <div class="dialog-container">
        <div class="dialog-header">
          <h3>保存设置</h3>
        </div>
        <div class="dialog-content">
          <p>确定要保存当前的群组设置吗？这些设置将应用到所有新创建的群组。</p>
        </div>
        <div class="dialog-footer">
          <button class="btn secondary" @click="showSaveDialog = false">取消</button>
          <button class="btn primary" @click="confirmSave">确定保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { groupManagementApi } from '@/api/groupManagement'
import { getTemplateList } from '@/api/messageTemplate'

// 响应式数据
const settings = reactive({
  // 基本设置
  defaultGroupStatus: 'AUTO',
  defaultAutoReply: true,
  defaultKeywordMonitoring: true,
  defaultMessageForwarding: false,
  
  // 自动回复设置
  defaultReplyTemplateId: '', // 使用模板ID而不是直接的模板内容
  defaultForwardTemplateId: '', // 转发模板ID
  replyDelay: 3,
  dailyReplyLimit: 100,
  enableSmartReply: true,
  
  // 监控设置
  messageFrequencyAlert: 50,
  keywordAlert: true,
  activityMonitoring: true,
  alertNotificationMethod: 'email',
  
  // 高级设置
  dataRetentionDays: 30,
  autoCleanup: true,
  apiRateLimit: 60,
  debugMode: false
})

const originalSettings = ref({})
const showSaveDialog = ref(false)
const loading = ref(false)

// 模板相关数据
const replyTemplates = ref({})
const forwardTemplates = ref({})
const templatesLoading = ref(false)

// 计算属性
const hasChanges = computed(() => {
  return JSON.stringify(settings) !== JSON.stringify(originalSettings.value)
})

// 方法
const loadSettings = async () => {
  try {
    loading.value = true
    const response = await groupManagementApi.getGroupSettings()
    Object.assign(settings, response.data)
    originalSettings.value = JSON.parse(JSON.stringify(settings))
  } catch (error) {
    console.error('加载设置失败:', error)
    // 使用默认设置
  } finally {
    loading.value = false
  }
}

// 加载消息模板
const loadTemplates = async () => {
  try {
    templatesLoading.value = true
    
    // 并行加载回复模板和转发模板
    const [replyResponse, forwardResponse] = await Promise.all([
      getTemplateList('reply'),
      getTemplateList('forward')
    ])
    
    if (replyResponse && replyResponse.data) {
      replyTemplates.value = replyResponse.data
    }
    
    if (forwardResponse && forwardResponse.data) {
      forwardTemplates.value = forwardResponse.data
    }
    
  } catch (error) {
    console.error('加载模板失败:', error)
    ElMessage.error('加载消息模板失败')
  } finally {
    templatesLoading.value = false
  }
}

const saveSettings = () => {
  // 验证延迟时间设置
  if (settings.replyDelayMin && settings.replyDelayMax && settings.replyDelayMin > settings.replyDelayMax) {
    ElMessage.error('最小延迟时间不能大于最大延迟时间')
    return
  }
  
  if (settings.replyDelayMin && settings.replyDelayMin < 1) {
    ElMessage.error('最小延迟时间不能小于1秒')
    return
  }
  
  if (settings.replyDelayMax && settings.replyDelayMax > 300) {
    ElMessage.error('最大延迟时间不能超过300秒')
    return
  }
  
  showSaveDialog.value = true
}

const confirmSave = async () => {
  try {
    loading.value = true
    await groupManagementApi.updateGroupSettings(settings)
    originalSettings.value = JSON.parse(JSON.stringify(settings))
    showSaveDialog.value = false
    // 显示成功消息
    ElMessage.success('群组设置保存成功')
    console.log('设置保存成功')
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const resetToDefaults = () => {
  Object.assign(settings, {
    defaultGroupStatus: 'AUTO',
    defaultAutoReply: true,
    defaultKeywordMonitoring: true,
    defaultMessageForwarding: false,
  // 自动回复设置
  defaultReplyTemplateId: '',
  defaultForwardTemplateId: '',
  replyDelayMin: 3,
  replyDelayMax: 8,
  dailyReplyLimit: 100,
  enableSmartReply: true,
    messageFrequencyAlert: 50,
    keywordAlert: true,
    activityMonitoring: true,
    alertNotificationMethod: 'email',
    dataRetentionDays: 30,
    autoCleanup: true,
    apiRateLimit: 60,
    debugMode: false
  })
}

// 生命周期
onMounted(() => {
  loadSettings()
  loadTemplates()
})
</script>

<style scoped>
.group-settings {
  padding: 24px;
  background: #f8fafc;
  min-height: 100vh;
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

/* 设置内容 */
.settings-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.settings-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px 0;
}

.section-header p {
  color: #64748b;
  margin: 0;
  font-size: 14px;
}

.settings-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 24px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.setting-item.full-width {
  flex-direction: column;
  align-items: stretch;
}

.setting-label {
  flex: 1;
}

.setting-label label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 4px;
}

.setting-desc {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.4;
}

.setting-control {
  flex-shrink: 0;
}

.setting-input,
.setting-select {
  width: 200px;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.setting-input:focus,
.setting-select:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.setting-select {
  cursor: pointer;
  background: white;
}

.setting-select:disabled {
  background: #f1f5f9;
  color: #94a3b8;
  cursor: not-allowed;
}

.setting-select option {
  padding: 8px;
}

.setting-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  resize: vertical;
  min-height: 100px;
  transition: border-color 0.2s;
}

.setting-textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 切换开关 */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 48px;
  height: 24px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: #cbd5e1;
  transition: 0.3s;
  border-radius: 24px;
}

.toggle-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
}

input:checked + .toggle-slider {
  background-color: #3b82f6;
}

input:checked + .toggle-slider:before {
  transform: translateX(24px);
}

/* 保存对话框 */
.save-dialog {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.dialog-container {
  position: relative;
  background: white;
  border-radius: 12px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  max-width: 400px;
  width: 90%;
}

.dialog-header {
  padding: 20px 24px 0;
}

.dialog-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.dialog-content {
  padding: 16px 24px;
}

.dialog-content p {
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

.dialog-footer {
  padding: 16px 24px 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn.primary {
  background: #3b82f6;
  color: white;
}

.btn.primary:hover {
  background: #2563eb;
}

.btn.secondary {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.btn.secondary:hover {
  background: #e2e8f0;
}

/* 延迟时间范围设置样式 */
.reply-delay-range {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.delay-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.delay-label {
  font-size: 14px;
  color: #475569;
  font-weight: 500;
  white-space: nowrap;
}

.delay-input {
  width: 80px !important;
  text-align: center;
}

.delay-unit {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

.delay-separator {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
  padding: 0 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .group-settings {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .setting-item {
    flex-direction: column;
    align-items: stretch;
  }
  
  .setting-input,
  .setting-select {
    width: 100%;
  }
  
  .reply-delay-range {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .delay-input-group {
    justify-content: space-between;
  }
  
  .delay-input {
    width: 100px !important;
  }
}
</style>