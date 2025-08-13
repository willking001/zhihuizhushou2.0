package com.dianxiaozhu.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Column;
import jakarta.persistence.PreUpdate;

/**
 * 消息模板实体类
 */
@Entity
@Table(name = "message_templates")
public class MessageTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String template;

    @Column(nullable = false)
    private String type; // forward 或 reply

    @ElementCollection
    @CollectionTable(name = "message_template_keywords", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "keyword")
    private List<String> keywords;

    @ElementCollection
    @CollectionTable(name = "message_template_conditions", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "condition_expr") // 修改列名，避免使用MySQL保留关键字
    private List<String> conditions;

    @Column
    private Integer priority;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column
    private String header;

    @Column
    private String attachmentRule;

    @Column
    private Boolean dataMasking = false;

    @ElementCollection
    @CollectionTable(name = "message_template_masking_rules", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "masking_rule")
    private List<String> maskingRules;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // 构造函数
    public MessageTemplate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getAttachmentRule() {
        return attachmentRule;
    }

    public void setAttachmentRule(String attachmentRule) {
        this.attachmentRule = attachmentRule;
    }

    public Boolean getDataMasking() {
        return dataMasking;
    }

    public void setDataMasking(Boolean dataMasking) {
        this.dataMasking = dataMasking;
    }

    public List<String> getMaskingRules() {
        return maskingRules;
    }

    public void setMaskingRules(List<String> maskingRules) {
        this.maskingRules = maskingRules;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}