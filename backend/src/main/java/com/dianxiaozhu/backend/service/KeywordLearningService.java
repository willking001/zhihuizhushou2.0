package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.KeywordConfig;
import com.dianxiaozhu.backend.mapper.KeywordConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 关键词学习服务
 * 实现智能自学习机制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordLearningService {

    private final JdbcTemplate jdbcTemplate;
    private final KeywordConfigMapper keywordConfigMapper;
    
    @Value("${keyword.learning.enabled:true}")
    private boolean learningEnabled;
    
    @Value("${keyword.learning.min-frequency:3}")
    private int minFrequency;
    
    @Value("${keyword.learning.learning-window-days:7}")
    private int learningWindowDays;
    
    @Value("${keyword.learning.confidence-threshold:0.7}")
    private double confidenceThreshold;
    
    @Value("${keyword.learning.max-learned-keywords:100}")
    private int maxLearnedKeywords;

    /**
     * 记录关键词检测
     */
    @Async
    public void recordKeywordDetection(String keyword, String messageContent, 
                                     String gridArea, Long userId, double confidenceScore) {
        if (!learningEnabled) {
            return;
        }
        
        try {
            String sql = "INSERT INTO keyword_learning_records (keyword, message_content, grid_area, user_id, confidence_score, context_info) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
            
            // 构建上下文信息
            Map<String, Object> contextInfo = new HashMap<>();
            contextInfo.put("messageLength", messageContent.length());
            contextInfo.put("detectionTime", LocalDateTime.now().toString());
            contextInfo.put("source", "auto_detection");
            
            jdbcTemplate.update(sql, keyword, messageContent, gridArea, userId, 
                              BigDecimal.valueOf(confidenceScore), 
                              objectToJson(contextInfo));
            
            log.debug("记录关键词检测: {} (置信度: {})", keyword, confidenceScore);
            
            // 检查是否需要推荐
            checkForRecommendation(keyword, gridArea);
            
        } catch (Exception e) {
            log.error("记录关键词检测失败", e);
        }
    }

    /**
     * 检查关键词推荐
     */
    private void checkForRecommendation(String keyword, String gridArea) {
        try {
            // 检查关键词是否已存在
            boolean exists = keywordConfigMapper.findAll().stream()
                    .anyMatch(k -> k.getKeyword().equals(keyword));
            if (exists) {
                return;
            }
            
            // 检查是否已有推荐
            String checkSql = "SELECT COUNT(*) FROM keyword_recommendations WHERE keyword = ? AND status = 'PENDING'";
            Integer existingCount = jdbcTemplate.queryForObject(checkSql, Integer.class, keyword);
            if (existingCount != null && existingCount > 0) {
                return;
            }
            
            // 计算频率和置信度
            String frequencySql = "SELECT COUNT(*) as frequency, AVG(confidence_score) as avg_confidence " +
                                "FROM keyword_learning_records " +
                                "WHERE keyword = ? AND grid_area = ? " +
                                "AND detection_time >= DATE_SUB(NOW(), INTERVAL ? DAY)";
            
            Map<String, Object> stats = jdbcTemplate.queryForMap(frequencySql, keyword, gridArea, learningWindowDays);
            
            int frequency = ((Number) stats.get("frequency")).intValue();
            double avgConfidence = stats.get("avg_confidence") != null ? 
                                 ((Number) stats.get("avg_confidence")).doubleValue() : 0.0;
            
            // 检查是否满足推荐条件
            if (frequency >= minFrequency && avgConfidence >= confidenceThreshold) {
                createRecommendation(keyword, frequency, avgConfidence, gridArea);
            }
            
        } catch (Exception e) {
            log.error("检查关键词推荐失败", e);
        }
    }

    /**
     * 创建关键词推荐
     */
    private void createRecommendation(String keyword, int frequency, double confidence, String gridArea) {
        try {
            String sql = "INSERT INTO keyword_recommendations (keyword, frequency, confidence_score, grid_area) " +
                        "VALUES (?, ?, ?, ?)";
            
            jdbcTemplate.update(sql, keyword, frequency, BigDecimal.valueOf(confidence), gridArea);
            
            log.info("创建关键词推荐: {} (频率: {}, 置信度: {}, 区域: {})", 
                    keyword, frequency, confidence, gridArea);
            
        } catch (Exception e) {
            log.error("创建关键词推荐失败", e);
        }
    }

    /**
     * 获取待审核推荐
     */
    public List<Map<String, Object>> getPendingRecommendations(String gridArea) {
        String sql = "SELECT * FROM keyword_recommendations WHERE status = 'PENDING'";
        List<Object> params = new ArrayList<>();
        
        if (gridArea != null && !gridArea.isEmpty()) {
            sql += " AND grid_area = ?";
            params.add(gridArea);
        }
        
        sql += " ORDER BY frequency DESC, confidence_score DESC";
        
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    /**
     * 审核推荐
     */
    @Transactional
    public void reviewRecommendation(Long recommendationId, String status, 
                                   Long reviewerId, String reviewNotes) {
        try {
            // 更新推荐状态
            String updateSql = "UPDATE keyword_recommendations SET status = ?, reviewed_at = NOW(), " +
                             "reviewed_by = ?, review_notes = ? WHERE id = ?";
            
            jdbcTemplate.update(updateSql, status, reviewerId, reviewNotes, recommendationId);
            
            // 如果批准，创建关键词配置
            if ("APPROVED".equals(status)) {
                createKeywordFromRecommendation(recommendationId);
            }
            
            log.info("审核关键词推荐: {} -> {}", recommendationId, status);
            
        } catch (Exception e) {
            log.error("审核关键词推荐失败", e);
            throw new RuntimeException("审核失败: " + e.getMessage());
        }
    }

    /**
     * 从推荐创建关键词配置
     */
    private void createKeywordFromRecommendation(Long recommendationId) {
        try {
            // 获取推荐信息
            String selectSql = "SELECT * FROM keyword_recommendations WHERE id = ?";
            Map<String, Object> recommendation = jdbcTemplate.queryForMap(selectSql, recommendationId);
            
            // 创建关键词配置
            KeywordConfig config = new KeywordConfig();
            config.setKeyword((String) recommendation.get("keyword"));
            config.setType(KeywordConfig.KeywordType.LOCAL);
            config.setPriority(KeywordConfig.Priority.NORMAL);
            config.setGridArea((String) recommendation.get("grid_area"));
            config.setDescription("智能学习推荐关键词");
            config.setIsActive(true);
            config.setCreatedBy(1L); // 系统创建
            config.setCreatedAt(java.time.LocalDateTime.now());
            config.setUpdatedAt(java.time.LocalDateTime.now());
            
            keywordConfigMapper.insert(config);
            
            log.info("从推荐创建关键词配置: {}", config.getKeyword());
            
        } catch (Exception e) {
            log.error("从推荐创建关键词配置失败", e);
            throw new RuntimeException("创建关键词配置失败: " + e.getMessage());
        }
    }

    /**
     * 获取学习统计
     */
    public Map<String, Object> getLearningStats(String gridArea) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 总检测次数
            String totalDetectionsSql = "SELECT COUNT(*) FROM keyword_learning_records";
            List<Object> params = new ArrayList<>();
            
            if (gridArea != null && !gridArea.isEmpty()) {
                totalDetectionsSql += " WHERE grid_area = ?";
                params.add(gridArea);
            }
            
            Integer totalDetections = jdbcTemplate.queryForObject(totalDetectionsSql, Integer.class, params.toArray());
            stats.put("totalDetections", totalDetections);
            
            // 待审核推荐数
            String pendingRecommendationsSql = "SELECT COUNT(*) FROM keyword_recommendations WHERE status = 'PENDING'";
            if (gridArea != null && !gridArea.isEmpty()) {
                pendingRecommendationsSql += " AND grid_area = ?";
            }
            
            Integer pendingRecommendations = jdbcTemplate.queryForObject(pendingRecommendationsSql, Integer.class, params.toArray());
            stats.put("pendingRecommendations", pendingRecommendations);
            
            // 已批准推荐数
            String approvedRecommendationsSql = "SELECT COUNT(*) FROM keyword_recommendations WHERE status = 'APPROVED'";
            if (gridArea != null && !gridArea.isEmpty()) {
                approvedRecommendationsSql += " AND grid_area = ?";
            }
            
            Integer approvedRecommendations = jdbcTemplate.queryForObject(approvedRecommendationsSql, Integer.class, params.toArray());
            stats.put("approvedRecommendations", approvedRecommendations);
            
            // 学习效率
            double learningEfficiency = totalDetections > 0 ? 
                                      (double) approvedRecommendations / totalDetections * 100 : 0;
            stats.put("learningEfficiency", Math.round(learningEfficiency * 100.0) / 100.0);
            
        } catch (Exception e) {
            log.error("获取学习统计失败", e);
        }
        
        return stats;
    }

    /**
     * 获取热门关键词
     */
    public List<Map<String, Object>> getHotKeywords(String gridArea, int limit) {
        String sql = "SELECT keyword, COUNT(*) as frequency, AVG(confidence_score) as avg_confidence " +
                    "FROM keyword_learning_records " +
                    "WHERE detection_time >= DATE_SUB(NOW(), INTERVAL ? DAY)";
        
        List<Object> params = new ArrayList<>();
        params.add(learningWindowDays);
        
        if (gridArea != null && !gridArea.isEmpty()) {
            sql += " AND grid_area = ?";
            params.add(gridArea);
        }
        
        sql += " GROUP BY keyword ORDER BY frequency DESC, avg_confidence DESC LIMIT ?";
        params.add(limit);
        
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    /**
     * 清理过期学习记录
     */
    @Async
    public void cleanupExpiredRecords() {
        try {
            String sql = "DELETE FROM keyword_learning_records WHERE detection_time < DATE_SUB(NOW(), INTERVAL ? DAY)";
            int deletedCount = jdbcTemplate.update(sql, learningWindowDays * 2); // 保留两倍窗口期的数据
            
            if (deletedCount > 0) {
                log.info("清理过期学习记录: {} 条", deletedCount);
            }
            
        } catch (Exception e) {
            log.error("清理过期学习记录失败", e);
        }
    }

    /**
     * 对象转JSON字符串
     */
    private String objectToJson(Object obj) {
        try {
            // 简单的JSON序列化，实际项目中应使用Jackson或Gson
            if (obj instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) obj;
                return map.entrySet().stream()
                    .map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
                    .collect(Collectors.joining(",", "{", "}"));
            }
            return obj.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
}