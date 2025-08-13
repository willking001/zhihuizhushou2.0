package com.dianxiaozhu.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * 用于存储系统级别的配置信息
 */
@Entity
@Table(name = "system_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 配置键名
     */
    @Column(nullable = false, unique = true, length = 100)
    private String configKey;

    /**
     * 配置值
     */
    @Column(columnDefinition = "TEXT")
    private String configValue;

    /**
     * 配置描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 配置类型：STRING, INTEGER, BOOLEAN, JSON
     */
    @Column(nullable = false, length = 20)
    private String configType;

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
     * 创建人ID
     */
    @Column(nullable = false)
    private Long creatorId;

    /**
     * 更新人ID
     */
    @Column(nullable = false)
    private Long updaterId;

    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;
}