-- 创建系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键名',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(500) COMMENT '配置描述',
    config_type VARCHAR(50) DEFAULT 'STRING' COMMENT '配置类型：STRING, INTEGER, BOOLEAN, JSON',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator_id BIGINT COMMENT '创建者ID',
    updater_id BIGINT COMMENT '更新者ID',
    remark VARCHAR(500) COMMENT '备注'
) COMMENT='系统配置表';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_config_key ON system_config(config_key);
CREATE INDEX IF NOT EXISTS idx_config_type ON system_config(config_type);
CREATE INDEX IF NOT EXISTS idx_enabled ON system_config(enabled);

-- 插入默认配置数据（使用INSERT IGNORE避免重复插入）
INSERT IGNORE INTO system_config (config_key, config_value, description, config_type, enabled) VALUES
('keyword.trigger.threshold', '5', '关键词触发阈值：客户端本地关键词触发多少次后上传到服务器', 'INTEGER', TRUE),
('system.maintenance.mode', 'false', '系统维护模式开关', 'BOOLEAN', TRUE),
('message.max.length', '1000', '消息最大长度限制', 'INTEGER', TRUE),
('file.upload.max.size', '10485760', '文件上传最大大小（字节）', 'INTEGER', TRUE),
('session.timeout', '3600', '会话超时时间（秒）', 'INTEGER', TRUE);