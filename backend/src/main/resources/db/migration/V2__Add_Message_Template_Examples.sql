-- 添加转发消息模板示例
INSERT INTO message_templates (name, template, type, priority, enabled, header, attachment_rule, data_masking, created_at)
VALUES ('客户咨询转发模板', '客户{{customer_name}}（联系方式：{{contact_info}}）咨询以下问题：\n\n{{message_content}}\n\n请相关部门尽快处理并回复。', 'forward', 10, true, '【重要客户咨询】', '转发所有附件', false, NOW());

-- 获取上面插入的转发模板ID
SET @forward_template_id = LAST_INSERT_ID();

-- 添加转发模板关键词
INSERT INTO message_template_keywords (template_id, keyword) VALUES (@forward_template_id, '咨询');
INSERT INTO message_template_keywords (template_id, keyword) VALUES (@forward_template_id, '问题');
INSERT INTO message_template_keywords (template_id, keyword) VALUES (@forward_template_id, '如何');

-- 添加转发模板条件
INSERT INTO message_template_conditions (template_id, condition_expr) VALUES (@forward_template_id, 'contains(message, "咨询")');
INSERT INTO message_template_conditions (template_id, condition_expr) VALUES (@forward_template_id, 'contains(message, "问题")');

-- 添加回复消息模板示例
INSERT INTO message_templates (name, template, type, priority, enabled, header, attachment_rule, data_masking, created_at)
VALUES ('标准客户回复模板', '尊敬的{{customer_name}}：\n\n感谢您的咨询。关于您提出的"{{question}}"问题，我们的回复如下：\n\n{{reply_content}}\n\n如有其他问题，请随时联系我们。\n\n祝好！\n客户服务团队', 'reply', 5, true, '【回复】', NULL, false, NOW());

-- 获取上面插入的回复模板ID
SET @reply_template_id = LAST_INSERT_ID();

-- 添加回复模板关键词
INSERT INTO message_template_keywords (template_id, keyword) VALUES (@reply_template_id, '回复');
INSERT INTO message_template_keywords (template_id, keyword) VALUES (@reply_template_id, '答复');
INSERT INTO message_template_keywords (template_id, keyword) VALUES (@reply_template_id, '解答');

-- 添加回复模板条件
INSERT INTO message_template_conditions (template_id, condition_expr) VALUES (@reply_template_id, 'is_reply = true');

-- 添加带数据脱敏的回复模板示例
INSERT INTO message_templates (name, template, type, priority, enabled, header, attachment_rule, data_masking, created_at)
VALUES ('敏感信息回复模板', '尊敬的客户：\n\n您的账户{{account_number}}已处理完毕，相关信息如下：\n\n{{sensitive_content}}\n\n请妥善保管您的个人信息，切勿泄露给他人。\n\n客户服务中心', 'reply', 3, true, '【重要通知】', NULL, true, NOW());

-- 获取上面插入的脱敏模板ID
SET @masking_template_id = LAST_INSERT_ID();

-- 添加脱敏模板关键词
INSERT INTO message_template_keywords (template_id, keyword) VALUES (@masking_template_id, '账户');
INSERT INTO message_template_keywords (template_id, keyword) VALUES (@masking_template_id, '敏感');

-- 添加脱敏规则
INSERT INTO message_template_masking_rules (template_id, masking_rule) VALUES (@masking_template_id, 'mask("account_number", 4, 4)');
INSERT INTO message_template_masking_rules (template_id, masking_rule) VALUES (@masking_template_id, 'mask("phone", 3, 4)');
INSERT INTO message_template_masking_rules (template_id, masking_rule) VALUES (@masking_template_id, 'mask("email", 3, 0, "@")');