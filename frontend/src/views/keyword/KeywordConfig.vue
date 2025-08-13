<template>
  <div class="keyword-config-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">关键词配置</h1>
          <p class="page-subtitle">管理系统中的关键词识别和处理规则</p>
        </div>
        <div class="header-actions">
          <button class="add-btn" @click="openAddDialog">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
            </svg>
            添加关键词
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
            placeholder="搜索关键词、描述或标签..."
            class="search-input"
          >
        </div>
        <div class="filter-controls">
          <select v-model="selectedCategory" class="filter-select">
            <option value="">所有分类</option>
            <option value="emergency">紧急事件</option>
            <option value="maintenance">维修保养</option>
            <option value="complaint">投诉建议</option>
            <option value="inquiry">咨询问答</option>
            <option value="other">其他</option>
          </select>
          <select v-model="selectedStatus" class="filter-select">
            <option value="">所有状态</option>
            <option value="active">已启用</option>
            <option value="inactive">已禁用</option>
          </select>
          <button class="reset-btn" @click="resetFilters">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
            重置
          </button>
        </div>
      </div>
    </div>

    <!-- 关键词统计卡片 -->
    <div class="stats-overview">
      <div class="stat-card total-card">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总关键词</div>
        </div>
      </div>
      <div class="stat-card active-card">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.active }}</div>
          <div class="stat-label">已启用</div>
        </div>
      </div>
      <div class="stat-card inactive-card">
        <div class="stat-icon">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stats.inactive }}</div>
          <div class="stat-label">已禁用</div>
        </div>
      </div>
    </div>

    <!-- 关键词列表 -->
    <div class="keyword-table-container">
      <div class="table-header">
        <h3 class="table-title">关键词列表</h3>
        <div class="table-actions">
          <button class="export-table-btn" @click="exportKeywords" :disabled="keywords.length === 0">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
            </svg>
            导出
          </button>
        </div>
      </div>
      
      <div class="table-wrapper">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="loading-spinner"></div>
          <div class="loading-text">加载中...</div>
        </div>
        
        <!-- 无数据提示 -->
        <div v-if="!loading && filteredKeywords.length === 0" class="empty-state">
          <svg class="empty-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M11 15h2v2h-2zm0-8h2v6h-2zm.99-5C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z"/>
          </svg>
          <p class="empty-text">暂无关键词数据</p>
        </div>
        
        <table class="keyword-table" v-if="!loading && filteredKeywords.length > 0">
          <thead>
            <tr>
              <th class="checkbox-column">
                <input type="checkbox" v-model="selectAll" @change="toggleSelectAll" class="table-checkbox">
              </th>
              <th class="sortable-column" @click="sortBy('keyword')">
                <span class="column-header">
                  关键词
                  <svg class="sort-icon" :class="{active: sortField === 'keyword'}" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M7 14l5-5 5 5z" v-if="sortOrder === 'asc' && sortField === 'keyword'"/>
                    <path d="M7 10l5 5 5-5z" v-else-if="sortOrder === 'desc' && sortField === 'keyword'"/>
                    <path d="M7 14l5-5 5 5z" v-else/>
                  </svg>
                </span>
              </th>
              <th class="description-column">描述</th>
              <th class="type-column">类型</th>
              <th class="priority-column">优先级</th>
              <th class="status-column">状态</th>
              <th class="hit-count-column">命中次数</th>
              <th class="trigger-threshold-column">触发阈值</th>
              <th class="sortable-column" @click="sortBy('createdAt')">
                <span class="column-header">
                  创建时间
                  <svg class="sort-icon" :class="{active: sortField === 'createdAt'}" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M7 14l5-5 5 5z" v-if="sortOrder === 'asc' && sortField === 'createdAt'"/>
                    <path d="M7 10l5 5 5-5z" v-else-if="sortOrder === 'desc' && sortField === 'createdAt'"/>
                    <path d="M7 14l5-5 5 5z" v-else/>
                  </svg>
                </span>
              </th>
              <th class="action-column">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="keyword in paginatedKeywords" :key="keyword.id" class="keyword-row">
              <td class="checkbox-column">
                <input type="checkbox" v-model="selectedKeywords" :value="keyword.id" class="table-checkbox">
              </td>
              <td class="keyword-column">
                <div class="keyword-info">
                  <div class="keyword-name">{{ keyword.keyword }}</div>
                </div>
              </td>
              <td class="description-column">
                <div class="description-text">{{ keyword.description || '-' }}</div>
              </td>
              <td class="type-column">
                <span class="type-badge" :class="`type-${keyword.type?.toLowerCase() || 'global'}`">
                  {{ getTypeLabel(keyword.type) }}
                </span>
              </td>
              <td class="priority-column">
                <span class="priority-badge" :class="`priority-${keyword.priority?.toLowerCase() || 'medium'}`">
                  {{ getPriorityLabel(keyword.priority) }}
                </span>
              </td>
              <td class="status-column">
                <span class="status-badge" :class="keyword.isActive ? 'status-active' : 'status-inactive'">
                  {{ keyword.isActive ? '已启用' : '已禁用' }}
                </span>
              </td>
              <td class="hit-count-column">
                <span class="hit-count">{{ keyword.hitCount || 0 }}</span>
              </td>
              <td class="trigger-threshold-column">
                <span class="trigger-threshold">{{ keyword.triggerThreshold || 3 }}</span>
              </td>
              <td class="time-column">
                <div class="time-info">
                  <div class="time-text">{{ formatDate(keyword.createdAt) }}</div>
                </div>
              </td>
              <td class="action-column">
                <div class="action-buttons">
                  <button class="action-btn edit-btn" @click="editKeyword(keyword)" title="编辑">
                    <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"/>
                    </svg>
                  </button>
                  <button class="action-btn toggle-btn" :class="keyword.isActive ? 'disable-btn' : 'enable-btn'" @click="toggleStatus(keyword)" :title="keyword.isActive ? '禁用' : '启用'">
                    <svg v-if="keyword.isActive" class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M17 7H7c-2.76 0-5 2.24-5 5s2.24 5 5 5h10c2.76 0 5-2.24 5-5s-2.24-5-5-5zM7 15c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3z"/>
                    </svg>
                    <svg v-else class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M17 7H7c-2.76 0-5 2.24-5 5s2.24 5 5 5h10c2.76 0 5-2.24 5-5s-2.24-5-5-5zm0 8c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3z"/>
                    </svg>
                  </button>
                  <button class="action-btn delete-btn" @click="handleDeleteKeyword(keyword)" title="删除">
                    <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
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
      <div class="pagination-container" v-if="!loading && filteredKeywords.length > 0">
        <div class="pagination-info">
          显示 {{ (currentPage - 1) * pageSize + 1 }} - {{ Math.min(currentPage * pageSize, filteredKeywords.length) }} 条，共 {{ filteredKeywords.length }} 条
        </div>
        <div class="pagination-controls">
          <button class="page-btn first-btn" @click="currentPage = 1" :disabled="currentPage === 1">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M18.41 16.59L13.82 12l4.59-4.59L17 6l-6 6 6 6zM6 6h2v12H6z"/>
            </svg>
          </button>
          <button class="page-btn prev-btn" @click="currentPage--" :disabled="currentPage === 1">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
            </svg>
          </button>
          <div class="page-numbers">
            <button 
              v-for="page in visiblePages" 
              :key="page" 
              class="page-btn number-btn" 
              :class="{active: page === currentPage}"
              @click="currentPage = page"
            >
              {{ page }}
            </button>
          </div>
          <button class="page-btn next-btn" @click="currentPage++" :disabled="currentPage === totalPages">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
            </svg>
          </button>
          <button class="page-btn last-btn" @click="currentPage = totalPages" :disabled="currentPage === totalPages">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M5.59 7.41L10.18 12l-4.59 4.59L7 18l6-6-6-6zM16 6h2v12h-2z"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div class="batch-actions-bar" v-if="selectedKeywords.length > 0">
      <div class="batch-info">
        已选择 {{ selectedKeywords.length }} 个关键词
      </div>
      <div class="batch-buttons">
        <button class="batch-btn enable-btn" @click="batchEnable">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          批量启用
        </button>
        <button class="batch-btn disable-btn" @click="batchDisable">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"/>
          </svg>
          批量禁用
        </button>
        <button class="batch-btn delete-btn" @click="batchDelete">
          <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
            <path d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"/>
          </svg>
          批量删除
        </button>
      </div>
    </div>

    <!-- 添加/编辑关键词对话框 -->
    <div class="modal-overlay" v-if="showDialog" @click="closeDialog">
      <div class="keyword-modal" @click.stop>
        <div class="modal-header">
          <h3 class="modal-title">{{ editingKeyword ? '编辑关键词' : '添加关键词' }}</h3>
          <button class="modal-close-btn" @click="closeDialog">
            <svg class="btn-icon" viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveKeyword" class="keyword-form">
            <div class="form-group">
              <label class="form-label">关键词 *</label>
              <input v-model="form.keyword" type="text" required placeholder="请输入关键词" class="form-input">
            </div>
            <div class="form-group">
              <label class="form-label">描述</label>
              <textarea v-model="form.description" placeholder="请输入关键词描述" class="form-textarea"></textarea>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">类型</label>
                <select v-model="form.type" class="form-select">
                  <option value="GLOBAL">全局</option>
                  <option value="LOCAL">本地</option>
                  <option value="CUSTOM">自定义</option>
                </select>
              </div>
              <div class="form-group">
                <label class="form-label">优先级</label>
                <select v-model="form.priority" class="form-select">
                  <option value="URGENT">紧急</option>
                  <option value="HIGH">高</option>
                  <option value="NORMAL">正常</option>
                  <option value="LOW">低</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">触发阈值</label>
                <input v-model.number="form.triggerThreshold" type="number" min="1" max="100" placeholder="默认3次" class="form-input">
                <small class="form-hint">客户端关键词触发多少次后自动上传到服务器</small>
              </div>
            </div>
            <div class="form-group">
              <label class="checkbox-label">
                <input type="checkbox" v-model="form.isActive" class="form-checkbox">
                <span class="checkbox-mark"></span>
                启用关键词
              </label>
            </div>
          </form>
        </div>
        <div class="modal-footer">
          <button class="modal-btn cancel-btn" @click="closeDialog">取消</button>
          <button class="modal-btn save-btn" @click="saveKeyword" :disabled="!form.keyword.trim()">保存</button>
        </div>
      </div>
    </div>


  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getKeywordList, createKeyword, updateKeyword, deleteKeyword as deleteKeywordApi, toggleKeywordStatus, getKeywordAnalysis } from '@/api/keyword'

// 响应式数据
const searchQuery = ref('')
const selectedCategory = ref('')
const selectedStatus = ref('')
const loading = ref(false)
const selectedKeywords = ref<number[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const sortField = ref('')
const sortOrder = ref('asc')

// 对话框相关
const showDialog = ref(false)
const editingKeyword = ref(null)
const form = ref({
  keyword: '',
  description: '',
  type: 'GLOBAL',
  priority: 'NORMAL',
  isActive: true,
  triggerThreshold: 3
})



// 关键词数据
const keywords = ref([])

const stats = computed(() => ({
  total: keywords.value.length,
  active: keywords.value.filter(k => k.isActive).length,
  inactive: keywords.value.filter(k => !k.isActive).length
}))

// 筛选后的关键词
const filteredKeywords = computed(() => {
  let filtered = keywords.value.filter(keyword => {
    const matchesSearch = !searchQuery.value || 
      keyword.keyword.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      (keyword.description && keyword.description.toLowerCase().includes(searchQuery.value.toLowerCase()))
    
    const matchesCategory = !selectedCategory.value || keyword.type === selectedCategory.value
    const matchesStatus = !selectedStatus.value || 
      (selectedStatus.value === 'active' && keyword.isActive) ||
      (selectedStatus.value === 'inactive' && !keyword.isActive)
    
    return matchesSearch && matchesCategory && matchesStatus
  })

  // 排序
  if (sortField.value) {
    filtered.sort((a, b) => {
      let aVal = a[sortField.value]
      let bVal = b[sortField.value]
      
      if (sortField.value === 'createdAt') {
        aVal = new Date(aVal).getTime()
        bVal = new Date(bVal).getTime()
      }
      
      if (sortOrder.value === 'asc') {
        return aVal > bVal ? 1 : -1
      } else {
        return aVal < bVal ? 1 : -1
      }
    })
  }

  return filtered
})

// 分页数据
const totalPages = computed(() => Math.ceil(filteredKeywords.value.length / pageSize.value))
const paginatedKeywords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredKeywords.value.slice(start, end)
})

// 分页相关
const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const pages = []
  
  if (total <= 7) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 4) {
      for (let i = 1; i <= 5; i++) {
        pages.push(i)
      }
      pages.push('...', total)
    } else if (current >= total - 3) {
      pages.push(1, '...')
      for (let i = total - 4; i <= total; i++) {
        pages.push(i)
      }
    } else {
      pages.push(1, '...', current - 1, current, current + 1, '...', total)
    }
  }
  
  return pages
})

// 全选状态
const selectAll = computed({
  get: () => selectedKeywords.value.length === paginatedKeywords.value.length && paginatedKeywords.value.length > 0,
  set: (value) => {
    if (value) {
      selectedKeywords.value = paginatedKeywords.value.map(k => k.id)
    } else {
      selectedKeywords.value = []
    }
  }
})

// 监听搜索和筛选变化
watch([searchQuery, selectedCategory, selectedStatus], () => {
  currentPage.value = 1
})

// 方法
const resetFilters = () => {
  searchQuery.value = ''
  selectedCategory.value = ''
  selectedStatus.value = ''
  currentPage.value = 1
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
  // 由计算属性处理
}

const openAddDialog = () => {
  editingKeyword.value = null
  form.value = {
    keyword: '',
    description: '',
    type: 'GLOBAL',
    priority: 'NORMAL',
    isActive: true,
    triggerThreshold: 3
  }
  showDialog.value = true
}

const closeDialog = () => {
  showDialog.value = false
  editingKeyword.value = null
}

const exportConfig = () => {
  const dataStr = JSON.stringify(keywords.value, null, 2)
  const dataBlob = new Blob([dataStr], { type: 'application/json' })
  const url = URL.createObjectURL(dataBlob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'keywords-config.json'
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('配置导出成功')
}

const exportKeywords = () => {
  exportConfig()
}

const editKeyword = (keyword: any) => {
  editingKeyword.value = keyword
  form.value = {
    keyword: keyword.keyword,
    description: keyword.description || '',
    type: keyword.type || 'GLOBAL',
    priority: keyword.priority || 'NORMAL',
    isActive: keyword.isActive,
    triggerThreshold: keyword.triggerThreshold || 3
  }
  showDialog.value = true
}

const saveKeyword = async () => {
  try {
    if (editingKeyword.value) {
      // 编辑
      await updateKeyword(editingKeyword.value.id, form.value)
      ElMessage.success('关键词更新成功')
    } else {
      // 新增
      await createKeyword(form.value)
      ElMessage.success('关键词添加成功')
    }
    closeDialog()
    fetchKeywords()
  } catch (error) {
    console.error('保存关键词失败:', error)
    ElMessage.error('保存关键词失败')
  }
}

const toggleStatus = async (keyword: any) => {
  try {
    await toggleKeywordStatus(keyword.id, !keyword.isActive)
    keyword.isActive = !keyword.isActive
    ElMessage.success(`关键词已${keyword.isActive ? '启用' : '禁用'}`)
  } catch (error) {
    console.error('切换状态失败:', error)
    ElMessage.error('切换状态失败')
  }
}

const handleDeleteKeyword = async (keyword: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个关键词吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteKeywordApi(keyword.id)
    ElMessage.success('删除成功')
    fetchKeywords() // 重新获取数据
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除关键词失败:', error)
      ElMessage.error('删除关键词失败')
    }
  }
}

const batchEnable = async () => {
  try {
    for (const id of selectedKeywords.value) {
      const keyword = keywords.value.find(k => k.id === id)
      if (keyword && !keyword.isActive) {
        await toggleKeywordStatus(id, true)
        keyword.isActive = true
      }
    }
    selectedKeywords.value = []
    ElMessage.success('批量启用成功')
  } catch (error) {
    console.error('批量启用失败:', error)
    ElMessage.error('批量启用失败')
  }
}

const batchDisable = async () => {
  try {
    for (const id of selectedKeywords.value) {
      const keyword = keywords.value.find(k => k.id === id)
      if (keyword && keyword.isActive) {
        await toggleKeywordStatus(id, false)
        keyword.isActive = false
      }
    }
    selectedKeywords.value = []
    ElMessage.success('批量禁用成功')
  } catch (error) {
    console.error('批量禁用失败:', error)
    ElMessage.error('批量禁用失败')
  }
}

const batchDelete = async () => {
  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedKeywords.value.length} 个关键词吗？`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    for (const id of selectedKeywords.value) {
      await deleteKeywordApi(id)
    }
    
    selectedKeywords.value = []
    ElMessage.success('批量删除成功')
    fetchKeywords()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const getTypeLabel = (type: string) => {
  const labels: Record<string, string> = {
    GLOBAL: '全局',
    LOCAL: '本地',
    CUSTOM: '自定义'
  }
  return labels[type] || type
}

const getPriorityLabel = (priority: string) => {
  const labels: Record<string, string> = {
    URGENT: '紧急',
    HIGH: '高',
    NORMAL: '正常',
    LOW: '低'
  }
  return labels[priority] || priority
}

const formatDate = (date: string) => {
  return new Date(date).toLocaleDateString('zh-CN')
}

// 获取关键词列表
const fetchKeywords = async () => {
  try {
      loading.value = true
      const response = await getKeywordList({})
      // 处理后端返回的数据结构
      if (response && response.keywords) {
        keywords.value = response.keywords
      } else if (response && Array.isArray(response)) {
        keywords.value = response
      } else {
        keywords.value = []
      }
    } catch (error) {
      console.error('获取关键词列表失败:', error)
      if (error.response) {
        console.error('错误详情:', error.response)
        ElMessage.error(`获取关键词列表失败: ${error.response.status} ${error.response.statusText}`)
      } else {
        ElMessage.error('获取关键词列表失败')
      }
    } finally {
      loading.value = false
    }
}




const formatTrendDate = (dateStr) => {
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()}`
}

onMounted(() => {
  fetchKeywords()
})
</script>

<style lang="scss" scoped>
/* 关键词配置页面样式 */
.keyword-config-page {
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
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

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
}

.export-btn, .add-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;

  .btn-icon {
    width: 18px;
    height: 18px;
  }
}

.export-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateY(-1px);
  }
}

.add-btn {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(10px);

  &:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: translateY(-1px);
  }
}

/* 搜索区域 */
.search-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.search-bar {
  display: flex;
  gap: 16px;
  align-items: center;
}

.search-input-wrapper {
  position: relative;
  flex: 1;
  max-width: 400px;

  .search-icon {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    width: 20px;
    height: 20px;
    color: #9ca3af;
  }

  .search-input {
    width: 100%;
    padding: 12px 12px 12px 44px;
    border: 2px solid #e5e7eb;
    border-radius: 8px;
    font-size: 14px;
    transition: border-color 0.2s;

    &:focus {
      outline: none;
      border-color: #3b82f6;
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
}

.filter-select {
  padding: 10px 12px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  transition: border-color 0.2s;

  &:focus {
    outline: none;
    border-color: #3b82f6;
  }
}

.reset-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  color: #6b7280;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  .btn-icon {
    width: 16px;
    height: 16px;
  }

  &:hover {
    border-color: #d1d5db;
    background: #f9fafb;
  }
}

/* 统计卡片 */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;

  &:hover {
    transform: translateY(-2px);
  }
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 24px;
    height: 24px;
  }
}

.total-card .stat-icon {
  background: #dbeafe;
  color: #1d4ed8;
}

.active-card .stat-icon {
  background: #dcfce7;
  color: #16a34a;
}

.inactive-card .stat-icon {
  background: #fee2e2;
  color: #dc2626;
}

.stat-content {
  .stat-value {
    font-size: 24px;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: 4px;
  }

  .stat-label {
    font-size: 14px;
    color: #6b7280;
  }
}

/* 表格容器 */
.keyword-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;

  .table-title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin: 0;
  }
}

.table-actions {
  .export-table-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    background: white;
    color: #374151;
    font-size: 14px;
    cursor: pointer;
    transition: all 0.2s;

    .btn-icon {
      width: 16px;
      height: 16px;
    }

    &:hover:not(:disabled) {
      border-color: #9ca3af;
      background: #f9fafb;
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

.table-wrapper {
  position: relative;
  overflow-x: auto;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #6b7280;

  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid #e5e7eb;
    border-top: 3px solid #3b82f6;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 16px;
  }

  .loading-text {
    font-size: 16px;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #6b7280;

  .empty-icon {
    width: 48px;
    height: 48px;
    margin-bottom: 16px;
    opacity: 0.5;
  }

  .empty-text {
    font-size: 16px;
    margin: 0;
  }
}

/* 表格样式 */
.keyword-table {
  width: 100%;
  border-collapse: collapse;

  th, td {
    padding: 16px;
    text-align: left;
    border-bottom: 1px solid #f3f4f6;
  }

  th {
    background: #f8fafc;
    font-weight: 600;
    color: #374151;
    font-size: 14px;
  }

  td {
    font-size: 14px;
    color: #1f2937;
  }
}

.checkbox-column {
  width: 48px;
  text-align: center;
}

.table-checkbox {
  width: 16px;
  height: 16px;
  cursor: pointer;
}

.sortable-column {
  cursor: pointer;
  user-select: none;
  transition: background-color 0.2s;

  &:hover {
    background: #f1f5f9;
  }

  .column-header {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .sort-icon {
    width: 16px;
    height: 16px;
    opacity: 0.4;
    transition: opacity 0.2s;

    &.active {
      opacity: 1;
    }
  }
}

.keyword-column {
  min-width: 150px;

  .keyword-name {
    font-weight: 500;
    color: #1f2937;
  }
}

.description-column {
  max-width: 200px;

  .description-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.type-column, .priority-column, .status-column {
  width: 100px;
}

.type-badge, .priority-badge, .status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  min-width: 60px;
}

.type-global {
  background: #dbeafe;
  color: #1d4ed8;
}

.type-local {
  background: #dcfce7;
  color: #16a34a;
}

.type-custom {
  background: #fef3c7;
  color: #d97706;
}

.priority-urgent {
  background: #fecaca;
  color: #b91c1c;
}

.priority-high {
  background: #fee2e2;
  color: #dc2626;
}

.priority-normal {
  background: #fef3c7;
  color: #d97706;
}

.priority-low {
  background: #dbeafe;
  color: #1d4ed8;
}

.status-active {
  background: #dcfce7;
  color: #16a34a;
}

.status-inactive {
  background: #f3f4f6;
  color: #6b7280;
}

.hit-count-column {
  width: 100px;
  text-align: center;

  .hit-count {
    font-weight: 500;
    color: #3b82f6;
  }
}

.trigger-threshold-column {
  width: 100px;
  text-align: center;

  .trigger-threshold {
    font-weight: 500;
    color: #059669;
    background: #ecfdf5;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 12px;
  }
}

.time-column {
  width: 120px;

  .time-text {
    color: #6b7280;
    font-size: 13px;
  }
}

.action-column {
  width: 140px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;

  .btn-icon {
    width: 16px;
    height: 16px;
  }
}

.edit-btn {
  background: #f3f4f6;
  color: #6b7280;

  &:hover {
    background: #e5e7eb;
    color: #374151;
  }
}

.enable-btn {
  background: #dcfce7;
  color: #16a34a;

  &:hover {
    background: #bbf7d0;
  }
}

.disable-btn {
  background: #fef3c7;
  color: #d97706;

  &:hover {
    background: #fde68a;
  }
}

.delete-btn {
  background: #fee2e2;
  color: #dc2626;

  &:hover {
    background: #fecaca;
  }
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
}

.pagination-info {
  font-size: 14px;
  color: #6b7280;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: white;
  color: #6b7280;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;

  .btn-icon {
    width: 16px;
    height: 16px;
  }

  &:hover:not(:disabled) {
    border-color: #3b82f6;
    color: #3b82f6;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }

  &.active {
    background: #3b82f6;
    border-color: #3b82f6;
    color: white;
  }
}

.page-numbers {
  display: flex;
  gap: 4px;
}

/* 批量操作栏 */
.batch-actions-bar {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  background: white;
  border-radius: 12px;
  padding: 16px 24px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  gap: 20px;
  z-index: 1000;
}

.batch-info {
  font-size: 14px;
  color: #374151;
  font-weight: 500;
}

.batch-buttons {
  display: flex;
  gap: 12px;
}

.batch-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  .btn-icon {
    width: 16px;
    height: 16px;
  }

  &.enable-btn {
    background: #dcfce7;
    color: #16a34a;

    &:hover {
      background: #bbf7d0;
    }
  }

  &.disable-btn {
    background: #fef3c7;
    color: #d97706;

    &:hover {
      background: #fde68a;
    }
  }

  &.delete-btn {
    background: #fee2e2;
    color: #dc2626;

    &:hover {
      background: #fecaca;
    }
  }
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.keyword-modal {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e5e7eb;

  .modal-title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin: 0;
  }

  .modal-close-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
    height: 32px;
    border: none;
    border-radius: 6px;
    background: #f3f4f6;
    color: #6b7280;
    cursor: pointer;
    transition: all 0.2s;

    .btn-icon {
      width: 18px;
      height: 18px;
    }

    &:hover {
      background: #e5e7eb;
      color: #374151;
    }
  }
}

.modal-body {
  padding: 24px;
  max-height: 60vh;
  overflow-y: auto;
}

.keyword-form {
  .form-group {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .form-label {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: #374151;
    margin-bottom: 6px;
  }

  .form-input, .form-textarea, .form-select {
    width: 100%;
    padding: 10px 12px;
    border: 2px solid #e5e7eb;
    border-radius: 6px;
    font-size: 14px;
    transition: border-color 0.2s;

    &:focus {
      outline: none;
      border-color: #3b82f6;
    }

    &::placeholder {
      color: #9ca3af;
    }
  }

  .form-textarea {
    min-height: 80px;
    resize: vertical;
  }

  .form-row {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
  }

  .form-hint {
    display: block;
    font-size: 12px;
    color: #6b7280;
    margin-top: 4px;
    line-height: 1.4;
  }

  .checkbox-label {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    font-size: 14px;
    color: #374151;

    .form-checkbox {
      width: 16px;
      height: 16px;
    }

    .checkbox-mark {
      position: relative;
      width: 16px;
      height: 16px;
      border: 2px solid #d1d5db;
      border-radius: 3px;
      transition: all 0.2s;

      &::after {
        content: '';
        position: absolute;
        left: 2px;
        top: -1px;
        width: 6px;
        height: 10px;
        border: solid white;
        border-width: 0 2px 2px 0;
        transform: rotate(45deg);
        opacity: 0;
        transition: opacity 0.2s;
      }
    }

    input:checked + .checkbox-mark {
      background: #3b82f6;
      border-color: #3b82f6;

      &::after {
        opacity: 1;
      }
    }
  }
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #e5e7eb;
}

.modal-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &.cancel-btn {
    background: #f3f4f6;
    color: #374151;

    &:hover {
      background: #e5e7eb;
    }
  }

  &.save-btn {
    background: #3b82f6;
    color: white;

    &:hover:not(:disabled) {
      background: #2563eb;
    }

    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .keyword-config-page {
    padding: 16px;
  }

  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .search-bar {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .filter-controls {
    flex-wrap: wrap;
  }

  .stats-overview {
    grid-template-columns: 1fr;
  }

  .table-wrapper {
    overflow-x: scroll;
  }

  .pagination-container {
     flex-direction: column;
     gap: 12px;
     align-items: center;
   }

   .batch-actions-bar {
     position: relative;
     bottom: auto;
     left: auto;
     transform: none;
     margin-top: 20px;
     flex-direction: column;
     gap: 12px;
   }

   .batch-buttons {
     flex-wrap: wrap;
     justify-content: center;
   }

   .keyword-modal {
    margin: 16px;
    max-width: calc(100% - 32px);
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}

/* 统计分析按钮样式 */
.analysis-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: #8b5cf6;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background: #7c3aed;
    transform: translateY(-1px);
  }

  .btn-icon {
    width: 16px;
    height: 16px;
  }
}




</style>