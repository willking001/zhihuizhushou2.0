package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.BusinessRule;
import com.dianxiaozhu.backend.entity.RuleAction;
import com.dianxiaozhu.backend.entity.RuleCondition;
import com.dianxiaozhu.backend.repository.BusinessRuleRepository;
import com.dianxiaozhu.backend.repository.RuleActionRepository;
import com.dianxiaozhu.backend.repository.RuleConditionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 业务规则服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BusinessRuleService {

    private final BusinessRuleRepository businessRuleRepository;
    private final RuleConditionRepository ruleConditionRepository;
    private final RuleActionRepository ruleActionRepository;

    /**
     * 获取所有业务规则
     * @return 规则列表
     */
    public List<BusinessRule> getAllRules() {
        return businessRuleRepository.findAll();
    }

    /**
     * 分页查询业务规则
     * @param pageable 分页参数
     * @return 分页结果
     */
    public Page<BusinessRule> getRules(Pageable pageable) {
        return businessRuleRepository.findAll(pageable);
    }

    /**
     * 根据条件分页查询业务规则
     * @param ruleType 规则类型
     * @param enabled 启用状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    public Page<BusinessRule> getRulesByConditions(String ruleType, Boolean enabled, Pageable pageable) {
        return businessRuleRepository.findByConditions(ruleType, enabled, pageable);
    }

    /**
     * 根据ID获取业务规则
     * @param id 规则ID
     * @return 规则
     */
    public Optional<BusinessRule> getRuleById(Long id) {
        return businessRuleRepository.findById(id);
    }

    /**
     * 根据规则名称获取业务规则
     * @param ruleName 规则名称
     * @return 规则
     */
    public BusinessRule getRuleByName(String ruleName) {
        return businessRuleRepository.findByRuleName(ruleName);
    }

    /**
     * 获取启用的规则列表，按优先级排序
     * @return 规则列表
     */
    public List<BusinessRule> getEnabledRules() {
        return businessRuleRepository.findByEnabledOrderByPriorityAsc(true);
    }

    /**
     * 根据规则类型获取启用的规则列表
     * @param ruleType 规则类型
     * @return 规则列表
     */
    public List<BusinessRule> getEnabledRulesByType(String ruleType) {
        return businessRuleRepository.findByRuleTypeAndEnabledOrderByPriorityAsc(ruleType, true);
    }

    /**
     * 创建业务规则
     * @param rule 规则信息
     * @return 创建的规则
     */
    @Transactional
    public BusinessRule createRule(BusinessRule rule) {
        rule.setCreateTime(LocalDateTime.now());
        rule.setUpdateTime(LocalDateTime.now());
        return businessRuleRepository.save(rule);
    }

    /**
     * 更新业务规则
     * @param id 规则ID
     * @param rule 规则信息
     * @return 更新的规则
     */
    @Transactional
    public BusinessRule updateRule(Long id, BusinessRule rule) {
        Optional<BusinessRule> existingRule = businessRuleRepository.findById(id);
        if (existingRule.isPresent()) {
            BusinessRule ruleToUpdate = existingRule.get();
            ruleToUpdate.setRuleName(rule.getRuleName());
            ruleToUpdate.setRuleDescription(rule.getRuleDescription());
            ruleToUpdate.setRuleType(rule.getRuleType());
            ruleToUpdate.setPriority(rule.getPriority());
            ruleToUpdate.setEnabled(rule.getEnabled());
            ruleToUpdate.setStartTime(rule.getStartTime());
            ruleToUpdate.setEndTime(rule.getEndTime());
            ruleToUpdate.setEffectiveDays(rule.getEffectiveDays());
            ruleToUpdate.setRemark(rule.getRemark());
            ruleToUpdate.setUpdaterId(rule.getUpdaterId());
            ruleToUpdate.setUpdateTime(LocalDateTime.now());
            return businessRuleRepository.save(ruleToUpdate);
        }
        throw new RuntimeException("规则不存在: " + id);
    }

    /**
     * 删除业务规则
     * @param id 规则ID
     */
    @Transactional
    public void deleteRule(Long id) {
        if (businessRuleRepository.existsById(id)) {
            businessRuleRepository.deleteById(id);
        } else {
            throw new RuntimeException("规则不存在: " + id);
        }
    }

    /**
     * 启用/禁用规则
     * @param id 规则ID
     * @param enabled 启用状态
     * @return 更新的规则
     */
    @Transactional
    public BusinessRule toggleRuleStatus(Long id, Boolean enabled) {
        Optional<BusinessRule> rule = businessRuleRepository.findById(id);
        if (rule.isPresent()) {
            BusinessRule ruleToUpdate = rule.get();
            ruleToUpdate.setEnabled(enabled);
            ruleToUpdate.setUpdateTime(LocalDateTime.now());
            return businessRuleRepository.save(ruleToUpdate);
        }
        throw new RuntimeException("规则不存在: " + id);
    }

    /**
     * 获取规则的条件列表
     * @param ruleId 规则ID
     * @return 条件列表
     */
    public List<RuleCondition> getRuleConditions(Long ruleId) {
        return ruleConditionRepository.findByBusinessRuleId(ruleId);
    }

    /**
     * 获取规则的动作列表
     * @param ruleId 规则ID
     * @return 动作列表
     */
    public List<RuleAction> getRuleActions(Long ruleId) {
        return ruleActionRepository.findByBusinessRuleIdOrderByExecutionOrderAsc(ruleId);
    }

    /**
     * 统计规则数量
     * @return 规则统计信息
     */
    public RuleStatistics getStatistics() {
        long totalRules = businessRuleRepository.count();
        long enabledRules = businessRuleRepository.countByEnabled(true);
        long disabledRules = businessRuleRepository.countByEnabled(false);
        
        return RuleStatistics.builder()
                .totalRules(totalRules)
                .enabledRules(enabledRules)
                .disabledRules(disabledRules)
                .build();
    }

    /**
     * 规则统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class RuleStatistics {
        private long totalRules;
        private long enabledRules;
        private long disabledRules;
    }
}