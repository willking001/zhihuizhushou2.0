package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.RuleChain;
import com.dianxiaozhu.backend.entity.RuleChainRelation;
import com.dianxiaozhu.backend.repository.RuleChainRepository;
import com.dianxiaozhu.backend.repository.RuleChainRelationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 规则链服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RuleChainService {

    private final RuleChainRepository ruleChainRepository;
    private final RuleChainRelationRepository ruleChainRelationRepository;

    /**
     * 获取所有规则链
     * @return 规则链列表
     */
    public List<RuleChain> getAllChains() {
        return ruleChainRepository.findAll();
    }

    /**
     * 获取启用的规则链
     * @return 启用的规则链列表
     */
    public List<RuleChain> getEnabledChains() {
        return ruleChainRepository.findByEnabled(true);
    }

    /**
     * 根据ID获取规则链
     * @param id 规则链ID
     * @return 规则链
     */
    public Optional<RuleChain> getChainById(Long id) {
        return ruleChainRepository.findById(id);
    }

    /**
     * 根据名称获取规则链
     * @param chainName 规则链名称
     * @return 规则链
     */
    public RuleChain getChainByName(String chainName) {
        return ruleChainRepository.findByChainName(chainName);
    }

    /**
     * 创建规则链
     * @param chain 规则链信息
     * @return 创建的规则链
     */
    @Transactional
    public RuleChain createChain(RuleChain chain) {
        chain.setCreateTime(LocalDateTime.now());
        chain.setUpdateTime(LocalDateTime.now());
        return ruleChainRepository.save(chain);
    }

    /**
     * 更新规则链
     * @param id 规则链ID
     * @param chain 规则链信息
     * @return 更新的规则链
     */
    @Transactional
    public RuleChain updateChain(Long id, RuleChain chain) {
        Optional<RuleChain> existingChain = ruleChainRepository.findById(id);
        if (existingChain.isPresent()) {
            RuleChain chainToUpdate = existingChain.get();
            chainToUpdate.setChainName(chain.getChainName());
            chainToUpdate.setChainDescription(chain.getChainDescription());
            chainToUpdate.setEnabled(chain.getEnabled());
            chainToUpdate.setUpdaterId(chain.getUpdaterId());
            chainToUpdate.setUpdateTime(LocalDateTime.now());
            return ruleChainRepository.save(chainToUpdate);
        }
        throw new RuntimeException("规则链不存在: " + id);
    }

    /**
     * 删除规则链
     * @param id 规则链ID
     */
    @Transactional
    public void deleteChain(Long id) {
        if (ruleChainRepository.existsById(id)) {
            // 先删除规则链关联关系
            ruleChainRelationRepository.deleteByRuleChainId(id);
            // 再删除规则链
            ruleChainRepository.deleteById(id);
        } else {
            throw new RuntimeException("规则链不存在: " + id);
        }
    }

    /**
     * 启用/禁用规则链
     * @param id 规则链ID
     * @param enabled 启用状态
     * @return 更新的规则链
     */
    @Transactional
    public RuleChain toggleChainStatus(Long id, Boolean enabled) {
        Optional<RuleChain> chain = ruleChainRepository.findById(id);
        if (chain.isPresent()) {
            RuleChain chainToUpdate = chain.get();
            chainToUpdate.setEnabled(enabled);
            chainToUpdate.setUpdateTime(LocalDateTime.now());
            return ruleChainRepository.save(chainToUpdate);
        }
        throw new RuntimeException("规则链不存在: " + id);
    }

    /**
     * 获取规则链的关联关系
     * @param chainId 规则链ID
     * @return 关联关系列表
     */
    public List<RuleChainRelation> getChainRelations(Long chainId) {
        return ruleChainRelationRepository.findByRuleChainIdOrderByExecutionOrderAsc(chainId);
    }

    /**
     * 添加规则到规则链
     * @param chainId 规则链ID
     * @param ruleId 规则ID
     * @param executionOrder 执行顺序
     * @param conditionLogic 条件逻辑
     * @return 创建的关联关系
     */
    @Transactional
    public RuleChainRelation addRuleToChain(Long chainId, Long ruleId, Integer executionOrder, String conditionLogic) {
        // 检查是否已存在关联
        if (ruleChainRelationRepository.existsByRuleChainIdAndBusinessRuleId(chainId, ruleId)) {
            throw new RuntimeException("规则已存在于规则链中");
        }
        
        RuleChainRelation relation = RuleChainRelation.builder()
                .executionOrder(executionOrder)
                .conditionLogic(conditionLogic)
                .createTime(LocalDateTime.now())
                .build();
        
        return ruleChainRelationRepository.save(relation);
    }

    /**
     * 从规则链中移除规则
     * @param chainId 规则链ID
     * @param ruleId 规则ID
     */
    @Transactional
    public void removeRuleFromChain(Long chainId, Long ruleId) {
        ruleChainRelationRepository.deleteByRuleChainIdAndBusinessRuleId(chainId, ruleId);
    }

    /**
     * 更新规则在规则链中的执行顺序
     * @param chainId 规则链ID
     * @param ruleId 规则ID
     * @param newOrder 新的执行顺序
     * @return 更新的关联关系
     */
    @Transactional
    public RuleChainRelation updateRuleOrder(Long chainId, Long ruleId, Integer newOrder) {
        Optional<RuleChainRelation> relation = ruleChainRelationRepository.findByRuleChainIdAndBusinessRuleId(chainId, ruleId);
        if (relation.isPresent()) {
            RuleChainRelation relationToUpdate = relation.get();
            relationToUpdate.setExecutionOrder(newOrder);
            return ruleChainRelationRepository.save(relationToUpdate);
        }
        throw new RuntimeException("规则链关联关系不存在");
    }

    /**
     * 更新规则在规则链中的条件逻辑
     * @param chainId 规则链ID
     * @param ruleId 规则ID
     * @param conditionLogic 条件逻辑
     * @return 更新的关联关系
     */
    @Transactional
    public RuleChainRelation updateRuleConditionLogic(Long chainId, Long ruleId, String conditionLogic) {
        Optional<RuleChainRelation> relation = ruleChainRelationRepository.findByRuleChainIdAndBusinessRuleId(chainId, ruleId);
        if (relation.isPresent()) {
            RuleChainRelation relationToUpdate = relation.get();
            relationToUpdate.setConditionLogic(conditionLogic);
            return ruleChainRelationRepository.save(relationToUpdate);
        }
        throw new RuntimeException("规则链关联关系不存在");
    }

    /**
     * 统计规则链数量
     * @return 规则链统计信息
     */
    public ChainStatistics getStatistics() {
        long totalChains = ruleChainRepository.count();
        long enabledChains = ruleChainRepository.countByEnabled(true);
        long disabledChains = ruleChainRepository.countByEnabled(false);
        
        return ChainStatistics.builder()
                .totalChains(totalChains)
                .enabledChains(enabledChains)
                .disabledChains(disabledChains)
                .build();
    }

    /**
     * 规则链统计信息
     */
    @lombok.Data
    @lombok.Builder
    public static class ChainStatistics {
        private long totalChains;
        private long enabledChains;
        private long disabledChains;
    }
}