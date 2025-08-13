package com.dianxiaozhu.backend.repository;

import com.dianxiaozhu.backend.entity.RuleAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 规则动作Repository
 */
@Repository
public interface RuleActionRepository extends JpaRepository<RuleAction, Long> {

    /**
     * 根据规则ID查找动作列表，按执行顺序排序
     * @param ruleId 规则ID
     * @return 动作列表
     */
    List<RuleAction> findByBusinessRuleIdOrderByExecutionOrderAsc(Long ruleId);

    /**
     * 根据动作类型查找动作列表
     * @param actionType 动作类型
     * @return 动作列表
     */
    List<RuleAction> findByActionType(String actionType);

    /**
     * 根据规则ID和动作类型查找动作列表
     * @param ruleId 规则ID
     * @param actionType 动作类型
     * @return 动作列表
     */
    List<RuleAction> findByBusinessRuleIdAndActionType(Long ruleId, String actionType);

    /**
     * 根据规则ID删除所有动作
     * @param ruleId 规则ID
     */
    void deleteByBusinessRuleId(Long ruleId);

    /**
     * 统计规则的动作数量
     * @param ruleId 规则ID
     * @return 动作数量
     */
    long countByBusinessRuleId(Long ruleId);
}