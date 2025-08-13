import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import ModernAdminLayout from '@/layouts/ModernAdminLayout.vue'

export const constantRoutes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('../views/NotFound.vue'),
    hidden: true
  }
]

export const asyncRoutes: Array<RouteRecordRaw> = [
  {
    path: '/',
    component: ModernAdminLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/dashboard/DashboardView.vue'),
        meta: { title: '仪表盘', icon: 'dashboard' }
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('../views/profile/ProfileView.vue'),
        meta: { title: '个人中心', icon: 'user' },
        hidden: true
      },
      {
        path: 'settings',
        name: 'settings',
        component: () => import('../views/settings/SettingsView.vue'),
        meta: { title: '系统设置', icon: 'setting' },
        hidden: true
      }
    ]
  },
  {
    path: '/messages',
    component: ModernAdminLayout,
    redirect: '/messages/list',
    name: 'Messages',
    meta: { title: '消息管理', icon: 'message' },
    children: [
      {
        path: 'list',
        name: 'MessageList',
        component: () => import('../views/message/MessageList.vue'),
        meta: { title: '消息列表' }
      },
      {
        path: 'templates',
        name: 'MessageTemplates',
        component: () => import('../views/message/MessageTemplateConfig.vue'),
        meta: { title: '消息模板配置' }
      },
      {
        path: 'template-demo',
        name: 'MessageTemplateDemo',
        component: () => import('../views/message/TemplateDemo.vue'),
        meta: { title: '模板示例' }
      }
    ]
  },
  {
    path: '/users',
    component: ModernAdminLayout,
    redirect: '/users/list',
    name: 'Users',
    meta: { title: '用户管理', icon: 'user' },
    children: [
      {
        path: 'list',
        name: 'users-list',
        component: () => import('../views/user/UserList.vue'),
        meta: { title: '用户列表' }
      }
    ]
  },
  {
    path: '/keywords',
    component: ModernAdminLayout,
    redirect: '/keywords/config',
    name: 'Keywords',
    meta: { title: '关键词管理', icon: 'keyword' },
    children: [
      {
        path: 'config',
        name: 'keyword-config',
        component: () => import('../views/keyword/KeywordConfig.vue'),
        meta: { title: '关键词配置' }
      },
      {
        path: 'analysis',
        name: 'keyword-analysis',
        component: () => import('../views/keyword/KeywordAnalysis.vue'),
        meta: { title: '关键词分析' }
      },
      {
        path: 'enhancement',
        name: 'keyword-enhancement',
        component: () => import('../views/keyword/KeywordEnhancement.vue'),
        meta: { title: '关键词智能学习' }
      },
      {
        path: 'compatibility',
        name: 'keyword-compatibility',
        component: () => import('../views/keyword-enhancement/CompatibilityMode.vue'),
        meta: { title: '模式管理' }
      },
      {
        path: 'business-rules',
        name: 'business-rules',
        component: () => import('../views/keyword/BusinessRules.vue'),
        meta: { title: '智能业务规则' }
      }
    ]
  },
  {
    path: '/nlp',
    component: ModernAdminLayout,
    redirect: '/nlp/process',
    name: 'NLP',
    meta: { title: 'NLP分析', icon: 'nlp' },
    children: [
      {
        path: 'process',
        name: 'nlp-process',
        component: () => import('../views/nlp/NlpProcessView.vue'),
        meta: { title: '自然语言处理' }
      },
      {
        path: 'config',
        name: 'nlp-config',
        component: () => import('../views/nlp/NlpConfigView.vue'),
        meta: { title: '自然语言模式配置' }
      }
    ]
  },
  {
    path: '/system',
    component: ModernAdminLayout,
    redirect: '/system/config',
    name: 'System',
    meta: { title: '系统管理', icon: 'setting' },
    children: [
      {
        path: 'config',
        name: 'system-config',
        component: () => import('../views/system/SystemConfigView.vue'),
        meta: { title: '系统配置' }
      },
      {
        path: 'monitor',
        name: 'system-monitor',
        component: () => import('../views/system/SystemMonitorView.vue'),
        meta: { title: '系统监控' }
      },
      {
        path: 'logs',
        name: 'system-logs',
        component: () => import('../views/system/SystemLogsView.vue'),
        meta: { title: '系统日志' }
      }
    ]
  },

  // 404 page must be placed at the end !!!
  { path: '/:pathMatch(.*)*', redirect: '/404', hidden: true }
]

const createNewRouter = () => createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  scrollBehavior: () => ({ top: 0 }),
  routes: [...constantRoutes, ...asyncRoutes]
})

const router = createNewRouter()

export function resetRouter() {
  const newRouter = createNewRouter()
  ;(router as any).matcher = (newRouter as any).matcher // reset router
}

export default router
