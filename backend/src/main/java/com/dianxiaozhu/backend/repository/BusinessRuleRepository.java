package com.dianxiaozhu.backend.repository;

import com.dianxiaozhu.backend.entity.BusinessRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 业务规则Repository
 */
@Repository
public interface BusinessRuleRepository extends JpaRepository<BusinessRule, Long> {

    /**
     * 根据规则类型查找规则列表
     * @param ruleType 规则类型
     * @return 规则列表
     */
    List<BusinessRule> findByRuleType(String ruleType);

    /**
     * 根据启用状态查找规则列表
     * @param enabled 启用状态
     * @return 规则列表
     */
    List<BusinessRule> findByEnabled(Boolean enabled);

    /**
     * 根据规则类型和启用状态查找规则列表，并按优先级排序
     * @param ruleType 规则类型
     * @param enabled 启用状态
     * @return 规则列表
     */
    List<BusinessRule> findByRuleTypeAndEnabledOrderByPriorityAsc(String ruleType, Boolean enabled);

    /**
     * 根据启用状态查找规则列表，并按优先级排序
     * @param enabled 启用状态
     * @return 规则列表
     */
    List<BusinessRule> findByEnabledOrderByPriorityAsc(Boolean enabled);

    /**
     * 根据规则名称查找规则
     * @param ruleName 规则名称
     * @return 规则
     */
    BusinessRule findByRuleName(String ruleName);

    /**
     * 分页查询规则，支持按规则类型和启用状态过滤
     * @param ruleType 规则类型（可选）
     * @param enabled 启用状态（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT br FROM BusinessRule br WHERE " +
           "(:ruleType IS NULL OR br.ruleType = :ruleType) AND " +
           "(:enabled IS NULL OR br.enabled = :enabled)")
    Page<BusinessRule> findByConditions(@Param("ruleType") String ruleType,
                                       @Param("enabled") Boolean enabled,
                                       Pageable pageable);

    /**
     * 根据创建者ID查找规则列表
     * @param creatorId 创建者ID
     * @return 规则列表
     */
    List<BusinessRule> findByCreatorId(Long creatorId);

    /**
     * 统计启用的规则数量
     * @return 启用的规则数量
     */
    long countByEnabled(Boolean enabled);

    /**
     * 根据规则类型统计数量
     * @param ruleType 规则类型
     * @return 规则数量
     */
    long countByRuleType(String ruleType);
}