package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.entity.SystemConfig;
import com.dianxiaozhu.backend.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 */
@RestController
@RequestMapping("/api/system/config")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * 获取所有系统配置
     */
    @GetMapping
    public ResponseEntity<List<SystemConfig>> getAllConfigs() {
        log.info("获取所有系统配置");
        List<SystemConfig> configs = systemConfigService.getAllConfigs();
        return ResponseEntity.ok(configs);
    }

    /**
     * 分页查询系统配置
     */
    @GetMapping("/page")
    public ResponseEntity<Page<SystemConfig>> getConfigsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        
        log.info("分页查询系统配置: page={}, size={}, sort={}, direction={}", page, size, sort, direction);
        
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<SystemConfig> configs = systemConfigService.getConfigs(pageable);
        return ResponseEntity.ok(configs);
    }

    /**
     * 根据ID获取配置
     */
    @GetMapping("/{id}")
    public ResponseEntity<SystemConfig> getConfigById(@PathVariable Long id) {
        log.info("根据ID获取配置: id={}", id);
        SystemConfig config = systemConfigService.getConfigById(id);
        return config != null ? ResponseEntity.ok(config) : ResponseEntity.notFound().build();
    }

    /**
     * 根据配置键名获取配置
     */
    @GetMapping("/key/{configKey}")
    public ResponseEntity<SystemConfig> getConfigByKey(@PathVariable String configKey) {
        log.info("根据配置键名获取配置: configKey={}", configKey);
        SystemConfig config = systemConfigService.getConfigByKey(configKey);
        return config != null ? ResponseEntity.ok(config) : ResponseEntity.notFound().build();
    }

    /**
     * 根据配置键名获取配置值
     */
    @GetMapping("/value/{configKey}")
    public ResponseEntity<Map<String, Object>> getConfigValue(@PathVariable String configKey) {
        log.info("根据配置键名获取配置值: configKey={}", configKey);
        String value = systemConfigService.getConfigValue(configKey);
        
        Map<String, Object> response = new HashMap<>();
        response.put("configKey", configKey);
        response.put("configValue", value);
        response.put("exists", value != null);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取关键词触发阈值
     */
    @GetMapping("/keyword-threshold")
    public ResponseEntity<Map<String, Object>> getKeywordThreshold() {
        log.info("获取关键词触发阈值");
        Integer threshold = systemConfigService.getIntConfigValue("keyword.trigger.threshold", 3);
        
        Map<String, Object> response = new HashMap<>();
        response.put("threshold", threshold);
        response.put("description", "客户端本地关键词触发多少次后上传到服务器");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 创建或更新配置
     */
    @PostMapping
    public ResponseEntity<SystemConfig> saveConfig(@RequestBody SystemConfig config) {
        log.info("创建或更新配置: {}", config.getConfigKey());
        
        try {
            SystemConfig savedConfig = systemConfigService.saveConfig(config);
            return ResponseEntity.ok(savedConfig);
        } catch (Exception e) {
            log.error("保存配置失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    public ResponseEntity<SystemConfig> updateConfig(@PathVariable Long id, @RequestBody SystemConfig config) {
        log.info("更新配置: id={}, configKey={}", id, config.getConfigKey());
        
        try {
            config.setId(id);
            SystemConfig updatedConfig = systemConfigService.saveConfig(config);
            return ResponseEntity.ok(updatedConfig);
        } catch (Exception e) {
            log.error("更新配置失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 批量更新配置
     */
    @PutMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchUpdateConfigs(@RequestBody Map<String, String> configs) {
        log.info("批量更新配置: {}", configs.keySet());
        
        try {
            Map<String, Object> result = systemConfigService.batchUpdateConfigs(configs);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("批量更新配置失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 设置关键词触发阈值
     */
    @PutMapping("/keyword-threshold")
    public ResponseEntity<Map<String, Object>> setKeywordThreshold(@RequestBody Map<String, Object> request) {
        log.info("设置关键词触发阈值: {}", request);
        
        try {
            Object thresholdObj = request.get("threshold");
            if (thresholdObj == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "阈值参数不能为空");
                return ResponseEntity.badRequest().body(error);
            }
            
            Integer threshold;
            if (thresholdObj instanceof Integer) {
                threshold = (Integer) thresholdObj;
            } else if (thresholdObj instanceof String) {
                threshold = Integer.parseInt((String) thresholdObj);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "阈值参数格式错误");
                return ResponseEntity.badRequest().body(error);
            }
            
            if (threshold < 1 || threshold > 100) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "阈值必须在1-100之间");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 更新配置
            SystemConfig config = systemConfigService.getConfigByKey("keyword.trigger.threshold");
            if (config == null) {
                config = SystemConfig.builder()
                        .configKey("keyword.trigger.threshold")
                        .configType("INTEGER")
                        .description("关键词触发阈值")
                        .enabled(true)
                        .remark("客户端本地关键词触发多少次后上传到服务器")
                        .build();
            }
            config.setConfigValue(threshold.toString());
            
            systemConfigService.saveConfig(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("threshold", threshold);
            response.put("message", "关键词触发阈值设置成功");
            
            return ResponseEntity.ok(response);
            
        } catch (NumberFormatException e) {
            log.error("阈值格式错误", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "阈值必须是有效的数字");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("设置关键词触发阈值失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取关键词审核开关状态
     */
    @GetMapping("/keyword-audit")
    public ResponseEntity<Map<String, Object>> getKeywordAuditStatus() {
        log.info("获取关键词审核开关状态");
        
        try {
            SystemConfig config = systemConfigService.getConfigByKey("keyword.audit.enabled");
            boolean auditEnabled = false;
            
            if (config != null && config.getConfigValue() != null) {
                auditEnabled = Boolean.parseBoolean(config.getConfigValue());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("code", 20000);
            response.put("message", "获取关键词审核状态成功");
            response.put("timestamp", System.currentTimeMillis());
            
            Map<String, Object> data = new HashMap<>();
            data.put("auditEnabled", auditEnabled);
            data.put("description", "关键词审核开关，开启后客户端上传的关键词需要人工审核");
            response.put("data", data);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取关键词审核开关状态失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("code", 50000);
            error.put("message", "获取关键词审核开关状态失败: " + e.getMessage());
            error.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(error);
        }
    }

    /**
     * 设置关键词审核开关
     */
    @PutMapping("/keyword-audit")
    public ResponseEntity<Map<String, Object>> setKeywordAuditStatus(@RequestBody Map<String, Object> request) {
        log.info("设置关键词审核开关: {}", request);
        
        try {
            Object auditEnabledObj = request.get("auditEnabled");
            if (auditEnabledObj == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "审核开关参数不能为空");
                return ResponseEntity.badRequest().body(error);
            }
            
            Boolean auditEnabled;
            if (auditEnabledObj instanceof Boolean) {
                auditEnabled = (Boolean) auditEnabledObj;
            } else if (auditEnabledObj instanceof String) {
                auditEnabled = Boolean.parseBoolean((String) auditEnabledObj);
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "审核开关参数格式错误");
                return ResponseEntity.badRequest().body(error);
            }
            
            // 更新配置
            SystemConfig config = systemConfigService.getConfigByKey("keyword.audit.enabled");
            if (config == null) {
                config = SystemConfig.builder()
                        .configKey("keyword.audit.enabled")
                        .configType("BOOLEAN")
                        .description("关键词审核开关，开启后客户端上传的关键词需要人工审核")
                        .enabled(true)
                        .remark("系统配置")
                        .build();
            }
            config.setConfigValue(auditEnabled.toString());
            
            systemConfigService.saveConfig(config);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("auditEnabled", auditEnabled);
            response.put("message", "关键词审核开关设置成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("设置关键词审核开关失败", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "设置关键词审核开关失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteConfig(@PathVariable Long id) {
        log.info("删除配置: id={}", id);
        
        try {
            systemConfigService.deleteConfig(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "配置删除成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除配置失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 初始化默认配置
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initDefaultConfigs() {
        log.info("初始化默认配置");
        
        try {
            systemConfigService.initDefaultConfigs();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "默认配置初始化成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("初始化默认配置失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}