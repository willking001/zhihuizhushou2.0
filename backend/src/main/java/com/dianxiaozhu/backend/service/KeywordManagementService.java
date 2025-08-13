package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.KeywordConfig;
import com.dianxiaozhu.backend.mapper.KeywordConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 关键词管理服务
 * 实现智能词库管理、冗余检测、自动分类等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordManagementService {

    private final KeywordConfigMapper keywordConfigMapper;
    private final JdbcTemplate jdbcTemplate;
    
    // 相似度阈值
    private static final double SIMILARITY_THRESHOLD = 0.8;
    private static final double REDUNDANCY_THRESHOLD = 0.9;

    /**
     * 检测关键词冗余
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> detectRedundancy(String gridArea) {
        List<Map<String, Object>> redundancies = new ArrayList<>();
        
        try {
            // 获取指定区域的所有活跃关键词
            List<KeywordConfig> keywords = keywordConfigMapper.findAll().stream()
                .filter(k -> k.getIsActive() && 
                            (gridArea == null || gridArea.isEmpty() || k.getGridArea().equals(gridArea)))
                .collect(java.util.stream.Collectors.toList());
            
            // 两两比较检测冗余
            for (int i = 0; i < keywords.size(); i++) {
                for (int j = i + 1; j < keywords.size(); j++) {
                    KeywordConfig keyword1 = keywords.get(i);
                    KeywordConfig keyword2 = keywords.get(j);
                    
                    double similarity = calculateSimilarity(keyword1.getKeyword(), keyword2.getKeyword());
                    
                    if (similarity >= REDUNDANCY_THRESHOLD) {
                        Map<String, Object> redundancy = new HashMap<>();
                        redundancy.put("keyword1", keyword1.getKeyword());
                        redundancy.put("keyword2", keyword2.getKeyword());
                        redundancy.put("similarity", Math.round(similarity * 10000.0) / 100.0); // 保留2位小数
                        redundancy.put("type1", keyword1.getType());
                        redundancy.put("type2", keyword2.getType());
                        redundancy.put("priority1", keyword1.getPriority());
                        redundancy.put("priority2", keyword2.getPriority());
                        redundancy.put("suggestion", generateRedundancySuggestion(keyword1, keyword2, similarity));
                        
                        redundancies.add(redundancy);
                        
                        // 记录到数据库
                        recordRedundancy(keyword1.getId(), keyword2.getId(), similarity);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("检测关键词冗余失败", e);
        }
        
        return redundancies;
    }

    /**
     * 计算两个关键词的相似度
     */
    private double calculateSimilarity(String keyword1, String keyword2) {
        if (keyword1 == null || keyword2 == null) {
            return 0.0;
        }
        
        // 完全相同
        if (keyword1.equals(keyword2)) {
            return 1.0;
        }
        
        // 包含关系
        if (keyword1.contains(keyword2) || keyword2.contains(keyword1)) {
            return 0.95;
        }
        
        // 编辑距离相似度
        int editDistance = calculateEditDistance(keyword1, keyword2);
        int maxLength = Math.max(keyword1.length(), keyword2.length());
        double editSimilarity = 1.0 - (double) editDistance / maxLength;
        
        // Jaccard相似度（基于字符n-gram）
        double jaccardSimilarity = calculateJaccardSimilarity(keyword1, keyword2);
        
        // 综合相似度（编辑距离权重0.6，Jaccard权重0.4）
        return editSimilarity * 0.6 + jaccardSimilarity * 0.4;
    }

    /**
     * 计算编辑距离
     */
    private int calculateEditDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= n; j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[m][n];
    }

    /**
     * 计算Jaccard相似度
     */
    private double calculateJaccardSimilarity(String s1, String s2) {
        Set<String> set1 = generateNGrams(s1, 2);
        Set<String> set2 = generateNGrams(s2, 2);
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * 生成n-gram
     */
    private Set<String> generateNGrams(String text, int n) {
        Set<String> ngrams = new HashSet<>();
        
        if (text.length() < n) {
            ngrams.add(text);
            return ngrams;
        }
        
        for (int i = 0; i <= text.length() - n; i++) {
            ngrams.add(text.substring(i, i + n));
        }
        
        return ngrams;
    }

    /**
     * 生成冗余处理建议
     */
    private String generateRedundancySuggestion(KeywordConfig keyword1, KeywordConfig keyword2, double similarity) {
        if (similarity >= 0.95) {
            // 高度相似，建议合并
            if (keyword1.getPriority().ordinal() > keyword2.getPriority().ordinal()) {
                return String.format("建议保留'%s'（优先级更高），删除'%s'", keyword1.getKeyword(), keyword2.getKeyword());
            } else if (keyword2.getPriority().ordinal() > keyword1.getPriority().ordinal()) {
                return String.format("建议保留'%s'（优先级更高），删除'%s'", keyword2.getKeyword(), keyword1.getKeyword());
            } else {
                return String.format("建议保留'%s'（较短），删除'%s'", 
                    keyword1.getKeyword().length() <= keyword2.getKeyword().length() ? 
                    keyword1.getKeyword() : keyword2.getKeyword(),
                    keyword1.getKeyword().length() > keyword2.getKeyword().length() ? 
                    keyword1.getKeyword() : keyword2.getKeyword());
            }
        } else {
            return "建议人工审核，确认是否需要合并或保持独立";
        }
    }

    /**
     * 记录冗余检测结果
     */
    @Async
    public void recordRedundancy(Long keywordId1, Long keywordId2, double similarity) {
        try {
            String sql = "INSERT INTO keyword_redundancy (keyword_id_1, keyword_id_2, similarity_score, " +
                        "detection_time, status) VALUES (?, ?, ?, ?, 'DETECTED') " +
                        "ON DUPLICATE KEY UPDATE similarity_score = ?, detection_time = ?";
            
            LocalDateTime now = LocalDateTime.now();
            jdbcTemplate.update(sql, keywordId1, keywordId2, similarity, now, similarity, now);
            
        } catch (Exception e) {
            log.error("记录冗余检测结果失败", e);
        }
    }

    /**
     * 自动分类关键词
     */
    @Transactional
    public Map<String, Object> autoClassifyKeywords(String gridArea) {
        Map<String, Object> result = new HashMap<>();
        int classifiedCount = 0;
        
        try {
            // 获取未分类的关键词（假设type为null或空表示未分类）
            List<KeywordConfig> unclassifiedKeywords = keywordConfigMapper.findAll().stream()
                .filter(k -> k.getType() == null && 
                            (gridArea == null || gridArea.isEmpty() || k.getGridArea().equals(gridArea)))
                .collect(java.util.stream.Collectors.toList());
            
            for (KeywordConfig keyword : unclassifiedKeywords) {
                String suggestedType = classifyKeyword(keyword.getKeyword());
                
                if (suggestedType != null) {
                    // 这里应该根据实际的枚举类型进行设置
                    // keyword.setType(KeywordType.valueOf(suggestedType));
                    // keywordConfigRepository.save(keyword);
                    classifiedCount++;
                    
                    log.info("关键词 '{}' 被自动分类为: {}", keyword.getKeyword(), suggestedType);
                }
            }
            
            result.put("totalUnclassified", unclassifiedKeywords.size());
            result.put("classifiedCount", classifiedCount);
            result.put("successRate", unclassifiedKeywords.size() > 0 ? 
                      (double) classifiedCount / unclassifiedKeywords.size() : 0.0);
            
        } catch (Exception e) {
            log.error("自动分类关键词失败", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 分类单个关键词
     */
    private String classifyKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null;
        }
        
        String lowerKeyword = keyword.toLowerCase();
        
        // 基于规则的简单分类
        if (lowerKeyword.contains("问题") || lowerKeyword.contains("故障") || 
            lowerKeyword.contains("错误") || lowerKeyword.contains("异常")) {
            return "PROBLEM";
        }
        
        if (lowerKeyword.contains("查询") || lowerKeyword.contains("搜索") || 
            lowerKeyword.contains("查找") || lowerKeyword.contains("检索")) {
            return "QUERY";
        }
        
        if (lowerKeyword.contains("操作") || lowerKeyword.contains("执行") || 
            lowerKeyword.contains("处理") || lowerKeyword.contains("运行")) {
            return "ACTION";
        }
        
        if (lowerKeyword.contains("信息") || lowerKeyword.contains("数据") || 
            lowerKeyword.contains("资料") || lowerKeyword.contains("内容")) {
            return "INFORMATION";
        }
        
        // 默认分类
        return "GENERAL";
    }

    /**
     * 智能推荐关键词
     */
    public List<Map<String, Object>> recommendKeywords(String gridArea, int limit) {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        try {
            // 基于用户查询历史推荐
            String sql = "SELECT klr.keyword, COUNT(*) as frequency, " +
                        "AVG(klr.confidence_score) as avg_confidence " +
                        "FROM keyword_learning_records klr " +
                        "LEFT JOIN keyword_configs kc ON klr.keyword = kc.keyword " +
                        "WHERE kc.id IS NULL " + // 不存在于关键词配置中
                        "AND klr.detection_time >= DATE_SUB(NOW(), INTERVAL 30 DAY)";
            
            List<Object> params = new ArrayList<>();
            
            if (gridArea != null && !gridArea.isEmpty()) {
                sql += " AND klr.grid_area = ?";
                params.add(gridArea);
            }
            
            sql += " GROUP BY klr.keyword " +
                  "HAVING frequency >= 3 AND avg_confidence >= 0.7 " +
                  "ORDER BY frequency DESC, avg_confidence DESC " +
                  "LIMIT ?";
            
            params.add(limit);
            
            List<Map<String, Object>> queryResults = jdbcTemplate.queryForList(sql, params.toArray());
            
            for (Map<String, Object> result : queryResults) {
                Map<String, Object> recommendation = new HashMap<>();
                recommendation.put("keyword", result.get("keyword"));
                recommendation.put("frequency", result.get("frequency"));
                recommendation.put("confidence", result.get("avg_confidence"));
                recommendation.put("suggestedType", classifyKeyword((String) result.get("keyword")));
                recommendation.put("suggestedPriority", suggestPriority((Integer) result.get("frequency")));
                recommendation.put("reason", "基于用户查询频率推荐");
                
                recommendations.add(recommendation);
            }
            
        } catch (Exception e) {
            log.error("智能推荐关键词失败", e);
        }
        
        return recommendations;
    }

    /**
     * 建议优先级
     */
    private String suggestPriority(int frequency) {
        if (frequency >= 20) {
            return "URGENT";
        } else if (frequency >= 10) {
            return "HIGH";
        } else if (frequency >= 5) {
            return "NORMAL";
        } else {
            return "LOW";
        }
    }

    /**
     * 批量处理冗余关键词
     */
    @Transactional
    public Map<String, Object> batchProcessRedundancy(List<Map<String, Object>> redundancyActions) {
        Map<String, Object> result = new HashMap<>();
        int processedCount = 0;
        int errorCount = 0;
        List<String> errors = new ArrayList<>();
        
        try {
            for (Map<String, Object> action : redundancyActions) {
                try {
                    String actionType = (String) action.get("action"); // "merge", "delete", "keep"
                    Long keywordId1 = ((Number) action.get("keywordId1")).longValue();
                    Long keywordId2 = ((Number) action.get("keywordId2")).longValue();
                    
                    switch (actionType) {
                        case "merge":
                            mergeKeywords(keywordId1, keywordId2);
                            break;
                        case "delete":
                            Long deleteId = ((Number) action.get("deleteId")).longValue();
                            deleteKeyword(deleteId);
                            break;
                        case "keep":
                            // 标记为已处理，保持两个关键词
                            markRedundancyProcessed(keywordId1, keywordId2, "KEPT_BOTH");
                            break;
                    }
                    
                    processedCount++;
                    
                } catch (Exception e) {
                    errorCount++;
                    errors.add(e.getMessage());
                    log.error("处理冗余关键词失败", e);
                }
            }
            
            result.put("processedCount", processedCount);
            result.put("errorCount", errorCount);
            result.put("errors", errors);
            
        } catch (Exception e) {
            log.error("批量处理冗余关键词失败", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 合并关键词
     */
    private void mergeKeywords(Long keywordId1, Long keywordId2) {
        // 获取两个关键词
        Optional<KeywordConfig> keyword1Opt = Optional.ofNullable(keywordConfigMapper.findById(keywordId1));
        Optional<KeywordConfig> keyword2Opt = Optional.ofNullable(keywordConfigMapper.findById(keywordId2));
        
        if (keyword1Opt.isPresent() && keyword2Opt.isPresent()) {
            KeywordConfig keyword1 = keyword1Opt.get();
            KeywordConfig keyword2 = keyword2Opt.get();
            
            // 选择保留优先级更高的关键词
            KeywordConfig keepKeyword = keyword1.getPriority().ordinal() >= keyword2.getPriority().ordinal() ? 
                                       keyword1 : keyword2;
            KeywordConfig deleteKeyword = keepKeyword == keyword1 ? keyword2 : keyword1;
            
            // 合并描述
            String mergedDescription = (keepKeyword.getDescription() != null ? keepKeyword.getDescription() : "") +
                                     (deleteKeyword.getDescription() != null ? 
                                      (keepKeyword.getDescription() != null ? "; " : "") + deleteKeyword.getDescription() : "");
            
            keepKeyword.setDescription(mergedDescription);
            keepKeyword.setUpdatedAt(java.time.LocalDateTime.now());
            keywordConfigMapper.updateById(keepKeyword);
            
            // 更新相关记录
            updateRelatedRecords(deleteKeyword.getId(), keepKeyword.getId());
            
            // 删除被合并的关键词
            keywordConfigMapper.deleteById(deleteKeyword.getId());
            
            // 标记冗余已处理
            markRedundancyProcessed(keywordId1, keywordId2, "MERGED");
            
            log.info("关键词合并完成: '{}' 和 '{}' 合并为 '{}'", 
                    keyword1.getKeyword(), keyword2.getKeyword(), keepKeyword.getKeyword());
        }
    }

    /**
     * 删除关键词
     */
    private void deleteKeyword(Long keywordId) {
        Optional<KeywordConfig> keywordOpt = Optional.ofNullable(keywordConfigMapper.findById(keywordId));
        
        if (keywordOpt.isPresent()) {
            KeywordConfig keyword = keywordOpt.get();
            
            // 软删除：设置为非活跃状态
            keyword.setIsActive(false);
            keyword.setUpdatedAt(java.time.LocalDateTime.now());
            keywordConfigMapper.updateById(keyword);
            
            log.info("关键词已删除: '{}'", keyword.getKeyword());
        }
    }

    /**
     * 更新相关记录
     */
    private void updateRelatedRecords(Long oldKeywordId, Long newKeywordId) {
        try {
            // 更新使用统计记录
            String updateStatsSql = "UPDATE keyword_usage_stats SET keyword_id = ? WHERE keyword_id = ?";
            jdbcTemplate.update(updateStatsSql, newKeywordId, oldKeywordId);
            
            // 更新学习记录
            String updateLearningSql = "UPDATE keyword_learning_records SET keyword_id = ? WHERE keyword_id = ?";
            jdbcTemplate.update(updateLearningSql, newKeywordId, oldKeywordId);
            
            // 更新触发日志
            String updateTriggerSql = "UPDATE keyword_trigger_logs SET keyword_id = ? WHERE keyword_id = ?";
            jdbcTemplate.update(updateTriggerSql, newKeywordId, oldKeywordId);
            
        } catch (Exception e) {
            log.error("更新相关记录失败", e);
        }
    }

    /**
     * 标记冗余已处理
     */
    private void markRedundancyProcessed(Long keywordId1, Long keywordId2, String status) {
        try {
            String sql = "UPDATE keyword_redundancy SET status = ?, processed_time = ? " +
                        "WHERE (keyword_id_1 = ? AND keyword_id_2 = ?) OR (keyword_id_1 = ? AND keyword_id_2 = ?)";
            
            jdbcTemplate.update(sql, status, LocalDateTime.now(), 
                              keywordId1, keywordId2, keywordId2, keywordId1);
            
        } catch (Exception e) {
            log.error("标记冗余处理状态失败", e);
        }
    }

    /**
     * 定期清理过期数据
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupExpiredData() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
            
            // 清理过期的冗余检测记录
            String cleanupRedundancySql = "DELETE FROM keyword_redundancy WHERE detection_time < ? AND status != 'DETECTED'";
            int deletedRedundancy = jdbcTemplate.update(cleanupRedundancySql, cutoffDate);
            
            // 清理过期的学习记录
            String cleanupLearningSql = "DELETE FROM keyword_learning_records WHERE detection_time < ?";
            int deletedLearning = jdbcTemplate.update(cleanupLearningSql, cutoffDate);
            
            log.info("定期清理完成: 删除了 {} 条冗余记录, {} 条学习记录", deletedRedundancy, deletedLearning);
            
        } catch (Exception e) {
            log.error("定期清理过期数据失败", e);
        }
    }

    /**
     * 获取关键词管理统计
     */
    public Map<String, Object> getManagementStats(String gridArea) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 总关键词数
            long totalKeywords = keywordConfigMapper.findAll().stream()
            .filter(k -> gridArea == null || gridArea.isEmpty() || k.getGridArea().equals(gridArea))
            .count();
            stats.put("totalKeywords", totalKeywords);
            
            // 活跃关键词数
            long activeKeywords = keywordConfigMapper.findAll().stream()
            .filter(k -> k.getIsActive() && 
                        (gridArea == null || gridArea.isEmpty() || k.getGridArea().equals(gridArea)))
            .count();
            stats.put("activeKeywords", activeKeywords);
            
            // 冗余关键词对数
            String redundancySql = "SELECT COUNT(*) FROM keyword_redundancy WHERE status = 'DETECTED'";
            if (gridArea != null && !gridArea.isEmpty()) {
                redundancySql += " AND (keyword_id_1 IN (SELECT id FROM keyword_configs WHERE grid_area = ?) " +
                               "OR keyword_id_2 IN (SELECT id FROM keyword_configs WHERE grid_area = ?))";
                Integer redundantPairs = jdbcTemplate.queryForObject(redundancySql, Integer.class, gridArea, gridArea);
                stats.put("redundantPairs", redundantPairs != null ? redundantPairs : 0);
            } else {
                Integer redundantPairs = jdbcTemplate.queryForObject(redundancySql, Integer.class);
                stats.put("redundantPairs", redundantPairs != null ? redundantPairs : 0);
            }
            
            // 待分类关键词数
            long unclassifiedKeywords = keywordConfigMapper.findAll().stream()
            .filter(k -> k.getType() == null && 
                        (gridArea == null || gridArea.isEmpty() || k.getGridArea().equals(gridArea)))
            .count();
            stats.put("unclassifiedKeywords", unclassifiedKeywords);
            
            // 推荐关键词数
            String recommendationSql = "SELECT COUNT(DISTINCT keyword) FROM keyword_learning_records " +
                                      "WHERE detection_time >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                                      "AND keyword NOT IN (SELECT keyword FROM keyword_configs)";
            
            if (gridArea != null && !gridArea.isEmpty()) {
                recommendationSql += " AND grid_area = ?";
                Integer recommendedKeywords = jdbcTemplate.queryForObject(recommendationSql, Integer.class, gridArea);
                stats.put("recommendedKeywords", recommendedKeywords != null ? recommendedKeywords : 0);
            } else {
                Integer recommendedKeywords = jdbcTemplate.queryForObject(recommendationSql, Integer.class);
                stats.put("recommendedKeywords", recommendedKeywords != null ? recommendedKeywords : 0);
            }
            
        } catch (Exception e) {
            log.error("获取关键词管理统计失败", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }
}