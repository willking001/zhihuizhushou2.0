package com.dianxiaozhu.backend.repository;

import com.dianxiaozhu.backend.entity.RuleCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 规则条件Repository
 */
@Repository
public interface RuleConditionRepository extends JpaRepository<RuleCondition, Long> {

    /**
     * 根据规则ID查找条件列表
     * @param ruleId 规则ID
     * @return 条件列表
     */
    List<RuleCondition> findByBusinessRuleId(Long ruleId);

    /**
     * 根据条件类型查找条件列表
     * @param conditionType 条件类型
     * @return 条件列表
     */
    List<RuleCondition> findByConditionType(String conditionType);

    /**
     * 根据规则ID和条件类型查找条件列表
     * @param ruleId 规则ID
     * @param conditionType 条件类型
     * @return 条件列表
     */
    List<RuleCondition> findByBusinessRuleIdAndConditionType(Long ruleId, String conditionType);

    /**
     * 根据规则ID删除所有条件
     * @param ruleId 规则ID
     */
    void deleteByBusinessRuleId(Long ruleId);

    /**
     * 统计规则的条件数量
     * @param ruleId 规则ID
     * @return 条件数量
     */
    long countByBusinessRuleId(Long ruleId);
}