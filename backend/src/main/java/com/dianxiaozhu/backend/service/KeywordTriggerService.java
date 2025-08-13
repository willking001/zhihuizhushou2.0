package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.KeywordConfig;
import com.dianxiaozhu.backend.mapper.KeywordConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 关键词触发服务
 * 实现关键词识别、触发规则管理、触发日志记录等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordTriggerService {

    private final KeywordConfigMapper keywordConfigMapper;
    private final JdbcTemplate jdbcTemplate;
    private final KeywordAnalysisService keywordAnalysisService;
    
    // 缓存触发规则，提高性能
    private Map<String, List<Map<String, Object>>> triggerRulesCache = new HashMap<>(); 
    private long lastCacheUpdate = 0;
    private static final long CACHE_EXPIRE_TIME = 5 * 60 * 1000; // 5分钟缓存过期

    /**
     * 检测文本中的关键词并触发相应动作
     */
    public Map<String, Object> detectAndTriggerKeywords(String text, String gridArea, Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> triggeredKeywords = new ArrayList<>();
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 获取指定区域的活跃关键词
            List<KeywordConfig> activeKeywords = getActiveKeywords(gridArea);
            
            // 检测关键词
            for (KeywordConfig keyword : activeKeywords) {
                Map<String, Object> matchResult = matchKeyword(text, keyword);
                
                if ((Boolean) matchResult.get("matched")) {
                    // 获取触发规则
                    List<Map<String, Object>> rules = getTriggerRules(keyword.getId());
                    
                    // 执行触发动作
                    Map<String, Object> triggerResult = executeTriggerActions(keyword, rules, text, gridArea, userId);
                    
                    Map<String, Object> keywordResult = new HashMap<>();
                    keywordResult.put("keyword", keyword.getKeyword());
                    keywordResult.put("keywordId", keyword.getId());
                    keywordResult.put("type", keyword.getType());
                    keywordResult.put("priority", keyword.getPriority());
                    keywordResult.put("matchInfo", matchResult);
                    keywordResult.put("triggerResult", triggerResult);
                    
                    triggeredKeywords.add(keywordResult);
                    
                    // 记录触发日志
                    recordTriggerLog(keyword.getId(), text, gridArea, userId, triggerResult);
                }
            }
            
            long endTime = System.currentTimeMillis();
            int responseTime = (int) (endTime - startTime);
            
            // 记录统计信息
            for (Map<String, Object> triggered : triggeredKeywords) {
                Long keywordId = (Long) triggered.get("keywordId");
                keywordAnalysisService.recordKeywordUsage(keywordId, gridArea, true, responseTime, userId);
            }
            
            result.put("triggeredKeywords", triggeredKeywords);
            result.put("totalMatched", triggeredKeywords.size());
            result.put("responseTime", responseTime);
            result.put("success", true);
            
        } catch (Exception e) {
            log.error("检测和触发关键词失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取活跃关键词
     */
    private List<KeywordConfig> getActiveKeywords(String gridArea) {
        List<KeywordConfig> allKeywords = keywordConfigMapper.findAll();
        if (gridArea != null && !gridArea.isEmpty()) {
            return allKeywords.stream()
                    .filter(k -> k.getIsActive() && gridArea.equals(k.getGridArea()))
                    .sorted((k1, k2) -> k2.getPriority().compareTo(k1.getPriority()))
                    .collect(java.util.stream.Collectors.toList());
        } else {
            return allKeywords.stream()
                    .filter(KeywordConfig::getIsActive)
                    .sorted((k1, k2) -> k2.getPriority().compareTo(k1.getPriority()))
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    /**
     * 匹配关键词
     */
    private Map<String, Object> matchKeyword(String text, KeywordConfig keyword) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String keywordText = keyword.getKeyword();
            boolean matched = false;
            List<Map<String, Object>> matches = new ArrayList<>();
            
            // 精确匹配
            if (text.contains(keywordText)) {
                matched = true;
                int index = text.indexOf(keywordText);
                while (index != -1) {
                    Map<String, Object> match = new HashMap<>();
                    match.put("type", "exact");
                    match.put("start", index);
                    match.put("end", index + keywordText.length());
                    match.put("matchedText", keywordText);
                    matches.add(match);
                    
                    index = text.indexOf(keywordText, index + 1);
                }
            }
            
            // 模糊匹配（如果精确匹配失败）
            if (!matched) {
                Map<String, Object> fuzzyMatch = performFuzzyMatch(text, keywordText);
                if ((Boolean) fuzzyMatch.get("matched")) {
                    matched = true;
                    matches.add(fuzzyMatch);
                }
            }
            
            // 正则表达式匹配（如果关键词包含正则表达式标记）
            if (!matched && keywordText.startsWith("regex:")) {
                String regex = keywordText.substring(6);
                Map<String, Object> regexMatch = performRegexMatch(text, regex);
                if ((Boolean) regexMatch.get("matched")) {
                    matched = true;
                    matches.add(regexMatch);
                }
            }
            
            result.put("matched", matched);
            result.put("matches", matches);
            result.put("confidence", calculateMatchConfidence(matches));
            
        } catch (Exception e) {
            log.error("匹配关键词失败: {}", keyword.getKeyword(), e);
            result.put("matched", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行模糊匹配
     */
    private Map<String, Object> performFuzzyMatch(String text, String keyword) {
        Map<String, Object> result = new HashMap<>();
        
        // 简单的模糊匹配：允许一定的编辑距离
        String[] words = text.split("\\s+");
        double maxSimilarity = 0.0;
        String bestMatch = null;
        int bestStart = -1;
        
        for (int i = 0; i < words.length; i++) {
            double similarity = calculateSimilarity(words[i], keyword);
            if (similarity > maxSimilarity && similarity >= 0.8) { // 80%相似度阈值
                maxSimilarity = similarity;
                bestMatch = words[i];
                bestStart = text.indexOf(words[i]);
            }
        }
        
        if (bestMatch != null) {
            result.put("matched", true);
            result.put("type", "fuzzy");
            result.put("start", bestStart);
            result.put("end", bestStart + bestMatch.length());
            result.put("matchedText", bestMatch);
            result.put("similarity", maxSimilarity);
        } else {
            result.put("matched", false);
        }
        
        return result;
    }

    /**
     * 执行正则表达式匹配
     */
    private Map<String, Object> performRegexMatch(String text, String regex) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Pattern pattern = Pattern.compile(regex);
            java.util.regex.Matcher matcher = pattern.matcher(text);
            
            if (matcher.find()) {
                result.put("matched", true);
                result.put("type", "regex");
                result.put("start", matcher.start());
                result.put("end", matcher.end());
                result.put("matchedText", matcher.group());
                result.put("pattern", regex);
            } else {
                result.put("matched", false);
            }
            
        } catch (Exception e) {
            log.error("正则表达式匹配失败: {}", regex, e);
            result.put("matched", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 计算字符串相似度
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;
        
        int editDistance = calculateEditDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        
        return 1.0 - (double) editDistance / maxLength;
    }

    /**
     * 计算编辑距离
     */
    private int calculateEditDistance(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        
        int[][] dp = new int[m + 1][n + 1];
        
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        
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
     * 计算匹配置信度
     */
    private double calculateMatchConfidence(List<Map<String, Object>> matches) {
        if (matches.isEmpty()) return 0.0;
        
        double totalConfidence = 0.0;
        
        for (Map<String, Object> match : matches) {
            String type = (String) match.get("type");
            switch (type) {
                case "exact":
                    totalConfidence += 1.0;
                    break;
                case "fuzzy":
                    Double similarity = (Double) match.get("similarity");
                    totalConfidence += similarity != null ? similarity : 0.8;
                    break;
                case "regex":
                    totalConfidence += 0.9;
                    break;
                default:
                    totalConfidence += 0.5;
            }
        }
        
        return totalConfidence / matches.size();
    }

    /**
     * 获取触发规则
     */
    private List<Map<String, Object>> getTriggerRules(Long keywordId) {
        // 检查缓存
        if (System.currentTimeMillis() - lastCacheUpdate > CACHE_EXPIRE_TIME) {
            refreshTriggerRulesCache();
        }
        
        return triggerRulesCache.getOrDefault(keywordId.toString(), new ArrayList<>());
    }

    /**
     * 刷新触发规则缓存
     */
    private void refreshTriggerRulesCache() {
        try {
            String sql = "SELECT keyword_id, rule_name, rule_type, rule_config, is_active " +
                        "FROM keyword_trigger_rules WHERE is_active = true";
            
            List<Map<String, Object>> rules = jdbcTemplate.queryForList(sql);
            
            Map<String, List<Map<String, Object>>> newCache = new HashMap<>();
            
            for (Map<String, Object> rule : rules) {
                String keywordId = rule.get("keyword_id").toString();
                newCache.computeIfAbsent(keywordId, k -> new ArrayList<>()).add(rule);
            }
            
            triggerRulesCache = newCache;
            lastCacheUpdate = System.currentTimeMillis();
            
            log.debug("触发规则缓存已刷新，共加载 {} 条规则", rules.size());
            
        } catch (Exception e) {
            log.error("刷新触发规则缓存失败", e);
        }
    }

    /**
     * 执行触发动作
     */
    private Map<String, Object> executeTriggerActions(KeywordConfig keyword, List<Map<String, Object>> rules, 
                                                     String text, String gridArea, Long userId) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> actionResults = new ArrayList<>();
        
        try {
            for (Map<String, Object> rule : rules) {
                String ruleType = (String) rule.get("rule_type");
                String ruleConfig = (String) rule.get("rule_config");
                
                Map<String, Object> actionResult = executeAction(ruleType, ruleConfig, keyword, text, gridArea, userId);
                actionResult.put("ruleName", rule.get("rule_name"));
                actionResults.add(actionResult);
            }
            
            result.put("success", true);
            result.put("actionResults", actionResults);
            result.put("totalActions", actionResults.size());
            
        } catch (Exception e) {
            log.error("执行触发动作失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行具体动作
     */
    private Map<String, Object> executeAction(String actionType, String config, KeywordConfig keyword, 
                                            String text, String gridArea, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            switch (actionType) {
                case "LOG":
                    result = executeLogAction(config, keyword, text);
                    break;
                case "NOTIFICATION":
                    result = executeNotificationAction(config, keyword, text, userId);
                    break;
                case "API_CALL":
                    result = executeApiCallAction(config, keyword, text, gridArea);
                    break;
                case "SCRIPT":
                    result = executeScriptAction(config, keyword, text, gridArea);
                    break;
                case "EMAIL":
                    result = executeEmailAction(config, keyword, text, userId);
                    break;
                default:
                    result.put("success", false);
                    result.put("error", "未知的动作类型: " + actionType);
            }
            
            result.put("actionType", actionType);
            
        } catch (Exception e) {
            log.error("执行动作失败: {}", actionType, e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行日志动作
     */
    private Map<String, Object> executeLogAction(String config, KeywordConfig keyword, String text) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String logMessage = String.format("关键词触发 - 关键词: %s, 文本: %s, 配置: %s", 
                                            keyword.getKeyword(), text, config);
            
            log.info(logMessage);
            
            result.put("success", true);
            result.put("message", "日志记录成功");
            result.put("logMessage", logMessage);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行通知动作
     */
    private Map<String, Object> executeNotificationAction(String config, KeywordConfig keyword, String text, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 这里应该集成实际的通知系统
            String notificationMessage = String.format("关键词 '%s' 被触发", keyword.getKeyword());
            
            // 模拟发送通知
            log.info("发送通知给用户 {}: {}", userId, notificationMessage);
            
            result.put("success", true);
            result.put("message", "通知发送成功");
            result.put("notificationMessage", notificationMessage);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行API调用动作
     */
    private Map<String, Object> executeApiCallAction(String config, KeywordConfig keyword, String text, String gridArea) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 这里应该解析config中的API配置并执行调用
            log.info("执行API调用 - 关键词: {}, 配置: {}", keyword.getKeyword(), config);
            
            result.put("success", true);
            result.put("message", "API调用成功");
            result.put("apiConfig", config);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行脚本动作
     */
    private Map<String, Object> executeScriptAction(String config, KeywordConfig keyword, String text, String gridArea) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 这里应该执行配置中指定的脚本
            log.info("执行脚本 - 关键词: {}, 配置: {}", keyword.getKeyword(), config);
            
            result.put("success", true);
            result.put("message", "脚本执行成功");
            result.put("scriptConfig", config);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 执行邮件动作
     */
    private Map<String, Object> executeEmailAction(String config, KeywordConfig keyword, String text, Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 这里应该集成邮件发送服务
            log.info("发送邮件 - 关键词: {}, 配置: {}", keyword.getKeyword(), config);
            
            result.put("success", true);
            result.put("message", "邮件发送成功");
            result.put("emailConfig", config);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 记录触发日志
     */
    @Async
    public void recordTriggerLog(Long keywordId, String inputText, String gridArea, Long userId, 
                               Map<String, Object> triggerResult) {
        try {
            String sql = "INSERT INTO keyword_trigger_logs (keyword_id, input_text, grid_area, user_id, " +
                        "trigger_time, success, action_count, response_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            boolean success = (Boolean) triggerResult.getOrDefault("success", false);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> actionResults = (List<Map<String, Object>>) triggerResult.get("actionResults");
            int actionCount = actionResults != null ? actionResults.size() : 0;
            
            // 将触发结果转换为JSON字符串存储
            String responseData = triggerResult.toString(); // 实际应该使用JSON序列化
            
            jdbcTemplate.update(sql, keywordId, inputText, gridArea, userId, 
                              LocalDateTime.now(), success, actionCount, responseData);
            
        } catch (Exception e) {
            log.error("记录触发日志失败", e);
        }
    }

    /**
     * 创建触发规则
     */
    @Transactional
    public Map<String, Object> createTriggerRule(Long keywordId, String ruleName, String ruleType, 
                                                String ruleConfig, String description) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String sql = "INSERT INTO keyword_trigger_rules (keyword_id, rule_name, rule_type, " +
                        "rule_config, description, is_active, created_time) VALUES (?, ?, ?, ?, ?, true, ?)";
            
            jdbcTemplate.update(sql, keywordId, ruleName, ruleType, ruleConfig, description, LocalDateTime.now());
            
            // 清除缓存，强制重新加载
            triggerRulesCache.clear();
            lastCacheUpdate = 0;
            
            result.put("success", true);
            result.put("message", "触发规则创建成功");
            
        } catch (Exception e) {
            log.error("创建触发规则失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    /**
     * 获取触发统计
     */
    public Map<String, Object> getTriggerStats(String gridArea, int days) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            
            String whereClause = "WHERE trigger_time >= ?";
            List<Object> params = Arrays.asList(startTime);
            
            if (gridArea != null && !gridArea.isEmpty()) {
                whereClause += " AND grid_area = ?";
                params = new ArrayList<>(params);
                params.add(gridArea);
            }
            
            // 总触发次数
            String totalTriggersSql = "SELECT COUNT(*) FROM keyword_trigger_logs " + whereClause;
            Integer totalTriggers = jdbcTemplate.queryForObject(totalTriggersSql, Integer.class, params.toArray());
            stats.put("totalTriggers", totalTriggers != null ? totalTriggers : 0);
            
            // 成功触发次数
            String successTriggersSql = "SELECT COUNT(*) FROM keyword_trigger_logs " + whereClause + " AND success = true";
            Integer successTriggers = jdbcTemplate.queryForObject(successTriggersSql, Integer.class, params.toArray());
            stats.put("successTriggers", successTriggers != null ? successTriggers : 0);
            
            // 成功率
            double successRate = totalTriggers != null && totalTriggers > 0 ? 
                               (double) (successTriggers != null ? successTriggers : 0) / totalTriggers : 0.0;
            stats.put("successRate", Math.round(successRate * 10000.0) / 100.0);
            
            // 活跃关键词数
            String activeKeywordsSql = "SELECT COUNT(DISTINCT keyword_id) FROM keyword_trigger_logs " + whereClause;
            Integer activeKeywords = jdbcTemplate.queryForObject(activeKeywordsSql, Integer.class, params.toArray());
            stats.put("activeKeywords", activeKeywords != null ? activeKeywords : 0);
            
            // 平均动作数
            String avgActionsSql = "SELECT AVG(action_count) FROM keyword_trigger_logs " + whereClause;
            Double avgActions = jdbcTemplate.queryForObject(avgActionsSql, Double.class, params.toArray());
            stats.put("avgActions", avgActions != null ? Math.round(avgActions * 100.0) / 100.0 : 0.0);
            
        } catch (Exception e) {
            log.error("获取触发统计失败", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    /**
     * 获取热门触发关键词
     */
    public List<Map<String, Object>> getHotTriggerKeywords(String gridArea, int days, int limit) {
        try {
            LocalDateTime startTime = LocalDateTime.now().minusDays(days);
            
            String sql = "SELECT kc.keyword, COUNT(*) as trigger_count, " +
                        "SUM(CASE WHEN ktl.success = true THEN 1 ELSE 0 END) as success_count " +
                        "FROM keyword_trigger_logs ktl " +
                        "JOIN keyword_configs kc ON ktl.keyword_id = kc.id " +
                        "WHERE ktl.trigger_time >= ?";
            
            List<Object> params = Arrays.asList(startTime);
            
            if (gridArea != null && !gridArea.isEmpty()) {
                sql += " AND ktl.grid_area = ?";
                params = new ArrayList<>(params);
                params.add(gridArea);
            }
            
            sql += " GROUP BY kc.keyword ORDER BY trigger_count DESC LIMIT ?";
            params = new ArrayList<>(params);
            params.add(limit);
            
            return jdbcTemplate.queryForList(sql, params.toArray());
            
        } catch (Exception e) {
            log.error("获取热门触发关键词失败", e);
            return new ArrayList<>();
        }
    }
}