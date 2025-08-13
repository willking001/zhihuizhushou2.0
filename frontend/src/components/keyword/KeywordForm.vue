<template>
  <div class="keyword-form-container">
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑关键词' : '新增关键词'"
      width="500px"
      :close-on-click-modal="false"
      :before-close="handleClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        class="keyword-form"
      >
        <el-form-item label="关键词" prop="keyword">
          <el-input v-model="form.keyword" placeholder="请输入关键词" />
        </el-form-item>
        
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" class="w-100">
            <el-option label="紧急事件" value="emergency" />
            <el-option label="维修保养" value="maintenance" />
            <el-option label="投诉建议" value="complaint" />
            <el-option label="咨询问答" value="inquiry" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择优先级" class="w-100">
            <el-option label="高" value="high" />
            <el-option label="中" value="medium" />
            <el-option label="低" value="low" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入关键词描述" 
          />
        </el-form-item>
        
        <el-form-item label="标签">
          <el-input 
            v-model="tagInput" 
            placeholder="输入标签后按回车添加"
            @keyup.enter="addTag"
          />
          <div class="tags-container">
            <el-tag
              v-for="(tag, index) in form.tags"
              :key="index"
              closable
              @close="removeTag(index)"
              class="tag-item"
            >
              {{ tag }}
            </el-tag>
          </div>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="form.status"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleClose">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="loading">
            {{ isEdit ? '保存' : '创建' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, defineProps, defineEmits, watch } from 'vue'
import { ElMessage, FormInstance } from 'element-plus'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  keywordData: {
    type: Object,
    default: () => ({})
  },
  isEdit: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'submit', 'cancel'])

const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()
const tagInput = ref('')

// 表单数据
const form = reactive({
  id: undefined as number | undefined,
  keyword: '',
  category: 'other',
  priority: 'medium',
  description: '',
  tags: [] as string[],
  status: true
})

// 表单验证规则
const rules = {
  keyword: [
    { required: true, message: '请输入关键词', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  description: [
    { required: false, message: '请输入描述', trigger: 'blur' },
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ]
}

// 监听visible属性变化
watch(() => props.visible, (val) => {
  dialogVisible.value = val
})

// 监听dialogVisible变化
watch(dialogVisible, (val) => {
  emit('update:visible', val)
  if (!val) {
    resetForm()
  }
})

// 监听keywordData变化，填充表单
watch(() => props.keywordData, (val) => {
  if (val && Object.keys(val).length > 0) {
    Object.keys(val).forEach(key => {
      if (key in form) {
        // @ts-ignore
        form[key] = val[key]
      }
    })
  }
}, { immediate: true, deep: true })

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.id = undefined
  form.keyword = ''
  form.category = 'other'
  form.priority = 'medium'
  form.description = ''
  form.tags = []
  form.status = true
  tagInput.value = ''
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  emit('cancel')
}

// 添加标签
const addTag = () => {
  const tag = tagInput.value.trim()
  if (tag && !form.tags.includes(tag)) {
    form.tags.push(tag)
    tagInput.value = ''
  }
}

// 移除标签
const removeTag = (index: number) => {
  form.tags.splice(index, 1)
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 创建要提交的数据对象
        const submitData = { ...form }
        
        // 提交表单数据
        emit('submit', submitData)
        
        // 关闭对话框
        dialogVisible.value = false
        
        // 显示成功消息
        ElMessage.success(props.isEdit ? '关键词更新成功' : '关键词创建成功')
        
        // 重置表单
        resetForm()
      } catch (error) {
        console.error('提交表单失败:', error)
        ElMessage.error('操作失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.keyword-form-container {
  .keyword-form {
    max-height: 60vh;
    overflow-y: auto;
    padding-right: 10px;
  }
  
  .w-100 {
    width: 100%;
  }
  
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
  
  .tags-container {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 8px;
  }
  
  .tag-item {
    margin-right: 0;
  }
}
</style>