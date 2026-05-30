package com.icampus.domain.entity;

import java.time.LocalDateTime;

/**
 * 用户贡献实体（纯 POJO，零框架依赖）
 */
public class Contribution {

    private Long id;
    private Long userId;
    private String question;
    private String answer;
    private String status; // pending / approved / rejected
    private String auditReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Contribution() {}

    // ========== getter/setter ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAuditReason() { return auditReason; }
    public void setAuditReason(String auditReason) { this.auditReason = auditReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
