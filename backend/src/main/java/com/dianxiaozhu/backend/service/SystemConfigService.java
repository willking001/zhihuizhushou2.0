package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.SystemConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService {

    /**
     * 获取所有系统配置
     * @return 配置列表
     */
    List<SystemConfig> getAllConfigs();

    /**
     * 分页查询系统配置
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<SystemConfig> getConfigs(Pageable pageable);

    /**
     * 根据ID获取配置
     * @param id 配置ID
     * @return 配置
     */
    SystemConfig getConfigById(Long id);

    /**
     * 根据配置键名获取配置
     * @param configKey 配置键名
     * @return 配置
     */
    SystemConfig getConfigByKey(String configKey);

    /**
     * 根据配置键名获取配置值
     * @param configKey 配置键名
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 根据配置键名获取配置值（带默认值）
     * @param configKey 配置键名
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 根据配置键名获取整数配置值
     * @param configKey 配置键名
     * @param defaultValue 默认值
     * @return 配置值
     */
    Integer getIntConfigValue(String configKey, Integer defaultValue);

    /**
     * 根据配置键名获取布尔配置值
     * @param configKey 配置键名
     * @param defaultValue 默认值
     * @return 配置值
     */
    Boolean getBooleanConfigValue(String configKey, Boolean defaultValue);

    /**
     * 创建或更新配置
     * @param config 配置
     * @return 保存后的配置
     */
    SystemConfig saveConfig(SystemConfig config);

    /**
     * 批量更新配置
     * @param configs 配置映射（key -> value）
     * @return 更新结果
     */
    Map<String, Object> batchUpdateConfigs(Map<String, String> configs);

    /**
     * 删除配置
     * @param id 配置ID
     */
    void deleteConfig(Long id);

    /**
     * 根据配置键名删除配置
     * @param configKey 配置键名
     */
    void deleteConfigByKey(String configKey);

    /**
     * 初始化默认配置
     */
    void initDefaultConfigs();
}