import request from '@/utils/request';

export interface Message {
  id: number;
  sender: string;
  recipient: string;
  content: string;
  type: string;
  timestamp: string;
}

export interface MessageQuery extends PageQuery {
  content?: string;
  sender?: string;
  dateRange?: [string, string] | null;
}

export const getMessageList = (params: MessageQuery) => {
  return request<PageResult<Message[]>>({
    url: '/api/messages',
    method: 'get',
    params
  });
};

export const getMessageDetail = (id: number) => {
  return request<Message>({
    url: `/messages/${id}`,
    method: 'get'
  });
};

export const createMessage = (data: Partial<Message>) => {
  return request({
    url: '/api/messages',
    method: 'post',
    data
  });
};

export const updateMessage = (id: number, data: Partial<Message>) => {
  return request({
    url: `/messages/${id}`,
    method: 'put',
    data
  });
};

export const deleteMessage = (id: number) => {
  return request({
    url: `/messages/${id}`,
    method: 'delete'
  });
};