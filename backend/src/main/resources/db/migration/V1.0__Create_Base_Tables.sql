-- 创建基础表结构
-- 版本: V1.0
-- 描述: 创建系统基础表，包括用户表、消息表、关键词配置表等

-- 1. 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    real_name VARCHAR(50) COMMENT '真实姓名',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色',
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '状态',
    grid_area VARCHAR(100) COMMENT '网格区域',
    last_login DATETIME COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_grid_area (grid_area),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 创建消息表
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    chat_room VARCHAR(200) NOT NULL COMMENT '聊天室/群组标识',
    sender VARCHAR(100) NOT NULL COMMENT '发送者',
    content TEXT NOT NULL COMMENT '消息内容',
    message_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '消息类型',
    is_group BOOLEAN DEFAULT FALSE COMMENT '是否群组消息',
    is_forwarded BOOLEAN DEFAULT FALSE COMMENT '是否已转发',
    forward_target VARCHAR(200) COMMENT '转发目标',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_chat_room (chat_room),
    INDEX idx_sender (sender),
    INDEX idx_created_at (created_at),
    INDEX idx_is_group (is_group),
    INDEX idx_is_forwarded (is_forwarded)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 3. 创建关键词配置表
CREATE TABLE IF NOT EXISTS keyword_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关键词配置ID',
    keyword VARCHAR(255) NOT NULL COMMENT '关键词',
    description VARCHAR(500) COMMENT '描述',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    priority INT DEFAULT 0 COMMENT '优先级',
    grid_area VARCHAR(100) COMMENT '网格区域',
    forward_target VARCHAR(200) COMMENT '转发目标',
    auto_reply_content TEXT COMMENT '自动回复内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator_id BIGINT COMMENT '创建者ID',
    updater_id BIGINT COMMENT '更新者ID',
    INDEX idx_keyword (keyword),
    INDEX idx_enabled (enabled),
    INDEX idx_grid_area (grid_area),
    INDEX idx_priority (priority)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关键词配置表';

-- 4. 创建消息模板表
CREATE TABLE IF NOT EXISTS message_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模板ID',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_content TEXT NOT NULL COMMENT '模板内容',
    template_type VARCHAR(50) DEFAULT 'GENERAL' COMMENT '模板类型',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    grid_area VARCHAR(100) COMMENT '网格区域',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    creator_id BIGINT COMMENT '创建者ID',
    updater_id BIGINT COMMENT '更新者ID',
    INDEX idx_template_name (template_name),
    INDEX idx_template_type (template_type),
    INDEX idx_enabled (enabled),
    INDEX idx_grid_area (grid_area)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板表';

-- 插入默认管理员用户
INSERT IGNORE INTO users (username, password, email, real_name, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@example.com', '系统管理员', 'ADMIN', 'ACTIVE');

COMMIT;