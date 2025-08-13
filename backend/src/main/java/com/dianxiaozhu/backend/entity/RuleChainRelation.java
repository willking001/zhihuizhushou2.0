package com.dianxiaozhu.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 规则链关联实体类
 */
@Entity
@Table(name = "rule_chain_relation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleChainRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的规则链
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chain_id", nullable = false)
    private RuleChain ruleChain;

    /**
     * 关联的业务规则
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private BusinessRule businessRule;

    /**
     * 执行顺序
     */
    @Column(nullable = false)
    private Integer executionOrder;

    /**
     * 条件逻辑：AND, OR
     */
    @Column(length = 10)
    private String conditionLogic;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (conditionLogic == null) {
            conditionLogic = "AND";
        }
    }
}