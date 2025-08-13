/**
 * 生成随机颜色
 * @returns 十六进制颜色代码
 */
export function generateRandomColor(): string {
  // 预定义一组美观的颜色
  const colors = [
    '#3b82f6', // 蓝色
    '#10b981', // 绿色
    '#f59e0b', // 橙色
    '#ef4444', // 红色
    '#8b5cf6', // 紫色
    '#ec4899', // 粉色
    '#06b6d4', // 青色
    '#14b8a6', // 蓝绿色
    '#f97316', // 深橙色
    '#6366f1', // 靛蓝色
    '#a855f7', // 亮紫色
    '#d946ef'  // 洋红色
  ];
  
  // 随机选择一个颜色
  return colors[Math.floor(Math.random() * colors.length)];
}