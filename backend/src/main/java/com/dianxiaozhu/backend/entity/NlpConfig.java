package com.dianxiaozhu.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * NLP配置实体类
 * 用于存储NLP处理相关的配置信息
 */
@Entity
@Table(name = "nlp_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NlpConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 配置类型：
     * 1-预处理配置
     * 2-意图识别配置
     * 3-实体提取配置
     * 4-情感分析配置
     * 5-文本分类配置
     * 6-关键信息提取配置
     */
    @Column(nullable = false)
    private Integer type;

    /**
     * 配置名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 配置内容（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 是否启用
     */
    @Column(nullable = false)
    private Boolean enabled;

    /**
     * 优先级（数字越小优先级越高）
     */
    @Column(nullable = false)
    private Integer priority;

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