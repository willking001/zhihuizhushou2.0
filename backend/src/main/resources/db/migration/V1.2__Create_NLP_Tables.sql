-- NLP配置表
CREATE TABLE IF NOT EXISTS nlp_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type INT NOT NULL COMMENT '配置类型：1-预处理配置，2-意图识别配置，3-实体提取配置，4-情感分析配置，5-文本分类配置，6-关键信息提取配置',
    name VARCHAR(100) NOT NULL COMMENT '配置名称',
    content TEXT NOT NULL COMMENT '配置内容（JSON格式）',
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    priority INT NOT NULL DEFAULT 0 COMMENT '优先级（数字越小优先级越高）',
    creator_id BIGINT COMMENT '创建人ID',
    updater_id BIGINT COMMENT '更新人ID',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_type (type),
    INDEX idx_type_enabled (type, enabled),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NLP配置表';

-- NLP处理结果表
CREATE TABLE IF NOT EXISTS nlp_result (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    message_id BIGINT NOT NULL COMMENT '关联消息ID',
    preprocess_result TEXT COMMENT '预处理结果（JSON格式）',
    intent_result TEXT COMMENT '意图识别结果（JSON格式）',
    entity_result TEXT COMMENT '实体提取结果（JSON格式）',
    sentiment_result TEXT COMMENT '情感分析结果（JSON格式）',
    classification_result TEXT COMMENT '文本分类结果（JSON格式）',
    key_info_result TEXT COMMENT '关键信息提取结果（JSON格式）',
    status INT NOT NULL DEFAULT 0 COMMENT '处理状态：0-处理中，1-处理完成，2-处理失败',
    process_time DATETIME NOT NULL COMMENT '处理时间',
    process_duration BIGINT COMMENT '处理耗时（毫秒）',
    error_message VARCHAR(1000) COMMENT '错误信息',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    INDEX idx_message_id (message_id),
    INDEX idx_status (status),
    INDEX idx_process_time (process_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='NLP处理结果表';

-- 插入默认NLP配置数据
INSERT INTO nlp_config (type, name, content, enabled, priority, creator_id, updater_id, create_time, update_time, remark)
VALUES
-- 预处理配置
(1, '默认预处理配置', '{"enableSimplifiedConversion":true,"enableSegmentation":true,"enableStopWordsFiltering":true,"enableTextCleaning":true}', TRUE, 0, 1, 1, NOW(), NOW(), '默认的文本预处理配置'),

-- 意图识别配置
(2, '默认意图识别配置', '{"intentCategories":["业务查询","故障报修","投诉建议","缴费咨询","其他"],"confidenceThreshold":0.6,"enableMultiIntent":true}', TRUE, 0, 1, 1, NOW(), NOW(), '默认的意图识别配置'),

-- 实体提取配置
(3, '默认实体提取配置', '{"enabledEntityTypes":["PERSON","LOCATION","ORGANIZATION","TIME","PHONE","DEVICE_ID"],"confidenceThreshold":0.6}', TRUE, 0, 1, 1, NOW(), NOW(), '默认的实体提取配置'),

-- 情感分析配置
(4, '默认情感分析配置', '{"sentimentCategories":["正面","负面","中性"],"enableRiskWarning":true,"riskThreshold":0.7}', TRUE, 0, 1, 1, NOW(), NOW(), '默认的情感分析配置'),

-- 文本分类配置
(5, '默认文本分类配置', '{"categories":["用电报装","电费查询","故障报修","业务办理","投诉建议","其他"],"confidenceThreshold":0.6,"enableMultiLabel":true,"multiLabelThreshold":0.3}', TRUE, 0, 1, 1, NOW(), NOW(), '默认的文本分类配置'),

-- 关键信息提取配置
(6, '默认关键信息提取配置', '{"enableKeywords":true,"enableSummary":true,"enableTimeInfo":true,"enableLocationInfo":true,"enablePersonInfo":true,"enableDeviceInfo":true,"enableNumberInfo":true,"keywordsCount":5,"summaryCount":3}', TRUE, 0, 1, 1, NOW(), NOW(), '默认的关键信息提取配置');