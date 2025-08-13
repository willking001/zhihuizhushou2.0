package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.entity.MessageTemplate;
import com.dianxiaozhu.backend.service.MessageTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 消息模板控制器
 * 处理消息模板的API请求
 */
@RestController
@RequestMapping("/api/system/templates")
@CrossOrigin(origins = "*")
public class TemplateController {

    private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);

    @Autowired
    private MessageTemplateService messageTemplateService;

    /**
     * 获取消息模板列表
     * @param type 模板类型 (forward 或 reply)
     * @return 模板列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getTemplates(@RequestParam(required = false) String type) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (type != null && !type.isEmpty()) {
                // 返回指定类型的模板
                Map<String, MessageTemplate> templates = messageTemplateService.getTemplatesMapByType(type);
                
                // 记录日志，帮助调试
                logger.info("获取{}类型的模板，找到{}个模板", type, templates.size());
                
                response.put("code", 20000); // 使用统一的成功状态码
                response.put("message", "success");
                response.put("data", templates);
                response.put("timestamp", LocalDateTime.now());
            } else {
                // 返回所有类型的模板
                Map<String, Map<String, MessageTemplate>> allTemplates = new HashMap<>();
                Map<String, MessageTemplate> forwardTemplates = messageTemplateService.getTemplatesMapByType("forward");
                Map<String, MessageTemplate> replyTemplates = messageTemplateService.getTemplatesMapByType("reply");
                
                // 记录日志，帮助调试
                logger.info("获取所有类型的模板，找到{}个转发模板和{}个回复模板", 
                        forwardTemplates.size(), replyTemplates.size());
                
                allTemplates.put("forward", forwardTemplates);
                allTemplates.put("reply", replyTemplates);
                
                response.put("code", 20000); // 使用统一的成功状态码
                response.put("message", "success");
                response.put("data", allTemplates);
                response.put("timestamp", LocalDateTime.now());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取模板失败", e);
            response.put("code", 50000); // 使用统一的错误状态码
            response.put("message", "获取模板失败: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 获取指定ID的模板
     * @param id 模板ID
     * @return 模板详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getTemplateById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<MessageTemplate> template = messageTemplateService.getTemplateById(id);
            
            if (template.isPresent()) {
                response.put("code", 200);
                response.put("message", "success");
                response.put("data", template.get());
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 404);
                response.put("message", "模板不存在");
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "获取模板失败: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 创建新模板
     * @param template 模板对象
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createTemplate(@RequestBody MessageTemplate template) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("创建新模板，类型: {}, 名称: {}", template.getType(), template.getName());
            // 设置创建时间
            template.setCreatedAt(LocalDateTime.now());
            
            // 保存模板
            MessageTemplate savedTemplate = messageTemplateService.createTemplate(template);
            
            response.put("code", 20000);
            response.put("message", "模板创建成功");
            response.put("data", savedTemplate);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("模板创建成功，ID: {}", savedTemplate.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            response.put("code", 50000);
            response.put("message", "创建模板失败: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            logger.error("创建模板失败，类型: {}, 名称: {}, 错误: {}", 
                template.getType(), template.getName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 更新模板
     * @param id 模板ID
     * @param templateDetails 更新的模板详情
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateTemplate(
            @PathVariable Long id,
            @RequestBody MessageTemplate templateDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            logger.info("更新模板，ID: {}", id);
            Optional<MessageTemplate> updatedTemplate = messageTemplateService.updateTemplate(id, templateDetails);
            
            if (updatedTemplate.isPresent()) {
                response.put("code", 20000);
                response.put("message", "模板更新成功");
                response.put("data", updatedTemplate.get());
                response.put("timestamp", LocalDateTime.now());
                logger.info("模板更新成功，ID: {}", id);
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 50000);
                response.put("message", "模板不存在");
                response.put("timestamp", LocalDateTime.now());
                logger.warn("模板不存在，ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            response.put("code", 50000);
            response.put("message", "更新模板失败: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            logger.error("更新模板失败，ID: {}, 错误: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 删除模板
     * @param id 模板ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean deleted = messageTemplateService.deleteTemplate(id);
            
            if (deleted) {
                response.put("code", 20000); // 使用统一的成功状态码
                response.put("message", "模板删除成功");
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.ok(response);
            } else {
                response.put("code", 50000); // 使用统一的错误状态码
                response.put("message", "模板不存在");
                response.put("timestamp", LocalDateTime.now());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
        } catch (Exception e) {
            response.put("code", 50000); // 使用统一的错误状态码
            response.put("message", "删除模板失败: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 导出模板
     * @return 导出的模板数据
     */
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportTemplates() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Map<String, MessageTemplate>> templates = messageTemplateService.exportTemplates();
            
            response.put("code", 200);
            response.put("message", "模板导出成功");
            response.put("data", templates);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "导出模板失败: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 导入模板
     * @param file 模板文件
     * @return 导入结果
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importTemplates(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 读取文件内容
            String content = new String(file.getBytes());
            
            // 解析JSON内容为模板对象
            // 这里需要使用JSON库进行解析，例如Jackson或Gson
            // 为简化示例，这里省略具体实现
            Map<String, Map<String, MessageTemplate>> templates = new HashMap<>(); // 假设已解析
            
            // 导入模板
            int count = messageTemplateService.importTemplates(templates);
            
            response.put("code", 200);
            response.put("message", "成功导入 " + count + " 个模板");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            response.put("code", 400);
            response.put("message", "读取文件失败: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "导入模板失败: " + e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}