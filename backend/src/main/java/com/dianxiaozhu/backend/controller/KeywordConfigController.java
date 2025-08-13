package com.dianxiaozhu.backend.controller;

import com.dianxiaozhu.backend.entity.KeywordConfig;
import com.dianxiaozhu.backend.service.KeywordConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KeywordConfigController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KeywordConfigController.class);
    
    private final KeywordConfigService keywordConfigService;
    
    /**
     * 获取所有活跃关键词
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveKeywords() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<KeywordConfig> keywords = keywordConfigService.getActiveKeywords();
            response.put("success", true);
            response.put("keywords", keywords);
            response.put("count", keywords.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取活跃关键词异常", e);
            response.put("success", false);
            response.put("message", "获取关键词失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取全局关键词
     */
    @GetMapping("/global")
    public ResponseEntity<Map<String, Object>> getGlobalKeywords() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<KeywordConfig> keywords = keywordConfigService.getActiveGlobalKeywords();
            response.put("success", true);
            response.put("keywords", keywords);
            response.put("count", keywords.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取全局关键词异常", e);
            response.put("success", false);
            response.put("message", "获取全局关键词失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 根据网格区域获取本地关键词
     */
    @GetMapping("/local/{gridArea}")
    public ResponseEntity<Map<String, Object>> getLocalKeywords(@PathVariable String gridArea) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<KeywordConfig> keywords = keywordConfigService.getActiveLocalKeywordsByGridArea(gridArea);
            response.put("success", true);
            response.put("keywords", keywords);
            response.put("count", keywords.size());
            response.put("gridArea", gridArea);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取本地关键词异常", e);
            response.put("success", false);
            response.put("message", "获取本地关键词失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取高优先级关键词
     */
    @GetMapping("/high-priority")
    public ResponseEntity<Map<String, Object>> getHighPriorityKeywords() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<KeywordConfig> keywords = keywordConfigService.getHighPriorityKeywords();
            response.put("success", true);
            response.put("keywords", keywords);
            response.put("count", keywords.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取高优先级关键词异常", e);
            response.put("success", false);
            response.put("message", "获取高优先级关键词失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取关键词字符串列表
     */
    @GetMapping("/strings")
    public ResponseEntity<Map<String, Object>> getKeywordStrings() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<String> keywordStrings = keywordConfigService.getActiveKeywordStrings();
            response.put("success", true);
            response.put("keywords", keywordStrings);
            response.put("count", keywordStrings.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取关键词字符串异常", e);
            response.put("success", false);
            response.put("message", "获取关键词字符串失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 创建关键词配置
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createKeywordConfig(@RequestBody KeywordConfig keywordConfig) {
        log.info("创建关键词配置: {}", keywordConfig.getKeyword());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            KeywordConfig created = keywordConfigService.createKeywordConfig(keywordConfig);
            response.put("success", true);
            response.put("message", "关键词配置创建成功");
            response.put("keyword", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建关键词配置异常", e);
            response.put("success", false);
            response.put("message", "创建关键词配置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 批量创建关键词配置
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchCreateKeywordConfigs(@RequestBody List<KeywordConfig> keywordConfigs) {
        log.info("批量创建关键词配置: {} 个", keywordConfigs.size());
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<KeywordConfig> created = keywordConfigService.batchCreateKeywordConfigs(keywordConfigs);
            response.put("success", true);
            response.put("message", "批量创建关键词配置成功");
            response.put("keywords", created);
            response.put("createdCount", created.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("批量创建关键词配置异常", e);
            response.put("success", false);
            response.put("message", "批量创建关键词配置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 更新关键词配置
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateKeywordConfig(
            @PathVariable Long id, 
            @RequestBody KeywordConfig keywordConfig) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            keywordConfig.setId(id);
            KeywordConfig updated = keywordConfigService.updateKeywordConfig(keywordConfig);
            response.put("success", true);
            response.put("message", "关键词配置更新成功");
            response.put("keyword", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新关键词配置异常", e);
            response.put("success", false);
            response.put("message", "更新关键词配置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 启用/禁用关键词配置
     */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleKeywordConfig(
            @PathVariable Long id, 
            @RequestBody Map<String, Boolean> toggleRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isActive = toggleRequest.get("isActive");
            keywordConfigService.toggleKeywordConfig(id, isActive);
            response.put("success", true);
            response.put("message", "关键词配置状态更新成功");
            response.put("isActive", isActive);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("切换关键词配置状态异常", e);
            response.put("success", false);
            response.put("message", "更新关键词配置状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 根据ID获取关键词配置
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getKeywordConfigById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<KeywordConfig> keywordOpt = keywordConfigService.getKeywordConfigById(id);
            
            if (keywordOpt.isPresent()) {
                response.put("success", true);
                response.put("keyword", keywordOpt.get());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "关键词配置不存在");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取关键词配置异常", e);
            response.put("success", false);
            response.put("message", "获取关键词配置失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取所有关键词配置
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllKeywordConfigs() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<KeywordConfig> keywords = keywordConfigService.getAllKeywordConfigs();
            response.put("success", true);
            response.put("keywords", keywords);
            response.put("count", keywords.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取所有关键词配置异常", e);
            response.put("success", false);
            response.put("message", "获取关键词配置失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 根据类型获取关键词配置
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getKeywordConfigsByType(@PathVariable String type) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            KeywordConfig.KeywordType keywordType = KeywordConfig.KeywordType.valueOf(type.toUpperCase());
            List<KeywordConfig> keywords = keywordConfigService.getKeywordConfigsByType(keywordType);
            response.put("success", true);
            response.put("keywords", keywords);
            response.put("count", keywords.size());
            response.put("type", type);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("根据类型获取关键词配置异常", e);
            response.put("success", false);
            response.put("message", "获取关键词配置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 删除关键词配置
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteKeywordConfig(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            keywordConfigService.deleteKeywordConfig(id);
            response.put("success", true);
            response.put("message", "关键词配置删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除关键词配置异常", e);
            response.put("success", false);
            response.put("message", "删除关键词配置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 获取关键词统计数据
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getKeywordStats(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = keywordConfigService.getKeywordStats(id);
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取关键词统计数据异常", e);
            response.put("success", false);
            response.put("message", "获取关键词统计数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取关键词命中率数据
     */
    @GetMapping("/hit-rate")
    public ResponseEntity<Map<String, Object>> getKeywordHitRate() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> hitRateData = keywordConfigService.getKeywordHitRate();
            response.put("success", true);
            response.put("data", hitRateData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取关键词命中率数据异常", e);
            response.put("success", false);
            response.put("message", "获取关键词命中率数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取关键词分析报告
     */
    @GetMapping("/analysis")
    public ResponseEntity<Map<String, Object>> getKeywordAnalysis(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String priority) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> analysis = keywordConfigService.getKeywordAnalysis(days, category, priority);
            response.put("success", true);
            response.put("data", analysis);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取关键词分析报告异常", e);
            response.put("success", false);
            response.put("message", "获取关键词分析报告失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 清除缓存
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            keywordConfigService.clearCache();
            response.put("success", true);
            response.put("message", "缓存清除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("清除缓存异常", e);
            response.put("success", false);
            response.put("message", "清除缓存失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}