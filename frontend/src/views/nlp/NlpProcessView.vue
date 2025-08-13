<template>
  <div class="nlp-process-container">
    <el-card class="nlp-process-card">
      <template #header>
        <div class="card-header">
          <h2>NLP文本处理</h2>
          <el-button type="primary" @click="processText" :loading="processing">处理文本</el-button>
        </div>
      </template>
      
      <el-form :model="form" label-width="120px">
        <el-form-item label="消息ID">
          <el-input v-model.number="form.messageId" placeholder="请输入消息ID" />
        </el-form-item>
        
        <el-form-item label="文本内容">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入需要处理的文本内容"
          />
        </el-form-item>
        
        <el-form-item label="处理类型">
          <el-checkbox-group v-model="selectedProcessTypes">
            <el-checkbox label="1" border>文本预处理</el-checkbox>
            <el-checkbox label="2" border>意图识别</el-checkbox>
            <el-checkbox label="4" border>实体提取</el-checkbox>
            <el-checkbox label="8" border>情感分析</el-checkbox>
            <el-checkbox label="16" border>文本分类</el-checkbox>
            <el-checkbox label="32" border>关键信息提取</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        
        <el-form-item label="处理模式">
          <el-radio-group v-model="form.async">
            <el-radio :label="false">同步处理</el-radio>
            <el-radio :label="true">异步处理</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card v-if="processResult" class="nlp-result-card">
      <template #header>
        <div class="card-header">
          <h2>处理结果</h2>
          <div>
            <el-tag v-if="processResult.status === 0" type="warning">处理中</el-tag>
            <el-tag v-else-if="processResult.status === 1" type="success">处理完成</el-tag>
            <el-tag v-else-if="processResult.status === 2" type="danger">处理失败</el-tag>
            <el-button v-if="processResult.status === 0" type="primary" size="small" @click="refreshResult" style="margin-left: 10px;">刷新结果</el-button>
          </div>
        </div>
      </template>
      
      <div v-if="processResult.status === 2" class="error-message">
        <el-alert
          :title="processResult.errorMessage || '处理失败'"
          type="error"
          :closable="false"
          show-icon
        />
      </div>
      
      <div v-else>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="处理ID">{{ processResult.processId }}</el-descriptions-item>
          <el-descriptions-item label="消息ID">{{ processResult.messageId }}</el-descriptions-item>
          <el-descriptions-item v-if="processResult.processDuration" label="处理耗时">
            {{ processResult.processDuration }} 毫秒
          </el-descriptions-item>
        </el-descriptions>
        
        <el-tabs v-model="activeTab" class="result-tabs">
          <!-- 预处理结果 -->
          <el-tab-pane v-if="processResult.preprocessResult" label="预处理结果" name="preprocess">
            <el-collapse>
              <el-collapse-item title="原始文本" name="originalText">
                <div>{{ processResult.preprocessResult.originalText }}</div>
              </el-collapse-item>
              <el-collapse-item title="简体转换" name="simplifiedText">
                <div>{{ processResult.preprocessResult.simplifiedText }}</div>
              </el-collapse-item>
              <el-collapse-item title="分词结果" name="words">
                <el-tag 
                  v-for="(word, index) in processResult.preprocessResult.words" 
                  :key="index"
                  class="word-tag"
                >
                  {{ word }}
                </el-tag>
              </el-collapse-item>
              <el-collapse-item title="词性标注" name="wordsWithNature">
                <el-table :data="processResult.preprocessResult.wordsWithNature" border style="width: 100%">
                  <el-table-column prop="word" label="词语" />
                  <el-table-column prop="nature" label="词性" />
                </el-table>
              </el-collapse-item>
              <el-collapse-item title="停用词过滤" name="filteredWords">
                <el-tag 
                  v-for="(word, index) in processResult.preprocessResult.filteredWords" 
                  :key="index"
                  class="word-tag"
                  type="success"
                >
                  {{ word }}
                </el-tag>
              </el-collapse-item>
              <el-collapse-item title="清洗后文本" name="cleanedText">
                <div>{{ processResult.preprocessResult.cleanedText }}</div>
              </el-collapse-item>
            </el-collapse>
          </el-tab-pane>
          
          <!-- 意图识别结果 -->
          <el-tab-pane v-if="processResult.intentResult" label="意图识别" name="intent">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="主要意图">
                <el-tag type="primary">{{ processResult.intentResult.primaryIntent }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="置信度">
                <el-progress 
                  :percentage="Math.round(processResult.intentResult.confidence * 100)" 
                  :format="percentageFormat"
                />
              </el-descriptions-item>
            </el-descriptions>
            
            <h4>意图得分详情：</h4>
            <el-table :data="intentScoresData" border style="width: 100%">
              <el-table-column prop="intent" label="意图" />
              <el-table-column prop="score" label="得分" width="180">
                <template #default="scope">
                  <el-progress 
                    :percentage="Math.round(scope.row.score * 100)" 
                    :format="percentageFormat"
                  />
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          
          <!-- 实体提取结果 -->
          <el-tab-pane v-if="processResult.entityResult" label="实体提取" name="entity">
            <el-empty v-if="!processResult.entityResult.entities || processResult.entityResult.entities.length === 0" description="未提取到实体" />
            <el-table v-else :data="processResult.entityResult.entities" border style="width: 100%">
              <el-table-column prop="type" label="实体类型">
                <template #default="scope">
                  <el-tag :type="getEntityTypeTag(scope.row.type)">{{ getEntityTypeLabel(scope.row.type) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="value" label="实体值" />
              <el-table-column prop="position" label="位置" width="80" />
            </el-table>
          </el-tab-pane>
          
          <!-- 情感分析结果 -->
          <el-tab-pane v-if="processResult.sentimentResult" label="情感分析" name="sentiment">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-card shadow="hover">
                  <template #header>
                    <div class="sentiment-header">
                      <span>情感倾向</span>
                      <el-tag 
                        :type="getSentimentTag(processResult.sentimentResult.sentiment)"
                        size="large"
                      >
                        {{ processResult.sentimentResult.sentiment }}
                      </el-tag>
                    </div>
                  </template>
                  <el-progress 
                    :percentage="Math.round(processResult.sentimentResult.score * 100)" 
                    :format="percentageFormat"
                    :color="getSentimentColor(processResult.sentimentResult.sentiment)"
                  />
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card shadow="hover">
                  <template #header>
                    <div class="sentiment-header">
                      <span>情感词统计</span>
                    </div>
                  </template>
                  <div class="sentiment-stats">
                    <div>
                      <span>正面词数量：</span>
                      <el-tag type="success">{{ processResult.sentimentResult.positiveCount }}</el-tag>
                    </div>
                    <div>
                      <span>负面词数量：</span>
                      <el-tag type="danger">{{ processResult.sentimentResult.negativeCount }}</el-tag>
                    </div>
                  </div>
                </el-card>
              </el-col>
            </el-row>
            
            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="12">
                <el-card shadow="hover">
                  <template #header>
                    <div class="sentiment-header">
                      <span>匹配到的正面词</span>
                    </div>
                  </template>
                  <div v-if="processResult.sentimentResult.matchedPositiveWords.length === 0" class="empty-words">
                    无匹配正面词
                  </div>
                  <el-tag 
                    v-for="(word, index) in processResult.sentimentResult.matchedPositiveWords" 
                    :key="index"
                    class="word-tag"
                    type="success"
                  >
                    {{ word }}
                  </el-tag>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card shadow="hover">
                  <template #header>
                    <div class="sentiment-header">
                      <span>匹配到的负面词</span>
                    </div>
                  </template>
                  <div v-if="processResult.sentimentResult.matchedNegativeWords.length === 0" class="empty-words">
                    无匹配负面词
                  </div>
                  <el-tag 
                    v-for="(word, index) in processResult.sentimentResult.matchedNegativeWords" 
                    :key="index"
                    class="word-tag"
                    type="danger"
                  >
                    {{ word }}
                  </el-tag>
                </el-card>
              </el-col>
            </el-row>
            
            <el-alert
              v-if="processResult.sentimentResult.riskWarning"
              :title="processResult.sentimentResult.riskMessage"
              :type="processResult.sentimentResult.riskLevel === '高' ? 'error' : 'warning'"
              show-icon
              style="margin-top: 20px;"
            />
          </el-tab-pane>
          
          <!-- 文本分类结果 -->
          <el-tab-pane v-if="processResult.classificationResult" label="文本分类" name="classification">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="主要类别">
                <el-tag type="primary">{{ processResult.classificationResult.primaryCategory }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="置信度">
                <el-progress 
                  :percentage="Math.round(processResult.classificationResult.confidence * 100)" 
                  :format="percentageFormat"
                />
              </el-descriptions-item>
              <el-descriptions-item label="多标签分类">
                <el-tag 
                  v-for="(label, index) in processResult.classificationResult.multiLabels" 
                  :key="index"
                  class="category-tag"
                  type="success"
                >
                  {{ label }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
            
            <h4>类别得分详情：</h4>
            <el-table :data="processResult.classificationResult.categories" border style="width: 100%">
              <el-table-column prop="category" label="类别" />
              <el-table-column prop="score" label="得分" width="180">
                <template #default="scope">
                  <el-progress 
                    :percentage="Math.round(scope.row.score * 100)" 
                    :format="percentageFormat"
                  />
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          
          <!-- 关键信息提取结果 -->
          <el-tab-pane v-if="processResult.keyInfoResult" label="关键信息提取" name="keyInfo">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-card shadow="hover">
                  <template #header>
                    <div class="key-info-header">
                      <span>关键词</span>
                    </div>
                  </template>
                  <div v-if="!processResult.keyInfoResult.keywords || processResult.keyInfoResult.keywords.length === 0" class="empty-words">
                    无关键词
                  </div>
                  <el-tag 
                    v-for="(word, index) in processResult.keyInfoResult.keywords" 
                    :key="index"
                    class="word-tag"
                    type="primary"
                  >
                    {{ word }}
                  </el-tag>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card shadow="hover">
                  <template #header>
                    <div class="key-info-header">
                      <span>摘要</span>
                    </div>
                  </template>
                  <div v-if="!processResult.keyInfoResult.summary || processResult.keyInfoResult.summary.length === 0" class="empty-words">
                    无摘要
                  </div>
                  <div v-for="(summary, index) in processResult.keyInfoResult.summary" :key="index" class="summary-item">
                    {{ summary }}
                  </div>
                </el-card>
              </el-col>
            </el-row>
            
            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="8">
                <el-card shadow="hover">
                  <template #header>
                    <div class="key-info-header">
                      <span>时间信息</span>
                    </div>
                  </template>
                  <div v-if="!processResult.keyInfoResult.timeInfo || processResult.keyInfoResult.timeInfo.length === 0" class="empty-words">
                    无时间信息
                  </div>
                  <el-tag 
                    v-for="(time, index) in processResult.keyInfoResult.timeInfo" 
                    :key="index"
                    class="info-tag"
                    type="warning"
                  >
                    {{ time }}
                  </el-tag>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover">
                  <template #header>
                    <div class="key-info-header">
                      <span>地点信息</span>
                    </div>
                  </template>
                  <div v-if="!processResult.keyInfoResult.locationInfo || processResult.keyInfoResult.locationInfo.length === 0" class="empty-words">
                    无地点信息
                  </div>
                  <el-tag 
                    v-for="(location, index) in processResult.keyInfoResult.locationInfo" 
                    :key="index"
                    class="info-tag"
                    type="success"
                  >
                    {{ location }}
                  </el-tag>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card shadow="hover">
                  <template #header>
                    <div class="key-info-header">
                      <span>人物信息</span>
                    </div>
                  </template>
                  <div v-if="!processResult.keyInfoResult.personInfo || processResult.keyInfoResult.personInfo.length === 0" class="empty-words">
                    无人物信息
                  </div>
                  <el-tag 
                    v-for="(person, index) in processResult.keyInfoResult.personInfo" 
                    :key="index"
                    class="info-tag"
                    type="info"
                  >
                    {{ person }}
                  </el-tag>
                </el-card>
              </el-col>
            </el-row>
            
            <el-row :gutter="20" style="margin-top: 20px;">
              <el-col :span="12">
                <el-card shadow="hover">
                  <template #header>
                    <div class="key-info-header">
                      <span>设备信息</span>
                    </div>
                  </template>
                  <div v-if="!processResult.keyInfoResult.deviceInfo || processResult.keyInfoResult.deviceInfo.length === 0" class="empty-words">
                    无设备信息
                  </div>
                  <el-tag 
                    v-for="(device, index) in processResult.keyInfoResult.deviceInfo" 
                    :key="index"
                    class="info-tag"
                    type="danger"
                  >
                    {{ device }}
                  </el-tag>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card shadow="hover">
                  <template #header>
                    <div class="key-info-header">
                      <span>数值信息</span>
                    </div>
                  </template>
                  <div v-if="!processResult.keyInfoResult.numberInfo || processResult.keyInfoResult.numberInfo.length === 0" class="empty-words">
                    无数值信息
                  </div>
                  <el-tag 
                    v-for="(number, index) in processResult.keyInfoResult.numberInfo" 
                    :key="index"
                    class="info-tag"
                    type="primary"
                  >
                    {{ number }}
                  </el-tag>
                </el-card>
              </el-col>
            </el-row>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

export default {
  name: 'NlpProcessView',
  setup() {
    // 表单数据
    const form = ref({
      messageId: null,
      content: '',
      processTypes: 0,
      async: false
    })
    
    // 选中的处理类型
    const selectedProcessTypes = ref(['1', '2', '4', '8', '16', '32'])
    
    // 处理结果
    const processResult = ref(null)
    
    // 处理中状态
    const processing = ref(false)
    
    // 当前激活的结果标签页
    const activeTab = ref('preprocess')
    
    // 计算处理类型的值
    const computeProcessTypes = () => {
      return selectedProcessTypes.value.reduce((sum, type) => sum + parseInt(type), 0)
    }
    
    // 意图得分数据转换
    const intentScoresData = computed(() => {
      if (!processResult.value || !processResult.value.intentResult || !processResult.value.intentResult.intentScores) {
        return []
      }
      
      const scores = processResult.value.intentResult.intentScores
      return Object.keys(scores).map(intent => ({
        intent,
        score: scores[intent]
      })).sort((a, b) => b.score - a.score)
    })
    
    // 百分比格式化
    const percentageFormat = (percentage) => {
      return percentage + '%'
    }
    
    // 获取实体类型标签
    const getEntityTypeLabel = (type) => {
      const typeMap = {
        'PERSON': '人名',
        'LOCATION': '地名',
        'ORGANIZATION': '机构名',
        'TIME': '时间',
        'PHONE': '电话',
        'DEVICE_ID': '设备编号'
      }
      return typeMap[type] || type
    }
    
    // 获取实体类型标签样式
    const getEntityTypeTag = (type) => {
      const typeTagMap = {
        'PERSON': 'info',
        'LOCATION': 'success',
        'ORGANIZATION': 'warning',
        'TIME': 'warning',
        'PHONE': 'danger',
        'DEVICE_ID': 'primary'
      }
      return typeTagMap[type] || ''
    }
    
    // 获取情感标签样式
    const getSentimentTag = (sentiment) => {
      const sentimentTagMap = {
        '正面': 'success',
        '负面': 'danger',
        '中性': 'info'
      }
      return sentimentTagMap[sentiment] || ''
    }
    
    // 获取情感颜色
    const getSentimentColor = (sentiment) => {
      const sentimentColorMap = {
        '正面': '#67C23A',
        '负面': '#F56C6C',
        '中性': '#909399'
      }
      return sentimentColorMap[sentiment] || ''
    }
    
    // 处理文本
    const processText = async () => {
      // 表单验证
      if (!form.value.messageId) {
        ElMessage.warning('请输入消息ID')
        return
      }
      
      if (!form.value.content) {
        ElMessage.warning('请输入文本内容')
        return
      }
      
      if (selectedProcessTypes.value.length === 0) {
        ElMessage.warning('请选择至少一种处理类型')
        return
      }
      
      try {
        processing.value = true
        
        // 计算处理类型值
        form.value.processTypes = computeProcessTypes()
        
        // 发送请求
        const response = await axios.post('/api/nlp/process', form.value)
        processResult.value = response.data
        
        // 如果是异步处理，设置定时刷新结果
        if (form.value.async && processResult.value.status === 0) {
          setTimeout(() => {
            refreshResult()
          }, 1000)
        }
        
        ElMessage.success('处理请求已提交')
      } catch (error) {
        console.error('处理文本失败', error)
        ElMessage.error('处理文本失败：' + (error.response?.data?.message || error.message || '未知错误'))
      } finally {
        processing.value = false
      }
    }
    
    // 刷新处理结果
    const refreshResult = async () => {
      if (!processResult.value || !processResult.value.processId) {
        return
      }
      
      try {
        const response = await axios.get(`/api/nlp/result/${processResult.value.processId}`)
        processResult.value = response.data
        
        // 如果仍在处理中，继续定时刷新
        if (processResult.value.status === 0) {
          setTimeout(() => {
            refreshResult()
          }, 1000)
        }
      } catch (error) {
        console.error('刷新处理结果失败', error)
        ElMessage.error('刷新处理结果失败：' + (error.response?.data?.message || error.message || '未知错误'))
      }
    }
    
    return {
      form,
      selectedProcessTypes,
      processResult,
      processing,
      activeTab,
      intentScoresData,
      percentageFormat,
      getEntityTypeLabel,
      getEntityTypeTag,
      getSentimentTag,
      getSentimentColor,
      processText,
      refreshResult
    }
  }
}
</script>

<style scoped>
.nlp-process-container {
  padding: 20px;
}

.nlp-process-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-tabs {
  margin-top: 20px;
}

.word-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.category-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.info-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.sentiment-header,
.key-info-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sentiment-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.empty-words {
  color: #909399;
  font-style: italic;
  padding: 10px 0;
}

.summary-item {
  margin-bottom: 10px;
  line-height: 1.5;
}

.error-message {
  margin-bottom: 20px;
}
</style>