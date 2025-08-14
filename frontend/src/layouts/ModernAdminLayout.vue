<template>
  <div class="modern-admin-layout">
    <!-- 侧边栏 -->
    <aside :class="['sidebar', { 'sidebar--collapsed': isCollapsed }]">
      <div class="sidebar__header">
        <div class="sidebar__logo">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
            </svg>
          </div>
          <transition name="fade">
            <span v-show="!isCollapsed" class="logo-text">智慧助手</span>
          </transition>
        </div>
        <button @click="toggleSidebar" class="sidebar__toggle">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"/>
          </svg>
        </button>
      </div>

      <nav class="sidebar__nav">
        <div class="nav-section">
          <div v-show="!isCollapsed" class="nav-section__title">主要功能</div>
          <ul class="nav-menu">
            <li class="nav-item">
              <router-link to="/dashboard" class="nav-link" active-class="nav-link--active">
                <div class="nav-icon">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/>
                  </svg>
                </div>
                <transition name="fade">
                  <span v-show="!isCollapsed" class="nav-text">仪表盘</span>
                </transition>
              </router-link>
            </li>
            
            <li class="nav-item">
              <div class="nav-group">
                <div @click="toggleGroup('messages')" class="nav-link nav-group__header">
                  <div class="nav-icon">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/>
                    </svg>
                  </div>
                  <transition name="fade">
                    <span v-show="!isCollapsed" class="nav-text">消息管理</span>
                  </transition>
                  <transition name="fade">
                    <div v-show="!isCollapsed" class="nav-arrow" :class="{ 'nav-arrow--expanded': expandedGroups.includes('messages') }">
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/>
                      </svg>
                    </div>
                  </transition>
                </div>
                <transition name="slide-down">
                  <ul v-show="expandedGroups.includes('messages') && !isCollapsed" class="nav-submenu">
                    <li><router-link to="/messages/list" class="nav-sublink">消息列表</router-link></li>
                    <li><router-link to="/messages/templates" class="nav-sublink">消息模板配置</router-link></li>
                  </ul>
                </transition>
              </div>
            </li>

            <li class="nav-item">
              <div class="nav-group">
                <div @click="toggleGroup('users')" class="nav-link nav-group__header">
                  <div class="nav-icon">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M16 7c0-2.21-1.79-4-4-4S8 4.79 8 7s1.79 4 4 4 4-1.79 4-4zm-4 7c-2.67 0-8 1.34-8 4v3h16v-3c0-2.66-5.33-4-8-4z"/>
                    </svg>
                  </div>
                  <transition name="fade">
                    <span v-show="!isCollapsed" class="nav-text">用户管理</span>
                  </transition>
                  <transition name="fade">
                    <div v-show="!isCollapsed" class="nav-arrow" :class="{ 'nav-arrow--expanded': expandedGroups.includes('users') }">
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/>
                      </svg>
                    </div>
                  </transition>
                </div>
                <transition name="slide-down">
                  <ul v-show="expandedGroups.includes('users') && !isCollapsed" class="nav-submenu">
                    <li><router-link to="/users" class="nav-sublink">用户列表</router-link></li>
                  </ul>
                </transition>
              </div>
            </li>

            <li class="nav-item">
              <div class="nav-group">
                <div @click="toggleGroup('keywords')" class="nav-link nav-group__header">
                  <div class="nav-icon">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12.65 10C11.83 7.67 9.61 6 7 6c-3.31 0-6 2.69-6 6s2.69 6 6 6c2.61 0 4.83-1.67 5.65-4H17v4h4v-4h2v-4H12.65zM7 14c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z"/>
                    </svg>
                  </div>
                  <transition name="fade">
                    <span v-show="!isCollapsed" class="nav-text">关键词管理</span>
                  </transition>
                  <transition name="fade">
                    <div v-show="!isCollapsed" class="nav-arrow" :class="{ 'nav-arrow--expanded': expandedGroups.includes('keywords') }">
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/>
                      </svg>
                    </div>
                  </transition>
                </div>
                <transition name="slide-down">
                  <ul v-show="expandedGroups.includes('keywords') && !isCollapsed" class="nav-submenu">
                    <li><router-link to="/keywords/config" class="nav-sublink">关键词配置</router-link></li>
                    <li><router-link to="/keywords/analysis" class="nav-sublink">关键词分析</router-link></li>
                    <li><router-link to="/keywords/enhancement" class="nav-sublink">关键词智能学习</router-link></li>
                    <li><router-link to="/keywords/compatibility" class="nav-sublink">模式管理</router-link></li>
                    <li><router-link to="/keywords/business-rules" class="nav-sublink">智能业务规则</router-link></li>
                  </ul>
                </transition>
              </div>
            </li>
            
            <li class="nav-item">
              <div class="nav-group">
                <div @click="toggleGroup('groups')" class="nav-link nav-group__header">
                  <div class="nav-icon">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M16 4c0-1.11.89-2 2-2s2 .89 2 2-.89 2-2 2-2-.89-2-2zM4 18v-1c0-2.66 5.33-4 8-4s8 1.34 8 4v1H4zM12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4z"/>
                    </svg>
                  </div>
                  <transition name="fade">
                    <span v-show="!isCollapsed" class="nav-text">群组管理</span>
                  </transition>
                  <transition name="fade">
                    <div v-show="!isCollapsed" class="nav-arrow" :class="{ 'nav-arrow--expanded': expandedGroups.includes('groups') }">
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/>
                      </svg>
                    </div>
                  </transition>
                </div>
                <transition name="slide-down">
                  <ul v-show="expandedGroups.includes('groups') && !isCollapsed" class="nav-submenu">
                    <li><router-link to="/groups/management" class="nav-sublink">群组管理</router-link></li>
                    <li><router-link to="/groups/statistics" class="nav-sublink">群组统计</router-link></li>
                    <li><router-link to="/groups/settings" class="nav-sublink">群组设置</router-link></li>
                  </ul>
                </transition>
              </div>
            </li>
            
            <li class="nav-item">
              <div class="nav-group">
                <div @click="toggleGroup('nlp')" class="nav-link nav-group__header">
                  <div class="nav-icon">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 2c-5.33 4.55-8 8.48-8 11.8 0 4.98 3.8 8.2 8 8.2s8-3.22 8-8.2c0-3.32-2.67-7.25-8-11.8zm0 18c-3.35 0-6-2.57-6-6.2 0-2.34 1.95-5.44 6-9.14 4.05 3.7 6 6.79 6 9.14 0 3.63-2.65 6.2-6 6.2zm-4.17-6c.37 0 .67.26.74.62.41 2.22 2.28 2.98 3.64 2.87.43-.02.79.32.79.75 0 .4-.32.73-.72.75-2.13.13-4.62-1.09-5.19-4.12-.08-.45.28-.87.74-.87z"/>
                    </svg>
                  </div>
                  <transition name="fade">
                    <span v-show="!isCollapsed" class="nav-text">NLP分析</span>
                  </transition>
                  <transition name="fade">
                    <div v-show="!isCollapsed" class="nav-arrow" :class="{ 'nav-arrow--expanded': expandedGroups.includes('nlp') }">
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/>
                      </svg>
                    </div>
                  </transition>
                </div>
                <transition name="slide-down">
                  <ul v-show="expandedGroups.includes('nlp') && !isCollapsed" class="nav-submenu">
                    <li><router-link to="/nlp/process" class="nav-sublink">自然语言处理</router-link></li>
                    <li><router-link to="/nlp/config" class="nav-sublink">自然语言模式配置</router-link></li>
                  </ul>
                </transition>
              </div>
            </li>
          </ul>
        </div>

        <div class="nav-section">
          <ul class="nav-menu">
            <li class="nav-item">
              <div class="nav-group">
                <div @click="toggleGroup('system')" class="nav-link nav-group__header">
                  <div class="nav-icon">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M19.14,12.94c0.04-0.3,0.06-0.61,0.06-0.94c0-0.32-0.02-0.64-0.07-0.94l2.03-1.58c0.18-0.14,0.23-0.41,0.12-0.61 l-1.92-3.32c-0.12-0.22-0.37-0.29-0.59-0.22l-2.39,0.96c-0.5-0.38-1.03-0.7-1.62-0.94L14.4,2.81c-0.04-0.24-0.24-0.41-0.48-0.41 h-3.84c-0.24,0-0.43,0.17-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29L5.24,5.33c-0.22-0.08-0.47,0-0.59,0.22L2.74,8.87 C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58C4.84,11.36,4.8,11.69,4.8,12s0.02,0.64,0.07,0.94l-2.03,1.58 c-0.18,0.14-0.23,0.41-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54 c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.44-0.17,0.47-0.41l0.36-2.54c0.59-0.24,1.13-0.56,1.62-0.94l2.39,0.96 c0.22,0.08,0.47,0,0.59-0.22l1.92-3.32c0.12-0.22,0.07-0.47-0.12-0.61L19.14,12.94z M12,15.6c-1.98,0-3.6-1.62-3.6-3.6 s1.62-3.6,3.6-3.6s3.6,1.62,3.6,3.6S13.98,15.6,12,15.6z"/>
                    </svg>
                  </div>
                  <transition name="fade">
                    <span v-show="!isCollapsed" class="nav-text">系统管理</span>
                  </transition>
                  <transition name="fade">
                    <div v-show="!isCollapsed" class="nav-arrow" :class="{ 'nav-arrow--expanded': expandedGroups.includes('system') }">
                      <svg viewBox="0 0 24 24" fill="currentColor">
                        <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/>
                      </svg>
                    </div>
                  </transition>
                </div>
                <transition name="slide-down">
                  <ul v-show="expandedGroups.includes('system') && !isCollapsed" class="nav-submenu">
                    <li><router-link to="/system/config" class="nav-sublink">系统配置</router-link></li>
                    <li><router-link to="/system/monitor" class="nav-sublink">系统监控</router-link></li>
                    <li><router-link to="/system/logs" class="nav-sublink">系统日志</router-link></li>
                  </ul>
                </transition>
              </div>
            </li>
          </ul>
        </div>
      </nav>
    </aside>

    <!-- 主内容区 -->
    <div class="main-wrapper">
      <!-- 顶部导航栏 -->
      <header class="header">
        <div class="header__left">
          <div class="breadcrumb">
            <span class="breadcrumb__item">{{ currentPageTitle }}</span>
          </div>
        </div>
        
        <div class="header__right">
          <div class="header__actions">
            <!-- 搜索 -->
            <div class="action-item">
              <button class="action-btn">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
                </svg>
              </button>
            </div>
            
            <!-- 通知 -->
            <div class="action-item">
              <button class="action-btn notification-btn">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.89 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
                </svg>
                <span class="notification-badge">3</span>
              </button>
            </div>
            
            <!-- 全屏 -->
            <div class="action-item">
              <button @click="toggleFullscreen" class="action-btn">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M7 14H5v5h5v-2H7v-3zm-2-4h2V7h3V5H5v5zm12 7h-3v2h5v-5h-2v3zM14 5v2h3v3h2V5h-5z"/>
                </svg>
              </button>
            </div>
            
            <!-- 用户菜单 -->
            <div class="action-item">
              <div class="user-menu" @click="toggleUserMenu">
                <div class="user-avatar">
                  <img src="https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=40&h=40&fit=crop&crop=face" alt="用户头像">
                </div>
                <div class="user-info">
                  <div class="user-name">管理员</div>
                  <div class="user-role">系统管理员</div>
                </div>
                <div class="user-arrow">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6 1.41-1.41z"/>
                  </svg>
                </div>
              </div>
              
              <transition name="dropdown">
                <div v-show="showUserMenu" class="user-dropdown">
                  <div @click="goToProfile" class="dropdown-item">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                    </svg>
                    个人中心
                  </div>
                  <div @click="goToSettings" class="dropdown-item">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M19.14,12.94c0.04-0.3,0.06-0.61,0.06-0.94c0-0.32-0.02-0.64-0.07-0.94l2.03-1.58c0.18-0.14,0.23-0.41,0.12-0.61 l-1.92-3.32c-0.12-0.22-0.37-0.29-0.59-0.22l-2.39,0.96c-0.5-0.38-1.03-0.7-1.62-0.94L14.4,2.81c-0.04-0.24-0.24-0.41-0.48-0.41 h-3.84c-0.24,0-0.43,0.17-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29L5.24,5.33c-0.22-0.08-0.47,0-0.59,0.22L2.74,8.87 C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58C4.84,11.36,4.8,11.69,4.8,12s0.02,0.64,0.07,0.94l-2.03,1.58 c-0.18,0.14-0.23,0.41-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54 c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.44-0.17,0.47-0.41l0.36-2.54c0.59-0.24,1.13-0.56,1.62-0.94l2.39,0.96 c0.22,0.08,0.47,0,0.59-0.22l1.92-3.32c0.12-0.22,0.07-0.47-0.12-0.61L19.14,12.94z M12,15.6c-1.98,0-3.6-1.62-3.6-3.6 s1.62-3.6,3.6-3.6s3.6,1.62,3.6,3.6S13.98,15.6,12,15.6z"/>
                    </svg>
                    系统设置
                  </div>
                  <div class="dropdown-divider"></div>
                  <div @click="logout" class="dropdown-item dropdown-item--danger">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/>
                    </svg>
                    退出登录
                  </div>
                </div>
              </transition>
            </div>
          </div>
        </div>
      </header>

      <!-- 主内容 -->
      <main class="content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" :key="$route.path" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapsed = ref(false)
const expandedGroups = ref([])
const showUserMenu = ref(false)

const currentPageTitle = computed(() => {
  return route.meta?.title as string || '仪表盘'
})

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const toggleGroup = (groupName: string) => {
  if (isCollapsed.value) {
    isCollapsed.value = false
  }
  
  const index = expandedGroups.value.indexOf(groupName)
  if (index > -1) {
    expandedGroups.value.splice(index, 1)
  } else {
    expandedGroups.value.push(groupName)
  }
}

const toggleUserMenu = () => {
  showUserMenu.value = !showUserMenu.value
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const logout = () => {
  userStore.logout().then(() => {
    ElMessage.success('退出登录成功')
    router.push('/login')
  }).catch(() => {
    ElMessage.error('退出登录失败')
  })
}

const goToProfile = () => {
  router.push('/profile')
  showUserMenu.value = false
}

const goToSettings = () => {
  router.push('/settings')
  showUserMenu.value = false
}

// 点击外部关闭用户菜单
const handleClickOutside = (event: Event) => {
  const target = event.target as Element
  if (!target.closest('.user-menu') && !target.closest('.user-dropdown')) {
    showUserMenu.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style lang="scss" scoped>
.modern-admin-layout {
  display: flex;
  height: 100vh;
  background: #f8fafc;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

// 侧边栏样式
.sidebar {
  width: 280px;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  color: #e2e8f0;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 100;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.12);

  &--collapsed {
    width: 80px;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24px 20px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  }

  &__logo {
    display: flex;
    align-items: center;
    gap: 12px;

    .logo-icon {
      width: 32px;
      height: 32px;
      background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;

      svg {
        width: 18px;
        height: 18px;
      }
    }

    .logo-text {
      font-size: 18px;
      font-weight: 700;
      color: #f1f5f9;
    }
  }

  &__toggle {
    background: none;
    border: none;
    color: #94a3b8;
    cursor: pointer;
    padding: 8px;
    border-radius: 6px;
    transition: all 0.2s;

    &:hover {
      background: rgba(255, 255, 255, 0.1);
      color: #f1f5f9;
    }

    svg {
      width: 20px;
      height: 20px;
    }
  }

  &__nav {
    padding: 20px 0;
    height: calc(100vh - 88px);
    overflow-y: auto;
    
    &::-webkit-scrollbar {
      width: 4px;
    }
    
    &::-webkit-scrollbar-track {
      background: transparent;
    }
    
    &::-webkit-scrollbar-thumb {
      background: rgba(255, 255, 255, 0.2);
      border-radius: 2px;
    }
  }
}

.nav-section {
  margin-bottom: 32px;

  &__title {
    font-size: 12px;
    font-weight: 600;
    color: #64748b;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    padding: 0 20px 12px;
  }
}

.nav-menu {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  margin-bottom: 4px;
}

.nav-link {
  display: flex;
  align-items: center;
  padding: 12px 20px;
  color: #cbd5e1;
  text-decoration: none;
  transition: all 0.2s;
  position: relative;
  cursor: pointer;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
    color: #f1f5f9;
  }

  &--active {
    background: linear-gradient(90deg, rgba(59, 130, 246, 0.15) 0%, transparent 100%);
    color: #3b82f6;
    border-right: 3px solid #3b82f6;

    .nav-icon {
      color: #3b82f6;
    }
  }
}

.nav-group__header {
  justify-content: space-between;
}

.nav-icon {
  width: 20px;
  height: 20px;
  margin-right: 12px;
  flex-shrink: 0;

  svg {
    width: 100%;
    height: 100%;
  }
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
}

.nav-arrow {
  width: 16px;
  height: 16px;
  transition: transform 0.2s;

  &--expanded {
    transform: rotate(180deg);
  }

  svg {
    width: 100%;
    height: 100%;
  }
}

.nav-submenu {
  list-style: none;
  padding: 0;
  margin: 8px 0 0 0;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  overflow: hidden;
}

.nav-sublink {
  display: block;
  padding: 10px 20px 10px 52px;
  color: #94a3b8;
  text-decoration: none;
  font-size: 13px;
  transition: all 0.2s;

  &:hover {
    background: rgba(255, 255, 255, 0.05);
    color: #e2e8f0;
  }

  &.router-link-active {
    background: rgba(59, 130, 246, 0.2);
    color: #3b82f6;
  }
}

// 主内容区样式
.main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.header {
  height: 72px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 50;

  &__left {
    display: flex;
    align-items: center;
  }

  &__right {
    display: flex;
    align-items: center;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.breadcrumb {
  &__item {
    font-size: 24px;
    font-weight: 700;
    color: #1e293b;
  }
}

.action-item {
  position: relative;
}

.action-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: none;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;

  &:hover {
    background: #f1f5f9;
    color: #1e293b;
  }

  svg {
    width: 20px;
    height: 20px;
  }
}

.notification-btn {
  .notification-badge {
    position: absolute;
    top: 8px;
    right: 8px;
    background: #ef4444;
    color: white;
    font-size: 10px;
    font-weight: 600;
    padding: 2px 6px;
    border-radius: 10px;
    min-width: 16px;
    text-align: center;
  }
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #f1f5f9;
  }
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  overflow: hidden;
  border: 2px solid #e2e8f0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.user-info {
  .user-name {
    font-size: 14px;
    font-weight: 600;
    color: #1e293b;
    line-height: 1.2;
  }

  .user-role {
    font-size: 12px;
    color: #64748b;
    line-height: 1.2;
  }
}

.user-arrow {
  width: 16px;
  height: 16px;
  color: #94a3b8;

  svg {
    width: 100%;
    height: 100%;
  }
}

.user-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  border: 1px solid #e2e8f0;
  min-width: 200px;
  overflow: hidden;
  z-index: 1000;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;

  &:hover {
    background: #f9fafb;
  }

  &--danger {
    color: #dc2626;

    &:hover {
      background: #fef2f2;
    }
  }

  svg {
    width: 16px;
    height: 16px;
  }
}

.dropdown-divider {
  height: 1px;
  background: #e5e7eb;
  margin: 4px 0;
}

.content {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  background: #f8fafc;
}

// 动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-down-enter-active {
  transition: all 0.3s ease-out;
}

.slide-down-leave-active {
  transition: all 0.3s ease-in;
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.dropdown-enter-active {
  transition: all 0.2s ease-out;
}

.dropdown-leave-active {
  transition: all 0.2s ease-in;
}

.dropdown-enter-from {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
}

.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-8px) scale(0.95);
}

.page-enter-active {
  transition: all 0.3s ease-out;
}

.page-leave-active {
  transition: all 0.3s ease-in;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

// 响应式设计
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 200;
    transform: translateX(-100%);

    &--collapsed {
      transform: translateX(0);
      width: 280px;
    }
  }

  .main-wrapper {
    margin-left: 0;
  }

  .header {
    padding: 0 16px;

    &__actions {
      gap: 4px;
    }
  }

  .content {
    padding: 16px;
  }

  .user-info {
    display: none;
  }
}
</style>