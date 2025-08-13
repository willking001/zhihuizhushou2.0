<template>
  <div class="settings-container">
    <div class="settings-header">
      <h1>系统设置</h1>
    </div>
    
    <div class="settings-content">
      <el-card class="settings-card">
        <template #header>
          <div class="card-header">
            <h3>界面设置</h3>
          </div>
        </template>
        <el-form label-width="120px">
          <el-form-item label="主题模式">
            <el-radio-group v-model="settings.theme">
              <el-radio label="light">浅色</el-radio>
              <el-radio label="dark">深色</el-radio>
              <el-radio label="auto">跟随系统</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="主题颜色">
            <el-color-picker v-model="settings.primaryColor" />
          </el-form-item>
          <el-form-item label="侧边栏">
            <el-switch
              v-model="settings.sidebarCollapsed"
              active-text="收起"
              inactive-text="展开"
            />
          </el-form-item>
          <el-form-item label="固定头部">
            <el-switch v-model="settings.fixedHeader" />
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="settings-card">
        <template #header>
          <div class="card-header">
            <h3>通知设置</h3>
          </div>
        </template>
        <el-form label-width="120px">
          <el-form-item label="系统通知">
            <el-switch v-model="settings.systemNotifications" />
          </el-form-item>
          <el-form-item label="消息提醒">
            <el-switch v-model="settings.messageNotifications" />
          </el-form-item>
          <el-form-item label="声音提醒">
            <el-switch v-model="settings.soundNotifications" />
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="settings-card">
        <template #header>
          <div class="card-header">
            <h3>其他设置</h3>
          </div>
        </template>
        <el-form label-width="120px">
          <el-form-item label="语言">
            <el-select v-model="settings.language">
              <el-option label="简体中文" value="zh-CN" />
              <el-option label="English" value="en-US" />
            </el-select>
          </el-form-item>
          <el-form-item label="时区">
            <el-select v-model="settings.timezone">
              <el-option label="(GMT+08:00) 北京" value="Asia/Shanghai" />
              <el-option label="(GMT+00:00) 伦敦" value="Europe/London" />
              <el-option label="(GMT-05:00) 纽约" value="America/New_York" />
            </el-select>
          </el-form-item>
        </el-form>
      </el-card>

      <div class="settings-actions">
        <el-button type="primary" @click="saveSettings">保存设置</el-button>
        <el-button @click="resetSettings">重置</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'

// 设置状态
const settings = reactive({
  theme: 'light',
  primaryColor: '#409EFF',
  sidebarCollapsed: false,
  fixedHeader: true,
  systemNotifications: true,
  messageNotifications: true,
  soundNotifications: false,
  language: 'zh-CN',
  timezone: 'Asia/Shanghai'
})

// 保存设置
const saveSettings = () => {
  // 这里应该调用保存设置的API
  localStorage.setItem('settings', JSON.stringify(settings))
  ElMessage.success('设置已保存')
}

// 重置设置
const resetSettings = () => {
  Object.assign(settings, {
    theme: 'light',
    primaryColor: '#409EFF',
    sidebarCollapsed: false,
    fixedHeader: true,
    systemNotifications: true,
    messageNotifications: true,
    soundNotifications: false,
    language: 'zh-CN',
    timezone: 'Asia/Shanghai'
  })
  ElMessage.info('设置已重置')
}
</script>

<style lang="scss" scoped>
.settings-container {
  padding: 20px;
}

.settings-header {
  margin-bottom: 20px;
  h1 {
    font-size: 24px;
    color: #303133;
  }
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.settings-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  h3 {
    margin: 0;
    font-size: 18px;
  }
}

.settings-actions {
  display: flex;
  justify-content: flex-start;
  gap: 10px;
  margin-top: 20px;
}
</style>