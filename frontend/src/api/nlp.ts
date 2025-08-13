import request from '@/utils/request'

// NLP配置相关API
export function getConfigList(params) {
  console.log('调用getConfigList API，参数：', params)
  // 确保与后端控制器路径匹配
  const apiUrl = '/api/nlp/config'
  console.log('请求URL：', apiUrl)
  return request({
    url: apiUrl,
    method: 'get',
    params
  }).then(response => {
    console.log('getConfigList API响应：', response)
    return response
  }).catch(error => {
    console.error('getConfigList API错误：', error)
    throw error
  })
}

export function getConfigById(id) {
  console.log(`调用getConfigById API，ID：${id}`)
  const apiUrl = `/api/nlp/config/${id}`
  console.log('请求URL：', apiUrl)
  return request({
    url: apiUrl,
    method: 'get'
  }).then(response => {
    console.log('getConfigById API响应：', response)
    return response
  }).catch(error => {
    console.error('getConfigById API错误：', error)
    throw error
  })
}

export function createConfig(data) {
  console.log('调用createConfig API，数据：', data)
  const apiUrl = '/api/nlp/config'
  console.log('请求URL：', apiUrl)
  return request({
    url: apiUrl,
    method: 'post',
    data
  }).then(response => {
    console.log('createConfig API响应：', response)
    return response
  }).catch(error => {
    console.error('createConfig API错误：', error)
    throw error
  })
}

// addConfig 作为 createConfig 的别名
export const addConfig = createConfig

export function updateConfig(id, data) {
  console.log('调用updateConfig API，ID：', id, '数据：', data)
  const apiUrl = `/api/nlp/config/${id}`
  console.log('请求URL：', apiUrl)
  return request({
    url: apiUrl,
    method: 'put',
    data
  }).then(response => {
    console.log('updateConfig API响应：', response)
    return response
  }).catch(error => {
    console.error('updateConfig API错误：', error)
    throw error
  })
}

export function deleteConfig(id) {
  console.log('调用deleteConfig API，ID：', id)
  const apiUrl = `/api/nlp/config/${id}`
  console.log('请求URL：', apiUrl)
  return request({
    url: apiUrl,
    method: 'delete'
  }).then(response => {
    console.log('deleteConfig API响应：', response)
    return response
  }).catch(error => {
    console.error('deleteConfig API错误：', error)
    throw error
  })
}

// NLP处理相关API
export function processText(data) {
  console.log('调用processText API，数据：', data)
  const apiUrl = '/api/nlp/process'
  console.log('请求URL：', apiUrl)
  return request({
    url: apiUrl,
    method: 'post',
    data
  }).then(response => {
    console.log('processText API响应：', response)
    return response
  }).catch(error => {
    console.error('processText API错误：', error)
    throw error
  })
}

export function getProcessResult(processId) {
  console.log(`调用getProcessResult API，处理ID：${processId}`)
  const apiUrl = `/api/nlp/result/${processId}`
  console.log('请求URL：', apiUrl)
  return request({
    url: apiUrl,
    method: 'get'
  }).then(response => {
    console.log('getProcessResult API响应：', response)
    return response
  }).catch(error => {
    console.error('getProcessResult API错误：', error)
    throw error
  })
}

export function getProcessResultByMessageId(messageId) {
  console.log(`调用getProcessResultByMessageId API，消息ID：${messageId}`)
  const apiUrl = `/api/nlp/result/message/${messageId}`
  console.log('请求URL：', apiUrl)
  return request({
    url: apiUrl,
    method: 'get'
  }).then(response => {
    console.log('getProcessResultByMessageId API响应：', response)
    return response
  }).catch(error => {
    console.error('getProcessResultByMessageId API错误：', error)
    throw error
  })
}