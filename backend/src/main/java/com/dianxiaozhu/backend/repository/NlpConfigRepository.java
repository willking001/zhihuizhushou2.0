package com.dianxiaozhu.backend.repository;

import com.dianxiaozhu.backend.entity.NlpConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * NLP配置Repository
 */
@Repository
public interface NlpConfigRepository extends JpaRepository<NlpConfig, Long> {

    /**
     * 根据类型查找配置列表
     * @param type 配置类型
     * @return 配置列表
     */
    List<NlpConfig> findByType(Integer type);

    /**
     * 根据类型和启用状态查找配置列表
     * @param type 配置类型
     * @param enabled 启用状态
     * @return 配置列表
     */
    List<NlpConfig> findByTypeAndEnabled(Integer type, Boolean enabled);

    /**
     * 根据类型和启用状态查找配置列表，并按优先级排序
     * @param type 配置类型
     * @param enabled 启用状态
     * @return 配置列表
     */
    List<NlpConfig> findByTypeAndEnabledOrderByPriorityAsc(Integer type, Boolean enabled);

    /**
     * 根据名称查找配置
     * @param name 配置名称
     * @return 配置
     */
    NlpConfig findByName(String name);
}