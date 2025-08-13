package com.dianxiaozhu.backend.repository;

import com.dianxiaozhu.backend.entity.RuleChainRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 规则链关联Repository
 */
@Repository
public interface RuleChainRelationRepository extends JpaRepository<RuleChainRelation, Long> {

    /**
     * 根据规则链ID查找关联列表，按执行顺序排序
     * @param chainId 规则链ID
     * @return 关联列表
     */
    List<RuleChainRelation> findByRuleChainIdOrderByExecutionOrderAsc(Long chainId);

    /**
     * 根据规则ID查找关联列表
     * @param ruleId 规则ID
     * @return 关联列表
     */
    List<RuleChainRelation> findByBusinessRuleId(Long ruleId);

    /**
     * 根据规则链ID和规则ID查找关联
     * @param chainId 规则链ID
     * @param ruleId 规则ID
     * @return 关联
     */
    Optional<RuleChainRelation> findByRuleChainIdAndBusinessRuleId(Long chainId, Long ruleId);

    /**
     * 根据规则链ID删除所有关联
     * @param chainId 规则链ID
     */
    void deleteByRuleChainId(Long chainId);

    /**
     * 根据规则ID删除所有关联
     * @param ruleId 规则ID
     */
    void deleteByBusinessRuleId(Long ruleId);

    /**
     * 统计规则链中的规则数量
     * @param chainId 规则链ID
     * @return 规则数量
     */
    long countByRuleChainId(Long chainId);

    /**
     * 检查规则是否已在规则链中
     * @param chainId 规则链ID
     * @param ruleId 规则ID
     * @return 是否存在
     */
    boolean existsByRuleChainIdAndBusinessRuleId(Long chainId, Long ruleId);



    /**
     * 根据规则链ID和规则ID删除关联关系
     * @param ruleChainId 规则链ID
     * @param ruleId 规则ID
     */
    @Modifying
    @Query("DELETE FROM RuleChainRelation r WHERE r.ruleChain.id = :ruleChainId AND r.businessRule.id = :ruleId")
    void deleteByRuleChainIdAndBusinessRuleId(@Param("ruleChainId") Long ruleChainId, @Param("ruleId") Long ruleId);
}