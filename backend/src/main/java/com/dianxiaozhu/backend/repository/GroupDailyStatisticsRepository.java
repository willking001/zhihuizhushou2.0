package com.dianxiaozhu.backend.repository;

import com.dianxiaozhu.backend.entity.GroupDailyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 群组日统计Repository
 */
@Repository
public interface GroupDailyStatisticsRepository extends JpaRepository<GroupDailyStatistics, Long> {

    /**
     * 根据群组和日期查找统计记录
     */
    Optional<GroupDailyStatistics> findByChatRoomAndStatDate(String chatRoom, LocalDate statDate);

    /**
     * 查找指定群组的历史统计数据
     */
    List<GroupDailyStatistics> findByChatRoomAndStatDateBetweenOrderByStatDateDesc(
            String chatRoom, LocalDate startDate, LocalDate endDate);

    /**
     * 查找指定日期范围内的所有统计数据
     */
    List<GroupDailyStatistics> findByStatDateBetweenOrderByStatDateDesc(
            LocalDate startDate, LocalDate endDate);

    /**
     * 统计指定日期范围内的总体数据
     */
    @Query("SELECT " +
           "SUM(g.messageCount) as totalMessages, " +
           "SUM(g.autoReplyCount) as totalAutoReplies, " +
           "SUM(g.manualTakeoverCount) as totalTakeovers, " +
           "AVG(g.avgResponseTime) as avgResponseTime, " +
           "AVG(g.satisfactionScore) as avgSatisfaction " +
           "FROM GroupDailyStatistics g " +
           "WHERE g.statDate BETWEEN :startDate AND :endDate")
    Object[] getOverallStatistics(@Param("startDate") LocalDate startDate, 
                                 @Param("endDate") LocalDate endDate);

    /**
     * 获取群组排行榜（按消息数排序）
     */
    @Query("SELECT g.chatRoom, SUM(g.messageCount) as totalMessages " +
           "FROM GroupDailyStatistics g " +
           "WHERE g.statDate BETWEEN :startDate AND :endDate " +
           "GROUP BY g.chatRoom " +
           "ORDER BY totalMessages DESC")
    List<Object[]> getGroupRankingByMessages(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);

    /**
     * 获取群组排行榜（按自动回复成功率排序）
     */
    @Query("SELECT g.chatRoom, " +
           "SUM(g.autoReplyCount) * 100.0 / SUM(g.messageCount) as successRate " +
           "FROM GroupDailyStatistics g " +
           "WHERE g.statDate BETWEEN :startDate AND :endDate " +
           "AND g.messageCount > 0 " +
           "GROUP BY g.chatRoom " +
           "ORDER BY successRate DESC")
    List<Object[]> getGroupRankingBySuccessRate(@Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);

    /**
     * 获取每日趋势数据
     */
    @Query("SELECT g.statDate, " +
           "SUM(g.messageCount) as dailyMessages, " +
           "SUM(g.autoReplyCount) as dailyAutoReplies, " +
           "SUM(g.manualTakeoverCount) as dailyTakeovers " +
           "FROM GroupDailyStatistics g " +
           "WHERE g.statDate BETWEEN :startDate AND :endDate " +
           "GROUP BY g.statDate " +
           "ORDER BY g.statDate ASC")
    List<Object[]> getDailyTrends(@Param("startDate") LocalDate startDate, 
                                 @Param("endDate") LocalDate endDate);

    /**
     * 查找表现异常的群组（接管率过高）
     */
    @Query("SELECT g.chatRoom, " +
           "(SUM(g.manualTakeoverCount) * 100.0 / SUM(g.messageCount)) as takeoverRate " +
           "FROM GroupDailyStatistics g " +
           "WHERE g.statDate BETWEEN :startDate AND :endDate " +
           "AND g.messageCount > :minMessages " +
           "GROUP BY g.chatRoom " +
           "HAVING (SUM(g.manualTakeoverCount) * 100.0 / SUM(g.messageCount)) > :threshold " +
           "ORDER BY (SUM(g.manualTakeoverCount) * 100.0 / SUM(g.messageCount)) DESC")
    List<Object[]> findAbnormalGroups(@Param("startDate") LocalDate startDate, 
                                     @Param("endDate") LocalDate endDate,
                                     @Param("minMessages") Integer minMessages,
                                     @Param("threshold") Double threshold);

    /**
     * 删除指定日期之前的历史数据
     */
    void deleteByStatDateBefore(LocalDate date);

    /**
     * 统计指定群组的总体表现
     */
    @Query("SELECT " +
           "COUNT(g) as totalDays, " +
           "SUM(g.messageCount) as totalMessages, " +
           "SUM(g.autoReplyCount) as totalAutoReplies, " +
           "AVG(g.avgResponseTime) as avgResponseTime, " +
           "AVG(g.satisfactionScore) as avgSatisfaction " +
           "FROM GroupDailyStatistics g " +
           "WHERE g.chatRoom = :chatRoom " +
           "AND g.statDate BETWEEN :startDate AND :endDate")
    Object[] getGroupPerformance(@Param("chatRoom") String chatRoom,
                                @Param("startDate") LocalDate startDate, 
                                @Param("endDate") LocalDate endDate);
}