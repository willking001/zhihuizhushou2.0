<template>
  <div class="not-found-container">
    <div class="content">
      <h1>404</h1>
      <p>抱歉，您访问的页面不存在。</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  text-align: center;
}

.content h1 {
  font-size: 100px;
  color: #409eff;
}

.content p {
  font-size: 20px;
  color: #606266;
  margin-bottom: 20px;
}
</style>