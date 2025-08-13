-- 添加电力领域相关的NLP配置
INSERT INTO nlp_config (type, name, content, enabled, priority, creator_id, updater_id, create_time, update_time, remark)
VALUES
-- 电力领域意图识别配置
(2, '电力领域意图识别配置', '{
  "intentDefinitions": [
    {
      "intent": "电力故障报修",
      "keywords": ["停电", "跳闸", "断电", "电表故障", "线路故障", "供电故障", "电压不稳", "电流异常", "短路", "漏电"],
      "description": "用户报告电力设备或供电线路故障"
    },
    {
      "intent": "电费查询",
      "keywords": ["电费", "电费查询", "电费账单", "用电量", "电表读数", "电费余额", "电费缴纳", "电费计算", "电价", "峰谷电价"],
      "description": "用户查询电费相关信息"
    },
    {
      "intent": "用电报装",
      "keywords": ["报装", "新装", "增容", "装表", "接电", "用电申请", "临时用电", "永久用电", "电力接入", "电表安装"],
      "description": "用户申请新装或增容用电服务"
    },
    {
      "intent": "电力设备咨询",
      "keywords": ["变压器", "配电箱", "电表", "智能电表", "高压线", "低压线", "电缆", "开关", "断路器", "保险丝"],
      "description": "用户咨询电力设备相关信息"
    },
    {
      "intent": "电力安全咨询",
      "keywords": ["电力安全", "安全用电", "漏电", "触电", "电气火灾", "安全隐患", "防雷", "绝缘", "安全距离", "电力设施保护"],
      "description": "用户咨询电力安全相关信息"
    },
    {
      "intent": "电力政策咨询",
      "keywords": ["电力政策", "电价政策", "阶梯电价", "峰谷电价", "居民用电", "商业用电", "工业用电", "农业用电", "补贴政策", "电力补贴"],
      "description": "用户咨询电力政策相关信息"
    }
  ],
  "confidenceThreshold": 0.6,
  "enableMultiIntent": true
}', TRUE, 5, 1, 1, NOW(), NOW(), '电力领域的意图识别配置，包含电力故障报修、电费查询、用电报装等意图'),

-- 电力领域实体提取配置
(3, '电力领域实体提取配置', '{
  "enableNER": true,
  "enableRegexExtraction": true,
  "customEntityPatterns": [
    {
      "entityType": "DEVICE_ID",
      "pattern": "[A-Z]{2}\\d{8}",
      "description": "变压器设备编号，如AB12345678"
    },
    {
      "entityType": "METER_ID",
      "pattern": "\\d{10}",
      "description": "电表编号，10位数字"
    },
    {
      "entityType": "POWER_POLE",
      "pattern": "[A-Z]{1,2}\\d{3,4}",
      "description": "电力杆塔编号，如A123、AB1234"
    },
    {
      "entityType": "VOLTAGE",
      "pattern": "\\d+(\\.\\d+)?[kK][vV]",
      "description": "电压值，如10kV、35kV、110kV、220kV"
    },
    {
      "entityType": "POWER",
      "pattern": "\\d+(\\.\\d+)?[kK][wW]",
      "description": "功率值，如5kW、10kW、100kW"
    },
    {
      "entityType": "CURRENT",
      "pattern": "\\d+(\\.\\d+)?[aA]",
      "description": "电流值，如5A、10A、100A"
    },
    {
      "entityType": "POWER_CONSUMPTION",
      "pattern": "\\d+(\\.\\d+)?[kK][wW][hH]",
      "description": "用电量，如100kWh、1000kWh"
    },
    {
      "entityType": "SUBSTATION",
      "pattern": "[\\u4e00-\\u9fa5]+(变电站|开关站|配电室)",
      "description": "变电站名称，如城南变电站"
    },
    {
      "entityType": "LINE",
      "pattern": "[\\u4e00-\\u9fa5]+(线路|线|支线)",
      "description": "线路名称，如城南线路、1号线"
    }
  ],
  "confidenceThreshold": 0.6
}', TRUE, 5, 1, 1, NOW(), NOW(), '电力领域的实体提取配置，包含设备编号、电表编号、电压值等实体类型'),

-- 电力领域文本分类配置
(5, '电力领域文本分类配置', '{
  "categories": [
    {
      "category": "供电服务",
      "keywords": ["报装", "新装", "增容", "接电", "装表", "用电申请", "临时用电", "永久用电"],
      "description": "用电报装、增容等供电服务类问题"
    },
    {
      "category": "电费业务",
      "keywords": ["电费", "账单", "缴费", "电价", "阶梯电价", "峰谷电价", "电费查询", "用电量", "电表读数"],
      "description": "电费查询、缴纳等电费相关业务"
    },
    {
      "category": "故障报修",
      "keywords": ["停电", "跳闸", "断电", "故障", "电压不稳", "电流异常", "短路", "漏电", "闪烁", "抖动"],
      "description": "电力故障报修类问题"
    },
    {
      "category": "设备维护",
      "keywords": ["变压器", "配电箱", "电表", "线路", "电缆", "开关", "断路器", "保险丝", "维修", "检修"],
      "description": "电力设备维护类问题"
    },
    {
      "category": "安全用电",
      "keywords": ["安全", "漏电", "触电", "火灾", "隐患", "防雷", "绝缘", "安全距离", "保护", "防护"],
      "description": "电力安全相关问题"
    },
    {
      "category": "政策咨询",
      "keywords": ["政策", "规定", "标准", "补贴", "优惠", "减免", "居民用电", "商业用电", "工业用电", "农业用电"],
      "description": "电力政策咨询类问题"
    },
    {
      "category": "投诉建议",
      "keywords": ["投诉", "建议", "意见", "不满", "态度", "服务质量", "处理不及时", "收费不合理", "服务态度", "服务效率"],
      "description": "对电力服务的投诉和建议"
    },
    {
      "category": "节能减排",
      "keywords": ["节能", "节电", "减排", "绿色能源", "可再生能源", "光伏", "风电", "智能用电", "能效", "碳中和"],
      "description": "节能减排、绿色能源相关问题"
    }
  ],
  "confidenceThreshold": 0.6,
  "enableMultiLabel": true,
  "multiLabelThreshold": 0.3
}', TRUE, 5, 1, 1, NOW(), NOW(), '电力领域的文本分类配置，包含供电服务、电费业务、故障报修等类别'),

-- 电力领域关键信息提取配置
(6, '电力领域关键信息提取配置', '{
  "enableKeywordExtraction": true,
  "enableSummaryExtraction": true,
  "enableTimeExtraction": true,
  "enableLocationExtraction": true,
  "enablePersonExtraction": true,
  "enableDeviceExtraction": true,
  "enableNumberExtraction": true,
  "keywordCount": 10,
  "summaryCount": 3,
  "customPatterns": [
    {
      "infoType": "DEVICE",
      "pattern": "变压器|配电箱|电表|智能电表|高压线|低压线|电缆|开关|断路器|保险丝|避雷器|绝缘子|电力杆塔|配电柜|环网柜|分接箱",
      "description": "电力设备名称"
    },
    {
      "infoType": "FAULT",
      "pattern": "停电|跳闸|断电|故障|电压不稳|电流异常|短路|漏电|闪烁|抖动|过载|过热|烧毁|损坏|老化|腐蚀|松动|脱落",
      "description": "电力故障类型"
    },
    {
      "infoType": "LOCATION",
      "pattern": "变电站|开关站|配电室|配电所|电力杆|电线杆|电缆沟|电缆井|电缆隧道|架空线|地下电缆|电力走廊",
      "description": "电力设施位置"
    },
    {
      "infoType": "BUSINESS",
      "pattern": "报装|新装|增容|接电|装表|用电申请|临时用电|永久用电|电费|账单|缴费|电价|阶梯电价|峰谷电价",
      "description": "电力业务类型"
    },
    {
      "infoType": "SAFETY",
      "pattern": "安全|漏电|触电|火灾|隐患|防雷|绝缘|安全距离|保护|防护|警示|警告|危险|应急|防范|预防",
      "description": "电力安全相关信息"
    }
  ]
}', TRUE, 5, 1, 1, NOW(), NOW(), '电力领域的关键信息提取配置，包含电力设备、故障类型、设施位置等自定义提取模式');