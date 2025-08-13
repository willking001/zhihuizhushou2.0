import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, getInfo } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { resetRouter } from '@/router'
import { encryptPassword } from '@/utils/crypto'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    name: '',
    avatar: '',
    roles: [] as string[]
  }),
  actions: {
    // user login
    login(userInfo: any) {
      const { username, password } = userInfo
      return new Promise((resolve, reject) => {
        // 发送原始密码，让后端进行SHA-256加密
        loginApi({ username: username.trim(), password: password })
          .then(response => {
            const { data } = response
            this.token = data.token
            setToken(data.token)
            resolve(data)
          })
          .catch(error => {
            reject(error)
          })
      })
    },

    // get user info
    getInfo() {
      return new Promise((resolve, reject) => {
        getInfo(this.token || '')
          .then(response => {
            const { data } = response

            if (!data) {
              reject('Verification failed, please Login again.')
            }

            const { roles, name, avatar } = data

            // roles must be a non-empty array
            if (!roles || roles.length <= 0) {
              reject('getInfo: roles must be a non-null array!')
            }

            this.roles = roles
            this.name = name
            this.avatar = avatar
            resolve(data)
          })
          .catch(error => {
            reject(error)
          })
      })
    },

    // user logout
    logout() {
      return new Promise((resolve, reject) => {
        logoutApi(this.token || '')
          .then(() => {
            this.token = ''
            this.roles = []
            removeToken() // must remove  token  first
            resetRouter()
            resolve(null)
          })
          .catch(error => {
            reject(error)
          })
      })
    },

    // remove token
    resetToken() {
      return new Promise(resolve => {
        this.token = ''
        this.roles = []
        removeToken()
        resolve(null)
      })
    }
  }
})