<template>
  <div class="login-container">
    <div class="login-background">
      <div class="bg-pattern"></div>
      <div class="bg-gradient"></div>
    </div>
    
    <div class="login-content">
      <div class="login-card">
        <div class="login-header">
          <div class="logo">
            <div class="logo-icon">
              <svg viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
              </svg>
            </div>
            <div class="logo-text">
              <h1>智慧电子助手</h1>
              <p>管理后台</p>
            </div>
          </div>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label for="username" class="form-label">用户名</label>
            <div class="input-wrapper">
              <div class="input-icon">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
                </svg>
              </div>
              <input
                id="username"
                v-model="loginForm.username"
                type="text"
                class="form-input"
                placeholder="请输入用户名"
                required
              >
            </div>
          </div>

          <div class="form-group">
            <label for="password" class="form-label">密码</label>
            <div class="input-wrapper">
              <div class="input-icon">
                <svg viewBox="0 0 24 24" fill="currentColor">
                  <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
                </svg>
              </div>
              <input
                id="password"
                v-model="loginForm.password"
                :type="showPassword ? 'text' : 'password'"
                class="form-input"
                placeholder="请输入密码"
                required
              >
              <button
                type="button"
                @click="togglePassword"
                class="password-toggle"
              >
                <svg v-if="showPassword" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-3.98.7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z"/>
                </svg>
                <svg v-else viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"/>
                </svg>
              </button>
            </div>
          </div>

          <div class="form-options">
            <label class="checkbox-wrapper">
              <input v-model="loginForm.remember" type="checkbox" class="checkbox">
              <span class="checkmark"></span>
              <span class="checkbox-label">记住我</span>
            </label>
            <a href="#" class="forgot-link">忘记密码？</a>
          </div>

          <button
            type="submit"
            :disabled="loading"
            class="login-button"
            :class="{ 'loading': loading }"
          >
            <div v-if="loading" class="loading-spinner"></div>
            <span>{{ loading ? '登录中...' : '登录' }}</span>
          </button>
        </form>

        <div class="login-footer">
          <p class="footer-text">新圣顺科技有限责任公司 版权所有</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
  remember: true
})

const loading = ref(false)
const showPassword = ref(false)

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const handleLogin = async () => {
  if (!loginForm.username || !loginForm.password) {
    ElMessage({
      message: '请输入用户名和密码',
      type: 'warning'
    })
    return
  }

  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage({
      message: '登录成功',
      type: 'success'
    })
    router.push('/')
  } catch (error: any) {
    console.error('登录失败:', error)
    // 错误提示已在request拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;

  .bg-pattern {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: 
      radial-gradient(circle at 25% 25%, rgba(59, 130, 246, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 75% 75%, rgba(139, 92, 246, 0.1) 0%, transparent 50%);
    background-size: 400px 400px;
    animation: float 20s ease-in-out infinite;
  }

  .bg-gradient {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    opacity: 0.9;
  }
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  33% {
    transform: translate(30px, -30px) rotate(120deg);
  }
  66% {
    transform: translate(-20px, 20px) rotate(240deg);
  }
}

.login-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.1),
    0 0 0 1px rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;

  .logo {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;

    .logo-icon {
      width: 64px;
      height: 64px;
      background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      box-shadow: 0 8px 32px rgba(59, 130, 246, 0.3);

      svg {
        width: 32px;
        height: 32px;
      }
    }

    .logo-text {
      h1 {
        font-size: 28px;
        font-weight: 800;
        color: #1e293b;
        margin: 0 0 8px 0;
        letter-spacing: -0.5px;
      }

      p {
        font-size: 16px;
        color: #64748b;
        margin: 0;
        font-weight: 500;
      }
    }
  }
}

.login-form {
  .form-group {
    margin-bottom: 24px;

    .form-label {
      display: block;
      font-size: 14px;
      font-weight: 600;
      color: #374151;
      margin-bottom: 8px;
    }

    .input-wrapper {
      position: relative;
      display: flex;
      align-items: center;

      .input-icon {
        position: absolute;
        left: 16px;
        width: 20px;
        height: 20px;
        color: #9ca3af;
        z-index: 1;

        svg {
          width: 100%;
          height: 100%;
        }
      }

      .form-input {
        width: 100%;
        height: 52px;
        padding: 0 48px 0 48px;
        border: 2px solid #e5e7eb;
        border-radius: 12px;
        font-size: 16px;
        background: #f9fafb;
        transition: all 0.2s;
        outline: none;

        &:focus {
          border-color: #3b82f6;
          background: white;
          box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        &::placeholder {
          color: #9ca3af;
        }
      }

      .password-toggle {
        position: absolute;
        right: 16px;
        background: none;
        border: none;
        color: #9ca3af;
        cursor: pointer;
        padding: 4px;
        border-radius: 4px;
        transition: all 0.2s;

        &:hover {
          color: #6b7280;
          background: #f3f4f6;
        }

        svg {
          width: 20px;
          height: 20px;
        }
      }
    }
  }
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;

  .checkbox-wrapper {
    display: flex;
    align-items: center;
    cursor: pointer;
    user-select: none;

    .checkbox {
      display: none;

      &:checked + .checkmark {
        background: #3b82f6;
        border-color: #3b82f6;

        &::after {
          opacity: 1;
          transform: rotate(45deg) scale(1);
        }
      }
    }

    .checkmark {
      width: 20px;
      height: 20px;
      border: 2px solid #d1d5db;
      border-radius: 4px;
      margin-right: 8px;
      position: relative;
      transition: all 0.2s;
      background: white;

      &::after {
        content: '';
        position: absolute;
        left: 5px;
        top: 1px;
        width: 6px;
        height: 10px;
        border: solid white;
        border-width: 0 2px 2px 0;
        opacity: 0;
        transform: rotate(45deg) scale(0.8);
        transition: all 0.2s;
      }
    }

    .checkbox-label {
      font-size: 14px;
      color: #374151;
      font-weight: 500;
    }
  }

  .forgot-link {
    font-size: 14px;
    color: #3b82f6;
    text-decoration: none;
    font-weight: 600;
    transition: color 0.2s;

    &:hover {
      color: #1d4ed8;
    }
  }
}

.login-button {
  width: 100%;
  height: 52px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  position: relative;
  overflow: hidden;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 8px 25px rgba(59, 130, 246, 0.4);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }

  &.loading {
    pointer-events: none;
  }

  .loading-spinner {
    width: 20px;
    height: 20px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    border-top: 2px solid white;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.login-footer {
  margin-top: 32px;
  text-align: center;

  .footer-text {
    font-size: 12px;
    color: #9ca3af;
    margin: 0;
  }
}

// 响应式设计
@media (max-width: 480px) {
  .login-content {
    padding: 16px;
  }

  .login-card {
    padding: 32px 24px;
    border-radius: 16px;
  }

  .login-header .logo .logo-text h1 {
    font-size: 24px;
  }

  .form-options {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
}
</style>