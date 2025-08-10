-- 电小助 2.0 数据库初始化脚本
-- 创建时间: 2024
-- 描述: 初始化项目所需的数据库表结构

USE dianxiaozhu;

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ================================
-- 用户相关表
-- ================================

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(255) NOT NULL COMMENT '密码',
    `nickname` varchar(100) DEFAULT NULL COMMENT '昵称',
    `avatar` varchar(500) DEFAULT NULL COMMENT '头像URL',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `status` tinyint DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `role` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `name` varchar(50) NOT NULL COMMENT '角色名称',
    `code` varchar(50) NOT NULL COMMENT '角色编码',
    `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
    `status` tinyint DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `role_id` bigint NOT NULL COMMENT '角色ID',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ================================
-- 网格员相关表
-- ================================

-- 网格员表
CREATE TABLE IF NOT EXISTS `grid_staff` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '网格员ID',
    `user_id` bigint NOT NULL COMMENT '关联用户ID',
    `staff_code` varchar(50) NOT NULL COMMENT '网格员编号',
    `name` varchar(100) NOT NULL COMMENT '姓名',
    `phone` varchar(20) NOT NULL COMMENT '手机号',
    `area_code` varchar(50) NOT NULL COMMENT '负责区域编码',
    `area_name` varchar(200) NOT NULL COMMENT '负责区域名称',
    `device_id` varchar(100) DEFAULT NULL COMMENT '设备唯一标识',
    `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
    `wechat_id` varchar(100) DEFAULT NULL COMMENT '微信号',
    `status` tinyint DEFAULT 1 COMMENT '状态：1-在线，0-离线',
    `last_heartbeat` datetime DEFAULT NULL COMMENT '最后心跳时间',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_staff_code` (`staff_code`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_area_code` (`area_code`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='网格员表';

-- 监控群组表
CREATE TABLE IF NOT EXISTS `monitored_groups` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '群组ID',
    `grid_staff_id` bigint NOT NULL COMMENT '网格员ID',
    `group_id` varchar(100) NOT NULL COMMENT '微信群ID',
    `group_name` varchar(200) NOT NULL COMMENT '群名称',
    `group_type` varchar(50) DEFAULT 'community' COMMENT '群类型：community-社区群，business-商户群',
    `member_count` int DEFAULT 0 COMMENT '群成员数量',
    `is_active` tinyint DEFAULT 1 COMMENT '是否活跃监控：1-是，0-否',
    `last_message_time` datetime DEFAULT NULL COMMENT '最后消息时间',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_id` (`group_id`),
    KEY `idx_grid_staff_id` (`grid_staff_id`),
    KEY `idx_group_type` (`group_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='监控群组表';

-- 设备监控日志表
CREATE TABLE IF NOT EXISTS `device_monitor_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `grid_staff_id` bigint NOT NULL COMMENT '网格员ID',
    `device_id` varchar(100) NOT NULL COMMENT '设备ID',
    `event_type` varchar(50) NOT NULL COMMENT '事件类型：online-上线，offline-离线，heartbeat-心跳，error-错误',
    `event_data` json DEFAULT NULL COMMENT '事件数据',
    `message` text COMMENT '事件描述',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_grid_staff_id` (`grid_staff_id`),
    KEY `idx_device_id` (`device_id`),
    KEY `idx_event_type` (`event_type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备监控日志表';

-- ================================
-- 消息相关表
-- ================================

-- 聊天记录表
CREATE TABLE IF NOT EXISTS `chat_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `session_id` varchar(100) NOT NULL COMMENT '会话ID',
    `user_id` bigint DEFAULT NULL COMMENT '用户ID',
    `message_type` varchar(20) NOT NULL COMMENT '消息类型：text-文本，image-图片，voice-语音',
    `message_content` text NOT NULL COMMENT '消息内容',
    `sender_type` varchar(20) NOT NULL COMMENT '发送者类型：user-用户，assistant-助手',
    `intent` varchar(100) DEFAULT NULL COMMENT '意图识别结果',
    `entities` json DEFAULT NULL COMMENT '实体提取结果',
    `response_time` int DEFAULT NULL COMMENT '响应时间(毫秒)',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天记录表';

-- ================================
-- 知识库相关表
-- ================================

-- 知识库表
CREATE TABLE IF NOT EXISTS `knowledge_base` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识ID',
    `title` varchar(200) NOT NULL COMMENT '标题',
    `content` text NOT NULL COMMENT '内容',
    `category` varchar(100) DEFAULT NULL COMMENT '分类',
    `tags` varchar(500) DEFAULT NULL COMMENT '标签，逗号分隔',
    `source` varchar(100) DEFAULT NULL COMMENT '来源',
    `status` tinyint DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `view_count` int DEFAULT 0 COMMENT '查看次数',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    FULLTEXT KEY `ft_title_content` (`title`, `content`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库表';

-- ================================
-- 工单相关表
-- ================================

-- 工单表
CREATE TABLE IF NOT EXISTS `work_order` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工单ID',
    `order_no` varchar(50) NOT NULL COMMENT '工单编号',
    `title` varchar(200) NOT NULL COMMENT '工单标题',
    `description` text COMMENT '工单描述',
    `category` varchar(100) NOT NULL COMMENT '工单分类',
    `priority` varchar(20) DEFAULT 'medium' COMMENT '优先级：low-低，medium-中，high-高，urgent-紧急',
    `status` varchar(20) DEFAULT 'pending' COMMENT '状态：pending-待处理，processing-处理中，completed-已完成，closed-已关闭',
    `submitter_id` bigint NOT NULL COMMENT '提交人ID',
    `assignee_id` bigint DEFAULT NULL COMMENT '处理人ID',
    `grid_staff_id` bigint DEFAULT NULL COMMENT '关联网格员ID',
    `source` varchar(50) DEFAULT 'manual' COMMENT '来源：manual-手动创建，wechat-微信群，auto-自动生成',
    `source_data` json DEFAULT NULL COMMENT '来源数据',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `completed_at` datetime DEFAULT NULL COMMENT '完成时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_submitter_id` (`submitter_id`),
    KEY `idx_assignee_id` (`assignee_id`),
    KEY `idx_grid_staff_id` (`grid_staff_id`),
    KEY `idx_status` (`status`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工单表';

-- ================================
-- 初始化数据
-- ================================

-- 插入默认角色
INSERT INTO `role` (`name`, `code`, `description`) VALUES
('超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限'),
('管理员', 'ADMIN', '系统管理员'),
('网格员', 'GRID_STAFF', '网格员用户'),
('普通用户', 'USER', '普通用户');

-- 插入默认管理员用户
INSERT INTO `user` (`username`, `password`, `nickname`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', '系统管理员', 1);

-- 为管理员分配角色
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 1);

-- 插入示例知识库数据
INSERT INTO `knowledge_base` (`title`, `content`, `category`, `tags`) VALUES
('如何办理居住证', '居住证办理需要提供身份证、租房合同、居住证明等材料...', '证件办理', '居住证,办理,证件'),
('垃圾分类指南', '垃圾分类分为可回收物、有害垃圾、湿垃圾、干垃圾四类...', '生活服务', '垃圾分类,环保,生活'),
('社区投诉流程', '社区投诉可通过以下渠道：1.社区服务中心 2.12345热线...', '投诉建议', '投诉,流程,服务');

SET FOREIGN_KEY_CHECKS = 1;

-- 创建完成提示
SELECT '数据库初始化完成！' as message;