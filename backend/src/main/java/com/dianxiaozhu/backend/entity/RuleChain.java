package com.dianxiaozhu.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 规则链配置实体类
 */
@Entity
@Table(name = "rule_chain")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleChain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 规则链名称
     */
    @Column(nullable = false, length = 100)
    private String chainName;

    /**
     * 规则链描述
     */
    @Column(length = 500)
    private String chainDescription;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(nullable = false)
    private LocalDateTime updateTime;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 更新者ID
     */
    private Long updaterId;

    /**
     * 规则链关联列表
     */
    @OneToMany(mappedBy = "ruleChain", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RuleChainRelation> relations;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}