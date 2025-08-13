# NLP处理模块API文档

## 概述

NLP处理模块提供了一系列API接口，用于对文本进行自然语言处理，包括文本预处理、意图识别、实体提取、情感分析、文本分类和关键信息提取等功能。

## 基础信息

- 基础路径：`/api/nlp`
- 请求方式：POST/GET
- 响应格式：JSON

## API接口列表

### 1. 文本处理接口

#### 1.1 处理文本

- 接口路径：`/api/nlp/process`
- 请求方式：POST
- 接口描述：对文本进行NLP处理，支持同步和异步处理模式

**请求参数：**

```json
{
  "messageId": 123456,  // 消息ID，必填
  "content": "我家停电了，请尽快处理",  // 文本内容，必填
  "processTypes": 63,  // 处理类型，必填，二进制位表示：1-预处理，2-意图识别，4-实体提取，8-情感分析，16-文本分类，32-关键信息提取，可组合使用
  "async": false  // 是否异步处理，选填，默认为false
}
```

**响应参数：**

```json
{
  "processId": 123,  // 处理ID
  "messageId": 123456,  // 消息ID
  "status": 1,  // 处理状态：0-处理中，1-处理完成，2-处理失败
  "preprocessResult": {  // 预处理结果，仅当processTypes包含1且处理完成时返回
    "originalText": "我家停电了，请尽快处理",
    "simplifiedText": "我家停电了，请尽快处理",
    "words": ["我", "家", "停电", "了", "，", "请", "尽快", "处理"],
    "wordsWithNature": [
      {"word": "我", "nature": "r"},
      {"word": "家", "nature": "n"},
      // ...
    ],
    "filteredWords": ["家", "停电", "尽快", "处理"],
    "cleanedText": "我家停电了 请尽快处理",
    "processedText": "我家停电了 请尽快处理"
  },
  "intentResult": {  // 意图识别结果，仅当processTypes包含2且处理完成时返回
    "primaryIntent": "故障报修",
    "confidence": 0.9,
    "intentScores": {
      "故障报修": 0.9,
      "业务查询": 0.2,
      // ...
    }
  },
  "entityResult": {  // 实体提取结果，仅当processTypes包含4且处理完成时返回
    "entities": [
      {
        "type": "DEVICE_ID",
        "value": "DX12345",
        "position": "10"
      },
      // ...
    ],
    "count": 1
  },
  "sentimentResult": {  // 情感分析结果，仅当processTypes包含8且处理完成时返回
    "sentiment": "负面",
    "score": 0.8,
    "positiveCount": 0,
    "negativeCount": 2,
    "matchedPositiveWords": [],
    "matchedNegativeWords": ["停电", "处理"],
    "riskWarning": true,
    "riskLevel": "中",
    "riskMessage": "检测到明显负面情绪，建议及时关注"
  },
  "classificationResult": {  // 文本分类结果，仅当processTypes包含16且处理完成时返回
    "primaryCategory": "故障报修",
    "confidence": 0.9,
    "categories": [
      {"category": "故障报修", "score": 0.9},
      {"category": "业务办理", "score": 0.3},
      // ...
    ],
    "multiLabels": ["故障报修", "业务办理"]
  },
  "keyInfoResult": {  // 关键信息提取结果，仅当processTypes包含32且处理完成时返回
    "keywords": ["停电", "处理", "尽快"],
    "summary": ["我家停电了，请尽快处理"],
    "timeInfo": [],
    "locationInfo": [],
    "personInfo": [],
    "deviceInfo": [],
    "numberInfo": []
  },
  "processDuration": 120,  // 处理耗时（毫秒），仅当处理完成时返回
  "errorMessage": null  // 错误信息，仅当处理失败时返回
}
```

#### 1.2 获取处理结果

- 接口路径：`/api/nlp/result/{processId}`
- 请求方式：GET
- 接口描述：根据处理ID获取处理结果，用于异步处理模式下查询处理结果

**请求参数：**

- processId：处理ID，路径参数

**响应参数：**

与处理文本接口的响应参数相同

#### 1.3 根据消息ID获取处理结果

- 接口路径：`/api/nlp/result/message/{messageId}`
- 请求方式：GET
- 接口描述：根据消息ID获取处理结果

**请求参数：**

- messageId：消息ID，路径参数

**响应参数：**

与处理文本接口的响应参数相同

### 2. 单项处理接口

#### 2.1 文本预处理

- 接口路径：`/api/nlp/preprocess`
- 请求方式：POST
- 接口描述：对文本进行预处理

**请求参数：**

请求体为文本内容

**响应参数：**

与处理文本接口的preprocessResult字段相同

#### 2.2 意图识别

- 接口路径：`/api/nlp/intent`
- 请求方式：POST
- 接口描述：对文本进行意图识别

**请求参数：**

请求体为文本内容

**响应参数：**

与处理文本接口的intentResult字段相同

#### 2.3 实体提取

- 接口路径：`/api/nlp/entity`
- 请求方式：POST
- 接口描述：对文本进行实体提取

**请求参数：**

请求体为文本内容

**响应参数：**

与处理文本接口的entityResult字段相同

#### 2.4 情感分析

- 接口路径：`/api/nlp/sentiment`
- 请求方式：POST
- 接口描述：对文本进行情感分析

**请求参数：**

请求体为文本内容

**响应参数：**

与处理文本接口的sentimentResult字段相同

#### 2.5 文本分类

- 接口路径：`/api/nlp/classify`
- 请求方式：POST
- 接口描述：对文本进行分类

**请求参数：**

请求体为文本内容

**响应参数：**

与处理文本接口的classificationResult字段相同

#### 2.6 关键信息提取

- 接口路径：`/api/nlp/keyinfo`
- 请求方式：POST
- 接口描述：提取文本中的关键信息

**请求参数：**

请求体为文本内容

**响应参数：**

与处理文本接口的keyInfoResult字段相同

### 3. 配置管理接口

#### 3.1 添加NLP配置

- 接口路径：`/api/nlp/config`
- 请求方式：POST
- 接口描述：添加NLP配置

**请求参数：**

```json
{
  "type": 1,  // 配置类型：1-预处理配置，2-意图识别配置，3-实体提取配置，4-情感分析配置，5-文本分类配置，6-关键信息提取配置
  "name": "自定义预处理配置",  // 配置名称
  "content": "{\"enableSimplifiedConversion\":true,\"enableSegmentation\":true,\"enableStopWordsFiltering\":false,\"enableTextCleaning\":true}",  // 配置内容（JSON格式）
  "enabled": true,  // 是否启用
  "priority": 1,  // 优先级（数字越小优先级越高）
  "remark": "自定义的文本预处理配置"  // 备注
}
```

**响应参数：**

```json
{
  "id": 2,
  "type": 1,
  "name": "自定义预处理配置",
  "content": "{\"enableSimplifiedConversion\":true,\"enableSegmentation\":true,\"enableStopWordsFiltering\":false,\"enableTextCleaning\":true}",
  "enabled": true,
  "priority": 1,
  "creatorId": 1,
  "updaterId": 1,
  "createTime": "2023-06-01T10:00:00",
  "updateTime": "2023-06-01T10:00:00",
  "remark": "自定义的文本预处理配置"
}
```

#### 3.2 更新NLP配置

- 接口路径：`/api/nlp/config/{id}`
- 请求方式：PUT
- 接口描述：更新NLP配置

**请求参数：**

- id：配置ID，路径参数
- 请求体参数与添加NLP配置接口相同

**响应参数：**

与添加NLP配置接口的响应参数相同

#### 3.3 删除NLP配置

- 接口路径：`/api/nlp/config/{id}`
- 请求方式：DELETE
- 接口描述：删除NLP配置

**请求参数：**

- id：配置ID，路径参数

**响应参数：**

```json
true  // 删除成功返回true，失败返回false
```

#### 3.4 获取NLP配置

- 接口路径：`/api/nlp/config/{id}`
- 请求方式：GET
- 接口描述：获取NLP配置

**请求参数：**

- id：配置ID，路径参数

**响应参数：**

与添加NLP配置接口的响应参数相同

#### 3.5 获取所有NLP配置

- 接口路径：`/api/nlp/config`
- 请求方式：GET
- 接口描述：获取所有NLP配置

**请求参数：**

无

**响应参数：**

```json
[
  {
    "id": 1,
    "type": 1,
    "name": "默认预处理配置",
    "content": "{\"enableSimplifiedConversion\":true,\"enableSegmentation\":true,\"enableStopWordsFiltering\":true,\"enableTextCleaning\":true}",
    "enabled": true,
    "priority": 0,
    "creatorId": 1,
    "updaterId": 1,
    "createTime": "2023-06-01T10:00:00",
    "updateTime": "2023-06-01T10:00:00",
    "remark": "默认的文本预处理配置"
  },
  // ...
]
```

#### 3.6 根据类型获取NLP配置

- 接口路径：`/api/nlp/config/type/{type}`
- 请求方式：GET
- 接口描述：根据类型获取NLP配置

**请求参数：**

- type：配置类型，路径参数

**响应参数：**

与获取所有NLP配置接口的响应参数相同，但仅包含指定类型的配置

#### 3.7 根据类型和启用状态获取NLP配置

- 接口路径：`/api/nlp/config/type/{type}/enabled/{enabled}`
- 请求方式：GET
- 接口描述：根据类型和启用状态获取NLP配置

**请求参数：**

- type：配置类型，路径参数
- enabled：启用状态，路径参数，true或false

**响应参数：**

与获取所有NLP配置接口的响应参数相同，但仅包含指定类型和启用状态的配置

## 错误码说明

| 错误码 | 说明 |
| --- | --- |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 使用示例

### 示例1：处理文本（同步模式）

**请求：**

```http
POST /api/nlp/process
Content-Type: application/json

{
  "messageId": 123456,
  "content": "我家停电了，请尽快处理",
  "processTypes": 63,
  "async": false
}
```

**响应：**

```json
{
  "processId": 123,
  "messageId": 123456,
  "status": 1,
  "preprocessResult": {
    "originalText": "我家停电了，请尽快处理",
    "simplifiedText": "我家停电了，请尽快处理",
    "words": ["我", "家", "停电", "了", "，", "请", "尽快", "处理"],
    "wordsWithNature": [
      {"word": "我", "nature": "r"},
      {"word": "家", "nature": "n"},
      {"word": "停电", "nature": "v"},
      {"word": "了", "nature": "u"},
      {"word": "，", "nature": "w"},
      {"word": "请", "nature": "v"},
      {"word": "尽快", "nature": "d"},
      {"word": "处理", "nature": "v"}
    ],
    "filteredWords": ["家", "停电", "尽快", "处理"],
    "cleanedText": "我家停电了 请尽快处理",
    "processedText": "我家停电了 请尽快处理"
  },
  "intentResult": {
    "primaryIntent": "故障报修",
    "confidence": 0.9,
    "intentScores": {
      "故障报修": 0.9,
      "业务查询": 0.2
    }
  },
  "entityResult": {
    "entities": [],
    "count": 0
  },
  "sentimentResult": {
    "sentiment": "负面",
    "score": 0.8,
    "positiveCount": 0,
    "negativeCount": 2,
    "matchedPositiveWords": [],
    "matchedNegativeWords": ["停电", "处理"],
    "riskWarning": true,
    "riskLevel": "中",
    "riskMessage": "检测到明显负面情绪，建议及时关注"
  },
  "classificationResult": {
    "primaryCategory": "故障报修",
    "confidence": 0.9,
    "categories": [
      {"category": "故障报修", "score": 0.9},
      {"category": "业务办理", "score": 0.3}
    ],
    "multiLabels": ["故障报修", "业务办理"]
  },
  "keyInfoResult": {
    "keywords": ["停电", "处理", "尽快"],
    "summary": ["我家停电了，请尽快处理"],
    "timeInfo": [],
    "locationInfo": [],
    "personInfo": [],
    "deviceInfo": [],
    "numberInfo": []
  },
  "processDuration": 120
}
```

### 示例2：处理文本（异步模式）

**请求：**

```http
POST /api/nlp/process
Content-Type: application/json

{
  "messageId": 123456,
  "content": "我家停电了，请尽快处理",
  "processTypes": 63,
  "async": true
}
```

**响应：**

```json
{
  "processId": 123,
  "messageId": 123456,
  "status": 0
}
```

**获取处理结果：**

```http
GET /api/nlp/result/123
```

**响应：**

与同步模式的响应相同

### 示例3：添加NLP配置

**请求：**

```http
POST /api/nlp/config
Content-Type: application/json

{
  "type": 1,
  "name": "自定义预处理配置",
  "content": "{\"enableSimplifiedConversion\":true,\"enableSegmentation\":true,\"enableStopWordsFiltering\":false,\"enableTextCleaning\":true}",
  "enabled": true,
  "priority": 1,
  "remark": "自定义的文本预处理配置"
}
```

**响应：**

```json
{
  "id": 2,
  "type": 1,
  "name": "自定义预处理配置",
  "content": "{\"enableSimplifiedConversion\":true,\"enableSegmentation\":true,\"enableStopWordsFiltering\":false,\"enableTextCleaning\":true}",
  "enabled": true,
  "priority": 1,
  "creatorId": 1,
  "updaterId": 1,
  "createTime": "2023-06-01T10:00:00",
  "updateTime": "2023-06-01T10:00:00",
  "remark": "自定义的文本预处理配置"
}
```