package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.MessageTemplate;
import com.dianxiaozhu.backend.repository.MessageTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 消息模板服务类
 */
@Service
public class MessageTemplateService {

    @Autowired
    private MessageTemplateRepository messageTemplateRepository;

    /**
     * 获取所有模板
     * @return 所有模板列表
     */
    public List<MessageTemplate> getAllTemplates() {
        return messageTemplateRepository.findAll();
    }

    /**
     * 根据类型获取模板
     * @param type 模板类型 (forward 或 reply)
     * @return 指定类型的模板列表
     */
    public List<MessageTemplate> getTemplatesByType(String type) {
        return messageTemplateRepository.findByType(type);
    }

    /**
     * 根据类型获取模板并转换为Map格式
     * @param type 模板类型 (forward 或 reply)
     * @return 模板Map，键为模板ID，值为模板对象
     */
    public Map<String, MessageTemplate> getTemplatesMapByType(String type) {
        if (type == null || (!type.equals("forward") && !type.equals("reply"))) {
            throw new IllegalArgumentException("模板类型必须为 'forward' 或 'reply'");
        }
        
        List<MessageTemplate> templates = messageTemplateRepository.findByType(type);
        Map<String, MessageTemplate> templateMap = new HashMap<>();
        
        if (templates != null) {
            for (MessageTemplate template : templates) {
                if (template != null && template.getId() != null) {
                    templateMap.put(template.getId().toString(), template);
                }
            }
        }
        
        return templateMap;
    }

    /**
     * 根据ID获取模板
     * @param id 模板ID
     * @return 模板对象
     */
    public Optional<MessageTemplate> getTemplateById(Long id) {
        return messageTemplateRepository.findById(id);
    }

    /**
     * 创建新模板
     * @param template 模板对象
     * @return 创建后的模板对象
     */
    public MessageTemplate createTemplate(MessageTemplate template) {
        template.setCreatedAt(LocalDateTime.now());
        return messageTemplateRepository.save(template);
    }

    /**
     * 更新模板
     * @param id 模板ID
     * @param templateDetails 更新的模板详情
     * @return 更新后的模板对象
     */
    public Optional<MessageTemplate> updateTemplate(Long id, MessageTemplate templateDetails) {
        return messageTemplateRepository.findById(id).map(template -> {
            template.setName(templateDetails.getName());
            template.setTemplate(templateDetails.getTemplate());
            template.setKeywords(templateDetails.getKeywords());
            template.setConditions(templateDetails.getConditions());
            template.setPriority(templateDetails.getPriority());
            template.setEnabled(templateDetails.getEnabled());
            template.setHeader(templateDetails.getHeader());
            template.setAttachmentRule(templateDetails.getAttachmentRule());
            template.setDataMasking(templateDetails.getDataMasking());
            template.setMaskingRules(templateDetails.getMaskingRules());
            return messageTemplateRepository.save(template);
        });
    }

    /**
     * 删除模板
     * @param id 模板ID
     * @return 是否删除成功
     */
    public boolean deleteTemplate(Long id) {
        if (messageTemplateRepository.existsById(id)) {
            messageTemplateRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 导出所有模板
     * @return 包含所有模板的Map
     */
    public Map<String, Map<String, MessageTemplate>> exportTemplates() {
        Map<String, Map<String, MessageTemplate>> result = new HashMap<>();
        
        // 获取转发模板
        Map<String, MessageTemplate> forwardTemplates = getTemplatesMapByType("forward");
        result.put("forward", forwardTemplates);
        
        // 获取回复模板
        Map<String, MessageTemplate> replyTemplates = getTemplatesMapByType("reply");
        result.put("reply", replyTemplates);
        
        return result;
    }

    /**
     * 导入模板
     * @param templates 要导入的模板Map
     * @return 导入的模板数量
     */
    public int importTemplates(Map<String, Map<String, MessageTemplate>> templates) {
        int count = 0;
        
        // 导入转发模板
        if (templates.containsKey("forward")) {
            for (MessageTemplate template : templates.get("forward").values()) {
                template.setType("forward");
                messageTemplateRepository.save(template);
                count++;
            }
        }
        
        // 导入回复模板
        if (templates.containsKey("reply")) {
            for (MessageTemplate template : templates.get("reply").values()) {
                template.setType("reply");
                messageTemplateRepository.save(template);
                count++;
            }
        }
        
        return count;
    }
}