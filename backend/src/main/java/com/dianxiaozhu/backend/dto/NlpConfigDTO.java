package com.dianxiaozhu.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NLP配置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NlpConfigDTO {

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
    private Integer type;
    
    /**
     * 配置名称
     */
    private String name;
    
    /**
     * 配置内容（JSON格式）
     */
    private String content;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 优先级（数字越小优先级越高）
     */
    private Integer priority;
    
    /**
     * 备注
     */
    private String remark;
}