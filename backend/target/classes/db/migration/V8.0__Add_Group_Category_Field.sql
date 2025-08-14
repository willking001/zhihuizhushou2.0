-- 添加群组分类字段到群组管理状态表
ALTER TABLE group_management_status ADD COLUMN group_category VARCHAR(50);

-- 为现有数据设置默认分类
UPDATE group_management_status SET group_category = '社区群' WHERE group_category IS NULL;