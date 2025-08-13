package com.dianxiaozhu.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 规则触发条件实体类
 */
@Entity
@Table(name = "rule_condition")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的业务规则
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    @JsonIgnore
    private BusinessRule businessRule;

    /**
     * 条件类型：KEYWORD_MATCH, PHRASE_MATCH, REGEX_MATCH
     */
    @Column(nullable = false, length = 50)
    private String conditionType;

    /**
     * 条件值（关键词、短语或正则表达式）
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String conditionValue;

    /**
     * 匹配模式：CONTAINS, EQUALS, STARTS_WITH, ENDS_WITH
     */
    @Column(length = 20)
    private String matchMode;

    /**
     * 是否区分大小写
     */
    @Column(nullable = false)
    private Boolean caseSensitive;

    /**
     * 条件权重
     */
    @Column(precision = 3, scale = 2)
    private BigDecimal weight;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (caseSensitive == null) {
            caseSensitive = false;
        }
        if (weight == null) {
            weight = BigDecimal.ONE;
        }
        if (matchMode == null) {
            matchMode = "CONTAINS";
        }
    }
}