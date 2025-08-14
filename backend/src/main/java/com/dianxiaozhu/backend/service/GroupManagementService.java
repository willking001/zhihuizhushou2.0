package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.GroupDailyStatistics;
import com.dianxiaozhu.backend.entity.GroupManagementStatus;
import com.dianxiaozhu.backend.entity.User;
import com.dianxiaozhu.backend.repository.GroupDailyStatisticsRepository;
import com.dianxiaozhu.backend.repository.GroupManagementStatusRepository;
import com.dianxiaozhu.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 群组管理服务类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GroupManagementService {

    private final GroupManagementStatusRepository groupStatusRepository;
    private final GroupDailyStatisticsRepository dailyStatsRepository;
    private final UserRepository userRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 创建群组
     */
    @Transactional
    public GroupManagementStatus createGroup(String chatRoom, String groupName, 
                                           String groupDescription, String groupCategory, 
                                           String gridOfficerId, String operator) {
        // 检查群组是否已存在
        if (groupStatusRepository.findByChatRoom(chatRoom).isPresent()) {
            throw new RuntimeException("群组已存在: " + chatRoom);
        }
        
        GroupManagementStatus newGroup = new GroupManagementStatus();
        newGroup.setChatRoom(chatRoom);
        newGroup.setGroupName(groupName);
        newGroup.setGroupCategory(groupCategory);
        newGroup.setCurrentStatus(GroupManagementStatus.GroupStatus.AUTO);
        newGroup.setAutoReplyEnabled(true);
        newGroup.setAutoForwardEnabled(true);
        newGroup.setGridOfficerId(gridOfficerId);
        newGroup.setLastActivityTime(LocalDateTime.now());
        newGroup.setCreatedTime(LocalDateTime.now());
        newGroup.setUpdatedTime(LocalDateTime.now());
        
        GroupManagementStatus savedGroup = groupStatusRepository.save(newGroup);
        
        log.info("群组创建成功: 群组[{}] 名称[{}], 操作人[{}]", chatRoom, groupName, operator);
        
        return savedGroup;
    }

    /**
     * 获取或创建群组状态
     */
    @Transactional
    public GroupManagementStatus getOrCreateGroupStatus(String chatRoom, String groupName) {
        return groupStatusRepository.findByChatRoom(chatRoom)
                .orElseGet(() -> {
                    GroupManagementStatus status = new GroupManagementStatus();
                    status.setChatRoom(chatRoom);
                    status.setGroupName(groupName);
                    status.setCurrentStatus(GroupManagementStatus.GroupStatus.AUTO);
                    status.setAutoReplyEnabled(true);
                    status.setAutoForwardEnabled(true);
                    status.setLastActivityTime(LocalDateTime.now());
                    return groupStatusRepository.save(status);
                });
    }

    /**
     * 更新群组状态
     */
    @Transactional
    public GroupManagementStatus updateGroupStatus(String chatRoom, GroupManagementStatus.GroupStatus status, String operator, String reason) {
        GroupManagementStatus groupStatus = groupStatusRepository.findByChatRoom(chatRoom)
                .orElseThrow(() -> new RuntimeException("群组不存在: " + chatRoom));
        
        groupStatus.setCurrentStatus(status);
        if (status == GroupManagementStatus.GroupStatus.MANUAL) {
            groupStatus.setTakeoverTime(LocalDateTime.now());
            groupStatus.setTakeoverBy(operator);
            groupStatus.setTakeoverReason(reason);
            // 增加接管次数
            groupStatusRepository.incrementTakeoverCount(chatRoom, LocalDateTime.now(), operator, reason);
        }
        
        return groupStatusRepository.save(groupStatus);
    }

    /**
     * 记录消息活动
     */
    @Transactional
    public void recordMessageActivity(String chatRoom, boolean isAutoReply) {
        // 更新群组状态中的消息计数
        groupStatusRepository.incrementMessageCount(chatRoom, LocalDateTime.now());
        
        if (isAutoReply) {
            groupStatusRepository.incrementAutoReplyCount(chatRoom);
        }
        
        // 更新或创建日统计记录
        updateDailyStatistics(chatRoom, isAutoReply);
    }

    /**
     * 更新日统计数据
     */
    @Transactional
    public void updateDailyStatistics(String chatRoom, boolean isAutoReply) {
        LocalDate today = LocalDate.now();
        GroupDailyStatistics stats = dailyStatsRepository.findByChatRoomAndStatDate(chatRoom, today)
                .orElseGet(() -> {
                    GroupDailyStatistics newStats = new GroupDailyStatistics();
                    newStats.setChatRoom(chatRoom);
                    newStats.setStatDate(today);
                    return newStats;
                });
        
        stats.setMessageCount(stats.getMessageCount() + 1);
        if (isAutoReply) {
            stats.setAutoReplyCount(stats.getAutoReplyCount() + 1);
        }
        
        dailyStatsRepository.save(stats);
    }

    /**
     * 获取群组列表
     */
    public List<GroupManagementStatus> getGroupList(GroupManagementStatus.GroupStatus status, String groupName) {
        if (status != null && groupName != null && !groupName.trim().isEmpty()) {
            return groupStatusRepository.findByCurrentStatus(status).stream()
                    .filter(g -> g.getGroupName() != null && g.getGroupName().contains(groupName.trim()))
                    .collect(Collectors.toList());
        } else if (status != null) {
            return groupStatusRepository.findByCurrentStatus(status);
        } else if (groupName != null && !groupName.trim().isEmpty()) {
            return groupStatusRepository.findByGroupNameContaining(groupName.trim());
        } else {
            return groupStatusRepository.findAll();
        }
    }

    /**
     * 获取活跃群组列表
     */
    public List<GroupManagementStatus> getActiveGroups(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return groupStatusRepository.findActiveGroups(since);
    }

    /**
     * 获取群组状态统计
     */
    public Map<String, Long> getGroupStatusStatistics() {
        List<Object[]> results = groupStatusRepository.countByStatus();
        Map<String, Long> statistics = new HashMap<>();
        
        for (Object[] result : results) {
            GroupManagementStatus.GroupStatus status = (GroupManagementStatus.GroupStatus) result[0];
            Long count = (Long) result[1];
            statistics.put(status.name(), count);
        }
        
        return statistics;
    }

    /**
     * 获取群组详细信息
     */
    public GroupManagementStatus getGroupDetails(String chatRoom) {
        return groupStatusRepository.findByChatRoom(chatRoom)
                .orElseThrow(() -> new RuntimeException("群组不存在: " + chatRoom));
    }

    /**
     * 获取群组历史统计
     */
    public List<GroupDailyStatistics> getGroupHistoryStats(String chatRoom, LocalDate startDate, LocalDate endDate) {
        return dailyStatsRepository.findByChatRoomAndStatDateBetweenOrderByStatDateDesc(chatRoom, startDate, endDate);
    }

    /**
     * 获取总体统计数据
     */
    public Map<String, Object> getOverallStatistics(LocalDate startDate, LocalDate endDate) {
        Object[] result = dailyStatsRepository.getOverallStatistics(startDate, endDate);
        Map<String, Object> statistics = new HashMap<>();
        
        if (result != null && result.length >= 5) {
            statistics.put("totalMessages", result[0] != null ? result[0] : 0);
            statistics.put("totalAutoReplies", result[1] != null ? result[1] : 0);
            statistics.put("totalTakeovers", result[2] != null ? result[2] : 0);
            statistics.put("avgResponseTime", result[3] != null ? result[3] : BigDecimal.ZERO);
            statistics.put("avgSatisfaction", result[4] != null ? result[4] : BigDecimal.ZERO);
        } else {
            // 当没有数据或数组长度不足时，返回默认值
            statistics.put("totalMessages", 0);
            statistics.put("totalAutoReplies", 0);
            statistics.put("totalTakeovers", 0);
            statistics.put("avgResponseTime", BigDecimal.ZERO);
            statistics.put("avgSatisfaction", BigDecimal.ZERO);
        }
        
        return statistics;
    }

    /**
     * 获取群组排行榜
     */
    public List<Map<String, Object>> getGroupRanking(String type, LocalDate startDate, LocalDate endDate) {
        List<Object[]> results;
        
        if ("messages".equals(type)) {
            results = dailyStatsRepository.getGroupRankingByMessages(startDate, endDate);
        } else if ("success_rate".equals(type)) {
            results = dailyStatsRepository.getGroupRankingBySuccessRate(startDate, endDate);
        } else {
            throw new IllegalArgumentException("不支持的排行榜类型: " + type);
        }
        
        return results.stream().map(result -> {
            Map<String, Object> item = new HashMap<>();
            item.put("chatRoom", result[0]);
            item.put("value", result[1]);
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 获取每日趋势数据
     */
    public List<Map<String, Object>> getDailyTrends(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = dailyStatsRepository.getDailyTrends(startDate, endDate);
        
        return results.stream().map(result -> {
            Map<String, Object> item = new HashMap<>();
            item.put("date", result[0]);
            item.put("messages", result[1]);
            item.put("autoReplies", result[2]);
            item.put("takeovers", result[3]);
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 查找需要关注的群组
     */
    public List<GroupManagementStatus> getGroupsNeedingAttention(Integer threshold) {
        if (threshold == null) {
            threshold = 5; // 默认阈值
        }
        return groupStatusRepository.findGroupsNeedingAttention(threshold, GroupManagementStatus.GroupStatus.AUTO);
    }

    /**
     * 批量更新群组状态
     */
    @Transactional
    public int batchUpdateGroupStatus(List<String> chatRooms, GroupManagementStatus.GroupStatus status) {
        return groupStatusRepository.batchUpdateStatus(chatRooms, status);
    }

    /**
     * 重置每日计数器（定时任务使用）
     */
    @Transactional
    public void resetDailyCounters() {
        log.info("重置群组每日计数器");
        groupStatusRepository.resetDailyCounters();
    }

    /**
     * 清理历史数据
     */
    @Transactional
    public void cleanupHistoryData(int daysToKeep) {
        LocalDate cutoffDate = LocalDate.now().minusDays(daysToKeep);
        log.info("清理{}之前的历史统计数据", cutoffDate);
        dailyStatsRepository.deleteByStatDateBefore(cutoffDate);
    }
    
    /**
     * 删除群组
     * @param chatRoom 群组标识
     * @return 是否删除成功
     */
    @Transactional
    public boolean deleteGroup(String chatRoom) {
        Optional<GroupManagementStatus> groupOpt = groupStatusRepository.findByChatRoom(chatRoom);
        
        if (groupOpt.isPresent()) {
            GroupManagementStatus group = groupOpt.get();
            // 删除群组状态
            groupStatusRepository.delete(group);
            // 删除群组相关的统计数据
            dailyStatsRepository.deleteByChatRoom(chatRoom);
            
            log.info("群组删除成功: {}", chatRoom);
            return true;
        } else {
            log.warn("尝试删除不存在的群组: {}", chatRoom);
            return false;
        }
    }

    /**
     * 获取群组设置
     */
    public Map<String, Object> getGroupSettings() {
        Map<String, Object> settings = new HashMap<>();
        
        // 基础设置
        settings.put("defaultGroupStatus", "AUTO");
        settings.put("autoReplyEnabled", true);
        settings.put("keywordMonitoringEnabled", true);
        
        // 自动回复配置
        Map<String, Object> autoReplyConfig = new HashMap<>();
        autoReplyConfig.put("template", "感谢您的消息，我们会尽快回复您。");
        autoReplyConfig.put("delay", 2);
        autoReplyConfig.put("dailyLimit", 50);
        autoReplyConfig.put("smartReplyEnabled", true);
        settings.put("autoReplyConfig", autoReplyConfig);
        
        // 监控设置
        Map<String, Object> monitoringConfig = new HashMap<>();
        monitoringConfig.put("frequencyAlerts", true);
        monitoringConfig.put("keywordAlerts", true);
        monitoringConfig.put("notificationMethods", Arrays.asList("email", "sms"));
        settings.put("monitoringConfig", monitoringConfig);
        
        // 高级设置
        Map<String, Object> advancedConfig = new HashMap<>();
        advancedConfig.put("dataRetentionDays", 90);
        advancedConfig.put("autoCleanupEnabled", true);
        advancedConfig.put("apiRateLimit", 1000);
        advancedConfig.put("debugMode", false);
        settings.put("advancedConfig", advancedConfig);
        
        return settings;
    }

    /**
     * 更新群组设置
     */
    @Transactional
    public Map<String, Object> updateGroupSettings(Map<String, Object> settings) {
        // 这里可以将设置保存到数据库或配置文件
        // 目前返回更新后的设置
        log.info("更新群组设置: {}", settings);
        return settings;
    }

    /**
     * 更新群组配置
     */
    @Transactional
    public Map<String, Object> updateGroupConfig(String chatRoom, Map<String, Object> config) {
        log.info("更新群组配置: chatRoom={}, config={}", chatRoom, config);
        
        // 查找群组
        GroupManagementStatus group = groupStatusRepository.findByChatRoom(chatRoom)
                .orElseThrow(() -> new RuntimeException("群组不存在: " + chatRoom));
        
        // 更新群组配置
        if (config.containsKey("currentStatus")) {
            String statusStr = (String) config.get("currentStatus");
            group.setCurrentStatus(GroupManagementStatus.GroupStatus.valueOf(statusStr));
        }
        
        if (config.containsKey("autoReplyEnabled")) {
            Boolean autoReply = (Boolean) config.get("autoReplyEnabled");
            group.setAutoReplyEnabled(autoReply);
        }
        
        if (config.containsKey("keywordMonitoring")) {
            // 这里可以添加关键词监控的逻辑
            log.info("更新关键词监控设置: {}", config.get("keywordMonitoring"));
        }
        
        if (config.containsKey("processingRule")) {
            // 这里可以添加处理规则的逻辑
            log.info("更新处理规则: {}", config.get("processingRule"));
        }
        
        if (config.containsKey("gridOfficerId")) {
            String gridOfficerId = (String) config.get("gridOfficerId");
            group.setGridOfficerId(gridOfficerId);
            log.info("更新网格员ID: {}", gridOfficerId);
        }
        
        // 保存更新
        GroupManagementStatus updatedGroup = groupStatusRepository.save(group);
        
        // 返回更新后的配置
        Map<String, Object> result = new HashMap<>();
        result.put("groupId", updatedGroup.getChatRoom());
        result.put("status", updatedGroup.getCurrentStatus().name());
        result.put("autoReply", updatedGroup.getAutoReplyEnabled());
        result.put("keywordMonitoring", true); // 默认值
        result.put("processingRule", "default"); // 默认值
        result.put("gridOfficerId", updatedGroup.getGridOfficerId());
        
        return result;
    }

    /**
     * 获取群组统计概览
     */
    public Map<String, Object> getStatisticsOverview(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        Map<String, Object> overview = new HashMap<>();
        
        // 总群组数
        long totalGroups = groupStatusRepository.count();
        overview.put("totalGroups", totalGroups);
        
        // 活跃群组数
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        long activeGroups = groupStatusRepository.countActiveGroups(since);
        overview.put("activeGroups", activeGroups);
        
        // 获取统计数据
        Object[] stats = dailyStatsRepository.getOverallStatistics(startDate, endDate);
        if (stats != null && stats.length >= 3) {
            overview.put("totalMessages", stats[0] != null ? stats[0] : 0);
            overview.put("totalAutoReplies", stats[1] != null ? stats[1] : 0);
            overview.put("totalTakeovers", stats[2] != null ? stats[2] : 0);
        } else {
            overview.put("totalMessages", 0);
            overview.put("totalAutoReplies", 0);
            overview.put("totalTakeovers", 0);
        }
        
        // 状态分布
        Map<String, Long> statusDistribution = getGroupStatusStatistics();
        overview.put("statusDistribution", statusDistribution);
        
        return overview;
    }

    /**
     * 获取群组活动排行
     */
    public List<Map<String, Object>> getActivityRanking(int days, int limit) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        List<Object[]> results = dailyStatsRepository.getGroupRankingByMessages(startDate, endDate);
        
        return results.stream()
                .limit(limit)
                .map(result -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("chatRoom", result[0]);
                    item.put("groupName", getGroupNameByChatRoom((String) result[0]));
                    item.put("messageCount", result[1]);
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取关键词命中统计
     */
    public List<Map<String, Object>> getKeywordHitStatistics(int days) {
        try {
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days);
            
            // 从关键词使用统计表获取真实数据
            String sql = "SELECT k.keyword, SUM(s.hit_count) as total_hits, " +
                        "SUM(s.trigger_count) as total_triggers " +
                        "FROM keyword_configs k " +
                        "LEFT JOIN keyword_usage_stats s ON k.id = s.keyword_id " +
                        "WHERE s.stat_date BETWEEN ? AND ? " +
                        "AND k.is_active = true " +
                        "GROUP BY k.id, k.keyword " +
                        "ORDER BY total_hits DESC " +
                        "LIMIT 10";
            
            List<Object[]> results = entityManager.createNativeQuery(sql)
                    .setParameter(1, startDate)
                    .setParameter(2, endDate)
                    .getResultList();
            
            List<Map<String, Object>> keywordStats = new ArrayList<>();
            
            for (Object[] result : results) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("keyword", result[0]);
                stat.put("hitCount", result[1] != null ? ((Number) result[1]).intValue() : 0);
                stat.put("triggerCount", result[2] != null ? ((Number) result[2]).intValue() : 0);
                
                // 计算趋势（简单实现：比较最近3天和前3天的数据）
                String trendSql = "SELECT SUM(hit_count) FROM keyword_usage_stats " +
                                "WHERE keyword_id = (SELECT id FROM keyword_configs WHERE keyword = ?) " +
                                "AND stat_date BETWEEN ? AND ?";
                
                LocalDate midDate = startDate.plusDays(days / 2);
                
                // 前半期数据
                Integer earlyHits = (Integer) entityManager.createNativeQuery(trendSql)
                        .setParameter(1, result[0])
                        .setParameter(2, startDate)
                        .setParameter(3, midDate)
                        .getSingleResult();
                
                // 后半期数据
                Integer lateHits = (Integer) entityManager.createNativeQuery(trendSql)
                        .setParameter(1, result[0])
                        .setParameter(2, midDate.plusDays(1))
                        .setParameter(3, endDate)
                        .getSingleResult();
                
                earlyHits = earlyHits != null ? earlyHits : 0;
                lateHits = lateHits != null ? lateHits : 0;
                
                String trend = "stable";
                if (lateHits > earlyHits * 1.1) {
                    trend = "up";
                } else if (lateHits < earlyHits * 0.9) {
                    trend = "down";
                }
                
                stat.put("trend", trend);
                keywordStats.add(stat);
            }
            
            return keywordStats;
            
        } catch (Exception e) {
            log.error("获取关键词命中统计失败", e);
            // 降级处理：返回空列表而不是模拟数据
            return new ArrayList<>();
        }
    }

    /**
     * 根据chatRoom获取群组名称
     */
    private String getGroupNameByChatRoom(String chatRoom) {
        return groupStatusRepository.findByChatRoom(chatRoom)
                .map(GroupManagementStatus::getGroupName)
                .orElse("未知群组");
    }

    /**
     * 获取网格员列表
     */
    public List<Map<String, Object>> getGridOfficers() {
        // 从用户表中获取角色为'grid'的用户
        List<User> gridUsers = userRepository.findByRole("grid");
        
        List<Map<String, Object>> gridOfficers = new ArrayList<>();
        
        for (User user : gridUsers) {
            // 只返回状态为ACTIVE的网格员
            if (user.getStatus() == User.UserStatus.ACTIVE) {
                Map<String, Object> officer = new HashMap<>();
                officer.put("id", user.getId().toString());
                officer.put("officerName", user.getUsername()); // 显示用户名
                officer.put("officerCode", user.getUsername()); // 使用用户名作为网格员编号
                officer.put("phone", user.getPhone());
                officer.put("wechat", user.getWechatName());
                officer.put("gridArea", user.getGridArea());
                officer.put("isActive", true);
                gridOfficers.add(officer);
            }
        }
        
        return gridOfficers;
    }

    /**
     * 分配网格员到群组
     */
    public void assignGridOfficerToGroup(String chatRoom, String officerId, String operator) {
        Optional<GroupManagementStatus> groupOpt = groupStatusRepository.findByChatRoom(chatRoom);
        if (groupOpt.isPresent()) {
            GroupManagementStatus group = groupOpt.get();
            group.setGridOfficerId(officerId);
            group.setUpdatedTime(LocalDateTime.now());
            groupStatusRepository.save(group);
            
            log.info("网格员分配成功: 群组[{}] 分配给网格员[{}], 操作人[{}]", chatRoom, officerId, operator);
        } else {
            throw new RuntimeException("群组不存在: " + chatRoom);
        }
    }
}