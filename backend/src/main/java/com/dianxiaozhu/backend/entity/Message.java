package com.dianxiaozhu.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "sender_name")
    private String senderName;

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    
    @Column(name = "sender_id")
    private String senderId;
    
    @Column(columnDefinition = "TEXT")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    @Enumerated(EnumType.STRING)
    private MessageType type = MessageType.TEXT;
    
    @Column(name = "chat_room")
    private String chatRoom;
    
    @Column(name = "is_group")
    private Boolean isGroup = false;
    
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
    
    @Column(name = "grid_user_id")
    private Long gridUserId;
    
    @Column(name = "is_forwarded")
    private Boolean isForwarded = false;

    public void setIsForwarded(Boolean isForwarded) {
        this.isForwarded = isForwarded;
    }
    
    @Column(name = "forward_reason")
    private String forwardReason;

    public void setForwardReason(String forwardReason) {
        this.forwardReason = forwardReason;
    }

    public String getForwardReason() {
        return forwardReason;
    }
    
    @Column(name = "keywords_matched")
    private String keywordsMatched;

    public String getKeywordsMatched() {
        return keywordsMatched;
    }

    public void setKeywordsMatched(String keywordsMatched) {
        this.keywordsMatched = keywordsMatched;
    }
    
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.NORMAL;
    
    // 群组管理相关字段
    @Column(name = "group_status")
    @Enumerated(EnumType.STRING)
    private GroupStatus groupStatus;
    
    @Column(name = "is_auto_reply")
    private Boolean isAutoReply = false;
    
    @Column(name = "takeover_trigger")
    private String takeoverTrigger;
    
    @Column(name = "response_time_seconds")
    private Integer responseTimeSeconds;
    
    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }
    
    public enum MessageType {
        TEXT, IMAGE, VOICE, VIDEO, FILE, LOCATION, LINK
    }
    
    public enum Priority {
        LOW, NORMAL, HIGH, URGENT
    }
    
    public enum GroupStatus {
        AUTO, MANUAL, PAUSED
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setChatRoom(String chatRoom) {
        this.chatRoom = chatRoom;
    }

    public void setIsGroup(Boolean isGroup) {
        this.isGroup = isGroup;
    }

    public void setGridUserId(Long gridUserId) {
        this.gridUserId = gridUserId;
    }

    public void setReceivedAt(java.time.LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public Long getId() {
        return id;
    }

    public Boolean getIsForwarded() {
        return isForwarded;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}