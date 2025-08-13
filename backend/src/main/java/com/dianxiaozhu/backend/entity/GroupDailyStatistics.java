package com.dianxiaozhu.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 群组日统计实体类
 * 用于存储群组每日的统计数据
 */
@Entity
@Table(name = "group_daily_statistics", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"chat_room", "stat_date"}))
@Data
@EqualsAndHashCode(callSuper = false)
public class GroupDailyStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 群组标识
     */
    @Column(name = "chat_room", nullable = false, length = 200)
    private String chatRoom;

    /**
     * 统计日期
     */
    @Column(name = "stat_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate statDate;

    /**
     * 消息总数
     */
    @Column(name = "message_count")
    private Integer messageCount = 0;

    /**
     * 自动回复数
     */
    @Column(name = "auto_reply_count")
    private Integer autoReplyCount = 0;

    /**
     * 人工接管次数
     */
    @Column(name = "manual_takeover_count")
    private Integer manualTakeoverCount = 0;

    /**
     * 平均响应时间（秒）
     */
    @Column(name = "avg_response_time", precision = 5, scale = 2)
    private BigDecimal avgResponseTime = BigDecimal.ZERO;

    /**
     * 满意度评分
     */
    @Column(name = "satisfaction_score", precision = 3, scale = 2)
    private BigDecimal satisfactionScore = BigDecimal.ZERO;

    /**
     * 创建时间
     */
    @Column(name = "created_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdTime = now;
        this.updatedTime = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTime = LocalDateTime.now();
    }

    /**
     * 计算自动回复成功率
     */
    public BigDecimal getAutoReplySuccessRate() {
        if (messageCount == null || messageCount == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(autoReplyCount)
                .divide(BigDecimal.valueOf(messageCount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * 计算人工接管率
     */
    public BigDecimal getManualTakeoverRate() {
        if (messageCount == null || messageCount == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(manualTakeoverCount)
                .divide(BigDecimal.valueOf(messageCount), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
}