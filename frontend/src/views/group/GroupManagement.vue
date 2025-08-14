<template>
  <div class="group-management">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">群组管理</h1>
          <p class="page-subtitle">管理和配置微信群组的智能回复功能</p>
        </div>
        <div class="header-actions">
          <button class="action-btn primary" @click="refreshData">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
            </svg>
            刷新数据
          </button>
          <button class="action-btn primary" @click="openCreateGroupDialog">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
            </svg>
            新建群组
          </button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon auto">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.autoGroups }}</div>
            <div class="stat-label">自动模式群组</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon manual">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.manualGroups }}</div>
            <div class="stat-label">人工接管群组</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon paused">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.pausedGroups }}</div>
            <div class="stat-label">暂停服务群组</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon total">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M16 4c0-1.11.89-2 2-2s2 .89 2 2-.89 2-2 2-2-.89-2-2zM4 18v-4h3v-3h2l3 2 4-2v3h3v4H4z"/>
            </svg>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalGroups }}</div>
            <div class="stat-label">总群组数</div>
          </div>
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
            placeholder="搜索群组名称或群聊室ID..."
            class="search-input"
          >
        </div>
        <div class="filter-controls">
          <select v-model="selectedStatus" class="filter-select">
            <option value="">所有状态</option>
            <option value="AUTO">自动模式</option>
            <option value="MANUAL">人工接管</option>
            <option value="PAUSED">暂停服务</option>
          </select>
          <select v-model="selectedReplyEnabled" class="filter-select">
            <option value="">回复状态</option>
            <option value="true">已启用</option>
            <option value="false">已禁用</option>
          </select>
          <button class="filter-btn" @click="resetFilters">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19.36 2.72L20.78 4.14l-1.42 1.42c-.43-.51-.9-.99-1.41-1.41L19.36 2.72zM5.93 17.57c-.59-.59-1.07-1.23-1.42-1.91l1.42-1.42c.43.51.9.99 1.41 1.41L5.93 17.57zM9.5 3C5.91 3 3 5.91 3 9.5S5.91 16 9.5 16s6.5-2.91 6.5-6.5S13.09 3 9.5 3zM9.5 14C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
            </svg>
            重置
          </button>
        </div>
      </div>
    </div>

    <!-- 群组列表 -->
    <div class="table-section">
      <div class="table-container">
        <table class="data-table">
          <thead>
            <tr>
              <th>群聊室ID</th>
              <th>群组名称</th>
              <th>当前状态</th>
              <th>自动回复</th>
              <th>今日消息</th>
              <th>今日回复</th>
              <th>接管次数</th>
              <th>配置时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading" class="loading-row">
              <td colspan="9" class="loading-cell">
                <div class="loading-spinner"></div>
                <span>加载中...</span>
              </td>
            </tr>
            <tr v-else-if="filteredGroups.length === 0" class="empty-row">
              <td colspan="9" class="empty-cell">
                <div class="empty-state">
                  <svg viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                  </svg>
                  <p>暂无群组数据</p>
                </div>
              </td>
            </tr>
            <tr v-else v-for="group in paginatedGroups" :key="group.chatRoom" class="data-row">
              <td class="chat-room-cell">
                <code>{{ group.chatRoom }}</code>
              </td>
              <td class="group-name-cell">
                <div class="group-name">{{ group.groupName || '未知群组' }}</div>
              </td>
              <td class="status-cell">
                <span class="status-badge" :class="group.currentStatus.toLowerCase()">
                  {{ getStatusText(group.currentStatus) }}
                </span>
              </td>
              <td class="reply-enabled-cell">
                <span class="reply-badge" :class="{ enabled: group.autoReplyEnabled, disabled: !group.autoReplyEnabled }">
                  {{ group.autoReplyEnabled ? '已启用' : '已禁用' }}
                </span>
              </td>
              <td class="count-cell">{{ group.dailyMessageCount }}</td>
              <td class="count-cell">{{ group.dailyReplyCount }}</td>
              <td class="count-cell">{{ group.dailyTakeoverCount }}</td>
              <td class="time-cell">{{ formatTime(group.lastActivityTime) }}</td>
              <td class="actions-cell">
                <div class="action-buttons">
                  <button class="action-btn view" @click="viewGroupDetail(group)" title="查看详情">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                    </svg>
                  </button>
                  <button class="action-btn config" @click="configGroup(group)" title="配置">
                    <svg viewBox="0 0 24 24" fill="currentColor">
                      <path d="M19.14,12.94c0.04-0.3,0.06-0.61,0.06-0.94c0-0.32-0.02-0.64-0.07-0.94l2.03-1.58c0.18-0.14,0.23-0.41,0.12-0.61 l-1.92-3.32c-0.12-0.22-0.37-0.29-0.59-0.22l-2.39,0.96c-0.5-0.38-1.03-0.7-1.62-0.94L14.4,2.81c-0.04-0.24-0.24-0.41-0.48-0.41 h-3.84c-0.24,0-0.43,0.17-0.47,0.41L9.25,5.35C8.66,5.59,8.12,5.92,7.63,6.29L5.24,5.33c-0.22-0.08-0.47,0-0.59,0.22L2.74,8.87 C2.62,9.08,2.66,9.34,2.86,9.48l2.03,1.58C4.84,11.36,4.8,11.69,4.8,12s0.02,0.64,0.07,0.94l-2.03,1.58 c-0.18,0.14-0.23,0.41-0.12,0.61l1.92,3.32c0.12,0.22,0.37,0.29,0.59,0.22l2.39-0.96c0.5,0.38,1.03,0.7,1.62,0.94l0.36,2.54 c0.05,0.24,0.24,0.41,0.48,0.41h3.84c0.24,0,0.44-0.17,0.47-0.41l0.36-2.54c0.59-0.24,1.13-0.56,1.62-0.94l2.39,0.96 c0.22,0.08,0.47,0,0.59-0.22l1.92-3.32c0.12-0.22,0.07-0.47-0.12-0.61L19.14,12.94z M12,15.6c-1.98,0-3.6-1.62-3.6-3.6 s1.62-3.6,3.6-3.6s3.6,1.62,3.6,3.6S13.98,15.6,12,15.6z"/>
                    </svg>
                  </button>
                  <button 
                    class="action-btn switch" 
                    :class="{ manual: group.currentStatus === 'AUTO', auto: group.currentStatus === 'MANUAL' }"
                    @click="switchGroupStatus(group)" 
                    :title="group.currentStatus === 'AUTO' ? '切换到人工接管' : '切换到自动模式'"
                  >
                    <svg v-if="group.currentStatus === 'AUTO'" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                    </svg>
                    <svg v-else viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"/>
                    </svg>
                  </button>
                  <button class="action-btn delete" @click="confirmDeleteGroup(group)" title="删除群组">
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
          显示 {{ (currentPage - 1) * pageSize + 1 }} - {{ Math.min(currentPage * pageSize, filteredGroups.length) }} 条，共 {{ filteredGroups.length }} 条
        </div>
        <div class="pagination-controls">
          <button 
            class="pagination-btn" 
            :disabled="currentPage === 1" 
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
            >
              {{ page }}
            </button>
          </span>
          <button 
            class="pagination-btn" 
            :disabled="currentPage === totalPages" 
            @click="currentPage++"
          >
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- 群组详情模态框 -->
    <div v-if="showGroupDetail" class="group-detail-modal">
      <div class="modal-overlay" @click="showGroupDetail = false"></div>
      <div class="modal-container">
        <div class="modal-header">
          <h3>群组详情</h3>
          <button class="close-btn" @click="showGroupDetail = false">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>
        <div class="modal-content">
          <div v-if="selectedGroup" class="group-detail">
            <div class="detail-section">
              <h4>基本信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>群聊室ID:</label>
                  <span>{{ selectedGroup.chatRoom }}</span>
                </div>
                <div class="detail-item">
                  <label>群组名称:</label>
                  <span>{{ selectedGroup.groupName || '未知群组' }}</span>
                </div>
                <div class="detail-item">
                  <label>当前状态:</label>
                  <span class="status-badge" :class="selectedGroup.currentStatus.toLowerCase()">
                    {{ getStatusText(selectedGroup.currentStatus) }}
                  </span>
                </div>
                <div class="detail-item">
                  <label>自动回复:</label>
                  <span class="reply-badge" :class="{ enabled: selectedGroup.autoReplyEnabled, disabled: !selectedGroup.autoReplyEnabled }">
                    {{ selectedGroup.autoReplyEnabled ? '已启用' : '已禁用' }}
                  </span>
                </div>
              </div>
            </div>
            <div class="detail-section">
              <h4>统计信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>今日消息数:</label>
                  <span>{{ selectedGroup.dailyMessageCount }}</span>
                </div>
                <div class="detail-item">
                  <label>今日回复数:</label>
                  <span>{{ selectedGroup.dailyReplyCount }}</span>
                </div>
                <div class="detail-item">
                  <label>今日接管次数:</label>
                  <span>{{ selectedGroup.dailyTakeoverCount }}</span>
                </div>
                <div class="detail-item">
                  <label>最后活动时间:</label>
                  <span>{{ formatTime(selectedGroup.lastActivityTime) }}</span>
                </div>
              </div>
            </div>
            <div class="detail-section" v-if="selectedGroup.takeoverReason">
              <h4>接管信息</h4>
              <div class="detail-grid">
                <div class="detail-item full-width">
                  <label>接管原因:</label>
                  <span>{{ selectedGroup.takeoverReason }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn secondary" @click="showGroupDetail = false">关闭</button>
          <button class="btn primary" @click="configGroup(selectedGroup)">配置群组</button>
        </div>
      </div>
    </div>

    <!-- 群组配置模态框 -->
    <div v-if="showGroupConfig" class="group-config-modal">
      <div class="modal-overlay" @click="showGroupConfig = false"></div>
      <div class="modal-container large">
        <div class="modal-header">
          <h3>群组配置</h3>
          <button class="close-btn" @click="showGroupConfig = false">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>
        <div class="modal-content">
          <div class="config-form">
            <div class="form-section">
              <h4>基本设置</h4>
              <div class="form-grid">
                <div class="form-item">
                  <label>群组名称:</label>
                  <input v-model="configForm.groupName" type="text" placeholder="输入群组名称">
                </div>
                <div class="form-item">
                  <label>自动回复:</label>
                  <select v-model="configForm.autoReplyEnabled">
                    <option :value="true">启用</option>
                    <option :value="false">禁用</option>
                  </select>
                </div>
                <div class="form-item">
                  <label>负责网格员:</label>
                  <select v-model="configForm.gridOfficerId">
                    <option value="">请选择网格员</option>
                    <option v-for="officer in gridOfficers" :key="officer.id" :value="officer.id">
                      {{ officer.officerName }} ({{ officer.phone || '无电话' }})
                    </option>
                  </select>
                </div>
              </div>
            </div>
            <div class="form-section">
              <h4>状态控制</h4>
              <div class="form-grid">
                <div class="form-item">
                  <label>运行状态:</label>
                  <select v-model="configForm.currentStatus">
                    <option value="AUTO">自动模式</option>
                    <option value="MANUAL">人工接管</option>
                    <option value="PAUSED">暂停服务</option>
                  </select>
                </div>
                <div class="form-item" v-if="configForm.currentStatus === 'MANUAL'">
                  <label>接管原因:</label>
                  <input v-model="configForm.takeoverReason" type="text" placeholder="输入接管原因">
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn secondary" @click="showGroupConfig = false">取消</button>
          <button class="btn primary" @click="saveGroupConfig">保存配置</button>
        </div>
      </div>
    </div>

    <!-- 创建群组模态框 -->
    <div v-if="showCreateGroup" class="create-group-modal">
      <div class="modal-overlay" @click="showCreateGroup = false"></div>
      <div class="modal-container">
        <div class="modal-header">
          <h3>新建群组</h3>
          <button class="close-btn" @click="showCreateGroup = false">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>
        <div class="modal-content">
          <div class="create-group-form">
            <div class="form-section">
              <h4>基本信息</h4>
              <div class="form-grid">
                <div class="form-item">
                  <label>群组标识 *:</label>
                  <input v-model="createForm.chatRoom" type="text" placeholder="输入群组标识（如：xxx@chatroom）" required>
                </div>
                <div class="form-item">
                  <label>群组名称 *:</label>
                  <input v-model="createForm.groupName" type="text" placeholder="输入群组名称" required>
                </div>
                <div class="form-item full-width">
                  <label>群组描述:</label>
                  <textarea v-model="createForm.groupDescription" placeholder="输入群组描述" rows="3"></textarea>
                </div>
                <div class="form-item">
                  <label>群组分类:</label>
                  <input v-model="createForm.groupCategory" type="text" placeholder="输入群组分类">
                </div>
                <div class="form-item">
                  <label>负责网格员:</label>
                  <select v-model="createForm.gridOfficerId">
                    <option value="">请选择网格员</option>
                    <option v-for="officer in gridOfficers" :key="officer.id" :value="officer.id">
                      {{ officer.officerName }} ({{ officer.phone || '无电话' }})
                    </option>
                  </select>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn secondary" @click="showCreateGroup = false">取消</button>
          <button class="btn primary" @click="createGroup" :disabled="!createForm.chatRoom || !createForm.groupName">
            创建群组
          </button>
        </div>
      </div>
    </div>

    <!-- 批量配置模态框 -->
    <div v-if="showBatchConfig" class="batch-config-modal">
      <div class="modal-overlay" @click="showBatchConfig = false"></div>
      <div class="modal-container">
        <div class="modal-header">
          <h3>批量配置</h3>
          <button class="close-btn" @click="showBatchConfig = false">
            <svg viewBox="0 0 24 24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>
        <div class="modal-content">
          <div class="batch-config-form">
            <div class="form-section">
              <h4>批量操作</h4>
              <div class="form-grid">
                <div class="form-item">
                  <label>操作类型:</label>
                  <select v-model="batchConfig.operation">
                    <option value="status">修改状态</option>
                    <option value="reply">修改自动回复</option>
                    <option value="reset">重置计数器</option>
                  </select>
                </div>
                <div class="form-item" v-if="batchConfig.operation === 'status'">
                  <label>目标状态:</label>
                  <select v-model="batchConfig.targetStatus">
                    <option value="AUTO">自动模式</option>
                    <option value="MANUAL">人工接管</option>
                    <option value="PAUSED">暂停服务</option>
                  </select>
                </div>
                <div class="form-item" v-if="batchConfig.operation === 'reply'">
                  <label>自动回复:</label>
                  <select v-model="batchConfig.autoReplyEnabled">
                    <option :value="true">启用</option>
                    <option :value="false">禁用</option>
                  </select>
                </div>
              </div>
            </div>
            <div class="form-section">
              <h4>选择群组</h4>
              <div class="group-selection">
                <div class="selection-header">
                  <label class="checkbox-label">
                    <input type="checkbox" v-model="selectAll" @change="toggleSelectAll">
                    <span class="checkmark"></span>
                    全选 ({{ selectedGroups.length }}/{{ groups.length }})
                  </label>
                </div>
                <div class="group-list">
                  <div v-for="group in groups" :key="group.chatRoom" class="group-item">
                    <label class="checkbox-label">
                      <input type="checkbox" :value="group.chatRoom" v-model="selectedGroups">
                      <span class="checkmark"></span>
                      <span class="group-info">
                        <span class="group-name">{{ group.groupName || '未知群组' }}</span>
                        <span class="group-id">{{ group.chatRoom }}</span>
                      </span>
                    </label>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn secondary" @click="showBatchConfig = false">取消</button>
          <button class="btn primary" @click="executeBatchConfig" :disabled="selectedGroups.length === 0">
            执行批量操作 ({{ selectedGroups.length }})
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { groupManagementApi } from '@/api/groupManagement'

// 响应式数据
const loading = ref(false)
const groups = ref([])
const searchQuery = ref('')
const selectedStatus = ref('')
const selectedReplyEnabled = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 统计数据
const stats = reactive({
  autoGroups: 0,
  manualGroups: 0,
  pausedGroups: 0,
  totalGroups: 0
})

// 模态框状态
const showGroupDetail = ref(false)
const showGroupConfig = ref(false)
const showBatchConfig = ref(false)
const showCreateGroup = ref(false)
const selectedGroup = ref(null)

// 配置表单
const configForm = reactive({
  chatRoom: '',
  groupName: '',
  currentStatus: 'AUTO',
  autoReplyEnabled: true,
  takeoverReason: '',
  gridOfficerId: ''
})

// 创建群组表单
const createForm = reactive({
  chatRoom: '',
  groupName: '',
  groupDescription: '',
  groupCategory: '',
  gridOfficerId: '',
  operator: 'admin'
})

// 网格员列表
const gridOfficers = ref([])

// 批量配置
const batchConfig = reactive({
  operation: 'status',
  targetStatus: 'AUTO',
  autoReplyEnabled: true
})
const selectedGroups = ref([])
const selectAll = ref(false)

// 计算属性
const filteredGroups = computed(() => {
  let filtered = groups.value
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    filtered = filtered.filter(group => 
      (group.groupName && group.groupName.toLowerCase().includes(query)) ||
      group.chatRoom.toLowerCase().includes(query)
    )
  }
  
  if (selectedStatus.value) {
    filtered = filtered.filter(group => group.currentStatus === selectedStatus.value)
  }
  
  if (selectedReplyEnabled.value !== '') {
    const enabled = selectedReplyEnabled.value === 'true'
    filtered = filtered.filter(group => group.autoReplyEnabled === enabled)
  }
  
  return filtered
})

const totalPages = computed(() => Math.ceil(filteredGroups.value.length / pageSize.value))

const paginatedGroups = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredGroups.value.slice(start, end)
})

const visiblePages = computed(() => {
  const total = totalPages.value
  const current = currentPage.value
  const delta = 2
  const range = []
  
  for (let i = Math.max(2, current - delta); i <= Math.min(total - 1, current + delta); i++) {
    range.push(i)
  }
  
  if (current - delta > 2) {
    range.unshift('...')
  }
  if (current + delta < total - 1) {
    range.push('...')
  }
  
  range.unshift(1)
  if (total > 1) {
    range.push(total)
  }
  
  return range
})

// 方法
const loadGroups = async () => {
  loading.value = true
  try {
    const response = await groupManagementApi.getGroupList()
    groups.value = response.data || []
    updateStats()
  } catch (error) {
    console.error('加载群组列表失败:', error)
    ElMessage.error('加载群组列表失败')
  } finally {
    loading.value = false
  }
}

const updateStats = () => {
  stats.totalGroups = groups.value.length
  stats.autoGroups = groups.value.filter(g => g.currentStatus === 'AUTO').length
  stats.manualGroups = groups.value.filter(g => g.currentStatus === 'MANUAL').length
  stats.pausedGroups = groups.value.filter(g => g.currentStatus === 'PAUSED').length
}

const refreshData = () => {
  loadGroups()
}

const resetFilters = () => {
  searchQuery.value = ''
  selectedStatus.value = ''
  selectedReplyEnabled.value = ''
  currentPage.value = 1
}

const viewGroupDetail = (group) => {
  selectedGroup.value = group
  showGroupDetail.value = true
}

// 添加缺失的方法
const handleSearch = () => {
  currentPage.value = 1
}

const handleStatusChange = () => {
  currentPage.value = 1
}

const handleReplyEnabledChange = () => {
  currentPage.value = 1
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const handlePageSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const handleSelectAll = () => {
  toggleSelectAll()
}

const handleSelectGroup = (chatRoom) => {
  const index = selectedGroups.value.indexOf(chatRoom)
  if (index > -1) {
    selectedGroups.value.splice(index, 1)
  } else {
    selectedGroups.value.push(chatRoom)
  }
}

// 删除群组
const confirmDeleteGroup = (group) => {
  ElMessageBox.confirm(
    `确定要删除群组 "${group.groupName || group.chatRoom}" 吗？此操作不可恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      try {
        const response = await groupManagementApi.deleteGroup(group.chatRoom)
        if (response.success) {
          ElMessage.success(response.message || '群组删除成功')
          // 刷新群组列表
          loadGroups()
        } else {
          ElMessage.error(response.message || '删除失败')
        }
      } catch (error) {
        console.error('删除群组失败:', error)
        ElMessage.error('删除群组失败: ' + (error.message || '未知错误'))
      }
    })
    .catch(() => {
      // 用户取消删除
    })
}

const viewGroupDetails = (group) => {
  viewGroupDetail(group)
}

const switchGroupMode = (group) => {
  switchGroupStatus(group)
}

const closeBatchConfigDialog = () => {
  showBatchConfig.value = false
}

const applyBatchConfig = () => {
  executeBatchConfig()
}

const configGroup = (group) => {
  configForm.chatRoom = group.chatRoom
  configForm.groupName = group.groupName || ''
  configForm.currentStatus = group.currentStatus
  configForm.autoReplyEnabled = group.autoReplyEnabled
  configForm.takeoverReason = group.takeoverReason || ''
  configForm.gridOfficerId = group.gridOfficerId || ''
  showGroupDetail.value = false
  showGroupConfig.value = true
  // 加载网格员列表
  loadGridOfficers()
}

const saveGroupConfig = async () => {
  try {
    await groupManagementApi.updateGroupConfig(configForm.chatRoom, configForm)
    ElMessage.success('群组配置保存成功')
    showGroupConfig.value = false
    loadGroups()
  } catch (error) {
    console.error('保存群组配置失败:', error)
    ElMessage.error('保存群组配置失败')
  }
}

const switchGroupStatus = async (group) => {
  const newStatus = group.currentStatus === 'AUTO' ? 'MANUAL' : 'AUTO'
  const action = newStatus === 'MANUAL' ? '切换到人工接管' : '切换到自动模式'
  
  try {
    await ElMessageBox.confirm(
      `确定要将群组 "${group.groupName || group.chatRoom}" ${action}吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await groupManagementApi.switchGroupStatus(group.chatRoom, newStatus)
    ElMessage.success(`${action}成功`)
    loadGroups()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换群组状态失败:', error)
      ElMessage.error('切换群组状态失败')
    }
  }
}

// 创建群组相关方法
const openCreateGroupDialog = () => {
  // 重置表单
  createForm.chatRoom = ''
  createForm.groupName = ''
  createForm.groupDescription = ''
  createForm.groupCategory = ''
  createForm.gridOfficerId = ''
  showCreateGroup.value = true
  // 加载网格员列表
  loadGridOfficers()
}

const loadGridOfficers = async () => {
  try {
    const response = await groupManagementApi.getGridOfficers()
    if (response.success) {
      gridOfficers.value = response.data
    }
  } catch (error) {
    console.error('加载网格员列表失败:', error)
  }
}

const createGroup = async () => {
  try {
    const response = await groupManagementApi.createGroup(createForm)
    if (response.success) {
      ElMessage.success('群组创建成功')
      showCreateGroup.value = false
      loadGroups() // 刷新群组列表
    } else {
      ElMessage.error(response.message || '创建群组失败')
    }
  } catch (error) {
    console.error('创建群组失败:', error)
    ElMessage.error('创建群组失败')
  }
}

const openBatchConfigDialog = () => {
  selectedGroups.value = []
  selectAll.value = false
  showBatchConfig.value = true
}

const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedGroups.value = groups.value.map(g => g.chatRoom)
  } else {
    selectedGroups.value = []
  }
}

const executeBatchConfig = async () => {
  if (selectedGroups.value.length === 0) {
    ElMessage.warning('请选择要操作的群组')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要对选中的 ${selectedGroups.value.length} 个群组执行批量操作吗？`,
      '确认批量操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await groupManagementApi.batchUpdateGroups({
      chatRooms: selectedGroups.value,
      operation: batchConfig.operation,
      config: {
        currentStatus: batchConfig.targetStatus,
        autoReplyEnabled: batchConfig.autoReplyEnabled
      }
    })
    
    ElMessage.success('批量操作执行成功')
    showBatchConfig.value = false
    loadGroups()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量操作失败:', error)
      ElMessage.error('批量操作失败')
    }
  }
}

const getStatusText = (status) => {
  const statusMap = {
    'AUTO': '自动模式',
    'MANUAL': '人工接管',
    'PAUSED': '暂停服务'
  }
  return statusMap[status] || status
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}

// 监听器
watch([searchQuery, selectedStatus, selectedReplyEnabled], () => {
  currentPage.value = 1
})

watch(selectedGroups, () => {
  selectAll.value = selectedGroups.value.length === groups.value.length && groups.value.length > 0
})

// 生命周期
onMounted(() => {
  loadGroups()
})
</script>

<style scoped>
/* 页面布局 */
.group-management {
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

/* 统计卡片 */
.stats-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon svg {
  width: 24px;
  height: 24px;
  color: white;
}

.stat-icon.auto {
  background: linear-gradient(135deg, #10b981, #059669);
}

.stat-icon.manual {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.stat-icon.paused {
  background: linear-gradient(135deg, #6b7280, #4b5563);
}

.stat-icon.total {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

/* 搜索区域 */
.search-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
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
}

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
}

.search-input:focus {
  outline: none;
  border-color: #3b82f6;
}

.filter-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-select {
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  background: white;
  cursor: pointer;
  transition: border-color 0.2s;
}

.filter-select:focus {
  outline: none;
  border-color: #3b82f6;
}

.filter-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  color: #6b7280;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  border-color: #d1d5db;
  background: #f9fafb;
}

.filter-btn svg {
  width: 16px;
  height: 16px;
}

/* 表格区域 */
.table-section {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  background: #f8fafc;
  padding: 16px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
  white-space: nowrap;
}

.data-table td {
  padding: 16px;
  border-bottom: 1px solid #f1f5f9;
  vertical-align: middle;
}

.data-row:hover {
  background: #f8fafc;
}

.loading-row,
.empty-row {
  background: #f8fafc;
}

.loading-cell,
.empty-cell {
  text-align: center;
  padding: 48px 16px;
  color: #6b7280;
}

.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid #e5e7eb;
  border-radius: 50%;
  border-top-color: #3b82f6;
  animation: spin 1s ease-in-out infinite;
  margin-right: 8px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-state svg {
  width: 48px;
  height: 48px;
  color: #d1d5db;
}

.chat-room-cell code {
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 12px;
  color: #374151;
}

.group-name {
  font-weight: 500;
  color: #1e293b;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.status-badge.auto {
  background: #d1fae5;
  color: #059669;
}

.status-badge.manual {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.paused {
  background: #f3f4f6;
  color: #6b7280;
}

.reply-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.reply-badge.enabled {
  background: #d1fae5;
  color: #059669;
}

.reply-badge.disabled {
  background: #fee2e2;
  color: #dc2626;
}

.count-cell {
  font-weight: 600;
  color: #374151;
}

.time-cell {
  color: #6b7280;
  font-size: 14px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-buttons .action-btn {
  width: 32px;
  height: 32px;
  padding: 0;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-buttons .action-btn svg {
  width: 16px;
  height: 16px;
}

.action-buttons .action-btn.view {
  background: #dbeafe;
  color: #1d4ed8;
}

.action-buttons .action-btn.view:hover {
  background: #bfdbfe;
}

.action-buttons .action-btn.config {
  background: #f3e8ff;
  color: #7c3aed;
}

.action-buttons .action-btn.config:hover {
  background: #e9d5ff;
}

.action-buttons .action-btn.switch.manual {
  background: #fef3c7;
  color: #d97706;
}

.action-buttons .action-btn.switch.manual:hover {
  background: #fde68a;
}

.action-buttons .action-btn.switch.auto {
  background: #d1fae5;
  color: #059669;
}

.action-buttons .action-btn.switch.auto:hover {
  background: #a7f3d0;
}

.action-buttons .action-btn.delete {
  background: #fee2e2;
  color: #dc2626;
}

.action-buttons .action-btn.delete:hover {
  background: #fecaca;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-top: 1px solid #f1f5f9;
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
}

.pagination-btn svg {
  width: 16px;
  height: 16px;
}

.pagination-btn:hover:not(:disabled) {
  background: #f9fafb;
  border-color: #d1d5db;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-numbers {
  display: flex;
  gap: 4px;
}

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
}

.page-btn:hover {
  background: #f9fafb;
  border-color: #d1d5db;
}

.page-btn.active {
  background: #3b82f6;
  border-color: #3b82f6;
  color: white;
}

/* 模态框 */
.group-detail-modal,
.group-config-modal,
.batch-config-modal,
.create-group-modal {
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

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
}

.modal-container {
  position: relative;
  background: white;
  border-radius: 12px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-container.large {
  max-width: 800px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 6px;
  background: #f1f5f9;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #e2e8f0;
  color: #374151;
}

.close-btn svg {
  width: 16px;
  height: 16px;
}

.modal-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px;
  border-top: 1px solid #e5e7eb;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
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

.btn.primary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
}

.btn.secondary {
  background: #f1f5f9;
  color: #475569;
}

.btn.secondary:hover {
  background: #e2e8f0;
}

/* 详情页面 */
.group-detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-section {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
}

.detail-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 16px 0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-item label {
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
}

.detail-item span {
  font-size: 14px;
  color: #374151;
}

/* 配置表单 */
.config-form,
.batch-config-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-section {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
}

.form-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 16px 0;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-item label {
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.form-item input,
.form-item select {
  padding: 12px 16px;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-item input:focus,
.form-item select:focus {
  outline: none;
  border-color: #3b82f6;
}

/* 批量配置 */
.group-selection {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  overflow: hidden;
}

.selection-header {
  background: #f8fafc;
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.group-list {
  max-height: 300px;
  overflow-y: auto;
}

.group-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f1f5f9;
}

.group-item:last-child {
  border-bottom: none;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  user-select: none;
}

.checkbox-label input[type="checkbox"] {
  display: none;
}

.checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid #e5e7eb;
  border-radius: 4px;
  position: relative;
  transition: all 0.2s;
}

.checkbox-label input[type="checkbox"]:checked + .checkmark {
  background: #3b82f6;
  border-color: #3b82f6;
}

.checkbox-label input[type="checkbox"]:checked + .checkmark::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 2px;
  width: 4px;
  height: 8px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.group-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.group-name {
  font-weight: 500;
  color: #1e293b;
}

.group-id {
  font-size: 12px;
  color: #6b7280;
  font-family: 'Monaco', 'Menlo', monospace;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .group-management {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .search-input-wrapper {
    max-width: none;
  }
  
  .filter-controls {
    flex-wrap: wrap;
  }
  
  .table-container {
    overflow-x: scroll;
  }
  
  .modal-container {
    width: 95%;
    margin: 20px;
  }
  
  .detail-grid,
  .form-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .pagination {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .pagination-controls {
    justify-content: center;
  }
}
</style>