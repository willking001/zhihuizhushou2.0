package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.entity.BusinessRule;
import com.dianxiaozhu.backend.entity.RuleAction;
import com.dianxiaozhu.backend.entity.RuleCondition;
import com.dianxiaozhu.backend.service.BusinessRuleService;
import com.dianxiaozhu.backend.service.RuleActionService;
import com.dianxiaozhu.backend.service.RuleConditionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 业务规则控制器
 */
@RestController
@RequestMapping("/api/business-rules")
@RequiredArgsConstructor
@Slf4j
public class BusinessRuleController {

    private final BusinessRuleService businessRuleService;
    private final RuleConditionService ruleConditionService;
    private final RuleActionService ruleActionService;

    /**
     * 获取所有业务规则
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRules() {
        try {
            List<BusinessRule> rules = businessRuleService.getAllRules();
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 20000);
            response.put("message", "查询成功");
            response.put("data", rules);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取所有业务规则失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 50000);
            response.put("message", "查询失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 分页查询业务规则
     */
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getRulesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String ruleType,
            @RequestParam(required = false) Boolean enabled) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<BusinessRule> rules = businessRuleService.getRulesByConditions(ruleType, enabled, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 20000);
            response.put("message", "查询成功");
            response.put("data", rules);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("分页查询业务规则失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("code", 50000);
            response.put("message", "查询失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 根据ID获取业务规则
     */
    @GetMapping("/{id}")
    public ResponseEntity<BusinessRule> getRuleById(@PathVariable Long id) {
        Optional<BusinessRule> rule = businessRuleService.getRuleById(id);
        return rule.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建业务规则
     */
    @PostMapping
    public ResponseEntity<BusinessRule> createRule(@RequestBody BusinessRule rule) {
        try {
            BusinessRule createdRule = businessRuleService.createRule(rule);
            return ResponseEntity.ok(createdRule);
        } catch (Exception e) {
            log.error("创建业务规则失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新业务规则
     */
    @PutMapping("/{id}")
    public ResponseEntity<BusinessRule> updateRule(@PathVariable Long id, @RequestBody BusinessRule rule) {
        try {
            BusinessRule updatedRule = businessRuleService.updateRule(id, rule);
            return ResponseEntity.ok(updatedRule);
        } catch (Exception e) {
            log.error("更新业务规则失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除业务规则
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRule(@PathVariable Long id) {
        try {
            businessRuleService.deleteRule(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "规则删除成功"));
        } catch (Exception e) {
            log.error("删除业务规则失败", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 启用/禁用规则
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<BusinessRule> toggleRuleStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        try {
            Boolean enabled = request.get("enabled");
            BusinessRule updatedRule = businessRuleService.toggleRuleStatus(id, enabled);
            return ResponseEntity.ok(updatedRule);
        } catch (Exception e) {
            log.error("切换规则状态失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取启用的规则列表
     */
    @GetMapping("/enabled")
    public ResponseEntity<List<BusinessRule>> getEnabledRules() {
        List<BusinessRule> rules = businessRuleService.getEnabledRules();
        return ResponseEntity.ok(rules);
    }

    /**
     * 根据类型获取启用的规则列表
     */
    @GetMapping("/enabled/{ruleType}")
    public ResponseEntity<List<BusinessRule>> getEnabledRulesByType(@PathVariable String ruleType) {
        List<BusinessRule> rules = businessRuleService.getEnabledRulesByType(ruleType);
        return ResponseEntity.ok(rules);
    }

    /**
     * 获取规则的条件列表
     */
    @GetMapping("/{id}/conditions")
    public ResponseEntity<List<RuleCondition>> getRuleConditions(@PathVariable Long id) {
        List<RuleCondition> conditions = ruleConditionService.getConditionsByRuleId(id);
        return ResponseEntity.ok(conditions);
    }

    /**
     * 为规则添加条件
     */
    @PostMapping("/{id}/conditions")
    public ResponseEntity<RuleCondition> addRuleCondition(@PathVariable Long id, @RequestBody RuleCondition condition) {
        try {
            RuleCondition createdCondition = ruleConditionService.createCondition(id, condition);
            return ResponseEntity.ok(createdCondition);
        } catch (Exception e) {
            log.error("添加规则条件失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 批量添加规则条件
     */
    @PostMapping("/{id}/conditions/batch")
    public ResponseEntity<List<RuleCondition>> addRuleConditions(@PathVariable Long id, @RequestBody List<RuleCondition> conditions) {
        try {
            List<RuleCondition> createdConditions = ruleConditionService.createConditions(id, conditions);
            return ResponseEntity.ok(createdConditions);
        } catch (Exception e) {
            log.error("批量添加规则条件失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取规则的动作列表
     */
    @GetMapping("/{id}/actions")
    public ResponseEntity<List<RuleAction>> getRuleActions(@PathVariable Long id) {
        List<RuleAction> actions = ruleActionService.getActionsByRuleId(id);
        return ResponseEntity.ok(actions);
    }

    /**
     * 为规则添加动作
     */
    @PostMapping("/{id}/actions")
    public ResponseEntity<RuleAction> addRuleAction(@PathVariable Long id, @RequestBody RuleAction action) {
        try {
            RuleAction createdAction = ruleActionService.createAction(id, action);
            return ResponseEntity.ok(createdAction);
        } catch (Exception e) {
            log.error("添加规则动作失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 批量添加规则动作
     */
    @PostMapping("/{id}/actions/batch")
    public ResponseEntity<List<RuleAction>> addRuleActions(@PathVariable Long id, @RequestBody List<RuleAction> actions) {
        try {
            List<RuleAction> createdActions = ruleActionService.createActions(id, actions);
            return ResponseEntity.ok(createdActions);
        } catch (Exception e) {
            log.error("批量添加规则动作失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取规则统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<BusinessRuleService.RuleStatistics> getStatistics() {
        BusinessRuleService.RuleStatistics statistics = businessRuleService.getStatistics();
        return ResponseEntity.ok(statistics);
    }
}