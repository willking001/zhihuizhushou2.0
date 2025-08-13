package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.entity.RuleChain;
import com.dianxiaozhu.backend.entity.RuleChainRelation;
import com.dianxiaozhu.backend.service.RuleChainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 规则链控制器
 */
@RestController
@RequestMapping("/api/rule-chains")
@RequiredArgsConstructor
@Slf4j
public class RuleChainController {

    private final RuleChainService ruleChainService;

    /**
     * 获取所有规则链
     */
    @GetMapping
    public ResponseEntity<List<RuleChain>> getAllChains() {
        List<RuleChain> chains = ruleChainService.getAllChains();
        return ResponseEntity.ok(chains);
    }

    /**
     * 获取启用的规则链
     */
    @GetMapping("/enabled")
    public ResponseEntity<List<RuleChain>> getEnabledChains() {
        List<RuleChain> chains = ruleChainService.getEnabledChains();
        return ResponseEntity.ok(chains);
    }

    /**
     * 根据ID获取规则链
     */
    @GetMapping("/{id}")
    public ResponseEntity<RuleChain> getChainById(@PathVariable Long id) {
        Optional<RuleChain> chain = ruleChainService.getChainById(id);
        return chain.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建规则链
     */
    @PostMapping
    public ResponseEntity<RuleChain> createChain(@RequestBody RuleChain chain) {
        try {
            RuleChain createdChain = ruleChainService.createChain(chain);
            return ResponseEntity.ok(createdChain);
        } catch (Exception e) {
            log.error("创建规则链失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新规则链
     */
    @PutMapping("/{id}")
    public ResponseEntity<RuleChain> updateChain(@PathVariable Long id, @RequestBody RuleChain chain) {
        try {
            RuleChain updatedChain = ruleChainService.updateChain(id, chain);
            return ResponseEntity.ok(updatedChain);
        } catch (Exception e) {
            log.error("更新规则链失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除规则链
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteChain(@PathVariable Long id) {
        try {
            ruleChainService.deleteChain(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "规则链删除成功"));
        } catch (Exception e) {
            log.error("删除规则链失败", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 启用/禁用规则链
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<RuleChain> toggleChainStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        try {
            Boolean enabled = request.get("enabled");
            RuleChain updatedChain = ruleChainService.toggleChainStatus(id, enabled);
            return ResponseEntity.ok(updatedChain);
        } catch (Exception e) {
            log.error("切换规则链状态失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取规则链的关联关系
     */
    @GetMapping("/{id}/relations")
    public ResponseEntity<List<RuleChainRelation>> getChainRelations(@PathVariable Long id) {
        List<RuleChainRelation> relations = ruleChainService.getChainRelations(id);
        return ResponseEntity.ok(relations);
    }

    /**
     * 添加规则到规则链
     */
    @PostMapping("/{id}/rules")
    public ResponseEntity<RuleChainRelation> addRuleToChain(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            Long ruleId = Long.valueOf(request.get("ruleId").toString());
            Integer executionOrder = Integer.valueOf(request.get("executionOrder").toString());
            String conditionLogic = request.get("conditionLogic").toString();
            
            RuleChainRelation relation = ruleChainService.addRuleToChain(id, ruleId, executionOrder, conditionLogic);
            return ResponseEntity.ok(relation);
        } catch (Exception e) {
            log.error("添加规则到规则链失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 从规则链中移除规则
     */
    @DeleteMapping("/{chainId}/rules/{ruleId}")
    public ResponseEntity<Map<String, Object>> removeRuleFromChain(
            @PathVariable Long chainId,
            @PathVariable Long ruleId) {
        try {
            ruleChainService.removeRuleFromChain(chainId, ruleId);
            return ResponseEntity.ok(Map.of("success", true, "message", "规则从规则链中移除成功"));
        } catch (Exception e) {
            log.error("从规则链中移除规则失败", e);
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * 更新规则在规则链中的执行顺序
     */
    @PutMapping("/{chainId}/rules/{ruleId}/order")
    public ResponseEntity<RuleChainRelation> updateRuleOrder(
            @PathVariable Long chainId,
            @PathVariable Long ruleId,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer newOrder = request.get("executionOrder");
            RuleChainRelation updatedRelation = ruleChainService.updateRuleOrder(chainId, ruleId, newOrder);
            return ResponseEntity.ok(updatedRelation);
        } catch (Exception e) {
            log.error("更新规则执行顺序失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新规则在规则链中的条件逻辑
     */
    @PutMapping("/{chainId}/rules/{ruleId}/condition-logic")
    public ResponseEntity<RuleChainRelation> updateRuleConditionLogic(
            @PathVariable Long chainId,
            @PathVariable Long ruleId,
            @RequestBody Map<String, String> request) {
        try {
            String conditionLogic = request.get("conditionLogic");
            RuleChainRelation updatedRelation = ruleChainService.updateRuleConditionLogic(chainId, ruleId, conditionLogic);
            return ResponseEntity.ok(updatedRelation);
        } catch (Exception e) {
            log.error("更新规则条件逻辑失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取规则链统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<RuleChainService.ChainStatistics> getStatistics() {
        RuleChainService.ChainStatistics statistics = ruleChainService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取条件逻辑枚举
     */
    @GetMapping("/condition-logics")
    public ResponseEntity<List<Map<String, String>>> getConditionLogics() {
        List<Map<String, String>> logics = List.of(
                Map.of("value", "AND", "label", "与（AND）"),
                Map.of("value", "OR", "label", "或（OR）")
        );
        return ResponseEntity.ok(logics);
    }
}