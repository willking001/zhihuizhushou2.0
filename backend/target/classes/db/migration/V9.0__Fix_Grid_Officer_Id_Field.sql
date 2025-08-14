-- 修复群组管理状态表中缺失的grid_officer_id字段
ALTER TABLE group_management_status ADD COLUMN grid_officer_id VARCHAR(50) COMMENT '分配的网格员ID';