package com.dianxiaozhu.backend.config;

import com.dianxiaozhu.backend.entity.MessageTemplate;
import com.dianxiaozhu.backend.repository.MessageTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

/**
 * 消息模板初始化器
 * 在应用启动时检查并初始化示例消息模板
 */
@Component
@Order(2) // 确保在其他初始化之后运行
public class MessageTemplateInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MessageTemplateInitializer.class);

    @Autowired
    private MessageTemplateRepository messageTemplateRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            // 检查是否已存在模板
            if (messageTemplateRepository.count() == 0) {
                logger.info("初始化消息模板示例...");
                
                // 创建转发消息模板示例
                createForwardTemplateExample();
                
                // 创建回复消息模板示例
                createReplyTemplateExample();
                
                // 创建带数据脱敏的回复模板示例
                createMaskingTemplateExample();
                
                logger.info("消息模板示例初始化完成");
            } else {
                logger.info("Message templates already exist, skipping initialization");
            }
        } catch (Exception e) {
            logger.error("初始化消息模板示例时发生错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 创建转发消息模板示例
     */
    private void createForwardTemplateExample() {
        MessageTemplate forwardTemplate = new MessageTemplate();
        forwardTemplate.setName("客户咨询转发模板");
        forwardTemplate.setTemplate("客户{{customer_name}}（联系方式：{{contact_info}}）咨询以下问题：\n\n{{message_content}}\n\n请相关部门尽快处理并回复。");
        forwardTemplate.setType("forward");
        forwardTemplate.setPriority(10);
        forwardTemplate.setEnabled(true);
        forwardTemplate.setHeader("【重要客户咨询】");
        forwardTemplate.setAttachmentRule("转发所有附件");
        forwardTemplate.setDataMasking(false);
        forwardTemplate.setCreatedAt(LocalDateTime.now());
        
        // 设置关键词
        forwardTemplate.setKeywords(Arrays.asList("咨询", "问题", "如何"));
        
        // 设置条件
        forwardTemplate.setConditions(Arrays.asList(
                "contains(message, \"咨询\")", 
                "contains(message, \"问题\")"
        ));
        
        messageTemplateRepository.save(forwardTemplate);
    }
    
    /**
     * 创建回复消息模板示例
     */
    private void createReplyTemplateExample() {
        MessageTemplate replyTemplate = new MessageTemplate();
        replyTemplate.setName("标准客户回复模板");
        replyTemplate.setTemplate("尊敬的{{customer_name}}：\n\n感谢您的咨询。关于您提出的\"{{question}}\"问题，我们的回复如下：\n\n{{reply_content}}\n\n如有其他问题，请随时联系我们。\n\n祝好！\n客户服务团队");
        replyTemplate.setType("reply");
        replyTemplate.setPriority(5);
        replyTemplate.setEnabled(true);
        replyTemplate.setHeader("【回复】");
        replyTemplate.setDataMasking(false);
        replyTemplate.setCreatedAt(LocalDateTime.now());
        
        // 设置关键词
        replyTemplate.setKeywords(Arrays.asList("回复", "答复", "解答"));
        
        // 设置条件
        replyTemplate.setConditions(Collections.singletonList("is_reply = true"));
        
        messageTemplateRepository.save(replyTemplate);
    }
    
    /**
     * 创建带数据脱敏的回复模板示例
     */
    private void createMaskingTemplateExample() {
        MessageTemplate maskingTemplate = new MessageTemplate();
        maskingTemplate.setName("敏感信息回复模板");
        maskingTemplate.setTemplate("尊敬的客户：\n\n您的账户{{account_number}}已处理完毕，相关信息如下：\n\n{{sensitive_content}}\n\n请妥善保管您的个人信息，切勿泄露给他人。\n\n客户服务中心");
        maskingTemplate.setType("reply");
        maskingTemplate.setPriority(3);
        maskingTemplate.setEnabled(true);
        maskingTemplate.setHeader("【重要通知】");
        maskingTemplate.setDataMasking(true);
        maskingTemplate.setCreatedAt(LocalDateTime.now());
        
        // 设置关键词
        maskingTemplate.setKeywords(Arrays.asList("账户", "敏感"));
        
        // 设置脱敏规则
        maskingTemplate.setMaskingRules(Arrays.asList(
                "mask(\"account_number\", 4, 4)",
                "mask(\"phone\", 3, 4)",
                "mask(\"email\", 3, 0, \"@\")"
        ));
        
        messageTemplateRepository.save(maskingTemplate);
    }
}