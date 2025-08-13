package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.GroupDailyStatistics;
import com.dianxiaozhu.backend.entity.GroupManagementStatus;
import com.dianxiaozhu.backend.repository.GroupDailyStatisticsRepository;
import com.dianxiaozhu.backend.repository.GroupManagementStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        
        if (result != null) {
            statistics.put("totalMessages", result[0] != null ? result[0] : 0);
            statistics.put("totalAutoReplies", result[1] != null ? result[1] : 0);
            statistics.put("totalTakeovers", result[2] != null ? result[2] : 0);
            statistics.put("avgResponseTime", result[3] != null ? result[3] : BigDecimal.ZERO);
            statistics.put("avgSatisfaction", result[4] != null ? result[4] : BigDecimal.ZERO);
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
}