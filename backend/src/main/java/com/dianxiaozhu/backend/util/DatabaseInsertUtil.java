package com.dianxiaozhu.backend.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInsertUtil {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insertAllData() {
        System.out.println("开始插入业务规则和消息模板...");
        
        try {
            // 插入转发智能业务规则
            insertForwardRule();
            
            // 插入回复业务规则
            insertReplyRule();
            
            // 插入转发消息模板
            insertForwardTemplate();
            
            System.out.println("所有数据插入完成！");
        } catch (Exception e) {
            System.err.println("插入数据时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertForwardRule() {
        System.out.println("插入转发智能业务规则...");
        
        // 插入转发业务规则
        String insertRuleSql = "INSERT INTO business_rule (rule_name, rule_description, rule_type, priority, enabled, effective_days, creator_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        Long ruleId = jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(id), 0) + 1 FROM business_rule", Long.class);
        
        jdbcTemplate.update(insertRuleSql, 
            "智能消息转发规则", 
            "检测到重要关键词时自动转发给相关负责人", 
            "MESSAGE_FORWARD", 
            1, 
            true, 
            "1,2,3,4,5,6,7", 
            1L);
        
        // 获取刚插入的规则ID
        Long insertedRuleId = jdbcTemplate.queryForObject(
            "SELECT id FROM business_rule WHERE rule_name = '智能消息转发规则'", Long.class);
        
        // 插入规则条件
        String insertConditionSql = "INSERT INTO rule_condition (rule_id, condition_type, condition_value, match_mode, case_sensitive) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertConditionSql, insertedRuleId, "KEYWORD_MATCH", "紧急,故障,停电,断网,系统异常", "CONTAINS", false);
        
        // 插入规则动作
        String insertActionSql = "INSERT INTO rule_action (rule_id, action_type, action_config, execution_order) VALUES (?, ?, ?, ?)";
        String actionConfig = "{\"targetUsers\":[\"admin\",\"manager\"],\"message\":\"检测到重要消息，请及时处理\",\"includeOriginal\":true,\"notificationMethod\":\"email\"}";
        jdbcTemplate.update(insertActionSql, insertedRuleId, "MESSAGE_FORWARD", actionConfig, 1);
        
        System.out.println("转发智能业务规则插入完成，规则ID: " + insertedRuleId);
    }

    private void insertReplyRule() {
        System.out.println("插入回复业务规则...");
        
        // 插入回复业务规则
        String insertRuleSql = "INSERT INTO business_rule (rule_name, rule_description, rule_type, priority, enabled, effective_days, creator_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        jdbcTemplate.update(insertRuleSql, 
            "智能自动回复规则", 
            "用户咨询常见问题时自动回复标准答案", 
            "AUTO_REPLY", 
            2, 
            true, 
            "1,2,3,4,5,6,7", 
            1L);
        
        // 获取刚插入的规则ID
        Long insertedRuleId = jdbcTemplate.queryForObject(
            "SELECT id FROM business_rule WHERE rule_name = '智能自动回复规则'", Long.class);
        
        // 插入规则条件
        String insertConditionSql = "INSERT INTO rule_condition (rule_id, condition_type, condition_value, match_mode, case_sensitive) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(insertConditionSql, insertedRuleId, "KEYWORD_MATCH", "如何,怎么,什么时候,费用,价格,收费标准", "CONTAINS", false);
        
        // 插入规则动作
        String insertActionSql = "INSERT INTO rule_action (rule_id, action_type, action_config, execution_order) VALUES (?, ?, ?, ?)";
        String actionConfig = "{\"replyMessage\":\"您好！感谢您的咨询。我们已收到您的问题，客服人员会在24小时内回复您。如有紧急情况，请拨打客服热线：400-123-4567。\",\"delay\":0,\"autoClose\":false}";
        jdbcTemplate.update(insertActionSql, insertedRuleId, "AUTO_REPLY", actionConfig, 1);
        
        System.out.println("回复业务规则插入完成，规则ID: " + insertedRuleId);
    }

    private void insertForwardTemplate() {
        System.out.println("插入转发消息模板...");
        
        // 插入转发消息模板
        String insertTemplateSql = "INSERT INTO message_templates (name, template, type, priority, enabled, header, attachment_rule, data_masking, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        
        String templateContent = "🔔 重要消息转发\n\n" +
                "原始消息：{{original_message}}\n" +
                "消息来源：{{group_name}}\n" +
                "发送人：{{sender_name}}\\{{sender_remark}}\n" +
                "发送时间：{{send_time}}\n" +
                "消息类型：{{message_level}}（紧急、高、中、低）\n\n" +
                "⚠️ 此消息已被系统识别为重要消息，请及时处理！\n\n" +
                "处理建议：\n" +
                "1. 立即查看消息内容\n" +
                "2. 评估紧急程度\n" +
                "3. 采取相应措施\n" +
                "4. 及时反馈处理结果";
        
        jdbcTemplate.update(insertTemplateSql, 
            "重要消息转发模板", 
            templateContent, 
            "forward", 
            1, 
            true, 
            "【系统智能转发】", 
            "转发所有附件", 
            false);
        
        // 获取刚插入的模板ID
        Long insertedTemplateId = jdbcTemplate.queryForObject(
            "SELECT id FROM message_templates WHERE name = '智能转发消息模板'", Long.class);
        
        // 插入模板关键词
        String insertKeywordSql = "INSERT INTO message_template_keywords (template_id, keyword) VALUES (?, ?)";
        String[] keywords = {"紧急", "故障", "停电", "断网", "系统异常", "重要", "urgent"};
        for (String keyword : keywords) {
            jdbcTemplate.update(insertKeywordSql, insertedTemplateId, keyword);
        }
        
        // 插入模板条件
        String insertConditionSql = "INSERT INTO message_template_conditions (template_id, condition_expr) VALUES (?, ?)";
        String[] conditions = {
            "contains(message, '紧急')",
            "contains(message, '故障')",
            "contains(message, '停电')",
            "priority >= 'high'"
        };
        for (String condition : conditions) {
            jdbcTemplate.update(insertConditionSql, insertedTemplateId, condition);
        }
        
        System.out.println("转发消息模板插入完成，模板ID: " + insertedTemplateId);
    }
}