package com.dianxiaozhu.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 智能业务规则实体类
 */
@Entity
@Table(name = "business_rule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 规则名称
     */
    @Column(nullable = false, length = 100)
    private String ruleName;

    /**
     * 规则描述
     */
    @Column(length = 500)
    private String ruleDescription;

    /**
     * 规则类型：KEYWORD_TRIGGER, MESSAGE_FORWARD, AUTO_REPLY
     */
    @Column(nullable = false, length = 50)
    private String ruleType;

    /**
     * 优先级（数字越小优先级越高）
     */
    @Column(nullable = false)
    private Integer priority;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled;

    /**
     * 生效开始时间
     */
    private LocalTime startTime;

    /**
     * 生效结束时间
     */
    private LocalTime endTime;

    /**
     * 生效日期：1,2,3,4,5,6,7 (周一到周日)
     */
    @Column(length = 20)
    private String effectiveDays;

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
     * 备注
     */
    @Column(length = 500)
    private String remark;
    
    // 群组管理相关字段
    /**
     * 适用的群组列表（逗号分隔的chatRoom）
     */
    @Column(name = "target_groups", columnDefinition = "TEXT")
    private String targetGroups;
    
    /**
     * 群组状态过滤：AUTO/MANUAL/PAUSED
     */
    @Column(name = "group_status_filter", length = 20)
    private String groupStatusFilter;
    
    /**
     * 是否启用群组自动接管
     */
    @Column(name = "enable_auto_takeover")
    private Boolean enableAutoTakeover = false;
    
    /**
     * 接管触发阈值（如连续触发次数）
     */
    @Column(name = "takeover_threshold")
    private Integer takeoverThreshold;
    
    /**
     * 接管原因模板
     */
    @Column(name = "takeover_reason_template", length = 500)
    private String takeoverReasonTemplate;

    /**
     * 规则条件列表
     */
    @OneToMany(mappedBy = "businessRule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RuleCondition> conditions;

    /**
     * 规则动作列表
     */
    @OneToMany(mappedBy = "businessRule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RuleAction> actions;

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