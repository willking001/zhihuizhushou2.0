package com.dianxiaozhu.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * NLP处理结果实体类
 * 用于存储消息的NLP处理结果
 */
@Entity
@Table(name = "nlp_result")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NlpResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的消息ID
     */
    @Column(nullable = false)
    private Long messageId;

    /**
     * 预处理结果（JSON格式）
     * 包含分词结果、繁简转换结果等
     */
    @Column(columnDefinition = "TEXT")
    private String preprocessResult;

    /**
     * 意图识别结果（JSON格式）
     * 包含意图类型、置信度等
     */
    @Column(columnDefinition = "TEXT")
    private String intentResult;

    /**
     * 实体提取结果（JSON格式）
     * 包含提取的实体类型、值等
     */
    @Column(columnDefinition = "TEXT")
    private String entityResult;

    /**
     * 情感分析结果（JSON格式）
     * 包含情感极性、置信度等
     */
    @Column(columnDefinition = "TEXT")
    private String sentimentResult;

    /**
     * 文本分类结果（JSON格式）
     * 包含分类标签、置信度等
     */
    @Column(columnDefinition = "TEXT")
    private String classificationResult;

    /**
     * 关键信息提取结果（JSON格式）
     * 包含提取的关键信息类型、值等
     */
    @Column(columnDefinition = "TEXT")
    private String keyInfoResult;

    /**
     * 处理状态：
     * 0-处理中
     * 1-处理完成
     * 2-处理失败
     */
    @Column(nullable = false)
    private Integer status;

    /**
     * 处理时间
     */
    @Column(nullable = false)
    private LocalDateTime processTime;

    /**
     * 处理耗时（毫秒）
     */
    private Long processDuration;

    /**
     * 错误信息（处理失败时记录）
     */
    @Column(length = 500)
    private String errorMessage;

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
}