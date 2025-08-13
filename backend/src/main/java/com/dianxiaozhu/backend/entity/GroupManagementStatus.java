package com.dianxiaozhu.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 群组管理状态实体类
 * 用于管理微信群组的状态和统计信息
 */
@Entity
@Table(name = "group_management_status")
@Data
@EqualsAndHashCode(callSuper = false)
public class GroupManagementStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 群组标识（对应messages表的chat_room）
     */
    @Column(name = "chat_room", nullable = false, unique = true, length = 200)
    private String chatRoom;

    /**
     * 群组名称
     */
    @Column(name = "group_name", length = 200)
    private String groupName;

    /**
     * 当前状态: AUTO/MANUAL/PAUSED
     */
    @Column(name = "current_status", length = 20)
    @Enumerated(EnumType.STRING)
    private GroupStatus currentStatus = GroupStatus.AUTO;

    /**
     * 自动回复开关
     */
    @Column(name = "auto_reply_enabled")
    private Boolean autoReplyEnabled = true;

    /**
     * 自动转发开关
     */
    @Column(name = "auto_forward_enabled")
    private Boolean autoForwardEnabled = true;

    /**
     * 最近接管原因
     */
    @Column(name = "takeover_reason", length = 500)
    private String takeoverReason;

    /**
     * 最近接管时间
     */
    @Column(name = "takeover_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime takeoverTime;

    /**
     * 接管人员
     */
    @Column(name = "takeover_by", length = 50)
    private String takeoverBy;

    /**
     * 今日消息数
     */
    @Column(name = "message_count_today")
    private Integer messageCountToday = 0;

    /**
     * 今日自动回复数
     */
    @Column(name = "auto_reply_count_today")
    private Integer autoReplyCountToday = 0;

    /**
     * 今日接管次数
     */
    @Column(name = "takeover_count_today")
    private Integer takeoverCountToday = 0;

    /**
     * 最后活跃时间
     */
    @Column(name = "last_activity_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActivityTime;

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

    /**
     * 群组状态枚举
     */
    public enum GroupStatus {
        AUTO("自动模式"),
        MANUAL("人工接管"),
        PAUSED("暂停服务");

        private final String description;

        GroupStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

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
}