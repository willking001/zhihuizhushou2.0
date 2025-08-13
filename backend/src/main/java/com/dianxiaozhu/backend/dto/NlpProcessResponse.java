package com.dianxiaozhu.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * NLP处理响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NlpProcessResponse {

    /**
     * 处理ID
     */
    private Long processId;
    
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 处理状态：
     * 0-处理中
     * 1-处理完成
     * 2-处理失败
     */
    private Integer status;
    
    /**
     * 预处理结果
     */
    private Map<String, Object> preprocessResult;
    
    /**
     * 意图识别结果
     */
    private Map<String, Object> intentResult;
    
    /**
     * 实体提取结果
     */
    private Map<String, Object> entityResult;
    
    /**
     * 情感分析结果
     */
    private Map<String, Object> sentimentResult;
    
    /**
     * 文本分类结果
     */
    private Map<String, Object> classificationResult;
    
    /**
     * 关键信息提取结果
     */
    private Map<String, Object> keyInfoResult;
    
    /**
     * 处理耗时（毫秒）
     */
    private Long processDuration;
    
    /**
     * 错误信息（处理失败时返回）
     */
    private String errorMessage;
}