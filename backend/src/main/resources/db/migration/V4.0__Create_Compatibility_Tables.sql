-- 创建关键词兼容模式相关表

-- 关键词触发日志表
CREATE TABLE IF NOT EXISTS keyword_trigger_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword_id BIGINT NOT NULL COMMENT '关键词配置ID',
    trigger_context TEXT COMMENT '触发上下文',
    triggered_by BIGINT COMMENT '触发用户ID',
    trigger_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '触发时间',
    
    INDEX idx_keyword_id (keyword_id),
    INDEX idx_triggered_by (triggered_by),
    INDEX idx_trigger_time (trigger_time),
    
    FOREIGN KEY (keyword_id) REFERENCES keyword_configs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关键词触发日志表';

-- 服务器提交表
CREATE TABLE IF NOT EXISTS keyword_server_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(255) NOT NULL COMMENT '关键词',
    grid_area VARCHAR(100) COMMENT '网格区域',
    client_keyword_id BIGINT NOT NULL COMMENT '客户端关键词ID',
    trigger_count INT NOT NULL DEFAULT 0 COMMENT '触发次数',
    submitted_by BIGINT NOT NULL COMMENT '提交者ID',
    submission_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING' COMMENT '审核状态',
    reviewed_by BIGINT COMMENT '审核者ID',
    review_time DATETIME COMMENT '审核时间',
    review_notes TEXT COMMENT '审核备注',
    
    INDEX idx_keyword (keyword),
    INDEX idx_grid_area (grid_area),
    INDEX idx_client_keyword_id (client_keyword_id),
    INDEX idx_submitted_by (submitted_by),
    INDEX idx_submission_time (submission_time),
    INDEX idx_status (status),
    INDEX idx_reviewed_by (reviewed_by),
    INDEX idx_review_time (review_time),
    
    FOREIGN KEY (client_keyword_id) REFERENCES keyword_configs(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务器提交表';

-- 为现有关键词配置表添加兼容模式字段的默认值
UPDATE keyword_configs SET 
    weight = CASE 
        WHEN type = 'GLOBAL' THEN 1
        WHEN type = 'LOCAL' THEN 2
        ELSE 2
    END
WHERE weight IS NULL;

UPDATE keyword_configs SET 
    source_type = CASE 
        WHEN type = 'GLOBAL' THEN 'SERVER'
        WHEN type = 'LOCAL' THEN 'CLIENT'
        ELSE 'CLIENT'
    END
WHERE source_type IS NULL;

UPDATE keyword_configs SET 
    trigger_threshold = 5
WHERE trigger_threshold IS NULL;

-- 创建兼容模式配置表
CREATE TABLE IF NOT EXISTS keyword_compatibility_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值',
    description TEXT COMMENT '配置描述',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兼容模式配置表';

-- 插入默认兼容模式配置
INSERT INTO keyword_compatibility_config (config_key, config_value, description) VALUES
('compatibility.enabled', 'true', '是否启用兼容模式'),
('compatibility.default_threshold', '5', '默认触发阈值'),
('compatibility.auto_approve', 'false', '是否自动审核通过'),
('compatibility.server_weight', '1', '服务器关键词权重'),
('compatibility.client_weight', '2', '客户端关键词权重'),
('compatibility.max_pending_submissions', '100', '最大待审核提交数量'),
('compatibility.submission_cooldown_hours', '24', '提交冷却时间（小时）')
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    description = VALUES(description),
    updated_at = CURRENT_TIMESTAMP;