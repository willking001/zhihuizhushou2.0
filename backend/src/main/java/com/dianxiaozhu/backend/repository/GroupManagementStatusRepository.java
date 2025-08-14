package com.dianxiaozhu.backend.repository;

import com.dianxiaozhu.backend.entity.GroupManagementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 群组管理状态Repository
 */
@Repository
public interface GroupManagementStatusRepository extends JpaRepository<GroupManagementStatus, Long> {

    /**
     * 根据群组标识查找状态
     */
    Optional<GroupManagementStatus> findByChatRoom(String chatRoom);

    /**
     * 根据当前状态查找群组列表
     */
    List<GroupManagementStatus> findByCurrentStatus(GroupManagementStatus.GroupStatus currentStatus);

    /**
     * 查找活跃的群组（最近有消息活动）
     */
    @Query("SELECT g FROM GroupManagementStatus g WHERE g.lastActivityTime >= :since ORDER BY g.lastActivityTime DESC")
    List<GroupManagementStatus> findActiveGroups(@Param("since") LocalDateTime since);

    /**
     * 统计活跃群组数量
     */
    @Query("SELECT COUNT(g) FROM GroupManagementStatus g WHERE g.lastActivityTime >= :since")
    long countActiveGroups(@Param("since") LocalDateTime since);

    /**
     * 统计各状态的群组数量
     */
    @Query("SELECT g.currentStatus, COUNT(g) FROM GroupManagementStatus g GROUP BY g.currentStatus")
    List<Object[]> countByStatus();

    /**
     * 更新群组今日消息数
     */
    @Modifying
    @Query("UPDATE GroupManagementStatus g SET g.messageCountToday = g.messageCountToday + 1, g.lastActivityTime = :now WHERE g.chatRoom = :chatRoom")
    int incrementMessageCount(@Param("chatRoom") String chatRoom, @Param("now") LocalDateTime now);

    /**
     * 更新群组今日自动回复数
     */
    @Modifying
    @Query("UPDATE GroupManagementStatus g SET g.autoReplyCountToday = g.autoReplyCountToday + 1 WHERE g.chatRoom = :chatRoom")
    int incrementAutoReplyCount(@Param("chatRoom") String chatRoom);

    /**
     * 更新群组今日接管次数
     */
    @Modifying
    @Query("UPDATE GroupManagementStatus g SET g.takeoverCountToday = g.takeoverCountToday + 1, g.takeoverTime = :takeoverTime, g.takeoverBy = :takeoverBy, g.takeoverReason = :reason WHERE g.chatRoom = :chatRoom")
    int incrementTakeoverCount(@Param("chatRoom") String chatRoom, 
                              @Param("takeoverTime") LocalDateTime takeoverTime,
                              @Param("takeoverBy") String takeoverBy,
                              @Param("reason") String reason);

    /**
     * 重置所有群组的今日统计数据（用于每日定时任务）
     */
    @Modifying
    @Query("UPDATE GroupManagementStatus g SET g.messageCountToday = 0, g.autoReplyCountToday = 0, g.takeoverCountToday = 0")
    int resetDailyCounters();

    /**
     * 查找需要人工接管的群组（基于接管次数阈值）
     */
    @Query("SELECT g FROM GroupManagementStatus g WHERE g.takeoverCountToday >= :threshold AND g.currentStatus = :status")
    List<GroupManagementStatus> findGroupsNeedingAttention(@Param("threshold") Integer threshold, @Param("status") GroupManagementStatus.GroupStatus status);

    /**
     * 根据群组名称模糊查询
     */
    List<GroupManagementStatus> findByGroupNameContaining(String groupName);

    /**
     * 查找启用自动回复的群组
     */
    List<GroupManagementStatus> findByAutoReplyEnabledTrue();

    /**
     * 查找启用自动转发的群组
     */
    List<GroupManagementStatus> findByAutoForwardEnabledTrue();

    /**
     * 批量更新群组状态
     */
    @Modifying
    @Query("UPDATE GroupManagementStatus g SET g.currentStatus = :status WHERE g.chatRoom IN :chatRooms")
    int batchUpdateStatus(@Param("chatRooms") List<String> chatRooms, @Param("status") GroupManagementStatus.GroupStatus status);
}