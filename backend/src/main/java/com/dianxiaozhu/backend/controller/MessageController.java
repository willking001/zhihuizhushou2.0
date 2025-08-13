package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.entity.Message;
import com.dianxiaozhu.backend.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MessageController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessageController.class);
    
    private final MessageService messageService;
    
    /**
     * 接收并保存消息
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveMessage(@RequestBody Message message) {
        log.info("接收消息: 发送者={}, 内容长度={}", message.getSenderName(), 
                message.getContent() != null ? message.getContent().length() : 0);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Message savedMessage = messageService.saveMessage(message);
            response.put("success", true);
            response.put("message", "消息保存成功");
            response.put("messageId", savedMessage.getId());
            response.put("isForwarded", savedMessage.getIsForwarded());
            
            if (savedMessage.getIsForwarded()) {
                response.put("forwardReason", savedMessage.getForwardReason());
                response.put("keywordsMatched", savedMessage.getKeywordsMatched());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("保存消息异常", e);
            response.put("success", false);
            response.put("message", "保存消息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 根据用户ID获取消息
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getMessagesByUserId(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Message> messages = messageService.getMessagesByUserId(userId);
            response.put("success", true);
            response.put("messages", messages);
            response.put("count", messages.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取用户消息异常", e);
            response.put("success", false);
            response.put("message", "获取消息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取今日消息
     */
    @GetMapping
    public ResponseEntity<org.springframework.data.domain.Page<Message>> getMessages(org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Message> messages = messageService.findMessages(pageable);
        return ResponseEntity.ok(messages);
    }



    @GetMapping("/analysis")
    public ResponseEntity<Map<String, Object>> getMessageAnalysis() {
        Map<String, Object> analysisData = messageService.getAnalysis();
        return ResponseEntity.ok(analysisData);
    }

    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayMessages() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Message> messages = messageService.getTodayMessages();
            response.put("success", true);
            response.put("messages", messages);
            response.put("count", messages.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取今日消息异常", e);
            response.put("success", false);
            response.put("message", "获取今日消息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取消息统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMessageStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            MessageService.MessageStats stats = messageService.getMessageStats();
            response.put("success", true);
            response.put("stats", Map.of(
                    "totalMessages", stats.getTotalMessages(),
                    "todayMessages", stats.getTodayMessages(),
                    "forwardedMessages", stats.getForwardedMessages()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取消息统计异常", e);
            response.put("success", false);
            response.put("message", "获取统计数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 根据用户ID获取消息统计
     */
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<Map<String, Object>> getMessageStatsByUser(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            MessageService.MessageStats stats = messageService.getMessageStatsByUser(userId);
            response.put("success", true);
            response.put("stats", Map.of(
                    "totalMessages", stats.getTotalMessages(),
                    "todayMessages", stats.getTodayMessages(),
                    "forwardedMessages", stats.getForwardedMessages()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取用户消息统计异常", e);
            response.put("success", false);
            response.put("message", "获取用户统计数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 根据关键词搜索消息
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMessages(@RequestParam String keyword) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Message> messages = messageService.searchMessagesByKeyword(keyword);
            response.put("success", true);
            response.put("messages", messages);
            response.put("count", messages.size());
            response.put("keyword", keyword);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("搜索消息异常", e);
            response.put("success", false);
            response.put("message", "搜索消息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取最近消息
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentMessages(
            @RequestParam(defaultValue = "50") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Message> messages = messageService.getRecentMessages(limit);
            response.put("success", true);
            response.put("messages", messages);
            response.put("count", messages.size());
            response.put("limit", limit);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取最近消息异常", e);
            response.put("success", false);
            response.put("message", "获取最近消息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 根据时间范围获取消息
     */
    @GetMapping("/range")
    public ResponseEntity<Map<String, Object>> getMessagesByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Message> messages = messageService.getMessagesByTimeRange(startTime, endTime);
            response.put("success", true);
            response.put("messages", messages);
            response.put("count", messages.size());
            response.put("startTime", startTime);
            response.put("endTime", endTime);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("根据时间范围获取消息异常", e);
            response.put("success", false);
            response.put("message", "获取消息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 根据ID获取消息详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getMessageById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Message> messageOpt = messageService.getMessageById(id);
            
            if (messageOpt.isPresent()) {
                response.put("success", true);
                response.put("message", messageOpt.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "消息不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取消息详情异常", e);
            response.put("success", false);
            response.put("message", "获取消息详情失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 删除消息
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteMessage(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            messageService.deleteMessage(id);
            response.put("success", true);
            response.put("message", "消息删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除消息异常", e);
            response.put("success", false);
            response.put("message", "删除消息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 批量接收消息
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> saveMessages(@RequestBody List<Message> messages) {
        log.info("批量接收消息: {} 条", messages.size());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            int savedCount = 0;
            int forwardedCount = 0;
            
            for (Message message : messages) {
                Message savedMessage = messageService.saveMessage(message);
                savedCount++;
                if (savedMessage.getIsForwarded()) {
                    forwardedCount++;
                }
            }
            
            response.put("success", true);
            response.put("message", "批量保存消息成功");
            response.put("savedCount", savedCount);
            response.put("forwardedCount", forwardedCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("批量保存消息异常", e);
            response.put("success", false);
            response.put("message", "批量保存消息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}