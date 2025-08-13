package com.dianxiaozhu.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 规则执行日志实体类
 */
@Entity
@Table(name = "rule_execution_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的业务规则
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private BusinessRule businessRule;

    /**
     * 关联消息ID
     */
    private Long messageId;

    /**
     * 触发内容
     */
    @Column(columnDefinition = "TEXT")
    private String triggerContent;

    /**
     * 匹配的条件（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String matchedConditions;

    /**
     * 执行的动作（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String executedActions;

    /**
     * 执行结果：SUCCESS, FAILED, PARTIAL
     */
    @Column(length = 20)
    private String executionResult;

    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 执行时间
     */
    @Column(nullable = false)
    private LocalDateTime executionTime;

    /**
     * 处理耗时（毫秒）
     */
    private Integer processingDuration;

    @PrePersist
    protected void onCreate() {
        executionTime = LocalDateTime.now();
        if (executionResult == null) {
            executionResult = "SUCCESS";
        }
    }
}