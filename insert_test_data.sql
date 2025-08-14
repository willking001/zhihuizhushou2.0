-- 插入测试数据用于群组统计功能验证
USE dianxiaozhu;

-- 插入群组日统计测试数据
INSERT INTO group_daily_statistics (chat_room, stat_date, message_count, auto_reply_count, manual_takeover_count, avg_response_time, satisfaction_score) VALUES
('群组A-电力维护', '2025-01-10', 45, 38, 2, 2.5, 4.2),
('群组A-电力维护', '2025-01-11', 52, 41, 3, 2.8, 4.1),
('群组A-电力维护', '2025-01-12', 38, 32, 1, 2.2, 4.3),
('群组A-电力维护', '2025-01-13', 61, 48, 4, 3.1, 4.0),
('群组A-电力维护', '2025-01-14', 43, 35, 2, 2.6, 4.2),

('群组B-设备监控', '2025-01-10', 32, 28, 1, 1.8, 4.4),
('群组B-设备监控', '2025-01-11', 41, 35, 2, 2.1, 4.3),
('群组B-设备监控', '2025-01-12', 29, 25, 1, 1.9, 4.5),
('群组B-设备监控', '2025-01-13', 48, 39, 3, 2.4, 4.2),
('群组B-设备监控', '2025-01-14', 35, 30, 1, 2.0, 4.4),

('群组C-故障处理', '2025-01-10', 28, 22, 3, 3.2, 3.8),
('群组C-故障处理', '2025-01-11', 34, 26, 4, 3.5, 3.7),
('群组C-故障处理', '2025-01-12', 25, 19, 2, 3.0, 3.9),
('群组C-故障处理', '2025-01-13', 42, 31, 5, 3.8, 3.6),
('群组C-故障处理', '2025-01-14', 31, 24, 3, 3.3, 3.8),

('群组D-安全巡检', '2025-01-10', 18, 15, 1, 1.5, 4.6),
('群组D-安全巡检', '2025-01-11', 22, 19, 1, 1.7, 4.5),
('群组D-安全巡检', '2025-01-12', 16, 14, 0, 1.4, 4.7),
('群组D-安全巡检', '2025-01-13', 25, 21, 2, 1.8, 4.4),
('群组D-安全巡检', '2025-01-14', 20, 17, 1, 1.6, 4.6);

-- 插入关键词配置测试数据（如果不存在）
INSERT IGNORE INTO keyword_configs (keyword, type, priority, is_active, grid_area, hit_count, created_at, updated_at) VALUES
('停电', 'EMERGENCY', 'HIGH', true, 'DEFAULT', 0, NOW(), NOW()),
('故障', 'FAULT', 'HIGH', true, 'DEFAULT', 0, NOW(), NOW()),
('断电', 'EMERGENCY', 'HIGH', true, 'DEFAULT', 0, NOW(), NOW()),
('设备异常', 'FAULT', 'MEDIUM', true, 'DEFAULT', 0, NOW(), NOW()),
('安全隐患', 'SAFETY', 'HIGH', true, 'DEFAULT', 0, NOW(), NOW()),
('维修', 'MAINTENANCE', 'MEDIUM', true, 'DEFAULT', 0, NOW(), NOW()),
('巡检', 'INSPECTION', 'LOW', true, 'DEFAULT', 0, NOW(), NOW()),
('报警', 'ALERT', 'HIGH', true, 'DEFAULT', 0, NOW(), NOW());

-- 插入关键词使用统计测试数据
INSERT INTO keyword_usage_stats (keyword_id, stat_date, hit_count, unique_users, trigger_count, success_rate, avg_response_time_ms, grid_area) 
SELECT 
    k.id,
    '2025-01-10' as stat_date,
    CASE k.keyword 
        WHEN '停电' THEN 15
        WHEN '故障' THEN 12
        WHEN '断电' THEN 8
        WHEN '设备异常' THEN 10
        WHEN '安全隐患' THEN 5
        WHEN '维修' THEN 7
        WHEN '巡检' THEN 3
        WHEN '报警' THEN 9
        ELSE 1
    END as hit_count,
    CASE k.keyword 
        WHEN '停电' THEN 8
        WHEN '故障' THEN 7
        WHEN '断电' THEN 5
        WHEN '设备异常' THEN 6
        WHEN '安全隐患' THEN 4
        WHEN '维修' THEN 5
        WHEN '巡检' THEN 3
        WHEN '报警' THEN 6
        ELSE 1
    END as unique_users,
    CASE k.keyword 
        WHEN '停电' THEN 13
        WHEN '故障' THEN 10
        WHEN '断电' THEN 7
        WHEN '设备异常' THEN 8
        WHEN '安全隐患' THEN 4
        WHEN '维修' THEN 6
        WHEN '巡检' THEN 3
        WHEN '报警' THEN 8
        ELSE 1
    END as trigger_count,
    0.85 as success_rate,
    2500 as avg_response_time_ms,
    'DEFAULT' as grid_area
FROM keyword_configs k
WHERE k.keyword IN ('停电', '故障', '断电', '设备异常', '安全隐患', '维修', '巡检', '报警');

-- 插入更多日期的关键词统计数据
INSERT INTO keyword_usage_stats (keyword_id, stat_date, hit_count, unique_users, trigger_count, success_rate, avg_response_time_ms, grid_area) 
SELECT 
    k.id,
    '2025-01-11' as stat_date,
    CASE k.keyword 
        WHEN '停电' THEN 18
        WHEN '故障' THEN 14
        WHEN '断电' THEN 6
        WHEN '设备异常' THEN 11
        WHEN '安全隐患' THEN 7
        WHEN '维修' THEN 9
        WHEN '巡检' THEN 4
        WHEN '报警' THEN 12
        ELSE 2
    END as hit_count,
    CASE k.keyword 
        WHEN '停电' THEN 9
        WHEN '故障' THEN 8
        WHEN '断电' THEN 4
        WHEN '设备异常' THEN 7
        WHEN '安全隐患' THEN 5
        WHEN '维修' THEN 6
        WHEN '巡检' THEN 4
        WHEN '报警' THEN 7
        ELSE 2
    END as unique_users,
    CASE k.keyword 
        WHEN '停电' THEN 16
        WHEN '故障' THEN 12
        WHEN '断电' THEN 5
        WHEN '设备异常' THEN 9
        WHEN '安全隐患' THEN 6
        WHEN '维修' THEN 8
        WHEN '巡检' THEN 4
        WHEN '报警' THEN 10
        ELSE 2
    END as trigger_count,
    0.87 as success_rate,
    2300 as avg_response_time_ms,
    'DEFAULT' as grid_area
FROM keyword_configs k
WHERE k.keyword IN ('停电', '故障', '断电', '设备异常', '安全隐患', '维修', '巡检', '报警');

-- 插入最近几天的数据
INSERT INTO keyword_usage_stats (keyword_id, stat_date, hit_count, unique_users, trigger_count, success_rate, avg_response_time_ms, grid_area) 
SELECT 
    k.id,
    '2025-01-12' as stat_date,
    CASE k.keyword 
        WHEN '停电' THEN 11
        WHEN '故障' THEN 16
        WHEN '断电' THEN 4
        WHEN '设备异常' THEN 13
        WHEN '安全隐患' THEN 3
        WHEN '维修' THEN 8
        WHEN '巡检' THEN 2
        WHEN '报警' THEN 7
        ELSE 1
    END as hit_count,
    CASE k.keyword 
        WHEN '停电' THEN 6
        WHEN '故障' THEN 9
        WHEN '断电' THEN 3
        WHEN '设备异常' THEN 8
        WHEN '安全隐患' THEN 3
        WHEN '维修' THEN 5
        WHEN '巡检' THEN 2
        WHEN '报警' THEN 5
        ELSE 1
    END as unique_users,
    CASE k.keyword 
        WHEN '停电' THEN 9
        WHEN '故障' THEN 14
        WHEN '断电' THEN 3
        WHEN '设备异常' THEN 11
        WHEN '安全隐患' THEN 2
        WHEN '维修' THEN 7
        WHEN '巡检' THEN 2
        WHEN '报警' THEN 6
        ELSE 1
    END as trigger_count,
    0.83 as success_rate,
    2700 as avg_response_time_ms,
    'DEFAULT' as grid_area
FROM keyword_configs k
WHERE k.keyword IN ('停电', '故障', '断电', '设备异常', '安全隐患', '维修', '巡检', '报警');

COMMIT;