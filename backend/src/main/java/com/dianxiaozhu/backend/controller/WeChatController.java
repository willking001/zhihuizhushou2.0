package com.dianxiaozhu.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信消息转发控制器
 * 处理Python客户端的微信消息发送请求
 */
@RestController
@RequestMapping("/api/wx")
@CrossOrigin(origins = "*")
public class WeChatController {

    /**
     * 发送微信消息接口
     * Python客户端调用此接口发送微信消息
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @RequestParam String message,
            @RequestParam String to) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证参数
            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消息内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (to == null || to.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "接收者不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 记录发送请求
            System.out.println("[微信转发] 时间: " + LocalDateTime.now());
            System.out.println("[微信转发] 目标: " + to);
            System.out.println("[微信转发] 内容: " + message);
            
            // TODO: 这里应该集成实际的微信发送逻辑
            // 可以集成以下方案之一：
            // 1. 微信机器人API (如 itchat, wechaty)
            // 2. 企业微信API
            // 3. 微信公众号API
            // 4. 第三方微信接口服务
            
            // 模拟发送成功
            boolean sendSuccess = simulateWeChatSend(message, to);
            
            if (sendSuccess) {
                response.put("success", true);
                response.put("message", "微信消息发送成功");
                response.put("timestamp", LocalDateTime.now());
                response.put("target", to);
                response.put("content_length", message.length());
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "微信消息发送失败");
                response.put("error_code", "SEND_FAILED");
                
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "发送微信消息时发生错误: " + e.getMessage());
            response.put("error_code", "INTERNAL_ERROR");
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 批量发送微信消息
     */
    @PostMapping("/send/batch")
    public ResponseEntity<Map<String, Object>> sendBatchMessages(
            @RequestBody Map<String, Object> requestData) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String message = (String) requestData.get("message");
            @SuppressWarnings("unchecked")
            java.util.List<String> targets = (java.util.List<String>) requestData.get("targets");
            
            if (message == null || message.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消息内容不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (targets == null || targets.isEmpty()) {
                response.put("success", false);
                response.put("message", "接收者列表不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 批量发送
            int successCount = 0;
            int failCount = 0;
            java.util.List<Map<String, Object>> results = new java.util.ArrayList<>();
            
            for (String target : targets) {
                try {
                    boolean sendSuccess = simulateWeChatSend(message, target);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("target", target);
                    result.put("success", sendSuccess);
                    result.put("timestamp", LocalDateTime.now());
                    
                    if (sendSuccess) {
                        successCount++;
                        result.put("message", "发送成功");
                    } else {
                        failCount++;
                        result.put("message", "发送失败");
                    }
                    
                    results.add(result);
                    
                } catch (Exception e) {
                    failCount++;
                    Map<String, Object> result = new HashMap<>();
                    result.put("target", target);
                    result.put("success", false);
                    result.put("message", "发送异常: " + e.getMessage());
                    result.put("timestamp", LocalDateTime.now());
                    results.add(result);
                }
            }
            
            response.put("success", true);
            response.put("message", String.format("批量发送完成，成功: %d, 失败: %d", successCount, failCount));
            response.put("total", targets.size());
            response.put("success_count", successCount);
            response.put("fail_count", failCount);
            response.put("results", results);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量发送微信消息时发生错误: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取微信发送状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getWeChatStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // TODO: 实际实现中应该检查微信客户端连接状态
            
            response.put("success", true);
            response.put("status", "connected"); // connected, disconnected, error
            response.put("message", "微信服务正常");
            response.put("timestamp", LocalDateTime.now());
            response.put("version", "1.0.0");
            response.put("capabilities", java.util.Arrays.asList(
                "send_text", "send_image", "send_file", "batch_send"
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取微信状态失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 测试微信连接
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testWeChatConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // TODO: 实际实现中应该测试微信客户端连接
            
            boolean isConnected = true; // 模拟连接成功
            
            if (isConnected) {
                response.put("success", true);
                response.put("message", "微信连接测试成功");
                response.put("status", "connected");
            } else {
                response.put("success", false);
                response.put("message", "微信连接测试失败");
                response.put("status", "disconnected");
            }
            
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "微信连接测试异常: " + e.getMessage());
            response.put("status", "error");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 模拟微信发送（开发阶段使用）
     * 实际部署时应该替换为真实的微信发送逻辑
     */
    private boolean simulateWeChatSend(String message, String to) {
        try {
            // 模拟发送延迟
            Thread.sleep(100);
            
            // 模拟90%的成功率
            double random = Math.random();
            boolean success = random > 0.1;
            
            if (success) {
                System.out.println(String.format("[模拟微信发送成功] 目标: %s, 内容: %s", to, message.substring(0, Math.min(50, message.length()))));
            } else {
                System.out.println(String.format("[模拟微信发送失败] 目标: %s", to));
            }
            
            return success;
            
        } catch (Exception e) {
            System.err.println("模拟微信发送异常: " + e.getMessage());
            return false;
        }
    }
}