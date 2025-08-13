<template>
  <div class="profile-container">
    <div class="profile-header">
      <h1>个人中心</h1>
    </div>
    
    <div class="profile-content">
      <el-card class="profile-card">
        <div class="user-info">
          <div class="avatar-container">
            <el-avatar :size="100" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
          </div>
          <div class="user-details">
            <h2>{{ userInfo.name }}</h2>
            <p>角色: {{ userInfo.roles.join(', ') }}</p>
          </div>
        </div>
      </el-card>

      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <h3>基本信息</h3>
          </div>
        </template>
        <el-form label-width="100px">
          <el-form-item label="用户名">
            <el-input v-model="userInfo.name" disabled />
          </el-form-item>
          <el-form-item label="角色">
            <el-input :value="userInfo.roles.join(', ')" disabled />
          </el-form-item>
        </el-form>
      </el-card>

      <el-card class="profile-card">
        <template #header>
          <div class="card-header">
            <h3>修改密码</h3>
          </div>
        </template>
        <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
          <el-form-item label="当前密码" prop="currentPassword">
            <el-input v-model="passwordForm.currentPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="passwordForm.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="changePassword">修改密码</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userInfo = reactive({
  name: '',
  roles: [] as string[]
})

const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate((valid) => {
    if (valid) {
      // 这里应该调用修改密码的API
      ElMessage.success('密码修改成功')
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }
  })
}

onMounted(async () => {
  // 获取用户信息
  try {
    const data = await userStore.getInfo()
    userInfo.name = userStore.name
    userInfo.roles = userStore.roles
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
})
</script>

<style lang="scss" scoped>
.profile-container {
  padding: 20px;
}

.profile-header {
  margin-bottom: 20px;
  h1 {
    font-size: 24px;
    color: #303133;
  }
}

.profile-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card {
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  padding: 20px 0;
}

.avatar-container {
  margin-right: 30px;
}

.user-details {
  h2 {
    margin: 0 0 10px 0;
    font-size: 20px;
  }
  p {
    margin: 0;
    color: #606266;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  h3 {
    margin: 0;
    font-size: 18px;
  }
}
</style>