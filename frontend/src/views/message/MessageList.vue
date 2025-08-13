<template>
  <div class="message-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">消息管理</h1>
          <p class="page-subtitle">管理和监控系统中的所有消息</p>
        </div>
        <div class="header-actions">
          <!-- 按钮已移除 -->
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
            placeholder="搜索消息内容、发送者或关键词..."
            class="search-input"
          >
        </div>
        <div class="filter-controls">
          <select v-model="selectedType" class="filter-select">
            <option value="">所有类型</option>
            <option value="text">文本消息</option>
            <option value="image">图片消息</option>
            <option value="voice">语音消息</option>
            <option value="video">视频消息</option>
          </select>
          <select v-model="selectedStatus" class="filter-select">
            <option value="">所有状态</option>
            <option value="pending">待处理</option>
            <option value="processed">已处理</option>
            <option value="forwarded">已转发</option>
            <option value="archived">已归档</option>
          </select>
          <select v-model="selectedPriority" class="filter-select">
            <option value="high">高优先级</option>
            <option value="medium">中优先级</option>
            <option value="low">低优先级</option>
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

    <!-- 消息统计卡片 -->
    <div class="stats-overview">
      <div class="stat-item">
        <div class="stat-icon total">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ messageStats.total }}</div>
          <div class="stat-label">总消息数</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon pending">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ messageStats.pending }}</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon processed">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ messageStats.processed }}</div>
          <div class="stat-label">已处理</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon forwarded">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 8V4l8 8-8 8v-4H4V8z"/>
          </svg>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ messageStats.forwarded }}</div>
          <div class="stat-label">已转发</div>
        </div>
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="message-table-container">
      <div class="table-header">
        <h3 class="table-title">消息列表</h3>
        <div class="table-actions">
          <button class="table-action-btn" @click="exportMessages">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
            </svg>
            导出
          </button>
        </div>
      </div>
      
      <div class="table-wrapper">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-overlay">
          <div class="loading-spinner"></div>
          <div class="loading-text">加载中...</div>
        </div>
        
        <!-- 无数据提示 -->
        <div v-if="!loading && messages.length === 0" class="no-data">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <path d="M11 15h2v2h-2zm0-8h2v6h-2zm.99-5C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8z"/>
          </svg>
          <p>暂无消息数据</p>
        </div>
        
        <table class="message-table" v-if="!loading && messages.length > 0">
          <thead>
            <tr>
              <th class="checkbox-col">
                <input type="checkbox" v-model="selectAll" @change="toggleSelectAll">
              </th>
              <th class="sortable" @click="sortBy('sender')">
                发送者
                <svg class="sort-icon" :class="{active: sortField === 'sender'}" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M7 14l5-5 5 5z" v-if="sortOrder === 'asc' && sortField === 'sender'"/>
                  <path d="M7 10l5 5 5-5z" v-else-if="sortOrder === 'desc' && sortField === 'sender'"/>
                  <path d="M7 14l5-5 5 5z" v-else/>
                </svg>
              </th>
              <th class="sortable" @click="sortBy('content')">
                消息内容
                <svg class="sort-icon" :class="{active: sortField === 'content'}" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M7 14l5-5 5 5z" v-if="sortOrder === 'asc' && sortField === 'content'"/>
                  <path d="M7 10l5 5 5-5z" v-else-if="sortOrder === 'desc' && sortField === 'content'"/>
                  <path d="M7 14l5-5 5 5z" v-else/>
                </svg>
              </th>
              <th>类型</th>
              <th>优先级</th>
              <th>状态</th>
              <th>关键词</th>
              <th class="sortable" @click="sortBy('createdAt')">
                接收时间
                <svg class="sort-icon" :class="{active: sortField === 'createdAt'}" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M7 14l5-5 5 5z" v-if="sortOrder === 'asc' && sortField === 'createdAt'"/>
                  <path d="M7 10l5 5 5-5z" v-else-if="sortOrder === 'desc' && sortField === 'createdAt'"/>
                  <path d="M7 14l5-5 5 5z" v-else/>
                </svg>
              </th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="message in paginatedMessages" :key="message.id" class="message-row">
              <td class="checkbox-col">
                <input type="checkbox" v-model="selectedMessages" :value="message.id">
              </td>
              <td class="sender-col">
                <div class="sender-info">
                  <div class="sender-avatar" :style="{ background: message.senderColor }">
                    {{ message.sender.charAt(0).toUpperCase() }}
                  </div>
                  <div class="sender-details">
                    <div class="sender-name">{{ message.sender }}</div>
                    <div class="sender-phone">{{ message.phone }}</div>
                  </div>
                </div>
              </td>
              <td class="content-col">
                <div class="message-content">
                  <div class="content-text">{{ truncateText(message.content, 50) }}</div>
                  <div v-if="message.attachments && message.attachments.length > 0" class="attachments">
                    <span class="attachment-count">{{ message.attachments.length }} 个附件</span>
                  </div>
                </div>
              </td>
              <td class="type-col">
                <span class="type-badge" :class="message.type">
                  <svg v-if="message.type === 'text'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"/>
                  </svg>
                  <svg v-else-if="message.type === 'image'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"/>
                  </svg>
                  <svg v-else-if="message.type === 'voice'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 14c1.66 0 2.99-1.34 2.99-3L15 5c0-1.66-1.34-3-3-3S9 3.34 9 5v6c0 1.66 1.34 3 3 3zm5.3-3c0 3-2.54 5.1-5.3 5.1S6.7 14 6.7 11H5c0 3.41 2.72 6.23 6 6.72V21h2v-3.28c3.28-.48 6-3.3 6-6.72h-1.7z"/>
                  </svg>
                  <svg v-else-if="message.type === 'video'" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M17 10.5V7c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.55 0 1-.45 1-1v-3.5l4 4v-11l-4 4z"/>
                  </svg>
                  {{ getTypeText(message.type) }}
                </span>
              </td>
              <td class="priority-col">
                <span class="priority-badge" :class="message.priority">
                  {{ getPriorityText(message.priority) }}
                </span>
              </td>
              <td class="status-col">
                <span class="status-badge" :class="message.status">
                  {{ getStatusText(message.status) }}
                </span>
              </td>
              <td class="keywords-col">
                <div class="keywords">
                  <span v-for="keyword in message.keywords" :key="keyword" class="keyword-tag">
                    {{ keyword }}
                  </span>
                </div>
              </td>
              <td class="created-at-col">{{ formatDate(message.createdAt) }}</td>
              <td class="actions-col">
                <div class="action-buttons">
                  <button class="action-btn view" @click="viewMessage(message)" title="查看详情">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                    </svg>
                  </button>
                  <button class="action-btn forward" @click="forwardMessage(message)" title="转发">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 8V4l8 8-8 8v-4H4V8z"/>
                    </svg>
                  </button>
                  <button class="action-btn process" @click="processMessage(message)" title="处理">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"/>
                    </svg>
                  </button>
                  <button class="action-btn archive" @click="archiveMessage(message)" title="归档">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M20.54 5.23l-1.39-1.68C18.88 3.21 18.47 3 18 3H6c-.47 0-.88.21-1.16.55L3.46 5.23C3.17 5.57 3 6.02 3 6.5V19c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V6.5c0-.48-.17-.93-.46-1.27zM12 17.5L6.5 12H10v-2h4v2h3.5L12 17.5zM5.12 5l.81-1h12l.94 1H5.12z"/>
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
          显示 {{ (currentPage - 1) * pageSize + 1 }} - {{ Math.min(currentPage * pageSize, totalCount) }} 条，共 {{ totalCount }} 条
        </div>
        <div class="pagination-controls">
          <button 
            class="pagination-btn" 
            :disabled="currentPage === 1 || loading" 
            @click="currentPage--"
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
              @click="currentPage = page"
              :disabled="loading"
            >
              {{ page }}
            </button>
          </span>
          <button 
            class="pagination-btn" 
            :disabled="currentPage === totalPages || loading" 
            @click="currentPage++"
          >
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
            </svg>
          </button>
          <select v-model="pageSize" :disabled="loading" class="page-size-select">
            <option value="10">10条/页</option>
            <option value="20">20条/页</option>
            <option value="50">50条/页</option>
            <option value="100">100条/页</option>
          </select>
        </div>
      </div>
    </div>
    
    <!-- 消息详情模态框 -->
    <div v-if="showMessageDetail" class="message-detail-modal">
      <div class="modal-overlay" @click="showMessageDetail = false"></div>
      <div class="modal-container">
        <div class="modal-header">
          <h2 class="modal-title">消息详情</h2>
          <button class="modal-close-btn" @click="showMessageDetail = false">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>
        <div class="modal-content" v-if="currentMessage">
          <div class="message-detail-item">
            <div class="detail-label">发送者</div>
            <div class="detail-value">{{ currentMessage.sender }}</div>
          </div>
          <div class="message-detail-item">
            <div class="detail-label">类型</div>
            <div class="detail-value">
              <span class="type-badge" :class="currentMessage.type">
                {{ getTypeText(currentMessage.type) }}
              </span>
            </div>
          </div>
          <div class="message-detail-item">
            <div class="detail-label">优先级</div>
            <div class="detail-value">
              <span class="priority-badge" :class="currentMessage.priority">
                {{ getPriorityText(currentMessage.priority) }}
              </span>
            </div>
          </div>
          <div class="message-detail-item">
            <div class="detail-label">状态</div>
            <div class="detail-value">
              <span class="status-badge" :class="currentMessage.status">
                {{ getStatusText(currentMessage.status) }}
              </span>
            </div>
          </div>
          <div class="message-detail-item">
            <div class="detail-label">时间</div>
            <div class="detail-value">{{ formatDate(currentMessage.createdAt) }}</div>
          </div>
          <div class="message-detail-item">
            <div class="detail-label">关键词</div>
            <div class="detail-value">
              <div class="keywords">
                <span v-for="keyword in currentMessage.keywords" :key="keyword" class="keyword-tag">
                  {{ keyword }}
                </span>
              </div>
            </div>
          </div>
          <div class="message-detail-item full-width">
            <div class="detail-label">内容</div>
            <div class="detail-value content-box">{{ currentMessage.content }}</div>
          </div>
          <div class="message-detail-item full-width" v-if="currentMessage.attachments && currentMessage.attachments.length > 0">
            <div class="detail-label">附件</div>
            <div class="detail-value">
              <div class="attachments-list">
                <div v-for="(attachment, index) in currentMessage.attachments" :key="index" class="attachment-item">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"/>
                  </svg>
                  <span class="attachment-name">{{ attachment }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <div class="modal-actions">
            <button class="modal-action-btn process" @click="processMessage(currentMessage)">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"/>
              </svg>
              标记为已处理
            </button>
            <button class="modal-action-btn forward" @click="forwardMessage(currentMessage)">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 8V4l8 8-8 8v-4H4V8z"/>
              </svg>
              转发
            </button>
            <button class="modal-action-btn archive" @click="archiveMessage(currentMessage)">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M20.54 5.23l-1.39-1.68C18.88 3.21 18.47 3 18 3H6c-.47 0-.88.21-1.16.55L3.46 5.23C3.17 5.57 3 6.02 3 6.5V19c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V6.5c0-.48-.17-.93-.46-1.27zM12 17.5L6.5 12H10v-2h4v2h3.5L12 17.5zM5.12 5l.81-1h12l.94 1H5.12z"/>
              </svg>
              归档
            </button>
          </div>
          <button class="modal-close-action" @click="showMessageDetail = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'

// 响应式数据
const searchQuery = ref('')
const selectedType = ref('')
const selectedStatus = ref('')
const selectedPriority = ref('')
const selectedMessages = ref<number[]>([])
const selectAll = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const sortField = ref('')
const sortOrder = ref('asc')


// 导入API函数
import { getMessageList, getMessageDetail, updateMessage } from '@/api/message';
import { ElMessage, ElLoading } from 'element-plus';

// 消息数据
const messages = ref<any[]>([])
const loading = ref(false)
const totalCount = ref(0)

// 查询参数
const queryParams = ref({
  page: 0,
  size: 10,
  content: '',
  sender: '',
  dateRange: null as [string, string] | null
})

// 加载消息列表数据
const loadMessages = async () => {
  loading.value = true
  try {
    // 构建查询参数
    const params = {
      ...queryParams.value,
      page: currentPage.value - 1, // 后端分页从0开始
      size: pageSize.value,
      content: searchQuery.value || undefined
    }
    
    const res = await getMessageList(params)
    if (res.data) {
      // 处理返回的消息数据
      messages.value = res.data.content.map((item: any) => ({
        id: item.id,
        sender: item.senderName || '未知发送者',
        phone: item.senderId || '',
        content: item.content || '',
        type: item.type?.toLowerCase() || 'text',
        priority: getPriorityByContent(item.content),
        status: item.isForwarded ? 'forwarded' : 'pending',
        keywords: item.keywordsMatched ? item.keywordsMatched.split(',') : [],
        attachments: item.attachments || [],
        createdAt: new Date(item.receivedAt || Date.now()),
        senderColor: getRandomColor(item.senderName || '')
      }))
      
      totalCount.value = res.data.totalElements || 0
      currentPage.value = res.data.number + 1 // 后端分页从0开始
      pageSize.value = res.data.size
    }
  } catch (error) {
    console.error('加载消息列表失败:', error)
    ElMessage.error('加载消息列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 根据内容判断优先级
const getPriorityByContent = (content: string) => {
  if (!content) return 'low'
  
  const highPriorityKeywords = ['紧急', '危险', '故障', '投诉']
  const mediumPriorityKeywords = ['建议', '维修', '问题']
  
  if (highPriorityKeywords.some(keyword => content.includes(keyword))) {
    return 'high'
  } else if (mediumPriorityKeywords.some(keyword => content.includes(keyword))) {
    return 'medium'
  }
  
  return 'low'
}

// 根据名称生成随机颜色
const getRandomColor = (name: string) => {
  const colors = [
    '#3b82f6', '#10b981', '#f59e0b', '#ef4444', 
    '#8b5cf6', '#06b6d4', '#ec4899', '#6366f1'
  ]
  
  // 使用名称的哈希值来确定颜色索引，确保同一个名称总是得到相同的颜色
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  
  const index = Math.abs(hash) % colors.length
  return colors[index]
}

// 计算属性
const messageStats = computed(() => {
  const stats = { total: 0, pending: 0, processed: 0, forwarded: 0 }
  messages.value.forEach(message => {
    stats.total++
    stats[message.status as keyof typeof stats]++
  })
  return stats
})

const filteredMessages = computed(() => {
  let filtered = messages.value

  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(message => 
      message.content.toLowerCase().includes(query) ||
      message.sender.toLowerCase().includes(query) ||
      message.keywords.some(keyword => keyword.toLowerCase().includes(query))
    )
  }

  // 类型过滤
  if (selectedType.value) {
    filtered = filtered.filter(message => message.type === selectedType.value)
  }

  // 状态过滤
  if (selectedStatus.value) {
    filtered = filtered.filter(message => message.status === selectedStatus.value)
  }

  // 优先级过滤
  if (selectedPriority.value) {
    filtered = filtered.filter(message => message.priority === selectedPriority.value)
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

// 使用后端分页，totalCount已经从API获取
const totalPages = computed(() => Math.ceil(totalCount.value / pageSize.value))

// 分页数据直接使用过滤后的消息
const paginatedMessages = computed(() => filteredMessages.value)

// 计算可见的页码
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
const getTypeText = (type: string) => {
  const typeMap = {
    text: '文本',
    image: '图片',
    voice: '语音',
    video: '视频'
  }
  return typeMap[type as keyof typeof typeMap] || type
}

const getPriorityText = (priority: string) => {
  const priorityMap = {
    high: '高',
    medium: '中',
    low: '低'
  }
  return priorityMap[priority as keyof typeof priorityMap] || priority
}

const getStatusText = (status: string) => {
  const statusMap = {
    pending: '待处理',
    processed: '已处理',
    forwarded: '已转发',
    archived: '已归档'
  }
  return statusMap[status as keyof typeof statusMap] || status
}

const formatDate = (date: Date) => {
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const truncateText = (text: string, maxLength: number) => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
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
    selectedMessages.value = filteredMessages.value.map(message => message.id)
  } else {
    selectedMessages.value = []
  }
}

const resetFilters = () => {
  searchQuery.value = ''
  selectedType.value = ''
  selectedStatus.value = ''
  selectedPriority.value = ''
  sortField.value = ''
  sortOrder.value = 'asc'
  
  // 重置查询参数
  queryParams.value = {
    page: 0,
    size: 10,
    content: '',
    sender: '',
    dateRange: null
  }
  
  // 重新加载数据
  loadMessages()
}

const showMessageDetail = ref(false)
const currentMessage = ref<any>(null)

const viewMessage = async (message: any) => {
  try {
    // 从后端获取消息详情
    const res = await getMessageDetail(message.id)
    if (res) {
      // 使用后端返回的详细数据更新当前消息
      currentMessage.value = {
        ...message,
        // 可以添加后端返回的额外字段
        fullContent: res.content,
        receivedAt: res.timestamp
      }
      showMessageDetail.value = true
    }
  } catch (error) {
    console.error('获取消息详情失败:', error)
    ElMessage.error('获取消息详情失败，请稍后重试')
    // 如果获取详情失败，使用列表中的数据显示
    currentMessage.value = message
    showMessageDetail.value = true
  }
}

const forwardMessage = async (message: any) => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '转发中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    // 调用API更新消息状态为已转发
    await updateMessage(message.id, { isForwarded: true })
    
    // 更新本地状态
    message.status = 'forwarded'
    ElMessage.success('消息已成功转发')
    
    // 如果是在详情模态框中操作，关闭模态框
    if (showMessageDetail.value) {
      showMessageDetail.value = false
    }
    
    // 重新加载消息列表
    loadMessages()
    
    loading.close()
  } catch (error) {
    console.error('转发消息失败:', error)
    ElMessage.error('转发消息失败，请稍后重试')
  }
}

const processMessage = async (message: any) => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '处理中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    // 调用API更新消息状态
    await updateMessage(message.id, { status: 'PROCESSED' })
    
    // 更新本地状态
    message.status = 'processed'
    ElMessage.success('消息已标记为已处理')
    
    // 如果是在模态框中操作，关闭模态框
    if (showMessageDetail.value) {
      showMessageDetail.value = false
    }
    
    // 重新加载消息列表
    loadMessages()
    
    loading.close()
  } catch (error) {
    console.error('处理消息失败:', error)
    ElMessage.error('处理消息失败，请稍后重试')
  }
}

const archiveMessage = async (message: any) => {
  try {
    const loading = ElLoading.service({
      lock: true,
      text: '归档中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    
    // 调用API更新消息状态为已归档
    await updateMessage(message.id, { status: 'ARCHIVED' })
    
    // 更新本地状态
    message.status = 'archived'
    ElMessage.success('消息已成功归档')
    
    // 如果是在模态框中操作，关闭模态框
    if (showMessageDetail.value) {
      showMessageDetail.value = false
    }
    
    // 重新加载消息列表
    loadMessages()
    
    loading.close()
  } catch (error) {
    console.error('归档消息失败:', error)
    ElMessage.error('归档消息失败，请稍后重试')
  }
}

const exportMessages = async () => {
  // 检查是否有选中的消息
  if (selectedMessages.value.length === 0) {
    ElMessage.warning('请选择要导出的消息')
    return
  }

  try {
    const loading = ElLoading.service({
      lock: true,
      text: '正在准备导出数据...',
      background: 'rgba(0, 0, 0, 0.7)'
    })

    // 获取选中的消息数据
    const messagesToExport = messages.value.filter(message => 
      selectedMessages.value.includes(message.id)
    )

    // 如果需要从后端获取完整数据，可以添加API调用
    // const exportData = await getMessageExportData(selectedMessages.value)
    // const messagesToExport = exportData.messages

    // 转换为CSV格式
    const headers = ['发送者', '内容', '类型', '优先级', '状态', '关键词', '接收时间']
    const csvContent = [
      headers.join(','),
      ...messagesToExport.map(message => [
        message.sender,
        `"${message.content.replace(/"/g, '""')}"`, // 处理内容中的引号
        getTypeText(message.type),
        getPriorityText(message.priority),
        getStatusText(message.status),
        message.keywords.join('|'),
        formatDate(message.createdAt)
      ].join(','))
    ].join('\n')

    // 创建下载链接
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.setAttribute('href', url)
    link.setAttribute('download', `消息导出_${new Date().toISOString().slice(0, 10)}.csv`)
    link.style.visibility = 'hidden'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    loading.close()
    ElMessage.success('消息导出成功')
  } catch (error) {
    console.error('导出消息失败:', error)
    ElMessage.error('导出消息失败，请稍后重试')
  }
}

// 监听搜索查询变化
watch(searchQuery, (newVal) => {
  // 更新查询参数
  queryParams.value.content = newVal
  // 重置到第一页
  currentPage.value = 1
  // 延迟执行搜索，避免频繁请求
  const debounceTimer = setTimeout(() => {
    loadMessages()
  }, 500)
  
  return () => clearTimeout(debounceTimer)
})

// 监听页码变化
watch(currentPage, () => {
  loadMessages()
})

// 监听每页条数变化
watch(pageSize, () => {
  // 重置到第一页
  currentPage.value = 1
  loadMessages()
})

onMounted(() => {
  // 组件挂载后加载消息列表
  loadMessages()
})
</script>

<style lang="scss" scoped>
.message-management {
  padding: 0;
  max-width: 100%;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

// 加载状态样式
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 10;
  border-radius: 12px;
  
  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid #f3f3f3;
    border-top: 3px solid #3b82f6;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 16px;
  }
  
  .loading-text {
    font-size: 16px;
    color: #4b5563;
    font-weight: 500;
  }
  
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
}

// 无数据提示样式
.no-data {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 48px 0;
  color: #9ca3af;
  
  svg {
    width: 48px;
    height: 48px;
    margin-bottom: 16px;
  }
  
  p {
    font-size: 16px;
    font-weight: 500;
    margin: 0;
  }
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
        min-width: 120px;

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

      &.total {
        background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
      }

      &.pending {
        background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
      }

      &.processed {
        background: linear-gradient(135deg, #10b981 0%, #059669 100%);
      }

      &.forwarded {
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

// 消息表格容器
.message-table-container {
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
        border-radius: 10px;
        background: white;
        color: #374151;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.2s;
        position: relative;
        overflow: hidden;

        &::before {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background: linear-gradient(135deg, rgba(79, 70, 229, 0.1) 0%, rgba(79, 70, 229, 0) 100%);
          opacity: 0;
          transition: opacity 0.3s ease;
        }

        svg {
          width: 18px;
          height: 18px;
          transition: transform 0.2s ease;
        }

        &:hover {
          background: #f9fafb;
          border-color: #4f46e5;
          color: #4f46e5;
          box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
          transform: translateY(-1px);

          &::before {
            opacity: 1;
          }

          svg {
            transform: scale(1.1);
          }
        }

        &:active {
          transform: translateY(0);
        }
      }
    }
  }

  .table-wrapper {
    overflow-x: auto;
  }

  .message-table {
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
          transition: all 0.2s;
          
          &.active {
            opacity: 1;
            color: #3b82f6;
          }
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

    .message-row {
      transition: background-color 0.2s;

      &:hover {
        background: #f8fafc;
      }
    }

    .sender-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .sender-avatar {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-weight: 600;
        font-size: 16px;
        flex-shrink: 0;
      }

      .sender-details {
        .sender-name {
          font-weight: 500;
          color: #1e293b;
          margin-bottom: 2px;
        }

        .sender-phone {
          font-size: 12px;
          color: #6b7280;
        }
      }
    }

    .message-content {
      .content-text {
        color: #374151;
        line-height: 1.4;
        margin-bottom: 4px;
      }

      .attachments {
        .attachment-count {
          font-size: 12px;
          color: #6b7280;
          background: #f3f4f6;
          padding: 2px 8px;
          border-radius: 12px;
        }
      }
    }

    .type-badge {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;

      svg {
        width: 14px;
        height: 14px;
      }

      &.text {
        background: #dbeafe;
        color: #1d4ed8;
      }

      &.image {
        background: #d1fae5;
        color: #059669;
      }

      &.voice {
        background: #fef3c7;
        color: #d97706;
      }

      &.video {
        background: #ede9fe;
        color: #7c3aed;
      }
    }

    .priority-badge {
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;

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
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;

      &.pending {
        background: #fef3c7;
        color: #d97706;
      }

      &.processed {
        background: #d1fae5;
        color: #059669;
      }

      &.forwarded {
        background: #dbeafe;
        color: #1d4ed8;
      }

      &.archived {
        background: #f3f4f6;
        color: #6b7280;
      }
    }

    .keywords {
      display: flex;
      flex-wrap: wrap;
      gap: 4px;

      .keyword-tag {
        background: #f1f5f9;
        color: #64748b;
        padding: 2px 8px;
        border-radius: 12px;
        font-size: 11px;
        font-weight: 500;
      }
    }

    .action-buttons {
      display: flex;
      gap: 6px;

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

        &.view {
          background: #dbeafe;
          color: #1d4ed8;

          &:hover {
            background: #bfdbfe;
          }
        }

        &.forward {
          background: #ede9fe;
          color: #7c3aed;

          &:hover {
            background: #ddd6fe;
          }
        }

        &.process {
          background: #d1fae5;
          color: #059669;

          &:hover {
            background: #a7f3d0;
          }
        }

        &.archive {
          background: #f3f4f6;
          color: #6b7280;

          &:hover {
            background: #e5e7eb;
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
    }
  }
  
  /* 消息详情模态框样式 */
  .message-detail-modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
  }
  
  .modal-overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    backdrop-filter: blur(2px);
  }
  
  .modal-container {
    position: relative;
    width: 90%;
    max-width: 800px;
    max-height: 90vh;
    background-color: white;
    border-radius: 12px;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    animation: modalFadeIn 0.3s ease-out;
  }
  
  @keyframes modalFadeIn {
    from {
      opacity: 0;
      transform: translateY(20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
  
  .modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px 24px;
    border-bottom: 1px solid #e5e7eb;
  }
  
  .modal-title {
    font-size: 20px;
    font-weight: 600;
    color: #111827;
      margin: 0;
  }
  
  .modal-close-btn {
    background: none;
    border: none;
    cursor: pointer;
    color: #6b7280;
    padding: 4px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
  }
  
  .modal-close-btn:hover {
    background-color: #f3f4f6;
    color: #111827;
  }
  
  .modal-close-btn svg {
    width: 24px;
    height: 24px;
  }
  
  .modal-content {
    padding: 24px;
    overflow-y: auto;
    max-height: calc(90vh - 140px);
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
  }
  
  .message-detail-item {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .message-detail-item.full-width {
    grid-column: span 2;
  }
  
  .detail-label {
    font-size: 14px;
    font-weight: 500;
    color: #6b7280;
  }
  
  .detail-value {
    font-size: 16px;
    color: #111827;
  }
  
  .content-box {
    background-color: #f9fafb;
    padding: 16px;
    border-radius: 8px;
    border: 1px solid #e5e7eb;
    white-space: pre-wrap;
    min-height: 100px;
  }
  
  .attachments-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .attachment-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    background-color: #f9fafb;
    border-radius: 6px;
    border: 1px solid #e5e7eb;
  }
  
  .attachment-item svg {
    width: 20px;
    height: 20px;
    color: #4f46e5;
  }
  
  .modal-footer {
    padding: 16px 24px;
    border-top: 1px solid #e5e7eb;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .modal-actions {
    display: flex;
    gap: 12px;
  }
  
  .modal-action-btn {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 16px;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border: none;
    background-color: #f3f4f6;
    color: #374151;
  }
  
  .modal-action-btn svg {
    width: 18px;
    height: 18px;
  }
  
  .modal-action-btn.process {
    background-color: #ecfdf5;
    color: #047857;
  }
  
  .modal-action-btn.process:hover {
    background-color: #d1fae5;
  }
  
  .modal-action-btn.forward {
    background-color: #eff6ff;
    color: #1d4ed8;
  }
  
  .modal-action-btn.forward:hover {
    background-color: #dbeafe;
  }
  
  .modal-action-btn.archive {
    background-color: #f3f4f6;
    color: #4b5563;
  }
  
  .modal-action-btn.archive:hover {
    background-color: #e5e7eb;
  }
  
  .modal-close-action {
    padding: 8px 16px;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    border: 1px solid #e5e7eb;
    background-color: white;
    color: #4b5563;
  }
  
  .modal-close-action:hover {
    background-color: #f3f4f6;
    color: #111827;
  }

  .pagination {
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

            &:hover {
              background: #2563eb;
            }
          }
        }
      }
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