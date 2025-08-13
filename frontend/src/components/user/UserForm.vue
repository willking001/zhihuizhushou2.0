<template>
  <div class="user-form-container">
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="500px"
      :close-on-click-modal="false"
      :before-close="handleClose"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        class="user-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号码" />
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" class="w-100">
            <el-option label="管理员" value="admin" />
            <el-option label="网格员" value="grid" />
            <el-option label="普通用户" value="user" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态" class="w-100">
            <el-option label="活跃" value="active" />
            <el-option label="非活跃" value="inactive" />
            <el-option label="已禁用" value="banned" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入密码" 
            show-password 
          />
          <div v-if="isEdit" class="form-tip">不填写则保持原密码不变</div>
        </el-form-item>
        
        <el-form-item v-if="form.password" label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="form.confirmPassword" 
            type="password" 
            placeholder="请再次输入密码" 
            show-password 
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
import { generateRandomColor } from '@/utils/color'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  userData: {
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

// 表单数据
const form = reactive({
  id: undefined as number | undefined,
  username: '',
  email: '',
  phone: '',
  role: 'user',
  status: 'active',
  password: '',
  confirmPassword: '',
  avatarColor: ''
})

// 表单验证规则
const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePassword = (rule: any, value: string, callback: any) => {
  if (props.isEdit && !value) {
    // 编辑模式下允许密码为空
    callback()
  } else if (value && value.length < 6) {
    callback(new Error('密码长度不能小于6位'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ],
  password: [
    { required: !props.isEdit, message: '请输入密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: false, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
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

// 监听userData变化，填充表单
watch(() => props.userData, (val) => {
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
  form.username = ''
  form.email = ''
  form.phone = ''
  form.role = 'user'
  form.status = 'active'
  form.password = ''
  form.confirmPassword = ''
  form.avatarColor = ''
}

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  emit('cancel')
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 如果是新增用户，生成随机头像颜色
        if (!props.isEdit) {
          form.avatarColor = generateRandomColor()
        }
        
        // 创建要提交的数据对象，移除确认密码字段
        const submitData = { ...form }
        delete submitData.confirmPassword
        
        // 如果是编辑模式且没有修改密码，则不提交密码字段
        if (props.isEdit && !form.password) {
          delete submitData.password
        }
        
        // 提交表单数据
        emit('submit', submitData)
        
        // 关闭对话框
        dialogVisible.value = false
        
        // 显示成功消息
        ElMessage.success(props.isEdit ? '用户信息更新成功' : '用户创建成功')
        
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
.user-form-container {
  .user-form {
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
  
  .form-tip {
    font-size: 12px;
    color: #909399;
    margin-top: 5px;
    line-height: 1.2;
  }
}
</style>