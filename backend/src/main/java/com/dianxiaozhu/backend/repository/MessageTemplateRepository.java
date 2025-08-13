package com.dianxiaozhu.backend.repository;

import com.dianxiaozhu.backend.entity.MessageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 消息模板仓库接口
 */
@Repository
public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {
    
    /**
     * 根据模板类型查找模板
     * @param type 模板类型 (forward 或 reply)
     * @return 模板列表
     */
    List<MessageTemplate> findByType(String type);
    
    /**
     * 根据模板类型和启用状态查找模板
     * @param type 模板类型 (forward 或 reply)
     * @param enabled 是否启用
     * @return 模板列表
     */
    List<MessageTemplate> findByTypeAndEnabled(String type, Boolean enabled);
    
    /**
     * 根据名称查找模板
     * @param name 模板名称
     * @return 模板列表
     */
    List<MessageTemplate> findByName(String name);
}