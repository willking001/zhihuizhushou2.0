-- 关键词功能增强表结构
-- 版本: V3.0
-- 描述: 添加智能学习、同步机制、分析统计等功能所需的表

-- 1. 关键词学习记录表
CREATE TABLE IF NOT EXISTS keyword_learning_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(255) NOT NULL COMMENT '关键词',
    message_content TEXT COMMENT '消息内容',
    grid_area VARCHAR(100) COMMENT '网格区域',
    user_id BIGINT COMMENT '用户ID',
    detection_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
    confidence_score DECIMAL(3,2) COMMENT '置信度分数',
    context_info JSON COMMENT '上下文信息',
    INDEX idx_keyword (keyword),
    INDEX idx_grid_area (grid_area),
    INDEX idx_detection_time (detection_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关键词学习记录表';

-- 2. 学习配置表
CREATE TABLE IF NOT EXISTS keyword_learning_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grid_area VARCHAR(100) COMMENT '网格区域',
    min_frequency INT DEFAULT 3 COMMENT '最小频率',
    learning_window_days INT DEFAULT 7 COMMENT '学习窗口天数',
    confidence_threshold DECIMAL(3,2) DEFAULT 0.7 COMMENT '置信度阈值',
    max_learned_keywords INT DEFAULT 100 COMMENT '最大学习关键词数量',
    auto_promote BOOLEAN DEFAULT TRUE COMMENT '自动提升',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习配置表';

-- 3. 学习推荐表
CREATE TABLE IF NOT EXISTS keyword_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(255) NOT NULL COMMENT '关键词',
    frequency INT COMMENT '频率',
    confidence_score DECIMAL(3,2) COMMENT '置信度分数',
    grid_area VARCHAR(100) COMMENT '网格区域',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '状态',
    recommended_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '推荐时间',
    reviewed_at TIMESTAMP NULL COMMENT '审核时间',
    reviewed_by BIGINT COMMENT '审核人',
    review_notes TEXT COMMENT '审核备注',
    INDEX idx_status (status),
    INDEX idx_grid_area (grid_area)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习推荐表';

-- 4. 同步日志表
CREATE TABLE IF NOT EXISTS keyword_sync_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL COMMENT '客户端ID',
    sync_type ENUM('FULL', 'INCREMENTAL') NOT NULL COMMENT '同步类型',
    sync_direction ENUM('UPLOAD', 'DOWNLOAD', 'BIDIRECTIONAL') NOT NULL COMMENT '同步方向',
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time TIMESTAMP NULL COMMENT '结束时间',
    status ENUM('RUNNING', 'SUCCESS', 'FAILED', 'PARTIAL') DEFAULT 'RUNNING' COMMENT '状态',
    records_processed INT DEFAULT 0 COMMENT '处理记录数',
    records_success INT DEFAULT 0 COMMENT '成功记录数',
    records_failed INT DEFAULT 0 COMMENT '失败记录数',
    error_message TEXT COMMENT '错误信息',
    sync_version VARCHAR(50) COMMENT '同步版本',
    INDEX idx_client_id (client_id),
    INDEX idx_sync_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='同步日志表';

-- 5. 版本控制表
CREATE TABLE IF NOT EXISTS keyword_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword_id BIGINT NOT NULL COMMENT '关键词ID',
    version_number INT NOT NULL COMMENT '版本号',
    change_type ENUM('CREATE', 'UPDATE', 'DELETE') NOT NULL COMMENT '变更类型',
    change_data JSON COMMENT '变更数据',
    changed_by BIGINT COMMENT '变更人',
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    client_id VARCHAR(100) COMMENT '客户端ID',
    FOREIGN KEY (keyword_id) REFERENCES keyword_configs(id),
    INDEX idx_keyword_id (keyword_id),
    INDEX idx_version (version_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='版本控制表';

-- 6. 冲突解决表
CREATE TABLE IF NOT EXISTS keyword_conflicts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword_id BIGINT NOT NULL COMMENT '关键词ID',
    client_id VARCHAR(100) NOT NULL COMMENT '客户端ID',
    server_version JSON COMMENT '服务器版本',
    client_version JSON COMMENT '客户端版本',
    conflict_type ENUM('UPDATE_UPDATE', 'UPDATE_DELETE', 'DELETE_UPDATE') NOT NULL COMMENT '冲突类型',
    resolution_strategy ENUM('SERVER_WINS', 'CLIENT_WINS', 'MERGE', 'MANUAL') DEFAULT 'MANUAL' COMMENT '解决策略',
    resolved BOOLEAN DEFAULT FALSE COMMENT '是否已解决',
    resolved_at TIMESTAMP NULL COMMENT '解决时间',
    resolved_by BIGINT COMMENT '解决人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='冲突解决表';

-- 7. 关键词分析表
CREATE TABLE IF NOT EXISTS keyword_analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword_id BIGINT NOT NULL COMMENT '关键词ID',
    importance_score DECIMAL(5,4) COMMENT '重要度分数',
    frequency_score DECIMAL(5,4) COMMENT '频率分数',
    context_relevance DECIMAL(5,4) COMMENT '上下文相关性',
    user_feedback_score DECIMAL(5,4) COMMENT '用户反馈分数',
    overall_score DECIMAL(5,4) COMMENT '总体分数',
    analysis_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '分析日期',
    algorithm_version VARCHAR(20) COMMENT '算法版本',
    FOREIGN KEY (keyword_id) REFERENCES keyword_configs(id),
    INDEX idx_keyword_id (keyword_id),
    INDEX idx_overall_score (overall_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关键词分析表';

-- 8. 冗余检测表
CREATE TABLE IF NOT EXISTS keyword_redundancy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword1_id BIGINT NOT NULL COMMENT '关键词1 ID',
    keyword2_id BIGINT NOT NULL COMMENT '关键词2 ID',
    similarity_score DECIMAL(5,4) COMMENT '相似度分数',
    redundancy_type ENUM('EXACT', 'SEMANTIC', 'CONTEXTUAL') NOT NULL COMMENT '冗余类型',
    detection_algorithm VARCHAR(50) COMMENT '检测算法',
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '检测时间',
    status ENUM('DETECTED', 'REVIEWED', 'MERGED', 'IGNORED') DEFAULT 'DETECTED' COMMENT '状态',
    FOREIGN KEY (keyword1_id) REFERENCES keyword_configs(id),
    FOREIGN KEY (keyword2_id) REFERENCES keyword_configs(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='冗余检测表';

-- 9. 触发规则表
CREATE TABLE IF NOT EXISTS keyword_trigger_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_name VARCHAR(255) NOT NULL COMMENT '规则名称',
    keyword_conditions JSON NOT NULL COMMENT '关键词条件',
    trigger_actions JSON NOT NULL COMMENT '触发动作',
    priority INT DEFAULT 0 COMMENT '优先级',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否激活',
    created_by BIGINT COMMENT '创建人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='触发规则表';

-- 10. 触发日志表
CREATE TABLE IF NOT EXISTS keyword_trigger_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id BIGINT NOT NULL COMMENT '规则ID',
    keyword VARCHAR(255) COMMENT '关键词',
    message_content TEXT COMMENT '消息内容',
    trigger_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '触发时间',
    execution_status ENUM('SUCCESS', 'FAILED', 'PARTIAL') NOT NULL COMMENT '执行状态',
    execution_result JSON COMMENT '执行结果',
    execution_duration_ms INT COMMENT '执行时长(毫秒)',
    error_message TEXT COMMENT '错误信息',
    FOREIGN KEY (rule_id) REFERENCES keyword_trigger_rules(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='触发日志表';

-- 11. 关键词使用统计表
CREATE TABLE IF NOT EXISTS keyword_usage_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword_id BIGINT NOT NULL COMMENT '关键词ID',
    stat_date DATE NOT NULL COMMENT '统计日期',
    hit_count INT DEFAULT 0 COMMENT '命中次数',
    unique_users INT DEFAULT 0 COMMENT '唯一用户数',
    trigger_count INT DEFAULT 0 COMMENT '触发次数',
    success_rate DECIMAL(5,4) COMMENT '成功率',
    avg_response_time_ms INT COMMENT '平均响应时间(毫秒)',
    grid_area VARCHAR(100) COMMENT '网格区域',
    FOREIGN KEY (keyword_id) REFERENCES keyword_configs(id),
    UNIQUE KEY uk_keyword_date_area (keyword_id, stat_date, grid_area)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关键词使用统计表';

-- 插入默认配置数据
INSERT INTO keyword_learning_config (grid_area, min_frequency, learning_window_days, confidence_threshold, max_learned_keywords, auto_promote) 
VALUES ('DEFAULT', 3, 7, 0.7, 100, TRUE);

-- 插入示例触发规则
INSERT INTO keyword_trigger_rules (rule_name, keyword_conditions, trigger_actions, priority, is_active, created_by) 
VALUES 
('停电关键词触发', '{"keywords":["停电","断电","没电"],"matchType":"ANY"}', '{"actions":[{"type":"NOTIFICATION","target":"ADMIN"},{"type":"AUTO_REPLY","message":"您好，我们已收到您的停电报告，将尽快处理。"}]}', 1, TRUE, 1),
('故障关键词触发', '{"keywords":["故障","坏了","不能用"],"matchType":"ANY"}', '{"actions":[{"type":"CREATE_TICKET","category":"FAULT"},{"type":"AUTO_REPLY","message":"您好，我们已记录您的故障报告，工作人员将联系您。"}]}', 2, TRUE, 1);