<template>
  <div class="keyword-enhancement">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-info">
          <h1 class="page-title">智能学习</h1>
          <p class="page-subtitle">智能学习、同步管理、分析优化一体化</p>
        </div>
        <div class="header-actions">
          <!-- 可以在这里添加操作按钮 -->
        </div>
      </div>
    </div>

    <!-- 功能概览卡片 -->
    <div class="overview-cards">
      <div class="card" @click="handleTabClick('learning')">
        <div class="card-icon learning">
          <i class="fas fa-brain"></i>
        </div>
        <div class="card-content">
          <h3>智能学习</h3>
          <p>{{ overviewData.learningStats?.pendingRecommendations || 0 }} 个待审核推荐</p>
        </div>
      </div>

      <div class="card" @click="handleTabClick('sync')">
        <div class="card-icon sync">
          <i class="fas fa-sync-alt"></i>
        </div>
        <div class="card-content">
          <h3>同步管理</h3>
          <p>成功率: {{ overviewData.syncStats?.successRate || 0 }}%</p>
        </div>
      </div>

      <div class="card" @click="handleTabClick('analysis')">
        <div class="card-icon analysis">
          <i class="fas fa-chart-line"></i>
        </div>
        <div class="card-content">
          <h3>数据分析</h3>
          <p>{{ overviewData.basicStats?.totalKeywords || 0 }} 个关键词</p>
        </div>
      </div>

      <div class="card" @click="handleTabClick('management')">
        <div class="card-icon management">
          <i class="fas fa-cogs"></i>
        </div>
        <div class="card-content">
          <h3>智能管理</h3>
          <p>{{ overviewData.managementStats?.redundantKeywords || 0 }} 个冗余关键词</p>
        </div>
      </div>
    </div>

    <!-- 标签页导航 -->
    <div class="tab-navigation">
      <button 
        v-for="tab in tabs" 
        :key="tab.key"
        :class="['tab-button', { active: activeTab === tab.key }]"
        @click="handleTabClick(tab.key)"
      >
        <i :class="tab.icon"></i>
        {{ tab.label }}
      </button>
    </div>
    


    <!-- 标签页内容 -->
    <div class="tab-content">
      <!-- 智能学习模块 -->
      <div v-if="activeTab === 'learning'" class="learning-module">
        <div class="module-header">
          <h2>智能学习模块</h2>
          <button class="btn-primary" @click="refreshLearningData">
            <i class="fas fa-refresh"></i> 刷新数据
          </button>
        </div>

        <div class="learning-stats">
          <div class="stat-item">
            <span class="label">学习记录:</span>
            <span class="value">{{ learningStats.totalRecords || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="label">待审核推荐:</span>
            <span class="value">{{ learningStats.pendingRecommendations || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="label">自动创建:</span>
            <span class="value">{{ learningStats.autoCreated || 0 }}</span>
          </div>
        </div>

        <div class="recommendations-section">
          <h3>待审核推荐</h3>
          <div v-if="!recommendations || recommendations.length === 0" class="empty-state">
            <i class="fas fa-inbox"></i>
            <p>暂无待审核推荐</p>
          </div>
          <div v-else class="recommendations-list">
            <div 
              v-for="rec in (recommendations || [])" 
              :key="rec.id"
              class="recommendation-item"
            >
              <div class="rec-info">
                <h4>{{ rec.keyword }}</h4>
                <p>置信度: {{ rec.confidence }}% | 检测次数: {{ rec.detectionCount }}</p>
                <p class="rec-reason">{{ rec.reason }}</p>
              </div>
              <div class="rec-actions">
                <button 
                  class="btn-success"
                  @click="reviewRecommendation(rec.id, true)"
                >
                  批准
                </button>
                <button 
                  class="btn-danger"
                  @click="reviewRecommendation(rec.id, false)"
                >
                  拒绝
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 同步管理模块 -->
      <div v-if="activeTab === 'sync'" class="sync-module">
        <div class="module-header">
          <h2>同步管理模块</h2>
        </div>

        <div class="sync-stats">
          <div class="stat-item">
            <span class="label">总同步次数:</span>
            <span class="value">{{ syncStats.totalSyncs || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="label">成功次数:</span>
            <span class="value">{{ syncStats.successSyncs || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="label">成功率:</span>
            <span class="value">{{ syncStats.successRate || 0 }}%</span>
          </div>
        </div>

        <div class="sync-history">
          <h3>同步历史</h3>
          <div class="history-list">
            <div 
              v-for="history in (syncHistory || [])" 
              :key="history.id"
              class="history-item"
            >
              <div class="history-info">
                <span class="sync-type">{{ history.syncType }}</span>
                <span class="sync-time">{{ formatTime(history.startTime) }}</span>
              </div>
              <div class="history-status">
                <span :class="['status', history.status.toLowerCase()]">{{ history.status }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 数据分析模块 -->
      <div v-if="activeTab === 'analysis'" class="analysis-module">
        <div class="module-header">
          <h2>数据分析模块</h2>
          <button class="btn-primary" @click="refreshAnalysisData">
            <i class="fas fa-chart-bar"></i> 刷新分析
          </button>
        </div>

        <div class="analysis-charts">
          <div class="chart-container">
            <h3>关键词使用趋势</h3>
            <div class="chart-placeholder">
              <i class="fas fa-chart-line"></i>
              <p>趋势图表</p>
            </div>
          </div>
          
          <div class="chart-container">
            <h3>热门关键词</h3>
            <div class="hot-keywords">
              <div 
                v-for="keyword in (hotKeywords || [])" 
                :key="keyword.id"
                class="keyword-tag"
              >
                {{ keyword.keyword }} ({{ keyword.count }})
              </div>
            </div>
          </div>
        </div>

        <div class="optimization-suggestions">
          <h3>优化建议</h3>
          <div class="suggestions-list">
            <div 
              v-for="suggestion in (optimizationSuggestions || [])" 
              :key="suggestion.id"
              class="suggestion-item"
            >
              <div class="suggestion-icon">
                <i :class="suggestion.icon"></i>
              </div>
              <div class="suggestion-content">
                <h4>{{ suggestion.title }}</h4>
                <p>{{ suggestion.description }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 智能管理模块 -->
      <div v-if="activeTab === 'management'" class="management-module">
        <div class="module-header">
          <h2>智能管理模块</h2>
          <button class="btn-primary" @click="autoClassifyKeywords">
            <i class="fas fa-magic"></i> 自动分类
          </button>
        </div>

        <div class="redundancy-detection">
          <h3>冗余检测</h3>
          <div class="redundancy-list">
            <div 
              v-for="redundancy in (redundantKeywords || [])" 
              :key="redundancy.id"
              class="redundancy-item"
            >
              <div class="redundancy-info">
                <h4>{{ redundancy.keyword1 }} ↔ {{ redundancy.keyword2 }}</h4>
                <p>相似度: {{ redundancy.similarity }}%</p>
              </div>
              <div class="redundancy-actions">
                <button class="btn-warning" @click="mergeKeywords(redundancy)">
                  合并
                </button>
                <button class="btn-danger" @click="deleteRedundant(redundancy)">
                  删除
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, watch } from 'vue'
import keywordEnhancementAPI from '@/api/keywordEnhancement'

export default {
  name: 'KeywordEnhancement',
  setup() {
    const activeTab = ref('learning')
    const loading = ref(false)
    
    const overviewData = reactive({
      learningStats: {},
      syncStats: {},
      basicStats: {},
      managementStats: {}
    })
    
    const learningStats = reactive({})
    const syncStats = reactive({})
    const recommendations = ref([])
    const syncHistory = ref([])
    const hotKeywords = ref([])
    const optimizationSuggestions = ref([])
    const redundantKeywords = ref([])
    
    const tabs = [
      { key: 'learning', label: '智能学习', icon: 'fas fa-brain' },
      { key: 'sync', label: '同步管理', icon: 'fas fa-sync-alt' },
      { key: 'analysis', label: '数据分析', icon: 'fas fa-chart-line' },
      { key: 'management', label: '智能管理', icon: 'fas fa-cogs' }
    ]
    
    // API 调用方法
    const loadOverviewData = async () => {
      try {
        const response = await keywordEnhancementAPI.overview.getOverview({})
        if (response && response.data) {
          Object.assign(overviewData, response.data)
        }
      } catch (error) {
        console.error('加载概览数据失败:', error)
      }
    }
    
    const loadLearningData = async () => {
      try {
        const [statsRes, recsRes] = await Promise.all([
          keywordEnhancementAPI.learning.getLearningStats({}),
          keywordEnhancementAPI.learning.getPendingRecommendations({})
        ])
        if (statsRes && statsRes.data) {
          Object.assign(learningStats, statsRes.data)
        }
        recommendations.value = (recsRes && recsRes.data) ? recsRes.data : []
      } catch (error) {
        console.error('加载学习数据失败:', error)
        recommendations.value = []
      }
    }
    
    const loadSyncData = async () => {
      try {
        const [statsRes, historyRes] = await Promise.all([
          keywordEnhancementAPI.sync.getSyncStats({}),
          keywordEnhancementAPI.sync.getSyncHistory({})
        ])
        if (statsRes && statsRes.data) {
          Object.assign(syncStats, statsRes.data)
        }
        syncHistory.value = (historyRes && historyRes.data) ? historyRes.data : []
      } catch (error) {
        console.error('加载同步数据失败:', error)
        syncHistory.value = []
      }
    }
    
    const loadAnalysisData = async () => {
      try {
        const [hotRes, suggestionsRes] = await Promise.all([
          keywordEnhancementAPI.learning.getHotKeywords({}),
          keywordEnhancementAPI.analysis.getOptimizationSuggestions({})
        ])
        hotKeywords.value = (hotRes && hotRes.data) ? hotRes.data : []
        optimizationSuggestions.value = (suggestionsRes && suggestionsRes.data) ? suggestionsRes.data : []
      } catch (error) {
        console.error('加载分析数据失败:', error)
        hotKeywords.value = []
        optimizationSuggestions.value = []
      }
    }
    
    const loadManagementData = async () => {
      try {
        const response = await keywordEnhancementAPI.management.detectRedundancy({})
        redundantKeywords.value = (response && response.data) ? response.data : []
      } catch (error) {
        console.error('加载管理数据失败:', error)
        redundantKeywords.value = []
      }
    }
    
    // 操作方法
    const reviewRecommendation = async (id, approved) => {
      try {
        await keywordEnhancementAPI.learning.reviewRecommendation(id, {
          approved,
          reviewedBy: 1 // 当前用户ID
        })
        await loadLearningData()
        await loadOverviewData()
      } catch (error) {
        console.error('审核推荐失败:', error)
      }
    }
    
    const startSync = async () => {
      try {
        await keywordEnhancementAPI.sync.startSync({
          clientId: 'web-client',
          syncType: 'INCREMENTAL',
          syncDirection: 'BIDIRECTIONAL'
        })
        await loadSyncData()
      } catch (error) {
        console.error('开始同步失败:', error)
      }
    }
    
    const autoClassifyKeywords = async () => {
      try {
        await keywordEnhancementAPI.management.autoClassifyKeywords({})
        await loadManagementData()
      } catch (error) {
        console.error('自动分类失败:', error)
      }
    }
    
    const mergeKeywords = async (redundancy) => {
      try {
        await keywordEnhancementAPI.management.batchProcessRedundancy([{
          action: 'MERGE',
          keywordIds: [redundancy.keyword1Id, redundancy.keyword2Id],
          targetKeywordId: redundancy.keyword1Id
        }])
        await loadManagementData()
      } catch (error) {
        console.error('合并关键词失败:', error)
      }
    }
    
    const deleteRedundant = async (redundancy) => {
      try {
        await keywordEnhancementAPI.management.batchProcessRedundancy([{
          action: 'DELETE',
          keywordIds: [redundancy.keyword2Id]
        }])
        await loadManagementData()
      } catch (error) {
        console.error('删除冗余关键词失败:', error)
      }
    }
    
    // 标签页点击处理
    const handleTabClick = (tabKey) => {
      activeTab.value = tabKey
    }
    
    // 刷新方法
    const refreshLearningData = () => loadLearningData()
    const refreshAnalysisData = () => loadAnalysisData()
    
    // 工具方法
    const formatTime = (timeStr) => {
      return new Date(timeStr).toLocaleString('zh-CN')
    }
    
    // 监听标签页切换，加载对应数据
    watch(activeTab, (newTab) => {
      switch (newTab) {
        case 'learning':
          loadLearningData()
          break
        case 'sync':
          loadSyncData()
          break
        case 'analysis':
          loadAnalysisData()
          break
        case 'management':
          loadManagementData()
          break
      }
    }, { immediate: true })

    onMounted(() => {
      loadOverviewData()
      // 移除这里的loadLearningData()调用，因为watch会在immediate: true时自动调用
    })
    
    return {
      activeTab,
      loading,
      overviewData,
      learningStats,
      syncStats,
      recommendations,
      syncHistory,
      hotKeywords,
      optimizationSuggestions,
      redundantKeywords,
      tabs,
      handleTabClick,
      reviewRecommendation,
      startSync,
      autoClassifyKeywords,
      mergeKeywords,
      deleteRedundant,
      refreshLearningData,
      refreshAnalysisData,
      formatTime
    }
  }
}
</script>

<style scoped>
.keyword-enhancement {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
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
    }
  }
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  align-items: center;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.15);
}

.card-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 20px;
  color: white;
}

.card-icon.learning { background: #3498db; }
.card-icon.sync { background: #2ecc71; }
.card-icon.analysis { background: #e74c3c; }
.card-icon.management { background: #f39c12; }

.card-content h3 {
  margin: 0 0 5px 0;
  color: #2c3e50;
}

.card-content p {
  margin: 0;
  color: #7f8c8d;
  font-size: 14px;
}

.tab-navigation {
  display: flex;
  border-bottom: 2px solid #ecf0f1;
  margin-bottom: 20px;
}

.tab-button {
  padding: 12px 20px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 16px;
  color: #7f8c8d;
  transition: color 0.2s, border-bottom 0.2s;
  border-bottom: 2px solid transparent;
}

.tab-button:hover {
  color: #3498db;
}

.tab-button.active {
  color: #3498db;
  border-bottom-color: #3498db;
}

.tab-button i {
  margin-right: 8px;
}

.module-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.module-header h2 {
  color: #2c3e50;
  margin: 0;
}

.btn-primary, .btn-success, .btn-danger, .btn-warning {
  padding: 8px 16px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.2s;
}

.btn-primary {
  background: #3498db;
  color: white;
}

.btn-primary:hover {
  background: #2980b9;
}

.btn-success {
  background: #2ecc71;
  color: white;
}

.btn-success:hover {
  background: #27ae60;
}

.btn-danger {
  background: #e74c3c;
  color: white;
}

.btn-danger:hover {
  background: #c0392b;
}

.btn-warning {
  background: #f39c12;
  color: white;
}

.btn-warning:hover {
  background: #e67e22;
}

.learning-stats, .sync-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-item {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  flex: 1;
}

.stat-item .label {
  display: block;
  color: #7f8c8d;
  font-size: 14px;
  margin-bottom: 5px;
}

.stat-item .value {
  display: block;
  color: #2c3e50;
  font-size: 24px;
  font-weight: bold;
}

.recommendations-list, .history-list, .redundancy-list {
  space-y: 10px;
}

.recommendation-item, .history-item, .redundancy-item {
  background: white;
  border: 1px solid #ecf0f1;
  border-radius: 8px;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.rec-info h4, .redundancy-info h4 {
  margin: 0 0 5px 0;
  color: #2c3e50;
}

.rec-info p, .redundancy-info p {
  margin: 0;
  color: #7f8c8d;
  font-size: 14px;
}

.rec-reason {
  font-style: italic;
}

.rec-actions, .redundancy-actions {
  display: flex;
  gap: 10px;
}

.history-info {
  display: flex;
  flex-direction: column;
}

.sync-type {
  font-weight: bold;
  color: #2c3e50;
}

.sync-time {
  color: #7f8c8d;
  font-size: 14px;
}

.status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
}

.status.success {
  background: #d4edda;
  color: #155724;
}

.status.failed {
  background: #f8d7da;
  color: #721c24;
}

.status.running {
  background: #fff3cd;
  color: #856404;
}

.analysis-charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.chart-container {
  background: white;
  border: 1px solid #ecf0f1;
  border-radius: 8px;
  padding: 20px;
}

.chart-container h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
}

.chart-placeholder {
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #bdc3c7;
}

.chart-placeholder i {
  font-size: 48px;
  margin-bottom: 10px;
}

.hot-keywords {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.keyword-tag {
  background: #3498db;
  color: white;
  padding: 5px 10px;
  border-radius: 15px;
  font-size: 14px;
}

.suggestions-list {
  space-y: 15px;
}

.suggestion-item {
  background: white;
  border: 1px solid #ecf0f1;
  border-radius: 8px;
  padding: 15px;
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.suggestion-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #3498db;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.suggestion-content h4 {
  margin: 0 0 5px 0;
  color: #2c3e50;
}

.suggestion-content p {
  margin: 0;
  color: #7f8c8d;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #bdc3c7;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 10px;
}

/* 响应式设计 */
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

  .overview-cards {
    grid-template-columns: 1fr;
  }
}
</style>