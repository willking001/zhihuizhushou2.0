/**
 * 系统状态码常量定义
 * 此文件集中定义了系统中使用的所有状态码及其含义
 */

// HTTP状态码
export const HTTP_STATUS = {
  OK: 200,                    // 请求成功
  BAD_REQUEST: 400,           // 请求参数错误
  UNAUTHORIZED: 401,          // 未认证
  FORBIDDEN: 403,             // 权限不足
  NOT_FOUND: 404,             // 资源不存在
  TOO_MANY_REQUESTS: 429,     // 请求频率限制
  INTERNAL_SERVER_ERROR: 500  // 服务器内部错误
}

// 业务状态码
export const BUSINESS_CODE = {
  SUCCESS: 20000,             // 操作成功
  ERROR: 50000,               // 操作失败
  LOGIN_ERROR: 50001,         // 用户名或密码错误
  TOKEN_EXPIRED: 50014        // Token过期
}

// 状态码描述
export const CODE_MESSAGE = {
  // HTTP状态码描述
  [HTTP_STATUS.OK]: '请求成功',
  [HTTP_STATUS.BAD_REQUEST]: '请求参数错误',
  [HTTP_STATUS.UNAUTHORIZED]: '用户未认证',
  [HTTP_STATUS.FORBIDDEN]: '用户无权限',
  [HTTP_STATUS.NOT_FOUND]: '请求资源不存在',
  [HTTP_STATUS.TOO_MANY_REQUESTS]: '请求过于频繁',
  [HTTP_STATUS.INTERNAL_SERVER_ERROR]: '服务器内部错误',
  
  // 业务状态码描述
  [BUSINESS_CODE.SUCCESS]: '操作成功',
  [BUSINESS_CODE.ERROR]: '操作失败',
  [BUSINESS_CODE.LOGIN_ERROR]: '用户名或密码错误',
  [BUSINESS_CODE.TOKEN_EXPIRED]: 'Token已过期，请重新登录'
}

/**
 * 根据状态码获取对应的消息
 * @param code 状态码
 * @returns 状态码对应的消息
 */
export function getMessageByCode(code: number): string {
  return CODE_MESSAGE[code] || '未知错误'
}

/**
 * 判断是否为成功的业务状态码
 * @param code 状态码
 * @returns 是否成功
 */
export function isSuccessCode(code: number): boolean {
  return code === BUSINESS_CODE.SUCCESS
}

/**
 * 判断是否需要重新登录的状态码
 * @param code 状态码
 * @returns 是否需要重新登录
 */
export function isReLoginCode(code: number): boolean {
  return [
    HTTP_STATUS.UNAUTHORIZED,
    HTTP_STATUS.FORBIDDEN,
    BUSINESS_CODE.TOKEN_EXPIRED
  ].includes(code)
}