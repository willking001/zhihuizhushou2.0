<template>
  <div class="nlp-config-container">
    <el-card class="nlp-config-card">
      <template #header>
        <div class="card-header">
          <h2>NLP配置管理</h2>
          <el-button type="primary" @click="openAddDialog">添加配置</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="配置类型">
          <el-select v-model="queryForm.type" placeholder="请选择配置类型" clearable>
            <el-option v-for="item in configTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置名称">
          <el-input v-model="queryForm.name" placeholder="请输入配置名称" clearable />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-select v-model="queryForm.enabled" placeholder="请选择启用状态" clearable>
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchConfigs">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table :data="configList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="配置类型" width="120">
          <template #default="scope">
            <el-tag :type="getConfigTypeTag(scope.row.type)">{{ getConfigTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="配置名称" />
        <el-table-column prop="enabled" label="启用状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'info'">{{ scope.row.enabled ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="viewConfig(scope.row)">查看</el-button>
            <el-button type="warning" size="small" @click="editConfig(scope.row)">编辑</el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="deleteConfig(scope.row)"
              :disabled="scope.row.type === 1 && scope.row.name === '默认预处理配置'"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 配置详情对话框 -->
    <el-dialog
      v-model="configDialogVisible"
      :title="dialogTitle"
      width="60%"
      :close-on-click-modal="false"
    >
      <el-form :model="configForm" :rules="configRules" ref="configFormRef" label-width="120px">
        <el-form-item label="配置类型" prop="type">
          <el-select v-model="configForm.type" placeholder="请选择配置类型" :disabled="dialogMode === 'view' || dialogMode === 'edit'">
            <el-option v-for="item in configTypes" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="configForm.name" placeholder="请输入配置名称" :disabled="dialogMode === 'view' || (dialogMode === 'edit' && configForm.type === 1 && configForm.name === '默认预处理配置')" />
        </el-form-item>
        <el-form-item label="启用状态" prop="enabled">
          <el-switch v-model="configForm.enabled" :disabled="dialogMode === 'view'" />
        </el-form-item>
        <el-form-item label="优先级" prop="priority">
          <el-input-number v-model="configForm.priority" :min="1" :max="100" :disabled="dialogMode === 'view'" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="configForm.remark" type="textarea" :rows="2" placeholder="请输入备注" :disabled="dialogMode === 'view'" />
        </el-form-item>
        <el-form-item label="配置内容" prop="content">
          <el-tabs v-model="activeConfigTab">
            <el-tab-pane label="JSON编辑器" name="json">
              <div class="json-editor-container" :class="{ 'view-mode': dialogMode === 'view' }">
                <el-input
                  v-model="configForm.content"
                  type="textarea"
                  :rows="15"
                  placeholder="请输入JSON格式的配置内容"
                  :disabled="dialogMode === 'view'"
                />
              </div>
            </el-tab-pane>
            <el-tab-pane label="表单编辑器" name="form">
              <div class="form-editor-container">
                <component 
                  :is="getConfigFormComponent(configForm.type)"
                  v-if="configForm.type"
                  v-model:config="parsedConfig"
                  :disabled="dialogMode === 'view'"
                />
                <el-empty v-else description="请先选择配置类型" />
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="configDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveConfig" v-if="dialogMode !== 'view'">保存</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="删除确认"
      width="30%"
    >
      <span>确定要删除配置 "{{ currentConfig?.name }}" 吗？此操作不可恢复！</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确定删除</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getConfigList, getConfigById, addConfig, updateConfig, deleteConfig as deleteNlpConfig } from '@/api/nlp'

// 预处理配置表单组件
const PreprocessConfigForm = {
  props: {
    config: {
      type: Object,
      default: () => ({
        enableSimplifiedConversion: true,
        enableWordSegmentation: true,
        enablePosTagging: true,
        enableStopwordRemoval: true,
        enableTextCleaning: true,
        customStopwords: []
      })
    },
    disabled: Boolean
  },
  emits: ['update:config'],
  setup(props, { emit }) {
    const localConfig = ref({ ...props.config })
    
    watch(() => props.config, (newVal) => {
      localConfig.value = { ...newVal }
    }, { deep: true })
    
    watch(localConfig, (newVal) => {
      emit('update:config', { ...newVal })
    }, { deep: true })
    
    const stopwordInput = ref('')
    
    const addStopword = () => {
      if (!stopwordInput.value) return
      if (!localConfig.value.customStopwords) {
        localConfig.value.customStopwords = []
      }
      if (!localConfig.value.customStopwords.includes(stopwordInput.value)) {
        localConfig.value.customStopwords.push(stopwordInput.value)
      }
      stopwordInput.value = ''
    }
    
    const removeStopword = (index) => {
      localConfig.value.customStopwords.splice(index, 1)
    }
    
    return {
      localConfig,
      stopwordInput,
      addStopword,
      removeStopword
    }
  },
  template: `
    <el-form label-width="180px">
      <el-form-item label="启用简繁体转换">
        <el-switch v-model="localConfig.enableSimplifiedConversion" :disabled="disabled" />
      </el-form-item>
      <el-form-item label="启用分词">
        <el-switch v-model="localConfig.enableWordSegmentation" :disabled="disabled" />
      </el-form-item>
      <el-form-item label="启用词性标注">
        <el-switch v-model="localConfig.enablePosTagging" :disabled="disabled" />
      </el-form-item>
      <el-form-item label="启用停用词过滤">
        <el-switch v-model="localConfig.enableStopwordRemoval" :disabled="disabled" />
      </el-form-item>
      <el-form-item label="启用文本清洗">
        <el-switch v-model="localConfig.enableTextCleaning" :disabled="disabled" />
      </el-form-item>
      <el-form-item label="自定义停用词">
        <div class="stopword-input" v-if="!disabled">
          <el-input v-model="stopwordInput" placeholder="请输入停用词" @keyup.enter="addStopword">
            <template #append>
              <el-button @click="addStopword">添加</el-button>
            </template>
          </el-input>
        </div>
        <div class="stopword-list">
          <el-tag
            v-for="(word, index) in localConfig.customStopwords"
            :key="index"
            closable
            :disable-transitions="false"
            @close="removeStopword(index)"
            :class="{ 'view-only': disabled }"
          >
            {{ word }}
          </el-tag>
          <el-empty v-if="!localConfig.customStopwords || localConfig.customStopwords.length === 0" description="暂无自定义停用词" />
        </div>
      </el-form-item>
    </el-form>
  `
}

// 意图识别配置表单组件
const IntentConfigForm = {
  props: {
    config: {
      type: Object,
      default: () => ({
        intentDefinitions: []
      })
    },
    disabled: Boolean
  },
  emits: ['update:config'],
  setup(props, { emit }) {
    const localConfig = ref({ ...props.config })
    
    watch(() => props.config, (newVal) => {
      localConfig.value = { ...newVal }
    }, { deep: true })
    
    watch(localConfig, (newVal) => {
      emit('update:config', { ...newVal })
    }, { deep: true })
    
    const newIntent = ref({
      intent: '',
      keywords: '',
      description: ''
    })
    
    const addIntent = () => {
      if (!newIntent.value.intent || !newIntent.value.keywords) return
      
      if (!localConfig.value.intentDefinitions) {
        localConfig.value.intentDefinitions = []
      }
      
      localConfig.value.intentDefinitions.push({
        intent: newIntent.value.intent,
        keywords: newIntent.value.keywords.split(',').map(k => k.trim()),
        description: newIntent.value.description
      })
      
      newIntent.value = {
        intent: '',
        keywords: '',
        description: ''
      }
    }
    
    const removeIntent = (index) => {
      localConfig.value.intentDefinitions.splice(index, 1)
    }
    
    return {
      localConfig,
      newIntent,
      addIntent,
      removeIntent
    }
  },
  template: `
    <el-form label-width="120px">
      <div v-if="!disabled" class="add-intent-form">
        <el-divider content-position="left">添加意图</el-divider>
        <el-form-item label="意图名称">
          <el-input v-model="newIntent.intent" placeholder="请输入意图名称" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="newIntent.keywords" placeholder="请输入关键词，多个关键词用逗号分隔" />
        </el-form-item>
        <el-form-item label="意图描述">
          <el-input v-model="newIntent.description" placeholder="请输入意图描述" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="addIntent">添加意图</el-button>
        </el-form-item>
      </div>
      
      <el-divider content-position="left">意图列表</el-divider>
      <el-table :data="localConfig.intentDefinitions" border style="width: 100%">
        <el-table-column prop="intent" label="意图名称" width="150" />
        <el-table-column prop="keywords" label="关键词">
          <template #default="scope">
            <el-tag
              v-for="(keyword, index) in scope.row.keywords"
              :key="index"
              class="intent-keyword"
            >
              {{ keyword }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="意图描述" />
        <el-table-column label="操作" width="100" v-if="!disabled">
          <template #default="scope">
            <el-button type="danger" size="small" @click="removeIntent(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!localConfig.intentDefinitions || localConfig.intentDefinitions.length === 0" description="暂无意图定义" />
    </el-form>
  `
}

// 实体提取配置表单组件
const EntityConfigForm = {
  props: {
    config: {
      type: Object,
      default: () => ({
        enableNER: true,
        enableRegexExtraction: true,
        customEntityPatterns: []
      })
    },
    disabled: Boolean
  },
  emits: ['update:config'],
  setup(props, { emit }) {
    const localConfig = ref({ ...props.config })
    
    watch(() => props.config, (newVal) => {
      localConfig.value = { ...newVal }
    }, { deep: true })
    
    watch(localConfig, (newVal) => {
      emit('update:config', { ...newVal })
    }, { deep: true })
    
    const newPattern = ref({
      entityType: '',
      pattern: '',
      description: ''
    })
    
    const entityTypes = [
      { label: '人名', value: 'PERSON' },
      { label: '地名', value: 'LOCATION' },
      { label: '机构名', value: 'ORGANIZATION' },
      { label: '时间', value: 'TIME' },
      { label: '电话', value: 'PHONE' },
      { label: '设备编号', value: 'DEVICE_ID' },
      { label: '自定义', value: 'CUSTOM' }
    ]
    
    const addPattern = () => {
      if (!newPattern.value.entityType || !newPattern.value.pattern) return
      
      if (!localConfig.value.customEntityPatterns) {
        localConfig.value.customEntityPatterns = []
      }
      
      localConfig.value.customEntityPatterns.push({
        entityType: newPattern.value.entityType,
        pattern: newPattern.value.pattern,
        description: newPattern.value.description
      })
      
      newPattern.value = {
        entityType: '',
        pattern: '',
        description: ''
      }
    }
    
    const removePattern = (index) => {
      localConfig.value.customEntityPatterns.splice(index, 1)
    }
    
    const getEntityTypeLabel = (type) => {
      const found = entityTypes.find(item => item.value === type)
      return found ? found.label : type
    }
    
    return {
      localConfig,
      newPattern,
      entityTypes,
      addPattern,
      removePattern,
      getEntityTypeLabel
    }
  },
  template: `
    <el-form label-width="180px">
      <el-form-item label="启用命名实体识别">
        <el-switch v-model="localConfig.enableNER" :disabled="disabled" />
      </el-form-item>
      <el-form-item label="启用正则表达式提取">
        <el-switch v-model="localConfig.enableRegexExtraction" :disabled="disabled" />
      </el-form-item>
      
      <div v-if="!disabled" class="add-pattern-form">
        <el-divider content-position="left">添加自定义实体模式</el-divider>
        <el-form-item label="实体类型">
          <el-select v-model="newPattern.entityType" placeholder="请选择实体类型">
            <el-option
              v-for="item in entityTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="正则表达式模式">
          <el-input v-model="newPattern.pattern" placeholder="请输入正则表达式模式" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newPattern.description" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="addPattern">添加模式</el-button>
        </el-form-item>
      </div>
      
      <el-divider content-position="left">自定义实体模式列表</el-divider>
      <el-table :data="localConfig.customEntityPatterns" border style="width: 100%">
        <el-table-column prop="entityType" label="实体类型" width="150">
          <template #default="scope">
            {{ getEntityTypeLabel(scope.row.entityType) }}
          </template>
        </el-table-column>
        <el-table-column prop="pattern" label="正则表达式模式" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="100" v-if="!disabled">
          <template #default="scope">
            <el-button type="danger" size="small" @click="removePattern(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!localConfig.customEntityPatterns || localConfig.customEntityPatterns.length === 0" description="暂无自定义实体模式" />
    </el-form>
  `
}

// 情感分析配置表单组件
const SentimentConfigForm = {
  props: {
    config: {
      type: Object,
      default: () => ({
        positiveWords: [],
        negativeWords: [],
        enableRiskWarning: true,
        riskKeywords: []
      })
    },
    disabled: Boolean
  },
  emits: ['update:config'],
  setup(props, { emit }) {
    const localConfig = ref({ ...props.config })
    
    watch(() => props.config, (newVal) => {
      localConfig.value = { ...newVal }
    }, { deep: true })
    
    watch(localConfig, (newVal) => {
      emit('update:config', { ...newVal })
    }, { deep: true })
    
    const positiveWordInput = ref('')
    const negativeWordInput = ref('')
    const riskKeywordInput = ref('')
    
    const addPositiveWord = () => {
      if (!positiveWordInput.value) return
      if (!localConfig.value.positiveWords) {
        localConfig.value.positiveWords = []
      }
      if (!localConfig.value.positiveWords.includes(positiveWordInput.value)) {
        localConfig.value.positiveWords.push(positiveWordInput.value)
      }
      positiveWordInput.value = ''
    }
    
    const removePositiveWord = (index) => {
      localConfig.value.positiveWords.splice(index, 1)
    }
    
    const addNegativeWord = () => {
      if (!negativeWordInput.value) return
      if (!localConfig.value.negativeWords) {
        localConfig.value.negativeWords = []
      }
      if (!localConfig.value.negativeWords.includes(negativeWordInput.value)) {
        localConfig.value.negativeWords.push(negativeWordInput.value)
      }
      negativeWordInput.value = ''
    }
    
    const removeNegativeWord = (index) => {
      localConfig.value.negativeWords.splice(index, 1)
    }
    
    const addRiskKeyword = () => {
      if (!riskKeywordInput.value) return
      if (!localConfig.value.riskKeywords) {
        localConfig.value.riskKeywords = []
      }
      if (!localConfig.value.riskKeywords.includes(riskKeywordInput.value)) {
        localConfig.value.riskKeywords.push(riskKeywordInput.value)
      }
      riskKeywordInput.value = ''
    }
    
    const removeRiskKeyword = (index) => {
      localConfig.value.riskKeywords.splice(index, 1)
    }
    
    return {
      localConfig,
      positiveWordInput,
      negativeWordInput,
      riskKeywordInput,
      addPositiveWord,
      removePositiveWord,
      addNegativeWord,
      removeNegativeWord,
      addRiskKeyword,
      removeRiskKeyword
    }
  },
  template: `
    <el-form label-width="120px">
      <el-form-item label="启用风险预警">
        <el-switch v-model="localConfig.enableRiskWarning" :disabled="disabled" />
      </el-form-item>
      
      <el-divider content-position="left">正面情感词</el-divider>
      <div class="word-input" v-if="!disabled">
        <el-input v-model="positiveWordInput" placeholder="请输入正面情感词" @keyup.enter="addPositiveWord">
          <template #append>
            <el-button @click="addPositiveWord">添加</el-button>
          </template>
        </el-input>
      </div>
      <div class="word-list">
        <el-tag
          v-for="(word, index) in localConfig.positiveWords"
          :key="index"
          type="success"
          closable
          :disable-transitions="false"
          @close="removePositiveWord(index)"
          :class="{ 'view-only': disabled }"
        >
          {{ word }}
        </el-tag>
        <el-empty v-if="!localConfig.positiveWords || localConfig.positiveWords.length === 0" description="暂无正面情感词" />
      </div>
      
      <el-divider content-position="left">负面情感词</el-divider>
      <div class="word-input" v-if="!disabled">
        <el-input v-model="negativeWordInput" placeholder="请输入负面情感词" @keyup.enter="addNegativeWord">
          <template #append>
            <el-button @click="addNegativeWord">添加</el-button>
          </template>
        </el-input>
      </div>
      <div class="word-list">
        <el-tag
          v-for="(word, index) in localConfig.negativeWords"
          :key="index"
          type="danger"
          closable
          :disable-transitions="false"
          @close="removeNegativeWord(index)"
          :class="{ 'view-only': disabled }"
        >
          {{ word }}
        </el-tag>
        <el-empty v-if="!localConfig.negativeWords || localConfig.negativeWords.length === 0" description="暂无负面情感词" />
      </div>
      
      <el-divider content-position="left">风险关键词</el-divider>
      <div class="word-input" v-if="!disabled">
        <el-input v-model="riskKeywordInput" placeholder="请输入风险关键词" @keyup.enter="addRiskKeyword">
          <template #append>
            <el-button @click="addRiskKeyword">添加</el-button>
          </template>
        </el-input>
      </div>
      <div class="word-list">
        <el-tag
          v-for="(word, index) in localConfig.riskKeywords"
          :key="index"
          type="warning"
          closable
          :disable-transitions="false"
          @close="removeRiskKeyword(index)"
          :class="{ 'view-only': disabled }"
        >
          {{ word }}
        </el-tag>
        <el-empty v-if="!localConfig.riskKeywords || localConfig.riskKeywords.length === 0" description="暂无风险关键词" />
      </div>
    </el-form>
  `
}

// 文本分类配置表单组件
const ClassificationConfigForm = {
  props: {
    config: {
      type: Object,
      default: () => ({
        categories: []
      })
    },
    disabled: Boolean
  },
  emits: ['update:config'],
  setup(props, { emit }) {
    const localConfig = ref({ ...props.config })
    
    watch(() => props.config, (newVal) => {
      localConfig.value = { ...newVal }
    }, { deep: true })
    
    watch(localConfig, (newVal) => {
      emit('update:config', { ...newVal })
    }, { deep: true })
    
    const newCategory = ref({
      category: '',
      keywords: '',
      description: ''
    })
    
    const addCategory = () => {
      if (!newCategory.value.category || !newCategory.value.keywords) return
      
      if (!localConfig.value.categories) {
        localConfig.value.categories = []
      }
      
      localConfig.value.categories.push({
        category: newCategory.value.category,
        keywords: newCategory.value.keywords.split(',').map(k => k.trim()),
        description: newCategory.value.description
      })
      
      newCategory.value = {
        category: '',
        keywords: '',
        description: ''
      }
    }
    
    const removeCategory = (index) => {
      localConfig.value.categories.splice(index, 1)
    }
    
    return {
      localConfig,
      newCategory,
      addCategory,
      removeCategory
    }
  },
  template: `
    <el-form label-width="120px">
      <div v-if="!disabled" class="add-category-form">
        <el-divider content-position="left">添加类别</el-divider>
        <el-form-item label="类别名称">
          <el-input v-model="newCategory.category" placeholder="请输入类别名称" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="newCategory.keywords" placeholder="请输入关键词，多个关键词用逗号分隔" />
        </el-form-item>
        <el-form-item label="类别描述">
          <el-input v-model="newCategory.description" placeholder="请输入类别描述" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="addCategory">添加类别</el-button>
        </el-form-item>
      </div>
      
      <el-divider content-position="left">类别列表</el-divider>
      <el-table :data="localConfig.categories" border style="width: 100%">
        <el-table-column prop="category" label="类别名称" width="150" />
        <el-table-column prop="keywords" label="关键词">
          <template #default="scope">
            <el-tag
              v-for="(keyword, index) in scope.row.keywords"
              :key="index"
              class="category-keyword"
            >
              {{ keyword }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="类别描述" />
        <el-table-column label="操作" width="100" v-if="!disabled">
          <template #default="scope">
            <el-button type="danger" size="small" @click="removeCategory(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!localConfig.categories || localConfig.categories.length === 0" description="暂无类别定义" />
    </el-form>
  `
}

// 关键信息提取配置表单组件
const KeyInfoConfigForm = {
  props: {
    config: {
      type: Object,
      default: () => ({
        enableKeywordExtraction: true,
        enableSummaryExtraction: true,
        enableTimeExtraction: true,
        enableLocationExtraction: true,
        enablePersonExtraction: true,
        enableDeviceExtraction: true,
        enableNumberExtraction: true,
        keywordCount: 10,
        summaryCount: 3,
        customPatterns: []
      })
    },
    disabled: Boolean
  },
  emits: ['update:config'],
  setup(props, { emit }) {
    const localConfig = ref({ ...props.config })
    
    watch(() => props.config, (newVal) => {
      localConfig.value = { ...newVal }
    }, { deep: true })
    
    watch(localConfig, (newVal) => {
      emit('update:config', { ...newVal })
    }, { deep: true })
    
    const newPattern = ref({
      infoType: '',
      pattern: '',
      description: ''
    })
    
    const infoTypes = [
      { label: '时间信息', value: 'TIME' },
      { label: '地点信息', value: 'LOCATION' },
      { label: '人物信息', value: 'PERSON' },
      { label: '设备信息', value: 'DEVICE' },
      { label: '数值信息', value: 'NUMBER' },
      { label: '自定义信息', value: 'CUSTOM' }
    ]
    
    const addPattern = () => {
      if (!newPattern.value.infoType || !newPattern.value.pattern) return
      
      if (!localConfig.value.customPatterns) {
        localConfig.value.customPatterns = []
      }
      
      localConfig.value.customPatterns.push({
        infoType: newPattern.value.infoType,
        pattern: newPattern.value.pattern,
        description: newPattern.value.description
      })
      
      newPattern.value = {
        infoType: '',
        pattern: '',
        description: ''
      }
    }
    
    const removePattern = (index) => {
      localConfig.value.customPatterns.splice(index, 1)
    }
    
    const getInfoTypeLabel = (type) => {
      const found = infoTypes.find(item => item.value === type)
      return found ? found.label : type
    }
    
    return {
      localConfig,
      newPattern,
      infoTypes,
      addPattern,
      removePattern,
      getInfoTypeLabel
    }
  },
  template: `
    <el-form label-width="180px">
      <el-form-item label="启用关键词提取">
        <el-switch v-model="localConfig.enableKeywordExtraction" :disabled="disabled" />
      </el-form-item>
      <el-form-item label="关键词数量">
        <el-input-number v-model="localConfig.keywordCount" :min="1" :max="20" :disabled="disabled" />
      </el-form-item>
      
      <el-form-item label="启用摘要提取">
        <el-switch v-model="localConfig.enableSummaryExtraction" :disabled="disabled" />
      </el-form-item>
      <el-form-item label="摘要句子数量">
        <el-input-number v-model="localConfig.summaryCount" :min="1" :max="5" :disabled="disabled" />
      </el-form-item>
      
      <el-form-item label="启用时间信息提取">
        <el-switch v-model="localConfig.enableTimeExtraction" :disabled="disabled" />
      </el-form-item>
      
      <el-form-item label="启用地点信息提取">
        <el-switch v-model="localConfig.enableLocationExtraction" :disabled="disabled" />
      </el-form-item>
      
      <el-form-item label="启用人物信息提取">
        <el-switch v-model="localConfig.enablePersonExtraction" :disabled="disabled" />
      </el-form-item>
      
      <el-form-item label="启用设备信息提取">
        <el-switch v-model="localConfig.enableDeviceExtraction" :disabled="disabled" />
      </el-form-item>
      
      <el-form-item label="启用数值信息提取">
        <el-switch v-model="localConfig.enableNumberExtraction" :disabled="disabled" />
      </el-form-item>
      
      <div v-if="!disabled" class="add-pattern-form">
        <el-divider content-position="left">添加自定义提取模式</el-divider>
        <el-form-item label="信息类型">
          <el-select v-model="newPattern.infoType" placeholder="请选择信息类型">
            <el-option
              v-for="item in infoTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="正则表达式模式">
          <el-input v-model="newPattern.pattern" placeholder="请输入正则表达式模式" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newPattern.description" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="addPattern">添加模式</el-button>
        </el-form-item>
      </div>
      
      <el-divider content-position="left">自定义提取模式列表</el-divider>
      <el-table :data="localConfig.customPatterns" border style="width: 100%">
        <el-table-column prop="infoType" label="信息类型" width="150">
          <template #default="scope">
            {{ getInfoTypeLabel(scope.row.infoType) }}
          </template>
        </el-table-column>
        <el-table-column prop="pattern" label="正则表达式模式" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="操作" width="100" v-if="!disabled">
          <template #default="scope">
            <el-button type="danger" size="small" @click="removePattern(scope.$index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!localConfig.customPatterns || localConfig.customPatterns.length === 0" description="暂无自定义提取模式" />
    </el-form>
  `
}

export default {
  name: 'NlpConfigView',
  components: {
    PreprocessConfigForm,
    IntentConfigForm,
    EntityConfigForm,
    SentimentConfigForm,
    ClassificationConfigForm,
    KeyInfoConfigForm
  },
  setup() {
    // 配置类型列表
    const configTypes = [
      { label: '预处理', value: 1 },
      { label: '意图识别', value: 2 },
      { label: '实体提取', value: 4 },
      { label: '情感分析', value: 8 },
      { label: '文本分类', value: 16 },
      { label: '关键信息提取', value: 32 }
    ]
    
    // 查询表单
    const queryForm = reactive({
      type: null,
      name: '',
      enabled: null
    })
    
    // 配置列表
    const configList = ref([])
    
    // 分页相关
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    
    // 加载状态
    const loading = ref(false)
    
    // 配置对话框相关
    const configDialogVisible = ref(false)
    const dialogMode = ref('add') // add, edit, view
    const dialogTitle = computed(() => {
      const mode = {
        'add': '添加',
        'edit': '编辑',
        'view': '查看'
      }[dialogMode.value]
      return `${mode}NLP配置`
    })
    
    // 配置表单
    const configForm = ref({
      id: null,
      type: null,
      name: '',
      content: '',
      enabled: true,
      priority: 10,
      remark: ''
    })
    
    // 配置表单校验规则
    const configRules = {
      type: [{ required: true, message: '请选择配置类型', trigger: 'change' }],
      name: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
      content: [{ required: true, message: '请输入配置内容', trigger: 'blur' }]
    }
    
    // 配置表单引用
    const configFormRef = ref(null)
    
    // 当前选中的配置（用于删除确认）
    const currentConfig = ref(null)
    
    // 删除对话框可见性
    const deleteDialogVisible = ref(false)
    
    // 配置内容标签页
    const activeConfigTab = ref('json')
    
    // 解析后的配置内容（用于表单编辑器）
    const parsedConfig = ref({})
    
    // 监听配置表单内容变化，更新解析后的配置
    watch(() => configForm.value.content, (newVal) => {
      try {
        if (newVal) {
          parsedConfig.value = JSON.parse(newVal)
        } else {
          parsedConfig.value = {}
        }
      } catch (e) {
        console.error('解析JSON失败', e)
        // 保持原有值不变
      }
    })
    
    // 监听解析后的配置变化，更新配置表单内容
    watch(parsedConfig, (newVal) => {
      try {
        configForm.value.content = JSON.stringify(newVal, null, 2)
      } catch (e) {
        console.error('生成JSON失败', e)
      }
    }, { deep: true })
    
    // 获取配置类型标签
    const getConfigTypeLabel = (type) => {
      const found = configTypes.find(item => item.value === type)
      return found ? found.label : '未知类型'
    }
    
    // 获取配置类型标签样式
    const getConfigTypeTag = (type) => {
      const typeTagMap = {
        1: '',
        2: 'success',
        4: 'warning',
        8: 'danger',
        16: 'info',
        32: 'primary'
      }
      return typeTagMap[type] || ''
    }
    
    // 获取配置表单组件
    const getConfigFormComponent = (type) => {
      const componentMap = {
        1: 'PreprocessConfigForm',
        2: 'IntentConfigForm',
        4: 'EntityConfigForm',
        8: 'SentimentConfigForm',
        16: 'ClassificationConfigForm',
        32: 'KeyInfoConfigForm'
      }
      return componentMap[type] || null
    }
    
    // 查询配置列表
    const searchConfigs = async () => {
      try {
        loading.value = true
        
        const params = {
          page: currentPage.value - 1,
          size: pageSize.value,
          ...queryForm
        }
        
        // 移除空值
        Object.keys(params).forEach(key => {
          if (params[key] === null || params[key] === '') {
            delete params[key]
          }
        })
        
        console.log('发送NLP配置查询请求，参数：', params)
        console.log('请求URL完整路径：', import.meta.env.VITE_APP_BASE_API + '/api/nlp/config')
        
        try {
          const response = await getConfigList(params)
          console.log('NLP配置查询响应：', response)
          
          if (response && response.content) {
            console.log('使用分页响应格式处理数据')
            configList.value = response.content
            total.value = response.totalElements
          } else if (Array.isArray(response)) {
            // 如果返回的是数组，直接使用
            console.log('使用数组响应格式处理数据')
            configList.value = response
            total.value = response.length
          } else if (response && typeof response === 'object') {
            // 尝试处理其他可能的响应格式
            console.log('尝试处理其他响应格式', response)
            if (Array.isArray(response.data)) {
              configList.value = response.data
              total.value = response.data.length
            } else if (response.code !== undefined) {
              // 可能是业务错误
              console.error('业务错误', response)
              ElMessage.error('查询失败：' + (response.message || '未知错误'))
              configList.value = []
              total.value = 0
            } else {
              console.error('无法识别的响应格式', response)
              configList.value = []
              total.value = 0
            }
          } else {
            console.error('响应数据格式不符合预期', response)
            configList.value = []
            total.value = 0
          }
        } catch (apiError) {
          console.error('API调用异常', apiError)
          console.error('错误详情', apiError.response || apiError.message || apiError)
          ElMessage.error('API调用异常：' + (apiError.message || '未知错误'))
          configList.value = []
          total.value = 0
        }
      } catch (error) {
        console.error('查询配置列表失败', error)
        console.error('错误堆栈', error.stack)
        ElMessage.error('查询配置列表失败：' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }
    
    // 重置查询条件
    const resetQuery = () => {
      queryForm.type = null
      queryForm.name = ''
      queryForm.enabled = null
      searchConfigs()
    }
    
    // 处理分页大小变化
    const handleSizeChange = (size) => {
      pageSize.value = size
      searchConfigs()
    }
    
    // 处理页码变化
    const handleCurrentChange = (page) => {
      currentPage.value = page
      searchConfigs()
    }
    
    // 打开添加对话框
    const openAddDialog = () => {
      dialogMode.value = 'add'
      configForm.value = {
        id: null,
        type: null,
        name: '',
        content: '',
        enabled: true,
        priority: 10,
        remark: ''
      }
      parsedConfig.value = {}
      configDialogVisible.value = true
      activeConfigTab.value = 'json'
    }
    
    // 查看配置
    const viewConfig = async (row) => {
      try {
        const response = await getConfigById(row.id)
        dialogMode.value = 'view'
        configForm.value = { ...response }
        try {
          parsedConfig.value = JSON.parse(response.content)
        } catch (e) {
          console.error('解析JSON失败', e)
          parsedConfig.value = {}
        }
        configDialogVisible.value = true
        activeConfigTab.value = 'form'
      } catch (error) {
        console.error('获取配置详情失败', error)
        ElMessage.error('获取配置详情失败：' + (error.message || '未知错误'))
      }
    }
    
    // 编辑配置
    const editConfig = async (row) => {
      try {
        const response = await getConfigById(row.id)
        dialogMode.value = 'edit'
        configForm.value = { ...response }
        try {
          parsedConfig.value = JSON.parse(response.content)
        } catch (e) {
          console.error('解析JSON失败', e)
          parsedConfig.value = {}
        }
        configDialogVisible.value = true
        activeConfigTab.value = 'form'
      } catch (error) {
        console.error('获取配置详情失败', error)
        ElMessage.error('获取配置详情失败：' + (error.message || '未知错误'))
      }
    }
    
    // 删除配置
    const deleteConfig = (row) => {
      currentConfig.value = row
      deleteDialogVisible.value = true
    }
    
    // 确认删除
    const confirmDelete = async () => {
      try {
        await deleteNlpConfig(currentConfig.value.id)
        ElMessage.success('删除成功')
        deleteDialogVisible.value = false
        searchConfigs()
      } catch (error) {
        console.error('删除配置失败', error)
        ElMessage.error('删除配置失败：' + (error.message || '未知错误'))
      }
    }
    
    // 保存配置
    const saveConfig = async () => {
      if (!configFormRef.value) return
      
      await configFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        try {
          // 验证JSON格式
          try {
            JSON.parse(configForm.value.content)
          } catch (e) {
            ElMessage.error('配置内容不是有效的JSON格式')
            return
          }
          
          if (dialogMode.value === 'add') {
            await addConfig(configForm.value)
            ElMessage.success('添加成功')
          } else if (dialogMode.value === 'edit') {
            await updateConfig(configForm.value.id, configForm.value)
            ElMessage.success('更新成功')
          }
          
          configDialogVisible.value = false
          searchConfigs()
        } catch (error) {
          console.error('保存配置失败', error)
          ElMessage.error('保存配置失败：' + (error.message || '未知错误'))
        }
      })
    }
    
    // 初始化加载数据
    searchConfigs()
    
    return {
      configTypes,
      queryForm,
      configList,
      currentPage,
      pageSize,
      total,
      loading,
      configDialogVisible,
      dialogMode,
      dialogTitle,
      configForm,
      configRules,
      configFormRef,
      currentConfig,
      deleteDialogVisible,
      activeConfigTab,
      parsedConfig,
      getConfigTypeLabel,
      getConfigTypeTag,
      getConfigFormComponent,
      searchConfigs,
      resetQuery,
      handleSizeChange,
      handleCurrentChange,
      openAddDialog,
      viewConfig,
      editConfig,
      deleteConfig,
      confirmDelete,
      saveConfig
    }
  }
}
</script>

<style scoped>
.nlp-config-container {
  padding: 20px;
}

.nlp-config-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.query-form {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.json-editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
}

.json-editor-container.view-mode {
  background-color: #f5f7fa;
}

.form-editor-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 20px;
  max-height: 500px;
  overflow-y: auto;
}

.word-input,
.stopword-input {
  margin-bottom: 10px;
}

.word-list,
.stopword-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}

.view-only {
  cursor: default;
}

.view-only .el-tag__close {
  display: none;
}

.intent-keyword,
.category-keyword {
  margin-right: 5px;
  margin-bottom: 5px;
}

.add-intent-form,
.add-category-form,
.add-pattern-form {
  margin-bottom: 20px;
}
</style>