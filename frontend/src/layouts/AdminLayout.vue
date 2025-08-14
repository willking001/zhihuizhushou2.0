<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Menu as IconMenu,
  Fold,
  Expand,
  User,
  UserFilled,
  Setting,
  SwitchButton,
  Bell,
  Search,
  FullScreen,
  Refresh,
  DataAnalysis,
  ChatDotRound,
  Key,
  Collection,
  Tickets,
  MagicStick,
  TrendCharts,
  OfficeBuilding,
  HelpFilled,
  Document
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const isCollapse = ref(false)
const userInfo = ref({
  username: '管理员',
  avatar: '/admin-avatar.svg',
  role: 'admin'
})

const menuItems = ref([
  { index: '/dashboard', title: '仪表盘', icon: DataAnalysis },
  {
    index: '/messages',
    title: '消息管理',
    icon: ChatDotRound,
    children: [
      { index: '/messages/list', title: '消息列表' },
      { index: '/messages/analysis', title: '消息分析' },
      { index: '/messages/templates', title: '消息模板' }
    ]
  },

  {
    index: '/groups',
    title: '群组管理',
    icon: UserFilled,
    children: [
      { index: '/groups/management', title: '群组管理' },
      { index: '/groups/statistics', title: '群组统计' },
      { index: '/groups/settings', title: '群组设置' }
    ]
  },
  {
    index: '/keywords',
    title: '关键词管理',
    icon: Key,
    children: [
      { index: '/keywords/server', title: '服务器关键词' },
      { index: '/keywords/local', title: '本地关键词' },
      { index: '/keywords/learned', title: '智能学习' },
      { index: '/keywords/sync', title: '同步管理' }
    ]
  },
  {
    index: '/knowledge',
    title: '知识库管理',
    icon: Collection,
    children: [
      { index: '/knowledge/base', title: '知识库' },
      { index: '/knowledge/qa', title: '问答管理' },
      { index: '/knowledge/training', title: '模型训练' }
    ]
  },
  {
    index: '/workorder',
    title: '工单管理',
    icon: Tickets,
    children: [
      { index: '/workorder/list', title: '工单列表' },
      { index: '/workorder/process', title: '工单处理' },
      { index: '/workorder/statistics', title: '工单统计' }
    ]
  },
  {
    index: '/ai',
    title: 'AI服务',
    icon: MagicStick,
    children: [
      { index: '/ai/chat', title: '智能问答' },
      { index: '/ai/nlp', title: 'NLP处理' },
      { index: '/ai/proactive', title: '主动服务' }
    ]
  },
  {
    index: '/analytics',
    title: '数据分析',
    icon: TrendCharts,
    children: [
      { index: '/analytics/overview', title: '数据概览' },
      { index: '/analytics/reports', title: '统计报表' },
      { index: '/analytics/trends', title: '趋势分析' }
    ]
  },
  {
    index: '/tenant',
    title: '多租户管理',
    icon: OfficeBuilding,
    children: [
      { index: '/tenant/list', title: '租户列表' },
      { index: '/tenant/config', title: '租户配置' },
      { index: '/tenant/isolation', title: '数据隔离' }
    ]
  },
  {
    index: '/i18n',
    title: '国际化',
    icon: HelpFilled,
    children: [
      { index: '/i18n/languages', title: '语言管理' },
      { index: '/i18n/translations', title: '翻译管理' }
    ]
  },
  {
    index: '/audit',
    title: '审计日志',
    icon: Document,
    children: [
      { index: '/audit/logs', title: '操作日志' },
      { index: '/audit/security', title: '安全审计' },
      { index: '/audit/compliance', title: '合规检查' }
    ]
  },
  {
    index: '/system',
    title: '系统管理',
    icon: Setting,
    children: [
      { index: '/system/config', title: '系统配置' },
      { index: '/system/monitor', title: '系统监控' },
      { index: '/system/backup', title: '数据备份' },

      { index: '/system/users', title: '用户管理' }
    ]
  }
])

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  return route.matched
    .filter(item => item.meta && item.meta.title)
    .map(item => ({
      title: item.meta.title as string,
      path: item.path
    }))
})

const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

const refreshPage = () => {
  router.go(0)
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const handleUserCommand = (command: string) => {
  if (command === 'logout') {
    ElMessage.success('退出登录成功')
    router.push('/login')
  } else {
    router.push(`/${command}`)
  }
}
</script>

<template>
  <el-container class="admin-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="sidebar-container">
      <div class="logo-container">
        <img src="/logo.png" alt="Logo" class="logo-img" />
        <h1 v-if="!isCollapse" class="logo-title">电小助</h1>
      </div>

      <el-scrollbar wrap-class="scrollbar-wrapper">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          :collapse="isCollapse"
          router
          :collapse-transition="false"
        >
          <template v-for="item in menuItems" :key="item.index">
            <el-sub-menu v-if="item.children?.length"
              :index="item.index"
            >
              <template #title>
                <el-icon><component :is="item.icon" /></el-icon>
                <span>{{ item.title }}</span>
              </template>
              <el-menu-item v-for="child in item.children" :key="child.index" :index="child.index">
                {{ child.title }}
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="item.index">
              <el-icon><component :is="item.icon" /></el-icon>
              <template #title>{{ item.title }}</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container class="main-container">
      <!-- 顶部导航栏 -->
      <el-header class="header-container">
        <div class="header-left">
          <div class="sidebar-toggler" @click="toggleSidebar">
            <el-icon :size="20">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </div>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path">
              {{ item.title }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <div class="action-icon-wrapper">
            <el-tooltip content="搜索" placement="bottom">
              <div class="action-icon">
                <el-icon><Search /></el-icon>
              </div>
            </el-tooltip>
            <el-tooltip content="刷新" placement="bottom">
              <div class="action-icon" @click="refreshPage">
                <el-icon><Refresh /></el-icon>
              </div>
            </el-tooltip>
            <el-tooltip content="全屏" placement="bottom">
              <div class="action-icon" @click="toggleFullscreen">
                <el-icon><FullScreen /></el-icon>
              </div>
            </el-tooltip>
            <el-popover placement="bottom" :width="320" trigger="click">
              <template #reference>
                <el-tooltip content="通知" placement="bottom">
                  <div class="action-icon">
                    <el-badge :value="12" class="item">
                      <el-icon><Bell /></el-icon>
                    </el-badge>
                  </div>
                </el-tooltip>
              </template>
              <div class="notification-panel">这里是通知面板</div>
            </el-popover>
          </div>

          <el-dropdown @command="handleUserCommand">
            <span class="user-info">
              <el-avatar :size="36" :src="userInfo.avatar" class="user-avatar" />
              <span v-if="!isCollapse" class="username">{{ userInfo.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  系统设置
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive>
              <component :is="Component" :key="route.path" />
            </keep-alive>
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style lang="scss" scoped>
:root {
  --primary-color: #1e88e5;
  --secondary-color: #26c6da;
  --sidebar-bg: #263238;
  --sidebar-text-color: #b0bec5;
  --sidebar-active-text-color: #ffffff;
  --header-bg: #37474f;
  --header-text-color: #ffffff;
  --main-bg: #eceff1;
  --card-bg: #ffffff;
  --card-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.admin-layout {
  height: 100vh;
  background-color: var(--main-bg);

  .sidebar-container {
    background-color: var(--sidebar-bg);
    transition: width 0.3s ease;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);

    .logo-container {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 64px;
      padding: 0 16px;
      background-color: rgba(0, 0, 0, 0.2);

      .logo-img {
        width: 32px;
        height: 32px;
        margin-right: 12px;
      }

      .logo-title {
        font-size: 18px;
        color: #ffffff;
        font-weight: 600;
        white-space: nowrap;
      }
    }

    .sidebar-menu {
      border-right: none;
      --el-menu-bg-color: var(--sidebar-bg);
      --el-menu-text-color: var(--sidebar-text-color);
      --el-menu-hover-bg-color: rgba(255, 255, 255, 0.08);
      --el-menu-active-color: var(--sidebar-active-text-color);

      .el-menu-item.is-active {
        background-color: var(--primary-color) !important;
        color: #ffffff !important;
        font-weight: 500;
        &::before {
          display: none;
        }
      }

      .el-menu-item, .el-sub-menu__title {
        height: 48px;
        line-height: 48px;
        &:hover {
          color: #ffffff;
        }
      }

      .el-sub-menu__title {
        i {
          color: var(--sidebar-text-color);
        }
      }
    }
  }

  .main-container {
    display: flex;
    flex-direction: column;
    .header-container {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 64px;
      padding: 0 24px;
      background: var(--header-bg);
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);

      .header-left {
        display: flex;
        align-items: center;
        .sidebar-toggler {
          cursor: pointer;
          padding: 0 12px;
          margin-left: -12px;
          font-size: 20px;
          color: var(--header-text-color);
          &:hover { color: var(--secondary-color); }
        }
        .el-breadcrumb {
          margin-left: 16px;
          :deep(.el-breadcrumb__inner) {
            color: var(--header-text-color);
          }
        }
      }

      .header-right {
        display: flex;
        align-items: center;
        .action-icon-wrapper {
          display: flex;
          align-items: center;
          margin-right: 24px;
          gap: 8px;
          .action-icon {
            padding: 8px;
            cursor: pointer;
            font-size: 18px;
            color: var(--header-text-color);
            border-radius: 50%;
            transition: all 0.3s;
            &:hover {
              background-color: rgba(255, 255, 255, 0.1);
              color: var(--secondary-color);
            }
          }
        }

        .user-info {
          display: flex;
          align-items: center;
          cursor: pointer;
          padding: 8px 12px;
          border-radius: 4px;
          transition: all 0.3s;
          &:hover {
            background-color: rgba(255, 255, 255, 0.1);
          }
          .user-avatar { 
            margin-right: 12px;
            border: 2px solid rgba(255, 255, 255, 0.3);
          }
          .username {
            font-weight: 500;
            color: var(--header-text-color);
          }
        }
      }
    }

    .main-content {
      padding: 24px;
      flex: 1;
      overflow-y: auto;
      background-color: var(--main-bg);
    }
  }
}

.fade-transform-leave-active,
.fade-transform-enter-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>