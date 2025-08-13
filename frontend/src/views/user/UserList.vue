<template>
  <div class="user-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">用户管理</h1>
          <p class="page-subtitle">管理系统中的所有用户账户</p>
        </div>
        <div class="header-actions">
          <button class="action-btn primary" @click="openAddUserDialog">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
            </svg>
            新增用户
          </button>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-section">
      <div class="search-bar">
        <div class="search-input-wrapper">
          <svg class="search-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
          </svg>
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="搜索用户名、邮箱或手机号..."
            class="search-input"
            @keyup.enter="handleSearch"
          >
        </div>
        <div class="filter-controls">
          <select v-model="selectedRole" class="filter-select" @change="handleFilterChange">
            <option value="">所有角色</option>
            <option value="admin">管理员</option>
            <option value="grid">网格员</option>
            <option value="user">普通用户</option>
          </select>
          <select v-model="selectedStatus" class="filter-select" @change="handleFilterChange">
            <option value="">所有状态</option>
            <option value="active">活跃</option>
            <option value="inactive">非活跃</option>
            <option value="banned">已禁用</option>
          </select>
          <button class="filter-btn" @click="resetFilters">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
            重置
          </button>
        </div>
      </div>
    </div>

    <!-- 用户统计卡片 -->
    <div class="stats-overview">
      <div class="stat-item">
        <div class="stat-icon admin">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM21 9V7L15 4V6C15 7.1 14.1 8 13 8H11C9.9 8 9 7.1 9 6V4L3 7V9H21ZM12 17.5L7.5 20L9 14.5L4.5 10.5L10 10L12 4.5L14 10L19.5 10.5L15 14.5L16.5 20L12 17.5Z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ userStats.admin }}</div>
          <div class="stat-label">管理员</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon grid">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ userStats.grid }}</div>
          <div class="stat-label">网格员</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon user">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M16 7c0-2.21-1.79-4-4-4S8 4.79 8 7s1.79 4 4 4 4-1.79 4-4zm-4 7c-2.67 0-8 1.34-8 4v3h16v-3c0-2.66-5.33-4-8-4z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ userStats.user }}</div>
          <div class="stat-label">普通用户</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon total">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M16 4c0-1.11.89-2 2-2s2 .89 2 2-.89 2-2 2-2-.89-2-2zM4 18v-4h3v4h2v-7.5c0-.83.67-1.5 1.5-1.5S12 9.67 12 10.5V11h2V9.5c0-1.93-1.57-3.5-3.5-3.5S7 7.57 7 9.5V11H4c-1.11 0-2 .89-2 2v5h2zm5.5-12.5c0-.83.67-1.5 1.5-1.5s1.5.67 1.5 1.5S11.83 7 11 7s-1.5-.67-1.5-1.5z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ userStats.total }}</div>
          <div class="stat-label">总用户数</div>
        </div>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="user-table-container">
      <div class="table-header">
        <h3 class="table-title">用户列表</h3>
      </div>
      
      <div class="table-wrapper">
        <table class="user-table">
          <thead>
            <tr>
              <th class="checkbox-col">
                <input type="checkbox" v-model="selectAll" @change="toggleSelectAll">
              </th>
              <th class="sortable" @click="sortBy('username')">
                用户名
                <svg class="sort-icon" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M7 14l5-5 5 5z"/>
                </svg>
              </th>
              <th>头像</th>
              <th class="sortable" @click="sortBy('email')">
                邮箱
                <svg class="sort-icon" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M7 14l5-5 5 5z"/>
                </svg>
              </th>
              <th class="sortable" @click="sortBy('phone')">
                手机号码
                <svg class="sort-icon" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M7 14l5-5 5 5z"/>
                </svg>
              </th>
              <th>角色</th>
              <th>状态</th>
              <th class="sortable" @click="sortBy('lastLogin')">
                最后登录
                <svg class="sort-icon" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M7 14l5-5 5 5z"/>
                </svg>
              </th>
              <th class="sortable" @click="sortBy('createdAt')">
                创建时间
                <svg class="sort-icon" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M7 14l5-5 5 5z"/>
                </svg>
              </th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="user in filteredUsers" :key="user.id" class="user-row">
              <td class="checkbox-col">
                <input type="checkbox" v-model="selectedUsers" :value="user.id">
              </td>
              <td class="username-col">
                <div class="username-wrapper">
                  <span class="username">{{ user.username }}</span>
                  <span v-if="user.isOnline" class="online-indicator"></span>
                </div>
              </td>
              <td class="avatar-col">
                <div class="avatar" :style="{ background: user.avatarColor }">
                  {{ user.username.charAt(0).toUpperCase() }}
                </div>
              </td>
              <td class="email-col">{{ user.email }}</td>
              <td class="phone-col">{{ user.phone || '-' }}</td>
              <td class="role-col">
                <span class="role-badge" :class="user.role">
                  {{ getRoleText(user.role) }}
                </span>
              </td>
              <td class="status-col">
                <span class="status-badge" :class="user.status">
                  {{ getStatusText(user.status) }}
                </span>
              </td>
              <td class="last-login-col">{{ formatDate(user.lastLogin) }}</td>
              <td class="created-at-col">{{ formatDate(user.createdAt) }}</td>
              <td class="actions-col">
                <div class="action-buttons">
                  <button class="action-btn edit" @click="editUser(user)" title="编辑">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                    </svg>
                  </button>
                  <button class="action-btn view" @click="viewUser(user)" title="查看">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                    </svg>
                  </button>
                  <button class="action-btn delete" @click="deleteUser(user)" title="删除">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <div class="pagination-info">
          显示 {{ (currentPage - 1) * pageSize + 1 }} - {{ Math.min(currentPage * pageSize, totalUsers) }} 条，共 {{ totalUsers }} 条
        </div>
        <div class="pagination-controls">
          <button 
            class="pagination-btn" 
            :disabled="currentPage === 1" 
            @click="handlePageChange(currentPage - 1)"
          >
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
            </svg>
          </button>
          <span class="page-numbers">
            <button 
              v-for="page in visiblePages" 
              :key="page"
              class="page-btn"
              :class="{ active: page === currentPage }"
              @click="handlePageChange(page)"
            >
              {{ page }}
            </button>
          </span>
          <button 
            class="pagination-btn" 
            :disabled="currentPage === totalPages" 
            @click="handlePageChange(currentPage + 1)"
          >
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
    
    <!-- 用户表单 -->
    <UserForm
      v-model:visible="userFormVisible"
      :userData="currentUser"
      :isEdit="isEditMode"
      @submit="handleUserFormSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import UserForm from '@/components/user/UserForm.vue'
import { getUserList, createUser, updateUser, deleteUser as deleteUserApi } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const searchQuery = ref('')
const selectedRole = ref('')
const selectedStatus = ref('')
const selectAll = ref(false)
const selectedUsers = ref<number[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const sortField = ref('')
const sortOrder = ref('asc')

// 用户表单相关
const userFormVisible = ref(false)
const currentUser = ref<any>(null)
const isEditMode = ref(false)

// 用户数据
const users = ref([])
const loading = ref(false)
const total = ref(0)

// 查询参数
const queryParams = ref({
  pageNum: 1,
  pageSize: 10,
  username: '',
  email: '',
  status: ''
})

// 获取用户列表数据
const fetchUserList = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      username: searchQuery.value,
      status: selectedStatus.value
    }
    
    // 调用API获取用户列表
    const response = await getUserList(params)
    if (response && response.data) {
      // 将后端返回的数据转换为前端需要的格式
      users.value = response.data.list.map((user: any) => ({
        ...user,
        lastLogin: user.lastLogin ? new Date(user.lastLogin) : null,
        createdAt: user.createdAt ? new Date(user.createdAt) : null,
        isOnline: false, // 这个字段可能需要从后端获取或者通过其他方式计算
        avatarColor: user.avatarColor || getRandomColor(user.username)
      }))
      total.value = response.data.total
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败，请重试')
  } finally {
    loading.value = false
  }
}

// 生成随机颜色
const getRandomColor = (username: string) => {
  const colors = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6', '#f97316']
  const index = username.charCodeAt(0) % colors.length
  return colors[index]
}

// 计算属性
const userStats = computed(() => {
  const stats = { admin: 0, grid: 0, user: 0, total: 0 }
  users.value.forEach(user => {
    if (user.role in stats) {
      stats[user.role as keyof typeof stats]++
    }
    stats.total++
  })
  return stats
})

const filteredUsers = computed(() => {
  let filtered = users.value

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(user => 
      user.username.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query) ||
      (user.phone && user.phone.includes(query))
    )
  }

  // 角色过滤
  if (selectedRole.value) {
    filtered = filtered.filter(user => user.role === selectedRole.value)
  }

  // 状态过滤
  if (selectedStatus.value) {
    filtered = filtered.filter(user => user.status === selectedStatus.value)
  }

  // 排序
  if (sortField.value) {
    filtered.sort((a, b) => {
      const aVal = a[sortField.value as keyof typeof a]
      const bVal = b[sortField.value as keyof typeof b]
      
      if (aVal < bVal) return sortOrder.value === 'asc' ? -1 : 1
      if (aVal > bVal) return sortOrder.value === 'asc' ? 1 : -1
      return 0
    })
  }

  return filtered
})

const totalUsers = computed(() => total.value || filteredUsers.value.length)
const totalPages = computed(() => Math.ceil(totalUsers.value / pageSize.value))

const visiblePages = computed(() => {
  const pages = []
  const start = Math.max(1, currentPage.value - 2)
  const end = Math.min(totalPages.value, currentPage.value + 2)
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

// 方法
const getRoleText = (role: string) => {
  const roleMap = {
    admin: '管理员',
    grid: '网格员',
    user: '普通用户'
  }
  return roleMap[role as keyof typeof roleMap] || role
}

const getStatusText = (status: string) => {
  const statusMap = {
    active: '活跃',
    inactive: '非活跃',
    banned: '已禁用'
  }
  return statusMap[status as keyof typeof statusMap] || status
}

const formatDate = (date: string | Date | null | undefined) => {
  if (!date) return '暂无数据';
  
  // 如果是字符串，转换为Date对象
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  
  return dateObj.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const sortBy = (field: string) => {
  if (sortField.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortOrder.value = 'asc'
  }
}

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedUsers.value = filteredUsers.value.map(user => user.id)
  } else {
    selectedUsers.value = []
  }
}

const resetFilters = () => {
  searchQuery.value = ''
  selectedRole.value = ''
  selectedStatus.value = ''
  sortField.value = ''
  sortOrder.value = 'asc'
  currentPage.value = 1
  // 重新获取用户列表
  fetchUserList()
}

// 处理页码变化
const handlePageChange = (page: number) => {
  // 确保页码在有效范围内
  if (page < 1) page = 1
  if (page > totalPages.value) page = totalPages.value
  
  // 更新当前页码
  currentPage.value = page
  
  // 重新获取用户列表
  fetchUserList()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1 // 重置为第一页
  fetchUserList()
}

// 处理筛选条件变化
const handleFilterChange = () => {
  currentPage.value = 1 // 重置为第一页
  fetchUserList()
}

// 编辑用户 - 打开编辑用户对话框
const editUser = (user: any) => {
  console.log('编辑用户:', user)
  // 打开编辑用户对话框
  isEditMode.value = true
  currentUser.value = { ...user }
  userFormVisible.value = true
}

const viewUser = (user: any) => {
  console.log('查看用户:', user)
  // 这里可以打开用户详情的模态框或跳转到详情页面
}

const deleteUser = (user: any) => {
  ElMessageBox.confirm(`确定要删除用户 ${user.username} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      // 调用删除用户API
      await deleteUserApi(user.id)
      ElMessage.success('删除成功')
      // 重新获取用户列表
      fetchUserList()
    } catch (error) {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败，请重试')
    }
  }).catch(() => {
    // 用户取消删除操作
  })
}

// 打开新增用户对话框
const openAddUserDialog = () => {
  isEditMode.value = false
  currentUser.value = null
  userFormVisible.value = true
}



// 处理用户表单提交
const handleUserFormSubmit = async (userData: any) => {
  try {
    if (isEditMode.value) {
      // 更新现有用户
      await updateUser(userData.id, userData)
      ElMessage.success('用户信息更新成功')
    } else {
      // 添加新用户
      await createUser(userData)
      ElMessage.success('用户创建成功')
    }
    // 重新获取用户列表
    fetchUserList()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

onMounted(() => {
  // 组件挂载后获取用户列表
  fetchUserList()
})
</script>

<style lang="scss" scoped>
.user-management {
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

// 搜索区域
.search-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f1f5f9;

  .search-bar {
    display: flex;
    gap: 16px;
    align-items: center;

    .search-input-wrapper {
      flex: 1;
      position: relative;

      .search-icon {
        position: absolute;
        left: 16px;
        top: 50%;
        transform: translateY(-50%);
        width: 20px;
        height: 20px;
        color: #9ca3af;
      }

      .search-input {
        width: 100%;
        padding: 12px 16px 12px 48px;
        border: 1px solid #e5e7eb;
        border-radius: 10px;
        font-size: 14px;
        background: #f9fafb;
        transition: all 0.2s;

        &:focus {
          outline: none;
          border-color: #3b82f6;
          background: white;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        &::placeholder {
          color: #9ca3af;
        }
      }
    }

    .filter-controls {
      display: flex;
      gap: 12px;
      align-items: center;

      .filter-select {
        padding: 12px 16px;
        border: 1px solid #e5e7eb;
        border-radius: 10px;
        background: white;
        font-size: 14px;
        color: #374151;
        cursor: pointer;
        transition: all 0.2s;

        &:focus {
          outline: none;
          border-color: #3b82f6;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }
      }

      .filter-btn {
        padding: 12px;
        border: 1px solid #e5e7eb;
        border-radius: 10px;
        background: white;
        color: #6b7280;
        cursor: pointer;
        transition: all 0.2s;
        display: flex;
        align-items: center;
        justify-content: center;

        svg {
          width: 16px;
          height: 16px;
        }

        &:hover {
          background: #f3f4f6;
          color: #374151;
        }
      }
    }
  }
}

// 统计概览
.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 32px;

  .stat-item {
    background: white;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
    border: 1px solid #f1f5f9;
    transition: all 0.2s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
    }

    .stat-icon {
      width: 48px;
      height: 48px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;

      svg {
        width: 24px;
        height: 24px;
      }

      &.admin {
        background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
      }

      &.grid {
        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
      }

      &.user {
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      }

      &.total {
        background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
      }
    }

    .stat-content {
      .stat-value {
        font-size: 24px;
        font-weight: 700;
        color: #1e293b;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 14px;
        color: #64748b;
        font-weight: 500;
      }
    }
  }
}

// 用户表格容器
.user-table-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 1px solid #f1f5f9;
  overflow: hidden;

  .table-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px;
    border-bottom: 1px solid #f1f5f9;

    .table-title {
      font-size: 18px;
      font-weight: 600;
      color: #1e293b;
      margin: 0;
    }

    .table-actions {
      display: flex;
      gap: 12px;

      .table-action-btn {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 16px;
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        background: white;
        color: #374151;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;

        svg {
          width: 16px;
          height: 16px;
        }

        &:hover {
          background: #f9fafb;
          border-color: #d1d5db;
        }
      }
    }
  }

  .table-wrapper {
    overflow-x: auto;
  }

  .user-table {
    width: 100%;
    border-collapse: collapse;

    th {
      background: #f8fafc;
      padding: 16px;
      text-align: left;
      font-weight: 600;
      color: #374151;
      font-size: 14px;
      border-bottom: 1px solid #e5e7eb;
      white-space: nowrap;

      &.sortable {
        cursor: pointer;
        user-select: none;
        position: relative;

        &:hover {
          background: #f1f5f9;
        }

        .sort-icon {
          width: 16px;
          height: 16px;
          margin-left: 4px;
          opacity: 0.5;
          transition: opacity 0.2s;
        }

        &:hover .sort-icon {
          opacity: 1;
        }
      }

      &.checkbox-col {
        width: 48px;
        text-align: center;
      }
    }

    td {
      padding: 16px;
      border-bottom: 1px solid #f1f5f9;
      font-size: 14px;
      color: #374151;
      vertical-align: middle;

      &.checkbox-col {
        text-align: center;
      }
    }

    .user-row {
      transition: background-color 0.2s;

      &:hover {
        background: #f8fafc;
      }
    }

    .username-wrapper {
      display: flex;
      align-items: center;
      gap: 8px;

      .username {
        font-weight: 500;
        color: #1e293b;
      }

      .online-indicator {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #22c55e;
        animation: pulse 2s infinite;
      }
    }

    .avatar {
      width: 40px;
      height: 40px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-weight: 600;
      font-size: 16px;
    }

    .role-badge {
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.5px;

      &.admin {
        background: #dbeafe;
        color: #1d4ed8;
      }

      &.grid {
        background: #d1fae5;
        color: #059669;
      }

      &.user {
        background: #fef3c7;
        color: #d97706;
      }
    }

    .status-badge {
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;

      &.active {
        background: #d1fae5;
        color: #059669;
      }

      &.inactive {
        background: #f3f4f6;
        color: #6b7280;
      }

      &.banned {
        background: #fee2e2;
        color: #dc2626;
      }
    }

    .action-buttons {
      display: flex;
      gap: 8px;

      .action-btn {
        width: 32px;
        height: 32px;
        border: none;
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s;

        svg {
          width: 16px;
          height: 16px;
        }

        &.edit {
          background: #dbeafe;
          color: #1d4ed8;

          &:hover {
            background: #bfdbfe;
          }
        }

        &.view {
          background: #d1fae5;
          color: #059669;

          &:hover {
            background: #a7f3d0;
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

  // 分页
  .pagination {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px 24px;
    border-top: 1px solid #f1f5f9;

    .pagination-info {
      font-size: 14px;
      color: #6b7280;
    }

    .pagination-controls {
      display: flex;
      align-items: center;
      gap: 8px;

      .pagination-btn {
        width: 36px;
        height: 36px;
        border: 1px solid #e5e7eb;
        border-radius: 6px;
        background: white;
        color: #374151;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s;

        svg {
          width: 16px;
          height: 16px;
        }

        &:hover:not(:disabled) {
          background: #f9fafb;
          border-color: #d1d5db;
        }

        &:disabled {
          opacity: 0.5;
          cursor: not-allowed;
        }
      }

      .page-numbers {
        display: flex;
        gap: 4px;

        .page-btn {
          width: 36px;
          height: 36px;
          border: 1px solid #e5e7eb;
          border-radius: 6px;
          background: white;
          color: #374151;
          display: flex;
          align-items: center;
          justify-content: center;
          cursor: pointer;
          transition: all 0.2s;
          font-size: 14px;
          font-weight: 500;

          &:hover {
            background: #f9fafb;
            border-color: #d1d5db;
          }

          &.active {
            background: #3b82f6;
            border-color: #3b82f6;
            color: white;
          }
        }
      }
    }
  }
}

// 动画
@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-header .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-controls {
    justify-content: flex-start;
    flex-wrap: wrap;
  }

  .stats-overview {
    grid-template-columns: 1fr;
  }

  .table-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .table-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .pagination {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
}
</style>