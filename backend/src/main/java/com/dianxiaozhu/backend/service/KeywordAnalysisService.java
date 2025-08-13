package com.dianxiaozhu.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 关键词分析服务
 * 实现关键词使用分析、热词发现、趋势预测等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordAnalysisService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 记录关键词使用统计
     */
    @Async
    public void recordKeywordUsage(Long keywordId, String gridArea, boolean triggered, 
                                  int responseTimeMs, Long userId) {
        try {
            LocalDate today = LocalDate.now();
            
            // 检查今日统计记录是否存在
            String checkSql = "SELECT id FROM keyword_usage_stats WHERE keyword_id = ? AND stat_date = ? AND grid_area = ?";
            List<Map<String, Object>> existing = jdbcTemplate.queryForList(checkSql, keywordId, today, gridArea);
            
            if (existing.isEmpty()) {
                // 创建新的统计记录
                String insertSql = "INSERT INTO keyword_usage_stats (keyword_id, stat_date, hit_count, " +
                                 "unique_users, trigger_count, avg_response_time_ms, grid_area) " +
                                 "VALUES (?, ?, 1, 1, ?, ?, ?)";
                
                jdbcTemplate.update(insertSql, keywordId, today, triggered ? 1 : 0, responseTimeMs, gridArea);
            } else {
                // 更新现有统计记录
                String updateSql = "UPDATE keyword_usage_stats SET " +
                                 "hit_count = hit_count + 1, " +
                                 "trigger_count = trigger_count + ?, " +
                                 "avg_response_time_ms = (avg_response_time_ms * hit_count + ?) / (hit_count + 1) " +
                                 "WHERE keyword_id = ? AND stat_date = ? AND grid_area = ?";
                
                jdbcTemplate.update(updateSql, triggered ? 1 : 0, responseTimeMs, keywordId, today, gridArea);
                
                // 更新唯一用户数（简化处理，实际应该维护用户集合）
                if (userId != null) {
                    String userUpdateSql = "UPDATE keyword_usage_stats SET unique_users = " +
                                         "(SELECT COUNT(DISTINCT user_id) FROM keyword_learning_records " +
                                         "WHERE keyword = (SELECT keyword FROM keyword_configs WHERE id = ?) " +
                                         "AND DATE(detection_time) = ?) " +
                                         "WHERE keyword_id = ? AND stat_date = ? AND grid_area = ?";
                    
                    jdbcTemplate.update(userUpdateSql, keywordId, today, keywordId, today, gridArea);
                }
            }
            
            // 计算成功率
            updateSuccessRate(keywordId, today, gridArea);
            
        } catch (Exception e) {
            log.error("记录关键词使用统计失败", e);
        }
    }

    /**
     * 更新成功率
     */
    private void updateSuccessRate(Long keywordId, LocalDate statDate, String gridArea) {
        try {
            String sql = "UPDATE keyword_usage_stats SET success_rate = " +
                        "CASE WHEN hit_count > 0 THEN trigger_count / hit_count ELSE 0 END " +
                        "WHERE keyword_id = ? AND stat_date = ? AND grid_area = ?";
            
            jdbcTemplate.update(sql, keywordId, statDate, gridArea);
        } catch (Exception e) {
            log.error("更新成功率失败", e);
        }
    }

    /**
     * 获取关键词分析报告
     */
    public Map<String, Object> getKeywordAnalysisReport(String gridArea, int days) {
        Map<String, Object> report = new HashMap<>();
        
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);
            
            // 基础统计
            report.put("basicStats", getBasicStats(gridArea, startDate, endDate));
            
            // 热门关键词
            report.put("hotKeywords", getHotKeywords(gridArea, startDate, endDate, 10));
            
            // 趋势分析
            report.put("trendAnalysis", getTrendAnalysis(gridArea, startDate, endDate));
            
            // 效果分析
            report.put("effectivenessAnalysis", getEffectivenessAnalysis(gridArea, startDate, endDate));
            
            // 地域分布
            report.put("geographicDistribution", getGeographicDistribution(startDate, endDate));
            
            // 时间分布
            report.put("timeDistribution", getTimeDistribution(gridArea, startDate, endDate));
            
        } catch (Exception e) {
            log.error("获取关键词分析报告失败", e);
            report.put("error", e.getMessage());
        }
        
        return report;
    }

    /**
     * 获取基础统计
     */
    private Map<String, Object> getBasicStats(String gridArea, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        String whereClause = "WHERE stat_date BETWEEN ? AND ?";
        List<Object> params = Arrays.asList(startDate, endDate);
        
        if (gridArea != null && !gridArea.isEmpty()) {
            whereClause += " AND grid_area = ?";
            params = new ArrayList<>(params);
            params.add(gridArea);
        }
        
        // 总命中次数
        String totalHitsSql = "SELECT SUM(hit_count) FROM keyword_usage_stats " + whereClause;
        Integer totalHits = jdbcTemplate.queryForObject(totalHitsSql, Integer.class, params.toArray());
        stats.put("totalHits", totalHits != null ? totalHits : 0);
        
        // 总触发次数
        String totalTriggersSql = "SELECT SUM(trigger_count) FROM keyword_usage_stats " + whereClause;
        Integer totalTriggers = jdbcTemplate.queryForObject(totalTriggersSql, Integer.class, params.toArray());
        stats.put("totalTriggers", totalTriggers != null ? totalTriggers : 0);
        
        // 总唯一用户数
        String totalUsersSql = "SELECT SUM(unique_users) FROM keyword_usage_stats " + whereClause;
        Integer totalUsers = jdbcTemplate.queryForObject(totalUsersSql, Integer.class, params.toArray());
        stats.put("totalUsers", totalUsers != null ? totalUsers : 0);
        
        // 平均成功率
        String avgSuccessRateSql = "SELECT AVG(success_rate) FROM keyword_usage_stats " + whereClause;
        BigDecimal avgSuccessRate = jdbcTemplate.queryForObject(avgSuccessRateSql, BigDecimal.class, params.toArray());
        stats.put("avgSuccessRate", avgSuccessRate != null ? 
                 avgSuccessRate.setScale(4, RoundingMode.HALF_UP).doubleValue() : 0.0);
        
        // 活跃关键词数
        String activeKeywordsSql = "SELECT COUNT(DISTINCT keyword_id) FROM keyword_usage_stats " + whereClause;
        Integer activeKeywords = jdbcTemplate.queryForObject(activeKeywordsSql, Integer.class, params.toArray());
        stats.put("activeKeywords", activeKeywords != null ? activeKeywords : 0);
        
        return stats;
    }

    /**
     * 获取热门关键词
     */
    private List<Map<String, Object>> getHotKeywords(String gridArea, LocalDate startDate, 
                                                    LocalDate endDate, int limit) {
        String sql = "SELECT kc.keyword, SUM(kus.hit_count) as total_hits, " +
                    "SUM(kus.trigger_count) as total_triggers, " +
                    "AVG(kus.success_rate) as avg_success_rate, " +
                    "SUM(kus.unique_users) as total_users " +
                    "FROM keyword_usage_stats kus " +
                    "JOIN keyword_configs kc ON kus.keyword_id = kc.id " +
                    "WHERE kus.stat_date BETWEEN ? AND ?";
        
        List<Object> params = Arrays.asList(startDate, endDate);
        
        if (gridArea != null && !gridArea.isEmpty()) {
            sql += " AND kus.grid_area = ?";
            params = new ArrayList<>(params);
            params.add(gridArea);
        }
        
        sql += " GROUP BY kc.keyword ORDER BY total_hits DESC LIMIT ?";
        params = new ArrayList<>(params);
        params.add(limit);
        
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    /**
     * 获取趋势分析
     */
    private Map<String, Object> getTrendAnalysis(String gridArea, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> trend = new HashMap<>();
        
        // 每日趋势
        String dailyTrendSql = "SELECT stat_date, SUM(hit_count) as daily_hits, " +
                              "SUM(trigger_count) as daily_triggers " +
                              "FROM keyword_usage_stats " +
                              "WHERE stat_date BETWEEN ? AND ?";
        
        List<Object> params = Arrays.asList(startDate, endDate);
        
        if (gridArea != null && !gridArea.isEmpty()) {
            dailyTrendSql += " AND grid_area = ?";
            params = new ArrayList<>(params);
            params.add(gridArea);
        }
        
        dailyTrendSql += " GROUP BY stat_date ORDER BY stat_date";
        
        List<Map<String, Object>> dailyTrend = jdbcTemplate.queryForList(dailyTrendSql, params.toArray());
        trend.put("dailyTrend", dailyTrend);
        
        // 增长率计算
        if (dailyTrend.size() >= 2) {
            Map<String, Object> firstDay = dailyTrend.get(0);
            Map<String, Object> lastDay = dailyTrend.get(dailyTrend.size() - 1);
            
            int firstDayHits = ((Number) firstDay.get("daily_hits")).intValue();
            int lastDayHits = ((Number) lastDay.get("daily_hits")).intValue();
            
            double growthRate = firstDayHits > 0 ? 
                              ((double) (lastDayHits - firstDayHits) / firstDayHits) * 100 : 0;
            
            trend.put("growthRate", Math.round(growthRate * 100.0) / 100.0);
        } else {
            trend.put("growthRate", 0.0);
        }
        
        return trend;
    }

    /**
     * 获取效果分析
     */
    private Map<String, Object> getEffectivenessAnalysis(String gridArea, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> effectiveness = new HashMap<>();
        
        // 高效关键词（成功率 > 80%）
        String highEffectiveSql = "SELECT kc.keyword, AVG(kus.success_rate) as avg_success_rate " +
                                "FROM keyword_usage_stats kus " +
                                "JOIN keyword_configs kc ON kus.keyword_id = kc.id " +
                                "WHERE kus.stat_date BETWEEN ? AND ?";
        
        List<Object> params = Arrays.asList(startDate, endDate);
        
        if (gridArea != null && !gridArea.isEmpty()) {
            highEffectiveSql += " AND kus.grid_area = ?";
            params = new ArrayList<>(params);
            params.add(gridArea);
        }
        
        highEffectiveSql += " GROUP BY kc.keyword HAVING avg_success_rate > 0.8 ORDER BY avg_success_rate DESC";
        
        List<Map<String, Object>> highEffectiveKeywords = jdbcTemplate.queryForList(highEffectiveSql, params.toArray());
        effectiveness.put("highEffectiveKeywords", highEffectiveKeywords);
        
        // 低效关键词（成功率 < 30%）
        String lowEffectiveSql = highEffectiveSql.replace("> 0.8", "< 0.3").replace("DESC", "ASC");
        List<Map<String, Object>> lowEffectiveKeywords = jdbcTemplate.queryForList(lowEffectiveSql, params.toArray());
        effectiveness.put("lowEffectiveKeywords", lowEffectiveKeywords);
        
        // 响应时间分析
        String responseTimeSql = "SELECT AVG(avg_response_time_ms) as avg_response_time, " +
                               "MIN(avg_response_time_ms) as min_response_time, " +
                               "MAX(avg_response_time_ms) as max_response_time " +
                               "FROM keyword_usage_stats " +
                               "WHERE stat_date BETWEEN ? AND ?";
        
        if (gridArea != null && !gridArea.isEmpty()) {
            responseTimeSql += " AND grid_area = ?";
        }
        
        Map<String, Object> responseTimeStats = jdbcTemplate.queryForMap(responseTimeSql, params.toArray());
        effectiveness.put("responseTimeStats", responseTimeStats);
        
        return effectiveness;
    }

    /**
     * 获取地域分布
     */
    private List<Map<String, Object>> getGeographicDistribution(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT grid_area, SUM(hit_count) as total_hits, " +
                    "SUM(trigger_count) as total_triggers, " +
                    "COUNT(DISTINCT keyword_id) as unique_keywords " +
                    "FROM keyword_usage_stats " +
                    "WHERE stat_date BETWEEN ? AND ? " +
                    "GROUP BY grid_area " +
                    "ORDER BY total_hits DESC";
        
        return jdbcTemplate.queryForList(sql, startDate, endDate);
    }

    /**
     * 获取时间分布
     */
    private Map<String, Object> getTimeDistribution(String gridArea, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> timeDistribution = new HashMap<>();
        
        // 按小时分布（从学习记录中获取）
        String hourlySql = "SELECT HOUR(detection_time) as hour, COUNT(*) as count " +
                          "FROM keyword_learning_records " +
                          "WHERE DATE(detection_time) BETWEEN ? AND ?";
        
        List<Object> params = Arrays.asList(startDate, endDate);
        
        if (gridArea != null && !gridArea.isEmpty()) {
            hourlySql += " AND grid_area = ?";
            params = new ArrayList<>(params);
            params.add(gridArea);
        }
        
        hourlySql += " GROUP BY HOUR(detection_time) ORDER BY hour";
        
        List<Map<String, Object>> hourlyDistribution = jdbcTemplate.queryForList(hourlySql, params.toArray());
        timeDistribution.put("hourlyDistribution", hourlyDistribution);
        
        // 按星期分布
        String weeklySql = "SELECT DAYOFWEEK(stat_date) as day_of_week, SUM(hit_count) as total_hits " +
                          "FROM keyword_usage_stats " +
                          "WHERE stat_date BETWEEN ? AND ?";
        
        params = Arrays.asList(startDate, endDate);
        
        if (gridArea != null && !gridArea.isEmpty()) {
            weeklySql += " AND grid_area = ?";
            params = new ArrayList<>(params);
            params.add(gridArea);
        }
        
        weeklySql += " GROUP BY DAYOFWEEK(stat_date) ORDER BY day_of_week";
        
        List<Map<String, Object>> weeklyDistribution = jdbcTemplate.queryForList(weeklySql, params.toArray());
        timeDistribution.put("weeklyDistribution", weeklyDistribution);
        
        return timeDistribution;
    }

    /**
     * 发现新兴关键词
     */
    public List<Map<String, Object>> discoverEmergingKeywords(String gridArea, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);
        LocalDate midDate = endDate.minusDays(days / 2);
        
        String sql = "SELECT kc.keyword, " +
                    "SUM(CASE WHEN kus.stat_date >= ? THEN kus.hit_count ELSE 0 END) as recent_hits, " +
                    "SUM(CASE WHEN kus.stat_date < ? THEN kus.hit_count ELSE 0 END) as early_hits " +
                    "FROM keyword_usage_stats kus " +
                    "JOIN keyword_configs kc ON kus.keyword_id = kc.id " +
                    "WHERE kus.stat_date BETWEEN ? AND ?";
        
        List<Object> params = Arrays.asList(midDate, midDate, startDate, endDate);
        
        if (gridArea != null && !gridArea.isEmpty()) {
            sql += " AND kus.grid_area = ?";
            params = new ArrayList<>(params);
            params.add(gridArea);
        }
        
        sql += " GROUP BY kc.keyword " +
              "HAVING recent_hits > early_hits * 2 AND recent_hits > 5 " +
              "ORDER BY (recent_hits - early_hits) DESC LIMIT 10";
        
        return jdbcTemplate.queryForList(sql, params.toArray());
    }

    /**
     * 预测关键词趋势
     */
    public Map<String, Object> predictKeywordTrend(Long keywordId, int futureDays) {
        Map<String, Object> prediction = new HashMap<>();
        
        try {
            // 获取历史数据
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(30); // 使用30天历史数据
            
            String sql = "SELECT stat_date, hit_count FROM keyword_usage_stats " +
                        "WHERE keyword_id = ? AND stat_date BETWEEN ? AND ? " +
                        "ORDER BY stat_date";
            
            List<Map<String, Object>> historicalData = jdbcTemplate.queryForList(sql, keywordId, startDate, endDate);
            
            if (historicalData.size() < 7) {
                prediction.put("error", "历史数据不足，无法进行预测");
                return prediction;
            }
            
            // 简单线性回归预测
            List<Double> values = historicalData.stream()
                .map(data -> ((Number) data.get("hit_count")).doubleValue())
                .collect(Collectors.toList());
            
            double[] trend = calculateLinearTrend(values);
            double slope = trend[0];
            double intercept = trend[1];
            
            // 预测未来值
            List<Map<String, Object>> predictions = new ArrayList<>();
            for (int i = 1; i <= futureDays; i++) {
                Map<String, Object> predictionPoint = new HashMap<>();
                LocalDate futureDate = endDate.plusDays(i);
                double predictedValue = Math.max(0, slope * (historicalData.size() + i) + intercept);
                
                predictionPoint.put("date", futureDate);
                predictionPoint.put("predictedHits", Math.round(predictedValue));
                predictions.add(predictionPoint);
            }
            
            prediction.put("predictions", predictions);
            prediction.put("trend", slope > 0 ? "上升" : slope < 0 ? "下降" : "平稳");
            prediction.put("confidence", calculatePredictionConfidence(values));
            
        } catch (Exception e) {
            log.error("预测关键词趋势失败", e);
            prediction.put("error", e.getMessage());
        }
        
        return prediction;
    }

    /**
     * 计算线性趋势
     */
    private double[] calculateLinearTrend(List<Double> values) {
        int n = values.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        
        for (int i = 0; i < n; i++) {
            double x = i + 1;
            double y = values.get(i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumXX += x * x;
        }
        
        double slope = (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX);
        double intercept = (sumY - slope * sumX) / n;
        
        return new double[]{slope, intercept};
    }

    /**
     * 计算预测置信度
     */
    private double calculatePredictionConfidence(List<Double> values) {
        if (values.size() < 3) return 0.0;
        
        // 计算变异系数作为置信度的反向指标
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average().orElse(0.0);
        
        double stdDev = Math.sqrt(variance);
        double coefficientOfVariation = mean > 0 ? stdDev / mean : 1.0;
        
        // 置信度 = 1 - 变异系数（限制在0-1之间）
        return Math.max(0.0, Math.min(1.0, 1.0 - coefficientOfVariation));
    }

    /**
     * 生成关键词优化建议
     */
    public List<Map<String, Object>> generateOptimizationSuggestions(String gridArea) {
        List<Map<String, Object>> suggestions = new ArrayList<>();
        
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(7);
            
            // 建议1：删除低效关键词
            String lowEffectiveSql = "SELECT kc.keyword, AVG(kus.success_rate) as avg_success_rate, " +
                                   "SUM(kus.hit_count) as total_hits " +
                                   "FROM keyword_usage_stats kus " +
                                   "JOIN keyword_configs kc ON kus.keyword_id = kc.id " +
                                   "WHERE kus.stat_date BETWEEN ? AND ?";
            
            List<Object> params = Arrays.asList(startDate, endDate);
            
            if (gridArea != null && !gridArea.isEmpty()) {
                lowEffectiveSql += " AND kus.grid_area = ?";
                params = new ArrayList<>(params);
                params.add(gridArea);
            }
            
            lowEffectiveSql += " GROUP BY kc.keyword " +
                             "HAVING avg_success_rate < 0.2 AND total_hits > 10";
            
            List<Map<String, Object>> lowEffectiveKeywords = jdbcTemplate.queryForList(lowEffectiveSql, params.toArray());
            
            if (!lowEffectiveKeywords.isEmpty()) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("type", "删除低效关键词");
                suggestion.put("priority", "高");
                suggestion.put("description", "以下关键词成功率较低，建议考虑删除或优化");
                suggestion.put("keywords", lowEffectiveKeywords);
                suggestions.add(suggestion);
            }
            
            // 建议2：推广高效关键词
            String highEffectiveSql = lowEffectiveSql.replace("< 0.2", "> 0.8");
            List<Map<String, Object>> highEffectiveKeywords = jdbcTemplate.queryForList(highEffectiveSql, params.toArray());
            
            if (!highEffectiveKeywords.isEmpty()) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("type", "推广高效关键词");
                suggestion.put("priority", "中");
                suggestion.put("description", "以下关键词效果良好，建议扩大使用范围");
                suggestion.put("keywords", highEffectiveKeywords);
                suggestions.add(suggestion);
            }
            
            // 建议3：关注新兴关键词
            List<Map<String, Object>> emergingKeywords = discoverEmergingKeywords(gridArea, 14);
            
            if (!emergingKeywords.isEmpty()) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("type", "关注新兴关键词");
                suggestion.put("priority", "中");
                suggestion.put("description", "以下关键词使用频率快速增长，建议重点关注");
                suggestion.put("keywords", emergingKeywords);
                suggestions.add(suggestion);
            }
            
        } catch (Exception e) {
            log.error("生成优化建议失败", e);
        }
        
        return suggestions;
    }
}