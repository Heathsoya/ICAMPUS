package com.icampus.domain.entity;

import java.time.LocalDateTime;

/**
 * 答案反馈实体（纯 POJO，零框架依赖）
 */
public class AnswerFeedback {

    private Long id;
    private Long questionLogId;
    private Long userId;
    private Boolean helpful;
    private String comment;
    private LocalDateTime createdAt;

    public AnswerFeedback() {}

    // ========== getter/setter ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getQuestionLogId() { return questionLogId; }
    public void setQuestionLogId(Long questionLogId) { this.questionLogId = questionLogId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Boolean getHelpful() { return helpful; }
    public void setHelpful(Boolean helpful) { this.helpful = helpful; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
