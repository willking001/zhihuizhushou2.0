package com.dianxiaozhu.backend.service.impl;

import com.dianxiaozhu.backend.entity.SystemConfig;
import com.dianxiaozhu.backend.repository.SystemConfigRepository;
import com.dianxiaozhu.backend.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 系统配置服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    @Override
    public List<SystemConfig> getAllConfigs() {
        return systemConfigRepository.findAll();
    }

    @Override
    public Page<SystemConfig> getConfigs(Pageable pageable) {
        return systemConfigRepository.findAll(pageable);
    }

    @Override
    public SystemConfig getConfigById(Long id) {
        return systemConfigRepository.findById(id).orElse(null);
    }

    @Override
    public SystemConfig getConfigByKey(String configKey) {
        return systemConfigRepository.findByConfigKeyAndEnabled(configKey, true).orElse(null);
    }

    @Override
    public String getConfigValue(String configKey) {
        return getConfigValue(configKey, null);
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        SystemConfig config = getConfigByKey(configKey);
        return config != null ? config.getConfigValue() : defaultValue;
    }

    @Override
    public Integer getIntConfigValue(String configKey, Integer defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("配置值转换为整数失败: {} = {}", configKey, value);
            return defaultValue;
        }
    }

    @Override
    public Boolean getBooleanConfigValue(String configKey, Boolean defaultValue) {
        String value = getConfigValue(configKey);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    @Transactional
    public SystemConfig saveConfig(SystemConfig config) {
        LocalDateTime now = LocalDateTime.now();
        
        if (config.getId() == null) {
            // 新增配置
            config.setCreateTime(now);
            config.setUpdateTime(now);
            if (config.getCreatorId() == null) {
                config.setCreatorId(1L); // 默认系统用户
            }
            config.setUpdaterId(config.getCreatorId());
        } else {
            // 更新配置
            SystemConfig existingConfig = getConfigById(config.getId());
            if (existingConfig != null) {
                config.setCreateTime(existingConfig.getCreateTime());
                config.setCreatorId(existingConfig.getCreatorId());
            }
            config.setUpdateTime(now);
            if (config.getUpdaterId() == null) {
                config.setUpdaterId(1L); // 默认系统用户
            }
        }
        
        return systemConfigRepository.save(config);
    }

    @Override
    @Transactional
    public Map<String, Object> batchUpdateConfigs(Map<String, String> configs) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;
        
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            try {
                String configKey = entry.getKey();
                String configValue = entry.getValue();
                
                Optional<SystemConfig> existingConfigOpt = systemConfigRepository.findByConfigKey(configKey);
                SystemConfig config;
                
                if (existingConfigOpt.isPresent()) {
                    // 更新现有配置
                    config = existingConfigOpt.get();
                    config.setConfigValue(configValue);
                } else {
                    // 创建新配置
                    config = SystemConfig.builder()
                            .configKey(configKey)
                            .configValue(configValue)
                            .configType("STRING")
                            .enabled(true)
                            .description("批量更新创建的配置")
                            .build();
                }
                
                saveConfig(config);
                successCount++;
            } catch (Exception e) {
                log.error("批量更新配置失败: {} = {}", entry.getKey(), entry.getValue(), e);
                failCount++;
            }
        }
        
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalCount", configs.size());
        
        return result;
    }

    @Override
    @Transactional
    public void deleteConfig(Long id) {
        systemConfigRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteConfigByKey(String configKey) {
        Optional<SystemConfig> config = systemConfigRepository.findByConfigKey(configKey);
        config.ifPresent(systemConfigRepository::delete);
    }

    @Override
    @Transactional
    public void initDefaultConfigs() {
        log.info("初始化默认系统配置");
        
        // 关键词触发阈值配置
        createDefaultConfigIfNotExists(
                "keyword.trigger.threshold",
                "3",
                "INTEGER",
                "关键词触发阈值",
                "客户端本地关键词触发多少次后上传到服务器，默认为3次"
        );
        
        // 其他默认配置可以在这里添加
        createDefaultConfigIfNotExists(
                "system.maintenance.mode",
                "false",
                "BOOLEAN",
                "系统维护模式",
                "是否开启系统维护模式"
        );
        
        createDefaultConfigIfNotExists(
                "message.max.length",
                "1000",
                "INTEGER",
                "消息最大长度",
                "单条消息的最大字符长度限制"
        );
        
        log.info("默认系统配置初始化完成");
    }
    
    private void createDefaultConfigIfNotExists(String configKey, String configValue, 
                                               String configType, String description, String remark) {
        Optional<SystemConfig> existingConfig = systemConfigRepository.findByConfigKey(configKey);
        if (existingConfig.isEmpty()) {
            SystemConfig config = SystemConfig.builder()
                    .configKey(configKey)
                    .configValue(configValue)
                    .configType(configType)
                    .description(description)
                    .enabled(true)
                    .creatorId(1L)
                    .updaterId(1L)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .remark(remark)
                    .build();
            systemConfigRepository.save(config);
            log.info("创建默认配置: {} = {}", configKey, configValue);
        }
    }
}