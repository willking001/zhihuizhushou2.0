import request from '@/utils/request';

export interface MessageTemplate {
  id: string;
  name: string;
  template: string;
  type: 'forward' | 'reply';
  keywords?: string[];
  conditions?: string[];
  priority?: number;
  enabled: boolean;
  header?: string;
  attachmentRule?: string;
  dataMasking?: boolean;
  maskingRules?: string[];
}

// 获取消息模板列表
export const getTemplateList = (type: 'forward' | 'reply') => {
  return request<Record<string, MessageTemplate>>({
    url: `/api/system/templates`,
    method: 'get',
    params: { type },
    // 添加错误处理
    validateStatus: (status) => {
      return status >= 200 && status < 300; // 默认只接受2xx的状态码
    }
  });
};

// 创建消息模板
export const createTemplate = (type: 'forward' | 'reply', data: Partial<MessageTemplate>) => {
  return request({
    url: '/api/system/templates',
    method: 'post',
    data: {
      ...data,
      type
    },
    validateStatus: (status) => {
      return status >= 200 && status < 300; // 只接受2xx状态码
    }
  });
};

// 更新消息模板
export const updateTemplate = (id: string, data: Partial<MessageTemplate>) => {
  return request({
    url: `/api/system/templates/${id}`,
    method: 'put',
    data,
    validateStatus: (status) => {
      return status >= 200 && status < 300; // 只接受2xx状态码
    }
  });
};

// 删除消息模板
export const deleteTemplate = (id: string) => {
  return request({
    url: `/api/system/templates/${id}`,
    method: 'delete',
    validateStatus: (status) => {
      return status >= 200 && status < 300; // 只接受2xx状态码
    }
  });
};

// 导出模板
export const exportTemplates = () => {
  return request({
    url: '/api/system/templates/export',
    method: 'get',
    responseType: 'blob'
  });
};

// 导入模板
export const importTemplates = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  
  return request({
    url: '/api/system/templates/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
};