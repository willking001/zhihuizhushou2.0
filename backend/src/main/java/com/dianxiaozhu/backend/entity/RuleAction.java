package com.dianxiaozhu.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 规则动作配置实体类
 */
@Entity
@Table(name = "rule_action")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleAction {

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
     * 动作类型：MESSAGE_FORWARD, AUTO_REPLY, CREATE_TICKET, NOTIFICATION
     */
    @Column(nullable = false, length = 50)
    private String actionType;

    /**
     * 动作配置（JSON格式）
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String actionConfig;

    /**
     * 执行顺序
     */
    @Column(nullable = false)
    private Integer executionOrder;

    /**
     * 创建时间
     */
    @Column(nullable = false)
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (executionOrder == null) {
            executionOrder = 1;
        }
    }
}