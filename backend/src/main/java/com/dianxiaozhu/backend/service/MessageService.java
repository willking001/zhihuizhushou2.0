package com.dianxiaozhu.backend.service;

import com.dianxiaozhu.backend.entity.Message;
import com.dianxiaozhu.backend.entity.KeywordConfig;
import com.dianxiaozhu.backend.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessageService.class);
    
    private final MessageRepository messageRepository;
    private final KeywordConfigService keywordConfigService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    /**
     * 保存消息
     */
    @Transactional
    public Message saveMessage(Message message) {
        log.info("保存消息: 发送者={}, 内容长度={}", message.getSenderName(), 
                message.getContent() != null ? message.getContent().length() : 0);
        
        // 检查是否需要转发
        checkAndMarkForForwarding(message);
        
        Message savedMessage = messageRepository.save(message);
        
        // 如果需要转发，发送到Kafka
        if (savedMessage.getIsForwarded()) {
            sendToKafka(savedMessage);
        }
        
        return savedMessage;
    }
    
    /**
     * 检查消息是否需要转发
     */
    private void checkAndMarkForForwarding(Message message) {
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            return;
        }
        
        String content = message.getContent().toLowerCase();
        List<KeywordConfig> activeKeywords = keywordConfigService.getActiveKeywords();
        
        List<String> matchedKeywords = activeKeywords.stream()
                .filter(keyword -> content.contains(keyword.getKeyword().toLowerCase()))
                .map(KeywordConfig::getKeyword)
                .collect(Collectors.toList());
        
        if (!matchedKeywords.isEmpty()) {
            message.setIsForwarded(true);
            message.setKeywordsMatched(String.join(",", matchedKeywords));
            message.setForwardReason("关键词匹配: " + String.join(",", matchedKeywords));
            
            // 设置优先级
            boolean hasHighPriorityKeyword = activeKeywords.stream()
                    .filter(keyword -> matchedKeywords.contains(keyword.getKeyword()))
                    .anyMatch(keyword -> keyword.getPriority() == KeywordConfig.Priority.HIGH || 
                                       keyword.getPriority() == KeywordConfig.Priority.URGENT);
            
            if (hasHighPriorityKeyword) {
                message.setPriority(Message.Priority.HIGH);
            }
            
            log.info("消息标记为转发: 匹配关键词={}", matchedKeywords);
        }
    }
    
    /**
     * 发送消息到Kafka
     */
    private void sendToKafka(Message message) {
        try {
            kafkaTemplate.send("message-forward", message);
            log.info("消息已发送到Kafka: messageId={}", message.getId());
        } catch (Exception e) {
            log.error("发送消息到Kafka失败: messageId={}", message.getId(), e);
        }
    }
    
    /**
     * 根据用户ID获取消息
     */
    public List<Message> getMessagesByUserId(Long userId) {
        return messageRepository.findByGridUserId(userId);
    }

    /**
     * 分页查询消息
     */
    public org.springframework.data.domain.Page<Message> findMessages(org.springframework.data.domain.Pageable pageable) {
        return messageRepository.findAll(pageable);
    }

    /**
     * 根据ID查找消息
     */
    public Optional<Message> findMessageById(Long id) {
        return messageRepository.findById(id);
    }

    /**
     * 获取消息统计数据
     */
    public java.util.Map<String, Object> getAnalysis() {
        java.util.Map<String, Object> analysis = new java.util.HashMap<>();
        analysis.put("totalMessages", messageRepository.countTotalMessages());
        analysis.put("todayMessages", messageRepository.countTodayMessages());
        analysis.put("forwardedMessages", messageRepository.countForwardedMessages());
        return analysis;
    }


    
    /**
     * 获取今日消息
     */
    public List<Message> getTodayMessages() {
        return messageRepository.findTodayMessages();
    }
    
    /**
     * 获取消息统计
     */
    public MessageStats getMessageStats() {
        Long totalMessages = messageRepository.countTotalMessages();
        Long todayMessages = messageRepository.countTodayMessages();
        Long forwardedMessages = messageRepository.countForwardedMessages();
        
        return new MessageStats(totalMessages, todayMessages, forwardedMessages);
    }
    
    /**
     * 根据用户ID获取今日消息统计
     */
    public MessageStats getMessageStatsByUser(Long userId) {
        Long todayMessages = messageRepository.countTodayMessagesByUser(userId);
        List<Message> userMessages = messageRepository.findByGridUserId(userId);
        Long totalMessages = (long) userMessages.size();
        Long forwardedMessages = userMessages.stream()
                .mapToLong(msg -> msg.getIsForwarded() ? 1 : 0)
                .sum();
        
        return new MessageStats(totalMessages, todayMessages, forwardedMessages);
    }
    
    /**
     * 根据关键词搜索消息
     */
    public List<Message> searchMessagesByKeyword(String keyword) {
        return messageRepository.findByContentContaining(keyword);
    }
    
    /**
     * 获取最近消息
     */
    public List<Message> getRecentMessages(int limit) {
        List<Message> messages = messageRepository.findRecentMessages();
        return messages.stream().limit(limit).collect(Collectors.toList());
    }
    
    /**
     * 根据时间范围获取消息
     */
    public List<Message> getMessagesByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return messageRepository.findByReceivedAtBetween(startTime, endTime);
    }
    
    /**
     * 根据ID获取消息
     */
    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }
    
    /**
     * 删除消息
     */
    @Transactional
    public void deleteMessage(Long id) {
        if (messageRepository.existsById(id)) {
            messageRepository.deleteById(id);
            log.info("消息删除成功: {}", id);
        } else {
            throw new RuntimeException("消息不存在: " + id);
        }
    }
    
    /**
     * 消息统计数据类
     */
    public static class MessageStats {
        private final Long totalMessages;
        private final Long todayMessages;
        private final Long forwardedMessages;
        
        public MessageStats(Long totalMessages, Long todayMessages, Long forwardedMessages) {
            this.totalMessages = totalMessages != null ? totalMessages : 0L;
            this.todayMessages = todayMessages != null ? todayMessages : 0L;
            this.forwardedMessages = forwardedMessages != null ? forwardedMessages : 0L;
        }
        
        public Long getTotalMessages() { return totalMessages; }
        public Long getTodayMessages() { return todayMessages; }
        public Long getForwardedMessages() { return forwardedMessages; }
    }
}