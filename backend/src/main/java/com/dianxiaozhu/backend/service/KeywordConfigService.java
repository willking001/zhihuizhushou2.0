package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.KeywordConfig;
import com.dianxiaozhu.backend.mapper.KeywordConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KeywordConfigService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KeywordConfigService.class);
    
    private final KeywordConfigMapper keywordConfigMapper;
    
    /**
     * 获取所有活跃关键词（带缓存）
     */
    @Cacheable("activeKeywords")
    public List<KeywordConfig> getActiveKeywords() {
        return keywordConfigMapper.findAll().stream()
                .filter(KeywordConfig::getIsActive)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取全局活跃关键词
     */
    @Cacheable("globalKeywords")
    public List<KeywordConfig> getActiveGlobalKeywords() {
        return keywordConfigMapper.findActiveGlobalKeywords(KeywordConfig.KeywordType.GLOBAL.name());
    }
    
    /**
     * 根据网格区域获取本地关键词
     */
    public List<KeywordConfig> getActiveLocalKeywordsByGridArea(String gridArea) {
        return keywordConfigMapper.findActiveLocalKeywordsByGridArea(KeywordConfig.KeywordType.LOCAL.name(), gridArea);
    }
    
    /**
     * 获取高优先级关键词
     */
    @Cacheable("highPriorityKeywords")
    public List<KeywordConfig> getHighPriorityKeywords() {
        return keywordConfigMapper.findAll().stream()
                .filter(k -> k.getPriority() == KeywordConfig.Priority.HIGH || k.getPriority() == KeywordConfig.Priority.URGENT)
                .filter(KeywordConfig::getIsActive)
                .collect(Collectors.toList());
    }
    
    /**
     * 创建关键词配置
     */
    @Transactional
    @CacheEvict(value = {"activeKeywords", "globalKeywords", "highPriorityKeywords"}, allEntries = true)
    public KeywordConfig createKeywordConfig(KeywordConfig keywordConfig) {
        log.info("创建关键词配置: {}", keywordConfig.getKeyword());
        
        // 检查关键词是否已存在
        List<KeywordConfig> existing = keywordConfigMapper.findAll().stream()
                .filter(k -> k.getKeyword().equals(keywordConfig.getKeyword()))
                .collect(Collectors.toList());
        if (!existing.isEmpty()) {
            throw new RuntimeException("关键词已存在: " + keywordConfig.getKeyword());
        }
        
        // 设置创建和更新时间
        keywordConfig.setCreatedAt(java.time.LocalDateTime.now());
        keywordConfig.setUpdatedAt(java.time.LocalDateTime.now());
        
        keywordConfigMapper.insert(keywordConfig);
        KeywordConfig saved = keywordConfig;
        log.info("关键词配置创建成功: {}", saved.getKeyword());
        
        return saved;
    }
    
    /**
     * 更新关键词配置
     */
    @Transactional
    @CacheEvict(value = {"activeKeywords", "globalKeywords", "highPriorityKeywords"}, allEntries = true)
    public KeywordConfig updateKeywordConfig(KeywordConfig keywordConfig) {
        log.info("更新关键词配置: {}", keywordConfig.getKeyword());
        
        KeywordConfig existing = keywordConfigMapper.findById(keywordConfig.getId());
        if (existing == null) {
            throw new RuntimeException("关键词配置不存在: " + keywordConfig.getId());
        }
        
        // 设置更新时间
        keywordConfig.setUpdatedAt(java.time.LocalDateTime.now());
        
        keywordConfigMapper.update(keywordConfig);
        KeywordConfig updated = keywordConfig;
        log.info("关键词配置更新成功: {}", updated.getKeyword());
        
        return updated;
    }
    
    /**
     * 删除关键词配置
     */
    @Transactional
    @CacheEvict(value = {"activeKeywords", "globalKeywords", "highPriorityKeywords"}, allEntries = true)
    public void deleteKeywordConfig(Long id) {
        KeywordConfig existing = keywordConfigMapper.findById(id);
        if (existing != null) {
            keywordConfigMapper.deleteById(id);
            log.info("关键词配置删除成功: {}", id);
        } else {
            throw new RuntimeException("关键词配置不存在: " + id);
        }
    }
    
    /**
     * 启用/禁用关键词配置
     */
    @Transactional
    @CacheEvict(value = {"activeKeywords", "globalKeywords", "highPriorityKeywords"}, allEntries = true)
    public void toggleKeywordConfig(Long id, boolean isActive) {
        KeywordConfig config = keywordConfigMapper.findById(id);
        if (config != null) {
            config.setIsActive(isActive);
            config.setUpdatedAt(java.time.LocalDateTime.now());
            keywordConfigMapper.update(config);
            log.info("关键词配置状态更新: {} -> {}", config.getKeyword(), isActive ? "启用" : "禁用");
        } else {
            throw new RuntimeException("关键词配置不存在: " + id);
        }
    }
    
    /**
     * 根据ID获取关键词配置
     */
    public Optional<KeywordConfig> getKeywordConfigById(Long id) {
        KeywordConfig config = keywordConfigMapper.findById(id);
        return Optional.ofNullable(config);
    }
    
    /**
     * 获取所有关键词配置
     */
    public List<KeywordConfig> getAllKeywordConfigs() {
        return keywordConfigMapper.findAll();
    }
    
    /**
     * 根据类型获取关键词配置
     */
    public List<KeywordConfig> getKeywordConfigsByType(KeywordConfig.KeywordType type) {
        return keywordConfigMapper.findAll().stream()
                .filter(k -> k.getType() == type)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据创建者获取关键词配置
     */
    public List<KeywordConfig> getKeywordConfigsByCreator(Long createdBy) {
        return keywordConfigMapper.findAll().stream()
                .filter(k -> k.getCreatedBy() != null && k.getCreatedBy().equals(createdBy))
                .collect(Collectors.toList());
    }
    
    /**
     * 批量创建关键词配置
     */
    @Transactional
    @CacheEvict(value = {"activeKeywords", "globalKeywords", "highPriorityKeywords"}, allEntries = true)
    public List<KeywordConfig> batchCreateKeywordConfigs(List<KeywordConfig> keywordConfigs) {
        log.info("批量创建关键词配置: {} 个", keywordConfigs.size());
        
        // 获取已存在的关键词
        List<String> existingKeywords = keywordConfigMapper.findAll().stream()
                .map(KeywordConfig::getKeyword)
                .collect(Collectors.toList());
        
        // 过滤掉已存在的关键词
        List<KeywordConfig> newConfigs = keywordConfigs.stream()
                .filter(config -> !existingKeywords.contains(config.getKeyword()))
                .collect(Collectors.toList());
        
        if (newConfigs.isEmpty()) {
            log.warn("所有关键词都已存在，无需创建");
            return List.of();
        }
        
        // 批量插入
        List<KeywordConfig> saved = newConfigs.stream().map(config -> {
            config.setCreatedAt(java.time.LocalDateTime.now());
            config.setUpdatedAt(java.time.LocalDateTime.now());
            keywordConfigMapper.insert(config);
            return config;
        }).collect(Collectors.toList());
        log.info("批量创建关键词配置成功: {} 个", saved.size());
        
        return saved;
    }
    
    /**
     * 获取关键词字符串列表（用于快速匹配）
     */
    @Cacheable("keywordStrings")
    public List<String> getActiveKeywordStrings() {
        return getActiveKeywords().stream()
                .map(KeywordConfig::getKeyword)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取关键词统计数据
     */
    public java.util.Map<String, Object> getKeywordStats(Long id) {
        KeywordConfig keyword = keywordConfigMapper.findById(id);
        if (keyword == null) {
            throw new RuntimeException("关键词配置不存在: " + id);
        }
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // 基本信息
        stats.put("keyword", keyword.getKeyword());
        stats.put("category", keyword.getType().toString());
        stats.put("priority", keyword.getPriority().toString());
        stats.put("status", keyword.getIsActive() ? "active" : "inactive");
        
        // 统计数据（模拟数据，实际应从数据库或缓存获取）
        stats.put("totalHits", keyword.getHitCount() != null ? keyword.getHitCount() : 0);
        stats.put("uniqueUsers", generateRandomStats(10, 100));
        stats.put("lastHitDate", keyword.getUpdatedAt());
        
        // 趋势数据（最近7天）
        java.util.List<java.util.Map<String, Object>> hitTrend = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            java.util.Map<String, Object> dayData = new java.util.HashMap<>();
            dayData.put("date", today.minusDays(i).toString());
            dayData.put("count", generateRandomStats(0, 20));
            hitTrend.add(dayData);
        }
        stats.put("hitTrend", hitTrend);
        
        // 用户分布数据
        java.util.List<java.util.Map<String, Object>> userDistribution = new java.util.ArrayList<>();
        String[] areas = {"网格A", "网格B", "网格C", "网格D"};
        for (String area : areas) {
            java.util.Map<String, Object> areaData = new java.util.HashMap<>();
            areaData.put("name", area);
            areaData.put("value", generateRandomStats(5, 50));
            userDistribution.add(areaData);
        }
        stats.put("userDistribution", userDistribution);
        
        // 相关关键词
        java.util.List<java.util.Map<String, Object>> relatedKeywords = new java.util.ArrayList<>();
        java.util.List<KeywordConfig> allKeywords = keywordConfigMapper.findAll().stream()
                .filter(k -> k.getType() == keyword.getType())
                .collect(Collectors.toList());
        for (int i = 0; i < Math.min(5, allKeywords.size()); i++) {
            if (!allKeywords.get(i).getId().equals(id)) {
                java.util.Map<String, Object> relatedData = new java.util.HashMap<>();
                relatedData.put("text", allKeywords.get(i).getKeyword());
                relatedData.put("count", generateRandomStats(1, 15));
                relatedKeywords.add(relatedData);
            }
        }
        stats.put("relatedKeywords", relatedKeywords);
        
        return stats;
    }
    
    /**
     * 获取关键词命中率数据
     */
    public java.util.Map<String, Object> getKeywordHitRate() {
        java.util.Map<String, Object> hitRateData = new java.util.HashMap<>();
        
        java.util.List<KeywordConfig> activeKeywords = getActiveKeywords();
        java.util.List<java.util.Map<String, Object>> keywordStats = new java.util.ArrayList<>();
        
        String[] colors = {"#3b82f6", "#ef4444", "#10b981", "#f59e0b", "#8b5cf6"};
        
        for (int i = 0; i < Math.min(5, activeKeywords.size()); i++) {
            KeywordConfig keyword = activeKeywords.get(i);
            java.util.Map<String, Object> stat = new java.util.HashMap<>();
            stat.put("name", keyword.getKeyword());
            int count = generateRandomStats(10, 100);
            stat.put("count", count);
            stat.put("percentage", Math.min(100, count));
            stat.put("color", colors[i % colors.length]);
            keywordStats.add(stat);
        }
        
        hitRateData.put("keywords", keywordStats);
        return hitRateData;
    }
    
    /**
     * 获取关键词分析报告
     */
    public java.util.Map<String, Object> getKeywordAnalysis(int days, String category, String priority) {
        java.util.Map<String, Object> analysis = new java.util.HashMap<>();
        
        // 总体统计
        java.util.Map<String, Object> overview = new java.util.HashMap<>();
        List<KeywordConfig> allConfigs = keywordConfigMapper.findAll();
        overview.put("totalKeywords", allConfigs.size());
        overview.put("activeKeywords", allConfigs.stream().mapToInt(k -> k.getIsActive() ? 1 : 0).sum());
        overview.put("totalHits", generateRandomStats(1000, 5000));
        overview.put("avgHitsPerKeyword", generateRandomStats(10, 50));
        analysis.put("overview", overview);
        
        // 分类统计
        java.util.List<java.util.Map<String, Object>> categoryStats = new java.util.ArrayList<>();
        for (KeywordConfig.KeywordType type : KeywordConfig.KeywordType.values()) {
            java.util.Map<String, Object> catStat = new java.util.HashMap<>();
            catStat.put("category", type.toString());
            catStat.put("count", keywordConfigMapper.findAll().stream()
                    .mapToInt(k -> k.getType() == type ? 1 : 0).sum());
            catStat.put("hits", generateRandomStats(100, 1000));
            categoryStats.add(catStat);
        }
        analysis.put("categoryStats", categoryStats);
        
        // 优先级统计
        java.util.List<java.util.Map<String, Object>> priorityStats = new java.util.ArrayList<>();
        for (KeywordConfig.Priority pri : KeywordConfig.Priority.values()) {
            java.util.Map<String, Object> priStat = new java.util.HashMap<>();
            priStat.put("priority", pri.toString());
            priStat.put("count", keywordConfigMapper.findAll().stream()
                    .mapToInt(k -> k.getPriority() == pri ? 1 : 0).sum());
            priStat.put("hits", generateRandomStats(50, 500));
            priorityStats.add(priStat);
        }
        analysis.put("priorityStats", priorityStats);
        
        // 趋势分析
        java.util.List<java.util.Map<String, Object>> trendData = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            java.util.Map<String, Object> dayData = new java.util.HashMap<>();
            dayData.put("date", today.minusDays(i).toString());
            dayData.put("hits", generateRandomStats(50, 200));
            dayData.put("uniqueKeywords", generateRandomStats(10, 30));
            trendData.add(dayData);
        }
        analysis.put("trendData", trendData);
        
        return analysis;
    }
    
    /**
     * 生成随机统计数据（用于演示）
     */
    private int generateRandomStats(int min, int max) {
        return min + (int) (Math.random() * (max - min + 1));
    }

    /**
     * 清除所有缓存
     */
    @CacheEvict(value = {"activeKeywords", "globalKeywords", "highPriorityKeywords", "keywordStrings"}, allEntries = true)
    public void clearCache() {
        log.info("关键词配置缓存已清除");
    }
}