package com.dianxiaozhu.backend.repository;

import com.dianxiaozhu.backend.entity.RuleChain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 规则链Repository
 */
@Repository
public interface RuleChainRepository extends JpaRepository<RuleChain, Long> {

    /**
     * 根据启用状态查找规则链列表
     * @param enabled 启用状态
     * @return 规则链列表
     */
    List<RuleChain> findByEnabled(Boolean enabled);

    /**
     * 根据规则链名称查找规则链
     * @param chainName 规则链名称
     * @return 规则链
     */
    RuleChain findByChainName(String chainName);

    /**
     * 根据创建者ID查找规则链列表
     * @param creatorId 创建者ID
     * @return 规则链列表
     */
    List<RuleChain> findByCreatorId(Long creatorId);

    /**
     * 统计启用的规则链数量
     * @param enabled 启用状态
     * @return 规则链数量
     */
    long countByEnabled(Boolean enabled);
}