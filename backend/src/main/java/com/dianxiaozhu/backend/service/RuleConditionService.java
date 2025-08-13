package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.BusinessRule;
import com.dianxiaozhu.backend.entity.RuleCondition;
import com.dianxiaozhu.backend.repository.BusinessRuleRepository;
import com.dianxiaozhu.backend.repository.RuleConditionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 规则条件服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleConditionService {

    private final RuleConditionRepository ruleConditionRepository;
    private final BusinessRuleRepository businessRuleRepository;

    /**
     * 获取规则的所有条件
     * @param ruleId 规则ID
     * @return 条件列表
     */
    public List<RuleCondition> getConditionsByRuleId(Long ruleId) {
        return ruleConditionRepository.findByBusinessRuleId(ruleId);
    }

    /**
     * 根据ID获取条件
     * @param id 条件ID
     * @return 条件
     */
    public Optional<RuleCondition> getConditionById(Long id) {
        return ruleConditionRepository.findById(id);
    }

    /**
     * 创建规则条件
     * @param ruleId 规则ID
     * @param condition 条件信息
     * @return 创建的条件
     */
    @Transactional
    public RuleCondition createCondition(Long ruleId, RuleCondition condition) {
        Optional<BusinessRule> rule = businessRuleRepository.findById(ruleId);
        if (rule.isPresent()) {
            condition.setBusinessRule(rule.get());
            condition.setCreateTime(LocalDateTime.now());
            return ruleConditionRepository.save(condition);
        }
        throw new RuntimeException("规则不存在: " + ruleId);
    }

    /**
     * 更新规则条件
     * @param id 条件ID
     * @param condition 条件信息
     * @return 更新的条件
     */
    @Transactional
    public RuleCondition updateCondition(Long id, RuleCondition condition) {
        Optional<RuleCondition> existingCondition = ruleConditionRepository.findById(id);
        if (existingCondition.isPresent()) {
            RuleCondition conditionToUpdate = existingCondition.get();
            conditionToUpdate.setConditionType(condition.getConditionType());
            conditionToUpdate.setConditionValue(condition.getConditionValue());
            conditionToUpdate.setMatchMode(condition.getMatchMode());
            conditionToUpdate.setCaseSensitive(condition.getCaseSensitive());
            conditionToUpdate.setWeight(condition.getWeight());
            return ruleConditionRepository.save(conditionToUpdate);
        }
        throw new RuntimeException("条件不存在: " + id);
    }

    /**
     * 删除规则条件
     * @param id 条件ID
     */
    @Transactional
    public void deleteCondition(Long id) {
        if (ruleConditionRepository.existsById(id)) {
            ruleConditionRepository.deleteById(id);
        } else {
            throw new RuntimeException("条件不存在: " + id);
        }
    }

    /**
     * 批量创建规则条件
     * @param ruleId 规则ID
     * @param conditions 条件列表
     * @return 创建的条件列表
     */
    @Transactional
    public List<RuleCondition> createConditions(Long ruleId, List<RuleCondition> conditions) {
        Optional<BusinessRule> rule = businessRuleRepository.findById(ruleId);
        if (rule.isPresent()) {
            BusinessRule businessRule = rule.get();
            conditions.forEach(condition -> {
                condition.setBusinessRule(businessRule);
                condition.setCreateTime(LocalDateTime.now());
            });
            return ruleConditionRepository.saveAll(conditions);
        }
        throw new RuntimeException("规则不存在: " + ruleId);
    }

    /**
     * 删除规则的所有条件
     * @param ruleId 规则ID
     */
    @Transactional
    public void deleteConditionsByRuleId(Long ruleId) {
        ruleConditionRepository.deleteByBusinessRuleId(ruleId);
    }

    /**
     * 根据条件类型获取条件列表
     * @param conditionType 条件类型
     * @return 条件列表
     */
    public List<RuleCondition> getConditionsByType(String conditionType) {
        return ruleConditionRepository.findByConditionType(conditionType);
    }

    /**
     * 统计规则的条件数量
     * @param ruleId 规则ID
     * @return 条件数量
     */
    public long countConditionsByRuleId(Long ruleId) {
        return ruleConditionRepository.countByBusinessRuleId(ruleId);
    }
}