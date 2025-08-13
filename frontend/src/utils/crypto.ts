import CryptoJS from 'crypto-js'

/**
 * 生成SHA-256哈希
 * @param input 输入字符串
 * @returns SHA-256哈希值（小写十六进制字符串）
 */
export function generateSHA256(input: string): string {
  try {
    const hash = CryptoJS.SHA256(input)
    return hash.toString(CryptoJS.enc.Hex)
  } catch (error) {
    console.error('SHA-256哈希生成失败:', error)
    return ''
  }
}

/**
 * 加密密码
 * 根据后端逻辑，优先使用SHA-256加密
 * @param password 原始密码
 * @returns 加密后的密码
 */
export function encryptPassword(password: string): string {
  return generateSHA256(password)
}