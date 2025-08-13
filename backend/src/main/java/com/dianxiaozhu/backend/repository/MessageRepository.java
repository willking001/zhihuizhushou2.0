package com.dianxiaozhu.backend.repository;

import com.dianxiaozhu.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    /**
     * 根据网格用户ID查找消息
     */
    List<Message> findByGridUserId(Long gridUserId);
    
    /**
     * 根据发送者查找消息
     */
    List<Message> findBySenderName(String senderName);
    
    /**
     * 查找已转发的消息
     */
    List<Message> findByIsForwarded(Boolean isForwarded);
    
    /**
     * 根据优先级查找消息
     */
    List<Message> findByPriority(Message.Priority priority);
    
    /**
     * 根据时间范围查找消息
     */
    @Query("SELECT m FROM Message m WHERE m.receivedAt BETWEEN :startTime AND :endTime")
    List<Message> findByReceivedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                         @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找今日消息
     */
    @Query("SELECT m FROM Message m WHERE DATE(m.receivedAt) = CURRENT_DATE")
    List<Message> findTodayMessages();
    
    /**
     * 统计总消息数
     */
    @Query("SELECT COUNT(m) FROM Message m")
    Long countTotalMessages();
    
    /**
     * 统计今日消息数
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE DATE(m.receivedAt) = CURRENT_DATE")
    Long countTodayMessages();
    
    /**
     * 统计转发消息数
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.isForwarded = true")
    Long countForwardedMessages();
    
    /**
     * 根据网格用户ID统计今日消息数
     */
    @Query("SELECT COUNT(m) FROM Message m WHERE m.gridUserId = :userId AND DATE(m.receivedAt) = CURRENT_DATE")
    Long countTodayMessagesByUser(@Param("userId") Long userId);
    
    /**
     * 根据关键词查找消息
     */
    @Query("SELECT m FROM Message m WHERE m.content LIKE %:keyword%")
    List<Message> findByContentContaining(@Param("keyword") String keyword);
    
    /**
     * 查找最近的消息（限制数量）
     */
    @Query("SELECT m FROM Message m ORDER BY m.receivedAt DESC")
    List<Message> findRecentMessages();
}