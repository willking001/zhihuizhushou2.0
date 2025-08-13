package com.dianxiaozhu.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NLP处理请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NlpProcessRequest {

    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 处理类型（可多选）：
     * 1-预处理
     * 2-意图识别
     * 4-实体提取
     * 8-情感分析
     * 16-文本分类
     * 32-关键信息提取
     * 组合使用时将值相加，如3表示同时进行预处理和意图识别
     */
    private Integer processTypes;
    
    /**
     * 是否异步处理
     */
    private Boolean async;
}