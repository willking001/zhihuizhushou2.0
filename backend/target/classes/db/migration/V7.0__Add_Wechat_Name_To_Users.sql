-- 添加微信名字段到用户表
-- 版本: V7.0
-- 描述: 为用户表添加wechat_name字段，用于网格员微信通知功能

-- 添加微信名字段
ALTER TABLE users 
ADD COLUMN wechat_name VARCHAR(100) COMMENT '微信名（用于网格员通知）';

-- 添加索引以提高查询性能
CREATE INDEX idx_users_wechat_name ON users(wechat_name);