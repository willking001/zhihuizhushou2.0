# API接口文档

## 1. 网格员客户端程序API

### 1.1 网格员登录

**接口地址：** `/api/grid/login`

**请求方式：** POST

**请求参数：**

```json
{
  "username": "用户名",
  "password": "密码",
  "grid_area": "网格区域"
}
```

**响应格式：**

```json
{
  "success": true,
  "message": "登录成功",
  "user": {
    "id": 1,
    "username": "用户名",
    "realName": "真实姓名",
    "gridArea": "网格区域",
    "phone": "手机号",
    "email": "邮箱"
  }
}
```

### 1.2 接收网格员消息

**接口地址：** `/api/grid/messages`

**请求方式：** POST

**请求参数：**

```json
{
  "content": "消息内容",
  "sender": "发送者",
  "timestamp": "2024-01-01T10:00:00",
  "source": "消息来源",
  "grid_area": "网格区域"
}
```

**响应格式：**

```json
{
  "success": true,
  "message_id": 123,
  "filtered": false,
  "keywords": [],
  "message": "消息接收成功"
}
```

### 1.3 获取消息统计

**接口地址：** `/api/grid/message_statistics`

**请求方式：** GET

**请求参数：**
- `username` (可选): 用户名

**响应格式：**

```json
{
  "success": true,
  "statistics": {
    "total_messages": 100,
    "forwarded_messages": 50,
    "pending_messages": 50
  }
}
```

### 1.4 获取转发关键词

**接口地址：** `/api/grid/forward_keywords`

**请求方式：** GET

**请求参数：**
- `gridArea` (可选): 网格区域

**响应格式：**

```json
{
  "success": true,
  "keywords": [
    {
      "id": 1,
      "keyword": "关键词",
      "priority": "HIGH",
      "scope": "GLOBAL"
    }
  ]
}
```

### 1.5 获取用户消息

**接口地址：** `/api/grid/messages`

**请求方式：** GET

**请求参数：**
- `username`: 用户名
- `page` (默认0): 页码
- `size` (默认50): 每页大小

**响应格式：**

```json
{
  "success": true,
  "messages": [
    {
      "id": 1,
      "content": "消息内容",
      "sender": "发送者",
      "timestamp": "2024-01-01T10:00:00",
      "forwarded": true
    }
  ],
  "total": 100,
  "page": 0,
  "size": 50
}
```

## 2. 关键词管理API

### 2.1 获取所有关键词配置

**接口地址：** `/api/keywords`

**请求方式：** GET

**响应格式：**

```json
{
  "success": true,
  "keywords": [
    {
      "id": 1,
      "keyword": "关键词",
      "type": "SENSITIVE",
      "priority": "HIGH",
      "scope": "GLOBAL",
      "enabled": true
    }
  ],
  "count": 10
}
```

### 2.2 获取活跃关键词

**接口地址：** `/api/keywords/active`

**请求方式：** GET

**响应格式：**

```json
{
  "success": true,
  "keywords": [
    {
      "id": 1,
      "keyword": "关键词",
      "type": "SENSITIVE",
      "priority": "HIGH"
    }
  ],
  "count": 5
}
```

### 2.3 获取全局关键词

**接口地址：** `/api/keywords/global`

**请求方式：** GET

**响应格式：**

```json
{
  "success": true,
  "keywords": [
    {
      "id": 1,
      "keyword": "全局关键词",
      "type": "SENSITIVE",
      "priority": "HIGH"
    }
  ],
  "count": 3
}
```

### 2.4 获取本地关键词

**接口地址：** `/api/keywords/local/{gridArea}`

**请求方式：** GET

**路径参数：**
- `gridArea`: 网格区域

**响应格式：**

```json
{
  "success": true,
  "keywords": [
    {
      "id": 1,
      "keyword": "本地关键词",
      "type": "LOCAL",
      "gridArea": "区域A"
    }
  ],
  "count": 2,
  "gridArea": "区域A"
}
```

### 2.5 创建关键词配置

**接口地址：** `/api/keywords`

**请求方式：** POST

**请求参数：**

```json
{
  "keyword": "新关键词",
  "type": "SENSITIVE",
  "priority": "MEDIUM",
  "scope": "GLOBAL",
  "enabled": true,
  "description": "关键词描述"
}
```

**响应格式：**

```json
{
  "success": true,
  "keyword": {
    "id": 1,
    "keyword": "新关键词",
    "type": "SENSITIVE",
    "priority": "MEDIUM"
  },
  "message": "关键词创建成功"
}
```

### 2.6 更新关键词配置

**接口地址：** `/api/keywords/{id}`

**请求方式：** PUT

**路径参数：**
- `id`: 关键词ID

**请求参数：**

```json
{
  "keyword": "更新的关键词",
  "type": "SENSITIVE",
  "priority": "HIGH",
  "enabled": true
}
```

**响应格式：**

```json
{
  "success": true,
  "keyword": {
    "id": 1,
    "keyword": "更新的关键词",
    "type": "SENSITIVE",
    "priority": "HIGH"
  },
  "message": "关键词更新成功"
}
```

### 2.7 删除关键词配置

**接口地址：** `/api/keywords/{id}`

**请求方式：** DELETE

**路径参数：**
- `id`: 关键词ID

**响应格式：**

```json
{
  "success": true,
  "message": "关键词删除成功"
}
```

## 3. 关键词增强功能API

### 3.1 关键词学习模块

#### 3.1.1 记录关键词检测

**接口地址：** `/api/keyword-enhancement/learning/detect`

**请求方式：** POST

**请求参数：**

```json
{
  "keyword": "检测到的关键词",
  "context": "上下文内容",
  "gridArea": "网格区域",
  "userId": 1,
  "confidence": 0.85
}
```

**响应格式：**

```json
{
  "success": true,
  "message": "关键词检测记录成功"
}
```

#### 3.1.2 获取待审核推荐

**接口地址：** `/api/keyword-enhancement/learning/recommendations`

**请求方式：** GET

**请求参数：**
- `gridArea` (可选): 网格区域
- `limit` (默认20): 返回数量限制

**响应格式：**

```json
[
  {
    "id": 1,
    "keyword": "推荐关键词",
    "frequency": 10,
    "confidence": 0.8,
    "status": "PENDING"
  }
]
```

#### 3.1.3 审核关键词推荐

**接口地址：** `/api/keyword-enhancement/learning/recommendations/{recommendationId}/review`

**请求方式：** POST

**路径参数：**
- `recommendationId`: 推荐ID

**请求参数：**

```json
{
  "action": "approve",
  "reviewComment": "审核意见",
  "reviewerId": 1
}
```

**响应格式：**

```json
{
  "success": true,
  "message": "审核完成"
}
```

### 3.2 关键词同步模块

#### 3.2.1 启动同步

**接口地址：** `/api/keyword-enhancement/sync/start`

**请求方式：** POST

**请求参数：**

```json
{
  "clientId": "客户端ID",
  "syncType": "FULL",
  "gridArea": "网格区域"
}
```

**响应格式：**

```json
{
  "success": true,
  "syncLogId": 123,
  "message": "同步已启动"
}
```

#### 3.2.2 获取同步状态

**接口地址：** `/api/keyword-enhancement/sync/status`

**请求方式：** GET

**请求参数：**
- `syncLogId` (可选): 同步日志ID

**响应格式：**

```json
{
  "success": true,
  "status": "IN_PROGRESS",
  "progress": 50,
  "message": "同步进行中"
}
```

### 3.3 关键词分析模块

#### 3.3.1 获取分析报告

**接口地址：** `/api/keyword-enhancement/analysis/report`

**请求方式：** GET

**请求参数：**
- `gridArea` (可选): 网格区域
- `days` (默认30): 分析天数

**响应格式：**

```json
{
  "success": true,
  "report": {
    "totalKeywords": 100,
    "activeKeywords": 80,
    "hitRate": 0.75,
    "trends": [
      {
        "date": "2024-01-01",
        "hits": 50
      }
    ]
  }
}
```

#### 3.3.2 获取新兴关键词

**接口地址：** `/api/keyword-enhancement/analysis/emerging`

**请求方式：** GET

**请求参数：**
- `gridArea` (可选): 网格区域
- `days` (默认14): 分析天数

**响应格式：**

```json
[
  {
    "keyword": "新兴关键词",
    "frequency": 25,
    "growthRate": 0.5,
    "firstSeen": "2024-01-01T10:00:00"
  }
]
```

## 4. NLP处理模块API

### 4.1 文本处理

**接口地址：** `/api/nlp/process`

**请求方式：** POST

**请求参数：**

```json
{
  "text": "待处理文本",
  "processType": "FULL",
  "options": {
    "enableSentiment": true,
    "enableEntity": true,
    "enableIntent": true
  }
}
```

**响应格式：**

```json
{
  "processId": 123,
  "text": "待处理文本",
  "sentiment": {
    "score": 0.8,
    "label": "POSITIVE"
  },
  "entities": [
    {
      "text": "实体",
      "type": "PERSON",
      "confidence": 0.9
    }
  ],
  "intent": {
    "name": "COMPLAINT",
    "confidence": 0.85
  }
}
```

### 4.2 文本预处理

**接口地址：** `/api/nlp/preprocess`

**请求方式：** POST

**请求参数：** 文本字符串

**响应格式：**

```json
{
  "originalText": "原始文本",
  "processedText": "处理后文本",
  "tokens": ["词1", "词2"],
  "cleanedText": "清理后文本"
}
```

### 4.3 意图识别

**接口地址：** `/api/nlp/intent`

**请求方式：** POST

**请求参数：** 文本字符串

**响应格式：**

```json
{
  "text": "输入文本",
  "intent": {
    "name": "COMPLAINT",
    "confidence": 0.85,
    "description": "投诉意图"
  },
  "alternatives": [
    {
      "name": "SUGGESTION",
      "confidence": 0.15
    }
  ]
}
```

### 4.4 实体提取

**接口地址：** `/api/nlp/entity`

**请求方式：** POST

**请求参数：** 文本字符串

**响应格式：**

```json
{
  "text": "输入文本",
  "entities": [
    {
      "text": "张三",
      "type": "PERSON",
      "start": 0,
      "end": 2,
      "confidence": 0.95
    },
    {
      "text": "北京市",
      "type": "LOCATION",
      "start": 5,
      "end": 8,
      "confidence": 0.9
    }
  ]
}
```

### 4.5 情感分析

**接口地址：** `/api/nlp/sentiment`

**请求方式：** POST

**请求参数：** 文本字符串

**响应格式：**

```json
{
  "text": "输入文本",
  "sentiment": {
    "label": "POSITIVE",
    "score": 0.85,
    "confidence": 0.9
  },
  "emotions": [
    {
      "emotion": "JOY",
      "score": 0.7
    },
    {
      "emotion": "TRUST",
      "score": 0.6
    }
  ]
}
```

## 5. 业务规则管理API

### 5.1 获取所有业务规则

**接口地址：** `/api/business-rules`

**请求方式：** GET

**响应格式：**

```json
[
  {
    "id": 1,
    "name": "规则名称",
    "ruleType": "KEYWORD_MATCH",
    "enabled": true,
    "priority": 1,
    "description": "规则描述"
  }
]
```

### 5.2 分页查询业务规则

**接口地址：** `/api/business-rules/page`

**请求方式：** GET

**请求参数：**
- `page` (默认0): 页码
- `size` (默认10): 每页大小
- `sortBy` (默认id): 排序字段
- `sortDir` (默认desc): 排序方向
- `ruleType` (可选): 规则类型
- `enabled` (可选): 是否启用

**响应格式：**

```json
{
  "content": [
    {
      "id": 1,
      "name": "规则名称",
      "ruleType": "KEYWORD_MATCH",
      "enabled": true
    }
  ],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

### 5.3 创建业务规则

**接口地址：** `/api/business-rules`

**请求方式：** POST

**请求参数：**

```json
{
  "name": "新规则",
  "ruleType": "KEYWORD_MATCH",
  "enabled": true,
  "priority": 1,
  "description": "规则描述",
  "conditions": [
    {
      "field": "content",
      "operator": "CONTAINS",
      "value": "关键词"
    }
  ],
  "actions": [
    {
      "actionType": "FORWARD",
      "parameters": {
        "target": "admin@example.com"
      }
    }
  ]
}
```

**响应格式：**

```json
{
  "id": 1,
  "name": "新规则",
  "ruleType": "KEYWORD_MATCH",
  "enabled": true,
  "createdAt": "2024-01-01T10:00:00"
}
```

### 5.4 更新业务规则

**接口地址：** `/api/business-rules/{id}`

**请求方式：** PUT

**路径参数：**
- `id`: 规则ID

**请求参数：** 同创建规则

**响应格式：**

```json
{
  "id": 1,
  "name": "更新的规则",
  "ruleType": "KEYWORD_MATCH",
  "enabled": true,
  "updatedAt": "2024-01-01T10:00:00"
}
```

### 5.5 删除业务规则

**接口地址：** `/api/business-rules/{id}`

**请求方式：** DELETE

**路径参数：**
- `id`: 规则ID

**响应格式：**

```json
{
  "success": true,
  "message": "规则删除成功"
}
```

## 6. 监控模块API

### 6.1 获取仪表盘数据

**接口地址：** `/api/monitor/dashboard`

**请求方式：** GET

**响应格式：**

```json
{
  "success": true,
  "data": {
    "stats": {
      "totalMessages": 1000,
      "pendingMessages": 50,
      "processedMessages": 950,
      "forwardedMessages": 800
    },
    "messageTrend": {
      "dates": ["2024-01-01", "2024-01-02"],
      "values": [100, 120]
    },
    "systemStatus": {
      "cpuUsage": 45.5,
      "memoryUsage": 60.2,
      "diskUsage": 30.8,
      "status": "HEALTHY"
    }
  }
}
```

### 6.2 获取系统状态

**接口地址：** `/api/monitor/system`

**请求方式：** GET

**响应格式：**

```json
{
  "success": true,
  "system": {
    "cpu": {
      "usage": 45.5,
      "cores": 8
    },
    "memory": {
      "used": 4096,
      "total": 8192,
      "usage": 50.0
    },
    "disk": {
      "used": 100,
      "total": 500,
      "usage": 20.0
    },
    "uptime": 86400
  }
}
```

### 6.3 获取业务监控数据

**接口地址：** `/api/monitor/business`

**请求方式：** GET

**响应格式：**

```json
{
  "success": true,
  "business": {
    "messageProcessing": {
      "totalProcessed": 1000,
      "successRate": 0.95,
      "averageProcessingTime": 150
    },
    "keywordMatching": {
      "totalMatches": 200,
      "hitRate": 0.2,
      "topKeywords": [
        {
          "keyword": "投诉",
          "count": 50
        }
      ]
    }
  }
}
```

## 7. 用户管理API

### 7.1 用户登录

**接口地址：** `/api/user/login`

**请求方式：** POST

**请求参数：**

```json
{
  "username": "用户名",
  "password": "密码"
}
```

**响应格式：**

```json
{
  "success": true,
  "data": {
    "token": "jwt-token"
  },
  "message": "登录成功"
}
```

### 7.2 获取用户列表

**接口地址：** `/api/user/list`

**请求方式：** GET

**请求参数：**
- `page` (默认0): 页码
- `size` (默认10): 每页大小
- `username` (可选): 用户名筛选
- `status` (可选): 状态筛选

**响应格式：**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "username": "用户名",
        "realName": "真实姓名",
        "email": "邮箱",
        "status": "ACTIVE",
        "gridArea": "网格区域"
      }
    ],
    "totalElements": 100,
    "totalPages": 10
  }
}
```

### 7.3 获取用户详情

**接口地址：** `/api/user/{id}`

**请求方式：** GET

**路径参数：**
- `id`: 用户ID

**响应格式：**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "username": "用户名",
    "realName": "真实姓名",
    "email": "邮箱",
    "phone": "手机号",
    "status": "ACTIVE",
    "gridArea": "网格区域",
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

## 8. 系统配置API

### 8.1 获取所有系统配置

**接口地址：** `/api/system/config`

**请求方式：** GET

**响应格式：**

```json
[
  {
    "id": 1,
    "configKey": "system.name",
    "configValue": "电小助2.0",
    "description": "系统名称",
    "category": "SYSTEM"
  }
]
```

### 8.2 分页查询系统配置

**接口地址：** `/api/system/config/page`

**请求方式：** GET

**请求参数：**
- `page` (默认0): 页码
- `size` (默认10): 每页大小
- `sort` (默认id): 排序字段
- `direction` (默认asc): 排序方向

**响应格式：**

```json
{
  "content": [
    {
      "id": 1,
      "configKey": "system.name",
      "configValue": "电小助2.0",
      "description": "系统名称"
    }
  ],
  "totalElements": 50,
  "totalPages": 5,
  "size": 10,
  "number": 0
}
```

### 8.3 根据配置键获取配置

**接口地址：** `/api/system/config/key/{configKey}`

**请求方式：** GET

**路径参数：**
- `configKey`: 配置键名

**响应格式：**

```json
{
  "id": 1,
  "configKey": "system.name",
  "configValue": "电小助2.0",
  "description": "系统名称",
  "category": "SYSTEM"
}
```

## 9. 健康检查API

### 9.1 系统健康检查

**接口地址：** `/api/test-alive`

**请求方式：** GET

**响应格式：** `"alive"`

### 9.2 关键词增强模块健康检查

**接口地址：** `/api/keyword-enhancement/health`

**请求方式：** GET

**响应格式：**

```json
{
  "status": "UP",
  "timestamp": "2024-01-01T10:00:00",
  "services": {
    "learning": "UP",
    "sync": "UP",
    "analysis": "UP",
    "management": "UP",
    "trigger": "UP"
  }
}
```

## 10. 欢迎页面API

### 10.1 系统欢迎信息

**接口地址：** `/`

**请求方式：** GET

**响应格式：**

```json
{
  "message": "欢迎使用电小助 2.0 后端服务",
  "timestamp": "2024-01-01T10:00:00"
}
```

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权访问 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 通用响应格式

### 成功响应

```json
{
  "success": true,
  "data": {},
  "message": "操作成功"
}
```

### 错误响应

```json
{
  "success": false,
  "error": "错误信息",
  "message": "操作失败"
}
```

## 注意事项

1. 所有API接口都支持跨域访问（CORS）
2. 时间格式统一使用ISO 8601标准：`YYYY-MM-DDTHH:mm:ss`
3. 分页参数中，页码从0开始
4. 所有POST/PUT请求的Content-Type为`application/json`
5. 部分接口需要用户认证，请在请求头中携带有效的token
6. API响应中的success字段表示请求是否成功处理
7. 网格区域（gridArea）参数用于区分不同的地理区域或管理范围