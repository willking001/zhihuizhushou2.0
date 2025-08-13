package com.dianxiaozhu.backend.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeywordConfig {
    
    private Long id;
    
    private String keyword;

    public String getKeyword() {
        return keyword;
    }
    
    private KeywordType type = KeywordType.GLOBAL;
    
    private Priority priority = Priority.NORMAL;
    
    private String description;
    
    private String gridArea;
    
    private Boolean isActive = true;
    
    private Long createdBy;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private Integer hitCount = 0;
    
    private Integer weight = 1; // 权重：服务器=1，客户端=2
    
    private SourceType sourceType = SourceType.SERVER;
    
    private Integer triggerThreshold = 3; // 触发上传的阈值
    
    public enum KeywordType {
        GLOBAL,    // 全局关键词
        LOCAL,     // 本地关键词
        CUSTOM     // 自定义关键词
    }
    
    public enum Priority {
        LOW, NORMAL, HIGH, URGENT
    }
    
    public enum SourceType {
        SERVER,    // 服务器端关键词
        CLIENT,    // 客户端本地关键词
        LEARNED    // 学习推荐关键词
    }

    public Priority getPriority() {
        return priority;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
}