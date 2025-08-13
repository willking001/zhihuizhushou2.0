package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.entity.User;
import com.dianxiaozhu.backend.entity.Message;
import com.dianxiaozhu.backend.service.UserService;
import com.dianxiaozhu.backend.service.MessageService;
import com.dianxiaozhu.backend.service.KeywordConfigService;
import com.dianxiaozhu.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.dianxiaozhu.backend.entity.KeywordConfig;
import java.util.stream.Collectors;

/**
 * 网格员客户端专用控制器
 * 处理Python客户端的API请求
 */
@RestController
@RequestMapping("/api/grid")
@CrossOrigin(origins = "*")
public class GridController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GridController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private KeywordConfigService keywordConfigService;

    @Autowired
    private UserRepository userRepository;

    /**
     * 网格员登录接口
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String username = loginData.get("username");
            String password = loginData.get("password");
            String gridArea = loginData.get("grid_area");
            
            if (username == null || password == null) {
                response.put("success", false);
                response.put("message", "用户名和密码不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 使用统一的BCrypt验证逻辑
            Optional<User> userOpt = userService.login(username, password);
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户名或密码错误");
                return ResponseEntity.ok(response);
            }
            User user = userOpt.get();
            
            // 检查用户状态（login方法已经验证了状态和更新了最后登录时间）
            if (user.getStatus() != User.UserStatus.ACTIVE) {
                response.put("success", false);
                response.put("message", "用户账户已被禁用");
                return ResponseEntity.ok(response);
            }
            
            // 返回成功响应
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "realName", user.getRealName() != null ? user.getRealName() : "",
                "gridArea", user.getGridArea() != null ? user.getGridArea() : "",
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "email", user.getEmail() != null ? user.getEmail() : ""
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "登录过程中发生错误: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 接收网格员消息
     */
    @PostMapping("/messages")
    public ResponseEntity<Map<String, Object>> receiveMessage(@RequestBody Map<String, Object> messageData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String content = (String) messageData.get("content");
            String sender = (String) messageData.get("sender");
            String gridArea = (String) messageData.get("grid_area");
            String timestamp = (String) messageData.get("timestamp");
            
            if (content == null || content.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消息内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 查找发送者用户
            Optional<User> userOpt = userService.findByUsername(sender);
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "发送者用户不存在");
                return ResponseEntity.badRequest().body(response);
            }
            User user = userOpt.get();
            
            // 创建消息对象
            Message message = new Message();
            message.setSenderName(sender);
            message.setSenderId(sender);
            message.setContent(content);
            message.setType(Message.MessageType.TEXT);
            message.setChatRoom(gridArea != null ? gridArea : "网格区域");
            message.setIsGroup(false);
            message.setGridUserId(user.getId());
            message.setReceivedAt(LocalDateTime.now());
            
            // 保存消息并检查关键词转发
            Message savedMessage = messageService.saveMessage(message);
            
            response.put("success", true);
            response.put("message", "消息接收成功");
            response.put("messageId", savedMessage.getId());
            response.put("isForwarded", savedMessage.getIsForwarded());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "消息处理失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取消息统计信息
     */
    @GetMapping("/message_statistics")
    public ResponseEntity<Map<String, Object>> getMessageStatistics(@RequestParam(required = false) String username) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Long> stats = new HashMap<>();
            
            if (username != null && !username.trim().isEmpty()) {
                // 获取特定用户的统计
                Optional<User> userOpt = userService.findByUsername(username);
                if (userOpt.isPresent()) {
                    MessageService.MessageStats messageStats = messageService.getMessageStatsByUser(userOpt.get().getId());
                    stats.put("totalMessages", messageStats.getTotalMessages());
                    stats.put("todayMessages", messageStats.getTodayMessages());
                    stats.put("forwardedMessages", messageStats.getForwardedMessages());
                } else {
                    response.put("success", false);
                    response.put("message", "用户不存在");
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                // 获取总体统计
                MessageService.MessageStats messageStats = messageService.getMessageStats();
                stats.put("totalMessages", messageStats.getTotalMessages());
                stats.put("todayMessages", messageStats.getTodayMessages());
                stats.put("forwardedMessages", messageStats.getForwardedMessages());
            }
            
            response.put("success", true);
            response.put("statistics", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取转发关键词配置
     */
    @GetMapping("/forward_keywords")
    public ResponseEntity<Map<String, Object>> getForwardKeywords(@RequestParam(required = false) String gridArea) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> globalKeywords = keywordConfigService.getActiveGlobalKeywords().stream()
                    .map(KeywordConfig::getKeyword)
                    .collect(Collectors.toList());
            List<String> localKeywords = List.of(); // 默认空列表
            
            if (gridArea != null && !gridArea.trim().isEmpty()) {
                localKeywords = keywordConfigService.getActiveLocalKeywordsByGridArea(gridArea).stream()
                        .map(KeywordConfig::getKeyword)
                        .collect(Collectors.toList());
            }
            
            response.put("success", true);
            response.put("global_keywords", String.join(",", globalKeywords));
            response.put("local_keywords", String.join(",", localKeywords));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取关键词配置失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取用户消息列表
     */
    @GetMapping("/messages")
    public ResponseEntity<Map<String, Object>> getUserMessages(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<User> userOpt = userService.findByUsername(username);
            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(response);
            }
            User user = userOpt.get();
            
            List<Message> messages = messageService.getMessagesByUserId(user.getId());
            
            // 简单分页处理
            int start = page * size;
            int end = Math.min(start + size, messages.size());
            List<Message> pagedMessages = messages.subList(Math.max(0, start), Math.max(0, end));
            
            response.put("success", true);
            response.put("messages", pagedMessages);
            response.put("total", messages.size());
            response.put("page", page);
            response.put("size", size);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取消息列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}