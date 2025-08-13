package com.dianxiaozhu.backend.repository;

import com.dianxiaozhu.backend.entity.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置Repository
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {

    /**
     * 根据配置键名查找配置
     * @param configKey 配置键名
     * @return 配置
     */
    Optional<SystemConfig> findByConfigKey(String configKey);

    /**
     * 根据配置类型查找配置列表
     * @param configType 配置类型
     * @return 配置列表
     */
    List<SystemConfig> findByConfigType(String configType);

    /**
     * 根据启用状态查找配置列表
     * @param enabled 启用状态
     * @return 配置列表
     */
    List<SystemConfig> findByEnabled(Boolean enabled);

    /**
     * 根据配置键名和启用状态查找配置
     * @param configKey 配置键名
     * @param enabled 启用状态
     * @return 配置
     */
    Optional<SystemConfig> findByConfigKeyAndEnabled(String configKey, Boolean enabled);
}