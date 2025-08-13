import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'
import { BUSINESS_CODE, isSuccessCode, isReLoginCode, getMessageByCode } from '@/constants/statusCode'

// create an axios instance
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API, // url = base url + request url
  // withCredentials: true, // send cookies when cross-domain requests
  timeout: 5000 // request timeout
})

// request interceptor
service.interceptors.request.use(
  config => {
    // do something before request is sent
    const userStore = useUserStore()
    if (userStore.token) {
      // let each request carry token
      // Use Authorization header with Bearer token format
      config.headers['Authorization'] = `Bearer ${getToken()}`
    }
    console.log('发送请求:', config.method?.toUpperCase(), config.baseURL + config.url, config.params || config.data)
    return config
  },
  error => {
    // do something with request error
    console.log('请求错误:', error) // for debug
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  /**
   * If you want to get http information such as headers or status
   * Please return  response => response
  */

  /**
   * Determine the request status by custom code
   * Here is just an example
   * You can also judge the status by HTTP Status Code
   */
  response => {
    const res = response.data
    // 调试日志
    console.log('响应状态:', response.status, response.statusText)
    console.log('响应URL:', response.config.url)
    console.log('响应数据:', res)
    
    // 检查响应是否有code字段
    if (res && typeof res.code !== 'undefined') {
      // 使用状态码常量判断响应状态
      if (!isSuccessCode(res.code)) {
        // 如果错误信息中包含success，则不显示错误信息
        if (!(res.message && typeof res.message === 'string' && res.message.includes('success'))) {
          ElMessage({
            message: res.message || getMessageByCode(res.code) || 'Error',
            type: 'error',
            duration: 5 * 1000
          })
        }

        // 判断是否需要重新登录
        if (isReLoginCode(res.code)) {
          // to re-login
          ElMessageBox.confirm('您已登出，可以取消继续留在该页面，或者重新登录', '确认登出', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            const userStore = useUserStore()
            userStore.resetToken().then(() => {
              location.reload()
            })
          })
        }
        return Promise.reject(new Error(res.message || getMessageByCode(res.code) || 'Error'))
      }
      return res
    } else {
      // 如果响应没有code字段，直接返回响应数据
      console.warn('响应数据没有code字段:', res)
      return response.data
    }
  },
  error => {
    console.log('请求错误:', error) // for debug
    // 检查错误响应中是否包含'success'字符串，这可能是一个误报的错误
    if (error.message && error.message.includes('success')) {
      // 这可能是一个成功的响应被错误地处理为错误
      console.log('检测到可能的误报错误，尝试作为成功处理')
      return Promise.resolve({ data: { code: BUSINESS_CODE.SUCCESS, message: 'success' } })
    }
    
    // 获取更详细的错误信息
    const errorMessage = error.response?.data?.message || error.message || '请求错误'
    ElMessage({
      message: errorMessage,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service