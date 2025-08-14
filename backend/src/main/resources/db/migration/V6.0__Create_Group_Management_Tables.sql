-- 群组管理功能数据库迁移脚本
-- 版本: V6.0
-- 描述: 创建群组管理相关表结构和初始化数据

-- 1. 扩展messages表，添加群组管理相关字段
ALTER TABLE messages 
ADD COLUMN group_status VARCHAR(20) DEFAULT 'AUTO' COMMENT '群组状态: AUTO/MANUAL/PAUSED',
ADD COLUMN takeover_trigger VARCHAR(50) COMMENT '接管触发类型: KEYWORD/SENTIMENT/REPETITION',
ADD COLUMN sentiment_score DECIMAL(3,2) COMMENT '情感分析得分',
ADD COLUMN repetition_count INT DEFAULT 0 COMMENT '重复询问计数',
ADD COLUMN manual_handled BOOLEAN DEFAULT FALSE COMMENT '是否已人工处理',
ADD COLUMN handled_by VARCHAR(50) COMMENT '处理人员',
ADD COLUMN handled_time DATETIME COMMENT '处理时间';

-- 添加索引
CREATE INDEX idx_messages_chat_room_status ON messages(chat_room, group_status);
CREATE INDEX idx_messages_takeover_trigger ON messages(takeover_trigger);
CREATE INDEX idx_messages_manual_handled ON messages(manual_handled);

-- 2. 创建群组状态管理表
CREATE TABLE group_management_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chat_room VARCHAR(200) NOT NULL UNIQUE COMMENT '群组标识（对应messages表的chat_room）',
    group_name VARCHAR(200) COMMENT '群组名称',
    current_status VARCHAR(20) DEFAULT 'AUTO' COMMENT '当前状态: AUTO/MANUAL/PAUSED',
    auto_reply_enabled BOOLEAN DEFAULT TRUE COMMENT '自动回复开关',
    auto_forward_enabled BOOLEAN DEFAULT TRUE COMMENT '自动转发开关',
    takeover_reason VARCHAR(500) COMMENT '最近接管原因',
    takeover_time DATETIME COMMENT '最近接管时间',
    takeover_by VARCHAR(50) COMMENT '接管人员',
    grid_officer_id VARCHAR(50) COMMENT '分配的网格员ID',
    message_count_today INT DEFAULT 0 COMMENT '今日消息数',
    auto_reply_count_today INT DEFAULT 0 COMMENT '今日自动回复数',
    takeover_count_today INT DEFAULT 0 COMMENT '今日接管次数',
    last_activity_time DATETIME COMMENT '最后活跃时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_chat_room (chat_room),
    INDEX idx_current_status (current_status),
    INDEX idx_last_activity (last_activity_time)
);

-- 3. 扩展keyword_configs表，添加群组管理相关字段
ALTER TABLE keyword_configs 
ADD COLUMN group_id VARCHAR(100) COMMENT '关联群组ID',
ADD COLUMN action_type VARCHAR(50) COMMENT '动作类型: AUTO_REPLY/FORWARD/TAKEOVER';

-- 添加索引
CREATE INDEX idx_keyword_configs_group_id ON keyword_configs(group_id);
CREATE INDEX idx_keyword_configs_action_type ON keyword_configs(action_type);

-- 4. 在system_config表中添加群组管理相关配置
INSERT INTO system_config (config_key, config_value, description, config_type, enabled, create_time, update_time, creator_id, updater_id) VALUES
('group.auto.reply.enabled', 'true', '群组自动回复总开关', 'GROUP_MANAGEMENT', true, NOW(), NOW(), 1, 1),
('group.auto.forward.enabled', 'true', '群组自动转发总开关', 'GROUP_MANAGEMENT', true, NOW(), NOW(), 1, 1),
('group.sentiment.threshold', '-0.5', '情感分析触发阈值', 'GROUP_MANAGEMENT', true, NOW(), NOW(), 1, 1),
('group.repetition.threshold', '3', '重复询问触发阈值', 'GROUP_MANAGEMENT', true, NOW(), NOW(), 1, 1),
('group.notification.phone', '', '默认通知手机号', 'GROUP_MANAGEMENT', true, NOW(), NOW(), 1, 1),
('group.notification.email', '', '默认通知邮箱', 'GROUP_MANAGEMENT', true, NOW(), NOW(), 1, 1),
('group.takeover.keywords', '["人工","客服","投诉","不满意"]', '人工接管触发关键词', 'GROUP_MANAGEMENT', true, NOW(), NOW(), 1, 1);

-- 5. 添加群组关键词配置示例
INSERT INTO keyword_configs (keyword, type, priority, description, grid_area, is_active, created_by, created_at, updated_at, group_id, action_type) VALUES
('人工客服', 'CUSTOM', 'HIGH', '触发人工接管', NULL, true, 1, NOW(), NOW(), NULL, 'TAKEOVER'),
('投诉', 'CUSTOM', 'URGENT', '投诉关键词触发人工接管', NULL, true, 1, NOW(), NOW(), NULL, 'TAKEOVER'),
('不满意', 'CUSTOM', 'HIGH', '不满意情绪触发人工接管', NULL, true, 1, NOW(), NOW(), NULL, 'TAKEOVER'),
('价格咨询', 'CUSTOM', 'NORMAL', '价格相关自动回复', NULL, true, 1, NOW(), NOW(), NULL, 'AUTO_REPLY');

-- 6. 创建群组统计视图（用于快速查询统计数据）
CREATE VIEW group_statistics_view AS
SELECT 
    m.chat_room,
    gms.group_name,
    gms.current_status,
    COUNT(m.id) as total_messages,
    COUNT(CASE WHEN m.is_forwarded = true THEN 1 END) as forwarded_messages,
    COUNT(CASE WHEN m.manual_handled = true THEN 1 END) as manual_handled_messages,
    COUNT(CASE WHEN m.takeover_trigger IS NOT NULL THEN 1 END) as takeover_triggered_messages,
    AVG(m.sentiment_score) as avg_sentiment_score,
    MAX(m.created_at) as last_message_time
FROM messages m
LEFT JOIN group_management_status gms ON m.chat_room = gms.chat_room
WHERE m.is_group = true
GROUP BY m.chat_room, gms.group_name, gms.current_status;

-- 7. 创建群组日统计表（用于存储每日统计数据）
CREATE TABLE group_daily_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    chat_room VARCHAR(200) NOT NULL,
    stat_date DATE NOT NULL,
    message_count INT DEFAULT 0 COMMENT '消息总数',
    auto_reply_count INT DEFAULT 0 COMMENT '自动回复数',
    manual_takeover_count INT DEFAULT 0 COMMENT '人工接管次数',
    avg_response_time DECIMAL(5,2) DEFAULT 0 COMMENT '平均响应时间（秒）',
    satisfaction_score DECIMAL(3,2) DEFAULT 0 COMMENT '满意度评分',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_chat_room_date (chat_room, stat_date),
    INDEX idx_stat_date (stat_date),
    INDEX idx_chat_room (chat_room)
);

COMMIT;