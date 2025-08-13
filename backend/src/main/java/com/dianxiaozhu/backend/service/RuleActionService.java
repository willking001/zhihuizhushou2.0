package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.BusinessRule;
import com.dianxiaozhu.backend.entity.RuleAction;
import com.dianxiaozhu.backend.repository.BusinessRuleRepository;
import com.dianxiaozhu.backend.repository.RuleActionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 规则动作服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleActionService {

    private final RuleActionRepository ruleActionRepository;
    private final BusinessRuleRepository businessRuleRepository;

    /**
     * 获取规则的所有动作
     * @param ruleId 规则ID
     * @return 动作列表
     */
    public List<RuleAction> getActionsByRuleId(Long ruleId) {
        return ruleActionRepository.findByBusinessRuleIdOrderByExecutionOrderAsc(ruleId);
    }

    /**
     * 根据ID获取动作
     * @param id 动作ID
     * @return 动作
     */
    public Optional<RuleAction> getActionById(Long id) {
        return ruleActionRepository.findById(id);
    }

    /**
     * 创建规则动作
     * @param ruleId 规则ID
     * @param action 动作信息
     * @return 创建的动作
     */
    @Transactional
    public RuleAction createAction(Long ruleId, RuleAction action) {
        Optional<BusinessRule> rule = businessRuleRepository.findById(ruleId);
        if (rule.isPresent()) {
            action.setBusinessRule(rule.get());
            action.setCreateTime(LocalDateTime.now());
            return ruleActionRepository.save(action);
        }
        throw new RuntimeException("规则不存在: " + ruleId);
    }

    /**
     * 更新规则动作
     * @param id 动作ID
     * @param action 动作信息
     * @return 更新的动作
     */
    @Transactional
    public RuleAction updateAction(Long id, RuleAction action) {
        Optional<RuleAction> existingAction = ruleActionRepository.findById(id);
        if (existingAction.isPresent()) {
            RuleAction actionToUpdate = existingAction.get();
            actionToUpdate.setActionType(action.getActionType());
            actionToUpdate.setActionConfig(action.getActionConfig());
            actionToUpdate.setExecutionOrder(action.getExecutionOrder());
            return ruleActionRepository.save(actionToUpdate);
        }
        throw new RuntimeException("动作不存在: " + id);
    }

    /**
     * 删除规则动作
     * @param id 动作ID
     */
    @Transactional
    public void deleteAction(Long id) {
        if (ruleActionRepository.existsById(id)) {
            ruleActionRepository.deleteById(id);
        } else {
            throw new RuntimeException("动作不存在: " + id);
        }
    }

    /**
     * 批量创建规则动作
     * @param ruleId 规则ID
     * @param actions 动作列表
     * @return 创建的动作列表
     */
    @Transactional
    public List<RuleAction> createActions(Long ruleId, List<RuleAction> actions) {
        Optional<BusinessRule> rule = businessRuleRepository.findById(ruleId);
        if (rule.isPresent()) {
            BusinessRule businessRule = rule.get();
            actions.forEach(action -> {
                action.setBusinessRule(businessRule);
                action.setCreateTime(LocalDateTime.now());
            });
            return ruleActionRepository.saveAll(actions);
        }
        throw new RuntimeException("规则不存在: " + ruleId);
    }

    /**
     * 删除规则的所有动作
     * @param ruleId 规则ID
     */
    @Transactional
    public void deleteActionsByRuleId(Long ruleId) {
        ruleActionRepository.deleteByBusinessRuleId(ruleId);
    }

    /**
     * 根据动作类型获取动作列表
     * @param actionType 动作类型
     * @return 动作列表
     */
    public List<RuleAction> getActionsByType(String actionType) {
        return ruleActionRepository.findByActionType(actionType);
    }

    /**
     * 统计规则的动作数量
     * @param ruleId 规则ID
     * @return 动作数量
     */
    public long countActionsByRuleId(Long ruleId) {
        return ruleActionRepository.countByBusinessRuleId(ruleId);
    }

    /**
     * 调整动作执行顺序
     * @param ruleId 规则ID
     * @param actionId 动作ID
     * @param newOrder 新的执行顺序
     * @return 更新的动作
     */
    @Transactional
    public RuleAction updateActionOrder(Long ruleId, Long actionId, Integer newOrder) {
        Optional<RuleAction> action = ruleActionRepository.findById(actionId);
        if (action.isPresent() && action.get().getBusinessRule().getId().equals(ruleId)) {
            RuleAction actionToUpdate = action.get();
            actionToUpdate.setExecutionOrder(newOrder);
            return ruleActionRepository.save(actionToUpdate);
        }
        throw new RuntimeException("动作不存在或不属于指定规则: " + actionId);
    }
}