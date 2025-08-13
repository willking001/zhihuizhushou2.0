/**
 * 日期时间工具函数
 */

/**
 * 格式化日期时间
 * @param dateTime 日期时间字符串或Date对象
 * @param format 格式化模式，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns 格式化后的日期时间字符串
 */
export const formatDateTime = (dateTime: string | Date | null | undefined, format: string = 'YYYY-MM-DD HH:mm:ss'): string => {
  if (!dateTime) return '-'
  
  const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
  
  if (isNaN(date.getTime())) return '-'
  
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

/**
 * 格式化日期
 * @param date 日期字符串或Date对象
 * @returns 格式化后的日期字符串 (YYYY-MM-DD)
 */
export const formatDate = (date: string | Date | null | undefined): string => {
  return formatDateTime(date, 'YYYY-MM-DD')
}

/**
 * 格式化时间
 * @param time 时间字符串或Date对象
 * @returns 格式化后的时间字符串 (HH:mm:ss)
 */
export const formatTime = (time: string | Date | null | undefined): string => {
  return formatDateTime(time, 'HH:mm:ss')
}

/**
 * 获取相对时间描述
 * @param dateTime 日期时间字符串或Date对象
 * @returns 相对时间描述，如 "刚刚"、"5分钟前"、"2小时前" 等
 */
export const getRelativeTime = (dateTime: string | Date | null | undefined): string => {
  if (!dateTime) return '-'
  
  const date = typeof dateTime === 'string' ? new Date(dateTime) : dateTime
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 0) return '未来时间'
  
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)
  
  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return formatDateTime(date, 'YYYY-MM-DD')
}

/**
 * 判断是否为今天
 * @param date 日期字符串或Date对象
 * @returns 是否为今天
 */
export const isToday = (date: string | Date | null | undefined): boolean => {
  if (!date) return false
  
  const targetDate = typeof date === 'string' ? new Date(date) : date
  const today = new Date()
  
  return targetDate.getFullYear() === today.getFullYear() &&
         targetDate.getMonth() === today.getMonth() &&
         targetDate.getDate() === today.getDate()
}

/**
 * 判断是否为本周
 * @param date 日期字符串或Date对象
 * @returns 是否为本周
 */
export const isThisWeek = (date: string | Date | null | undefined): boolean => {
  if (!date) return false
  
  const targetDate = typeof date === 'string' ? new Date(date) : date
  const today = new Date()
  
  // 获取本周的开始时间（周一）
  const startOfWeek = new Date(today)
  const dayOfWeek = today.getDay() === 0 ? 7 : today.getDay() // 将周日从0改为7
  startOfWeek.setDate(today.getDate() - dayOfWeek + 1)
  startOfWeek.setHours(0, 0, 0, 0)
  
  // 获取本周的结束时间（周日）
  const endOfWeek = new Date(startOfWeek)
  endOfWeek.setDate(startOfWeek.getDate() + 6)
  endOfWeek.setHours(23, 59, 59, 999)
  
  return targetDate >= startOfWeek && targetDate <= endOfWeek
}