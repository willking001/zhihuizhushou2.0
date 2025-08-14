import request from '@/utils/request'
// 定义分页查询接口
interface PageQuery {
  pageNum: number;
  pageSize: number;
}

// 定义分页结果接口
interface PageResult<T> {
  total: number;
  list: T;
}

// User interfaces
export interface User {
  id: number
  username: string
  email: string
  phone?: string
  wechatName?: string  // 微信名，用于网格员通知
  role: string
  status: 'active' | 'inactive' | 'banned'
  createdAt: string
  lastLogin?: string
  avatarColor?: string
  isOnline?: boolean
}

export interface UserQuery extends PageQuery {
  username?: string
  email?: string
  phone?: string
  role?: string
  status?: string
}

// API functions
export const login = (data: any) => {
  return request({
    url: '/api/user/login',
    method: 'post',
    data
  })
}

export const getInfo = (token: string) => {
  return request({
    url: '/api/user/info',
    method: 'get'
    // token已通过请求拦截器在Authorization header中发送
  })
}

export const logout = (token: string) => {
  return request({
    url: '/api/user/logout',
    method: 'post'
    // token已通过请求拦截器在Authorization header中发送
  })
}

export const getUserList = (params: UserQuery) => {
  return request<PageResult<User[]>>({
    url: '/api/user',
    method: 'get',
    params
  })
}

export const getUserDetail = (id: number) => {
  return request<User>({
    url: `/api/user/${id}`,
    method: 'get'
  })
}

export const createUser = (data: Partial<User>) => {
  return request({
    url: '/api/user',
    method: 'post',
    data
  })
}

export const updateUser = (id: number, data: Partial<User>) => {
  return request({
    url: `/api/user/${id}`,
    method: 'put',
    data
  })
}

export const deleteUser = (id: number) => {
  return request({
    url: `/api/user/${id}`,
    method: 'delete'
  })
}