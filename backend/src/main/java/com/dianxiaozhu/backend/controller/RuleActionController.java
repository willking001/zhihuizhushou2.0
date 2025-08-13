package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.entity.RuleAction;
import com.dianxiaozhu.backend.service.RuleActionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 规则动作控制器
 */
@RestController
@RequestMapping("/api/rule-actions")
@RequiredArgsConstructor
@Slf4j
public class RuleActionController {

    private final RuleActionService ruleActionService;

    /**
     * 根据ID获取动作
     */
    @GetMapping("/{id}")
    public ResponseEntity<RuleAction> getActionById(@PathVariable Long id) {
        Optional<RuleAction> action = ruleActionService.getActionById(id);
        return action.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新动作
     */
    @PutMapping("/{id}")
    public ResponseEntity<RuleAction> updateAction(@PathVariable Long id, @RequestBody RuleAction action) {
        try {
            RuleAction updatedAction = ruleActionService.updateAction(id, action);
            return ResponseEntity.ok(updatedAction);
        } catch (Exception e) {
            log.error("更新规则动作失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除动作
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteAction(@PathVariable Long id) {
        try {
            ruleActionService.deleteAction(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "动作删除成功"));
        } catch (Exception e) {
            log.error("删除规则动作失败", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 根据动作类型获取动作列表
     */
    @GetMapping("/type/{actionType}")
    public ResponseEntity<List<RuleAction>> getActionsByType(@PathVariable String actionType) {
        List<RuleAction> actions = ruleActionService.getActionsByType(actionType);
        return ResponseEntity.ok(actions);
    }

    /**
     * 更新动作执行顺序
     */
    @PutMapping("/{id}/order")
    public ResponseEntity<RuleAction> updateActionOrder(
            @PathVariable Long id,
            @RequestParam Long ruleId,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer newOrder = request.get("executionOrder");
            RuleAction updatedAction = ruleActionService.updateActionOrder(ruleId, id, newOrder);
            return ResponseEntity.ok(updatedAction);
        } catch (Exception e) {
            log.error("更新动作执行顺序失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取动作类型枚举
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, String>>> getActionTypes() {
        List<Map<String, String>> types = List.of(
                Map.of("value", "FORWARD", "label", "消息转发"),
                Map.of("value", "AUTO_REPLY", "label", "自动回复"),
                Map.of("value", "NOTIFICATION", "label", "通知提醒"),
                Map.of("value", "LOG", "label", "记录日志"),
                Map.of("value", "WEBHOOK", "label", "Webhook调用"),
                Map.of("value", "EMAIL", "label", "邮件发送"),
                Map.of("value", "SMS", "label", "短信发送")
        );
        return ResponseEntity.ok(types);
    }

    /**
     * 获取动作配置模板
     */
    @GetMapping("/config-template/{actionType}")
    public ResponseEntity<Map<String, Object>> getActionConfigTemplate(@PathVariable String actionType) {
        Map<String, Object> template;
        
        switch (actionType.toUpperCase()) {
            case "FORWARD":
                template = Map.of(
                        "targetUrl", "",
                        "method", "POST",
                        "headers", Map.of(),
                        "timeout", 5000
                );
                break;
            case "AUTO_REPLY":
                template = Map.of(
                        "replyContent", "",
                        "replyType", "text",
                        "delay", 0
                );
                break;
            case "NOTIFICATION":
                template = Map.of(
                        "title", "",
                        "content", "",
                        "recipients", List.of(),
                        "priority", "normal"
                );
                break;
            case "WEBHOOK":
                template = Map.of(
                        "url", "",
                        "method", "POST",
                        "headers", Map.of("Content-Type", "application/json"),
                        "payload", Map.of(),
                        "timeout", 5000
                );
                break;
            case "EMAIL":
                template = Map.of(
                        "to", List.of(),
                        "cc", List.of(),
                        "subject", "",
                        "content", "",
                        "contentType", "text/plain"
                );
                break;
            case "SMS":
                template = Map.of(
                        "phoneNumbers", List.of(),
                        "content", "",
                        "template", ""
                );
                break;
            default:
                template = Map.of();
        }
        
        return ResponseEntity.ok(template);
    }
}