package com.icampus.domain.entity;

import java.time.LocalDateTime;

/**
 * 知识库条目实体（纯 POJO，零框架依赖）
 */
public class KnowledgeBase {

    private Long id;
    private String question;
    private String answer;
    private String category;
    private String keywords;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public KnowledgeBase() {}

    // ========== getter/setter ==========

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
