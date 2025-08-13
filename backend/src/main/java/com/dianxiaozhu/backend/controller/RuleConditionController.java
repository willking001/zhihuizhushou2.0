package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.entity.RuleCondition;
import com.dianxiaozhu.backend.service.RuleConditionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 规则条件控制器
 */
@RestController
@RequestMapping("/api/rule-conditions")
@RequiredArgsConstructor
@Slf4j
public class RuleConditionController {

    private final RuleConditionService ruleConditionService;

    /**
     * 根据ID获取条件
     */
    @GetMapping("/{id}")
    public ResponseEntity<RuleCondition> getConditionById(@PathVariable Long id) {
        Optional<RuleCondition> condition = ruleConditionService.getConditionById(id);
        return condition.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新条件
     */
    @PutMapping("/{id}")
    public ResponseEntity<RuleCondition> updateCondition(@PathVariable Long id, @RequestBody RuleCondition condition) {
        try {
            RuleCondition updatedCondition = ruleConditionService.updateCondition(id, condition);
            return ResponseEntity.ok(updatedCondition);
        } catch (Exception e) {
            log.error("更新规则条件失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除条件
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCondition(@PathVariable Long id) {
        try {
            ruleConditionService.deleteCondition(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "条件删除成功"));
        } catch (Exception e) {
            log.error("删除规则条件失败", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 根据条件类型获取条件列表
     */
    @GetMapping("/type/{conditionType}")
    public ResponseEntity<List<RuleCondition>> getConditionsByType(@PathVariable String conditionType) {
        List<RuleCondition> conditions = ruleConditionService.getConditionsByType(conditionType);
        return ResponseEntity.ok(conditions);
    }

    /**
     * 获取条件类型枚举
     */
    @GetMapping("/types")
    public ResponseEntity<List<Map<String, String>>> getConditionTypes() {
        List<Map<String, String>> types = List.of(
                Map.of("value", "KEYWORD", "label", "关键词匹配"),
                Map.of("value", "PHRASE", "label", "短语匹配"),
                Map.of("value", "REGEX", "label", "正则表达式"),
                Map.of("value", "EXACT", "label", "精确匹配"),
                Map.of("value", "CONTAINS", "label", "包含匹配")
        );
        return ResponseEntity.ok(types);
    }

    /**
     * 获取匹配模式枚举
     */
    @GetMapping("/match-modes")
    public ResponseEntity<List<Map<String, String>>> getMatchModes() {
        List<Map<String, String>> modes = List.of(
                Map.of("value", "FULL", "label", "完全匹配"),
                Map.of("value", "PARTIAL", "label", "部分匹配"),
                Map.of("value", "START", "label", "开头匹配"),
                Map.of("value", "END", "label", "结尾匹配")
        );
        return ResponseEntity.ok(modes);
    }
}